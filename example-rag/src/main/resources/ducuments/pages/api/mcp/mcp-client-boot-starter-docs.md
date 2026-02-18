# Стартер загрузки клиента MCP

Средство запуска загрузки клиента Spring AI MCP (Model Context Protocol) обеспечивает автоматическую настройку функций клиента MCP в приложениях Spring Boot.
Он поддерживает как синхронные, так и асинхронные реализации клиента с различными вариантами транспорта.

Стартер загрузки клиента MCP обеспечивает:

- Управление несколькими экземплярами клиента
- Автоматическая инициализация клиента (если включена)
- Поддержка нескольких именованных транспортов (STDIO, Http/SSE и Streamable HTTP).
- Интеграция со средой выполнения инструментов Spring AI.
- Возможности фильтрации инструментов для выборочного включения/исключения инструментов.
- Настраиваемое создание префикса имени инструмента во избежание конфликтов имен.
- Правильное управление жизненным циклом с автоматической очисткой ресурсов при закрытии контекста приложения.
- Настраиваемое создание клиентов через настройщики

## Закуски

### Стандартный клиент MCP

```xml
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-starter-mcp-client</artifactId>
</dependency>
```

Стандартный стартер подключается одновременно к одному или нескольким серверам MCP через транспорты `STDIO` (в процессе), `SSE`, `Streamable-HTTP` и `Stateless Streamable-HTTP`.
Транспорты SSE и Streamable-Http используют реализацию транспорта на основе JDK HttpClient.
Каждое подключение к серверу MCP создает новый экземпляр клиента MCP.
Вы можете выбрать клиенты MCP `SYNC` или `ASYNC` (примечание: нельзя смешивать синхронные и асинхронные клиенты).
Для производственного развертывания мы рекомендуем использовать соединение SSE и StreamableHttp на основе WebFlux с `spring-ai-starter-mcp-client-webflux`.

### Клиент WebFlux

Стартер WebFlux обеспечивает аналогичную функциональность стандартному стартеру, но использует реализацию транспорта Streamable-Http, Stateless Streamable-Http и SSE на основе WebFlux.

```xml
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-starter-mcp-client-webflux</artifactId>
</dependency>
```

## Свойства конфигурации

### Общие свойства

Общие свойства имеют префикс `spring.ai.mcp.client`:

[cols="3,4,3"]
| Свойство | Описание | Значение по умолчанию |
| --- |---|---|
| `enabled` |  |  |
| Включить/отключить клиент MCP |  |  |
| `true` |  |  |
| `name` |  |  |
| Имя экземпляра клиента MCP |  |  |
| `spring-ai-mcp-client` |  |  |
| `version` |  |  |
| Версия экземпляра клиента MCP |  |  |
| `1.0.0` |  |  |
| `initialized` |  |  |
| Инициализировать ли клиенты при создании |  |  |
|`true`|  |  |
| `request-timeout` |  |  |
| Продолжительность тайм-аута для запросов клиента MCP |  |  |
| `20s` |  |  |
| `type` |  |  |
| Тип клиента (SYNC или ASYNC). Все клиенты должны быть синхронизированными или асинхронными; смешивание не поддерживается |  |  |
| `SYNC` |  |  |
| `root-change-notification` |  |  |
| Включить/отключить уведомления об изменении корневого каталога для всех клиентов. |  |  |
|`true`|  |  |
| `toolcallback.enabled` |  |  |
| Включить/отключить интеграцию обратного вызова инструмента MCP со средой выполнения инструментов Spring AI. |  |  |
|`true`|  |  |

### Свойства аннотаций MCP

Аннотации клиента MCP предоставляют декларативный способ реализации обработчиков клиента MCP с использованием аннотаций Java.
Свойства клиентских mcp-аннотаций имеют префикс `spring.ai.mcp.client.annotation-scanner`:

[cols="3,4,3"]
|Свойство|Описание|Значение по умолчанию|
|---|---|---|
|`enabled`|  |  |
| Включить/отключить автоматическое сканирование аннотаций клиента MCP |  |  |
|`true`|  |  |

### Свойства транспорта Stdio

Свойства для стандартного транспорта ввода-вывода имеют префикс `spring.ai.mcp.client.stdio`:

