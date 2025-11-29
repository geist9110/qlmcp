package com.qlmcp.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import com.qlmcp.backend.dto.AuthProvider;
import com.qlmcp.backend.dto.MemoryCategory;
import com.qlmcp.backend.entity.Account;
import com.qlmcp.backend.entity.Memory;
import com.qlmcp.backend.repository.MemoryRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
public class MemoryServiceTest {

    @Mock
    MemoryRepository memoryRepository;

    @Mock
    AccountService accountService;

    @InjectMocks
    MemoryService memoryService;

    Account account;

    @BeforeEach
    void setup() {
        account = new Account(AuthProvider.GOOGLE, "123456789");
        when(accountService.getAccountFromContext()).thenReturn(account);
    }

    @Test
    @DisplayName("Create Memory Success")
    void createMemorySuccess() {
        // given
        MemoryCategory expectMemoryCategory = MemoryCategory.Project;
        String expectContent = "This is expect memory";

        // when
        memoryService.createMemory(expectMemoryCategory, expectContent);

        // then
        verify(memoryRepository).save(
                argThat(m -> m.getAccount().equals(account)
                        && m.getContent().equals(expectContent)
                        && m.getMemoryCategory() == expectMemoryCategory));
        verify(accountService).getAccountFromContext();
    }

    @Test
    @DisplayName("Read Memory Success")
    void readMemorySuccess() {
        // given
        List<String> expectContents = List.of("This is Project Memory", "This is Second Project Memory");
        MemoryCategory expectMemoryCategory = MemoryCategory.Project;
        List<Memory> expectMemory = List.of(
                Memory.builder()
                        .account(account)
                        .memoryCategory(MemoryCategory.Project)
                        .content("This is Project Memory")
                        .build(),
                Memory.builder()
                        .account(account)
                        .memoryCategory(MemoryCategory.Project)
                        .content("This is Second Project Memory")
                        .build());

        // when
        when(memoryRepository.findAllByAccountAndMemoryCategory(account, MemoryCategory.Project))
                .thenReturn(expectMemory);

        // then
        assertEquals(expectContents, memoryService.readMemory(expectMemoryCategory));
        verify(memoryRepository).findAllByAccountAndMemoryCategory(account, MemoryCategory.Project);
    }
}
