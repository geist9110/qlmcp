package com.qlmcp.backend.tool;

import com.qlmcp.backend.config.AiProperties;
import com.qlmcp.backend.config.ToolMeta;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@ToolMeta(
    name = "query",
    description = "Query tool is used to answer questions related to QL MCP.",
    inputSchema = """
            {
                "type": "object",
                "properties": {
                    "question": {
                        "type": "string",
                        "description": "The question to be answered."
                    }
                },
                "required": ["question"]
            }
        """
)
@Component
@RequiredArgsConstructor
public class QueryTool {

    private final AiProperties aiProperties;
    private RestClient restClient = RestClient.create();

    public Map<String, Object> call(Map<?, ?> arguments) {
        return Map.of(
            "content", List.of(
                Map.of(
                    "type", "text",
                    "text", sendChat((String) arguments.get("question"))
                )
            ),
            "isError", false
        );
    }

    private String sendChat(String message) {
        String apiUrl = aiProperties.getBaseUrl() + "/" + aiProperties.getModel() + ":"
            + aiProperties.getType();

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put(
            "system_instruction",
            Map.of(
                "parts",
                List.of(Map.of("text", "You are a ql mcp assistant."))
            )
        );

        requestBody.put(
            "contents",
            List.of(
                Map.of(
                    "parts",
                    List.of(Map.of("text", message))
                )
            )
        );

        requestBody.put(
            "generationConfig",
            Map.of(
                "thinkingConfig",
                Map.of("thinkingBudget", 0)
            )
        );

        Map response = restClient.post()
            .uri(URI.create(apiUrl))
            .header("x-goog-api-key", aiProperties.getKey())
            .contentType(MediaType.APPLICATION_JSON)
            .body(requestBody)
            .retrieve()
            .body(Map.class);

        List<Map<String, Object>> candidates = (List<Map<String, Object>>) response.get(
            "candidates");
        if (candidates.size() == 1) {
            Map<String, Object> candidate = candidates.get(0);
        }

        Map<String, Object> content = (Map<String, Object>) candidates.get(0).get("content");
        List<Map<String, Object>> parts = (List<Map<String, Object>>) content.get("parts");
        return (String) parts.get(0).get("text");
    }
}
