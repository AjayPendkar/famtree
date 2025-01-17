package com.famtree.famtree.dto;

import lombok.Data;

@Data
public class OtpVerificationRequest {
    private String mobile;
    private String otp;
} 