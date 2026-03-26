# Шпаргалка по проекту example-rag-by-spring-documentation

## 📦 Модули

```
example-rag-by-spring-documentation/
├── example-rag                    # RAG-приложение (Spring AI + PostgreSQL + Ollama)
├── stdio-sync-mcp-server          # MCP-сервер (STDIO, синхронный)
├── stdio-sync-mcp-client          # MCP-клиент (STDIO, синхронный)
├── webmvc-sync-mcp-server         # MCP-сервер (WebMVC, SSE)
├── webmvc-sync-mcp-client         # MCP-клиент (WebMVC, SSE)
├── webflux-async-mcp-server       # MCP-сервер (WebFlux, SSE, асинхронный)
├── webflux-async-mcp-client       # MCP-клиент (WebFlux, SSE)
└── mcp-server-jar-unpacker        # Утилита декомпиляции JAR
```

## 🚀 Быстрый старт

### Сборка

```bash
# Весь проект
mvn clean package

# Конкретный модуль
mvn clean package -pl mcp-server-jar-unpacker
```

### Тесты

```bash
# Весь проект
mvn test

# Конкретный модуль
mvn test -pl stdio-sync-mcp-server
mvn test -pl webmvc-sync-mcp-server -Dtest=WebMvcClientTest
mvn test -pl mcp-server-jar-unpacker
```

### Запуск

```bash
# example-rag
mvn spring-boot:run -pl example-rag

# stdio-sync-mcp-client
mvn spring-boot:run -pl stdio-sync-mcp-client

# MCP-серверы с STDIO-профилем
mvn spring-boot:run -pl webmvc-sync-mcp-server -Dspring-boot.run.profiles=stdio
mvn spring-boot:run -pl webflux-async-mcp-server -Dspring-boot.run.profiles=stdio

# mcp-server-jar-unpacker
java -jar mcp-server-jar-unpacker/target/mcp-server-jar-unpacker-1.0-SNAPSHOT.jar
```

## 📝 Работа с задачами

### Создать новую задачу

```bash
# Копирование шаблона
cp .qwen/workplace/task_template.md .qwen/workplace/to_work/TASK-017_описание.md

# Редактирование
vim .qwen/workplace/to_work/TASK-017_описание.md
```

### Обновить TASK_INDEX.md

Добавить строку в таблицу:
```markdown
| 017 | Описание задачи | 📋 | 2026-03-26 | | TASK-017_описание.md |
```

### Завершить задачу

1. Отметить все чек-боксы в файле задачи
2. Переместить в archive:
```bash
mv .qwen/workplace/to_work/TASK-XXX_описание.md .qwen/workplace/archive/
```
3. Обновить статус в TASK_INDEX.md на ✅

## 🎯 TDD Цикл

```
┌─────────────┐
│     RED     │ → Тест падает
└──────┬──────┘
       ▼
┌─────────────┐
│    GREEN    │ → Минимальный код
└──────┬──────┘
       ▼
┌─────────────┐
│  REFACTOR   │ → Улучшение кода
└─────────────┘
```

### Именование тестов

```java
@Test
@DisplayName("Дан валидный запрос, когда инициализация, тогда возвращаем capabilities")
void givenValidRequestWhenInitializeThenReturnsCapabilities() {
    // Arrange
    // Act
    // Assert
}
```

## 🔍 Навигация

### Поиск файлов

```bash
# Найти Java-класс
find . -name "*Server.java"

# Найти все тесты
find . -name "*Test.java"
```

### Поиск по коду

```bash
# Найти использование аннотации
grep -r "@RestController" --include="*.java" .

# Найти метод
grep -r "public.*initialize" --include="*.java" .
```

## 📚 Документация

| Файл | Описание |
|------|----------|
| `QWEN.md` | Полная настройка Qwen Code для проекта |
| `README-VADIM.md` | Общая информация о модулях |
| `mcp-server-jar-unpacker/QWEN.md` | Документация по утилите декомпиляции |
| `.qwen/workplace/PROJECT_RULES.md` | Правила проекта (XP, TDD) |
| `.qwen/workplace/TASK_INDEX.md` | Индекс всех задач |

## 🛠️ Полезные команды

```bash
# Дерево зависимостей
mvn dependency:tree -pl <module-name>

# Сборка без тестов
mvn clean install -pl <module-name> -DskipTests

# Запуск конкретного теста
mvn test -pl mcp-server-jar-unpacker -Dtest=JsonUtilsTest

# Полное тестирование mcp-server-jar-unpacker
cd mcp-server-jar-unpacker && python3 test_all_tools.py
```

## 📊 Сравнение WebMVC vs WebFlux

| Критерий | WebMVC | WebFlux |
|----------|--------|---------|
| Тип | Синхронный (блокирующий) | Асинхронный (неблокирующий) |
| Потоки | Один поток — один запрос | Event-loop, мало потоков |
| Сервер | Servlet-контейнер | Реактивный сервер |
| Транспорт | SSE | SSE/STDIO |

## 🔗 Ссылки

- [Spring AI](https://docs.spring.io/spring-ai/docs/current/reference/html/)
- [MCP Specification](https://modelcontextprotocol.io/)
- [JUnit 5](https://junit.org/junit5/docs/current/user-guide/)
- [Mockito](https://javadoc.io/doc/org.mockito/mockito-core/latest/)
