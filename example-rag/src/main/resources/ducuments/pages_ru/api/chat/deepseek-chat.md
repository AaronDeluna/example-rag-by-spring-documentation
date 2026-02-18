# DeepSeek Chat

Spring AI поддерживает различные языковые модели AI от DeepSeek. Вы можете взаимодействовать с языковыми моделями DeepSeek и создать многоязычного разговорного помощника на основе моделей DeepSeek.

## Предварительные требования

Вам необходимо создать API-ключ в DeepSeek для доступа к языковым моделям DeepSeek.

Создайте учетную запись на https://platform.deepseek.com/sign_up[страница регистрации DeepSeek] и сгенерируйте токен на https://platform.deepseek.com/api_keys[странице API-ключей].

Проект Spring AI определяет свойство конфигурации с именем `spring.ai.deepseek.api-key`, которое вы должны установить в значение `API Key`, полученное со страницы API-ключей.

Вы можете установить это свойство конфигурации в вашем файле `application.properties`:

```properties
spring.ai.deepseek.api-key=<ваш-api-ключ-deepseek>
```

Для повышения безопасности при работе с конфиденциальной информацией, такой как API-ключи, вы можете использовать язык выражений Spring (SpEL) для ссылки на пользовательскую переменную окружения:

```yaml
# В application.yml
spring:
  ai:
    deepseek:
      api-key: ${DEEPSEEK_API_KEY}
```

```bash
# В вашей среде или .env файле
export DEEPSEEK_API_KEY=<ваш-api-ключ-deepseek>
```

Вы также можете установить эту конфигурацию программно в коде вашего приложения:

```java
// Получите API-ключ из безопасного источника или переменной окружения
String apiKey = System.getenv("DEEPSEEK_API_KEY");
```

### Добавление репозиториев и BOM

Артефакты Spring AI публикуются в репозиториях Spring Milestone и Snapshot.
Обратитесь к разделу xref:getting-started.adoc#artifact-repositories[Артефактные репозитории], чтобы добавить эти репозитории в вашу систему сборки.

Для упрощения управления зависимостями Spring AI предоставляет BOM (bill of materials), чтобы гарантировать, что одна и та же версия Spring AI используется на протяжении всего вашего проекта. Обратитесь к разделу xref:getting-started.adoc#dependency-management[Управление зависимостями], чтобы добавить BOM Spring AI в вашу систему сборки.

## Автоконфигурация

Spring AI предоставляет автоконфигурацию Spring Boot для модели DeepSeek Chat.
Чтобы включить ее, добавьте следующую зависимость в файл Maven `pom.xml` вашего проекта:

```xml
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-starter-model-deepseek</artifactId>
</dependency>
```

или в файл Gradle `build.gradle`.

```groovy
dependencies {
    implementation 'org.springframework.ai:spring-ai-starter-model-deepseek'
}
```

> **Совет:** Обратитесь к разделу xref:getting-started.adoc#dependency-management[Управление зависимостями], чтобы добавить BOM Spring AI в ваш файл сборки.

### Свойства чата

#### Свойства повторных попыток

Префикс `spring.ai.retry` используется как префикс свойства, который позволяет вам настроить механизм повторных попыток для модели DeepSeek Chat.

| Свойство | Описание | По умолчанию |
| --- | --- | --- |

| spring.ai.retry.max-attempts | Максимальное количество попыток повторного запроса. | 10 |
| --- | --- | --- |
| spring.ai.retry.backoff.initial-interval | Начальная продолжительность ожидания для политики экспоненциального отката. | 2 сек. |
| spring.ai.retry.backoff.multiplier | Множитель интервала отката. | 5 |
| spring.ai.retry.backoff.max-interval | Максимальная продолжительность отката. | 3 мин. |
| spring.ai.retry.on-client-errors | Если false, выбрасывает NonTransientAiException и не пытается повторить запрос для кодов ошибок клиента `4xx` | false |
| spring.ai.retry.exclude-on-http-codes | Список кодов состояния HTTP, которые не должны вызывать повторный запрос (например, для выброса NonTransientAiException). | пусто |
| spring.ai.retry.on-http-codes | Список кодов состояния HTTP, которые должны вызывать повторный запрос (например, для выброса TransientAiException). | пусто |

