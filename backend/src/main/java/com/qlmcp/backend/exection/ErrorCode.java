package com.qlmcp.backend.exection;

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
