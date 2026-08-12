package com.example.sales.adapter.persistence;

import com.example.sales.domain.Sale;
import com.example.sales.port.SaleRepository;
import org.springframework.stereotype.Repository;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Потокобезпечне сховище продажів <b>у пам'яті</b> (без JPA/БД — вимога ПЗ-2).
 * <p>Реалізація порту {@link SaleRepository}. У майбутньому її місце може зайняти
 * JPA-репозиторій без зміни решти коду.
 */
@Repository
public class InMemorySaleRepository implements SaleRepository {

    private final ConcurrentHashMap<Long, Sale> data = new ConcurrentHashMap<>();
    private final AtomicLong seq = new AtomicLong();

    @Override
    public Sale add(Sale sale) {
        long id = seq.incrementAndGet();
        Sale withId = new Sale(id, sale.manager(), sale.product(), sale.amount(), sale.region(), sale.date());
        data.put(id, withId);
        return withId;
    }

    @Override
    public List<Sale> findAll() {
        return data.values().stream()
                .sorted(Comparator.comparing(Sale::id))
                .toList();
    }

    @Override
    public List<Sale> findByRegion(String region) {
        return data.values().stream()
                .filter(s -> s.region().equals(region))
                .sorted(Comparator.comparing(Sale::id))
                .toList();
    }

    @Override
    public List<Sale> findByMonth(int year, int month) {
        return data.values().stream()
                .filter(s -> s.date().getYear() == year && s.date().getMonthValue() == month)
                .sorted(Comparator.comparing(Sale::date))
                .toList();
    }
}
