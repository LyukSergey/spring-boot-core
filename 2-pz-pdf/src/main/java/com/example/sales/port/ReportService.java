package com.example.sales.port;

import com.example.sales.domain.ReportData;

import java.time.YearMonth;

/**
 * Порт застосунку-оркестратора: збирає дані → рахує підсумок → генерує документ → (за потреби) надсилає.
 * <p>Це фасад, за яким ховаються всі інші порти. Контролери й планувальник залежать
 * тільки від нього.
 */
public interface ReportService {

    /** Зібрати дані звіту за місяць (продажі + підсумок). */
    ReportData collect(YearMonth period);

    /** Згенерувати документ звіту за місяць. */
    byte[] generate(YearMonth period);

    /** Згенерувати й надіслати звіт за місяць отримувачам. */
    void generateAndSend(YearMonth period);
}
