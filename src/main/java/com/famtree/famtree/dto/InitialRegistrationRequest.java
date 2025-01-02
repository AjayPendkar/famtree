package com.famtree.famtree.dto;

import lombok.Data;
import com.famtree.famtree.enums.UserRole;

@Data
public class InitialRegistrationRequest {
    private String mobile;
    private String familyName;
    private UserRole role;
} 