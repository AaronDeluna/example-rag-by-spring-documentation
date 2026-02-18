# OpenAI Chat

Spring AI поддерживает различные языковые модели ИИ от OpenAI, компании, стоящей за ChatGPT, которая сыграла ключевую роль в пробуждении интереса к генерации текста на основе ИИ благодаря созданию передовых моделей генерации текста и встраиваний.

## Предварительные требования

Вам необходимо создать API в OpenAI для доступа к моделям ChatGPT.

Создайте учетную запись на https://platform.openai.com/signup[страница регистрации OpenAI] и сгенерируйте токен на https://platform.openai.com/account/api-keys[страница API-ключей].

Проект Spring AI определяет свойство конфигурации с именем `spring.ai.openai.api-key`, которое вы должны установить в значение `API Key`, полученного с openai.com.

Вы можете установить это свойство конфигурации в вашем файле `application.properties`:

```properties
spring.ai.openai.api-key=<ваш-openai-api-ключ>
```

Для повышения безопасности при работе с конфиденциальной информацией, такой как API-ключи, вы можете использовать язык выражений Spring (SpEL) для ссылки на пользовательскую переменную окружения:

```yaml
# В application.yml
spring:
  ai:
    openai:
      api-key: ${OPENAI_API_KEY}
```

```bash
# В вашей среде или .env файле
export OPENAI_API_KEY=<ваш-openai-api-ключ>
```

Вы также можете установить эту конфигурацию программно в коде вашего приложения:

```java
// Получите API-ключ из безопасного источника или переменной окружения
String apiKey = System.getenv("OPENAI_API_KEY");
```

### Добавление репозиториев и BOM

Артефакты Spring AI публикуются в репозиториях Maven Central и Spring Snapshot.
Смотрите раздел xref:getting-started.adoc#artifact-repositories[Артефактные репозитории], чтобы добавить эти репозитории в вашу систему сборки.

Чтобы помочь с управлением зависимостями, Spring AI предоставляет BOM (спецификация материалов), чтобы гарантировать, что одна и та же версия Spring AI используется на протяжении всего проекта. Смотрите раздел xref:getting-started.adoc#dependency-management[Управление зависимостями], чтобы добавить BOM Spring AI в вашу систему сборки.

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

> **Совет:** Смотрите раздел xref:getting-started.adoc#dependency-management[Управление зависимостями], чтобы добавить BOM Spring AI в ваш файл сборки.

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
| spring.ai.retry.on-client-errors | Если false, выбросить NonTransientAiException и не пытаться повторить для кодов ошибок клиента `4xx` | false
| spring.ai.retry.exclude-on-http-codes | Список кодов состояния HTTP, которые не должны вызывать повторную попытку (например, для выброса NonTransientAiException). | пусто
| spring.ai.retry.on-http-codes | Список кодов состояния HTTP, которые должны вызывать повторную попытку (например, для выброса TransientAiException). | пусто
|====

#### Свойства подключенияПрефикс `spring.ai.openai` используется в качестве префикса свойств, который позволяет вам подключаться к OpenAI.

[cols="3,5,1", stripes=even]
|====
| Свойство | Описание | По умолчанию

| spring.ai.openai.base-url        | URL для подключения |  https://api.openai.com
| spring.ai.openai.api-key         | API-ключ           |  -
| spring.ai.openai.organization-id | При желании вы можете указать, какую организацию использовать для API-запроса. |  -
| spring.ai.openai.project-id      | При желании вы можете указать, какой проект использовать для API-запроса. |  -
|====

> **Совет:** Для пользователей, которые принадлежат нескольким организациям (или получают доступ к своим проектам через свой устаревший API-ключ пользователя), вы можете по желанию указать, какая организация и проект используются для API-запроса. Использование этих API-запросов будет учитываться как использование для указанной организации и проекта.

#### Заголовок User-Agent

Spring AI автоматически отправляет заголовок `User-Agent: spring-ai` со всеми запросами к OpenAI. Это помогает OpenAI идентифицировать запросы, поступающие от Spring AI, для аналитики и поддержки. Этот заголовок отправляется автоматически и не требует настройки со стороны пользователей Spring AI.

Если вы являетесь поставщиком API, создающим совместимую с OpenAI службу, вы можете отслеживать использование Spring AI, считывая HTTP-заголовок `User-Agent` из входящих запросов на вашем сервере.

#### Свойства конфигурации[NOTE]
====
Включение и отключение автонастроек чата теперь настраивается через свойства верхнего уровня с префиксом `spring.ai.model.chat`.

Чтобы включить, используйте spring.ai.model.chat=openai (по умолчанию включено)

Чтобы отключить, используйте spring.ai.model.chat=none (или любое значение, не соответствующее openai)

Это изменение сделано для возможности конфигурации нескольких моделей.
====

Префикс `spring.ai.openai.chat` — это префикс свойства, который позволяет вам настраивать реализацию модели чата для OpenAI.

[cols="3,5,1", stripes=even]
|====
| Свойство | Описание | По умолчанию

