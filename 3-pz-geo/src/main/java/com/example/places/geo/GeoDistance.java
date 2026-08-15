package com.example.places.geo;

/**
 * Відстань між двома точками на сфері (Земля) за формулою Haversine. Результат — кілометри.
 *
 * <p>Чому Haversine, а не Піфагор: координати — це кути, а не метри; 1° довготи біля
 * екватора ≈ 111 км, а біля полюса → 0. Множник {@code cos(lat1)·cos(lat2)} у формулі
 * коригує «сходження меридіанів» і дає відстань по дузі великого кола.
 *
 * <p>Чиста функція без БД/мережі — ідеально тестується (див. GeoDistanceTest).
 * ПОВНА реалізація.
 */
public final class GeoDistance {

    private static final double EARTH_RADIUS_KM = 6371.0;

    private GeoDistance() {
    }

    public static double km(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return EARTH_RADIUS_KM * c;
    }
}
