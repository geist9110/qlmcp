package com.qlmcp.backend;

import static com.qlmcp.backend.util.ReflectionHelper.getFieldValue;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.server.McpServerFeatures.SyncToolSpecification;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.mcp.SyncMcpToolCallbackProvider;
import org.springframework.ai.tool.resolution.ToolCallbackResolver;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Map;

@ActiveProfiles("test")
@SpringBootTest
class BackendApplicationTests {

    @Autowired private ObjectProvider<List<SyncToolSpecification>> tools;

    @Autowired ObjectProvider<List<McpSyncClient>> syncMcpClients;

    @Autowired private ToolCallbackResolver toolCallbackResolver;

    @Autowired private ChatClient chatClient;

    @Autowired private ApplicationContext applicationContext;

    @Autowired private Environment environment;

    @Test
    @DisplayName("Spring Application Context가 정상적으로 로드되는지 테스트")
    void contextLoads() {
        String appName = environment.getProperty("spring.application.name");
        assertEquals("backend", appName);
    }

    @Test
    @DisplayName("SyncTool 전체를 불렀을 때 서버 툴만 불러오는지 테스트")
    void internalToolTest() {
        List<SyncToolSpecification> serverTools = tools.stream().flatMap(List::stream).toList();
        assertAll(() -> assertNotNull(serverTools), () -> assertEquals(1, serverTools.size()));
    }

    @Test
    @DisplayName("ToolCallbackResolver가 외부 툴만 불러오는지 테스트")
    void externalToolTest() {
        List<?> actualResolver =
                getFieldValue(toolCallbackResolver, "toolCallbackResolvers", List.class);
        Map<?, ?> actualToolCallbackMap =
                getFieldValue(actualResolver.getFirst(), "toolCallbacks", Map.class);

        assertAll(
                () -> assertNotNull(toolCallbackResolver),
                () ->
                        assertEquals(
                                getSyncToolCallbackProvider().getToolCallbacks().length,
                                actualToolCallbackMap.size()));
    }

    private SyncMcpToolCallbackProvider getSyncToolCallbackProvider() {
        List<McpSyncClient> mcpClients = syncMcpClients.stream().flatMap(List::stream).toList();
        return SyncMcpToolCallbackProvider.builder().mcpClients(mcpClients).build();
    }

    @Test
    @DisplayName("ChatClient가 외부 툴을 기본으로 가지는지 테스트")
    void chatClientTest() {
        Object defaultChatClientRequest =
                getFieldValue(chatClient, "defaultChatClientRequest", Object.class);

        List<?> actualToolCallbacks =
                getFieldValue(defaultChatClientRequest, "toolCallbacks", List.class);

        // 현재 PoC를 위한 Memory 툴이 추가되어 있기에 +1을 진행
        assertEquals(
                getSyncToolCallbackProvider().getToolCallbacks().length + 1,
                actualToolCallbacks.size());
    }

    @Test
    @DisplayName("Application Context가 SyncMcpToolCallbackProvider을 반환값으로 가지는 빈을 가지지 않는지 확인하는 테스트")
    void noSyncMcpToolCallbackProviderBeanTest() {
        Map<String, SyncMcpToolCallbackProvider> beans =
                applicationContext.getBeansOfType(SyncMcpToolCallbackProvider.class);

        assertEquals(0, beans.size());
    }
}
