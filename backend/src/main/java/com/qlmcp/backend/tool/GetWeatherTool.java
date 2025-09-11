package com.qlmcp.backend.tool;

import com.qlmcp.backend.exception.CustomException;
import com.qlmcp.backend.exception.ErrorCode;
import org.springframework.stereotype.Component;

@Component
public class GetWeatherTool {

    public String getWeather(String city) {
        if (city == null || city.isEmpty()) {
            throw new CustomException(ErrorCode.INVALID_PARAMS);
        }

        return String.format("Sunnyday in %s", city);
    }
}
