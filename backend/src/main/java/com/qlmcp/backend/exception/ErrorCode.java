package com.qlmcp.backend.exception;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum ErrorCode {
    METHOD_NOT_FOUND(-32601, "Method not found"),
    INVALID_PARAMS(-32602, "Invalid params"),
    TOOL_NOT_FOUND(-32603, "Tool not found"),
    SCHEMA_PARSING_ERROR(-32604, "Schema parsing error"),
    TOOL_EXECUTION_ERROR(-32605, "Tool execution error"),
    ;

    private final int code;
    private final String message;
}
