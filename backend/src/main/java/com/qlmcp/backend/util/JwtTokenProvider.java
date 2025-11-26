package com.qlmcp.backend.util;

import java.util.Date;

import javax.crypto.SecretKey;

import com.qlmcp.backend.dto.AuthProvider;
import com.qlmcp.backend.property.JwtProperties;

import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

    private final JwtProperties jwtProperties;
    private final SecretKey secretKey;

    public String generateAccessToken(String username, String clientId, String scope,
            AuthProvider authProvider) {
        Date now = new Date();
        Date expiryDate = new Date(
                now.getTime() + this.jwtProperties.getAccessTokenValidity() * 1000L);
        return Jwts.builder().setSubject(username).claim("client_id", clientId)
                .claim("scope", scope).claim("provider", authProvider).setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(this.secretKey, SignatureAlgorithm.forName(this.jwtProperties.getAlgorithm()))
                .compact();
    }

    private Jws<Claims> parseClaimesJws(String token) {
        return Jwts.parserBuilder().setSigningKey(this.secretKey).build().parseClaimsJws(token);
    }

    public String getUsernameFromToken(String token) {
        return this.parseClaimesJws(token).getBody().getSubject();
    }

    public boolean validateToken(String token) {
        try {
            this.parseClaimesJws(token);
            return true;
        } catch (IllegalArgumentException | JwtException var3) {
            return false;
        }
    }

    public long getAccessTokenValidity() {
        return this.jwtProperties.getAccessTokenValidity();
    }
}
