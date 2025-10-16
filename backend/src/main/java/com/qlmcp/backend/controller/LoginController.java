package com.qlmcp.backend.controller;

import com.qlmcp.backend.entity.AuthorizationCode;
import com.qlmcp.backend.repository.AuthorizationCodeRepository;
import java.security.Principal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.util.UriComponentsBuilder;

@Slf4j
@Controller
@RequiredArgsConstructor
public class LoginController {

    private final RegisteredClientRepository registeredClientRepository;
    private final AuthorizationCodeRepository authorizationCodeRepository;

    @GetMapping("/oauth2/authorize")
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
}
