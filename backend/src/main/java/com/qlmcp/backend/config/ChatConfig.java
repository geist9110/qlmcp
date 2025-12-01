package com.qlmcp.backend.config;

import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.List;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.AdvisorChain;
import org.springframework.ai.chat.client.advisor.api.BaseAdvisor;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

@Configuration
public class ChatConfig {

    @Value("classpath:prompts/system-prompt.md")
    private Resource systemPrompt;

    @Bean
    ChatClient chatClient(
            ChatClient.Builder chatClientBuilder,
            List<ToolCallbackProvider> toolCallbackProviders) {
        List<ToolCallback> providerToolCallbacks = toolCallbackProviders
                .stream()
                .map(pr -> List.of(pr.getToolCallbacks()))
                .flatMap(Collection::stream)
                .toList();

        return chatClientBuilder
                .defaultSystem(systemPrompt, StandardCharsets.UTF_8)
                .defaultToolCallbacks(providerToolCallbacks)
                .defaultAdvisors(new WrapUserInputAdvisor())
                .build();
    }

    private class WrapUserInputAdvisor implements BaseAdvisor {

        private int order = 0;

        @Override
        public int getOrder() {
            return this.order;
        }

        @Override
        public ChatClientRequest before(ChatClientRequest chatClientRequest, AdvisorChain advisorChain) {
            Prompt prompt = chatClientRequest.prompt();
            String userText = prompt.getUserMessage().getText();

            String wrapped = """
                    ###USER_QUERY_START###
                    %s
                    ###USER_QUERY_END###
                    """.formatted(userText);

            return chatClientRequest.mutate()
                    .prompt(prompt.augmentUserMessage(wrapped))
                    .build();
        }

        @Override
        public ChatClientResponse after(ChatClientResponse chatClientResponse, AdvisorChain advisorChain) {
            return chatClientResponse;
        }

    }
}
