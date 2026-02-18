# MiniMax Chat

Spring AI поддерживает различные языковые модели AI от MiniMax. Вы можете взаимодействовать с языковыми моделями MiniMax и создать многоязычного разговорного помощника на основе моделей MiniMax.

## Предварительные требования

Вам необходимо создать API с MiniMax для доступа к языковым моделям MiniMax.

Создайте учетную запись на https://www.minimaxi.com/login[страница регистрации MiniMax] и сгенерируйте токен на https://www.minimaxi.com/user-center/basic-information/interface-key[странице API ключей].

Проект Spring AI определяет свойство конфигурации с именем `spring.ai.minimax.api-key`, которое вы должны установить в значение `API Key`, полученное на странице API ключей.

Вы можете установить это свойство конфигурации в вашем файле `application.properties`:

```properties
spring.ai.minimax.api-key=<ваш-minimax-api-key>
```

Для повышения безопасности при работе с конфиденциальной информацией, такой как API ключи, вы можете использовать язык выражений Spring (SpEL) для ссылки на переменную окружения:

```yaml
# В application.yml
spring:
  ai:
    minimax:
      api-key: ${MINIMAX_API_KEY}
```

```bash
# В вашем окружении или .env файле
export MINIMAX_API_KEY=<ваш-minimax-api-key>
```

Вы также можете установить эту конфигурацию программно в коде вашего приложения:

```java
// Получите API ключ из безопасного источника или переменной окружения
String apiKey = System.getenv("MINIMAX_API_KEY");
```

### Добавление репозиториев и BOM

Артефакты Spring AI публикуются в репозиториях Maven Central и Spring Snapshot.
Обратитесь к разделу xref:getting-started.adoc#artifact-repositories[Артефактные репозитории], чтобы добавить эти репозитории в вашу систему сборки.

Чтобы помочь с управлением зависимостями, Spring AI предоставляет BOM (спецификация материалов), чтобы гарантировать, что одна и та же версия Spring AI используется на протяжении всего проекта. Обратитесь к разделу xref:getting-started.adoc#dependency-management[Управление зависимостями], чтобы добавить BOM Spring AI в вашу систему сборки.

## Автоконфигурация

[ПРИМЕЧАНИЕ]
====
В автоконфигурации Spring AI произошли значительные изменения в названиях артефактов модулей стартеров.
Пожалуйста, обратитесь к https://docs.spring.io/spring-ai/reference/upgrade-notes.html[заметкам об обновлении] для получения дополнительной информации.
====

Spring AI предоставляет автоконфигурацию Spring Boot для клиента MiniMax Chat.
Чтобы включить ее, добавьте следующую зависимость в файл `pom.xml` вашего проекта Maven:

```xml
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-starter-model-minimax</artifactId>
</dependency>
```

или в файл сборки Gradle `build.gradle`.

```groovy
dependencies {
    implementation 'org.springframework.ai:spring-ai-starter-model-minimax'
}
```

> **Совет:** Обратитесь к разделу xref:getting-started.adoc#dependency-management[Управление зависимостями], чтобы добавить BOM Spring AI в ваш файл сборки.

### Свойства чата

#### Свойства повторной попытки

Префикс `spring.ai.retry` используется как префикс свойства, который позволяет вам настроить механизм повторной попытки для модели чата MiniMax.

[cols="3,5,1", stripes=even]
|====
| Свойство | Описание | По умолчанию

| spring.ai.retry.max-attempts   | Максимальное количество попыток повторной попытки. |  10
| spring.ai.retry.backoff.initial-interval | Начальная продолжительность ожидания для политики экспоненциального отката. |  2 сек.
| spring.ai.retry.backoff.multiplier | Множитель интервала отката. |  5
| spring.ai.retry.backoff.max-interval | Максимальная продолжительность отката. |  3 мин.
| spring.ai.retry.on-client-errors | Если false, выбросить NonTransientAiException и не пытаться повторить для кодов ошибок клиента `4xx` | false
| spring.ai.retry.exclude-on-http-codes | Список кодов состояния HTTP, которые не должны вызывать повторную попытку (например, для выброса NonTransientAiException). | пусто
| spring.ai.retry.on-http-codes | Список кодов состояния HTTP, которые должны вызывать повторную попытку (например, для выброса TransientAiException). | пусто
|====

#### Свойства подключения

Префикс `spring.ai.minimax` используется как префикс свойства, который позволяет вам подключиться к MiniMax.

