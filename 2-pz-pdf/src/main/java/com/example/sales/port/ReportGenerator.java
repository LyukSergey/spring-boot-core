package com.example.sales.port;

import com.example.sales.domain.ReportData;

/**
 * Порт генерації звіту у бінарний документ (PDF, Excel тощо).
 * <p><b>Головна точка абстракції завдання.</b> Реалізація ({@code OpenPdfReportGenerator})
 * залежить лише від доменних моделей і цього інтерфейсу — вона <b>абсолютно незалежна</b>
 * від Spring Web, пошти та сховища, тому тестується ізольовано.
 */
public interface ReportGenerator {

    /**
     * Згенерувати документ звіту.
     *
     * @param data готові дані звіту (період, продажі, підсумок)
     * @return байти згенерованого документа
     */
    byte[] generate(ReportData data);

    /** MIME-тип згенерованого документа (напр. {@code application/pdf}). */
    String contentType();

    /** Розширення файлу без крапки (напр. {@code pdf}). */
    String fileExtension();
}
