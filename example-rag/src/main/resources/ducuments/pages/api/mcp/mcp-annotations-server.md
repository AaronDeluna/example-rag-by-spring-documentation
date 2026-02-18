# Аннотации сервера MCP

Аннотации сервера MCP предоставляют декларативный способ реализации функций сервера MCP с использованием аннотаций Java. 
Эти аннотации упрощают создание инструментов, ресурсов, подсказок и обработчиков завершения.

## Аннотации сервера

### @McpTool

Аннотация `@McpTool` помечает метод как реализацию инструмента MCP с автоматическим созданием схемы JSON.

#### Основное использование

```java
@Component
public class CalculatorTools {

    @McpTool(name = "add", description = "Add two numbers together")
    public int add(
            @McpToolParam(description = "First number", required = true) int a,
            @McpToolParam(description = "Second number", required = true) int b) {
        return a + b;
    }
}
```

#### Расширенные функции

```java
@McpTool(name = "calculate-area", 
         description = "Calculate the area of a rectangle",
         annotations = McpTool.McpAnnotations(
             title = "Rectangle Area Calculator",
             readOnlyHint = true,
             destructiveHint = false,
             idempotentHint = true
         ))
public AreaResult calculateRectangleArea(
        @McpToolParam(description = "Width", required = true) double width,
        @McpToolParam(description = "Height", required = true) double height) {
    
    return new AreaResult(width * height, "square units");
}
```

#### С контекстом запроса

Инструменты могут получить доступ к контексту запроса для расширенных операций:

```java
@McpTool(name = "process-data", description = "Process data with request context")
public String processData(
        McpSyncRequestContext context,
        @McpToolParam(description = "Data to process", required = true) String data) {
    
    // Send logging notification
    context.info("Processing data: " + data);
    
    // Send progress notification (using convenient method)
    context.progress(p -> p.progress(0.5).total(1.0).message("Processing..."));
    
    // Ping the client
    context.ping();
    
    return "Processed: " + data.toUpperCase();
}
```

#### Поддержка динамической схемы

Инструменты могут принимать `CallToolRequest` для обработки схемы времени выполнения:

```java
@McpTool(name = "flexible-tool", description = "Process dynamic schema")
public CallToolResult processDynamic(CallToolRequest request) {
    Map<String, Object> args = request.arguments();
    
    // Process based on runtime schema
    String result = "Processed " + args.size() + " arguments dynamically";
    
    return CallToolResult.builder()
        .addTextContent(result)
        .build();
}
```

#### Отслеживание прогресса

Инструменты могут получать токены прогресса для отслеживания длительных операций:

```java
@McpTool(name = "long-task", description = "Long-running task with progress")
public String performLongTask(
        McpSyncRequestContext context,
        @McpToolParam(description = "Task name", required = true) String taskName) {
    
    // Access progress token from context
    String progressToken = context.request().progressToken();
    
    if (progressToken != null) {
        context.progress(p -> p.progress(0.0).total(1.0).message("Starting task"));
        
        // Perform work...
        
        context.progress(p -> p.progress(1.0).total(1.0).message("Task completed"));
    }
    
    return "Task " + taskName + " completed";
}
```

### @McpResource

Аннотация `@McpResource` обеспечивает доступ к ресурсам через шаблоны URI.

#### Основное использование

```java
@Component
public class ResourceProvider {

    @McpResource(
        uri = "config://{key}", 
        name = "Configuration", 
        description = "Provides configuration data")
    public String getConfig(String key) {
        return configData.get(key);
    }
}
```

#### С ReadResourceResult

```java
@McpResource(
    uri = "user-profile://{username}", 
    name = "User Profile", 
    description = "Provides user profile information")
public ReadResourceResult getUserProfile(String username) {
    String profileData = loadUserProfile(username);
    
    return new ReadResourceResult(List.of(
        new TextResourceContents(
            "user-profile://" + username,
            "application/json", 
            profileData)
    ));
}
```

#### С контекстом запроса

```java
@McpResource(
    uri = "data://{id}", 
    name = "Data Resource", 
    description = "Resource with request context")
public ReadResourceResult getData(
        McpSyncRequestContext context, 
        String id) {
    
    // Send logging notification using convenient method
    context.info("Accessing resource: " + id);
    
    // Ping the client
    context.ping();
    
    String data = fetchData(id);
    
    return new ReadResourceResult(List.of(
        new TextResourceContents("data://" + id, "text/plain", data)
    ));
}
```

### @McpPrompt

Аннотация `@McpPrompt` генерирует подсказки для взаимодействия с ИИ.

#### Основное использование

