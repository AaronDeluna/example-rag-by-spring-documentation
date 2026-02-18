# ZhiPu AI Chat

Spring AI поддерживает различные языковые модели ИИ от ZhiPu AI. Вы можете взаимодействовать с языковыми моделями ZhiPu AI и создать многоязычного разговорного помощника на основе моделей ZhiPuAI.

Если вы не говорите по-китайски, вы можете посетить международный сайт ZhiPuAI https://z.ai/model-api[Z.ai]

## Предварительные требования

Вам необходимо создать API с ZhiPuAI, чтобы получить доступ к языковым моделям ZhiPu AI.

Создайте учетную запись на https://open.bigmodel.cn/login[Страница регистрации ZhiPu AI] (или https://chat.z.ai/auth[Страница регистрации ZhiPuAI]) и сгенерируйте токен на https://open.bigmodel.cn/usercenter/apikeys[Страница API ключей] (или https://z.ai/manage-apikey/apikey-list[Страница API ключей ZhiPuAI]).

Проект Spring AI определяет свойство конфигурации с именем `spring.ai.zhipuai.api-key`, которое вы должны установить в значение `API Key`, полученное со страницы API ключей.

Вы можете установить это свойство конфигурации в вашем файле `application.properties`:

```properties
spring.ai.zhipuai.api-key=<ваш-zhipuai-api-key>
```

Для повышения безопасности при работе с конфиденциальной информацией, такой как API ключи, вы можете использовать язык выражений Spring (SpEL) для ссылки на пользовательскую переменную окружения:

```yaml
# В application.yml
spring:
  ai:
    zhipuai:
      api-key: ${ZHIPUAI_API_KEY}
```

```bash
# В вашей среде или .env файле
export ZHIPUAI_API_KEY=<ваш-zhipuai-api-key>
```

Вы также можете установить эту конфигурацию программно в коде вашего приложения:

```java
// Получите API ключ из безопасного источника или переменной окружения
String apiKey = System.getenv("ZHIPUAI_API_KEY");
```

### Добавление репозиториев и BOM

Артефакты Spring AI публикуются в репозиториях Maven Central и Spring Snapshot.
Обратитесь к разделу xref:getting-started.adoc#artifact-repositories[Артефакты репозиториев], чтобы добавить эти репозитории в вашу систему сборки.

Чтобы помочь с управлением зависимостями, Spring AI предоставляет BOM (спецификация материалов), чтобы гарантировать, что одна и та же версия Spring AI используется на протяжении всего проекта. Обратитесь к разделу xref:getting-started.adoc#dependency-management[Управление зависимостями], чтобы добавить BOM Spring AI в вашу систему сборки.

## Автоконфигурация

[ПРИМЕЧАНИЕ]
====
В автоконфигурации Spring AI произошли значительные изменения в названиях артефактов модулей стартеров.
Пожалуйста, обратитесь к https://docs.spring.io/spring-ai/reference/upgrade-notes.html[заметкам об обновлении] для получения дополнительной информации.
====

Spring AI предоставляет автоконфигурацию Spring Boot для клиента ZhiPuAI Chat.
Чтобы включить ее, добавьте следующую зависимость в файл `pom.xml` вашего проекта Maven:

```xml
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-starter-model-zhipuai</artifactId>
</dependency>
```

или в файл сборки Gradle `build.gradle`.

```groovy
dependencies {
    implementation 'org.springframework.ai:spring-ai-starter-model-zhipuai'
}
```

> **Совет:** Обратитесь к разделу xref:getting-started.adoc#dependency-management[Управление зависимостями], чтобы добавить BOM Spring AI в ваш файл сборки.

### Свойства чата

#### Свойства повторной попытки

Префикс `spring.ai.retry` используется как префикс свойства, который позволяет вам настроить механизм повторной попытки для модели чата ZhiPu AI.

| Свойство | Описание | По умолчанию |
| --- | --- | --- |

| spring.ai.retry.max-attempts | Максимальное количество попыток повторной попытки. | 10 |
| --- | --- | --- |
| spring.ai.retry.backoff.initial-interval | Начальная продолжительность ожидания для политики экспоненциального отката. | 2 сек. |
| spring.ai.retry.backoff.multiplier | Множитель интервала отката. | 5 |
| spring.ai.retry.backoff.max-interval | Максимальная продолжительность отката. | 3 мин. |
| spring.ai.retry.on-client-errors | Если false, выбросить NonTransientAiException и не пытаться повторить для кодов ошибок клиента `4xx` | false |
| spring.ai.retry.exclude-on-http-codes | Список кодов состояния HTTP, которые не должны вызывать повторную попытку (например, для выброса NonTransientAiException). | пусто |
| spring.ai.retry.on-http-codes | Список кодов состояния HTTP, которые должны вызывать повторную попытку (например, для выброса TransientAiException). | пусто |

