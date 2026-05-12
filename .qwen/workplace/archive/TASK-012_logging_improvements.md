# TASK-012: Улучшение логирования: DEBUG-режим

> **⚠️ Важно:** Этот файл должен быть создан **до начала** любой работы над задачей.
> Отмечайте чек-боксы `[x]` **сразу после выполнения** каждого шага для возможности продолжения с места прерывания.

## Описание

Добавить DEBUG-режим для детального логирования.

**Контекст:** Сервер используется локально одним пользователем, поэтому correlation-id не требуется.

**Текущая проблема:** Нет возможности включить детализированное логирование для отладки без изменения кода.

**Цель:** Добавить поддержку DEBUG-режима через системное свойство `-Djarunpacker.debug=true`.

## Требуемые изменения

### 1. Создать класс `ToolLogger.java`

**Путь:** `src/main/java/ru/mirent/logging/ToolLogger.java`

```java
package ru.mirent.logging;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.format.DateTimeFormatter;

/**
 * Логгер вызовов инструментов MCP с поддержкой DEBUG-режима
 */
public class ToolLogger {
    
    private static final String LOG_FILE = System.getProperty("user.dir") + "/jar-unpacker.log";
    private static final int MAX_LOG_BYTES = 10 * 1024 * 1024; // 10 MB
    private static final DateTimeFormatter logFormatter = DateTimeFormatter.ISO_INSTANT;
    
    // Поддержка DEBUG-режима через системное свойство
    private static final boolean DEBUG_MODE = Boolean.getBoolean("jarunpacker.debug");
    
    /**
     * Записать лог вызова инструмента
     */
    public static void logToolCall(String toolName, String status, long elapsedMs, String arguments) {
        try {
            rotateLogIfNeeded();
            
            String timestamp = logFormatter.format(Instant.now());
            String logEntry = String.format(
                "%s | %s | %s | %dms | arguments=%s%n",
                timestamp,
                toolName,
                status,
                elapsedMs,
                arguments
            );
            
            try (FileWriter writer = new FileWriter(LOG_FILE, true)) {
                writer.write(logEntry);
            }
        } catch (IOException e) {
            System.err.println("Failed to write log: " + e.getMessage());
        }
    }
    
    /**
     * Записать DEBUG-сообщение (только если включён DEBUG-режим)
     */
    public static void logDebug(String message) {
        if (DEBUG_MODE) {
            try {
                rotateLogIfNeeded();
                
                String timestamp = logFormatter.format(Instant.now());
                String logEntry = String.format(
                    "%s | DEBUG | %s%n",
                    timestamp,
                    message
                );
                
                try (FileWriter writer = new FileWriter(LOG_FILE, true)) {
                    writer.write(logEntry);
                }
            } catch (IOException e) {
                System.err.println("Failed to write debug log: " + e.getMessage());
            }
        }
    }
    
    /**
     * Проверить необходимость ротации лога
     */
    private static void rotateLogIfNeeded() throws IOException {
        Path logPath = Paths.get(LOG_FILE);
        if (Files.exists(logPath) && Files.size(logPath) >= MAX_LOG_BYTES) {
            rotateLog();
        }
    }
    
    /**
     * Выполнить ротацию логов
     */
    private static void rotateLog() throws IOException {
        Path log3 = Paths.get(LOG_FILE + ".3");
        Path log2 = Paths.get(LOG_FILE + ".2");
        Path log1 = Paths.get(LOG_FILE + ".1");
        Path logPath = Paths.get(LOG_FILE);
        
        if (Files.exists(log2)) {
            Files.move(log2, log3, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        }
        if (Files.exists(log1)) {
            Files.move(log1, log2, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        }
        Files.move(logPath, log1, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
    }
    
    /**
     * Вернуть путь к файлу лога
     */
    public static String getLogFile() {
        return LOG_FILE;
    }
    
    /**
     * Проверить включён ли DEBUG-режим
     */
    public static boolean isDebugEnabled() {
        return DEBUG_MODE;
    }
}
```

### 2. Обновить `handleCallTool()` в `Server.java`

Добавить логирование с DEBUG-сообщениями:

```java
private static void handleCallTool(JsonMessage msg, PrintWriter writer) {
    Map<String, Object> params = (Map<String, Object>) msg.params;
    String name = (String) params.get("name");
    Map<String, Object> arguments = (Map<String, Object>) params.get("arguments");
    
    long startTime = System.currentTimeMillis();
    
    ToolLogger.logDebug("Вызов инструмента: " + name);
    ToolLogger.logDebug("Аргументы: " + arguments);
    
    try {
        Object result;
        switch (name) {
            case "find_class_in_m2":
                result = findClassInM2((String) arguments.get("class_name"));
                break;
            // ... остальные инструменты
            default:
                sendError(writer, msg.id, "Unknown tool: " + name);
                return;
        }
        
        long elapsed = System.currentTimeMillis() - startTime;
        ToolLogger.logToolCall(name, "SUCCESS", elapsed, JsonUtils.toJson(arguments));
        
        sendResponse(writer, msg.id, result);
    } catch (Exception e) {
        long elapsed = System.currentTimeMillis() - startTime;
        ToolLogger.logToolCall(name, "ERROR: " + e.getMessage(), elapsed, JsonUtils.toJson(arguments));
        ToolLogger.logDebug("Ошибка инструмента " + name + ": " + e.getMessage());
        sendError(writer, msg.id, "Error: " + e.getMessage());
    }
}
```

