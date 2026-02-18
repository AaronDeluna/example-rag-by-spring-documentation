# MCP Client Boot Starter

Spring AI MCP (Model Context Protocol) Client Boot Starter предоставляет автонастройку для функциональности клиента MCP в приложениях Spring Boot. Он поддерживает как синхронные, так и асинхронные реализации клиентов с различными транспортными опциями.

MCP Client Boot Starter предоставляет:

- Управление несколькими экземплярами клиентов
- Автоматическую инициализацию клиентов (если включено)
- Поддержку нескольких именованных транспортов (STDIO, Http/SSE и Streamable HTTP)
- Интеграцию с инструментальной системой выполнения Spring AI
- Возможности фильтрации инструментов для выборочного включения/исключения инструментов
- Настраиваемое создание префикса имени инструмента для избежания конфликтов имен
- Правильное управление жизненным циклом с автоматической очисткой ресурсов при закрытии контекста приложения
- Настраиваемое создание клиентов через кастомизаторы

## Стартеры

### Стандартный MCP клиент

```xml
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-starter-mcp-client</artifactId>
</dependency>
```

Стандартный стартер одновременно подключается к одному или нескольким серверам MCP через `STDIO` (в процессе), `SSE`, `Streamable-HTTP` и `Stateless Streamable-HTTP` транспорты. Транспорты SSE и Streamable-Http используют реализацию транспорта на основе JDK HttpClient. Каждое подключение к серверу MCP создает новый экземпляр клиента MCP. Вы можете выбрать либо `SYNC`, либо `ASYNC` клиентов MCP (обратите внимание: нельзя смешивать синхронные и асинхронные клиенты). Для развертывания в производственной среде мы рекомендуем использовать соединение на основе WebFlux с SSE и StreamableHttp с `spring-ai-starter-mcp-client-webflux`.

### WebFlux клиент

Стартер WebFlux предоставляет аналогичную функциональность стандартного стартера, но использует реализацию транспорта на основе WebFlux с Streamable-Http, Stateless Streamable-Http и SSE.

```xml
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-starter-mcp-client-webflux</artifactId>
</dependency>
```

## Свойства конфигурации

### Общие свойства

Общие свойства начинаются с префикса `spring.ai.mcp.client`:

[cols="3,4,3"]
| Свойство | Описание | Значение по умолчанию |
| --- | --- | --- |
| `enabled` |  |  |
| Включить/выключить клиент MCP |  |  |
| `true` |  |  |
| `name` |  |  |
| Имя экземпляра клиента MCP |  |  |
| `spring-ai-mcp-client` |  |  |
| `version` |  |  |
| Версия экземпляра клиента MCP |  |  |
| `1.0.0` |  |  |
| `initialized` |  |  |
| Инициализировать клиентов при создании |  |  |
| `true` |  |  |
| `request-timeout` |  |  |
| Время ожидания для запросов клиента MCP |  |  |
| `20s` |  |  |
| `type` |  |  |
| Тип клиента (SYNC или ASYNC). Все клиенты должны быть либо синхронными, либо асинхронными; смешивание не поддерживается |  |  |
| `SYNC` |  |  |
| `root-change-notification` |  |  |
| Включить/выключить уведомления о изменении корня для всех клиентов |  |  |
| `true` |  |  |
| `toolcallback.enabled` |  |  |
| Включить/выключить интеграцию обратного вызова инструмента MCP с инструментальной системой выполнения Spring AI |  |  |
| `true` |  |  |

### Свойства аннотаций MCP

Аннотации клиента MCP предоставляют декларативный способ реализации обработчиков клиентов MCP с использованием аннотаций Java. Свойства аннотаций клиента mcp-annotations начинаются с префикса `spring.ai.mcp.client.annotation-scanner`:

[cols="3,4,3"]
| Свойство | Описание | Значение по умолчанию |
| --- | --- | --- |
| `enabled` |  |  |
| Включить/выключить автосканирование аннотаций клиента MCP |  |  |
| `true` |  |  |

