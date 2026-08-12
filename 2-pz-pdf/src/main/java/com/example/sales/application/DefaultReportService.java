package com.example.sales.application;

import com.example.sales.domain.ReportData;
import com.example.sales.domain.Sale;
import com.example.sales.domain.SalesSummary;
import com.example.sales.port.ReportGenerator;
import com.example.sales.port.ReportSender;
import com.example.sales.port.ReportService;
import com.example.sales.port.SaleRepository;
import com.example.sales.port.SummaryCalculator;
import org.springframework.stereotype.Service;

import java.time.YearMonth;
import java.util.List;

/**
 * Оркестратор звіту (реалізація порту {@link ReportService}).
 * <p>Зв'язує порти: сховище → підрахунок → генерація документа → надсилання.
 * Залежить лише від інтерфейсів, тому конкретні реалізації легко підмінити.
 */
@Service
public class DefaultReportService implements ReportService {

    private final SaleRepository repository;
    private final SummaryCalculator calculator;
    private final ReportGenerator generator;
    private final ReportSender sender;

    public DefaultReportService(SaleRepository repository,
                                SummaryCalculator calculator,
                                ReportGenerator generator,
                                ReportSender sender) {
        this.repository = repository;
        this.calculator = calculator;
        this.generator = generator;
        this.sender = sender;
    }

    @Override
    public ReportData collect(YearMonth period) {
        List<Sale> sales = repository.findByMonth(period.getYear(), period.getMonthValue());
        SalesSummary summary = calculator.calculate(sales);
        return new ReportData(period, sales, summary);
    }

    @Override
    public byte[] generate(YearMonth period) {
        return generator.generate(collect(period));
    }

    @Override
    public void generateAndSend(YearMonth period) {
        ReportData data = collect(period);
        byte[] document = generator.generate(data);
        String filename = "sales-" + period + "." + generator.fileExtension();
        sender.send(data, document, filename);
    }
}
