# example-rag-by-spring-documentation

Многомодульный Maven-проект для демонстрации работы с **Spring AI** и **MCP** (Model Context Protocol).

---

## 📦 Модули проекта

| Модуль | Описание | Транспорт | Тип | Документация |
|--------|----------|-----------|-----|--------------|
| [`example-rag`](./example-rag/) | RAG-приложение на Spring AI | N/A | Spring Boot App | [README](./example-rag/README-IVAN.md) |
| [`stdio-sync-mcp-server`](./stdio-sync-mcp-server/) | MCP-сервер | STDIO | Синхронный | — |
| [`stdio-sync-mcp-client`](./stdio-sync-mcp-client/) | MCP-клиент | STDIO | Синхронный | — |
| [`webmvc-sync-mcp-server`](./webmvc-sync-mcp-server/) | MCP-сервер | SSE/STDIO | Синхронный | — |
| [`webmvc-sync-mcp-client`](./webmvc-sync-mcp-client/) | MCP-клиент | SSE | Синхронный | — |
| [`webflux-async-mcp-server`](./webflux-async-mcp-server/) | MCP-сервер | SSE/STDIO | Асинхронный | — |
| [`webflux-async-mcp-client`](./webflux-async-mcp-client/) | MCP-клиент | SSE | Асинхронный | — |
| [`mcp-server-jar-unpacker`](./mcp-server-jar-unpacker/) | Декомпиляция JAR | STDIO | Утилитарный | [QWEN.md](./mcp-server-jar-unpacker/QWEN.md) |

---

## 🏗️ Архитектура

```
example-rag-by-spring-documentation/
├── pom.xml                           # Родительский POM
├── README.md                         # Этот файл — обзор проекта
├── CHEATSHEET.md                     # Шпаргалка по командам
├── QWEN.md                           # Настройка Qwen Code
├── DOCUMENTATION_AUDIT.md            # Аудит документации
├── .qwen/                            # Настройки Qwen Code
│   └── workplace/                    # Рабочее пространство
│       ├── PROJECT_RULES.md          # Правила проекта (XP, TDD)
│       ├── TASK_INDEX.md             # Индекс задач
│       ├── task_template.md          # Шаблон задач
│       ├── to_work/                  # Активные задачи
│       └── archive/                  # Выполненные задачи
├── example-rag/                      # RAG-приложение (Spring AI + PostgreSQL + Ollama)
├── stdio-sync-mcp-server/            # STDIO сервер
├── stdio-sync-mcp-client/            # STDIO клиент
├── webmvc-sync-mcp-server/           # WebMVC сервер (SSE)
├── webmvc-sync-mcp-client/           # WebMVC клиент (SSE)
├── webflux-async-mcp-server/         # WebFlux сервер (SSE, async)
├── webflux-async-mcp-client/         # WebFlux клиент (SSE)
└── mcp-server-jar-unpacker/          # Утилита декомпиляции JAR
```

---

## 🚀 Быстрый старт

### Сборка

```bash
# Весь проект
mvn clean package

# Конкретный модуль
mvn clean package -pl <module-name>
```

### Запуск

```bash
# example-rag
mvn spring-boot:run -pl example-rag

# MCP-клиент
mvn spring-boot:run -pl stdio-sync-mcp-client

# MCP-серверы с STDIO-профилем
mvn spring-boot:run -pl webmvc-sync-mcp-server -Dspring-boot.run.profiles=stdio

# mcp-server-jar-unpacker
java -jar mcp-server-jar-unpacker/target/mcp-server-jar-unpacker-1.0-SNAPSHOT.jar
```

### Тесты

```bash
# Весь проект
mvn test

# Конкретный модуль
mvn test -pl stdio-sync-mcp-server
mvn test -pl mcp-server-jar-unpacker
```

**Подробнее:** [CHEATSHEET.md](./CHEATSHEET.md)

---

## 📊 Сравнение WebMVC vs WebFlux

| Критерий | WebMVC | WebFlux |
|----------|--------|---------|
| **Тип** | Синхронный (блокирующий) | Асинхронный (неблокирующий) |
| **Потоки** | Один поток — один запрос | Event-loop, мало потоков |
| **Сервер** | Servlet-контейнер | Реактивный сервер |
| **Транспорт** | SSE/STDIO | SSE/STDIO |
| **Подход** | Блокирующий I/O | Reactive Streams |

---

## 🛠️ Технологии

| Технология | Версия | Модули |
|------------|--------|--------|
| Java | 17 | Все |
| Spring Boot | 3.5.5 | MCP-модули |
| Spring Boot | 4.0.2 | example-rag |
| Spring AI | 1.0.3 | MCP-модули |
| Spring AI | 2.0.0-M2 | example-rag |
| Maven | 3.x | Все |
| JUnit 5 | 5.8.2+ | Все |
| Logbook | 3.5.0 | webmvc-* |

---

## 📚 Документация

| Файл | Описание |
|------|----------|
| **[CHEATSHEET.md](./CHEATSHEET.md)** | Шпаргалка: команды сборки, запуска, тестов |
| **[QWEN.md](./QWEN.md)** | Настройка Qwen Code, рабочий процесс, задачи |
| **[.qwen/workplace/PROJECT_RULES.md](./.qwen/workplace/PROJECT_RULES.md)** | Правила проекта: TDD, XP, конвенции |
| **[mcp-server-jar-unpacker/QWEN.md](./mcp-server-jar-unpacker/QWEN.md)** | Документация по утилите декомпиляции |
| **[.qwen/workplace/TASK_INDEX.md](./.qwen/workplace/TASK_INDEX.md)** | Индекс всех задач проекта |

---

## 🎯 Рабочий процесс

### Создание задачи

```bash
cp .qwen/workplace/task_template.md .qwen/workplace/to_work/TASK-XXX_описание.md
```

### TDD цикл

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

**Подробнее:** [.qwen/workplace/PROJECT_RULES.md](./.qwen/workplace/PROJECT_RULES.md)

---

## 🔗 Ссылки

- [Spring AI Documentation](https://docs.spring.io/spring-ai/docs/current/reference/html/)
- [MCP Specification](https://modelcontextprotocol.io/)
- [JUnit 5 User Guide](https://junit.org/junit5/docs/current/user-guide/)
- [Mockito Documentation](https://javadoc.io/doc/org.mockito/mockito-core/latest/)

---

**Версия:** 1.0  
**Дата:** 2026-03-26
