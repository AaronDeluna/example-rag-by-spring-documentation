# TASK-003: Разделение Server.java на модули: JsonRpcHandler

> **⚠️ Важно:** Этот файл должен быть создан **до начала** любой работы над задачей.
> Отмечайте чек-боксы `[x]` **сразу после выполнения** каждого шага для возможности продолжения с места прерывания.

## Описание

Выделить обработку JSON-RPC протокола из `Server.java` в отдельный класс `JsonRpcHandler.java`.

**Текущая проблема:** `Server.java` содержит 895 строк, смешивает обработку протокола MCP, поиск в JAR, декомпиляцию и логирование.

**Цель:** Выделить логику обработки JSON-RPC сообщений в отдельный класс, уменьшив `Server.java` до роли координатора.

## Текущее состояние

В `Server.java` находятся методы:
- `runMcpServer()` — основной цикл чтения stdin
- `handleInitialize()` — обработка инициализации MCP
- `handleListTools()` — возврат списка инструментов
- `handleCallTool()` — вызов инструментов
- `sendResponse()` — отправка JSON-RPC ответа
- `sendError()` — отправка ошибки
- `parseJson()` — парсинг JSON в `JsonMessage`

## Требуемые изменения

### 1. Создать класс `JsonRpcHandler.java`

**Путь:** `src/main/java/ru/mirent/JsonRpcHandler.java`

**Ответственность:** Обработка JSON-RPC 2.0 сообщений протокола MCP.

**Структура класса:**

```java
package ru.mirent;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Обработчик JSON-RPC 2.0 сообщений протокола MCP.
 * Обрабатывает: initialize, notifications/initialized, tools/list, tools/call
 */
public class JsonRpcHandler {
    
    private final ToolRegistry toolRegistry;
    
    public JsonRpcHandler(ToolRegistry toolRegistry) {
        this.toolRegistry = toolRegistry;
    }
    
    /**
     * Обработка входящего JSON-RPC сообщения
     */
    public JsonMessage handle(String json) throws IOException {
        JsonMessage msg = parseJson(json);
        
        if ("initialize".equals(msg.method)) {
            return handleInitialize(msg);
        } else if ("notifications/initialized".equals(msg.method)) {
            return null; // уведомление, ответ не требуется
        } else if ("tools/list".equals(msg.method)) {
            return handleListTools(msg);
        } else if ("tools/call".equals(msg.method)) {
            return handleCallTool(msg);
        } else {
            return createError(msg.id, "Unknown method: " + msg.method);
        }
    }
    
    private JsonMessage handleInitialize(JsonMessage msg) {
        Map<String, Object> result = new HashMap<>();
        result.put("protocolVersion", "2024-11-05");
        
        Map<String, Object> serverInfo = new HashMap<>();
        serverInfo.put("name", "jar-unpacker");
        serverInfo.put("version", "1.0.0");
        result.put("serverInfo", serverInfo);
        
        Map<String, Object> capabilities = new HashMap<>();
        capabilities.put("tools", new HashMap<String, Object>());
        result.put("capabilities", capabilities);
        
        return createResponse(msg.id, result);
    }
    
    private JsonMessage handleListTools(JsonMessage msg) {
        List<Map<String, Object>> tools = toolRegistry.getTools();
        Map<String, Object> result = new HashMap<>();
        result.put("tools", tools);
        return createResponse(msg.id, result);
    }
    
    private JsonMessage handleCallTool(JsonMessage msg) {
        Map<String, Object> params = (Map<String, Object>) msg.params;
        String name = (String) params.get("name");
        Map<String, Object> arguments = (Map<String, Object>) params.get("arguments");
        
        Object result = toolRegistry.callTool(name, arguments);
        return createResponse(msg.id, result);
    }
    
    private JsonMessage createResponse(Object id, Object result) {
        JsonMessage response = new JsonMessage();
        response.id = id;
        response.result = result;
        return response;
    }
    
    private JsonMessage createError(Object id, String message) {
        JsonMessage error = new JsonMessage();
        error.id = id;
        error.error = Map.of("code", -32603, "message", message);
        return error;
    }
    
    private JsonMessage parseJson(String json) throws IOException {
        JsonUtils.Parsed parsed = JsonUtils.parse(json);
        JsonMessage msg = new JsonMessage();
        msg.id = parsed.id;
        msg.method = parsed.method;
        msg.params = parsed.params;
        return msg;
    }
}
```

### 2. Создать класс `JsonMessage.java`

**Путь:** `src/main/java/ru/mirent/JsonMessage.java`

```java
package ru.mirent;

/**
 * Представление JSON-RPC сообщения
 */
public class JsonMessage {
    public Object id;
    public String method;
    public Object params;
    public Object result;
    public Object error;
}
```

### 3. Создать интерфейс `ToolRegistry.java`

**Путь:** `src/main/java/ru/mirent/ToolRegistry.java`

