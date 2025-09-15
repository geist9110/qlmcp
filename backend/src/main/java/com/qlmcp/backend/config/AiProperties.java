package com.qlmcp.backend.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "ai.api")
public class AiProperties {

    private String key;
    private String baseUrl;
    private String model;
    private String type;
}
