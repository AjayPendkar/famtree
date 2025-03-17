package com.famtree.famtree.controller;

import com.famtree.famtree.dto.ApiResponse;
import com.famtree.famtree.dto.AuthResponse;
import com.famtree.famtree.dto.OtpResponse;
import com.famtree.famtree.dto.OtpRequest;
import com.famtree.famtree.dto.OtpVerificationRequest;
import com.famtree.famtree.dto.InitialRegistrationRequest;
import com.famtree.famtree.dto.AdminAuthRequest;
import com.famtree.famtree.dto.MobileLoginRequest;
import com.famtree.famtree.dto.LoginResponse;
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
    public ResponseEntity<ApiResponse<OtpResponse>> sendOtp(@RequestBody OtpRequest request) {
        return ResponseEntity.ok(authService.handleOtpRequest(request));
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<ApiResponse<OtpResponse>> verifyOtp(@RequestBody OtpVerificationRequest request) {
        return ResponseEntity.ok(authService.verifyOtp(request));
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

    @PostMapping("/mobile/login")
    public ResponseEntity<ApiResponse<LoginResponse>> mobileLogin(@RequestBody MobileLoginRequest request) {
        return ResponseEntity.ok(authService.mobileLogin(request));
    }

    @PostMapping("/mobile/login/send-otp")
    public ResponseEntity<ApiResponse<OtpResponse>> sendLoginOtp(@RequestBody OtpRequest request) {
        return ResponseEntity.ok(authService.sendLoginOtp(request));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<?>> logout(@RequestHeader("Authorization") String token) {
        try {
            authService.logout(token);
            return ResponseEntity.ok(ApiResponse.success(null, "Logged out successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error(e.getMessage(), HttpStatus.BAD_REQUEST));
        }
    }

    @PostMapping("/logout/all-devices")
    public ResponseEntity<ApiResponse<?>> logoutAllDevices(@RequestHeader("Authorization") String token) {
        try {
            authService.logoutAllDevices(token);
            return ResponseEntity.ok(ApiResponse.success(null, "Logged out from all devices successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error(e.getMessage(), HttpStatus.BAD_REQUEST));
        }
    }
} 