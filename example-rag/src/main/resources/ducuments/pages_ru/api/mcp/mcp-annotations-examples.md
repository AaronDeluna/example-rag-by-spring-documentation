# Примеры аннотаций MCP

Эта страница предоставляет полные примеры использования аннотаций MCP в приложениях Spring AI.

## Полные примеры приложений

### Сервер простого калькулятора

Полный пример сервера MCP, предоставляющего инструменты для калькулятора:

```java
@SpringBootApplication
public class CalculatorServerApplication {
    public static void main(String[] args) {
        SpringApplication.run(CalculatorServerApplication.class, args);
    }
}

@Component
public class CalculatorTools {

    @McpTool(name = "add", description = "Сложить два числа")
    public double add(
            @McpToolParam(description = "Первое число", required = true) double a,
            @McpToolParam(description = "Второе число", required = true) double b) {
        return a + b;
    }

    @McpTool(name = "subtract", description = "Вычесть два числа")
    public double subtract(
            @McpToolParam(description = "Первое число", required = true) double a,
            @McpToolParam(description = "Второе число", required = true) double b) {
        return a - b;
    }

    @McpTool(name = "multiply", description = "Умножить два числа")
    public double multiply(
            @McpToolParam(description = "Первое число", required = true) double a,
            @McpToolParam(description = "Второе число", required = true) double b) {
        return a * b;
    }

    @McpTool(name = "divide", description = "Разделить два числа")
    public double divide(
            @McpToolParam(description = "Делимое", required = true) double dividend,
            @McpToolParam(description = "Делитель", required = true) double divisor) {
        if (divisor == 0) {
            throw new IllegalArgumentException("Деление на ноль");
        }
        return dividend / divisor;
    }

    @McpTool(name = "calculate-expression",
             description = "Вычислить сложное математическое выражение")
    public CallToolResult calculateExpression(
            CallToolRequest request,
            McpSyncRequestContext context) {

        Map<String, Object> args = request.arguments();
        String expression = (String) args.get("expression");

        // Используйте удобный метод логирования
        context.info("Вычисление: " + expression);

        try {
            double result = evaluateExpression(expression);
            return CallToolResult.builder()
                .addTextContent("Результат: " + result)
                .build();
        } catch (Exception e) {
            return CallToolResult.builder()
                .isError(true)
                .addTextContent("Ошибка: " + e.getMessage())
                .build();
        }
    }
}
```

Конфигурация:

```yaml
spring:
  ai:
    mcp:
      server:
        name: calculator-server
        version: 1.0.0
        type: SYNC
        protocol: SSE  # или STDIO, STREAMABLE
        capabilities:
          tool: true
          resource: true
          prompt: true
          completion: true
```

### Сервер обработки документов```markdown
Пример сервера обработки документов с ресурсами и подсказками:

