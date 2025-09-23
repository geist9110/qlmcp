package com.qlmcp.backend.tool;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class QueryTool {

    private final ChatClient chatClient;

    @Tool(name = "query", description = "Execute query with custom tools")
    String query(@ToolParam String query) {
        return chatClient
            .prompt(query)
            .call()
            .content();
    }
}
