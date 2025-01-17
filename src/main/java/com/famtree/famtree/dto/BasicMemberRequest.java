package com.famtree.famtree.dto;

import lombok.Data;
import java.util.List;

@Data
public class BasicMemberRequest {
    private String firstName;
    private String mobile;
    private String relation;  // Relation to family head
    private boolean isBlocked;  // Add this field
    private String profilePicture;  // Add this
    private List<String> photos;    // Add this
} 