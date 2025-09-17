package com.qlmcp.backend.service;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.qlmcp.backend.config.McpProperties;
import com.qlmcp.backend.config.ToolRegistry;
import com.qlmcp.backend.dto.JsonRpcRequest;
import com.qlmcp.backend.dto.JsonRpcResponse;
import com.qlmcp.backend.dto.Method;
import com.qlmcp.backend.dto.ToolInformation;
import com.qlmcp.backend.tool.ToolInterface;
import java.util.LinkedList;
import java.util.List;
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
        JsonRpcRequest request = mock(JsonRpcRequest.class);
        when(request.getMethod()).thenReturn(Method.INITIALIZE);
        when(request.getId()).thenReturn(requestId);
        when(mcpProperties.getProtocolVersion()).thenReturn("1.0.0");
        when(mcpProperties.getServerName()).thenReturn("test-server");
        when(mcpProperties.getServerVersion()).thenReturn("0.1.0");

        // when
        JsonRpcResponse actual = mcpService.initialize(requestId);

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
    @DisplayName("createResponse - TOOLS_LIST")
    void createResponse_toolsList() {
        // given
        List<ToolInformation> expectTools = new LinkedList<>();

        Object requestId = "test-id";
        JsonRpcRequest request = mock(JsonRpcRequest.class);
        when(request.getMethod()).thenReturn(Method.TOOLS_LIST);
        when(request.getId()).thenReturn(requestId);
        when(toolRegistry.getToolInformationList())
            .thenReturn(expectTools);

        // when
        JsonRpcResponse actual = mcpService.toolList(requestId);

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

        class TestTool implements ToolInterface {

            public Map<String, Object> call(Map<?, ?> args) {
                return expectResult;
            }
        }

        JsonRpcRequest request = mock(JsonRpcRequest.class);
        when(request.getMethod()).thenReturn(Method.TOOLS_CALL);
        when(request.getId()).thenReturn(requestId);
        when(request.getParams()).thenReturn(params);
        when(toolRegistry.getToolByName("test-tool"))
            .thenReturn(new TestTool());

        // when
        JsonRpcResponse actual = mcpService.callTools(request);

        // then
        Map<String, Object> result = (Map<String, Object>) actual.getResult();

        assertAll(
            () -> assertNotNull(actual),
            () -> assertEquals(requestId, actual.getId()),
            () -> assertEquals(expectResult, result)
        );
    }
}