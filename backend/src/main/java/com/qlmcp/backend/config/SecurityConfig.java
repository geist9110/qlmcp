package com.qlmcp.backend.config;

import com.qlmcp.backend.config.McpConfig.MCPAuthenticationEntryPoint;
import com.qlmcp.backend.service.CustomOAuth2UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.web.SecurityFilterChain;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtDecoder jwtDecoder;
    private final CustomOAuth2UserService customOAuth2UserService;
    private final MCPAuthenticationEntryPoint mcpAuthenticationEntryPoint;

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(
                        authorize -> authorize
                                // Well known
                                .requestMatchers("/.well-known/**").permitAll()

                                // Resources
                                .requestMatchers("/login/**", "/image/**", "/css/**", "/script/**").permitAll()

                                // MCP
                                .requestMatchers("/mcp").authenticated()

                                // OAuth
                                .requestMatchers("/oauth2/authorize").authenticated()
                                .requestMatchers("/oauth2/providers", "/oauth2/token").permitAll()

                                // DCR
                                .requestMatchers("/dcr/register").permitAll()

                                // Others
                                .anyRequest().denyAll())
                .oauth2ResourceServer(
                        oauth2 -> oauth2.jwt(
                                jwt -> jwt.decoder(jwtDecoder))
                                .authenticationEntryPoint(mcpAuthenticationEntryPoint))
                .oauth2Login(
                        oauth -> oauth
                                .loginPage("/login")
                                .authorizationEndpoint(
                                        endpoint -> endpoint
                                                .baseUri("/oauth2/login"))
                                .redirectionEndpoint(endpoint -> endpoint
                                        .baseUri("/oauth2/callback/*"))
                                .userInfoEndpoint(userInfo -> userInfo
                                        .userService(customOAuth2UserService)));

        return http.build();
    }
}
