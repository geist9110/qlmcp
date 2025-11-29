package com.qlmcp.backend.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Optional;

import com.qlmcp.backend.dto.AuthProvider;
import com.qlmcp.backend.dto.QuotaMethod;
import com.qlmcp.backend.entity.Account;
import com.qlmcp.backend.entity.Quota;
import com.qlmcp.backend.repository.QuotaRepository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
public class QuotaServiceTest {

    @Mock
    QuotaRepository quotaRepository;

    @InjectMocks
    QuotaService quotaService;

    @Test
    @DisplayName("[SUCCESS] increaseAndCheck - exist quota input -> return true")
    void increaseAndCheck_ExistQuotaInput_ReturnTrue() {
        // given
        Account expectAccount = new Account(AuthProvider.GOOGLE, "test-provider-id");
        LocalDate date = LocalDate.now();
        Quota expectQuota = new Quota(expectAccount, date, QuotaMethod.QUERY);

        ReflectionTestUtils.setField(expectQuota, "count", 99);

        // when
        when(quotaRepository.findByAccountAndDate(expectAccount, date))
                .thenReturn(Optional.of(expectQuota));

        // then
        assertTrue(quotaService.increaseAndCheck(expectAccount, QuotaMethod.QUERY, 100));
    }

    @Test
    @DisplayName("[SUCCESS] increaseAndCheck - not exist quota input -> return true")
    void increaseAndCheck_NotExistQuotaInput_ReturnTrue() {
        // given
        Account expectAccount = new Account(AuthProvider.GOOGLE, "test-provider-id");
        LocalDate date = LocalDate.now();

        // when
        when(quotaRepository.findByAccountAndDate(expectAccount, date))
                .thenReturn(Optional.empty());
        when(quotaRepository.save(any(Quota.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Then
        assertTrue(quotaService.increaseAndCheck(expectAccount, QuotaMethod.QUERY, 100));
        verify(quotaRepository).save(
                argThat(quota -> quota.getAccount().equals(expectAccount)
                        && quota.getCount() == 1
                        && quota.getDate().equals(date)
                        && quota.getMethod().equals(QuotaMethod.QUERY)));

    }

    @Test
    @DisplayName("[SUCCESS] increaseAndCheck - over limit input -> return false")
    void increaseAndCheck_OverLimitInput_ReturnFalse() {
        // given
        Account expectAccount = new Account(AuthProvider.GOOGLE, "test-provider-id");
        LocalDate date = LocalDate.now();
        Quota expectQuota = new Quota(expectAccount, date, QuotaMethod.QUERY);
        ReflectionTestUtils.setField(expectQuota, "count", 100);

        // when
        when(quotaRepository.findByAccountAndDate(expectAccount, date))
                .thenReturn(Optional.of(expectQuota));

        // then
        assertFalse(quotaService.increaseAndCheck(expectAccount, QuotaMethod.QUERY, 100));
    }

}
