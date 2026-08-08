package com.example._pz11.controller;

import com.example._pz11.model.Book;
import com.example._pz11.service.BookService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Веб-шар. @RestController робить клас біном контролера.
 * @RequestMapping("/books") задає спільний префікс для всіх методів.
 *
 * Контролер сам нічого не рахує і не перевіряє — він лише приймає запит
 * і делегує сервісу. Розділення обов'язків:
 *   контролер — про HTTP, сервіс — про бізнес-правила, репозиторій — про дані.
 */
@RestController
@RequestMapping("/books")
public class BookController {

    private static final Logger log = LoggerFactory.getLogger(BookController.class);

    private final BookService service;

    /**
     * Конструкторна ін'єкція — Spring підставить готовий бін BookService.
     */
    public BookController(BookService service) {
        this.service = service;
        log.info("Створено бін: BookController");
    }

    /**
     * GET /books — список усіх книг.
     */
    @GetMapping
    public List<Book> all() {
        return service.getAllBooks();
    }

    /**
     * GET /books/{id} — одна книга за ідентифікатором.
     */
    @GetMapping("/{id}")
    public Book one(@PathVariable Long id) {
        return service.getBook(id);
    }

    /**
     * POST /books — додати нову книгу (JSON у тілі запиту).
     */
    @PostMapping
    public Book add(@RequestBody Book book) {
        return service.addBook(book);
    }

    /**
     * POST /books/{id}/borrow — видати книгу.
     */
    @PostMapping("/{id}/borrow")
    public Book borrow(@PathVariable Long id) {
        return service.borrowBook(id);
    }

    /**
     * POST /books/{id}/return — повернути книгу до каталогу.
     */
    @PostMapping("/{id}/return")
    public Book giveBack(@PathVariable Long id) {
        return service.returnBook(id);
    }

    /**
     * DELETE /books/{id} — видалити книгу.
     */
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        service.deleteBook(id);
    }
}
