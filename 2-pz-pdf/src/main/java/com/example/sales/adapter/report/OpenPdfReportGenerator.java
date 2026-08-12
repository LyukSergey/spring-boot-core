package com.example.sales.adapter.report;

import com.example.sales.domain.ReportData;
import com.example.sales.domain.Sale;
import com.example.sales.domain.SalesSummary;
import com.example.sales.port.ReportGenerator;
import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * Генерація PDF-звіту через OpenPDF (форк iText 4, вільна ліцензія).
 *
 * <p><b>Абсолютно незалежна реалізація.</b> Клас:
 * <ul>
 *   <li>залежить лише від доменних моделей ({@link ReportData} та ін.) і власного порту
 *       {@link ReportGenerator} — <b>жодних</b> Spring Web / Mail / сховища;</li>
 *   <li>приймає шрифт як {@code byte[]} у конструкторі, а не Spring {@code Resource} чи
 *       {@code @Value} — тому створюється й тестується <b>без Spring-контексту</b>
 *       (див. {@code OpenPdfReportGeneratorTest}).</li>
 * </ul>
 *
 * <p><b>Кирилиця.</b> Головна пастка ПЗ: стандартні шрифти iText/PDFBox не малюють
 * кирилицю. Тут вбудовується TTF-шрифт з підтримкою кирилиці через
 * {@code BaseFont.createFont(..., IDENTITY_H, EMBEDDED, ...)} — саме {@code IDENTITY_H}
 * + вбудований TTF роблять кирилицю читабельною замість «квадратиків».
 */
public class OpenPdfReportGenerator implements ReportGenerator {

    private static final Color HEADER_BG = new Color(230, 230, 230);
    private static final Color BAR_COLOR = new Color(52, 120, 200);

    /** Байти TTF-шрифту з підтримкою кирилиці (напр. DejaVu Sans). */
    private final byte[] cyrillicTtf;

    /**
     * @param cyrillicTtf вміст TTF-файлу зі шрифтом, що підтримує кирилицю
     */
    public OpenPdfReportGenerator(byte[] cyrillicTtf) {
        if (cyrillicTtf == null || cyrillicTtf.length == 0) {
            throw new IllegalArgumentException("Cyrillic TTF font bytes must not be empty");
        }
        this.cyrillicTtf = cyrillicTtf.clone();
    }

    @Override
    public String contentType() {
        return "application/pdf";
    }

    @Override
    public String fileExtension() {
        return "pdf";
    }

    @Override
    public byte[] generate(ReportData data) {
        try {
            return build(data);
        } catch (DocumentException e) {
            throw new ReportGenerationException("Failed to build PDF report", e);
        }
    }

    private byte[] build(ReportData data) throws DocumentException {
        BaseFont bf = cyrillicBaseFont();
        Font h1 = new Font(bf, 18, Font.BOLD);
        Font h2 = new Font(bf, 13, Font.BOLD);
        Font normal = new Font(bf, 11);
        Font small = new Font(bf, 9);

        SalesSummary summary = data.summary();
        List<Sale> sales = data.sales();

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document doc = new Document(PageSize.A4, 40, 40, 50, 40);
        PdfWriter.getInstance(doc, out);
        doc.open();

        // Заголовок і період
        doc.add(new Paragraph("Звіт про продажі", h1));
        doc.add(new Paragraph("Період: " + data.period(), normal));
        doc.add(new Paragraph("Кількість продажів: " + summary.count(), normal));
        doc.add(new Paragraph("Загальна сума: " + summary.total() + " грн", h2));
        doc.add(Chunk.NEWLINE);

        // Таблиця продажів
        doc.add(new Paragraph("Деталі:", h2));
        doc.add(salesTable(sales, h2, normal));
        doc.add(Chunk.NEWLINE);

        // Підсумки по регіонах і менеджерах
        addBreakdown(doc, "Підсумок по регіонах", summary.byRegion(), h2, normal);
        addBreakdown(doc, "Топ-менеджери", summary.byManager(), h2, normal);

        // Бонус: діаграма (стовпчики) по регіонах
        if (!summary.byRegion().isEmpty()) {
            doc.add(new Paragraph("Діаграма по регіонах", h2));
            doc.add(barChart(summary.byRegion(), summary.total(), normal, small));
        }

        doc.close();
        return out.toByteArray();
    }

