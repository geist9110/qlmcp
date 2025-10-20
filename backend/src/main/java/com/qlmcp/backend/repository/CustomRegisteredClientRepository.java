package com.qlmcp.backend.repository;


import com.qlmcp.backend.entity.Client;
import lombok.AllArgsConstructor;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class CustomRegisteredClientRepository implements RegisteredClientRepository {

    private ClientRepository clientRepository;

    @Override
    public void save(RegisteredClient registeredClient) {
        Client client = Client.builder()
            .id(registeredClient.getId())
            .clientId(registeredClient.getClientId())
            .clientSecret(registeredClient.getClientSecret())
            .clientName(registeredClient.getClientName())
            .authenticationMethods(registeredClient.getClientAuthenticationMethods())
            .authorizationGrantTypes(registeredClient.getAuthorizationGrantTypes())
            .redirectUris(registeredClient.getRedirectUris())
            .scopes(registeredClient.getScopes())
            .build();

        clientRepository.save(client);
    }

    @Override
    public RegisteredClient findById(String id) {
        return clientRepository
            .findById(id)
            .map(this::toRegisteredClient)
            .orElse(null);
    }

    @Override
    public RegisteredClient findByClientId(String clientId) {
        return clientRepository
            .findByClientId(clientId)
            .map(this::toRegisteredClient)
            .orElse(null);
    }

    private RegisteredClient toRegisteredClient(Client client) {
        return RegisteredClient
            .withId(client.getId())
            .clientId(client.getClientId())
            .clientSecret(client.getClientSecret())
            .clientAuthenticationMethods(
                (methods) -> methods.addAll(client.getAuthenticationMethods()))
            .authorizationGrantTypes(
                (types) -> types.addAll(client.getAuthorizationGrantTypes())
            )
            .redirectUris((uris) -> uris.addAll(client.getRedirectUris()))
            .scopes((scopes) -> scopes.addAll(client.getScopes()))
            .build()
            ;
    }
}
