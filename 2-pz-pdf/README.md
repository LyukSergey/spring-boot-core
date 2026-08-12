# ПЗ-2. Генератор PDF-звітів про продажі з email-розсилкою

Spring Boot (Maven, Java 17) сервіс: менеджери додають продажі через REST API (дані **в пам'яті**,
без БД), сервіс генерує **PDF-звіт** з кирилицею, розсилає його **вкладенням** на email і робить це
**автоматично** раз на місяць (`@Scheduled`).

## Архітектура — порти й адаптери (на інтерфейсах)

Уся логіка описана **інтерфейсами (портами)**; конкретні класи — **адаптери**, які легко підмінити.

| Порт (інтерфейс) `port/` | Адаптер (реалізація) |
|--------------------------|----------------------|
| `SaleRepository`         | `adapter/persistence/InMemorySaleRepository` (ConcurrentHashMap) |
| `SummaryCalculator`      | `adapter/report/DefaultSummaryCalculator` |
| `ReportGenerator`        | `adapter/report/OpenPdfReportGenerator` (**незалежна** генерація PDF) |
| `ReportSender`           | `adapter/mail/EmailReportSender` (SMTP + вкладення) |
| `ReportService` (фасад)  | `application/DefaultReportService` (оркестратор) |

```
com.example.sales
├── SalesApplication              (@SpringBootApplication + @EnableScheduling)
├── domain/                       Sale, SalesSummary, ReportData        (чисті моделі)
├── port/                         SaleRepository, SummaryCalculator,
│                                 ReportGenerator, ReportSender, ReportService   (ІНТЕРФЕЙСИ)
├── adapter/
│   ├── persistence/InMemorySaleRepository
│   ├── report/DefaultSummaryCalculator
│   ├── report/OpenPdfReportGenerator          ← НЕЗАЛЕЖНА реалізація PDF
│   ├── report/ReportGeneratorConfig           ← Spring-«проводка»: вантажить шрифт, створює бін
│   └── mail/EmailReportSender
├── application/                  DefaultReportService, ScheduledReport
└── web/                          SaleController, ReportController, dto/, error/
resources/
├── application.yml               (секрети — через ENV, НЕ в git)
├── application-local.example.yml (приклад локального конфігу зі SMTP)
└── fonts/DejaVuSans.ttf          (шрифт із кирилицею)
```

### Чому генерація PDF «абсолютно незалежна»

`OpenPdfReportGenerator`:

- залежить **лише** від доменних моделей і власного порту `ReportGenerator` — **жодних**
  Spring Web / Mail / сховища;
- **не має Spring-анотацій**: шрифт приймається як `byte[]` у конструкторі (не `@Value`/`Resource`).
  Spring-«проводка» винесена окремо в `ReportGeneratorConfig`;
- тому створюється і **тестується без Spring-контексту** — див.
  `OpenPdfReportGeneratorTest`.

## Вибір PDF-бібліотеки

**OpenPDF 2.0.3** (форк iText 4, ліцензія LGPL/MPL — вільна для навчання й комерції).
Сучасний **iText 7 має AGPL**, тому не він.

### Кирилиця (головна пастка)

Стандартні шрифти PDF не малюють кирилицю → «квадратики». Рішення — вбудований TTF:

```java
BaseFont.createFont("cyrillic-font.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED, true, ttf, null);
```

`IDENTITY_H` + `EMBEDDED` + TTF з кирилицею (**DejaVu Sans**) = текст читабельний.
Перевірено: `pdffonts` показує `GBNQKL+DejaVuSans … Identity-H … emb: yes`.

## Ендпоінти

| Метод | URL | Опис |
|-------|-----|------|
| `POST` | `/sales` | додати продаж (валідація) |
| `GET`  | `/sales?region=&from=&to=` | список із фільтрами (регіон + період — бонус) |
| `GET`  | `/reports/sales.pdf?month=2026-09` | PDF-звіт inline |
| `POST` | `/reports/send?month=2026-09` | згенерувати й розіслати email-ом |

`@Scheduled(cron = "${report.cron}")` — 1-го числа кожного місяця о 08:00 розсилає звіт за
попередній місяць.

## Запуск

```bash
mvn test          # усі тести (включно з генерацією PDF у target/sample-sales-2026-09.pdf)
mvn spring-boot:run
```

SMTP-секрети — через змінні оточення (в git їх немає):

```bash
MAIL_USERNAME=you@gmail.com MAIL_PASSWORD=app_password \
REPORT_RECIPIENTS=boss@example.com,lead@example.com \
mvn spring-boot:run
```

### Перевірка вручну

```bash
curl -X POST http://localhost:8080/sales -H "Content-Type: application/json" \
  -d '{"manager":"Іван","product":"Ноутбук","amount":25000,"region":"Захід","date":"2026-07-05"}'

open "http://localhost:8080/reports/sales.pdf?month=2026-07"
curl -X POST "http://localhost:8080/reports/send?month=2026-07"
```

## Тести (`mvn test` — зелений)

- `DefaultSummaryCalculatorTest` — підрахунок підсумків (total, byRegion, byManager).
- `OpenPdfReportGeneratorTest` — **незалежна** генерація PDF з кириличними даними: PDF не порожній,
  починається з `%PDF-`, зберігається в `target/sample-sales-2026-09.pdf` для візуальної перевірки.
- `SaleControllerTest` — REST: POST/GET, валідація (400), віддача PDF.

## Реалізовані бонуси

- **+2** діаграма (стовпчики по регіонах) у PDF.
- **+2** фільтр звіту за довільним періодом (`from`/`to`) у `GET /sales`.
