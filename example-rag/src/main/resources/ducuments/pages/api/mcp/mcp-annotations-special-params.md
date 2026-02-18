# Специальные параметры аннотаций MCP

Аннотации MCP поддерживают несколько специальных типов параметров, которые предоставляют дополнительный контекст и функциональность аннотированным методам. 
Эти параметры автоматически вводятся платформой и исключаются из создания схемы JSON.

## Специальные типы параметров

### МкпМета

Класс `McpMeta` обеспечивает доступ к метаданным из запросов, уведомлений и результатов MCP.

#### Обзор

- Автоматически вводится при использовании в качестве параметра метода
- Исключено из ограничений количества параметров и создания схемы JSON.
- Обеспечивает удобный доступ к метаданным с помощью метода `get(String key)`.
- Если в запросе нет метаданных, вводится пустой объект `McpMeta`.

#### Использование в инструментах

```java
@McpTool(name = "contextual-tool", description = "Tool with metadata access")
public String processWithContext(
        @McpToolParam(description = "Input data", required = true) String data,
        McpMeta meta) {
    
    // Access metadata from the request
    String userId = (String) meta.get("userId");
    String sessionId = (String) meta.get("sessionId");
    String userRole = (String) meta.get("userRole");
    
    // Use metadata to customize behavior
    if ("admin".equals(userRole)) {
        return processAsAdmin(data, userId);
    } else {
        return processAsUser(data, userId);
    }
}
```

#### Использование в ресурсах

```java
@McpResource(uri = "secure-data://{id}", name = "Secure Data")
public ReadResourceResult getSecureData(String id, McpMeta meta) {
    
    String requestingUser = (String) meta.get("requestingUser");
    String accessLevel = (String) meta.get("accessLevel");
    
    // Check access permissions using metadata
    if (!"admin".equals(accessLevel)) {
        return new ReadResourceResult(List.of(
            new TextResourceContents("secure-data://" + id, 
                "text/plain", "Access denied")
        ));
    }
    
    String data = loadSecureData(id);
    return new ReadResourceResult(List.of(
        new TextResourceContents("secure-data://" + id, 
            "text/plain", data)
    ));
}
```

#### Использование в подсказках

```java
@McpPrompt(name = "localized-prompt", description = "Localized prompt generation")
public GetPromptResult localizedPrompt(
        @McpArg(name = "topic", required = true) String topic,
        McpMeta meta) {
    
    String language = (String) meta.get("language");
    String region = (String) meta.get("region");
    
    // Generate localized content based on metadata
    String message = generateLocalizedMessage(topic, language, region);
    
    return new GetPromptResult("Localized Prompt",
        List.of(new PromptMessage(Role.ASSISTANT, new TextContent(message)))
    );
}
```

### @McpProgressToken

Аннотация `@McpProgressToken` отмечает параметр для получения токенов прогресса из запросов MCP.

#### Обзор

- Тип параметра должен быть `String`.
- Автоматически получает значение токена прогресса из запроса
- Исключено из сгенерированной схемы JSON.
- Если маркер прогресса отсутствует, вводится `null`.
- Используется для отслеживания длительных операций.

#### Использование в инструментах

```java
@McpTool(name = "long-operation", description = "Long-running operation with progress")
public String performLongOperation(
        @McpProgressToken String progressToken,
        @McpToolParam(description = "Operation name", required = true) String operation,
        @McpToolParam(description = "Duration in seconds", required = true) int duration,
        McpSyncServerExchange exchange) {
    
    if (progressToken != null) {
        // Send initial progress
        exchange.progressNotification(new ProgressNotification(
            progressToken, 0.0, 1.0, "Starting " + operation));
        
        // Simulate work with progress updates
        for (int i = 1; i <= duration; i++) {
            Thread.sleep(1000);
            double progress = (double) i / duration;
            
            exchange.progressNotification(new ProgressNotification(
                progressToken, progress, 1.0, 
                String.format("Processing... %d%%", (int)(progress * 100))));
        }
    }
    
    return "Operation " + operation + " completed";
}
```

#### Использование в ресурсах

```java
@McpResource(uri = "large-file://{path}", name = "Large File Resource")
public ReadResourceResult getLargeFile(
        @McpProgressToken String progressToken,
        String path,
        McpSyncServerExchange exchange) {
    
    File file = new File(path);
    long fileSize = file.length();
    
    if (progressToken != null) {
        // Track file reading progress
        exchange.progressNotification(new ProgressNotification(
            progressToken, 0.0, fileSize, "Reading file"));
    }
    
    String content = readFileWithProgress(file, progressToken, exchange);
    
    if (progressToken != null) {
        exchange.progressNotification(new ProgressNotification(
            progressToken, fileSize, fileSize, "File read complete"));
    }
    
    return new ReadResourceResult(List.of(
        new TextResourceContents("large-file://" + path, "text/plain", content)
    ));
}
```

### McpSyncRequestContext/McpAsyncRequestContext

Объекты контекста запроса обеспечивают унифицированный доступ к информации запроса MCP и операциям на стороне сервера.

