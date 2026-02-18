## Streamable-HTTP MCP Servers

[Streamable HTTP transport](https://modelcontextprotocol.io/specification/2025-06-18/basic/transports#streamable-http) позволяет серверам MCP работать как независимые процессы, которые могут обрабатывать несколько клиентских подключений с использованием HTTP POST и GET запросов, с опциональной передачей событий от сервера (SSE) для нескольких сообщений от сервера. Он заменяет транспорт SSE.

Эти серверы, представленные с версией спецификации [2025-03-26](https://modelcontextprotocol.io/specification/2025-03-26), идеально подходят для приложений, которые должны уведомлять клиентов о динамических изменениях в инструментах, ресурсах или подсказках.

> **Совет:** Установите свойство `spring.ai.mcp.server.protocol=STREAMABLE`

> **Совет:** Используйте xref:api/mcp/mcp-client-boot-starter-docs#_streamable_http_transport_properties[Streamable-HTTP клиенты] для подключения к Streamable-HTTP серверам.

### Streamable-HTTP WebMVC Server

Используйте зависимость `spring-ai-starter-mcp-server-webmvc`:

```xml
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-starter-mcp-server-webmvc</artifactId>
</dependency>
```

и установите свойство `spring.ai.mcp.server.protocol` в `STREAMABLE`.

- Полные возможности MCP сервера с использованием Spring MVC Streamable транспорта
- Поддержка инструментов, ресурсов, подсказок, завершения, ведения журнала, прогресса, пинга, возможностей изменения корня
- Управление постоянными подключениями

### Streamable-HTTP WebFlux Server

Используйте зависимость `spring-ai-starter-mcp-server-webflux`:

```xml
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-starter-mcp-server-webflux</artifactId>
</dependency>
```

и установите свойство `spring.ai.mcp.server.protocol` в `STREAMABLE`.

- Реактивный MCP сервер с использованием WebFlux Streamable транспорта
- Поддержка инструментов, ресурсов, подсказок, завершения, ведения журнала, прогресса, пинга, возможностей изменения корня
- Неблокирующее управление постоянными подключениями

## Configuration Properties

### Common Properties

Все общие свойства начинаются с префикса `spring.ai.mcp.server`:

[options="header"]
| Property | Description | Default |
| --- | --- | --- |
| `enabled` | Включить/выключить streamable MCP сервер | `true` |
| `protocol` | Протокол MCP сервера | Должен быть установлен в `STREAMABLE` для включения streamable сервера |
| `tool-callback-converter` | Включить/выключить преобразование Spring AI ToolCallbacks в спецификации MCP Tool | `true` |
| `name` | Имя сервера для идентификации | `mcp-server` |
| `version` | Версия сервера | `1.0.0` |
| `instructions` | Дополнительные инструкции для взаимодействия с клиентом | `null` |
| `type` | Тип сервера (SYNC/ASYNC) | `SYNC` |
| `capabilities.resource` | Включить/выключить возможности ресурсов | `true` |
| `capabilities.tool` | Включить/выключить возможности инструментов | `true` |
| `capabilities.prompt` | Включить/выключить возможности подсказок | `true` |
| `capabilities.completion` | Включить/выключить возможности завершения | `true` |
| `resource-change-notification` | Включить уведомления об изменениях ресурсов | `true` |
| `prompt-change-notification` | Включить уведомления об изменениях подсказок | `true` |
| `tool-change-notification` | Включить уведомления об изменениях инструментов | `true` |
| `tool-response-mime-type` | MIME тип ответа по имени инструмента | `-` |
| `request-timeout` | Длительность таймаута запроса | `20 seconds` |

### MCP Annotations Properties

Аннотации MCP сервера предоставляют декларативный способ реализации обработчиков сервера MCP с использованием аннотаций Java.

Свойства аннотаций сервера mcp-annotations начинаются с префикса `spring.ai.mcp.server.annotation-scanner`:

[cols="3,4,3"]
| Property | Description | Default Value |
| --- | --- | --- |
| `enabled` |  |  |
| Включить/выключить автоматическое сканирование аннотаций сервера MCP |  |  |
| `true` |  |  |

### Streamable-HTTP PropertiesВсе свойства streamable-HTTP начинаются с префикса `spring.ai.mcp.server.streamable-http`:

[options="header"]
| Свойство | Описание | По умолчанию |
| --- | --- | --- |
| `mcp-endpoint` | Путь к пользовательскому MCP-эндпоинту | `/mcp` |
| `keep-alive-interval` | Интервал поддержания соединения | `null` (отключено) |
| `disallow-delete` | Запретить операции удаления | `false` |

## Возможности и функции

Сервер MCP поддерживает четыре основных типа возможностей, которые можно включать или отключать по отдельности:

- **Инструменты** - Включить/отключить возможности инструментов с помощью `spring.ai.mcp.server.capabilities.tool=true|false`
- **Ресурсы** - Включить/отключить возможности ресурсов с помощью `spring.ai.mcp.server.capabilities.resource=true|false`
- **Подсказки** - Включить/отключить возможности подсказок с помощью `spring.ai.mcp.server.capabilities.prompt=true|false`
- **Завершения** - Включить/отключить возможности завершений с помощью `spring.ai.mcp.server.capabilities.completion=true|false`

Все возможности включены по умолчанию. Отключение возможности предотвратит регистрацию сервера и предоставление соответствующих функций клиентам.

Стартер загрузки сервера MCP позволяет серверам предоставлять инструменты, ресурсы и подсказки клиентам. Он автоматически преобразует пользовательские обработчики возможностей, зарегистрированные как Spring-бины, в синхронные/асинхронные спецификации в зависимости от типа сервера:

### link:https://modelcontextprotocol.io/specification/2025-03-26/server/tools[Инструменты]
Позволяет серверам предоставлять инструменты, которые могут быть вызваны языковыми моделями. Стартер загрузки сервера MCP предоставляет:

- Поддержка уведомлений об изменениях
- xref:api/tools.adoc[Spring AI Tools] автоматически преобразуются в синхронные/асинхронные спецификации в зависимости от типа сервера
- Автоматическая спецификация инструмента через Spring-бины:

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

Поддерживается xref:api/tools.adoc#_tool_context[ToolContext], что позволяет передавать контекстную информацию в вызовы инструментов. Он содержит экземпляр `McpSyncServerExchange` под ключом `exchange`, доступный через `McpToolUtils.getMcpExchange(toolContext)`. См. этот https://github.com/spring-projects/spring-ai-examples/blob/3fab8483b8deddc241b1e16b8b049616604b7767/model-context-protocol/sampling/mcp-weather-webmvc-server/src/main/java/org/springframework/ai/mcp/sample/server/WeatherService.java#L59-L126[пример], демонстрирующий `exchange.loggingNotification(...)` и `exchange.createMessage(...)`.

### link:https://modelcontextprotocol.io/specification/2025-03-26/server/resources/[Ресурсы]```
Предоставляет стандартизированный способ для серверов открывать ресурсы для клиентов.

- Спецификации статических и динамических ресурсов
- Поддержка уведомлений об изменениях
- Поддержка шаблонов ресурсов
- Автоматическое преобразование между синхронными/асинхронными спецификациями ресурсов
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

### link:https://modelcontextprotocol.io/specification/2025-03-26/server/prompts/[Шаблоны]

Предоставляет стандартизированный способ для серверов открывать шаблоны подсказок для клиентов.

- Поддержка уведомлений об изменениях
- Версионирование шаблонов
- Автоматическое преобразование между синхронными/асинхронными спецификациями подсказок
- Автоматическая спецификация подсказок через Spring бины:

```java
@Bean
public List<McpServerFeatures.SyncPromptSpecification> myPrompts() {
    var prompt = new McpSchema.Prompt("greeting", "Шаблон дружелюбного приветствия",
        List.of(new McpSchema.PromptArgument("name", "Имя для приветствия", true)));

    var promptSpecification = new McpServerFeatures.SyncPromptSpecification(prompt, (exchange, getPromptRequest) -> {
        String nameArgument = (String) getPromptRequest.arguments().get("name");
        if (nameArgument == null) { nameArgument = "друг"; }
        var userMessage = new PromptMessage(Role.USER, new TextContent("Привет, " + nameArgument + "! Как я могу помочь вам сегодня?"));
        return new GetPromptResult("Персонализированное сообщение приветствия", List.of(userMessage));
    });

    return List.of(promptSpecification);
}
```

### link:https://modelcontextprotocol.io/specification/2025-03-26/server/utilities/completion/[Завершения]

Предоставляет стандартизированный способ для серверов открывать возможности завершения для клиентов.

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
Внутри инструмента, обработчика вызовов ресурсов, подсказок или завершений используйте предоставленный объект `McpSyncServerExchange`/`McpAsyncServerExchange` `exchange` для отправки сообщений журналов:

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
    // Обработка сообщений журнала
});
```
```### link:https://modelcontextprotocol.io/specification/2025-03-26/basic/utilities/progress[Progress]

