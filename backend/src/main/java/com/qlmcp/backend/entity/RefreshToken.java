package com.qlmcp.backend.entity;

import com.qlmcp.backend.dto.AuthProvider;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor
@Getter
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String token;

    @Column(nullable = false)
    private String username;

    @Column(nullable = false)
    private String clientId;

    @Column(nullable = false)
    private AuthProvider authProvider;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    @Column(nullable = false)
    private boolean revoked;

    private String scope;

    public RefreshToken(
        String username,
        String clientId,
        String scope,
        long validitySeconds,
        AuthProvider authProvider) {
        this.token = UUID.randomUUID().toString();
        this.username = username;
        this.clientId = clientId;
        this.scope = scope;
        this.expiresAt = LocalDateTime.now().plusSeconds(validitySeconds);
        this.authProvider = authProvider;
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }

    public boolean isValid() {
        return !revoked && !isExpired();
    }

    public void revoke() {
        this.revoked = true;
    }
}
