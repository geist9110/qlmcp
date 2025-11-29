package com.qlmcp.backend.entity;

import java.time.LocalDate;

import com.qlmcp.backend.dto.QuotaMethod;

import org.hibernate.annotations.ColumnDefault;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Quota {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    long id;

    @ManyToOne
    private Account account;
    private LocalDate date;

    @ColumnDefault("0")
    private int count;

    @Enumerated(EnumType.STRING)
    private QuotaMethod method;

    public Quota(Account account, LocalDate date, QuotaMethod method) {
        this.account = account;
        this.date = date;
        this.method = method;
    }

    public boolean isLimitExceeded(int limit) {
        return count >= limit;
    }

    public void increase() {
        this.count++;
    }
}
