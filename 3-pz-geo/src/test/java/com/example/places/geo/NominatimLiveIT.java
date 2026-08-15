package com.example.places.geo;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Реальна інтеграція з ЖИВИМ Nominatim (ходить у мережу).
 * Вимкнено за замовчуванням, щоб {@code mvn test} не залежав від інтернету / rate-limit.
 *
 * <p>Запуск: {@code mvn test -Dnominatim.live=true -Dtest=NominatimLiveIT}
 *
 * <p>Перевіряє, що реальний сервіс повертає координати Львова в очікуваному діапазоні
 * і що зворотне геокодування дає непорожню адресу.
 */
@EnabledIfSystemProperty(named = "nominatim.live", matches = "true")
class NominatimLiveIT {

    private static final String BASE_URL = "https://nominatim.openstreetmap.org";
    private static final String USER_AGENT = "student-places-app/1.0 (lyukstermoizol@gmail.com)";

    @Test
    void geocodesRealLvivAddress() {
        NominatimClient client = new NominatimClient(BASE_URL, USER_AGENT);

        Optional<Coordinates> coords = client.geocode("Львів, площа Ринок");

        assertThat(coords).isPresent();
        assertThat(coords.get().lat()).isBetween(49.0, 50.5);
        assertThat(coords.get().lon()).isBetween(23.5, 24.5);
    }

    @Test
    void reverseGeocodesRealCoordinates() {
        NominatimClient client = new NominatimClient(BASE_URL, USER_AGENT);

        Optional<String> address = client.reverse(49.8419, 24.0315);

        assertThat(address).isPresent();
        assertThat(address.get()).containsIgnoringCase("Львів");
    }
}
