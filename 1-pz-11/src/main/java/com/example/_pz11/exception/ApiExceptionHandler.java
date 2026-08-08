package com.example._pz11.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Глобальний обробник виключень для всіх контролерів.
 *
 * @RestControllerAdvice перехоплює виключення, кинуті з контролерів/сервісів,
 * і перетворює їх на HTTP-відповідь із правильним статус-кодом.
 */
@RestControllerAdvice
public class ApiExceptionHandler {

    /**
     * Книгу не знайдено → 404 Not Found.
     */
    @ExceptionHandler(BookNotFoundException.class)
    public ProblemDetail handleNotFound(BookNotFoundException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    /**
     * Порушення бізнес-правила (ліміт каталогу, книгу вже видано) → 409 Conflict.
     */
    @ExceptionHandler(IllegalStateException.class)
    public ProblemDetail handleConflict(IllegalStateException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
    }
}
