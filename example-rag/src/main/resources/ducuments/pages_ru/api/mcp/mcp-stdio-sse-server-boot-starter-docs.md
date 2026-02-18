## STDIO и SSE MCP Серверы

Серверы STDIO и SSE MCP поддерживают несколько механизмов передачи, каждый из которых имеет свой собственный стартер.

> **Совет:** Используйте xref:api/mcp/mcp-client-boot-starter-docs#_stdio_transport_properties[клиенты STDIO] или xref:api/mcp/mcp-client-boot-starter-docs#_sse_transport_properties[клиенты SSE] для подключения к серверам STDIO и SSE.

### Сервер STDIO MCP

Полная поддержка функций MCP сервера с транспортом сервера `STDIO`.

```xml
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-starter-mcp-server</artifactId>
</dependency>
```

- Подходит для командных и настольных инструментов
- Не требует дополнительных веб-зависимостей
- Конфигурация основных компонентов сервера
- Обработка спецификаций инструментов, ресурсов и подсказок
- Управление возможностями сервера и уведомлениями об изменениях
- Поддержка как синхронных, так и асинхронных реализаций сервера

### Сервер SSE WebMVC

Полная поддержка функций MCP сервера с транспортом сервера `SSE` (События, отправляемые сервером), основанным на Spring MVC и с опциональным транспортом `STDIO`.

```xml
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-starter-mcp-server-webmvc</artifactId>
</dependency>
```

- HTTP-транспорт на основе Spring MVC (`WebMvcSseServerTransportProvider`)
- Автоматически настроенные конечные точки SSE
- Опциональный транспорт `STDIO` (включается установкой `spring.ai.mcp.server.stdio=true`)
- Включает зависимости `spring-boot-starter-web` и `mcp-spring-webmvc`

### Сервер SSE WebFlux

Полная поддержка функций MCP сервера с транспортом сервера `SSE` (События, отправляемые сервером), основанным на Spring WebFlux и с опциональным транспортом `STDIO`.

```xml
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-starter-mcp-server-webflux</artifactId>
</dependency>
```

Стартер активирует автоматические конфигурации `McpWebFluxServerAutoConfiguration` и `McpServerAutoConfiguration`, чтобы предоставить:

- Реактивный транспорт с использованием Spring WebFlux (`WebFluxSseServerTransportProvider`)
- Автоматически настроенные реактивные конечные точки SSE
- Опциональный транспорт `STDIO` (включается установкой `spring.ai.mcp.server.stdio=true`)
- Включает зависимости `spring-boot-starter-webflux` и `mcp-spring-webflux`

[NOTE]
====
Из-за поведения Spring Boot по умолчанию, когда на classpath присутствуют как `org.springframework.web.servlet.DispatcherServlet`, так и `org.springframework.web.reactive.DispatcherHandler`, Spring Boot будет придавать приоритет `DispatcherServlet`. В результате, если ваш проект использует `spring-boot-starter-web`, рекомендуется использовать `spring-ai-starter-mcp-server-webmvc` вместо `spring-ai-starter-mcp-server-webflux`.
====

## Свойства конфигурации

### Общие свойстваВсе общие свойства начинаются с префикса `spring.ai.mcp.server`:

[options="header"]
| Свойство | Описание | По умолчанию |
| --- | --- | --- |
| `enabled` | Включить/выключить сервер MCP | `true` |
| `tool-callback-converter` | Включить/выключить преобразование Spring AI ToolCallbacks в спецификации инструментов MCP | `true` |
| `stdio` | Включить/выключить транспорт STDIO | `false` |
| `name` | Имя сервера для идентификации | `mcp-server` |
| `version` | Версия сервера | `1.0.0` |
| `instructions` | Дополнительные инструкции для предоставления клиенту руководства по взаимодействию с этим сервером | `null` |
| `type` | Тип сервера (SYNC/ASYNC) | `SYNC` |
| `capabilities.resource` | Включить/выключить возможности ресурсов | `true` |
| `capabilities.tool` | Включить/выключить возможности инструментов | `true` |
| `capabilities.prompt` | Включить/выключить возможности подсказок | `true` |
| `capabilities.completion` | Включить/выключить возможности завершения | `true` |
| `resource-change-notification` | Включить уведомления об изменениях ресурсов | `true` |
| `prompt-change-notification` | Включить уведомления об изменениях подсказок | `true` |
| `tool-change-notification` | Включить уведомления об изменениях инструментов | `true` |
| `tool-response-mime-type` | Дополнительный MIME-тип ответа для каждого имени инструмента. Например, `spring.ai.mcp.server.tool-response-mime-type.generateImage=image/png` свяжет MIME-тип `image/png` с именем инструмента `generateImage()` | `-` |
| `request-timeout` | Время ожидания ответов сервера перед истечением времени ожидания запросов. Применяется ко всем запросам, сделанным через клиент, включая вызовы инструментов, доступ к ресурсам и операции с подсказками | `20 секунд` |

