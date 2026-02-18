# Groq Chat

https://groq.com/[Groq] — это чрезвычайно быстрый, основанный на LPU™, движок вывода ИИ, который поддерживает различные https://console.groq.com/docs/models[AI Models], 
поддерживает `Tool/Function Calling` и предоставляет конечную точку, совместимую с `OpenAI API`.

Spring AI интегрируется с https://groq.com/[Groq], повторно используя существующий xref::api/chat/openai-chat.adoc[OpenAI] клиент. 
Для этого вам необходимо получить https://console.groq.com/keys[Groq Api Key], установить базовый URL на https://api.groq.com/openai и выбрать одну из 
предоставленных https://console.groq.com/docs/models[Groq models].

![w=800,align="center"](spring-ai-groq-integration.jpg)

> **Примечание:** API Groq не полностью совместим с API OpenAI.
Обратите внимание на следующие https://console.groq.com/docs/openai[ограничения совместимости].
Кроме того, в настоящее время Groq не поддерживает мультимодальные сообщения.

Проверьте тесты https://github.com/spring-projects/spring-ai/blob/main/models/spring-ai-openai/src/test/java/org/springframework/ai/openai/chat/proxy/GroqWithOpenAiChatModelIT.java[GroqWithOpenAiChatModelIT.java] 
для примеров использования Groq с Spring AI.

## Предварительные требования

- **Создайте API Key**:
Посетите https://console.groq.com/keys[здесь], чтобы создать API Key.
Проект Spring AI определяет свойство конфигурации с именем `spring.ai.openai.api-key`, которое вы должны установить в значение `API Key`, полученного с groq.com.

- **Установите URL Groq**:
Вы должны установить свойство `spring.ai.openai.base-url` на `+https://api.groq.com/openai+`.

- **Выберите модель Groq**:
Используйте свойство `spring.ai.openai.chat.model=<model name>`, чтобы выбрать из доступных https://console.groq.com/docs/models[Groq Models].

Вы можете установить эти свойства конфигурации в вашем файле `application.properties`:

```properties
spring.ai.openai.api-key=<your-groq-api-key>
spring.ai.openai.base-url=https://api.groq.com/openai
spring.ai.openai.chat.model=llama3-70b-8192
```

Для повышения безопасности при работе с конфиденциальной информацией, такой как API ключи, вы можете использовать язык выражений Spring (SpEL) для ссылки на пользовательские переменные окружения:

```yaml
# В application.yml
spring:
  ai:
    openai:
      api-key: ${GROQ_API_KEY}
      base-url: ${GROQ_BASE_URL}
      chat:
        model: ${GROQ_MODEL}
```

```bash
# В вашем окружении или .env файле
export GROQ_API_KEY=<your-groq-api-key>
export GROQ_BASE_URL=https://api.groq.com/openai
export GROQ_MODEL=llama3-70b-8192
```

Вы также можете установить эти конфигурации программно в коде вашего приложения:

```java
// Получите конфигурацию из безопасных источников или переменных окружения
String apiKey = System.getenv("GROQ_API_KEY");
String baseUrl = System.getenv("GROQ_BASE_URL");
String model = System.getenv("GROQ_MODEL");
```

### Добавьте репозитории и BOM

Артефакты Spring AI публикуются в Maven Central и Spring Snapshot репозиториях.
Обратитесь к разделу xref:getting-started.adoc#artifact-repositories[Artifact Repositories], чтобы добавить эти репозитории в вашу систему сборки.

Чтобы помочь с управлением зависимостями, Spring AI предоставляет BOM (bill of materials), чтобы гарантировать, что одна и та же версия Spring AI используется на протяжении всего проекта. Обратитесь к разделу xref:getting-started.adoc#dependency-management[Dependency Management], чтобы добавить BOM Spring AI в вашу систему сборки.

## Автоконфигурация

[NOTE]
====
В автоконфигурации Spring AI произошли значительные изменения в названиях артефактов модулей стартеров.
Пожалуйста, обратитесь к https://docs.spring.io/spring-ai/reference/upgrade-notes.html[заметкам об обновлении] для получения дополнительной информации.
====

Spring AI предоставляет автоконфигурацию Spring Boot для клиента OpenAI Chat.
Чтобы включить ее, добавьте следующую зависимость в файл сборки Maven `pom.xml` или Gradle `build.gradle` вашего проекта:

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

> **Совет:** Обратитесь к разделу xref:getting-started.adoc#dependency-management[Dependency Management], чтобы добавить BOM Spring AI в ваш файл сборки.

### Свойства чата

#### Свойства повторной попытки

Префикс `spring.ai.retry` используется как префикс свойства, который позволяет вам настроить механизм повторной попытки для модели чата OpenAI.

