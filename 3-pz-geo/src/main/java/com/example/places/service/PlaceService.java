package com.example.places.service;

import com.example.places.dto.CreatePlaceRequest;
import com.example.places.dto.NearbyDto;
import com.example.places.dto.PlaceDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * Бізнес-логіка місць. Інтерфейс (архітектура) —
 * реалізацію писати студенту в {@link PlaceServiceImpl}.
 */
public interface PlaceService {

    /** Створити місце: зберегти + підтягнути координати за адресою через гео-сервіс. */
    PlaceDto create(CreatePlaceRequest req);

    /** Місця однієї категорії з пагінацією. */
    Page<PlaceDto> byCategory(String category, Pageable pageable);

    /** Пошук за частиною назви (регістронезалежний). */
    List<PlaceDto> searchByName(String part);

    /** Місця в радіусі radiusKm від (lat,lon), відсортовані за відстанню (Haversine). */
    List<NearbyDto> nearby(double lat, double lon, double radiusKm);

    /** Видалити місце; кинути PlaceNotFoundException, якщо не існує. */
    void delete(Long id);
}
