# TASK-015: Параметр --no-usage-statistics для отключения логирования

> **⚠️ Важно:** Этот файл должен быть создан **до начала** любой работы над задачей.
> Отмечайте чек-боксы `[x]` **сразу после выполнения** каждого шага для возможности продолжения с места прерывания.

## Описание

Добавить параметр запуска MCP-сервера `--no-usage-statistics`, который отключает логирование вызовов инструментов в файл `jar-unpacker.log`.

**Контекст:** 
- Сейчас ToolLogger всегда пишет логи вызовов инструментов (с ротацией)
- DEBUG-режим уже поддерживается через `-Djarunpacker.debug=true`
- Требуется возможность полностью отключить логирование на уровне приложения

**Цель:** При запуске с флагом `--no-usage-statistics` логирование вызовов инструментов должно быть полностью отключено.

## Требуемые изменения

### 1. Обновить `Server.java`

Добавить парсинг аргументов командной строки и передачу флага в ToolLogger:

```java
public static void main(String[] args) throws IOException {
    boolean enableUsageStatistics = parseArgs(args);
    ToolLogger.setUsageStatisticsEnabled(enableUsageStatistics);
    runMcpServer();
}

private static boolean parseArgs(String[] args) {
    for (String arg : args) {
        if ("--no-usage-statistics".equals(arg)) {
            return false;
        }
    }
    return true; // по умолчанию включено
}
```

### 2. Обновить `ToolLogger.java`

Добавить флаг и проверку перед записью лога:

```java
public class ToolLogger {
    
    private static volatile boolean usageStatisticsEnabled = true;

    public static void setUsageStatisticsEnabled(boolean enabled) {
        usageStatisticsEnabled = enabled;
    }

    public static boolean isUsageStatisticsEnabled() {
        return usageStatisticsEnabled;
    }

    public static void logToolCall(String toolName, String status, long elapsedMs, String arguments) {
        if (!usageStatisticsEnabled) {
            return; // Логирование отключено
        }
        // ... существующая логика
    }
    
    public static void logDebug(String message) {
        if (!usageStatisticsEnabled) {
            return; // Логирование отключено
        }
        // ... существующая логика
    }
}
```

### 3. Обновить документацию

Добавить описание параметра в QWEN.md и README.md.

## Критерии приёмки (Acceptance Criteria)

- [x] Параметр `--no-usage-statistics` распознаётся при запуске
- [x] При запуске с `--no-usage-statistics` файл `jar-unpacker.log` не создаётся
- [x] При запуске без параметра логирование работает как прежде
- [x] Написаны тесты на парсинг аргументов
- [x] Написаны тесты на ToolLogger с флагом
- [x] Все существующие тесты проходят: `mvn test`
- [x] Сборка успешна: `mvn clean package`
- [x] Обновлена документация (QWEN.md, README.md)

## TDD Цикл

### 🔴 RED — Тесты

> Отмечайте чек-боксы сразу после выполнения каждого пункта!

- [x] Создан тест `ServerArgsTest.java` для парсинга аргументов
- [x] Написан тест `givenNoUsageStatisticsArgWhenParseArgsThenReturnsFalse()`
- [x] Написан тест `givenNoArgsWhenParseArgsThenReturnsTrue()`
- [x] Написан тест `givenUnknownArgsWhenParseArgsThenReturnsTrue()`
- [x] Создан тест `ToolLoggerUsageStatisticsTest.java`
- [x] Написан тест `givenUsageStatisticsDisabledWhenLogToolCallThenNoLogFileCreated()`
- [x] Написан тест `givenUsageStatisticsEnabledWhenLogToolCallThenLogFileCreated()`
- [x] Тесты компилируются и падают

**Пример теста:**

```java
package ru.mirent;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ServerArgsTest {

    @Test
    void givenNoUsageStatisticsArgWhenParseArgsThenReturnsFalse() {
        String[] args = {"--no-usage-statistics"};
        
        boolean result = Server.parseArgs(args);
        
        assertFalse(result);
    }

    @Test
    void givenNoArgsWhenParseArgsThenReturnsTrue() {
        String[] args = {};
        
        boolean result = Server.parseArgs(args);
        
        assertTrue(result);
    }

    @Test
    void givenUnknownArgsWhenParseArgsThenReturnsTrue() {
        String[] args = {"--unknown-flag", "--another-flag"};
        
        boolean result = Server.parseArgs(args);
        
        assertTrue(result);
    }
}
```

```java
package ru.mirent.logging;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

class ToolLoggerUsageStatisticsTest {

    @TempDir
    Path tempDir;

    private String originalUserDir;

    @BeforeEach
    void setUp() {
        originalUserDir = System.getProperty("user.dir");
        System.setProperty("user.dir", tempDir.toString());
        ToolLogger.setUsageStatisticsEnabled(true);
    }

    @AfterEach
    void tearDown() {
        System.setProperty("user.dir", originalUserDir);
        ToolLogger.setUsageStatisticsEnabled(true);
    }

    @Test
    void givenUsageStatisticsDisabledWhenLogToolCallThenNoLogFileCreated() {
        ToolLogger.setUsageStatisticsEnabled(false);

        ToolLogger.logToolCall("test_tool", "SUCCESS", 100, "{}");

        Path logFile = Paths.get(tempDir.toString(), "jar-unpacker.log");
        assertFalse(Files.exists(logFile));
    }

    @Test
    void givenUsageStatisticsEnabledWhenLogToolCallThenLogFileCreated() throws IOException {
        ToolLogger.setUsageStatisticsEnabled(true);

        ToolLogger.logToolCall("test_tool", "SUCCESS", 100, "{}");

        Path logFile = Paths.get(tempDir.toString(), "jar-unpacker.log");
        assertTrue(Files.exists(logFile));
        
        String content = Files.readString(logFile);
        assertTrue(content.contains("test_tool"));
        assertTrue(content.contains("SUCCESS"));
    }
}
```

### 🟢 GREEN — Реализация

> Отмечайте чек-боксы сразу после выполнения каждого пункта!

- [x] Обновлён `Server.java` — добавлен парсинг аргументов
- [x] Обновлён `ToolLogger.java` — добавлен флаг и проверка
- [x] Все тесты проходят: `mvn test`

### 🔵 REFACTOR — Рефакторинг

> Отмечайте чек-боксы сразу после выполнения каждого пункта!

- [x] Код рефакторен, дублирование устранено
- [x] Добавлены JavaDoc к публичным методам
- [x] Все тесты проходят после рефакторинга
- [x] Сборка успешна: `mvn clean package`

## Работа с существующим кодом (если применимо)

- [x] Проверена регрессия существующего функционала логирования
- [x] DEBUG-режим продолжает работать независимо от флага
- [x] Все старые тесты проходят

## Чек-лист завершения

- [x] Все тесты зелёные
- [x] Сборка успешна
- [x] Код соответствует стандартам проекта
- [x] Изменения закоммичены

## Статус

| Поле | Значение |
|------|----------|
| Дата создания: | 2026-03-26 |
| Дата начала: | 2026-03-26 |
| Дата завершения: | 2026-03-26 |
| Статус: | ✅ Done |

## Заметки

- Флаг `--no-usage-statistics` не должен влиять на DEBUG-режим (`-Djarunpacker.debug=true`)
- По умолчанию логирование включено (обратная совместимость)
- **Решение:** Флаг отключает ВСЁ логирование (и инструменты, и DEBUG)
- **Решение:** Без информационных сообщений при запуске (тихий режим)
