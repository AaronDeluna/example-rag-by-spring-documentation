# Mistral AI Chat

Spring AI поддерживает различные языковые модели AI от Mistral AI. Вы можете взаимодействовать с языковыми моделями Mistral AI и создать многоязычного разговорного помощника на основе моделей Mistral.

> **Совет:** Mistral AI также предлагает конечную точку, совместимую с OpenAI API. Ознакомьтесь с разделом xref:_openai_api_compatibility[Совместимость с OpenAI API], чтобы узнать, как использовать интеграцию xref:api/chat/openai-chat.adoc[Spring AI OpenAI] для общения с конечной точкой Mistral.

## Предварительные требования

Вам необходимо создать API с Mistral AI, чтобы получить доступ к языковым моделям Mistral AI.

Создайте учетную запись на https://auth.mistral.ai/ui/registration[странице регистрации Mistral AI] и сгенерируйте токен на https://console.mistral.ai/api-keys/[странице API-ключей].

Проект Spring AI определяет свойство конфигурации с именем `spring.ai.mistralai.api-key`, которое вы должны установить в значение `API Key`, полученное с console.mistral.ai.

Вы можете установить это свойство конфигурации в вашем файле `application.properties`:

```properties
spring.ai.mistralai.api-key=<ваш-mistralai-api-key>
```

Для повышения безопасности при работе с конфиденциальной информацией, такой как API-ключи, вы можете использовать язык выражений Spring (SpEL) для ссылки на пользовательскую переменную окружения:

```yaml
# В application.yml
spring:
  ai:
    mistralai:
      api-key: ${MISTRALAI_API_KEY}
```

```bash
# В вашей среде или .env файле
export MISTRALAI_API_KEY=<ваш-mistralai-api-key>
```

Вы также можете установить эту конфигурацию программно в коде вашего приложения:

```java
// Получите API-ключ из безопасного источника или переменной окружения
String apiKey = System.getenv("MISTRALAI_API_KEY");
```

### Добавление репозиториев и BOM

Артефакты Spring AI публикуются в репозиториях Maven Central и Spring Snapshot. Обратитесь к разделу xref:getting-started.adoc#artifact-repositories[Артефакты репозиториев], чтобы добавить эти репозитории в вашу систему сборки.

Чтобы помочь с управлением зависимостями, Spring AI предоставляет BOM (bill of materials), чтобы гарантировать, что одна и та же версия Spring AI используется на протяжении всего проекта. Обратитесь к разделу xref:getting-started.adoc#dependency-management[Управление зависимостями], чтобы добавить BOM Spring AI в вашу систему сборки.

## Автоконфигурация

[NOTE]
====
В автоконфигурации Spring AI произошли значительные изменения в названиях артефактов модулей-стартеров. Пожалуйста, обратитесь к https://docs.spring.io/spring-ai/reference/upgrade-notes.html[заметкам об обновлении] для получения дополнительной информации.
====

Spring AI предоставляет автоконфигурацию Spring Boot для клиента Mistral AI Chat. Чтобы включить ее, добавьте следующую зависимость в файл `pom.xml` вашего проекта Maven:

```xml
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-starter-model-mistral-ai</artifactId>
</dependency>
```

или в файл сборки Gradle `build.gradle`.

```groovy
dependencies {
    implementation 'org.springframework.ai:spring-ai-starter-model-mistral-ai'
}
```

> **Совет:** Обратитесь к разделу xref:getting-started.adoc#dependency-management[Управление зависимостями], чтобы добавить BOM Spring AI в ваш файл сборки.

### Свойства чата

#### Свойства повторных попыток

Префикс `spring.ai.retry` используется как префикс свойства, который позволяет вам настроить механизм повторных попыток для модели чата Mistral AI.

[cols="3,5,1", stripes=even]
|====
| Свойство | Описание | По умолчанию

