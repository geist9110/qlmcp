package com.qlmcp.backend.tool;

import lombok.extern.slf4j.Slf4j;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class MemoryTool {

    @Tool(name = "auth-poc", description = "auth poc tool")
    private String authPoCTool() {
        log.info("=== Auth PoC Tool ===");
        log.info(SecurityContextHolder.getContext().getAuthentication().getName());

        return "Hello, World!";
    }
}
