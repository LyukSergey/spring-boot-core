package com.example.sales.adapter.report;

import com.example.sales.domain.Sale;
import com.example.sales.domain.SalesSummary;
import com.example.sales.port.SummaryCalculator;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Реалізація підрахунку підсумків. Чиста логіка — жодних побічних ефектів.
 */
@Component
public class DefaultSummaryCalculator implements SummaryCalculator {

    @Override
    public SalesSummary calculate(List<Sale> sales) {
        BigDecimal total = sales.stream()
                .map(Sale::amount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<String, BigDecimal> byRegion = sales.stream().collect(Collectors.groupingBy(
                Sale::region,
                Collectors.reducing(BigDecimal.ZERO, Sale::amount, BigDecimal::add)));

        Map<String, BigDecimal> byManager = sales.stream().collect(Collectors.groupingBy(
                Sale::manager,
                Collectors.reducing(BigDecimal.ZERO, Sale::amount, BigDecimal::add)));

        return new SalesSummary(total, byRegion, byManager, sales.size());
    }
}
