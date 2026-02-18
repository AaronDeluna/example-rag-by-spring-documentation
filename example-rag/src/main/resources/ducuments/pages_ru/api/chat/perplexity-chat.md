# Perplexity Chat

https://perplexity.ai/[Perplexity AI] предоставляет уникальную AI-службу, которая интегрирует свои языковые модели с возможностями поиска в реальном времени. Она предлагает различные модели и поддерживает потоковые ответы для разговорного AI.

Spring AI интегрируется с Perplexity AI, повторно используя существующий xref::api/chat/openai-chat.adoc[OpenAI] клиент. Чтобы начать, вам нужно получить https://docs.perplexity.ai/guides/getting-started[Perplexity API Key], настроить базовый URL и выбрать одну из поддерживаемых https://docs.perplexity.ai/guides/model-cards[моделей].

![w=800,align="center"](spring-ai-perplexity-integration.jpg)

> **Примечание:** API Perplexity не полностью совместим с API OpenAI. Perplexity сочетает результаты веб-поиска в реальном времени с ответами своих языковых моделей. В отличие от OpenAI, Perplexity не предоставляет механизмы `toolCalls` - `function call`. Кроме того, в настоящее время Perplexity не поддерживает мультимодальные сообщения.

Проверьте тесты https://github.com/spring-projects/spring-ai/blob/main/models/spring-ai-openai/src/test/java/org/springframework/ai/openai/chat/proxy/PerplexityWithOpenAiChatModelIT.java[PerplexityWithOpenAiChatModelIT.java] для примеров использования Perplexity с Spring AI.

## Предварительные требования

- **Создайте API Key**:
Посетите https://docs.perplexity.ai/guides/getting-started[здесь], чтобы создать API Key. Настройте его, используя свойство `spring.ai.openai.api-key` в вашем проекте Spring AI.

- **Установите базовый URL Perplexity**:
Установите свойство `spring.ai.openai.base-url` на `+https://api.perplexity.ai+`.

- **Выберите модель Perplexity**:
Используйте свойство `spring.ai.openai.chat.model=<имя модели>`, чтобы указать модель. Обратитесь к https://docs.perplexity.ai/guides/model-cards[Поддерживаемые модели] для доступных вариантов.

- **Установите путь для завершения чата**:
Установите `spring.ai.openai.chat.completions-path` на `/chat/completions`. Обратитесь к https://docs.perplexity.ai/api-reference/chat-completions[API завершения чата] для получения дополнительной информации.

Вы можете установить эти параметры конфигурации в вашем файле `application.properties`:

```properties
spring.ai.openai.api-key=<ваш-perplexity-api-key>
spring.ai.openai.base-url=https://api.perplexity.ai
spring.ai.openai.chat.model=llama-3.1-sonar-small-128k-online
spring.ai.openai.chat.completions-path=/chat/completions
```

Для повышения безопасности при работе с конфиденциальной информацией, такой как API ключи, вы можете использовать язык выражений Spring (SpEL) для ссылки на пользовательские переменные окружения:

```yaml
# В application.yml
spring:
  ai:
    openai:
      api-key: ${PERPLEXITY_API_KEY}
      base-url: ${PERPLEXITY_BASE_URL}
      chat:
        model: ${PERPLEXITY_MODEL}
        completions-path: ${PERPLEXITY_COMPLETIONS_PATH}
```

```bash
# В вашем окружении или .env файле
export PERPLEXITY_API_KEY=<ваш-perplexity-api-key>
export PERPLEXITY_BASE_URL=https://api.perplexity.ai
export PERPLEXITY_MODEL=llama-3.1-sonar-small-128k-online
export PERPLEXITY_COMPLETIONS_PATH=/chat/completions
```

Вы также можете установить эти конфигурации программно в вашем коде приложения:

```java
// Получите конфигурацию из безопасных источников или переменных окружения
String apiKey = System.getenv("PERPLEXITY_API_KEY");
String baseUrl = System.getenv("PERPLEXITY_BASE_URL");
String model = System.getenv("PERPLEXITY_MODEL");
String completionsPath = System.getenv("PERPLEXITY_COMPLETIONS_PATH");
```

### Добавьте репозитории и BOM

Артефакты Spring AI публикуются в репозиториях Maven Central и Spring Snapshot. Обратитесь к разделу xref:getting-started.adoc#artifact-repositories[Артефакты репозитории], чтобы добавить эти репозитории в вашу систему сборки.

Чтобы помочь с управлением зависимостями, Spring AI предоставляет BOM (bill of materials), чтобы гарантировать, что одна и та же версия Spring AI используется на протяжении всего проекта. Обратитесь к разделу xref:getting-started.adoc#dependency-management[Управление зависимостями], чтобы добавить BOM Spring AI в вашу систему сборки.

## Автоконфигурация

[NOTE]
====
В автоконфигурации Spring AI произошли значительные изменения в названиях артефактов модулей стартеров. Пожалуйста, обратитесь к https://docs.spring.io/spring-ai/reference/upgrade-notes.html[заметкам об обновлении] для получения дополнительной информации.
====

