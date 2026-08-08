package com.example._pz11.config;

import com.example._pz11.model.Book;
import com.example._pz11.service.BookService;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * Початкове заповнення каталогу.
 *
 * @Component робить клас біном. Клас реалізує CommandLineRunner —
 * спеціальний інтерфейс Spring Boot. Метод run() виконується автоматично
 * один раз після того, як контекст повністю піднявся.
 * Ми не викликаємо run() вручну — це робить контейнер.
 */
@Slf4j
@Component
public class DataInitializer implements CommandLineRunner {

    private final BookService bookService;

    public DataInitializer(BookService bookService) {
        this.bookService = bookService;
    }

    @Override
    public void run(String... args) {
        bookService.addBook(new Book("Java. Повне керівництво", "Герберт Шилдт", 2022));
        bookService.addBook(new Book("Чистий код", "Роберт Мартін", 2008));
        bookService.addBook(new Book("Spring у дії", "Крейг Волс", 2022));
        log.info("Початкові книги додано до каталогу");
    }
}
