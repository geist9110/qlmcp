package com.qlmcp.backend.controller;

import com.qlmcp.backend.dto.OAuthProviderResponseDto;
import com.qlmcp.backend.entity.AuthorizationCode;
import com.qlmcp.backend.entity.RefreshToken;
import com.qlmcp.backend.repository.AuthorizationCodeRepository;
import com.qlmcp.backend.repository.RefreshTokenRepository;
import com.qlmcp.backend.util.JwtTokenProvider;
import com.qlmcp.backend.util.PkceVerifier;
import java.security.Principal;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2AuthorizationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.util.UriComponentsBuilder;

@Slf4j
@Controller
@RequestMapping("/oauth2")
@RequiredArgsConstructor
public class OAuth2Controller {

    private final RegisteredClientRepository registeredClientRepository;
    private final AuthorizationCodeRepository authorizationCodeRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final PkceVerifier pkceVerifier;

    @Value("${jwt.refresh-token-validity}")
    private int refreshTokenValidity;

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

    @GetMapping("/authorize")
    public String authorize(
        @RequestParam("client_id") String clientId,
        @RequestParam("redirect_uri") String redirectUri,
        @RequestParam("code_challenge") String codeChallenge,
        @RequestParam("code_challenge_method") String codeChallengeMethod,
        @RequestParam("response_type") String responseType,
        @RequestParam(required = false) String scope,
        @RequestParam(required = false) String state,
        @RequestParam(required = false) String resource,
        Principal principal
    ) {
        log.info("=== Authorization Request START ===");
        log.info("Client ID: {}", clientId);
        log.info("Redirect URI: {}", redirectUri);
        log.info("Authenticated Principal: {}", principal.getName());

        if (!"code".equals(responseType)) {
            return redirectWithError(
                redirectUri,
                "unsupported_response_type",
                "Only 'code' response type is supported",
                state
            );
        }

        RegisteredClient client = registeredClientRepository.findByClientId(clientId);
        if (client == null) {
            return redirectWithError(
                redirectUri,
                "invalid_client",
                "Client not found",
                state
            );
        }

        if (!client.getRedirectUris().contains(redirectUri)) {
            throw new OAuth2AuthenticationException("invalid_redirect_uri");
        }

        if (codeChallenge == null || codeChallengeMethod == null) {
            return redirectWithError(
                redirectUri,
                "invalid_request",
                "PKCE parameters are required",
                state
            );
        }

        if (!"S256".equals(codeChallengeMethod)
            && !"plain".equals(codeChallengeMethod)) {
            return redirectWithError(
                redirectUri,
                "invalid_request",
                "Unsupported code_challenge_method",
                state
            );
        }

        AuthorizationCode authCode = new AuthorizationCode(
            principal.getName(),
            clientId,
            redirectUri,
            codeChallenge,
            codeChallengeMethod,
            scope,
            state
        );
        authorizationCodeRepository.save(authCode);

        log.info(
            "Authorization Code Issued - code: {}, user: {}",
            authCode.getCode(),
            principal.getName()
        );
        log.info("=== Authorization Request END ===");

        return "redirect:" + buildRedirectUri(
            redirectUri,
            authCode.getCode(),
            state
        );
    }

    private String buildRedirectUri(String redirectUri, String code, String state) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(redirectUri)
            .queryParam("code", code);

        if (state != null) {
            builder.queryParam("state", state);
        }

