package com.qlmcp.backend.service;

import java.util.Optional;

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
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (!(authentication instanceof JwtAuthenticationToken jwtAuth)) {
            throw CustomException.badRequest(ErrorCode.INVALID_TOKEN);
        }
        Jwt jwt = (Jwt) jwtAuth.getPrincipal();
        AuthProvider provider = AuthProvider.valueOf(jwt.getClaim("provider"));
        String providerId = jwt.getSubject();
        Optional<Account> account = accountRepository
                .findByProviderAndProviderId(provider, providerId);

        if (account.isEmpty()) {
            throw CustomException.badRequest(ErrorCode.INVALID_TOKEN);
        }
        return account.get();
    }
}
