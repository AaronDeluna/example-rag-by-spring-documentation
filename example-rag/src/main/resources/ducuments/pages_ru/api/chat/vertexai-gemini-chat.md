# VertexAI Gemini Chat

API [Vertex AI Gemini](https://cloud.google.com/vertex-ai/docs/generative-ai/multimodal/overview) позволяет разработчикам создавать приложения генеративного ИИ с использованием модели Gemini. 
API Vertex AI Gemini поддерживает мультимодальные подсказки в качестве входных данных и текст или код в качестве выходных данных. 
Мультимодальная модель — это модель, способная обрабатывать информацию из нескольких модальностей, включая изображения, видео и текст. Например, вы можете отправить модели фотографию тарелки с печеньем и попросить ее предоставить вам рецепт этих печений.

Gemini — это семейство моделей генеративного ИИ, разработанных Google DeepMind, предназначенное для мультимодальных случаев использования. API Gemini предоставляет доступ к [Gemini 2.0 Flash](https://cloud.google.com/vertex-ai/generative-ai/docs/models/gemini/2-0-flash) и [Gemini 2.0 Flash-Lite](https://cloud.google.com/vertex-ai/generative-ai/docs/models/gemini/2-0-flash-lite). 
Для получения спецификаций моделей API Vertex AI Gemini смотрите [Информацию о моделях](https://cloud.google.com/vertex-ai/generative-ai/docs/models#gemini-models).

[Справочник API Gemini](https://cloud.google.com/vertex-ai/generative-ai/docs/model-reference/inference)

## Предварительные требования

- Установите [gcloud](https://cloud.google.com/sdk/docs/install) CLI, подходящий для вашей ОС.
- Аутентифицируйтесь, выполнив следующую команду. 
Замените `PROJECT_ID` на идентификатор вашего проекта Google Cloud и `ACCOUNT` на ваше имя пользователя Google Cloud.

[source]
```
gcloud config set project <PROJECT_ID> &&
gcloud auth application-default login <ACCOUNT>
```

## Автонастройка

[ПРИМЕЧАНИЕ]
====
В автонастройке Spring AI произошли значительные изменения, касающиеся имен артефактов стартовых модулей. 
Пожалуйста, обратитесь к [заметкам об обновлении](https://docs.spring.io/spring-ai/reference/upgrade-notes.html) для получения дополнительной информации.
====

Spring AI предоставляет автонастройку Spring Boot для клиента VertexAI Gemini Chat. 
Чтобы включить ее, добавьте следующую зависимость в файл сборки Maven `pom.xml` или Gradle `build.gradle` вашего проекта:

[tabs]
======
Maven::
+
```xml
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-starter-model-vertex-ai-gemini</artifactId>
</dependency>
```

Gradle::
+
```groovy
dependencies {
    implementation 'org.springframework.ai:spring-ai-starter-model-vertex-ai-gemini'
}
```
======

> **Совет:** Обратитесь к разделу [Управление зависимостями](xref:getting-started.adoc#dependency-management), чтобы добавить BOM Spring AI в ваш файл сборки.

### Свойства чата

[ПРИМЕЧАНИЕ]
====
Включение и отключение автонастроек чата теперь настраивается через свойства верхнего уровня с префиксом `spring.ai.model.chat`.

Чтобы включить, используйте spring.ai.model.chat=vertexai (по умолчанию включено).

Чтобы отключить, используйте spring.ai.model.chat=none (или любое значение, которое не соответствует vertexai).

Это изменение сделано для того, чтобы позволить конфигурацию нескольких моделей.
====

Префикс `spring.ai.vertex.ai.gemini` используется в качестве префикса свойств, который позволяет вам подключаться к VertexAI.

| Свойство | Описание | По умолчанию |
| --- | --- | --- |

| spring.ai.model.chat | Включить клиент модели чата | vertexai |
| --- | --- | --- |
| spring.ai.vertex.ai.gemini.project-id | Идентификатор проекта Google Cloud Platform | - |
| spring.ai.vertex.ai.gemini.location | Регион | - |
| spring.ai.vertex.ai.gemini.credentials-uri | URI для учетных данных Vertex AI Gemini. При предоставлении он используется для создания экземпляра `GoogleCredentials` для аутентификации `VertexAI`. | - |
| spring.ai.vertex.ai.gemini.api-endpoint | Конечная точка API Vertex AI Gemini. | - |
| spring.ai.vertex.ai.gemini.scopes |  | - |
| spring.ai.vertex.ai.gemini.transport | Транспорт API. GRPC или REST. | GRPC |

Префикс `spring.ai.vertex.ai.gemini.chat` — это префикс свойств, который позволяет вам настраивать реализацию модели чата для VertexAI Gemini Chat.

| Свойство | Описание | По умолчанию |
| --- | --- | --- |

| spring.ai.vertex.ai.gemini.chat.options.model | Поддерживаемые [модели чата Vertex AI Gemini](https://cloud.google.com/vertex-ai/generative-ai/docs/models#gemini-models) для использования включают `gemini-2.0-flash`, `gemini-2.0-flash-lite` и новые модели `gemini-2.5-pro-preview-03-25`, `gemini-2.5-flash-preview-04-17`. | gemini-2.0-flash |
| --- | --- | --- |
| spring.ai.vertex.ai.gemini.chat.options.response-mime-type | MIME-тип выходного ответа сгенерированного текстового кандидата. | `text/plain`: (по умолчанию) Текстовый вывод или `application/json`: JSON-ответ. |
| spring.ai.vertex.ai.gemini.chat.options.response-schema | Строка, содержащая схему выходного ответа в формате OpenAPI, как описано в https://ai.google.dev/gemini-api/docs/structured-output#json-schemas. | - |
| spring.ai.vertex.ai.gemini.chat.options.google-search-retrieval | Использовать функцию Grounding Google Search | `true` или `false`, по умолчанию `false`. |
| spring.ai.vertex.ai.gemini.chat.options.temperature | Управляет случайностью вывода. Значения могут варьироваться от [0.0,1.0], включая. Значение, близкое к 1.0, будет производить более разнообразные ответы, в то время как значение, близкое к 0.0, обычно приведет к менее неожиданным ответам от генеративной модели. Это значение указывает значение по умолчанию, которое будет использоваться сервером при вызове генеративной модели. | - |
| spring.ai.vertex.ai.gemini.chat.options.top-k | Максимальное количество токенов, которые следует учитывать при выборке. Генеративная модель использует комбинированную выборку Top-k и nucleus. Выборка Top-k учитывает набор наиболее вероятных токенов topK. | - |
| spring.ai.vertex.ai.gemini.chat.options.top-p | Максимальная кумулятивная вероятность токенов, которые следует учитывать при выборке. Генеративная модель использует комбинированную выборку Top-k и nucleus. Выборка nucleus учитывает наименьший набор токенов, сумма вероятностей которых составляет не менее topP. | - |
| spring.ai.vertex.ai.gemini.chat.options.candidate-count | Количество сгенерированных ответных сообщений для возврата. Это значение должно быть в диапазоне [1, 8], включая. По умолчанию 1. | 1 |
| spring.ai.vertex.ai.gemini.chat.options.max-output-tokens | Максимальное количество токенов для генерации. | - |
| spring.ai.vertex.ai.gemini.chat.options.tool-names | Список инструментов, идентифицированных по их именам, которые следует включить для вызова функций в одном запросе. Инструменты с этими именами должны существовать в реестре ToolCallback. | - |
| spring.ai.vertex.ai.gemini.chat.options.tool-callbacks | Обратные вызовы инструментов для регистрации с ChatModel. | - |
| spring.ai.vertex.ai.gemini.chat.options.internal-tool-execution-enabled | Если true, выполнение инструмента должно быть выполнено, в противном случае ответ от модели возвращается пользователю. По умолчанию null, но если это null, будет учитываться `ToolCallingChatOptions.DEFAULT_TOOL_EXECUTION_ENABLED`, который равен true | - |
| spring.ai.vertex.ai.gemini.chat.options.safety-settings | Список настроек безопасности для управления фильтрами безопасности, как определено в [Фильтры безопасности Vertex AI](https://cloud.google.com/vertex-ai/generative-ai/docs/multimodal/configure-safety-filters). Каждая настройка безопасности может иметь метод, порог и категорию. | - |


> **Совет:** Все свойства с префиксом `spring.ai.vertex.ai.gemini.chat.options` могут быть переопределены во время выполнения, добавив специфичные для запроса <<chat-options>> в вызов `Prompt`.


[VertexAiGeminiChatOptions.java](https://github.com/spring-projects/spring-ai/blob/main/models/spring-ai-vertex-ai-gemini/src/main/java/org/springframework/ai/vertexai/gemini/VertexAiGeminiChatOptions.java) предоставляет конфигурации модели, такие как температура, topK и т. д.

При запуске параметры по умолчанию могут быть настроены с помощью конструктора `VertexAiGeminiChatModel(api, options)` или свойств `spring.ai.vertex.ai.chat.options.*`.

Во время выполнения вы можете переопределить параметры по умолчанию, добавив новые, специфичные для запроса, параметры в вызов `Prompt`. 
Например, чтобы переопределить температуру по умолчанию для конкретного запроса:

```java
ChatResponse response = chatModel.call(
    new Prompt(
        "Сгенерируйте имена 5 известных пиратов.",
        VertexAiGeminiChatOptions.builder()
            .temperature(0.4)
        .build()
    ));
```

> **Совет:** В дополнение к специфичным для модели `VertexAiGeminiChatOptions` вы можете использовать переносимый экземпляр [ChatOptions](https://github.com/spring-projects/spring-ai/blob/main/spring-ai-model/src/main/java/org/springframework/ai/chat/prompt/ChatOptions.java), созданный с помощью [ChatOptions#builder()](https://github.com/spring-projects/spring-ai/blob/main/spring-ai-model/src/main/java/org/springframework/ai/chat/prompt/DefaultChatOptionsBuilder.java).

## Вызов инструментов

Модель Vertex AI Gemini поддерживает возможности вызова инструментов (в контексте Google Gemini это называется `вызов функций`), позволяя моделям использовать инструменты во время разговоров. 
Вот пример того, как определить и использовать инструменты на основе `@Tool`:

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

Дополнительную информацию смотрите в документации [Инструменты](xref:api/tools.adoc).

## Мультимодальность

Мультимодальность относится к способности модели одновременно понимать и обрабатывать информацию из различных (входных) источников, включая `текст`, `pdf`, `изображения`, `аудио` и другие форматы данных.

### Изображение, Аудио, Видео
Модели AI Gemini от Google поддерживают эту возможность, понимая и интегрируя текст, код, аудио, изображения и видео. 
Для получения дополнительной информации смотрите блог [Представляем Gemini](https://blog.google/technology/ai/google-gemini-ai/#introducing-gemini).

Интерфейс `Message` в Spring AI поддерживает мультимодальные модели ИИ, вводя тип Media. 
Этот тип содержит данные и информацию о медиа-вложениях в сообщениях, используя `org.springframework.util.MimeType` и `java.lang.Object` для необработанных медиа-данных.

Ниже приведен простой пример кода, извлеченный из [VertexAiGeminiChatModelIT#multiModalityTest()](https://github.com/spring-projects/spring-ai/blob/main/models/spring-ai-vertex-ai-gemini/src/test/java/org/springframework/ai/vertexai/gemini/VertexAiGeminiChatModelIT.java), демонстрирующий сочетание текста пользователя с изображением.

```java
byte[] data = new ClassPathResource("/vertex-test.png").getContentAsByteArray();

var userMessage = new UserMessage("Объясните, что вы видите на этой картинке?",
        List.of(new Media(MimeTypeUtils.IMAGE_PNG, this.data)));

ChatResponse response = chatModel.call(new Prompt(List.of(this.userMessage)));
```

### PDF

Последняя версия Vertex Gemini поддерживает типы входных данных PDF. 
Используйте MIME-тип `application/pdf`, чтобы прикрепить PDF-файл к сообщению:

```java
var pdfData = new ClassPathResource("/spring-ai-reference-overview.pdf");

var userMessage = new UserMessage(
        "Вы очень профессиональный специалист по обобщению документов. Пожалуйста, обобщите данный документ.",
        List.of(new Media(new MimeType("application", "pdf"), pdfData)));

var response = this.chatModel.call(new Prompt(List.of(userMessage)));
```

## Настройки безопасности и рейтинги безопасности

API Vertex AI Gemini предоставляет возможности фильтрации безопасности, чтобы помочь вам контролировать вредоносный контент как в подсказках, так и в ответах. 
Для получения дополнительной информации смотрите [документацию по фильтрам безопасности Vertex AI](https://cloud.google.com/vertex-ai/generative-ai/docs/multimodal/configure-safety-filters).

### Настройка настроек безопасности

Вы можете настроить настройки безопасности, чтобы контролировать порог, при котором контент блокируется для различных категорий вреда. 
Доступные категории вреда:

- `HARM_CATEGORY_HATE_SPEECH` - Контент ненависти
- `HARM_CATEGORY_DANGEROUS_CONTENT` - Опасный контент
- `HARM_CATEGORY_HARASSMENT` - Контент преследования
- `HARM_CATEGORY_SEXUALLY_EXPLICIT` - Контент сексуального характера
- `HARM_CATEGORY_CIVIC_INTEGRITY` - Контент гражданской целостности

Доступные уровни порога:

- `BLOCK_LOW_AND_ABOVE` - Блокировать при низкой, средней или высокой вероятности небезопасного контента
- `BLOCK_MEDIUM_AND_ABOVE` - Блокировать при средней или высокой вероятности небезопасного контента
- `BLOCK_ONLY_HIGH` - Блокировать только при высокой вероятности небезопасного контента
- `BLOCK_NONE` - Никогда не блокировать (используйте с осторожностью)

```java
List<VertexAiGeminiSafetySetting> safetySettings = List.of(
    VertexAiGeminiSafetySetting.builder()
        .withCategory(VertexAiGeminiSafetySetting.HarmCategory.HARM_CATEGORY_HARASSMENT)
        .withThreshold(VertexAiGeminiSafetySetting.HarmBlockThreshold.BLOCK_LOW_AND_ABOVE)
        .build(),
    VertexAiGeminiSafetySetting.builder()
        .withCategory(VertexAiGeminiSafetySetting.HarmCategory.HARM_CATEGORY_HATE_SPEECH)
        .withThreshold(VertexAiGeminiSafetySetting.HarmBlockThreshold.BLOCK_MEDIUM_AND_ABOVE)
        .build());

ChatResponse response = chatModel.call(new Prompt("Ваша подсказка здесь",
    VertexAiGeminiChatOptions.builder()
        .safetySettings(safetySettings)
        .build()));
```

### Доступ к рейтингам безопасности в ответах

Когда настройки безопасности настроены, API Gemini возвращает рейтинги безопасности для каждого кандидата на ответ. 
Эти рейтинги указывают вероятность и серьезность вредоносного контента в каждой категории.

Рейтинги безопасности доступны в метаданных `AssistantMessage` под ключом `"safetyRatings"`:

```java
ChatResponse response = chatModel.call(new Prompt(prompt,
    VertexAiGeminiChatOptions.builder()
        .safetySettings(safetySettings)
        .build()));

// Доступ к рейтингам безопасности из ответа
List<VertexAiGeminiSafetyRating> safetyRatings =
    (List<VertexAiGeminiSafetyRating>) response.getResult()
        .getOutput()
        .getMetadata()
        .get("safetyRatings");

for (VertexAiGeminiSafetyRating rating : safetyRatings) {
    System.out.println("Категория: " + rating.category());
    System.out.println("Вероятность: " + rating.probability());
    System.out.println("Серьезность: " + rating.severity());
    System.out.println("Заблокировано: " + rating.blocked());
}
```

Запись `VertexAiGeminiSafetyRating` содержит:

- `category` - Категория вреда (например, `HARM_CATEGORY_HARASSMENT`)
- `probability` - Уровень вероятности (`NEGLIGIBLE`, `LOW`, `MEDIUM`, `HIGH`)
- `blocked` - Была ли заблокирована информация из-за этого рейтинга
- `probabilityScore` - Сырой балл вероятности (от 0.0 до 1.0)
- `severity` - Уровень серьезности (`HARM_SEVERITY_NEGLIGIBLE`, `HARM_SEVERITY_LOW`, `HARM_SEVERITY_MEDIUM`, `HARM_SEVERITY_HIGH`)
- `severityScore` - Сырой балл серьезности (от 0.0 до 1.0)

## Пример контроллера

[Создайте](https://start.spring.io/) новый проект Spring Boot и добавьте `spring-ai-starter-model-vertex-ai-gemini` в зависимости вашего pom (или gradle).

Добавьте файл `application.properties` в директорию `src/main/resources`, чтобы включить и настроить модель чата VertexAi:

```application.properties
spring.ai.vertex.ai.gemini.project-id=PROJECT_ID
spring.ai.vertex.ai.gemini.location=LOCATION
spring.ai.vertex.ai.gemini.chat.options.model=gemini-2.0-flash
spring.ai.vertex.ai.gemini.chat.options.temperature=0.5
```

> **Совет:** Замените `project-id` на идентификатор вашего проекта Google Cloud, а `location` — на регион Google Cloud, например `us-central1`, `europe-west1` и т. д.

[ПРИМЕЧАНИЕ]
====
Каждая модель имеет свой собственный набор поддерживаемых регионов, вы можете найти список поддерживаемых регионов на странице модели.

Например, модель=`gemini-2.5-flash` в настоящее время доступна только в регионе `us-central1`, вы должны установить location=`us-central1`, следуя странице модели [Gemini 2.5 Flash - Поддерживаемые регионы](https://cloud.google.com/vertex-ai/generative-ai/docs/models/gemini/2-5-flash).
====

Это создаст реализацию `VertexAiGeminiChatModel`, которую вы можете внедрить в свой класс. 
Вот пример простого класса `@Controller`, который использует модель чата для генерации текста.

```java
@RestController
public class ChatController {

    private final VertexAiGeminiChatModel chatModel;

    @Autowired
    public ChatController(VertexAiGeminiChatModel chatModel) {
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

## Ручная настройка

[VertexAiGeminiChatModel](https://github.com/spring-projects/spring-ai/blob/main/models/spring-ai-vertex-ai-gemini/src/main/java/org/springframework/ai/vertexai/gemini/VertexAiGeminiChatModel.java) реализует `ChatModel` и использует `VertexAI` для подключения к службе Vertex AI Gemini.

Добавьте зависимость `spring-ai-vertex-ai-gemini` в файл Maven `pom.xml` вашего проекта:

```xml
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-vertex-ai-gemini</artifactId>
</dependency>
```

или в файл сборки Gradle `build.gradle`.

```groovy
dependencies {
    implementation 'org.springframework.ai:spring-ai-vertex-ai-gemini'
}
```

> **Совет:** Обратитесь к разделу [Управление зависимостями](xref:getting-started.adoc#dependency-management), чтобы добавить BOM Spring AI в ваш файл сборки.

Затем создайте `VertexAiGeminiChatModel` и используйте его для генерации текста:

```java
VertexAI vertexApi =  new VertexAI(projectId, location);

var chatModel = new VertexAiGeminiChatModel(this.vertexApi,
    VertexAiGeminiChatOptions.builder()
        .model(ChatModel.GEMINI_2_0_FLASH)
        .temperature(0.4)
    .build());

ChatResponse response = this.chatModel.call(
    new Prompt("Сгенерируйте имена 5 известных пиратов."));
```

`VertexAiGeminiChatOptions` предоставляет информацию о конфигурации для запросов чата. 
`VertexAiGeminiChatOptions.Builder` — это удобный строитель опций.


Следующая диаграмма классов иллюстрирует нативный Java API Vertex AI Gemini:

![w=800,align="center"](vertex-ai-gemini-native-api.jpg)
