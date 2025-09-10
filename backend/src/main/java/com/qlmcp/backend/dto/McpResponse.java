package com.qlmcp.backend.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class McpResponse {

    @Builder.Default
    private String jsonrpc = "2.0";
    private Object id;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Object result;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Object error;

}
