package com.qlmcp.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import com.qlmcp.backend.dto.AuthProvider;
import com.qlmcp.backend.entity.Account;
import com.qlmcp.backend.exception.CustomException;
import com.qlmcp.backend.exception.ErrorCode;
import com.qlmcp.backend.repository.AccountRepository;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

@ExtendWith(MockitoExtension.class)
public class AccountServiceTest {

    @Mock
    AccountRepository accountRepository;

    @Mock
    SecurityContext securityContext;

    @Mock
    Authentication authentication;

    @Mock
    JwtAuthenticationToken authenticationToken;

    @Mock
    Jwt jwt;

    @InjectMocks
    AccountService accountService;

    private MockedStatic<SecurityContextHolder> securityContextHolder;

    @BeforeEach
    void setupSecurityContextHolder() {
        securityContextHolder = Mockito.mockStatic(SecurityContextHolder.class);
        securityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);
    }

    @AfterEach
    void cleanupSecurityContextHolder() {
        securityContextHolder.close();
    }

    @Test
    @DisplayName("[EXCEPTION] getAccountFromContext - authentication is not oauth2 token input -> badRequest is thrown")
    void getAccountFromContext_AuthenticationIsNotOAuth2_throwInvalidToken() {
        // when
        when(securityContext.getAuthentication()).thenReturn(authentication);

        // then
        CustomException exception = assertThrows(CustomException.class, () -> accountService.getAccountFromContext());
        assertEquals(ErrorCode.INVALID_TOKEN, exception.getErrorCode());
    }

    @Test
    @DisplayName("[EXCEPTION] getAccountFromContext - not exist account token input -> badRequest is thrown")
    void getAccountFromContext_AccountIsMissing_throwInvalidToken() {
        // given
        AuthProvider expectProvider = AuthProvider.GITHUB;
        String invalidProviderId = "invalid-user";

        // when
        when(securityContext.getAuthentication()).thenReturn(authenticationToken);
        when(authenticationToken.getPrincipal()).thenReturn(jwt);
        when(jwt.getSubject()).thenReturn(invalidProviderId);
        when(jwt.getClaim("provider")).thenReturn("GITHUB");
        when(accountRepository.findByProviderAndProviderId(expectProvider, invalidProviderId))
                .thenReturn(Optional.empty());

        // then
        CustomException exception = assertThrows(CustomException.class, () -> accountService.getAccountFromContext());

        assertEquals(ErrorCode.INVALID_TOKEN, exception.getErrorCode());
        verify(accountRepository).findByProviderAndProviderId(expectProvider, invalidProviderId);
    }

    @Test
    @DisplayName("[SUCCESS] getAccountFromContext - valid input -> return account")
    void getAccountFromContext_AccountExists_returnAccount() {
        // given
        AuthProvider expectProvider = AuthProvider.GOOGLE;
        String expectProviderId = "existing-user";
        Account expectAccount = new Account(expectProvider, expectProviderId);

        // when
        when(securityContext.getAuthentication()).thenReturn(authenticationToken);
        when(authenticationToken.getPrincipal()).thenReturn(jwt);
        when(jwt.getSubject()).thenReturn(expectProviderId);
        when(jwt.getClaim("provider")).thenReturn("GOOGLE");
        when(accountRepository.findByProviderAndProviderId(expectProvider, expectProviderId))
                .thenReturn(Optional.of(expectAccount));

        // then
        Account actualAccount = accountService.getAccountFromContext();
        assertEquals(expectAccount, actualAccount);
        verify(accountRepository).findByProviderAndProviderId(expectProvider, expectProviderId);
    }
}