[cols="3,5,1", stripes=even]
|====
| Свойство | Описание | По умолчанию

| spring.ai.retry.max-attempts   | Максимальное количество попыток повторной попытки. |  10
| spring.ai.retry.backoff.initial-interval | Начальная продолжительность ожидания для политики экспоненциального увеличения. |  2 сек.
| spring.ai.retry.backoff.multiplier | Множитель интервала увеличения. |  5
| spring.ai.retry.backoff.max-interval | Максимальная продолжительность увеличения. |  3 мин.
| spring.ai.retry.on-client-errors | Если false, выбросьте NonTransientAiException и не пытайтесь повторить для кодов ошибок клиента `4xx` | false
| spring.ai.retry.exclude-on-http-codes | Список кодов состояния HTTP, которые не должны вызывать повторную попытку (например, для выброса NonTransientAiException). | пусто
| spring.ai.retry.on-http-codes | Список кодов состояния HTTP, которые должны вызывать повторную попытку (например, для выброса TransientAiException). | пусто
|====

#### Свойства подключения

Префикс `spring.ai.openai` используется как префикс свойства, который позволяет вам подключиться к OpenAI.

[cols="3,5,1", stripes=even]
|====
| Свойство | Описание | По умолчанию

| spring.ai.openai.base-url   | URL для подключения. Должен быть установлен на `+https://api.groq.com/openai+` | -
| spring.ai.openai.api-key    | API Key Groq           |  -
|====

#### Свойства конфигурации

[NOTE]
====
Включение и отключение автоконфигураций чата теперь настраивается через свойства верхнего уровня с префиксом `spring.ai.model.chat`.

Чтобы включить, spring.ai.model.chat=openai (по умолчанию включено)

Чтобы отключить, spring.ai.model.chat=none (или любое значение, которое не совпадает с openai)

Это изменение сделано для того, чтобы позволить конфигурацию нескольких моделей.
====

Префикс `spring.ai.openai.chat` — это префикс свойства, который позволяет вам настроить реализацию модели чата для OpenAI.

[cols="3,5,1", stripes=even]
|====
| Свойство | Описание | По умолчанию

| spring.ai.openai.chat.enabled (Удалено и больше не действительно) | Включить модель чата OpenAI.  | true
| spring.ai.openai.chat | Включить модель чата OpenAI.  | openai
| spring.ai.openai.chat.base-url   | Необязательный переопределяет spring.ai.openai.base-url для предоставления специфического для чата URL. Должен быть установлен на `+https://api.groq.com/openai+` |  -
| spring.ai.openai.chat.api-key   | Необязательный переопределяет spring.ai.openai.api-key для предоставления специфического для чата api-key |  -
| spring.ai.openai.chat.options.model | Доступные названия https://console.groq.com/docs/models[моделей] — это `llama3-8b-8192`, `llama3-70b-8192`, `mixtral-8x7b-32768`, `gemma2-9b-it`. | -
| spring.ai.openai.chat.options.temperature | Температура выборки, которая контролирует очевидную креативность сгенерированных завершений. Более высокие значения сделают вывод более случайным, в то время как более низкие значения сделают результаты более сфокусированными и детерминированными. Не рекомендуется изменять температуру и top_p для одного и того же запроса на завершение, так как взаимодействие этих двух настроек трудно предсказать. | 0.8
| spring.ai.openai.chat.options.frequencyPenalty | Число от -2.0 до 2.0. Положительные значения штрафуют новые токены на основе их существующей частоты в тексте до сих пор, уменьшая вероятность повторения той же строки дословно. | 0.0f
| spring.ai.openai.chat.options.maxTokens | Максимальное количество токенов, которые нужно сгенерировать в завершении чата. Общая длина входных токенов и сгенерированных токенов ограничена длиной контекста модели. | -
| spring.ai.openai.chat.options.n | Сколько вариантов завершения чата сгенерировать для каждого входного сообщения. Обратите внимание, что вам будет выставлен счет на основе количества сгенерированных токенов по всем вариантам. Держите n равным 1, чтобы минимизировать затраты. | 1
| spring.ai.openai.chat.options.presencePenalty | Число от -2.0 до 2.0. Положительные значения штрафуют новые токены на основе того, появляются ли они в тексте до сих пор, увеличивая вероятность того, что модель будет говорить о новых темах. | -
| spring.ai.openai.chat.options.responseFormat | Объект, указывающий формат, который модель должна выводить. Установка в `{ "type": "json_object" }` включает режим JSON, который гарантирует, что сообщение, сгенерированное моделью, является допустимым JSON. | -
| spring.ai.openai.chat.options.seed | Эта функция находится в бета-версии. Если указано, наша система постарается сделать выборку детерминированной, так что повторные запросы с тем же семенем и параметрами должны возвращать один и тот же результат. | -
| spring.ai.openai.chat.options.stop | До 4 последовательностей, при которых API прекратит генерировать дальнейшие токены. | -
| spring.ai.openai.chat.options.topP | Альтернатива выборке с температурой, называемая выборкой по ядру, где модель учитывает результаты токенов с верхней вероятностью p. Таким образом, 0.1 означает, что учитываются только токены, составляющие верхние 10% вероятностной массы. Мы обычно рекомендуем изменять это или температуру, но не оба. | -
| spring.ai.openai.chat.options.tools | Список инструментов, которые модель может вызывать. В настоящее время поддерживаются только функции в качестве инструмента. Используйте это, чтобы предоставить список функций, для которых модель может генерировать JSON-входы. | -
| spring.ai.openai.chat.options.toolChoice | Управляет тем, какая (если есть) функция вызывается моделью. none означает, что модель не будет вызывать функцию и вместо этого сгенерирует сообщение. auto означает, что модель может выбирать между генерацией сообщения или вызовом функции. Указание конкретной функции через {"type": "function", "function": {"name": "my_function"}} заставляет модель вызвать эту функцию. none — это значение по умолчанию, когда функции отсутствуют. auto — это значение по умолчанию, если функции присутствуют. | -
| spring.ai.openai.chat.options.user | Уникальный идентификатор, представляющий вашего конечного пользователя, который может помочь OpenAI отслеживать и обнаруживать злоупотребления. | -
| spring.ai.openai.chat.options.stream-usage | (Только для потоковой передачи) Установите, чтобы добавить дополнительный фрагмент с статистикой использования токенов для всего запроса. Поле `choices` для этого фрагмента является пустым массивом, и все другие фрагменты также будут включать поле использования, но со значением null. | false
| spring.ai.openai.chat.options.tool-names | Список инструментов, идентифицированных по их именам, для включения в вызов функции в одном запросе. Инструменты с этими именами должны существовать в реестре ToolCallback. | -
| spring.ai.openai.chat.options.tool-callbacks | Обратные вызовы инструментов для регистрации с ChatModel. | -
| spring.ai.openai.chat.options.internal-tool-execution-enabled | Если false, Spring AI не будет обрабатывать вызовы инструментов внутренне, а будет проксировать их клиенту. Тогда ответственность за обработку вызовов инструментов, их распределение на соответствующую функцию и возврат результатов лежит на клиенте. Если true (по умолчанию), Spring AI будет обрабатывать вызовы функций внутренне. Применимо только для моделей чата с поддержкой вызова функций | true
|====

