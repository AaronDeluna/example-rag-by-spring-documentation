# Специальные параметры аннотаций MCP

Аннотации MCP поддерживают несколько типов специальных параметров, которые предоставляют дополнительный контекст и функциональность для аннотированных методов. Эти параметры автоматически внедряются фреймворком и исключаются из генерации схемы JSON.

## Специальные типы параметров

### McpMeta

Класс `McpMeta` предоставляет доступ к метаданным из запросов, уведомлений и результатов MCP.

#### Обзор

- Автоматически внедряется при использовании в качестве параметра метода
- Исключен из ограничений по количеству параметров и генерации схемы JSON
- Обеспечивает удобный доступ к метаданным через метод `get(String key)`
- Если в запросе нет метаданных, внедряется пустой объект `McpMeta`

#### Использование в инструментах

```java
@McpTool(name = "contextual-tool", description = "Инструмент с доступом к метаданным")
public String processWithContext(
        @McpToolParam(description = "Входные данные", required = true) String data,
        McpMeta meta) {
    
    // Доступ к метаданным из запроса
    String userId = (String) meta.get("userId");
    String sessionId = (String) meta.get("sessionId");
    String userRole = (String) meta.get("userRole");
    
    // Использование метаданных для настройки поведения
    if ("admin".equals(userRole)) {
        return processAsAdmin(data, userId);
    } else {
        return processAsUser(data, userId);
    }
}
```

#### Использование в ресурсах

```java
@McpResource(uri = "secure-data://{id}", name = "Защищенные данные")
public ReadResourceResult getSecureData(String id, McpMeta meta) {
    
    String requestingUser = (String) meta.get("requestingUser");
    String accessLevel = (String) meta.get("accessLevel");
    
    // Проверка прав доступа с использованием метаданных
    if (!"admin".equals(accessLevel)) {
        return new ReadResourceResult(List.of(
            new TextResourceContents("secure-data://" + id, 
                "text/plain", "Доступ запрещен")
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
@McpPrompt(name = "localized-prompt", description = "Генерация локализованных подсказок")
public GetPromptResult localizedPrompt(
        @McpArg(name = "topic", required = true) String topic,
        McpMeta meta) {
    
    String language = (String) meta.get("language");
    String region = (String) meta.get("region");
    
    // Генерация локализованного контента на основе метаданных
    String message = generateLocalizedMessage(topic, language, region);
    
    return new GetPromptResult("Локализованная подсказка",
        List.of(new PromptMessage(Role.ASSISTANT, new TextContent(message)))
    );
}
```

### @McpProgressToken

Аннотация `@McpProgressToken` помечает параметр для получения токенов прогресса из запросов MCP.

#### Обзор

- Тип параметра должен быть `String`
- Автоматически получает значение токена прогресса из запроса
- Исключен из сгенерированной схемы JSON
- Если токен прогресса отсутствует, внедряется `null`
- Используется для отслеживания длительных операций

#### Использование в инструментах```markdown
```java
@McpTool(name = "long-operation", description = "Долгосрочная операция с прогрессом")
public String performLongOperation(
        @McpProgressToken String progressToken,
        @McpToolParam(description = "Название операции", required = true) String operation,
        @McpToolParam(description = "Продолжительность в секундах", required = true) int duration,
        McpSyncServerExchange exchange) {
    
    if (progressToken != null) {
        // Отправить начальный прогресс
        exchange.progressNotification(new ProgressNotification(
            progressToken, 0.0, 1.0, "Запуск " + operation));
        
        // Симуляция работы с обновлениями прогресса
        for (int i = 1; i <= duration; i++) {
            Thread.sleep(1000);
            double progress = (double) i / duration;
            
            exchange.progressNotification(new ProgressNotification(
                progressToken, progress, 1.0, 
                String.format("Обработка... %d%%", (int)(progress * 100))));
        }
    }
    
    return "Операция " + operation + " завершена";
}
```

