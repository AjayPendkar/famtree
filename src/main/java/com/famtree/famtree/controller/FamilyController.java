package com.famtree.famtree.controller;

import com.famtree.famtree.dto.ApiResponse;
import com.famtree.famtree.dto.FamilyResponse;
import com.famtree.famtree.dto.FamilyHeadResponse;
import com.famtree.famtree.service.FamilyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import com.famtree.famtree.dto.FamilyDetailsRequest;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/families")
@RequiredArgsConstructor
public class FamilyController {
    private final FamilyService familyService;

    @PostMapping("/complete-registration")
    public ResponseEntity<ApiResponse<?>> completeFamilyRegistration(
            @RequestHeader("Authorization") String token,
            @RequestBody FamilyDetailsRequest request) {
        try {
            FamilyResponse response = familyService.completeFamilyRegistration(token, request);
            if (response.getFamilyHead() != null && response.getFamilyHead().isVerified() && response.getFamilyUid() == null) {
                // Return 200 with just a message for existing registrations
                return ResponseEntity.ok(ApiResponse.builder()
                    .status(HttpStatus.OK.value())
                    .message("User already registered as family head")
                    .data(null)
                    .build());
            }
            return ResponseEntity.ok(ApiResponse.success(response, "Family registration completed successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity
                .badRequest()
                .body(ApiResponse.error(e.getMessage(), HttpStatus.BAD_REQUEST));
        }
    }

    @GetMapping("/heads")
    public ResponseEntity<ApiResponse<?>> getAllFamilyHeads() {
        List<FamilyHeadResponse> heads = familyService.getAllFamilyHeads();
        return ResponseEntity.ok(ApiResponse.success(heads, "Family heads retrieved successfully"));
    }

    @PutMapping("/update-profile")
    public ResponseEntity<ApiResponse<?>> updateFamilyProfile(
            @RequestHeader("Authorization") String token,
            @RequestBody FamilyDetailsRequest request) {
        try {
            FamilyResponse response = familyService.updateFamilyProfile(token, request);
            return ResponseEntity.ok(ApiResponse.success(response, "Family profile updated successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity
                .badRequest()
                .body(ApiResponse.error(e.getMessage(), HttpStatus.BAD_REQUEST));
        }
    }

    @PutMapping("/update-complete-registration")
    public ResponseEntity<ApiResponse<?>> updateCompleteRegistration(
            @RequestHeader("Authorization") String token,
            @RequestBody FamilyDetailsRequest request) {
        try {
            FamilyResponse response = familyService.updateCompleteRegistration(token, request);
            return ResponseEntity.ok(ApiResponse.success(response, "Family registration updated successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity
                .badRequest()
                .body(ApiResponse.error(e.getMessage(), HttpStatus.BAD_REQUEST));
        }
    }
} 