> **Совет:** Все свойства с префиксом `spring.ai.openai.chat.options` могут быть переопределены во время выполнения, добавив специфичные для запроса <<chat-options>> к вызову `Prompt`.

## Опции времени выполнения [[chat-options]]

https://github.com/spring-projects/spring-ai/blob/main/models/spring-ai-openai/src/main/java/org/springframework/ai/openai/OpenAiChatOptions.java[OpenAiChatOptions.java] предоставляет конфигурации модели, такие как используемая модель, температура, штраф за частоту и т. д.

При запуске параметры по умолчанию могут быть настроены с помощью конструктора `OpenAiChatModel(api, options)` или свойств `spring.ai.openai.chat.options.*`.

Во время выполнения вы можете переопределить параметры по умолчанию, добавив новые, специфичные для запроса, параметры к вызову `Prompt`.
Например, чтобы переопределить модель и температуру по умолчанию для конкретного запроса:

```java
ChatResponse response = chatModel.call(
    new Prompt(
        "Сгенерируйте имена 5 известных пиратов.",
        OpenAiChatOptions.builder()
            .model("mixtral-8x7b-32768")
            .temperature(0.4)
        .build()
    ));
```

> **Совет:** В дополнение к специфичным для модели https://github.com/spring-projects/spring-ai/blob/main/models/spring-ai-openai/src/main/java/org/springframework/ai/openai/OpenAiChatOptions.java[OpenAiChatOptions] вы можете использовать переносимый [ChatOptions](https://github.com/spring-projects/spring-ai/blob/main/spring-ai-model/src/main/java/org/springframework/ai/chat/prompt/ChatOptions.java) экземпляр, созданный с помощью [ChatOptions#builder()](https://github.com/spring-projects/spring-ai/blob/main/spring-ai-model/src/main/java/org/springframework/ai/chat/prompt/DefaultChatOptionsBuilder.java).

## Вызов функций

Конечные точки API Groq поддерживают https://console.groq.com/docs/tool-use[вызов инструментов/функций], когда выбирается одна из моделей, поддерживающих Tool/Function.

> **Совет:** Проверьте инструменты https://console.groq.com/docs/tool-use[Поддерживаемые модели].

