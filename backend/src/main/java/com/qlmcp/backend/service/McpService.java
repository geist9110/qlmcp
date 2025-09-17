package com.qlmcp.backend.service;

import com.qlmcp.backend.config.McpProperties;
import com.qlmcp.backend.config.ToolRegistry;
import com.qlmcp.backend.dto.JsonRpcRequest.McpRequest;
import com.qlmcp.backend.dto.JsonRpcRequest.ToolsCallRequest;
import com.qlmcp.backend.dto.JsonRpcResponse;
import com.qlmcp.backend.exception.CustomException;
import com.qlmcp.backend.exception.ErrorCode;
import com.qlmcp.backend.tool.ToolInterface;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class McpService {

    private final McpProperties mcpProperties;
    private final ToolRegistry toolRegistry;

    public JsonRpcResponse initialize(Object requestId) {
        Map<String, Object> initializeResult = new HashMap<>();
        initializeResult.put("protocolVersion", mcpProperties.getProtocolVersion());
        initializeResult.put("capabilities", Map.of(
            "tools", Map.of()
        ));
        initializeResult.put("serverInfo", Map.of(
            "name", mcpProperties.getServerName(),
            "version", mcpProperties.getServerVersion()
        ));
        initializeResult.put("instructions", "Optional instructions for the client");

        return JsonRpcResponse.builder()
            .id(requestId)
            .result(initializeResult)
            .build();
    }

    public JsonRpcResponse toolList(Object requestId) {
        return JsonRpcResponse.builder()
            .id(requestId)
            .result(Map.of("tools", toolRegistry.getToolInformationList()))
            .build();
    }

    public JsonRpcResponse callTools(McpRequest request) {
        ToolsCallRequest.Params toolsCallRequest = ((ToolsCallRequest) request).getParams();
        String name = toolsCallRequest.getName();
        Map<String, Object> arguments = toolsCallRequest.getArguments();

        return JsonRpcResponse.builder()
            .id(request.getId())
            .result(executeTool(name, arguments))
            .build();
    }

    private Map<String, Object> executeTool(String name, Map<String, Object> arguments) {
        ToolInterface tool = toolRegistry.getToolByName(name);

        if (tool == null) {
            throw new CustomException(ErrorCode.TOOL_NOT_FOUND);
        }

        return tool.call(arguments);
    }
}
