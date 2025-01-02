package com.famtree.famtree.controller;

import com.famtree.famtree.dto.ApiResponse;
import com.famtree.famtree.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import com.famtree.famtree.dto.UserResponse;
import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {
    private final AdminService adminService;

    @GetMapping("/users")
    public ResponseEntity<ApiResponse<?>> getAllUsers() {
        List<UserResponse> users = adminService.getAllUsers();
        return ResponseEntity.ok(ApiResponse.success(users, "Users retrieved successfully"));
    }

    @GetMapping("/users/{role}")
    public ResponseEntity<ApiResponse<?>> getUsersByRole(@PathVariable String role) {
        List<UserResponse> users = adminService.getUsersByRole(role);
        return ResponseEntity.ok(ApiResponse.success(users, "Users retrieved successfully"));
    }

    @DeleteMapping("/users/{mobile}")
    public ResponseEntity<ApiResponse<?>> deleteUser(@PathVariable String mobile) {
        adminService.deleteUser(mobile);
        return ResponseEntity.ok(ApiResponse.success(null, "User deleted successfully"));
    }

    @PutMapping("/users/{mobile}/role")
    public ResponseEntity<ApiResponse<?>> updateUserRole(
            @PathVariable String mobile,
            @RequestParam String newRole) {
        UserResponse user = adminService.updateUserRole(mobile, newRole);
        return ResponseEntity.ok(ApiResponse.success(user, "User role updated successfully"));
    }

    @GetMapping("/families")
    public ResponseEntity<ApiResponse<?>> getAllFamilies() {
        return ResponseEntity.ok(ApiResponse.success(
            adminService.getAllFamilies(),
            "Families retrieved successfully"
        ));
    }

    @DeleteMapping("/families/{familyUid}")
    public ResponseEntity<ApiResponse<?>> deleteFamily(@PathVariable String familyUid) {
        adminService.deleteFamily(familyUid);
        return ResponseEntity.ok(ApiResponse.success(null, "Family deleted successfully"));
    }
} 