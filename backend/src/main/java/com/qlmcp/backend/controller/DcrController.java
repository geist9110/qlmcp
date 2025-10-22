package com.qlmcp.backend.controller;


import com.qlmcp.backend.dto.ClientRegistrationRequest;
import com.qlmcp.backend.dto.ClientRegistrationResponse;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/dcr")
public class DcrController {

    private final RegisteredClientRepository registeredClientRepository;

    @PostMapping("/register")
    public ResponseEntity<ClientRegistrationResponse> registerClient(
        @RequestBody ClientRegistrationRequest request
    ) {

        log.info("=== Dynamic Client Registration Request START ===");
        log.info("Client Name: {}", request.clientName());
        log.info("Redirect URIs: {}", request.redirectUris());

        String clientId = UUID.randomUUID().toString();
        String clientSecret = UUID.randomUUID().toString();

        RegisteredClient client = RegisteredClient
            .withId(UUID.randomUUID().toString())
            .clientId(clientId)
            .clientSecret(clientSecret)
            .clientName(request.clientName())
            .redirectUris(uris -> uris.addAll(request.redirectUris()))
            .authorizationGrantTypes(types -> {
                types.add(AuthorizationGrantType.AUTHORIZATION_CODE);
                types.add(AuthorizationGrantType.REFRESH_TOKEN);
            })
            .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
            .scope("openid")
            .build();

        registeredClientRepository.save(client);

        log.info("Client Id: {}", clientId);
        log.info("Client Secret: {}", clientSecret);
        log.info("=== Dynamic Client Registration Request END ===");

        return ResponseEntity.ok(
            new ClientRegistrationResponse(
                clientId,
                clientSecret,
                request.redirectUris()
            )
        );
    }
}
