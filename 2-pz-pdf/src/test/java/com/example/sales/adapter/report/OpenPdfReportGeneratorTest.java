package com.example.sales.adapter.report;

import com.example.sales.domain.ReportData;
import com.example.sales.domain.Sale;
import com.example.sales.domain.SalesSummary;
import com.example.sales.port.ReportGenerator;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Тест <b>незалежної</b> генерації PDF.
 * <p>Створює {@link OpenPdfReportGenerator} напряму (без Spring-контексту), передаючи
 * байти шрифту, генерує реальний PDF із кириличними даними й перевіряє його валідність.
 * Згенерований файл також зберігається у {@code target/} — його можна відкрити очима.
 */
class OpenPdfReportGeneratorTest {

    /** Кириличні тестові дані за вересень 2026. */
    private static final List<Sale> SALES = List.of(
            new Sale(1L, "Іван Петренко", "Ноутбук Lenovo", new BigDecimal("25000.00"), "Захід", LocalDate.of(2026, 9, 1)),
            new Sale(2L, "Іван Петренко", "Монітор Dell", new BigDecimal("8000.50"), "Схід", LocalDate.of(2026, 9, 3)),
            new Sale(3L, "Олег Ковальчук", "Клавіатура", new BigDecimal("1200.00"), "Захід", LocalDate.of(2026, 9, 5)),
            new Sale(4L, "Марія Шевченко", "Принтер HP", new BigDecimal("6500.00"), "Центр", LocalDate.of(2026, 9, 10))
    );

    private ReportGenerator generator() throws IOException {
        return new OpenPdfReportGenerator(fontBytes());
    }

    private byte[] fontBytes() throws IOException {
        try (InputStream in = getClass().getResourceAsStream("/fonts/DejaVuSans.ttf")) {
            assertThat(in).as("DejaVuSans.ttf must be on the classpath").isNotNull();
            return in.readAllBytes();
        }
    }

    private ReportData sampleData() {
        BigDecimal total = SALES.stream().map(Sale::amount).reduce(BigDecimal.ZERO, BigDecimal::add);
        SalesSummary summary = new DefaultSummaryCalculator().calculate(SALES);
        assertThat(summary.total()).isEqualByComparingTo(total);
        return new ReportData(YearMonth.of(2026, 9), SALES, summary);
    }

    @Test
    void generatesValidNonEmptyPdfWithCyrillicData() throws IOException {
        ReportGenerator gen = generator();

        byte[] pdf = gen.generate(sampleData());

        assertThat(pdf).isNotEmpty();
        // Сигнатура PDF-файлу
        assertThat(new String(pdf, 0, 5)).startsWith("%PDF-");
        // Метадані порту
        assertThat(gen.contentType()).isEqualTo("application/pdf");
        assertThat(gen.fileExtension()).isEqualTo("pdf");

        // Зберігаємо результат, щоб можна було відкрити й перевірити кирилицю очима
        Path outFile = Path.of("target", "sample-sales-2026-09.pdf");
        Files.createDirectories(outFile.getParent());
        Files.write(outFile, pdf);
        assertThat(Files.size(outFile)).isGreaterThan(1000L);
        System.out.println("PDF saved to: " + outFile.toAbsolutePath());
    }

    @Test
    void handlesEmptySalesGracefully() throws IOException {
        ReportGenerator gen = generator();
        SalesSummary empty = new DefaultSummaryCalculator().calculate(List.of());
        ReportData data = new ReportData(YearMonth.of(2026, 9), List.of(), empty);

        byte[] pdf = gen.generate(data);

        assertThat(pdf).isNotEmpty();
        assertThat(new String(pdf, 0, 5)).startsWith("%PDF-");
    }

    @Test
    void rejectsEmptyFont() {
        assertThatThrownBy(() -> new OpenPdfReportGenerator(new byte[0]))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