| spring.ai.retry.max-attempts   | Максимальное количество попыток повторного запроса. |  10
| spring.ai.retry.backoff.initial-interval | Начальная продолжительность ожидания для политики экспоненциального отката. |  2 сек.
| spring.ai.retry.backoff.multiplier | Множитель интервала отката. |  5
| spring.ai.retry.backoff.max-interval | Максимальная продолжительность отката. |  3 мин.
| spring.ai.retry.on-client-errors | Если false, выбросить NonTransientAiException и не пытаться повторить запрос для кодов ошибок клиента `4xx` | false
| spring.ai.retry.exclude-on-http-codes | Список кодов состояния HTTP, которые не должны вызывать повторный запрос (например, для выброса NonTransientAiException). | пусто
| spring.ai.retry.on-http-codes | Список кодов состояния HTTP, которые должны вызывать повторный запрос (например, для выброса TransientAiException). | пусто
|====

#### Свойства подключения

Префикс `spring.ai.mistralai` используется как префикс свойства, который позволяет вам подключаться к OpenAI.

[cols="3,5,1", stripes=even]
|====
| Свойство | Описание | По умолчанию

| spring.ai.mistralai.base-url   | URL для подключения |  https://api.mistral.ai
| spring.ai.mistralai.api-key    | API-ключ           |  -
|====

#### Свойства конфигурации

[NOTE]
====
Включение и отключение автоконфигураций чата теперь настраивается через свойства верхнего уровня с префиксом `spring.ai.model.chat`.

Чтобы включить, используйте spring.ai.model.chat=mistral (по умолчанию включено)

Чтобы отключить, используйте spring.ai.model.chat=none (или любое значение, которое не совпадает с mistral)

Это изменение сделано для того, чтобы позволить конфигурацию нескольких моделей.
====

Префикс `spring.ai.mistralai.chat` — это префикс свойства, который позволяет вам настроить реализацию модели чата для Mistral AI.

[cols="3,5,1", stripes=even]
|====
| Свойство | Описание | По умолчанию

| spring.ai.mistralai.chat.enabled (Удалено и больше не актуально) | Включить модель чата Mistral AI.  | true
| spring.ai.model.chat | Включить модель чата Mistral AI.  | mistral
| spring.ai.mistralai.chat.base-url   | Необязательное переопределение свойства `spring.ai.mistralai.base-url` для предоставления URL, специфичного для чата. |  -
| spring.ai.mistralai.chat.api-key   | Необязательное переопределение свойства `spring.ai.mistralai.api-key` для предоставления API-ключа, специфичного для чата. |  -
| spring.ai.mistralai.chat.options.model | Это модель чата Mistral AI, которую следует использовать | `open-mistral-7b`, `open-mixtral-8x7b`, `open-mixtral-8x22b`, `mistral-small-latest`, `mistral-large-latest`
| spring.ai.mistralai.chat.options.temperature | Температура выборки, которая контролирует очевидную креативность сгенерированных завершений. Более высокие значения сделают вывод более случайным, в то время как более низкие значения сделают результаты более сосредоточенными и детерминированными. Не рекомендуется изменять `temperature` и `top_p` для одного и того же запроса на завершение, так как взаимодействие этих двух настроек трудно предсказать. | 0.8
| spring.ai.mistralai.chat.options.maxTokens | Максимальное количество токенов, которые нужно сгенерировать в завершении чата. Общая длина входных токенов и сгенерированных токенов ограничена длиной контекста модели. | -
| spring.ai.mistralai.chat.options.safePrompt | Указывает, следует ли вставлять защитный запрос перед всеми разговорами. | false
| spring.ai.mistralai.chat.options.randomSeed | Эта функция находится в бета-версии. Если указано, наша система постарается выполнить выборку детерминированно, так что повторные запросы с тем же семенем и параметрами должны возвращать один и тот же результат. | -
| spring.ai.mistralai.chat.options.stop | Остановить генерацию, если этот токен обнаружен. Или если один из этих токенов обнаружен при предоставлении массива. | -
| spring.ai.mistralai.chat.options.topP | Альтернатива выборке с температурой, называемая ядерной выборкой, где модель учитывает результаты токенов с вероятностью top_p. Таким образом, 0.1 означает, что учитываются только токены, составляющие верхние 10% вероятностной массы. Мы обычно рекомендуем изменять это или `temperature`, но не оба. | -
| spring.ai.mistralai.chat.options.responseFormat | Объект, указывающий формат, который модель должна выводить. Установка в `{ "type": "json_object" }` включает режим JSON, который гарантирует, что сообщение, сгенерированное моделью, является допустимым JSON. Установка в `{ "type": "json_schema" }` с предоставленной схемой включает нативные структурированные выходные данные, что гарантирует, что модель будет соответствовать вашей предоставленной JSON-схеме. См. раздел <<structured-output>> для получения дополнительной информации.| -
| spring.ai.mistralai.chat.options.tools | Список инструментов, которые модель может вызывать. В настоящее время поддерживаются только функции в качестве инструмента. Используйте это, чтобы предоставить список функций, для которых модель может генерировать JSON-входы. | -
| spring.ai.mistralai.chat.options.toolChoice | Управляет тем, какая (если есть) функция вызывается моделью. `none` означает, что модель не будет вызывать функцию и вместо этого сгенерирует сообщение. `auto` означает, что модель может выбирать между генерацией сообщения или вызовом функции. Указание конкретной функции через `{"type": "function", "function": {"name": "my_function"}}` заставляет модель вызвать эту функцию. `none` — это значение по умолчанию, когда функции отсутствуют. `auto` — это значение по умолчанию, если функции присутствуют. | -
| spring.ai.mistralai.chat.options.tool-names | Список инструментов, идентифицированных по их именам, для включения вызова функций в одном запросе. Инструменты с этими именами должны существовать в реестре ToolCallback. | -
| spring.ai.mistralai.chat.options.tool-callbacks | Обратные вызовы инструментов для регистрации с ChatModel. | -
| spring.ai.mistralai.chat.options.internal-tool-execution-enabled | Если false, Spring AI не будет обрабатывать вызовы инструментов внутренне, а будет проксировать их клиенту. Тогда ответственность за обработку вызовов инструментов, их распределение на соответствующую функцию и возврат результатов ложится на клиента. Если true (по умолчанию), Spring AI будет обрабатывать вызовы функций внутренне. Применимо только для моделей чата с поддержкой вызова функций | true
|====

