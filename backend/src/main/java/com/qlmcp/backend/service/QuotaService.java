package com.qlmcp.backend.service;

import java.time.LocalDate;

import com.qlmcp.backend.dto.QuotaMethod;
import com.qlmcp.backend.entity.Account;
import com.qlmcp.backend.entity.Quota;
import com.qlmcp.backend.repository.QuotaRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class QuotaService {

    private final QuotaRepository quotaRepository;

    @Transactional
    public boolean increaseAndCheck(Account account, QuotaMethod method, int limit) {
        LocalDate today = LocalDate.now();

        Quota quota = quotaRepository
                .findByAccountAndDate(account, today)
                .orElseGet(() -> quotaRepository
                        .save(new Quota(account, today, method)));

        if (quota.isLimitExceeded(limit)) {
            return false;
        }

        quota.increase();
        return true;
    }

}
