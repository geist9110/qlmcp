package com.qlmcp.backend.dto;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Getter;

public class JsonRpcRequest {

    @JsonTypeInfo(
        use = Id.NAME,
        property = "method",
        visible = true,
        defaultImpl = UnknownRequest.class
    )
    @JsonSubTypes({
        @JsonSubTypes.Type(value = InitializeRequest.class, name = "initialize"),
        @JsonSubTypes.Type(value = NotificationRequest.class, name = "notifications/initialized"),
        @JsonSubTypes.Type(value = ToolsListRequest.class, name = "tools/list"),
        @JsonSubTypes.Type(value = ToolsCallRequest.class, name = "tools/call")
    })
    @Getter
    public static class McpRequest {

        private final String jsonrpc = "2.0";
        private Object id;
        private String method;
    }

    @Getter
    public static class InitializeRequest extends McpRequest {

        private InitializeRequest.Params params;

        @Getter
        public static class Params {

            private String protocolVersion;
            private Map<String, Object> capabilities;
            private Map<String, Object> clientInfo;
        }
    }

    @Getter
    public static class NotificationRequest extends McpRequest {

    }

    @Getter
    public static class ToolsListRequest extends McpRequest {

    }

    @Getter
    public static class ToolsCallRequest extends McpRequest {

        private ToolsCallRequest.Params params;

        @Getter
        @AllArgsConstructor
        public static class Params {

            private String name;
            private Map<String, Object> arguments;
        }
    }

    @Getter
    public static class UnknownRequest extends McpRequest {
        // 명시되지 않은 method 타입이 들어온 경우 에러 핸들링을 위한 클래스
    }
}
