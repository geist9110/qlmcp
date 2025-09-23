package com.qlmcp.backend.config;

import io.modelcontextprotocol.client.McpSyncClient;
import java.util.List;
import lombok.NonNull;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.mcp.SyncMcpToolCallbackProvider;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.resolution.ToolCallbackResolver;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class ChatConfig {

    @Bean
    @Primary
    public ToolCallbackResolver emptyToolCallbackResolver() {
        return new ToolCallbackResolver() {
            @Override
            public ToolCallback resolve(@NonNull String toolName) {
                return null;
            }
        };
    }

    @Bean
    public ChatClient chatClient(
        ChatClient.Builder chatClientBuilder,
        ObjectProvider<List<McpSyncClient>> syncMcpClients
    ) {
        List<McpSyncClient> mcpClients = syncMcpClients.stream().flatMap(List::stream).toList();

        return chatClientBuilder
            .defaultToolCallbacks(new SyncMcpToolCallbackProvider(mcpClients))
            .build();
    }
}
