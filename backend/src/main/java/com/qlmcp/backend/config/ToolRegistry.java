package com.qlmcp.backend.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.qlmcp.backend.dto.ToolInformation;
import com.qlmcp.backend.exception.CustomException;
import com.qlmcp.backend.exception.ErrorCode;
import com.qlmcp.backend.tool.ToolInterface;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.Getter;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Component
public class ToolRegistry {

    private final ApplicationContext applicationContext;
    private final ObjectMapper objectMapper;
    private final Map<String, ToolInterface> toolMap;

    @Getter
    private final List<ToolInformation> toolInformationList;

    public ToolRegistry(
        ApplicationContext applicationContext,
        ObjectMapper objectMapper
    ) {
        this.applicationContext = applicationContext;
        this.objectMapper = objectMapper;
        this.toolMap = createToolMap();
        this.toolInformationList = createToolInformationList();
    }

    private Map<String, ToolInterface> createToolMap() {
        return applicationContext
            .getBeansWithAnnotation(ToolMeta.class)
            .values()
            .stream()
            .collect(
                Collectors.toMap(
                    bean -> bean.getClass().getAnnotation(ToolMeta.class).name(),
                    bean -> (ToolInterface) bean
                )
            );
    }

    private List<ToolInformation> createToolInformationList() {
        return toolMap.values().stream().map(bean -> {
            ToolMeta meta = bean.getClass().getAnnotation(ToolMeta.class);

            try {
                Object inputSchema = objectMapper.readValue(meta.inputSchema(), Map.class);

                return new ToolInformation(
                    meta.name(),
                    meta.description(),
                    inputSchema
                );
            } catch (Exception e) {
                throw new CustomException(ErrorCode.SCHEMA_PARSING_ERROR);
            }
        }).toList();
    }

    public ToolInterface getToolByName(String name) {
        return toolMap.get(name);
    }

}
