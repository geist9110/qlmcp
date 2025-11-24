package com.qlmcp.backend.dto;

import java.util.List;

import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ClientRegistrationDto {

    @JsonNaming(SnakeCaseStrategy.class)
    public static record Request(
            String clientName,
            List<String> redirectUris) {
    }

    public static record Command(
            String clientName,
            List<String> redirectUris) {

    }

    @JsonNaming(SnakeCaseStrategy.class)
    public static record Response(
            String clientId,
            String clientSecret,
            List<String> redirectUris) {

    }

    public static Command toCommand(Request request) {
        return new Command(
                request.clientName,
                request.redirectUris);
    }
}
