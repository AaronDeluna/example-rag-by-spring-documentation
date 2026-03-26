# MCP Server JAR Unpacker

## Project Overview

**MCP Server JAR Unpacker** — это MCP (Model Context Protocol) сервер, предоставляющий инструменты для поиска, анализа и декомпиляции Java-классов внутри JAR-файлов в локальном Maven-репозитории (`~/.m2/repository`).

### Основное назначение

Сервер позволяет:
- Искать Java-классы по имени в JAR-файлах Maven-репозитория
- Получать краткую схему класса (пакет, импорты, поля, сигнатуры методов) без тел методов
- Извлекать исходный код конкретных методов
- Декомпилировать полные исходники классов с помощью CFR

### Технологии

- **Язык:** Java 17
- **Сборка:** Maven
- **Тестирование:** JUnit 5 (junit-jupiter 5.9.2)
- **Декомпилятор:** CFR (cfr-0.152.jar)
- **Протокол:** MCP (Model Context Protocol) через JSON-RPC 2.0

### Архитектура

```
src/main/java/ru/mirent/
├── Server.java                 # Основной класс MCP-сервера
├── JsonRpcHandler.java         # Обработка JSON-RPC запросов
├── DefaultToolRegistry.java    # Реестр инструментов
├── ToolRegistry.java           # Интерфейс реестра
├── Tool.java                   # Интерфейс инструмента
├── tools/                      # Инструменты MCP
│   ├── AbstractTool.java       # Базовый класс инструмента
│   ├── FindClassTool.java
│   ├── GetClassOutlineTool.java
│   ├── GetMethodSourceTool.java
│   ├── DecompileClassTool.java
│   ├── ListClassesInJarTool.java
│   └── SearchClassesByPatternTool.java
└── services/                   # Сервисы бизнес-логики
    ├── JarCacheService.java    # Кэширование JAR-файлов
    ├── JarSearchService.java   # Поиск классов в JAR
    └── DecompilationService.java # Декомпиляция через CFR
```

**Ключевые компоненты:**

**Server.java:**
- `runMcpServer()` — основной цикл обработки JSON-RPC запросов
- `main()` — точка входа

**JsonRpcHandler.java:**
- `handleInitialize()` — инициализация MCP-протокола
- `handleToolsList()` — получение списка инструментов
- `handleToolsCall()` — вызов инструмента

**Tool-классы:**
- `FindClassTool` — поиск JAR по имени класса
- `GetClassOutlineTool` — схема класса
- `GetMethodSourceTool` — код метода
- `DecompileClassTool` — полная декомпиляция
- `ListClassesInJarTool` — список классов в JAR
- `SearchClassesByPatternTool` — поиск по regex-паттерну

**Services:**
- `JarCacheService` — кэширование списка JAR с TTL и инвалидацией
- `JarSearchService` — многопоточный поиск классов в JAR
- `DecompilationService` — декомпиляция через CFR с таймаутом

## Building and Running

### Сборка проекта

```bash
mvn clean package
```

### Запуск сервера

```bash
java -jar target/mcp-server-jar-unpacker-1.0-SNAPSHOT.jar
```

**Параметры запуска:**

| Параметр | Описание |
|----------|----------|
| `--no-usage-statistics` | Отключить логирование вызовов инструментов в `jar-unpacker.log` |

**Пример:**
```bash
java -jar target/mcp-server-jar-unpacker-1.0-SNAPSHOT.jar --no-usage-statistics
```

**Важно:** Файл `cfr-0.152.jar` должен находиться в той же директории, что и запускаемый JAR, либо в корне проекта.

### Запуск тестов

```bash
mvn test
```

### Конфигурация

- **Maven-репозиторий:** `~/.m2/repository` (автоматически определяется)
- **Выходная директория декомпиляции:** `/tmp/cfr-decompiled`
- **Таймаут декомпиляции:** 60 секунд
- **Максимальное количество потоков:** min(16, availableProcessors)

### Характеристики

| Параметр | Значение |
|----------|----------|
| Maven-репозиторий | `~/.m2/repository` |
| Выходная директория | `/tmp/cfr-decompiled` |
| Таймаут декомпиляции | 60 секунд |
| Макс. потоков | min(16, availableProcessors) |
| Логирование | `jar-unpacker.log` (10 МБ, 3 архива) |
| Исключаются из поиска | `-sources.jar`, `-javadoc.jar` |
| Кэширование | TTL + инвалидация при изменении ~/.m2 |

### Логирование

Сервер автоматически ведёт лог всех вызовов инструментов в файл `jar-unpacker.log` в рабочей директории.

**Формат записи лога:**
```
<timestamp> | <instrument_name> | <status> | <elapsed_ms> | arguments=<json_arguments>
```

