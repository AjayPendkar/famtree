package com.famtree.famtree.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FamilyHeadResponse {
    private String familyName;
    private String headName;
    private String headMobile;
    private int familyCount;
    private String familyUid;
    private String description;
} 