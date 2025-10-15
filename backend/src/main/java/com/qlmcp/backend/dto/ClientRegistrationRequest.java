package com.qlmcp.backend.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.util.List;

@JsonNaming(SnakeCaseStrategy.class)
public record ClientRegistrationRequest(
    String clientName,
    List<String> redirectUris
) {

}
