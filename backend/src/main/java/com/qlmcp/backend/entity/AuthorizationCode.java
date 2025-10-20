package com.qlmcp.backend.entity;

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
public class AuthorizationCode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String code;

    @Column(nullable = false)
    private String username;

    @Column(nullable = false)
    private String clientId;

    @Column(nullable = false)
    private String redirectUri;

    @Column(nullable = false)
    private String codeChallenge;

    @Column(nullable = false)
    private String codeChallengeMethod;

    private String scope;
    private String state;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    @Column(nullable = false)
    private boolean used = false;

    public AuthorizationCode(
        String username,
        String clientId,
        String redirectUri,
        String codeChallenge,
        String codeChallengeMethod,
        String scope,
        String state
    ) {
        this.code = UUID.randomUUID().toString();
        this.username = username;
        this.clientId = clientId;
        this.redirectUri = redirectUri;
        this.codeChallenge = codeChallenge;
        this.codeChallengeMethod = codeChallengeMethod;
        this.scope = scope;
        this.state = state;
        this.expiresAt = LocalDateTime.now().plusMinutes(5);
        this.used = false;
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }

    public boolean isValid() {
        return !used && !isExpired();
    }

    public void markUsed() {
        this.used = true;
    }
}
