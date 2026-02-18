## Stateless Streamable-HTTP MCP Servers

Stateless Streamable-HTTP MCP серверы предназначены для упрощенных развертываний, где состояние сессии не сохраняется между запросами. Эти серверы идеально подходят для архитектур микросервисов и облачных развертываний.

> **Совет:** Установите свойство `spring.ai.mcp.server.protocol=STATELESS`

> **Совет:** Используйте xref:api/mcp/mcp-client-boot-starter-docs#_streamable_http_transport_properties[Streamable-HTTP клиенты] для подключения к статeless серверам.

> **Примечание:** Статeless серверы не поддерживают запросы сообщений к MCP клиенту (например, elicitation, sampling, ping).

### Stateless WebMVC Server

Используйте зависимость `spring-ai-starter-mcp-server-webmvc`:

```xml
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-starter-mcp-server-webmvc</artifactId>
</dependency>
```

и установите свойство `spring.ai.mcp.server.protocol` в `STATELESS`.

```
spring.ai.mcp.server.protocol=STATELESS
```

- Stateless работа с транспортом Spring MVC
- Нет управления состоянием сессии
- Упрощенная модель развертывания
- Оптимизировано для облачных сред

### Stateless WebFlux Server

Используйте зависимость `spring-ai-starter-mcp-server-webflux`:

```xml
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-starter-mcp-server-webflux</artifactId>
</dependency>
```

и установите свойство `spring.ai.mcp.server.protocol` в `STATELESS`.

- Реактивная stateless работа с транспортом WebFlux
- Нет управления состоянием сессии
- Неблокирующая обработка запросов
- Оптимизировано для сценариев с высокой пропускной способностью

## Configuration Properties

### Common Properties

Все общие свойства начинаются с `spring.ai.mcp.server`:

[options="header"]
| Property | Description | Default |
| --- | --- | --- |
| `enabled` | Включить/выключить stateless MCP сервер | `true` |
| `protocol` | Протокол MCP сервера | Должен быть установлен в `STATELESS` для включения stateless сервера |
| `tool-callback-converter` | Включить/выключить преобразование Spring AI ToolCallbacks в спецификации MCP Tool | `true` |
| `name` | Имя сервера для идентификации | `mcp-server` |
| `version` | Версия сервера | `1.0.0` |
| `instructions` | Дополнительные инструкции для взаимодействия с клиентом | `null` |
| `type` | Тип сервера (SYNC/ASYNC) | `SYNC` |
| `capabilities.resource` | Включить/выключить возможности ресурсов | `true` |
| `capabilities.tool` | Включить/выключить возможности инструментов | `true` |
| `capabilities.prompt` | Включить/выключить возможности подсказок | `true` |
| `capabilities.completion` | Включить/выключить возможности завершения | `true` |
| `tool-response-mime-type` | MIME тип ответа по имени инструмента | `-` |
| `request-timeout` | Длительность таймаута запроса | `20 seconds` |

### MCP Annotations Properties

Аннотации MCP сервера предоставляют декларативный способ реализации обработчиков сервера MCP с использованием аннотаций Java.

Свойства аннотаций сервера mcp-annotations начинаются с `spring.ai.mcp.server.annotation-scanner`:

| Property | Description | Default Value |
| --- | --- | --- |
| `enabled` |  |  |
| Включить/выключить авто-сканирование аннотаций сервера MCP |  |  |
| `true` |  |  |

### Stateless Connection Properties

Все свойства подключения начинаются с `spring.ai.mcp.server.stateless`:

[options="header"]
| Property | Description | Default |
| --- | --- | --- |
| `mcp-endpoint` | Пользовательский путь конечной точки MCP | `/mcp` |
| `disallow-delete` | Запретить операции удаления | `false` |

## Features and Capabilities

MCP Server Boot Starter позволяет серверам предоставлять инструменты, ресурсы и подсказки клиентам. Он автоматически преобразует пользовательские обработчики возможностей, зарегистрированные как Spring бины, в синхронные/асинхронные спецификации в зависимости от типа сервера:

