# Data Analysis

Консольное приложение на Spring Boot для учёта наборов данных и анализа измерений.

## Требования

- Java 21
- Maven 3.6.3 или новее

Данные хранятся в памяти и сбрасываются при перезапуске приложения.

## Модель

- `User` — учётная запись с логином, хешем пароля BCrypt и отображаемым именем.
- `DataSet` — набор данных, принадлежащий пользователю.
- `DataPoint` — числовое измерение набора с названием метрики, значением, единицей и временем записи.

Все три сущности имеют уникальный идентификатор. Репозитории используют singleton-списки, внедряемые Spring.

## Команды которые я запускал


```text
register anna secret Anna
login anna secret
sets create weather weather_station_readings
points add 1 temperature 18.5 C
points add 1 temperature 21.0 C
analyze 1 temperature
points list 1
logout
```

Для каждой метрики команда analyze показывает количество записей, сумму, среднее, минимум и максимум. Команды sets, points и analyze доступны после входа; пользователь может работать только со своими наборами.

Полный список команд выводится при запуске или командой help.

## Actuator

После запуска доступны:

- `http://localhost:8080/actuator/health`
- `http://localhost:8080/actuator/info`
- `http://localhost:8080/actuator/metrics`
- `http://localhost:8080/actuator/metrics/jvm.info`
