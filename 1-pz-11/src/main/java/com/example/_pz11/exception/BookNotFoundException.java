package com.example._pz11.exception;

/**
 * Виключення, яке кидається, коли книгу не знайдено в сховищі.
 * Кидається із сервісу.
 */
public class BookNotFoundException extends RuntimeException {

    public BookNotFoundException(Long id) {
        super("Книгу з id=" + id + " не знайдено");
    }
}
