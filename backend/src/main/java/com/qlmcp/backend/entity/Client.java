package com.qlmcp.backend.entity;

import com.qlmcp.backend.util.EntityConverters.AuthorizationGrantTypeSetConverter;
import com.qlmcp.backend.util.EntityConverters.ClientAuthenticationMethodSetConverter;
import com.qlmcp.backend.util.EntityConverters.StringSetConverter;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
public class Client {

    @Id
    private String id;

    private String clientId;

    private String clientName;

    private String clientSecret;

    @Convert(converter = ClientAuthenticationMethodSetConverter.class)
    private Set<ClientAuthenticationMethod> authenticationMethods;

    @Convert(converter = AuthorizationGrantTypeSetConverter.class)
    private Set<AuthorizationGrantType> authorizationGrantTypes;

    @Convert(converter = StringSetConverter.class)
    private Set<String> redirectUris;

    @Convert(converter = StringSetConverter.class)
    private Set<String> scopes;
}