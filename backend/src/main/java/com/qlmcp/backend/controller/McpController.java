package com.qlmcp.backend.controller;

import com.qlmcp.backend.dto.JsonRpcRequest;
import com.qlmcp.backend.dto.JsonRpcResponse;
import com.qlmcp.backend.service.McpService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/mcp")
@RequiredArgsConstructor
public class McpController {

    private final McpService mcpService;

    @PostMapping
    public ResponseEntity<JsonRpcResponse> handleMcp(
        @RequestBody JsonRpcRequest request
    ) {
        JsonRpcResponse mcpResponse = mcpService.createResponse(request);

        if (mcpResponse == null) {
            return ResponseEntity.accepted().build();
        }

        return ResponseEntity.ok()
            .contentType(MediaType.APPLICATION_JSON)
            .body(mcpService.createResponse(request));
    }

    @GetMapping
    public ResponseEntity<Void> handleSSEConnection() {
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).build();
    }
}