        return builder.toUriString();
    }

    private String redirectWithError(String redirectUri, String error,
        String errorDescription, String state) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(redirectUri)
            .queryParam("error", error)
            .queryParam("error_description", errorDescription);

        if (state != null) {
            builder.queryParam("state", state);
        }

        return "redirect:" + builder.toUriString();
    }

    @PostMapping("/token")
    public ResponseEntity<?> token(
        @RequestParam("grant_type") String grantType,
        @RequestParam(value = "code", required = false) String code,
        @RequestParam(value = "code_verifier", required = false) String codeVerifier,
        @RequestParam(value = "redirect_uri", required = false) String redirectUri,
        @RequestParam(value = "client_id", required = false) String clientId,
        @RequestParam(value = "client_secret", required = false) String clientSecret,
        @RequestParam(value = "refresh_token", required = false) String refreshTokenValue,
        @RequestHeader(value = "Authorization", required = false) String authHeader
    ) {
        log.info("Token request - grant_type: {}", grantType);

        try {
            // 클라이언트 인증
            ClientCredentials credentials = extractClientCredentials(
                authHeader, clientId,
                clientSecret);
            RegisteredClient client = authenticateClient(credentials);

            if ("authorization_code".equals(grantType)) {
                return handleAuthorizationCodeGrant(code, codeVerifier, redirectUri, client);
            } else if ("refresh_token".equals(grantType)) {
                return handleRefreshTokenGrant(refreshTokenValue, client);
            } else {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "unsupported_grant_type"));
            }

        } catch (OAuth2AuthorizationException e) {
            log.error("Token error: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("error", "invalid_client", "error_description", e.getMessage()));
        } catch (Exception e) {
            log.error("Token error", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "server_error"));
        }
    }

    private ResponseEntity<?> handleAuthorizationCodeGrant(
        String code, String codeVerifier, String redirectUri, RegisteredClient client) {

        // 1. Authorization Code 조회
        AuthorizationCode authCode = authorizationCodeRepository.findByCode(code)
            .orElseThrow(() -> new OAuth2AuthorizationException(new OAuth2Error(
                "Invalid authorization code"))
            );

        // 2. 유효성 검증
        if (!authCode.isValid()) {
            authorizationCodeRepository.delete(authCode);
            throw new OAuth2AuthorizationException(
                new OAuth2Error("Authorization code expired or used"));
        }

        // 3. 클라이언트 일치 검증
        if (!authCode.getClientId().equals(client.getClientId())) {
            throw new OAuth2AuthorizationException(new OAuth2Error("Client mismatch"));
        }

        // 4. redirect_uri 검증
        if (!authCode.getRedirectUri().equals(redirectUri)) {
            throw new OAuth2AuthorizationException(new OAuth2Error("Redirect URI mismatch"));
        }

        // 5. PKCE 검증
        if (codeVerifier == null) {
            throw new OAuth2AuthorizationException(new OAuth2Error("code_verifier required"));
        }

        if (!pkceVerifier.verify(authCode.getCodeChallenge(),
            authCode.getCodeChallengeMethod(),
            codeVerifier)) {
            authorizationCodeRepository.delete(authCode);
            throw new OAuth2AuthorizationException(new OAuth2Error("Invalid code_verifier"));
        }

        // 6. Authorization Code 사용 처리 (1회용!)
        authCode.markUsed();
        authorizationCodeRepository.save(authCode);

        // 7. 토큰 생성
        String accessToken = jwtTokenProvider.generateAccessToken(
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

        log.info("Access token issued - user: {}, client: {}",
            authCode.getUsername(), client.getClientId());

        // 8. 응답
        return ResponseEntity.ok(Map.of(
            "access_token", accessToken,
            "token_type", "Bearer",
            "expires_in", jwtTokenProvider.getAccessTokenValidity(),
            "refresh_token", refreshToken.getToken(),
            "scope", authCode.getScope() != null ? authCode.getScope() : ""
        ));
    }

    private ResponseEntity<?> handleRefreshTokenGrant(
        String refreshTokenValue, RegisteredClient client) {

        // 1. Refresh Token 조회
        RefreshToken refreshToken = refreshTokenRepository.findByToken(refreshTokenValue)
            .orElseThrow(
                () -> new OAuth2AuthorizationException(new OAuth2Error("Invalid refresh token")));

        // 2. 유효성 검증
        if (!refreshToken.isValid()) {
            refreshTokenRepository.delete(refreshToken);
            throw new OAuth2AuthorizationException(
                new OAuth2Error("Refresh token expired or revoked"));
        }

        // 3. 클라이언트 일치 검증
        if (!refreshToken.getClientId().equals(client.getClientId())) {
            throw new OAuth2AuthorizationException(new OAuth2Error("Client mismatch"));
        }

        // 4. 새 Access Token 생성
        String accessToken = jwtTokenProvider.generateAccessToken(
            refreshToken.getUsername(),
            client.getClientId(),
            refreshToken.getScope()
        );

        log.info("Access token refreshed - user: {}, client: {}",
            refreshToken.getUsername(), client.getClientId());

        // 5. 응답 (refresh token은 재사용)
        return ResponseEntity.ok(Map.of(
            "access_token", accessToken,
            "token_type", "Bearer",
            "expires_in", jwtTokenProvider.getAccessTokenValidity(),
            "refresh_token", refreshTokenValue,
            "scope", refreshToken.getScope() != null ? refreshToken.getScope() : ""
        ));
    }

    private ClientCredentials extractClientCredentials(
        String authHeader, String clientId, String clientSecret) {

        // Basic Auth 헤더에서 추출
        if (authHeader != null && authHeader.startsWith("Basic ")) {
            String base64Credentials = authHeader.substring(6);
            String credentials = new String(Base64.getDecoder().decode(base64Credentials));
            String[] parts = credentials.split(":", 2);

            return new ClientCredentials(parts[0],
                parts.length > 1 ? parts[1] : null);
        }

        // Body 파라미터에서 추출
        return new ClientCredentials(clientId, clientSecret);
    }

    private RegisteredClient authenticateClient(
        ClientCredentials credentials) {
        if (credentials.clientId() == null) {
            throw new OAuth2AuthorizationException(
                new OAuth2Error("Client authentication required"));
        }

        RegisteredClient client = registeredClientRepository.findByClientId(credentials.clientId());
        if (client == null) {
            throw new OAuth2AuthorizationException(new OAuth2Error("Unknown client"));
        }

        // client_secret 검증
        if (credentials.clientSecret() != null) {
            String storedSecret = client.getClientSecret();
            // {noop} 제거
            if (storedSecret.startsWith("{noop}")) {
                storedSecret = storedSecret.substring(6);
            }

            if (!storedSecret.equals(credentials.clientSecret())) {
                throw new OAuth2AuthorizationException(
                    new OAuth2Error("Invalid client credentials"));
            }
        }

        return client;
    }

    private record ClientCredentials(String clientId, String clientSecret) {

    }
}
