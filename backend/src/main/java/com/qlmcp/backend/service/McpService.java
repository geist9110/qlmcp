package com.qlmcp.backend.service;

import com.qlmcp.backend.dto.McpRequest;
import com.qlmcp.backend.dto.McpResponse;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class McpService {

    public McpResponse createResponse(McpRequest request) {
        if (request.getMethod().equals("initialize")) {
            return McpResponse.builder()
                .id(request.getId())
                .result(createInitializeResult())
                .build();
        }

        if (request.getMethod().equals("notifications/initialized")) {
            return null;
        }

        if (request.getMethod().equals("tools/list")) {
            return McpResponse.builder()
                .id(request.getId())
                .result(createToolsListResult())
                .build();
        }

        if (request.getMethod().equals("tools/call")) {
            return McpResponse.builder()
                .id(request.getId())
                .result(createWeatherResult("Seoul"))
                .build();
        }

        return McpResponse.builder()
            .id(request.getId())
            .error(Map.of(
                "code", -32601,
                "message", "Method not found: " + request.getMethod()
            ))
            .build();
    }

    private Map<String, Object> createInitializeResult() {
        return Map.of(
            "protocolVersion", "2025-06-18",
            "capabilities", Map.of(
                "tools", Map.of()
            ),
            "serverInfo", Map.of(
                "name", "qlmcp-server",
                "version", "1.0.0"
            )
        );
    }

    private Map<String, Object> createToolsListResult() {
        return Map.of(
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
