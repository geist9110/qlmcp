package com.qlmcp.backend.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "spring.ai.openai.chat.options.prompt")
public class PromptConfig {

    @JsonProperty("system-prompt")
    private String systemPrompt;

    @JsonProperty("user-prompt-start")
    private String userPromptStart;

    @JsonProperty("user-prompt-end")
    private String userPromptEnd;

    @JsonProperty("log-tag")
    private String logTag;
}
