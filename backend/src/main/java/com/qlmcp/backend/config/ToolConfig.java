package com.qlmcp.backend.config;

import com.qlmcp.backend.tool.MemoryTool;

import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ToolConfig {

    @Bean
    public ToolCallbackProvider toolCallbackProvider(MemoryTool memoryTool) {
        return MethodToolCallbackProvider.builder()
                .toolObjects(memoryTool)
                .build();
    }
}
