package com.example.places.geo;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Тести обгортки геокодування з МОКОМ клієнта (без реального Nominatim).
 * Перевіряють стійкість до збоїв — головну вимогу завдання.
 */
class GeocodingServiceTest {

    @Test
    void returnsCoordinatesFromClient() {
        GeocodingClient client = mock(GeocodingClient.class);
        when(client.geocode("Львів, Ринок")).thenReturn(Optional.of(new Coordinates(49.84, 24.03)));

        GeocodingService service = new GeocodingServiceImpl(client);

        assertThat(service.geocode("Львів, Ринок")).contains(new Coordinates(49.84, 24.03));
    }

    @Test
    void returnsEmptyWhenAddressNotFound() {
        GeocodingClient client = mock(GeocodingClient.class);
        when(client.geocode("не існує")).thenReturn(Optional.empty());

        GeocodingService service = new GeocodingServiceImpl(client);

        assertThat(service.geocode("не існує")).isEmpty();
    }

    @Test
    void swallowsApiFailureAndReturnsEmpty() {
        // Стійкість до збоїв: якщо API кинув виняток — сервіс НЕ падає, а віддає empty.
        GeocodingClient client = mock(GeocodingClient.class);
        when(client.geocode("Львів")).thenThrow(new RuntimeException("Nominatim down"));

        GeocodingService service = new GeocodingServiceImpl(client);

        assertThat(service.geocode("Львів")).isEmpty();
    }
}
