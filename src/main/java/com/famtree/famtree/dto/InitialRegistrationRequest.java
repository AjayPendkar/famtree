package com.famtree.famtree.dto;

import lombok.Data;
import com.fasterxml.jackson.annotation.JsonProperty;

@Data
public class InitialRegistrationRequest {
    private String mobile;
    private String familyName;
    
    @JsonProperty("isFamilyHead")
    private boolean familyHead;
    
    public boolean isFamilyHead() {
        return familyHead;
    }
} 