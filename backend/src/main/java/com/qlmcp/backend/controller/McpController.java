package com.qlmcp.backend.controller;

import com.qlmcp.backend.dto.JsonRpcRequest;
import com.qlmcp.backend.dto.JsonRpcResponse;
import com.qlmcp.backend.dto.Method;
import com.qlmcp.backend.exception.CustomException;
import com.qlmcp.backend.exception.ErrorCode;
import com.qlmcp.backend.service.McpService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/mcp")
@RequiredArgsConstructor
public class McpController {

    private final McpService mcpService;

    @PostMapping
    public ResponseEntity<JsonRpcResponse> handleMcp(
        @RequestBody JsonRpcRequest request
    ) {
        Method method = request.getMethod();

        if (method == Method.INITIALIZE) {
            return ResponseEntity
                .ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(mcpService.initialize(request.getId()));
        }

        if (method == Method.NOTIFICATIONS_INITIALIZED) {
            return ResponseEntity
                .accepted()
                .build();
        }

        if (method == Method.TOOLS_LIST) {
            return ResponseEntity
                .ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(mcpService.toolList(request.getId()));
        }

        if (method == Method.TOOLS_CALL) {
            return ResponseEntity
                .ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(mcpService.callTools(request));
        }

        throw new CustomException(ErrorCode.METHOD_NOT_FOUND);
    }

    @GetMapping
    public ResponseEntity<Void> handleSSEConnection() {
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).build();
    }
}