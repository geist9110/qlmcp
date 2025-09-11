package com.qlmcp.backend.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum Method {
    INITIALIZE("initialize"),
    NOTIFICATIONS_INITIALIZED("notifications/initialized"),
    TOOLS_LIST("tools/list"),
    TOOLS_CALL("tools/call");

    private static final Map<String, Method> VALUE_MAP = Stream
        .of(values())
        .collect(Collectors.toMap(m -> m.method, m -> m));
    @JsonValue
    private final String method;

    @JsonCreator
    public static Method fromValue(String value) {
        Method method = VALUE_MAP.get(value);

        if (method == null) {
            throw new IllegalArgumentException("Unknown method: " + value);
        }

        return method;
    }
}