```java
@Component
public class DocumentServer {

    private final Map<String, Document> documents = new ConcurrentHashMap<>();

    @McpResource(
        uri = "document://{id}",
        name = "Документ",
        description = "Доступ к сохранённым документам")
    public ReadResourceResult getDocument(String id, McpMeta meta) {
        Document doc = documents.get(id);

        if (doc == null) {
            return new ReadResourceResult(List.of(
                new TextResourceContents("document://" + id,
                    "text/plain", "Документ не найден")
            ));
        }

        // Проверка прав доступа из метаданных
        String accessLevel = (String) meta.get("accessLevel");
        if ("restricted".equals(doc.getClassification()) &&
            !"admin".equals(accessLevel)) {
            return new ReadResourceResult(List.of(
                new TextResourceContents("document://" + id,
                    "text/plain", "Доступ запрещён")
            ));
        }

        return new ReadResourceResult(List.of(
            new TextResourceContents("document://" + id,
                doc.getMimeType(), doc.getContent())
        ));
    }

    @McpTool(name = "analyze-document",
             description = "Анализировать содержимое документа")
    public String analyzeDocument(
            McpSyncRequestContext context,
            @McpToolParam(description = "ID документа", required = true) String docId,
            @McpToolParam(description = "Тип анализа", required = false) String type) {

        Document doc = documents.get(docId);
        if (doc == null) {
            return "Документ не найден";
        }

        // Получение токена прогресса из контекста
        String progressToken = context.request().progressToken();

        if (progressToken != null) {
            context.progress(p -> p.progress(0.0).total(1.0).message("Начало анализа"));
        }

        // Выполнение анализа
        String analysisType = type != null ? type : "summary";
        String result = performAnalysis(doc, analysisType);

        if (progressToken != null) {
            context.progress(p -> p.progress(1.0).total(1.0).message("Анализ завершён"));
        }

        return result;
    }

    @McpPrompt(
        name = "document-summary",
        description = "Сгенерировать подсказку для резюме документа")
    public GetPromptResult documentSummaryPrompt(
            @McpArg(name = "docId", required = true) String docId,
            @McpArg(name = "length", required = false) String length) {

        Document doc = documents.get(docId);
        if (doc == null) {
            return new GetPromptResult("Ошибка",
                List.of(new PromptMessage(Role.SYSTEM,
                    new TextContent("Документ не найден"))));
        }

        String promptText = String.format(
            "Пожалуйста, резюмируйте следующий документ в %s:\n\n%s",
            length != null ? length : "нескольких абзацах",
            doc.getContent()
        );

        return new GetPromptResult("Резюме документа",
            List.of(new PromptMessage(Role.USER, new TextContent(promptText))));
    }

    @McpComplete(prompt = "document-summary")
    public List<String> completeDocumentId(String prefix) {
        return documents.keySet().stream()
            .filter(id -> id.startsWith(prefix))
            .sorted()
            .limit(10)
            .toList();
    }
}
```

### MCP Клиент с обработчиками
```# Полное приложение клиента MCP с различными обработчиками:

```java
@SpringBootApplication
public class McpClientApplication {
    public static void main(String[] args) {
        SpringApplication.run(McpClientApplication.class, args);
    }
}

@Component
public class ClientHandlers {

    private final Logger logger = LoggerFactory.getLogger(ClientHandlers.class);
    private final ProgressTracker progressTracker = new ProgressTracker();
    private final ChatModel chatModel;

    public ClientHandlers(@Lazy ChatModel chatModel) {
        this.chatModel = chatModel;
    }

    @McpLogging(clients = "server1")
    public void handleLogging(LoggingMessageNotification notification) {
        switch (notification.level()) {
            case ERROR:
                logger.error("[MCP] {} - {}", notification.logger(), notification.data());
                break;
            case WARNING:
                logger.warn("[MCP] {} - {}", notification.logger(), notification.data());
                break;
            case INFO:
                logger.info("[MCP] {} - {}", notification.logger(), notification.data());
                break;
            default:
                logger.debug("[MCP] {} - {}", notification.logger(), notification.data());
        }
    }

    @McpSampling(clients = "server1")
    public CreateMessageResult handleSampling(CreateMessageRequest request) {
        // Используйте Spring AI ChatModel для выборки
        List<Message> messages = request.messages().stream()
            .map(msg -> {
                if (msg.role() == Role.USER) {
                    return new UserMessage(((TextContent) msg.content()).text());
                } else {
                    return AssistantMessage.builder()
                        .content(((TextContent) msg.content()).text())
                        .build();
                }
            })
            .toList();

        ChatResponse response = chatModel.call(new Prompt(messages));

        return CreateMessageResult.builder()
            .role(Role.ASSISTANT)
            .content(new TextContent(response.getResult().getOutput().getText()))
            .model(request.modelPreferences().hints().get(0).name())
            .build();
    }

