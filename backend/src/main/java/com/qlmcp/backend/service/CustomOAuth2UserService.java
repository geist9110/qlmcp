package com.qlmcp.backend.service;

import com.qlmcp.backend.dto.AuthProvider;
import com.qlmcp.backend.entity.Account;
import com.qlmcp.backend.repository.AccountRepository;
import java.util.Map;
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

        OAuth2UserInfo oAuth2UserInfo = getOAuth2UserInfo(
            authProvider,
            oAuth2User.getAttributes()
        );

        accountRepository
            .findByProviderAndProviderId(
                authProvider,
                oAuth2UserInfo.getProviderId()
            ).orElseGet(() -> {
                Account newAccount = new Account(
                    authProvider,
                    oAuth2UserInfo.getProviderId()
                );
                return accountRepository.save(newAccount);
            });

        return oAuth2User;
    }

    private OAuth2UserInfo getOAuth2UserInfo(
        AuthProvider authProvider,
        Map<String, Object> attributes
    ) {
        if (authProvider == AuthProvider.GITHUB) {
            return new GithubOAuth2UserInfo(attributes);
        }

        if (authProvider == AuthProvider.GOOGLE) {
            return new GoogleOAuth2UserInfo(attributes);
        }

        throw new OAuth2AuthenticationException("Unsupported OAuth2 provider: " + authProvider);
    }

    private interface OAuth2UserInfo {

        String getProviderId();
    }

    private record GithubOAuth2UserInfo(Map<String, Object> attributes) implements OAuth2UserInfo {

        @Override
        public String getProviderId() {
            return String.valueOf(attributes.get("id"));
        }
    }

    private record GoogleOAuth2UserInfo(Map<String, Object> attributes) implements OAuth2UserInfo {

        @Override
        public String getProviderId() {
            return String.valueOf(attributes.get("sub"));
        }
    }
}
