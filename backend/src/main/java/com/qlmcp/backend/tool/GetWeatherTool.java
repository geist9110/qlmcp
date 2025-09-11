package com.qlmcp.backend.tool;

import com.qlmcp.backend.config.ToolMeta;
import com.qlmcp.backend.exception.CustomException;
import com.qlmcp.backend.exception.ErrorCode;
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
public class GetWeatherTool {

    public Map<String, Object> call(Map<?, ?> arguments) {
        return Map.of(
            "content", List.of(
                Map.of(
                    "type", "text",
                    "text", getWeather((String) arguments.get("city"))
                )
            ),
            "isError", false
        );
    }

    private String getWeather(String city) {
        if (city == null || city.isEmpty()) {
            throw new CustomException(ErrorCode.INVALID_PARAMS);
        }

        return String.format("Sunnyday in %s", city);
    }
}
