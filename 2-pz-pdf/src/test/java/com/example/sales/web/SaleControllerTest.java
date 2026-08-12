package com.example.sales.web;

import com.example.sales.port.ReportSender;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Інтеграційний тест REST-шару: POST/GET /sales, валідація, PDF-ендпоінт.
 * <p>SMTP замоканий, щоб контекст піднявся без реального поштового сервера.
 */
@SpringBootTest
@AutoConfigureMockMvc
class SaleControllerTest {

    @Autowired
    MockMvc mvc;

    @MockBean
    JavaMailSender mailSender;

    @MockBean
    ReportSender reportSender;

    @Test
    void createsSaleAndListsIt() throws Exception {
        // дата в минулому (@PastOrPresent) — незалежно від сьогоднішнього дня
        String body = """
                {"manager":"Іван","product":"Ноутбук","amount":25000,"region":"Захід","date":"2026-07-05"}
                """;

        mvc.perform(post("/sales").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.manager", is("Іван")))
                .andExpect(jsonPath("$.region", is("Захід")));

        mvc.perform(get("/sales").param("region", "Захід"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].manager", is("Іван")));
    }

    @Test
    void rejectsInvalidSaleWith400() throws Exception {
        // порожній manager, від'ємна сума (дата валідна — щоб 400 був саме через ці поля)
        String body = """
                {"manager":"","product":"X","amount":-5,"region":"Захід","date":"2026-07-05"}
                """;

        mvc.perform(post("/sales").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.fields.manager").exists())
                .andExpect(jsonPath("$.fields.amount").exists());
    }

    @Test
    void returnsPdfForMonth() throws Exception {
        mvc.perform(get("/reports/sales.pdf").param("month", "2026-09"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_PDF))
                .andExpect(header().string("Content-Disposition", is("inline; filename=sales-2026-09.pdf")));
    }
}
