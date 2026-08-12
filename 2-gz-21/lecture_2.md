# Лекція 2. Spring Boot і робота з базою даних (Spring Data JPA)

> **Тривалість:** 4 години (остання лекція курсу).
> **Після цієї лекції:** 14 годин практики — це все.
> **Мета:** щоб студент після цієї лекції міг самостійно спроєктувати сутності, репозиторії,
> зв'язки між таблицями, транзакції та повний REST+БД застосунок на практиці.

---

## Що ми вже вміємо (з Лекції 1 та ПЗ)

Коротке нагадування, на що спираємось:

- **IoC / DI** — контейнер сам створює об'єкти (біни) і «вкладає» залежності через конструктор.
- **`@Component` / `@Service` / `@Repository`** — стереотипи, які роблять клас біном.
- **`@SpringBootApplication`**, стартери, `pom.xml`, `application.yml`, `@Value`.
- **`@Scheduled` cron**, інтеграція із зовнішнім API (Google Sheets), відправка email (ПЗ-1.2).

Раніше дані «жили» у зовнішній таблиці або в пам'яті. **Сьогодні ми даємо застосунку власну базу даних** — щоб дані переживали перезапуск, шукались, зв'язувались і змінювались транзакційно.

---

## План лекції (4 години)

| Блок | Тема | ~Час |
|------|------|------|
| 1 | Навіщо БД у застосунку. JDBC → JPA → Spring Data. Модель шарів | 25 хв |
| 2 | Перший проєкт: залежності, H2, `application.yml`, автоконфіг | 25 хв |
| 3 | Сутність `@Entity`, `@Id`, генерація ключів, `@Column` | 30 хв |
| 4 | `JpaRepository`, derived queries, `@Query`, пагінація/сортування | 40 хв |
| — | **Перерва** | 10 хв |
| 5 | Шари: Controller → Service → Repository. DTO, валідація, обробка помилок | 40 хв |
| 6 | Зв'язки: `@ManyToOne` / `@OneToMany` / `@ManyToMany`, LAZY vs EAGER, N+1 | 35 хв |
| 7 | Транзакції `@Transactional`: rollback, `readOnly`, проксі й самовиклик, 5 правил | 30 хв |
| 8 | PostgreSQL замість H2 + міграції (Flyway та Liquibase) | 30 хв |
| 9 | `DataSeeder` (наповнення БД) + тести шару даних `@DataJpaTest` | 20 хв |
| 10 | Фінальна вправа (пошук+пагінація, `GET /{id}`) + міст до 14 год практики | 15 хв |

> **Позначки по тексту:**
> **▶ Запусти зараз** — зупиняємось, запускаємо, дивимось реальний вивід (не рухаємось далі, поки не побачили).
> **✍ Міні-вправа** — студент робить сам (5–10 хв), потім розбираємо разом.
> Ці точки — головна відмінність *заняття* від конспекту: код без запуску не закарбовується.

---

## Блок 1. Навіщо БД і як Spring з нею працює

### Проблема

Застосунок без БД втрачає дані при перезапуску. Нам потрібно:
- **зберігати** (create), **читати** (read), **змінювати** (update), **видаляти** (delete) — CRUD;
- **шукати** за умовами (усі студенти групи КН-11);
- гарантувати **цілісність** (або всі зміни застосувались, або жодна — транзакція).

### Три рівні абстракції

```
JDBC            → ти пишеш SQL руками, мапиш ResultSet у об'єкти вручну. Багато boilerplate.
   ↓
JPA (Hibernate) → ти описуєш КЛАСИ, ORM сам генерує SQL і мапить рядки в об'єкти.
   ↓
Spring Data JPA → ти описуєш ІНТЕРФЕЙС репозиторію, Spring сам пише реалізацію.
```

**JPA** (Jakarta Persistence API) — це специфікація ORM (Object-Relational Mapping).
**Hibernate** — найпопулярніша реалізація JPA (її і підключає Spring Boot за замовчуванням).
**Spring Data JPA** — надбудова, що прибирає навіть написання репозиторіїв.

### Шари застосунку (запам'ятати як мантру)

```
   HTTP-запит
       │
       ▼
┌──────────────┐   приймає запит, віддає відповідь (JSON). НЕ містить логіки.
│  Controller  │
└──────┬───────┘
       ▼
┌──────────────┐   бізнес-логіка, транзакції, правила. НЕ знає про HTTP.
│   Service    │
└──────┬───────┘
       ▼
┌──────────────┐   доступ до БД (CRUD, запити). НЕ містить бізнес-логіки.
│  Repository  │
└──────┬───────┘
       ▼
   База даних
```

Кожен шар знає лише про сусіда під собою. Це прямий наслідок DI з Лекції 1: Controller отримує Service через конструктор, Service — Repository.

---

## Блок 2. Перший проєкт з базою даних

### Залежності (`pom.xml`)

На https://start.spring.io/ додаємо: **Spring Web**, **Spring Data JPA**, **H2 Database**, **Validation**.

```xml
<dependencies>
    <!-- REST-контролери -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>

    <!-- JPA + Hibernate + Spring Data -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-jpa</artifactId>
    </dependency>

    <!-- Валідація (@NotNull, @Size...) -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-validation</artifactId>
    </dependency>

    <!-- H2: вбудована БД у пам'яті — ідеальна для навчання і тестів -->
    <dependency>
        <groupId>com.h2database</groupId>
        <artifactId>h2</artifactId>
        <scope>runtime</scope>
    </dependency>

    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-test</artifactId>
        <scope>test</scope>
    </dependency>
</dependencies>
```

### Конфігурація (`src/main/resources/application.yml`)

