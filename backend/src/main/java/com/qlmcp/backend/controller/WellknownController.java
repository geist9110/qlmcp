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

    @GetMapping("/oauth-authorization-server")
    public ResponseEntity<Map<String, Object>> getAuthorizationServerMetadata(
        HttpServletRequest request
    ) {
        log.info("=== Authorization Server Metadata Request START ===");

        String baseUrl = request.getScheme() + "://"
            + request.getServerName() + ":"
            + request.getServerPort();

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("issuer", baseUrl);
        metadata.put("authorization_endpoint", baseUrl + "/oauth2/authorize");
        metadata.put("token_endpoint", baseUrl + "/oauth2/token");
        metadata.put("jwks_uri", baseUrl + "/oauth2/jwks");
        metadata.put("revocation_endpoint", baseUrl + "/oauth2/revoke");
        metadata.put("introspection_endpoint", baseUrl + "/oauth2/introspect");

        metadata.put("response_types_supported", List.of("code"));
        metadata.put("grant_types_supported", List.of("authorization_code", "refresh_token"));
        metadata.put("token_endpoint_auth_methods_supported",
            List.of("client_secret_basic", "client_secret_post"));
        metadata.put("code_challenge_methods_supported", List.of("S256"));
        metadata.put("scopes_supported", List.of("mcp.read", "mcp.write"));

        log.info("Returning metadata: {}", metadata);
        log.info("=== Authorization Server Metadata Request END ===");
        return ResponseEntity.ok(metadata);
    }
}
