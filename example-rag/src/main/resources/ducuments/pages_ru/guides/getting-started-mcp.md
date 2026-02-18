# Начало работы с Протоколом Контекста Модели (MCP)

Протокол Контекста Модели (MCP) стандартизирует взаимодействие AI-приложений с внешними инструментами и ресурсами.

Spring рано присоединился к экосистеме MCP в качестве ключевого участника, помогая разрабатывать и поддерживать [официальный MCP Java SDK](https://github.com/modelcontextprotocol/java-sdk), который служит основой для реализаций MCP на Java. 
Основываясь на этом вкладе, Spring AI предоставляет поддержку MCP через Boot Starters и аннотации, что упрощает создание как MCP-серверов, так и клиентов.

## Вводное видео

**[Введение в Протокол Контекста Модели (MCP) - YouTube](https://www.youtube.com/watch?v=FLpS7OfD5-s)**

Начните здесь для получения вводного обзора Протокола Контекста Модели, объясняющего основные концепции и архитектуру.

## Полный учебник и исходный код

**📖 Блог-учебник:** [Подключите ваш AI ко всему](https://spring.io/blog/2025/09/16/spring-ai-mcp-intro-blog)

**💻 Полный исходный код:** [Репозиторий примера погоды MCP](https://github.com/tzolov/spring-ai-mcp-blogpost)

Учебник охватывает основные аспекты разработки MCP с использованием Spring AI, включая расширенные функции и шаблоны развертывания. 
Все примеры кода ниже взяты из этого учебника.

## Быстрый старт

Самый быстрый способ начать — это использовать аннотационный подход Spring AI. Следующие примеры взяты из блога-учебника:

### Простой MCP-сервер

```java
@Service
public class WeatherService {

    @McpTool(description = "Получить текущую температуру для местоположения")
    public String getTemperature(
            @McpToolParam(description = "Название города", required = true) String city) {
        return String.format("Текущая температура в %s: 22°C", city);
    }
}
```

Добавьте зависимость и настройте:

```xml
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-starter-mcp-server-webmvc</artifactId>
</dependency>
```

```properties
spring.ai.mcp.server.protocol=STREAMABLE
```

### Простой MCP-клиент

```java
@Bean
public CommandLineRunner demo(ChatClient chatClient, ToolCallbackProvider mcpTools) {
    return args -> {
        String response = chatClient
            .prompt("Какова погода в Париже?")
            .toolCallbacks(mcpTools)
            .call()
            .content();
        System.out.println(response);
    };
}
```

Добавьте зависимость и настройте:

```xml
<dependency>
  <groupId>org.springframework.ai</groupId>
  <artifactId>spring-ai-starter-mcp-client</artifactId>
</dependency>
```

```yaml
spring:
  ai:
    mcp:
      client:
        streamable-http:
          connections:
            weather-server:
              url: http://localhost:8080
```

## Учебные ресурсы

### Видео реализации

**[Интеграция Протокола Контекста Модели (MCP) Spring AI - YouTube](https://www.youtube.com/watch?v=hmEVUtulHTI)**

Видеопошаговое руководство по интеграции MCP в Spring AI, охватывающее как серверные, так и клиентские реализации.

## Репозиторий дополнительных примеров

Помимо примеров из учебника, репозиторий [Spring AI Examples](https://github.com/spring-projects/spring-ai-examples/tree/main/model-context-protocol) содержит множество реализаций MCP.

### Рекомендуемые точки начала

**Примеры на основе аннотаций**

- [Полный пример аннотаций](https://github.com/spring-projects/spring-ai-examples/tree/main/model-context-protocol/mcp-annotations/) - Все функции аннотаций (Клиент и Сервер)
- [Сэмплирование с аннотациями](https://github.com/spring-projects/spring-ai-examples/tree/main/model-context-protocol/sampling/annotations/) - Расширенный двунаправленный AI (Клиент и Сервер)
- [Учебник по погоде MCP](https://github.com/tzolov/spring-ai-mcp-blogpost) - Полный исходный код учебника (Клиент и Сервер)

### По случаям использования**Службы погоды:**

- [WebFlux Weather Server](https://github.com/spring-projects/spring-ai-examples/tree/main/model-context-protocol/weather/starter-webflux-server)
- [OAuth2 Secured Weather Server](https://github.com/spring-projects/spring-ai-examples/tree/main/model-context-protocol/weather/starter-webmvc-oauth2-server)

**Интеграция данных:**

- [SQLite AI Chatbot](https://github.com/spring-projects/spring-ai-examples/tree/main/model-context-protocol/sqlite/chatbot)
- [Filesystem Access Server](https://github.com/spring-projects/spring-ai-examples/tree/main/model-context-protocol/filesystem)

**Веб-интеграция:**

- [Brave Search Chatbot](https://github.com/spring-projects/spring-ai-examples/tree/main/model-context-protocol/web-search/brave-chatbot)

**Примеры клиентов:**

- [Basic MCP Client](https://github.com/spring-projects/spring-ai-examples/tree/main/model-context-protocol/client-starter/starter-default-client)
- [Annotations Client](https://github.com/spring-projects/spring-ai-examples/tree/main/model-context-protocol/mcp-annotations/mcp-annotations-client)

## Ресурсы сообщества

- [Awesome Spring AI](https://github.com/spring-ai-community/awesome-spring-ai) - Примеры и ресурсы сообщества
- [Официальная спецификация MCP](https://modelcontextprotocol.org/)
- [Официальный MCP Java SDK](https://github.com/modelcontextprotocol/java-sdk) - Java SDK, разработанный командой Spring
- [Документация MCP Java SDK](https://modelcontextprotocol.io/sdk/java/mcp-overview)

## Справочная документация

- xref:api/mcp/mcp-overview.adoc[Обзор и архитектура MCP]
- xref:api/mcp/mcp-annotations-overview.adoc[Руководство по аннотациям MCP]
- xref:api/mcp/mcp-server-boot-starter-docs.adoc[Стартеры загрузки сервера]
- xref:api/mcp/mcp-client-boot-starter-docs.adoc[Стартеры загрузки клиента]
