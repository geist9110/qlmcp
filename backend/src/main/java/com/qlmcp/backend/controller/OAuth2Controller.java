package com.qlmcp.backend.controller;

import com.qlmcp.backend.dto.OAuthProviderResponseDto;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/oauth2")
@RequiredArgsConstructor
public class OAuth2Controller {

    @GetMapping("/providers")
    public ResponseEntity<List<OAuthProviderResponseDto>> getProviders() {
        return ResponseEntity
            .ok(
                List.of(
                    new OAuthProviderResponseDto("google", "/oauth2/login/google"),
                    new OAuthProviderResponseDto("github", "/oauth2/login/github")
                )
            );
    }
}
