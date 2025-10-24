package com.qlmcp.backend.dto;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class AuthorizeDto {

    private String responseType;
    private String clientId;
    private String redirectUri;
    private String codeChallenge;
    private String codeChallengeMethod;
    private String state;
    private String scope;
    private String userName;

}
