package com.qlmcp.backend.config;

import java.io.IOException;

import com.qlmcp.backend.service.CustomOAuth2UserService;
import com.qlmcp.backend.util.JwtTokenProvider;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtTokenProvider jwtTokenProvider;
    private final CustomOAuth2UserService customOAuth2UserService;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

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
                                jwt -> jwt.decoder(jwtTokenProvider.getJwtDecoder()))
                                .authenticationEntryPoint(new MCPAuthenticationEntryPoint()))
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

    private static class MCPAuthenticationEntryPoint implements AuthenticationEntryPoint {

        @Override
        public void commence(
                HttpServletRequest request,
                HttpServletResponse response,
                AuthenticationException authException) throws IOException, ServletException {
            String baseUrl = request.getScheme() + "://"
                    + request.getServerName() + ":"
                    + request.getServerPort();

            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setHeader("WWW-Authenticate",
                    String.format(
                            "Bearer authorization_server=\"%s\", ",
                            baseUrl));
        }
    }
}
