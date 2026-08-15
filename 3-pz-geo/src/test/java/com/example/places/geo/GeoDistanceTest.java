package com.example.places.geo;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/** Тест формули Haversine — без мережі, без БД (чиста функція). */
class GeoDistanceTest {

    @Test
    void computesKnownDistanceLvivKyiv() {
        // Львів ↔ Київ ≈ 470 км
        double d = GeoDistance.km(49.8397, 24.0297, 50.4501, 30.5234);
        assertThat(d).isBetween(460.0, 480.0);
    }

    @Test
    void samePointIsZero() {
        assertThat(GeoDistance.km(49.8419, 24.0315, 49.8419, 24.0315)).isZero();
    }

    @Test
    void isSymmetric() {
        double ab = GeoDistance.km(49.84, 24.03, 50.45, 30.52);
        double ba = GeoDistance.km(50.45, 30.52, 49.84, 24.03);
        assertThat(ab).isCloseTo(ba, org.assertj.core.data.Offset.offset(1e-9));
    }

    @Test
    void oneDegreeOfLatitudeIsAbout111km() {
        // 1° широти по меридіану ≈ 111 км (перевіряє масштаб/радіус Землі)
        double d = GeoDistance.km(49.0, 24.0, 50.0, 24.0);
        assertThat(d).isBetween(110.0, 112.0);
    }
}
