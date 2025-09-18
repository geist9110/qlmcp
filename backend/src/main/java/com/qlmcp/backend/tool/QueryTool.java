package com.qlmcp.backend.tool;

import com.qlmcp.backend.config.AiProperties;
import com.qlmcp.backend.config.ToolMeta;
import com.qlmcp.backend.dto.GeminiApiRequest;
import com.qlmcp.backend.dto.GeminiApiRequest.Content;
import com.qlmcp.backend.dto.GeminiApiRequest.GenerationConfig;
import com.qlmcp.backend.dto.GeminiApiRequest.GenerationConfig.ThinkingConfig;
import com.qlmcp.backend.dto.GeminiApiRequest.SystemInstruction;
import com.qlmcp.backend.dto.GeminiApiRequest.Tool;
import com.qlmcp.backend.dto.GeminiApiRequest.Tool.FunctionDeclaration;
import com.qlmcp.backend.dto.GeminiApiResponse;
import com.qlmcp.backend.dto.GeminiApiResponse.Candidate.Content.Part;
import com.qlmcp.backend.dto.GeminiApiResponse.Candidate.Content.TextPart;
import java.net.URI;
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
public class QueryTool implements ToolInterface {

    private final AiProperties aiProperties;
    private final RestClient restClient = RestClient.create();

    @Override
    public List<Object> call(Object id, Map<?, ?> arguments) {
        return List.of(
            Map.of(
                "type", "text",
                "text", sendChat(
                    new Content("user", (String) arguments.get("question"))
                )
            )
        );
    }

    private String sendChat(Content content) {
        String apiUrl = aiProperties.getBaseUrl() + "/" + aiProperties.getModel() + ":"
            + aiProperties.getType();

        GeminiApiRequest requestBody = GeminiApiRequest
            .builder()
            .systemInstruction(
                new SystemInstruction("You are a ql mcp assistant.")
            )
            .contents(
                List.of(content)
            )
            .generationConfig(
                new GenerationConfig(
                    new ThinkingConfig(0)
                )
            )
            .tools(
                List.of(
                    new Tool(
                        new FunctionDeclaration(
                            "get_nowcast_observation",
                            "특정 위경도의 날씨 정보를 가져옵니다. Args: lon (float): 경도 값 lat (float): 위도 값",
                            Map.of(
                                "type", "object",
                                "properties", Map.of(
                                    "lon", Map.of(
                                        "title", "Lon",
                                        "type", "number"
                                    ),
                                    "lat", Map.of(
                                        "title", "Lat",
                                        "type", "number"
                                    )
                                ),
                                "required", List.of("lon", "lat")
                            )
                        )
                    )
                )
            )
            .build();

        GeminiApiResponse response = restClient.post()
            .uri(URI.create(apiUrl))
            .header("x-goog-api-key", aiProperties.getKey())
            .contentType(MediaType.APPLICATION_JSON)
            .body(requestBody)
            .retrieve()
            .body(GeminiApiResponse.class);

        assert response != null;
        List<Part> parts = response.getCandidates().getFirst().getContent().getParts();

        Part part = parts.getFirst();
        if (part instanceof TextPart) {
            return ((TextPart) part).getText();
        }
        return "";
    }
}
