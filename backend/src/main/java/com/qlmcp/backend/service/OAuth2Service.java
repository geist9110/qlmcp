package com.qlmcp.backend.service;

import com.qlmcp.backend.dto.OAuthProviderResponseDto;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class OAuth2Service {

    public List<OAuthProviderResponseDto> getProviders() {
        return List.of(
            new OAuthProviderResponseDto("google", "/oauth2/login/google"),
            new OAuthProviderResponseDto("github", "/oauth2/login/github")
        );
    }
}
