package com.qlmcp.backend.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class McpException extends RuntimeException {

    private final Object id;
    private final ErrorCode errorCode;
}