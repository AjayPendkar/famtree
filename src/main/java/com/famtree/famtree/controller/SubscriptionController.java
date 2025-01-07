package com.famtree.famtree.controller;

import com.famtree.famtree.dto.ApiResponse;
import com.famtree.famtree.dto.SubscriptionDTO;
import com.famtree.famtree.service.SubscriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/subscription")
@RequiredArgsConstructor
public class SubscriptionController {
    private final SubscriptionService subscriptionService;

    @GetMapping("/details")
    public ResponseEntity<ApiResponse<?>> getSubscriptionDetails(@RequestParam String familyId) {
        try {
            SubscriptionDTO details = subscriptionService.getSubscriptionDetails(familyId);
            return ResponseEntity.ok(ApiResponse.success(details, "Subscription details retrieved successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error(e.getMessage(), HttpStatus.BAD_REQUEST));
        }
    }

    @PostMapping("/upgrade")
    public ResponseEntity<ApiResponse<?>> upgradeSubscription(@RequestBody Map<String, String> request) {
        try {
            subscriptionService.upgradeSubscription(
                request.get("familyId"),
                request.get("plan")
            );
            return ResponseEntity.ok(ApiResponse.success(null, "Subscription activated successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error(e.getMessage(), HttpStatus.BAD_REQUEST));
        }
    }
} 