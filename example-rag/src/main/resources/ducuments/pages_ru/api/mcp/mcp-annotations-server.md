# Аннотации MCP Server

Аннотации MCP Server предоставляют декларативный способ реализации функциональности сервера MCP с использованием аннотаций Java. Эти аннотации упрощают создание инструментов, ресурсов, подсказок и обработчиков завершения.

## Аннотации сервера

### @McpTool

Аннотация `@McpTool` помечает метод как реализацию инструмента MCP с автоматической генерацией схемы JSON.

#### Основное использование

```java
@Component
public class CalculatorTools {

    @McpTool(name = "add", description = "Сложить два числа")
    public int add(
            @McpToolParam(description = "Первое число", required = true) int a,
            @McpToolParam(description = "Второе число", required = true) int b) {
        return a + b;
    }
}
```

#### Расширенные функции

```java
@McpTool(name = "calculate-area", 
         description = "Вычислить площадь прямоугольника",
         annotations = McpTool.McpAnnotations(
             title = "Калькулятор площади прямоугольника",
             readOnlyHint = true,
             destructiveHint = false,
             idempotentHint = true
         ))
public AreaResult calculateRectangleArea(
        @McpToolParam(description = "Ширина", required = true) double width,
        @McpToolParam(description = "Высота", required = true) double height) {
    
    return new AreaResult(width * height, "квадратные единицы");
}
```

#### С контекстом запроса

Инструменты могут получать доступ к контексту запроса для выполнения сложных операций:

```java
@McpTool(name = "process-data", description = "Обработать данные с контекстом запроса")
public String processData(
        McpSyncRequestContext context,
        @McpToolParam(description = "Данные для обработки", required = true) String data) {
    
    // Отправить уведомление о логировании
    context.info("Обработка данных: " + data);
    
    // Отправить уведомление о прогрессе (с использованием удобного метода)
    context.progress(p -> p.progress(0.5).total(1.0).message("Обработка..."));
    
    // Пинговать клиента
    context.ping();
    
    return "Обработано: " + data.toUpperCase();
}
```

#### Поддержка динамической схемы

Инструменты могут принимать `CallToolRequest` для обработки схемы во время выполнения:

```java
@McpTool(name = "flexible-tool", description = "Обработка динамической схемы")
public CallToolResult processDynamic(CallToolRequest request) {
    Map<String, Object> args = request.arguments();
    
    // Обработка на основе схемы во время выполнения
    String result = "Обработано " + args.size() + " аргументов динамически";
    
    return CallToolResult.builder()
        .addTextContent(result)
        .build();
}
```

#### Отслеживание прогресса

Инструменты могут получать токены прогресса для отслеживания длительных операций:

```java
@McpTool(name = "long-task", description = "Длительная задача с отслеживанием прогресса")
public String performLongTask(
        McpSyncRequestContext context,
        @McpToolParam(description = "Название задачи", required = true) String taskName) {
    
    // Получить токен прогресса из контекста
    String progressToken = context.request().progressToken();
    
    if (progressToken != null) {
        context.progress(p -> p.progress(0.0).total(1.0).message("Запуск задачи"));
        
        // Выполнить работу...
        
        context.progress(p -> p.progress(1.0).total(1.0).message("Задача завершена"));
    }
    
    return "Задача " + taskName + " завершена";
}
```

### @McpResource

Аннотация `@McpResource` предоставляет доступ к ресурсам через шаблоны URI.

#### Основное использование

```java
@Component
public class ResourceProvider {

    @McpResource(
        uri = "config://{key}", 
        name = "Конфигурация", 
        description = "Предоставляет данные конфигурации")
    public String getConfig(String key) {
        return configData.get(key);
    }
}
```

#### С ReadResourceResult```markdown
```java
@McpResource(
    uri = "user-profile://{username}", 
    name = "Профиль пользователя", 
    description = "Предоставляет информацию о профиле пользователя")
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
    name = "Ресурс данных", 
    description = "Ресурс с контекстом запроса")
public ReadResourceResult getData(
        McpSyncRequestContext context, 
        String id) {
    
    // Отправить уведомление о логировании с использованием удобного метода
    context.info("Доступ к ресурсу: " + id);
    
    // Пинговать клиента
    context.ping();
    
    String data = fetchData(id);
    
    return new ReadResourceResult(List.of(
        new TextResourceContents("data://" + id, "text/plain", data)
    ));
}
```

### @McpPrompt

Аннотация `@McpPrompt` генерирует сообщения подсказок для взаимодействия с ИИ.

#### Основное использование

