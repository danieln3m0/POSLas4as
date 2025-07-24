package com.las4as.POSBackend.shared.interfaces.rest.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {
    private boolean success;
    private String message;
    private String code;
    private T data;
    private LocalDateTime timestamp;
    
    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(true, message, "SUCCESS", data, LocalDateTime.now());
    }
    
    public static <T> ApiResponse<T> success(String message, String code, T data) {
        return new ApiResponse<>(true, message, code, data, LocalDateTime.now());
    }
    
    public static <T> ApiResponse<T> error(String message, String code) {
        return new ApiResponse<>(false, message, code, null, LocalDateTime.now());
    }
    
    public static <T> ApiResponse<T> error(String message, String code, T data) {
        return new ApiResponse<>(false, message, code, data, LocalDateTime.now());
    }
}
