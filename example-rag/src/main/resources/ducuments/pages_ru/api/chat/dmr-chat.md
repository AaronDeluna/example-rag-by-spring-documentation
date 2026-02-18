# Docker Model Runner Chat

https://docs.docker.com/desktop/features/model-runner/[Docker Model Runner] — это движок вывода ИИ, предлагающий широкий выбор моделей от [различных поставщиков](https://hub.docker.com/u/ai).

Spring AI интегрируется с Docker Model Runner, повторно используя существующий xref::api/chat/openai-chat.adoc[OpenAI] поддерживаемый `ChatClient`.
Для этого установите базовый URL на `http://localhost:12434/engines` и выберите одну из предоставленных https://hub.docker.com/u/ai[LLM моделей].

Проверьте https://github.com/spring-projects/spring-ai/blob/main/models/spring-ai-openai/src/test/java/org/springframework/ai/openai/chat/proxy/DockerModelRunnerWithOpenAiChatModelIT.java[DockerModelRunnerWithOpenAiChatModelIT.java] тесты для примеров использования Docker Model Runner с Spring AI.

## Предварительные требования

- Скачайте Docker Desktop для Mac 4.40.0.

Выберите один из следующих вариантов для включения Model Runner:

Вариант 1:

- Включите Model Runner `docker desktop enable model-runner --tcp 12434`.
- Установите базовый URL на `http://localhost:12434/engines`

Вариант 2:

- Включите Model Runner `docker desktop enable model-runner`.
- Используйте Testcontainers и установите базовый URL следующим образом:

```java
@Container
private static final DockerModelRunnerContainer DMR = new DockerModelRunnerContainer("alpine/socat:1.7.4.3-r0");

@Bean
public OpenAiApi chatCompletionApi() {
	var baseUrl = DMR.getOpenAIEndpoint();
	return OpenAiApi.builder().baseUrl(baseUrl).apiKey("test").build();
}
```

Вы можете узнать больше о Docker Model Runner, прочитав https://www.docker.com/blog/run-llms-locally/[блог о запуске LLM локально с помощью Docker].

## Автоконфигурация

[NOTE]
====
Идентификаторы артефактов для стартовых модулей Spring AI были переименованы с версии 1.0.0.M7. Имена зависимостей теперь должны следовать обновленным шаблонам именования для моделей, векторных хранилищ и стартовых модулей MCP.
Пожалуйста, обратитесь к https://docs.spring.io/spring-ai/reference/upgrade-notes.html[заметкам об обновлении] для получения дополнительной информации.
====

Spring AI предоставляет автоконфигурацию Spring Boot для OpenAI Chat Client.
Чтобы включить ее, добавьте следующую зависимость в файл Maven `pom.xml` вашего проекта:

```xml
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-starter-model-openai</artifactId>
</dependency>
```

или добавьте следующее в файл сборки Gradle `build.gradle`.

```groovy
dependencies {
    implementation 'org.springframework.ai:spring-ai-starter-model-openai'
}
```

> **Совет:** Обратитесь к разделу xref:getting-started.adoc#dependency-management[Управление зависимостями], чтобы добавить BOM Spring AI в ваш файл сборки.

### Свойства чата

#### Свойства повторных попыток

Префикс `spring.ai.retry` используется как префикс свойства, который позволяет вам настроить механизм повторных попыток для модели чата OpenAI.

| Свойство | Описание | По умолчанию |
| --- | --- | --- |

| spring.ai.retry.max-attempts | Максимальное количество попыток повторного запроса. | 10 |
| --- | --- | --- |
| spring.ai.retry.backoff.initial-interval | Начальная продолжительность ожидания для политики экспоненциального отката. | 2 сек. |
| spring.ai.retry.backoff.multiplier | Множитель интервала отката. | 5 |
| spring.ai.retry.backoff.max-interval | Максимальная продолжительность отката. | 3 мин. |
| spring.ai.retry.on-client-errors | Если false, выбросить NonTransientAiException и не пытаться повторить запрос для кодов ошибок клиента `4xx` | false |
| spring.ai.retry.exclude-on-http-codes | Список кодов состояния HTTP, которые не должны вызывать повторную попытку (например, для выброса NonTransientAiException). | пусто |
| spring.ai.retry.on-http-codes | Список кодов состояния HTTP, которые должны вызывать повторную попытку (например, для выброса TransientAiException). | пусто |

#### Свойства подключения

Префикс `spring.ai.openai` используется как префикс свойства, который позволяет вам подключиться к OpenAI.

| Свойство | Описание | По умолчанию |
| --- | --- | --- |

| spring.ai.openai.base-url | URL для подключения. Должен быть установлен на `https://hub.docker.com/u/ai` | - |
| --- | --- | --- |
| spring.ai.openai.api-key | Любая строка | - |