#### Свойства подключения

Префикс `spring.ai.deepseek` используется как префикс свойства, который позволяет вам подключиться к DeepSeek.

| Свойство | Описание | По умолчанию |
| --- | --- | --- |

| spring.ai.deepseek.base-url | URL для подключения | `+https://api.deepseek.com+` |
| --- | --- | --- |
| spring.ai.deepseek.api-key | API-ключ | - |

#### Свойства конфигурации

Префикс `spring.ai.deepseek.chat` — это префикс свойства, который позволяет вам настроить реализацию модели чата для DeepSeek.

| Свойство | Описание | По умолчанию |
| --- | --- | --- |

| spring.ai.deepseek.chat.enabled | Включает модель чата DeepSeek. | true |
| --- | --- | --- |
| spring.ai.deepseek.chat.base-url | Опционально переопределяет spring.ai.deepseek.base-url для предоставления URL, специфичного для чата | `+https://api.deepseek.com/+` |
| spring.ai.deepseek.chat.api-key | Опционально переопределяет spring.ai.deepseek.api-key для предоставления API-ключа, специфичного для чата | - |
| spring.ai.deepseek.chat.completions-path | Путь к конечной точке завершений чата | `/chat/completions` |
| spring.ai.deepseek.chat.beta-prefix-path | Префиксный путь к конечной точке бета-функции | `/beta` |
| spring.ai.deepseek.chat.options.model | ID модели для использования. Вы можете использовать либо deepseek-reasoner, либо deepseek-chat. | deepseek-chat |
| spring.ai.deepseek.chat.options.frequencyPenalty | Число от -2.0 до 2.0. Положительные значения штрафуют новые токены на основе их существующей частоты в тексте, уменьшая вероятность повторения той же строки дословно. | 0.0f |
| spring.ai.deepseek.chat.options.maxTokens | Максимальное количество токенов для генерации в завершении чата. Общая длина входных токенов и сгенерированных токенов ограничена длиной контекста модели. | - |
| spring.ai.deepseek.chat.options.presencePenalty | Число от -2.0 до 2.0. Положительные значения штрафуют новые токены на основе того, появляются ли они в тексте, увеличивая вероятность модели говорить о новых темах. | 0.0f |
| spring.ai.deepseek.chat.options.stop | До 4 последовательностей, при которых API остановит генерацию дальнейших токенов. | - |
| spring.ai.deepseek.chat.options.temperature | Какую температуру выборки использовать, от 0 до 2. Более высокие значения, такие как 0.8, сделают вывод более случайным, в то время как более низкие значения, такие как 0.2, сделают его более сосредоточенным и детерминированным. Мы обычно рекомендуем изменять это или top_p, но не оба. | 1.0F |
| spring.ai.deepseek.chat.options.topP | Альтернатива выборке с температурой, называемая ядерной выборкой, где модель учитывает результаты токенов с вероятностью top_p. Таким образом, 0.1 означает, что учитываются только токены, составляющие верхние 10% вероятностной массы. Мы обычно рекомендуем изменять это или температуру, но не оба. | 1.0F |
| spring.ai.deepseek.chat.options.logprobs | Возвращать ли логарифмические вероятности выходных токенов или нет. Если true, возвращает логарифмические вероятности каждого выходного токена, возвращенного в содержимом сообщения. | - |
| spring.ai.deepseek.chat.options.topLogprobs | Целое число от 0 до 20, указывающее количество наиболее вероятных токенов, которые следует вернуть на каждой позиции токена, каждый с соответствующей логарифмической вероятностью. logprobs должен быть установлен в true, если используется этот параметр. | - |
| spring.ai.deepseek.chat.options.tool-names | Список инструментов, идентифицированных по их именам, которые следует включить для вызова функций в одном запросе. Инструменты с этими именами должны существовать в реестре ToolCallback. | - |
| spring.ai.deepseek.chat.options.tool-callbacks | Обратные вызовы инструментов для регистрации с ChatModel. | - |
| spring.ai.deepseek.chat.options.internal-tool-execution-enabled | Если false, Spring AI не будет обрабатывать вызовы инструментов внутренне, а будет проксировать их клиенту. Тогда клиент несет ответственность за обработку вызовов инструментов, их распределение на соответствующую функцию и возврат результатов. Если true (по умолчанию), Spring AI будет обрабатывать вызовы функций внутренне. Применимо только для моделей чата с поддержкой вызова функций | true |