#### Использование в ресурсах

```java
@McpResource(uri = "large-file://{path}", name = "Ресурс большого файла")
public ReadResourceResult getLargeFile(
        @McpProgressToken String progressToken,
        String path,
        McpSyncServerExchange exchange) {
    
    File file = new File(path);
    long fileSize = file.length();
    
    if (progressToken != null) {
        // Отслеживание прогресса чтения файла
        exchange.progressNotification(new ProgressNotification(
            progressToken, 0.0, fileSize, "Чтение файла"));
    }
    
    String content = readFileWithProgress(file, progressToken, exchange);
    
    if (progressToken != null) {
        exchange.progressNotification(new ProgressNotification(
            progressToken, fileSize, fileSize, "Чтение файла завершено"));
    }
    
    return new ReadResourceResult(List.of(
        new TextResourceContents("large-file://" + path, "text/plain", content)
    ));
}
```

### McpSyncRequestContext / McpAsyncRequestContext

Объекты контекста запроса предоставляют унифицированный доступ к информации о запросах MCP и операциям на стороне сервера.

#### Обзор

- Предоставляет унифицированный интерфейс для как состоявшихся, так и безсостоявшихся операций
- Автоматически внедряется при использовании в качестве параметра
- Исключается из генерации JSON-схемы
- Позволяет использовать расширенные функции, такие как ведение журнала, уведомления о прогрессе, выборка и elicitation
- Работает как в состоявшемся (обмен сервером), так и в безсостоявшемся (контекст передачи) режимах

#### Возможности McpSyncRequestContext

```java
public record UserInfo(String name, String email, int age) {}

@McpTool(name = "advanced-tool", description = "Инструмент с полными возможностями сервера")
public String advancedTool(
        McpSyncRequestContext context,
        @McpToolParam(description = "Ввод", required = true) String input) {
    
    // Отправить уведомление о ведении журнала
    context.info("Обработка: " + input);
    
    // Пинговать клиента
    context.ping();
    
    // Отправить обновления прогресса
    context.progress(50); // 50% завершено
    
    // Проверить, поддерживается ли elicitation, перед его использованием
    if (context.elicitEnabled()) {
        // Запросить дополнительную информацию у пользователя
        StructuredElicitResult<UserInfo> elicitResult = context.elicit(
            e -> e.message("Необходима дополнительная информация"),
            UserInfo.class
        );
        
        if (elicitResult.action() == ElicitResult.Action.ACCEPT) {
            UserInfo userInfo = elicitResult.structuredContent();
            // Использовать информацию о пользователе
        }
    }
    
    // Проверить, поддерживается ли выборка, перед ее использованием
    if (context.sampleEnabled()) {
        // Запросить выборку LLM
        CreateMessageResult samplingResult = context.sample(
            s -> s.message("Процесс: " + input)
                .modelPreferences(pref -> pref.modelHints("gpt-4"))
        );
    }
    
    return "Обработано с использованием расширенных функций";
}
```

#### Возможности McpAsyncRequestContext
``````java
public record UserInfo(String name, String email, int age) {}

