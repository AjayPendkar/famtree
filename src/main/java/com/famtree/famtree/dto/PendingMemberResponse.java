package com.famtree.famtree.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PendingMemberResponse {
    private String firstName;
    private String mobile;
    private String relation;
    private String verificationCode;
    private String memberUid;
    private String familyUid;
} 