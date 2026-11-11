## ShareIt

REST API для сервиса совместного использования вещей. Пользователи могут размещать вещи, доступные для аренды,
бронировать вещи других пользователей, оставлять отзывы и создавать заявки на нужные вещи.

### Технологии

- Java 21
- Spring Boot 3.3.2
- Spring Data JPA / Hibernate
- PostgreSQL 16
- H2 (тесты репозиториев)
- Maven
- JUnit 5, Mockito, MockMvc
- Docker Compose
- Postman / Newman

### Функциональность

- Создание, просмотр, обновление и удаление пользователей.
- Добавление, редактирование и поиск доступных вещей.
- Создание заявок на вещи и просмотр ответов на заявки.
- Привязка вещей к заявкам.
- Создание бронирований и подтверждение или отклонение заявок на бронирование владельцем вещи.
- Просмотр бронирований с фильтрацией по статусу.
- Отображение предыдущего и следующего бронирования для владельца вещи.
- Добавление и просмотр отзывов о вещах.

### Запуск

Необходимы JDK 21, Maven и Docker Compose.

**PostgreSQL**

```bash
docker compose up -d
docker compose ps
```

**Server**

```bash
mvn -pl server spring-boot:run
```

**Gateway** (в отдельном терминале)

```bash
mvn -pl gateway spring-boot:run
```

**Остановить контейнеры**

```bash
docker compose down
```

### Тестирование

**Сборка и тесты**

```bash
mvn clean verify
```

**Checkstyle**

```bash
mvn checkstyle:check
```

**Newman**

```bash
npx newman run postman/sprint-16.postman_collection.json \
  --env-var "baseUrl=http://localhost:8080"
```

**Отчёт Newman в JSON**

```bash
npx newman run postman/sprint-16.postman_collection.json \
  --env-var "baseUrl=http://localhost:8080" \
  --reporters cli,json \
  --reporter-json-export newman-results.json
```

### API

| Ресурс         | Базовый путь              |
|----------------|---------------------------|
| Пользователи   | `/users`                  |
| Вещи           | `/items`                  |
| Бронирования   | `/bookings`               |
| Заявки на вещи | `/requests`               |
| Отзывы         | `/items/{itemId}/comment` |

Для операций, требующих идентификации пользователя, используется HTTP-заголовок `X-Sharer-User-Id`.

### Структура проекта

- `gateway` — HTTP API, валидация запросов и передача их серверу.
- `server` — бизнес-логика, работа с базой данных и формирование ответов.