Spring AI предоставляет автоконфигурацию Spring Boot для клиента OpenAI Chat. Чтобы включить ее, добавьте следующую зависимость в файл сборки Maven `pom.xml` или Gradle `build.gradle` вашего проекта:

[tabs]
======
Maven::
+
```xml
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-starter-model-openai</artifactId>
</dependency>
```

Gradle::
+
```groovy
dependencies {
    implementation 'org.springframework.ai:spring-ai-starter-model-openai'
}
```
======

> **Совет:** Обратитесь к разделу xref:getting-started.adoc#dependency-management[Управление зависимостями], чтобы добавить BOM Spring AI в ваш файл сборки.

### Свойства чата

#### Свойства повторной попытки

Префикс `spring.ai.retry` используется как префикс свойства, который позволяет вам настроить механизм повторной попытки для модели чата OpenAI.

[cols="3,5,1", stripes=even]
| Свойство | Описание | По умолчанию

| spring.ai.retry.max-attempts   | Максимальное количество попыток повторной попытки. |  10
| spring.ai.retry.backoff.initial-interval | Начальная продолжительность ожидания для политики экспоненциального отката. |  2 сек.
| spring.ai.retry.backoff.multiplier | Множитель интервала отката. |  5
| spring.ai.retry.backoff.max-interval | Максимальная продолжительность отката. |  3 мин.
| spring.ai.retry.on-client-errors | Если false, выбросить NonTransientAiException и не пытаться повторить для кодов ошибок клиента `4xx` | false
| spring.ai.retry.exclude-on-http-codes | Список кодов состояния HTTP, которые не должны вызывать повторную попытку (например, для выброса NonTransientAiException). | пусто
| spring.ai.retry.on-http-codes | Список кодов состояния HTTP, которые должны вызывать повторную попытку (например, для выброса TransientAiException). | пусто

#### Свойства подключения

Префикс `spring.ai.openai` используется как префикс свойства, который позволяет вам подключаться к OpenAI.

[cols="3,5,1", stripes=even]
| Свойство | Описание | По умолчанию

| spring.ai.openai.base-url   | URL для подключения. Должен быть установлен на `+https://api.perplexity.ai+` | -
| spring.ai.openai.chat.api-key    | Ваш Perplexity API Key | -

#### Свойства конфигурации

[NOTE]
====
Включение и отключение автоконфигураций чата теперь настраивается через свойства верхнего уровня с префиксом `spring.ai.model.chat`.

Чтобы включить, spring.ai.model.chat=openai (по умолчанию включено)

Чтобы отключить, spring.ai.model.chat=none (или любое значение, которое не совпадает с openai)

Это изменение сделано для того, чтобы позволить конфигурацию нескольких моделей.
====

Префикс `spring.ai.openai.chat` - это префикс свойства, который позволяет вам настроить реализацию модели чата для OpenAI.
[cols="3,5,1", stripes=even]
| Свойство | Описание | По умолчанию

| spring.ai.model.chat | Включить модель чата OpenAI.  | openai
| spring.ai.openai.chat.model      | Одна из поддерживаемых https://docs.perplexity.ai/guides/model-cards[моделей Perplexity]. Пример: `llama-3.1-sonar-small-128k-online`. | -
| spring.ai.openai.chat.base-url   | Необязательный переопределяет spring.ai.openai.base-url для предоставления специфического для чата URL. Должен быть установлен на `+https://api.perplexity.ai+` |  -
| spring.ai.openai.chat.completions-path | Должен быть установлен на `/chat/completions` | `/v1/chat/completions`
| spring.ai.openai.chat.options.temperature | Количество случайности в ответе, значение от 0 включительно до 2 исключительно. Более высокие значения более случайные, а более низкие значения более детерминированные. Обязательный диапазон: `0 < x < 2`. | 0.2
| spring.ai.openai.chat.options.frequencyPenalty | Множитель штрафа, больший 0. Значения больше 1.0 штрафуют новые токены на основе их существующей частоты в тексте до сих пор, уменьшая вероятность модели повторить ту же строку дословно. Значение 1.0 означает отсутствие штрафа. Несовместимо с presence_penalty. Обязательный диапазон: `x > 0`. | 1
| spring.ai.openai.chat.options.maxTokens | Максимальное количество токенов завершения, возвращаемых API. Общее количество токенов, запрашиваемых в max_tokens, плюс количество токенов запроса, отправленных в сообщениях, не должно превышать лимит токенов окна контекста запрашиваемой модели. Если не указано, модель будет генерировать токены, пока не достигнет своего токена остановки или конца своего окна контекста. | -
| spring.ai.openai.chat.options.presencePenalty | Значение от -2.0 до 2.0. Положительные значения штрафуют новые токены на основе того, появляются ли они в тексте до сих пор, увеличивая вероятность модели говорить о новых темах. Несовместимо с `frequency_penalty`. Обязательный диапазон: `-2 < x < 2` | 0
| spring.ai.openai.chat.options.topP | Порог выборки ядра, значение от 0 до 1 включительно. Для каждого последующего токена модель учитывает результаты токенов с вероятностью top_p. Мы рекомендуем изменять либо top_k, либо top_p, но не оба. Обязательный диапазон: `0 < x < 1` | 0.9
| spring.ai.openai.chat.options.stream-usage | (Только для потоковой передачи) Установите, чтобы добавить дополнительный фрагмент с статистикой использования токенов для всего запроса. Поле `choices` для этого фрагмента является пустым массивом, и все другие фрагменты также будут включать поле использования, но со значением null. | false

