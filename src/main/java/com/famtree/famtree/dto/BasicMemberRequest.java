package com.famtree.famtree.dto;

import lombok.Data;

@Data
public class BasicMemberRequest {
    private String firstName;
    private String mobile;
    private String relation;  // Relation to family head
} 