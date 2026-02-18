# MCP Server Boot Starter

[Серверы Model Context Protocol (MCP)](https://modelcontextprotocol.io/docs/learn/server-concepts) — это программы, которые предоставляют определенные возможности для AI-приложений через стандартизированные интерфейсы протоколов. Каждый сервер обеспечивает специализированную функциональность для конкретной области.

Spring AI MCP Server Boot Starters предоставляют автоматическую конфигурацию для настройки [MCP серверов](https://modelcontextprotocol.io/docs/learn/server-concepts) в приложениях Spring Boot. Они обеспечивают бесшовную интеграцию возможностей MCP сервера с системой автоматической конфигурации Spring Boot.

MCP Server Boot Starters предлагают:

- Автоматическую конфигурацию компонентов MCP сервера, включая инструменты, ресурсы и подсказки
- Поддержку различных версий протокола MCP, включая STDIO, SSE, Streamable-HTTP и безсостояние серверов
- Поддержку как синхронных, так и асинхронных режимов работы
- Несколько вариантов транспортного уровня
- Гибкую спецификацию инструментов, ресурсов и подсказок
- Возможности уведомления об изменениях
- xref:api/mcp/mcp-annotations-server.adoc[Разработка серверов на основе аннотаций] с автоматическим сканированием и регистрацией бинов

## MCP Server Boot Starters

MCP серверы поддерживают несколько протоколов и транспортных механизмов. Используйте специальный стартер и правильное свойство `spring.ai.mcp.server.protocol` для настройки вашего сервера:

### STDIO

[options="header"]
| Тип сервера | Зависимость | Свойство |
| --- | --- | --- |
| xref:api/mcp/mcp-stdio-sse-server-boot-starter-docs.adoc[Стандартный ввод/вывод (STDIO)] | `spring-ai-starter-mcp-server` | `spring.ai.mcp.server.stdio=true` |

### WebMVC

| Тип сервера | Зависимость | Свойство |
| --- | --- | --- |
| xref:api/mcp/mcp-stdio-sse-server-boot-starter-docs.adoc#_sse_webmvc_serve[SSE WebMVC] | `spring-ai-starter-mcp-server-webmvc` | `spring.ai.mcp.server.protocol=SSE` или пусто |
| xref:api/mcp/mcp-streamable-http-server-boot-starter-docs.adoc#_streamable_http_webmvc_server[Streamable-HTTP WebMVC] | `spring-ai-starter-mcp-server-webmvc` | `spring.ai.mcp.server.protocol=STREAMABLE` |
| xref:api/mcp/mcp-stateless-server-boot-starter-docs.adoc#_stateless_webmvc_server[Безсостояние WebMVC] | `spring-ai-starter-mcp-server-webmvc` | `spring.ai.mcp.server.protocol=STATELESS` |

### WebMVC (Reactive)
| Тип сервера | Зависимость | Свойство |
| --- | --- | --- |
| xref:api/mcp/mcp-stdio-sse-server-boot-starter-docs.adoc#_sse_webflux_serve[SSE WebFlux] | `spring-ai-starter-mcp-server-webflux` | `spring.ai.mcp.server.protocol=SSE` или пусто |
| xref:api/mcp/mcp-streamable-http-server-boot-starter-docs.adoc#_streamable_http_webflux_server[Streamable-HTTP WebFlux] | `spring-ai-starter-mcp-server-webflux` | `spring.ai.mcp.server.protocol=STREAMABLE` |
| xref:api/mcp/mcp-stateless-server-boot-starter-docs.adoc#_stateless_webflux_server[Безсостояние WebFlux] | `spring-ai-starter-mcp-server-webflux` | `spring.ai.mcp.server.protocol=STATELESS` |

## Возможности сервера

В зависимости от типов сервера и транспорта, MCP серверы могут поддерживать различные возможности, такие как:

- **Инструменты** - Позволяет серверам предоставлять инструменты, которые могут быть вызваны языковыми моделями
- **Ресурсы** - Обеспечивает стандартизированный способ для серверов предоставлять ресурсы клиентам
- **Подсказки** - Обеспечивает стандартизированный способ для серверов предоставлять шаблоны подсказок клиентам
- **Утилита/Завершения** - Обеспечивает стандартизированный способ для серверов предлагать автозавершение аргументов для подсказок и URI ресурсов
- **Утилита/Логирование** - Обеспечивает стандартизированный способ для серверов отправлять структурированные сообщения журналов клиентам
- **Утилита/Прогресс** - Необязательное отслеживание прогресса для длительных операций через уведомления
- **Утилита/Пинг** - Необязательный механизм проверки работоспособности сервера для отчета о его состоянии

Все возможности включены по умолчанию. Отключение возможности предотвратит регистрацию сервера и предоставление соответствующих функций клиентам.

## Протоколы сервераMCP предоставляет несколько типов протоколов, включая:

- xref:api/mcp/mcp-stdio-sse-server-boot-starter-docs.adoc[**STDIO**] - Протокол в процессе (например, сервер работает внутри хост-приложения). Связь осуществляется через стандартный ввод и стандартный вывод. Чтобы включить `STDIO`, установите `spring.ai.mcp.server.stdio=true`.
- xref:api/mcp/mcp-stdio-sse-server-boot-starter-docs.adoc#_sse_webmvc_server[**SSE**] - Протокол событий, отправляемых сервером, для обновлений в реальном времени. Сервер работает как независимый процесс, который может обрабатывать несколько подключений клиентов.
- xref:api/mcp/mcp-streamable-http-server-boot-starter-docs.adoc[**Streamable-HTTP**] - [Streamable HTTP транспорт](https://modelcontextprotocol.io/specification/2025-06-18/basic/transports#streamable-http) позволяет серверам MCP работать как независимые процессы, которые могут обрабатывать несколько подключений клиентов с использованием HTTP POST и GET запросов, с опциональной потоковой передачей событий, отправляемых сервером (SSE) для нескольких сообщений от сервера. Он заменяет транспорт SSE. Чтобы включить протокол `STREAMABLE`, установите `spring.ai.mcp.server.protocol=STREAMABLE`.
- xref:api/mcp/mcp-stateless-server-boot-starter-docs.adoc[**Stateless**] - Безсостоящие серверы MCP предназначены для упрощенных развертываний, где состояние сессии не сохраняется между запросами. Они идеально подходят для архитектур микросервисов и облачных развертываний. Чтобы включить протокол `STATELESS`, установите `spring.ai.mcp.server.protocol=STATELESS`.

## Опции API сервера Sync/Async

API сервера MCP поддерживает императивные (т.е. синхронные) и реактивные (например, асинхронные) модели программирования.

- **Синхронный сервер** - Тип сервера по умолчанию, реализованный с использованием `McpSyncServer`. Он предназначен для простых паттернов запрос-ответ в ваших приложениях. Чтобы включить этот тип сервера, установите `spring.ai.mcp.server.type=SYNC` в вашей конфигурации. При активации он автоматически обрабатывает конфигурацию синхронных спецификаций инструментов.

**ПРИМЕЧАНИЕ:** Синхронный сервер будет регистрировать только синхронные методы с аннотациями MCP. Асинхронные методы будут проигнорированы.

- **Асинхронный сервер** - Реализация асинхронного сервера использует `McpAsyncServer` и оптимизирована для неблокирующих операций. Чтобы включить этот тип сервера, настройте ваше приложение с помощью `spring.ai.mcp.server.type=ASYNC`. Этот тип сервера автоматически настраивает асинхронные спецификации инструментов с поддержкой встроенного Project Reactor.

**ПРИМЕЧАНИЕ:** Асинхронный сервер будет регистрировать только асинхронные методы с аннотациями MCP. Синхронные методы будут проигнорированы.

## Аннотации сервера MCP

Загрузчики сервера MCP Boot предоставляют всестороннюю поддержку разработки серверов на основе аннотаций, позволяя вам создавать серверы MCP с использованием декларативных аннотаций Java вместо ручной конфигурации.

### Ключевые аннотации

- **xref:api/mcp/mcp-annotations-server.adoc#_mcptool[@McpTool]** - Помечает методы как инструменты MCP с автоматической генерацией схемы JSON
- **xref:api/mcp/mcp-annotations-server.adoc#_mcpresource[@McpResource]** - Обеспечивает доступ к ресурсам через шаблоны URI
- **xref:api/mcp/mcp-annotations-server.adoc#_mcpprompt[@McpPrompt]** - Генерирует сообщения подсказок для взаимодействия с ИИ
- **xref:api/mcp/mcp-annotations-server.adoc#_mcpcomplete[@McpComplete]** - Обеспечивает функциональность автозаполнения для подсказок

### Специальные параметры

Система аннотаций поддерживает xref:api/mcp/mcp-annotations-special-params.adoc[типы специальных параметров], которые предоставляют дополнительный контекст:

- **`McpMeta`** - Доступ к метаданным из запросов MCP
- **`@McpProgressToken`** - Получение токенов прогресса для длительных операций
- **`McpSyncServerExchange`/`McpAsyncServerExchange`** - Полный контекст сервера для сложных операций
- **`McpTransportContext`** - Легковесный контекст для безсостоящих операций
- **`CallToolRequest`** - Поддержка динамической схемы для гибких инструментов

### Простой пример```java
@Component
public class CalculatorTools {

    @McpTool(name = "add", description = "Сложить два числа")
    public int add(
            @McpToolParam(description = "Первое число", required = true) int a,
            @McpToolParam(description = "Второе число", required = true) int b) {
        return a + b;
    }

    @McpResource(uri = "config://{key}", name = "Конфигурация")
    public String getConfig(String key) {
        return configData.get(key);
    }
}
```

### Добавление данных в McpTransportContext

По умолчанию `McpTransportContext` пустой (`McpTransportContext.EMPTY`).
Это сделано намеренно, чтобы сохранить независимость транспорта MCP-сервера.

Если вам нужны специфические для транспорта метаданные (например, HTTP-заголовки, удаленный хост и т. д.) в ваших инструментах,
настройте `TransportContextExtractor` на вашем транспортном провайдере.

```java
@Bean
public WebMvcStreamableServerTransportProvider transport(ObjectMapper objectMapper) {
    return WebMvcStreamableServerTransportProvider.builder()
        .contextExtractor(serverRequest -> {
            String authorization = serverRequest.headers().firstHeader("Authorization");
            return McpTransportContext.create(Map.of("authorization", authorization));
        })
        .build();
}
```

После настройки получите доступ к контексту через `McpSyncRequestContext` (или `McpAsyncRequestContext`) в вашем инструменте.

```java
@McpTool
public String accessProtectedResource(McpSyncRequestContext requestContext) {
    McpTransportContext context = requestContext.transportContext();
    String authorization = (String) context.get("authorization");

    return "Успешно получен доступ к защищенному ресурсу.";
}
```

### Автоконфигурация

С помощью автоконфигурации Spring Boot аннотированные бины автоматически обнаруживаются и регистрируются:

```java
@SpringBootApplication
public class McpServerApplication {
    public static void main(String[] args) {
        SpringApplication.run(McpServerApplication.class, args);
    }
}
```

Автоконфигурация будет:

1. Сканировать бины с аннотациями MCP
2. Создавать соответствующие спецификации
3. Регистрировать их в MCP-сервере
4. Обрабатывать как синхронные, так и асинхронные реализации на основе конфигурации

### Свойства конфигурации

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

### Дополнительные ресурсы

- xref:api/mcp/mcp-annotations-server.adoc[Справочник по аннотациям сервера] - Полное руководство по аннотациям сервера
- xref:api/mcp/mcp-annotations-special-params.adoc[Специальные параметры] - Расширенная инъекция параметров
- xref:api/mcp/mcp-annotations-examples.adoc[Примеры] - Полные примеры и случаи использования


## Пример приложений

- [Сервер погоды (SSE WebFlux)](https://github.com/spring-projects/spring-ai-examples/tree/main/model-context-protocol/weather/starter-webflux-server) - Spring AI MCP Server Boot Starter с транспортом WebFlux
- [Сервер погоды (STDIO)](https://github.com/spring-projects/spring-ai-examples/tree/main/model-context-protocol/weather/starter-stdio-server) - Spring AI MCP Server Boot Starter с транспортом STDIO
- [Сервер погоды с ручной конфигурацией](https://github.com/spring-projects/spring-ai-examples/tree/main/model-context-protocol/weather/manual-webflux-server) - Spring AI MCP Server Boot Starter, который не использует автоконфигурацию, а использует Java SDK для ручной настройки сервера
- Пример Streamable-HTTP WebFlux/WebMVC - TODO
- Пример Stateless WebFlux/WebMVC - TODO

## Дополнительные ресурсы
```- xref:api/mcp/mcp-annotations-server.adoc[MCP Server Annotations] - Декларативная разработка серверов с помощью аннотаций
- xref:api/mcp/mcp-annotations-special-params.adoc[Special Parameters] - Расширенная инъекция параметров и доступ к контексту
- xref:api/mcp/mcp-annotations-examples.adoc[MCP Annotations Examples] - Полные примеры и сценарии использования
- [Документация Spring AI](https://docs.spring.io/spring-ai/reference/)
- [Спецификация протокола контекста модели](https://modelcontextprotocol.io/specification)
- [Автонастройка Spring Boot](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.developing-auto-configuration)
