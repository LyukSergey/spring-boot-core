package com.example._pz11.service;

import com.example._pz11.exception.BookNotFoundException;
import com.example._pz11.model.Book;
import com.example._pz11.repository.BookRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Бін бізнес-логіки. Анотація @Service робить його біном,
 * відповідальним за бізнес-правила та перевірки.
 *
 * Залежність repository оголошена як final — задається один раз і не змінюється.
 * Ми ніде не пишемо new BookRepository. Ми лише оголосили, що сервіс його потребує.
 * Це і є конструкторна ін'єкція залежностей.
 */
@Service
public class BookService {

    private static final Logger log = LoggerFactory.getLogger(BookService.class);

    private final BookRepository repository;

    /**
     * Максимальна кількість книг у каталозі.
     * Береться з application.yaml (ключ library.max-books), дефолт 100.
     */
    @Value("${library.max-books:100}")
    private int maxBooks;

    /**
     * Spring САМ передасть сюди готовий бін BookRepository.
     */
    public BookService(BookRepository repository) {
        this.repository = repository;
        log.info("Створено бін: BookService");
    }

    public List<Book> getAllBooks() {
        return repository.findAll();
    }

    public Book getBook(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new BookNotFoundException(id));
    }

    public Book addBook(Book book) {
        if (repository.findAll().size() >= maxBooks) {
            throw new IllegalStateException("Каталог заповнено: ліміт " + maxBooks + " книг");
        }
        book.setId(null); // очищаємо id, щоб репозиторій згенерував новий
        return repository.save(book);
    }

    public Book borrowBook(Long id) {
        Book book = getBook(id);
        if (!book.isAvailable()) {
            throw new IllegalStateException("Книгу з id=" + id + " вже видано");
        }
        book.setAvailable(false);
        return repository.save(book);
    }

    public Book returnBook(Long id) {
        Book book = getBook(id);
        book.setAvailable(true);
        return repository.save(book);
    }

    public void deleteBook(Long id) {
        boolean deleted = repository.deleteById(id);
        if (!deleted) {
            throw new BookNotFoundException(id);
        }
    }
}
