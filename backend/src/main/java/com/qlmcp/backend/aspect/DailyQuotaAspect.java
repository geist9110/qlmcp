package com.qlmcp.backend.aspect;

import com.qlmcp.backend.annotation.DailyQuota;
import com.qlmcp.backend.dto.QuotaMethod;
import com.qlmcp.backend.entity.Account;
import com.qlmcp.backend.exception.CustomException;
import com.qlmcp.backend.exception.ErrorCode;
import com.qlmcp.backend.service.AccountService;
import com.qlmcp.backend.service.QuotaService;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Aspect
@Component
@RequiredArgsConstructor
public class DailyQuotaAspect {

    private final AccountService accountService;
    private final QuotaService quotaService;

    @Around("@annotation(dailyQuota)")
    public Object checkDailyQuota(
            ProceedingJoinPoint pjp,
            DailyQuota dailyQuota) throws Throwable {
        Account account = accountService.getAccountFromContext();
        QuotaMethod method = dailyQuota.method();
        int limit = dailyQuota.limit();

        boolean allowed = quotaService.increaseAndCheck(account, method, limit);
        if (!allowed) {
            throw CustomException.badRequest(ErrorCode.QUOTA_EXCEEDED);
        }

        return pjp.proceed();
    }
}
