# Data Analysis — учёт и анализ личных расходов

Приложение на Spring Boot с хранением данных в PostgreSQL через Spring Data JPA / Hibernate.

## Требования

- Java 21
- Maven 3.6.3 или новее
- PostgreSQL (база `data_analysis`)

## Настройка базы данных

Создать базу:

```sql
CREATE DATABASE data_analysis;
```

Параметры подключения — в `src/main/resources/application.properties`
(`spring.datasource.url`, `username`, `password`). Таблицы создаются автоматически
при старте (`spring.jpa.hibernate.ddl-auto=update`).

## Структура БД

5 сущностей (см. [ER-диаграмму](docs/er-diagram.md)):

- `User` — пользователь (login, password_hash, display_name).
- `Account` — счёт (name, balance, currency).
- `Category` — категория расходов (name, description, monthly_limit).
- `Expense` — трата (amount, spent_at, comment).
- `Budget` — плановый бюджет по категории (period_start, period_end, planned_amount).

## Доступ к данным

- Репозитории Spring Data на базе `CrudRepository` — пакет `repository`.
- Query Method с ключевыми словами And/Between: `ExpenseRepository.findByUserIdAndAmountBetween`.
- JPQL через связанную сущность: `ExpenseRepository.findByCategoryName` (`@Query`).
- Те же два запроса на Criteria API: `ExpenseRepositoryCustom` + `ExpenseRepositoryImpl`.

## Транзакции

`AccountServiceImpl.createAccountWithExpense` создаёт счёт и первую трату в одной
транзакции через `PlatformTransactionManager`. Трата не может существовать без счёта,
поэтому при некорректной трате транзакция откатывается и счёт не сохраняется.

## Тесты

```bash
mvn test
```

- `ExpenseRepositoryQueryTest` — Query Method и `@Query`.
- `ExpenseCriteriaTest` — аналогичные запросы на Criteria API.
- `AccountServiceTransactionTest` — транзакционный метод: успешный сценарий и откат.

Тесты выполняются на in-memory базе H2 и не требуют запущенного PostgreSQL.

## Actuator

- `http://localhost:8080/actuator/health`
- `http://localhost:8080/actuator/metrics`
- `http://localhost:8080/actuator/metrics/jvm.info`
