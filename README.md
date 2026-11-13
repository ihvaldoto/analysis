

## Практическая работа 8: автоматизированное тестирование

Все тесты запускаются командой `mvn test`. Запускать приложение или PostgreSQL отдельно не нужно: HTTP и браузерные тесты поднимают приложение на свободном порту с тестовой базой H2.

| Тесты | Что проверяют |
|---|---|
| `AppUserServiceUnitTest` | JUnit 5 и Mockito: поиск пользователя, регистрация, валидация и исключения при повторном логине. |
| `ExpenseRestAssuredTest` | HTTP-ответы контроллера расходов: найденная запись, 400, 404, пустой результат и включение границ периода и минимальной суммы. |
| `LoginLogoutUiTest` | Вход и выход через формы в Chrome, переход на список трат и запрет доступа к нему после выхода. |

Для UI-теста нужен Google Chrome. Тест запускает браузер без окна, а WebDriverManager подбирает ChromeDriver. Если драйвер уже скачан, его можно передать явно: `mvn "-Dwebdriver.chrome.driver=C:/tools/chromedriver.exe" test`.

JUnit и Mockito подключены через `spring-boot-starter-test`; версии JUnit, Mockito, REST Assured и Selenium задаёт Spring Boot. Mockito использует `mock-maker-subclass`, поэтому этим тестам не нужно подключать Java-агент во время выполнения.

## Actuator

После запуска доступны:

- `http://localhost:8080/actuator/health`
- `http://localhost:8080/actuator/info`
- `http://localhost:8080/actuator/metrics`
- `http://localhost:8080/actuator/metrics/jvm.info`
