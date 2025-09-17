package com.qlmcp.backend.exception;

import com.qlmcp.backend.controller.McpController;
import com.qlmcp.backend.dto.JsonRpcResponse;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(basePackageClasses = McpController.class)
public class McpExceptionHandler {

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<JsonRpcResponse> handleCustomException(CustomException e) {
        return ResponseEntity
            .internalServerError()
            .body(JsonRpcResponse.builder()
                .error(Map.of(
                    "code", e.getErrorCode().getCode(),
                    "message", e.getErrorCode().getMessage()
                ))
                .build());
    }
}
