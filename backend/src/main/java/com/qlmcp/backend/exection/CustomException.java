package com.qlmcp.backend.exection;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.util.UriComponentsBuilder;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CustomException extends RuntimeException {

    private ErrorCode errorCode;
    private HttpStatus httpStatus;
    private HttpHeaders headers;

    public static CustomException redirect(ErrorCode errorCode, String redirectUri) {
        HttpHeaders headers = new HttpHeaders();

        UriComponentsBuilder builder = UriComponentsBuilder
            .fromUriString(redirectUri)
            .queryParam("error_code", errorCode.getCode())
            .queryParam("error_message", errorCode.getMessage());

        headers.add(HttpHeaders.LOCATION, builder.toUriString());

        return CustomException
            .builder()
            .httpStatus(HttpStatus.FOUND)
            .headers(headers)
            .build();
    }

    public static CustomException badRequest(ErrorCode errorCode) {
        return CustomException
            .builder()
            .httpStatus(HttpStatus.BAD_REQUEST)
            .errorCode(errorCode)
            .build();
    }
}
