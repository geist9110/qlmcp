package com.qlmcp.backend.repository;

import java.util.List;

import com.qlmcp.backend.dto.MemoryCategory;
import com.qlmcp.backend.entity.Account;
import com.qlmcp.backend.entity.Memory;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MemoryRepository extends JpaRepository<Memory, Long> {

    List<Memory> findAllByAccountAndMemoryCategory(Account account, MemoryCategory memoryCategory);

}
