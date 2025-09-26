package com.qlmcp.backend;

import static com.qlmcp.backend.util.ReflectionHelper.getFieldValue;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.modelcontextprotocol.client.McpSyncClient;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.mcp.SyncMcpToolCallbackProvider;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.resolution.ToolCallbackResolver;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;

@SpringBootTest
class BackendApplicationTests {

    @Autowired
    private ToolCallbackProvider toolCallbackProvider;

    @Autowired
    ObjectProvider<List<McpSyncClient>> syncMcpClients;

    @Autowired
    private ToolCallbackResolver toolCallbackResolver;

    @Autowired
    private ChatClient chatClient;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private Environment environment;

    @Test
    @DisplayName("Spring Application Context가 정상적으로 로드되는지 테스트")
    void contextLoads() {
        String appName = environment.getProperty("spring.application.name");
        assertEquals("test", appName);
    }

    @Test
    @DisplayName("toolCallbackProvider을 불렀을 때 내부 툴만 불러오는지 테스트")
    void internalToolTest() throws JsonProcessingException {
        ToolCallback actualToolCallback = toolCallbackProvider.getToolCallbacks()[0];
        Map<String, Object> actualToolSchema = objectMapper.readValue(
            actualToolCallback.getToolDefinition().inputSchema(),
            new TypeReference<>() {
            }
        );

        assertAll(
            () -> assertNotNull(toolCallbackProvider),
            () -> assertEquals(1, toolCallbackProvider.getToolCallbacks().length),
            () -> assertEquals("query", actualToolCallback.getToolDefinition().name()),
            () -> assertEquals("Execute query with custom tools",
                actualToolCallback.getToolDefinition().description()),
            () -> assertFalse(actualToolCallback.getToolMetadata().returnDirect()),
            () -> assertEquals("object", actualToolSchema.get("type")),
            () -> assertEquals(
                Map.of(
                    "query", Map.of(
                        "type", "string")
                ),
                actualToolSchema.get("properties")),
            () -> assertEquals(List.of("query"), actualToolSchema.get("required")),
            () -> assertFalse((boolean) actualToolSchema.get("additionalProperties"))
        );
    }

    @Test
    @DisplayName("ToolCallbackResolver가 외부 툴만 불러오는지 테스트")
    void externalToolTest() {
        List<?> actualResolver = getFieldValue(
            toolCallbackResolver,
            "toolCallbackResolvers",
            List.class
        );
        Map<?, ?> actualToolCallbackMap = getFieldValue(
            actualResolver.getFirst(),
            "toolCallbacks",
            Map.class
        );

        assertAll(
            () -> assertNotNull(toolCallbackResolver),
            () -> assertEquals(
                getSyncToolCallbackProvider().getToolCallbacks().length,
                actualToolCallbackMap.size()
            )
        );
    }

    private SyncMcpToolCallbackProvider getSyncToolCallbackProvider() {
        List<McpSyncClient> mcpClients = syncMcpClients.stream().flatMap(List::stream).toList();
        return new SyncMcpToolCallbackProvider(mcpClients);
    }

    @Test
    @DisplayName("ChatClient가 외부 툴을 기본으로 가지는지 테스트")
    void chatClientTest() {
        Object defaultChatClientRequest = getFieldValue(
            chatClient,
            "defaultChatClientRequest",
            Object.class
        );

        List<?> actualToolCallbacks = getFieldValue(
            defaultChatClientRequest,
            "toolCallbacks",
            List.class
        );

        assertEquals(
            getSyncToolCallbackProvider().getToolCallbacks().length,
            actualToolCallbacks.size()
        );
    }

    @Test
    @DisplayName("Application Context가 SyncMcpToolCallbackProvider을 반환값으로 가지는 빈을 가지지 않는지 확인하는 테스트")
    void noSyncMcpToolCallbackProviderBeanTest() {
        Map<String, SyncMcpToolCallbackProvider> beans =
            applicationContext.getBeansOfType(SyncMcpToolCallbackProvider.class);

        assertEquals(0, beans.size());
    }
}
