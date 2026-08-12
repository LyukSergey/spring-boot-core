package com.example.sales.adapter.mail;

import com.example.sales.domain.ReportData;
import com.example.sales.port.ReportSender;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Надсилання звіту email-ом із вкладенням (реалізація порту {@link ReportSender}).
 * <p>Використовує {@link MimeMessageHelper} у режимі multipart для додавання вкладення.
 */
@Component
public class EmailReportSender implements ReportSender {

    private final JavaMailSender mail;
    private final List<String> recipients;
    private final String from;

    public EmailReportSender(JavaMailSender mail,
                             @Value("${report.recipients}") List<String> recipients,
                             @Value("${spring.mail.username:}") String from) {
        this.mail = mail;
        this.recipients = recipients;
        this.from = from;
    }

    @Override
    public void send(ReportData data, byte[] document, String filename) {
        try {
            MimeMessage msg = mail.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(msg, true, "UTF-8"); // true = multipart
            if (from != null && !from.isBlank()) {
                helper.setFrom(from);
            }
            helper.setTo(recipients.toArray(new String[0]));
            helper.setSubject("Звіт про продажі за " + data.period());
            helper.setText("Вітаю! У вкладенні — звіт про продажі за " + data.period() + ".");
            helper.addAttachment(filename, new ByteArrayResource(document));
            mail.send(msg);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to send report email for " + data.period(), e);
        }
    }
}
