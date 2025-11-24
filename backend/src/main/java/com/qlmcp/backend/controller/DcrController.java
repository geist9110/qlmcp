package com.qlmcp.backend.controller;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import com.qlmcp.backend.dto.ClientRegistrationRequest;
import com.qlmcp.backend.dto.ClientRegistrationResponse;
import com.qlmcp.backend.entity.Client;
import com.qlmcp.backend.repository.ClientRepository;

import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/dcr")
public class DcrController {

    private final ClientRepository clientRepository;

    @PostMapping("/register")
    public ResponseEntity<ClientRegistrationResponse> registerClient(
            @RequestBody ClientRegistrationRequest request) {
        Client client = clientRepository
                .findByClientName(request.clientName())
                .orElseGet(
                        () -> {
                            Client newClient = Client.builder()
                                    .id(UUID.randomUUID().toString())
                                    .clientId(UUID.randomUUID().toString())
                                    .clientSecret(UUID.randomUUID().toString())
                                    .clientName(request.clientName())
                                    .redirectUris(new HashSet<>(request.redirectUris()))
                                    .authorizationGrantTypes(Set.of(
                                            AuthorizationGrantType.AUTHORIZATION_CODE,
                                            AuthorizationGrantType.REFRESH_TOKEN))
                                    .authenticationMethods(
                                            Set.of(ClientAuthenticationMethod.CLIENT_SECRET_BASIC))
                                    .scopes(Set.of("openid"))
                                    .build();

                            return clientRepository.save(newClient);
                        });

        return ResponseEntity.ok(
                new ClientRegistrationResponse(
                        client.getClientId(),
                        client.getClientSecret(),
                        request.redirectUris()));
    }
}
