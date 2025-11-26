package com.qlmcp.backend.tool;

import java.util.List;

import com.qlmcp.backend.dto.MemoryCategory;
import com.qlmcp.backend.service.MemoryService;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class MemoryTool {

    private final MemoryService memoryService;

    @Tool(name = "read_project_memory", description = "read project memory")
    public List<String> readProjectMemory() {
        return memoryService.readMemory(MemoryCategory.Project);
    }

    @Tool(name = "read_area_memory", description = "read area memory")
    public List<String> readAreaMemory() {
        return memoryService.readMemory(MemoryCategory.Area);
    }

    @Tool(name = "read_resource_memory", description = "read resource memory")
    public List<String> readResourceMemory() {
        return memoryService.readMemory(MemoryCategory.Resource);
    }

    @Tool(name = "read_archive_memory", description = "read archive memory")
    public List<String> readArchiveMemory() {
        return memoryService.readMemory(MemoryCategory.Archive);
    }

    @Tool(name = "create_project_memory", description = "create project memory")
    public void createProjectMemory(@ToolParam String content) {
        memoryService.createMemory(MemoryCategory.Project, content);
    }

    @Tool(name = "create_area_memory", description = "create area memory")
    public void createAreaMemory(@ToolParam String content) {
        memoryService.createMemory(MemoryCategory.Area, content);
    }

    @Tool(name = "create_resource_memory", description = "create resource memory")
    public void createResourceMemory(@ToolParam String content) {
        memoryService.createMemory(MemoryCategory.Resource, content);
    }

    @Tool(name = "create_archive_memory", description = "create archive memory")
    public void createArchiveMemory(@ToolParam String content) {
        memoryService.createMemory(MemoryCategory.Archive, content);
    }
}