```yaml
spring:
  datasource:
    url: jdbc:h2:mem:school;DB_CLOSE_DELAY=-1   # in-memory БД "school", живе поки живе застосунок
    driver-class-name: org.h2.Driver
    username: sa
    password: ""
  jpa:
    hibernate:
      ddl-auto: update        # Hibernate САМ створює/оновлює таблиці за @Entity (для навчання; про prod — Блок 8)
    show-sql: true            # друкувати SQL у консоль — БЕЗЦІННО для навчання
    properties:
      hibernate:
        format_sql: true      # форматувати SQL красиво
  h2:
    console:
      enabled: true           # веб-консоль H2 за адресою http://localhost:8080/h2-console
      path: /h2-console
```

> **`ddl-auto`:** `update` зручний для навчання (таблиці з'являються самі). У проді використовують `validate` + окремі міграції (Flyway/Liquibase) — про це в Блоці 8.

**Що сталося магічного:** ми не написали жодного рядка налаштування з'єднання з БД у Java. Spring Boot **автоконфігурація** побачила H2 у classpath і сама створила `DataSource`, `EntityManager`, `TransactionManager`. Це та сама «згортка складності», що й `@SpringBootApplication` з Лекції 1.

Запускаємо, відкриваємо `http://localhost:8080/h2-console`, поле **JDBC URL** = `jdbc:h2:mem:school`, **Connect** — бачимо порожню БД. Далі наповнимо її.

> **▶ Запусти зараз №1.** Запусти застосунок (`mvn spring-boot:run`). У консолі має бути:
> ```
> Tomcat started on port 8080
> HikariPool-1 - Start completed.       ← пул з'єднань до БД піднявся
> Started SchoolApplication in 2.3 seconds
> ```
> Відкрий `http://localhost:8080/h2-console`, введи JDBC URL `jdbc:h2:mem:school`, **Connect**.
> Таблиць ще немає (жодної `@Entity`) — це нормально. **Головне: застосунок бачить БД.**
> Якщо консоль не відкрилась — перевір `spring.h2.console.enabled: true` і що порт 8080 вільний.

---

## Блок 3. Сутність (`@Entity`) — клас, що стає таблицею

Робитимемо наскрізний приклад: **облік студентів** (розвиток теми ПЗ-1.2, але тепер дані у власній БД).

`src/main/java/com/example/school/student/Student.java`:

```java
package com.example.school.student;

import jakarta.persistence.*;

/**
 * Сутність "Студент" — цей клас Hibernate відобразить у таблицю STUDENTS.
 * Кожне поле → колонка; кожен об'єкт → рядок.
 */
@Entity
@Table(name = "students")
public class Student {

    @Id                                                  // первинний ключ
    @GeneratedValue(strategy = GenerationType.IDENTITY)  // БД сама видає id (auto-increment)
    private Long id;

    @Column(nullable = false, length = 100)
    private String firstName;

    @Column(nullable = false, length = 100)
    private String lastName;

    @Column(name = "group_name", nullable = false, length = 20)
    private String groupName;

    @Column(unique = true)
    private String email;

    // JPA ВИМАГАЄ конструктор без аргументів (Hibernate створює об'єкт через нього).
    protected Student() { }

    public Student(String firstName, String lastName, String groupName, String email) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.groupName = groupName;
        this.email = email;
    }

    // геттери/сеттери
    public Long getId() { return id; }
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public String getGroupName() { return groupName; }
    public void setGroupName(String groupName) { this.groupName = groupName; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}
```

**Ключові анотації:**

| Анотація | Що робить |
|----------|-----------|
| `@Entity` | клас = таблиця, керована JPA |
| `@Table(name=...)` | ім'я таблиці (без неї — ім'я класу) |
| `@Id` | поле = первинний ключ |
| `@GeneratedValue` | хто генерує ключ (`IDENTITY` — БД, `SEQUENCE` — послідовність) |
| `@Column` | параметри колонки (`nullable`, `length`, `unique`, `name`) |

> **Пастка:** конструктор без аргументів обов'язковий. Тому в `Student` є `protected Student() {}` — Hibernate ним створює порожній об'єкт, а потім заповнює поля.

> **▶ Запусти зараз №2.** Перезапусти застосунок і подивись у консоль — завдяки `show-sql`
> Hibernate надрукує згенерований DDL:
> ```sql
> Hibernate:
>     create table students (
>         id bigint generated by default as identity,
>         email varchar(255) unique,
>         first_name varchar(100) not null,
>         group_name varchar(20) not null,
>         last_name varchar(100) not null,
>         primary key (id)
>     )
> ```
> **Це і є суть JPA:** ти написав Java-клас — Hibernate написав SQL. Онови H2-консоль (`F5`) —
> таблиця `STUDENTS` тепер є. Зверни увагу: `firstName` → колонка `first_name` (Spring сам
> перетворює camelCase у snake_case).

> **✍ Міні-вправа №1 (5 хв).** Додай у `Student` поле `enrollmentYear` (рік вступу, `int`).
> Перезапусти, знайди в консолі оновлений `create table` — переконайся, що з'явилась колонка
> `enrollment_year`. *Питання для обговорення:* чому для `int` немає `not null` за замовчуванням,
> а якби зробили `Integer` — була б різниця?

---

## Блок 4. Репозиторій — інтерфейс, який Spring реалізує сам

Це магія Spring Data. Ми **пишемо лише інтерфейс** — Spring під час старту генерує реалізацію з усіма CRUD-методами.

`src/main/java/com/example/school/student/StudentRepository.java`:

```java
package com.example.school.student;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

/**
 * Успадкувавши JpaRepository<Student, Long>, ми БЕЗКОШТОВНО отримуємо:
 *   save(s), findById(id), findAll(), deleteById(id), count(), existsById(id)...
 * А методи нижче Spring реалізує САМ, читаючи їхню НАЗВУ (derived queries).
 */
public interface StudentRepository extends JpaRepository<Student, Long> {

    // SELECT * FROM students WHERE group_name = ?
    List<Student> findByGroupName(String groupName);

    // SELECT * FROM students WHERE last_name LIKE %?% (ignore case)
    List<Student> findByLastNameContainingIgnoreCase(String part);

    // SELECT COUNT(*) FROM students WHERE group_name = ?
    long countByGroupName(String groupName);

    boolean existsByEmail(String email);
}
```

