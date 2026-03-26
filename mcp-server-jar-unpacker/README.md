# MCP Server JAR Unpacker

[![Java 17](https://img.shields.io/badge/Java-17-blue.svg)](https://openjdk.java.net/)
[![Maven](https://img.shields.io/badge/Maven-3.6+-orange.svg)](https://maven.apache.org/)
[![MCP](https://img.shields.io/badge/MCP-Server-green.svg)](https://modelcontextprotocol.io/)

MCP-сервер для поиска, анализа и декомпиляции Java-классов в JAR-файлах локального Maven-репозитория.

## 📋 Возможности

Сервер предоставляет **6 инструментов** через MCP:

| Инструмент | Описание |
|------------|----------|
| `find_class_in_m2` | Поиск JAR-файлов по имени класса |
| `get_class_outline` | Схема класса (пакет, импорты, поля, сигнатуры методов) |
| `get_method_source` | Исходный код конкретного метода |
| `decompile_class` | Полная декомпиляция через CFR |
| `list_classes_in_jar` | Список всех классов в JAR-файле |
| `search_classes_by_pattern` | Поиск классов по regex-паттерну |

## 🚀 Быстрый старт

### Требования

- Java 17+
- Maven 3.6+
- Файл `cfr-0.152.jar` в корне проекта

### Сборка

```bash
mvn clean package
```

### Запуск

```bash
java -jar target/mcp-server-jar-unpacker-1.0-SNAPSHOT.jar
```

**Параметры запуска:**

| Параметр | Описание |
|----------|----------|
| `--no-usage-statistics` | Отключить логирование вызовов инструментов |

**Пример:**
```bash
java -jar target/mcp-server-jar-unpacker-1.0-SNAPSHOT.jar --no-usage-statistics
```

## 🔌 Подключение

### Через MCP Inspector

```bash
npx @modelcontextprotocol/inspector java -jar target/mcp-server-jar-unpacker-1.0-SNAPSHOT.jar
```

### Claude Desktop

Добавьте в `claude_desktop_config.json`:

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
      "cwd": "/absolute/path/to/mcp-server-jar-unpacker"
    }
  }
}
```

### Через SDK

```typescript
import { Client } from '@modelcontextprotocol/sdk/client/index.js';
import { StdioClientTransport } from '@modelcontextprotocol/sdk/client/stdio.js';

const transport = new StdioClientTransport({
  command: 'java',
  args: ['-jar', 'target/mcp-server-jar-unpacker-1.0-SNAPSHOT.jar'],
  cwd: '/path/to/project'
});

const client = new Client({ name: 'my-client', version: '1.0.0' });
await client.connect(transport);
```

## 📖 Примеры использования

### 1. Поиск класса

```json
{
  "name": "find_class_in_m2",
  "arguments": {"class_name": "com.google.common.base.Preconditions"}
}
```

### 2. Получение схемы класса

```json
{
  "name": "get_class_outline",
  "arguments": {
    "jar_path": "/home/user/.m2/repository/com/google/guava/guava/33.4.0-jre/guava-33.4.0-jre.jar",
    "class_fqn": "com.google.common.base.Preconditions"
  }
}
```

### 3. Поиск по паттерну

```json
{
  "name": "search_classes_by_pattern",
  "arguments": {"pattern": ".*Controller$"}
}
```

### 4. Список классов в JAR

```json
{
  "name": "list_classes_in_jar",
  "arguments": {
    "jar_path": "/home/user/.m2/repository/org/springframework/spring-web/6.0.0/spring-web-6.0.0.jar"
  }
}
```

## 🏗️ Архитектура

```
src/main/java/ru/mirent/
├── Server.java                 # Основной класс MCP-сервера
├── JsonRpcHandler.java         # Обработка JSON-RPC запросов
├── DefaultToolRegistry.java    # Реестр инструментов
├── ToolRegistry.java           # Интерфейс реестра
├── Tool.java                   # Интерфейс инструмента
├── tools/
│   ├── AbstractTool.java
│   ├── FindClassTool.java
│   ├── GetClassOutlineTool.java
│   ├── GetMethodSourceTool.java
│   ├── DecompileClassTool.java
│   ├── ListClassesInJarTool.java
│   └── SearchClassesByPatternTool.java
└── services/
    ├── JarCacheService.java    # Кэширование JAR-файлов
    ├── JarSearchService.java   # Поиск классов в JAR
    └── DecompilationService.java # Декомпиляция через CFR
```

## 🧪 Тестирование

### Запуск всех тестов

```bash
mvn test
```

**Статистика:** 209 тестов (13 интеграционных пропускаются)

### Python-скрипты для тестирования

```bash
# Базовый тест протокола
python3 test_mcp_server.py

# Полное тестирование всех инструментов
python3 test_all_tools.py
```

## 📊 Характеристики

| Параметр | Значение |
|----------|----------|
| Maven-репозиторий | `~/.m2/repository` |
| Выходная директория | `/tmp/cfr-decompiled` |
| Таймаут декомпиляции | 60 секунд |
| Макс. потоков | min(16, availableProcessors) |
| Логирование | `jar-unpacker.log` (10 МБ, 3 архива) |

## 🔧 Конфигурация

### Переменные окружения

| Переменная | Описание | По умолчанию |
|------------|----------|--------------|
| `M2_REPO` | Путь к Maven-репозиторию | `~/.m2/repository` |
| `CFR_PATH` | Путь к CFR JAR | `cfr-0.152.jar` |
| `OUTPUT_DIR` | Директория декомпиляции | `/tmp/cfr-decompiled` |

## 📝 Логирование

Сервер автоматически ведёт лог вызовов инструментов в файл `jar-unpacker.log`:

```
2026-03-26T10:15:30.123Z | find_class_in_m2 | SUCCESS | 245ms | arguments={"class_name":"..."}
2026-03-26T10:15:31.456Z | get_class_outline | SUCCESS | 89ms | arguments={"jar_path":"..."}
2026-03-26T10:15:32.789Z | get_method_source | ERROR: Method not found | 12ms | arguments={...}
```

## 🛠️ Разработка

### Структура проекта

```
mcp-server-jar-unpacker/
├── pom.xml
├── cfr-0.152.jar
├── QWEN.md              # Полная документация
├── README.md            # Этот файл
├── test_mcp_server.py   # Базовый тест
├── test_all_tools.py    # Полное тестирование
├── .qwen/
│   ├── settings.json
│   └── workplace/              # Рабочее пространство задач
│       ├── TASK_INDEX.md       # Индекс задач
│       ├── PROJECT_RULES.md    # Правила проекта
│       ├── to_work/            # Активные задачи
│       └── archive/            # Выполненные задачи
└── src/
    ├── main/java/ru/mirent/
    └── test/java/ru/mirent/
```

### Практики

- **TDD:** Красный → Зелёный → Рефакторинг
- **Extreme Programming:** Непрерывная интеграция, простой дизайн
- **Git:** Ветка `task/XXX-<name>`, коммиты `TASK-XXX: описание`

### Выполненные задачи

Все 15 задач выполнены и находятся в архиве:
- TASK-001: Реализация тестов для Server.java с Mockito
- TASK-002: Логирование вызовов инструментов MCP-сервера
- TASK-003: Разделение Server.java на модули: JsonRpcHandler
- TASK-004: Разделение Server.java на модули: Tool-классы
- TASK-005: Разделение Server.java на модули: Services
- TASK-006: Валидация путей: защита от path traversal
- TASK-007: Валидация FQN класса: защита от инъекций
- TASK-008: Умное кэширование JAR с TTL
- TASK-009: Инвалидация кэша при изменении ~/.m2
- TASK-010: Интеграционные тесты с реальными JAR
- TASK-011: Characterization tests для legacy-кода
- TASK-012: Улучшение логирования: DEBUG-режим
- TASK-013: Новый инструмент: list_classes_in_jar
- TASK-014: Новый инструмент: search_classes_by_pattern
- TASK-015: Параметр --no-usage-statistics для отключения логов

## 📦 Зависимости

| Зависимость | Версия | Описание |
|-------------|--------|----------|
| JUnit Jupiter | 5.9.2 | Тестирование |
| CFR | 0.152 | Декомпилятор |

## 📄 Лицензия

MIT

## 🔗 Ссылки

- [MCP Protocol](https://modelcontextprotocol.io/)
- [CFR Decompiler](https://www.benf.org/other/cfr/)
- [Maven Repository](https://mvnrepository.com/)
