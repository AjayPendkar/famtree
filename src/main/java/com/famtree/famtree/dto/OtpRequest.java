package com.famtree.famtree.dto;

import lombok.Data;

@Data
public class OtpRequest {
    private String mobile;
    private String familyName;
    private String role;
} 