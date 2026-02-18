# NVIDIA Chat

https://docs.api.nvidia.com/nim/reference/llm-apis[NVIDIA LLM API] — это прокси-движок для ИИ-инференса, предлагающий широкий выбор моделей от [различных поставщиков](https://docs.api.nvidia.com/nim/reference/llm-apis#models).

Spring AI интегрируется с NVIDIA LLM API, повторно используя существующий xref::api/chat/openai-chat.adoc[OpenAI] клиент. 
Для этого необходимо установить базовый URL на `+https://integrate.api.nvidia.com+`, выбрать одну из предоставленных https://docs.api.nvidia.com/nim/reference/llm-apis#model[LLM моделей] и получить `api-key` для нее.

![w=800,align="center"](spring-ai-nvidia-llm-api-1.jpg)

> **Примечание:** NVIDIA LLM API требует явной установки параметра `max-tokens`, иначе будет выброшена ошибка сервера.

Проверьте https://github.com/spring-projects/spring-ai/blob/main/models/spring-ai-openai/src/test/java/org/springframework/ai/openai/chat/proxy/NvidiaWithOpenAiChatModelIT.java[NvidiaWithOpenAiChatModelIT.java] тесты 
для примеров использования NVIDIA LLM API с Spring AI.

## Предварительные требования

