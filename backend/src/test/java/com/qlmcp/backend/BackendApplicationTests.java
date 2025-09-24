package com.qlmcp.backend;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;

@SpringBootTest
class BackendApplicationTests {

    @Autowired
    private ToolCallbackProvider toolCallbackProvider;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private Environment environment;

    @Test
    void contextLoads() {
        String appName = environment.getProperty("spring.application.name");
        assertThat(appName).isEqualTo("test");
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
}
