package com.las4as.POSBackend.shared.interfaces.rest.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponse {
    private String error;
    private String code;
    private String message;
    private LocalDateTime timestamp;
    private String path;
    
    public static ErrorResponse of(String error, String code, String message, String path) {
        return new ErrorResponse(error, code, message, LocalDateTime.now(), path);
    }
    
    public static ErrorResponse of(String error, String code, String path) {
        return new ErrorResponse(error, code, error, LocalDateTime.now(), path);
    }
}
