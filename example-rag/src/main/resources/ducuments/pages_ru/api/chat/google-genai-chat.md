# Google GenAI Chat

[Google GenAI API](https://ai.google.dev/gemini-api/docs) позволяет разработчикам создавать приложения с генеративным ИИ, используя модели Gemini от Google через Gemini Developer API или Vertex AI. Google GenAI API поддерживает многомодальные запросы в качестве входных данных и выводит текст или код. Многомодальная модель способна обрабатывать информацию из нескольких модальностей, включая изображения, видео и текст. Например, вы можете отправить модели фотографию тарелки с печеньем и попросить ее предоставить вам рецепт этих печений.

Gemini — это семейство моделей генеративного ИИ, разработанных Google DeepMind, предназначенное для многомодальных случаев использования. Gemini API предоставляет доступ к [Gemini 2.0 Flash](https://ai.google.dev/gemini-api/docs/models#gemini-2.0-flash), [Gemini 2.0 Flash-Lite](https://ai.google.dev/gemini-api/docs/models#gemini-2.0-flash-lite), всем моделям [Gemini Pro](https://ai.google.dev/gemini-api/docs/models), включая самую последнюю [Gemini 3 Pro](https://ai.google.dev/gemini-api/docs/models#gemini-3-pro).

Эта реализация предоставляет два режима аутентификации:

- **Gemini Developer API**: Используйте API-ключ для быстрого прототипирования и разработки
- **Vertex AI**: Используйте учетные данные Google Cloud для развертываний в производственной среде с корпоративными функциями

[Gemini API Reference](https://ai.google.dev/api)

## Предварительные требования

Выберите один из следующих методов аутентификации:

### Вариант 1: Gemini Developer API (API-ключ)

- Получите API-ключ в [Google AI Studio](https://aistudio.google.com/app/apikey)
- Установите API-ключ в качестве переменной окружения или в свойствах вашего приложения

### Вариант 2: Vertex AI (Google Cloud)

- Установите [gcloud](https://cloud.google.com/sdk/docs/install) CLI, подходящий для вашей ОС.
- Аутентифицируйтесь, выполнив следующую команду. Замените `PROJECT_ID` на идентификатор вашего проекта Google Cloud и `ACCOUNT` на ваше имя пользователя Google Cloud.

[source]
```
gcloud config set project <PROJECT_ID> &&
gcloud auth application-default login <ACCOUNT>
```

## Автонастройка

[ПРИМЕЧАНИЕ]
====
В Spring AI произошли значительные изменения в автонастройке, названиях артефактов стартовых модулей.
Пожалуйста, обратитесь к [заметкам об обновлении](https://docs.spring.io/spring-ai/reference/upgrade-notes.html) для получения дополнительной информации.
====

Spring AI предоставляет автонастройку Spring Boot для клиента Google GenAI Chat. Чтобы включить ее, добавьте следующую зависимость в файлы сборки Maven `pom.xml` или Gradle `build.gradle` вашего проекта:

[tabs]
======
Maven::
+
```xml
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-starter-model-google-genai</artifactId>
</dependency>
```

Gradle::
+
```groovy
dependencies {
    implementation 'org.springframework.ai:spring-ai-starter-model-google-genai'
}
```
======

> **Совет:** Обратитесь к разделу xref:getting-started.adoc#dependency-management[Управление зависимостями], чтобы добавить BOM Spring AI в ваш файл сборки.

### Свойства чата

[ПРИМЕЧАНИЕ]
====
Включение и отключение автонастроек чата теперь настраивается через свойства верхнего уровня с префиксом `spring.ai.model.chat`.

Чтобы включить, используйте spring.ai.model.chat=google-genai (по умолчанию включено)

Чтобы отключить, используйте spring.ai.model.chat=none (или любое значение, которое не соответствует google-genai)

Это изменение сделано для того, чтобы позволить конфигурацию нескольких моделей.
====

#### Свойства подключения

Префикс `spring.ai.google.genai` используется в качестве префикса свойств, который позволяет вам подключаться к Google GenAI.

[cols="3,5,1", stripes=even]
|====
| Свойство | Описание | По умолчанию

| spring.ai.model.chat   | Включить клиент модели чата |  google-genai
| spring.ai.google.genai.api-key   | API-ключ для Gemini Developer API. Если он предоставлен, клиент использует Gemini Developer API вместо Vertex AI. |  -
| spring.ai.google.genai.project-id   | Идентификатор проекта Google Cloud Platform (обязателен для режима Vertex AI) |  -
| spring.ai.google.genai.location    | Регион Google Cloud (обязателен для режима Vertex AI) |  -
| spring.ai.google.genai.credentials-uri    | URI для учетных данных Google Cloud. Если он предоставлен, он используется для создания экземпляра `GoogleCredentials` для аутентификации. |  -
|====

#### Свойства модели чата

Префикс `spring.ai.google.genai.chat` — это префикс свойств, который позволяет вам настраивать реализацию модели чата для Google GenAI Chat.

[cols="3,5,1", stripes=even]
|====
| Свойство | Описание | По умолчанию

| spring.ai.google.genai.chat.options.model | Поддерживаемые https://ai.google.dev/gemini-api/docs/models[модели Google GenAI Chat], которые можно использовать, включают `gemini-2.0-flash`, `gemini-2.0-flash-lite`, `gemini-pro` и `gemini-1.5-flash`. | gemini-2.0-flash
| spring.ai.google.genai.chat.options.response-mime-type | MIME-тип выходного ответа сгенерированного текстового кандидата. |  `text/plain`: (по умолчанию) Текстовый вывод или `application/json`: JSON-ответ.
| spring.ai.google.genai.chat.options.google-search-retrieval | Использовать функцию Grounding Google Search | `true` или `false`, по умолчанию `false`.
| spring.ai.google.genai.chat.options.temperature | Управляет случайностью вывода. Значения могут варьироваться от [0.0,1.0], включая. Значение, ближе к 1.0, будет производить более разнообразные ответы, в то время как значение, ближе к 0.0, обычно приведет к менее неожиданным ответам от генеративной модели. | -
| spring.ai.google.genai.chat.options.top-k | Максимальное количество токенов, которые следует учитывать при выборке. Генеративная модель использует комбинированную выборку Top-k и nucleus. Выборка Top-k учитывает набор из topK наиболее вероятных токенов. | -
| spring.ai.google.genai.chat.options.top-p | Максимальная кумулятивная вероятность токенов, которые следует учитывать при выборке. Генеративная модель использует комбинированную выборку Top-k и nucleus. Выборка nucleus учитывает наименьший набор токенов, сумма вероятностей которых составляет не менее topP.  | -
| spring.ai.google.genai.chat.options.candidate-count | Количество сгенерированных ответных сообщений для возврата. Это значение должно быть в диапазоне [1, 8], включая. По умолчанию 1. | 1
| spring.ai.google.genai.chat.options.max-output-tokens | Максимальное количество токенов для генерации. | -
| spring.ai.google.genai.chat.options.frequency-penalty | Штрафы за частоту для уменьшения повторений. | -
| spring.ai.google.genai.chat.options.presence-penalty | Штрафы за присутствие для уменьшения повторений. | -
| spring.ai.google.genai.chat.options.thinking-budget | Бюджет на размышления для процесса размышления. См. <<thinking-config>>. | -
| spring.ai.google.genai.chat.options.thinking-level | Уровень токенов размышлений, которые модель должна генерировать. Допустимые значения: `LOW`, `HIGH`, `THINKING_LEVEL_UNSPECIFIED`. См. <<thinking-config>>. | -
| spring.ai.google.genai.chat.options.include-thoughts | Включить подписи мыслей для вызова функций. **Обязательно** для Gemini 3 Pro, чтобы избежать ошибок валидации во время внутреннего цикла выполнения инструмента. См. <<thought-signatures>>. | false
| spring.ai.google.genai.chat.options.tool-names | Список инструментов, идентифицированных по их именам, которые следует включить для вызова функций в одном запросе. Инструменты с этими именами должны существовать в реестре ToolCallback. | -
| spring.ai.google.genai.chat.options.tool-callbacks | Обратные вызовы инструментов для регистрации с ChatModel. | -
| spring.ai.google.genai.chat.options.internal-tool-execution-enabled | Если true, выполнение инструмента должно быть выполнено, в противном случае ответ от модели возвращается пользователю. По умолчанию null, но если он null, будет учитываться `ToolCallingChatOptions.DEFAULT_TOOL_EXECUTION_ENABLED`, который равен true | -
| spring.ai.google.genai.chat.options.safety-settings | Список настроек безопасности для управления фильтрами безопасности, как определено в https://ai.google.dev/gemini-api/docs/safety-settings[Настройки безопасности Google GenAI]. Каждая настройка безопасности может иметь метод, порог и категорию. | -
| spring.ai.google.genai.chat.options.cached-content-name | Имя кэшированного контента, который следует использовать для этого запроса. Когда установлено вместе с `use-cached-content=true`, кэшированный контент будет использоваться в качестве контекста. См. <<cached-content>>. | -
| spring.ai.google.genai.chat.options.use-cached-content | Использовать ли кэшированный контент, если он доступен. Когда true и `cached-content-name` установлен, система будет использовать кэшированный контент. | false
| spring.ai.google.genai.chat.options.auto-cache-threshold | Автоматически кэшировать запросы, которые превышают этот порог токенов. Когда установлено, запросы, превышающие это значение, будут автоматически кэшироваться для повторного использования. Установите в null, чтобы отключить авто-кэширование. | -
| spring.ai.google.genai.chat.options.auto-cache-ttl | Время жизни (Duration) для автоматически кэшированного контента в формате ISO-8601 (например, `PT1H` для 1 часа). Используется, когда авто-кэширование включено. | PT1H
| spring.ai.google.genai.chat.enable-cached-content | Включить бин `GoogleGenAiCachedContentService` для управления кэшированным контентом. | true

|====

> **Совет:** Все свойства с префиксом `spring.ai.google.genai.chat.options` могут быть переопределены во время выполнения, добавив специфичные для запроса <<chat-options>> к вызову `Prompt`.

## Опции времени выполнения [[chat-options]]

[GoogleGenAiChatOptions.java](https://github.com/spring-projects/spring-ai/blob/main/models/spring-ai-google-genai/src/main/java/org/springframework/ai/google/genai/GoogleGenAiChatOptions.java) предоставляет конфигурации модели, такие как температура, topK и т. д.

При запуске параметры по умолчанию могут быть настроены с помощью конструктора `GoogleGenAiChatModel(client, options)` или свойств `spring.ai.google.genai.chat.options.*`.

Во время выполнения вы можете переопределить параметры по умолчанию, добавив новые, специфичные для запроса, параметры к вызову `Prompt`. Например, чтобы переопределить температуру по умолчанию для конкретного запроса:

```java
ChatResponse response = chatModel.call(
    new Prompt(
        "Сгенерируйте имена 5 известных пиратов.",
        GoogleGenAiChatOptions.builder()
            .temperature(0.4)
        .build()
    ));
```

> **Совет:** В дополнение к специфичным для модели `GoogleGenAiChatOptions` вы можете использовать переносимый экземпляр [ChatOptions](https://github.com/spring-projects/spring-ai/blob/main/spring-ai-model/src/main/java/org/springframework/ai/chat/prompt/ChatOptions.java), созданный с помощью [ChatOptions#builder()](https://github.com/spring-projects/spring-ai/blob/main/spring-ai-model/src/main/java/org/springframework/ai/chat/prompt/DefaultChatOptionsBuilder.java).

## Вызов инструментов

Модель Google GenAI поддерживает возможности вызова инструментов (вызова функций), позволяя моделям использовать инструменты во время разговоров. Вот пример того, как определить и использовать инструменты на основе `@Tool`:

```java

public class WeatherService {

    @Tool(description = "Получить погоду в местоположении")
    public String weatherByLocation(@ToolParam(description= "Название города или штата") String location) {
        ...
    }
}

String response = ChatClient.create(this.chatModel)
        .prompt("Какова погода в Бостоне?")
        .tools(new WeatherService())
        .call()
        .content();
```

Вы также можете использовать бины java.util.function в качестве инструментов:

```java
@Bean
@Description("Получить погоду в местоположении. Вернуть температуру в формате 36°F или 36°C.")
public Function<Request, Response> weatherFunction() {
    return new MockWeatherService();
}

String response = ChatClient.create(this.chatModel)
        .prompt("Какова погода в Бостоне?")
        .toolNames("weatherFunction")
        .inputType(Request.class)
        .call()
        .content();
```

Дополнительную информацию смотрите в документации xref:api/tools.adoc[Инструменты].

## Конфигурация размышлений [[thinking-config]]

Модели Gemini поддерживают возможность "размышления", которая позволяет модели выполнять более глубокое рассуждение перед генерацией ответов. Это контролируется через `ThinkingConfig`, который включает три связанных параметра: `thinkingBudget`, `thinkingLevel` и `includeThoughts`.

### Уровень размышлений

Параметр `thinkingLevel` управляет глубиной токенов размышлений, которые генерирует модель. Это доступно для моделей, которые поддерживают размышления (например, Gemini 3 Pro Preview).

[cols="1,3", stripes=even]
|====
| Значение | Описание

| `LOW` | Минимальные размышления. Используйте для простых запросов, где скорость предпочтительнее глубокого анализа.
| `HIGH` | Обширные размышления. Используйте для сложных задач, требующих глубокого анализа и пошагового рассуждения.
| `THINKING_LEVEL_UNSPECIFIED` | Модель использует свое поведение по умолчанию.
|====

#### Конфигурация через свойства

```application.properties
spring.ai.google.genai.chat.options.model=gemini-3-pro-preview
spring.ai.google.genai.chat.options.thinking-level=HIGH
```

#### Программная конфигурация

```java
import org.springframework.ai.google.genai.common.GoogleGenAiThinkingLevel;

ChatResponse response = chatModel.call(
    new Prompt(
        "Объясните теорию относительности простыми словами.",
        GoogleGenAiChatOptions.builder()
            .model("gemini-3-pro-preview")
            .thinkingLevel(GoogleGenAiThinkingLevel.HIGH)
            .build()
    ));
```

### Бюджет размышлений

Параметр `thinkingBudget` устанавливает бюджет токенов для процесса размышлений:

- **Положительное значение**: Максимальное количество токенов для размышлений (например, `8192`)
- **Ноль (`0`)**: Полностью отключает размышления
- **Не установлен**: Модель решает автоматически в зависимости от сложности запроса

```java
ChatResponse response = chatModel.call(
    new Prompt(
        "Решите эту сложную математическую задачу пошагово.",
        GoogleGenAiChatOptions.builder()
            .model("gemini-2.5-pro")
            .thinkingBudget(8192)
            .build()
    ));
```

### Совместимость параметров

[ВАЖНО]
====
**`thinkingLevel` и `thinkingBudget` являются взаимно исключающими.** Вы не можете использовать оба в одном запросе - это приведет к ошибке API.

- Используйте `thinkingLevel` (`LOW`, `HIGH`) для **моделей Gemini 3 Pro**
- Используйте `thinkingBudget` (количество токенов) для **моделей серии Gemini 2.5**
====

Вы можете комбинировать `includeThoughts` с `thinkingLevel` или `thinkingBudget` (но не с обоими):

```java
// Для Gemini 3 Pro: используйте thinkingLevel + includeThoughts
ChatResponse response = chatModel.call(
    new Prompt(
        "Проанализируйте этот сложный сценарий.",
        GoogleGenAiChatOptions.builder()
            .model("gemini-3-pro-preview")
            .thinkingLevel(GoogleGenAiThinkingLevel.HIGH)
            .includeThoughts(true)
            .build()
    ));

// Для Gemini 2.5: используйте thinkingBudget + includeThoughts
ChatResponse response = chatModel.call(
    new Prompt(
        "Проанализируйте этот сложный сценарий.",
        GoogleGenAiChatOptions.builder()
            .model("gemini-2.5-pro")
            .thinkingBudget(8192)
            .includeThoughts(true)
            .build()
    ));
```

### Поддержка моделей

Опции конфигурации размышлений специфичны для модели:

[cols="2,1,1,2", stripes=even]
|====
| Модель | thinkingLevel | thinkingBudget | Примечания

| Gemini 3 Pro (Preview)
| ✅ Поддерживается
| ⚠️ Обратная совместимость только
| Используйте `thinkingLevel`. Невозможно отключить размышления. Требует **глобальной** конечной точки.

| Gemini 2.5 Pro
| ❌ Не поддерживается
| ✅ Поддерживается
| Используйте `thinkingBudget`. Установите в 0, чтобы отключить, -1 для динамического.

| Gemini 2.5 Flash
| ❌ Не поддерживается
| ✅ Поддерживается
| Используйте `thinkingBudget`. Установите в 0, чтобы отключить, -1 для динамического.

| Gemini 2.5 Flash-Lite
| ❌ Не поддерживается
| ✅ Поддерживается
| Размышления отключены по умолчанию. Установите `thinkingBudget`, чтобы включить.

| Gemini 2.0 Flash
| ❌ Не поддерживается
| ❌ Не поддерживается
| Размышления недоступны.
|====

[ВАЖНО]
====
- Использование `thinkingLevel` с неподдерживаемыми моделями (например, Gemini 2.5 или ранее) приведет к ошибке API.
- Gemini 3 Pro Preview доступен только на **глобальных** конечных точках. Установите `spring.ai.google.genai.location=global` или `GOOGLE_CLOUD_LOCATION=global`.
- Проверьте [документацию Google GenAI Thinking](https://ai.google.dev/gemini-api/docs/thinking) для получения актуальных возможностей модели.
====

> **Примечание:** Включение функций размышлений увеличивает использование токенов и затраты на API. Используйте соответственно в зависимости от сложности ваших запросов.

## Подписи мыслей [[thought-signatures]]

Gemini 3 Pro вводит подписи мыслей, которые представляют собой непрозрачные массивы байтов, сохраняющие контекст размышлений модели во время вызова функций. Когда `includeThoughts` включен, модель возвращает подписи мыслей, которые должны быть переданы обратно в **тот же ход** во время внутреннего цикла выполнения инструмента.

### Когда важны подписи мыслей

**ВАЖНО**: Валидация подписей мыслей применяется только к **текущему ходу** - в частности, во время внутреннего цикла выполнения инструмента, когда модель делает вызовы функций (как параллельные, так и последовательные). API **не** проверяет подписи мыслей для предыдущих ходов в истории разговора.

Согласно [документации Google](https://ai.google.dev/gemini-api/docs/thought-signatures):

- Валидация применяется только к вызовам функций в текущем ходе
- Подписи предыдущих ходов не нужно сохранять
- Отсутствие подписей в вызовах функций текущего хода приводит к ошибкам HTTP 400 для Gemini 3 Pro
- Для параллельных вызовов функций только первая часть `functionCall` содержит подпись

Для моделей Gemini 2.5 Pro и более ранних подписки мыслей являются необязательными, и API допускает гибкость.

### Конфигурация

Включите подписи мыслей, используя свойства конфигурации:

```application.properties
spring.ai.google.genai.chat.options.model=gemini-3-pro-preview
spring.ai.google.genai.chat.options.include-thoughts=true
```

Или программно во время выполнения:

```java
ChatResponse response = chatModel.call(
    new Prompt(
        "Ваш вопрос здесь",
        GoogleGenAiChatOptions.builder()
            .model("gemini-3-pro-preview")
            .includeThoughts(true)
            .toolCallbacks(callbacks)
            .build()
    ));
```

### Автоматическая обработка

Spring AI автоматически обрабатывает подписи мыслей во время внутреннего цикла выполнения инструмента. Когда `internalToolExecutionEnabled` равно true (по умолчанию), Spring AI:

1. **Извлекает** подписи мыслей из ответов модели
2. **Прикрепляет** их к правильным частям `functionCall`, когда отправляет обратно ответы функций
3. **Пропагирует** их правильно во время вызовов функций в пределах одного хода (как параллельных, так и последовательных)

Вам не нужно вручную управлять подписями мыслей - Spring AI гарантирует, что они правильно прикреплены к частям `functionCall`, как требуется спецификацией API.

### Пример с вызовом функций

```java
@Bean
@Description("Получить погоду в местоположении")
public Function<WeatherRequest, WeatherResponse> weatherFunction() {
    return new WeatherService();
}

// Включите includeThoughts для Gemini 3 Pro с вызовом функций
String response = ChatClient.create(this.chatModel)
        .prompt("Какова погода в Бостоне?")
        .options(GoogleGenAiChatOptions.builder()
            .model("gemini-3-pro-preview")
            .includeThoughts(true)
            .build())
        .toolNames("weatherFunction")
        .call()
        .content();
```

### Ручной режим выполнения инструмента

Если вы установите `internalToolExecutionEnabled=false`, чтобы вручную контролировать цикл выполнения инструмента, вам необходимо самостоятельно обрабатывать подписи мыслей при использовании Gemini 3 Pro с `includeThoughts=true`.

**Требования для ручного выполнения инструмента с подписями мыслей:**

1. Извлеките подписи мыслей из метаданных ответа:
+
```java
AssistantMessage assistantMessage = response.getResult().getOutput();
Map<String, Object> metadata = assistantMessage.getMetadata();
List<byte[]> thoughtSignatures = (List<byte[]>) metadata.get("thoughtSignatures");
```

2. При отправке обратно ответов функций включите оригинальное `AssistantMessage` с его метаданными в вашей истории сообщений. Spring AI автоматически прикрепит подписи мыслей к правильным частям `functionCall`.

3. Для Gemini 3 Pro, если не сохранить подписи мыслей в текущем ходе, это приведет к ошибкам HTTP 400 от API.

> **Важно:** Только вызовы функций текущего хода требуют подписей мыслей. При начале нового хода разговора (после завершения раунда вызова функций) вам не нужно сохранять подписи предыдущего хода.

> **Примечание:** Включение `includeThoughts` увеличивает использование токенов, так как процессы размышлений включены в ответы. Это влияет на затраты на API, но обеспечивает лучшую прозрачность размышлений.

## Многомодальность

Многомодальность относится к способности модели одновременно понимать и обрабатывать информацию из различных (входных) источников, включая `text`, `pdf`, `images`, `audio` и другие форматы данных.

### Изображение, аудио, видео
Модели Google Gemini AI поддерживают эту возможность, понимая и интегрируя текст, код, аудио, изображения и видео. Для получения дополнительной информации смотрите блог https://blog.google/technology/ai/google-gemini-ai/#introducing-gemini[Представляем Gemini].

Интерфейс `Message` Spring AI поддерживает многомодальные модели ИИ, вводя тип Media. Этот тип содержит данные и информацию о медиа-вложениях в сообщениях, используя `org.springframework.util.MimeType` от Spring и `java.lang.Object` для необработанных медиа-данных.

Ниже приведен простой пример кода, извлеченный из [GoogleGenAiChatModelIT.java](https://github.com/spring-projects/spring-ai/blob/main/models/spring-ai-google-genai/src/test/java/org/springframework/ai/google/genai/GoogleGenAiChatModelIT.java), демонстрирующий комбинацию текста пользователя с изображением.

```java
byte[] data = new ClassPathResource("/vertex-test.png").getContentAsByteArray();

var userMessage = UserMessage.builder()
			.text("Объясните, что вы видите на этой картинке?")
			.media(List.of(new Media(MimeTypeUtils.IMAGE_PNG, data)))
			.build();

ChatResponse response = chatModel.call(new Prompt(List.of(this.userMessage)));
```

### PDF

Google GenAI предоставляет поддержку для типов входных данных PDF. Используйте медиа-тип `application/pdf`, чтобы прикрепить PDF-файл к сообщению:

```java
var pdfData = new ClassPathResource("/spring-ai-reference-overview.pdf");

var userMessage = UserMessage.builder()
			.text("Вы очень профессиональный специалист по суммированию документов. Пожалуйста, подведите итоги данного документа.")
			.media(List.of(new Media(new MimeType("application", "pdf"), pdfData)))
			.build();

var response = this.chatModel.call(new Prompt(List.of(userMessage)));
```

## Кэшированный контент [[cached-content]]

[Контекстное кэширование](https://ai.google.dev/gemini-api/docs/caching) Google GenAI позволяет вам кэшировать большие объемы контента (таких как длинные документы, репозитории кода или медиа) и повторно использовать его в нескольких запросах. Это значительно снижает затраты на API и улучшает задержку ответа для повторяющихся запросов на один и тот же контент.

### Преимущества

- **Снижение затрат**: Кэшированные токены тарифицируются по гораздо более низкой ставке, чем обычные входные токены (обычно на 75-90% дешевле)
- **Улучшенная производительность**: Повторное использование кэшированного контента снижает время обработки для больших контекстов
- **Согласованность**: Один и тот же кэшированный контекст обеспечивает согласованные ответы на несколько запросов

### Требования к кэшу

- Минимальный размер кэша: 32,768 токенов (примерно 25,000 слов)
- Максимальная продолжительность кэша: 1 час по умолчанию (настраиваемая через TTL)
- Кэшированный контент должен включать либо системные инструкции, либо историю разговора

### Использование службы кэшированного контента

Spring AI предоставляет `GoogleGenAiCachedContentService` для программного управления кэшем. Служба автоматически настраивается при использовании автонастройки Spring Boot.

#### Создание кэшированного контента

```java
@Autowired
private GoogleGenAiCachedContentService cachedContentService;

// Создайте кэшированный контент с большим документом
String largeDocument = "... ваш большой контекст здесь (>32k токенов) ...";

CachedContentRequest request = CachedContentRequest.builder()
    .model("gemini-2.0-flash")
    .contents(List.of(
        Content.builder()
            .role("user")
            .parts(List.of(Part.fromText(largeDocument)))
            .build()
    ))
    .displayName("Мой большой документ кэша")
    .ttl(Duration.ofHours(1))
    .build();

GoogleGenAiCachedContent cachedContent = cachedContentService.create(request);
String cacheName = cachedContent.getName(); // Сохраните это для повторного использования
```

#### Использование кэшированного контента в запросах чата

После того как вы создали кэшированный контент, укажите его в ваших запросах чата:

```java
ChatResponse response = chatModel.call(
    new Prompt(
        "Подведите итоги ключевых моментов из документа",
        GoogleGenAiChatOptions.builder()
            .useCachedContent(true)
            .cachedContentName(cacheName) // Используйте имя кэшированного контента
            .build()
    ));
```

Или через свойства конфигурации:

```application.properties
spring.ai.google.genai.chat.options.use-cached-content=true
spring.ai.google.genai.chat.options.cached-content-name=cachedContent/your-cache-name
```

#### Управление кэшированным контентом

`GoogleGenAiCachedContentService` предоставляет комплексное управление кэшем:

```java
// Извлечь кэшированный контент
GoogleGenAiCachedContent content = cachedContentService.get(cacheName);

// Обновить TTL кэша
CachedContentUpdateRequest updateRequest = CachedContentUpdateRequest.builder()
    .ttl(Duration.ofHours(2))
    .build();
GoogleGenAiCachedContent updated = cachedContentService.update(cacheName, updateRequest);

// Список всех кэшированных контентов
List<GoogleGenAiCachedContent> allCaches = cachedContentService.listAll();

// Удалить кэшированный контент
boolean deleted = cachedContentService.delete(cacheName);

// Увеличить TTL кэша
GoogleGenAiCachedContent extended = cachedContentService.extendTtl(cacheName, Duration.ofMinutes(30));

// Очистка истекших кэшей
int removedCount = cachedContentService.cleanupExpired();
```

#### Асинхронные операции

Все операции имеют асинхронные варианты:

```java
CompletableFuture<GoogleGenAiCachedContent> futureCache =
    cachedContentService.createAsync(request);

CompletableFuture<GoogleGenAiCachedContent> futureGet =
    cachedContentService.getAsync(cacheName);

CompletableFuture<Boolean> futureDelete =
    cachedContentService.deleteAsync(cacheName);
```

### Авто-кэширование

Spring AI может автоматически кэшировать большие запросы, когда они превышают указанный порог токенов:

```application.properties
# Автоматически кэшировать запросы, превышающие 100,000 токенов
spring.ai.google.genai.chat.options.auto-cache-threshold=100000
# Установить авто-кэш TTL на 1 час
spring.ai.google.genai.chat.options.auto-cache-ttl=PT1H
```

Или программно:

```java
ChatResponse response = chatModel.call(
    new Prompt(
        largePrompt,
        GoogleGenAiChatOptions.builder()
            .autoCacheThreshold(100000)
            .autoCacheTtl(Duration.ofHours(1))
            .build()
    ));
```

> **Примечание:** Авто-кэширование полезно для одноразовых больших контекстов. Для повторного использования одного и того же контекста более эффективно вручную создавать и ссылаться на кэшированный контент.

### Мониторинг использования кэша

Кэшированный контент включает метаданные использования, доступные через службу:

```java
GoogleGenAiCachedContent content = cachedContentService.get(cacheName);

// Проверьте, истек ли кэш
boolean expired = content.isExpired();

// Получите оставшийся TTL
Duration remaining = content.getRemainingTtl();

// Получите метаданные использования
CachedContentUsageMetadata metadata = content.getUsageMetadata();
if (metadata != null) {
    System.out.println("Общее количество токенов: " + metadata.totalTokenCount().orElse(0));
}
```

### Рекомендации по лучшим практикам

1. **Срок жизни кэша**: Установите соответствующий TTL в зависимости от вашего случая использования. Более короткие TTL для часто меняющегося контента, более длинные для статического контента.
2. **Именование кэша**: Используйте описательные имена для легкой идентификации кэшированного контента.
3. **Очистка**: Периодически очищайте истекшие кэши для поддержания порядка.
4. **Порог токенов**: Кэшируйте только контент, который превышает минимальный порог (32,768 токенов).
5. **Оптимизация затрат**: Повторно используйте кэшированный контент в нескольких запросах для максимизации экономии.

### Пример конфигурации

Полный пример конфигурации:

```application.properties
# Включить службу кэшированного контента (включена по умолчанию)
spring.ai.google.genai.chat.enable-cached-content=true

# Использовать конкретный кэшированный контент
spring.ai.google.genai.chat.options.use-cached-content=true
spring.ai.google.genai.chat.options.cached-content-name=cachedContent/my-cache-123

# Конфигурация авто-кэширования
spring.ai.google.genai.chat.options.auto-cache-threshold=50000
spring.ai.google.genai.chat.options.auto-cache-ttl=PT30M
```

## Пример контроллера

[Создайте](https://start.spring.io/) новый проект Spring Boot и добавьте `spring-ai-starter-model-google-genai` в зависимости вашего pom (или gradle).

Добавьте файл `application.properties` в директорию `src/main/resources`, чтобы включить и настроить модель чата Google GenAI:

### Использование Gemini Developer API (API-ключ)

```application.properties
spring.ai.google.genai.api-key=YOUR_API_KEY
spring.ai.google.genai.chat.options.model=gemini-2.0-flash
spring.ai.google.genai.chat.options.temperature=0.5
```

### Использование Vertex AI

```application.properties
spring.ai.google.genai.project-id=PROJECT_ID
spring.ai.google.genai.location=LOCATION
spring.ai.google.genai.chat.options.model=gemini-2.0-flash
spring.ai.google.genai.chat.options.temperature=0.5
```

> **Совет:** Замените `project-id` на идентификатор вашего проекта Google Cloud, а `location` на регион Google Cloud, например `us-central1`, `europe-west1` и т. д.

[ПРИМЕЧАНИЕ]
====
Каждая модель имеет свой набор поддерживаемых регионов, вы можете найти список поддерживаемых регионов на странице модели.
====

Это создаст реализацию `GoogleGenAiChatModel`, которую вы можете внедрить в свой класс. Вот пример простого класса `@Controller`, который использует модель чата для генерации текста.

```java
@RestController
public class ChatController {

    private final GoogleGenAiChatModel chatModel;

    @Autowired
    public ChatController(GoogleGenAiChatModel chatModel) {
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

## Ручная конфигурация

[GoogleGenAiChatModel](https://github.com/spring-projects/spring-ai/blob/main/models/spring-ai-google-genai/src/main/java/org/springframework/ai/google/genai/GoogleGenAiChatModel.java) реализует `ChatModel` и использует `com.google.genai.Client` для подключения к службе Google GenAI.

Добавьте зависимость `spring-ai-google-genai` в файл Maven `pom.xml` вашего проекта:

```xml
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-google-genai</artifactId>
</dependency>
```

или в файл сборки Gradle `build.gradle`.

```groovy
dependencies {
    implementation 'org.springframework.ai:spring-ai-google-genai'
}
```

> **Совет:** Обратитесь к разделу xref:getting-started.adoc#dependency-management[Управление зависимостями], чтобы добавить BOM Spring AI в ваш файл сборки.

Затем создайте `GoogleGenAiChatModel` и используйте его для генерации текста:

### Использование API-ключа

```java
Client genAiClient = Client.builder()
    .apiKey(System.getenv("GOOGLE_API_KEY"))
    .build();

var chatModel = new GoogleGenAiChatModel(genAiClient,
    GoogleGenAiChatOptions.builder()
        .model(ChatModel.GEMINI_2_0_FLASH)
        .temperature(0.4)
    .build());

ChatResponse response = this.chatModel.call(
    new Prompt("Сгенерируйте имена 5 известных пиратов."));
```

### Использование Vertex AI

```java
Client genAiClient = Client.builder()
    .project(System.getenv("GOOGLE_CLOUD_PROJECT"))
    .location(System.getenv("GOOGLE_CLOUD_LOCATION"))
    .vertexAI(true)
    .build();

var chatModel = new GoogleGenAiChatModel(genAiClient,
    GoogleGenAiChatOptions.builder()
        .model(ChatModel.GEMINI_2_0_FLASH)
        .temperature(0.4)
    .build());

ChatResponse response = this.chatModel.call(
    new Prompt("Сгенерируйте имена 5 известных пиратов."));
```

`GoogleGenAiChatOptions` предоставляет информацию о конфигурации для запросов чата. `GoogleGenAiChatOptions.Builder` — это удобный строитель опций.

## Миграция с Vertex AI Gemini

Если вы в настоящее время используете реализацию Vertex AI Gemini (`spring-ai-vertex-ai-gemini`), вы можете мигрировать на Google GenAI с минимальными изменениями:

### Ключевые различия

1. **SDK**: Google GenAI использует новый `com.google.genai.Client` вместо `com.google.cloud.vertexai.VertexAI`
2. **Аутентификация**: Поддерживает как API-ключ, так и учетные данные Google Cloud
3. **Имена пакетов**: Классы находятся в `org.springframework.ai.google.genai` вместо `org.springframework.ai.vertexai.gemini`
4. **Префикс свойств**: Использует `spring.ai.google.genai` вместо `spring.ai.vertex.ai.gemini`

### Когда использовать Google GenAI против Vertex AI Gemini

**Используйте Google GenAI, когда:**
- Вы хотите быстрое прототипирование с API-ключами
- Вам нужны последние функции Gemini из Developer API
- Вы хотите гибкость в переключении между режимами API-ключа и Vertex AI

**Используйте Vertex AI Gemini, когда:**
- У вас есть существующая инфраструктура Vertex AI
- Вам нужны специфические функции предприятия Vertex AI
- Ваша организация требует развертывания только в Google Cloud

## Низкоуровневый Java-клиент [[low-level-api]]

Реализация Google GenAI основана на новом Java SDK Google GenAI, который предоставляет современный, упрощенный API для доступа к моделям Gemini.
