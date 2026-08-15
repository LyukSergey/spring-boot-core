package com.example.places.geo;

import java.util.Optional;

/**
 * Обгортка над зовнішнім гео-API з кешем і стійкістю до збоїв.
 * Інтерфейс (архітектура) — реалізація в {@link GeocodingServiceImpl}.
 */
public interface GeocodingService {

    /** Адреса → координати. Порожньо, якщо не знайдено АБО API недоступний (без винятків нагору). */
    Optional<Coordinates> geocode(String address);

    /** Координати → адреса (reverse-геокодування, бонус). */
    Optional<String> reverse(double lat, double lon);
}
