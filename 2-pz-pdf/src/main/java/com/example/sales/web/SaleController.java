package com.example.sales.web;

import com.example.sales.domain.Sale;
import com.example.sales.port.SaleRepository;
import com.example.sales.web.dto.CreateSaleRequest;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

/**
 * REST API продажів.
 * <p>Тонкий шар: делегує до порту {@link SaleRepository}, жодної бізнес-логіки в контролері.
 */
@RestController
@RequestMapping("/sales")
public class SaleController {

    private final SaleRepository repository;

    public SaleController(SaleRepository repository) {
        this.repository = repository;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Sale add(@Valid @RequestBody CreateSaleRequest r) {
        return repository.add(new Sale(null, r.manager(), r.product(), r.amount(), r.region(), r.date()));
    }

    /**
     * Список продажів із опційними фільтрами за регіоном і періодом.
     *
     * @param region фільтр за регіоном (необов'язковий)
     * @param from   початок періоду включно (необов'язковий)
     * @param to     кінець періоду включно (необов'язковий)
     */
    @GetMapping
    public List<Sale> list(
            @RequestParam(required = false) String region,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {

        return repository.findAll().stream()
                .filter(s -> region == null || s.region().equals(region))
                .filter(s -> from == null || !s.date().isBefore(from))
                .filter(s -> to == null || !s.date().isAfter(to))
                .toList();
    }
}
