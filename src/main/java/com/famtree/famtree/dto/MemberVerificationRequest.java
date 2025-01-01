package com.famtree.famtree.dto;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberVerificationRequest {
    private String mobile;
    private String verificationCode;
    private String familyName;
    private String headName;
} 