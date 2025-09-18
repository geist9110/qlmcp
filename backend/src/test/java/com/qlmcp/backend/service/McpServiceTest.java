package com.qlmcp.backend.service;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.qlmcp.backend.config.McpProperties;
import com.qlmcp.backend.config.ToolRegistry;
import com.qlmcp.backend.dto.JsonRpcRequest.McpRequest;
import com.qlmcp.backend.dto.JsonRpcRequest.ToolsCallRequest;
import com.qlmcp.backend.dto.JsonRpcResponse.InitializeResponse;
import com.qlmcp.backend.dto.JsonRpcResponse.McpResponse;
import com.qlmcp.backend.dto.JsonRpcResponse.ToolsCallResponse;
import com.qlmcp.backend.dto.JsonRpcResponse.ToolsListResponse;
import com.qlmcp.backend.dto.ToolInformation;
import com.qlmcp.backend.tool.ToolInterface;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class McpServiceTest {

    private final McpProperties mcpProperties = mock(McpProperties.class);
    private ToolRegistry toolRegistry;
    private McpService mcpService;

    private final String expectJsonRpcVersion = "2.0";
    private final String requestId = "test-id";
    private final String expectProtocolVersion = "1.0.0";
    private final String expectServerName = "test-server";
    private final String expectServerVersion = "0.1.0";

    // Properties를 통해 Instructions 값을 주입받도록 변경 필요
    private final String expectInstructions = "Optional instructions for the client";

    @BeforeEach
    void setUp() {
        toolRegistry = mock(ToolRegistry.class);
        mcpService = new McpService(mcpProperties, toolRegistry);

        when(mcpProperties.getProtocolVersion()).thenReturn(expectProtocolVersion);
        when(mcpProperties.getServerName()).thenReturn(expectServerName);
        when(mcpProperties.getServerVersion()).thenReturn(expectServerVersion);
    }

    @Test
    @DisplayName("createResponse - INITIALIZE")
    void createResponse_initialize() {
        // given
        McpRequest request = mock(McpRequest.class);

        // when
        when(request.getMethod()).thenReturn("initialize");
        when(request.getId()).thenReturn(requestId);

        // then
        InitializeResponse actual = (InitializeResponse) mcpService.initialize(requestId);
        InitializeResponse.Result result = actual.getResult();

        assertAll(
            () -> assertNotNull(actual),
            () -> assertEquals(expectJsonRpcVersion, actual.getJsonrpc()),
            () -> assertEquals(requestId, actual.getId()),
            () -> assertEquals(expectProtocolVersion, result.getProtocolVersion()),
            () -> assertEquals(expectInstructions, result.getInstructions()),
            () -> assertTrue(result.getServerInfo().containsKey("name")),
            () -> assertTrue(result.getServerInfo().containsKey("version")),
            () -> assertEquals(expectServerName, result.getServerInfo().get("name")),
            () -> assertEquals(expectServerVersion, result.getServerInfo().get("version")),
            () -> assertTrue(result.getCapabilities().containsKey("tools")),
            () -> assertEquals(Map.of(), result.getCapabilities().get("tools"))
        );
    }

    @Test
    @DisplayName("createResponse - TOOLS_LIST")
    void createResponse_toolsList() {
        // given
        List<ToolInformation> expectTools = new LinkedList<>();
        McpRequest request = mock(McpRequest.class);

        // when
        when(request.getMethod()).thenReturn("tools/list");
        when(request.getId()).thenReturn(requestId);
        when(toolRegistry.getToolInformationList()).thenReturn(expectTools);

        // then
        ToolsListResponse actual = (ToolsListResponse) mcpService.toolList(requestId);

        assertAll(
            () -> assertNotNull(actual),
            () -> assertEquals(expectJsonRpcVersion, actual.getJsonrpc()),
            () -> assertEquals(requestId, actual.getId()),
            () -> assertEquals(expectTools, actual.getResult().getTools())
        );
    }

    @Test
    @DisplayName("createResponse - TOOLS_CALL")
    void createResponse_toolsCall() {
        // given
        ToolsCallRequest.Params params = new ToolsCallRequest.Params(
            "test-tool",
            Map.of("arguments",
                Map.of("param1", "value1")
            )
        );
        Map<String, Object> expectResult = Map.of("result", "success");

        class TestTool implements ToolInterface {

            @Override
            public List<Object> call(Object id, Map<?, ?> arguments) {
                return List.of(expectResult);
            }
        }

        ToolsCallRequest request = mock(ToolsCallRequest.class);

        // when
        when(request.getMethod()).thenReturn("tools/call");
        when(request.getId()).thenReturn(requestId);
        when(request.getParams()).thenReturn(params);
        when(toolRegistry.getToolByName("test-tool")).thenReturn(new TestTool());

        // then
        McpResponse actual = mcpService.callTools(request);
        ToolsCallResponse.Result result = ((ToolsCallResponse) actual).getResult();

        assertAll(
            () -> assertNotNull(actual),
            () -> assertEquals(expectJsonRpcVersion, actual.getJsonrpc()),
            () -> assertEquals(requestId, actual.getId()),
            () -> assertFalse(result.getIsError()),
            () -> assertEquals(1, result.getContent().size()),
            () -> assertEquals(expectResult, result.getContent().getFirst())
        );
    }
}