### Свойства транспорта StdioProperties для стандартного I/O транспорта имеют префикс `spring.ai.mcp.client.stdio`:

[cols="3,4,3"]
| Свойство | Описание | Значение по умолчанию |
| --- | --- | --- |
| `servers-configuration` |  |  |
| Ресурс, содержащий конфигурацию MCP серверов в формате JSON |  |  |
| - |  |  |
| `connections` |  |  |
| Карта именованных конфигураций stdio соединений |  |  |
| - |  |  |
| `connections.[name].command` |  |  |
| Команда для выполнения на MCP сервере |  |  |
| - |  |  |
| `connections.[name].args` |  |  |
| Список аргументов команды |  |  |
| - |  |  |
| `connections.[name].env` |  |  |
| Карта переменных окружения для процесса сервера |  |  |
| - |  |  |

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

В качестве альтернативы, вы можете настроить stdio соединения, используя внешний JSON файл в формате [Claude Desktop](https://modelcontextprotocol.io/quickstart/user):

```yaml
spring:
  ai:
    mcp:
      client:
        stdio:
          servers-configuration: classpath:mcp-servers.json
```

Формат Claude Desktop выглядит следующим образом:

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

### Конфигурация STDIO для Windows

> **Важно:** В Windows команды, такие как `npx`, `npm` и `node`, реализованы как **пакетные файлы** (`.cmd`), а не как нативные исполняемые файлы. Java's `ProcessBuilder` не может выполнять пакетные файлы напрямую и требует обертку `cmd.exe /c`.

#### Почему Windows требует специальной обработки

Когда Java's `ProcessBuilder` (используемый внутренне `StdioClientTransport`) пытается запустить процесс в Windows, он может выполнять только:

- Нативные исполняемые файлы (`.exe`)
- Системные команды, доступные для `cmd.exe`

Пакетные файлы Windows, такие как `npx.cmd`, `npm.cmd` и даже `python.cmd` (из Microsoft Store), требуют оболочку `cmd.exe` для их выполнения.

#### Решение: обертка cmd.exe

Оборачивайте команды пакетных файлов с помощью `cmd.exe /c`:

**Конфигурация для Windows:**
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

**Конфигурация для Linux/macOS:**
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

#### Кросс-платформенная программная конфигурацияДля приложений, которые должны работать на разных платформах без отдельных файлов конфигурации, используйте определение ОС в вашем приложении Spring Boot:

```java
@Bean(destroyMethod = "close")
@ConditionalOnMissingBean(McpSyncClient.class)
public McpSyncClient mcpClient() {
    ServerParameters stdioParams;

    if (isWindows()) {
        // Windows: cmd.exe /c npx подход
        var winArgs = new ArrayList<>(Arrays.asList(
            "/c", "npx", "-y", "@modelcontextprotocol/server-filesystem", "target"));
        stdioParams = ServerParameters.builder("cmd.exe")
                .args(winArgs)
                .build();
    } else {
        // Linux/Mac: прямой подход npx
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

> **Примечание:** При использовании программной конфигурации с `@Bean` добавьте `@ConditionalOnMissingBean(McpSyncClient.class)`, чтобы избежать конфликтов с автонастройкой из JSON-файлов.

#### Учет путей

**Относительные пути** (рекомендуется для портативности):
```json
{
  "command": "cmd.exe",
  "args": ["/c", "npx", "-y", "@modelcontextprotocol/server-filesystem", "target"]
}
```

Сервер MCP разрешает относительные пути на основе рабочего каталога приложения.

**Абсолютные пути** (Windows требует обратные слэши или экранированные прямые слэши):
```json
{
  "command": "cmd.exe",
  "args": ["/c", "npx", "-y", "@modelcontextprotocol/server-filesystem", "C:\\Users\\username\\project\\target"]
}
```

#### Общие пакетные файлы Windows, требующие cmd.exe

- `npx.cmd`, `npm.cmd` - менеджеры пакетов Node
- `python.cmd` - Python (установка из Microsoft Store)
- `pip.cmd` - менеджер пакетов Python
- `mvn.cmd` - обертка Maven
- `gradle.cmd` - обертка Gradle
- Пользовательские скрипты `.cmd` или `.bat`

#### Пример реализации

Смотрите [Spring AI Examples - Filesystem](https://github.com/spring-projects/spring-ai-examples/tree/main/model-context-protocol/filesystem) для полной реализации кроссплатформенного клиента MCP, который автоматически определяет ОС и настраивает клиента соответствующим образом.

### Свойства транспорта Streamable-HTTP

Используется для подключения к Streamable-HTTP и Stateless Streamable-HTTP серверам MCP.

Свойства для транспорта Streamable-HTTP начинаются с префикса `spring.ai.mcp.client.streamable-http`:

[cols="3,4,3"]
| Свойство | Описание | Значение по умолчанию |
| --- | --- | --- |
| `connections` |  |  |
| Карта именованных конфигураций соединений Streamable-HTTP |  |  |
| - |  |  |
| `connections.[name].url` |  |  |
| Базовый URL-адрес для Streamable-Http связи с сервером MCP |  |  |
| - |  |  |
| `connections.[name].endpoint` |  |  |
| потоковый HTTP-эндпоинт (в качестве суффикса URL) для использования в соединении |  |  |
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

### Свойства транспорта SSEProperties для транспорта Server-Sent Events (SSE) начинаются с префикса `spring.ai.mcp.client.sse`:

[cols="3,4,3"]
| Свойство | Описание | Значение по умолчанию |
| --- | --- | --- |
| `connections` |  |  |
| Карта именованных конфигураций соединений SSE |  |  |
| - |  |  |
| `connections.[name].url` |  |  |
| Базовый URL-адрес для SSE-связи с сервером MCP |  |  |
| - |  |  |
| `connections.[name].sse-endpoint` |  |  |
| конечная точка sse (в качестве суффикса URL), используемая для соединения |  |  |
| `/sse` |  |  |

Примеры конфигураций:
```yaml
spring:
  ai:
    mcp:
      client:
        sse:
          connections:
            # Простая конфигурация с использованием конечной точки по умолчанию /sse
            server1:
              url: http://localhost:8080
            # Пользовательская конечная точка SSE
            server2:
              url: http://otherserver:8081
              sse-endpoint: /custom-sse
            # Сложный URL с путем и токеном (например, MCP Hub)
            mcp-hub:
              url: http://localhost:3000
              sse-endpoint: /mcp-hub/sse/cf9ec4527e3c4a2cbb149a85ea45ab01
            # Конечная точка SSE с параметрами запроса
            api-server:
              url: https://api.example.com
              sse-endpoint: /v1/mcp/events?token=abc123&format=json
```

#### Рекомендации по разделению URL

Когда у вас есть полный URL SSE, разделите его на базовый URL и путь конечной точки:

[cols="2,2"]
| Полный URL | Конфигурация |
| --- | --- |
| `\http://localhost:3000/mcp-hub/sse/token123` |  |
| `url: http://localhost:3000` + |  |
| `\https://api.service.com/v2/events?key=secret` |  |
| `url: https://api.service.com` + |  |
| `\http://localhost:8080/sse` |  |
| `url: http://localhost:8080` + |  |

#### Устранение неполадок соединений SSE

**Ошибки 404 Not Found:**

- Проверьте разделение URL: убедитесь, что базовый `url` содержит только схему, хост и порт
- Убедитесь, что `sse-endpoint` начинается с `/` и включает полный путь и параметры запроса
- Проверьте полный URL напрямую в браузере или с помощью curl, чтобы подтвердить его доступность

### Свойства Streamable Http Transport

Свойства для транспорта Streamable Http начинаются с префикса `spring.ai.mcp.client.streamable-http`:

[cols="3,4,3"]
| Свойство | Описание | Значение по умолчанию |
| --- | --- | --- |
| `connections` |  |  |
| Карта именованных конфигураций соединений Streamable Http |  |  |
| - |  |  |
| `connections.[name].url` |  |  |
| Базовый URL-адрес для Streamable-Http связи с сервером MCP |  |  |
| - |  |  |
| `connections.[name].endpoint` |  |  |
| конечная точка streamable-http (в качестве суффикса URL), используемая для соединения |  |  |
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

## Особенности

### Типы клиентов Sync/Async

Стартовый пакет поддерживает два типа клиентов:

- Синхронный - тип клиента по умолчанию (`spring.ai.mcp.client.type=SYNC`), подходит для традиционных паттернов запрос-ответ с блокирующими операциями

**ПРИМЕЧАНИЕ:** Синхронный клиент будет регистрировать только синхронные методы с аннотациями MCP. Асинхронные методы будут игнорироваться.

- Асинхронный - подходит для реактивных приложений с неблокирующими операциями, настраивается с помощью `spring.ai.mcp.client.type=ASYNC`

**ПРИМЕЧАНИЕ:** Асинхронный клиент будет регистрировать только асинхронные методы с аннотациями MCP. Синхронные методы будут игнорироваться.

### Настройка клиента

Автонастройка предоставляет обширные возможности настройки спецификаций клиента через интерфейсы обратных вызовов. Эти кастомизаторы позволяют настраивать различные аспекты поведения клиента MCP, от таймаутов запросов до обработки событий и обработки сообщений.

#### Типы настройкиДоступные параметры настройки:

- **Конфигурация запросов** - Установите пользовательские тайм-ауты для запросов
- [**Пользовательские обработчики выборки**](https://modelcontextprotocol.io/specification/2025-06-18/client/sampling) - стандартизированный способ для серверов запрашивать выборку LLM (`completions` или `generations`) от LLM через клиентов. Этот процесс позволяет клиентам контролировать доступ к модели, выбор и разрешения, позволяя серверам использовать возможности ИИ — без необходимости в API-ключах сервера.
- [**Доступ к файловой системе (Корни)**](https://modelcontextprotocol.io/specification/2025-06-18/client/roots) - стандартизированный способ для клиентов предоставлять файловые `roots` серверам. Корни определяют границы, в пределах которых серверы могут работать в файловой системе, позволяя им понимать, к каким директориям и файлам у них есть доступ. Серверы могут запрашивать список корней у поддерживающих клиентов и получать уведомления, когда этот список изменяется.
- [**Обработчики эликации**](https://modelcontextprotocol.io/specification/2025-06-18/client/elicitation) - стандартизированный способ для серверов запрашивать дополнительную информацию у пользователей через клиента во время взаимодействий.
- **Обработчики событий** - обработчик клиента, который уведомляется, когда происходит определенное событие на сервере:
  - Уведомления об изменении инструментов - когда список доступных инструментов сервера изменяется
  - Уведомления об изменении ресурсов - когда список доступных ресурсов сервера изменяется.
  - Уведомления об изменении подсказок - когда список доступных подсказок сервера изменяется.
  - [**Обработчики логирования**](https://modelcontextprotocol.io/specification/2025-06-18/server/utilities/logging) - стандартизированный способ для серверов отправлять структурированные сообщения логов клиентам.
  - [**Обработчики прогресса**](https://modelcontextprotocol.io/specification/2025-06-18/basic/utilities/progress) - стандартизированный способ для серверов отправлять структурированные сообщения о прогрессе клиентам.

Клиенты могут контролировать уровень детализации логирования, устанавливая минимальные уровни логов


#### Пример настройки клиентаВы можете реализовать либо `McpSyncClientCustomizer` для синхронных клиентов, либо `McpAsyncClientCustomizer` для асинхронных клиентов, в зависимости от потребностей вашего приложения.

[tabs]
======
Синхронный::
+
```java
@Component
public class CustomMcpSyncClientCustomizer implements McpSyncClientCustomizer {
    @Override
    public void customize(String serverConfigurationName, McpClient.SyncSpec spec) {

        // Настройка конфигурации таймаута запроса
        spec.requestTimeout(Duration.ofSeconds(30));

        // Устанавливает корневые URI, к которым может получить доступ этот клиент.
        spec.roots(roots);

        // Устанавливает пользовательский обработчик выборки для обработки запросов на создание сообщений.
        spec.sampling((CreateMessageRequest messageRequest) -> {
            // Обработка выборки
            CreateMessageResult result = ...
            return result;
        });

        // Устанавливает пользовательский обработчик элицитации для обработки запросов на элицитацию.
        spec.elicitation((ElicitRequest request) -> {
          // обработка элицитации
          return new ElicitResult(ElicitResult.Action.ACCEPT, Map.of("message", request.message()));
        });

        // Добавляет потребителя, который будет уведомлен, когда получены уведомления о прогрессе.
        spec.progressConsumer((ProgressNotification progress) -> {
         // Обработка уведомлений о прогрессе
        });

        // Добавляет потребителя, который будет уведомлен, когда изменяются доступные инструменты, например, когда инструменты
        // добавляются или удаляются.
        spec.toolsChangeConsumer((List<McpSchema.Tool> tools) -> {
            // Обработка изменения инструментов
        });

        // Добавляет потребителя, который будет уведомлен, когда изменяются доступные ресурсы, например, когда ресурсы
        // добавляются или удаляются.
        spec.resourcesChangeConsumer((List<McpSchema.Resource> resources) -> {
            // Обработка изменения ресурсов
        });

        // Добавляет потребителя, который будет уведомлен, когда изменяются доступные подсказки, например, когда подсказки
        // добавляются или удаляются.
        spec.promptsChangeConsumer((List<McpSchema.Prompt> prompts) -> {
            // Обработка изменения подсказок
        });

        // Добавляет потребителя, который будет уведомлен, когда получены сообщения журнала от сервера.
        spec.loggingConsumer((McpSchema.LoggingMessageNotification log) -> {
            // Обработка сообщений журнала
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
        // Настройка конфигурации асинхронного клиента
        spec.requestTimeout(Duration.ofSeconds(30));
    }
}
```
======
Параметр `serverConfigurationName` — это имя конфигурации сервера, к которой применяется кастомизатор и для которой создается MCP клиент.

Автонастройка MCP клиента автоматически обнаруживает и применяет любые кастомизаторы, найденные в контексте приложения.

### Поддержка транспорта

Автонастройка поддерживает несколько типов транспорта:

- Стандартный ввод/вывод (Stdio) (активируется с помощью `spring-ai-starter-mcp-client` и `spring-ai-starter-mcp-client-webflux`)
- (HttpClient) HTTP/SSE и Streamable-HTTP (активируется с помощью `spring-ai-starter-mcp-client`)
- (WebFlux) HTTP/SSE и Streamable-HTTP (активируется с помощью `spring-ai-starter-mcp-client-webflux`)

### Фильтрация инструментовMCP Client Boot Starter поддерживает фильтрацию обнаруженных инструментов через интерфейс `McpToolFilter`. Это позволяет вам избирательно включать или исключать инструменты на основе пользовательских критериев, таких как информация о подключении MCP или свойства инструмента.

Чтобы реализовать фильтрацию инструментов, создайте бин, который реализует интерфейс `McpToolFilter`:

```java
@Component
public class CustomMcpToolFilter implements McpToolFilter {

    @Override
    public boolean test(McpConnectionInfo connectionInfo, McpSchema.Tool tool) {
        // Логика фильтрации на основе информации о подключении и свойств инструмента
        // Верните true, чтобы включить инструмент, false, чтобы исключить его

        // Пример: Исключить инструменты из конкретного клиента
        if (connectionInfo.clientInfo().name().equals("restricted-client")) {
            return false;
        }

        // Пример: Включать только инструменты с определенными именами
        if (tool.name().startsWith("allowed_")) {
            return true;
        }

        // Пример: Фильтрация на основе описания инструмента или других свойств
        if (tool.description() != null &&
            tool.description().contains("experimental")) {
            return false;
        }

        return true; // Включить все остальные инструменты по умолчанию
    }
}
```

Запись `McpConnectionInfo` предоставляет доступ к:

- `clientCapabilities` - Возможности клиента MCP
- `clientInfo` - Информация о клиенте MCP (имя и версия)
- `initializeResult` - Результат инициализации от сервера MCP

Фильтр автоматически обнаруживается и применяется как к синхронным, так и к асинхронным провайдерам обратных вызовов инструментов MCP. Если пользовательский фильтр не предоставлен, все обнаруженные инструменты включаются по умолчанию.

Примечание: В контексте приложения должен быть определен только один бин `McpToolFilter`. Если требуется несколько фильтров, объедините их в одну реализацию составного фильтра.

### Генерация префикса имени инструмента```markdown
MCP Client Boot Starter поддерживает настраиваемую генерацию префиксов имен инструментов через интерфейс `McpToolNamePrefixGenerator`. Эта функция помогает избежать конфликтов имен при интеграции инструментов из нескольких серверов MCP, добавляя уникальные префиксы к именам инструментов.

По умолчанию, если не предоставлен пользовательский бин `McpToolNamePrefixGenerator`, стартер использует `DefaultMcpToolNamePrefixGenerator`, который обеспечивает уникальность имен инструментов для всех подключений клиента MCP. Стандартный генератор:

- Отслеживает все существующие подключения и имена инструментов для обеспечения уникальности
- Форматирует имена инструментов, заменяя неалфавитные символы на подчеркивания (например, `my-tool` становится `my_tool`)
- Когда обнаруживаются дублирующиеся имена инструментов в разных подключениях, добавляет префикс счетчика (например, `alt_1_toolName`, `alt_2_toolName`)
- Является потокобезопасным и поддерживает идемпотентность - одна и та же комбинация (клиент, сервер, инструмент) всегда получает одно и то же уникальное имя
- Обеспечивает, чтобы финальное имя не превышало 64 символов (усеченное с начала, если необходимо)

Например:
- Первое появление инструмента `search` → `search`
- Второе появление инструмента `search` из другого подключения → `alt_1_search`
- Инструмент со специальными символами `my-special-tool` → `my_special_tool`

Вы можете настроить это поведение, предоставив свою собственную реализацию:

```java
@Component
public class CustomToolNamePrefixGenerator implements McpToolNamePrefixGenerator {

    @Override
    public String prefixedToolName(McpConnectionInfo connectionInfo, Tool tool) {
        // Пользовательская логика для генерации имен инструментов с префиксом

        // Пример: Используйте имя сервера и версию в качестве префикса
        String serverName = connectionInfo.initializeResult().serverInfo().name();
        String serverVersion = connectionInfo.initializeResult().serverInfo().version();
        return serverName + "_v" + serverVersion.replace(".", "_") + "_" + tool.name();
    }
}
```

Запись `McpConnectionInfo` предоставляет полную информацию о подключении MCP:

- `clientCapabilities` - Возможности клиента MCP
- `clientInfo` - Информация о клиенте MCP (имя, заголовок и версия)
- `initializeResult` - Результат инициализации от сервера MCP, включая информацию о сервере

#### Встроенные генераторы префиксов

Фреймворк предоставляет несколько встроенных генераторов префиксов:

- `DefaultMcpToolNamePrefixGenerator` - Обеспечивает уникальность имен инструментов, отслеживая дубликаты и добавляя префиксы счетчика при необходимости (используется по умолчанию, если не предоставлен пользовательский бин)
- `McpToolNamePrefixGenerator.noPrefix()` - Возвращает имена инструментов без какого-либо префикса (может вызвать конфликты, если несколько серверов предоставляют инструменты с одинаковым именем)

Чтобы полностью отключить префиксирование и использовать сырые имена инструментов (не рекомендуется при использовании нескольких серверов MCP), зарегистрируйте генератор без префикса как бин:

```java
@Configuration
public class McpConfiguration {

    @Bean
    public McpToolNamePrefixGenerator mcpToolNamePrefixGenerator() {
        return McpToolNamePrefixGenerator.noPrefix();
    }
}
```

Генератор префиксов автоматически обнаруживается и применяется как к синхронным, так и к асинхронным провайдерам обратных вызовов инструментов MCP через механизм `ObjectProvider` Spring. Если не предоставлен пользовательский бин генератора, автоматически используется `DefaultMcpToolNamePrefixGenerator`.

> **Внимание:** При использовании `McpToolNamePrefixGenerator.noPrefix()` с несколькими серверами MCP дублирующиеся имена инструментов вызовут `IllegalStateException`. Стандартный `DefaultMcpToolNamePrefixGenerator` предотвращает это, автоматически добавляя уникальные префиксы к дублирующимся именам инструментов.

### Конвертер контекста инструмента в мета-данные MCP
``````markdown
MCP Client Boot Starter поддерживает настраиваемое преобразование xref:api/tools.adoc#_tool_context[ToolContext] от Spring AI в метаданные вызова инструмента MCP через интерфейс `ToolContextToMcpMetaConverter`. Эта функция позволяет передавать дополнительную контекстную информацию (например, идентификатор пользователя, токен секретов) в качестве метаданных вместе с аргументами вызова, сгенерированными LLM.

Например, вы можете передать MCP `progressToken` в ваш [MCP Progress Flow](https://modelcontextprotocol.io/specification/2025-06-18/basic/utilities/progress#progress-flow) в контексте инструмента, чтобы отслеживать прогресс длительных операций:

```java
ChatModel chatModel = ...

String response = ChatClient.create(chatModel)
        .prompt("Расскажите больше о клиенте с ID 42")
        .toolContext(Map.of("progressToken", "my-progress-token"))
        .call()
        .content();
```

По умолчанию, если не предоставлен пользовательский бин конвертера, стартер использует `ToolContextToMcpMetaConverter.defaultConverter()`, который:

- Фильтрует ключ обмена MCP (`McpToolUtils.TOOL_CONTEXT_MCP_EXCHANGE_KEY`)
- Фильтрует записи с нулевыми значениями
- Передает все остальные записи контекста в качестве метаданных

Вы можете настроить это поведение, предоставив свою собственную реализацию:

```java
@Component
public class CustomToolContextToMcpMetaConverter implements ToolContextToMcpMetaConverter {

    @Override
    public Map<String, Object> convert(ToolContext toolContext) {
        if (toolContext == null || toolContext.getContext() == null) {
            return Map.of();
        }

        // Пользовательская логика для преобразования контекста инструмента в метаданные MCP
        Map<String, Object> metadata = new HashMap<>();

        // Пример: Добавить пользовательский префикс ко всем ключам
        for (Map.Entry<String, Object> entry : toolContext.getContext().entrySet()) {
            if (entry.getValue() != null) {
                metadata.put("app_" + entry.getKey(), entry.getValue());
            }
        }

        // Пример: Добавить дополнительные метаданные
        metadata.put("timestamp", System.currentTimeMillis());
        metadata.put("source", "spring-ai");

        return metadata;
    }
}
```

#### Встроенные конвертеры

Фреймворк предоставляет встроенные конвертеры:

- `ToolContextToMcpMetaConverter.defaultConverter()` - Фильтрует ключ обмена MCP и нулевые значения (используется по умолчанию, если не предоставлен пользовательский бин)
- `ToolContextToMcpMetaConverter.noOp()` - Возвращает пустую карту, фактически отключая преобразование контекста в метаданные

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

Конвертер автоматически обнаруживается и применяется как к синхронным, так и к асинхронным обратным вызовам инструментов MCP через механизм `ObjectProvider` Spring. Если не предоставлен пользовательский бин конвертера, по умолчанию используется стандартный конвертер.

### Отключение автонастройки MCP ToolCallback

Автонастройка MCP ToolCallback включена по умолчанию, но может быть отключена с помощью свойства `spring.ai.mcp.client.toolcallback.enabled=false`.

При отключении никакой бин `ToolCallbackProvider` не создается из доступных инструментов MCP.

## Аннотации MCP Client
```The MCP Client Boot Starter автоматически обнаруживает и регистрирует аннотированные методы для обработки различных операций клиента MCP:

- **@McpLogging** - Обрабатывает уведомления о сообщениях журналирования от серверов MCP
- **@McpSampling** - Обрабатывает запросы на выборку от серверов MCP для завершений LLM
- **@McpElicitation** - Обрабатывает запросы на уточнение для сбора дополнительной информации от пользователей
- **@McpProgress** - Обрабатывает уведомления о прогрессе для длительных операций
- **@McpToolListChanged** - Обрабатывает уведомления, когда изменяется список инструментов сервера
- **@McpResourceListChanged** - Обрабатывает уведомления, когда изменяется список ресурсов сервера
- **@McpPromptListChanged** - Обрабатывает уведомления, когда изменяется список подсказок сервера

Пример использования:

```java
@Component
public class McpClientHandlers {

    @McpLogging(clients = "server1")
    public void handleLoggingMessage(LoggingMessageNotification notification) {
        System.out.println("Получено сообщение журнала: " + notification.level() +
                          " - " + notification.data());
    }

    @McpSampling(clients = "server1")
    public CreateMessageResult handleSamplingRequest(CreateMessageRequest request) {
        // Обработка запроса и генерация ответа
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
        System.out.println(String.format("Прогресс: %.2f%% - %s",
            percentage, notification.message()));
    }

    @McpToolListChanged(clients = "server1")
    public void handleToolListChanged(List<McpSchema.Tool> updatedTools) {
        System.out.println("Список инструментов обновлен: " + updatedTools.size() + " доступных инструментов");
        // Обновление локального реестра инструментов
        toolRegistry.updateTools(updatedTools);
    }
}
```

Аннотации поддерживают как синхронные, так и асинхронные реализации и могут быть настроены для конкретных клиентов с помощью параметра `clients`:

```java
@McpLogging(clients = "server1")
public void handleServer1Logs(LoggingMessageNotification notification) {
    // Обработка журналов от конкретного сервера
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

Для получения подробной информации обо всех доступных аннотациях и их шаблонах использования смотрите документацию xref:api/mcp/mcp-annotations-client.adoc[MCP Client Annotations].

## Пример использованияДобавьте соответствующую стартовую зависимость в ваш проект и настройте клиент в `application.properties` или `application.yml`:

```yaml
spring:
  ai:
    mcp:
      client:
        enabled: true
        name: my-mcp-client
        version: 1.0.0
        request-timeout: 30s
        type: SYNC  # или ASYNC для реактивных приложений
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

Бины клиента MCP будут автоматически настроены и доступны для внедрения:

```java
@Autowired
private List<McpSyncClient> mcpSyncClients;  // Для синхронного клиента

// ИЛИ

@Autowired
private List<McpAsyncClient> mcpAsyncClients;  // Для асинхронного клиента
```

Когда обратные вызовы инструмента включены (поведение по умолчанию), зарегистрированные инструменты MCP со всеми клиентами MCP предоставляются в виде экземпляра `ToolCallbackProvider`:

```java
@Autowired
private SyncMcpToolCallbackProvider toolCallbackProvider;
ToolCallback[] toolCallbacks = toolCallbackProvider.getToolCallbacks();
```

## Примеры приложений

- [Brave Web Search Chatbot](https://github.com/spring-projects/spring-ai-examples/tree/main/model-context-protocol/web-search/brave-chatbot) - Чат-бот, который использует Протокол Контекста Модели для взаимодействия с сервером веб-поиска.
- [Default MCP Client Starter](https://github.com/spring-projects/spring-ai-examples/tree/main/model-context-protocol/client-starter/starter-default-client) - Простой пример использования стартера MCP Client `spring-ai-starter-mcp-client`.
- [WebFlux MCP Client Starter](https://github.com/spring-projects/spring-ai-examples/tree/main/model-context-protocol/client-starter/starter-webflux-client) - Простой пример использования стартера MCP Client `spring-ai-starter-mcp-client-webflux`.

## Дополнительные ресурсы

- [Документация Spring AI](https://docs.spring.io/spring-ai/reference/)
- [Спецификация Протокола Контекста Модели](https://modelcontextprotocol.github.io/specification/)
- [Автонастройка Spring Boot](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.developing-auto-configuration)
