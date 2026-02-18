# OpenAI SDK Chat (Официальный)

Spring AI поддерживает языковые модели OpenAI через OpenAI Java SDK, предоставляя надежную и официально поддерживаемую интеграцию с сервисами OpenAI, включая Microsoft Foundry и модели GitHub.

> **Примечание:** Эта реализация использует официальный [OpenAI Java SDK](https://github.com/openai/openai-java) от OpenAI. Для альтернативной реализации Spring AI смотрите xref:api/chat/openai-chat.adoc[OpenAI Chat].

Модуль OpenAI SDK автоматически определяет поставщика услуг (OpenAI, Microsoft Foundry или модели GitHub) на основе базового URL, который вы предоставляете.

## Аутентификация

Аутентификация осуществляется с использованием базового URL и API-ключа. Реализация предоставляет гибкие параметры конфигурации через свойства Spring Boot или переменные окружения.

### Использование OpenAI

Если вы используете OpenAI напрямую, создайте учетную запись на https://platform.openai.com/signup[страница регистрации OpenAI] и сгенерируйте API-ключ на https://platform.openai.com/account/api-keys[странице API-ключей].

Базовый URL не нужно устанавливать, так как по умолчанию он равен `https://api.openai.com/v1`:

```properties
spring.ai.openai-sdk.api-key=<ваш-openai-api-ключ>
# base-url является необязательным, по умолчанию https://api.openai.com/v1
```

Или с использованием переменных окружения:

```bash
export OPENAI_API_KEY=<ваш-openai-api-ключ>
# OPENAI_BASE_URL является необязательным, по умолчанию https://api.openai.com/v1
```

### Использование Microsoft Foundry

Microsoft Foundry автоматически определяется при использовании URL Microsoft Foundry. Вы можете настроить его с помощью свойств:

```properties
spring.ai.openai-sdk.base-url=https://<ваш-url-развертывания>.openai.azure.com
spring.ai.openai-sdk.api-key=<ваш-api-ключ>
spring.ai.openai-sdk.microsoft-deployment-name=<ваше-имя-развертывания>
```

Или с использованием переменных окружения:

```bash
export OPENAI_BASE_URL=https://<ваш-url-развертывания>.openai.azure.com
export OPENAI_API_KEY=<ваш-api-ключ>
```

**Аутентификация без пароля (рекомендуется для Azure):**

Microsoft Foundry поддерживает аутентификацию без пароля без предоставления API-ключа, что более безопасно при работе в Azure.

Чтобы включить аутентификацию без пароля, добавьте зависимость `com.azure:azure-identity`:

```xml
<dependency>
    <groupId>com.azure</groupId>
    <artifactId>azure-identity</artifactId>
</dependency>
```

Затем настройте без API-ключа:

```properties
spring.ai.openai-sdk.base-url=https://<ваш-url-развертывания>.openai.azure.com
spring.ai.openai-sdk.microsoft-deployment-name=<ваше-имя-развертывания>
# API-ключ не нужен - будут использоваться учетные данные Azure из окружения
```

### Использование моделей GitHub

Модели GitHub автоматически определяются при использовании базового URL моделей GitHub. Вам нужно создать токен доступа GitHub (PAT) с областью `models:read`.

```properties
spring.ai.openai-sdk.base-url=https://models.inference.ai.azure.com
spring.ai.openai-sdk.api-key=github_pat_XXXXXXXXXXX
```

Или с использованием переменных окружения:

```bash
export OPENAI_BASE_URL=https://models.inference.ai.azure.com
export OPENAI_API_KEY=github_pat_XXXXXXXXXXX
```

> **Совет:** Для повышения безопасности при работе с конфиденциальной информацией, такой как API-ключи, вы можете использовать язык выражений Spring (SpEL) в ваших свойствах:

```properties
spring.ai.openai-sdk.api-key=${OPENAI_API_KEY}
```

### Добавление репозиториев и BOM

Артефакты Spring AI публикуются в репозиториях Maven Central и Spring Snapshot.
Смотрите раздел xref:getting-started.adoc#artifact-repositories[Артефактные репозитории], чтобы добавить эти репозитории в вашу систему сборки.

Чтобы помочь с управлением зависимостями, Spring AI предоставляет BOM (bill of materials), чтобы гарантировать, что одна и та же версия Spring AI используется на протяжении всего проекта. Смотрите раздел xref:getting-started.adoc#dependency-management[Управление зависимостями], чтобы добавить BOM Spring AI в вашу систему сборки.

## Автоконфигурация

Spring AI предоставляет автоконфигурацию Spring Boot для клиента OpenAI SDK Chat.
Чтобы включить ее, добавьте следующую зависимость в файл сборки Maven `pom.xml` или Gradle `build.gradle` вашего проекта:

[tabs]
======
Maven::
+
```xml
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-starter-model-openai-sdk</artifactId>
</dependency>
```

Gradle::
+
```groovy
dependencies {
    implementation 'org.springframework.ai:spring-ai-starter-model-openai-sdk'
}
```
======

> **Совет:** Смотрите раздел xref:getting-started.adoc#dependency-management[Управление зависимостями], чтобы добавить BOM Spring AI в ваш файл сборки.

### Свойства конфигурации

#### Свойства подключения

Префикс `spring.ai.openai-sdk` используется в качестве префикса свойств, который позволяет вам настраивать клиент OpenAI SDK.

[cols="3,5,1", stripes=even]
|====
| Свойство | Описание | По умолчанию

| spring.ai.openai-sdk.base-url        | URL для подключения. Автоопределяется из переменной окружения `OPENAI_BASE_URL`, если не установлено. |  https://api.openai.com/v1
| spring.ai.openai-sdk.api-key         | API-ключ. Автоопределяется из переменной окружения `OPENAI_API_KEY`, если не установлено. |  -
| spring.ai.openai-sdk.organization-id | Опционально укажите, какую организацию использовать для API-запросов. |  -
| spring.ai.openai-sdk.timeout         | Длительность таймаута запроса. |  -
| spring.ai.openai-sdk.max-retries     | Максимальное количество попыток повторного запроса для неудачных запросов. |  -
| spring.ai.openai-sdk.proxy           | Настройки прокси для клиента OpenAI (объект Java `Proxy`). |  -
| spring.ai.openai-sdk.custom-headers  | Пользовательские HTTP-заголовки, которые следует включить в запросы. Карта имени заголовка к значению заголовка. |  -
|====

#### Свойства Microsoft Foundry (Azure OpenAI)

Реализация OpenAI SDK предоставляет нативную поддержку для Microsoft Foundry (Azure OpenAI) с автоматической конфигурацией:

[cols="3,5,1", stripes=even]
|====
| Свойство | Описание | По умолчанию

| spring.ai.openai-sdk.microsoft-foundry           | Включить режим Microsoft Foundry. Автоопределяется, если базовый URL содержит `openai.azure.com`, `cognitiveservices.azure.com` или `.openai.microsoftFoundry.com`. |  false
| spring.ai.openai-sdk.microsoft-deployment-name | Имя развертывания Microsoft Foundry. Если не указано, будет использоваться имя модели. Также доступно через псевдоним `deployment-name`. |  -
| spring.ai.openai-sdk.microsoft-foundry-service-version | Версия API-сервиса Microsoft Foundry. |  -
| spring.ai.openai-sdk.credential      | Объект учетных данных для аутентификации без пароля (требуется зависимость `com.azure:azure-identity`). |  -
|====

> **Совет:** Microsoft Foundry поддерживает аутентификацию без пароля. Добавьте зависимость `com.azure:azure-identity`, и реализация автоматически попытается использовать учетные данные Azure из окружения, когда API-ключ не предоставлен.

#### Свойства моделей GitHub

Доступна нативная поддержка моделей GitHub:

[cols="3,5,1", stripes=even]
|====
| Свойство | Описание | По умолчанию

| spring.ai.openai-sdk.github-models   | Включить режим моделей GitHub. Автоопределяется, если базовый URL содержит `models.github.ai` или `models.inference.ai.azure.com`. |  false
|====

> **Совет:** Модели GitHub требуют токен доступа с областью `models:read`. Установите его через переменную окружения `OPENAI_API_KEY` или свойство `spring.ai.openai-sdk.api-key`.

#### Свойства модели чата

Префикс `spring.ai.openai-sdk.chat` является префиксом свойств для настройки реализации модели чата:

[cols="3,5,1", stripes=even]
|====
| Свойство | Описание | По умолчанию

| spring.ai.openai-sdk.chat.options.model | Имя модели чата OpenAI, которую следует использовать. Вы можете выбрать между моделями, такими как: `gpt-5-mini`, `gpt-4o`, `gpt-4o-mini`, `gpt-4-turbo`, `o1`, `o3-mini` и другими. Смотрите страницу https://platform.openai.com/docs/models[модели] для получения дополнительной информации. | `gpt-5-mini`
| spring.ai.openai-sdk.chat.options.temperature | Температура выборки, которая контролирует очевидную креативность сгенерированных завершений. Более высокие значения сделают вывод более случайным, в то время как более низкие значения сделают результаты более сосредоточенными и детерминированными. Не рекомендуется изменять `temperature` и `top_p` для одного и того же запроса завершения, так как взаимодействие этих двух настроек трудно предсказать. | 1.0
| spring.ai.openai-sdk.chat.options.frequency-penalty | Число от -2.0 до 2.0. Положительные значения штрафуют новые токены на основе их существующей частоты в тексте до сих пор, уменьшая вероятность повторения той же строки дословно. | 0.0
| spring.ai.openai-sdk.chat.options.logit-bias | Измените вероятность появления указанных токенов в завершении. | -
| spring.ai.openai-sdk.chat.options.logprobs | Указывает, следует ли возвращать логарифмические вероятности выходных токенов. | false
| spring.ai.openai-sdk.chat.options.top-logprobs | Целое число от 0 до 5, указывающее количество наиболее вероятных токенов, которые следует вернуть на каждой позиции токена. Требует, чтобы `logprobs` было истинным. | -
| spring.ai.openai-sdk.chat.options.max-tokens | Максимальное количество токенов для генерации. **Используйте для моделей без рассуждений** (например, gpt-4o, gpt-3.5-turbo). **Не может использоваться с моделями рассуждений** (например, o1, o3, o4-mini серии). **Взаимно исключается с maxCompletionTokens**. | -
| spring.ai.openai-sdk.chat.options.max-completion-tokens | Верхний предел количества токенов, которые могут быть сгенерированы для завершения, включая видимые выходные токены и токены рассуждений. **Требуется для моделей рассуждений** (например, o1, o3, o4-mini серии). **Не может использоваться с моделями без рассуждений**. **Взаимно исключается с maxTokens**. | -
| spring.ai.openai-sdk.chat.options.n | Сколько вариантов завершения чата сгенерировать для каждого входного сообщения. | 1
| spring.ai.openai-sdk.chat.options.output-modalities | Список выходных модальностей. Может включать "text" и "audio". | -
| spring.ai.openai-sdk.chat.options.output-audio | Параметры для аудиовыхода. Используйте `AudioParameters` с голосом (ALLOY, ASH, BALLAD, CORAL, ECHO, FABLE, ONYX, NOVA, SAGE, SHIMMER) и форматом (MP3, FLAC, OPUS, PCM16, WAV, AAC). | -
| spring.ai.openai-sdk.chat.options.presence-penalty | Число от -2.0 до 2.0. Положительные значения штрафуют новые токены на основе того, появляются ли они в тексте до сих пор. | 0.0
| spring.ai.openai-sdk.chat.options.response-format.type | Тип формата ответа: `TEXT`, `JSON_OBJECT` или `JSON_SCHEMA`. | TEXT
| spring.ai.openai-sdk.chat.options.response-format.json-schema | JSON-схема для структурированных выходов, когда тип равен `JSON_SCHEMA`. | -
| spring.ai.openai-sdk.chat.options.seed | Если указано, система постарается сделать выборку детерминированно для воспроизводимых результатов. | -
| spring.ai.openai-sdk.chat.options.stop | До 4 последовательностей, при которых API прекратит генерировать дальнейшие токены. | -
| spring.ai.openai-sdk.chat.options.top-p | Альтернатива выборке с температурой, называемая ядерной выборкой. | -
| spring.ai.openai-sdk.chat.options.user | Уникальный идентификатор, представляющий вашего конечного пользователя, который может помочь OpenAI отслеживать и обнаруживать злоупотребления. | -
| spring.ai.openai-sdk.chat.options.parallel-tool-calls | Указывает, следует ли включить параллельные вызовы функций во время использования инструментов. | true
| spring.ai.openai-sdk.chat.options.reasoning-effort | Ограничивает усилия по рассуждению для моделей рассуждений: `low`, `medium` или `high`. | -
| spring.ai.openai-sdk.chat.options.verbosity | Контролирует подробность ответа модели. | -
| spring.ai.openai-sdk.chat.options.store | Указывает, следует ли сохранять вывод этого запроса на завершение чата для использования в дистилляции модели OpenAI или продуктах evals. | false
| spring.ai.openai-sdk.chat.options.metadata | Теги и значения, определенные разработчиком, используемые для фильтрации завершений на панели управления. | -
| spring.ai.openai-sdk.chat.options.service-tier | Указывает уровень задержки, который следует использовать: `auto`, `default`, `flex` или `priority`. | -
| spring.ai.openai-sdk.chat.options.stream-options.include-usage | Указывает, следует ли включать статистику использования в потоковых ответах. | false
| spring.ai.openai-sdk.chat.options.stream-options.include-obfuscation | Указывает, следует ли включать обфускацию в потоковых ответах. | false
| spring.ai.openai-sdk.chat.options.tool-choice | Контролирует, какая (если есть) функция вызывается моделью. | -
| spring.ai.openai-sdk.chat.options.internal-tool-execution-enabled | Если false, Spring AI будет проксировать вызовы инструментов клиенту для ручной обработки. Если true (по умолчанию), Spring AI обрабатывает вызовы функций внутренне. | true
|====

[NOTE]
====
При использовании моделей GPT-5, таких как `gpt-5`, `gpt-5-mini` и `gpt-5-nano`, параметр `temperature` не поддерживается.
Эти модели оптимизированы для рассуждений и не используют температуру.
Указание значения температуры приведет к ошибке.
В отличие от этого, разговорные модели, такие как `gpt-5-chat`, поддерживают параметр `temperature`.
====

> **Совет:** Все свойства с префиксом `spring.ai.openai-sdk.chat.options` могут быть переопределены во время выполнения, добавляя специфические для запроса <<chat-options>> в вызов `Prompt`.

### Параметры ограничения токенов: использование, специфичное для модели

OpenAI предоставляет два взаимно исключающих параметра для контроля ограничений на генерацию токенов:

[cols="2,3,3", stripes=even]
|====
| Параметр | Случай использования | Совместимые модели

| `maxTokens` | Модели без рассуждений | gpt-4o, gpt-4o-mini, gpt-4-turbo, gpt-3.5-turbo
| `maxCompletionTokens` | Модели рассуждений | o1, o1-mini, o1-preview, o3, o4-mini серии
|====

> **Важно:** Эти параметры **взаимно исключают друг друга**. Установка обоих приведет к ошибке API от OpenAI.

#### Примеры использования

**Для моделей без рассуждений (gpt-4o, gpt-3.5-turbo):**
```java
ChatResponse response = chatModel.call(
    new Prompt(
        "Объясните квантовые вычисления простыми словами.",
        OpenAiSdkChatOptions.builder()
            .model("gpt-4o")
            .maxTokens(150)  // Используйте maxTokens для моделей без рассуждений
        .build()
    ));
```

**Для моделей рассуждений (o1, o3 серии):**
```java
ChatResponse response = chatModel.call(
    new Prompt(
        "Решите эту сложную математическую задачу шаг за шагом: ...",
        OpenAiSdkChatOptions.builder()
            .model("o1-preview")
            .maxCompletionTokens(1000)  // Используйте maxCompletionTokens для моделей рассуждений
        .build()
    ));
```

## Опции времени выполнения [[chat-options]]

Класс https://github.com/spring-projects/spring-ai/blob/main/models/spring-ai-openai-sdk/src/main/java/org/springframework/ai/openaisdk/OpenAiSdkChatOptions.java[OpenAiSdkChatOptions.java] предоставляет конфигурации модели, такие как модель для использования, температура, штраф за частоту и т. д.

При запуске параметры по умолчанию могут быть настроены с помощью конструктора `OpenAiSdkChatModel(options)` или свойств `spring.ai.openai-sdk.chat.options.*`.

Во время выполнения вы можете переопределить параметры по умолчанию, добавляя новые, специфические для запроса параметры в вызов `Prompt`.
Например, чтобы переопределить модель и температуру по умолчанию для конкретного запроса:

```java
ChatResponse response = chatModel.call(
    new Prompt(
        "Сгенерируйте имена 5 известных пиратов.",
        OpenAiSdkChatOptions.builder()
            .model("gpt-4o")
            .temperature(0.4)
        .build()
    ));
```

> **Совет:** В дополнение к специфическим для модели https://github.com/spring-projects/spring-ai/blob/main/models/spring-ai-openai-sdk/src/main/java/org/springframework/ai/openaisdk/OpenAiSdkChatOptions.java[OpenAiSdkChatOptions] вы можете использовать переносимый [ChatOptions](https://github.com/spring-projects/spring-ai/blob/main/spring-ai-model/src/main/java/org/springframework/ai/chat/prompt/ChatOptions.java) экземпляр, созданный с помощью [ChatOptions#builder()](https://github.com/spring-projects/spring-ai/blob/main/spring-ai-model/src/main/java/org/springframework/ai/chat/prompt/DefaultChatOptionsBuilder.java).

## Вызов инструментов

Вы можете зарегистрировать пользовательские функции или методы Java с `OpenAiSdkChatModel` и позволить модели OpenAI интеллектуально выбирать, чтобы вывести JSON-объект, содержащий аргументы для вызова одной или нескольких зарегистрированных функций/инструментов.
Это мощная техника для соединения возможностей LLM с внешними инструментами и API.
Читать далее о xref:api/tools.adoc[Вызов инструментов].

Пример использования:

```java
var chatOptions = OpenAiSdkChatOptions.builder()
    .toolCallbacks(List.of(
        FunctionToolCallback.builder("getCurrentWeather", new WeatherService())
            .description("Получить погоду в местоположении")
            .inputType(WeatherService.Request.class)
            .build()))
    .build();

ChatResponse response = chatModel.call(
    new Prompt("Какова погода в Сан-Франциско?", chatOptions));
```

## Мультимодальность

Мультимодальность относится к способности модели одновременно понимать и обрабатывать информацию из различных источников, включая текст, изображения, аудио и другие форматы данных.

### Видение

Модели OpenAI, которые предлагают поддержку мультимодальности в области зрения, включают `gpt-4`, `gpt-4o` и `gpt-4o-mini`.
Смотрите руководство [Vision](https://platform.openai.com/docs/guides/vision) для получения дополнительной информации.

Интерфейс Spring AI [Message](https://github.com/spring-projects/spring-ai/blob/main/spring-ai-model/src/main/java/org/springframework/ai/chat/messages/Message.java) облегчает мультимодальные AI модели, вводя тип [Media](https://github.com/spring-projects/spring-ai/blob/main/spring-ai-commons/src/main/java/org/springframework/ai/content/Media.java).

Ниже приведен пример кода, иллюстрирующий слияние текста пользователя с изображением:

```java
var imageResource = new ClassPathResource("/multimodal.test.png");

var userMessage = new UserMessage(
    "Объясните, что вы видите на этом изображении?",
    List.of(new Media(MimeTypeUtils.IMAGE_PNG, imageResource)));

ChatResponse response = chatModel.call(
    new Prompt(userMessage, 
        OpenAiSdkChatOptions.builder()
            .model("gpt-4o")
            .build()));
```

Или с использованием URL изображения:

```java
var userMessage = new UserMessage(
    "Объясните, что вы видите на этом изображении?",
    List.of(Media.builder()
        .mimeType(MimeTypeUtils.IMAGE_PNG)
        .data(URI.create("https://docs.spring.io/spring-ai/reference/_images/multimodal.test.png"))
        .build()));

ChatResponse response = chatModel.call(new Prompt(userMessage));
```

> **Совет:** Вы также можете передавать несколько изображений.

### Аудио

Модели OpenAI, которые предлагают поддержку аудиовхода, включают `gpt-4o-audio-preview`.
Смотрите руководство [Audio](https://platform.openai.com/docs/guides/audio) для получения дополнительной информации.

Spring AI поддерживает аудиофайлы в формате base64 с сообщением.
В настоящее время OpenAI поддерживает следующие медиа-типы: `audio/mp3` и `audio/wav`.

Пример аудиовхода:

```java
var audioResource = new ClassPathResource("speech1.mp3");

var userMessage = new UserMessage(
    "О чем эта запись?",
    List.of(new Media(MimeTypeUtils.parseMimeType("audio/mp3"), audioResource)));

ChatResponse response = chatModel.call(
    new Prompt(userMessage,
        OpenAiSdkChatOptions.builder()
            .model("gpt-4o-audio-preview")
            .build()));
```

### Аудиовыход

Модель `gpt-4o-audio-preview` может генерировать аудиовыходы.

Пример генерации аудиовыхода:

```java
var userMessage = new UserMessage("Расскажите мне шутку о Spring Framework");

ChatResponse response = chatModel.call(
    new Prompt(userMessage,
        OpenAiSdkChatOptions.builder()
            .model("gpt-4o-audio-preview")
            .outputModalities(List.of("text", "audio"))
            .outputAudio(new AudioParameters(Voice.ALLOY, AudioResponseFormat.WAV))
            .build()));

String text = response.getResult().getOutput().getText(); // текстовая транскрипция
byte[] waveAudio = response.getResult().getOutput().getMedia().get(0).getDataAsByteArray(); // аудиоданные
```

## Структурированные выходы

OpenAI предоставляет пользовательские https://platform.openai.com/docs/guides/structured-outputs[структурированные выходы] API, которые гарантируют, что ваша модель генерирует ответы, строго соответствующие предоставленной вами `JSON-схеме`.

### Конфигурация

Вы можете установить формат ответа программно с помощью строителя `OpenAiSdkChatOptions`:

```java
String jsonSchema = """
    {
        "type": "object",
        "properties": {
            "steps": {
                "type": "array",
                "items": {
                    "type": "object",
                    "properties": {
                        "explanation": { "type": "string" },
                        "output": { "type": "string" }
                    },
                    "required": ["explanation", "output"],
                    "additionalProperties": false
                }
            },
            "final_answer": { "type": "string" }
        },
        "required": ["steps", "final_answer"],
        "additionalProperties": false
    }
    """;

Prompt prompt = new Prompt(
    "как я могу решить 8x + 7 = -23",
    OpenAiSdkChatOptions.builder()
        .model("gpt-4o-mini")
        .responseFormat(ResponseFormat.builder()
            .type(ResponseFormat.Type.JSON_SCHEMA)
            .jsonSchema(jsonSchema)
            .build())
        .build());

ChatResponse response = chatModel.call(prompt);
```

### Интеграция с BeanOutputConverter

Вы можете использовать существующие xref::api/structured-output-converter.adoc#_bean_output_converter[утилиты BeanOutputConverter]:

```java
record MathReasoning(
    @JsonProperty(required = true, value = "steps") Steps steps,
    @JsonProperty(required = true, value = "final_answer") String finalAnswer) {
    
    record Steps(
        @JsonProperty(required = true, value = "items") Items[] items) {
        
        record Items(
            @JsonProperty(required = true, value = "explanation") String explanation,
            @JsonProperty(required = true, value = "output") String output) {
        }
    }
}

var outputConverter = new BeanOutputConverter<>(MathReasoning.class);
String jsonSchema = outputConverter.getJsonSchema();

Prompt prompt = new Prompt(
    "как я могу решить 8x + 7 = -23",
    OpenAiSdkChatOptions.builder()
        .model("gpt-4o-mini")
        .responseFormat(ResponseFormat.builder()
            .type(ResponseFormat.Type.JSON_SCHEMA)
            .jsonSchema(jsonSchema)
            .build())
        .build());

ChatResponse response = chatModel.call(prompt);
MathReasoning mathReasoning = outputConverter.convert(
    response.getResult().getOutput().getText());
```

## Пример контроллера

https://start.spring.io/[Создайте] новый проект Spring Boot и добавьте `spring-ai-openai-sdk` в зависимости вашего pom (или gradle).

Добавьте файл `application.properties` в директорию `src/main/resources`, чтобы настроить модель чата OpenAI SDK:

```application.properties
spring.ai.openai-sdk.api-key=YOUR_API_KEY
spring.ai.openai-sdk.chat.options.model=gpt-5-mini
spring.ai.openai-sdk.chat.options.temperature=0.7
```

> **Совет:** Замените `api-key` на ваши учетные данные OpenAI.

Это создаст реализацию `OpenAiSdkChatModel`, которую вы можете внедрить в ваши классы.
Вот пример простого класса `@RestController`, который использует модель чата для генерации текста.

```java
@RestController
public class ChatController {

    private final OpenAiSdkChatModel chatModel;

    @Autowired
    public ChatController(OpenAiSdkChatModel chatModel) {
        this.chatModel = chatModel;
    }

    @GetMapping("/ai/generate")
    public Map<String,String> generate(
            @RequestParam(value = "message", defaultValue = "Расскажи мне шутку") String message) {
        return Map.of("generation", chatModel.call(message));
    }

    @GetMapping("/ai/generateStream")
    public Flux<ChatResponse> generateStream(
            @RequestParam(value = "message", defaultValue = "Расскажи мне шутку") String message) {
        Prompt prompt = new Prompt(new UserMessage(message));
        return chatModel.stream(prompt);
    }
}
```

## Ручная конфигурация

Класс https://github.com/spring-projects/spring-ai/blob/main/models/spring-ai-openai-sdk/src/main/java/org/springframework/ai/openaisdk/OpenAiSdkChatModel.java[OpenAiSdkChatModel] реализует `ChatModel` и использует официальный OpenAI Java SDK для подключения к сервису OpenAI.

Добавьте зависимость `spring-ai-openai-sdk` в файл Maven `pom.xml` вашего проекта:

```xml
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-openai-sdk</artifactId>
</dependency>
```

или в файл сборки Gradle `build.gradle`:

```groovy
dependencies {
    implementation 'org.springframework.ai:spring-ai-openai-sdk'
}
```

> **Совет:** Смотрите раздел xref:getting-started.adoc#dependency-management[Управление зависимостями], чтобы добавить BOM Spring AI в ваш файл сборки.

Затем создайте `OpenAiSdkChatModel` и используйте его для генерации текста:

```java
var chatOptions = OpenAiSdkChatOptions.builder()
    .model("gpt-4o")
    .temperature(0.7)
    .apiKey(System.getenv("OPENAI_API_KEY"))
    .build();

var chatModel = OpenAiSdkChatModel.builder()
    .options(chatOptions)
    .build();

ChatResponse response = chatModel.call(
    new Prompt("Сгенерируйте имена 5 известных пиратов."));

// Или с потоковыми ответами
Flux<ChatResponse> response = chatModel.stream(
    new Prompt("Сгенерируйте имена 5 известных пиратов."));
```

### Конфигурация Microsoft Foundry

Для Microsoft Foundry:

```java
var chatOptions = OpenAiSdkChatOptions.builder()
    .baseUrl("https://your-resource.openai.azure.com")
    .apiKey(System.getenv("OPENAI_API_KEY"))
    .deploymentName("gpt-4")
    .azureOpenAIServiceVersion(AzureOpenAIServiceVersion.V2024_10_01_PREVIEW)
    .azure(true)  // Включает режим Microsoft Foundry
    .build();

var chatModel = OpenAiSdkChatModel.builder()
    .options(chatOptions)
    .build();
```

> **Совет:** Microsoft Foundry поддерживает аутентификацию без пароля. Добавьте зависимость `com.azure:azure-identity` в ваш проект. Если вы не предоставите API-ключ, реализация автоматически попытается использовать учетные данные Azure из вашего окружения.

### Конфигурация моделей GitHub

Для моделей GitHub:

```java
var chatOptions = OpenAiSdkChatOptions.builder()
    .baseUrl("https://models.inference.ai.azure.com")
    .apiKey(System.getenv("GITHUB_TOKEN"))
    .model("gpt-4o")
    .githubModels(true)
    .build();

var chatModel = OpenAiSdkChatModel.builder()
    .options(chatOptions)
    .build();
```

## Ключевые отличия от Spring AI OpenAI

Эта реализация отличается от xref:api/chat/openai-chat.adoc[реализации Spring AI OpenAI] несколькими способами:

[cols="2,3,3", stripes=even]
|====
| Аспект | Официальный OpenAI SDK | Существующий OpenAI

| **HTTP-клиент** | OkHttp (через официальный SDK) | Spring RestClient/WebClient
| **Обновления API** | Автоматические через обновления SDK | Ручное обслуживание
| **Поддержка Azure** | Нативная с аутентификацией без пароля | Ручное построение URL
| **Модели GitHub** | Нативная поддержка | Не поддерживается
| **Аудио/Модерация** | Пока не поддерживается | Полностью поддерживается
| **Логика повторных попыток** | Управляется SDK (экспоненциальный откат) | Spring Retry (настраиваемый)
| **Зависимости** | Официальный OpenAI SDK | Spring WebFlux
|====

**Когда использовать OpenAI SDK:**

- Вы начинаете новый проект
- Вы в основном используете Microsoft Foundry или модели GitHub
- Вы хотите автоматические обновления API от OpenAI
- Вам не нужны функции транскрипции аудио или модерации
- Вы предпочитаете официальную поддержку SDK

**Когда использовать Spring AI OpenAI:**

- У вас уже есть существующий проект, использующий его
- Вам нужны функции транскрипции аудио или модерации
- Вам требуется детальный контроль HTTP
- Вы хотите нативную поддержку Spring Reactive
- Вам нужны настраиваемые стратегии повторных попыток

## Наблюдаемость

Реализация OpenAI SDK поддерживает функции наблюдаемости Spring AI через Micrometer.
Все операции модели чата инструментированы для мониторинга и трассировки.

## Ограничения

Следующие функции еще не поддерживаются в реализации OpenAI SDK:

- Генерация речи (TTS)
- Транскрипция аудио
- API модерации
- Операции API файлов

Эти функции доступны в xref:api/chat/openai-chat.adoc[реализации Spring AI OpenAI].

## Дополнительные ресурсы

- [Официальный OpenAI Java SDK](https://github.com/openai/openai-java)
- [Документация OpenAI Chat API](https://platform.openai.com/docs/api-reference/chat)
- [Модели OpenAI](https://platform.openai.com/docs/models)
- [Документация Microsoft Foundry](https://learn.microsoft.com/en-us/azure/ai-foundry/)
- [Модели GitHub](https://github.com/marketplace/models)
