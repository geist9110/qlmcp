package com.qlmcp.backend.tool;

import com.qlmcp.backend.config.ToolMeta;
import com.qlmcp.backend.exception.CustomException;
import com.qlmcp.backend.exception.ErrorCode;
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

    public String getWeather(String city) {
        if (city == null || city.isEmpty()) {
            throw new CustomException(ErrorCode.INVALID_PARAMS);
        }

        return String.format("Sunnyday in %s", city);
    }
}