- Создайте [учетную запись NVIDIA](https://build.nvidia.com/explore/discover) с достаточным количеством кредитов.
- Выберите модель LLM для использования. Например, `meta/llama-3.1-70b-instruct`, показанную на скриншоте ниже.
- На странице выбранной модели вы можете получить `api-key` для доступа к этой модели.

![w=800,align="center"](spring-ai-nvidia-registration.jpg)

## Автоконфигурация

[NOTE]
====
В автоконфигурации Spring AI произошли значительные изменения, названия артефактов стартовых модулей.
Пожалуйста, обратитесь к https://docs.spring.io/spring-ai/reference/upgrade-notes.html[заметкам об обновлении] для получения дополнительной информации.
====

Spring AI предоставляет автоконфигурацию Spring Boot для клиента OpenAI Chat.
Чтобы включить ее, добавьте следующую зависимость в файл Maven `pom.xml` вашего проекта:

```xml
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-starter-model-openai</artifactId>
</dependency>
```

или в файл сборки Gradle `build.gradle`.

```groovy
dependencies {
    implementation 'org.springframework.ai:spring-ai-starter-model-openai'
}
```

> **Совет:** Обратитесь к разделу xref:getting-started.adoc#dependency-management[Управление зависимостями], чтобы добавить BOM Spring AI в ваш файл сборки.

### Свойства чата

#### Свойства повторной попытки

Префикс `spring.ai.retry` используется как префикс свойства, который позволяет вам настроить механизм повторной попытки для модели чата OpenAI.

[cols="3,5,1", stripes=even]
|====
| Свойство | Описание | По умолчанию

| spring.ai.retry.max-attempts   | Максимальное количество попыток повторной попытки. |  10
| spring.ai.retry.backoff.initial-interval | Начальная продолжительность ожидания для политики экспоненциального отката. |  2 сек.
| spring.ai.retry.backoff.multiplier | Множитель интервала отката. |  5
| spring.ai.retry.backoff.max-interval | Максимальная продолжительность отката. |  3 мин.
| spring.ai.retry.on-client-errors | Если false, выбрасывается NonTransientAiException, и не производится попытка повторной попытки для кодов ошибок клиента `4xx` | false
| spring.ai.retry.exclude-on-http-codes | Список кодов состояния HTTP, которые не должны вызывать повторную попытку (например, для выброса NonTransientAiException). | пусто
| spring.ai.retry.on-http-codes | Список кодов состояния HTTP, которые должны вызывать повторную попытку (например, для выброса TransientAiException). | пусто
|====

#### Свойства подключения

Префикс `spring.ai.openai` используется как префикс свойства, который позволяет вам подключиться к OpenAI.

[cols="3,5,1", stripes=even]
|====
| Свойство | Описание | По умолчанию

| spring.ai.openai.base-url   | URL для подключения. Должен быть установлен на `+https://integrate.api.nvidia.com+` | -
| spring.ai.openai.api-key    | API-ключ NVIDIA           |  -
|====

#### Свойства конфигурации

[NOTE]
====
Включение и отключение автоконфигураций чата теперь настраивается через свойства верхнего уровня с префиксом `spring.ai.model.chat`.

Чтобы включить, spring.ai.model.chat=openai (по умолчанию включено)

Чтобы отключить, spring.ai.model.chat=none (или любое значение, которое не соответствует openai)

Это изменение сделано для того, чтобы позволить конфигурацию нескольких моделей.
====

Префикс `spring.ai.openai.chat` — это префикс свойства, который позволяет вам настроить реализацию модели чата для OpenAI.

[cols="3,5,1", stripes=even]
|====
| Свойство | Описание | По умолчанию

| spring.ai.openai.chat.enabled (Удалено и больше не действительно) | Включить модель чата OpenAI.  | true
| spring.ai.model.chat | Включить модель чата OpenAI.  | openai
| spring.ai.openai.chat.base-url   | Необязательный переопределяет spring.ai.openai.base-url для предоставления специфического URL для чата. Должен быть установлен на `+https://integrate.api.nvidia.com+` |  -
| spring.ai.openai.chat.api-key   | Необязательный переопределяет spring.ai.openai.api-key для предоставления специфического API-ключа для чата |  -
| spring.ai.openai.chat.options.model | Модель [NVIDIA LLM](https://docs.api.nvidia.com/nim/reference/llm-apis#models) для использования | -
| spring.ai.openai.chat.options.temperature | Температура выборки, которая контролирует очевидную креативность сгенерированных завершений. Более высокие значения сделают вывод более случайным, в то время как более низкие значения сделают результаты более сфокусированными и детерминированными. Не рекомендуется изменять температуру и top_p для одного и того же запроса на завершение, так как взаимодействие этих двух настроек трудно предсказать. | 0.8
| spring.ai.openai.chat.options.frequencyPenalty | Число от -2.0 до 2.0. Положительные значения штрафуют новые токены на основе их существующей частоты в тексте до сих пор, уменьшая вероятность повторения той же строки дословно. | 0.0f
| spring.ai.openai.chat.options.maxTokens | Максимальное количество токенов для генерации в завершении чата. Общая длина входных токенов и сгенерированных токенов ограничена длиной контекста модели.  | ПРИМЕЧАНИЕ: NVIDIA LLM API требует явной установки параметра `max-tokens`, иначе будет выброшена ошибка сервера.
| spring.ai.openai.chat.options.n | Сколько вариантов завершения чата сгенерировать для каждого входного сообщения. Обратите внимание, что вы будете платить в зависимости от количества сгенерированных токенов по всем вариантам. Держите n равным 1, чтобы минимизировать затраты. | 1
| spring.ai.openai.chat.options.presencePenalty | Число от -2.0 до 2.0. Положительные значения штрафуют новые токены на основе того, появляются ли они в тексте до сих пор, увеличивая вероятность модели говорить о новых темах. | -
| spring.ai.openai.chat.options.responseFormat | Объект, указывающий формат, который модель должна выводить. Установка в `{ "type": "json_object" }` включает режим JSON, который гарантирует, что сообщение, сгенерированное моделью, является допустимым JSON.| -
| spring.ai.openai.chat.options.seed | Эта функция находится в бета-версии. Если указано, наша система постарается сделать выборку детерминированной, так что повторные запросы с тем же семенем и параметрами должны возвращать один и тот же результат. | -
| spring.ai.openai.chat.options.stop | До 4 последовательностей, при которых API остановит генерацию дальнейших токенов. | -
| spring.ai.openai.chat.options.topP | Альтернатива выборке с температурой, называемая ядерной выборкой, где модель учитывает результаты токенов с вероятностной массой top_p. Таким образом, 0.1 означает, что учитываются только токены, составляющие верхние 10% вероятностной массы. Мы обычно рекомендуем изменять это или температуру, но не оба. | -
| spring.ai.openai.chat.options.tools | Список инструментов, которые модель может вызывать. В настоящее время поддерживаются только функции в качестве инструмента. Используйте это, чтобы предоставить список функций, для которых модель может генерировать JSON-входы. | -
| spring.ai.openai.chat.options.toolChoice | Управляет тем, какая (если есть) функция вызывается моделью. none означает, что модель не будет вызывать функцию и вместо этого сгенерирует сообщение. auto означает, что модель может выбирать между генерацией сообщения или вызовом функции. Указание конкретной функции через {"type: "function", "function": {"name": "my_function"}} заставляет модель вызывать эту функцию. none — это значение по умолчанию, когда функции отсутствуют. auto — это значение по умолчанию, если функции присутствуют. | -
| spring.ai.openai.chat.options.user | Уникальный идентификатор, представляющий вашего конечного пользователя, который может помочь OpenAI отслеживать и обнаруживать злоупотребления. | -
| spring.ai.openai.chat.options.stream-usage | (Только для потоковой передачи) Установите, чтобы добавить дополнительный фрагмент с статистикой использования токенов для всего запроса. Поле `choices` для этого фрагмента является пустым массивом, и все другие фрагменты также будут включать поле использования, но со значением null. | false
| spring.ai.openai.chat.options.tool-names | Список инструментов, идентифицированных по их именам, для включения в вызов функции в одном запросе. Инструменты с этими именами должны существовать в реестре ToolCallback. | -
| spring.ai.openai.chat.options.tool-callbacks | Обратные вызовы инструментов для регистрации с ChatModel. | -
| spring.ai.openai.chat.options.internal-tool-execution-enabled | Если false, Spring AI не будет обрабатывать вызовы инструментов внутренне, а будет проксировать их клиенту. Тогда ответственность за обработку вызовов инструментов, их распределение на соответствующую функцию и возврат результатов лежит на клиенте. Если true (по умолчанию), Spring AI будет обрабатывать вызовы функций внутренне. Применимо только для моделей чата с поддержкой вызова функций | true
|====

> **Совет:** Все свойства с префиксом `spring.ai.openai.chat.options` могут быть переопределены во время выполнения, добавив специфические для запроса <<chat-options>> в вызов `Prompt`.

## Опции времени выполнения [[chat-options]]

Файл https://github.com/spring-projects/spring-ai/blob/main/models/spring-ai-openai/src/main/java/org/springframework/ai/openai/OpenAiChatOptions.java[OpenAiChatOptions.java] предоставляет конфигурации модели, такие как используемая модель, температура, штраф за частоту и т. д.

При запуске параметры по умолчанию могут быть настроены с помощью конструктора `OpenAiChatModel(api, options)` или свойств `spring.ai.openai.chat.options.*`.

Во время выполнения вы можете переопределить параметры по умолчанию, добавив новые, специфические для запроса, параметры в вызов `Prompt`.
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

> **Совет:** В дополнение к специфическим для модели https://github.com/spring-projects/spring-ai/blob/main/models/spring-ai-openai/src/main/java/org/springframework/ai/openai/OpenAiChatOptions.java[OpenAiChatOptions] вы можете использовать переносимый [ChatOptions](https://github.com/spring-projects/spring-ai/blob/main/spring-ai-model/src/main/java/org/springframework/ai/chat/prompt/ChatOptions.java) экземпляр, созданный с помощью [ChatOptions#builder()](https://github.com/spring-projects/spring-ai/blob/main/spring-ai-model/src/main/java/org/springframework/ai/chat/prompt/DefaultChatOptionsBuilder.java).

## Вызов функций

NVIDIA LLM API поддерживает вызов инструментов/функций при выборе модели, которая это поддерживает.

![w=800,align="center"](spring-ai-nvidia-function-calling.jpg)

Вы можете зарегистрировать пользовательские Java-функции с вашим ChatModel и позволить предоставленной модели интеллектуально выбирать вывод JSON-объекта, содержащего аргументы для вызова одной или нескольких зарегистрированных функций. 
Это мощная техника для соединения возможностей LLM с внешними инструментами и API. 

### Пример инструмента

Вот простой пример того, как использовать вызов функций NVIDIA LLM API с Spring AI:

```application.properties
spring.ai.openai.api-key=${NVIDIA_API_KEY}
spring.ai.openai.base-url=https://integrate.api.nvidia.com
spring.ai.openai.chat.options.model=meta/llama-3.1-70b-instruct
spring.ai.openai.chat.options.max-tokens=2048
```

```java
@SpringBootApplication
public class NvidiaLlmApplication {

    public static void main(String[] args) {
        SpringApplication.run(NvidiaLlmApplication.class, args);
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
    @Description("Получить погоду в местоположении")
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
    
Читать далее о [Вызове функций OpenAI](https://docs.spring.io/spring-ai/reference/api/chat/functions/openai-chat-functions.html).


## Пример контроллера

https://start.spring.io/[Создайте] новый проект Spring Boot и добавьте `spring-ai-starter-model-openai` в зависимости вашего pom (или gradle).

Добавьте файл `application.properties` в директорию `src/main/resources`, чтобы включить и настроить модель чата OpenAi:

```application.properties
spring.ai.openai.api-key=${NVIDIA_API_KEY}
spring.ai.openai.base-url=https://integrate.api.nvidia.com
spring.ai.openai.chat.options.model=meta/llama-3.1-70b-instruct

# NVIDIA LLM API не поддерживает встраивания, поэтому мы должны отключить его.
spring.ai.openai.embedding.enabled=false

# NVIDIA LLM API требует, чтобы этот параметр был установлен явно, иначе будет выброшена ошибка сервера.
spring.ai.openai.chat.options.max-tokens=2048
```

> **Совет:** замените `api-key` на ваши учетные данные NVIDIA.

> **Примечание:** NVIDIA LLM API требует явной установки параметра `max-token`, иначе будет выброшена ошибка сервера.


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
