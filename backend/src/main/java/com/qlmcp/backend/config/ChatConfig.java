package com.qlmcp.backend.config;

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
