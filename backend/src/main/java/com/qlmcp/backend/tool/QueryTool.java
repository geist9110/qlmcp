package com.qlmcp.backend.tool;

import com.qlmcp.backend.annotation.DailyQuota;
import com.qlmcp.backend.dto.QuotaMethod;

import org.springaicommunity.mcp.annotation.McpTool;
import org.springaicommunity.mcp.annotation.McpToolParam;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;

import lombok.AllArgsConstructor;

@Component
@AllArgsConstructor
public class QueryTool {

    private final ChatClient chatClient;

    @DailyQuota(limit = 100, method = QuotaMethod.QUERY)
    @McpTool(name = "query", description = "Execute query with custom tools")
    String query(@McpToolParam String query) {
        return chatClient
                .prompt()
                .user(query)
                .call()
                .content();
    }
}
