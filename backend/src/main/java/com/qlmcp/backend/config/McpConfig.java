package com.qlmcp.backend.config;

import com.qlmcp.backend.tool.DateTimeTool;
import com.qlmcp.backend.tool.QueryTool;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class McpConfig {

    @Bean
    public ToolCallbackProvider toolCallBackProvider(
        DateTimeTool dateTimeTool,
        QueryTool queryTool
    ) {
        return MethodToolCallbackProvider.builder()
            .toolObjects(dateTimeTool, queryTool)
            .build();
    }
}