**Пример:**
```
2026-03-26T10:15:30.123Z | find_class_in_m2 | SUCCESS | 245ms | arguments={"class_name":"com.example.MyClass"}
2026-03-26T10:15:31.456Z | get_class_outline | SUCCESS | 89ms | arguments={"jar_path":"...","class_fqn":"..."}
2026-03-26T10:15:32.789Z | get_method_source | ERROR: Method not found | 12ms | arguments={"jar_path":"...","class_fqn":"...","method_name":"..."}
```

**Ротация логов:**
- Максимальный размер файла: 10 МБ
- Количество архивных файлов: 3 (`.log.1`, `.log.2`, `.log.3`)
- При достижении лимита текущий лог переименовывается в `.log.1`, старые сдвигаются

## Подключение MCP-сервера

### Подключение через MCP Inspector

MCP Inspector — это официальный инструмент для тестирования и отладки MCP-серверов.

```bash
npx @modelcontextprotocol/inspector java -jar target/mcp-server-jar-unpacker-1.0-SNAPSHOT.jar
```

Или с указанием пути к CFR:

```bash
cd /path/to/mcp-server-jar-unpacker
npx @modelcontextprotocol/inspector java -jar target/mcp-server-jar-unpacker-1.0-SNAPSHOT.jar
```

### Подключение к Claude Desktop

Добавьте конфигурацию в файл `claude_desktop_config.json`:

**macOS:** `~/Library/Application Support/Claude/claude_desktop_config.json`  
**Windows:** `%APPDATA%\Claude\claude_desktop_config.json`

```json
{
  "mcpServers": {
    "jar-unpacker": {
      "command": "java",
      "args": [
        "-jar",
        "/absolute/path/to/mcp-server-jar-unpacker/target/mcp-server-jar-unpacker-1.0-SNAPSHOT.jar"
      ],
      "env": {},
      "cwd": "/absolute/path/to/mcp-server-jar-unpacker"
    }
  }
}
```

**Важно:**
- Укажите полный путь до JAR-файла в `args`
- Укажите рабочую директорию проекта в `cwd` (для доступа к `cfr-0.152.jar`)
- Перезапустите Claude Desktop после изменения конфигурации

### Подключение через MCP SDK (для разработчиков)

```typescript
import { Client } from '@modelcontextprotocol/sdk/client/index.js';
import { StdioClientTransport } from '@modelcontextprotocol/sdk/client/stdio.js';

const transport = new StdioClientTransport({
  command: 'java',
  args: ['-jar', '/path/to/mcp-server-jar-unpacker-1.0-SNAPSHOT.jar'],
  cwd: '/path/to/mcp-server-jar-unpacker'
});

const client = new Client({
  name: 'my-mcp-client',
  version: '1.0.0'
});

await client.connect(transport);
```

### Проверка подключения

После подключения сервер должен предоставить 4 инструмента:
- `find_class_in_m2`
- `get_class_outline`
- `get_method_source`
- `decompile_class`

Убедитесь, что:
1. Файл `cfr-0.152.jar` доступен в рабочей директории
2. Пути в конфигурации абсолютные
3. Java 17+ установлена и доступна в PATH

## Доступные инструменты (Tools)

Сервер предоставляет 6 инструментов через MCP:

| Инструмент | Описание |
|------------|----------|
| `find_class_in_m2` | Поиск JAR-файлов, содержащих указанный класс |
| `get_class_outline` | Получение схемы класса (пакет, импорты, поля, сигнатуры методов) |
| `get_method_source` | Извлечение исходного кода конкретного метода |
| `decompile_class` | Полная декомпиляция класса через CFR |
| `list_classes_in_jar` | Список всех .class файлов в указанном JAR |
| `search_classes_by_pattern` | Поиск классов по regex-паттерну во всех JAR Maven-репозитория |

### Рекомендуемый рабочий процесс

1. `find_class_in_m2` → найти JAR с нужным классом
2. `get_class_outline` → получить схему класса (экономия токенов)
3. `get_method_source` → извлечь код конкретных методов
4. `decompile_class` → только если нужен полный исходник
5. `search_classes_by_pattern` → найти классы по шаблону (например, все *Controller, *Template)

## Тестирование MCP-сервера

### Быстрая проверка через командную строку

Проверка списка доступных инструментов:

```bash
cd /path/to/mcp-server-jar-unpacker && echo '{"jsonrpc":"2.0","id":1,"method":"tools/list","params":{}}' | timeout 5 java -jar target/mcp-server-jar-unpacker-1.0-SNAPSHOT.jar
```

Ожидаемый результат — JSON-ответ со списком из 6 инструментов:
- `find_class_in_m2`
- `get_class_outline`
- `get_method_source`
- `decompile_class`
- `list_classes_in_jar`
- `search_classes_by_pattern`