> **Примечание:** Вы можете переопределить общие `spring.ai.deepseek.base-url` и `spring.ai.deepseek.api-key` для реализаций `ChatModel`.
Свойства `spring.ai.deepseek.chat.base-url` и `spring.ai.deepseek.chat.api-key`, если установлены, имеют приоритет над общими свойствами.
Это полезно, если вы хотите использовать разные учетные записи DeepSeek для разных моделей и разных конечных точек моделей.

> **Совет:** Все свойства с префиксом `spring.ai.deepseek.chat.options` могут быть переопределены во время выполнения, добавив специфичные для запроса <<chat-options>> в вызов `Prompt`.


[DeepSeekChatOptions.java](https://github.com/spring-projects/spring-ai/blob/main/models/spring-ai-deepseek/src/main/java/org/springframework/ai/deepseek/DeepSeekChatOptions.java) предоставляет конфигурации модели, такие как модель для использования, температура, штраф за частоту и т. д.

При запуске параметры по умолчанию могут быть настроены с помощью конструктора `DeepSeekChatModel(api, options)` или свойств `spring.ai.deepseek.chat.options.*`.

Во время выполнения вы можете переопределить параметры по умолчанию, добавив новые, специфичные для запроса параметры в вызов `Prompt`.
Например, чтобы переопределить модель и температуру по умолчанию для конкретного запроса:

```java
ChatResponse response = chatModel.call(
    new Prompt(
        "Сгенерируйте имена 5 известных пиратов. Пожалуйста, предоставьте ответ в формате JSON без каких-либо маркеров блока кода, таких как ```json```.",
        DeepSeekChatOptions.builder()
            .withModel(DeepSeekApi.ChatModel.DEEPSEEK_CHAT.getValue())
            .withTemperature(0.8f)
        .build()
    ));
```

> **Совет:** В дополнение к специфичным для модели [DeepSeekChatOptions](https://github.com/spring-projects/spring-ai/blob/main/models/spring-ai-deepseek/src/main/java/org/springframework/ai/deepseek/DeepSeekChatOptions.java) вы можете использовать переносимый [ChatOptions](https://github.com/spring-projects/spring-ai/blob/main/spring-ai-model/src/main/java/org/springframework/ai/chat/prompt/ChatOptions.java) экземпляр, созданный с помощью [ChatOptions#builder()](https://github.com/spring-projects/spring-ai/blob/main/spring-ai-model/src/main/java/org/springframework/ai/chat/prompt/DefaultChatOptionsBuilder.java).

## Пример контроллера (автоконфигурация)

https://start.spring.io/[Создайте] новый проект Spring Boot и добавьте `spring-ai-starter-model-deepseek` в зависимости вашего pom (или gradle).

Добавьте файл `application.properties` в директорию `src/main/resources`, чтобы включить и настроить модель DeepSeek Chat:

```application.properties
spring.ai.deepseek.api-key=YOUR_API_KEY
spring.ai.deepseek.chat.options.model=deepseek-chat
spring.ai.deepseek.chat.options.temperature=0.8
```

> **Совет:** Замените `api-key` на ваши учетные данные DeepSeek.

Это создаст реализацию `DeepSeekChatModel`, которую вы можете внедрить в свой класс.
Вот пример простого класса `@Controller`, который использует модель чата для генерации текста.

```java
@RestController
public class ChatController {

    private final DeepSeekChatModel chatModel;

    @Autowired
    public ChatController(DeepSeekChatModel chatModel) {
        this.chatModel = chatModel;
    }

    @GetMapping("/ai/generate")
    public Map generate(@RequestParam(value = "message", defaultValue = "Расскажи мне шутку") String message) {
        return Map.of("generation", chatModel.call(message));
    }

    @GetMapping("/ai/generateStream")
	public Flux<ChatResponse> generateStream(@RequestParam(value = "message", defaultValue = "Расскажи мне шутку") String message) {
        var prompt = new Prompt(new UserMessage(message));
        return chatModel.stream(prompt);
    }
}
```

## Завершение префикса чата
Завершение префикса чата следует API завершения чата, где пользователи предоставляют префиксное сообщение помощника, чтобы модель завершила остальную часть сообщения.

При использовании завершения префикса пользователь должен убедиться, что последнее сообщение в списке сообщений является DeepSeekAssistantMessage.

Ниже приведен полный пример кода на Java для завершения префикса чата. В этом примере мы устанавливаем префиксное сообщение помощника на "```python\n", чтобы заставить модель выводить код Python, и устанавливаем параметр stop на ['```'], чтобы предотвратить дополнительные объяснения от модели.

```java
@RestController
public class CodeGenerateController {

    private final DeepSeekChatModel chatModel;

    @Autowired
    public ChatController(DeepSeekChatModel chatModel) {
        this.chatModel = chatModel;
    }

    @GetMapping("/ai/generatePythonCode")
    public String generate(@RequestParam(value = "message", defaultValue = "Пожалуйста, напишите код быстрой сортировки") String message) {
		UserMessage userMessage = new UserMessage(message);
		Message assistantMessage = DeepSeekAssistantMessage.prefixAssistantMessage("```python\\n");
		Prompt prompt = new Prompt(List.of(userMessage, assistantMessage), ChatOptions.builder().stopSequences(List.of("```")).build());
		ChatResponse response = chatModel.call(prompt);
		return response.getResult().getOutput().getText();
    }
}
```

## Модель рассуждений (deepseek-reasoner)
`deepseek-reasoner` — это модель рассуждений, разработанная DeepSeek. Перед тем как предоставить окончательный ответ, модель сначала генерирует цепочку размышлений (CoT), чтобы повысить точность своих ответов. Наш API предоставляет пользователям доступ к содержимому CoT, сгенерированному `deepseek-reasoner`, позволяя им просматривать, отображать и обрабатывать его.

Вы можете использовать `DeepSeekAssistantMessage`, чтобы получить содержимое CoT, сгенерированное `deepseek-reasoner`.
```java
public void deepSeekReasonerExample() {
    DeepSeekChatOptions promptOptions = DeepSeekChatOptions.builder()
            .model(DeepSeekApi.ChatModel.DEEPSEEK_REASONER.getValue())
            .build();
    Prompt prompt = new Prompt("9.11 и 9.8, что больше?", promptOptions);
    ChatResponse response = chatModel.call(prompt);

    // Получите содержимое CoT, сгенерированное deepseek-reasoner, доступное только при использовании модели deepseek-reasoner
    DeepSeekAssistantMessage deepSeekAssistantMessage = (DeepSeekAssistantMessage) response.getResult().getOutput();
    String reasoningContent = deepSeekAssistantMessage.getReasoningContent();
    String text = deepSeekAssistantMessage.getText();
}
```
## Модель рассуждений Многораундный разговор
В каждом раунде разговора модель выводит CoT (reasoning_content) и окончательный ответ (content). В следующем раунде разговора CoT из предыдущих раундов не конкатенируется в контекст, как показано на следующей диаграмме:

![Мультимодальный тестовый образ, выравнивание="центр"](deepseek_r1_multiround_example.png)

Обратите внимание, что если поле reasoning_content включено в последовательность входных сообщений, API вернет ошибку 400. Поэтому вы должны удалить поле reasoning_content из ответа API перед выполнением запроса к API, как показано в примере API.
```java
public String deepSeekReasonerMultiRoundExample() {
    List<Message> messages = new ArrayList<>();
    messages.add(new UserMessage("9.11 и 9.8, что больше?"));
    DeepSeekChatOptions promptOptions = DeepSeekChatOptions.builder()
            .model(DeepSeekApi.ChatModel.DEEPSEEK_REASONER.getValue())
            .build();

    Prompt prompt = new Prompt(messages, promptOptions);
    ChatResponse response = chatModel.call(prompt);

    DeepSeekAssistantMessage deepSeekAssistantMessage = (DeepSeekAssistantMessage) response.getResult().getOutput();
    String reasoningContent = deepSeekAssistantMessage.getReasoningContent();
    String text = deepSeekAssistantMessage.getText();

    messages.add(AssistantMessage.builder().content(Objects.requireNonNull(text)).build());
    messages.add(new UserMessage("Сколько Rs в слове 'клубника'?"));
    Prompt prompt2 = new Prompt(messages, promptOptions);
    ChatResponse response2 = chatModel.call(prompt2);

    DeepSeekAssistantMessage deepSeekAssistantMessage2 = (DeepSeekAssistantMessage) response2.getResult().getOutput();
    String reasoningContent2 = deepSeekAssistantMessage2.getReasoningContent();
    return deepSeekAssistantMessage2.getText();
}
```

## Ручная конфигурация

[DeepSeekChatModel](https://github.com/spring-projects/spring-ai/blob/main/models/spring-ai-deepseek/src/main/java/org/springframework/ai/deepseek/DeepSeekChatModel.java) реализует `ChatModel` и `StreamingChatModel` и использует <<low-level-api>> для подключения к сервису DeepSeek.

Добавьте зависимость `spring-ai-deepseek` в файл Maven `pom.xml` вашего проекта:

```xml
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-deepseek</artifactId>
</dependency>
```

или в файл Gradle `build.gradle`.

```groovy
dependencies {
    implementation 'org.springframework.ai:spring-ai-deepseek'
}
```

> **Совет:** Обратитесь к разделу xref:getting-started.adoc#dependency-management[Управление зависимостями], чтобы добавить BOM Spring AI в ваш файл сборки.

Затем создайте `DeepSeekChatModel` и используйте его для генерации текста:

```java
DeepSeekApi deepSeekApi = DeepSeekApi.builder()
        .apiKey(System.getenv("DEEPSEEK_API_KEY"))
        .build();
