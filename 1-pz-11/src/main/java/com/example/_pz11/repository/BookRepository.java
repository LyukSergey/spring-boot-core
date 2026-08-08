package com.example._pz11.repository;

import com.example._pz11.model.Book;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Сховище даних. Анотація @Repository робить клас біном для роботи з даними.
 *
 * Тут замість справжньої бази — звичайна Map у пам'яті:
 *   ключ — Long (ідентифікатор книги), значення — об'єкт Book.
 *
 * Для генерації ідентифікаторів використовуємо AtomicLong — потокобезпечний лічильник.
 *
 * Коли з'явиться Spring Data JPA, цей клас замінимо на інтерфейс,
 * але контракт методів лишиться тим самим.
 */
@Repository
public class BookRepository {

    private final Map<Long, Book> storage = new ConcurrentHashMap<>();
    private final AtomicLong sequence = new AtomicLong(0);

    /**
     * Повертає всі книги як список.
     */
    public List<Book> findAll() {
        return new ArrayList<>(storage.values());
    }

    /**
     * Шукає книгу за ідентифікатором.
     * Якщо книги немає — повертає порожній Optional.
     */
    public Optional<Book> findById(Long id) {
        return Optional.ofNullable(storage.get(id));
    }

    /**
     * Зберігає книгу. Якщо у книги немає ідентифікатора —
     * генеруємо новий через incrementAndGet.
     */
    public Book save(Book book) {
        if (book.getId() == null) {
            book.setId(sequence.incrementAndGet());
        }
        storage.put(book.getId(), book);
        return book;
    }

    /**
     * Видаляє книгу з мапи.
     * Повертає true, якщо книга була; false — якщо не знайдено.
     */
    public boolean deleteById(Long id) {
        return storage.remove(id) != null;
    }
}
