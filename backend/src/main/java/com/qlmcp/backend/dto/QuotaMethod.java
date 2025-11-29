package com.qlmcp.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum QuotaMethod {
    QUERY("QUERY");

    private final String value;
}
