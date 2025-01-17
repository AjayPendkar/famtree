package com.famtree.famtree.dto;

import lombok.Data;
import lombok.Builder;

@Data
@Builder
public class OtpResponse {
    private String message;
    private String token;
    private String otp;
    private String role;
    private String familyName;
    private String familyUid;
    private boolean success;
    private String mobile;
    private boolean isVerified;
    private boolean isFamilyHead;
} 