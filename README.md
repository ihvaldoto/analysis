

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
## Actuator

После запуска доступны:

- `http://localhost:8080/actuator/health`
- `http://localhost:8080/actuator/info`
- `http://localhost:8080/actuator/metrics`
- `http://localhost:8080/actuator/metrics/jvm.info`
