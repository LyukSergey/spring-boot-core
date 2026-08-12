package com.example.sales.port;

import com.example.sales.domain.Sale;
import com.example.sales.domain.SalesSummary;

import java.util.List;

/**
 * Порт підрахунку підсумків звіту (загальна сума, розбивки по регіонах/менеджерах).
 * <p>Чиста логіка без побічних ефектів — реалізацію легко покрити unit-тестами.
 */
public interface SummaryCalculator {

    /** Порахувати підсумок за списком продажів. */
    SalesSummary calculate(List<Sale> sales);
}