### Derived queries — Spring читає назву методу

Spring розбирає ім'я методу на ключові слова й будує SQL:

| Метод | Згенерований SQL (спрощено) |
|-------|------------------------------|
| `findByGroupName` | `WHERE group_name = ?` |
| `findByGroupNameAndEmail` | `WHERE group_name = ? AND email = ?` |
| `findByLastNameContaining` | `WHERE last_name LIKE %?%` |
| `findByGroupNameOrderByLastNameAsc` | `WHERE group_name = ? ORDER BY last_name ASC` |
| `countByGroupName` | `SELECT COUNT(*) ... WHERE group_name = ?` |
| `existsByEmail` | чи є рядок з таким email |

### Коли назви замало — `@Query`

```java
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface StudentRepository extends JpaRepository<Student, Long> {

    // JPQL — працює над СУТНОСТЯМИ (Student), не над таблицями. s.groupName — це ПОЛЕ.
    @Query("SELECT s FROM Student s WHERE s.groupName = :g ORDER BY s.lastName")
    List<Student> studentsOfGroup(@Param("g") String group);

    // Нативний SQL — коли треба щось специфічне для конкретної БД
    @Query(value = "SELECT * FROM students WHERE email LIKE %:domain", nativeQuery = true)
    List<Student> byEmailDomain(@Param("domain") String domain);
}
```

### Пагінація і сортування

Коли рядків тисячі — не тягнемо все одразу:

```java
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

Page<Student> findByGroupName(String groupName, Pageable pageable);
```

```java
// у сервісі:
Pageable p = PageRequest.of(0, 20, Sort.by("lastName").ascending()); // сторінка 0, по 20
Page<Student> page = repo.findByGroupName("КН-11", p);
page.getContent();       // самі студенти
page.getTotalElements(); // скільки всього
page.getTotalPages();
```

> **✍ Міні-вправа №2 (7 хв).** Додай у `StudentRepository` метод, який знаходить студентів
> групи, впорядкованих за прізвищем:
> ```java
> List<Student> findByGroupNameOrderByLastNameAsc(String groupName);
> ```
> А потім — спробуй *навмисне помилитись* у назві: напиши `findByGrupName` (одруківка).
> **Запусти.** Застосунок **впаде на старті** з `PropertyReferenceException: No property 'grupName' found`.
> *Висновок для студентів:* Spring Data перевіряє назви методів під час старту, а не в рантаймі —
> одруківка в derived query = впав одразу, а не «мовчки не працює».

---

## Блок 5. Шари: Controller → Service → Repository + DTO, валідація й помилки

### Чому DTO, а не віддавати сутність напряму

**DTO** (Data Transfer Object) — окремий об'єкт для передачі через API. Причини:
- сутність `Student` може мати поля, які **не можна віддавати** назовні (напр. внутрішні прапорці);
- на **вхід** нам не потрібен `id` (його видає БД), а потрібна лише «форма запиту»;
- контракт API не має ламатись через зміну структури таблиці.

`src/main/java/com/example/school/student/dto/StudentDto.java`:

```java
package com.example.school.student.dto;

// Що ВІДДАЄМО клієнту (у відповіді).
public record StudentDto(Long id, String firstName, String lastName, String groupName, String email) { }
```

`src/main/java/com/example/school/student/dto/CreateStudentRequest.java`:

```java
package com.example.school.student.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

// Що ПРИЙМАЄМО від клієнта (у запиті на створення). Без id — його дасть БД.
public record CreateStudentRequest(
        @NotBlank String firstName,
        @NotBlank String lastName,
        @NotBlank String groupName,
        @Email String email
) { }
```

### Сервіс — бізнес-логіка

`src/main/java/com/example/school/student/StudentService.java`:

```java
package com.example.school.student;

import com.example.school.student.dto.CreateStudentRequest;
import com.example.school.student.dto.StudentDto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class StudentService {

    private final StudentRepository repo;

    // Конструкторна ін'єкція — той самий DI, що й у Лекції 1.
    public StudentService(StudentRepository repo) {
        this.repo = repo;
    }

    @Transactional(readOnly = true)   // читання — транзакція лише для читання (оптимізація)
    public List<StudentDto> byGroup(String group) {
        return repo.findByGroupName(group).stream()
                .map(StudentService::toDto)
                .toList();
    }

    @Transactional   // запис → повноцінна транзакція (про це Блок 7)
    public StudentDto create(CreateStudentRequest req) {
        if (repo.existsByEmail(req.email())) {
            throw new IllegalStateException("Студент з таким email вже існує");
        }
        Student saved = repo.save(
                new Student(req.firstName(), req.lastName(), req.groupName(), req.email()));
        return toDto(saved);
    }

    @Transactional
    public void delete(Long id) {
        if (!repo.existsById(id)) {
            throw new StudentNotFoundException(id);
        }
        repo.deleteById(id);
    }

    // Мапінг сутність → DTO (в одному місці, щоб не дублювати).
    static StudentDto toDto(Student s) {
        return new StudentDto(s.getId(), s.getFirstName(), s.getLastName(), s.getGroupName(), s.getEmail());
    }
}
```

### Контролер — тонкий шар над HTTP

`src/main/java/com/example/school/student/StudentController.java`:

```java
package com.example.school.student;

import com.example.school.student.dto.CreateStudentRequest;
import com.example.school.student.dto.StudentDto;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/students")   // усі шляхи починаються з /students
public class StudentController {

    private final StudentService service;

    public StudentController(StudentService service) {
        this.service = service;
    }

    // GET /students?group=КН-11
    @GetMapping
    public List<StudentDto> list(@RequestParam String group) {
        return service.byGroup(group);
    }

    // POST /students  {json}
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)     // 201, а не 200
    public StudentDto create(@Valid @RequestBody CreateStudentRequest req) {
        return service.create(req);         // @Valid запускає валідацію DTO
    }

    // DELETE /students/5
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)  // 204
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}
```

Зверни увагу: **контролер не знає про БД, сервіс не знає про HTTP**. Кожен робить своє.

### ▶ Запусти зараз №3 — перший живий виклик API

Тепер у нас є повний ланцюг. Перезапусти застосунок і смикни його через `curl` (або Postman).

**1) Створюємо студента (POST):**

```bash
curl -i -X POST http://localhost:8080/students \
  -H "Content-Type: application/json" \
  -d '{"firstName":"Іван","lastName":"Іваненко","groupName":"КН-11","email":"ivan@ex.com"}'
```

Очікувана відповідь — **201 Created** і JSON з присвоєним `id`:

```
HTTP/1.1 201
Content-Type: application/json

{"id":1,"firstName":"Іван","lastName":"Іваненко","groupName":"КН-11","email":"ivan@ex.com"}
```

А в **консолі** застосунку одночасно:

```sql
Hibernate:
    insert into students (email, first_name, group_name, last_name) values (?, ?, ?, ?)
```

> **Це кульмінаційний момент лекції:** один HTTP-запит пройшов усі три шари —
> Controller прийняв JSON → Service застосував логіку в транзакції → Repository зробив INSERT →
> Hibernate згенерував SQL → рядок ліг у БД. Покажи це повільно.

**2) Читаємо студентів групи (GET):**

```bash
curl "http://localhost:8080/students?group=КН-11"
```

```json
[{"id":1,"firstName":"Іван","lastName":"Іваненко","groupName":"КН-11","email":"ivan@ex.com"}]
```

**3) Видаляємо (DELETE):**

```bash
curl -i -X DELETE http://localhost:8080/students/1
```

```
HTTP/1.1 204 No Content
```

Онови H2-консоль (`SELECT * FROM students`) — переконайся, що рядок зник.

> **💡 Щоб було що читати:** аби не створювати студентів руками щоразу, зручно додати `DataSeeder`,
> який наповнює БД при старті. Повний код і чому він **лише для dev** — у **Блоці 9**.

### Валідація на вході

Ми вже поставили `@NotBlank`, `@Email` у `CreateStudentRequest` і `@Valid` у контролері.
Якщо клієнт надішле порожнє `firstName` — Spring **сам** поверне 400 ще до входу в сервіс.

### Власний виняток

`src/main/java/com/example/school/student/StudentNotFoundException.java`:

```java
package com.example.school.student;

public class StudentNotFoundException extends RuntimeException {
    public StudentNotFoundException(Long id) {
        super("Студента з id=" + id + " не знайдено");
    }
}
```

### Централізована обробка — `@RestControllerAdvice`

Замість `try/catch` у кожному контролері — одне місце, що перетворює винятки на HTTP-відповіді:

`src/main/java/com/example/school/error/GlobalExceptionHandler.java`:

```java
package com.example.school.error;

import com.example.school.student.StudentNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice   // «слухає» винятки з УСІХ контролерів
public class GlobalExceptionHandler {

    // 404 — не знайдено
    @ExceptionHandler(StudentNotFoundException.class)
    public ResponseEntity<Map<String, String>> notFound(StudentNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", e.getMessage()));
    }

    // 409 — конфлікт (дублікат email)
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, String>> conflict(IllegalStateException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("error", e.getMessage()));
    }

    // 400 — помилки валідації @Valid: збираємо всі поля з поясненнями
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> validation(MethodArgumentNotValidException e) {
        Map<String, String> errors = e.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(f -> f.getField(), f -> f.getDefaultMessage(), (a, b) -> a));
        return ResponseEntity.badRequest().body(errors);
    }
}
```

Тепер API повертає **зрозумілі помилки з правильними кодами**, а не 500 із stack trace.

