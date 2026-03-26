# Настройка Qwen Code для работы со всем проектом

## 📋 Обзор проекта

**example-rag-by-spring-documentation** — многомодульный Maven-проект для демонстрации работы с Spring AI и MCP (Model Context Protocol).

### Модули проекта

| Модуль | Описание | Транспорт | Тип |
|--------|----------|-----------|-----|
| `example-rag` | RAG-приложение на Spring AI | N/A | Spring Boot App |
| `stdio-sync-mcp-server` | MCP-сервер с STDIO транспортом | STDIO | Синхронный |
| `stdio-sync-mcp-client` | MCP-клиент для STDIO-сервера | STDIO | Синхронный |
| `webmvc-sync-mcp-server` | MCP-сервер на Spring WebMVC | SSE/STDIO | Синхронный |
| `webmvc-sync-mcp-client` | MCP-клиент для WebMVC-сервера | SSE | Синхронный |
| `webflux-async-mcp-server` | MCP-сервер на Spring WebFlux | SSE/STDIO | Асинхронный |
| `webflux-async-mcp-client` | MCP-клиент для WebFlux-сервера | SSE | Асинхронный |
| `mcp-server-jar-unpacker` | Инструмент для декомпиляции JAR | STDIO | Утилитарный |

### Технологии

- **Java:** 17
- **Spring Boot:** 3.5.5 / 4.0.2 (в example-rag)
- **Spring AI:** 1.0.3 / 2.0.0-M2
- **Maven:** 3.x
- **Тестирование:** JUnit 5, Mockito
- **Логирование:** Logbook

---

## 🏗️ Архитектура проекта

```
example-rag-by-spring-documentation/
├── pom.xml                           # Родительский POM
├── README-VADIM.md                   # Общая документация
├── .qwen/                            # Настройки Qwen Code
│   ├── settings.json
│   └── workplace/                    # Рабочее пространство (наследует правила из mcp-server-jar-unpacker)
│       ├── PROJECT_RULES.md          # Правила проекта (XP, TDD)
│       ├── TASK_INDEX.md             # Индекс задач
│       ├── task_template.md          # Шаблон задач
│       ├── to_work/                  # Активные задачи
│       └── archive/                  # Выполненные задачи
├── example-rag/                      # RAG-приложение
│   ├── pom.xml
│   └── src/main/java/rag/ai/
├── stdio-sync-mcp-server/            # STDIO сервер
│   ├── pom.xml
│   └── src/main/java/ru/mirent/stdio/
├── stdio-sync-mcp-client/            # STDIO клиент
│   ├── pom.xml
│   └── src/main/java/ru/mirent/stdio/client/
├── webmvc-sync-mcp-server/           # WebMVC сервер
│   ├── pom.xml
│   └── src/main/java/ru/mirent/webmvc/
├── webmvc-sync-mcp-client/           # WebMVC клиент
│   ├── pom.xml
│   └── src/main/java/ru/mirent/webmvc/client/
├── webflux-async-mcp-server/         # WebFlux сервер
│   ├── pom.xml
│   └── src/main/java/ru/mirent/webflux/
├── webflux-async-mcp-client/         # WebFlux клиент
│   ├── pom.xml
│   └── src/main/java/ru/mirent/webflux/client/
└── mcp-server-jar-unpacker/          # Утилита декомпиляции
    ├── pom.xml
    ├── QWEN.md                       # Детальная документация модуля
    ├── cfr-0.152.jar
    └── src/main/java/ru/mirent/
```

---

## 🔧 Конфигурация Qwen Code

### 1. Правила проекта (наследуются из mcp-server-jar-unpacker)

**Применяются ко всем модулям:**

- **TDD (Red-Green-Refactor):** Тесты пишутся до кода
- **Extreme Programming:** Простой дизайн, частые коммиты, рефакторинг
- **Именование тестов:** `given<Условие>When<Действие>Then<ОжидаемыйРезультат>()`
- **AAA Pattern:** Arrange-Act-Assert в структуре тестов
- **Задачи:** Файл задачи создаётся ДО начала работы

