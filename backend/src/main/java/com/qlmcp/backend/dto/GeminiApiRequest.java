package com.qlmcp.backend.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class GeminiApiRequest {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("system_instruction")
    private final SystemInstruction systemInstruction;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private final List<Content> contents;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private final GenerationConfig generationConfig;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private final List<Tool> tools;

    @Getter
    public static class SystemInstruction {

        private final List<Map<String, String>> parts;

        public SystemInstruction(String... parts) {
            this.parts = Arrays.stream(parts)
                .map(part -> Map.of("text", part))
                .toList();
        }
    }

    @Getter
    public static class Content {

        private final String role;
        private final List<Map<String, String>> parts;

        public Content(String role, String... parts) {
            this.role = role;
            this.parts = Arrays.stream(parts)
                .map(part -> Map.of("text", part))
                .toList();
        }
    }

    public record GenerationConfig(
        @JsonInclude(JsonInclude.Include.NON_NULL)
        GeminiApiRequest.GenerationConfig.ThinkingConfig thinkingConfig
    ) {

        public GenerationConfig(ThinkingConfig thinkingConfig) {
            this.thinkingConfig = thinkingConfig;
        }

        public record ThinkingConfig(int thinkingBudget) {

        }
    }

    @Getter
    public static class Tool {

        private final List<FunctionDeclaration> functionDeclarations;

        public Tool(FunctionDeclaration... functionDeclarations) {
            this.functionDeclarations = Arrays.asList(functionDeclarations);
        }

        public record FunctionDeclaration(
            String name,
            String description,
            Map<String, Object> parameters
        ) {

        }
    }
}
