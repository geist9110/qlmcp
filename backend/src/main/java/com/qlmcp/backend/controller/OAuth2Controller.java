package com.qlmcp.backend.controller;

import java.util.List;

import com.qlmcp.backend.dto.AuthorizeDto;
import com.qlmcp.backend.dto.OAuthProviderResponseDto;
import com.qlmcp.backend.dto.TokenDto;
import com.qlmcp.backend.service.OAuth2Service;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/oauth2")
@RequiredArgsConstructor
public class OAuth2Controller {

    private final OAuth2Service oAuth2Service;

    @GetMapping("/providers")
    public ResponseEntity<List<OAuthProviderResponseDto>> getProviders() {
        return ResponseEntity.ok(oAuth2Service.getProviders());
    }

    @GetMapping("/authorize")
    public ResponseEntity<Void> authorize(
            @ModelAttribute AuthorizeDto.Request request,
            OAuth2AuthenticationToken principal) {
        AuthorizeDto.Response response = oAuth2Service.getAuthorizeCode(
                AuthorizeDto.toCommand(request, principal));

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.LOCATION, buildRedirectUri(
                response.getRedirectUri(),
                response.getAuthCode(),
                response.getState()));

        return ResponseEntity
                .status(HttpStatus.FOUND)
                .headers(headers)
                .build();
    }

    private String buildRedirectUri(String redirectUri, String code, String state) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(redirectUri)
                .queryParam("code", code);

        if (state != null) {
            builder.queryParam("state", state);
        }

        return builder.toUriString();
    }

    @PostMapping("/token")
    public ResponseEntity<TokenDto.Response> getToken(
            @ModelAttribute TokenDto.Request request,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        return ResponseEntity.ok(
                oAuth2Service.getToken(TokenDto.toCommand(request, authHeader)));
    }
}
