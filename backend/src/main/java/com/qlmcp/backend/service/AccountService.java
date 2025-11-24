package com.qlmcp.backend.service;

import com.qlmcp.backend.dto.AuthProvider;
import com.qlmcp.backend.entity.Account;
import com.qlmcp.backend.exception.CustomException;
import com.qlmcp.backend.exception.ErrorCode;
import com.qlmcp.backend.repository.AccountRepository;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;

    public Account getAccountFromContext() {

        Jwt jwt = parseJwtAuthenticationToken();
        AuthProvider provider = AuthProvider.valueOf(jwt.getClaim("provider"));
        String providerId = jwt.getSubject();

        return accountRepository
                .findByProviderAndProviderId(provider, providerId)
                .orElseThrow(() -> CustomException.badRequest(ErrorCode.INVALID_TOKEN));
    }

    private Jwt parseJwtAuthenticationToken() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (!(authentication instanceof JwtAuthenticationToken jwtAuth)) {
            throw CustomException.badRequest(ErrorCode.INVALID_TOKEN);
        }
        return (Jwt) jwtAuth.getPrincipal();
    }
}
