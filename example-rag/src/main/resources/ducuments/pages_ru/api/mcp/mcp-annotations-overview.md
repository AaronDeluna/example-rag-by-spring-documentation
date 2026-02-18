# MCP Аннотации

Модуль аннотаций Spring AI MCP предоставляет обработку методов на основе аннотаций для серверов и клиентов [Model Context Protocol (MCP)](https://github.com/modelcontextprotocol/spec) на Java. 
Он упрощает создание и регистрацию методов сервера MCP и обработчиков клиентов с помощью чистого, декларативного подхода, использующего аннотации Java.

Аннотации MCP позволяют разработчикам создавать и регистрировать обработчики операций MCP, используя декларативные аннотации. 
Этот подход упрощает реализацию функциональности сервера и клиента MCP, уменьшая количество шаблонного кода и улучшая поддерживаемость.

Эта библиотека строится на основе [MCP Java SDK](https://github.com/modelcontextprotocol/java-sdk), чтобы предоставить более высокий уровень, основанный на аннотациях, программной модели для реализации серверов и клиентов MCP.

## Архитектура

Модуль аннотаций MCP состоит из:

### Аннотации сервера

Для серверов MCP предоставляются следующие аннотации:

- `@McpTool` - Реализует инструменты MCP с автоматической генерацией схемы JSON
- `@McpResource` - Обеспечивает доступ к ресурсам через шаблоны URI
- `@McpPrompt` - Генерирует сообщения подсказок
- `@McpComplete` - Обеспечивает функциональность автозаполнения

### Аннотации клиента

Для клиентов MCP предоставляются следующие аннотации:

- `@McpLogging` - Обрабатывает уведомления о сообщениях журнала
- `@McpSampling` - Обрабатывает запросы на выборку
- `@McpElicitation` - Обрабатывает запросы на получение дополнительной информации
- `@McpProgress` - Обрабатывает уведомления о прогрессе во время длительных операций
- `@McpToolListChanged` - Обрабатывает уведомления об изменении списка инструментов
- `@McpResourceListChanged` - Обрабатывает уведомления об изменении списка ресурсов
- `@McpPromptListChanged` - Обрабатывает уведомления об изменении списка подсказок

### Специальные параметры и аннотации

- `McpSyncRequestContext` - Специальный тип параметра для синхронных операций, который предоставляет унифицированный интерфейс для доступа к контексту запроса MCP, включая оригинальный запрос, обмен сервером (для состояний операций), транспортный контекст (для статeless операций) и удобные методы для ведения журнала, прогресса, выборки и получения информации. Этот параметр автоматически внедряется и исключается из генерации схемы JSON. **Поддерживается в методах Complete, Prompt, Resource и Tool.**
- `McpAsyncRequestContext` - Специальный тип параметра для асинхронных операций, который предоставляет тот же унифицированный интерфейс, что и `McpSyncRequestContext`, но с реактивными (основанными на Mono) типами возвращаемых значений. Этот параметр автоматически внедряется и исключается из генерации схемы JSON. **Поддерживается в методах Complete, Prompt, Resource и Tool.**
- `McpTransportContext` - Специальный тип параметра для stateless операций, который предоставляет легкий доступ к контексту на уровне транспорта без полной функциональности обмена сервером. Этот параметр автоматически внедряется и исключается из генерации схемы JSON.
- `@McpProgressToken` - Помечает параметр метода для получения токена прогресса из запроса. Этот параметр автоматически внедряется и исключается из сгенерированной схемы JSON. **Примечание:** При использовании `McpSyncRequestContext` или `McpAsyncRequestContext` токен прогресса можно получить через `ctx.request().progressToken()`, вместо использования этой аннотации.
- `McpMeta` - Специальный тип параметра, который предоставляет доступ к метаданным из запросов, уведомлений и результатов MCP. Этот параметр автоматически внедряется и исключается из ограничений по количеству параметров и генерации схемы JSON. **Примечание:** При использовании `McpSyncRequestContext` или `McpAsyncRequestContext` метаданные можно получить через `ctx.requestMeta()`.

## Начало работы

### ЗависимостиДобавьте зависимость аннотаций MCP в ваш проект:

```xml
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-mcp-annotations</artifactId>
</dependency>
```

Аннотации MCP автоматически включаются при использовании любого из стартовых пакетов MCP Boot:

- `spring-ai-starter-mcp-client`
- `spring-ai-starter-mcp-client-webflux`
- `spring-ai-starter-mcp-server`
- `spring-ai-starter-mcp-server-webflux`
- `spring-ai-starter-mcp-server-webmvc`

### Конфигурация

Сканирование аннотаций включено по умолчанию при использовании стартовых пакетов MCP Boot. Вы можете настроить поведение сканирования, используя следующие свойства:

#### Сканер аннотаций клиента

```yaml
spring:
  ai:
    mcp:
      client:
        annotation-scanner:
          enabled: true  # Включить/выключить сканирование аннотаций
```

#### Сканер аннотаций сервера

```yaml
spring:
  ai:
    mcp:
      server:
        annotation-scanner:
          enabled: true  # Включить/выключить сканирование аннотаций
```

## Простой пример

Вот простой пример использования аннотаций MCP для создания инструмента калькулятора:

```java
@Component
public class CalculatorTools {

    @McpTool(name = "add", description = "Сложить два числа")
    public int add(
            @McpToolParam(description = "Первое число", required = true) int a,
            @McpToolParam(description = "Второе число", required = true) int b) {
        return a + b;
    }

    @McpTool(name = "multiply", description = "Умножить два числа")
    public double multiply(
            @McpToolParam(description = "Первое число", required = true) double x,
            @McpToolParam(description = "Второе число", required = true) double y) {
        return x * y;
    }
}
```

И простой обработчик клиента для логирования:

```java
@Component
public class LoggingHandler {

    @McpLogging(clients = "my-server")
    public void handleLoggingMessage(LoggingMessageNotification notification) {
        System.out.println("Получено сообщение лога: " + notification.level() + 
                          " - " + notification.data());
    }
}
```

С автоматической конфигурацией Spring Boot эти аннотированные бины автоматически обнаруживаются и регистрируются на сервере или клиенте MCP.

## Документация

- xref:api/mcp/mcp-annotations-client.adoc[Аннотации клиента] - Подробное руководство по аннотациям на стороне клиента
- xref:api/mcp/mcp-annotations-server.adoc[Аннотации сервера] - Подробное руководство по аннотациям на стороне сервера
- xref:api/mcp/mcp-annotations-special-params.adoc[Специальные параметры] - Руководство по специальным типам параметров
- xref:api/mcp/mcp-annotations-examples.adoc[Примеры] - Комплексные примеры и сценарии использования

## Дополнительные ресурсы

- xref:api/mcp/mcp-overview.adoc[Обзор MCP]
- xref:api/mcp/mcp-client-boot-starter-docs.adoc[Документация по стартовому пакету клиента MCP]
- xref:api/mcp/mcp-server-boot-starter-docs.adoc[Документация по стартовому пакету сервера MCP]
- [Спецификация протокола контекста модели](https://modelcontextprotocol.github.io/specification/)
