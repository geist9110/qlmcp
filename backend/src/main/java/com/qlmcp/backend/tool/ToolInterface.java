package com.qlmcp.backend.tool;

import java.util.List;
import java.util.Map;

public interface ToolInterface {

    public List<Object> call(Object id, Map<?, ?> arguments);
}
