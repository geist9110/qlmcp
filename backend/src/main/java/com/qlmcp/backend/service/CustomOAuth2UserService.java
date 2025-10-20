package com.qlmcp.backend.service;

import com.qlmcp.backend.entity.Account;
import com.qlmcp.backend.entity.Account.AuthProvider;
import com.qlmcp.backend.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final AccountRepository accountRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        String registrationId = userRequest
            .getClientRegistration()
            .getRegistrationId();

        AuthProvider authProvider = AuthProvider
            .valueOf(registrationId.toUpperCase());

        log.info("=== Loaded user from OAuth2 provider: {} ===", registrationId);
        log.info("Current user: {}", oAuth2User.getAttributes());
        log.info("=========================================");

        Account account = accountRepository
            .findByProviderAndProviderId(authProvider,
                oAuth2User.getAttributes().get("id").toString())
            .orElseGet(() -> {
                Account newAccount = new Account(
                    authProvider,
                    oAuth2User.getAttributes().get("id").toString()
                );
                return accountRepository.save(newAccount);
            });

        return oAuth2User;
    }
}
