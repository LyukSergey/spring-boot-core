package com.example.sales.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Вхідний DTO для створення продажу з валідацією. */
public record CreateSaleRequest(
        @NotBlank String manager,
        @NotBlank String product,
        @NotNull @Positive BigDecimal amount,
        @NotBlank String region,
        @NotNull @PastOrPresent LocalDate date
) {
}