```java
package ru.mirent;

import java.util.List;
import java.util.Map;

/**
 * Реестр инструментов MCP
 */
public interface ToolRegistry {
    
    /**
     * Вернуть список доступных инструментов
     */
    List<Map<String, Object>> getTools();
    
    /**
     * Вызвать инструмент по имени
     * @param name имя инструмента
     * @param arguments аргументы вызова
     * @return результат выполнения
     */
    Object callTool(String name, Map<String, Object> arguments);
}
```

### 4. Обновить `Server.java`

Удалить методы:
- `handleInitialize()`
- `handleListTools()`
- `handleCallTool()`
- `sendResponse()`
- `sendError()`
- `parseJson()`

Оставить:
- `runMcpServer()` — цикл с использованием `JsonRpcHandler`
- `main()` — точка входа
- Вспомогательные методы (`createStringProp()`, `countChar()`, и т.д.)

**Пример обновления `runMcpServer()`:**

```java
private static void runMcpServer() throws IOException {
    ToolRegistry registry = new DefaultToolRegistry();
    JsonRpcHandler handler = new JsonRpcHandler(registry);
    
    BufferedReader reader = new BufferedReader(new InputStreamReader(System.in, "UTF-8"));
    PrintWriter writer = new PrintWriter(new OutputStreamWriter(System.out, "UTF-8"), true);
    
    while (true) {
        String line = reader.readLine();
        if (line == null) {
            break;
        }
        
        if (line.trim().isEmpty()) {
            continue;
        }
        
        try {
            JsonMessage response = handler.handle(line);
            if (response != null) {
                writer.println(JsonUtils.toJson(response));
                writer.flush();
            }
        } catch (Exception e) {
            JsonMessage error = new JsonMessage();
            error.id = 0;
            error.error = Map.of("code", -32603, "message", "Internal error: " + e.getMessage());
            writer.println(JsonUtils.toJson(error));
            writer.flush();
        }
    }
}
```

## Критерии приёмки (Acceptance Criteria)

- [x] Создан класс `JsonRpcHandler.java` с обработкой всех методов MCP
- [x] Создан класс `JsonMessage.java`
- [x] Создан интерфейс `ToolRegistry.java`
- [x] Обновлён `Server.java` — удалены дублирующиеся методы
- [x] `Server.java` сократился минимум на 200 строк (с 895 до 658 строк)
- [x] Все существующие тесты проходят: `mvn test` (73 теста)
- [x] Сборка успешна: `mvn clean package`
- [x] MCP-сервер запускается и отвечает на `tools/list`

## TDD Цикл

### 🔴 RED — Тесты

> Отмечайте чек-боксы сразу после выполнения каждого пункта!

- [x] Создан тест `JsonRpcHandlerTest.java` в `src/test/java/ru/mirent/`
- [x] Написан тест `givenInitializeRequestWhenHandleThenReturnsProtocolVersion()`
- [x] Написан тест `givenToolsListRequestWhenHandleThenReturnsTools()`
- [x] Написан тест `givenUnknownMethodWhenHandleThenReturnsError()`
- [x] Тесты компилируются и падают (класс `JsonRpcHandler` ещё не существовал)

### 🟢 GREEN — Реализация

> Отмечайте чек-боксы сразу после выполнения каждого пункта!

- [x] Создан файл `JsonRpcHandler.java` с минимальной реализацией
- [x] Создан файл `JsonMessage.java`
- [x] Создан файл `ToolRegistry.java`
- [x] Создан файл `JsonUtils.java` (извлечён из `Server.java`)
- [x] Создан файл `DefaultToolRegistry.java`
- [x] Обновлён `Server.java` для использования `JsonRpcHandler`
- [x] Все тесты проходят: `mvn test`

### 🔵 REFACTOR — Рефакторинг

> Отмечайте чек-боксы сразу после выполнения каждого пункта!

- [x] Устранено дублирование кода между `Server.java` и `JsonRpcHandler.java`
- [x] Добавлены JavaDoc к публичным методам
- [x] Все тесты проходят после рефакторинга
- [x] Сборка успешна: `mvn clean package`

## Работа с существующим кодом (если применимо)

- [ ] Написан characterization test для текущего поведения `handleInitialize()`
- [ ] Написан characterization test для текущего поведения `handleListTools()`
- [ ] Написан characterization test для текущего поведения `handleCallTool()`
- [ ] Тесты проходят (фиксация поведения)
- [ ] Проверена регрессия после изменений

## Чек-лист завершения

- [ ] Все тесты зелёные
- [ ] Сборка успешна
- [ ] Код соответствует стандартам проекта
- [ ] Изменения закоммичены

## Статус

| Поле | Значение |
|------|----------|
| Дата создания: | 2026-03-26 |
| Дата начала: | 2026-03-26 |
| Дата завершения: | |
| Статус: | 📋 Pending |

## Заметки

- Этот класс не должен зависеть от конкретных реализаций инструментов
- `ToolRegistry` — интерфейс для внедрения зависимостей
- В следующей задаче (TASK-004) будет создана реализация `DefaultToolRegistry`
