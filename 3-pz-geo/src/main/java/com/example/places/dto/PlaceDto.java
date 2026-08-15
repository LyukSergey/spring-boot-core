package com.example.places.dto;

import com.example.places.model.Place;

/** Відповідь API: місце з координатами (lat/lon можуть бути null). */
public record PlaceDto(
        Long id,
        String name,
        String address,
        String category,
        Double lat,
        Double lon) {

    public static PlaceDto of(Place p) {
        return new PlaceDto(p.getId(), p.getName(), p.getAddress(), p.getCategory(), p.getLat(), p.getLon());
    }
}
