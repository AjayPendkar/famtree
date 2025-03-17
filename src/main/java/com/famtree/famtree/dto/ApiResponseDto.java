package com.famtree.famtree.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponseDto<T> {
    private int status;
    private String message;
    private T data;

    public static <T> ApiResponseDto<T> success(T data, String message) {
        return ApiResponseDto.<T>builder()
                .status(HttpStatus.OK.value())
                .message(message)
                .data(data)
                .build();
    }

    public static <T> ApiResponseDto<T> error(String message, HttpStatus status) {
        return ApiResponseDto.<T>builder()
                .status(status.value())
                .message(message)
                .build();
    }
} 