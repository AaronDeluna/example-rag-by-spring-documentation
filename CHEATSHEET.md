# Шпаргалка по проекту example-rag-by-spring-documentation

> **📋 Обзор проекта:** [README.md](./README.md) — модули, архитектура, сравнение WebMVC/WebFlux  
> **⚙️ Настройка Qwen Code:** [QWEN.md](./QWEN.md) — рабочий процесс, задачи  
> **📜 Правила проекта:** [.qwen/workplace/PROJECT_RULES.md](./.qwen/workplace/PROJECT_RULES.md) — TDD, XP, конвенции

---

## 🚀 Быстрый старт

### Сборка

```bash
# Весь проект
mvn clean package

# Конкретный модуль
mvn clean package -pl <module-name>

# Без тестов
mvn clean install -pl <module-name> -DskipTests
```

### Тесты

```bash
# Весь проект
mvn test

# Конкретный модуль
mvn test -pl stdio-sync-mcp-server
mvn test -pl webmvc-sync-mcp-server -Dtest=WebMvcClientTest
mvn test -pl mcp-server-jar-unpacker

# Конкретный тест
mvn test -pl mcp-server-jar-unpacker -Dtest=JsonUtilsTest
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

---

## 📝 Работа с задачами

### Создать новую задачу

```bash
# Копирование шаблона
cp .qwen/workplace/task_template.md .qwen/workplace/to_work/TASK-XXX_описание.md

# Редактирование
vim .qwen/workplace/to_work/TASK-XXX_описание.md
```

### Обновить TASK_INDEX.md

Добавить строку в таблицу:
```markdown
| XXX | Описание задачи | 📋 | 2026-03-26 | | TASK-XXX_описание.md |
```

### Завершить задачу

```bash
# Переместить в archive
mv .qwen/workplace/to_work/TASK-XXX_описание.md .qwen/workplace/archive/
```

**Подробнее:** [QWEN.md](./QWEN.md), [PROJECT_RULES.md](./.qwen/workplace/PROJECT_RULES.md)

---

## 🎯 TDD Цикл

**Подробнее:** [PROJECT_RULES.md](./.qwen/workplace/PROJECT_RULES.md)

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

---

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

---

## 📚 Документация

| Файл | Описание |
|------|----------|
| [README.md](./README.md) | Обзор проекта, модули, архитектура |
| [QWEN.md](./QWEN.md) | Настройка Qwen Code, рабочий процесс |
| [PROJECT_RULES.md](./.qwen/workplace/PROJECT_RULES.md) | Правила проекта (XP, TDD) |
| [TASK_INDEX.md](./.qwen/workplace/TASK_INDEX.md) | Индекс всех задач |
| [mcp-server-jar-unpacker/QWEN.md](./mcp-server-jar-unpacker/QWEN.md) | Утилита декомпиляции JAR |

---

## 🛠️ Полезные команды

```bash
# Дерево зависимостей
mvn dependency:tree -pl <module-name>

# Зависимости плагинов
mvn dependency:resolve-plugins

# Полное тестирование mcp-server-jar-unpacker
cd mcp-server-jar-unpacker && python3 test_all_tools.py
```

---

## 🔗 Ссылки

- [Spring AI](https://docs.spring.io/spring-ai/docs/current/reference/html/)
- [MCP Specification](https://modelcontextprotocol.io/)
- [JUnit 5](https://junit.org/junit5/docs/current/user-guide/)
- [Mockito](https://javadoc.io/doc/org.mockito/mockito-core/latest/)
