package com.example.sales.adapter.report;

/** Помилка під час генерації документа звіту. */
public class ReportGenerationException extends RuntimeException {

    public ReportGenerationException(String message, Throwable cause) {
        super(message, cause);
    }
}
