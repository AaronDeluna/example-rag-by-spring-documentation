```markdown
# Аннотации клиента MCP

Аннотации клиента MCP предоставляют декларативный способ реализации обработчиков клиентов MCP с использованием аннотаций Java. 
Эти аннотации упрощают обработку уведомлений сервера и операций на стороне клиента.

[ВАЖНО]
**Все аннотации клиента MCP ДОЛЖНЫ включать параметр `clients`** для ассоциации обработчика с конкретным подключением клиента MCP. Значение `clients` должно соответствовать имени подключения, настроенному в ваших свойствах приложения.

## Аннотации клиента

### @McpLogging

Аннотация `@McpLogging` обрабатывает уведомления о сообщениях журнала от серверов MCP.

#### Основное использование

```java
@Component
public class LoggingHandler {

    @McpLogging(clients = "my-mcp-server")
    public void handleLoggingMessage(LoggingMessageNotification notification) {
        System.out.println("Получено сообщение журнала: " + notification.level() + 
                          " - " + notification.data());
    }
}
```

#### С индивидуальными параметрами

```java
@McpLogging(clients = "my-mcp-server")
public void handleLoggingWithParams(LoggingLevel level, String logger, String data) {
    System.out.println(String.format("[%s] %s: %s", level, logger, data));
}
```

### @McpSampling

Аннотация `@McpSampling` обрабатывает запросы на выборку от серверов MCP для завершений LLM.

#### Синхронная реализация

```java
@Component
public class SamplingHandler {

    @McpSampling(clients = "llm-server")
    public CreateMessageResult handleSamplingRequest(CreateMessageRequest request) {
        // Обработка запроса и генерация ответа
        String response = generateLLMResponse(request);
        
        return CreateMessageResult.builder()
            .role(Role.ASSISTANT)
            .content(new TextContent(response))
            .model("gpt-4")
            .build();
    }
}
```

#### Асинхронная реализация

```java
@Component
public class AsyncSamplingHandler {

    @McpSampling(clients = "llm-server")
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
}
```

### @McpElicitation

Аннотация `@McpElicitation` обрабатывает запросы на уточнение для сбора дополнительной информации от пользователей.

#### Основное использование

```java
@Component
public class ElicitationHandler {

    @McpElicitation(clients = "interactive-server")
    public ElicitResult handleElicitationRequest(ElicitRequest request) {
        // Представьте запрос пользователю и соберите ввод
        Map<String, Object> userData = presentFormToUser(request.requestedSchema());
        
        if (userData != null) {
            return new ElicitResult(ElicitResult.Action.ACCEPT, userData);
        } else {
            return new ElicitResult(ElicitResult.Action.DECLINE, null);
        }
    }
}
```

#### С взаимодействием с пользователем
``````java
@McpToolListChanged(clients = "tool-server")
public Mono<Void> handleAsyncToolListChanged(List<McpSchema.Tool> updatedTools) {
    return Mono.fromRunnable(() -> {
        System.out.println("Список инструментов обновлен: " + updatedTools.size() + " доступных инструментов");
        
        // Обновить локальный реестр инструментов
        toolRegistry.updateTools(updatedTools);
        
        // Логировать новые инструменты
        for (McpSchema.Tool tool : updatedTools) {
            System.out.println("  - " + tool.name() + ": " + tool.description());
        }
    });
}
``````java
@McpToolListChanged(clients = "tool-server")
public Mono<Void> handleAsyncToolListChanged(List<McpSchema.Tool> updatedTools) {
    return Mono.fromRunnable(() -> {
        // Обработка обновления списка инструментов асинхронно
        processToolListUpdate(updatedTools);
        
        // Уведомление заинтересованных компонентов
        eventBus.publish(new ToolListUpdatedEvent(updatedTools));
    }).then();
}
```

#### Обновления инструментов, специфичных для клиента

```java
@McpToolListChanged(clients = "dynamic-server")
public void handleDynamicServerToolUpdate(List<McpSchema.Tool> updatedTools) {
    // Обработка инструментов с конкретного сервера, который часто меняет свои инструменты
    dynamicToolManager.updateServerTools("dynamic-server", updatedTools);
    
    // Переоценка доступности инструментов
    reevaluateToolCapabilities();
}
```

### @McpResourceListChanged

Аннотация `@McpResourceListChanged` обрабатывает уведомления, когда изменяется список ресурсов сервера.

#### Основное использование

```java
@Component
public class ResourceListChangedHandler {

    @McpResourceListChanged(clients = "resource-server")
    public void handleResourceListChanged(List<McpSchema.Resource> updatedResources) {
        System.out.println("Ресурсы обновлены: " + updatedResources.size());
        
        // Обновление кэша ресурсов
        resourceCache.clear();
        for (McpSchema.Resource resource : updatedResources) {
            resourceCache.register(resource);
        }
    }
}
```