### Свойства аннотаций MCP

Аннотации сервера MCP предоставляют декларативный способ реализации обработчиков сервера MCP с использованием аннотаций Java.

Свойства аннотаций mcp начинаются с префикса `spring.ai.mcp.server.annotation-scanner`:

[cols="3,4,3"]
| Свойство | Описание | Значение по умолчанию |
| --- | --- | --- |
| `enabled` |  |  |
| Включить/выключить автоматическое сканирование аннотаций сервера MCP |  |  |
| `true` |  |  |

### Свойства SSE

Все свойства SSE начинаются с префикса `spring.ai.mcp.server`:

[options="header"]
| Свойство | Описание | По умолчанию |
| --- | --- | --- |
| `sse-message-endpoint` | Пользовательский путь конечной точки SSE для веб-транспорта, который будет использоваться клиентом для отправки сообщений | `/mcp/message` |
| `sse-endpoint` | Пользовательский путь конечной точки SSE для веб-транспорта | `/sse` |
| `base-url` | Дополнительный префикс URL. Например, `base-url=/api/v1` означает, что клиент должен получить доступ к конечной точке SSE по адресу `/api/v1` + `sse-endpoint`, а конечная точка сообщений — `/api/v1` + `sse-message-endpoint` | `-` |
| `keep-alive-interval` | Интервал поддержания соединения | `null` (выключено) |

> **Примечание:** В целях обратной совместимости свойства SSE не имеют дополнительного суффикса (например, `.sse`).

## Функции и возможности

Стартовый пакет MCP Server Boot позволяет серверам предоставлять инструменты, ресурсы и подсказки клиентам. Он автоматически преобразует пользовательские обработчики возможностей, зарегистрированные как Spring бины, в синхронные/асинхронные спецификации в зависимости от типа сервера:

### link:https://spec.modelcontextprotocol.io/specification/2024-11-05/server/tools/[Инструменты]Позволяет серверам предоставлять инструменты, которые могут быть вызваны языковыми моделями. MCP Server Boot Starter предоставляет:

- Поддержка уведомлений об изменениях
- xref:api/tools.adoc[Spring AI Tools] автоматически конвертируются в синхронные/асинхронные спецификации в зависимости от типа сервера
- Автоматическая спецификация инструментов через Spring бины:

```java
@Bean
public ToolCallbackProvider myTools(...) {
    List<ToolCallback> tools = ...
    return ToolCallbackProvider.from(tools);
}
```

или с использованием низкоуровневого API:

```java
@Bean
public List<McpServerFeatures.SyncToolSpecification> myTools(...) {
    List<McpServerFeatures.SyncToolSpecification> tools = ...
    return tools;
}
```

Автонастройка автоматически обнаружит и зарегистрирует все обратные вызовы инструментов из:

- Индивидуальных бинов `ToolCallback`
- Списков бинов `ToolCallback`
- Бинов `ToolCallbackProvider`

Инструменты дублируются по имени, при этом используется первое вхождение каждого имени инструмента.

> **Совет:** Вы можете отключить автоматическое обнаружение и регистрацию всех обратных вызовов инструментов, установив `tool-callback-converter` в `false`.

#### Поддержка контекста инструмента

Поддерживается xref:api/tools.adoc#_tool_context[ToolContext], позволяя передавать контекстную информацию в вызовы инструментов. Он содержит экземпляр `McpSyncServerExchange` под ключом `exchange`, доступный через `McpToolUtils.getMcpExchange(toolContext)`. См. этот https://github.com/spring-projects/spring-ai-examples/blob/3fab8483b8deddc241b1e16b8b049616604b7767/model-context-protocol/sampling/mcp-weather-webmvc-server/src/main/java/org/springframework/ai/mcp/sample/server/WeatherService.java#L59-L126[пример], демонстрирующий `exchange.loggingNotification(...)` и `exchange.createMessage(...)`.

### link:https://spec.modelcontextprotocol.io/specification/2024-11-05/server/resources/[Ресурсы]

Предоставляет стандартизированный способ для серверов предоставлять ресурсы клиентам.

- Спецификации статических и динамических ресурсов
- Необязательные уведомления об изменениях
- Поддержка шаблонов ресурсов
- Автоматическая конвертация между синхронными/асинхронными спецификациями ресурсов
- Автоматическая спецификация ресурсов через Spring бины:

