package com.qlmcp.backend.controller;

import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/.well-known")
public class WellknownController {

    @GetMapping("/oauth-protected-resource")
    public ResponseEntity<Map<String, Object>> getProtectedResourceMetadata(
        HttpServletRequest request
    ) {
        log.info("=== Protected Resource Metadata Request START ===");

        String baseUrl = request.getScheme() + "://"
            + request.getServerName() + ":"
            + request.getServerPort();

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("resource", baseUrl + "/mcp");
        metadata.put("authorization_servers", List.of(baseUrl));

        log.info("Returning metadata: {}", metadata);
        log.info("=== Protected Resource Metadata Request END ===");
        return ResponseEntity.ok(metadata);
    }
}
