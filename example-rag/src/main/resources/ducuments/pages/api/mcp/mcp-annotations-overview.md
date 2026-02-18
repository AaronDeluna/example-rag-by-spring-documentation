# Аннотации MCP

Модуль Spring AI MCP Annotations обеспечивает обработку методов на основе аннотаций для серверов и клиентов [Протокол контекста модели (MCP)](https://github.com/modelcontextprotocol/spec) на Java. 
Он упрощает создание и регистрацию методов сервера MCP и обработчиков клиентов за счет чистого декларативного подхода с использованием аннотаций Java.

 Аннотации MCP позволяют разработчикам создавать и регистрировать обработчики операций MCP, используя декларативные аннотации.
Этот подход упрощает реализацию функций сервера и клиента MCP за счет сокращения шаблонного кода и улучшения удобства обслуживания.

Эта библиотека построена на основе [MCP Java SDK](https://github.com/modelcontextprotocol/java-sdk) и обеспечивает высокоуровневую модель программирования на основе аннотаций для реализации серверов и клиентов MCP.

## Архитектура

Модуль аннотаций MCP состоит из:

### Аннотации сервера

Для серверов MCP предоставляются следующие аннотации:

- `@McpTool` — реализует инструменты MCP с автоматическим созданием схемы JSON.
- `@McpResource` — обеспечивает доступ к ресурсам через шаблоны URI.
- `@McpPrompt` — генерирует подсказки
- `@McpComplete` – обеспечивает функцию автозаполнения.

### Аннотации клиента

Для клиентов MCP предоставляются следующие аннотации:

- `@McpLogging` — обрабатывает уведомления о сообщениях журнала.
- `@McpSampling` – обрабатывает запросы на выборку.
- `@McpElicitation` – обрабатывает запросы на получение дополнительной информации.
- `@McpProgress` — обрабатывает уведомления о ходе выполнения во время длительных операций.
- `@McpToolListChanged` – обрабатывает уведомления об изменении списка инструментов.
- `@McpResourceListChanged` — обрабатывает уведомления об изменении списка ресурсов.
- `@McpPromptListChanged` – обрабатывает уведомления об изменении списка подсказок.


### Специальные параметры и аннотации

- `McpSyncRequestContext` — специальный тип параметра для синхронных операций, который обеспечивает унифицированный интерфейс для доступа к контексту запроса MCP, включая исходный запрос, обмен сервером (для операций с отслеживанием состояния), транспортный контекст (для операций без отслеживания состояния), а также удобные методы для регистрации, выполнения, выборки и извлечения информации. Этот параметр автоматически вводится и исключается из создания схемы JSON. ***Поддерживается в методах Complete, Prompt, Resource и Tool.***
- `McpAsyncRequestContext` — специальный тип параметра для асинхронных операций, который предоставляет тот же унифицированный интерфейс, что и `McpSyncRequestContext`, но с реактивными (моно) типами возврата. Этот параметр автоматически вводится и исключается из создания схемы JSON. ***Поддерживается в методах Complete, Prompt, Resource и Tool.***
- `McpTransportContext` — специальный тип параметра для операций без отслеживания состояния, который обеспечивает упрощенный доступ к контексту транспортного уровня без полной функциональности обмена сервером. Этот параметр автоматически вводится и исключается из генерации схемы JSON.
- `@McpProgressToken` — отмечает параметр метода для получения токена выполнения запроса. Этот параметр автоматически добавляется и исключается из сгенерированной схемы JSON. ***Примечание.*** При использовании `McpSyncRequestContext` или `McpAsyncRequestContext` доступ к токену выполнения можно получить через `ctx.request().progressToken()` вместо использования этой аннотации.
- `McpMeta` — специальный тип параметра, обеспечивающий доступ к метаданным из запросов, уведомлений и результатов MCP. Этот параметр автоматически вводится и исключается из ограничений количества параметров и создания схемы JSON. ***Примечание.*** При использовании `McpSyncRequestContext` или `McpAsyncRequestContext` метаданные можно получить через `ctx.requestMeta()`.

## Начиная

### Зависимости

Добавьте зависимость аннотаций MCP в свой проект:

```xml
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-mcp-annotations</artifactId>
</dependency>
```

Аннотации MCP автоматически включаются при использовании любого из загрузочных стартеров MCP:

- `spring-ai-starter-mcp-client`
- `spring-ai-starter-mcp-client-webflux`
- `spring-ai-starter-mcp-server`
- `spring-ai-starter-mcp-server-webflux`
- `spring-ai-starter-mcp-server-webmvc`

### Конфигурация

Сканирование аннотаций включено по умолчанию при использовании загрузочных стартеров MCP. Вы можете настроить поведение сканирования, используя следующие свойства:

#### Клиентский сканер аннотаций

```yaml
spring:
  ai:
    mcp:
      client:
        annotation-scanner:
          enabled: true  # Enable/disable annotation scanning
```

#### Серверный сканер аннотаций

```yaml
spring:
  ai:
    mcp:
      server:
        annotation-scanner:
          enabled: true  # Enable/disable annotation scanning
```

## Быстрый пример

Вот простой пример использования аннотаций MCP для создания калькулятора:

```java
@Component
public class CalculatorTools {

    @McpTool(name = "add", description = "Add two numbers together")
    public int add(
            @McpToolParam(description = "First number", required = true) int a,
            @McpToolParam(description = "Second number", required = true) int b) {
        return a + b;
    }

    @McpTool(name = "multiply", description = "Multiply two numbers")
    public double multiply(
            @McpToolParam(description = "First number", required = true) double x,
            @McpToolParam(description = "Second number", required = true) double y) {
        return x * y;
    }
}
```

И простой клиентский обработчик для логирования:

```java
@Component
public class LoggingHandler {

    @McpLogging(clients = "my-server")
    public void handleLoggingMessage(LoggingMessageNotification notification) {
        System.out.println("Received log: " + notification.level() + 
                          " - " + notification.data());
    }
}
```

Благодаря автоматической настройке Spring Boot эти аннотированные bean-компоненты автоматически обнаруживаются и регистрируются на сервере или клиенте MCP.

## Документация

- [Аннотации клиента](api/mcp/mcp-annotations-client.md) — подробное руководство по аннотациям на стороне клиента.
- [Аннотации сервера](api/mcp/mcp-annotations-server.md) — подробное руководство по аннотациям на стороне сервера.
- [Специальные параметры](api/mcp/mcp-annotations-special-params.md) — Руководство по специальным типам параметров
- [Примеры](api/mcp/mcp-annotations-examples.md) — подробные примеры и варианты использования.

## Дополнительные ресурсы

- [Обзор MCP](api/mcp/mcp-overview.md)
- [Стартер загрузки клиента MCP](api/mcp/mcp-client-boot-starter-docs.md)
- [Стартер загрузки сервера MCP](api/mcp/mcp-server-boot-starter-docs.md)
- [Спецификация протокола контекста модели](https://modelcontextprotocol.github.io/specification/)
