package com.qlmcp.backend.dto;

import com.qlmcp.backend.exception.ErrorCode;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Getter;

public class JsonRpcResponse {

    @Getter
    public static class McpResponse {

        private final String jsonrpc = "2.0";
        private final Object id;

        public McpResponse(Object id) {
            this.id = id;
        }
    }

    @Getter
    public static class InitializeResponse extends McpResponse {

        private final Result result;

        public InitializeResponse(
            Object id,
            String protocolVersion,
            String instructions,
            Map<String, Object> capabilities,
            Map<String, Object> serverInfo
        ) {
            super(id);
            this.result = new Result(
                protocolVersion,
                instructions,
                capabilities,
                serverInfo
            );
        }

        @Getter
        @AllArgsConstructor
        public static class Result {

            private String protocolVersion;
            private String instructions;
            private Map<String, Object> capabilities;
            private Map<String, Object> serverInfo;
        }
    }

    @Getter
    public static class ToolsListResponse extends McpResponse {

        private final Result result;

        public ToolsListResponse(Object id, List<ToolInformation> tools) {
            super(id);
            this.result = new Result(tools);

        }

        @Getter
        @AllArgsConstructor
        public static class Result {

            private List<ToolInformation> tools;
        }
    }

    @Getter
    public static class ToolsCallResponse extends McpResponse {

        private final Result result;

        public ToolsCallResponse(Object id, List<Object> content) {
            super(id);
            this.result = new Result(content);
        }

        @Getter
        public static class Result {

            private final List<Object> content;
            private final boolean isError = false;

            public Result(List<Object> content) {
                this.content = content;
            }
        }
    }

    @Getter
    public static class ErrorResponse extends McpResponse {

        private final ErrorCode error;

        public ErrorResponse(Object id, ErrorCode error) {
            super(id);
            this.error = error;
        }
    }
}