    @McpElicitation(clients = "server1")
    public ElicitResult handleElicitation(ElicitRequest request) {
        // В реальном приложении это будет показывать диалоговое окно UI
        Map<String, Object> userData = new HashMap<>();

        logger.info("Запрошена элицитация: {}", request.message());

        // Симуляция ввода пользователя на основе схемы
        Map<String, Object> schema = request.requestedSchema();
        if (schema != null && schema.containsKey("properties")) {
            @SuppressWarnings("unchecked")
            Map<String, Object> properties = (Map<String, Object>) schema.get("properties");

            properties.forEach((key, value) -> {
                // В реальном приложении запросить у пользователя каждое поле
                userData.put(key, getDefaultValueForProperty(key, value));
            });
        }

        return new ElicitResult(ElicitResult.Action.ACCEPT, userData);
    }

    @McpProgress(clients = "server1")
    public void handleProgress(ProgressNotification notification) {
        progressTracker.update(
            notification.progressToken(),
            notification.progress(),
            notification.total(),
            notification.message()
        );

        // Обновить UI или отправить уведомление по веб-сокету
        broadcastProgress(notification);
    }

    @McpToolListChanged(clients = "server1")
    public void handleServer1ToolsChanged(List<McpSchema.Tool> tools) {
        logger.info("Инструменты Server1 обновлены: доступно {} инструментов", tools.size());

        // Обновить реестр инструментов
        toolRegistry.updateServerTools("server1", tools);

        // Уведомить UI о необходимости обновить список инструментов
        eventBus.publish(new ToolsUpdatedEvent("server1", tools));
    }

    @McpResourceListChanged(clients = "server1")
    public void handleServer1ResourcesChanged(List<McpSchema.Resource> resources) {
        logger.info("Ресурсы Server1 обновлены: доступно {} ресурсов", resources.size());

        // Очистить кэш ресурсов для этого сервера
        resourceCache.clearServer("server1");

        // Зарегистрировать новые ресурсы
        resources.forEach(resource ->
            resourceCache.register("server1", resource));
    }
}
```

Конфигурация:

```yaml
spring:
  ai:
    mcp:
      client:
        type: SYNC
        initialized: true
        request-timeout: 30s
        annotation-scanner:
          enabled: true
        sse:
          connections:
            server1:
              url: http://localhost:8080
        stdio:
          connections:
            local-tool:
              command: /usr/local/bin/mcp-tool
              args:
                - --mode=production
```## Примеры асинхронного программирования

### Асинхронный сервер инструментов

```java
@Component
public class AsyncDataProcessor {

    @McpTool(name = "fetch-data", description = "Получить данные из внешнего источника")
    public Mono<DataResult> fetchData(
            @McpToolParam(description = "URL источника данных", required = true) String url,
            @McpToolParam(description = "Таймаут в секундах", required = false) Integer timeout) {

        Duration timeoutDuration = Duration.ofSeconds(timeout != null ? timeout : 30);

        return WebClient.create()
            .get()
            .uri(url)
            .retrieve()
            .bodyToMono(String.class)
            .map(data -> new DataResult(url, data, System.currentTimeMillis()))
            .timeout(timeoutDuration)
            .onErrorReturn(new DataResult(url, "Ошибка при получении данных", 0L));
    }

    @McpTool(name = "process-stream", description = "Обработать поток данных")
    public Flux<String> processStream(
            McpAsyncRequestContext context,
            @McpToolParam(description = "Количество элементов", required = true) int count) {

        // Получить токен прогресса из контекста
        String progressToken = context.request().progressToken();

        return Flux.range(1, count)
            .delayElements(Duration.ofMillis(100))
            .flatMap(i -> {
                if (progressToken != null) {
                    double progress = (double) i / count;
                    return context.progress(p -> p.progress(progress).total(1.0).message("Обработка элемента " + i))
                        .thenReturn("Обработан элемент " + i);
                }
                return Mono.just("Обработан элемент " + i);
            });
    }