### 2. Рабочее пространство для всех модулей

Для работы с любым модулем проекта используйте единую структуру задач в `.qwen/workplace/`:

```bash
# Создание новой задачи для любого модуля
cp .qwen/workplace/task_template.md .qwen/workplace/to_work/TASK-XXX_описание.md
```

### 3. Контекст модулей

При работе с конкретным модулем применяйте соответствующую документацию:

| Модуль | Документация |
|--------|--------------|
| `mcp-server-jar-unpacker` | `mcp-server-jar-unpacker/QWEN.md` |
| `example-rag` | `README-VADIM.md` + код модуля |
| Остальные MCP-модули | `README-VADIM.md` + код модуля |

---

## 📝 Рабочий процесс

### Создание задачи для любого модуля

1. **Создайте файл задачи** (ДО начала работы):
```bash
cd /home/vadim/IdeaProjects/java/my-projects/ai/example-rag-by-spring-documentation
cp .qwen/workplace/task_template.md .qwen/workplace/to_work/TASK-016_описание.md
```

2. **Заполните описание:**
   - Укажите модуль, с которым работаете
   - Опишите контекст и цель
   - Добавьте критерии приёмки

3. **Обновите TASK_INDEX.md:**
   - Добавьте новую строку в таблицу
   - Установите статус 📋 Pending

4. **Перед началом работы:**
   - Установите статус 🔧 In Progress

### TDD цикл для любого модуля

```
┌─────────────┐
│     RED     │ → Пишем тест в модуле (например, webmvc-sync-mcp-server)
└──────┬──────┘
       │
       ▼
┌─────────────┐
│    GREEN    │ → Пишем минимальный код в этом же модуле
└──────┬──────┘
       │
       ▼
┌─────────────┐
│  REFACTOR   │ → Улучшаем код, проверяем все модули
└──────┬──────┘
```

### Запуск тестов для конкретного модуля

```bash
# Запуск тестов в конкретном модуле
mvn test -pl <module-name>

# Примеры:
mvn test -pl stdio-sync-mcp-server
mvn test -pl webmvc-sync-mcp-server -Dtest=WebMvcClientTest
mvn test -pl mcp-server-jar-unpacker

# Запуск тестов во всём проекте
mvn test

# Сборка конкретного модуля
mvn clean package -pl <module-name>

# Сборка всего проекта
mvn clean package
```

### Сборка с пропуском тестов (для быстрого развёртывания)

```bash
mvn clean install -pl <module-name> -DskipTests
```

---

## 🚀 Команды для работы с модулями

### example-rag (RAG-приложение)

```bash
# Запуск приложения
mvn spring-boot:run -pl example-rag

# Сборка
mvn clean package -pl example-rag
```

### STDIO MCP-сервер/клиент

```bash
# Сборка сервера
mvn clean install -pl stdio-sync-mcp-server -DskipTests

# Запуск теста-клиента
mvn test -pl stdio-sync-mcp-server -Dtest=ru.mirent.stdio.StdioClientTest

# Запуск клиента как отдельного приложения
mvn spring-boot:run -pl stdio-sync-mcp-client
```

### WebMVC MCP-сервер/клиент

```bash
# Запуск теста-клиента
mvn test -pl webmvc-sync-mcp-server -Dtest=ru.mirent.webmvc.WebMvcClientTest

# Запуск сервера с профилем stdio
mvn spring-boot:run -pl webmvc-sync-mcp-server -Dspring-boot.run.profiles=stdio
```

### WebFlux MCP-сервер/клиент

```bash
# Запуск теста-клиента
mvn test -pl webflux-async-mcp-server -Dtest=ru.mirent.webflux.WebFluxClientTest

# Запуск сервера с профилем stdio
mvn spring-boot:run -pl webflux-async-mcp-server -Dspring-boot.run.profiles=stdio
```

