package com.qlmcp.backend.service;

import java.util.Base64;
import java.util.List;

import com.qlmcp.backend.dto.AuthorizeDto;
import com.qlmcp.backend.dto.AuthorizeDto.CodeChallengeMethod;
import com.qlmcp.backend.dto.AuthorizeDto.ResponseType;
import com.qlmcp.backend.dto.ClientCredential;
import com.qlmcp.backend.dto.OAuthProviderResponseDto;
import com.qlmcp.backend.dto.TokenDto;
import com.qlmcp.backend.dto.TokenDto.GrantType;
import com.qlmcp.backend.entity.AuthorizationCode;
import com.qlmcp.backend.entity.Client;
import com.qlmcp.backend.entity.RefreshToken;
import com.qlmcp.backend.exception.CustomException;
import com.qlmcp.backend.exception.ErrorCode;
import com.qlmcp.backend.property.JwtProperties;
import com.qlmcp.backend.repository.AuthorizationCodeRepository;
import com.qlmcp.backend.repository.ClientRepository;
import com.qlmcp.backend.repository.RefreshTokenRepository;
import com.qlmcp.backend.util.JwtTokenProvider;
import com.qlmcp.backend.util.PkceVerifier;

import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OAuth2Service {

    private final ClientRepository clientRepository;
    private final AuthorizationCodeRepository authorizationCodeRepository;
    private final PkceVerifier pkceVerifier;
    private final JwtTokenProvider jwtTokenProvider;
    private final JwtProperties jwtProperties;
    private final RefreshTokenRepository refreshTokenRepository;

    public List<OAuthProviderResponseDto> getProviders() {
        return List.of(
                new OAuthProviderResponseDto("google", "/oauth2/login/google"),
                new OAuthProviderResponseDto("github", "/oauth2/login/github"));
    }

    public AuthorizeDto.Response getAuthorizeCode(AuthorizeDto.Command command) {
        validateResponseType(command.getResponseType());

        Client client = clientRepository
                .findByClientId(command.getClientId())
                .orElseThrow(() -> CustomException.badRequest(ErrorCode.INVALID_CLIENT));

        validateRedirectUris(client, command.getRedirectUri());
        validateCodeChallengeMethod(command.getCodeChallengeMethod(), command.getRedirectUri(), command.getState());

        AuthorizationCode authCode = createAuthorizationCode(command);
        authorizationCodeRepository.save(authCode);

        return AuthorizeDto.Response.builder()
                .authCode(authCode.getCode())
                .redirectUri(authCode.getRedirectUri())
                .state(authCode.getState())
                .build();
    }

    private void validateResponseType(ResponseType responseType) {
        if (responseType != ResponseType.CODE) {
            throw CustomException.badRequest(ErrorCode.UNSUPPORTED_RESPONSE_TYPE);
        }
    }

    private void validateRedirectUris(Client client, String redirectUri) {
        if (!client.getRedirectUris().contains(redirectUri)) {
            throw CustomException.badRequest(ErrorCode.INVALID_REDIRECT_URI);
        }
    }

    private void validateCodeChallengeMethod(CodeChallengeMethod codeChallengeMethod, String redirectUri,
            String state) {
        if (codeChallengeMethod != CodeChallengeMethod.S256) {
            UriComponentsBuilder builder = UriComponentsBuilder
                    .fromUriString(redirectUri)
                    .queryParam("state", state);

            throw CustomException.redirect(
                    ErrorCode.PKCE_REQUIRED,
                    builder.build().toUriString());
        }
    }

    private AuthorizationCode createAuthorizationCode(AuthorizeDto.Command command) {
        return new AuthorizationCode(
                command.getUserName(),
                command.getClientId(),
                command.getAuthProvider(),
                command.getRedirectUri(),
                command.getCodeChallenge(),
                command.getCodeChallengeMethod().toString(),
                command.getScope(),
                command.getState());
    }

    public TokenDto.Response getToken(TokenDto.Command command) {
        ClientCredential clientCredential = getClientCredential(command.getAuthHeader(), command.getClientId(),
                command.getClientSecret());

        Client client = authenticateClient(clientCredential);

        return switch (command.getGrantType()) {
            case GrantType.AUTHORIZATION_CODE ->
                handleAuthorizationCodeGrant(command.getCode(), command.getCodeVerifier(), command.getRedirectUri(),
                        client);
            case GrantType.REFRESH_TOKEN -> handleRefreshTokenGrant(command.getRefreshTokenValue(), client);
            default -> throw CustomException.badRequest(ErrorCode.INVALID_GRANT_TYPE);
        };
    }

    private ClientCredential getClientCredential(
            String authHeader,
            String clientId,
            String clientSecret) {
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

    private TokenDto.Response handleAuthorizationCodeGrant(
            String code,
            String codeVerifier,
            String redirectUri,
            Client client) {
        AuthorizationCode authCode = authorizationCodeRepository
                .findByCode(code)
                .orElseThrow(() -> CustomException.badRequest(ErrorCode.INVALID_CODE));

        if (!authCode.isValid()
                || !authCode.getClientId().equals(client.getClientId())
                || !authCode.getRedirectUri().equals(redirectUri)
                || codeVerifier == null
                || !pkceVerifier.verify(authCode.getCodeChallenge(), authCode.getCodeChallengeMethod(),
                        codeVerifier)) {
            authorizationCodeRepository.delete(authCode);
            throw CustomException.badRequest(ErrorCode.INVALID_CODE);
        }

        authCode.markUsed();
        authorizationCodeRepository.save(authCode);

        String accessToken = jwtTokenProvider
                .generateAccessToken(
                        authCode.getUsername(),
                        client.getClientId(),
                        authCode.getScope(),
                        authCode.getAuthProvider());

        RefreshToken refreshToken = new RefreshToken(
                authCode.getUsername(),
                client.getClientId(),
                authCode.getScope(),
                jwtProperties.getRefreshTokenValidity(),
                authCode.getAuthProvider());
        refreshTokenRepository.save(refreshToken);

        return TokenDto.Response.builder()
                .accessToken(accessToken)
                .tokenType("Bearer")
                .expiresIn(jwtTokenProvider.getAccessTokenValidity())
                .refreshToken(refreshToken.getToken())
                .scope(authCode.getScope())
                .build();
    }

    private TokenDto.Response handleRefreshTokenGrant(String refreshTokenValue, Client client) {
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
                refreshToken.getScope(),
                refreshToken.getAuthProvider());

        return TokenDto.Response.builder()
                .accessToken(accessToken)
                .tokenType("Bearer")
                .expiresIn(jwtTokenProvider.getAccessTokenValidity())
                .refreshToken(refreshToken.getToken())
                .scope(refreshToken.getScope())
                .build();
    }
}
