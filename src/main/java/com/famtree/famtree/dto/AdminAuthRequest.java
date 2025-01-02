package com.famtree.famtree.dto;

import com.famtree.famtree.enums.UserRole;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminAuthRequest {
    private String email;
    private String password;
    private String firstName;
    private String lastName;
    private UserRole role;  // Only for registration
} 