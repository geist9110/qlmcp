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
        private final ResponseType responseType;
        private final String clientId;
        private final AuthProvider authProvider;
        private final String redirectUri;
        private final String codeChallenge;
        private final CodeChallengeMethod codeChallengeMethod;
        private final String state;
        private final String scope;
        private final String userName;

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
                .responseType(ResponseType.from(request.response_type))
                .scope(request.scope)
                .state(request.state)
                .codeChallenge(request.code_challenge)
                .codeChallengeMethod(CodeChallengeMethod.from(request.code_challenge_method))
                .authProvider(AuthProvider.valueOf(principal.getAuthorizedClientRegistrationId().toUpperCase()))
                .userName(principal.getName())
                .build();
    }

    public static enum ResponseType {
        CODE;

        public static ResponseType from(String raw) {
            return switch (raw) {
                case "code" -> CODE;
                default -> null;
            };
        }
    }

    public enum CodeChallengeMethod {
        S256;

        public static CodeChallengeMethod from(String raw) {
            return switch (raw) {
                case "S256" -> S256;
                default -> null;
            };
        }
    }
}
