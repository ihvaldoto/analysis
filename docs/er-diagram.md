# ER-диаграмма БД «Учёт и анализ личных расходов»

```mermaid
erDiagram
    USERS ||--o{ ACCOUNTS : "владеет"
    USERS ||--o{ CATEGORIES : "создаёт"
    USERS ||--o{ EXPENSES : "совершает"
    ACCOUNTS ||--o{ EXPENSES : "источник средств"
    CATEGORIES ||--o{ EXPENSES : "классифицирует"
    CATEGORIES ||--o{ BUDGETS : "планирует"

    USERS {
        bigint id PK
        varchar login UK
        varchar password_hash
        varchar display_name
    }

    ACCOUNTS {
        bigint id PK
        varchar name
        numeric balance
        varchar currency
        bigint user_id FK
    }

    CATEGORIES {
        bigint id PK
        varchar name
        varchar description
        numeric monthly_limit
        bigint user_id FK
    }

    EXPENSES {
        bigint id PK
        numeric amount
        timestamp spent_at
        varchar comment
        bigint user_id FK
        bigint account_id FK
        bigint category_id FK
    }

    BUDGETS {
        bigint id PK
        date period_start
        date period_end
        numeric planned_amount
        bigint category_id FK
    }
```

## Сущности

| Сущность | Логические атрибуты | Связи |
|---|---|---|
| **User** (пользователь) | login, password_hash, display_name | 1 → N Account, Category, Expense |
| **Account** (счёт/кошелёк) | name, balance, currency | N → 1 User; 1 → N Expense |
| **Category** (категория расходов) | name, description, monthly_limit | N → 1 User; 1 → N Expense, Budget |
| **Expense** (трата) | amount, spent_at, comment | N → 1 User, Account, Category |
| **Budget** (плановый бюджет) | period_start, period_end, planned_amount | N → 1 Category |

У каждой сущности не менее 3 логических атрибутов (без учёта первичных и внешних ключей) и хотя бы одна связь с другой сущностью.
