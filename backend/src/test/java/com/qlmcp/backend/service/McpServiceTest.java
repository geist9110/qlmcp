package com.qlmcp.backend.service;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.qlmcp.backend.config.McpProperties;
import com.qlmcp.backend.config.ToolRegistry;
import com.qlmcp.backend.dto.McpRequest;
import com.qlmcp.backend.dto.McpResponse;
import com.qlmcp.backend.dto.Method;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class McpServiceTest {

    private McpProperties mcpProperties;
    private ToolRegistry toolRegistry;
    private McpService mcpService;

    @BeforeEach
    void setUp() {
        mcpProperties = mock(McpProperties.class);
        toolRegistry = mock(ToolRegistry.class);
        mcpService = new McpService(mcpProperties, toolRegistry);
    }

    @Test
    @DisplayName("createResponse - INITIALIZE")
    void createResponse_initialize() {
        // given
        Object requestId = "test-id";
        McpRequest request = mock(McpRequest.class);
        when(request.getMethod()).thenReturn(Method.INITIALIZE);
        when(request.getId()).thenReturn(requestId);
        when(mcpProperties.getProtocolVersion()).thenReturn("1.0.0");
        when(mcpProperties.getServerName()).thenReturn("test-server");
        when(mcpProperties.getServerVersion()).thenReturn("0.1.0");

        // when
        McpResponse actual = mcpService.createResponse(request);

        // then
        Map<String, Object> result = (Map<String, Object>) actual.getResult();
        Map<String, Object> serverInfo = (Map<String, Object>) result.get("serverInfo");

        assertAll(
            () -> assertNotNull(actual),
            () -> assertEquals(requestId, actual.getId()),
            () -> assertEquals("1.0.0", result.get("protocolVersion")),
            () -> assertTrue(result.containsKey("capabilities")),
            () -> assertTrue(result.containsKey("serverInfo")),
            () -> assertEquals("test-server", serverInfo.get("name")),
            () -> assertEquals("0.1.0", serverInfo.get("version"))
        );
    }

    @Test
    @DisplayName("createResponse - NOTIFICATIONS_INITIALIZED")
    void createResponse_notificationsInitialized() {
        // given
        McpRequest request = mock(McpRequest.class);
        when(request.getMethod()).thenReturn(Method.NOTIFICATIONS_INITIALIZED);

        // when
        McpResponse actual = mcpService.createResponse(request);

        // then
        assertNull(actual);
    }

    @Test
    @DisplayName("createResponse - TOOLS_LIST")
    void createResponse_toolsList() {
        // given
        Map expectTools = Map.of("tools", new String[]{});

        Object requestId = "test-id";
        McpRequest request = mock(McpRequest.class);
        when(request.getMethod()).thenReturn(Method.TOOLS_LIST);
        when(request.getId()).thenReturn(requestId);
        when(toolRegistry.getToolsList())
            .thenReturn(expectTools);

        // when
        McpResponse actual = mcpService.createResponse(request);

        // then
        Map<String, Object> result = (Map<String, Object>) actual.getResult();

        assertAll(
            () -> assertNotNull(actual),
            () -> assertEquals(requestId, actual.getId()),
            () -> assertTrue(result.containsKey("tools"))
        );
    }

    @Test
    @DisplayName("createResponse - TOOLS_CALL")
    void createResponse_toolsCall() {
        // given
        Object requestId = "test-id";
        Map<String, Object> params = Map.of("name", "test-tool",
            "arguments", Map.of("param1", "value1"));
        Map<String, Object> expectResult = Map.of("result", "success");

        McpRequest request = mock(McpRequest.class);
        when(request.getMethod()).thenReturn(Method.TOOLS_CALL);
        when(request.getId()).thenReturn(requestId);
        when(request.getParams()).thenReturn(params);
        when(toolRegistry.getToolByName("test-tool")).thenReturn(new Object() {
            public Map<String, Object> call(Map<String, Object> args) {
                return expectResult;
            }
        });

        // when
        McpResponse actual = mcpService.createResponse(request);

        // then
        Map<String, Object> result = (Map<String, Object>) actual.getResult();

        assertAll(
            () -> assertNotNull(actual),
            () -> assertEquals(requestId, actual.getId()),
            () -> assertEquals(expectResult, result)
        );
    }
}