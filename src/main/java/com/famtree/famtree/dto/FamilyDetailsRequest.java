package com.famtree.famtree.dto;

import lombok.Data;
import java.util.List;

@Data
public class FamilyDetailsRequest {
    private String familyName;
    private String address;
    private String description;
    private int memberCount;  // Total number of family members excluding head
    private boolean isBlocked = false;  // Set default value
    private UserDetailsRequest familyHead;
    private List<BasicMemberRequest> members;  // Basic details for other members
} 