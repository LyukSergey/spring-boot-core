package com.example.sales.port;

import com.example.sales.domain.Sale;

import java.util.List;

/**
 * Порт сховища продажів.
 * <p>Абстрагує спосіб зберігання. У ПЗ-2 реалізація — у пам'яті
 * ({@code InMemorySaleRepository}); у майбутньому її може замінити JPA-репозиторій
 * без зміни решти коду.
 */
public interface SaleRepository {

    /** Додати продаж; повертає збережений об'єкт з присвоєним id. */
    Sale add(Sale sale);

    /** Усі продажі. */
    List<Sale> findAll();

    /** Продажі за конкретним регіоном. */
    List<Sale> findByRegion(String region);

    /** Продажі за конкретний місяць (для звіту), відсортовані за датою. */
    List<Sale> findByMonth(int year, int month);
}