#### Свойства конфигурации

[NOTE]
====
Включение и отключение автоконфигураций чата теперь осуществляется через свойства верхнего уровня с префиксом `spring.ai.model.chat`.

Чтобы включить, `spring.ai.model.chat=openai` (по умолчанию включено)

Чтобы отключить, `spring.ai.model.chat=none` (или любое значение, которое не соответствует openai)

Это изменение позволяет настраивать несколько моделей в вашем приложении.
====

Префикс `spring.ai.openai.chat` — это префикс свойства, который позволяет вам настроить реализацию модели чата для OpenAI.

| Свойство | Описание | По умолчанию |
| --- | --- | --- |

| spring.ai.openai.chat.enabled (Удалено и больше не актуально) | Включить модель чата OpenAI. | true |
| --- | --- | --- |
| spring.ai.model.chat | Включить модель чата OpenAI. | openai |
| spring.ai.openai.chat.base-url | Необязательный, переопределяет `spring.ai.openai.base-url`, чтобы предоставить URL, специфичный для чата. Должен быть установлен на `http://localhost:12434/engines` | - |
| spring.ai.openai.chat.api-key | Необязательный, переопределяет spring.ai.openai.api-key, чтобы предоставить специфичный для чата api-key | - |
| spring.ai.openai.chat.options.model | Модель [LLM](https://hub.docker.com/u/ai), которую следует использовать | - |
| spring.ai.openai.chat.options.temperature | Температура выборки, которая контролирует очевидную креативность сгенерированных завершений. Более высокие значения сделают вывод более случайным, в то время как более низкие значения сделают результаты более сосредоточенными и детерминированными. Не рекомендуется изменять температуру и top_p для одного и того же запроса на завершение, так как взаимодействие этих двух настроек трудно предсказать. | 0.8 |
| spring.ai.openai.chat.options.frequencyPenalty | Число от -2.0 до 2.0. Положительные значения штрафуют новые токены на основе их существующей частоты в тексте до сих пор, уменьшая вероятность повторения той же строки дословно. | 0.0f |
| spring.ai.openai.chat.options.maxTokens | Максимальное количество токенов, которые нужно сгенерировать в завершении чата. Общая длина входных токенов и сгенерированных токенов ограничена длиной контекста модели. | - |
| spring.ai.openai.chat.options.n | Сколько вариантов завершения чата сгенерировать для каждого входного сообщения. Обратите внимание, что вы будете платить в зависимости от количества сгенерированных токенов по всем вариантам. Держите n равным 1, чтобы минимизировать затраты. | 1 |
| spring.ai.openai.chat.options.presencePenalty | Число от -2.0 до 2.0. Положительные значения штрафуют новые токены на основе того, появляются ли они в тексте до сих пор, увеличивая вероятность модели говорить о новых темах. | - |
| spring.ai.openai.chat.options.responseFormat | Объект, указывающий формат, который модель должна выводить. Установка в `{ "type": "json_object" }` включает режим JSON, который гарантирует, что сообщение, сгенерированное моделью, является допустимым JSON. | - |
| spring.ai.openai.chat.options.seed | Эта функция находится в бета-версии. Если указано, наша система постарается выполнить выборку детерминированно, так что повторные запросы с тем же семенем и параметрами должны возвращать один и тот же результат. | - |
| spring.ai.openai.chat.options.stop | До 4 последовательностей, при которых API прекратит генерировать дальнейшие токены. | - |
| spring.ai.openai.chat.options.topP | Альтернатива выборке с температурой, называемая выборкой по ядру, где модель учитывает результаты токенов с вероятностью top_p. Таким образом, 0.1 означает, что учитываются только токены, составляющие верхние 10% вероятностной массы. Мы обычно рекомендуем изменять это или температуру, но не оба. | - |
| spring.ai.openai.chat.options.tools | Список инструментов, которые модель может вызывать. В настоящее время поддерживаются только функции в качестве инструмента. Используйте это, чтобы предоставить список функций, для которых модель может генерировать JSON-входы. | - |
| spring.ai.openai.chat.options.toolChoice | Управляет тем, какая (если есть) функция вызывается моделью. none означает, что модель не будет вызывать функцию и вместо этого сгенерирует сообщение. auto означает, что модель может выбирать между генерацией сообщения или вызовом функции. Указание конкретной функции через {"type: "function", "function": {"name": "my_function"}} заставляет модель вызвать эту функцию. none — это значение по умолчанию, когда функции отсутствуют. auto — это значение по умолчанию, если функции присутствуют. | - |
| spring.ai.openai.chat.options.user | Уникальный идентификатор, представляющий вашего конечного пользователя, который может помочь OpenAI отслеживать и обнаруживать злоупотребления. | - |
| spring.ai.openai.chat.options.stream-usage | (Только для потоковой передачи) Установите, чтобы добавить дополнительный фрагмент с статистикой использования токенов для всего запроса. Поле `choices` для этого фрагмента является пустым массивом, и все остальные фрагменты также будут включать поле использования, но со значением null. | false |
| spring.ai.openai.chat.options.tool-names | Список инструментов, идентифицированных по их именам, для включения в вызов функции в одном запросе. Инструменты с этими именами должны существовать в реестре ToolCallback. | - |
| spring.ai.openai.chat.options.tool-callbacks | Обратные вызовы инструментов для регистрации с ChatModel. | - |
| spring.ai.openai.chat.options.internal-tool-execution-enabled | Если false, Spring AI не будет обрабатывать вызовы инструментов внутренне, а будет проксировать их клиенту. Тогда ответственность за обработку вызовов инструментов, их распределение на соответствующую функцию и возврат результатов лежит на клиенте. Если true (по умолчанию), Spring AI будет обрабатывать вызовы функций внутренне. Применимо только для моделей чата с поддержкой вызова функций | true |

> **Совет:** Все свойства с префиксом `spring.ai.openai.chat.options` могут быть переопределены во время выполнения, добавив специфичные для запроса <<chat-options>> к вызову `Prompt`.


Файл https://github.com/spring-projects/spring-ai/blob/main/models/spring-ai-openai/src/main/java/org/springframework/ai/openai/OpenAiChatOptions.java[OpenAiChatOptions.java] предоставляет конфигурации модели, такие как используемая модель, температура, штраф за частоту и т. д.

При запуске параметры по умолчанию могут быть настроены с помощью конструктора `OpenAiChatModel(api, options)` или свойств `spring.ai.openai.chat.options.*`.

Во время выполнения вы можете переопределить параметры по умолчанию, добавив новые, специфичные для запроса, параметры к вызову `Prompt`.
Например, чтобы переопределить модель и температуру по умолчанию для конкретного запроса:

```java
ChatResponse response = chatModel.call(
    new Prompt(
        "Сгенерируйте имена 5 известных пиратов.",
        OpenAiChatOptions.builder()
            .model("ai/gemma3:4B-F16")
        .build()
    ));
```

> **Совет:** В дополнение к специфичным для модели https://github.com/spring-projects/spring-ai/blob/main/models/spring-ai-openai/src/main/java/org/springframework/ai/openai/OpenAiChatOptions.java[OpenAiChatOptions] вы можете использовать переносимый [ChatOptions](https://github.com/spring-projects/spring-ai/blob/main/spring-ai-model/src/main/java/org/springframework/ai/chat/prompt/ChatOptions.java) экземпляр, созданный с помощью [ChatOptions#builder()](https://github.com/spring-projects/spring-ai/blob/main/spring-ai-model/src/main/java/org/springframework/ai/chat/prompt/DefaultChatOptionsBuilder.java).

## Вызов функций

Docker Model Runner поддерживает вызов инструментов/функций при выборе модели, которая это поддерживает.

Вы можете зарегистрировать пользовательские функции Java с вашим ChatModel и позволить предоставленной модели разумно выбирать выводить JSON-объект, содержащий аргументы для вызова одной или нескольких зарегистрированных функций.
Это мощная техника для соединения возможностей LLM с внешними инструментами и API.

### Пример инструмента

Вот простой пример того, как использовать вызов функций Docker Model Runner с Spring AI:

```application.properties
spring.ai.openai.api-key=test
spring.ai.openai.base-url=http://localhost:12434/engines
spring.ai.openai.chat.options.model=ai/gemma3:4B-F16
```

```java
@SpringBootApplication
public class DockerModelRunnerLlmApplication {

    public static void main(String[] args) {
        SpringApplication.run(DockerModelRunnerLlmApplication.class, args);
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
Ожидаемый ответ: "Погода в Амстердаме в настоящее время 20 градусов Цельсия, а погода в Париже в настоящее время 25 градусов Цельсия."

Читать далее о [вызове функций OpenAI](https://docs.spring.io/spring-ai/reference/api/chat/functions/openai-chat-functions.html).


## Пример контроллера

https://start.spring.io/[Создайте] новый проект Spring Boot и добавьте `spring-ai-starter-model-openai` в ваши зависимости pom (или gradle).

Добавьте файл `application.properties` в директорию `src/main/resources`, чтобы включить и настроить модель чата OpenAi:

```application.properties
spring.ai.openai.api-key=test
spring.ai.openai.base-url=http://localhost:12434/engines
spring.ai.openai.chat.options.model=ai/gemma3:4B-F16

# Docker Model Runner не поддерживает встраивания, поэтому мы должны отключить их.
spring.ai.openai.embedding.enabled=false
```


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