```java
@Component
public class PromptProvider {

    @McpPrompt(
        name = "greeting", 
        description = "Сгенерировать приветственное сообщение")
    public GetPromptResult greeting(
            @McpArg(name = "name", description = "Имя пользователя", required = true) 
            String name) {
        
        String message = "Здравствуйте, " + name + "! Как я могу помочь вам сегодня?";
        
        return new GetPromptResult(
            "Приветствие",
            List.of(new PromptMessage(Role.ASSISTANT, new TextContent(message)))
        );
    }
}
```

#### С необязательными аргументами

```java
@McpPrompt(
    name = "personalized-message",
    description = "Сгенерировать персонализированное сообщение")
public GetPromptResult personalizedMessage(
        @McpArg(name = "name", required = true) String name,
        @McpArg(name = "age", required = false) Integer age,
        @McpArg(name = "interests", required = false) String interests) {
    
    StringBuilder message = new StringBuilder();
    message.append("Здравствуйте, ").append(name).append("!\n\n");
    
    if (age != null) {
        message.append("В возрасте ").append(age).append(" лет, ");
        // Добавить контент, связанный с возрастом
    }
    
    if (interests != null && !interests.isEmpty()) {
        message.append("Ваш интерес к ").append(interests);
        // Добавить контент, связанный с интересами
    }
    
    return new GetPromptResult(
        "Персонализированное сообщение",
        List.of(new PromptMessage(Role.ASSISTANT, new TextContent(message.toString())))
    );
}
```

### @McpComplete

Аннотация `@McpComplete` предоставляет функциональность автозаполнения для подсказок.

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
    
    // Разные варианты автозаполнения в зависимости от имени аргумента
    if ("city".equals(argumentName)) {
        return completeCities(prefix);
    } else if ("country".equals(argumentName)) {
        return completeCountries(prefix);
    }
    
    return List.of();
}
```

#### С CompleteResult
``````java
@McpComplete(prompt = "code-completion")
public CompleteResult completeCode(String prefix) {
    List<String> completions = generateCodeCompletions(prefix);
    
    return new CompleteResult(
        new CompleteResult.CompleteCompletion(
            completions,
            completions.size(),  // всего
            hasMoreCompletions   // флаг hasMore
        )
    );
}
```

## Stateless vs Stateful Implementations

### Unified Request Context (Рекомендуется)

Используйте `McpSyncRequestContext` или `McpAsyncRequestContext` для унифицированного интерфейса, который работает как с состоянием, так и без:

```java
public record UserInfo(String name, String email, int age) {}

@McpTool(name = "unified-tool", description = "Инструмент с унифицированным контекстом запроса")
public String unifiedTool(
        McpSyncRequestContext context,
        @McpToolParam(description = "Ввод", required = true) String input) {
    
    // Доступ к запросу и метаданным
    String progressToken = context.request().progressToken();
    
    // Логирование с удобными методами
    context.info("Обработка: " + input);
    
    // Уведомления о прогрессе (Обратите внимание, что клиент должен установить токен прогресса 
    // в своем запросе, чтобы получать обновления о прогрессе)
    context.progress(50); // Простая процентная доля    
    
    // Пинг клиента
    context.ping();
    
    // Проверьте возможности перед использованием
    if (context.elicitEnabled()) {
        // Запрос пользовательского ввода (только в режиме с состоянием)
        StructuredElicitResult<UserInfo> elicitResult = context.elicit(UserInfo.class);
        if (elicitResult.action() == ElicitResult.Action.ACCEPT) {
            // Используйте полученные данные
        }
    }
    
    if (context.sampleEnabled()) {
        // Запрос выборки LLM (только в режиме с состоянием)
        CreateMessageResult samplingResult = context.sample("Сгенерировать ответ");
        // Используйте результат выборки
    }
    
    return "Обработано с унифицированным контекстом";
}
```

### Простые операции (без контекста)

Для простых операций вы можете полностью опустить параметры контекста:

```java
@McpTool(name = "simple-add", description = "Простое сложение")
public int simpleAdd(
        @McpToolParam(description = "Первое число", required = true) int a,
        @McpToolParam(description = "Второе число", required = true) int b) {
    return a + b;
}
```

### Легковесный Stateless (с McpTransportContext)

Для безсостояния операций, где вам нужен минимальный транспортный контекст:

```java
@McpTool(name = "stateless-tool", description = "Безсостояние с транспортным контекстом")
public String statelessTool(
        McpTransportContext context,
        @McpToolParam(description = "Ввод", required = true) String input) {
    // Доступ только к транспортному контексту
    // Нет двусторонних операций (корни, извлечение, выборка)
    return "Обработано: " + input;
}
```

[ВАЖНО]
**Безсостояние серверы не поддерживают двусторонние операции:**

Поэтому методы, использующие `McpSyncRequestContext` или `McpAsyncRequestContext` в безсостоянии, игнорируются.

## Фильтрация методов по типу сервера

