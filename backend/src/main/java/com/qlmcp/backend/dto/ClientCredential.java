package com.qlmcp.backend.dto;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class ClientCredential {

    private String clientId;
    private String clientSecret;

}
