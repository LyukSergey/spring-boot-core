package com.example.sales.application;

import com.example.sales.port.ReportService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.YearMonth;

/**
 * Автоматична щомісячна розсилка звіту.
 * <p>За cron-виразом {@code report.cron} (1-го числа кожного місяця) формує й розсилає
 * звіт за <b>попередній</b> місяць.
 */
@Component
public class ScheduledReport {

    private static final Logger log = LoggerFactory.getLogger(ScheduledReport.class);

    private final ReportService reportService;

    public ScheduledReport(ReportService reportService) {
        this.reportService = reportService;
    }

    @Scheduled(cron = "${report.cron}")
    public void monthly() {
        YearMonth previous = YearMonth.now().minusMonths(1);
        try {
            reportService.generateAndSend(previous);
            log.info("Monthly sales report for {} sent", previous);
        } catch (Exception e) {
            log.error("Monthly report for {} failed", previous, e);
        }
    }
}