Фреймворк аннотаций MCP автоматически фильтрует аннотированные методы на основе типа сервера и характеристик метода. Это гарантирует, что только соответствующие методы регистрируются для каждой конфигурации сервера.
Предупреждение записывается для каждого отфильтрованного метода, чтобы помочь с отладкой.

### Синхронная vs Асинхронная фильтрация

#### Синхронные серверы
``````markdown
Синхронные серверы (настроенные с `spring.ai.mcp.server.type=SYNC`) используют синхронные провайдеры, которые:

- **Принимают** методы с нереактивными типами возвращаемых значений:
  - Примитивные типы (`int`, `double`, `boolean`)
  - Объектные типы (`String`, `Integer`, пользовательские POJO)
  - Типы MCP (`CallToolResult`, `ReadResourceResult`, `GetPromptResult`, `CompleteResult`)
  - Коллекции (`List<String>`, `Map<String, Object>`)

- **Фильтруют** методы с реактивными типами возвращаемых значений:
  - `Mono<T>`
  - `Flux<T>`
  - `Publisher<T>`

```java
@Component
public class SyncTools {
    
    @McpTool(name = "sync-tool", description = "Синхронный инструмент")
    public String syncTool(String input) {
        // Этот метод БУДЕТ зарегистрирован на синхронных серверах
        return "Обработано: " + input;
    }
    
    @McpTool(name = "async-tool", description = "Асинхронный инструмент")
    public Mono<String> asyncTool(String input) {
        // Этот метод будет ОТФИЛЬТРОВАН на синхронных серверах
        // Будет записано предупреждение
        return Mono.just("Обработано: " + input);
    }
}
```

#### Асинхронные серверы

Асинхронные серверы (настроенные с `spring.ai.mcp.server.type=ASYNC`) используют асинхронные провайдеры, которые:

- **Принимают** методы с реактивными типами возвращаемых значений:
  - `Mono<T>` (для одиночных результатов)
  - `Flux<T>` (для потоковых результатов)
  - `Publisher<T>` (универсальный реактивный тип)

- **Фильтруют** методы с нереактивными типами возвращаемых значений:
  - Примитивные типы
  - Объектные типы
  - Коллекции
  - Типы результатов MCP

```java
@Component
public class AsyncTools {
    
    @McpTool(name = "async-tool", description = "Асинхронный инструмент")
    public Mono<String> asyncTool(String input) {
        // Этот метод БУДЕТ зарегистрирован на асинхронных серверах
        return Mono.just("Обработано: " + input);
    }
    
    @McpTool(name = "sync-tool", description = "Синхронный инструмент")
    public String syncTool(String input) {
        // Этот метод будет ОТФИЛЬТРОВАН на асинхронных серверах
        // Будет записано предупреждение
        return "Обработано: " + input;
    }
}
```

### Состояние и безсостояние фильтрации

#### Состояние серверов

Состояние серверов поддерживают двустороннюю связь и принимают методы с:

- **Параметрами контекста для двусторонней связи**:
  - `McpSyncRequestContext` (для синхронных операций)
  - `McpAsyncRequestContext` (для асинхронных операций)
  - `McpSyncServerExchange` (устаревший, для синхронных операций)
  - `McpAsyncServerExchange` (устаревший, для асинхронных операций)

- Поддержка двусторонних операций:
  - `roots()` - Доступ к корневым директориям
  - `elicit()` - Запрос ввода от пользователя
  - `sample()` - Запрос выборки LLM

```java
@Component
public class StatefulTools {
    
    @McpTool(name = "interactive-tool", description = "Инструмент с двусторонними операциями")
    public String interactiveTool(
            McpSyncRequestContext context,
            @McpToolParam(description = "Ввод", required = true) String input) {
        
        // Этот метод БУДЕТ зарегистрирован на серверах с состоянием
        // Может использовать elicitation, sampling, roots
        if (context.sampleEnabled()) {
            var samplingResult = context.sample("Сгенерировать ответ");
            // Обработка результата выборки...
        }
        
        return "Обработано с контекстом";
    }
}
```

#### Безсостояние серверов
```Stateless-серверы оптимизированы для простых паттернов запрос-ответ и:

- **Фильтруют** методы с двунаправленными параметрами контекста:
  - Методы с `McpSyncRequestContext` пропускаются
  - Методы с `McpAsyncRequestContext` пропускаются
  - Методы с `McpSyncServerExchange` пропускаются
  - Методы с `McpAsyncServerExchange` пропускаются
  - Для каждого отфильтрованного метода записывается предупреждение

- **Принимают** методы с:
  - `McpTransportContext` (легковесный статeless контекст)
  - Отсутствием параметров контекста
  - Только обычными параметрами `@McpToolParam`

- **Не** поддерживают двунаправленные операции:
  - `roots()` - Недоступно
  - `elicit()` - Недоступно
  - `sample()` - Недоступно

```java
@Component
public class StatelessTools {
    
