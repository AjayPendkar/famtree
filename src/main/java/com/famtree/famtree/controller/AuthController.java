package com.famtree.famtree.controller;

import com.famtree.famtree.dto.ApiResponse;
import com.famtree.famtree.dto.AuthResponse;
import com.famtree.famtree.dto.InitialRegistrationRequest;
import com.famtree.famtree.dto.AdminAuthRequest;
import com.famtree.famtree.service.AuthService;
import com.famtree.famtree.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;
    private final UserRepository userRepository;

    @PostMapping("/admin/register")
    public ResponseEntity<ApiResponse<AuthResponse>> registerAdmin(@RequestBody AdminAuthRequest request) {
        try {
            AuthResponse response = authService.registerAdmin(request);
            return ResponseEntity.ok(ApiResponse.success(
                response,
                "Admin registered successfully"
            ));
        } catch (RuntimeException e) {
            return ResponseEntity
                .badRequest()
                .body(ApiResponse.error(e.getMessage(), HttpStatus.BAD_REQUEST));
        }
    }

    @PostMapping("/admin/login")
    public ResponseEntity<ApiResponse<AuthResponse>> loginAdmin(@RequestBody AdminAuthRequest request) {
        try {
            AuthResponse response = authService.loginAdmin(request);
            return ResponseEntity.ok(ApiResponse.success(
                response,
                "Admin logged in successfully"
            ));
        } catch (RuntimeException e) {
            return ResponseEntity
                .badRequest()
                .body(ApiResponse.error(e.getMessage(), HttpStatus.BAD_REQUEST));
        }
    }

    @PostMapping("/send-otp")
    public ResponseEntity<ApiResponse<AuthResponse>> sendOtp(@RequestBody InitialRegistrationRequest request) {
        try {
            AuthResponse response = authService.sendOtp(request);
            return ResponseEntity.ok(ApiResponse.success(
                response,
                "OTP sent successfully"
            ));
        } catch (RuntimeException e) {
            return ResponseEntity
                .badRequest()
                .body(ApiResponse.error(e.getMessage(), HttpStatus.BAD_REQUEST));
        }
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<ApiResponse<AuthResponse>> verifyOtp(@RequestBody Map<String, String> request) {
        try {
            AuthResponse response = authService.verifyOtp(request.get("mobile"), request.get("otp"));
            return response.isSuccess() 
                ? ResponseEntity.ok(ApiResponse.success(
                    response,
                    "OTP verified successfully"
                ))
                : ResponseEntity.badRequest().body(ApiResponse.error(response.getMessage(), HttpStatus.BAD_REQUEST));
        } catch (RuntimeException e) {
            return ResponseEntity
                .badRequest()
                .body(ApiResponse.error(e.getMessage(), HttpStatus.BAD_REQUEST));
        }
    }

    @GetMapping("/check-user/{mobile}")
    public ResponseEntity<?> checkUser(@PathVariable String mobile) {
        long count = userRepository.countByMobile(mobile);
        boolean exists = userRepository.findByMobile(mobile).isPresent();
        
        Map<String, Object> response = new HashMap<>();
        response.put("mobile", mobile);
        response.put("count", count);
        response.put("exists", exists);
        
        return ResponseEntity.ok(response);
    }
} 