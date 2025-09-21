package com.qlmcp.backend.config;

import lombok.NonNull;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.resolution.ToolCallbackResolver;
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
    public ChatClient chatClient(ChatClient.Builder chatClientBuilder) {
        return chatClientBuilder.build();
    }
}