![w=800,align="center"](spring-ai-groq-functions-2.jpg)

Вы можете зарегистрировать пользовательские функции Java с вашим ChatModel и позволить предоставленной модели Groq интеллектуально выбирать вывод JSON-объекта, содержащего аргументы для вызова одной или нескольких зарегистрированных функций. 
Это мощная техника для соединения возможностей LLM с внешними инструментами и API. 

### Пример инструмента

Вот простой пример того, как использовать вызов функций Groq с Spring AI:

```java
@SpringBootApplication
public class GroqApplication {

    public static void main(String[] args) {
        SpringApplication.run(GroqApplication.class, args);
    }

    @Bean
    CommandLineRunner runner(ChatClient.Builder chatClientBuilder) {
        return args -> {
            var chatClient = chatClientBuilder.build();

            var response = chatClient.prompt()
                .user("Какова погода в Амстердаме и Париже?")
                .functions("weatherFunction") // ссылка по имени бина.
                .call()
                .content();

            System.out.println(response);
        };
    }

    @Bean
    @Description("Получить погоду в локации")
    public Function<WeatherRequest, WeatherResponse> weatherFunction() {
        return new MockWeatherService();
    }

    public static class MockWeatherService implements Function<WeatherRequest, WeatherResponse> {

        public record WeatherRequest(String location, String unit) {}
        public record WeatherResponse(double temp, String unit) {}

        @Override
        public WeatherResponse apply(WeatherRequest request) {
            double temperature = request.location().contains("Амстердам") ? 20 : 25;
            return new WeatherResponse(temperature, request.unit);
        }
    }
}
```
    
В этом примере, когда модели нужна информация о погоде, она автоматически вызовет бин `weatherFunction`, который затем может получить данные о погоде в реальном времени.
Ожидаемый ответ выглядит так: "Погода в Амстердаме в настоящее время 20 градусов Цельсия, а погода в Париже в настоящее время 25 градусов Цельсия."
    
Узнайте больше о [вызове функций OpenAI](https://docs.spring.io/spring-ai/reference/api/chat/functions/openai-chat-functions.html).

## Мультимодальный

> **Примечание:** В настоящее время API Groq не поддерживает медиа-контент.

## Пример контроллера

https://start.spring.io/[Создайте] новый проект Spring Boot и добавьте `spring-ai-starter-model-openai` в ваши зависимости pom (или gradle).

Добавьте файл `application.properties` в директорию `src/main/resources`, чтобы включить и настроить модель чата OpenAi:

```application.properties
spring.ai.openai.api-key=<GROQ_API_KEY>
spring.ai.openai.base-url=https://api.groq.com/openai
spring.ai.openai.chat.options.model=llama3-70b-8192
spring.ai.openai.chat.options.temperature=0.7
```

> **Совет:** замените `api-key` на ваши учетные данные OpenAI.

Это создаст реализацию `OpenAiChatModel`, которую вы можете внедрить в свой класс.
Вот пример простого класса `@Controller`, который использует модель чата для генерации текста.

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

## Ручная конфигурация

https://github.com/spring-projects/spring-ai/blob/main/models/spring-ai-openai/src/main/java/org/springframework/ai/openai/OpenAiChatModel.java[OpenAiChatModel] реализует `ChatModel` и `StreamingChatModel` и использует <<low-level-api>> для подключения к сервису OpenAI.

Добавьте зависимость `spring-ai-openai` в файл Maven `pom.xml` вашего проекта:

```xml
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-openai</artifactId>
</dependency>
```

или в файл сборки Gradle `build.gradle`.

```groovy
dependencies {
    implementation 'org.springframework.ai:spring-ai-openai'
}
```

> **Совет:** Обратитесь к разделу xref:getting-started.adoc#dependency-management[Dependency Management], чтобы добавить BOM Spring AI в ваш файл сборки.

Затем создайте `OpenAiChatModel` и используйте его для генерации текста:

```java
var openAiApi = new OpenAiApi("https://api.groq.com/openai", System.getenv("GROQ_API_KEY"));
var openAiChatOptions = OpenAiChatOptions.builder()
            .model("llama3-70b-8192")
            .temperature(0.4)
            .maxTokens(200)
        .build();
var chatModel = new OpenAiChatModel(this.openAiApi, this.openAiChatOptions);


ChatResponse response = this.chatModel.call(
    new Prompt("Сгенерируйте имена 5 известных пиратов."));

// Или с потоковыми ответами
Flux<ChatResponse> response = this.chatModel.stream(
    new Prompt("Сгенерируйте имена 5 известных пиратов."));
```

`OpenAiChatOptions` предоставляет информацию о конфигурации для запросов чата.
`OpenAiChatOptions.Builder` — это флюидный строитель опций.
