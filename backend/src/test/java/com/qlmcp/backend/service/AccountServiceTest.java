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
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;

@ExtendWith(MockitoExtension.class)
public class AccountServiceTest {

    @Mock
    AccountRepository accountRepository;

    @Mock
    SecurityContext securityContext;

    @Mock
    Authentication authentication;

    @Mock
    OAuth2AuthenticationToken authenticationToken;

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
        String expectAuthorizedClientRegistrationId = "GITHUB";
        String invalidUserName = "invalid-user";

        // when
        when(securityContext.getAuthentication()).thenReturn(authenticationToken);
        when(authenticationToken.getAuthorizedClientRegistrationId()).thenReturn(expectAuthorizedClientRegistrationId);
        when(authenticationToken.getName()).thenReturn(invalidUserName);
        when(accountRepository.findByProviderAndProviderId(AuthProvider.GITHUB, invalidUserName))
                .thenReturn(Optional.empty());

        // then
        CustomException exception = assertThrows(CustomException.class, () -> accountService.getAccountFromContext());

        assertEquals(ErrorCode.INVALID_TOKEN, exception.getErrorCode());
        verify(accountRepository).findByProviderAndProviderId(AuthProvider.GITHUB, invalidUserName);
    }

    @Test
    @DisplayName("[SUCCESS] getAccountFromContext - valid input -> return account")
    void getAccountFromContext_AccountExists_returnAccount() {
        // given
        String expectAuthorizedClientRegistrationId = "GOOGLE";
        String expectUserName = "existing-user";
        Account expectAccount = new Account(AuthProvider.GOOGLE, expectUserName);

        // when
        when(securityContext.getAuthentication()).thenReturn(authenticationToken);
        when(authenticationToken.getAuthorizedClientRegistrationId()).thenReturn(expectAuthorizedClientRegistrationId);
        when(authenticationToken.getName()).thenReturn(expectUserName);
        when(accountRepository.findByProviderAndProviderId(AuthProvider.GOOGLE, expectUserName))
                .thenReturn(Optional.of(expectAccount));

        // then
        Account actualAccount = accountService.getAccountFromContext();
        assertEquals(expectAccount, actualAccount);
        verify(accountRepository).findByProviderAndProviderId(AuthProvider.GOOGLE, expectUserName);
    }
}