#### С анализом ресурсов

```java
@McpResourceListChanged(clients = "resource-server")
public void analyzeResourceChanges(List<McpSchema.Resource> updatedResources) {
    // Анализ изменений
    Set<String> newUris = updatedResources.stream()
        .map(McpSchema.Resource::uri)
        .collect(Collectors.toSet());
    
    Set<String> removedUris = previousUris.stream()
        .filter(uri -> !newUris.contains(uri))
        .collect(Collectors.toSet());
    
    if (!removedUris.isEmpty()) {
        handleRemovedResources(removedUris);
    }
    
    // Обновление отслеживания
    previousUris = newUris;
}
```

### @McpPromptListChanged

Аннотация `@McpPromptListChanged` обрабатывает уведомления, когда изменяется список подсказок сервера.

#### Основное использование

```java
@Component
public class PromptListChangedHandler {

    @McpPromptListChanged(clients = "prompt-server")
    public void handlePromptListChanged(List<McpSchema.Prompt> updatedPrompts) {
        System.out.println("Подсказки обновлены: " + updatedPrompts.size());
        
        // Обновление каталога подсказок
        promptCatalog.updatePrompts(updatedPrompts);
        
        // Обновление интерфейса, если необходимо
        if (uiController != null) {
            uiController.refreshPromptList(updatedPrompts);
        }
    }
}
```

#### Асинхронная обработка

```java
@McpPromptListChanged(clients = "prompt-server")
public Mono<Void> handleAsyncPromptUpdate(List<McpSchema.Prompt> updatedPrompts) {
    return Flux.fromIterable(updatedPrompts)
        .flatMap(prompt -> validatePrompt(prompt))
        .collectList()
        .doOnNext(validPrompts -> {
            promptRepository.saveAll(validPrompts);
        })
        .then();
}
```

## Интеграция с Spring Boot
```С помощью автонастройки Spring Boot обработчики клиентов автоматически обнаруживаются и регистрируются:

```java
@SpringBootApplication
public class McpClientApplication {
    public static void main(String[] args) {
        SpringApplication.run(McpClientApplication.class, args);
    }
}

@Component
public class MyClientHandlers {

    @McpLogging(clients = "my-server")
    public void handleLogs(LoggingMessageNotification notification) {
        // Обработка логов
    }

    @McpSampling(clients = "my-server")
    public CreateMessageResult handleSampling(CreateMessageRequest request) {
        // Обработка выборки
    }

    @McpProgress(clients = "my-server")
    public void handleProgress(ProgressNotification notification) {
        // Обработка прогресса
    }
}
```

Автонастройка будет:

1. Сканировать бины с аннотациями MCP клиента
2. Создавать соответствующие спецификации
3. Регистрировать их с клиентом MCP
4. Поддерживать как синхронные, так и асинхронные реализации
5. Обрабатывать несколько клиентов с обработчиками, специфичными для клиентов

## Свойства конфигурации

Настройте сканер аннотаций клиентов и соединения клиентов:

```yaml
spring:
  ai:
    mcp:
      client:
        type: SYNC  # или ASYNC
        annotation-scanner:
          enabled: true
        # Настройте соединения клиентов - имена соединений становятся значениями клиентов
        sse:
          connections:
            my-server:  # Это становится клиентами
              url: http://localhost:8080
            tool-server:  # Другие клиенты
              url: http://localhost:8081
        stdio:
          connections:
            local-server:  # Это становится клиентами
              command: /path/to/mcp-server
              args:
                - --mode=production
```

[ВАЖНО]
Параметр `clients` в аннотациях должен соответствовать именам соединений, определенным в вашей конфигурации. В приведенном выше примере допустимые значения `clients` будут: `"my-server"`, `"tool-server"` и `"local-server"`.

## Использование с MCP клиентом

Аннотированные обработчики автоматически интегрируются с клиентом MCP:

```java
@Autowired
private List<McpSyncClient> mcpClients;

// Клиенты автоматически будут использовать ваши аннотированные обработчики на основе клиентов
// Регистрация вручную не требуется - обработчики сопоставляются с клиентами по имени
```

Для каждого соединения клиента MCP обработчики с совпадающими `clients` будут автоматически зарегистрированы и вызваны, когда происходят соответствующие события.

## Дополнительные ресурсы

- xref:api/mcp/mcp-annotations-overview.adoc[Обзор аннотаций MCP]
- xref:api/mcp/mcp-annotations-server.adoc[Аннотации сервера]
- xref:api/mcp/mcp-annotations-special-params.adoc[Специальные параметры]
- xref:api/mcp/mcp-client-boot-starter-docs.adoc[Документация по MCP Client Boot Starter]
