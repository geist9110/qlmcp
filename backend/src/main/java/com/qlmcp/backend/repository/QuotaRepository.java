package com.qlmcp.backend.repository;

import java.time.LocalDate;
import java.util.Optional;

import com.qlmcp.backend.entity.Account;
import com.qlmcp.backend.entity.Quota;

import org.springframework.data.jpa.repository.JpaRepository;

public interface QuotaRepository extends JpaRepository<Quota, Long> {

    Optional<Quota> findByAccountAndDate(Account account, LocalDate date);

}
