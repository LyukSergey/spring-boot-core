package com.example.places.dto;

/** Місце + його відстань (км) від точки запиту у пошуку поблизу. */
public record NearbyDto(PlaceDto place, double distanceKm) {
}
