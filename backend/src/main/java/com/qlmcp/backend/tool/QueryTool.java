package com.qlmcp.backend.tool;

import com.qlmcp.backend.annotation.DailyQuota;
import com.qlmcp.backend.dto.QuotaMethod;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import lombok.AllArgsConstructor;

@Component
@AllArgsConstructor
public class QueryTool {

    private final ChatClient chatClient;

    @DailyQuota(limit = 100, method = QuotaMethod.QUERY)
    @Tool(name = "query", description = "Execute query with custom tools")
    String query(@ToolParam String query) {
        return chatClient
                .prompt()
                .user(query)
                .call()
                .content();
    }
}
