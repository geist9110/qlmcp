package com.qlmcp.backend.dto;

import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class AuthorizeDto {

    @Setter
    @NoArgsConstructor
    public static class Request {
        private String client_id;
        private String redirect_uri;
        private String code_challenge;
        private String code_challenge_method;
        private String response_type;
        private String scope;
        private String state;
    }

    @Getter
    @Builder
    public static class Command {
        private String responseType;
        private String clientId;
        private AuthProvider authProvider;
        private String redirectUri;
        private String codeChallenge;
        private String codeChallengeMethod;
        private String state;
        private String scope;
        private String userName;

    }

    @Getter
    @Builder
    public static class Response {
        private String redirectUri;
        private String authCode;
        private String state;
    }

    public static Command toCommand(Request request, OAuth2AuthenticationToken principal) {
        return Command.builder()
                .clientId(request.client_id)
                .redirectUri(request.redirect_uri)
                .responseType(request.response_type)
                .scope(request.scope)
                .state(request.state)
                .codeChallenge(request.code_challenge)
                .codeChallengeMethod(request.code_challenge_method)
                .authProvider(AuthProvider.valueOf(principal.getAuthorizedClientRegistrationId().toUpperCase()))
                .userName(principal.getName())
                .build();
    }
}
