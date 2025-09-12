package com.qlmcp.backend.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "ai.mcp")
public class McpProperties {

    private String protocolVersion;
    private String serverName;
    private String serverVersion;
}