### link:https://modelcontextprotocol.io/specification/2025-03-26/server/tools[Tools]Позволяет серверам предоставлять инструменты, которые могут быть вызваны языковыми моделями. MCP Server Boot Starter предоставляет:

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
public List<McpStatelessServerFeatures.SyncToolSpecification> myTools(...) {
    List<McpStatelessServerFeatures.SyncToolSpecification> tools = ...
    return tools;
}
```

Автонастройка автоматически обнаружит и зарегистрирует все обратные вызовы инструментов из:

- Индивидуальных бинов `ToolCallback`
- Списков бинов `ToolCallback`
- Бинов `ToolCallbackProvider`

Инструменты дублируются по имени, при этом используется первое вхождение каждого имени инструмента.

> **Совет:** Вы можете отключить автоматическое обнаружение и регистрацию всех обратных вызовов инструментов, установив `tool-callback-converter` в `false`.

> **Примечание:** Поддержка контекста инструментов не применима для статeless-серверов.

### link:https://modelcontextprotocol.io/specification/2025-03-26/server/resources/[Ресурсы]

Предоставляет стандартизированный способ для серверов открывать ресурсы для клиентов.

- Спецификации статических и динамических ресурсов
- Необязательные уведомления об изменениях
- Поддержка шаблонов ресурсов
- Автоматическая конвертация между синхронными/асинхронными спецификациями ресурсов
- Автоматическая спецификация ресурсов через Spring бины:

```java
@Bean
public List<McpStatelessServerFeatures.SyncResourceSpecification> myResources(...) {
    var systemInfoResource = new McpSchema.Resource(...);
    var resourceSpecification = new McpStatelessServerFeatures.SyncResourceSpecification(systemInfoResource, (context, request) -> {
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
- Автоматическая конвертация между синхронными/асинхронными спецификациями подсказок
- Автоматическая спецификация подсказок через Spring бины:

```java
@Bean
public List<McpStatelessServerFeatures.SyncPromptSpecification> myPrompts() {
    var prompt = new McpSchema.Prompt("greeting", "Шаблон дружелюбного приветствия",
        List.of(new McpSchema.PromptArgument("name", "Имя для приветствия", true)));

    var promptSpecification = new McpStatelessServerFeatures.SyncPromptSpecification(prompt, (context, getPromptRequest) -> {
        String nameArgument = (String) getPromptRequest.arguments().get("name");
        if (nameArgument == null) { nameArgument = "друг"; }
        var userMessage = new PromptMessage(Role.USER, new TextContent("Привет " + nameArgument + "! Как я могу помочь вам сегодня?"));
        return new GetPromptResult("Персонализированное приветственное сообщение", List.of(userMessage));
    });

    return List.of(promptSpecification);
}
```

### link:https://modelcontextprotocol.io/specification/2025-03-26/server/utilities/completion/[Завершение]Предоставляет стандартизированный способ для серверов раскрывать возможности завершения клиентам.

- Поддержка как синхронных, так и асинхронных спецификаций завершения
- Автоматическая регистрация через Spring бины:

```java
@Bean
public List<McpStatelessServerFeatures.SyncCompletionSpecification> myCompletions() {
    var completion = new McpStatelessServerFeatures.SyncCompletionSpecification(
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

## Примеры использования

### Конфигурация без состояния сервера
```yaml
spring:
  ai:
    mcp:
      server:
        protocol: STATELESS
        name: stateless-mcp-server
        version: 1.0.0
        type: ASYNC
        instructions: "Этот сервер без состояния оптимизирован для облачных развертываний"
        streamable-http:          
          mcp-endpoint: /api/mcp
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

Авто-конфигурация автоматически зарегистрирует обратные вызовы инструментов как инструменты MCP. Вы можете иметь несколько бинов, производящих ToolCallbacks, и авто-конфигурация объединит их.
