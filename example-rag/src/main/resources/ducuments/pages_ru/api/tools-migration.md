# Миграция с API FunctionCallback на API ToolCallback

Этот гид поможет вам мигрировать с устаревшего API `FunctionCallback` на новый API `ToolCallback` в Spring AI. Для получения дополнительной информации о новых API, ознакомьтесь с документацией xref:api/tools.adoc[Tools Calling].

## Обзор изменений

Эти изменения являются частью более широкой инициативы по улучшению и расширению возможностей вызова инструментов в Spring AI. Среди прочего, новый API переходит от терминологии "функции" к терминологии "инструменты", чтобы лучше соответствовать отраслевым стандартам. Это включает в себя несколько изменений в API при сохранении обратной совместимости через устаревшие методы.

## Ключевые изменения

1. `FunctionCallback` → `ToolCallback`
2. `FunctionCallback.builder().function()` → `FunctionToolCallback.builder()`
3. `FunctionCallback.builder().method()` → `MethodToolCallback.builder()`
4. `FunctionCallingOptions` → `ToolCallingChatOptions`
5. `ChatClient.builder().defaultFunctions()` → `ChatClient.builder().defaultTools()`
6. `ChatClient.functions()` → `ChatClient.tools()`
7. `FunctionCallingOptions.builder().functions()` → `ToolCallingChatOptions.builder().toolNames()`
8. `FunctionCallingOptions.builder().functionCallbacks()` → `ToolCallingChatOptions.builder().toolCallbacks()`

## Примеры миграции

### 1. Базовый Callback функции

До:
```java
FunctionCallback.builder()
    .function("getCurrentWeather", new MockWeatherService())
    .description("Get the weather in location")
    .inputType(MockWeatherService.Request.class)
    .build()
```

После:
```java
FunctionToolCallback.builder("getCurrentWeather", new MockWeatherService())
    .description("Get the weather in location")
    .inputType(MockWeatherService.Request.class)
    .build()
```

### 2. Использование ChatClient

До:
```java
String response = ChatClient.create(chatModel)
    .prompt()
    .user("Какова погода в Сан-Франциско?")
    .functions(FunctionCallback.builder()
        .function("getCurrentWeather", new MockWeatherService())
        .description("Get the weather in location")
        .inputType(MockWeatherService.Request.class)
        .build())
    .call()
    .content();
```

После:
```java
String response = ChatClient.create(chatModel)
    .prompt()
    .user("Какова погода в Сан-Франциско?")
    .tools(FunctionToolCallback.builder("getCurrentWeather", new MockWeatherService())
        .description("Get the weather in location")
        .inputType(MockWeatherService.Request.class)
        .build())
    .call()
    .content();
```

### 3. Callback функции на основе методов

До:
```java
FunctionCallback.builder()
    .method("getWeatherInLocation", String.class, Unit.class)
    .description("Get the weather in location")
    .targetClass(TestFunctionClass.class)
    .build()
```

После:
```java
var toolMethod = ReflectionUtils.findMethod(TestFunctionClass.class, "getWeatherInLocation");

MethodToolCallback.builder()
    .toolDefinition(ToolDefinition.builder(toolMethod)
        .description("Get the weather in location")
        .build())
    .toolMethod(toolMethod)
    .build()
```

Или с декларативным подходом:
```java
class WeatherTools {

    @Tool(description = "Get the weather in location")
    public void getWeatherInLocation(String location, Unit unit) {
        // ...
    }

}
```

И вы можете использовать тот же API `ChatClient#tools()` для регистрации callback инструментов на основе методов:

```java
String response = ChatClient.create(chatModel)
    .prompt()
    .user("Какова погода в Сан-Франциско?")
    .tools(MethodToolCallback.builder()
        .toolDefinition(ToolDefinition.builder(toolMethod)
            .description("Get the weather in location")
            .build())
        .toolMethod(toolMethod)
        .build())
    .call()
    .content();
```

