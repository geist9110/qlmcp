package com.qlmcp.backend.controller;

import com.qlmcp.backend.dto.OAuthProviderResponseDto;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @GetMapping("/providers")
    public ResponseEntity<List<OAuthProviderResponseDto>> getOAuthProviders() {
        List<OAuthProviderResponseDto> providers = List.of(
            new OAuthProviderResponseDto("google", "/oauth2/authorization/google"),
            new OAuthProviderResponseDto("github", "/oauth2/authorization/github")
        );
        return ResponseEntity.ok(providers);
    }
}
