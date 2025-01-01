package com.famtree.famtree.controller;

import com.famtree.famtree.dto.ApiResponse;
import com.famtree.famtree.dto.MemberRegistrationRequest;
import com.famtree.famtree.dto.MemberResponse;
import com.famtree.famtree.dto.MemberVerificationRequest;
import com.famtree.famtree.dto.PendingMemberResponse;
import com.famtree.famtree.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class MemberController {
    private final MemberService memberService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<?>> registerMember(
            @RequestBody MemberRegistrationRequest request) {
        MemberResponse response = memberService.registerMember(request);
        return ResponseEntity.ok(ApiResponse.success(response, "Member registration initiated"));
    }

    @PostMapping("/verify")
    public ResponseEntity<ApiResponse<?>> verifyMember(
            @RequestHeader("Authorization") String token,
            @RequestBody MemberVerificationRequest request) {
        MemberResponse response = memberService.verifyMember(token, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Member verified successfully"));
    }

    @GetMapping("/pending")
    public ResponseEntity<ApiResponse<?>> getPendingMembers(
            @RequestHeader("Authorization") String token) {
        List<PendingMemberResponse> pendingMembers = memberService.getPendingMembers(token);
        return ResponseEntity.ok(ApiResponse.success(pendingMembers, "Pending members retrieved"));
    }

    @PostMapping("/verify-family-code")
    public ResponseEntity<ApiResponse> verifyFamilyCode(
            @RequestHeader("Authorization") String token,
            @RequestBody MemberVerificationRequest request
    ) {
        try {
            System.out.println("Received request: " + request);
            System.out.println("Token: " + token);
            
            MemberResponse response = memberService.verifyFamilyCode(token, request);
            return ResponseEntity.ok(new ApiResponse(200, "Verification successful", response));
        } catch (Exception e) {
            System.out.println("Error in verifyFamilyCode: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse(500, "Error: " + e.getMessage(), null));
        }
    }
}