    @McpResource(uri = "async-data://{id}", name = "Асинхронные данные")
    public Mono<ReadResourceResult> getAsyncData(String id) {
        return Mono.fromCallable(() -> loadDataAsync(id))
            .subscribeOn(Schedulers.boundedElastic())
            .map(data -> new ReadResourceResult(List.of(
                new TextResourceContents("async-data://" + id,
                    "application/json", data)
            )));
    }
}
```

### Асинхронные обработчики клиентов

```java
@Component
public class AsyncClientHandlers {

    @McpSampling(clients = "async-server")
    public Mono<CreateMessageResult> handleAsyncSampling(CreateMessageRequest request) {
        return Mono.fromCallable(() -> {
            // Подготовить запрос для LLM
            String prompt = extractPrompt(request);
            return prompt;
        })
        .flatMap(prompt -> callLLMAsync(prompt))
        .map(response -> CreateMessageResult.builder()
            .role(Role.ASSISTANT)
            .content(new TextContent(response))
            .model("gpt-4")
            .build())
        .timeout(Duration.ofSeconds(30));
    }

    @McpProgress(clients = "async-server")
    public Mono<Void> handleAsyncProgress(ProgressNotification notification) {
        return Mono.fromRunnable(() -> {
            // Обновить отслеживание прогресса
            updateProgressAsync(notification);
        })
        .then(broadcastProgressAsync(notification))
        .subscribeOn(Schedulers.parallel());
    }

    @McpElicitation(clients = "async-server")
    public Mono<ElicitResult> handleAsyncElicitation(ElicitRequest request) {
        return showUserDialogAsync(request)
            .map(userData -> {
                if (userData != null && !userData.isEmpty()) {
                    return new ElicitResult(ElicitResult.Action.ACCEPT, userData);
                } else {
                    return new ElicitResult(ElicitResult.Action.DECLINE, null);
                }
            })
            .timeout(Duration.ofMinutes(5))
            .onErrorReturn(new ElicitResult(ElicitResult.Action.CANCEL, null));
    }
}
```

## Примеры безсостояния сервера```java
@Component
public class StatelessTools {

    // Простой статeless инструмент
    @McpTool(name = "format-text", description = "Форматировать текст")
    public String formatText(
            @McpToolParam(description = "Текст для форматирования", required = true) String text,
            @McpToolParam(description = "Тип формата", required = true) String format) {

        return switch (format.toLowerCase()) {
            case "uppercase" -> text.toUpperCase();
            case "lowercase" -> text.toLowerCase();
            case "title" -> toTitleCase(text);
            case "reverse" -> new StringBuilder(text).reverse().toString();
            default -> text;
        };
    }

    // Stateless с контекстом передачи
    @McpTool(name = "validate-json", description = "Проверить JSON")
    public CallToolResult validateJson(
            McpTransportContext context,
            @McpToolParam(description = "Строка JSON", required = true) String json) {

        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.readTree(json);

            return CallToolResult.builder()
                .addTextContent("Корректный JSON")
                .structuredContent(Map.of("valid", true))
                .build();
        } catch (Exception e) {
            return CallToolResult.builder()
                .addTextContent("Некорректный JSON: " + e.getMessage())
                .structuredContent(Map.of("valid", false, "error", e.getMessage()))
                .build();
        }
    }

    @McpResource(uri = "static://{path}", name = "Статический ресурс")
    public String getStaticResource(String path) {
        // Простой статeless ресурс
        return loadStaticContent(path);
    }

    @McpPrompt(name = "template", description = "Шаблон запроса")
    public GetPromptResult templatePrompt(
            @McpArg(name = "template", required = true) String templateName,
            @McpArg(name = "variables", required = false) String variables) {

        String template = loadTemplate(templateName);
        if (variables != null) {
            template = substituteVariables(template, variables);
        }

        return new GetPromptResult("Шаблон: " + templateName,
            List.of(new PromptMessage(Role.USER, new TextContent(template))));
    }
}
```

## MCP Сэмплирование с несколькими поставщиками LLM

Этот пример демонстрирует, как использовать MCP Сэмплирование для генерации креативного контента от нескольких поставщиков LLM, показывая аннотационно-ориентированный подход как для серверных, так и для клиентских реализаций.

### Реализация сервера сэмплирования
```markdown
Сервер предоставляет инструмент погоды, который использует MCP Sampling для генерации стихов от различных поставщиков LLM:

```java
@Service
public class WeatherService {

    private final RestClient restClient = RestClient.create();

    public record WeatherResponse(Current current) {
        public record Current(LocalDateTime time, int interval, double temperature_2m) {
        }
    }

    @McpTool(description = "Получить температуру (в градусах Цельсия) для конкретного местоположения")
    public String getTemperature2(McpSyncServerExchange exchange,
            @McpToolParam(description = "Широта местоположения") double latitude,
            @McpToolParam(description = "Долгота местоположения") double longitude) {

        // Получение данных о погоде
        WeatherResponse weatherResponse = restClient
                .get()
                .uri("https://api.open-meteo.com/v1/forecast?latitude={latitude}&longitude={longitude}&current=temperature_2m",
                        latitude, longitude)
                .retrieve()
                .body(WeatherResponse.class);

        StringBuilder openAiWeatherPoem = new StringBuilder();
        StringBuilder anthropicWeatherPoem = new StringBuilder();

        // Отправка уведомления о логировании
        exchange.loggingNotification(LoggingMessageNotification.builder()
                .level(LoggingLevel.INFO)
                .data("Начало выборки")
                .build());

        // Проверка, поддерживает ли клиент выборку
        if (exchange.getClientCapabilities().sampling() != null) {
            var messageRequestBuilder = McpSchema.CreateMessageRequest.builder()
                    .systemPrompt("Вы поэт!")
                    .messages(List.of(new McpSchema.SamplingMessage(McpSchema.Role.USER,
                            new McpSchema.TextContent(
                                    "Пожалуйста, напишите стихотворение о прогнозе погоды (температура в градусах Цельсия). Используйте формат markdown :\n "
                                            + ModelOptionsUtils.toJsonStringPrettyPrinter(weatherResponse)))));

            // Запрос стихотворения у OpenAI
            var openAiLlmMessageRequest = messageRequestBuilder
                    .modelPreferences(ModelPreferences.builder().addHint("openai").build())
                    .build();
            CreateMessageResult openAiLlmResponse = exchange.createMessage(openAiLlmMessageRequest);
            openAiWeatherPoem.append(((McpSchema.TextContent) openAiLlmResponse.content()).text());

            // Запрос стихотворения у Anthropic
            var anthropicLlmMessageRequest = messageRequestBuilder
                    .modelPreferences(ModelPreferences.builder().addHint("anthropic").build())
                    .build();
            CreateMessageResult anthropicAiLlmResponse = exchange.createMessage(anthropicLlmMessageRequest);
            anthropicWeatherPoem.append(((McpSchema.TextContent) anthropicAiLlmResponse.content()).text());
        }

        exchange.loggingNotification(LoggingMessageNotification.builder()
                .level(LoggingLevel.INFO)
                .data("Завершение выборки")
                .build());

        // Объединение результатов
        String responseWithPoems = "Стихотворение OpenAI о погоде: " + openAiWeatherPoem.toString() + "\n\n" +
                "Стихотворение Anthropic о погоде: " + anthropicWeatherPoem.toString() + "\n"
                + ModelOptionsUtils.toJsonStringPrettyPrinter(weatherResponse);

        return responseWithPoems;
    }
}
```

### Реализация клиента выборки
``````markdown
Клиент обрабатывает запросы на выборку, направляя их к соответствующим поставщикам LLM на основе подсказок модели:

```java
@Service
public class McpClientHandlers {

    private static final Logger logger = LoggerFactory.getLogger(McpClientHandlers.class);

    @Autowired
    Map<String, ChatClient> chatClients;

    @McpProgress(clients = "server1")
    public void progressHandler(ProgressNotification progressNotification) {
        logger.info("MCP PROGRESS: [{}] progress: {} total: {} message: {}",
                progressNotification.progressToken(), progressNotification.progress(),
                progressNotification.total(), progressNotification.message());
    }

    @McpLogging(clients = "server1")
    public void loggingHandler(LoggingMessageNotification loggingMessage) {
        logger.info("MCP LOGGING: [{}] {}", loggingMessage.level(), loggingMessage.data());
    }

    @McpSampling(clients = "server1")
    public CreateMessageResult samplingHandler(CreateMessageRequest llmRequest) {
        logger.info("MCP SAMPLING: {}", llmRequest);

        // Извлечение пользовательского запроса и подсказки модели
        var userPrompt = ((McpSchema.TextContent) llmRequest.messages().get(0).content()).text();
        String modelHint = llmRequest.modelPreferences().hints().get(0).name();

        // Поиск подходящего ChatClient на основе подсказки модели
        ChatClient hintedChatClient = chatClients.entrySet().stream()
                .filter(e -> e.getKey().contains(modelHint))
                .findFirst()
                .orElseThrow()
                .getValue();

        // Генерация ответа с использованием выбранной модели
        String response = hintedChatClient.prompt()
                .system(llmRequest.systemPrompt())
                .user(userPrompt)
                .call()
                .content();

        return CreateMessageResult.builder()
                .content(new McpSchema.TextContent(response))
                .build();
    }
}
```

### Настройка клиентского приложения

Зарегистрируйте инструменты и обработчики MCP в клиентском приложении:

```java
@SpringBootApplication
public class McpClientApplication {

    public static void main(String[] args) {
        SpringApplication.run(McpClientApplication.class, args).close();
    }

    @Bean
    public CommandLineRunner predefinedQuestions(OpenAiChatModel openAiChatModel,
            ToolCallbackProvider mcpToolProvider) {

        return args -> {

            ChatClient chatClient = ChatClient.builder(openAiChatModel)
                    .defaultToolCallbacks(mcpToolProvider)
                    .build();

            String userQuestion = """
                    Какова погода в Амстердаме прямо сейчас?
                    Пожалуйста, включите все креативные ответы от всех поставщиков LLM.
                    После того, как другие поставщики добавят стихотворение, которое синтезирует стихи от всех остальных поставщиков.
                    """;

            System.out.println("> USER: " + userQuestion);
            System.out.println("> ASSISTANT: " + chatClient.prompt(userQuestion).call().content());
        };
    }
}
```

### Конфигурация

#### Конфигурация сервера

```yaml
# Server application.properties
spring.ai.mcp.server.name=mcp-sampling-server-annotations
spring.ai.mcp.server.version=0.0.1
spring.ai.mcp.server.protocol=STREAMABLE
spring.main.banner-mode=off
```

#### Конфигурация клиента

```yaml
# Client application.properties
spring.application.name=mcp
spring.main.web-application-type=none

# Отключить автонастройку клиента чата по умолчанию для нескольких моделей
spring.ai.chat.client.enabled=false

# API ключи
spring.ai.openai.api-key=${OPENAI_API_KEY}
spring.ai.anthropic.api-key=${ANTHROPIC_API_KEY}

# Подключение клиента MCP с использованием безстатусного HTTP-транспорта
spring.ai.mcp.client.streamable-http.connections.server1.url=http://localhost:8080

# Отключить обратный вызов инструмента, чтобы предотвратить циклические зависимости
spring.ai.mcp.client.toolcallback.enabled=false
```

