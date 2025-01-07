package com.famtree.famtree.controller;

import com.famtree.famtree.dto.ApiResponse;
import com.famtree.famtree.entity.Review;
import com.famtree.famtree.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/connections/reviews")
@RequiredArgsConstructor
public class ReviewController {
    private final ReviewService reviewService;

    @PostMapping
    public ResponseEntity<ApiResponse<?>> submitReview(@RequestBody Map<String, Object> request) {
        try {
            reviewService.submitReview(
                Long.parseLong(request.get("connectionId").toString()),
                request.get("reviewerFamilyId").toString(),
                Double.parseDouble(request.get("rating").toString()),
                request.get("review").toString()
            );
            return ResponseEntity.ok(ApiResponse.success(null, "Review submitted successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error(e.getMessage(), HttpStatus.BAD_REQUEST));
        }
    }

    @GetMapping("/family/{familyId}")
    public ResponseEntity<ApiResponse<?>> getFamilyReviews(@PathVariable String familyId) {
        try {
            List<Review> reviews = reviewService.getFamilyReviews(familyId);
            Double rating = reviewService.getFamilyRating(familyId);
            
            Map<String, Object> response = Map.of(
                "reviews", reviews,
                "averageRating", rating != null ? rating : 0.0
            );
            
            return ResponseEntity.ok(ApiResponse.success(response, "Reviews retrieved successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error(e.getMessage(), HttpStatus.BAD_REQUEST));
        }
    }

    @PutMapping("/{reviewId}")
    public ResponseEntity<ApiResponse<?>> updateReview(
            @PathVariable Long reviewId,
            @RequestBody Map<String, Object> request) {
        try {
            reviewService.updateReview(
                reviewId,
                request.get("reviewerFamilyId").toString(),
                Double.parseDouble(request.get("rating").toString()),
                request.get("review").toString()
            );
            return ResponseEntity.ok(ApiResponse.success(null, "Review updated successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error(e.getMessage(), HttpStatus.BAD_REQUEST));
        }
    }

    @DeleteMapping("/{reviewId}")
    public ResponseEntity<ApiResponse<?>> deleteReview(
            @PathVariable Long reviewId,
            @RequestParam String reviewerFamilyId) {
        try {
            reviewService.deleteReview(reviewId, reviewerFamilyId);
            return ResponseEntity.ok(ApiResponse.success(null, "Review deleted successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error(e.getMessage(), HttpStatus.BAD_REQUEST));
        }
    }
} 