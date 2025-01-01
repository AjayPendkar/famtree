package com.famtree.famtree.dto;

import lombok.Data;

@Data
public class FamilyRegistrationRequest {
    private String familyName;
    private String uniqueIdentifier;
    private UserRegistrationRequest familyHead;
} 