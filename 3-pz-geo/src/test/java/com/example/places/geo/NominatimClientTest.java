package com.example.places.geo;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Детермінований тест розбору відповіді Nominatim через локальний HTTP-стаб.
 * Мережа НЕ потрібна — запускається завжди в {@code mvn test}.
 *
 * <p>Піднімає мінімальний HTTP-сервер на localhost, віддає фіксований JSON у форматі
 * Nominatim і перевіряє, що клієнт коректно мапить lat/lon і слати обов'язковий User-Agent.
 */
class NominatimClientTest {

    @Test
    void parsesGeocodeResponseAndSendsUserAgent() throws Exception {
        String json = "[{\"lat\":\"49.8419\",\"lon\":\"24.0315\",\"display_name\":\"Львів, площа Ринок\"}]";
        try (StubHttpServer server = StubHttpServer.start(json)) {
            NominatimClient client = new NominatimClient(server.baseUrl(), "test-agent/1.0 (test@example.com)");

            Optional<Coordinates> result = client.geocode("Львів, площа Ринок");

            assertThat(result).contains(new Coordinates(49.8419, 24.0315));
            assertThat(server.lastUserAgent()).isEqualTo("test-agent/1.0 (test@example.com)");
            assertThat(server.lastPath()).startsWith("/search");
        }
    }

    @Test
    void returnsEmptyOnEmptyArray() throws Exception {
        try (StubHttpServer server = StubHttpServer.start("[]")) {
            NominatimClient client = new NominatimClient(server.baseUrl(), "test-agent/1.0 (test@example.com)");

            assertThat(client.geocode("нічого не знайдено")).isEmpty();
        }
    }

    @Test
    void parsesReverseResponse() throws Exception {
        String json = "{\"lat\":\"49.8419\",\"lon\":\"24.0315\",\"display_name\":\"Львів, площа Ринок, Україна\"}";
        try (StubHttpServer server = StubHttpServer.start(json)) {
            NominatimClient client = new NominatimClient(server.baseUrl(), "test-agent/1.0 (test@example.com)");

            assertThat(client.reverse(49.8419, 24.0315)).contains("Львів, площа Ринок, Україна");
        }
    }
}
