package com.example.places.geo;

import java.util.Optional;

/**
 * Низькорівневий клієнт зовнішнього гео-API. Інтерфейс дозволяє підмінити
 * реалізацію (реальний Nominatim / мок у тестах / інший провайдер).
 */
public interface GeocodingClient {

    /** Пряме геокодування: адреса → координати. Кидає виняток при мережевій/HTTP-помилці. */
    Optional<Coordinates> geocode(String address);

    /** Зворотне геокодування: координати → адреса. */
    Optional<String> reverse(double lat, double lon);
}
