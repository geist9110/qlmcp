package com.qlmcp.backend.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "spring.ai.openai.chat.options.prompt")
public class PromptConfig {

    private String systemPrompt;

    private String userPromptStart;

    private String userPromptEnd;

    private String logTag;
}
