package com.qlmcp.backend.tool;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import com.qlmcp.backend.dto.MemoryCategory;
import com.qlmcp.backend.service.MemoryService;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class MemoryToolTest {

    @Mock
    MemoryService memoryService;

    @InjectMocks
    MemoryTool memoryTool;

    @Test
    @DisplayName("Read Project Memory Test Success")
    void readProjectMemoryTestSuccess() {
        // given
        List<String> expected = List.of("This is Project Memory", "memory", "third memory");

        // when
        when(memoryService.readMemory(MemoryCategory.Project)).thenReturn(expected);

        // then
        assertEquals(expected, memoryTool.readProjectMemory());
        verify(memoryService).readMemory(MemoryCategory.Project);
    }

    @Test
    @DisplayName("Read Area Memory Test Success")
    void readAreaMemoryTestSuccess() {
        // given
        List<String> expected = List.of("This is Area Memory", "memory");

        // when
        when(memoryService.readMemory(MemoryCategory.Area)).thenReturn(expected);

        // then
        assertEquals(expected, memoryTool.readAreaMemory());
        verify(memoryService).readMemory(MemoryCategory.Area);
    }

    @Test
    @DisplayName("Read Resource Memory Test Success")
    void readResourceMemoryTestSuccess() {
        // given
        List<String> expected = List.of("This is Resource memory");

        // when
        when(memoryService.readMemory(MemoryCategory.Resource))
                .thenReturn(expected);

        // then
        assertEquals(expected, memoryTool.readResourceMemory());
        verify(memoryService).readMemory(MemoryCategory.Resource);
    }

    @Test
    @DisplayName("Read Archive Memory Test Success")
    void readArchiveMemoryTestSuccess() {
        // given
        List<String> expected = List.of("This is Archive Memory", "archive", "hello world!", "");

        // when
        when(memoryService.readMemory(MemoryCategory.Archive))
                .thenReturn(expected);

        // then
        assertEquals(expected, memoryTool.readArchiveMemory());
        verify(memoryService).readMemory(MemoryCategory.Archive);
    }

    @Test
    @DisplayName("Create Project Memory Success")
    void createProjectMemorySuccess() {
        // given
        String content = "Project Memory Content";

        // when
        memoryTool.createProjectMemory(content);

        // then
        verify(memoryService).createMemory(MemoryCategory.Project, content);
    }

    @Test
    @DisplayName("Create Area Memory Success")
    void createAreaMemorySuccess() {
        // given
        String content = "Area Memory Content";

        // when
        memoryTool.createAreaMemory(content);

        // then
        verify(memoryService).createMemory(MemoryCategory.Area, content);
    }

    @Test
    @DisplayName("Create Resource Memory Success")
    void createResourceMemorySuccess() {
        // given
        String content = "Resource Memory Content";

        // when
        memoryTool.createResourceMemory(content);

        // then
        verify(memoryService).createMemory(MemoryCategory.Resource, content);
    }

    @Test
    @DisplayName("Create Archive Memory Success")
    void createArchiveMemorySuccess() {
        // given
        String content = "Archive Memory Content";

        // when
        memoryTool.createArchiveMemory(content);

        // then
        verify(memoryService).createMemory(MemoryCategory.Archive, content);
    }
}