### mcp-server-jar-unpacker

```bash
# Сборка
mvn clean package -pl mcp-server-jar-unpacker

# Запуск сервера
java -jar mcp-server-jar-unpacker/target/mcp-server-jar-unpacker-1.0-SNAPSHOT.jar

# Запуск тестов
mvn test -pl mcp-server-jar-unpacker

# Полное тестирование инструментов (Python)
cd mcp-server-jar-unpacker && python3 test_all_tools.py
```

---

## 📊 Зависимости между модулями

```
example-rag-by-spring-documentation (pom.xml - родитель)
│
├── example-rag (Spring Boot 4.0.2, Spring AI 2.0.0-M2)
│   └── spring-ai-starter-model-ollama
│   └── spring-ai-starter-vector-store-pgvector
│
├── stdio-sync-mcp-server (Spring AI MCP Server STDIO)
│   └── spring-ai-starter-mcp-server
│
├── stdio-sync-mcp-client (Spring AI MCP Client STDIO)
│
├── webmvc-sync-mcp-server (Spring AI MCP Server WebMVC SSE)
│   └── spring-ai-starter-mcp-server-webmvc
│   └── logbook-spring-boot-starter
│
├── webmvc-sync-mcp-client (Spring AI MCP Client SSE)
│
├── webflux-async-mcp-server (Spring AI MCP Server WebFlux SSE)
│   └── spring-ai-starter-mcp-server-webflux
│
├── webflux-async-mcp-client (Spring AI MCP Client SSE)
│
└── mcp-server-jar-unpacker (Независимый MCP-сервер)
    └── cfr-0.152.jar (декомпилятор)
```

---

## 🎯 Конвенции для всех модулей

### Именование файлов задач

```
TASK-<ID>_<краткое-описание>.md
```

**Примеры:**
- `TASK-016_add_new_mcp_tool.md`
- `TASK-017_fix_webflux_async_bug.md`
- `TASK-018_update_spring_ai_version.md`

### Именование тестов

```java
// Формат: given<Условие>When<Действие>Then<ОжидаемыйРезультат>
@Test
@DisplayName("Описание сценария")
void givenValidMcpRequestWhenHandleInitializeThenReturnsCapabilities() {
    // Arrange
    // Act
    // Assert
}
```

### Структура теста (AAA Pattern)

```java
@Test
@DisplayName("Дан валидный JSON-запрос, когда инициализация, тогда возвращаем capabilities")
void givenValidJsonRequestWhenInitializeThenReturnsCapabilities() {
    // Arrange — подготовка
    McpServer server = new McpServer();
    
    // Act — действие
    JsonRpcResponse response = server.initialize(request);
    
    // Assert — проверка
    assertNotNull(response.getResult());
    assertEquals("2024-11-05", response.getResult().getProtocolVersion());
}
```

### Коммиты в Git

```bash
# Формат: "TASK-XXX: краткое описание (#N)"
git commit -m "TASK-016: добавить новый MCP-инструмент (#16)"
git commit -m "TASK-017: исправить асинхронную обработку в WebFlux (#17)"
```

### Ветвление Git

```bash
# Создание ветки для задачи
git checkout -b task/016-add-new-mcp-tool
git checkout -b task/017-fix-webflux-bug
```

---

## 🔍 Поиск и навигация по коду

### Поиск классов в модуле

```bash
# Найти все классы в модуле
find webmvc-sync-mcp-server/src -name "*.java"

# Найти конкретный класс
find . -name "*Server.java"

# Найти использование аннотации
grep -r "@RestController" webmvc-sync-mcp-server/src
```

### Поиск по кодовой базе (через Qwen Code)

Используйте инструменты:
- `glob` — поиск файлов по паттерну
- `grep_search` — поиск по содержимому файлов
- `read_file` — чтение конкретного файла
- `agent` — делегирование сложных задач поиска

---

## 📁 Управление задачами

### Просмотр активных задач