```java
@Component
public class PromptProvider {

    @McpPrompt(
        name = "greeting", 
        description = "Generate a greeting message")
    public GetPromptResult greeting(
            @McpArg(name = "name", description = "User's name", required = true) 
            String name) {
        
        String message = "Hello, " + name + "! How can I help you today?";
        
        return new GetPromptResult(
            "Greeting",
            List.of(new PromptMessage(Role.ASSISTANT, new TextContent(message)))
        );
    }
}
```

#### С необязательными аргументами

```java
@McpPrompt(
    name = "personalized-message",
    description = "Generate a personalized message")
public GetPromptResult personalizedMessage(
        @McpArg(name = "name", required = true) String name,
        @McpArg(name = "age", required = false) Integer age,
        @McpArg(name = "interests", required = false) String interests) {
    
    StringBuilder message = new StringBuilder();
    message.append("Hello, ").append(name).append("!\n\n");
    
    if (age != null) {
        message.append("At ").append(age).append(" years old, ");
        // Add age-specific content
    }
    
    if (interests != null && !interests.isEmpty()) {
        message.append("Your interest in ").append(interests);
        // Add interest-specific content
    }
    
    return new GetPromptResult(
        "Personalized Message",
        List.of(new PromptMessage(Role.ASSISTANT, new TextContent(message.toString())))
    );
}
```

### @McpComplete

Аннотация `@McpComplete` обеспечивает функцию автозаполнения подсказок.

#### Основное использование

```java
@Component
public class CompletionProvider {

    @McpComplete(prompt = "city-search")
    public List<String> completeCityName(String prefix) {
        return cities.stream()
            .filter(city -> city.toLowerCase().startsWith(prefix.toLowerCase()))
            .limit(10)
            .toList();
    }
}
```

#### С CompleteRequest.CompleteArgument

```java
@McpComplete(prompt = "travel-planner")
public List<String> completeTravelDestination(CompleteRequest.CompleteArgument argument) {
    String prefix = argument.value().toLowerCase();
    String argumentName = argument.name();
    
    // Different completions based on argument name
    if ("city".equals(argumentName)) {
        return completeCities(prefix);
    } else if ("country".equals(argumentName)) {
        return completeCountries(prefix);
    }
    
    return List.of();
}
```

#### С полным результатом

```java
@McpComplete(prompt = "code-completion")
public CompleteResult completeCode(String prefix) {
    List<String> completions = generateCodeCompletions(prefix);
    
    return new CompleteResult(
        new CompleteResult.CompleteCompletion(
            completions,
            completions.size(),  // total
            hasMoreCompletions   // hasMore flag
        )
    );
}
```

## Реализации без сохранения состояния и реализации с сохранением состояния

### Единый контекст запроса (рекомендуется)

Используйте `McpSyncRequestContext` или `McpAsyncRequestContext` для унифицированного интерфейса, который работает как с операциями с сохранением состояния, так и с операциями без него:

```java
public record UserInfo(String name, String email, int age) {}

@McpTool(name = "unified-tool", description = "Tool with unified request context")
public String unifiedTool(
        McpSyncRequestContext context,
        @McpToolParam(description = "Input", required = true) String input) {
    
    // Access request and metadata
    String progressToken = context.request().progressToken();
    
    // Logging with convenient methods
    context.info("Processing: " + input);
    
    // Progress notifications (Note client should set a progress token 
    // with its request to be able to receive progress updates)
    context.progress(50); // Simple percentage    
    
    // Ping client
    context.ping();
    
    // Check capabilities before using
    if (context.elicitEnabled()) {
        // Request user input (only in stateful mode)
        StructuredElicitResult<UserInfo> elicitResult = context.elicit(UserInfo.class);
        if (elicitResult.action() == ElicitResult.Action.ACCEPT) {
            // Use elicited data
        }
    }
    
    if (context.sampleEnabled()) {
        // Request LLM sampling (only in stateful mode)
        CreateMessageResult samplingResult = context.sample("Generate response");
        // Use sampling result
    }
    
    return "Processed with unified context";
}
```

### Простые операции (без контекста)

Для простых операций вы можете полностью опустить параметры контекста:

```java
@McpTool(name = "simple-add", description = "Simple addition")
public int simpleAdd(
        @McpToolParam(description = "First number", required = true) int a,
        @McpToolParam(description = "Second number", required = true) int b) {
    return a + b;
}
```

### Легкий вариант без сохранения состояния (с McpTransportContext)

Для операций без сохранения состояния, когда вам нужен минимальный транспортный контекст:

```java
@McpTool(name = "stateless-tool", description = "Stateless with transport context")
public String statelessTool(
        McpTransportContext context,
        @McpToolParam(description = "Input", required = true) String input) {
    // Access transport-level context only
    // No bidirectional operations (roots, elicitation, sampling)
    return "Processed: " + input;
}
```

[ВАЖНЫЙ]
***Серверы без сохранения состояния не поддерживают двунаправленные операции:***

Поэтому методы, использующие `McpSyncRequestContext` или `McpAsyncRequestContext` в режиме без сохранения состояния, игнорируются. 