> **▶ Запусти зараз №4 — перевіряємо, що помилки красиві.** Перезапусти й спробуй «зламати» API:
>
> **Погані дані (порожнє ім'я, кривий email) → 400:**
> ```bash
> curl -i -X POST http://localhost:8080/students \
>   -H "Content-Type: application/json" \
>   -d '{"firstName":"","lastName":"Тест","groupName":"КН-11","email":"не-email"}'
> ```
> ```
> HTTP/1.1 400 Bad Request
> {"firstName":"must not be blank","email":"must be a well-formed email address"}
> ```
>
> **Видалення неіснуючого → 404:**
> ```bash
> curl -i -X DELETE http://localhost:8080/students/999
> ```
> ```
> HTTP/1.1 404 Not Found
> {"error":"Студента з id=999 не знайдено"}
> ```
>
> **Дублікат email → 409:** створи двічі того самого — другий раз отримаєш
> `409 Conflict` з `{"error":"Студент з таким email вже існує"}`.
>
> *Ключова думка:* жодного 500 і stack trace — клієнт бачить **зрозумілу помилку з правильним кодом**.

> **✍ Міні-вправа №3 (8 хв).** Перевір усі три коди помилок «руками». Надішли `curl`-ом:
> **(а)** порожнє `firstName` і кривий `email` → маєш отримати **400** з переліком полів;
> **(б)** `DELETE /students/999` (неіснуючий) → **404**;
> **(в)** двічі створи студента з тим самим `email` → другий раз **409 Conflict**.
> *Питання для обговорення:* чому `@RestControllerAdvice` кращий за `try/catch` у кожному контролері?
> *(Ендпоінт `GET /students/{id}` зберемо у фінальному блоці — Міні-вправа №4.)*

---

## Блок 6. Зв'язки між сутностями

Реальні дані пов'язані: студент належить групі, у групи багато студентів, студент має багато оцінок. Змоделюймо це.

### `@ManyToOne` / `@OneToMany` — багато студентів в одній групі

`Group.java`:

```java
@Entity
@Table(name = "groups")
public class Group {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;   // "КН-11"

    // ОДНА група → БАГАТО студентів. mappedBy вказує, що "власник" зв'язку — поле group у Student.
    @OneToMany(mappedBy = "group", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Student> students = new ArrayList<>();

    protected Group() { }
    public Group(String name) { this.name = name; }
    // геттери/сеттери...
}
```

`Student.java` (додаємо зв'язок замість рядка `groupName`):

```java
@Entity
@Table(name = "students")
public class Student {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String firstName;
    private String lastName;

    // БАГАТО студентів → ОДНА група. Тут ФІЗИЧНО живе зовнішній ключ group_id.
    @ManyToOne(fetch = FetchType.LAZY)          // LAZY: групу тягнемо лише коли реально звертаємось
    @JoinColumn(name = "group_id")
    private Group group;

    // ...
}
```

> **Хто «власник» зв'язку:** сторона з `@JoinColumn` (тут `Student`) тримає зовнішній ключ.
> `mappedBy` на іншій стороні (`Group`) каже: «я лише дзеркало, ключа в мене немає».

### LAZY vs EAGER і проблема N+1

- **LAZY** (лінива загрузка) — пов'язана сутність вантажиться тільки при зверненні (`student.getGroup()`). За замовчуванням для `@ManyToOne` варто ставити **LAZY**.
- **EAGER** — вантажиться одразу разом із батьківською. Здається зручним, але призводить до зайвих запитів.

**Проблема N+1** — класична пастка ORM:

```java
List<Student> all = repo.findAll();        // 1 запит: SELECT * FROM students
for (Student s : all) {
    System.out.println(s.getGroup().getName());  // + по 1 запиту НА КОЖНОГО студента!
}
// Разом: 1 + N запитів → тормозить на великих даних.
```

**Рішення — `JOIN FETCH`** (тягнемо все одним запитом):

```java
@Query("SELECT s FROM Student s JOIN FETCH s.group")
List<Student> findAllWithGroup();   // 1 запит із JOIN замість 1+N
```

> **▶ Запусти зараз №5 — побач N+1 на власні очі.** Це найкраще демонструвати через `show-sql`.
> Виклич `findAll()` і пройдись по `getGroup().getName()` у циклі — у консолі побачиш:
> ```sql
> Hibernate: select ... from students          ← 1 запит
> Hibernate: select ... from groups where id=? ← +1
> Hibernate: select ... from groups where id=? ← +1
> Hibernate: select ... from groups where id=? ← +1   (по одному НА КОЖНОГО студента!)
> ```
> Тепер заміни виклик на `findAllWithGroup()` — і в консолі **один рядок** із `join`.
> *Мораль:* кількість SQL-запитів не видно з Java-коду — її видно **тільки в консолі**.
> Тому `show-sql: true` під час навчання і розробки — обов'язковий.

### `@ManyToMany` — студенти й курси

```java
@Entity
public class Course {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String title;

    @ManyToMany(mappedBy = "courses")
    private Set<Student> students = new HashSet<>();
}

@Entity
public class Student {
    // ...
    @ManyToMany
    @JoinTable(                                  // проміжна таблиця student_course
        name = "student_course",
        joinColumns = @JoinColumn(name = "student_id"),
        inverseJoinColumns = @JoinColumn(name = "course_id"))
    private Set<Course> courses = new HashSet<>();
}
```

Spring/Hibernate **сам створить проміжну таблицю** `student_course` з двома зовнішніми ключами.

> **Порада для практики:** починайте зі зв'язків `@ManyToOne` (найпростіші й найчастіші).
> `@ManyToMany` беріть лише коли справді потрібна проміжна таблиця.

---

## Блок 7. Транзакції `@Transactional`

**Транзакція** — група операцій, що виконуються «все або нічого». Якщо посеред запису стається помилка — усі зміни **відкочуються** (rollback), БД лишається в узгодженому стані. `@Transactional` ставлять **на рівні сервісу** (клас або окремий метод) — це природна межа бізнес-операції.

### Приклад, де це критично

```java
@Transactional
public void transferStudent(Long studentId, String newGroup) {
    Student s = repo.findById(studentId).orElseThrow();
    s.setGroupName(newGroup);           // зміна 1
    auditRepo.save(new Audit("moved")); // зміна 2
    if (somethingWrong) {
        throw new RuntimeException();   // ← ОБИДВІ зміни відкотяться, БД чиста
    }
}
```

Дві зміни бази йдуть в одній транзакції: змінили групу студента **і** зберегли аудит. Якщо після цього кинути `RuntimeException` — обидві зміни не застосуються, база лишається в узгодженому стані.

### Пастка №1 — самовиклик (`this.method()`)

Найпоширеніша пастка. `@Transactional` працює через **проксі** Spring. Виклик анотованого методу **з того самого класу** (`this.doWork()`) йде повз проксі — транзакція **не відкривається**.

`OrderService.java`:

```java
// ❌ Самовиклик — транзакція НЕ відкриється
@Service
public class OrderService {

    public void outer() {
        this.doWork();          // йде повз проксі Spring
    }

    @Transactional
    public void doWork() {
        // транзакція не активна — self-invocation
    }
}

// ✅ Правильно — транзакційний метод у окремому біні
@Service
@RequiredArgsConstructor
public class OrderProcessor {

    @Transactional
    public void doWork() {
        // тут транзакція активна: виклик прийшов ЗЗОВНІ, через проксі
    }
}

@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderProcessor processor;   // інший бін → інший проксі

    public void outer() {
        processor.doWork();     // виклик через проксі → транзакція відкривається
    }
}
```

**Виправлення:** винести транзакційний метод в окремий бін і викликати його ззовні (через ін'єктований бін), а не через `this`.

### Що Spring НАСПРАВДІ кладе в контейнер (проксі)

Spring не кладе у контейнер ваш оригінальний `OrderProcessor`. Під час старту він **генерує підклас** (CGLIB-проксі), який перехоплює виклик, відкриває транзакцію, викликає ваш код і робить commit або rollback:

```java
// Спрощена модель згенерованого проксі-класу (CGLIB-підклас).
class OrderProcessor$$SpringCGLIB extends OrderProcessor {
    private final TransactionManager txManager;
    private final OrderProcessor target;   // ваш справжній об'єкт

    @Override
    public void doWork() {
        TransactionStatus tx = txManager.getTransaction(...);   // 1. відкрити транзакцію
        try {
            target.doWork();                                    // 2. викликати ваш код
            txManager.commit(tx);                               // 3a. успіх → commit
        } catch (RuntimeException e) {
            txManager.rollback(tx);                             // 3b. RuntimeException → rollback
            throw e;
        }
    }
}
```

Так Spring додає транзакційну логіку **без зміни вашого коду**. Виклик ззовні завжди йде через проксі; виклик через `this` проксі не бачить — тому й транзакція не відкривається.

### 5 правил `@Transactional` (з прикладами)

| Правило | ✅ Правильно | ❌ Неправильно |
|---------|-------------|----------------|
| **Відкат лише на `RuntimeException`** | `@Transactional void save() { throw new RuntimeException(); }` | `@Transactional void save() throws Exception { throw new Exception(); }` (checked → rollback НЕ буде без `rollbackFor`) |
| **`readOnly` для читання** | `@Transactional(readOnly = true) List<StudentDto> list() { ... }` | `@Transactional List<StudentDto> list() { ... }` (зайвий overhead на читанні) |
| **Не самовиклик** | викликати з іншого біна: `service.doWork();` | `public void outer() { this.doWork(); }` |
| **Не ігнорувати виняток** | дати винятку вийти назовні | `try { ... } catch (Exception e) { log.error(e); }` — проковтнутий виняток = commit |
| **Не приватний метод** | `@Transactional public void doWork() { ... }` | `@Transactional private void doWork() { ... }` (і так само не працює на `@PostConstruct`) |

> **`readOnly = true`** — підказка Hibernate: не робити знімків завантажених сутностей і не порівнювати їх наприкінці транзакції. Невелика, але безкоштовна оптимізація для методів лише-читання.

---

## Блок 8. Від H2 до PostgreSQL + міграції

### Перехід з H2 на PostgreSQL — кроки

1. Додати драйвер PostgreSQL у `pom.xml`.
2. Оновити параметри з'єднання у `application.yml` (адреса, `username`, `password`).
3. Змінити `ddl-auto` на `validate` (Hibernate лише перевіряє схему).
4. Розгорнути PostgreSQL локально через `docker-compose`.
5. Запустити застосунок і перевірити з'єднання з базою.

`pom.xml`:

```xml
<dependency>
    <groupId>org.postgresql</groupId>
    <artifactId>postgresql</artifactId>
    <scope>runtime</scope>
</dependency>
```

`application.yml`:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/school
    username: postgres
    password: postgres
  jpa:
    hibernate:
      ddl-auto: validate       # у проді Hibernate НЕ змінює схему, лише перевіряє
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
```

Підняти Postgres локально одним рядком (`docker-compose.yml`):

```yaml
services:
  db:
    image: postgres:16
    environment:
      POSTGRES_DB: school
      POSTGRES_PASSWORD: postgres
    ports:
      - "5432:5432"
```

### Чому `ddl-auto: update` не для прода

`update` зручний для навчання (таблиці з'являються самі), але у проді **небезпечний**:
- не вміє видаляти/перейменовувати колонки;
- може **мовчки** не застосувати зміну.

У проді ставлять `ddl-auto: validate`, а схему змінюють **міграціями** — версійованими файлами, де кожна зміна = окремий пронумерований файл, що виконується рівно один раз. Два найпопулярніші інструменти — **Flyway** і **Liquibase**.

### Міграції через Flyway

Flyway використовує звичайні SQL-файли з префіксом версії. Файл `V1__init.sql` виконається рівно один раз при першому старті:

`src/main/resources/db/migration/V1__init.sql`:

```sql
-- Flyway виконає цей файл один раз при першому старті
CREATE TABLE students (
    id          BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    first_name  VARCHAR(100) NOT NULL,
    last_name   VARCHAR(100) NOT NULL,
    group_name  VARCHAR(20)  NOT NULL,
    email       VARCHAR(255) UNIQUE
);
```

**Структура Flyway** (папка `src/main/resources/db/migration/`):

```
src/main/resources/db/migration/
├── V1__init.sql                  ← початкова схема
└── V2__add_enrollment_year.sql   ← наступні зміни
```

Flyway виконує файли **по порядку версій**, кожен — лише один раз. Історію виконаних міграцій зберігає у службовій таблиці `flyway_schema_history`.

### Міграції через Liquibase (XML)

Той самий підхід, але через XML (або YAML/JSON). Потрібні головний changelog і файли з окремими змінами.

Головний файл — точка входу, що підключає інші:

`src/main/resources/db/changelog/db.changelog-master.xml`:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<databaseChangeLog
    xmlns="http://www.liquibase.org/xml/ns/dbchangelog"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xsi:schemaLocation="http://www.liquibase.org/xml/ns/dbchangelog
        http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-4.20.xsd">

    <include file="db/changelog/changes/001-create-students.xml"/>
</databaseChangeLog>
```

Конкретна міграція. Тег `<changeSet>` має `id` та `author` — Liquibase зберігає ці дані у службовій таблиці й **ніколи не виконує той самий changeSet двічі**:

`src/main/resources/db/changelog/changes/001-create-students.xml`:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<databaseChangeLog
    xmlns="http://www.liquibase.org/xml/ns/dbchangelog"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xsi:schemaLocation="http://www.liquibase.org/xml/ns/dbchangelog
        http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-4.20.xsd">

    <changeSet id="001" author="ivan">
        <createTable tableName="students">
            <column name="id" type="BIGINT" autoIncrement="true">
                <constraints primaryKey="true"/>
            </column>
            <column name="first_name" type="VARCHAR(100)">
                <constraints nullable="false"/>
            </column>
            <column name="last_name" type="VARCHAR(100)">
                <constraints nullable="false"/>
            </column>
            <column name="group_name" type="VARCHAR(20)">
                <constraints nullable="false"/>
            </column>
            <column name="email" type="VARCHAR(255)">
                <constraints unique="true"/>
            </column>
        </createTable>
    </changeSet>
</databaseChangeLog>
```

**Структура Liquibase** (папка `src/main/resources/db/changelog/`):

```
src/main/resources/db/changelog/
├── db.changelog-master.xml              ← головний файл, підключає інші
└── changes/
    ├── 001-create-students.xml          ← перший changeSet
    └── 002-add-enrollment-year.xml      ← наступна зміна
```

Liquibase фіксує виконані changeSets у службовій таблиці `DATABASECHANGELOG`; нові changeSets застосовуються автоматично при старті застосунку.

### Flyway vs Liquibase — коротке порівняння

| Критерій | Flyway | Liquibase |
|----------|--------|-----------|
| Формат файлів | SQL (або Java) | XML, YAML, JSON, SQL |
| Іменування | `V1__назва.sql` (префікс версії) | `<changeSet>` з `id` та `author` |
| Rollback | не підтримує автоматично | підтримує через тег `rollback` |
| Службова таблиця | `flyway_schema_history` | `DATABASECHANGELOG` |
| Складність старту | дуже просто (краще для початківців) | потребує більше налаштувань, гнучкіший для складних проєктів |

> **Порада:** для навчання і невеликих проєктів беріть **Flyway** — простий SQL, мінімум церемоній. **Liquibase** обирайте, коли потрібні rollback, кілька СУБД і складніші сценарії.

---

## Блок 9. `DataSeeder` + тести шару даних

### `DataSeeder` — авто-наповнення БД на старті

Щоразу після перезапуску H2 (in-memory) втрачає дані. Щоб не вводити студентів вручну кожного разу, додаємо клас `DataSeeder`, що реалізує `CommandLineRunner`. Spring викликає метод `run` **автоматично після старту** застосунку. Перевірка кількості записів захищає від дублів при повторному старті.

`src/main/java/com/example/school/DataSeeder.java`:

```java
package com.example.school;

import com.example.school.student.Student;
import com.example.school.student.StudentRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component                                          // Spring робить клас біном
public class DataSeeder implements CommandLineRunner {

    private final StudentRepository repo;
    public DataSeeder(StudentRepository repo) { this.repo = repo; }

    @Override
    public void run(String... args) {
        if (repo.count() > 0) return;               // вже є дані → не дублювати
        repo.save(new Student("Іван",  "Іваненко",  "КН-11", "ivan@ex.com"));
        repo.save(new Student("Петро", "Петренко",  "КН-11", "petro@ex.com"));
        repo.save(new Student("Сидір", "Сидоренко", "КН-12", "sydir@ex.com"));
    }
}
```

> **💡 `DataSeeder` — лише для dev.** Підходить для навчального або dev-середовища. У проді
> початкові дані вносять **міграціями** (Flyway / Liquibase), а не через `CommandLineRunner`.

### Тест шару даних — `@DataJpaTest`

Анотація `@DataJpaTest` піднімає **лише JPA-шар** (без web-контексту), автоматично підключає in-memory БД і **відкочує зміни після кожного тесту** — тести швидкі й ізольовані. Репозиторій впроваджуємо через `@Autowired`.

`src/test/java/com/example/school/student/StudentRepositoryTest.java`:

```java
package com.example.school.student;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest   // піднімає ЛИШЕ JPA-шар + in-memory БД, швидко
class StudentRepositoryTest {

    @Autowired
    StudentRepository repo;

    @Test
    void findsStudentsByGroup() {
        repo.save(new Student("Іван", "Іваненко", "КН-11", "ivan@ex.com"));
        repo.save(new Student("Петро", "Петренко", "КН-11", "petro@ex.com"));
        repo.save(new Student("Сидір", "Сидоренко", "КН-12", "sydir@ex.com"));

        List<Student> kn11 = repo.findByGroupName("КН-11");

        assertThat(kn11).hasSize(2);
        assertThat(repo.countByGroupName("КН-11")).isEqualTo(2);
        assertThat(repo.existsByEmail("ivan@ex.com")).isTrue();
    }
}
```

---

## Блок 10. Фінальна вправа + підсумок і міст до практики

### Фінальна вправа — пошук студентів із пагінацією

Ця вправа збирає **весь ланцюг** докупи: репозиторій → сервіс → контролер → перевірка. Додаємо ендпоінт `GET /students/search?lastName=Ів` — пошук за частиною прізвища (без урахування регістру) з пагінацією.

**Крок 1 — метод репозиторію** (Spring Data будує запит за назвою; `Pageable` дає номер сторінки, розмір і сортування):

```java
public interface StudentRepository extends JpaRepository<Student, Long> {
    // ... існуючі методи
    Page<Student> findByLastNameContainingIgnoreCase(String lastName, Pageable pageable);
}
```

**Крок 2 — метод сервісу** (`readOnly`-транзакція; формуємо `PageRequest` із сортуванням, мапимо кожну сутність у DTO):

```java
@Transactional(readOnly = true)
public Page<StudentDto> search(String lastName, int page, int size) {
    Pageable pageable = PageRequest.of(page, size, Sort.by("lastName").ascending());
    return repo.findByLastNameContainingIgnoreCase(lastName, pageable)
               .map(StudentService::toDto);
}
```

**Крок 3 — ендпоінт контролера** (`page`/`size` мають значення за замовчуванням):

```java
// GET /students/search?lastName=Ів&page=0&size=20
@GetMapping("/search")
public Page<StudentDto> search(
        @RequestParam String lastName,
        @RequestParam(defaultValue = "0")  int page,
        @RequestParam(defaultValue = "20") int size) {
    return service.search(lastName, page, size);
}
```

**Крок 4 — перевірка `curl`-ом** і читання згенерованого SQL у консолі:

```bash
curl "http://localhost:8080/students/search?lastName=ів&page=0&size=20"
```

```json
{
  "content": [
    {"id":1,"firstName":"Іван","lastName":"Іваненко","groupName":"КН-11","email":"ivan@ex.com"}
  ],
  "totalElements": 1,
  "totalPages": 1,
  "number": 0,
  "size": 20
}
```

Відповідь містить масив `content` зі знайденими студентами плюс метадані пагінації (`totalElements`, `totalPages`).

> **✍ Міні-вправа №4 — `GET /students/{id}` (один студент за id).**
> **1.** У сервісі — пошук за id, кинути `StudentNotFoundException`, якщо не знайдено:
> ```java
> @Transactional(readOnly = true)
> public StudentDto getById(Long id) {
>     return repo.findById(id)
>                .map(StudentService::toDto)
>                .orElseThrow(() -> new StudentNotFoundException(id));
> }
> ```
> **2.** У контролері — ендпоінт із `@PathVariable`:
> ```java
> // GET /students/5
> @GetMapping("/{id}")
> public StudentDto getOne(@PathVariable Long id) {
>     return service.getById(id);
> }
> ```
> **3.** Перевір `curl`-ом обидва сценарії:
> - `GET /students/1` → **200** + JSON студента;
> - `GET /students/999` → **404** + `{"error": "..."}` (через `@RestControllerAdvice` з Блоку 5).

### Підсумок — що ми зібрали за лекцію

Повний вертикальний зріз застосунку з БД:

```
HTTP → StudentController → StudentService (@Transactional) → StudentRepository → H2/PostgreSQL
        (DTO, @Valid)        (бізнес-логіка)                  (Spring Data)
                    ↑
        GlobalExceptionHandler (404/409/400)
```

### Головні ідеї, які треба винести

1. **Сутність = таблиця** (`@Entity`), об'єкт = рядок.
2. **Репозиторій — це інтерфейс**; Spring пише реалізацію сам (derived queries + `@Query`).
3. **Три шари**: Controller (HTTP) → Service (логіка+транзакції) → Repository (БД). Не змішувати.
4. **DTO ≠ Entity** — не віддавай сутність напряму назовні.
5. **Зв'язки**: `@ManyToOne` (з `@JoinColumn`) — найчастіший; стеж за **LAZY** і **N+1**.
6. **`@Transactional`** на сервісі; rollback лише на `RuntimeException`, працює через проксі (тому самовиклик `this.method()` не відкриває транзакцію).
7. Помилки — через `@RestControllerAdvice`, а не 500.
8. **Міграції** (Flyway / Liquibase) у проді замість `ddl-auto: update`.

### Чек-лист «я готовий до практики»

- [ ] Можу з нуля створити проєкт із `spring-boot-starter-data-jpa` + H2.
- [ ] Напишу `@Entity` з `@Id`, `@GeneratedValue`, `@Column`.
- [ ] Оголошу `JpaRepository` і додам derived-query метод.
- [ ] Побудую ланцюг Controller → Service → Repository з DTO.
- [ ] Додам `@Valid` і `@RestControllerAdvice`.
- [ ] Зроблю зв'язок `@ManyToOne`/`@OneToMany`.
- [ ] Поставлю `@Transactional` там, де потрібно, і поясню чому.
- [ ] Напишу `@DataJpaTest`.
- [ ] Запущу застосунок і перевірю кожен ендпоінт через `curl` (200/201/204/400/404/409).
- [ ] Читаю SQL у консолі (`show-sql`) і впізнаю N+1.
- [ ] Розумію, коли потрібні міграції (Flyway / Liquibase) замість `ddl-auto`.

### Що спробувати на 14 годинах практики (ідеї проєкту)

Розширити наскрізний приклад «Школа/Студенти» у повноцінний застосунок:

1. **CRUD студентів і груп** через REST + БД (база).
2. **Зв'язки**: студент ↔ група (`@ManyToOne`), студент ↔ курси (`@ManyToMany`).
3. **Пошук і пагінація** студентів за групою/прізвищем.
4. **Валідація + обробка помилок** на всіх ендпоінтах.
5. **Облік відвідуваності** (розвиток ПЗ-1.2): сутність `Attendance` зі зв'язком на `Student` і датою — тепер дані з бази, а не з Google Sheet.
6. **Звіт** (з ПЗ-1.2) — рахувати відсутніх запитом до БД, а не парсингом таблиці.
7. **Тести** репозиторіїв (`@DataJpaTest`) і сервісів.
8. **(на відмінно)** PostgreSQL у Docker + міграції Flyway.

> Логічний місток: у ПЗ-1.2 дані були у Google Sheet і рахувались у пам'яті. На практиці —
> **ті самі дані, але у власній БД**, з нормальними зв'язками, пошуком і транзакціями. Це і є
> природний фінал курсу: від «застосунок читає чужу таблицю» до «застосунок володіє своїми даними».

---

*Кінець Лекції 2. Далі — 14 годин практики. Успіху!*
