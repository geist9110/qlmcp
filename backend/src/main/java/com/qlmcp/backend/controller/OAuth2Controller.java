package com.qlmcp.backend.controller;

import com.qlmcp.backend.dto.AuthorizeDto;
import com.qlmcp.backend.dto.ClientCredential;
import com.qlmcp.backend.dto.OAuthProviderResponseDto;
import com.qlmcp.backend.dto.TokenDto;
import com.qlmcp.backend.entity.Client;
import com.qlmcp.backend.exection.CustomException;
import com.qlmcp.backend.exection.ErrorCode;
import com.qlmcp.backend.service.OAuth2Service;
import java.security.Principal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

@Slf4j
@RestController
@RequestMapping("/oauth2")
@RequiredArgsConstructor
public class OAuth2Controller {

    private final OAuth2Service oAuth2Service;

    @GetMapping("/providers")
    public ResponseEntity<List<OAuthProviderResponseDto>> getProviders() {
        return ResponseEntity.ok(oAuth2Service.getProviders());
    }

    @GetMapping("/authorize")
    public ResponseEntity<?> authorize(
        @RequestParam("client_id") String clientId,
        @RequestParam("redirect_uri") String redirectUri,
        @RequestParam("code_challenge") String codeChallenge,
        @RequestParam("code_challenge_method") String codeChallengeMethod,
        @RequestParam("response_type") String responseType,
        @RequestParam(required = false) String scope,
        @RequestParam(required = false) String state,
        Principal principal
    ) {
        log.info("=== Authorization Request START ===");
        log.info("Client ID: {}", clientId);
        log.info("Redirect URI: {}", redirectUri);
        log.info("Authenticated Principal: {}", principal.getName());

        String authCode = oAuth2Service.getAuthorizeCode(
            AuthorizeDto.builder()
                .responseType(responseType)
                .scope(scope)
                .state(state)
                .redirectUri(redirectUri)
                .codeChallenge(codeChallenge)
                .codeChallengeMethod(codeChallengeMethod)
                .clientId(clientId)
                .userName(principal.getName())
                .build()
        );

        log.info(
            "Authorization Code Issued - code: {}, user: {}",
            authCode,
            principal.getName()
        );
        log.info("=== Authorization Request END ===");

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.LOCATION, buildRedirectUri(
            redirectUri,
            authCode,
            state
        ));

        return ResponseEntity
            .status(HttpStatus.FOUND)
            .headers(headers)
            .build();
    }

    private String buildRedirectUri(String redirectUri, String code, String state) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(redirectUri)
            .queryParam("code", code);

        if (state != null) {
            builder.queryParam("state", state);
        }

        return builder.toUriString();
    }

    @PostMapping("/token")
    public ResponseEntity<TokenDto> token(
        @RequestParam("grant_type") String grantType,
        @RequestParam(value = "code", required = false) String code,
        @RequestParam(value = "code_verifier", required = false) String codeVerifier,
        @RequestParam(value = "redirect_uri", required = false) String redirectUri,
        @RequestParam(value = "client_id", required = false) String clientId,
        @RequestParam(value = "client_secret", required = false) String clientSecret,
        @RequestParam(value = "refresh_token", required = false) String refreshTokenValue,
        @RequestHeader(value = "Authorization", required = false) String authHeader
    ) {
        ClientCredential credentials = oAuth2Service.getClientCredential(
            authHeader,
            clientId,
            clientSecret
        );

        Client client = oAuth2Service.authenticateClient(credentials);

        if ("authorization_code".equals(grantType)) {
            return ResponseEntity.ok(
                oAuth2Service.handleAuthorizationCodeGrant(
                    code,
                    codeVerifier,
                    redirectUri,
                    client
                )
            );
        }

        if ("refresh_token".equals(grantType)) {
            return ResponseEntity.ok(
                oAuth2Service.handleRefreshTokenGrant(refreshTokenValue, client)
            );
        }

        throw CustomException.badRequest(ErrorCode.INVALID_GRANT_TYPE);
    }
}
