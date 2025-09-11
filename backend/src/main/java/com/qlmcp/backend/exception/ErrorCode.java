package com.qlmcp.backend.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorCode {
    METHOD_NOT_FOUND(-32601, "Method not found");

    private final int code;
    private final String message;
}
