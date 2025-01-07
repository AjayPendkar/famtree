package com.famtree.famtree.controller;

import com.famtree.famtree.dto.ApiResponse;
import com.famtree.famtree.dto.ConnectionRequestDTO;
import com.famtree.famtree.service.ConnectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/connections")
@RequiredArgsConstructor
public class ConnectionController {
    private final ConnectionService connectionService;

    @PostMapping("/request")
    public ResponseEntity<ApiResponse<?>> sendConnectionRequest(@RequestBody Map<String, String> request) {
        try {
            connectionService.sendConnectionRequest(
                request.get("requesterFamilyId"),
                request.get("receiverFamilyId"),
                request.get("message")
            );
            return ResponseEntity.ok(ApiResponse.success(null, "Request sent successfully"));
        } catch (RuntimeException e) {
            if ("REQUEST_LIMIT_EXCEEDED".equals(e.getMessage())) {
                return ResponseEntity.ok(ApiResponse.builder()
                    .status(302)  // Found/Redirect
                    .message("Please upgrade your subscription for more requests")
                    .data(Map.of("redirect", "/subscription"))
                    .build());
            }
            return ResponseEntity.badRequest()
                .body(ApiResponse.error(e.getMessage(), HttpStatus.BAD_REQUEST));
        }
    }

    @GetMapping("/requests")
    public ResponseEntity<ApiResponse<?>> getConnectionRequests(
            @RequestParam String familyId,
            @RequestParam String type,
            @RequestParam String status) {
        try {
            List<ConnectionRequestDTO> requests = connectionService.getConnectionRequests(familyId, type, status);
            return ResponseEntity.ok(ApiResponse.success(requests, "Requests retrieved successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error(e.getMessage(), HttpStatus.BAD_REQUEST));
        }
    }

    @PatchMapping("/requests/{id}")
    public ResponseEntity<ApiResponse<?>> updateRequestStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        try {
            connectionService.updateRequestStatus(id, request.get("status"));
            return ResponseEntity.ok(ApiResponse.success(null, "Request status updated"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error(e.getMessage(), HttpStatus.BAD_REQUEST));
        }
    }
} 