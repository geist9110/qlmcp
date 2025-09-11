package com.qlmcp.backend.service;

import com.qlmcp.backend.config.McpProperties;
import com.qlmcp.backend.dto.McpRequest;
import com.qlmcp.backend.dto.McpResponse;
import com.qlmcp.backend.dto.Method;
import com.qlmcp.backend.exception.CustomException;
import com.qlmcp.backend.exception.ErrorCode;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class McpService {

    private final McpProperties mcpProperties;

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
            Object paramsObj = request.getParams();
            String city = null;
            if (paramsObj instanceof Map) {
                Object argumentsObj = ((Map<?, ?>) paramsObj).get("arguments");
                if (argumentsObj instanceof Map) {
                    Object cityObj = ((Map<?, ?>) argumentsObj).get("city");
                    if (cityObj instanceof String) {
                        city = (String) cityObj;
                    }
                }
            }

            if (city == null || city.isEmpty()) {
                return McpResponse.builder()
                    .id(request.getId())
                    .error(Map.of(
                        "code", -32602,
                        "message", "Invalid params: 'city' is required"
                    ))
                    .build();
            }

            return McpResponse.builder()
                .id(request.getId())
                .result(createWeatherResult(city))
                .build();
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
        Map<String, Object> toolListResult = Map.of(
            "tools", List.of(
                Map.of(
                    "name", "get_weather",
                    "description", "Get weather information",
                    "inputSchema", Map.of(
                        "type", "object",
                        "properties", Map.of(
                            "city", Map.of(
                                "type", "string",
                                "description", "City name"
                            )
                        ),
                        "required", List.of("city")
                    )
                )
            )
        );

        return McpResponse.builder()
            .id(requestId)
            .result(toolListResult)
            .build();
    }

    private Map<String, Object> createWeatherResult(String city) {
        return Map.of(
            "content", List.of(
                Map.of(
                    "type", "text",
                    "text", String.format("Sunnyday in %s", city)
                )
            ),
            "isError", false
        );
    }
}