@McpTool(name = "async-advanced-tool", description = "Асинхронный инструмент с возможностями сервера")
public Mono<String> asyncAdvancedTool(
        McpAsyncRequestContext context,
        @McpToolParam(description = "Входные данные", required = true) String input) {
    
    return context.info("Асинхронная обработка: " + input)
        .then(context.progress(25))
        .then(context.ping())
        .flatMap(v -> {
            // Выполнить элицитацию, если поддерживается
            if (context.elicitEnabled()) {
                return context.elicitation(UserInfo.class)
                    .map(userInfo -> "Обработка для пользователя: " + userInfo.name());
            }
            return Mono.just("Обработка...");
        })
        .flatMap(msg -> {
            // Выполнить выборку, если поддерживается
            if (context.sampleEnabled()) {
                return context.sampling("Процесс: " + input)
                    .map(result -> "Завершено: " + result);
            }
            return Mono.just("Завершено: " + msg);
        });
}
```

### McpTransportContext

Легковесный контекст для безсостояний операций.

#### Обзор

- Предоставляет минимальный контекст без полного обмена с сервером
- Используется в безсостояний реализациях
- Автоматически внедряется при использовании в качестве параметра
- Исключен из генерации JSON-схемы

#### Пример использования

```java
@McpTool(name = "stateless-tool", description = "Безсостояний инструмент с контекстом")
public String statelessTool(
        McpTransportContext context,
        @McpToolParam(description = "Входные данные", required = true) String input) {
    
    // Ограниченный доступ к контексту
    // Полезно для операций на уровне транспорта
    
    return "Обработано в безсостояний режиме: " + input;
}