[cols="3,5,1", stripes=even]
|====
| Свойство | Описание | По умолчанию

| spring.ai.minimax.base-url   | URL для подключения |  https://api.minimax.chat
| spring.ai.minimax.api-key    | API ключ           |  -
|====

#### Свойства конфигурации

[ПРИМЕЧАНИЕ]
====
Включение и отключение автоконфигураций чата теперь настраивается через свойства верхнего уровня с префиксом `spring.ai.model.chat`.

Чтобы включить, spring.ai.model.chat=minimax (по умолчанию включено)

Чтобы отключить, spring.ai.model.chat=none (или любое значение, которое не совпадает с minimax)

Это изменение сделано для того, чтобы позволить конфигурацию нескольких моделей.
====

Префикс `spring.ai.minimax.chat` — это префикс свойства, который позволяет вам настроить реализацию модели чата для MiniMax.

[cols="3,5,1", stripes=even]
|====
| Свойство | Описание | По умолчанию

| spring.ai.minimax.chat.enabled (Удалено и больше не действительно) | Включить модель чата MiniMax.  | true
| spring.ai.model.chat | Включить модель чата MiniMax.  | minimax
| spring.ai.minimax.chat.base-url | Необязательный переопределяет spring.ai.minimax.base-url для предоставления специфического для чата URL |  https://api.minimax.chat
| spring.ai.minimax.chat.api-key | Необязательный переопределяет spring.ai.minimax.api-key для предоставления специфического для чата api-key |  -
| spring.ai.minimax.chat.options.model | Это модель чата MiniMax, которую следует использовать | `abab6.5g-chat` (модели `abab5.5-chat`, `abab5.5s-chat`, `abab6.5-chat`, `abab6.5g-chat`, `abab6.5t-chat` и `abab6.5s-chat` указывают на последние версии моделей)
| spring.ai.minimax.chat.options.maxTokens | Максимальное количество токенов для генерации в завершении чата. Общая длина входных токенов и сгенерированных токенов ограничена длиной контекста модели. | -
| spring.ai.minimax.chat.options.temperature | Температура выборки, которая контролирует очевидную креативность сгенерированных завершений. Более высокие значения сделают вывод более случайным, в то время как более низкие значения сделают результаты более сфокусированными и детерминированными. Не рекомендуется изменять температуру и top_p для одного и того же запроса на завершение, так как взаимодействие этих двух настроек трудно предсказать. | -
| spring.ai.minimax.chat.options.topP | Альтернатива выборке с температурой, называемая выборкой по ядру, где модель учитывает результаты токенов с вероятностью top_p. Таким образом, 0.1 означает, что учитываются только токены, составляющие верхние 10% вероятностной массы. Мы обычно рекомендуем изменять это или температуру, но не оба. | 1.0
| spring.ai.minimax.chat.options.n | Сколько вариантов завершения чата сгенерировать для каждого входного сообщения. Обратите внимание, что с вас будет взиматься плата на основе количества сгенерированных токенов по всем вариантам. Значение по умолчанию равно 1 и не может превышать 5. В частности, когда температура очень мала и близка к 0, мы можем вернуть только 1 результат. Если n уже установлено и >1 в это время, сервис вернет недопустимый параметр ввода (invalid_request_error) | 1
| spring.ai.minimax.chat.options.presencePenalty | Число от -2.0 до 2.0. Положительные значения штрафуют новые токены на основе того, появляются ли они в тексте до сих пор, увеличивая вероятность модели говорить о новых темах. |  0.0f
| spring.ai.minimax.chat.options.frequencyPenalty | Число от -2.0 до 2.0. Положительные значения штрафуют новые токены на основе их существующей частоты в тексте до сих пор, уменьшая вероятность модели повторять одну и ту же строку дословно. | 0.0f
| spring.ai.minimax.chat.options.stop | Модель прекратит генерировать символы, указанные в stop, и в настоящее время поддерживает только одно стоп-слово в формате ["stop_word1"] | -
| spring.ai.minimax.chat.options.tool-names | Список инструментов, идентифицированных по их именам, которые следует включить для вызова функций в одном запросе. Инструменты с этими именами должны существовать в реестре ToolCallback. | -
| spring.ai.minimax.chat.options.tool-callbacks | Обратные вызовы инструментов для регистрации с ChatModel. | -
| spring.ai.minimax.chat.options.internal-tool-execution-enabled | Если false, Spring AI не будет обрабатывать вызовы инструментов внутренне, а будет проксировать их клиенту. Тогда ответственность за обработку вызовов инструментов, их распределение на соответствующую функцию и возврат результатов лежит на клиенте. Если true (по умолчанию), Spring AI будет обрабатывать вызовы функций внутренне. Применимо только для моделей чата с поддержкой вызова функций | true
|====

