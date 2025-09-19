package com.qlmcp.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ToolInformation {

    private String name;
    private String description;
    private Object inputSchema;
}
