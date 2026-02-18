# Model Context Protocol (MCP)

> **Совет:** **Вы новичок в MCP?** Начните с нашего xref:guides/getting-started-mcp.adoc[Руководства по началу работы с MCP] для быстрого введения и практических примеров.

[Model Context Protocol](https://modelcontextprotocol.org/docs/concepts/architecture) (MCP) — это стандартизированный протокол, который позволяет ИИ-моделям взаимодействовать с внешними инструментами и ресурсами структурированным образом. Рассматривайте его как мост между вашими ИИ-моделями и реальным миром, позволяя им получать доступ к базам данных, API, файловым системам и другим внешним сервисам через единый интерфейс. Он поддерживает несколько механизмов передачи для обеспечения гибкости в различных средах.

[MCP Java SDK](https://modelcontextprotocol.io/sdk/java/mcp-overview) предоставляет реализацию Model Context Protocol на Java, позволяя стандартизированное взаимодействие с ИИ-моделями и инструментами как через синхронные, так и асинхронные коммуникационные паттерны.

Spring AI поддерживает MCP с помощью комплексной поддержки через специальные Boot Starters и аннотации MCP Java, что делает создание сложных приложений на базе ИИ, которые могут бесшовно подключаться к внешним системам, проще простого. Это означает, что разработчики Spring могут участвовать в обеих сторонах экосистемы MCP — создавая ИИ-приложения, которые используют MCP-серверы, и создавая MCP-серверы, которые предоставляют услуги на базе Spring более широкой ИИ-сообществу. Начните разработку ваших ИИ-приложений с поддержкой MCP, используя [Spring Initializer](https://start.spring.io).

## Архитектура MCP Java SDK

> **Совет:** Этот раздел предоставляет обзор [архитектуры MCP Java SDK](https://modelcontextprotocol.io/sdk/java/mcp-overview). Для интеграции Spring AI MCP обратитесь к документации xref:#_spring_ai_mcp_integration[Spring AI MCP Boot Starters].

Java-реализация MCP следует трехуровневой архитектуре, которая разделяет обязанности для удобства обслуживания и гибкости:

.МCP Stack Architecture
![MCP Stack Architecture, align=center](mcp/mcp-stack.svg)

### Уровень Клиент/Сервер (Верхний)

Верхний уровень обрабатывает основную бизнес-логику приложения и операции протокола:

- **McpClient** - Управляет операциями на стороне клиента и соединениями с сервером
- **McpServer** - Обрабатывает операции протокола на стороне сервера и запросы клиентов
- Оба компонента используют нижележащий уровень сессий для управления коммуникацией

### Уровень Сессий (Средний)

Средний уровень управляет коммуникационными паттернами и поддерживает состояние соединения:

- **McpSession** - Основной интерфейс управления сессиями
- **McpClientSession** - Реализация сессии, специфичная для клиента
- **McpServerSession** - Реализация сессии, специфичная для сервера

### Транспортный Уровень (Нижний)

Нижний уровень обрабатывает фактическую передачу сообщений и сериализацию:

- **McpTransport** - Управляет сериализацией и десериализацией сообщений JSON-RPC
- Поддерживает несколько реализаций транспорта (STDIO, HTTP/SSE, Streamable-HTTP и др.)
- Обеспечивает основу для всей вышестоящей коммуникации

| link:https://modelcontextprotocol.io/sdk/java/mcp-client[MCP Client] |
| --- |
| --- |

| link:https://modelcontextprotocol.io/sdk/java/mcp-server[MCP Server] |
| --- |
| --- |

Для получения подробных рекомендаций по реализации с использованием низкоуровневых API MCP Client/Server обратитесь к [документации MCP Java SDK](https://modelcontextprotocol.io/sdk/java/mcp-overview). Для упрощенной настройки с использованием Spring Boot используйте описанные ниже MCP Boot Starters.

## Интеграция Spring AI MCP

Spring AI предоставляет интеграцию MCP через следующие Spring Boot стартеры:

### link:mcp-client-boot-starter-docs.html[Клиентские Стартеры]

- `spring-ai-starter-mcp-client` - Основной стартер, предоставляющий поддержку `STDIO`, Servlet-основанного `Streamable-HTTP`, `Stateless Streamable-HTTP` и `SSE`
- `spring-ai-starter-mcp-client-webflux` - Реализация транспорта на основе WebFlux для `Streamable-HTTP`, `Stateless Streamable-HTTP` и `SSE`

### link:mcp-server-boot-starter-docs.html[Серверные Стартеры]

#### STDIO[options="header"]
| Тип сервера | Зависимость | Свойство |
| --- | --- | --- |
| xref:api/mcp/mcp-stdio-sse-server-boot-starter-docs.adoc[Стандартный ввод/вывод (STDIO)] | `spring-ai-starter-mcp-server` | `spring.ai.mcp.server.stdio=true` |

#### WebMVC

| Тип сервера | Зависимость | Свойство |
| --- | --- | --- |
| xref:api/mcp/mcp-stdio-sse-server-boot-starter-docs.adoc#_sse_webmvc_serve[SSE WebMVC] | `spring-ai-starter-mcp-server-webmvc` | `spring.ai.mcp.server.protocol=SSE` или пусто |
| xref:api/mcp/mcp-streamable-http-server-boot-starter-docs.adoc#_streamable_http_webmvc_server[Streamable-HTTP WebMVC] | `spring-ai-starter-mcp-server-webmvc` | `spring.ai.mcp.server.protocol=STREAMABLE` |
| xref:api/mcp/mcp-stateless-server-boot-starter-docs.adoc#_stateless_webmvc_server[Stateless Streamable-HTTP WebMVC] | `spring-ai-starter-mcp-server-webmvc` | `spring.ai.mcp.server.protocol=STATELESS` |

#### WebMVC (Reactive)
| Тип сервера | Зависимость | Свойство |
| --- | --- | --- |
| xref:api/mcp/mcp-stdio-sse-server-boot-starter-docs.adoc#_sse_webflux_serve[SSE WebFlux] | `spring-ai-starter-mcp-server-webflux` | `spring.ai.mcp.server.protocol=SSE` или пусто |
| xref:api/mcp/mcp-streamable-http-server-boot-starter-docs.adoc#_streamable_http_webflux_server[Streamable-HTTP WebFlux] | `spring-ai-starter-mcp-server-webflux` | `spring.ai.mcp.server.protocol=STREAMABLE` |
| xref:api/mcp/mcp-stateless-server-boot-starter-docs.adoc#_stateless_webflux_server[Stateless Streamable-HTTP WebFlux] | `spring-ai-starter-mcp-server-webflux` | `spring.ai.mcp.server.protocol=STATELESS` |

## xref:api/mcp/mcp-annotations-overview.adoc[Аннотации Spring AI MCP]

В дополнение к программной конфигурации клиента и сервера MCP, Spring AI предоставляет обработку методов на основе аннотаций для серверов и клиентов MCP через модуль xref:api/mcp/mcp-annotations-overview.adoc[Аннотации MCP]. 
Этот подход упрощает создание и регистрацию операций MCP, используя чистую декларативную модель программирования с аннотациями Java.

Модуль аннотаций MCP позволяет разработчикам:

- Создавать инструменты, ресурсы и подсказки MCP с помощью простых аннотаций
- Обрабатывать уведомления и запросы на стороне клиента декларативно
- Сокращать количество шаблонного кода и улучшать поддерживаемость
- Автоматически генерировать JSON-схемы для параметров инструментов
- Получать доступ к специальным параметрам и информации о контексте

Ключевые особенности включают:   

- xref:api/mcp/mcp-annotations-server.adoc[Аннотации сервера]: `@McpTool`, `@McpResource`, `@McpPrompt`, `@McpComplete`
- xref:api/mcp/mcp-annotations-client.adoc[Аннотации клиента]: `@McpLogging`, `@McpSampling`, `@McpElicitation`, `@McpProgress`
- xref:api/mcp/mcp-annotations-special-params.adoc[Специальные параметры]: `McpSyncServerExchange`, `McpAsyncServerExchange`, `McpTransportContext`, `McpMeta`
- **Автоматическое обнаружение**: Сканирование аннотаций с настраиваемым включением/исключением пакетов
- **Интеграция с Spring Boot**: Бесшовная интеграция с MCP Boot Starters

## Дополнительные ресурсы

- xref:api/mcp/mcp-annotations-overview.adoc[Документация по аннотациям MCP]
- [Документация по MCP Client Boot Starters](mcp-client-boot-starter-docs.html)
- [Документация по MCP Server Boot Starters](mcp-server-boot-starter-docs.html)
- [Документация по утилитам MCP](mcp-helpers.html)
- [Спецификация протокола контекста модели](https://modelcontextprotocol.github.io/specification/)
