package com.example.sales.adapter.report;

import com.example.sales.domain.Sale;
import com.example.sales.domain.SalesSummary;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/** Тест підрахунку підсумків (без PDF, без мережі). */
class DefaultSummaryCalculatorTest {

    private final DefaultSummaryCalculator calculator = new DefaultSummaryCalculator();

    @Test
    void sumsTotalAndBreakdowns() {
        List<Sale> sales = List.of(
                new Sale(1L, "Іван", "A", new BigDecimal("100"), "Захід", LocalDate.of(2026, 9, 1)),
                new Sale(2L, "Іван", "B", new BigDecimal("200"), "Схід", LocalDate.of(2026, 9, 2)),
                new Sale(3L, "Олег", "C", new BigDecimal("50"), "Захід", LocalDate.of(2026, 9, 3)));

        SalesSummary s = calculator.calculate(sales);

        assertThat(s.count()).isEqualTo(3);
        assertThat(s.total()).isEqualByComparingTo("350");
        assertThat(s.byRegion().get("Захід")).isEqualByComparingTo("150");
        assertThat(s.byRegion().get("Схід")).isEqualByComparingTo("200");
        assertThat(s.byManager().get("Іван")).isEqualByComparingTo("300");
        assertThat(s.byManager().get("Олег")).isEqualByComparingTo("50");
    }

    @Test
    void emptyListGivesZeroTotal() {
        SalesSummary s = calculator.calculate(List.of());

        assertThat(s.count()).isZero();
        assertThat(s.total()).isEqualByComparingTo("0");
        assertThat(s.byRegion()).isEmpty();
        assertThat(s.byManager()).isEmpty();
    }
}
