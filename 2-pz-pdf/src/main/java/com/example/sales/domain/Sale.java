package com.example.sales.domain;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Один продаж — доменна модель.
 * <p>Гроші — ЗАВЖДИ {@link BigDecimal}, ніколи {@code double} (щоб не втрачати копійки).
 */
public record Sale(
        Long id,
        String manager,
        String product,
        BigDecimal amount,
        String region,
        LocalDate date
) {
}
