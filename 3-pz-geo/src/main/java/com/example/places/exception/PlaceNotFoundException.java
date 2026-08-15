package com.example.places.exception;

/** Кидається, коли місце з таким id не знайдено → мапиться у 404. */
public class PlaceNotFoundException extends RuntimeException {

    public PlaceNotFoundException(Long id) {
        super("Місце з id=" + id + " не знайдено");
    }
}
