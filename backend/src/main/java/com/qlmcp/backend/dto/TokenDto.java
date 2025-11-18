package com.qlmcp.backend.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TokenDto {

    @Setter
    @NoArgsConstructor
    public static class Request {
        private String grant_type;
        private String code;
        private String code_verifier;
        private String redirect_uri;
        private String refresh_token;
    }

    @Getter
    @Builder
    public static class Command {
        private final GrantType grantType;
        private final String code;
        private final String codeVerifier;
        private final String redirectUri;
        private final String refreshTokenValue;
        private final String authHeader;
    }

    @Getter
    @Builder
    @JsonNaming(SnakeCaseStrategy.class)
    public static class Response {
        private final String accessToken;
        private final String tokenType;
        private final long expiresIn;
        private final String refreshToken;
        private final String scope;
    }

    public static Command toCommand(Request request, String authHeader) {
        return Command.builder()
                .grantType(GrantType.from(request.grant_type))
                .code(request.code)
                .codeVerifier(request.code_verifier)
                .redirectUri(request.redirect_uri)
                .refreshTokenValue(request.refresh_token)
                .authHeader(authHeader)
                .build();
    }

    public enum GrantType {
        AUTHORIZATION_CODE,
        REFRESH_TOKEN;

        public static GrantType from(String raw) {
            return switch (raw) {
                case "authorization_code" -> AUTHORIZATION_CODE;
                case "refresh_token" -> REFRESH_TOKEN;
                default -> null;
            };
        }
    }
}