    /**
     * Головний рядок завдання: без вбудованого TTF-шрифту з {@code IDENTITY_H}
     * кирилиця перетворюється на «квадратики».
     */
    private BaseFont cyrillicBaseFont() {
        try {
            return BaseFont.createFont(
                    "cyrillic-font.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED,
                    true, cyrillicTtf, null);
        } catch (Exception e) {
            throw new ReportGenerationException("Failed to load cyrillic TTF font", e);
        }
    }

    private PdfPTable salesTable(List<Sale> sales, Font headFont, Font cellFont) {
        PdfPTable table = new PdfPTable(new float[]{2, 3, 3, 2, 2});
        table.setWidthPercentage(100);
        for (String head : List.of("Дата", "Менеджер", "Товар", "Регіон", "Сума")) {
            PdfPCell c = new PdfPCell(new Phrase(head, headFont));
            c.setBackgroundColor(HEADER_BG);
            c.setPadding(5f);
            table.addCell(c);
        }
        for (Sale s : sales) {
            table.addCell(cell(s.date().toString(), cellFont));
            table.addCell(cell(s.manager(), cellFont));
            table.addCell(cell(s.product(), cellFont));
            table.addCell(cell(s.region(), cellFont));
            table.addCell(cell(s.amount().toPlainString(), cellFont));
        }
        return table;
    }

    private PdfPCell cell(String text, Font font) {
        PdfPCell c = new PdfPCell(new Phrase(text, font));
        c.setPadding(4f);
        return c;
    }

    private void addBreakdown(Document doc, String title,
                              Map<String, BigDecimal> map, Font h2, Font normal) throws DocumentException {
        doc.add(new Paragraph(title, h2));
        map.entrySet().stream()
                .sorted(Map.Entry.<String, BigDecimal>comparingByValue().reversed()) // від більшого
                .forEach(e -> addLine(doc, "  " + e.getKey() + ": " + e.getValue() + " грн", normal));
        doc.add(Chunk.NEWLINE);
    }

    /** Проста горизонтальна діаграма-стовпчики по регіонах (bonus). */
    private PdfPTable barChart(Map<String, BigDecimal> byRegion, BigDecimal total, Font labelFont, Font valueFont) {
        PdfPTable chart = new PdfPTable(new float[]{3, 7});
        chart.setWidthPercentage(100);

        BigDecimal max = byRegion.values().stream()
                .max(Comparator.naturalOrder())
                .orElse(BigDecimal.ONE);
        if (max.signum() == 0) {
            max = BigDecimal.ONE;
        }

        List<Map.Entry<String, BigDecimal>> ordered = byRegion.entrySet().stream()
                .sorted(Map.Entry.<String, BigDecimal>comparingByValue().reversed())
                .toList();

        for (Map.Entry<String, BigDecimal> e : ordered) {
            chart.addCell(cell(e.getKey(), labelFont));

            // Ширина стовпчика пропорційна до максимуму (від 1 до 40 «блоків»)
            int width = e.getValue()
                    .multiply(BigDecimal.valueOf(40))
                    .divide(max, 0, java.math.RoundingMode.HALF_UP)
                    .intValue();
            width = Math.max(width, 1);

            PdfPCell bar = new PdfPCell();
            bar.setBorder(0);
            bar.setPadding(2f);

            PdfPTable inner = new PdfPTable(1);
            inner.setWidthPercentage(Math.max(5, Math.min(100, width * 100 / 40)));
            PdfPCell colored = new PdfPCell(new Phrase(" " + e.getValue() + " грн", valueFont));
            colored.setBackgroundColor(BAR_COLOR);
            colored.setBorder(0);
            colored.setPadding(3f);
            colored.setHorizontalAlignment(Element.ALIGN_LEFT);
            inner.addCell(colored);
            bar.addElement(inner);

            chart.addCell(bar);
        }
        return chart;
    }

    private void addLine(Document doc, String text, Font font) {
        try {
            doc.add(new Paragraph(text, font));
        } catch (DocumentException ex) {
            throw new ReportGenerationException("Failed to add paragraph to PDF", ex);
        }
    }
}
