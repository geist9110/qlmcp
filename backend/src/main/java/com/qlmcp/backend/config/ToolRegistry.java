package com.qlmcp.backend.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.qlmcp.backend.exception.CustomException;
import com.qlmcp.backend.exception.ErrorCode;
import com.qlmcp.backend.tool.ToolInterface;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ToolRegistry {

    private final ApplicationContext applicationContext;
    private final ObjectMapper objectMapper;

    public ToolInterface getToolByName(String name) {
        Map<String, Object> toolMap = applicationContext
            .getBeansWithAnnotation(ToolMeta.class)
            .values()
            .stream()
            .collect(
                Collectors.toMap(
                    bean -> bean.getClass().getAnnotation(ToolMeta.class).name(),
                    bean -> bean
                )
            );

        return (ToolInterface) toolMap.get(name);
    }

    public Map<String, List<Map<String, Object>>> getToolsList() {
        List<Map<String, Object>> toolList = applicationContext
            .getBeansWithAnnotation(ToolMeta.class)
            .values()
            .stream()
            .map(bean -> {
                ToolMeta meta = bean.getClass().getAnnotation(ToolMeta.class);
                Object inputSchema;

                try {
                    inputSchema = objectMapper.readValue(meta.inputSchema(), Map.class);
                } catch (Exception e) {
                    throw new CustomException(ErrorCode.SCHEMA_PARSING_ERROR);
                }

                return Map.of(
                    "name", meta.name(),
                    "description", meta.description(),
                    "inputSchema", inputSchema
                );
            })
            .toList();

        return Map.of("tools", toolList);
    }

}