@McpResource(uri = "stateless://{id}", name = "Безсостояственный ресурс")
public ReadResourceResult statelessResource(
        McpTransportContext context,
        String id) {
    
    // Доступ к транспортному контексту при необходимости
    String data = loadData(id);
    
    return new ReadResourceResult(List.of(
        new TextResourceContents("stateless://" + id, "text/plain", data)
    ));
}
```

### CallToolRequest

Специальный параметр для инструментов, которым нужен доступ к полному запросу с динамической схемой.

#### Обзор

- Предоставляет доступ к полному запросу инструмента
- Позволяет обрабатывать динамические схемы во время выполнения
- Автоматически внедряется и исключается из генерации схемы
- Полезно для гибких инструментов, которые адаптируются к различным входным схемам

#### Примеры использования

```java
@McpTool(name = "dynamic-tool", description = "Инструмент с поддержкой динамической схемы")
public CallToolResult processDynamicSchema(CallToolRequest request) {
    Map<String, Object> args = request.arguments();
    
    // Обработка на основе любой схемы, предоставленной во время выполнения
    StringBuilder result = new StringBuilder("Обработано:\n");
    
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
``````markdown
```java
@McpTool(name = "hybrid-tool", description = "Инструмент с типизированными и динамическими параметрами")
public String processHybrid(
        @McpToolParam(description = "Операция", required = true) String operation,
        @McpToolParam(description = "Приоритет", required = false) Integer priority,
        CallToolRequest request) {
    
    // Используйте типизированные параметры для известных полей
    String result = "Операция: " + operation;
    if (priority != null) {
        result += " (Приоритет: " + priority + ")";
    }
    
    // Доступ к дополнительным динамическим аргументам
    Map<String, Object> allArgs = request.arguments();
    
    // Удалите известные параметры, чтобы получить только дополнительные
    Map<String, Object> additionalArgs = new HashMap<>(allArgs);
    additionalArgs.remove("operation");
    additionalArgs.remove("priority");
    
    if (!additionalArgs.isEmpty()) {
        result += " с " + additionalArgs.size() + " дополнительными параметрами";
    }
    
    return result;
}
```

#### С токеном прогресса

```java
@McpTool(name = "flexible-with-progress", description = "Гибкий инструмент с прогрессом")
public CallToolResult flexibleWithProgress(
        @McpProgressToken String progressToken,
        CallToolRequest request,
        McpSyncServerExchange exchange) {
    
    Map<String, Object> args = request.arguments();
    
    if (progressToken != null) {
        exchange.progressNotification(new ProgressNotification(
            progressToken, 0.0, 1.0, "Обработка динамического запроса"));
    }
    
    // Обработка динамических аргументов
    String result = processDynamicArgs(args);
    
    if (progressToken != null) {
        exchange.progressNotification(new ProgressNotification(
            progressToken, 1.0, 1.0, "Завершено"));
    }
    
    return CallToolResult.builder()
        .addTextContent(result)
        .build();
}
```

## Правила внедрения параметров

### Автоматическое внедрение

Следующие параметры автоматически внедряются фреймворком:

1. `McpMeta` - Метаданные из запроса
2. `@McpProgressToken String` - Токен прогресса, если доступен
3. `McpSyncServerExchange` / `McpAsyncServerExchange` - Контекст обмена на сервере
4. `McpTransportContext` - Контекст передачи для безсостояния операций
5. `CallToolRequest` - Полный запрос инструмента для динамической схемы

### Генерация схемы

Специальные параметры исключаются из генерации JSON-схемы:

- Они не появляются в входной схеме инструмента
- Они не учитываются в лимитах параметров
- Они не видны клиентам MCP

### Обработка null

- `McpMeta` - Никогда не null, пустой объект, если нет метаданных
- `@McpProgressToken` - Может быть null, если токен не предоставлен
- Обмены на сервере - Никогда не null при правильной конфигурации
- `CallToolRequest` - Никогда не null для методов инструмента

## Лучшие практики

### Используйте McpMeta для контекста

```java
@McpTool(name = "context-aware", description = "Инструмент с учетом контекста")
public String contextAware(
        @McpToolParam(description = "Данные", required = true) String data,
        McpMeta meta) {
    
    // Всегда проверяйте на null значения в метаданных
    String userId = (String) meta.get("userId");
    if (userId == null) {
        userId = "анонимный";
    }
    
    return processForUser(data, userId);
}
```

### Проверки на null для токена прогресса

```java
@McpTool(name = "safe-progress", description = "Безопасная обработка прогресса")
public String safeProgress(
        @McpProgressToken String progressToken,
        @McpToolParam(description = "Задача", required = true) String task,
        McpSyncServerExchange exchange) {
    
    // Всегда проверяйте, доступен ли токен прогресса
    if (progressToken != null) {
        exchange.progressNotification(new ProgressNotification(
            progressToken, 0.0, 1.0, "Начало"));
    }
    
    // Выполнение работы...
    
    if (progressToken != null) {
        exchange.progressNotification(new ProgressNotification(
            progressToken, 1.0, 1.0, "Завершено"));
    }
    
    return "Задача выполнена";
}
```

### Выберите правильный контекст
```- Используйте `McpSyncRequestContext` / `McpAsyncRequestContext` для унифицированного доступа к контексту запроса, поддерживающего как состояние, так и безсостояние с удобными вспомогательными методами
- Используйте `McpTransportContext` для простых безсостояний операций, когда вам нужен только контекст на уровне транспорта
- Полностью опустите параметры контекста для самых простых случаев

### Проверка возможностей

Всегда проверяйте поддержку возможностей перед использованием функций клиента:

```java
@McpTool(name = "capability-aware", description = "Инструмент, который проверяет возможности")
public String capabilityAware(
        McpSyncRequestContext context,
        @McpToolParam(description = "Данные", required = true) String data) {
    
    // Проверьте, поддерживается ли элицитация перед ее использованием
    if (context.elicitEnabled()) {
        // Безопасно использовать элицитацию
        var result = context.elicit(UserInfo.class);
        // Обработка результата...
    }
    
    // Проверьте, поддерживается ли выборка перед ее использованием
    if (context.sampleEnabled()) {
        // Безопасно использовать выборку
        var samplingResult = context.sample("Процесс: " + data);
        // Обработка результата...
    }
    
    // Примечание: Безсостояние серверы не поддерживают двунаправленные операции
    // (корни, элицитация, выборка) и вернут false для этих проверок
    
    return "Обработано с учетом возможностей";
}
```

## Дополнительные ресурсы

- xref:api/mcp/mcp-annotations-overview.adoc[Обзор аннотаций MCP]
- xref:api/mcp/mcp-annotations-server.adoc[Аннотации сервера]
- xref:api/mcp/mcp-annotations-client.adoc[Аннотации клиента]
- xref:api/mcp/mcp-annotations-examples.adoc[Примеры]