> **Примечание:** Вы можете переопределить общие `spring.ai.minimax.base-url` и `spring.ai.minimax.api-key` для реализаций `ChatModel`.
Свойства `spring.ai.minimax.chat.base-url` и `spring.ai.minimax.chat.api-key`, если установлены, имеют приоритет над общими свойствами.
Это полезно, если вы хотите использовать разные учетные записи MiniMax для разных моделей и разные конечные точки моделей.

> **Совет:** Все свойства с префиксом `spring.ai.minimax.chat.options` могут быть переопределены во время выполнения, добавив специфичные для запроса <<chat-options>> в вызов `Prompt`.

## Опции времени выполнения [[chat-options]]

[MiniMaxChatOptions.java](https://github.com/spring-projects/spring-ai/blob/main/models/spring-ai-minimax/src/main/java/org/springframework/ai/minimax/MiniMaxChatOptions.java) предоставляет конфигурации модели, такие как модель для использования, температура, штраф за частоту и т. д.

При запуске параметры по умолчанию могут быть настроены с помощью конструктора `MiniMaxChatModel(api, options)` или свойств `spring.ai.minimax.chat.options.*`.

Во время выполнения вы можете переопределить параметры по умолчанию, добавив новые, специфичные для запроса, параметры в вызов `Prompt`.
Например, чтобы переопределить модель и температуру по умолчанию для конкретного запроса:

```java
ChatResponse response = chatModel.call(
    new Prompt(
        "Сгенерируйте имена 5 известных пиратов.",
        MiniMaxChatOptions.builder()
            .model(MiniMaxApi.ChatModel.ABAB_6_5_S_Chat.getValue())
            .temperature(0.5)
        .build()
    ));
```

> **Совет:** В дополнение к специфичным для модели [MiniMaxChatOptions](https://github.com/spring-projects/spring-ai/blob/main/models/spring-ai-minimax/src/main/java/org/springframework/ai/minimax/MiniMaxChatOptions.java) вы можете использовать переносимый [ChatOptions](https://github.com/spring-projects/spring-ai/blob/main/spring-ai-model/src/main/java/org/springframework/ai/chat/prompt/ChatOptions.java) экземпляр, созданный с помощью [ChatOptions#builder()](https://github.com/spring-projects/spring-ai/blob/main/spring-ai-model/src/main/java/org/springframework/ai/chat/prompt/DefaultChatOptionsBuilder.java).

## Пример контроллера

https://start.spring.io/[Создайте] новый проект Spring Boot и добавьте `spring-ai-starter-model-minimax` в зависимости вашего pom (или gradle).

Добавьте файл `application.properties` в директорию `src/main/resources`, чтобы включить и настроить модель чата MiniMax:

```application.properties
spring.ai.minimax.api-key=YOUR_API_KEY
spring.ai.minimax.chat.options.model=abab6.5g-chat
spring.ai.minimax.chat.options.temperature=0.7
```

> **Совет:** замените `api-key` на ваши учетные данные MiniMax.

Это создаст реализацию `MiniMaxChatModel`, которую вы можете внедрить в ваш класс.
Вот пример простого класса `@Controller`, который использует модель чата для генерации текста.

```java
@RestController
public class ChatController {

    private final MiniMaxChatModel chatModel;

    @Autowired
    public ChatController(MiniMaxChatModel chatModel) {
        this.chatModel = chatModel;
    }

    @GetMapping("/ai/generate")
    public Map generate(@RequestParam(value = "message", defaultValue = "Расскажи мне шутку") String message) {
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

[MiniMaxChatModel](https://github.com/spring-projects/spring-ai/blob/main/models/spring-ai-minimax/src/main/java/org/springframework/ai/minimax/MiniMaxChatModel.java) реализует `ChatModel` и `StreamingChatModel` и использует <<низкоуровневый API>> для подключения к сервису MiniMax.

Добавьте зависимость `spring-ai-minimax` в файл `pom.xml` вашего проекта Maven:

```xml
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-minimax</artifactId>
</dependency>
```

или в файл сборки Gradle `build.gradle`.

```groovy
dependencies {
    implementation 'org.springframework.ai:spring-ai-minimax'
}
```

> **Совет:** Обратитесь к разделу xref:getting-started.adoc#dependency-management[Управление зависимостями], чтобы добавить BOM Spring AI в ваш файл сборки.

Затем создайте `MiniMaxChatModel` и используйте его для генерации текста:

```java
var miniMaxApi = new MiniMaxApi(System.getenv("MINIMAX_API_KEY"));

var chatModel = new MiniMaxChatModel(this.miniMaxApi, MiniMaxChatOptions.builder()
                .model(MiniMaxApi.ChatModel.ABAB_6_5_S_Chat.getValue())
                .temperature(0.4)
                .maxTokens(200)
                .build());

ChatResponse response = this.chatModel.call(
    new Prompt("Сгенерируйте имена 5 известных пиратов."));

// Или с потоковыми ответами
Flux<ChatResponse> streamResponse = this.chatModel.stream(
    new Prompt("Сгенерируйте имена 5 известных пиратов."));
```

`MiniMaxChatOptions` предоставляет информацию о конфигурации для запросов чата.
`MiniMaxChatOptions.Builder` — это флюидный строитель опций.

### Низкоуровневый клиент MiniMaxApi [[low-level-api]]

[MiniMaxApi](https://github.com/spring-projects/spring-ai/blob/main/models/spring-ai-minimax/src/main/java/org/springframework/ai/minimax/api/MiniMaxApi.java) предоставляет легковесный Java клиент для [MiniMax API](https://www.minimaxi.com/document/guides/chat-model/V2).

Вот простой фрагмент, как использовать API программно:

```java
MiniMaxApi miniMaxApi =
    new MiniMaxApi(System.getenv("MINIMAX_API_KEY"));

ChatCompletionMessage chatCompletionMessage =
    new ChatCompletionMessage("Привет, мир", Role.USER);

// Синхронный запрос
ResponseEntity<ChatCompletion> response = this.miniMaxApi.chatCompletionEntity(
    new ChatCompletionRequest(List.of(this.chatCompletionMessage), MiniMaxApi.ChatModel.ABAB_6_5_S_Chat.getValue(), 0.7, false));

// Потоковый запрос
Flux<ChatCompletionChunk> streamResponse = this.miniMaxApi.chatCompletionStream(
    new ChatCompletionRequest(List.of(this.chatCompletionMessage), MiniMaxApi.ChatModel.ABAB_6_5_S_Chat.getValue(), 0.7, true));
```

Следуйте https://github.com/spring-projects/spring-ai/blob/main/models/spring-ai-minimax/src/main/java/org/springframework/ai/minimax/api/MiniMaxApi.java[JavaDoc MiniMaxApi.java] для получения дополнительной информации.

### Веб-поиск чата [[web-search]]

Модель MiniMax поддерживала функцию веб-поиска. Функция веб-поиска позволяет вам искать информацию в Интернете и возвращать результаты в ответе чата.

О веб-поиске следуйте https://platform.minimaxi.com/document/ChatCompletion%20v2[MiniMax ChatCompletion] для получения дополнительной информации.

Вот простой фрагмент, как использовать веб-поиск:

```java
UserMessage userMessage = new UserMessage(
        "Сколько золотых медалей США выиграли всего на Олимпийских играх 2024 года?");

List<Message> messages = new ArrayList<>(List.of(this.userMessage));

List<MiniMaxApi.FunctionTool> functionTool = List.of(MiniMaxApi.FunctionTool.webSearchFunctionTool());

MiniMaxChatOptions options = MiniMaxChatOptions.builder()
    .model(MiniMaxApi.ChatModel.ABAB_6_5_S_Chat.value)
    .tools(this.functionTool)
    .build();


// Синхронный запрос
ChatResponse response = chatModel.call(new Prompt(this.messages, this.options));

// Потоковый запрос
Flux<ChatResponse> streamResponse = chatModel.stream(new Prompt(this.messages, this.options));
```

#### Примеры MiniMaxApi
- Тест [MiniMaxApiIT.java](https://github.com/spring-projects/spring-ai/blob/main/models/spring-ai-minimax/src/test/java/org/springframework/ai/minimax/api/MiniMaxApiIT.java) предоставляет некоторые общие примеры использования легковесной библиотеки.

- Тест [MiniMaxApiToolFunctionCallIT.java](https://github.com/spring-projects/spring-ai/blob/main/models/spring-ai-minimax/src/test/java/org/springframework/ai/minimax/api/MiniMaxApiToolFunctionCallIT.java) показывает, как использовать низкоуровневый API для вызова функций инструментов.
