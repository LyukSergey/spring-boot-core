package com.example.sales.domain;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Порахований звіт: загальна сума + розбивки по регіонах та менеджерах.
 * <p>Чиста доменна модель — жодного PDF/HTTP/пошти, лише цифри. Легко тестується.
 *
 * @param total     загальна сума всіх продажів
 * @param byRegion  сума по кожному регіону
 * @param byManager сума по кожному менеджеру (топ-продавці)
 * @param count     кількість продажів
 */
public record SalesSummary(
        BigDecimal total,
        Map<String, BigDecimal> byRegion,
        Map<String, BigDecimal> byManager,
        int count
) {
}
