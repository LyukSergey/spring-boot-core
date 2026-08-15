package com.example.places.repo;

import com.example.places.model.Place;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Репозиторій місць. Spring Data сам генерує реалізацію derived-queries —
 * тому це чистий інтерфейс (архітектура), який при цьому повністю робочий.
 */
public interface PlaceRepository extends JpaRepository<Place, Long> {

    /** Місця однієї категорії з пагінацією. */
    Page<Place> findByCategory(String category, Pageable pageable);

    /** Пошук за частиною назви (регістронезалежний). */
    List<Place> findByNameContainingIgnoreCase(String part);

    /**
     * Для пошуку поблизу тягнемо ЛИШЕ місця з координатами.
     * Свідомий компроміс: точний гео-пошук у SQL вимагав би PostGIS (див. README).
     */
    List<Place> findByLatIsNotNullAndLonIsNotNull();
}