Или с декларативным подходом:

```java
String response = ChatClient.create(chatModel)
    .prompt()
    .user("Какова погода в Сан-Франциско?")
    .tools(new WeatherTools())
    .call()
    .content();
```### 4. Конфигурация параметров

Перед:
```java
FunctionCallingOptions.builder()
    .model(modelName)
    .function("weatherFunction")
    .build()
```

После:
```java
ToolCallingChatOptions.builder()
    .model(modelName)
    .toolNames("weatherFunction")
    .build()
```

### 5. Стандартные функции в сборщике ChatClient

Перед:
```java
ChatClient.builder(chatModel)
    .defaultFunctions(FunctionCallback.builder()
        .function("getCurrentWeather", new MockWeatherService())
        .description("Получить погоду в местоположении")
        .inputType(MockWeatherService.Request.class)
        .build())
    .build()
```

После:
```java
ChatClient.builder(chatModel)
    .defaultTools(FunctionToolCallback.builder("getCurrentWeather", new MockWeatherService())
        .description("Получить погоду в местоположении")
        .inputType(MockWeatherService.Request.class)
        .build())
    .build()
```

### 6. Конфигурация Spring Bean

Перед:
```java
@Bean
public FunctionCallback weatherFunctionInfo() {
    return FunctionCallback.builder()
        .function("WeatherInfo", new MockWeatherService())
        .description("Получить текущую погоду")
        .inputType(MockWeatherService.Request.class)
        .build();
}
```

После:
```java
@Bean
public ToolCallback weatherFunctionInfo() {
    return FunctionToolCallback.builder("WeatherInfo", new MockWeatherService())
        .description("Получить текущую погоду")
        .inputType(MockWeatherService.Request.class)
        .build();
}
```

## Ломающие изменения

1. Конфигурация `method()` в функциях обратного вызова была заменена на более явную конфигурацию метода инструмента с использованием `ToolDefinition` и `MethodToolCallback`.

2. При использовании обратных вызовов на основе методов теперь необходимо явно находить метод с помощью `ReflectionUtils` и предоставлять его сборщику. В качестве альтернативы вы можете использовать декларативный подход с аннотацией `@Tool`.

3. Для нестатических методов теперь необходимо предоставить как метод, так и целевой объект:
```java
MethodToolCallback.builder()
    .toolDefinition(ToolDefinition.builder(toolMethod)
        .description("Описание")
        .build())
    .toolMethod(toolMethod)
    .toolObject(targetObject)
    .build()
```

## Устаревшие методы

Следующие методы устарели и будут удалены в будущем релизе:

- `ChatClient.Builder.defaultFunctions(String...)`
- `ChatClient.Builder.defaultFunctions(FunctionCallback...)`
- `ChatClient.RequestSpec.functions()`

Используйте их аналоги `tools` вместо этого.

## Декларативная спецификация с @Tool

Теперь вы можете использовать аннотацию на уровне метода (`@Tool`) для регистрации инструментов в Spring AI:

```java
class Home {

    @Tool(description = "Включить или выключить свет в комнате.")
    void turnLight(String roomName, boolean on) {
        // ...
        logger.info("Включить свет в комнате: {} на: {}", roomName, on);
    }
}

String response = ChatClient.create(this.chatModel).prompt()
        .user("Включите свет в гостиной.")
        .tools(new Home())
        .call()
        .content();
```

## Дополнительные заметки

1. Новый API обеспечивает лучшую разделенность между определением инструмента и его реализацией.
2. Определения инструментов могут быть повторно использованы в различных реализациях.
3. Шаблон сборщика был упрощен для общих случаев использования.
4. Улучшена поддержка инструментов на основе методов с улучшенной обработкой ошибок.

## График

Устаревшие методы будут поддерживаться для обратной совместимости в текущей версии, но будут удалены в следующем релизе. Рекомендуется как можно скорее перейти на новый API.
