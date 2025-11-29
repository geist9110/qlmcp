package com.qlmcp.backend.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.qlmcp.backend.config.CustomConfig;
import com.qlmcp.backend.config.McpConfig.MCPAuthenticationEntryPoint;
import com.qlmcp.backend.config.SecurityConfig;
import com.qlmcp.backend.service.CustomOAuth2UserService;
import com.qlmcp.backend.util.JwtTokenProvider;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@Tag("slice")
@Import(SecurityConfig.class)
@WebMvcTest(WellknownController.class)
public class WellknownControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    CustomConfig customConfig;

    @MockitoBean
    JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    CustomOAuth2UserService customOAuth2UserService;

    @MockitoBean
    MCPAuthenticationEntryPoint mcpAuthenticationEntryPoint;

    @MockitoBean
    JwtDecoder jwtDecoder;

    @Autowired
    ObjectMapper objectMapper;

    @BeforeEach
    public void setUp() {

        when(customConfig.getAuthServerUrl())
                .thenReturn("http://localhost:8080");

        when(customConfig.getResourceServerUrl())
                .thenReturn("http://localhost:8080");
    }

    @Test
    @DisplayName("Get OAuth Protected Resource Metadata")
    void getOAuthProtectedResourceMetadata() throws Exception {
        // then
        mockMvc.perform(get("/.well-known/oauth-protected-resource"))
                .andExpectAll(
                        status().isOk(),
                        content().contentType(MediaType.APPLICATION_JSON),
                        content().json(
                                objectMapper.writeValueAsString(
                                        Map.of("resource", "http://localhost:8080/mcp",
                                                "authorization_servers", List.of("http://localhost:8080")))));
    }

    @Test
    @DisplayName("Get Authorization Server Metadata")
    void getAuthorizationServerMetadata() throws Exception {
        mockMvc.perform(get("/.well-known/oauth-authorization-server"))
                .andExpectAll(
                        status().isOk(),
                        content().contentType(MediaType.APPLICATION_JSON),
                        content().json(
                                objectMapper.writeValueAsString(
                                        Map.of(
                                                "issuer", "http://localhost:8080",
                                                "registration_endpoint", "http://localhost:8080/dcr/register",
                                                "authorization_endpoint", "http://localhost:8080/oauth2/authorize",
                                                "response_types_supported", List.of("code"),
                                                "grant_types_supported", List.of("authorization_code", "refresh_token"),
                                                "token_endpoint_auth_methods_supported",
                                                List.of("client_secret_basic", "client_secret_post"),
                                                "code_challenge_methods_supported", List.of("S256"),
                                                "scopes_supported", List.of("openid")))));
    }
}
