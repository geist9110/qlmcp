package com.qlmcp.backend.exection;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ErrorCode> handleException(CustomException exception) {
        return ResponseEntity
            .status(exception.getHttpStatus())
            .headers(exception.getHeaders())
            .body(exception.getErrorCode());
    }
}
