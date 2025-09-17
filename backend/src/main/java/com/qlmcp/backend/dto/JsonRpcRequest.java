package com.qlmcp.backend.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;

@Getter
public class JsonRpcRequest {

    private String jsonrpc = "2.0";
    private Object id;
    private Method method;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Object params;
}
