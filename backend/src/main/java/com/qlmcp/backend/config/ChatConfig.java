package com.qlmcp.backend.config;

import java.util.Collection;
import java.util.List;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ChatConfig {

    @Bean
    ChatClient chatClient(
            ChatClient.Builder chatClientBuilder,
            PromptConfig promptConfig,
            List<ToolCallbackProvider> toolCallbackProviders) {
        List<ToolCallback> providerToolCallbacks = toolCallbackProviders
                .stream()
                .map(pr -> List.of(pr.getToolCallbacks()))
                .flatMap(Collection::stream)
                .toList();

        return chatClientBuilder
                .defaultSystem(promptConfig.getSystemPrompt())
                .defaultToolCallbacks(providerToolCallbacks)
                .build();
    }
}
