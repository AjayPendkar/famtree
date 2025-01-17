package com.famtree.famtree.dto;

import lombok.Data;

@Data
public class MobileLoginRequest {
    private String mobile;
    private String otp;
} 