#### Обзор

- Обеспечивает унифицированный интерфейс для операций с сохранением и без сохранения состояния.
- Автоматически вводится при использовании в качестве параметра
- Исключено из создания схемы JSON.
- Включает расширенные функции, такие как ведение журнала, уведомления о ходе выполнения, выборка и сбор данных.
- Работает как с режимами с отслеживанием состояния (обмен серверами), так и с режимами без сохранения состояния (транспортный контекст).

#### Возможности McpSyncRequestContext

```java
public record UserInfo(String name, String email, int age) {}

@McpTool(name = "advanced-tool", description = "Tool with full server capabilities")
public String advancedTool(
        McpSyncRequestContext context,
        @McpToolParam(description = "Input", required = true) String input) {
    
    // Send logging notification
    context.info("Processing: " + input);
    
    // Ping the client
    context.ping();
    
    // Send progress updates
    context.progress(50); // 50% complete
    
    // Check if elicitation is supported before using it
    if (context.elicitEnabled()) {
        // Request additional information from user
        StructuredElicitResult<UserInfo> elicitResult = context.elicit(
            e -> e.message("Need additional information"),
            UserInfo.class
        );
        
        if (elicitResult.action() == ElicitResult.Action.ACCEPT) {
            UserInfo userInfo = elicitResult.structuredContent();
            // Use the user information
        }
    }
    
    // Check if sampling is supported before using it
    if (context.sampleEnabled()) {
        // Request LLM sampling
        CreateMessageResult samplingResult = context.sample(
            s -> s.message("Process: " + input)
                .modelPreferences(pref -> pref.modelHints("gpt-4"))
        );
    }
    
    return "Processed with advanced features";
}
```

#### Возможности McpAsyncRequestContext

```java
public record UserInfo(String name, String email, int age) {}

@McpTool(name = "async-advanced-tool", description = "Async tool with server capabilities")
public Mono<String> asyncAdvancedTool(
        McpAsyncRequestContext context,
        @McpToolParam(description = "Input", required = true) String input) {
    
    return context.info("Async processing: " + input)
        .then(context.progress(25))
        .then(context.ping())
        .flatMap(v -> {
            // Perform elicitation if supported
            if (context.elicitEnabled()) {
                return context.elicitation(UserInfo.class)
                    .map(userInfo -> "Processing for user: " + userInfo.name());
            }
            return Mono.just("Processing...");
        })
        .flatMap(msg -> {
            // Perform sampling if supported
            if (context.sampleEnabled()) {
                return context.sampling("Process: " + input)
                    .map(result -> "Completed: " + result);
            }
            return Mono.just("Completed: " + msg);
        });
}
```

### McpTransportContext

Облегченный контекст для операций без сохранения состояния.

#### Обзор

- Обеспечивает минимальный контекст без полного обмена серверами
- Используется в реализациях без сохранения состояния
- Автоматически вводится при использовании в качестве параметра
- Исключено из создания схемы JSON.

#### Пример использования

```java
@McpTool(name = "stateless-tool", description = "Stateless tool with context")
public String statelessTool(
        McpTransportContext context,
        @McpToolParam(description = "Input", required = true) String input) {
    
    // Limited context access
    // Useful for transport-level operations
    
    return "Processed in stateless mode: " + input;
}

@McpResource(uri = "stateless://{id}", name = "Stateless Resource")
public ReadResourceResult statelessResource(
        McpTransportContext context,
        String id) {
    
    // Access transport context if needed
    String data = loadData(id);
    
    return new ReadResourceResult(List.of(
        new TextResourceContents("stateless://" + id, "text/plain", data)
    ));
}
```

### CallToolRequest

Специальный параметр для инструментов, которым необходим доступ к полному запросу с динамической схемой.

#### Обзор

- Предоставляет доступ ко всему запросу инструмента.
- Включает динамическую обработку схемы во время выполнения.
- Автоматически вводится и исключается из генерации схемы.
- Полезно для гибких инструментов, которые адаптируются к различным схемам ввода.

#### Примеры использования

```java
@McpTool(name = "dynamic-tool", description = "Tool with dynamic schema support")
public CallToolResult processDynamicSchema(CallToolRequest request) {
    Map<String, Object> args = request.arguments();
    
    // Process based on whatever schema was provided at runtime
    StringBuilder result = new StringBuilder("Processed:\n");
    
    for (Map.Entry<String, Object> entry : args.entrySet()) {
        result.append("  ").append(entry.getKey())
              .append(": ").append(entry.getValue()).append("\n");
    }
    
    return CallToolResult.builder()
        .addTextContent(result.toString())
        .build();
}
```

#### Смешанные параметры

