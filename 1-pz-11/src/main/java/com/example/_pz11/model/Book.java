package com.example._pz11.model;

/**
 * Модель книги.
 *
 * Це НЕ бін. Це звичайний POJO (Plain Java Object).
 * Spring цей клас не створює і не керує ним — ми самі створюємо об'єкт Book,
 * коли додаємо книгу або коли Spring десеріалізує JSON-тіло запиту.
 */
public class Book {

    private Long id;
    private String title;
    private String author;
    private int year;
    private boolean available = true; // нова книга одразу доступна

    /**
     * Порожній конструктор потрібен для десеріалізації JSON.
     * Без нього Spring не зможе зібрати об'єкт із тіла запиту.
     */
    public Book() {
    }

    /**
     * Конструктор для зручного створення книги вручну.
     */
    public Book(String title, String author, int year) {
        this.title = title;
        this.author = author;
        this.year = year;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }
}
