package com.qlmcp.backend.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorCode {

    // OAuth2 Error
    UNSUPPORTED_RESPONSE_TYPE(
        "OAUTH_001",
        "unsupported response type"
    ),
    PKCE_REQUIRED(
        "OAUTH_002",
        "PKCE parameters are required"
    ),
    UNSUPPORTED_CODE_CHALLENGE_METHOD(
        "OAUTH_003",
        "unsupported code challenge method"
    ),
    INVALID_CODE(
        "OAUTH_004",
        "invalid code"
    ),
    INVALID_TOKEN(
        "OAUTH_005",
        "invalid token"
    ),
    REFRESH_TOKEN_EXPIRED(
        "OAUTH_006",
        "refresh token expired"
    ),
    INVALID_GRANT_TYPE(
        "OAUTH_007",
        "invalid grant type"
    ),

    // Client Error
    INVALID_CLIENT(
        "CLIENT_001",
        "invalid client"
    ),
    INVALID_REDIRECT_URI(
        "CLIENT_002",
        "invalid redirect uri"
    );

    private final String code;
    private final String message;
}