    @McpTool(name = "simple-tool", description = "Простой статeless инструмент")
    public String simpleTool(@McpToolParam(description = "Входные данные") String input) {
        // Этот метод БУДЕТ зарегистрирован на stateless серверах
        return "Обработано: " + input;
    }
    
    @McpTool(name = "context-tool", description = "Инструмент с транспортным контекстом")
    public String contextTool(
            McpTransportContext context,
            @McpToolParam(description = "Входные данные") String input) {
        // Этот метод БУДЕТ зарегистрирован на stateless серверах
        return "Обработано: " + input;
    }
    
    @McpTool(name = "bidirectional-tool", description = "Инструмент с двунаправленным контекстом")
    public String bidirectionalTool(
            McpSyncRequestContext context,
            @McpToolParam(description = "Входные данные") String input) {
        // Этот метод будет ОТФИЛЬТРОВАН на stateless серверах
        // Будет записано предупреждение
        return "Обработано с выборкой";
    }
}
```

### Резюме фильтрации

| Тип сервера | Принятые методы | Отфильтрованные методы |
| --- | --- | --- |
| **Синхронный Stateful** |  |  |
| Нереактивные возвращаемые значения + двунаправленный контекст |  |  |
| Реактивные возвращаемые значения (Mono/Flux) |  |  |
| **Асинхронный Stateful** |  |  |
| Реактивные возвращаемые значения (Mono/Flux) + двунаправленный контекст |  |  |
| Нереактивные возвращаемые значения |  |  |
| **Синхронный Stateless** |  |  |
| Нереактивные возвращаемые значения + без двунаправленного контекста |  |  |
| Реактивные возвращаемые значения ИЛИ параметры двунаправленного контекста |  |  |
| **Асинхронный Stateless** |  |  |
| Реактивные возвращаемые значения (Mono/Flux) + без двунаправленного контекста |  |  |
| Нереактивные возвращаемые значения ИЛИ параметры двунаправленного контекста |  |  |

[TIP]
**Лучшие практики фильтрации методов:**

1. **Соблюдайте соответствие методов** с вашим типом сервера - используйте синхронные методы для синхронных серверов, асинхронные для асинхронных серверов
2. **Разделяйте stateful и stateless** реализации на разные классы для ясности
3. **Проверяйте логи** во время запуска на наличие предупреждений об отфильтрованных методах
4. **Используйте правильный контекст** - `McpSyncRequestContext`/`McpAsyncRequestContext` для stateful, `McpTransportContext` для stateless
5. **Тестируйте оба режима**, если вы поддерживаете как stateful, так и stateless развертывания

## Поддержка асинхронности

Все аннотации сервера поддерживают асинхронные реализации с использованием Reactor:

```java
@Component
public class AsyncTools {

    @McpTool(name = "async-fetch", description = "Асинхронное получение данных")
    public Mono<String> asyncFetch(
            @McpToolParam(description = "URL", required = true) String url) {
        
        return Mono.fromCallable(() -> {
            // Симуляция асинхронной операции
            return fetchFromUrl(url);
        }).subscribeOn(Schedulers.boundedElastic());
    }

    @McpResource(uri = "async-data://{id}", name = "Асинхронные данные")
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

## Интеграция с Spring BootС автоматической конфигурацией Spring Boot аннотированные бины автоматически обнаруживаются и регистрируются:

```java
@SpringBootApplication
public class McpServerApplication {
    public static void main(String[] args) {
        SpringApplication.run(McpServerApplication.class, args);
    }
}

@Component
public class MyMcpTools {
    // Ваши методы с аннотацией @McpTool
}

@Component
public class MyMcpResources {
    // Ваши методы с аннотацией @McpResource
}
```

Автоматическая конфигурация будет:

1. Сканировать бины с аннотациями MCP
2. Создавать соответствующие спецификации
3. Регистрировать их на сервере MCP
4. Обрабатывать как синхронные, так и асинхронные реализации в зависимости от конфигурации

## Свойства конфигурации

Настройте сканер аннотаций сервера:

```yaml
spring:
  ai:
    mcp:
      server:
        type: SYNC  # или ASYNC
        annotation-scanner:
          enabled: true
```

## Дополнительные ресурсы

- xref:api/mcp/mcp-annotations-overview.adoc[Обзор аннотаций MCP]
- xref:api/mcp/mcp-annotations-client.adoc[Аннотации клиента]
- xref:api/mcp/mcp-annotations-special-params.adoc[Специальные параметры]
- xref:api/mcp/mcp-server-boot-starter-docs.adoc[Документация по MCP Server Boot Starter]
