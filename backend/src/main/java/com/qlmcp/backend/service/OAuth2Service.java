package com.qlmcp.backend.service;

import com.qlmcp.backend.dto.AuthorizeDto;
import com.qlmcp.backend.dto.ClientCredential;
import com.qlmcp.backend.dto.OAuthProviderResponseDto;
import com.qlmcp.backend.dto.TokenDto;
import com.qlmcp.backend.entity.AuthorizationCode;
import com.qlmcp.backend.entity.Client;
import com.qlmcp.backend.entity.RefreshToken;
import com.qlmcp.backend.exception.CustomException;
import com.qlmcp.backend.exception.ErrorCode;
import com.qlmcp.backend.repository.AuthorizationCodeRepository;
import com.qlmcp.backend.repository.ClientRepository;
import com.qlmcp.backend.repository.RefreshTokenRepository;
import com.qlmcp.backend.util.JwtTokenProvider;
import com.qlmcp.backend.util.PkceVerifier;
import java.util.Base64;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

@Service
@RequiredArgsConstructor
public class OAuth2Service {

    private final ClientRepository clientRepository;
    private final AuthorizationCodeRepository authorizationCodeRepository;
    private final PkceVerifier pkceVerifier;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;

    @Value("${jwt.refresh-token-validity}")
    private int refreshTokenValidity;

    public List<OAuthProviderResponseDto> getProviders() {
        return List.of(
            new OAuthProviderResponseDto("google", "/oauth2/login/google"),
            new OAuthProviderResponseDto("github", "/oauth2/login/github")
        );
    }

    public String getAuthorizeCode(AuthorizeDto authorizeDto) {
        if (!authorizeDto.getResponseType().equals("code")) {
            throw CustomException.badRequest(ErrorCode.UNSUPPORTED_RESPONSE_TYPE);
        }

        Client client = clientRepository
            .findByClientId(authorizeDto.getClientId())
            .orElseThrow(() -> CustomException.badRequest(ErrorCode.INVALID_CLIENT));

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

    public TokenDto getToken(String grantType, String code, String codeVerifier, String redirectUri,
        String clientId, String clientSecret, String refreshTokenValue, String authHeader) {
        ClientCredential clientCredential = getClientCredential(authHeader, clientId, clientSecret);

        Client client = authenticateClient(clientCredential);

        if (grantType.equals("authorization_code")) {
            return handleAuthorizationCodeGrant(code, codeVerifier, redirectUri, client);
        }

        if (grantType.equals("refresh_token")) {
            return handleRefreshTokenGrant(refreshTokenValue, client);
        }

        throw CustomException.badRequest(ErrorCode.INVALID_GRANT_TYPE);
    }

    private ClientCredential getClientCredential(
        String authHeader,
        String clientId,
        String clientSecret
    ) {
        if (authHeader != null && authHeader.startsWith("Basic ")) {
            String base64Credentials = authHeader.replace("Basic ", "");
            String credentials = new String(Base64.getDecoder().decode(base64Credentials));
            String[] parts = credentials.split(":");

            return ClientCredential.builder()
                .clientId(parts[0])
                .clientSecret(parts.length > 1 ? parts[1] : null)
                .build();
        }

        return ClientCredential.builder()
            .clientId(clientId)
            .clientSecret(clientSecret)
            .build();
    }

    private Client authenticateClient(ClientCredential clientCredential) {
        if (clientCredential.getClientId() == null) {
            throw CustomException.badRequest(ErrorCode.INVALID_CLIENT);
        }

        Client client = clientRepository
            .findByClientId(clientCredential.getClientId())
            .orElseThrow(() -> CustomException.badRequest(ErrorCode.INVALID_CLIENT));

        if (client.getClientSecret() == null) {
            throw CustomException.badRequest(ErrorCode.INVALID_CLIENT);
        }

        String storedClientSecret = client
            .getClientSecret()
            .replace("{noop}", "");

        if (!storedClientSecret.equals(clientCredential.getClientSecret())) {
            throw CustomException.badRequest(ErrorCode.INVALID_CLIENT);
        }

        return client;
    }

    private TokenDto handleAuthorizationCodeGrant(
        String code,
        String codeVerifier,
        String redirectUri,
        Client client
    ) {
        AuthorizationCode authCode = authorizationCodeRepository
            .findByCode(code)
            .orElseThrow(() -> CustomException.badRequest(ErrorCode.INVALID_CODE));

        if (!authCode.isValid()
            || !authCode.getClientId().equals(client.getClientId())
            || !authCode.getRedirectUri().equals(redirectUri)
            || codeVerifier == null
            || !pkceVerifier.verify(authCode.getCodeChallenge(), authCode.getCodeChallengeMethod(),
            codeVerifier)
        ) {
            authorizationCodeRepository.delete(authCode);
            throw CustomException.badRequest(ErrorCode.INVALID_CODE);
        }

        authCode.markUsed();
        authorizationCodeRepository.save(authCode);

        String accessToken = jwtTokenProvider
            .generateAccessToken(
                authCode.getUsername(),
                client.getClientId(),
                authCode.getScope()
            );

        RefreshToken refreshToken = new RefreshToken(
            authCode.getUsername(),
            client.getClientId(),
            authCode.getScope(),
            refreshTokenValidity
        );
        refreshTokenRepository.save(refreshToken);

        return TokenDto.builder()
            .accessToken(accessToken)
            .tokenType("Bearer")
            .expiresIn(jwtTokenProvider.getAccessTokenValidity())
            .refreshToken(refreshToken.getToken())
            .scope(authCode.getScope())
            .build();
    }

    private TokenDto handleRefreshTokenGrant(String refreshTokenValue, Client client) {
        RefreshToken refreshToken = refreshTokenRepository
            .findByToken(refreshTokenValue)
            .orElseThrow(() -> CustomException.badRequest(ErrorCode.INVALID_TOKEN));

        if (!refreshToken.getClientId().equals(client.getClientId())) {
            throw CustomException.badRequest(ErrorCode.INVALID_TOKEN);
        }

        if (!refreshToken.isValid()) {
            throw CustomException.badRequest(ErrorCode.REFRESH_TOKEN_EXPIRED);
        }

        String accessToken = jwtTokenProvider.generateAccessToken(
            refreshToken.getUsername(),
            client.getClientId(),
            refreshToken.getScope()
        );

        return TokenDto.builder()
            .accessToken(accessToken)
            .tokenType("Bearer")
            .expiresIn(jwtTokenProvider.getAccessTokenValidity())
            .refreshToken(refreshToken.getToken())
            .scope(refreshToken.getScope())
            .build();
    }
}
