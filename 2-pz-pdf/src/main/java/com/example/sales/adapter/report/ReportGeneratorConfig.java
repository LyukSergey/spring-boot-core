package com.example.sales.adapter.report;

import com.example.sales.port.ReportGenerator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.io.UncheckedIOException;

/**
 * Spring-конфігурація генератора PDF.
 * <p>Завантажує TTF-шрифт із ресурсів і передає його байти в
 * {@link OpenPdfReportGenerator}. Завдяки цьому сама реалізація генератора не має
 * Spring-анотацій і залишається незалежною — тут лише «проводка».
 */
@Configuration
public class ReportGeneratorConfig {

    @Bean
    public ReportGenerator pdfReportGenerator(@Value("${report.font-path}") Resource fontResource) {
        try {
            byte[] ttf = fontResource.getInputStream().readAllBytes();
            return new OpenPdfReportGenerator(ttf);
        } catch (IOException e) {
            throw new UncheckedIOException(
                    "Cannot load cyrillic font from " + fontResource + " (report.font-path)", e);
        }
    }
}