```java
@McpTool(name = "hybrid-tool", description = "Tool with typed and dynamic parameters")
public String processHybrid(
        @McpToolParam(description = "Operation", required = true) String operation,
        @McpToolParam(description = "Priority", required = false) Integer priority,
        CallToolRequest request) {
    
    // Use typed parameters for known fields
    String result = "Operation: " + operation;
    if (priority != null) {
        result += " (Priority: " + priority + ")";
    }
    
    // Access additional dynamic arguments
    Map<String, Object> allArgs = request.arguments();
    
    // Remove known parameters to get only additional ones
    Map<String, Object> additionalArgs = new HashMap<>(allArgs);
    additionalArgs.remove("operation");
    additionalArgs.remove("priority");
    
    if (!additionalArgs.isEmpty()) {
        result += " with " + additionalArgs.size() + " additional parameters";
    }
    
    return result;
}
```

#### С жетоном прогресса

```java
@McpTool(name = "flexible-with-progress", description = "Flexible tool with progress")
public CallToolResult flexibleWithProgress(
        @McpProgressToken String progressToken,
        CallToolRequest request,
        McpSyncServerExchange exchange) {
    
    Map<String, Object> args = request.arguments();
    
    if (progressToken != null) {
        exchange.progressNotification(new ProgressNotification(
            progressToken, 0.0, 1.0, "Processing dynamic request"));
    }
    
    // Process dynamic arguments
    String result = processDynamicArgs(args);
    
    if (progressToken != null) {
        exchange.progressNotification(new ProgressNotification(
            progressToken, 1.0, 1.0, "Complete"));
    }
    
    return CallToolResult.builder()
        .addTextContent(result)
        .build();
}
```

## Правила внедрения параметров

### Автоматическая инъекция

Следующие параметры автоматически вводятся платформой:

1. `McpMeta` — Метаданные из запроса
2. `@McpProgressToken String` — токен прогресса, если доступен.
3. `McpSyncServerExchange` / `McpAsyncServerExchange` — Контекст обмена сервером
4. `McpTransportContext` — Транспортный контекст для операций без сохранения состояния.
5. `CallToolRequest` – полный запрос инструмента для динамической схемы.

### Генерация схемы

Специальные параметры исключены из генерации схемы JSON:

- Они не отображаются во входной схеме инструмента.
- Они не учитываются при расчете ограничений параметров.
- Они не видны клиентам MCP.

### Обработка нуля

- `McpMeta` — Никогда не имеет значения null, пустой объект, если нет метаданных.
- `@McpProgressToken` – может иметь значение null, если токен не предоставлен.
- Серверные обмены — никогда не обнуляются при правильной настройке
- `CallToolRequest` — никогда не обнулять значения для методов инструмента.

## Лучшие практики

### Используйте McpMeta для контекста

```java
@McpTool(name = "context-aware", description = "Context-aware tool")
public String contextAware(
        @McpToolParam(description = "Data", required = true) String data,
        McpMeta meta) {
    
    // Always check for null values in metadata
    String userId = (String) meta.get("userId");
    if (userId == null) {
        userId = "anonymous";
    }
    
    return processForUser(data, userId);
}
```

### Проверки нуля токена прогресса

```java
@McpTool(name = "safe-progress", description = "Safe progress handling")
public String safeProgress(
        @McpProgressToken String progressToken,
        @McpToolParam(description = "Task", required = true) String task,
        McpSyncServerExchange exchange) {
    
    // Always check if progress token is available
    if (progressToken != null) {
        exchange.progressNotification(new ProgressNotification(
            progressToken, 0.0, 1.0, "Starting"));
    }
    
    // Perform work...
    
    if (progressToken != null) {
        exchange.progressNotification(new ProgressNotification(
            progressToken, 1.0, 1.0, "Complete"));
    }
    
    return "Task completed";
}
```

### Выберите правильный контекст

- Используйте `McpSyncRequestContext`/`McpAsyncRequestContext` для унифицированного доступа к контексту запроса, поддерживая операции как с сохранением, так и без сохранения состояния с помощью удобных вспомогательных методов.
- Используйте `McpTransportContext` для простых операций без сохранения состояния, когда вам нужен только контекст транспортного уровня.
- Полностью опустите параметры контекста для самых простых случаев.

### Проверка возможностей

Всегда проверяйте поддержку возможностей перед использованием функций клиента:

```java
@McpTool(name = "capability-aware", description = "Tool that checks capabilities")
public String capabilityAware(
        McpSyncRequestContext context,
        @McpToolParam(description = "Data", required = true) String data) {
    
    // Check if elicitation is supported before using it
    if (context.elicitEnabled()) {
        // Safe to use elicitation
        var result = context.elicit(UserInfo.class);
        // Process result...
    }
    
    // Check if sampling is supported before using it
    if (context.sampleEnabled()) {
        // Safe to use sampling
        var samplingResult = context.sample("Process: " + data);
        // Process result...
    }
    
    // Note: Stateless servers do not support bidirectional operations
    // (roots, elicitation, sampling) and will return false for these checks
    
    return "Processed with capability awareness";
}
```

## Дополнительные ресурсы

- [Обзор аннотаций MCP](api/mcp/mcp-annotations-overview.md)
- [Аннотации сервера](api/mcp/mcp-annotations-server.md)
- [Аннотации клиента](api/mcp/mcp-annotations-client.md)
- [Примеры](api/mcp/mcp-annotations-examples.md)
