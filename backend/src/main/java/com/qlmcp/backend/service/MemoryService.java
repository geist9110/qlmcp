package com.qlmcp.backend.service;

import java.util.List;

import com.qlmcp.backend.dto.MemoryCategory;
import com.qlmcp.backend.entity.Memory;
import com.qlmcp.backend.repository.MemoryRepository;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MemoryService {

    private final MemoryRepository memoryRepository;
    private final AccountService accountService;

    public void createMemory(MemoryCategory category, String content) {
        Memory memory = Memory.builder()
                .account(accountService.getAccountFromContext())
                .memoryCategory(category)
                .content(content)
                .build();

        memoryRepository.save(memory);
    }

    public List<String> readMemory(MemoryCategory category) {
        return memoryRepository
                .findAllByAccountAndMemoryCategory(accountService.getAccountFromContext(), category)
                .stream()
                .map(memory -> memory.getContent())
                .toList();
    }

}