DeepSeekChatOptions options = DeepSeekChatOptions.builder()
        .model(DeepSeekApi.ChatModel.DEEPSEEK_CHAT.getValue())
        .temperature(0.4)
        .maxTokens(200)
        .build();
DeepSeekChatModel chatModel = DeepSeekChatModel.builder()
        .deepSeekApi(deepSeekApi)
        .defaultOptions(options)
        .build();
ChatResponse response = chatModel.call(
    new Prompt("Сгенерируйте имена 5 известных пиратов."));

// Или с потоковыми ответами
Flux<ChatResponse> streamResponse = chatModel.stream(
    new Prompt("Сгенерируйте имена 5 известных пиратов."));
```

`DeepSeekChatOptions` предоставляет информацию о конфигурации для запросов чата.
`DeepSeekChatOptions.Builder` — это флюидный строитель опций.


[DeepSeekApi](https://github.com/spring-projects/spring-ai/blob/main/models/spring-ai-deepseek/src/main/java/org/springframework/ai/deepseek/api/DeepSeekApi.java) — это легковесный Java-клиент для [DeepSeek API](https://platform.deepseek.com/api-docs/).

Вот простой фрагмент, показывающий, как использовать API программно:

```java
DeepSeekApi deepSeekApi =
    new DeepSeekApi(System.getenv("DEEPSEEK_API_KEY"));

