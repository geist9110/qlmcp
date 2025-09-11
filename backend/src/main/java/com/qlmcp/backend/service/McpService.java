package com.qlmcp.backend.service;

import com.qlmcp.backend.config.McpProperties;
import com.qlmcp.backend.config.ToolRegistry;
import com.qlmcp.backend.dto.McpRequest;
import com.qlmcp.backend.dto.McpResponse;
import com.qlmcp.backend.dto.Method;
import com.qlmcp.backend.exception.CustomException;
import com.qlmcp.backend.exception.ErrorCode;
import com.qlmcp.backend.tool.GetWeatherTool;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class McpService {

    private final McpProperties mcpProperties;
    private final ToolRegistry toolRegistry;
    private final GetWeatherTool getWeatherTool;

    public McpResponse createResponse(McpRequest request) {
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

    private McpResponse initialize(Object requestId) {
        Map<String, Object> initializeResult = Map.of(
            "protocolVersion", mcpProperties.getProtocolVersion(),
            "capabilities", Map.of(
                "tools", Map.of()
            ),
            "serverInfo", Map.of(
                "name", mcpProperties.getServerName(),
                "version", mcpProperties.getServerVersion()
            )
        );

        return McpResponse.builder()
            .id(requestId)
            .result(initializeResult)
            .build();
    }

    private McpResponse toolList(Object requestId) {
        return McpResponse.builder()
            .id(requestId)
            .result(toolRegistry.getToolsList())
            .build();
    }

    private McpResponse callTools(McpRequest request) {
        Map<String, Object> toolResult = switchingTools(request.getParams());

        return McpResponse.builder()
            .id(request.getId())
            .result(toolResult)
            .build();
    }

    private Map<String, Object> switchingTools(Object params) {
        String toolName = (String) ((Map<?, ?>) params).get("name");
        Map<?, ?> arguments = parseArguments(params);

        if (toolName.equals("get_weather")) {
            return getWeatherTool.call(arguments);
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