```java
@Bean
public List<McpServerFeatures.SyncResourceSpecification> myResources(...) {
    var systemInfoResource = new McpSchema.Resource(...);
    var resourceSpecification = new McpServerFeatures.SyncResourceSpecification(systemInfoResource, (exchange, request) -> {
        try {
            var systemInfo = Map.of(...);
            String jsonContent = new ObjectMapper().writeValueAsString(systemInfo);
            return new McpSchema.ReadResourceResult(
                    List.of(new McpSchema.TextResourceContents(request.uri(), "application/json", jsonContent)));
        }
        catch (Exception e) {
            throw new RuntimeException("Не удалось сгенерировать информацию о системе", e);
        }
    });

    return List.of(resourceSpecification);
}
```

### link:https://spec.modelcontextprotocol.io/specification/2024-11-05/server/prompts/[Подсказки]```markdown
Предоставляет стандартизированный способ для серверов раскрывать шаблоны запросов клиентам.

- Поддержка уведомлений об изменениях
- Версионирование шаблонов
- Автоматическое преобразование между синхронными/асинхронными спецификациями запросов
- Автоматическая спецификация запросов через Spring бины:

```java
@Bean
public List<McpServerFeatures.SyncPromptSpecification> myPrompts() {
    var prompt = new McpSchema.Prompt("greeting", "Шаблон дружелюбного приветствия",
        List.of(new McpSchema.PromptArgument("name", "Имя для приветствия", true)));

    var promptSpecification = new McpServerFeatures.SyncPromptSpecification(prompt, (exchange, getPromptRequest) -> {
        String nameArgument = (String) getPromptRequest.arguments().get("name");
        if (nameArgument == null) { nameArgument = "друг"; }
        var userMessage = new PromptMessage(Role.USER, new TextContent("Привет " + nameArgument + "! Как я могу помочь вам сегодня?"));
        return new GetPromptResult("Персонализированное приветственное сообщение", List.of(userMessage));
    });

    return List.of(promptSpecification);
}
```

### link:https://spec.modelcontextprotocol.io/specification/2024-11-05/server/completions/[Завершения]

Предоставляет стандартизированный способ для серверов раскрывать возможности завершения клиентам.

- Поддержка как синхронных, так и асинхронных спецификаций завершения
- Автоматическая регистрация через Spring бины:

```java
@Bean
public List<McpServerFeatures.SyncCompletionSpecification> myCompletions() {
    var completion = new McpServerFeatures.SyncCompletionSpecification(
        new McpSchema.PromptReference(
					"ref/prompt", "code-completion", "Предоставляет предложения по завершению кода"),
        (exchange, request) -> {
            // Реализация, которая возвращает предложения по завершению
            return new McpSchema.CompleteResult(List.of("python", "pytorch", "pyside"), 10, true);
        }
    );

    return List.of(completion);
}
```

### link:https://modelcontextprotocol.io/specification/2025-03-26/server/utilities/logging/[Логирование]

Предоставляет стандартизированный способ для серверов отправлять структурированные сообщения журналов клиентам. 
Внутри инструмента, обработчика ресурса, запроса или завершения используйте предоставленный объект `McpSyncServerExchange`/`McpAsyncServerExchange` `exchange` для отправки сообщений журналов:

```java
(exchange, request) -> {
        exchange.loggingNotification(LoggingMessageNotification.builder()
            .level(LoggingLevel.INFO)
            .logger("test-logger")
            .data("Это тестовое сообщение журнала")
            .build());
}
```

На клиенте MCP вы можете зарегистрировать xref::api/mcp/mcp-client-boot-starter-docs#_customization_types[потребителей журналов] для обработки этих сообщений:

```java
mcpClientSpec.loggingConsumer((McpSchema.LoggingMessageNotification log) -> {
    // Обработка сообщений журналов
});
```

### link:https://modelcontextprotocol.io/specification/2025-03-26/basic/utilities/progress[Прогресс]

Предоставляет стандартизированный способ для серверов отправлять обновления о прогрессе клиентам.
Внутри инструмента, обработчика ресурса, запроса или завершения используйте предоставленный объект `McpSyncServerExchange`/`McpAsyncServerExchange` `exchange` для отправки уведомлений о прогрессе:

```java
(exchange, request) -> {
        exchange.progressNotification(ProgressNotification.builder()
            .progressToken("test-progress-token")
            .progress(0.25)
            .total(1.0)
            .message("вызов инструмента в процессе")
            .build());
}
```

Клиент Mcp может получать уведомления о прогрессе и обновлять свой интерфейс соответственно.
Для этого ему необходимо зарегистрировать потребителя прогресса.

