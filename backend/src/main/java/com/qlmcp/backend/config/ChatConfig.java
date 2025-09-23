package com.qlmcp.backend.config;

import io.modelcontextprotocol.client.McpSyncClient;
import java.util.ArrayList;
import java.util.List;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.mcp.SyncMcpToolCallbackProvider;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.resolution.DelegatingToolCallbackResolver;
import org.springframework.ai.tool.resolution.SpringBeanToolCallbackResolver;
import org.springframework.ai.tool.resolution.StaticToolCallbackResolver;
import org.springframework.ai.tool.resolution.ToolCallbackResolver;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.support.GenericApplicationContext;

@Configuration
public class ChatConfig {

    /**
     * ToolCallbackResolver 빈 오버라이딩으로 기본적인 토대는 동일하다. LLM이 외부 MCP 툴을 콜백으로 받도록 수정하여 내부에서 정의된 툴을 사용할 수
     * 없도록한다.
     *
     * @see : ToolCallingAutoConfiguration#toolCallbackResolver
     */
    @Bean
    @Primary
    public ToolCallbackResolver externalToolCallbackResolver(
        GenericApplicationContext applicationContext,
        List<ToolCallback> toolCallbacks,
        ObjectProvider<List<McpSyncClient>> syncMcpClients
    ) {
        List<ToolCallbackProvider> tcbProviders = List
            .of(getSyncMcpToolCallbackProvider(syncMcpClients));

        List<ToolCallback> allFunctionAndToolCallbacks = new ArrayList<>(toolCallbacks);
        tcbProviders.stream()
            .map(pr -> List.of(pr.getToolCallbacks()))
            .forEach(allFunctionAndToolCallbacks::addAll);

        var staticToolCallbackResolver = new StaticToolCallbackResolver(
            allFunctionAndToolCallbacks);

        var springBeanToolCallbackResolver = SpringBeanToolCallbackResolver.builder()
            .applicationContext(applicationContext)
            .build();

        return new DelegatingToolCallbackResolver(
            List.of(staticToolCallbackResolver, springBeanToolCallbackResolver));
    }

    @Bean
    public ChatClient chatClient(
        ChatClient.Builder chatClientBuilder,
        ObjectProvider<List<McpSyncClient>> syncMcpClients
    ) {
        return chatClientBuilder
            .defaultToolCallbacks(getSyncMcpToolCallbackProvider(syncMcpClients))
            .build();
    }

    // Bean으로 등록시 외부에 Tool이 노출되므로 등록하면 안된다.
    private SyncMcpToolCallbackProvider getSyncMcpToolCallbackProvider(
        ObjectProvider<List<McpSyncClient>> syncMcpClients
    ) {
        List<McpSyncClient> mcpClients = syncMcpClients.stream().flatMap(List::stream).toList();
        return new SyncMcpToolCallbackProvider(mcpClients);
    }
}
