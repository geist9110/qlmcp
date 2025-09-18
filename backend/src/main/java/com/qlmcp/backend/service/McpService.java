package com.qlmcp.backend.service;

import com.qlmcp.backend.config.McpProperties;
import com.qlmcp.backend.config.ToolRegistry;
import com.qlmcp.backend.dto.JsonRpcRequest.McpRequest;
import com.qlmcp.backend.dto.JsonRpcRequest.ToolsCallRequest;
import com.qlmcp.backend.dto.JsonRpcResponse.InitializeResponse;
import com.qlmcp.backend.dto.JsonRpcResponse.McpResponse;
import com.qlmcp.backend.dto.JsonRpcResponse.ToolsCallResponse;
import com.qlmcp.backend.dto.JsonRpcResponse.ToolsListResponse;
import com.qlmcp.backend.exception.CustomException;
import com.qlmcp.backend.exception.ErrorCode;
import com.qlmcp.backend.tool.ToolInterface;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class McpService {

    private final McpProperties mcpProperties;
    private final ToolRegistry toolRegistry;

    public McpResponse initialize(Object requestId) {
        return new InitializeResponse(
            requestId,
            mcpProperties.getProtocolVersion(),
            "Optional instructions for the client",
            Map.of(
                "tools", Map.of()
            ),
            Map.of(
                "name", mcpProperties.getServerName(),
                "version", mcpProperties.getServerVersion()
            )
        );
    }

    public McpResponse toolList(Object requestId) {
        return new ToolsListResponse(
            requestId,
            toolRegistry.getToolInformationList()
        );
    }

    public McpResponse callTools(McpRequest request) {
        ToolsCallRequest toolCallRequest = (ToolsCallRequest) request;
        ToolsCallRequest.Params params = toolCallRequest.getParams();
        ToolInterface tool = toolRegistry.getToolByName(params.getName());

        if (tool == null) {
            throw new CustomException(request.getId(), ErrorCode.TOOL_NOT_FOUND);
        }

        return new ToolsCallResponse(
            request.getId(),
            tool.call(request.getId(), params.getArguments())
        );
    }
}
