# MCP Utilities
:page-title: Spring AI MCP Utilities

Утилиты MCP предоставляют базовую поддержку для интеграции Протокола Контекста Модели с приложениями Spring AI. 
Эти утилиты обеспечивают бесшовную связь между системой инструментов Spring AI и серверами MCP, поддерживая как синхронные, так и асинхронные операции.
Они обычно используются для программной конфигурации и взаимодействия с клиентом и сервером MCP.
Для более упрощенной конфигурации рассмотрите возможность использования стартовых пакетов.

## ToolCallback Utility

### Tool Callback Adapter

Адаптирует инструменты MCP к интерфейсу инструментов Spring AI с поддержкой как синхронного, так и асинхронного выполнения.

[tabs]
======
Sync::
+
```java
McpSyncClient mcpClient = // получить клиент MCP
Tool mcpTool = // получить определение инструмента MCP
ToolCallback callback = new SyncMcpToolCallback(mcpClient, mcpTool);

// Используйте инструмент через интерфейсы Spring AI
ToolDefinition definition = callback.getToolDefinition();
String result = callback.call("{\"param\": \"value\"}");
```

Async::
+
```java
McpAsyncClient mcpClient = // получить клиент MCP
Tool mcpTool = // получить определение инструмента MCP
ToolCallback callback = new AsyncMcpToolCallback(mcpClient, mcpTool);

// Используйте инструмент через интерфейсы Spring AI
ToolDefinition definition = callback.getToolDefinition();
String result = callback.call("{\"param\": \"value\"}");
```
======

### Tool Callback Providers

Обнаруживает и предоставляет инструменты MCP от клиентов MCP.

[tabs]
======
Sync::
+
```java
McpSyncClient mcpClient = // получить клиент MCP
ToolCallbackProvider provider = new SyncMcpToolCallbackProvider(mcpClient);

// Получить все доступные инструменты
ToolCallback[] tools = provider.getToolCallbacks();
```
+
Для нескольких клиентов:
+
```java
List<McpSyncClient> clients = // получить список клиентов
List<ToolCallback> callbacks = SyncMcpToolCallbackProvider.syncToolCallbacks(clients);
```
+
Для динамического выбора подмножества клиентов 
+
```java
@Autowired
private List<McpSyncClient> mcpSyncClients;

public ToolCallbackProvider buildProvider(Set<String> allowedServerNames) {
    // Фильтрация по server.name().
    List<McpSyncClient> selected = mcpSyncClients.stream()
        .filter(c -> allowedServerNames.contains(c.getServerInfo().name()))
        .toList();

    return new SyncMcpToolCallbackProvider(selected);
}

```
Async::
+
```java
McpAsyncClient mcpClient = // получить клиент MCP
ToolCallbackProvider provider = new AsyncMcpToolCallbackProvider(mcpClient);

// Получить все доступные инструменты
ToolCallback[] tools = provider.getToolCallbacks();
```
+
Для нескольких клиентов:
+
```java
List<McpAsyncClient> clients = // получить список клиентов
Flux<ToolCallback> callbacks = AsyncMcpToolCallbackProvider.asyncToolCallbacks(clients);
```
======

## McpToolUtils

### ToolCallbacks to ToolSpecifications

Преобразование обратных вызовов инструментов Spring AI в спецификации инструментов MCP:

[tabs]
======
Sync::
+
```java
List<ToolCallback> toolCallbacks = // получить обратные вызовы инструментов
List<SyncToolSpecifications> syncToolSpecs = McpToolUtils.toSyncToolSpecifications(toolCallbacks);
```
+
затем вы можете использовать `McpServer.SyncSpecification` для регистрации спецификаций инструментов:
+
```java
McpServer.SyncSpecification syncSpec = ...
syncSpec.tools(syncToolSpecs);
```

Async::
+
```java
List<ToolCallback> toolCallbacks = // получить обратные вызовы инструментов
List<AsyncToolSpecification> asyncToolSpecifications = McpToolUtils.toAsyncToolSpecifications(toolCallbacks);
```
+
затем вы можете использовать `McpServer.AsyncSpecification` для регистрации спецификаций инструментов:
+
```java
McpServer.AsyncSpecification asyncSpec = ...
asyncSpec.tools(asyncToolSpecifications);
```
======

### MCP Clients to ToolCallbacksПолучение обратных вызовов инструментов от клиентов MCP

[tabs]
======
Синхронный::
+
```java
List<McpSyncClient> syncClients = // получить синхронные клиенты
List<ToolCallback> syncCallbacks = McpToolUtils.getToolCallbacksFromSyncClients(syncClients);
```

Асинхронный::
+
```java
List<McpAsyncClient> asyncClients = // получить асинхронные клиенты
List<ToolCallback> asyncCallbacks = McpToolUtils.getToolCallbacksFromAsyncClients(asyncClients);
```
======

## Поддержка нативных изображений

Класс `McpHints` предоставляет подсказки для нативных изображений GraalVM для классов схемы MCP. Этот класс автоматически регистрирует все необходимые подсказки для рефлексии для классов схемы MCP при создании нативных изображений.