### Ключевые функции, продемонстрированные
```1. **Многообразное выборочное моделирование**: Сервер запрашивает контент у нескольких поставщиков LLM, используя подсказки моделей
2. **Обработчики на основе аннотаций**: Клиент использует аннотации `@McpSampling`, `@McpLogging` и `@McpProgress`
3. **Безстатусный HTTP-транспорт**: Использует потоковый протокол для связи
4. **Генерация креативного контента**: Генерирует стихи о погодных данных из разных моделей
5. **Унифицированная обработка ответов**: Объединяет ответы от нескольких поставщиков в один результат

### Пример вывода

При запуске клиента вы увидите вывод, подобный следующему:

[source]
```
> USER: Какова погода в Амстердаме прямо сейчас?
Пожалуйста, включите все креативные ответы от всех поставщиков LLM.
После других поставщиков добавьте стихотворение, которое синтезирует стихи от всех остальных поставщиков.

> ASSISTANT:
Стихотворение от OpenAI о погоде:
**Зимний шепот Амстердама**
*Температура: 4.2°C*

В объятиях Амстердама, где каналы отражают небо,
Нежный холод в 4.2 градуса проносится мимо...

Стихотворение от Anthropic о погоде:
**Размышления у канала**
*Текущие условия: 4.2°C*

У водных путей, где отдыхают велосипеды,
Зимний воздух испытывает Амстердам на прочность...

Данные о погоде:
{
  "current": {
    "time": "2025-01-23T11:00",
    "interval": 900,
    "temperature_2m": 4.2
  }
}
```

## Интеграция с Spring AI

Пример, показывающий инструменты MCP, интегрированные с вызовом функций Spring AI:

```java
@RestController
@RequestMapping("/chat")
public class ChatController {

    private final ChatModel chatModel;
    private final SyncMcpToolCallbackProvider toolCallbackProvider;

    public ChatController(ChatModel chatModel,
                          SyncMcpToolCallbackProvider toolCallbackProvider) {
        this.chatModel = chatModel;
        this.toolCallbackProvider = toolCallbackProvider;
    }

    @PostMapping
    public ChatResponse chat(@RequestBody ChatRequest request) {
        // Получить инструменты MCP как обратные вызовы функций Spring AI
        ToolCallback[] mcpTools = toolCallbackProvider.getToolCallbacks();

        // Создать подсказку с инструментами MCP
        Prompt prompt = new Prompt(
            request.getMessage(),
            ChatOptionsBuilder.builder()
                .withTools(mcpTools)
                .build()
        );

        // Вызвать модель чата с доступными инструментами MCP
        return chatModel.call(prompt);
    }
}

@Component
public class WeatherTools {

    @McpTool(name = "get-weather", description = "Получить текущую погоду")
    public WeatherInfo getWeather(
            @McpToolParam(description = "Название города", required = true) String city,
            @McpToolParam(description = "Единицы измерения (метрические/имперские)", required = false) String units) {

        String unit = units != null ? units : "metric";

        // Вызвать API погоды
        return weatherService.getCurrentWeather(city, unit);
    }

    @McpTool(name = "get-forecast", description = "Получить прогноз погоды")
    public ForecastInfo getForecast(
            @McpToolParam(description = "Название города", required = true) String city,
            @McpToolParam(description = "Дни (1-7)", required = false) Integer days) {

        int forecastDays = days != null ? days : 3;

        return weatherService.getForecast(city, forecastDays);
    }
}
```

## Дополнительные ресурсы

- xref:api/mcp/mcp-annotations-overview.adoc[Обзор аннотаций MCP]
- xref:api/mcp/mcp-annotations-server.adoc[Справочник аннотаций сервера]
- xref:api/mcp/mcp-annotations-client.adoc[Справочник аннотаций клиента]
- xref:api/mcp/mcp-annotations-special-params.adoc[Справочник специальных параметров]
- [Примеры Spring AI MCP на GitHub](https://github.com/spring-projects/spring-ai-examples/tree/main/model-context-protocol)
