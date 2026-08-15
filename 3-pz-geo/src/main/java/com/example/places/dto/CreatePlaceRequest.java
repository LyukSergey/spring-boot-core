package com.example.places.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Запит на створення місця. Координати НЕ передаються —
 * їх сервіс підтягне з Nominatim за адресою.
 */
public record CreatePlaceRequest(
        @NotBlank String name,
        @NotBlank String address,
        String category) {
}
