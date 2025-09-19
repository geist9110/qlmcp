package com.qlmcp.backend.tool;

import com.qlmcp.backend.config.ToolMeta;
import com.qlmcp.backend.exception.ErrorCode;
import com.qlmcp.backend.exception.McpException;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

@ToolMeta(
    name = "get_weather",
    description = "Get weather information",
    inputSchema = """
            {
                "type": "object",
                "properties": {
                    "city": {
                        "type": "string",
                        "description": "City name"
                    }
                },
                "required": ["city"]
            }
        """
)
@Component
public class GetWeatherTool implements ToolInterface {

    @Override
    public List<Object> call(Object id, Map<?, ?> arguments) {
        return List.of(
            Map.of(
                "type", "text",
                "text", getWeather(id, (String) arguments.get("city"))
            )
        );
    }

    private String getWeather(Object id, String city) {
        if (city == null || city.isEmpty()) {
            throw new McpException(id, ErrorCode.INVALID_PARAMS);
        }

        return String.format("Sunnyday in %s", city);
    }
}