## Фильтрация методов по типу сервера

Платформа аннотаций MCP автоматически фильтрует аннотированные методы на основе типа сервера и характеристик метода. Это гарантирует, что для каждой конфигурации сервера будут зарегистрированы только соответствующие методы.
Для каждого отфильтрованного метода регистрируется предупреждение, которое помогает при отладке.

### Синхронная и асинхронная фильтрация

#### Синхронные серверы

Синхронные серверы (настроенные с помощью `spring.ai.mcp.server.type=SYNC`) используют синхронных поставщиков, которые:

- ***Accept*** методы с нереактивными типами возврата:
  - Примитивные типы (`int`, `double`, `boolean`)
  - Типы объектов (`String`, `Integer`, пользовательские POJO)
  - Типы MCP (`CallToolResult`, `ReadResourceResult`, `GetPromptResult`, `CompleteResult`)
  - Коллекции (`List<String>`, `Map<String, Object>`)

- ***Отфильтровать*** методы с реактивными типами возврата:
  - `Mono<T>`
  - `Flux<T>`
  - `Publisher<T>`

```java
@Component
public class SyncTools {
    
    @McpTool(name = "sync-tool", description = "Synchronous tool")
    public String syncTool(String input) {
        // This method WILL be registered on sync servers
        return "Processed: " + input;
    }
    
    @McpTool(name = "async-tool", description = "Async tool")
    public Mono<String> asyncTool(String input) {
        // This method will be FILTERED OUT on sync servers
        // A warning will be logged
        return Mono.just("Processed: " + input);
    }
}
```

#### Асинхронные серверы

Асинхронные серверы (настроенные с помощью `spring.ai.mcp.server.type=ASYNC`) используют асинхронных поставщиков, которые:

- ***Accept*** методы с реактивными типами возврата:
  - `Mono<T>` (для одиночных результатов)
  - `Flux<T>` (для потоковой передачи результатов)
  - `Publisher<T>` (общий реактивный тип)

- ***Отфильтровать*** методы с нереактивными типами возврата:
  - Примитивные типы
  - Типы объектов
  - Коллекции
  - Типы результатов MCP

```java
@Component
public class AsyncTools {
    
    @McpTool(name = "async-tool", description = "Async tool")
    public Mono<String> asyncTool(String input) {
        // This method WILL be registered on async servers
        return Mono.just("Processed: " + input);
    }
    
    @McpTool(name = "sync-tool", description = "Sync tool")
    public String syncTool(String input) {
        // This method will be FILTERED OUT on async servers
        // A warning will be logged
        return "Processed: " + input;
    }
}
```

### Фильтрация с сохранением состояния и фильтрация без сохранения состояния

#### Серверы с отслеживанием состояния

Серверы с отслеживанием состояния поддерживают двунаправленную связь и принимают методы с:

- ***Параметры двунаправленного контекста***:
  - `McpSyncRequestContext` (для операций синхронизации)
  - `McpAsyncRequestContext` (для асинхронных операций)
  - `McpSyncServerExchange` (старый вариант, для операций синхронизации)
  - `McpAsyncServerExchange` (устаревший, для асинхронных операций)

- Поддержка двунаправленных операций:
  - `roots()` — доступ к корневым каталогам
  - `elicit()` – запросить ввод данных пользователем
  - `sample()` – запрос выборки LLM

```java
@Component
public class StatefulTools {
    
    @McpTool(name = "interactive-tool", description = "Tool with bidirectional operations")
    public String interactiveTool(
            McpSyncRequestContext context,
            @McpToolParam(description = "Input", required = true) String input) {
        
        // This method WILL be registered on stateful servers
        // Can use elicitation, sampling, roots
        if (context.sampleEnabled()) {
            var samplingResult = context.sample("Generate response");
            // Process sampling result...
        }
        
        return "Processed with context";
    }
}
```

#### Серверы без сохранения состояния

Серверы без отслеживания состояния оптимизированы для простых шаблонов запросов и ответов и:

- ***Отфильтровать*** методы с двунаправленными параметрами контекста:
  - Методы с `McpSyncRequestContext` пропускаются.
  - Методы с `McpAsyncRequestContext` пропускаются.
  - Методы с `McpSyncServerExchange` пропускаются.
  - Методы с `McpAsyncServerExchange` пропускаются.
  - Предупреждение регистрируется для каждого отфильтрованного метода.

- ***Принять*** методы с помощью:
  - `McpTransportContext` (облегченный контекст без сохранения состояния)
  - Нет параметра контекста вообще
  - Только обычные параметры `@McpToolParam`

- ***Не*** поддерживать двунаправленные операции:
  - `roots()` – Недоступно
  - `elicit()` – Недоступно
  - `sample()` – Недоступно

