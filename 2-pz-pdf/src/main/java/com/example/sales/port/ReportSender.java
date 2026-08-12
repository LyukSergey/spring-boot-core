package com.example.sales.port;

import com.example.sales.domain.ReportData;

/**
 * Порт надсилання готового звіту отримувачам (email тощо).
 * <p>Реалізація ({@code EmailReportSender}) інкапсулює SMTP і вкладення.
 */
public interface ReportSender {

    /**
     * Надіслати документ звіту.
     *
     * @param data     дані звіту (для теми/тексту листа)
     * @param document байти згенерованого документа
     * @param filename ім'я файлу-вкладення
     */
    void send(ReportData data, byte[] document, String filename);
}
