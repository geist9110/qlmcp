package com.qlmcp.backend;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.spec.McpSchema.Tool;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.ai.mcp.SyncMcpToolCallbackProvider;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.resolution.ToolCallbackResolver;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
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
    private ObjectMapper objectMapper;

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
        for (McpSyncClient client : Objects.requireNonNull(syncMcpClients.getIfAvailable())) {
            String clientName = client
                .getClientInfo()
                .name()
                .replaceAll("-", "_")
                .replaceAll(" ", "") + "_";

            for (Tool tool : client.listTools().tools()) {
                assertNotNull(
                    toolCallbackResolver
                        .resolve(clientName + tool.name())
                );
            }
        }

        assertAll(
            () -> assertNotNull(toolCallbackResolver),
            () -> assertEquals(
                getSyncToolCallbackProvider().getToolCallbacks().length,
                getToolCallbacksMap().size()
            )
        );
    }

    private Map<?, ?> getToolCallbacksMap()
        throws NoSuchFieldException, IllegalAccessException {
        // 1. DelegatingToolCallbackResolver의 resolvers 리스트 가져오기
        Field resolversField = toolCallbackResolver
            .getClass()
            .getDeclaredField("toolCallbackResolvers");
        resolversField.setAccessible(true);
        List<?> resolvers = (List<?>) resolversField.get(toolCallbackResolver);

        // 2. StaticToolCallbackResolver (첫 번째 resolver) 가져오기
        Object staticResolver = resolvers.getFirst();

        // 3. StaticToolCallbackResolver의 toolCallbacks Map 필드 가져오기
        Field toolCallbacksField = staticResolver
            .getClass()
            .getDeclaredField("toolCallbacks");
        toolCallbacksField.setAccessible(true);
        return (Map<?, ?>) toolCallbacksField.get(staticResolver);
    }

    private SyncMcpToolCallbackProvider getSyncToolCallbackProvider() {
        List<McpSyncClient> mcpClients = syncMcpClients.stream().flatMap(List::stream).toList();
        return new SyncMcpToolCallbackProvider(mcpClients);
    }
}