ChatCompletionMessage chatCompletionMessage =
    new ChatCompletionMessage("Привет, мир", Role.USER);

// Синхронный запрос
ResponseEntity<ChatCompletion> response = deepSeekApi.chatCompletionEntity(
    new ChatCompletionRequest(List.of(chatCompletionMessage), DeepSeekApi.ChatModel.DEEPSEEK_CHAT.getValue(), 0.7, false));

// Потоковый запрос
Flux<ChatCompletionChunk> streamResponse = deepSeekApi.chatCompletionStream(
    new ChatCompletionRequest(List.of(chatCompletionMessage), DeepSeekApi.ChatModel.DEEPSEEK_CHAT.getValue(), 0.7, true));
```

Следуйте https://github.com/spring-projects/spring-ai/blob/main/models/spring-ai-deepseek/src/main/java/org/springframework/ai/deepseek/api/DeepSeekApi.java[JavaDoc DeepSeekApi.java] для получения дополнительной информации.

#### Примеры DeepSeekApi
- Тест [DeepSeekApiIT.java](https://github.com/spring-projects/spring-ai/blob/main/models/spring-ai-deepseek/src/test/java/org/springframework/ai/deepseek/api/DeepSeekApiIT.java) предоставляет некоторые общие примеры использования легковесной библиотеки.
