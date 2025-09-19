package com.qlmcp.backend.exception;


import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class InitializeException extends RuntimeException {

    private final ErrorCode errorCode;
}
