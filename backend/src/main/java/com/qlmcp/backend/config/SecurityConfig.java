package com.qlmcp.backend.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.server.authorization.client.InMemoryRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.filter.OncePerRequestFilter;

@Slf4j
@Configuration
public class SecurityConfig {

    @Bean
    @Order(1)
    public SecurityFilterChain wellknwonFilterChain(HttpSecurity http) throws Exception {
        http
            .securityMatcher("/.well-known/**")
            .authorizeHttpRequests(
                authorize -> authorize.anyRequest().permitAll()
            )
            .csrf(AbstractHttpConfigurer::disable)
            .addFilterBefore(
                new LoggingFilter("Well-known Filter"),
                UsernamePasswordAuthenticationFilter.class
            )
        ;
        return http.build();
    }

    @Bean
    @Order(2)
    public SecurityFilterChain loginFilterChain(HttpSecurity http) throws Exception {
        http
            .securityMatcher("/login", "/oauth2/**")
            .authorizeHttpRequests(
                authorize -> authorize.anyRequest().permitAll()
            )
            .csrf(AbstractHttpConfigurer::disable)
            .addFilterBefore(
                new LoggingFilter("Login Filter"),
                UsernamePasswordAuthenticationFilter.class
            )
        ;
        return http.build();
    }

    @Bean
    @Order(3)
    public SecurityFilterChain mcpFilterChain(HttpSecurity http) throws Exception {
        http
            .securityMatcher("/mcp/**")
            .authorizeHttpRequests(authorize -> authorize
                .anyRequest().authenticated()
            )
            .oauth2ResourceServer(
                oauth2 -> oauth2.jwt(Customizer.withDefaults())
                    .authenticationEntryPoint(new MCPAUthenticationEntryPoint())
            )
            .csrf(AbstractHttpConfigurer::disable)
            .addFilterBefore(
                new LoggingFilter("MCP Filter"),
                UsernamePasswordAuthenticationFilter.class
            )
        ;
        return http.build();
    }

    private static class MCPAUthenticationEntryPoint implements AuthenticationEntryPoint {

        @Override
        public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException
        ) throws IOException, ServletException {
            log.info("=== MCP Authentication Failed ===");
            log.info("Request URI: {}", request.getRequestURI());
            log.info("Auth Exception: {}", authException.getMessage());

            String baseUrl = request.getScheme() + "://"
                + request.getServerName() + ":"
                + request.getServerPort();

            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setHeader("WWW-Authenticate",
                String.format(
                    "Bearer authorization_server=\"%s\", ",
                    baseUrl
                ));

            log.info("WWW-Authenticate Header: {}", response.getHeader("WWW-Authenticate"));
            log.info("=== End of MCP Authentication Failed ===");
        }
    }

    @RequiredArgsConstructor
    private static class LoggingFilter extends OncePerRequestFilter {

        private final String filterName;

        @Override
        protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
        ) throws ServletException, IOException {
            log.info("=== {} START ===", filterName);
            log.info("Method: {} {}", request.getMethod(), request.getRequestURI());
            log.info("Query String: {}", request.getQueryString());

            String authHeader = request.getHeader("Authorization");
            if (authHeader != null) {
                log.info("Authorization: {}", authHeader
                );
            } else {
                log.info("Authorization: (none)");
            }

            try {
                filterChain.doFilter(request, response);
            } finally {
                log.info("Response Status: {}", response.getStatus());
                log.info("=== {} END ===\n", filterName);
            }
        }
    }

    @Bean
    public RegisteredClientRepository registeredClientRepository() {

        RegisteredClient dummyClient = RegisteredClient.withId(UUID.randomUUID().toString())
            .clientId("dummy-client")
            .clientSecret("{noop}dummy-secret")
            .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
            .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
            .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
            .redirectUri("http://localhost:8080/dummy")
            .scope("mcp.read")
            .scope("mcp.write")
            .build();

        return new InMemoryRegisteredClientRepository(dummyClient);
    }
}
