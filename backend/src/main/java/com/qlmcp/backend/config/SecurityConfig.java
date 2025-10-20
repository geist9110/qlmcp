package com.qlmcp.backend.config;

import com.qlmcp.backend.util.JwtTokenProvider;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.filter.OncePerRequestFilter;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtTokenProvider jwtTokenProvider;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(
                authorize -> authorize
                    .requestMatchers("/.well-known/**").permitAll()
                    .requestMatchers("/login/**").permitAll()
                    .requestMatchers("/mcp/**").authenticated()
                    .requestMatchers("/oauth2/register").permitAll()
                    .requestMatchers("/oauth2/token").permitAll()
                    .requestMatchers("/oauth2/authorize").authenticated()
                    .anyRequest().denyAll()
            )
            .oauth2ResourceServer(
                oauth2 -> oauth2.jwt(
                        jwt -> jwt.decoder(jwtTokenProvider.getJwtDecoder())
                    )
                    .authenticationEntryPoint(new MCPAUthenticationEntryPoint())
            )
            .oauth2Login(Customizer.withDefaults())
            .addFilterBefore(
                new LoggingFilter("HTTP Request"),
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
}
