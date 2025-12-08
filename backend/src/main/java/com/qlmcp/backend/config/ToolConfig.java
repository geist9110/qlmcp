package com.qlmcp.backend.config;

import java.util.Arrays;
import java.util.List;

import com.qlmcp.backend.tool.MemoryTool;
import com.qlmcp.backend.tool.QueryTool;

import org.springframework.ai.mcp.McpToolUtils;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.MimeType;

import io.modelcontextprotocol.server.McpStatelessServerFeatures.SyncToolSpecification;

@Configuration
public class ToolConfig {

    @Bean
    ToolCallbackProvider toolCallbackProvider(MemoryTool memoryTool) {
        return MethodToolCallbackProvider.builder()
                .toolObjects(memoryTool)
                .build();
    }

    @Bean
    List<SyncToolSpecification> externalTool(QueryTool queryTool) {
        ToolCallbackProvider toolCallbackProvider = MethodToolCallbackProvider.builder()
                .toolObjects(queryTool)
                .build();
        return Arrays.stream(toolCallbackProvider.getToolCallbacks())
                .map(toolCallback -> McpToolUtils.toStatelessSyncToolSpecification(toolCallback,
                        MimeType.valueOf("application/json")))
                .toList();
    }
}
