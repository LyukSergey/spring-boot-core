package com.example._pz11.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Перший ендпоінт.
 *
 * @RestController робить клас біном-контролером, що повертає дані клієнту,
 * а не HTML-сторінку.
 * @GetMapping("/hello") прив'язує метод до GET-запиту за адресою /hello.
 */
@RestController
public class HelloController {

    @GetMapping("/hello")
    public String hello() {
        return "Привіт, Spring Boot! Каталог книг вітає вас.";
    }
}
