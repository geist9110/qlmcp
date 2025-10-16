package com.qlmcp.backend.util;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;

public class EntityConverters {

    @AllArgsConstructor(access = AccessLevel.PROTECTED)
    private static abstract class SetConverter<T> implements AttributeConverter<Set<T>, String> {

        private final Function<String, T> constructor;
        private final Function<T, String> valueExtractor;

        @Override
        public String convertToDatabaseColumn(Set<T> attribute) {
            if (attribute == null || attribute.isEmpty()) {
                return "";
            }
            return attribute.stream()
                .map(valueExtractor)
                .collect(Collectors.joining(","));
        }

        @Override
        public Set<T> convertToEntityAttribute(String dbData) {
            if (dbData == null || dbData.trim().isEmpty()) {
                return new HashSet<>();
            }
            return Arrays.stream(dbData.split(","))
                .map(String::trim)
                .map(constructor)
                .collect(Collectors.toSet());
        }
    }

    @Converter
    public static class ClientAuthenticationMethodSetConverter extends
        SetConverter<ClientAuthenticationMethod> {

        public ClientAuthenticationMethodSetConverter() {
            super(
                ClientAuthenticationMethod::new,
                ClientAuthenticationMethod::getValue
            );
        }
    }

    @Converter
    public static class AuthorizationGrantTypeSetConverter extends
        SetConverter<AuthorizationGrantType> {

        public AuthorizationGrantTypeSetConverter() {
            super(
                AuthorizationGrantType::new,
                AuthorizationGrantType::getValue
            );
        }
    }

    @Converter
    public static class StringSetConverter extends SetConverter<String> {

        public StringSetConverter() {
            super(s -> s, s -> s);
        }
    }
}
