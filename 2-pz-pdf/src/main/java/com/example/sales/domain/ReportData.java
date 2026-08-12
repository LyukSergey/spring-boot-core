package com.example.sales.domain;

import java.time.YearMonth;
import java.util.List;

/**
 * Повний набір даних для одного звіту.
 * <p>Саме цей об'єкт передається генератору звіту (PDF/Excel/…). Завдяки цьому
 * реалізація генератора є <b>абсолютно незалежною</b>: вона отримує готові дані
 * й нічого не знає ні про сховище, ні про HTTP, ні про Spring.
 *
 * @param period  період звіту (місяць)
 * @param sales   продажі за період
 * @param summary порахований підсумок
 */
public record ReportData(
        YearMonth period,
        List<Sale> sales,
        SalesSummary summary
) {
}
