package com.famtree.famtree.controller;

import com.famtree.famtree.dto.ApiResponse;
import com.famtree.famtree.dto.HomePageResponse;
import com.famtree.famtree.service.HomeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/home")
@RequiredArgsConstructor
public class HomeController {
    private final HomeService homeService;

    @GetMapping
    public ResponseEntity<ApiResponse<?>> getHomePageData(
            @RequestHeader("Authorization") String token) {
        try {
            HomePageResponse response = homeService.getHomePageData(token);
            return ResponseEntity.ok(ApiResponse.success(response, "Home page data retrieved successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error(e.getMessage(), HttpStatus.BAD_REQUEST));
        }
    }
} 