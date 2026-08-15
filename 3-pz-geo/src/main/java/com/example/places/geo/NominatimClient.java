package com.example.places.geo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Optional;

/**
 * Геокодування через OpenStreetMap Nominatim. ПОВНА реалізація зовнішнього API.
 *
 * <p>ПРАВИЛА сервісу (порушиш — заблокують):
 * <ul>
 *   <li>обов'язковий свій {@code User-Agent} із контактом — інакше 403;</li>
 *   <li>не частіше 1 запиту/сек — тут реалізовано простим throttle.</li>
 * </ul>
 * Повтори тієї ж адреси у мережу не йдуть — їх ловить кеш у {@link GeocodingService}.
 */
@Component
public class NominatimClient implements GeocodingClient {

    private static final Logger log = LoggerFactory.getLogger(NominatimClient.class);

    /** Мінімальний інтервал між запитами до Nominatim (правило 1 req/s). */
    private static final long MIN_INTERVAL_MS = 1000L;

    private final RestClient http;

    /** Час останнього запиту — для дотримання 1 req/s. */
    private long lastCallAt = 0L;

    public NominatimClient(@Value("${nominatim.base-url}") String baseUrl,
                           @Value("${nominatim.user-agent}") String userAgent) {
        this.http = RestClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("User-Agent", userAgent) // ← без цього Nominatim відмовляє (403)
                .build();
    }

    /** Сира відповідь Nominatim; нам треба лише lat/lon, решту ігноруємо. */
    public record NominatimResult(String lat, String lon, String display_name) {
    }

    /**
     * Пряме геокодування: адреса → координати.
     * Кидає виняток лише при мережевій/HTTP-помилці — його ловить {@link GeocodingService}.
     */
    @Override
    public Optional<Coordinates> geocode(String address) {
        throttle();
        NominatimResult[] results = http.get()
                .uri(uri -> uri.path("/search")
                        .queryParam("q", address)
                        .queryParam("format", "json")
                        .queryParam("limit", 1)
                        .build())
                .retrieve()
                .body(NominatimResult[].class);

        if (results == null || results.length == 0) {
            return Optional.empty();
        }
        NominatimResult r = results[0];
        return Optional.of(new Coordinates(Double.parseDouble(r.lat()), Double.parseDouble(r.lon())));
    }

    /**
     * Зворотне (reverse) геокодування: координати → адреса. Бонус із завдання.
     */
    @Override
    public Optional<String> reverse(double lat, double lon) {
        throttle();
        NominatimResult r = http.get()
                .uri(uri -> uri.path("/reverse")
                        .queryParam("lat", lat)
                        .queryParam("lon", lon)
                        .queryParam("format", "json")
                        .build())
                .retrieve()
                .body(NominatimResult.class);

        return Optional.ofNullable(r).map(NominatimResult::display_name);
    }

    /** Простий throttle: гарантує паузу ≥ 1 c між викликами до Nominatim. */
    private synchronized void throttle() {
        long now = System.currentTimeMillis();
        long wait = MIN_INTERVAL_MS - (now - lastCallAt);
        if (wait > 0) {
            try {
                Thread.sleep(wait);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.warn("Throttle sleep interrupted", e);
            }
        }
        lastCallAt = System.currentTimeMillis();
    }
}
