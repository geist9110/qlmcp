package com.qlmcp.backend.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import javax.crypto.SecretKey;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Getter
public class JwtTokenProvider {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.access-token-validity}")
    private long accessTokenValidity;

    public String generateAccessToken(
        String username,
        String clientId,
        String scope
    ) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + accessTokenValidity * 1000);

        return Jwts.builder()
            .setSubject(username)
            .claim("client_id", clientId)
            .claim("scope", scope)
            .setIssuedAt(now)
            .setExpiration(expiryDate)
            .signWith(SignatureAlgorithm.HS256, getSecretKey())
            .compact();
    }

    public JwtDecoder getJwtDecoder() {
        return NimbusJwtDecoder
            .withSecretKey(getSecretKey())
            .macAlgorithm(MacAlgorithm.HS256)
            .build();
    }

    private SecretKey getSecretKey() {
        return Keys.hmacShaKeyFor(
            secret.getBytes(StandardCharsets.UTF_8)
        );
    }

    public String getUsernameFromToken(String token) {
        Claims claims = Jwts.parser()
            .setSigningKey(secret)
            .parseClaimsJws(token)
            .getBody();

        return claims.getSubject();
    }

    public boolean validateToken(String token) {
        try {
            Jwts
                .parser()
                .setSigningKey(secret)
                .parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
}
