package com.qlmcp.backend.service;

import com.qlmcp.backend.config.McpProperties;
import com.qlmcp.backend.config.ToolRegistry;
import com.qlmcp.backend.dto.JsonRpcRequest;
import com.qlmcp.backend.dto.JsonRpcResponse;
import com.qlmcp.backend.dto.Method;
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

    public JsonRpcResponse createResponse(JsonRpcRequest request) {
        if (request.getMethod() == Method.INITIALIZE) {
            return initialize(request.getId());
        }

        if (request.getMethod() == Method.NOTIFICATIONS_INITIALIZED) {
            return null;
        }

        if (request.getMethod() == Method.TOOLS_LIST) {
            return toolList(request.getId());
        }

        if (request.getMethod() == Method.TOOLS_CALL) {
            return callTools(request);
        }

        throw new CustomException(ErrorCode.METHOD_NOT_FOUND);
    }

    private JsonRpcResponse initialize(Object requestId) {
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

    private JsonRpcResponse toolList(Object requestId) {
        return JsonRpcResponse.builder()
            .id(requestId)
            .result(toolRegistry.getToolsList())
            .build();
    }

    private JsonRpcResponse callTools(JsonRpcRequest request) {
        return JsonRpcResponse.builder()
            .id(request.getId())
            .result(switchingTools(request.getParams()))
            .build();
    }

    private Map<String, Object> switchingTools(Object params) {
        Map<?, ?> arguments = parseArguments(params);

        ToolInterface tool = toolRegistry.getToolByName((String) ((Map<?, ?>) params).get("name"));

        if (tool != null) {
            try {
                return tool.call(arguments);
            } catch (Exception e) {
                throw new CustomException(ErrorCode.TOOL_EXECUTION_ERROR);
            }
        }

        throw new CustomException(ErrorCode.TOOL_NOT_FOUND);
    }

    private Map<?, ?> parseArguments(Object params) {
        if (params instanceof Map) {
            Object argumentsObj = ((Map<?, ?>) params).get("arguments");
            if (argumentsObj instanceof Map) {
                return (Map<?, ?>) argumentsObj;
            }
        }

        throw new CustomException(ErrorCode.INVALID_PARAMS);
    }
}