> **Примечание:** Вы можете переопределить общие `spring.ai.mistralai.base-url` и `spring.ai.mistralai.api-key` для реализаций `ChatModel` и `EmbeddingModel`. Свойства `spring.ai.mistralai.chat.base-url` и `spring.ai.mistralai.chat.api-key`, если установлены, имеют приоритет над общими свойствами. Это полезно, если вы хотите использовать разные учетные записи Mistral AI для разных моделей и разных конечных точек моделей.

> **Совет:** Все свойства с префиксом `spring.ai.mistralai.chat.options` могут быть переопределены во время выполнения, добавляя специфические для запроса <<chat-options>> к вызову `Prompt`.

## Опции времени выполнения [[chat-options]]

[Модель MistralAiChatOptions.java](https://github.com/spring-projects/spring-ai/blob/main/models/spring-ai-mistral-ai/src/main/java/org/springframework/ai/mistralai/MistralAiChatOptions.java) предоставляет конфигурации модели, такие как используемая модель, температура, штраф за частоту и т. д.

При запуске параметры по умолчанию могут быть настроены с помощью конструктора `MistralAiChatModel(api, options)` или свойств `spring.ai.mistralai.chat.options.*`.

Во время выполнения вы можете переопределить параметры по умолчанию, добавляя новые, специфические для запроса параметры к вызову `Prompt`. Например, чтобы переопределить модель и температуру по умолчанию для конкретного запроса:

```java
ChatResponse response = chatModel.call(
    new Prompt(
        "Сгенерируйте имена 5 известных пиратов.",
        MistralAiChatOptions.builder()
            .model(MistralAiApi.ChatModel.MISTRAL_LARGE.getValue())
            .temperature(0.5)
        .build()
    ));
```

> **Совет:** В дополнение к специфическим для модели [MistralAiChatOptions](https://github.com/spring-projects/spring-ai/blob/main/models/spring-ai-mistral-ai/src/main/java/org/springframework/ai/mistralai/MistralAiChatOptions.java) вы можете использовать переносимый [ChatOptions](https://github.com/spring-projects/spring-ai/blob/main/spring-ai-model/src/main/java/org/springframework/ai/chat/prompt/ChatOptions.java) экземпляр, созданный с помощью [ChatOptions#builder()](https://github.com/spring-projects/spring-ai/blob/main/spring-ai-model/src/main/java/org/springframework/ai/chat/prompt/DefaultChatOptionsBuilder.java).

## Вызов функций

Вы можете зарегистрировать пользовательские функции Java с `MistralAiChatModel` и позволить модели Mistral AI интеллектуально выбирать выводить JSON-объект, содержащий аргументы для вызова одной или нескольких зарегистрированных функций. Это мощная техника для соединения возможностей LLM с внешними инструментами и API. Узнайте больше о xref:api/tools.adoc[Вызов инструментов].

## Структурированный вывод [[structured-output]]

Mistral AI поддерживает нативные структурированные выходные данные через JSON Schema, гарантируя, что модель генерирует ответы, которые строго соответствуют вашей указанной структуре. Эта функция доступна для моделей Mistral Small и более поздних.

### Использование ChatClient с нативным структурированным выводом

Самый простой способ использовать структурированный вывод — это использовать высокоуровневый API `ChatClient` и советник `ENABLE_NATIVE_STRUCTURED_OUTPUT`:

```java
record ActorsFilms(String actor, List<String> movies) {}

ActorsFilms actorsFilms = ChatClient.create(chatModel).prompt()
    .advisors(AdvisorParams.ENABLE_NATIVE_STRUCTURED_OUTPUT)
    .user("Сгенерируйте фильмографию 5 фильмов для Тома Хэнкса.")
    .call()
    .entity(ActorsFilms.class);
```

Этот подход автоматически:
- Генерирует JSON-схему из вашего класса Java
- Настраивает модель на использование нативного структурированного вывода
- Парсит ответ в ваш указанный тип

### Использование ResponseFormat напрямую

Для большего контроля вы можете использовать класс `ResponseFormat` с `MistralAiChatOptions`:

```java
record MovieRecommendation(String title, String director, int year, String plotSummary) {}

var options = MistralAiChatOptions.builder()
    .model(MistralAiApi.ChatModel.MISTRAL_SMALL.getValue())
    .responseFormat(ResponseFormat.jsonSchema(MovieRecommendation.class))
    .build();

ChatResponse response = chatModel.call(
    new Prompt("Рекомендуйте классический научно-фантастический фильм.", options));
```

Класс `ResponseFormat` предоставляет несколько фабричных методов:

- `ResponseFormat.text()` - Возвращает вывод в виде обычного текста (по умолчанию)
- `ResponseFormat.jsonObject()` - Возвращает допустимый JSON (без принудительного соблюдения схемы)
- `ResponseFormat.jsonSchema(Class<?>)` - Генерирует схему из класса Java
- `ResponseFormat.jsonSchema(String)` - Использует строку JSON-схемы
- `ResponseFormat.jsonSchema(Map)` - Использует карту JSON-схемы

### Режим JSON против структурированного вывода

Mistral AI поддерживает два режима, связанных с JSON:

- **Режим JSON** (`json_object`): Гарантирует допустимый вывод JSON, но не принуждает к конкретной структуре
- **Структурированный вывод** (`json_schema`): Гарантирует вывод, соответствующий вашей JSON-схеме

```java
// Режим JSON - любой допустимый JSON
var jsonMode = MistralAiChatOptions.builder()
    .responseFormat(ResponseFormat.jsonObject())
    .build();

// Структурированный вывод - принудительно соблюдаемая конкретная схема
var structuredOutput = MistralAiChatOptions.builder()
    .responseFormat(ResponseFormat.jsonSchema(MyClass.class))
    .build();
```

Для получения дополнительной информации о структурированных выходных данных смотрите документацию xref:api/structured-output-converter.adoc[Конвертер структурированных выходных данных].

## Мультимодальность

Мультимодальность относится к способности модели одновременно понимать и обрабатывать информацию из различных источников, включая текст, изображения, аудио и другие форматы данных. Mistral AI поддерживает текстовые и визуальные модальности.

### Визуальные данные

Модели Mistral AI, которые предлагают поддержку мультимодальности, включают `pixtral-large-latest`. Ознакомьтесь с руководством [Vision](https://docs.mistral.ai/capabilities/vision/) для получения дополнительной информации.

API [User Message](https://docs.mistral.ai/api/#tag/chat/operation/chat_completion_v1_chat_completions_post) Mistral AI может включать список изображений в формате base64 или URL изображений с сообщением. Интерфейс [Message](https://github.com/spring-projects/spring-ai/blob/main/spring-ai-client-chat/src/main/java/org/springframework/ai/chat/messages/Message.java) Spring AI облегчает работу с мультимодальными AI моделями, вводя тип [Media](https://github.com/spring-projects/spring-ai/blob/main/spring-ai-commons/src/main/java/org/springframework/ai/content/Media.java). Этот тип охватывает данные и детали о медиа-вложениях в сообщениях, используя `org.springframework.util.MimeType` и `org.springframework.core.io.Resource` для необработанных медиа-данных.

Ниже приведен пример кода, извлеченный из `MistralAiChatModelIT.java`, иллюстрирующий сочетание текста пользователя с изображением.

```java
var imageResource = new ClassPathResource("/multimodal.test.png");

var userMessage = new UserMessage("Объясните, что вы видите на этой картинке?",
        new Media(MimeTypeUtils.IMAGE_PNG, this.imageResource));

ChatResponse response = chatModel.call(new Prompt(this.userMessage,
        ChatOptions.builder().model(MistralAiApi.ChatModel.PIXTRAL_LARGE.getValue()).build()));
```

или эквивалент с URL изображения:

```java
var userMessage = new UserMessage("Объясните, что вы видите на этой картинке?",
        new Media(MimeTypeUtils.IMAGE_PNG,
                URI.create("https://docs.spring.io/spring-ai/reference/_images/multimodal.test.png")));

ChatResponse response = chatModel.call(new Prompt(this.userMessage,
        ChatOptions.builder().model(MistralAiApi.ChatModel.PIXTRAL_LARGE.getValue()).build()));
```

> **Совет:** Вы также можете передавать несколько изображений.

Пример показывает, как модель принимает в качестве входных данных изображение `multimodal.test.png`:

![Изображение теста мультимодальности, 200, 200, align="left"](multimodal.test.png)

вместе с текстовым сообщением "Объясните, что вы видите на этой картинке?", и генерирует ответ, подобный этому:

```
Это изображение фруктовой миски с простым дизайном. Миска сделана из металла с изогнутыми проволочными краями, которые создают открытую структуру, позволяя фруктам быть видимыми со всех сторон. Внутри миски находятся два желтых банана, лежащих на красном яблоке. Бананы слегка перезрелые, о чем свидетельствуют коричневые пятна на их кожуре. У миски есть металлическое кольцо сверху, вероятно, для того, чтобы служить ручкой для переноски. Миска стоит на ровной поверхности с нейтральным фоном, который обеспечивает четкий вид на фрукты внутри.
```

## Совместимость с OpenAI API

Mistral совместим с OpenAI API, и вы можете использовать клиент xref:api/chat/openai-chat.adoc[Spring AI OpenAI] для общения с Mistral. Для этого вам необходимо настроить базовый URL OpenAI на платформу Mistral AI: `spring.ai.openai.chat.base-url=https://api.mistral.ai`, выбрать модель Mistral: `spring.ai.openai.chat.options.model=mistral-small-latest` и установить API-ключ Mistral AI: `spring.ai.openai.chat.api-key=<ВАШ MISTRAL API KEY>`.

Проверьте тесты [MistralWithOpenAiChatModelIT.java](https://github.com/spring-projects/spring-ai/blob/main/models/spring-ai-openai/src/test/java/org/springframework/ai/openai/chat/proxy/MistralWithOpenAiChatModelIT.java) для примеров использования Mistral через Spring AI OpenAI.

## Пример контроллера (Автоконфигурация)

https://start.spring.io/[Создайте] новый проект Spring Boot и добавьте `spring-ai-starter-model-mistral-ai` в зависимости вашего pom (или gradle).

Добавьте файл `application.properties` в директорию `src/main/resources`, чтобы включить и настроить модель чата Mistral AI:

```application.properties
spring.ai.mistralai.api-key=YOUR_API_KEY
spring.ai.mistralai.chat.options.model=mistral-small
spring.ai.mistralai.chat.options.temperature=0.7
```

> **Совет:** Замените `api-key` на ваши учетные данные Mistral AI.

Это создаст реализацию `MistralAiChatModel`, которую вы можете внедрить в ваши классы. Вот пример простого класса `@RestController`, который использует модель чата для генерации текста.

```java
@RestController
public class ChatController {

    private final MistralAiChatModel chatModel;

    @Autowired
    public ChatController(MistralAiChatModel chatModel) {
        this.chatModel = chatModel;
    }

    @GetMapping("/ai/generate")
    public Map<String,String> generate(@RequestParam(value = "message", defaultValue = "Расскажи мне шутку") String message) {
        return Map.of("generation", this.chatModel.call(message));
    }

    @GetMapping("/ai/generateStream")
	public Flux<ChatResponse> generateStream(@RequestParam(value = "message", defaultValue = "Расскажи мне шутку") String message) {
        var prompt = new Prompt(new UserMessage(message));
        return this.chatModel.stream(prompt);
    }
}
```

## Ручная конфигурация

[Модель MistralAiChatModel](https://github.com/spring-projects/spring-ai/blob/main/models/spring-ai-mistral-ai/src/main/java/org/springframework/ai/mistralai/MistralAiChatModel.java) реализует `ChatModel` и `StreamingChatModel` и использует <<low-level-api>> для подключения к сервису Mistral AI.

Добавьте зависимость `spring-ai-mistral-ai` в файл `pom.xml` вашего проекта Maven:

```xml
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-mistral-ai</artifactId>
</dependency>
```

или в файл сборки Gradle `build.gradle`.

```groovy
dependencies {
    implementation 'org.springframework.ai:spring-ai-mistral-ai'
}
```

> **Совет:** Обратитесь к разделу xref:getting-started.adoc#dependency-management[Управление зависимостями], чтобы добавить BOM Spring AI в ваш файл сборки.

Затем создайте `MistralAiChatModel` и используйте его для генерации текста:

```java
var mistralAiApi = new MistralAiApi(System.getenv("MISTRAL_AI_API_KEY"));

var chatModel = new MistralAiChatModel(this.mistralAiApi, MistralAiChatOptions.builder()
                .model(MistralAiApi.ChatModel.MISTRAL_LARGE.getValue())
                .temperature(0.4)
                .maxTokens(200)
                .build());

ChatResponse response = this.chatModel.call(
    new Prompt("Сгенерируйте имена 5 известных пиратов."));

// Или с потоковыми ответами
Flux<ChatResponse> response = this.chatModel.stream(
    new Prompt("Сгенерируйте имена 5 известных пиратов."));
```

`MistralAiChatOptions` предоставляет информацию о конфигурации для запросов чата. `MistralAiChatOptions.Builder` — это флюидный строитель опций.

### Низкоуровневый клиент MistralAiApi [[low-level-api]]

[Модель MistralAiApi](https://github.com/spring-projects/spring-ai/blob/main/models/spring-ai-mistral-ai/src/main/java/org/springframework/ai/mistralai/api/MistralAiApi.java) предоставляет легковесный Java-клиент для [API Mistral AI](https://docs.mistral.ai/api/).

Вот простой фрагмент, показывающий, как использовать API программно:

```java
MistralAiApi mistralAiApi = new MistralAiApi(System.getenv("MISTRAL_AI_API_KEY"));

ChatCompletionMessage chatCompletionMessage =
    new ChatCompletionMessage("Привет, мир", Role.USER);

// Синхронный запрос
ResponseEntity<ChatCompletion> response = this.mistralAiApi.chatCompletionEntity(
    new ChatCompletionRequest(List.of(this.chatCompletionMessage), MistralAiApi.ChatModel.MISTRAL_LARGE.getValue(), 0.8, false));

// Потоковый запрос
Flux<ChatCompletionChunk> streamResponse = this.mistralAiApi.chatCompletionStream(
        new ChatCompletionRequest(List.of(this.chatCompletionMessage), MistralAiApi.ChatModel.MISTRAL_LARGE.getValue(), 0.8, true));
```

Следуйте https://github.com/spring-projects/spring-ai/blob/main/models/spring-ai-mistral-ai/src/main/java/org/springframework/ai/mistralai/api/MistralAiApi.java[MistralAiApi.java]'s JavaDoc для получения дополнительной информации.

#### Примеры MistralAiApi

- Тесты [MistralAiApiIT.java](https://github.com/spring-projects/spring-ai/blob/main/models/spring-ai-mistral-ai/src/test/java/org/springframework/ai/mistralai/api/MistralAiApiIT.java) предоставляют некоторые общие примеры использования легковесной библиотеки.

- Тесты [PaymentStatusFunctionCallingIT.java](https://github.com/spring-projects/spring-ai/blob/main/models/spring-ai-mistral-ai/src/test/java/org/springframework/ai/mistralai/api/tool/PaymentStatusFunctionCallingIT.java) показывают, как использовать низкоуровневый API для вызова функций инструментов. Основано на учебнике [Вызов функций Mistral AI](https://docs.mistral.ai/guides/function-calling/).

## Mistral AI OCR

Spring AI поддерживает оптическое распознавание символов (OCR) с Mistral AI. Это позволяет вам извлекать текст и данные изображений из документов.

## Предварительные требования

Вам необходимо создать API с Mistral AI, чтобы получить доступ к языковым моделям Mistral AI. Создайте учетную запись на https://auth.mistral.ai/ui/registration[странице регистрации Mistral AI] и сгенерируйте токен на https://console.mistral.ai/api-keys/[странице API-ключей].

### Добавление зависимостей

Чтобы использовать API Mistral AI OCR, вам необходимо добавить зависимость `spring-ai-mistral-ai` в ваш проект.

```xml
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-mistral-ai</artifactId>
</dependency>
```

или в файл сборки Gradle `build.gradle`.

```groovy
dependencies {
    implementation 'org.springframework.ai:spring-ai-mistral-ai'
}
```

### Низкоуровневый клиент MistralOcrApi

[Модель MistralOcrApi](https://github.com/spring-projects/spring-ai/blob/main/models/spring-ai-mistral-ai/src/main/java/org/springframework/ai/mistralai/api/MistralOcrApi.java) предоставляет легковесный Java-клиент для [API Mistral AI OCR](https://docs.mistral.ai/api/#tag/OCR).

Вот простой фрагмент, показывающий, как использовать API программно:

```java
MistralOcrApi mistralAiApi = new MistralOcrApi(System.getenv("MISTRAL_AI_API_KEY"));

String documentUrl = "https://arxiv.org/pdf/2201.04234";
MistralOcrApi.OCRRequest request = new MistralOcrApi.OCRRequest(
        MistralOcrApi.OCRModel.MISTRAL_OCR_LATEST.getValue(), "test_id",
        new MistralOcrApi.OCRRequest.DocumentURLChunk(documentUrl), List.of(0, 1, 2), true, 5, 50);

ResponseEntity<MistralOcrApi.OCRResponse> response = mistralAiApi.ocr(request);
```

Следуйте https://github.com/spring-projects/spring-ai/blob/main/models/spring-ai-mistral-ai/src/main/java/org/springframework/ai/mistralai/api/MistralOcrApi.java[MistralOcrApi.java]'s JavaDoc для получения дополнительной информации.

#### Пример MistralOcrApi

- Тесты [MistralOcrApiIT.java](https://github.com/spring-projects/spring-ai/blob/main/models/spring-ai-mistral-ai/src/test/java/org/springframework/ai/mistralai/api/MistralOcrApiIT.java) предоставляют некоторые общие примеры использования легковесной библиотеки.