> **Совет:** Все свойства с префиксом `spring.ai.openai.chat.options` могут быть переопределены во время выполнения, добавив специфичные для запроса <<chat-options>> в вызов `Prompt`.

## Опции времени выполнения [[chat-options]]

Файл https://github.com/spring-projects/spring-ai/blob/main/models/spring-ai-openai/src/main/java/org/springframework/ai/openai/OpenAiChatOptions.java[OpenAiChatOptions.java] предоставляет конфигурации модели, такие как модель для использования, температура, штраф за частоту и т. д.

При запуске параметры по умолчанию могут быть настроены с помощью конструктора `OpenAiChatModel(api, options)` или свойств `spring.ai.openai.chat.options.*`.

Во время выполнения вы можете переопределить параметры по умолчанию, добавив новые, специфичные для запроса, параметры в вызов `Prompt`. Например, чтобы переопределить модель и температуру по умолчанию для конкретного запроса:

```java
ChatResponse response = chatModel.call(
    new Prompt(
        "Сгенерируйте имена 5 известных пиратов.",
        OpenAiChatOptions.builder()
            .model("llama-3.1-sonar-large-128k-online")
            .temperature(0.4)
        .build()
    ));
```

> **Совет:** В дополнение к специфичным для модели https://github.com/spring-projects/spring-ai/blob/main/models/spring-ai-openai/src/main/java/org/springframework/ai/openai/OpenAiChatOptions.java[OpenAiChatOptions] вы можете использовать переносимый [ChatOptions](https://github.com/spring-projects/spring-ai/blob/main/spring-ai-model/src/main/java/org/springframework/ai/chat/prompt/ChatOptions.java) экземпляр, созданный с помощью [ChatOptions#builder()](https://github.com/spring-projects/spring-ai/blob/main/spring-ai-model/src/main/java/org/springframework/ai/chat/prompt/DefaultChatOptionsBuilder.java).

## Вызов функций

> **Примечание:** Perplexity не поддерживает явный вызов функций. Вместо этого он интегрирует результаты поиска непосредственно в ответы.

## Мультимодальные возможности

> **Примечание:** В настоящее время API Perplexity не поддерживает медиа-контент.

## Пример контроллера

https://start.spring.io/[Создайте] новый проект Spring Boot и добавьте `spring-ai-starter-model-openai` в зависимости вашего pom (или gradle).

Добавьте файл `application.properties` в директорию `src/main/resources`, чтобы включить и настроить модель чата OpenAi:

```application.properties
spring.ai.openai.api-key=<PERPLEXITY_API_KEY>
spring.ai.openai.base-url=https://api.perplexity.ai
spring.ai.openai.chat.completions-path=/chat/completions
spring.ai.openai.chat.options.model=llama-3.1-sonar-small-128k-online
spring.ai.openai.chat.options.temperature=0.7

# API Perplexity не поддерживает встраивания, поэтому мы должны отключить его.
spring.ai.openai.embedding.enabled=false
```

> **Совет:** замените `api-key` на ваш Perplexity Api ключ.

Это создаст реализацию `OpenAiChatModel`, которую вы можете внедрить в ваш класс. Вот пример простого класса `@Controller`, который использует модель чата для генерации текста.

```java
@RestController
public class ChatController {

    private final OpenAiChatModel chatModel;

    @Autowired
    public ChatController(OpenAiChatModel chatModel) {
        this.chatModel = chatModel;
    }

    @GetMapping("/ai/generate")
    public Map generate(@RequestParam(value = "message", defaultValue = "Расскажи мне шутку") String message) {
        return Map.of("generation", this.chatModel.call(message));
    }

    @GetMapping("/ai/generateStream")
	public Flux<ChatResponse> generateStream(@RequestParam(value = "message", defaultValue = "Расскажи мне шутку") String message) {
        Prompt prompt = new Prompt(new UserMessage(message));
        return this.chatModel.stream(prompt);
    }
}
```

## Поддерживаемые модели

Perplexity поддерживает несколько моделей, оптимизированных для разговорного AI с улучшенным поиском. Обратитесь к https://docs.perplexity.ai/guides/model-cards[Поддерживаемые модели] для получения подробной информации.

## Ссылки

- https://docs.perplexity.ai/home[Домашняя документация]
- https://docs.perplexity.ai/api-reference/chat-completions[Справочник API]
- https://docs.perplexity.ai/guides/getting-started[Начало работы]
- https://docs.perplexity.ai/guides/rate-limits[Ограничения по ставкам]
