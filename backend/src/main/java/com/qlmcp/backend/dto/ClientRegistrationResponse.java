package com.qlmcp.backend.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.util.List;

@JsonNaming(SnakeCaseStrategy.class)
public record ClientRegistrationResponse(
    String clientId,
    String clientSecret,
    List<String> redirectUris
) {

}
