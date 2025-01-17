package com.famtree.famtree.dto;

import lombok.Data;
import lombok.Builder;

@Data
@Builder
public class ErrorResponse {
    private int status;
    private String error;
    private String message;
    private String path;
} 