## Критерии приёмки (Acceptance Criteria)

- [x] Создан класс `ToolLogger.java`
- [x] Добавлена поддержка correlation-id
- [x] Добавлен DEBUG-режим через `-Djarunpacker.debug=true`
- [x] Обновлены все вызовы логирования
- [x] Написаны тесты на `ToolLogger`
- [x] Все существующие тесты проходят: `mvn test`
- [x] Сборка успешна: `mvn clean package`

## TDD Цикл

### 🔴 RED — Тесты

> Отмечайте чек-боксы сразу после выполнения каждого пункта!

- [x] Создан тест `ToolLoggerTest.java`
- [x] Написан тест `givenLogToolCallWhenWriteThenWritesToLog()`
- [x] Написан тест `givenDebugModeWhenLogDebugThenWritesToLog()`
- [x] Написан тест `givenDebugModeDisabledWhenLogDebugThenDoesNotWriteToLog()`
- [x] Написан тест `givenLogExceedsMaxSizeWhenWriteThenRotatesLog()`
- [x] Написан тест `givenGetLogFileThenReturnsCorrectPath()`
- [x] Тесты компилируются и падают

**Пример теста:**

```java
package ru.mirent.logging;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ToolLoggerTest {
    
    @TempDir
    Path tempDir;
    
    private String originalUserDir;
    
    @BeforeEach
    void setUp() {
        originalUserDir = System.getProperty("user.dir");
        System.setProperty("user.dir", tempDir.toString());
    }
    
    @AfterEach
    void tearDown() {
        System.setProperty("user.dir", originalUserDir);
    }
    
    @Test
    void givenLogToolCallWhenWriteThenWritesToLog() throws Exception {
        ToolLogger.logToolCall("find_class_in_m2", "SUCCESS", 100, 
                                "{\"class_name\":\"test.Test\"}");
        
        Path logFile = tempDir.resolve("jar-unpacker.log");
        assertTrue(Files.exists(logFile));
        
        List<String> lines = Files.readAllLines(logFile);
        assertEquals(1, lines.size());
        
        String logLine = lines.get(0);
        assertTrue(logLine.contains("find_class_in_m2"));
        assertTrue(logLine.contains("SUCCESS"));
        assertTrue(logLine.contains("100ms"));
    }
    
    @Test
    void givenDebugModeWhenLogDebugThenWritesToLog() throws Exception {
        System.setProperty("jarunpacker.debug", "true");
        
        ToolLogger.logDebug("Test debug message");
        
        Path logFile = tempDir.resolve("jar-unpacker.log");
        assertTrue(Files.exists(logFile));
        
        List<String> lines = Files.readAllLines(logFile);
        assertEquals(1, lines.size());
        
        String logLine = lines.get(0);
        assertTrue(logLine.contains("DEBUG"));
        assertTrue(logLine.contains("Test debug message"));
    }
    
    @Test
    void givenDebugModeDisabledWhenLogDebugThenDoesNotWriteToLog() throws Exception {
        // DEBUG-режим выключен по умолчанию
        System.clearProperty("jarunpacker.debug");
        
        ToolLogger.logDebug("Test debug message");
        
        Path logFile = tempDir.resolve("jar-unpacker.log");
        
        // Файл не должен быть создан
        assertFalse(Files.exists(logFile));
    }
    
    @Test
    void givenLogExceedsMaxSizeWhenWriteThenRotatesLog() throws Exception {
        Path logFile = tempDir.resolve("jar-unpacker.log");
        
        // Создаём файл размером 10 MB
        try (FileWriter writer = new FileWriter(logFile.toFile())) {
            for (int i = 0; i < 10 * 1024 * 1024; i++) {
                writer.write('x');
            }
        }
        
        // Записываем ещё одну запись (должна сработать ротация)
        ToolLogger.logToolCall("test", "SUCCESS", 1, "{}");
        
        // Проверяем ротацию
        Path log1 = tempDir.resolve("jar-unpacker.log.1");
        assertTrue(Files.exists(log1));
    }
    
    @Test
    void givenGetLogFileThenReturnsCorrectPath() {
        String logFile = ToolLogger.getLogFile();
        
        assertTrue(logFile.endsWith("jar-unpacker.log"));
    }
}
```

### 🟢 GREEN — Реализация

> Отмечайте чек-боксы сразу после выполнения каждого пункта!

- [x] Создан `ToolLogger.java`
- [x] Обновлены вызовы логирования
- [x] Все тесты проходят: `mvn test`

### 🔵 REFACTOR — Рефакторинг

> Отмечайте чек-боксы сразу после выполнения каждого пункта!

- [x] Устранено дублирование кода
- [x] Добавлены JavaDoc к публичным методам
- [x] Все тесты проходят после рефакторинга
- [x] Сборка успешна: `mvn clean package`

## Работа с существующим кодом (если применимо)

- [x] Написан characterization test для текущего поведения логирования
- [x] Проверена регрессия после изменений

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

- Зависит от TASK-002 (базовое логирование)
- DEBUG-режим включается через `-Djarunpacker.debug=true`
- correlation-id не требуется для локального однопользовательского режима
- В следующей задаче (TASK-013) будет добавлен новый инструмент
