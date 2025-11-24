package com.qlmcp.backend.service;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import com.qlmcp.backend.dto.ClientRegistrationDto;
import com.qlmcp.backend.entity.Client;
import com.qlmcp.backend.repository.ClientRepository;

import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DcrService {

    private final ClientRepository clientRepository;

    public ClientRegistrationDto.Response registerClient(
            ClientRegistrationDto.Command command) {
        Client client = clientRepository
                .findByClientName(command.clientName())
                .orElseGet(() -> createClient(command));

        return new ClientRegistrationDto.Response(
                client.getClientId(),
                client.getClientSecret(),
                command.redirectUris());
    }

    private Client createClient(ClientRegistrationDto.Command command) {
        Client newClient = Client.builder()
                .id(UUID.randomUUID().toString())
                .clientId(UUID.randomUUID().toString())
                .clientSecret(UUID.randomUUID().toString())
                .clientName(command.clientName())
                .redirectUris(new HashSet<>(command.redirectUris()))
                .authorizationGrantTypes(Set.of(
                        AuthorizationGrantType.AUTHORIZATION_CODE,
                        AuthorizationGrantType.REFRESH_TOKEN))
                .authenticationMethods(
                        Set.of(ClientAuthenticationMethod.CLIENT_SECRET_BASIC))
                .scopes(Set.of("openid"))
                .build();

        return clientRepository.save(newClient);
    }
}