[cols="3,4,3"]
|Свойство|Описание|Значение по умолчанию|
|---|---|---|
| `servers-configuration` |  |  |
| Ресурс, содержащий конфигурацию серверов MCP в формате JSON. |  |  |
| - |  |  |
| `connections` |  |  |
| Карта конфигураций именованных соединений stdio |  |  |
|-|  |  |
| `connections.[name].command` |  |  |
| Команда для выполнения на сервере MCP |  |  |
|-|  |  |
| `connections.[name].args` |  |  |
| Список аргументов команды |  |  |
|-|  |  |
| `connections.[name].env` |  |  |
| Карта переменных среды для серверного процесса |  |  |
|-|  |  |

Пример конфигурации:
```yaml
spring:
  ai:
    mcp:
      client:
        stdio:
          root-change-notification: true
          connections:
            server1:
              command: /path/to/server
              args:
                - --port=8080
                - --mode=production
              env:
                API_KEY: your-api-key
                DEBUG: "true"
```

Альтернативно вы можете настроить подключения stdio с помощью внешнего файла JSON, используя [Клод Десктопный формат](https://modelcontextprotocol.io/quickstart/user):

```yaml
spring:
  ai:
    mcp:
      client:
        stdio:
          servers-configuration: classpath:mcp-servers.json
```

Формат Claude Desktop выглядит так:

```json
{
  "mcpServers": {
    "filesystem": {
      "command": "npx",
      "args": [
        "-y",
        "@modelcontextprotocol/server-filesystem",
        "/Users/username/Desktop",
        "/Users/username/Downloads"
      ]
    }
  }
}
```

### Конфигурация Windows STDIO

> **Важно:** В Windows такие команды, как `npx`, `npm` и `node`, реализованы как ***пакетные файлы*** (`.cmd`), а не как собственные исполняемые файлы. Java `ProcessBuilder` не может выполнять пакетные файлы напрямую и требует оболочки `cmd.exe /c`.

#### Почему Windows требует особого обращения

Когда Java `ProcessBuilder` (используемый внутри `StdioClientTransport`) пытается запустить процесс в Windows, он может выполнить только:

- Собственные исполняемые файлы (`.exe` файлы)
- Системные команды, доступные `cmd.exe`

Пакетные файлы Windows, такие как `npx.cmd`, `npm.cmd` и даже `python.cmd` (из Microsoft Store), для их выполнения требуют оболочки `cmd.exe`.

#### Решение: оболочка cmd.exe

Оберните команды пакетного файла с помощью `cmd.exe /c`:

***Конфигурация Windows:***
```json
{
  "mcpServers": {
    "filesystem": {
      "command": "cmd.exe",
      "args": [
        "/c",
        "npx",
        "-y",
        "@modelcontextprotocol/server-filesystem",
        "C:\\Users\\username\\Desktop"
      ]
    }
  }
}
```

***Конфигурация Linux/macOS:***
```json
{
  "mcpServers": {
    "filesystem": {
      "command": "npx",
      "args": [
        "-y",
        "@modelcontextprotocol/server-filesystem",
        "/Users/username/Desktop"
      ]
    }
  }
}
```

#### Кроссплатформенная программная конфигурация

Для приложений, которым необходимо работать на разных платформах без отдельных файлов конфигурации, используйте обнаружение ОС в приложении Spring Boot:

```java
@Bean(destroyMethod = "close")
@ConditionalOnMissingBean(McpSyncClient.class)
public McpSyncClient mcpClient() {
    ServerParameters stdioParams;

    if (isWindows()) {
        // Windows: cmd.exe /c npx approach
        var winArgs = new ArrayList<>(Arrays.asList(
            "/c", "npx", "-y", "@modelcontextprotocol/server-filesystem", "target"));
        stdioParams = ServerParameters.builder("cmd.exe")
                .args(winArgs)
                .build();
    } else {
        // Linux/Mac: direct npx approach
        stdioParams = ServerParameters.builder("npx")
                .args("-y", "@modelcontextprotocol/server-filesystem", "target")
                .build();
    }

    return McpClient.sync(new StdioClientTransport(stdioParams, McpJsonMapper.createDefault()))
            .requestTimeout(Duration.ofSeconds(10))
            .build()
            .initialize();
}

private static boolean isWindows() {
    return System.getProperty("os.name").toLowerCase().contains("win");
}
```

> **Примечание:** При использовании программной конфигурации с `@Bean` добавьте `@ConditionalOnMissingBean(McpSyncClient.class)`, чтобы избежать конфликтов с автоматической настройкой из файлов JSON.

#### Соображения относительно пути

***Относительные пути*** (рекомендуется для переносимости):
```json
{
  "command": "cmd.exe",
  "args": ["/c", "npx", "-y", "@modelcontextprotocol/server-filesystem", "target"]
}
```

Сервер MCP определяет относительные пути на основе рабочего каталога приложения.

***Абсолютные пути*** (Windows требует использования обратной косой черты или экранированной косой черты):
```json
{
  "command": "cmd.exe",
  "args": ["/c", "npx", "-y", "@modelcontextprotocol/server-filesystem", "C:\\Users\\username\\project\\target"]
}
```

#### Распространенные пакетные файлы Windows, требующие cmd.exe

- `npx.cmd`, `npm.cmd` — менеджеры пакетов узлов.
- `python.cmd` — Python (установка из Microsoft Store)
- `pip.cmd` — менеджер пакетов Python
- `mvn.cmd` — оболочка Maven
- Error 500 (Server Error)!!1500.That’s an error.There was an error. Please try again later.That’s all we know.
- Пользовательские сценарии `.cmd` или `.bat`.

#### Эталонная реализация

См. [Примеры Spring AI — файловая система](https://github.com/spring-projects/spring-ai-examples/tree/main/model-context-protocol/filesystem) для полной реализации кросс-платформенного клиента MCP, который автоматически определяет ОС и соответствующим образом настраивает клиент.

### Свойства транспорта Streamable-HTTP

Используется для подключения к серверам MCP Streamable-HTTP и Streamable-HTTP без сохранения состояния.

Свойства для транспорта Streamable-HTTP имеют префикс `spring.ai.mcp.client.streamable-http`:

[cols="3,4,3"]
|Свойство|Описание|Значение по умолчанию|
|---|---|---|
|`connections`|  |  |
| Карта именованных конфигураций соединений Streamable-HTTP |  |  |
|-|  |  |
| `connections.[name].url` |  |  |
| Конечная точка базового URL-адреса для связи Streamable-Http с сервером MCP. |  |  |
|-|  |  |
| `connections.[name].endpoint` |  |  |
| конечная точкаstreamable-http (как суффикс URL-адреса), используемая для соединения |  |  |
| `/mcp` |  |  |

Пример конфигурации:
```yaml
spring:
  ai:
    mcp:
      client:
        streamable-http:
          connections:
            server1:
              url: http://localhost:8080
            server2:
              url: http://otherserver:8081
              endpoint: /custom-sse
```

### Транспортные свойства SSE

Свойства для транспорта событий, отправленных сервером (SSE), имеют префикс `spring.ai.mcp.client.sse`:

[cols="3,4,3"]
|Свойство|Описание|Значение по умолчанию|
|---|---|---|
|`connections`|  |  |
| Карта именованных конфигураций соединений SSE |  |  |
|-|  |  |
|`connections.[name].url`|  |  |
| Конечная точка базового URL-адреса для связи SSE с сервером MCP. |  |  |
|-|  |  |
| `connections.[name].sse-endpoint` |  |  |
| конечная точка sse (как суффикс URL-адреса), используемая для соединения |  |  |
| `/sse` |  |  |

Примеры конфигураций:
```yaml
spring:
  ai:
    mcp:
      client:
        sse:
          connections:
            # Simple configuration using default /sse endpoint
            server1:
              url: http://localhost:8080
            # Custom SSE endpoint
            server2:
              url: http://otherserver:8081
              sse-endpoint: /custom-sse
            # Complex URL with path and token (like MCP Hub)
            mcp-hub:
              url: http://localhost:3000
              sse-endpoint: /mcp-hub/sse/cf9ec4527e3c4a2cbb149a85ea45ab01
            # SSE endpoint with query parameters
            api-server:
              url: https://api.example.com
              sse-endpoint: /v1/mcp/events?token=abc123&format=json
```

#### Рекомендации по разделению URL-адресов

Если у вас есть полный URL-адрес SSE, разделите его на базовый URL-адрес и путь к конечной точке:

[cols="2,2"]
| Полный URL-адрес | Конфигурация |
|---|---|
| `\http://localhost:3000/mcp-hub/sse/token123` |  |
| `url: http://localhost:3000` + |  |
| `\https://api.service.com/v2/events?key=secret` |  |
| `url: https://api.service.com` + |  |
| `\http://localhost:8080/sse` |  |
| `url: http://localhost:8080` + |  |

#### Устранение неполадок с подключениями SSE

**Ошибки 404 «Не найдено»:**

- Проверьте разделение URL-адресов: убедитесь, что базовый `url` содержит только схему, хост и порт.
- Убедитесь, что `sse-endpoint` начинается с `/` и включает полный путь и параметры запроса.
- Проверьте полный URL-адрес непосредственно в браузере или закрутите, чтобы убедиться, что он доступен.

### Свойства потокового транспорта HTTP

Свойства для транспорта Streamable Http имеют префикс `spring.ai.mcp.client.streamable-http`:

[cols="3,4,3"]
|Свойство|Описание|Значение по умолчанию|
|---|---|---|
|`connections`|  |  |
| Карта именованных конфигураций соединений Streamable Http |  |  |
|-|  |  |
|`connections.[name].url`|  |  |
|Конечная точка базового URL-адреса для связи Streamable-Http с сервером MCP.|  |  |
|-|  |  |
|`connections.[name].endpoint`|  |  |
|конечная точкаstreamable-http (как суффикс URL-адреса), используемая для соединения|  |  |
|`/mcp`|  |  |

Пример конфигурации:
```yaml
spring:
  ai:
    mcp:
      client:
        streamable-http:
          connections:
            server1:
              url: http://localhost:8080
            server2:
              url: http://otherserver:8081
              endpoint: /custom-sse
```

## Функции

### Типы клиентов синхронизации/асинхронности

Стартер поддерживает два типа клиентов:

- Синхронный — тип клиента по умолчанию (`spring.ai.mcp.client.type=SYNC`), подходящий для традиционных шаблонов запроса-ответа с операциями блокировки.

***ПРИМЕЧАНИЕ.*** Клиент SYNC регистрирует только синхронные методы с аннотациями MCP. Асинхронные методы будут игнорироваться.

- Асинхронный — подходит для реактивных приложений с неблокирующими операциями, настроенных с помощью `spring.ai.mcp.client.type=ASYNC`.

***ПРИМЕЧАНИЕ.*** Клиент ASYNC регистрирует только асинхронные методы с аннотациями MCP. Синхронные методы будут игнорироваться.

### Настройка клиента

Автоматическая конфигурация предоставляет широкие возможности настройки спецификаций клиента через интерфейсы обратного вызова. Эти настройщики позволяют настраивать различные аспекты поведения клиента MCP, от таймаутов запросов до обработки событий и обработки сообщений.

#### Типы настройки

Доступны следующие варианты настройки:

- **Конфигурация запроса** – установка индивидуального времени ожидания запроса.
- [**Пользовательские обработчики выборки**](https://modelcontextprotocol.io/specification/2025-06-18/client/sampling) — стандартизированный способ запроса серверами выборки LLM (`completions` или `generations`) от LLM через клиентов. Этот поток позволяет клиентам сохранять контроль над доступом к модели, ее выбором и разрешениями, одновременно позволяя серверам использовать возможности искусственного интеллекта — без необходимости использования ключей API сервера.
- [**Доступ к файловой системе (корни)**](https://modelcontextprotocol.io/specification/2025-06-18/client/roots) — стандартизированный способ предоставления клиентам файловой системы `roots` серверам.
Корни определяют границы того, где серверы могут работать в файловой системе, позволяя им понять, к каким каталогам и файлам они имеют доступ.
Серверы могут запрашивать список корней у поддерживающих клиентов и получать уведомления при изменении этого списка.
- [**Обработчики сбора данных**](https://modelcontextprotocol.io/specification/2025-06-18/client/elicitation) — стандартизированный способ запроса серверами дополнительной информации от пользователей через клиент во время взаимодействия.
- **Обработчики событий** — обработчик клиента, который будет уведомлен при возникновении определенного события на сервере:
  - Уведомления об изменении инструментов — при изменении списка доступных инструментов сервера.
  - Уведомления об изменении ресурсов — при изменении списка доступных ресурсов сервера.
  - Уведомления об изменении подсказок — при изменении списка доступных подсказок сервера.
  - [**Обработчики журналирования**](https://modelcontextprotocol.io/specification/2025-06-18/server/utilities/logging) — стандартизированный способ отправки серверами структурированных сообщений журнала клиентам.
  - [**Обработчики прогресса**](https://modelcontextprotocol.io/specification/2025-06-18/basic/utilities/progress) — стандартизированный способ отправки серверами структурированных сообщений о ходе выполнения клиентам.

Error 500 (Server Error)!!1500.That’s an error.There was an error. Please try again later.That’s all we know.


#### Пример настройки клиента

Вы можете реализовать либо `McpSyncClientCustomizer` для синхронных клиентов, либо `McpAsyncClientCustomizer` для асинхронных клиентов, в зависимости от потребностей вашего приложения.

[табы]
======
Синхронизация::
+
```java
@Component
public class CustomMcpSyncClientCustomizer implements McpSyncClientCustomizer {
    @Override
    public void customize(String serverConfigurationName, McpClient.SyncSpec spec) {

        // Customize the request timeout configuration
        spec.requestTimeout(Duration.ofSeconds(30));

        // Sets the root URIs that this client can access.
        spec.roots(roots);

        // Sets a custom sampling handler for processing message creation requests.
        spec.sampling((CreateMessageRequest messageRequest) -> {
            // Handle sampling
            CreateMessageResult result = ...
            return result;
        });

        // Sets a custom elicitation handler for processing elicitation requests.
        spec.elicitation((ElicitRequest request) -> {
          // handle elicitation
          return new ElicitResult(ElicitResult.Action.ACCEPT, Map.of("message", request.message()));
        });

        // Adds a consumer to be notified when progress notifications are received.
        spec.progressConsumer((ProgressNotification progress) -> {
         // Handle progress notifications
        });

        // Adds a consumer to be notified when the available tools change, such as tools
        // being added or removed.
        spec.toolsChangeConsumer((List<McpSchema.Tool> tools) -> {
            // Handle tools change
        });

        // Adds a consumer to be notified when the available resources change, such as resources
        // being added or removed.
        spec.resourcesChangeConsumer((List<McpSchema.Resource> resources) -> {
            // Handle resources change
        });

        // Adds a consumer to be notified when the available prompts change, such as prompts
        // being added or removed.
        spec.promptsChangeConsumer((List<McpSchema.Prompt> prompts) -> {
            // Handle prompts change
        });

        // Adds a consumer to be notified when logging messages are received from the server.
        spec.loggingConsumer((McpSchema.LoggingMessageNotification log) -> {
            // Handle log messages
        });
    }
}
```

Асинхронный::
+
```java
@Component
public class CustomMcpAsyncClientCustomizer implements McpAsyncClientCustomizer {
    @Override
    public void customize(String serverConfigurationName, McpClient.AsyncSpec spec) {
        // Customize the async client configuration
        spec.requestTimeout(Duration.ofSeconds(30));
    }
}
```
======
Параметр `serverConfigurationName` — это имя конфигурации сервера, к которой применяется настройщик и для которого создается клиент MCP.

Автоматическая настройка клиента MCP автоматически обнаруживает и применяет все настройщики, найденные в контексте приложения.

### Транспортная поддержка

Автоконфигурация поддерживает несколько типов транспорта:

- Стандартный ввод-вывод (Stdio) (активируется `spring-ai-starter-mcp-client` и `spring-ai-starter-mcp-client-webflux`)
- (HttpClient) HTTP/SSE и Streamable-HTTP (активируется `spring-ai-starter-mcp-client`)
- (WebFlux) HTTP/SSE и Streamable-HTTP (активируется `spring-ai-starter-mcp-client-webflux`)

### Фильтрация инструментов

Средство загрузки клиента MCP поддерживает фильтрацию обнаруженных инструментов через интерфейс `McpToolFilter`. Это позволяет выборочно включать или исключать инструменты на основе пользовательских критериев, таких как информация о соединении MCP или свойства инструмента.

Чтобы реализовать инструментальную фильтрацию, создайте компонент, реализующий интерфейс `McpToolFilter`:

```java
@Component
public class CustomMcpToolFilter implements McpToolFilter {

    @Override
    public boolean test(McpConnectionInfo connectionInfo, McpSchema.Tool tool) {
        // Filter logic based on connection information and tool properties
        // Return true to include the tool, false to exclude it

        // Example: Exclude tools from a specific client
        if (connectionInfo.clientInfo().name().equals("restricted-client")) {
            return false;
        }

        // Example: Only include tools with specific names
        if (tool.name().startsWith("allowed_")) {
            return true;
        }

        // Example: Filter based on tool description or other properties
        if (tool.description() != null &&
            tool.description().contains("experimental")) {
            return false;
        }

        return true; // Include all other tools by default
    }
}
```

Запись `McpConnectionInfo` предоставляет доступ к:

- `clientCapabilities` - Возможности клиента MCP
- `clientInfo` - Информация о клиенте MCP (название и версия)
- `initializeResult` — Результат инициализации с сервера MCP

Фильтр автоматически обнаруживается и применяется как к синхронным, так и к асинхронным поставщикам обратного вызова инструмента MCP.
Если настраиваемый фильтр не указан, все обнаруженные инструменты включаются по умолчанию.

Примечание. В контексте приложения следует определить только один компонент `McpToolFilter`.
Если необходимо несколько фильтров, объедините их в одну реализацию составного фильтра.

### Генерация префикса имени инструмента

Средство запуска клиента MCP поддерживает создание настраиваемого префикса имени инструмента через интерфейс `McpToolNamePrefixGenerator`. Эта функция помогает избежать конфликтов имен при интеграции инструментов с нескольких серверов MCP за счет добавления уникальных префиксов к именам инструментов.

По умолчанию, если пользовательский bean-компонент `McpToolNamePrefixGenerator` не указан, стартер использует `DefaultMcpToolNamePrefixGenerator`, что обеспечивает уникальные имена инструментов во всех клиентских подключениях MCP. Генератор по умолчанию:

- Отслеживает все существующие соединения и названия инструментов, чтобы обеспечить уникальность.
- Форматирует имена инструментов, заменяя небуквенно-цифровые символы подчеркиванием (e.g., `my-tool` становится `my_tool`).
- При обнаружении повторяющихся названий инструментов в разных соединениях добавляется префикс счетчика (e.g., `alt_1_toolName`, `alt_2_toolName`).
- Является поточно-ориентированным и поддерживает идемпотентность — одна и та же комбинация (клиент, сервер, инструмент) всегда получает одно и то же уникальное имя.
- Обеспечивает, чтобы окончательное имя не превышало 64 символов (при необходимости усекается с начала).

Например:
- Первое появление инструмента `search` → `search`
- Второе появление инструмента `search` из другого соединения → `alt_1_search`.
- Инструмент со специальными символами `my-special-tool` → `my_special_tool`

Вы можете настроить это поведение, предоставив собственную реализацию:

```java
@Component
public class CustomToolNamePrefixGenerator implements McpToolNamePrefixGenerator {

    @Override
    public String prefixedToolName(McpConnectionInfo connectionInfo, Tool tool) {
        // Custom logic to generate prefixed tool names

        // Example: Use server name and version as prefix
        String serverName = connectionInfo.initializeResult().serverInfo().name();
        String serverVersion = connectionInfo.initializeResult().serverInfo().version();
        return serverName + "_v" + serverVersion.replace(".", "_") + "_" + tool.name();
    }
}
```

Запись `McpConnectionInfo` предоставляет исчерпывающую информацию о соединении MCP:

- `clientCapabilities` - Возможности клиента MCP
- `clientInfo` — Информация о клиенте MCP (имя, название и версия)
- `initializeResult` — результат инициализации с сервера MCP, включая информацию о сервере.

#### Встроенные генераторы префиксов

Фреймворк предоставляет несколько встроенных генераторов префиксов:

- `DefaultMcpToolNamePrefixGenerator` — обеспечивает уникальность имен инструментов, отслеживая дубликаты и добавляя при необходимости префиксы счетчиков (используется по умолчанию, если не указан пользовательский компонент).
- `McpToolNamePrefixGenerator.noPrefix()` — возвращает имена инструментов без префикса (может вызвать конфликты, если несколько серверов предоставляют инструменты с одинаковым именем)

Чтобы полностью отключить префиксы и использовать необработанные имена инструментов (не рекомендуется при использовании нескольких серверов MCP), зарегистрируйте генератор без префиксов как компонент:

```java
@Configuration
public class McpConfiguration {

    @Bean
    public McpToolNamePrefixGenerator mcpToolNamePrefixGenerator() {
        return McpToolNamePrefixGenerator.noPrefix();
    }
}
```

Генератор префиксов автоматически обнаруживается и применяется как к синхронным, так и к асинхронным поставщикам обратного вызова инструмента MCP через механизм Spring `ObjectProvider`.
Если специальный компонент-генератор не указан, `DefaultMcpToolNamePrefixGenerator` используется автоматически.

> **Внимание:** При использовании `McpToolNamePrefixGenerator.noPrefix()` с несколькими серверами MCP повторяющиеся имена инструментов приведут к ошибке `IllegalStateException`. Значение по умолчанию `DefaultMcpToolNamePrefixGenerator` предотвращает это, автоматически добавляя уникальные префиксы для дублирования имен инструментов.

### Контекст инструмента в метаконвертер MCP

Стартер загрузки клиента MCP поддерживает настраиваемое преобразование [ToolContext](api/tools.md#_tool_context) Spring AI в метаданные вызова инструмента MCP через интерфейс `ToolContextToMcpMetaConverter`.
Эта функция позволяет передавать дополнительную контекстную информацию (e.g. идентификатор пользователя, секретный токен) в качестве метаданных вместе с аргументами вызова, сгенерированными LLM.

Например, вы можете передать MCP `progressToken` вашему [Ход выполнения MCP](https://modelcontextprotocol.io/specification/2025-06-18/basic/utilities/progress#progress-flow) в контексте инструмента, чтобы отслеживать ход длительных операций:

```java
ChatModel chatModel = ...

String response = ChatClient.create(chatModel)
        .prompt("Tell me more about the customer with ID 42")
        .toolContext(Map.of("progressToken", "my-progress-token"))
        .call()
        .content();
```

По умолчанию, если пользовательский компонент-конвертер не предоставлен, стартер использует `ToolContextToMcpMetaConverter.defaultConverter()`, который:

- Отфильтровывает ключ обмена MCP (`McpToolUtils.TOOL_CONTEXT_MCP_EXCHANGE_KEY`)
- Отфильтровывает записи с нулевыми значениями
- Проходит через все остальные записи контекста как метаданные.




Вы можете настроить это поведение, предоставив собственную реализацию:

```java
@Component
public class CustomToolContextToMcpMetaConverter implements ToolContextToMcpMetaConverter {

    @Override
    public Map<String, Object> convert(ToolContext toolContext) {
        if (toolContext == null || toolContext.getContext() == null) {
            return Map.of();
        }

        // Custom logic to convert tool context to MCP metadata
        Map<String, Object> metadata = new HashMap<>();

        // Example: Add custom prefix to all keys
        for (Map.Entry<String, Object> entry : toolContext.getContext().entrySet()) {
            if (entry.getValue() != null) {
                metadata.put("app_" + entry.getKey(), entry.getValue());
            }
        }

        // Example: Add additional metadata
        metadata.put("timestamp", System.currentTimeMillis());
        metadata.put("source", "spring-ai");

        return metadata;
    }
}
```

#### Встроенные конвертеры

Фреймворк предоставляет встроенные конвертеры:

- `ToolContextToMcpMetaConverter.defaultConverter()` — отфильтровывает ключ обмена MCP и нулевые значения (используется по умолчанию, если не указан пользовательский компонент)
- `ToolContextToMcpMetaConverter.noOp()` — возвращает пустую карту, фактически отключая преобразование контекста в метаданные.

Чтобы полностью отключить преобразование контекста в метаданные:

```java
@Configuration
public class McpConfiguration {

    @Bean
    public ToolContextToMcpMetaConverter toolContextToMcpMetaConverter() {
        return ToolContextToMcpMetaConverter.noOp();
    }
}
```

Конвертер автоматически обнаруживается и применяется как к синхронным, так и к асинхронным обратным вызовам инструмента MCP с помощью механизма Spring `ObjectProvider`.
Если пользовательский компонент-конвертер не указан, автоматически используется конвертер по умолчанию.

### Отключите автоматическую настройку MCP ToolCallback

Автоматическая настройка MCP ToolCallback включена по умолчанию, но ее можно отключить с помощью свойства `spring.ai.mcp.client.toolcallback.enabled=false`.

Если этот параметр отключен, компонент `ToolCallbackProvider` не создается из доступных инструментов MCP.

## Аннотации клиента MCP

Средство запуска клиента MCP автоматически обнаруживает и регистрирует аннотированные методы для обработки различных операций клиента MCP:

- **@McpLogging** — обрабатывает уведомления о сообщениях журнала с серверов MCP.
- **@McpSampling** – обрабатывает запросы выборки от серверов MCP для завершения LLM.
- **@McpElicitation** – обрабатывает запросы на получение дополнительной информации от пользователей.
- **@McpProgress** — обрабатывает уведомления о ходе длительных операций.
- **@McpToolListChanged** — обрабатывает уведомления при изменении списка инструментов сервера.
- **@McpResourceListChanged** – обрабатывает уведомления при изменении списка ресурсов сервера.
- **@McpPromptListChanged** — обрабатывает уведомления при изменении списка подсказок сервера.

Пример использования:

```java
@Component
public class McpClientHandlers {

    @McpLogging(clients = "server1")
    public void handleLoggingMessage(LoggingMessageNotification notification) {
        System.out.println("Received log: " + notification.level() +
                          " - " + notification.data());
    }

    @McpSampling(clients = "server1")
    public CreateMessageResult handleSamplingRequest(CreateMessageRequest request) {
        // Process the request and generate a response
        String response = generateLLMResponse(request);

        return CreateMessageResult.builder()
            .role(Role.ASSISTANT)
            .content(new TextContent(response))
            .model("gpt-4")
            .build();
    }

    @McpProgress(clients = "server1")
    public void handleProgressNotification(ProgressNotification notification) {
        double percentage = notification.progress() * 100;
        System.out.println(String.format("Progress: %.2f%% - %s",
            percentage, notification.message()));
    }

    @McpToolListChanged(clients = "server1")
    public void handleToolListChanged(List<McpSchema.Tool> updatedTools) {
        System.out.println("Tool list updated: " + updatedTools.size() + " tools available");
        // Update local tool registry
        toolRegistry.updateTools(updatedTools);
    }
}
```

Аннотации поддерживают как синхронную, так и асинхронную реализацию, и их можно настроить для конкретных клиентов с помощью параметра `clients`:

```java
@McpLogging(clients = "server1")
public void handleServer1Logs(LoggingMessageNotification notification) {
    // Handle logs from specific server
    logToFile("server1.log", notification);
}

@McpSampling(clients = "server1")
public Mono<CreateMessageResult> handleAsyncSampling(CreateMessageRequest request) {
    return Mono.fromCallable(() -> {
        String response = generateLLMResponse(request);
        return CreateMessageResult.builder()
            .role(Role.ASSISTANT)
            .content(new TextContent(response))
            .model("gpt-4")
            .build();
    }).subscribeOn(Schedulers.boundedElastic());
}
```

Подробную информацию обо всех доступных аннотациях и шаблонах их использования см. в документации [Аннотации клиента MCP](api/mcp/mcp-annotations-client.md).

## Пример использования

Добавьте в свой проект соответствующую начальную зависимость и настройте клиент в `application.properties` или `application.yml`:

```yaml
spring:
  ai:
    mcp:
      client:
        enabled: true
        name: my-mcp-client
        version: 1.0.0
        request-timeout: 30s
        type: SYNC  # or ASYNC for reactive applications
        sse:
          connections:
            server1:
              url: http://localhost:8080
            server2:
              url: http://otherserver:8081
        streamable-http:
          connections:
            server3:
              url: http://localhost:8083
              endpoint: /mcp
        stdio:
          root-change-notification: false
          connections:
            server1:
              command: /path/to/server
              args:
                - --port=8080
                - --mode=production
              env:
                API_KEY: your-api-key
                DEBUG: "true"
```

Клиентские компоненты MCP будут автоматически настроены и доступны для внедрения:

```java
@Autowired
private List<McpSyncClient> mcpSyncClients;  // For sync client

// OR

@Autowired
private List<McpAsyncClient> mcpAsyncClients;  // For async client
```

Когда обратные вызовы инструментов включены (поведение по умолчанию), зарегистрированные инструменты MCP со всеми клиентами MCP предоставляются как экземпляр `ToolCallbackProvider`:

```java
@Autowired
private SyncMcpToolCallbackProvider toolCallbackProvider;
ToolCallback[] toolCallbacks = toolCallbackProvider.getToolCallbacks();
```

## Примеры приложений

- [Храбрый чат-бот веб-поиска](https://github.com/spring-projects/spring-ai-examples/tree/main/model-context-protocol/web-search/brave-chatbot) — чат-бот, который использует протокол контекста модели для взаимодействия с сервером веб-поиска.
- [Стартовый клиент MCP по умолчанию](https://github.com/spring-projects/spring-ai-examples/tree/main/model-context-protocol/client-starter/starter-default-client) — простой пример использования стартовой загрузки клиента `spring-ai-starter-mcp-client` MCP по умолчанию.
- [Стартовый клиент WebFlux MCP](https://github.com/spring-projects/spring-ai-examples/tree/main/model-context-protocol/client-starter/starter-webflux-client) — простой пример использования средства запуска клиента `spring-ai-starter-mcp-client-webflux` MCP.

## Дополнительные ресурсы

- [Spring Документация по искусственному интеллекту](https://docs.spring.io/spring-ai/reference/)
- [Спецификация протокола контекста модели](https://modelcontextprotocol.github.io/specification/)
- [Автоконфигурация Spring Boot](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.developing-auto-configuration)