#### Свойства подключения

Префикс `spring.ai.zhipuai` используется как префикс свойства, который позволяет вам подключиться к ZhiPuAI.

| Свойство | Описание | По умолчанию |
| --- | --- | --- |

| spring.ai.zhipuai.base-url | URL для подключения к API ZhiPuAI. + |
| --- | --- |
Если вы используете платформу Z.ai, вам нужно установить его на `https://api.z.ai/api/paas[https://api.z.ai/api/paas]`. |  `https://open.bigmodel.cn/api/paas[https://open.bigmodel.cn/api/paas]`
| spring.ai.zhipuai.api-key | API ключ | - |
| --- | --- | --- |

#### Свойства конфигурации

[ПРИМЕЧАНИЕ]
====
Включение и отключение автоконфигураций чата теперь настраивается через свойства верхнего уровня с префиксом `spring.ai.model.chat`.

Чтобы включить, spring.ai.model.chat=zhipuai (по умолчанию включено)

Чтобы отключить, spring.ai.model.chat=none (или любое значение, которое не совпадает с zhipuai)

Это изменение сделано для того, чтобы позволить конфигурацию нескольких моделей.
====

Префикс `spring.ai.zhipuai.chat` — это префикс свойства, который позволяет вам настроить реализацию модели чата для ZhiPuAI.

| Свойство | Описание | По умолчанию |
| --- | --- | --- |

| spring.ai.zhipuai.chat.enabled (Удалено и больше не действительно) | Включить модель чата ZhiPuAI. | true |
| --- | --- | --- |
| spring.ai.model.chat | Включить модель чата ZhiPuAI. | zhipuai |
| spring.ai.zhipuai.chat.base-url | Необязательный переопределяет spring.ai.zhipuai.base-url для предоставления специфического URL для чата. Если вы используете платформу Z.ai, вам нужно установить его на `https://api.z.ai/api/paas[https://api.z.ai/api/paas]`. | `https://open.bigmodel.cn/api/paas[https://open.bigmodel.cn/api/paas]` |
| spring.ai.zhipuai.chat.api-key | Необязательный переопределяет spring.ai.zhipuai.api-key для предоставления специфического для чата api-key. | - |
| spring.ai.zhipuai.chat.options.model | Это модель чата ZhiPuAI, которую следует использовать. Вы можете выбрать между моделями, такими как: `glm-4.6`, `glm-4.5`, `glm-4-air` и другими. | `glm-4-air` |
| spring.ai.zhipuai.chat.options.maxTokens | Максимальное количество токенов, которые нужно сгенерировать в завершении чата. Общая длина входных токенов и сгенерированных токенов ограничена длиной контекста модели. | - |
| spring.ai.zhipuai.chat.options.temperature | Какую температуру выборки использовать, от 0 до 1. Более высокие значения, такие как 0.8, сделают вывод более случайным, в то время как более низкие значения, такие как 0.2, сделают его более сосредоточенным и детерминированным. Мы обычно рекомендуем изменять это или top_p, но не оба. | - |
| spring.ai.zhipuai.chat.options.topP | Альтернатива выборке с температурой, называемая выборкой по ядру, где модель учитывает результаты токенов с вероятностью top_p. Таким образом, 0.1 означает, что учитываются только токены, составляющие верхние 10% вероятностной массы. Мы обычно рекомендуем изменять это или температуру, но не оба. | 1.0 |
| spring.ai.zhipuai.chat.options.stop | Модель прекратит генерировать символы, указанные в stop, и в настоящее время поддерживает только одно стоп-слово в формате ["stop_word1"] | - |
| spring.ai.zhipuai.chat.options.user | Уникальный идентификатор, представляющий вашего конечного пользователя, который может помочь ZhiPuAI отслеживать и обнаруживать злоупотребления. | - |
| spring.ai.zhipuai.chat.options.requestId | Параметр передается клиентом и должен обеспечивать уникальность. Он используется для различения уникального идентификатора для каждого запроса. Если клиент не предоставляет его, платформа сгенерирует его по умолчанию. | - |
| spring.ai.zhipuai.chat.options.doSample | Когда do_sample установлено в true, стратегия выборки включена. Если do_sample равно false, параметры стратегии выборки температура и top_p не будут действовать. | true |
| spring.ai.zhipuai.chat.options.response-format.type | Управляет форматом вывода модели. Установите на `json_object`, чтобы гарантировать, что сообщение является допустимым объектом JSON. Доступные варианты: `text` или `json_object`. | - |
| spring.ai.zhipuai.chat.options.thinking.type | Управляет тем, включать ли цепочку размышлений большой модели. Доступные варианты: `enabled` или `disabled`. | - |
| spring.ai.zhipuai.chat.options.tool-names | Список инструментов, идентифицированных по их именам, которые следует включить для вызова функций в одном запросе. Инструменты с этими именами должны существовать в реестре ToolCallback. | - |
| spring.ai.zhipuai.chat.options.tool-callbacks | Обратные вызовы инструментов для регистрации с ChatModel. | - |
| spring.ai.zhipuai.chat.options.internal-tool-execution-enabled | Если false, Spring AI не будет обрабатывать вызовы инструментов внутренне, а будет проксировать их клиенту. Тогда клиент несет ответственность за обработку вызовов инструментов, их распределение на соответствующую функцию и возврат результатов. Если true (по умолчанию), Spring AI будет обрабатывать вызовы функций внутренне. Применимо только для моделей чата с поддержкой вызова функций | true |