Предоставляет стандартизированный способ для серверов отправлять обновления о прогрессе клиентам. 
Внутри обработчика вызова инструмента, ресурса, подсказки или завершения используйте предоставленный объект `McpSyncServerExchange`/`McpAsyncServerExchange` `exchange` для отправки уведомлений о прогрессе:

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

### link:https://modelcontextprotocol.io/specification/2025-03-26/client/roots#root-list-changes[Root List Changes]

Когда корни изменяются, клиенты, поддерживающие `listChanged`, отправляют уведомление об изменении корня.

- Поддержка мониторинга изменений корней
- Автоматическая конвертация в асинхронные потребители для реактивных приложений
- Необязательная регистрация через Spring бины

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
Внутри обработчика вызова инструмента, ресурса, подсказки или завершения используйте предоставленный объект `McpSyncServerExchange`/`McpAsyncServerExchange` `exchange` для отправки ping-сообщений:

```java
(exchange, request) -> {
        exchange.ping();
}
```

### Keep Alive

Сервер может по желанию периодически отправлять ping-сообщения подключенным клиентам для проверки состояния соединения.

По умолчанию функция keep-alive отключена. 
Чтобы включить keep-alive, установите свойство `keep-alive-interval` в вашей конфигурации:

```yaml
spring:
  ai:
    mcp:
      server:
        streamable-http:
          keep-alive-interval: 30s
```

> **Примечание:** В настоящее время для streamable-http серверов механизм keep-alive доступен только для соединения [Listening for Messages from the Server (SSE)](https://modelcontextprotocol.io/specification/2025-03-26/basic/transports#listening-for-messages-from-the-server).

## Примеры использования

### Конфигурация Streamable HTTP сервера
```yaml
# Используя spring-ai-starter-mcp-server-streamable-webmvc
spring:
  ai:
    mcp:
      server:
        protocol: STREAMABLE
        name: streamable-mcp-server
        version: 1.0.0
        type: SYNC
        instructions: "Этот потоковый сервер предоставляет уведомления в реальном времени"
        resource-change-notification: true
        tool-change-notification: true
        prompt-change-notification: true
        streamable-http:
          mcp-endpoint: /api/mcp
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

Авто-конфигурация автоматически зарегистрирует обратные вызовы инструментов как инструменты MCP. 
Вы можете иметь несколько бинов, производящих ToolCallbacks, и авто-конфигурация объединит их.
