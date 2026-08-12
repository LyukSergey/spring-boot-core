package com.example.sales.web;

import com.example.sales.port.ReportService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.YearMonth;

/**
 * REST API звітів: віддача PDF та ручна email-розсилка.
 * <p>Залежить лише від порту {@link ReportService}.
 */
@RestController
@RequestMapping("/reports")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    /** {@code GET /reports/sales.pdf?month=2026-09} — віддати PDF (браузер відкриє inline). */
    @GetMapping("/sales.pdf")
    public ResponseEntity<byte[]> pdf(@RequestParam String month) {
        YearMonth period = YearMonth.parse(month);
        byte[] body = reportService.generate(period);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=sales-" + month + ".pdf")
                .body(body);
    }

    /** {@code POST /reports/send?month=2026-09} — згенерувати й розіслати email-ом. */
    @PostMapping("/send")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void send(@RequestParam String month) {
        reportService.generateAndSend(YearMonth.parse(month));
    }
}
