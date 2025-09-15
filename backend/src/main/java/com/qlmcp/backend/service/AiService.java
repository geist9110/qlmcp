package com.qlmcp.backend.service;

import com.qlmcp.backend.config.AiProperties;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
@RequiredArgsConstructor
public class AiService {

    private final AiProperties aiProperties;
    private RestClient restClient = RestClient.create();

    public String sendChat(String message) {
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
