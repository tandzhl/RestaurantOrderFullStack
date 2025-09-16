package com.pdnt.restaurant.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(WebException.class)
    public ResponseEntity<Map<String, Object>> handleWebException(WebException ex) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("timestamp", LocalDateTime.now());
        errorResponse.put("code", ex.getErrorCode().getCode());
        errorResponse.put("message", ex.getErrorCode().getMessage());

        HttpStatus status;
        switch (ex.getErrorCode()) {
            case UNAUTHORIZED:
                status = HttpStatus.UNAUTHORIZED; // 401
                break;
            case FORBIDDEN:
                status = HttpStatus.FORBIDDEN; // 403
                break;
            default:
                status = HttpStatus.BAD_REQUEST; // mặc định 400
        }

        return new ResponseEntity<>(errorResponse, status);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(Exception ex) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("timestamp", LocalDateTime.now());
        errorResponse.put("code", ErrorCode.UNCATEGORIZED_EXCEPTION.getCode());
        errorResponse.put("message", ex.getMessage());

        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