```java
@Component
public class StatelessTools {
    
    @McpTool(name = "simple-tool", description = "Simple stateless tool")
    public String simpleTool(@McpToolParam(description = "Input") String input) {
        // This method WILL be registered on stateless servers
        return "Processed: " + input;
    }
    
    @McpTool(name = "context-tool", description = "Tool with transport context")
    public String contextTool(
            McpTransportContext context,
            @McpToolParam(description = "Input") String input) {
        // This method WILL be registered on stateless servers
        return "Processed: " + input;
    }
    
    @McpTool(name = "bidirectional-tool", description = "Tool with bidirectional context")
    public String bidirectionalTool(
            McpSyncRequestContext context,
            @McpToolParam(description = "Input") String input) {
        // This method will be FILTERED OUT on stateless servers
        // A warning will be logged
        return "Processed with sampling";
    }
}
```

### Сводка фильтрации

[cols="1,2,2"]
| Тип сервера | Принятые методы | Фильтрованные методы |
| --- |---|---|
| **Синхронизировать с отслеживанием состояния** |  |  |
| Нереактивные возвраты + двунаправленный контекст |  |  |
| Реактивная отдача (моно/флюкс) |  |  |
| **Асинхронное управление состоянием** |  |  |
| Реактивный возврат (Mono/Flux) + двунаправленный контекст |  |  |
| Нереактивная доходность |  |  |
| **Синхронизация без сохранения состояния** |  |  |
| Нереактивные возвраты + отсутствие двунаправленного контекста |  |  |
| Реактивный возвращает ИЛИ двунаправленные параметры контекста |  |  |
| **Асинхронный без сохранения состояния** |  |  |
| Реактивные возвраты (Mono/Flux) + отсутствие двунаправленного контекста |  |  |
| Нереактивные возвраты ИЛИ двунаправленные параметры контекста |  |  |

[КОНЧИК]
***Рекомендации по фильтрации методов:***

1. ***Сохраняйте методы в соответствии*** с типом вашего сервера — используйте методы синхронизации для серверов синхронизации, асинхронные для асинхронных серверов.
2. ***Разделите реализации с сохранением и без сохранения состояния*** в разные классы для ясности.
3. ***Проверяйте журналы*** во время запуска на предмет предупреждений отфильтрованных методов.
4. ***Используйте правильный контекст*** – `McpSyncRequestContext`/`McpAsyncRequestContext` для сохранения состояния, `McpTransportContext` для состояния без сохранения состояния.
5. ***Протестируйте оба режима***, если вы поддерживаете развертывания как с отслеживанием, так и без отслеживания состояния.

## Асинхронная поддержка

Все аннотации сервера поддерживают асинхронную реализацию с использованием Reactor:

```java
@Component
public class AsyncTools {

    @McpTool(name = "async-fetch", description = "Fetch data asynchronously")
    public Mono<String> asyncFetch(
            @McpToolParam(description = "URL", required = true) String url) {
        
        return Mono.fromCallable(() -> {
            // Simulate async operation
            return fetchFromUrl(url);
        }).subscribeOn(Schedulers.boundedElastic());
    }

    @McpResource(uri = "async-data://{id}", name = "Async Data")
    public Mono<ReadResourceResult> asyncResource(String id) {
        return Mono.fromCallable(() -> {
            String data = loadData(id);
            return new ReadResourceResult(List.of(
                new TextResourceContents("async-data://" + id, "text/plain", data)
            ));
        }).delayElements(Duration.ofMillis(100));
    }
}
```

## Интеграция Spring Boot

При автоматической настройке Spring Boot аннотированные bean-компоненты автоматически обнаруживаются и регистрируются:

```java
@SpringBootApplication
public class McpServerApplication {
    public static void main(String[] args) {
        SpringApplication.run(McpServerApplication.class, args);
    }
}

@Component
public class MyMcpTools {
    // Your @McpTool annotated methods
}

@Component
public class MyMcpResources {
    // Your @McpResource annotated methods
}
```

Автоматическая настройка позволит:

1. Сканирование bean-компонентов с аннотациями MCP
2. Создайте соответствующие спецификации
3. Зарегистрируйте их на сервере MCP.
4. Обработка как синхронных, так и асинхронных реализаций на основе конфигурации.

## Свойства конфигурации

Настройте сканер аннотаций сервера:

```yaml
spring:
  ai:
    mcp:
      server:
        type: SYNC  # or ASYNC
        annotation-scanner:
          enabled: true
```

## Дополнительные ресурсы

- [Обзор аннотаций MCP](api/mcp/mcp-annotations-overview.md)
- [Аннотации клиента](api/mcp/mcp-annotations-client.md)
- [Специальные параметры](api/mcp/mcp-annotations-special-params.md)
- [Стартер загрузки сервера MCP](api/mcp/mcp-server-boot-starter-docs.md)