### Автоматизированное тестирование через Python-скрипт

В проекте доступен тестовый скрипт `.qwen/workplace/test_mcp_server.py`, который проверяет:
- Инициализацию MCP-протокола (`initialize`)
- Получение списка инструментов (`tools/list`)
- Вызов инструмента поиска классов (`find_class_in_m2`)

**Запуск тестов:**

```bash
cd /path/to/mcp-server-jar-unpacker
python3 .qwen/workplace/test_mcp_server.py
```

**Структура тестов:**

| Тест | Описание |
|------|----------|
| `test_initialize()` | Проверка инициализации MCP-протокола |
| `test_list_tools()` | Получение списка доступных инструментов |
| `test_find_class()` | Поиск класса `com.google.common.base.Preconditions` |

**Требования:**
- Python 3.6+
- Собранный JAR-файл (`mvn clean package`)
- Java 17+

### Полное тестирование всех инструментов

Скрипт `test_all_tools.py` последовательно вызывает все 6 инструментов MCP-сервера и проверяет их работоспособность.

**Запуск:**

```bash
cd /path/to/mcp-server-jar-unpacker
python3 test_all_tools.py
```

**Что проверяет:**

| Шаг | Инструмент | Описание |
|-----|------------|----------|
| 1 | `initialize` | Инициализация MCP-протокола |
| 2 | `tools/list` | Получение списка доступных инструментов |
| 3 | `find_class_in_m2` | Поиск JAR-файлов с классом `com.google.common.base.Preconditions` |
| 4 | `get_class_outline` | Получение схемы класса из Guava 33.4.0-jre |
| 5 | `get_method_source` | Извлечение кода метода `checkNotNull` |
| 6 | `decompile_class` | Полная декомпиляция класса через CFR |
| 7 | `list_classes_in_jar` | Список классов в JAR-файле |
| 8 | `search_classes_by_pattern` | Поиск классов по паттерну (например, .*Template.*) |

**Статистика тестирования:**
- Всего тестов: 209
- Пропущено (интеграционные): 13
- Все тесты проходят успешно

**Требования:**
- Python 3.6+
- Собранный JAR-файл (`mvn clean package`)
- Java 17+
- Файл `cfr-0.152.jar` в корне проекта

**Пример вывода:**
```
============================================================
MCP Server JAR Unpacker - Полное тестирование инструментов
============================================================
✓ MCP-сервер запущен: target/mcp-server-jar-unpacker-1.0-SNAPSHOT.jar

============================================================
1. Инициализация MCP-протокола
============================================================
✓ Инициализация успешна
  Protocol: 2024-11-05
  Server: jar-unpacker

============================================================
2. Получение списка инструментов
============================================================
✓ Найдено инструментов: 6
  - find_class_in_m2: Поиск Java-класса внутри JAR-файлов...
  - get_class_outline: Получить краткую схему Java-класса...
  - get_method_source: Извлечь исходный код конкретного метода...
  - decompile_class: Вернуть полный декомпилированный исходник...
  - list_classes_in_jar: Список всех .class файлов в указанном JAR...
  - search_classes_by_pattern: Поиск Java-классов по regex-паттерну...

============================================================
3. Инструмент: find_class_in_m2
============================================================
✓ Результат поиска:
  Найдено JAR-файлов: 134 для Preconditions.class:
  /home/vadim/.m2/repository/com/google/guava/guava/33.4.0-jre/guava-33.4.0-jre.jar
  ...

============================================================
4. Инструмент: get_class_outline
============================================================
✓ Схема класса:
  /*
   * Decompiled with CFR 0.152.
   */
  package com.google.common.base;
  ...

============================================================
5. Инструмент: get_method_source
============================================================
✓ Исходный код метода:
  public static <T> T checkNotNull(@CheckForNull T reference) {
  ...

============================================================
6. Инструмент: decompile_class
============================================================
✓ Декомпилированный класс:
  // com.google.common.base.Preconditions
  // JAR: guava-33.4.0-jre.jar
  ...

============================================================
✓ Все инструменты успешно протестированы!
============================================================
```

### Примеры JSON-RPC запросов

**Инициализация:**
```json
{"jsonrpc":"2.0","id":0,"method":"initialize","params":{"protocolVersion":"2024-11-05","capabilities":{},"clientInfo":{"name":"test-client","version":"1.0.0"}}}
```

**Получение списка инструментов:**
```json
{"jsonrpc":"2.0","id":1,"method":"tools/list","params":{}}
```

**Вызов инструмента:**
```json
{"jsonrpc":"2.0","id":2,"method":"tools/call","params":{"name":"find_class_in_m2","arguments":{"class_name":"org.example.MyClass"}}}
```

