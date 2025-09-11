package com.qlmcp.backend.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class CustomException extends RuntimeException {

    private final ErrorCode errorCode;

    private static CustomException methodNotFound() {
        return new CustomException(ErrorCode.METHOD_NOT_FOUND);
    }
}