package com.qlmcp.backend.service;

import com.qlmcp.backend.dto.AuthorizeDto;
import com.qlmcp.backend.dto.OAuthProviderResponseDto;
import com.qlmcp.backend.entity.AuthorizationCode;
import com.qlmcp.backend.exection.CustomException;
import com.qlmcp.backend.exection.ErrorCode;
import com.qlmcp.backend.repository.AuthorizationCodeRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

@Service
@RequiredArgsConstructor
public class OAuth2Service {

    private final RegisteredClientRepository registeredClientRepository;
    private final AuthorizationCodeRepository authorizationCodeRepository;

    public List<OAuthProviderResponseDto> getProviders() {
        return List.of(
            new OAuthProviderResponseDto("google", "/oauth2/login/google"),
            new OAuthProviderResponseDto("github", "/oauth2/login/github")
        );
    }

    public String getAuthorizeCode(
        AuthorizeDto authorizeDto
    ) {
        if (!authorizeDto.getResponseType().equals("code")) {
            throw CustomException.badRequest(ErrorCode.UNSUPPORTED_RESPONSE_TYPE);
        }

        RegisteredClient client = registeredClientRepository.findByClientId(
            authorizeDto.getClientId());
        if (client == null) {
            throw CustomException.badRequest(ErrorCode.INVALID_CLIENT);
        }

        if (!client.getRedirectUris().contains(authorizeDto.getRedirectUri())) {
            throw CustomException.badRequest(ErrorCode.INVALID_REDIRECT_URI);
        }

        UriComponentsBuilder builder = UriComponentsBuilder
            .fromUriString(authorizeDto.getRedirectUri())
            .queryParam("state", authorizeDto.getState());

        if (authorizeDto.getCodeChallenge() == null
            || authorizeDto.getCodeChallengeMethod() == null) {
            throw CustomException.redirect(
                ErrorCode.PKCE_REQUIRED,
                builder.build().toUriString()
            );
        }

        if (!authorizeDto.getCodeChallengeMethod().equals("S256")
            && !authorizeDto.getCodeChallengeMethod().equals("plain")) {
            throw CustomException.redirect(
                ErrorCode.PKCE_REQUIRED,
                builder.build().toUriString()
            );
        }

        AuthorizationCode authCode = new AuthorizationCode(
            authorizeDto.getUserName(),
            authorizeDto.getClientId(),
            authorizeDto.getRedirectUri(),
            authorizeDto.getCodeChallenge(),
            authorizeDto.getCodeChallengeMethod(),
            authorizeDto.getScope(),
            authorizeDto.getState()
        );
        authorizationCodeRepository.save(authCode);
        return authCode.getCode();
    }
}
