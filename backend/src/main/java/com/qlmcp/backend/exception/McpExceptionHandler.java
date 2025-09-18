package com.qlmcp.backend.exception;

import com.qlmcp.backend.controller.McpController;
import com.qlmcp.backend.dto.JsonRpcResponse.ErrorResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(basePackageClasses = McpController.class)
public class McpExceptionHandler {

    @ExceptionHandler(McpException.class)
    public ResponseEntity<ErrorResponse> handleCustomException(McpException e) {
        return ResponseEntity
            .internalServerError()
            .body(new ErrorResponse(
                e.getId(),
                e.getErrorCode()
            ));
    }
}