```bash
ls -la .qwen/workplace/to_work/
```

### Просмотр выполненных задач

```bash
ls -la .qwen/workplace/archive/
cat .qwen/workplace/TASK_INDEX.md
```

### Статусы задач

| Статус | Значение | Папка |
|--------|----------|-------|
| 📋 Pending | Задача запланирована | `to_work/` |
| 🔧 In Progress | Задача в работе (TDD цикл) | `to_work/` |
| 🧪 Testing | Тесты написаны, код в работе | `to_work/` |
| ✅ Done | Задача выполнена, тесты зелёные | `archive/` |

---

## 🧪 Тестирование

### Запуск всех тестов проекта

```bash
mvn clean test
```

### Запуск тестов конкретного модуля

```bash
mvn clean test -pl mcp-server-jar-unpacker
mvn clean test -pl webmvc-sync-mcp-server
```

### Запуск конкретного теста

```bash
mvn test -pl mcp-server-jar-unpacker -Dtest=JsonUtilsTest
mvn test -pl webmvc-sync-mcp-server -Dtest=WebMvcClientTest
```

### Проверка покрытия тестами (если настроен JaCoCo)

```bash
mvn jacoco:report
```

---

## 🛠️ Отладка и логирование

### Включение DEBUG-логов

Для модулей с Logbook:

```properties
# application.properties
logging.level.org.zalando.logbook=DEBUG
logging.level.ru.mirent=DEBUG
```

### Просмотр логов MCP-сервера

```bash
# Для mcp-server-jar-unpacker
tail -f mcp-server-jar-unpacker/jar-unpacker.log
```

---

## 📚 Документация по модулям

### mcp-server-jar-unpacker

Полная документация: [`mcp-server-jar-unpacker/QWEN.md`](./mcp-server-jar-unpacker/QWEN.md)

**Инструменты:**
- `find_class_in_m2` — поиск JAR по имени класса
- `get_class_outline` — схема класса
- `get_method_source` — код метода
- `decompile_class` — полная декомпиляция
- `list_classes_in_jar` — список классов в JAR
- `search_classes_by_pattern` — поиск по regex-паттерну

### Остальные модули

Документация: [`README-VADIM.md`](./README-VADIM.md)

**MCP-инструменты в серверах:**
- `echo` — тестовый инструмент
- `calculate` — вычисления
- (добавляются по мере разработки)

---

## 🎓 Best Practices

### 1. Всегда создавайте файл задачи ДО начала работы

Это позволяет продолжить с места прерывания в случае сбоя.

### 2. Следуйте TDD циклу

```
RED → GREEN → REFACTOR
```

Не пишите код без теста!

### 3. characterisation тесты для legacy-кода

Перед изменением существующего кода без тестов:
1. Напишите тест, фиксирующий текущее поведение
2. Убедитесь, что тест проходит
3. Внесите изменения
4. Проверьте регрессию

### 4. Простой дизайн (Simple Design)

- Избегайте излишней абстракции
- Называйте переменные и методы понятно
- Устраняйте дублирование (DRY)

### 5. Частые коммиты

Делайте коммиты после каждого пройденного TDD-цикла.

---

## 🔗 Полезные ссылки

- [Spring AI Documentation](https://docs.spring.io/spring-ai/docs/current/reference/html/)
- [MCP Specification](https://modelcontextprotocol.io/)
- [JUnit 5 User Guide](https://junit.org/junit5/docs/current/user-guide/)
- [Mockito Documentation](https://javadoc.io/doc/org.mockito/mockito-core/latest/)

---

## 📞 Поддержка

При возникновении вопросов:
1. Проверьте документацию модуля (`QWEN.md` или `README-VADIM.md`)
2. Посмотрите существующие задачи в `.qwen/workplace/archive/`
3. Изучите тесты для понимания контекста

---

**Версия документа:** 1.0  
**Дата создания:** 2026-03-26  
**Основано на:** `mcp-server-jar-unpacker/QWEN.md`