```java
mcpClientSpec.progressConsumer((McpSchema.ProgressNotification progress) -> {
    // Обработка уведомлений о прогрессе
});
```

### link:https://spec.modelcontextprotocol.io/specification/2024-11-05/client/roots/#root-list-changes[Изменения в корневом списке]
```Когда корни изменяются, клиенты, поддерживающие `listChanged`, отправляют уведомление об изменении корня.

- Поддержка мониторинга изменений корней
- Автоматическая конвертация в асинхронные потребители для реактивных приложений
- Опциональная регистрация через Spring бины

```java
@Bean
public BiConsumer<McpSyncServerExchange, List<McpSchema.Root>> rootsChangeHandler() {
    return (exchange, roots) -> {
        logger.info("Регистрация корневых ресурсов: {}", roots);
    };
}
```

### link:https://modelcontextprotocol.io/specification/2025-03-26/basic/utilities/ping/[Ping]

Механизм Ping для сервера, чтобы проверить, что его клиенты все еще активны.
Внутри инструмента, обработчик вызовов ресурсов, подсказок или завершений использует предоставленный объект `McpSyncServerExchange`/`McpAsyncServerExchange` `exchange` для отправки ping-сообщений:

```java
(exchange, request) -> {
        exchange.ping();
}
```

### Поддержка соединения

Сервер может опционально периодически отправлять ping-сообщения подключенным клиентам для проверки состояния соединения.

По умолчанию поддержка соединения отключена. 
Чтобы включить поддержку соединения, установите свойство `keep-alive-interval` в вашей конфигурации:

```yaml
spring:
  ai:
    mcp:
      server:
        keep-alive-interval: 30s
```

## Примеры использования

### Конфигурация стандартного сервера STDIO
```yaml
# Используя spring-ai-starter-mcp-server
spring:
  ai:
    mcp:
      server:
        name: stdio-mcp-server
        version: 1.0.0
        type: SYNC
```

### Конфигурация сервера WebMVC
```yaml
# Используя spring-ai-starter-mcp-server-webmvc
spring:
  ai:
    mcp:
      server:
        name: webmvc-mcp-server
        version: 1.0.0
        type: SYNC
        instructions: "Этот сервер предоставляет инструменты и ресурсы для получения информации о погоде"
        capabilities:
          tool: true
          resource: true
          prompt: true
          completion: true
        # свойства sse
        sse-message-endpoint: /mcp/messages
        keep-alive-interval: 30s
```

### Конфигурация сервера WebFlux
```yaml
# Используя spring-ai-starter-mcp-server-webflux
spring:
  ai:
    mcp:
      server:
        name: webflux-mcp-server
        version: 1.0.0
        type: ASYNC  # Рекомендуется для реактивных приложений
        instructions: "Этот реактивный сервер предоставляет инструменты и ресурсы для получения информации о погоде"
        capabilities:
          tool: true
          resource: true
          prompt: true
          completion: true
        # свойства sse
        sse-message-endpoint: /mcp/messages
        keep-alive-interval: 30s
```

### Создание приложения Spring Boot с MCP сервером

```java
@Service
public class WeatherService {

    @Tool(description = "Получить информацию о погоде по названию города")
    public String getWeather(String cityName) {
        // Реализация
    }
}

@SpringBootApplication
public class McpServerApplication {

    private static final Logger logger = LoggerFactory.getLogger(McpServerApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(McpServerApplication.class, args);
    }

	@Bean
	public ToolCallbackProvider weatherTools(WeatherService weatherService) {
		return MethodToolCallbackProvider.builder().toolObjects(weatherService).build();
	}
}
```

Автонастройка автоматически зарегистрирует обратные вызовы инструментов как инструменты MCP.
Вы можете иметь несколько бинов, производящих ToolCallbacks, и автонастройка объединит их. 

## Пример приложений- [Сервер погоды (WebFlux)](https://github.com/spring-projects/spring-ai-examples/tree/main/model-context-protocol/weather/starter-webflux-server) - Spring AI MCP Server Boot Starter с транспортом WebFlux
- [Сервер погоды (STDIO)](https://github.com/spring-projects/spring-ai-examples/tree/main/model-context-protocol/weather/starter-stdio-server) - Spring AI MCP Server Boot Starter с транспортом STDIO
- [Ручная конфигурация сервера погоды](https://github.com/spring-projects/spring-ai-examples/tree/main/model-context-protocol/weather/manual-webflux-server) - Spring AI MCP Server Boot Starter, который не использует автонастройку, а использует Java SDK для ручной конфигурации сервера
