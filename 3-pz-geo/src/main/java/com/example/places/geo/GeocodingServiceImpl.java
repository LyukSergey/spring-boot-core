package com.example.places.geo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * ПОВНА реалізація гео-сервісу: кеш + стійкість до збоїв.
 *
 * <ul>
 *   <li>{@code @Cacheable} — та сама адреса не піде в зовнішній API двічі
 *       (вимога завдання — не спамити Nominatim).</li>
 *   <li>{@code try/catch} — якщо API недоступний, повертаємо {@code empty},
 *       а не валимо запит 500-кою; місце збережеться без координат,
 *       догеокодуємо пізніше.</li>
 * </ul>
 */
@Service
public class GeocodingServiceImpl implements GeocodingService {

    private static final Logger log = LoggerFactory.getLogger(GeocodingServiceImpl.class);

    private final GeocodingClient client;

    public GeocodingServiceImpl(GeocodingClient client) {
        this.client = client;
    }

    @Override
    @Cacheable("geocode")
    public Optional<Coordinates> geocode(String address) {
        try {
            return client.geocode(address);
        } catch (Exception e) {
            log.warn("Geocoding failed for '{}': {}", address, e.getMessage());
            return Optional.empty();
        }
    }

    @Override
    @Cacheable("reverse")
    public Optional<String> reverse(double lat, double lon) {
        try {
            return client.reverse(lat, lon);
        } catch (Exception e) {
            log.warn("Reverse geocoding failed for ({}, {}): {}", lat, lon, e.getMessage());
            return Optional.empty();
        }
    }
}
