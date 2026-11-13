# Учёт и анализ личных расходов

Spring Boot приложение. Java 21, Maven 3.6.3+.


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

## Практическая работа 8: автоматизированное тестирование

Все тесты запускаются командой `mvn test`. Запускать приложение или PostgreSQL отдельно не нужно: HTTP и браузерные тесты поднимают приложение на свободном порту с тестовой базой H2.

| Тесты | Что проверяют |
|---|---|
| `AppUserServiceUnitTest` | JUnit 5 и Mockito: поиск пользователя, регистрация, валидация и исключения при повторном логине. |
| `ExpenseRestAssuredTest` | HTTP-ответы контроллера расходов: найденная запись, 400, 404, пустой результат и включение границ периода и минимальной суммы. |
| `LoginLogoutUiTest` | Вход и выход через формы в Chrome, переход на список трат и запрет доступа к нему после выхода. |

Для UI-теста нужен Google Chrome. Тест запускает браузер без окна, а WebDriverManager подбирает ChromeDriver. Если драйвер уже скачан, его можно передать явно: `mvn "-Dwebdriver.chrome.driver=C:/tools/chromedriver.exe" test`.

JUnit и Mockito подключены через `spring-boot-starter-test`; версии JUnit, Mockito, REST Assured и Selenium задаёт Spring Boot. Mockito использует `mock-maker-subclass`, поэтому этим тестам не нужно подключать Java-агент во время выполнения.


warning: in the working copy of 'README.md', LF will be replaced by CRLF the next time Git touches it

## Actuator

После запуска доступны:

- `http://localhost:8080/actuator/health`
- `http://localhost:8080/actuator/info`
- `http://localhost:8080/actuator/metrics`
- `http://localhost:8080/actuator/metrics/jvm.info`
