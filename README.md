# Учёт и анализ личных расходов

Spring Boot приложение. Java 21, Maven 3.6.3+.

## Практическая работа 4: БД, Hibernate, Spring Data

Данные расходов хранятся в PostgreSQL.

### Запуск

1. Установите PostgreSQL и создайте базу: `CREATE DATABASE data_analysis;`
2. При необходимости задайте переменные `DB_URL`, `DB_USER`, `DB_PASSWORD`
   (по умолчанию `jdbc:postgresql://localhost:5432/data_analysis`, `postgres` / `postgres`).
3. `mvn spring-boot:run` — таблицы создаются автоматически (`ddl-auto=update`).
4. `mvn test` — тесты используют встроенную H2 в режиме PostgreSQL, запущенная СУБД не нужна.

### ER-диаграмма

```mermaid
erDiagram
    APP_USERS ||--o{ ACCOUNTS : owns
    APP_USERS ||--o{ CATEGORIES : owns
    ACCOUNTS ||--o{ EXPENSES : "spent from"
    CATEGORIES ||--o{ EXPENSES : classifies
    CATEGORIES ||--o{ BUDGETS : limits

    APP_USERS {
        bigint id PK
        varchar login UK
        varchar display_name
        varchar email
    }
    ACCOUNTS {
        bigint id PK
        varchar name
        varchar currency
        numeric balance
        bigint owner_id FK
    }
    CATEGORIES {
        bigint id PK
        varchar name
        varchar description
        boolean archived
        bigint owner_id FK
    }
    EXPENSES {
        bigint id PK
        numeric amount
        varchar description
        date spent_at
        bigint account_id FK
        bigint category_id FK
    }
    BUDGETS {
        bigint id PK
        numeric limit_amount
        date period_start
        date period_end
        bigint category_id FK
    }
```

### Структура кода (`ru.student.dataanalysis.expense`)

- `entity` — сущности `AppUser`, `Account`, `Category`, `Expense`, `Budget`.
- `repository` — `CrudRepository` для каждой сущности. В `ExpenseRepository`:
  `findBySpentAtBetweenAndAmountGreaterThanEqual` (Query Lookup Strategies, `Between` + `And`) и
  `findByCategoryName` (JPQL через `@Query`, поиск по связанной сущности).
- `dao` — `ExpenseCriteriaRepository` и `ExpenseCriteriaRepositoryImpl`: те же два запроса на Criteria API.
- `service` — `ExpenseTrackingService.registerExpense`: запись траты и списание со счёта в одной транзакции
  (`PlatformTransactionManager`); при нехватке средств всё откатывается.
- Тесты: `src/test/java/.../expense` (положительные кейсы и откат транзакции).

## Практическая работа 5: Spring Web, REST API

Запуск: `mvn spring-boot:run`, порт 8080.

| Что | Адрес |
|---|---|
| Репозитории через Spring Data REST | `/users`, `/accounts`, `/categories`, `/expenses`, `/budgets` |
| Поиск Spring Data REST | `/expenses/search/findByCategoryName?categoryName=Еда` |
| Свои REST-методы (репозиторий) | `/custom/expenses/findByPeriodAndMinAmount?from=2031-03-01&to=2031-03-31&minAmount=100`, `/custom/expenses/findByCategoryName?categoryName=Еда` |
| Те же методы на Criteria API | `/custom/expenses/criteria/findByPeriodAndMinAmount`, `/custom/expenses/criteria/findByCategoryName` |
| Трата по id (404, если нет) | `/custom/expenses/{id}` |
| HTML-страница со списком трат | `/custom/expenses/view/list` |
| Swagger UI | `/swagger-ui/index.html` (спецификация: `/v3/api-docs`) |

Ошибки обрабатывает `ExceptionControllerAdvice` и всегда отвечает JSON `{"message": "..."}`
со статусом 400, 404, 405 или 500.

Код: `expense/controller` (REST и HTML), `expense/exception` (обработка ошибок),
шаблон `src/main/resources/templates/expenseList.html`, тесты `RestApiTest`.

Версия springdoc 2.8.9 вместо 2.6.0 из методички: 2.6.0 не рассчитана на Spring Boot 3.5.
## Практическая работа 6: Spring Security

Вход по логину и паролю через свою форму, роли `USER` и `ADMIN`.

- `/registration` — регистрация (доступна всем). Пароль хранится как BCrypt-хеш, роль новому пользователю всегда `USER`, из формы её выбрать нельзя.
- `/login`, `/logout` — вход и выход (выход — кнопка на странице `/custom/expenses/view/list`).
- Всё остальное только для вошедших; неавторизованного перенаправляет на `/login`.
- Только `ADMIN`: Swagger UI (`/swagger-ui/index.html`, `/v3/api-docs`), список пользователей `/users`, `/actuator/**`.
- Администратор создаётся при старте из `app.admin.login` / `app.admin.password` (в `application.properties` по умолчанию `admin` / `admin`, смените). Если `app.admin.password` не задан, администратор не создаётся.
- Хеш пароля скрыт из JSON (`@JsonIgnore`), через REST не отдаётся.
- CSRF-защита включена: формы Thymeleaf подставляют токен сами, а POST/PUT/DELETE к REST из Swagger UI без токена вернут 403.

Код: `expense/security` (`SecurityConfig`, `AppUserDetailsService`, `AdminInitializer`), `expense/service/AppUserService`,
`expense/controller` (`RegistrationController`, `LoginController`), шаблоны `login.html` и `registration.html`,
тесты `SecurityTest` и `AppUserServiceTest`.
## Практическая работа 7: многопоточность и асинхронность

Отчёт со статистикой приложения в виде HTML-страницы.

| Что | Адрес |
|---|---|
| Создать отчёт и запустить формирование (возвращает id, не ждёт результата) | `POST /custom/reports` |
| Получить отчёт по id | `GET /custom/reports/{id}` |

Ответ `GET`: готовый отчёт — HTML (200); ещё формируется — JSON `{"message": ...}` (202);
завершился ошибкой — JSON с текстом ошибки (500); такого id нет — 404.

Как формируется (`ReportServiceImpl.generateReportAsync`): метод работает через `CompletableFuture`; внутри
количество пользователей и список трат считаются в двух отдельных `Thread`, главный поток ждёт их через `join()`.
В отчёте: число пользователей, таблица трат, время каждого из двух шагов и общее время. В БД отчёт хранится
в сущности `Report` (id, статус `CREATED` / `COMPLETED` / `ERROR`, содержимое). Данные в HTML экранируются.

`POST /custom/reports` освобождён от CSRF-проверки, чтобы его можно было вызвать из Swagger UI и клиентов без формы.
Вход в систему при этом по-прежнему нужен.

Код: `expense/entity/Report`, `ReportStatus`, `expense/service/ReportService(Impl)`, `expense/controller/ReportController`,
тесты `ReportServiceTest`, `ReportErrorTest`, `ReportControllerTest`.
## Actuator

После запуска доступны:

- `http://localhost:8080/actuator/health`
- `http://localhost:8080/actuator/info`
- `http://localhost:8080/actuator/metrics`
- `http://localhost:8080/actuator/metrics/jvm.info`