## Development Conventions

### Структура кода

- Основной сервер реализован в одном файле `Server.java` с вложенными статическими классами
- JSON-утилиты (`JsonUtils`, `Gson`) реализованы вручную без внешних зависимостей (кроме CFR)
- Используется многопоточность для ускорения поиска по JAR-файлам

### Тестирование

- Тесты написаны на JUnit 5
- Используется `@Nested` для группировки тестов по функциональности
- Приватные методы тестируются через reflection
- Покрыты тестами: `toJson()`, `parse()`, `extractJsonObject()`, `extractJsonArray()`, `parseJsonObject()`, `parseJsonArray()`, `escapeJson()`

### Стиль кода

- Имена классов/методов на английском
- Сообщения об ошибках и описания инструментов на русском
- Обработка исключений с заглушками в критических секциях (многопоточный поиск)

## Файловая структура проекта

```
mcp-server-jar-unpacker/
├── pom.xml                           # Maven конфигурация
├── cfr-0.152.jar                     # Декомпилятор CFR (внешняя зависимость)
├── QWEN.md                           # Полная документация проекта
├── README.md                         # Краткая документация (для GitHub)
├── .gitignore                        # Git ignore правила
├── jar-unpacker.log                  # Файл лога вызовов инструментов
├── test_all_tools.py                 # Скрипт полного тестирования всех инструментов
├── test_mcp_server.py                # Базовый тест MCP-протокола
├── .qwen/                        # Директория настроек и задач
│   ├── settings.json
│   └── workplace/                # Рабочее пространство задач
│       ├── TASK_INDEX.md         # Индекс всех задач с таблицей статусов
│       ├── PROJECT_RULES.md      # Правила проекта (XP, TDD)
│       ├── task_template.md      # Шаблон для новых задач
│       ├── to_work/              # Активные задачи
│       └── archive/              # Выполненные задачи
├── src/
│   ├── main/java/ru/mirent/
│   │   ├── Server.java               # Основной класс MCP-сервера
│   │   ├── JsonRpcHandler.java       # Обработка JSON-RPC запросов
│   │   ├── DefaultToolRegistry.java  # Реестр инструментов
│   │   ├── ToolRegistry.java         # Интерфейс реестра
│   │   ├── Tool.java                 # Интерфейс инструмента
│   │   ├── tools/                    # Инструменты MCP
│   │   └── services/                 # Сервисы бизнес-логики
│   ├── main/resources/               # Ресурсы приложения
│   └── test/java/ru/mirent/          # Тесты (JUnit 5)
└── target/                           # Выходная директория сборки
```

## Примечания

- Поддержка числовых и строковых `id` в JSON-RPC сообщениях
- Многопоточный поиск с ограничением 16 потоков
- Автоматическая ротация логов (10 МБ, 3 архива)
- Кэширование JAR с TTL и инвалидацией при изменении ~/.m2

## Выполненные задачи

Все 15 задач выполнены и заархивированы:

| ID  | Название | Файл |
|-----|----------|------|
| 001 | Реализация тестов для Server.java с Mockito | TASK_001_tests_for_server.md |
| 002 | Логирование вызовов инструментов MCP-сервера | TASK_002_logging_task.md |
| 003 | Разделение Server.java на модули: JsonRpcHandler | TASK-003_json_rpc_handler.md |
| 004 | Разделение Server.java на модули: Tool-классы | TASK-004_tool_classes.md |
| 005 | Разделение Server.java на модули: Services | TASK-005_services.md |
| 006 | Валидация путей: защита от path traversal | TASK-006_path_validation.md |
| 007 | Валидация FQN класса: защита от инъекций | TASK-007_fqn_validation.md |
| 008 | Умное кэширование JAR с TTL | TASK-008_cache_ttl.md |
| 009 | Инвалидация кэша при изменении ~/.m2 | TASK-009_cache_invalidation.md |
| 010 | Интеграционные тесты с реальными JAR | TASK-010_integration_tests.md |
| 011 | Characterization tests для legacy-кода | TASK-011_characterization_tests.md |
| 012 | Улучшение логирования: DEBUG-режим | TASK-012_logging_improvements.md |
| 013 | Новый инструмент: list_classes_in_jar | TASK-013_list_classes_tool.md |
| 014 | Новый инструмент: search_classes_by_pattern | TASK-014_search_pattern_tool.md |
| 015 | Параметр --no-usage-statistics для отключения логов | TASK-015_no_usage_statistics.md |

## Qwen Added Memories
- В проекте mcp-server-jar-unpacker автоматически применяю правила из .qwen/workplace/PROJECT_RULES.md: TDD (Red-Green-Refactor), именование тестов given-when-then CamelCase, AAA Pattern, практики Extreme Programming
