package com.qlmcp.backend.tool;

import java.util.Map;

public interface ToolInterface {

    public Map<String, Object> call(Map<?, ?> arguments);
}