| spring.ai.openai.chat.enabled (Удалено и больше не актуально) | Включить модель чата OpenAI.  | true
| spring.ai.model.chat | Включить модель чата OpenAI.  | openai
| spring.ai.openai.chat.base-url   | Необязательное переопределение свойства `spring.ai.openai.base-url` для предоставления URL, специфичного для чата. |  -
| spring.ai.openai.chat.completions-path   | Путь, который будет добавлен к базовому URL. |  `/v1/chat/completions`
| spring.ai.openai.chat.api-key   | Необязательное переопределение `spring.ai.openai.api-key` для предоставления специфичного для чата API-ключа. |  -
| spring.ai.openai.chat.organization-id | Опционально, вы можете указать, какую организацию использовать для API-запроса. |  -
| spring.ai.openai.chat.project-id      | Опционально, вы можете указать, какой проект использовать для API-запроса. |  -
| spring.ai.openai.chat.options.model | Название модели чата OpenAI, которую следует использовать. Вы можете выбрать между моделями, такими как: `gpt-5-mini`, `gpt-4o`, `gpt-4o-mini`, `gpt-4-turbo`, `gpt-3.5-turbo` и другими. Дополнительную информацию смотрите на странице https://platform.openai.com/docs/models[models]. | `gpt-5-mini`
| spring.ai.openai.chat.options.temperature | Температура выборки, которая контролирует очевидную креативность сгенерированных завершений. Более высокие значения сделают вывод более случайным, в то время как более низкие значения сделают результаты более сфокусированными и детерминированными. Не рекомендуется изменять `temperature` и `top_p` для одного и того же запроса завершения, так как взаимодействие этих двух настроек трудно предсказать. | 0.8
| spring.ai.openai.chat.options.frequencyPenalty | Число от -2.0 до 2.0. Положительные значения штрафуют новые токены на основе их существующей частоты в тексте до сих пор, уменьшая вероятность повторения одной и той же строки дословно. | 0.0f
| spring.ai.openai.chat.options.logitBias | Изменить вероятность появления указанных токенов в завершении. | -
| spring.ai.openai.chat.options.maxTokens | Максимальное количество токенов, которые можно сгенерировать в завершении чата. Общая длина входных токенов и сгенерированных токенов ограничена длиной контекста модели. **Используйте для моделей без рассуждений** (например, gpt-4o, gpt-3.5-turbo). **Не может использоваться с моделями рассуждений** (например, o1, o3, o4-mini серии). **Взаимно исключает maxCompletionTokens** - установка обоих приведет к ошибке API. | -
| spring.ai.openai.chat.options.maxCompletionTokens | Верхний предел для количества токенов, которые могут быть сгенерированы для завершения, включая видимые выходные токены и токены рассуждений. **Обязательно для моделей рассуждений** (например, o1, o3, o4-mini серии). **Не может использоваться с моделями без рассуждений** (например, gpt-4o, gpt-3.5-turbo). **Взаимно исключает maxTokens** - установка обоих приведет к ошибке API. | -
| spring.ai.openai.chat.options.n | Сколько вариантов завершения чата сгенерировать для каждого входного сообщения. Обратите внимание, что вы будете платить в зависимости от количества сгенерированных токенов по всем вариантам. Держите `n` равным 1, чтобы минимизировать затраты. | 1
| spring.ai.openai.chat.options.store | Нужно ли сохранять вывод этого запроса на завершение чата для использования в нашей модели | false
| spring.ai.openai.chat.options.metadata | Теги и значения, определенные разработчиком, используемые для фильтрации завершений на панели завершений чата | пустая карта
| spring.ai.openai.chat.options.output-modalities | Типы выходных данных, которые вы хотите, чтобы модель сгенерировала для этого запроса. Большинство моделей способны генерировать текст, что является значением по умолчанию.
Модель `gpt-4o-audio-preview` также может использоваться для генерации аудио. Чтобы запросить, чтобы эта модель генерировала как текстовые, так и аудио ответы,
вы можете использовать: `text`, `audio`. Не поддерживается для потоковой передачи. | -
| spring.ai.openai.chat.options.output-audio | Параметры аудио для генерации аудио. Обязательно, когда запрашивается аудиовыход с `output-modalities`: `audio`.
Требует модель `gpt-4o-audio-preview` и не поддерживается для потоковых завершений. | -
| spring.ai.openai.chat.options.presencePenalty | Число от -2.0 до 2.0. Положительные значения штрафуют новые токены на основе того, появляются ли они в тексте до сих пор, увеличивая вероятность модели говорить о новых темах. | -
| spring.ai.openai.chat.options.responseFormat.type | Совместимо с `GPT-4o`, `GPT-4o mini`, `GPT-4 Turbo` и всеми моделями `GPT-3.5 Turbo`, новее `gpt-3.5-turbo-1106`. Тип `JSON_OBJECT` включает режим JSON, который гарантирует, что сообщение, сгенерированное моделью, является действительным JSON.
Тип `JSON_SCHEMA` включает [Структурированные Выходы](https://platform.openai.com/docs/guides/structured-outputs), которые гарантируют, что модель будет соответствовать вашему предоставленному JSON-схеме. Тип JSON_SCHEMA требует также установки свойства `responseFormat.schema`. | -
| spring.ai.openai.chat.options.responseFormat.name | Имя схемы формата ответа. Применимо только для `responseFormat.type=JSON_SCHEMA` | custom_schema
| spring.ai.openai.chat.options.responseFormat.schema | JSON-схема формата ответа. Применимо только для `responseFormat.type=JSON_SCHEMA` | -
| spring.ai.openai.chat.options.responseFormat.strict | Степень строгости соблюдения схемы формата ответа JSON. Применимо только для `responseFormat.type=JSON_SCHEMA` | -
| spring.ai.openai.chat.options.seed | Эта функция находится в бета-версии. Если указано, наша система будет стараться делать выборку детерминированно, так что повторные запросы с тем же семенем и параметрами должны возвращать один и тот же результат. | -
| spring.ai.openai.chat.options.stop | До 4 последовательностей, при которых API прекратит генерировать дальнейшие токены. | -
| spring.ai.openai.chat.options.topP | Альтернатива выборке с температурой, называемая ядерной выборкой, где модель учитывает результаты токенов с вероятностью `top_p`. Таким образом, 0.1 означает, что учитываются только токены, составляющие верхние 10% вероятностной массы. Мы обычно рекомендуем изменять это или `temperature`, но не оба. | -
| spring.ai.openai.chat.options.tools | Список инструментов, которые модель может вызывать. В настоящее время поддерживаются только функции в качестве инструмента. Используйте это, чтобы предоставить список функций, для которых модель может генерировать JSON-входы. | -
| spring.ai.openai.chat.options.toolChoice | Управляет тем, какая (если есть) функция вызывается моделью. `none` означает, что модель не будет вызывать функцию и вместо этого сгенерирует сообщение. `auto` означает, что модель может выбирать между генерацией сообщения или вызовом функции. Указание конкретной функции через `{"type": "function", "function": {"name": "my_function"}}` заставляет модель вызвать эту функцию. `none` является значением по умолчанию, когда функции отсутствуют. `auto` является значением по умолчанию, если функции присутствуют. | -
| spring.ai.openai.chat.options.user | Уникальный идентификатор, представляющий вашего конечного пользователя, который может помочь OpenAI отслеживать и обнаруживать злоупотребления. | -
| spring.ai.openai.chat.options.stream-usage | (Только для потоковой передачи) Установите, чтобы добавить дополнительный фрагмент с статистикой использования токенов для всего запроса. Поле `choices` для этого фрагмента является пустым массивом, и все другие фрагменты также будут включать поле использования, но с нулевым значением. | false
| spring.ai.openai.chat.options.parallel-tool-calls | Нужно ли включить [параллельные вызовы функций](https://platform.openai.com/docs/guides/function-calling/parallel-function-calling) во время использования инструмента. | true
| spring.ai.openai.chat.options.prompt-cache-key | Ключ кэша, используемый OpenAI для оптимизации коэффициентов попадания кэша для схожих запросов. Улучшает задержку и снижает затраты. Заменяет устаревшее поле `user` для целей кэширования. [Узнайте больше](https://platform.openai.com/docs/guides/prompt-caching). | -
| spring.ai.openai.chat.options.safety-identifier | Стабильный идентификатор, который помогает OpenAI обнаруживать пользователей, нарушающих правила использования. Должен быть хэшированным значением (например, хэшированное имя пользователя или электронная почта). Заменяет устаревшее поле `user` для отслеживания безопасности. [Узнайте больше](https://platform.openai.com/docs/guides/safety-best-practices#safety-identifiers). | -
| spring.ai.openai.chat.options.http-headers | Необязательные HTTP-заголовки, которые будут добавлены к запросу на завершение чата. Чтобы переопределить `api-key`, вам нужно использовать заголовок `Authorization`, и вы должны префиксировать значение ключа префиксом `Bearer`. | -
| spring.ai.openai.chat.options.tool-names | Список инструментов, идентифицированных по их именам, для включения в вызов функции в одном запросе. Инструменты с этими именами должны существовать в реестре ToolCallback. | -
| spring.ai.openai.chat.options.tool-callbacks | Обратные вызовы инструментов для регистрации с ChatModel. | -
| spring.ai.openai.chat.options.internal-tool-execution-enabled | Если false, Spring AI не будет обрабатывать вызовы инструментов внутренне, а будет проксировать их клиенту. Тогда клиент несет ответственность за обработку вызовов инструментов, их распределение на соответствующие функции и возврат результатов. Если true (по умолчанию), Spring AI будет обрабатывать вызовы функций внутренне. Применимо только для моделей чата с поддержкой вызова функций | true
| spring.ai.openai.chat.options.service-tier | Указывает [тип обработки](https://platform.openai.com/docs/api-reference/responses/create#responses_create-service_tier), используемый для обслуживания запроса. | -
| spring.ai.openai.chat.options.extra-body | Дополнительные параметры, которые следует включить в запрос. Принимает любые пары ключ-значение, которые упрощаются до верхнего уровня JSON-запроса. Предназначено для использования с серверами, совместимыми с OpenAI (vLLM, Ollama и т. д.), которые поддерживают параметры, выходящие за рамки стандартного API OpenAI. Официальный API OpenAI игнорирует неизвестные параметры. См. <<openai-compatible-servers>> для получения подробной информации. | -
|====

[NOTE]
====
При использовании моделей GPT-5, таких как `gpt-5`, `gpt-5-mini` и `gpt-5-nano`, параметр `temperature` не поддерживается.
Эти модели оптимизированы для рассуждений и не используют температуру.
Указание значения температуры приведет к ошибке.
В отличие от этого, разговорные модели, такие как `gpt-5-chat`, поддерживают параметр `temperature`.
====

> **Примечание:** Вы можете переопределить общие `spring.ai.openai.base-url` и `spring.ai.openai.api-key` для реализаций `ChatModel` и `EmbeddingModel`.
Свойства `spring.ai.openai.chat.base-url` и `spring.ai.openai.chat.api-key`, если установлены, имеют приоритет над общими свойствами.
Это полезно, если вы хотите использовать разные учетные записи OpenAI для разных моделей и разных конечных точек моделей.

> **Совет:** Все свойства с префиксом `spring.ai.openai.chat.options` могут быть переопределены во время выполнения, добавляя специфичные для запроса <<chat-options>> в вызов `Prompt`.### Параметры ограничения токенов: использование, зависящее от модели

OpenAI предоставляет два взаимно исключающих параметра для управления ограничениями на генерацию токенов:

[cols="2,3,3", stripes=even]
|====
| Параметр | Случай использования | Совместимые модели

| `maxTokens` | Модели без рассуждений | gpt-4o, gpt-4o-mini, gpt-4-turbo, gpt-3.5-turbo
| `maxCompletionTokens` | Модели с рассуждениями | o1, o1-mini, o1-preview, o3, o4-mini series
|====

> **Важно:** Эти параметры являются **взаимно исключающими**. Установка обоих приведет к ошибке API от OpenAI.

#### Примеры использования

**Для моделей без рассуждений (gpt-4o, gpt-3.5-turbo):**
```java
ChatResponse response = chatModel.call(
    new Prompt(
        "Объясните квантовые вычисления простыми словами.",
        OpenAiChatOptions.builder()
            .model("gpt-4o")
            .maxTokens(150)  // Используйте maxTokens для моделей без рассуждений
        .build()
    ));
```

**Для моделей с рассуждениями (o1, o3 series):**
```java
ChatResponse response = chatModel.call(
    new Prompt(
        "Решите эту сложную математическую задачу шаг за шагом: ...",
        OpenAiChatOptions.builder()
            .model("o1-preview")
            .maxCompletionTokens(1000)  // Используйте maxCompletionTokens для моделей с рассуждениями
        .build()
    ));
```

**Проверка паттерна строителя:**
Строитель OpenAI ChatOptions автоматически обеспечивает взаимную исключительность с подходом "последний установленный выигрывает":

```java
// Это автоматически очистит maxTokens и использует maxCompletionTokens
OpenAiChatOptions options = OpenAiChatOptions.builder()
    .maxTokens(100)           // Установить первым
    .maxCompletionTokens(200) // Это очищает maxTokens и записывает предупреждение
    .build();

// Результат: maxTokens = null, maxCompletionTokens = 200
```

## Параметры времени выполнения [[chat-options]]

Класс https://github.com/spring-projects/spring-ai/blob/main/models/spring-ai-openai/src/main/java/org/springframework/ai/openai/OpenAiChatOptions.java[OpenAiChatOptions.java] предоставляет конфигурации модели, такие как используемая модель, температура, штраф за частоту и т. д.

При запуске параметры по умолчанию могут быть настроены с помощью конструктора `OpenAiChatModel(api, options)` или свойств `spring.ai.openai.chat.options.*`.

Во время выполнения вы можете переопределить параметры по умолчанию, добавив новые, специфичные для запроса, параметры в вызов `Prompt`.
Например, чтобы переопределить модель и температуру по умолчанию для конкретного запроса:

```java
ChatResponse response = chatModel.call(
    new Prompt(
        "Сгенерируйте имена 5 известных пиратов.",
        OpenAiChatOptions.builder()
            .model("gpt-4o")
            .temperature(0.4)
        .build()
    ));
```

> **Совет:** В дополнение к специфичным для модели https://github.com/spring-projects/spring-ai/blob/main/models/spring-ai-openai/src/main/java/org/springframework/ai/openai/OpenAiChatOptions.java[OpenAiChatOptions] вы можете использовать переносимый [ChatOptions](https://github.com/spring-projects/spring-ai/blob/main/spring-ai-model/src/main/java/org/springframework/ai/chat/prompt/ChatOptions.java) экземпляр, созданный с помощью [ChatOptions#builder()](https://github.com/spring-projects/spring-ai/blob/main/spring-ai-model/src/main/java/org/springframework/ai/chat/prompt/DefaultChatOptionsBuilder.java).

## Вызов функций

Вы можете зарегистрировать пользовательские функции Java с `OpenAiChatModel` и позволить модели OpenAI интеллектуально выбирать вывод JSON-объекта, содержащего аргументы для вызова одной или нескольких зарегистрированных функций.
Это мощная техника для соединения возможностей LLM с внешними инструментами и API.
Узнайте больше о xref:api/tools.adoc[Вызов инструментов].

## Мультимодальность

Мультимодальность относится к способности модели одновременно понимать и обрабатывать информацию из различных источников, включая текст, изображения, аудио и другие форматы данных.
OpenAI поддерживает текстовые, визуальные и аудио входные модальности.

### ВизуализацияOpenAI модели, которые предлагают поддержку многомодальных возможностей, включают `gpt-4`, `gpt-4o` и `gpt-4o-mini`. Для получения дополнительной информации обратитесь к [руководству по Vision](https://platform.openai.com/docs/guides/vision).

OpenAI [User Message API](https://platform.openai.com/docs/api-reference/chat/create#chat-create-messages) может включать список изображений в формате base64 или URL изображений вместе с сообщением. Интерфейс [Message](https://github.com/spring-projects/spring-ai/blob/main/spring-ai-model/src/main/java/org/springframework/ai/chat/messages/Message.java) от Spring AI облегчает работу с многомодальными AI моделями, вводя тип [Media](https://github.com/spring-projects/spring-ai/blob/main/spring-ai-commons/src/main/java/org/springframework/ai/content/Media.java). Этот тип охватывает данные и детали, касающиеся медиа-вложений в сообщениях, используя `org.springframework.util.MimeType` и `org.springframework.core.io.Resource` для необработанных медиа-данных.

Ниже приведен пример кода, взятый из [OpenAiChatModelIT.java](https://github.com/spring-projects/spring-ai/blob/c9a3e66f90187ce7eae7eb78c462ec622685de6c/models/spring-ai-openai/src/test/java/org/springframework/ai/openai/chat/OpenAiChatModelIT.java#L293), иллюстрирующий объединение пользовательского текста с изображением с использованием модели `gpt-4o`.

```java
var imageResource = new ClassPathResource("/multimodal.test.png");

var userMessage = new UserMessage("Объясните, что вы видите на этом изображении?",
        new Media(MimeTypeUtils.IMAGE_PNG, this.imageResource));

ChatResponse response = chatModel.call(new Prompt(this.userMessage,
        OpenAiChatOptions.builder().model(OpenAiApi.ChatModel.GPT_4_O.getValue()).build()));
```

> **Совет:** GPT_4_VISION_PREVIEW будет доступен только для существующих пользователей этой модели с 17 июня 2024 года. Если вы не являетесь существующим пользователем, пожалуйста, используйте модели GPT_4_O или GPT_4_TURBO. Более подробная информация [здесь](https://platform.openai.com/docs/deprecations/2024-06-06-gpt-4-32k-and-vision-preview-models).

или эквивалент URL изображения с использованием модели `gpt-4o`:

```java
var userMessage = new UserMessage("Объясните, что вы видите на этом изображении?",
        new Media(MimeTypeUtils.IMAGE_PNG,
                URI.create("https://docs.spring.io/spring-ai/reference/_images/multimodal.test.png")));

ChatResponse response = chatModel.call(new Prompt(this.userMessage,
        OpenAiChatOptions.builder().model(OpenAiApi.ChatModel.GPT_4_O.getValue()).build()));
```

> **Совет:** Вы также можете передавать несколько изображений.

Пример показывает модель, принимающую в качестве входных данных изображение `multimodal.test.png`:

![Multimodal Test Image, 200, 200, align="left"](multimodal.test.png)

вместе с текстовым сообщением "Объясните, что вы видите на этом изображении?", и генерирующую ответ, подобный этому:

```
Это изображение фруктовой миски с простым дизайном. Миска сделана из металла с изогнутыми проволочными краями, которые создают открытую структуру, позволяя фруктам быть видимыми со всех сторон. Внутри миски находятся два желтых банана, лежащих на том, что, похоже, является красным яблоком. Бананы слегка перезрелые, о чем свидетельствуют коричневые пятна на их кожуре. У миски есть металлическое кольцо сверху, вероятно, для удобства переноски. Миска стоит на ровной поверхности с нейтральным фоном, который обеспечивает четкий вид на фрукты внутри.
```

### АудиоOpenAI модели, которые предлагают поддержку мультимодального ввода аудио, включают `gpt-4o-audio-preview`. 
Смотрите руководство по [Audio](https://platform.openai.com/docs/guides/audio) для получения дополнительной информации.

OpenAI [User Message API](https://platform.openai.com/docs/api-reference/chat/create#chat-create-messages) может включать список аудиофайлов, закодированных в base64, вместе с сообщением. 
Интерфейс [Message](https://github.com/spring-projects/spring-ai/blob/main/spring-ai-model/src/main/java/org/springframework/ai/chat/messages/Message.java) от Spring AI облегчает работу с мультимодальными AI моделями, вводя тип [Media](https://github.com/spring-projects/spring-ai/blob/main/spring-ai-commons/src/main/java/org/springframework/ai/content/Media.java). 
Этот тип охватывает данные и детали, касающиеся медиа-вложений в сообщениях, используя `org.springframework.util.MimeType` от Spring и `org.springframework.core.io.Resource` для необработанных медиа-данных. 
В настоящее время OpenAI поддерживает только следующие типы медиа: `audio/mp3` и `audio/wav`.

Ниже приведен пример кода, извлеченный из [OpenAiChatModelIT.java](https://github.com/spring-projects/spring-ai/blob/c9a3e66f90187ce7eae7eb78c462ec622685de6c/models/spring-ai-openai/src/test/java/org/springframework/ai/openai/chat/OpenAiChatModelIT.java#L442), иллюстрирующий объединение пользовательского текста с аудиофайлом, используя модель `gpt-4o-audio-preview`.

```java
var audioResource = new ClassPathResource("speech1.mp3");

var userMessage = new UserMessage("Что это за запись?",
        List.of(new Media(MimeTypeUtils.parseMimeType("audio/mp3"), audioResource)));

ChatResponse response = chatModel.call(new Prompt(List.of(userMessage),
        OpenAiChatOptions.builder().model(OpenAiApi.ChatModel.GPT_4_O_AUDIO_PREVIEW).build()));
```

> **Совет:** Вы также можете передавать несколько аудиофайлов.

### Выходное аудио

OpenAI модели, которые предлагают поддержку мультимодального ввода аудио, включают `gpt-4o-audio-preview`. 
Смотрите руководство по [Audio](https://platform.openai.com/docs/guides/audio) для получения дополнительной информации.

OpenAI [Assistant Message API](https://platform.openai.com/docs/api-reference/chat/create#chat-create-messages) может содержать список аудиофайлов, закодированных в base64, вместе с сообщением. 
Интерфейс [Message](https://github.com/spring-projects/spring-ai/blob/main/spring-ai-model/src/main/java/org/springframework/ai/chat/messages/Message.java) от Spring AI облегчает работу с мультимодальными AI моделями, вводя тип [Media](https://github.com/spring-projects/spring-ai/blob/main/spring-ai-commons/src/main/java/org/springframework/ai/content/Media.java). 
Этот тип охватывает данные и детали, касающиеся медиа-вложений в сообщениях, используя `org.springframework.util.MimeType` от Spring и `org.springframework.core.io.Resource` для необработанных медиа-данных. 
В настоящее время OpenAI поддерживает только следующие аудиотипы: `audio/mp3` и `audio/wav`.

Ниже приведен пример кода, иллюстрирующий ответ пользовательского текста вместе с массивом байтов аудио, используя модель `gpt-4o-audio-preview`:

```java
var userMessage = new UserMessage("Расскажи мне шутку о Spring Framework");

ChatResponse response = chatModel.call(new Prompt(List.of(userMessage),
        OpenAiChatOptions.builder()
            .model(OpenAiApi.ChatModel.GPT_4_O_AUDIO_PREVIEW)
            .outputModalities(List.of("text", "audio"))
            .outputAudio(new AudioParameters(Voice.ALLOY, AudioResponseFormat.WAV))
            .build()));

String text = response.getResult().getOutput().getText(); // транскрипция аудио

byte[] waveAudio = response.getResult().getOutput().getMedia().get(0).getDataAsByteArray(); // аудиоданные
```

Вы должны указать модальность `audio` в `OpenAiChatOptions`, чтобы сгенерировать аудиовыход. 
Класс `AudioParameters` предоставляет голос и аудиоформат для аудиовыхода.

## Структурированные выходные данныеOpenAI предоставляет настраиваемые https://platform.openai.com/docs/guides/structured-outputs[Structured Outputs] API, которые гарантируют, что ваша модель генерирует ответы, строго соответствующие предоставленной вами `JSON Schema`. В дополнение к существующему xref::api/structured-output-converter.adoc[Structured Output Converter], эти API предлагают улучшенный контроль и точность.

> **Примечание:** В настоящее время OpenAI поддерживает формат [подмножества языка JSON Schema](https://platform.openai.com/docs/guides/structured-outputs/supported-schemas).

### Конфигурация

Spring AI позволяет настраивать формат ответа как программно с помощью билдера `OpenAiChatOptions`, так и через свойства приложения.

#### Использование билдера Chat Options

Вы можете установить формат ответа программно с помощью билдера `OpenAiChatOptions`, как показано ниже:

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

Prompt prompt = new Prompt("как мне решить 8x + 7 = -23",
        OpenAiChatOptions.builder()
            .model(ChatModel.GPT_4_O_MINI)
            .responseFormat(new ResponseFormat(ResponseFormat.Type.JSON_SCHEMA, this.jsonSchema))
            .build());

ChatResponse response = this.openAiChatModel.call(this.prompt);
```

> **Примечание:** Соблюдайте формат [подмножества языка JSON Schema](https://platform.openai.com/docs/guides/structured-outputs/supported-schemas).

#### Интеграция с утилитами BeanOutputConverterВы можете использовать существующие xref::api/structured-output-converter.adoc#_bean_output_converter[BeanOutputConverter] утилиты для автоматической генерации JSON Schema из ваших доменных объектов, а затем преобразования структурированного ответа в экземпляры, специфичные для домена:

--
[tabs]
======
Java::
+
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

var jsonSchema = this.outputConverter.getJsonSchema();

Prompt prompt = new Prompt("как я могу решить 8x + 7 = -23",
        OpenAiChatOptions.builder()
            .model(ChatModel.GPT_4_O_MINI)
            .responseFormat(new ResponseFormat(ResponseFormat.Type.JSON_SCHEMA, this.jsonSchema))
            .build());

ChatResponse response = this.openAiChatModel.call(this.prompt);
String content = this.response.getResult().getOutput().getText();

MathReasoning mathReasoning = this.outputConverter.convert(this.content);
```
Kotlin::
+
```kotlin
data class MathReasoning(
	val steps: Steps,
	@get:JsonProperty(value = "final_answer") val finalAnswer: String) {

	data class Steps(val items: Array<Items>) {

		data class Items(
			val explanation: String,
			val output: String)
	}
}

val outputConverter = BeanOutputConverter(MathReasoning::class.java)

val jsonSchema = outputConverter.jsonSchema;

val prompt = Prompt("как я могу решить 8x + 7 = -23",
	OpenAiChatOptions.builder()
		.model(ChatModel.GPT_4_O_MINI)
		.responseFormat(ResponseFormat(ResponseFormat.Type.JSON_SCHEMA, jsonSchema))
		.build())

val response = openAiChatModel.call(prompt)
val content = response.getResult().getOutput().getText()

val mathReasoning = outputConverter.convert(content)
```
======
--

> **Примечание:** Хотя это и не обязательно для JSON Schema, OpenAI [обязывает](https://platform.openai.com/docs/guides/structured-outputs/all-fields-must-be-required#all-fields-must-be-required) обязательные поля для корректной работы структурированного ответа. В Kotlin используется рефлексия для определения, какие свойства являются обязательными, а какие нет, на основе нулевости типов и значений по умолчанию параметров, поэтому для большинства случаев `@get:JsonProperty(required = true)` не требуется. `@get:JsonProperty(value = "custom_name")` может быть полезен для настройки имени свойства. Убедитесь, что аннотация генерируется на соответствующих геттерах с помощью синтаксиса `@get:`, см. [сопутствующую документацию](https://kotlinlang.org/docs/annotations.html#annotation-use-site-targets).

#### Настройка через свойства приложения

В качестве альтернативы, при использовании автонастройки OpenAI, вы можете настроить желаемый формат ответа через следующие свойства приложения:

```application.properties
spring.ai.openai.api-key=YOUR_API_KEY
spring.ai.openai.chat.options.model=gpt-4o-mini

spring.ai.openai.chat.options.response-format.type=JSON_SCHEMA
spring.ai.openai.chat.options.response-format.name=MySchemaName
spring.ai.openai.chat.options.response-format.schema={"type":"object","properties":{"steps":{"type":"array","items":{"type":"object","properties":{"explanation":{"type":"string"},"output":{"type":"string"}},"required":["explanation","output"],"additionalProperties":false}},"final_answer":{"type":"string"}},"required":["steps","final_answer"],"additionalProperties":false}
spring.ai.openai.chat.options.response-format.strict=true
```

## Пример контроллераhttps://start.spring.io/[Создайте] новый проект Spring Boot и добавьте `spring-ai-starter-model-openai` в зависимости вашего pom (или gradle).

Добавьте файл `application.properties` в директорию `src/main/resources`, чтобы включить и настроить модель чата OpenAi:

```application.properties
spring.ai.openai.api-key=YOUR_API_KEY
spring.ai.openai.chat.options.model=gpt-4o
spring.ai.openai.chat.options.temperature=0.7
```

> **Совет:** Замените `api-key` на ваши учетные данные OpenAI.

Это создаст реализацию `OpenAiChatModel`, которую вы сможете внедрить в ваши классы. Вот пример простого класса `@RestController`, который использует модель чата для генерации текста.

```java
@RestController
public class ChatController {

    private final OpenAiChatModel chatModel;

    @Autowired
    public ChatController(OpenAiChatModel chatModel) {
        this.chatModel = chatModel;
    }

    @GetMapping("/ai/generate")
    public Map<String,String> generate(@RequestParam(value = "message", defaultValue = "Расскажи мне шутку") String message) {
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

> **Совет:** Обратитесь к разделу xref:getting-started.adoc#dependency-management[Управление зависимостями], чтобы добавить Spring AI BOM в ваш файл сборки.

Далее создайте `OpenAiChatModel` и используйте его для генерации текста:

```java
var openAiApi = OpenAiApi.builder()
            .apiKey(System.getenv("OPENAI_API_KEY"))
            .build();
var openAiChatOptions = OpenAiChatOptions.builder()
            .model("gpt-3.5-turbo")
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

`OpenAiChatOptions` предоставляет информацию о конфигурации для запросов чата. `OpenAiApi.Builder` и `OpenAiChatOptions.Builder` являются флюентными билдерами для клиента API и конфигурации чата соответственно.

## Низкоуровневый клиент OpenAiApi [[low-level-api]]The https://github.com/spring-projects/spring-ai/blob/main/models/spring-ai-openai/src/main/java/org/springframework/ai/openai/api/OpenAiFileApi.java[OpenAiFileApi] предоставляет легковесный Java-клиент для OpenAI Files API, позволяющий выполнять операции управления файлами, такие как загрузка, перечисление, получение, удаление файлов и доступ к содержимому файлов. [OpenAI File API](https://platform.openai.com/docs/api-reference/files)

Вот простой фрагмент, показывающий, как использовать API программно:

```java
OpenAiFileApi openAiFileApi = OpenAiFileApi.builder()
			.apiKey(new SimpleApiKey(System.getenv("OPENAI_API_KEY")))
			.build();

// Загрузка файла
byte[] fileBytes = Files.readAllBytes(Paths.get("evals.jsonl")); 
OpenAiFileApi.UploadFileRequest uploadRequest = OpenAiFileApi.UploadFileRequest.builder()
			.file(fileBytes)
			.fileName("evals-data.jsonl")
			.purpose(OpenAiFileApi.Purpose.EVALS)
			.build();
ResponseEntity<OpenAiFileApi.FileObject> uploadResponse = openAiFileApi.uploadFile(uploadRequest);

// Перечисление файлов
OpenAiFileApi.ListFileRequest listRequest = OpenAiFileApi.ListFileRequest.builder()
			.purpose(OpenAiFileApi.Purpose.EVALS)
			.build();
ResponseEntity<OpenAiFileApi.FileObjectResponse> listResponse = openAiFileApi.listFiles(listRequest);

// Получение информации о файле
ResponseEntity<OpenAiFileApi.FileObject> fileInfo = openAiFileApi.retrieveFile("file-id");

// Удаление файла
ResponseEntity<OpenAiFileApi.DeleteFileResponse> deleteResponse = openAiFileApi.deleteFile("file-id");

// Получение содержимого файла
ResponseEntity<String> fileContent = openAiFileApi.retrieveFileContent("file-id");
```

### Примеры низкоуровневого API файлов- Тесты [OpenAiFileApiIT.java](https://github.com/spring-projects/spring-ai/blob/main/models/spring-ai-openai/src/test/java/org/springframework/ai/openai/api/OpenAiFileApiIT.java) предоставляют общие примеры использования легковесной библиотеки API для работы с файлами.

## Управление API-ключами

Spring AI предоставляет гибкое управление API-ключами через интерфейс `ApiKey` и его реализации. Стандартная реализация, `SimpleApiKey`, подходит для большинства случаев, но вы также можете создать собственные реализации для более сложных сценариев.

### Стандартная конфигурация

По умолчанию автоконфигурация Spring Boot создаст бин API-ключа, используя свойство `spring.ai.openai.api-key`:

```properties
spring.ai.openai.api-key=ваш-api-ключ-здесь
```

### Настройка пользовательского API-ключа

Вы можете создать пользовательский экземпляр `OpenAiApi` с вашей собственной реализацией `ApiKey`, используя паттерн строителя:

```java
ApiKey customApiKey = new ApiKey() {
    @Override
    public String getValue() {
        // Пользовательская логика для получения API-ключа
        return "ваш-api-ключ-здесь";
    }
};

OpenAiApi openAiApi = OpenAiApi.builder()
    .apiKey(customApiKey)
    .build();

// Создайте модель чата с пользовательским экземпляром OpenAiApi
OpenAiChatModel chatModel = OpenAiChatModel.builder()
    .openAiApi(openAiApi)
    .build();
// Создайте ChatClient, используя пользовательскую модель чата
ChatClient openAiChatClient = ChatClient.builder(chatModel).build();
```

Это полезно, когда вам нужно:

- Получить API-ключ из безопасного хранилища ключей
- Динамически изменять API-ключи
- Реализовать пользовательскую логику выбора API-ключа

## Использование дополнительных параметров с серверами, совместимыми с OpenAI [[openai-compatible-servers]]

Сервера вывода, совместимые с OpenAI, такие как vLLM, Ollama и другие, часто поддерживают дополнительные параметры, помимо тех, что определены в стандартном API OpenAI. Например, эти серверы могут принимать такие параметры, как `top_k`, `repetition_penalty` или другие управляющие параметры выборки, которые официальный API OpenAI не распознает.

Опция `extraBody` позволяет вам передавать произвольные параметры этим серверам. Любые пары ключ-значение, предоставленные в `extraBody`, включаются на верхнем уровне JSON-запроса, что позволяет вам использовать специфические для сервера функции, используя клиент OpenAI от Spring AI.

[ВАЖНО]
====
Параметр `extraBody` предназначен для использования с серверами, совместимыми с OpenAI, а не с официальным API OpenAI. Хотя официальный API OpenAI будет игнорировать неизвестные параметры, они не имеют смысла там. Всегда обращайтесь к документации вашего конкретного сервера, чтобы определить, какие параметры поддерживаются.
====

### Конфигурация с помощью свойств

Вы можете настроить дополнительные параметры, используя свойства Spring Boot. Каждое свойство под `spring.ai.openai.chat.options.extra-body` становится параметром верхнего уровня в запросе:

```properties
spring.ai.openai.base-url=http://localhost:8000
spring.ai.openai.chat.options.model=meta-llama/Llama-3-8B-Instruct
spring.ai.openai.chat.options.temperature=0.7
spring.ai.openai.chat.options.extra-body.top_k=50
spring.ai.openai.chat.options.extra-body.repetition_penalty=1.1
```

Эта конфигурация создаст JSON-запрос, подобный следующему:

```json
{
  "model": "meta-llama/Llama-3-8B-Instruct",
  "temperature": 0.7,
  "top_k": 50,
  "repetition_penalty": 1.1,
  "messages": [...]
}
```

### Конфигурация во время выполнения с помощью строителя

Вы также можете указать дополнительные параметры во время выполнения, используя билдера опций:

```java
ChatResponse response = chatModel.call(
    new Prompt(
        "Расскажи мне креативную историю",
        OpenAiChatOptions.builder()
            .model("meta-llama/Llama-3-8B-Instruct")
            .temperature(0.7)
            .extraBody(Map.of(
                "top_k", 50,
                "repetition_penalty", 1.1,
                "frequency_penalty", 0.5
            ))
            .build()
    ));
```

### Пример: сервер vLLMКогда вы запускаете vLLM с моделью Llama, вы можете использовать параметры выборки, специфичные для vLLM:

```properties
spring.ai.openai.base-url=http://localhost:8000
spring.ai.openai.chat.options.model=meta-llama/Llama-3-70B-Instruct
spring.ai.openai.chat.options.extra-body.top_k=40
spring.ai.openai.chat.options.extra-body.top_p=0.95
spring.ai.openai.chat.options.extra-body.repetition_penalty=1.05
spring.ai.openai.chat.options.extra-body.min_p=0.05
```

Обратитесь к [документации vLLM](https://docs.vllm.ai/en/latest/) для получения полного списка поддерживаемых параметров выборки.

### Пример: Сервер Ollama

При использовании Ollama через совместимый с OpenAI конечный пункт вы можете передавать параметры, специфичные для Ollama:

```java
OpenAiChatOptions options = OpenAiChatOptions.builder()
    .model("llama3.2")
    .extraBody(Map.of(
        "num_predict", 100,
        "top_k", 40,
        "repeat_penalty", 1.1
    ))
    .build();

ChatResponse response = chatModel.call(new Prompt("Сгенерировать текст", options));
```

Обратитесь к [документации API Ollama](https://github.com/ollama/ollama/blob/main/docs/api.md) для получения доступных параметров.

[ПРИМЕЧАНИЕ]
====
Параметр `extraBody` принимает любой `Map<String, Object>`, что позволяет вам передавать любые параметры, поддерживаемые вашим целевым сервером.
Spring AI не проверяет эти параметры — они передаются напрямую на сервер.
Этот дизайн обеспечивает максимальную гибкость для работы с различными реализациями, совместимыми с OpenAI.
====

### Содержимое рассуждений от моделей рассуждений

Некоторые серверы, совместимые с OpenAI и поддерживающие модели рассуждений (такие как DeepSeek R1, vLLM с парсерами рассуждений), предоставляют внутреннюю цепочку размышлений модели через поле `reasoning_content` в своих ответах API.
Это поле содержит пошаговый процесс рассуждений, который модель использовала для достижения своего окончательного ответа.

Spring AI сопоставляет это поле из JSON-ответа с ключом `reasoningContent` в метаданных AssistantMessage.

[ВАЖНО]
====
**Важное различие в доступности `reasoning_content`:**

- **Серверы, совместимые с OpenAI** (DeepSeek, vLLM): Предоставляют `reasoning_content` в ответах API Chat Completions ✅
- **Официальные модели OpenAI** (GPT-5, o1, o3): **НЕ** предоставляют текст рассуждений в ответах API Chat Completions ❌

Официальные модели рассуждений OpenAI скрывают содержимое цепочки размышлений при использовании API Chat Completions.
Они только предоставляют количество `reasoning_tokens` в статистике использования.
Чтобы получить фактический текст рассуждений от официальных моделей OpenAI, вы должны использовать API Responses OpenAI (отдельный конечный пункт, который в настоящее время не поддерживается этим клиентом).

**Резервное поведение:** Когда сервер не предоставляет `reasoning_content` (например, официальные Chat Completions OpenAI), поле метаданных `reasoningContent` будет пустой строкой.
====

#### Доступ к содержимому рассужденийКогда вы используете совместимый сервер, вы можете получить доступ к содержимому рассуждений из метаданных ответа.

**Использование ChatModel напрямую:**

```java
// Настройка для использования DeepSeek R1 или vLLM с моделью рассуждений
ChatResponse response = chatModel.call(
    new Prompt("Какое число больше: 9.11 или 9.8?")
);

// Получение сообщения от ассистента
AssistantMessage message = response.getResult().getOutput();

// Доступ к содержимому рассуждений из метаданных
String reasoning = message.getMetadata().get("reasoningContent");
if (reasoning != null && !reasoning.isEmpty()) {
    System.out.println("Процесс рассуждений модели:");
    System.out.println(reasoning);
}

// Окончательный ответ находится в обычном содержимом
System.out.println("\nОкончательный ответ:");
System.out.println(message.getContent());
```

**Использование ChatClient:**

```java
ChatClient chatClient = ChatClient.create(chatModel);

String result = chatClient.prompt()
    .user("Какое число больше: 9.11 или 9.8?")
    .call()
    .chatResponse()
    .getResult()
    .getOutput()
    .getContent();

// Чтобы получить доступ к содержимому рассуждений с помощью ChatClient, получите полный ответ
ChatResponse response = chatClient.prompt()
    .user("Какое число больше: 9.11 или 9.8?")
    .call()
    .chatResponse();

AssistantMessage message = response.getResult().getOutput();
String reasoning = message.getMetadata().get("reasoningContent");
```

#### Потоковое содержимое рассуждений

При использовании потоковых ответов содержимое рассуждений накапливается по частям, как и обычное содержимое сообщений:

```java
Flux<ChatResponse> responseFlux = chatModel.stream(
    new Prompt("Решите эту логическую задачу...")
);

StringBuilder reasoning = new StringBuilder();
StringBuilder answer = new StringBuilder();

responseFlux.subscribe(chunk -> {
    AssistantMessage message = chunk.getResult().getOutput();

    // Накопление рассуждений, если они присутствуют
    String reasoningChunk = message.getMetadata().get("reasoningContent");
    if (reasoningChunk != null) {
        reasoning.append(reasoningChunk);
    }

    // Накопление окончательного ответа
    if (message.getContent() != null) {
        answer.append(message.getContent());
    }
});
```

#### Пример: DeepSeek R1

DeepSeek R1 — это модель рассуждений, которая раскрывает свой внутренний процесс рассуждений:

```properties
spring.ai.openai.api-key=${DEEPSEEK_API_KEY}
spring.ai.openai.base-url=https://api.deepseek.com
spring.ai.openai.chat.options.model=deepseek-reasoner
```

Когда вы делаете запросы к DeepSeek R1, ответы будут включать как содержимое рассуждений (процесс мышления модели), так и окончательный ответ.

Смотрите [документацию API DeepSeek](https://api-docs.deepseek.com/guides/reasoning_model) для получения дополнительной информации о моделях рассуждений.

#### Пример: vLLM с парсером рассуждений

vLLM поддерживает модели рассуждений, когда настроен с парсером рассуждений:

```bash
vllm serve deepseek-ai/DeepSeek-R1-Distill-Qwen-1.5B \
    --enable-reasoning \
    --reasoning-parser deepseek_r1
```

```properties
spring.ai.openai.base-url=http://localhost:8000
spring.ai.openai.chat.options.model=deepseek-ai/DeepSeek-R1-Distill-Qwen-1.5B
```

Обратитесь к [документации по выходным данным рассуждений vLLM](https://docs.vllm.ai/en/latest/features/reasoning_outputs.html) для получения информации о поддерживаемых моделях и парсерах рассуждений.

[ПРИМЕЧАНИЕ]
====
Доступность `reasoning_content` полностью зависит от сервера вывода, который вы используете.
Не все совместимые с OpenAI серверы предоставляют содержимое рассуждений, даже при использовании моделей, способных к рассуждениям.
Всегда обращайтесь к документации API вашего сервера, чтобы понять, какие поля доступны в ответах.
====
