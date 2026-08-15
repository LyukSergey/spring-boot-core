package com.example.places;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

/**
 * Точка входу гео-сервісу «Мої місця».
 *
 * <p>{@code @EnableCaching} вмикає Spring Cache — потрібен, щоб {@code @Cacheable}
 * у {@link com.example.places.geo.GeocodingService} не ходив у Nominatim двічі за
 * тією ж адресою (вимога завдання «не спамити зовнішній API»).
 */
@SpringBootApplication
@EnableCaching
public class PlacesApplication {

    public static void main(String[] args) {
        SpringApplication.run(PlacesApplication.class, args);
    }
}