> **Примечание:** Вы можете переопределить общие `spring.ai.zhipuai.base-url` и `spring.ai.zhipuai.api-key` для реализаций `ChatModel`.
Свойства `spring.ai.zhipuai.chat.base-url` и `spring.ai.zhipuai.chat.api-key`, если установлены, имеют приоритет над общими свойствами.
Это полезно, если вы хотите использовать разные учетные записи ZhiPuAI для разных моделей и разные конечные точки моделей.

> **Совет:** Все свойства с префиксом `spring.ai.zhipuai.chat.options` могут быть переопределены во время выполнения, добавив специфичные для запроса <<chat-options>> в вызов `Prompt`.


[ZhiPuAiChatOptions.java](https://github.com/spring-projects/spring-ai/blob/main/models/spring-ai-zhipuai/src/main/java/org/springframework/ai/zhipuai/ZhiPuAiChatOptions.java) предоставляет конфигурации модели, такие как модель для использования, температура, штраф за частоту и т. д.

При запуске параметры по умолчанию могут быть настроены с помощью конструктора `ZhiPuAiChatModel(api, options)` или свойств `spring.ai.zhipuai.chat.options.*`.

Во время выполнения вы можете переопределить параметры по умолчанию, добавив новые, специфичные для запроса, параметры в вызов `Prompt`.
Например, чтобы переопределить модель и температуру по умолчанию для конкретного запроса:

```java
ChatResponse response = chatModel.call(
    new Prompt(
        "Сгенерируйте имена 5 известных пиратов.",
        ZhiPuAiChatOptions.builder()
            .model(ZhiPuAiApi.ChatModel.GLM_4_Air.getValue())
            .temperature(0.5)
        .build()
    ));
```

> **Совет:** В дополнение к специфичным для модели [ZhiPuAiChatOptions](https://github.com/spring-projects/spring-ai/blob/main/models/spring-ai-zhipuai/src/main/java/org/springframework/ai/zhipuai/ZhiPuAiChatOptions.java) вы можете использовать переносимый экземпляр [ChatOptions](https://github.com/spring-projects/spring-ai/blob/main/spring-ai-model/src/main/java/org/springframework/ai/chat/prompt/ChatOptions.java), созданный с помощью [ChatOptions#builder()](https://github.com/spring-projects/spring-ai/blob/main/spring-ai-model/src/main/java/org/springframework/ai/chat/prompt/DefaultChatOptionsBuilder.java).

## Пример контроллера

https://start.spring.io/[Создайте] новый проект Spring Boot и добавьте `spring-ai-starter-model-zhipuai` в зависимости вашего pom (или gradle).

Добавьте файл `application.properties` в директорию `src/main/resources`, чтобы включить и настроить модель чата ZhiPuAi:

```application.properties
spring.ai.zhipuai.api-key=YOUR_API_KEY
spring.ai.zhipuai.chat.options.model=glm-4-air
spring.ai.zhipuai.chat.options.temperature=0.7
```

> **Совет:** замените `api-key` на ваши учетные данные ZhiPuAI.

Это создаст реализацию `ZhiPuAiChatModel`, которую вы можете внедрить в ваш класс.
Вот пример простого класса `@Controller`, который использует модель чата для генерации текста.

```java
@RestController
public class ChatController {

    private final ZhiPuAiChatModel chatModel;

    @Autowired
    public ChatController(ZhiPuAiChatModel chatModel) {
        this.chatModel = chatModel;
    }

    @GetMapping("/ai/generate")
    public Map generate(@RequestParam(value = "message", defaultValue = "Расскажи мне шутку") String message) {
        return Map.of("generation", this.chatModel.call(message));
    }

    @GetMapping(value = "/ai/generateStream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
	public Flux<ChatResponse> generateStream(@RequestParam(value = "message", defaultValue = "Расскажи мне шутку") String message) {
        var prompt = new Prompt(new UserMessage(message));
        return this.chatModel.stream(prompt);
    }
}
```

## Ручная конфигурация

[ZhiPuAiChatModel](https://github.com/spring-projects/spring-ai/blob/main/models/spring-ai-zhipuai/src/main/java/org/springframework/ai/zhipuai/ZhiPuAiChatModel.java) реализует `ChatModel` и `StreamingChatModel` и использует <<низкоуровневый API>> для подключения к сервису ZhiPuAI.

Добавьте зависимость `spring-ai-zhipuai` в файл `pom.xml` вашего проекта Maven:

```xml
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-zhipuai</artifactId>
</dependency>
```

или в файл сборки Gradle `build.gradle`.

```groovy
dependencies {
    implementation 'org.springframework.ai:spring-ai-zhipuai'
}
```

> **Совет:** Обратитесь к разделу xref:getting-started.adoc#dependency-management[Управление зависимостями], чтобы добавить BOM Spring AI в ваш файл сборки.

Затем создайте `ZhiPuAiChatModel` и используйте его для генерации текста:

```java
var zhiPuAiApi = new ZhiPuAiApi(System.getenv("ZHIPU_AI_API_KEY"));

var chatModel = new ZhiPuAiChatModel(this.zhiPuAiApi, ZhiPuAiChatOptions.builder()
                .model(ZhiPuAiApi.ChatModel.GLM_4_Air.getValue())
                .temperature(0.4)
                .maxTokens(200)
                .build());

ChatResponse response = this.chatModel.call(
    new Prompt("Сгенерируйте имена 5 известных пиратов."));

// Или с потоковыми ответами
Flux<ChatResponse> streamResponse = this.chatModel.stream(
    new Prompt("Сгенерируйте имена 5 известных пиратов."));
```

`ZhiPuAiChatOptions` предоставляет информацию о конфигурации для запросов чата.
`ZhiPuAiChatOptions.Builder` — это удобный строитель параметров.


[ZhiPuAiApi](https://github.com/spring-projects/spring-ai/blob/main/models/spring-ai-zhipuai/src/main/java/org/springframework/ai/zhipuai/api/ZhiPuAiApi.java) предоставляет легковесный Java-клиент для [ZhiPu AI API](https://open.bigmodel.cn/dev/api).

Вот простой фрагмент, как использовать API программно:

```java
ZhiPuAiApi zhiPuAiApi =
    new ZhiPuAiApi(System.getenv("ZHIPU_AI_API_KEY"));

ChatCompletionMessage chatCompletionMessage =
    new ChatCompletionMessage("Привет, мир", Role.USER);

// Синхронный запрос
ResponseEntity<ChatCompletion> response = this.zhiPuAiApi.chatCompletionEntity(
    new ChatCompletionRequest(List.of(this.chatCompletionMessage), ZhiPuAiApi.ChatModel.GLM_4_Air.getValue(), 0.7, false));

// Потоковый запрос
Flux<ChatCompletionChunk> streamResponse = this.zhiPuAiApi.chatCompletionStream(
        new ChatCompletionRequest(List.of(this.chatCompletionMessage), ZhiPuAiApi.ChatModel.GLM_4_Air.getValue(), 0.7, true));
```

Следуйте https://github.com/spring-projects/spring-ai/blob/main/models/spring-ai-zhipuai/src/main/java/org/springframework/ai/zhipuai/api/ZhiPuAiApi.java[JavaDoc ZhiPuAiApi.java] для получения дополнительной информации.

#### Примеры ZhiPuAiApi
- Тест [ZhiPuAiApiIT.java](https://github.com/spring-projects/spring-ai/blob/main/models/spring-ai-zhipuai/src/test/java/org/springframework/ai/zhipuai/api/ZhiPuAiApiIT.java) предоставляет некоторые общие примеры использования легковесной библиотеки.
