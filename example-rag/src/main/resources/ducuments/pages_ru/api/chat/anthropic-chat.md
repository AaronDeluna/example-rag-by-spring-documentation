# Anthropic Chat

[Anthropic Claude](https://www.anthropic.com/) — это семейство базовых моделей ИИ, которые можно использовать в различных приложениях. Для разработчиков и бизнеса вы можете воспользоваться доступом к API и строить свои решения на основе [инфраструктуры ИИ Anthropic](https://www.anthropic.com/api).

Spring AI поддерживает Anthropic [Messaging API](https://docs.anthropic.com/claude/reference/messages_post) для синхронной и потоковой генерации текста.

> **Совет:** Модели Claude от Anthropic также доступны через Amazon Bedrock Converse. Spring AI предоставляет специализированные реализации клиента xref:api/chat/bedrock-converse.adoc[Amazon Bedrock Converse Anthropic].

## Предварительные требования

Вам необходимо создать API-ключ на портале Anthropic.

Создайте учетную запись на https://console.anthropic.com/dashboard[панель управления API Anthropic] и сгенерируйте API-ключ на странице https://console.anthropic.com/settings/keys[Получить API-ключи].

Проект Spring AI определяет свойство конфигурации с именем `spring.ai.anthropic.api-key`, которое вы должны установить в значение `API Key`, полученного с anthropic.com.

Вы можете установить это свойство конфигурации в вашем файле `application.properties`:

```properties
spring.ai.anthropic.api-key=<ваш-anthropic-api-ключ>
```

Для повышения безопасности при работе с конфиденциальной информацией, такой как API-ключи, вы можете использовать язык выражений Spring (SpEL) для ссылки на пользовательскую переменную окружения:

```yaml
# В application.yml
spring:
  ai:
    anthropic:
      api-key: ${ANTHROPIC_API_KEY}
```

```bash
# В вашей среде или .env файле
export ANTHROPIC_API_KEY=<ваш-anthropic-api-ключ>
```

Вы также можете получить эту конфигурацию программно в вашем коде приложения:

```java
// Получение API-ключа из безопасного источника или переменной окружения
String apiKey = System.getenv("ANTHROPIC_API_KEY");
```

### Добавление репозиториев и BOM

Артефакты Spring AI публикуются в репозиториях Maven Central и Spring Snapshot. Обратитесь к разделу xref:getting-started.adoc#artifact-repositories[Репозитории артефактов], чтобы добавить эти репозитории в вашу систему сборки.

Чтобы помочь с управлением зависимостями, Spring AI предоставляет BOM (спецификация материалов), чтобы гарантировать, что одна и та же версия Spring AI используется на протяжении всего проекта. Обратитесь к разделу xref:getting-started.adoc#dependency-management[Управление зависимостями], чтобы добавить BOM Spring AI в вашу систему сборки.

## Автоконфигурация

[NOTE]
====
В автоконфигурации Spring AI произошли значительные изменения в названиях артефактов модулей-стартеров. Пожалуйста, обратитесь к https://docs.spring.io/spring-ai/reference/upgrade-notes.html[заметкам об обновлении] для получения дополнительной информации.
====

Spring AI предоставляет автоконфигурацию Spring Boot для клиента Anthropic Chat. Чтобы включить ее, добавьте следующую зависимость в файл Maven `pom.xml` или Gradle `build.gradle` вашего проекта:

[tabs]
======
Maven::
+
```xml
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-starter-model-anthropic</artifactId>
</dependency>
```

Gradle::
+
```groovy
dependencies {
    implementation 'org.springframework.ai:spring-ai-starter-model-anthropic'
}
```
======

> **Совет:** Обратитесь к разделу xref:getting-started.adoc#dependency-management[Управление зависимостями], чтобы добавить BOM Spring AI в ваш файл сборки.

### Свойства чата

#### Свойства повторной попыткиThe prefix `spring.ai.retry` используется как префикс свойств, который позволяет настраивать механизм повторных попыток для модели чата Anthropic.

[cols="3,5,1", stripes=even]
|====
| Свойство | Описание | По умолчанию

| spring.ai.retry.max-attempts   | Максимальное количество попыток повторного запроса. |  10
| spring.ai.retry.backoff.initial-interval | Начальная продолжительность ожидания для политики экспоненциального увеличения интервала. |  2 сек.
| spring.ai.retry.backoff.multiplier | Множитель для увеличения интервала ожидания. |  5
| spring.ai.retry.backoff.max-interval | Максимальная продолжительность ожидания. |  3 мин.
| spring.ai.retry.on-client-errors | Если false, выбрасывается NonTransientAiException, и повторная попытка не осуществляется для кодов ошибок клиента `4xx` | false
| spring.ai.retry.exclude-on-http-codes | Список кодов состояния HTTP, которые не должны вызывать повторную попытку (например, для выбрасывания NonTransientAiException). | пусто
| spring.ai.retry.on-http-codes | Список кодов состояния HTTP, которые должны вызывать повторную попытку (например, для выбрасывания TransientAiException). | пусто
|====

> **Примечание:** в настоящее время политики повторных попыток не применимы для потокового API.

#### Свойства подключения

Префикс `spring.ai.anthropic` используется как префикс свойств, который позволяет подключаться к Anthropic.

[cols="3,5,1", stripes=even]
|====
| Свойство | Описание | По умолчанию

| spring.ai.anthropic.base-url   | URL для подключения |  https://api.anthropic.com
| spring.ai.anthropic.completions-path   | Путь, который добавляется к базовому URL. |  `/v1/chat/completions`
| spring.ai.anthropic.version   | Версия API Anthropic |  2023-06-01
| spring.ai.anthropic.api-key    | API-ключ           |  -
| spring.ai.anthropic.beta-version | Включает новые/экспериментальные функции. Если установлено значение `max-tokens-3-5-sonnet-2024-07-15`, лимит выходных токенов увеличивается с `4096` до `8192` токенов (только для claude-3-5-sonnet). | `tools-2024-04-04`
|====

#### Свойства конфигурации[NOTE]
====
Включение и отключение автонастроек чата теперь настраивается через свойства верхнего уровня с префиксом `spring.ai.model.chat`.

Чтобы включить, используйте spring.ai.model.chat=anthropic (по умолчанию включено)

Чтобы отключить, используйте spring.ai.model.chat=none (или любое значение, не совпадающее с anthropic)

Это изменение сделано для возможности конфигурации нескольких моделей.
====

Префикс `spring.ai.anthropic.chat` — это префикс свойства, который позволяет вам настраивать реализацию модели чата для Anthropic.

[cols="3,5,1", stripes=even]
|====
| Свойство | Описание | По умолчанию

| spring.ai.anthropic.chat.enabled (Удалено и больше не действительно) | Включить модель чата Anthropic.  | true
| spring.ai.model.chat | Включить модель чата Anthropic.  | anthropic
| spring.ai.anthropic.chat.options.model | Это модель чата Anthropic, которую нужно использовать. Поддерживает: `claude-sonnet-4-5`, `claude-opus-4-5`, `claude-haiku-4-5`, `claude-opus-4-1`, `claude-opus-4-0`, `claude-sonnet-4-0`, `claude-3-7-sonnet-latest`, `claude-3-5-sonnet-latest`, `claude-3-5-haiku-latest`, `claude-3-opus-latest`, `claude-3-haiku-20240307` | `claude-sonnet-4-5`
| spring.ai.anthropic.chat.options.temperature | Температура выборки, которая контролирует очевидную креативность сгенерированных завершений. Более высокие значения сделают вывод более случайным, в то время как более низкие значения сделают результаты более сфокусированными и детерминированными. Не рекомендуется изменять температуру и top_p для одного и того же запроса завершения, так как взаимодействие этих двух настроек трудно предсказать. | 0.8
| spring.ai.anthropic.chat.options.max-tokens | Максимальное количество токенов, которые нужно сгенерировать в завершении чата. Общая длина входных токенов и сгенерированных токенов ограничена длиной контекста модели. | 500
| spring.ai.anthropic.chat.options.stop-sequence | Пользовательские текстовые последовательности, которые заставят модель прекратить генерацию. Наши модели обычно останавливаются, когда они естественным образом завершили свой ход, что приведет к значению stop_reason ответа "end_turn". Если вы хотите, чтобы модель остановила генерацию при встрече с пользовательскими строками текста, вы можете использовать параметр stop_sequences. Если модель встретит одну из пользовательских последовательностей, значение stop_reason ответа будет "stop_sequence", а значение stop_sequence ответа будет содержать совпавшую последовательность остановки. | -
| spring.ai.anthropic.chat.options.top-p | Используйте выборку по ядру. В выборке по ядру мы вычисляем кумулятивное распределение по всем вариантам для каждого последующего токена в порядке убывания вероятности и обрываем его, как только оно достигает определенной вероятности, указанной в top_p. Вы должны изменять либо температуру, либо top_p, но не оба. Рекомендуется только для продвинутых случаев использования. Обычно вам нужно использовать только температуру. | -
| spring.ai.anthropic.chat.options.top-k | Выбирайте только из топ K вариантов для каждого последующего токена. Используется для удаления "длинного хвоста" низковероятностных ответов. Узнайте больше технических деталей здесь. Рекомендуется только для продвинутых случаев использования. Обычно вам нужно использовать только температуру. | -
| spring.ai.anthropic.chat.options.tool-names | Список инструментов, идентифицированных по их именам, которые можно включить для вызова инструментов в одном запросе. Инструменты с этими именами должны существовать в реестре toolCallbacks. | -
| spring.ai.anthropic.chat.options.tool-callbacks | Обратные вызовы инструментов для регистрации с ChatModel. | -
| spring.ai.anthropic.chat.options.toolChoice | Управляет тем, какой (если есть) инструмент вызывается моделью. `none` означает, что модель не будет вызывать функцию и вместо этого сгенерирует сообщение. `auto` означает, что модель может выбирать между генерацией сообщения или вызовом инструмента. Указание конкретного инструмента через `{"type": "tool", "name": "my_tool"}` заставляет модель вызвать этот инструмент. `none` является значением по умолчанию, когда функции отсутствуют. `auto` является значением по умолчанию, если функции присутствуют. | -
| spring.ai.anthropic.chat.options.internal-tool-execution-enabled | Если false, Spring AI не будет обрабатывать вызовы инструментов внутренне, а будет проксировать их клиенту. Тогда клиент несет ответственность за обработку вызовов инструментов, их распределение на соответствующие функции и возврат результатов. Если true (по умолчанию), Spring AI будет обрабатывать вызовы функций внутренне. Применимо только для моделей чата с поддержкой вызова функций | true
| spring.ai.anthropic.chat.options.http-headers | Дополнительные HTTP-заголовки, которые будут добавлены к запросу завершения чата. | -
|====

> **Совет:** Для получения последнего списка псевдонимов моделей и их описаний смотрите [официальную документацию по псевдонимам моделей Anthropic](https://docs.anthropic.com/en/docs/about-claude/models/overview#model-aliases).

> **Совет:** Все свойства с префиксом `spring.ai.anthropic.chat.options` могут быть переопределены во время выполнения, добавив специфичные для запроса <<chat-options>> в вызов `Prompt`.## Параметры времени выполнения [[chat-options]]

Файл https://github.com/spring-projects/spring-ai/blob/main/models/spring-ai-anthropic/src/main/java/org/springframework/ai/anthropic/AnthropicChatOptions.java[AnthropicChatOptions.java] предоставляет конфигурации модели, такие как используемая модель, температура, максимальное количество токенов и т. д.

При запуске параметры по умолчанию можно настроить с помощью конструктора `AnthropicChatModel(api, options)` или свойств `spring.ai.anthropic.chat.options.*`.

Во время выполнения вы можете переопределить параметры по умолчанию, добавив новые, специфичные для запроса, параметры в вызов `Prompt`.
Например, чтобы переопределить модель и температуру по умолчанию для конкретного запроса:

```java
ChatResponse response = chatModel.call(
    new Prompt(
        "Сгенерируйте имена 5 известных пиратов.",
        AnthropicChatOptions.builder()
            .model("claude-3-7-sonnet-latest")
            .temperature(0.4)
        .build()
    ));
```

> **Совет:** В дополнение к параметрам https://github.com/spring-projects/spring-ai/blob/main/models/spring-ai-anthropic/src/main/java/org/springframework/ai/anthropic/AnthropicChatOptions.java[AnthropicChatOptions], специфичным для модели, вы можете использовать переносимый экземпляр [ChatOptions](https://github.com/spring-projects/spring-ai/blob/main/spring-ai-model/src/main/java/org/springframework/ai/chat/prompt/ChatOptions.java), созданный с помощью [ChatOptions#builder()](https://github.com/spring-projects/spring-ai/blob/main/spring-ai-model/src/main/java/org/springframework/ai/chat/prompt/DefaultChatOptionsBuilder.java).

## Кэширование подсказок

Функция https://docs.anthropic.com/en/docs/build-with-claude/prompt-caching[кэширования подсказок] от Anthropic позволяет кэшировать часто используемые подсказки, чтобы снизить затраты и улучшить время отклика при повторных взаимодействиях.
Когда вы кэшируете подсказку, последующие идентичные запросы могут повторно использовать кэшированное содержимое, значительно уменьшая количество обрабатываемых входных токенов.

[ПРИМЕЧАНИЕ]
====
**Поддерживаемые модели**

Кэширование подсказок в настоящее время поддерживается для Claude Sonnet 4.5, Claude Opus 4.5, Claude Haiku 4.5, Claude Opus 4, Claude Sonnet 4, Claude Sonnet 3.7, Claude Sonnet 3.5, Claude Haiku 3.5, Claude Haiku 3 и Claude Opus 3.

**Требования к токенам**

Разные модели имеют разные минимальные пороги токенов для эффективности кэширования:
- Claude Sonnet 4: 1024+ токенов
- Модели Claude Haiku: 2048+ токенов
- Другие модели: 1024+ токенов
====

### Стратегии кэширования```markdown
Spring AI предоставляет стратегическое размещение кэша через перечисление `AnthropicCacheStrategy`.
Каждая стратегия автоматически размещает точки прерывания кэша в оптимальных местах, оставаясь в пределах лимита в 4 точки прерывания от Anthropic.

[cols="2,3,5", stripes=even]
|====
| Стратегия | Используемые точки прерывания | Случай использования

| `NONE`
| 0
| Полностью отключает кэширование подсказок.
Используйте, когда запросы одноразовые или содержимое слишком мало, чтобы извлечь выгоду из кэширования.

| `SYSTEM_ONLY`
| 1
| Кэширует содержимое системного сообщения.
Инструменты кэшируются неявно через автоматический механизм обратного просмотра на ~20 блоков от Anthropic.
Используйте, когда системные подсказки большие и стабильные с менее чем 20 инструментами.

| `TOOLS_ONLY`
| 1
| Кэширует только определения инструментов. Системные сообщения остаются некэшированными и обрабатываются заново при каждом запросе.
Используйте, когда определения инструментов большие и стабильные (5000+ токенов), но системные подсказки часто меняются или варьируются в зависимости от арендатора/контекста.

| `SYSTEM_AND_TOOLS`
| 2
| Явно кэширует как определения инструментов (точка прерывания 1), так и системное сообщение (точка прерывания 2).
Используйте, когда у вас 20+ инструментов (выше автоматического обратного просмотра) или вы хотите детерминированное кэширование обоих компонентов.
Изменения в системе не аннулируют кэш инструментов.

| `CONVERSATION_HISTORY`
| 1-4
| Кэширует всю историю разговора до текущего вопроса пользователя.
Используйте для многоповоротных разговоров с памятью чата, где история разговора со временем растет.
|====

> **Важно:** Из-за каскадной аннулирования от Anthropic изменение определений инструментов аннулирует ВСЕ последующие точки прерывания кэша (системные, сообщения).
Стабильность инструментов критична при использовании стратегий `SYSTEM_AND_TOOLS` или `CONVERSATION_HISTORY`.

### Включение кэширования подсказок

Включите кэширование подсказок, установив `cacheOptions` в `AnthropicChatOptions` и выбрав `strategy`.

#### Кэширование только системных сообщений

Лучше всего подходит для: Стабильных системных подсказок с <20 инструментами (инструменты кэшируются неявно через автоматическое обратное просмотр).

```java
// Кэшировать содержимое системного сообщения (инструменты кэшируются неявно)
ChatResponse response = chatModel.call(
    new Prompt(
        List.of(
            new SystemMessage("Вы полезный AI-ассистент с обширными знаниями..."),
            new UserMessage("Что такое машинное обучение?")
        ),
        AnthropicChatOptions.builder()
            .model("claude-sonnet-4")
            .cacheOptions(AnthropicCacheOptions.builder()
                .strategy(AnthropicCacheStrategy.SYSTEM_ONLY)
                .build())
            .maxTokens(500)
            .build()
    )
);
```

#### Кэширование только инструментов

Лучше всего подходит для: Больших стабильных наборов инструментов с динамическими системными подсказками (мультиарендные приложения, A/B тестирование).

```java
// Кэшировать определения инструментов, системная подсказка обрабатывается заново каждый раз
ChatResponse response = chatModel.call(
    new Prompt(
        List.of(
            new SystemMessage("Вы " + persona + " ассистент..."), // Динамично для каждого арендатора
            new UserMessage("Какова погода в Сан-Франциско?")
        ),
        AnthropicChatOptions.builder()
            .model("claude-sonnet-4")
            .cacheOptions(AnthropicCacheOptions.builder()
                .strategy(AnthropicCacheStrategy.TOOLS_ONLY)
                .build())
            .toolCallbacks(weatherToolCallback) // Большой набор инструментов кэшируется
            .maxTokens(500)
            .build()
    )
);
```

#### Кэширование системных сообщений и инструментов
```Best for: 20+ инструментов (помимо автоматического возврата) или когда оба компонента должны кэшироваться независимо.

```java
// Кэшируйте как определения инструментов, так и системное сообщение с независимыми контрольными точками
// Изменение системы не приведет к недействительности кэша инструментов (но изменение инструментов недействительно для обоих)
ChatResponse response = chatModel.call(
    new Prompt(
        List.of(
            new SystemMessage("Вы помощник по анализу погоды..."),
            new UserMessage("Какова погода в Сан-Франциско?")
        ),
        AnthropicChatOptions.builder()
            .model("claude-sonnet-4")
            .cacheOptions(AnthropicCacheOptions.builder()
                .strategy(AnthropicCacheStrategy.SYSTEM_AND_TOOLS)
                .build())
            .toolCallbacks(weatherToolCallback) // 20+ инструментов
            .maxTokens(500)
            .build()
    )
);
```

#### Кэширование истории беседы

```java
// Кэшируйте историю беседы с ChatClient и памятью (контрольная точка кэша на последнем сообщении пользователя)
ChatClient chatClient = ChatClient.builder(chatModel)
    .defaultSystem("Вы персонализированный карьерный консультант...")
    .defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory)
        .conversationId(conversationId)
        .build())
    .build();

String response = chatClient.prompt()
    .user("Какой карьерный совет вы бы мне дали?")
    .options(AnthropicChatOptions.builder()
        .model("claude-sonnet-4")
        .cacheOptions(AnthropicCacheOptions.builder()
            .strategy(AnthropicCacheStrategy.CONVERSATION_HISTORY)
            .build())
        .maxTokens(500)
        .build())
    .call()
    .content();
```

#### Использование Fluent API ChatClient

```java
String response = ChatClient.create(chatModel)
    .prompt()
    .system("Вы эксперт по анализу документов...")
    .user("Проанализируйте этот большой документ: " + document)
    .options(AnthropicChatOptions.builder()
        .model("claude-sonnet-4")
        .cacheOptions(AnthropicCacheOptions.builder()
            .strategy(AnthropicCacheStrategy.SYSTEM_ONLY)
            .build())
        .build())
    .call()
    .content();
```

### Расширенные параметры кэширования

#### TTL для каждого сообщения (5 мин или 1 ч)

По умолчанию кэшируемый контент использует TTL в 5 минут.
Вы можете установить TTL в 1 час для определенных типов сообщений.
Когда используется TTL в 1 час, Spring AI автоматически устанавливает необходимый заголовок бета-версии Anthropic.

```java
ChatResponse response = chatModel.call(
    new Prompt(
        List.of(new SystemMessage(largeSystemPrompt)),
        AnthropicChatOptions.builder()
            .model("claude-sonnet-4")
            .cacheOptions(AnthropicCacheOptions.builder()
                .strategy(AnthropicCacheStrategy.SYSTEM_ONLY)
                .messageTypeTtl(MessageType.SYSTEM, AnthropicCacheTtl.ONE_HOUR)
                .build())
            .maxTokens(500)
            .build()
    )
);
```

> **Примечание:** Расширенный TTL использует бета-функцию Anthropic `extended-cache-ttl-2025-04-11`.

#### Фильтры допустимости кэша

Контролируйте, когда используются контрольные точки кэша, устанавливая минимальные длины контента и необязательную функцию длины на основе токенов:

```java
AnthropicCacheOptions cache = AnthropicCacheOptions.builder()
    .strategy(AnthropicCacheStrategy.CONVERSATION_HISTORY)
    .messageTypeMinContentLength(MessageType.SYSTEM, 1024)
    .messageTypeMinContentLength(MessageType.USER, 1024)
    .messageTypeMinContentLength(MessageType.ASSISTANT, 1024)
    .contentLengthFunction(text -> MyTokenCounter.count(text))
    .build();

ChatResponse response = chatModel.call(
    new Prompt(
        List.of(/* сообщения */),
        AnthropicChatOptions.builder()
            .model("claude-sonnet-4")
            .cacheOptions(cache)
            .build()
    )
);
```

> **Примечание:** Определения инструментов всегда учитываются для кэширования, если используется стратегия `SYSTEM_AND_TOOLS`, независимо от длины контента.

### Пример использованияВот полный пример, демонстрирующий кэширование запросов с отслеживанием затрат:

```java
// Создайте системный контент, который будет использоваться несколько раз
String largeSystemPrompt = "Вы эксперт в области проектирования программного обеспечения, специализирующийся на распределенных системах...";

// Первый запрос - создает кэш
ChatResponse firstResponse = chatModel.call(
    new Prompt(
        List.of(
            new SystemMessage(largeSystemPrompt),
            new UserMessage("Что такое архитектура микросервисов?")
        ),
        AnthropicChatOptions.builder()
            .model("claude-sonnet-4")
            .cacheOptions(AnthropicCacheOptions.builder()
                .strategy(AnthropicCacheStrategy.SYSTEM_ONLY)
                .build())
            .maxTokens(500)
            .build()
    )
);

// Доступ к использованию токенов, связанных с кэшем
AnthropicApi.Usage firstUsage = (AnthropicApi.Usage) firstResponse.getMetadata()
    .getUsage().getNativeUsage();

System.out.println("Токены создания кэша: " + firstUsage.cacheCreationInputTokens());
System.out.println("Токены чтения кэша: " + firstUsage.cacheReadInputTokens());

// Второй запрос с тем же системным запросом - чтение из кэша  
ChatResponse secondResponse = chatModel.call(
    new Prompt(
        List.of(
            new SystemMessage(largeSystemPrompt),
            new UserMessage("Каковы преимущества событийного источника?")
        ),
        AnthropicChatOptions.builder()
            .model("claude-sonnet-4")
            .cacheOptions(AnthropicCacheOptions.builder()
                .strategy(AnthropicCacheStrategy.SYSTEM_ONLY)
                .build())
            .maxTokens(500)
            .build()
    )
);

AnthropicApi.Usage secondUsage = (AnthropicApi.Usage) secondResponse.getMetadata()
    .getUsage().getNativeUsage();

System.out.println("Токены создания кэша: " + secondUsage.cacheCreationInputTokens()); // Должно быть 0
System.out.println("Токены чтения кэша: " + secondUsage.cacheReadInputTokens()); // Должно быть > 0
```

### Отслеживание использования токенов

Запись `Usage` предоставляет подробную информацию о потреблении токенов, связанных с кэшем.
Чтобы получить метрики кэша, специфичные для Anthropic, используйте метод `getNativeUsage()`:

```java
AnthropicApi.Usage usage = (AnthropicApi.Usage) response.getMetadata()
    .getUsage().getNativeUsage();
```

Метрики, специфичные для кэша, включают:

- `cacheCreationInputTokens()`: Возвращает количество токенов, использованных при создании записи кэша
- `cacheReadInputTokens()`: Возвращает количество токенов, прочитанных из существующей записи кэша

Когда вы впервые отправляете кэшированный запрос:
- `cacheCreationInputTokens()` будет больше 0
- `cacheReadInputTokens()` будет 0

Когда вы снова отправляете тот же кэшированный запрос:
- `cacheCreationInputTokens()` будет 0
- `cacheReadInputTokens()` будет больше 0

### Реальные примеры использования

#### Анализ юридических документов#### Эффективный анализ крупных юридических контрактов или документов по соблюдению норм с кэшированием содержимого документа для нескольких вопросов:

```java
// Загрузить юридический контракт (PDF или текст)
String legalContract = loadDocument("merger-agreement.pdf"); // ~3000 токенов

// Системный запрос с юридической экспертизой
String legalSystemPrompt = "Вы эксперт по юридическому анализу, специализирующийся на корпоративном праве. " +
    "Проанализируйте следующий контракт и предоставьте точные ответы о терминах, обязательствах и рисках: " +
    legalContract;

// Первый анализ - создается кэш
ChatResponse riskAnalysis = chatModel.call(
    new Prompt(
        List.of(
            new SystemMessage(legalSystemPrompt),
            new UserMessage("Каковы ключевые условия расторжения и связанные с ними штрафы?")
        ),
        AnthropicChatOptions.builder()
            .model("claude-sonnet-4")
            .cacheOptions(AnthropicCacheOptions.builder()
                .strategy(AnthropicCacheStrategy.SYSTEM_ONLY)
                .build())
            .maxTokens(1000)
            .build()
    )
);

// Последующие вопросы используют кэшированный документ - экономия 90% затрат
ChatResponse obligationAnalysis = chatModel.call(
    new Prompt(
        List.of(
            new SystemMessage(legalSystemPrompt), // То же содержимое - попадание в кэш
            new UserMessage("Перечислите все финансовые обязательства и графики платежей.")
        ),
        AnthropicChatOptions.builder()
            .model("claude-sonnet-4")
            .cacheOptions(AnthropicCacheOptions.builder()
                .strategy(AnthropicCacheStrategy.SYSTEM_ONLY)
                .build())
            .maxTokens(1000)
            .build()
    )
);
```

#### Пакетный обзор кода

Обработка нескольких файлов кода с едиными критериями обзора при кэшировании руководящих принципов обзора:

```java
// Определить всеобъемлющие руководящие принципы обзора кода
String reviewGuidelines = """
    Вы старший программист, проводящий обзоры кода. Применяйте эти критерии:
    - Уязвимости безопасности и лучшие практики
    - Оптимизация производительности и использование памяти
    - Поддерживаемость и читаемость кода
    - Покрытие тестами и крайние случаи
    - Шаблоны проектирования и соответствие архитектуре
    """;

List<String> codeFiles = Arrays.asList(
    "UserService.java", "PaymentController.java", "SecurityConfig.java"
);

List<String> reviews = new ArrayList<>();

for (String filename : codeFiles) {
    String sourceCode = loadSourceFile(filename);
    
    ChatResponse review = chatModel.call(
        new Prompt(
            List.of(
                new SystemMessage(reviewGuidelines), // Кэшировано для всех обзоров
                new UserMessage("Просмотрите этот код " + filename + ":\n\n" + sourceCode)
            ),
            AnthropicChatOptions.builder()
                .model("claude-sonnet-4")
                .cacheOptions(AnthropicCacheOptions.builder()
                    .strategy(AnthropicCacheStrategy.SYSTEM_ONLY)
                    .build())
                .maxTokens(800)
                .build()
        )
    );
    
    reviews.add(review.getResult().getOutput().getText());
}

// Руководящие принципы кэшируются после первого запроса, последующие обзоры быстрее и дешевле
```

#### Многоарендный SaaS с общими инструментами# Создание многопользовательского приложения, где инструменты общие, но системные подсказки настраиваются для каждого арендатора

```java
// Определите большой набор общих инструментов (используется всеми арендаторами)
List<FunctionCallback> sharedTools = Arrays.asList(
    weatherToolCallback,    // ~500 токенов
    calendarToolCallback,   // ~800 токенов
    emailToolCallback,      // ~700 токенов
    analyticsToolCallback,  // ~600 токенов
    reportingToolCallback,  // ~900 токенов
    // ... еще 20+ инструментов, всего 5000+ токенов
);

@Service
public class MultiTenantAIService {

    public String handleTenantRequest(String tenantId, String userQuery) {
        // Получите конфигурацию, специфичную для арендатора
        TenantConfig config = tenantRepository.findById(tenantId);

        // Динамическая системная подсказка для каждого арендатора
        String tenantSystemPrompt = String.format("""
            Вы — AI-ассистент компании %s. Ценности компании: %s.
            Голос бренда: %s. Требования по соблюдению: %s.
            """, config.companyName(), config.values(),
                 config.brandVoice(), config.compliance());

        ChatResponse response = chatModel.call(
            new Prompt(
                List.of(
                    new SystemMessage(tenantSystemPrompt), // Различается для каждого арендатора, НЕ кэшируется
                    new UserMessage(userQuery)
                ),
                AnthropicChatOptions.builder()
                    .model("claude-sonnet-4")
                    .cacheOptions(AnthropicCacheOptions.builder()
                        .strategy(AnthropicCacheStrategy.TOOLS_ONLY) // Кэшировать только инструменты
                        .build())
                    .toolCallbacks(sharedTools) // Кэшируется один раз, общий для всех арендаторов
                    .maxTokens(800)
                    .build()
            )
        );

        return response.getResult().getOutput().getText();
    }
}

// Инструменты кэшируются один раз (5000 токенов @ 10% = 500 токенов за попадания в кэш)
// Уникальная системная подсказка для каждого арендатора обрабатывается заново (200-500 токенов @ 100%)
// Всего за запрос: ~700-1000 токенов против 5500+ без TOOLS_ONLY
```

#### Служба поддержки клиентов с базой знаний

Создайте систему поддержки клиентов, которая кэширует вашу базу знаний о продукте для последовательных и точных ответов:

```java
// Загрузите обширную базу знаний о продукте
String knowledgeBase = """
    ДОКУМЕНТАЦИЯ ПРОДУКТА:
    - API-эндпоинты и методы аутентификации
    - Общие процедуры устранения неполадок
    - Подробности о выставлении счетов и подписках
    - Руководства по интеграции и примеры
    - Известные проблемы и обходные пути
    """ + loadProductDocs(); // ~2500 токенов

@Service
public class CustomerSupportService {

    public String handleCustomerQuery(String customerQuery, String customerId) {
        ChatResponse response = chatModel.call(
            new Prompt(
                List.of(
                    new SystemMessage("Вы — полезный агент службы поддержки клиентов. " +
                        "Используйте эту базу знаний для предоставления точных решений: " + knowledgeBase),
                    new UserMessage("Клиент " + customerId + " спрашивает: " + customerQuery)
                ),
                AnthropicChatOptions.builder()
                    .model("claude-sonnet-4")
                    .cacheOptions(AnthropicCacheOptions.builder()
                        .strategy(AnthropicCacheStrategy.SYSTEM_ONLY)
                        .build())
                    .maxTokens(600)
                    .build()
            )
        );

        return response.getResult().getOutput().getText();
    }
}

// База знаний кэшируется для всех запросов клиентов
// Несколько агентов поддержки могут воспользоваться одним и тем же кэшированным контентом
```

### Лучшие практики1. **Выбор правильной стратегии**:
   - Используйте `SYSTEM_ONLY` для стабильных системных подсказок с <20 инструментами (инструменты кэшируются неявно через автоматический возврат)
   - Используйте `TOOLS_ONLY` для больших стабильных наборов инструментов (5000+ токенов) с динамическими системными подсказками (мультиарендные, A/B тестирование)
   - Используйте `SYSTEM_AND_TOOLS`, когда у вас 20+ инструментов (за пределами автоматического возврата) или когда вы хотите, чтобы оба кэшировались независимо
   - Используйте `CONVERSATION_HISTORY` с памятью ChatClient для многосерийных разговоров
   - Используйте `NONE`, чтобы явно отключить кэширование

2. **Понимание каскадной недействительности**: Иерархия кэша Anthropic (`tools → system → messages`) означает, что изменения распространяются вниз:
   - Изменение **инструментов** делает недействительными: инструменты + система + сообщения (все кэши) ❌❌❌
   - Изменение **системы** делает недействительными: система + сообщения (кэш инструментов остается действительным) ✅❌❌
   - Изменение **сообщений** делает недействительными: только сообщения (кэши инструментов и системы остаются действительными) ✅✅❌

   **Стабильность инструментов критична** при использовании стратегий `SYSTEM_AND_TOOLS` или `CONVERSATION_HISTORY`.

3. **Независимость SYSTEM_AND_TOOLS**: При использовании `SYSTEM_AND_TOOLS` изменение системного сообщения НЕ делает недействительным кэш инструментов, что позволяет эффективно повторно использовать кэшированные инструменты, даже когда системные подсказки варьируются.

4. **Соответствие требованиям к токенам**: Сосредоточьтесь на кэшировании контента, который соответствует минимальным требованиям к токенам (1024+ токена для моделей Sonnet 4, 2048+ для моделей Haiku).

5. **Повторное использование идентичного контента**: Кэширование работает лучше всего с точными совпадениями контента подсказки. Даже небольшие изменения потребуют новой записи в кэше.

6. **Мониторинг использования токенов**: Используйте статистику использования кэша для отслеживания его эффективности:
   ```java
   AnthropicApi.Usage usage = (AnthropicApi.Usage) response.getMetadata().getUsage().getNativeUsage();
   if (usage != null) {
       System.out.println("Создание кэша: " + usage.cacheCreationInputTokens());
       System.out.println("Чтение кэша: " + usage.cacheReadInputTokens());
   }
   ```

7. **Стратегическое размещение кэша**: Реализация автоматически размещает точки разрыва кэша в оптимальных местах в зависимости от выбранной стратегии, обеспечивая соблюдение лимита в 4 точки разрыва от Anthropic.

8. **Срок службы кэша**: Значение по умолчанию для TTL составляет 5 минут; установите TTL на 1 час для каждого типа сообщения через `messageTypeTtl(...)`. Каждый доступ к кэшу сбрасывает таймер.

9. **Ограничения кэширования инструментов**: Имейте в виду, что взаимодействия на основе инструментов могут не предоставлять метаданные использования кэша в ответе.

### Подробности реализации

Реализация кэширования подсказок в Spring AI основывается на следующих ключевых принципах проектирования:

1. **Стратегическое размещение кэша**: Точки разрыва кэша автоматически размещаются в оптимальных местах в зависимости от выбранной стратегии, обеспечивая соблюдение лимита в 4 точки разрыва от Anthropic.
   - `CONVERSATION_HISTORY` размещает точки разрыва кэша на: инструментах (если они присутствуют), системном сообщении и последнем сообщении пользователя
   - Это позволяет Anthropic использовать префиксное соответствие для поэтапного кэширования растущей истории разговора
   - Каждый ход строится на предыдущем кэшированном префиксе, максимизируя повторное использование кэша

2. **Портативность провайдера**: Конфигурация кэша осуществляется через `AnthropicChatOptions`, а не отдельные сообщения, что сохраняет совместимость при переходе между различными провайдерами ИИ.

3. **Потокобезопасность**: Отслеживание точек разрыва кэша реализовано с использованием потокобезопасных механизмов для корректной обработки параллельных запросов.

4. **Автоматическая сортировка контента**: Реализация обеспечивает правильный порядок передачи блоков JSON и управления кэшем в соответствии с требованиями API Anthropic.

5. **Проверка права на агрегирование**: Для `CONVERSATION_HISTORY` реализация учитывает все типы сообщений (пользователь, ассистент, инструмент) в последних ~20 блоках контента при определении, соответствует ли комбинированный контент минимальному порогу токенов для кэширования.

### Будущие улучшенияТекущие стратегии кэширования разработаны для эффективной обработки **90% распространенных случаев использования**. Для приложений, требующих более детального контроля, будущие улучшения могут включать:

- **Управление кэшированием на уровне сообщений** для точного размещения контрольных точек
- **Кэширование многоблочного контента** внутри отдельных сообщений  
- **Расширенный выбор границ кэша** для сложных сценариев инструментов
- **Смешанные стратегии TTL** для оптимизированных иерархий кэша

Эти улучшения сохранят полную обратную совместимость, открывая при этом полные возможности кэширования подсказок Anthropic для специализированных случаев использования.

## Мыслительный процесс

Модели Anthropic Claude поддерживают функцию "мыслительного процесса", которая позволяет модели демонстрировать свой процесс рассуждения перед предоставлением окончательного ответа. Эта функция обеспечивает более прозрачное и детализированное решение проблем, особенно для сложных вопросов, требующих пошагового рассуждения.

[ПРИМЕЧАНИЕ]
====
**Поддерживаемые модели**

Функция мышления поддерживается следующими моделями Claude:

- Модели Claude 4 (`claude-opus-4-20250514`, `claude-sonnet-4-20250514`)
- Claude 3.7 Sonnet (`claude-3-7-sonnet-20250219`)

**Возможности моделей:**

- **Claude 3.7 Sonnet**: Возвращает полный вывод мышления. Поведение последовательное, но не поддерживает обобщенное или чередующееся мышление.
- **Модели Claude 4**: Поддерживают обобщенное мышление, чередующееся мышление и улучшенную интеграцию инструментов.

Структура API-запроса одинакова для всех поддерживаемых моделей, но поведение вывода различается.
====

### Конфигурация мышления

Чтобы включить мышление на любой поддерживаемой модели Claude, добавьте следующую конфигурацию в ваш запрос:

#### Обязательная конфигурация

1. **Добавьте объект `thinking`**:
- `"type": "enabled"`
- `budget_tokens`: Лимит токенов для рассуждения (рекомендуется начинать с 1024)

2. **Правила бюджета токенов**:
- `budget_tokens` обычно должен быть меньше `max_tokens`
- Claude может использовать меньше токенов, чем выделено
- Большие бюджеты увеличивают глубину рассуждения, но могут повлиять на задержку
- При использовании инструментов с чередующимся мышлением (только Claude 4) это ограничение ослабляется, но пока не поддерживается в Spring AI.

#### Ключевые соображения

- **Claude 3.7** возвращает полный контент мышления в ответе
- **Claude 4** возвращает **обобщенную** версию внутреннего рассуждения модели для уменьшения задержки и защиты конфиденциального контента
- **Токены мышления подлежат оплате** как часть выходных токенов (даже если не все они видны в ответе)
- **Чередующееся мышление** доступно только на моделях Claude 4 и требует бета-заголовка `interleaved-thinking-2025-05-14`

#### Интеграция инструментов и чередующееся мышление

Модели Claude 4 поддерживают чередующееся мышление с использованием инструментов, позволяя модели рассуждать между вызовами инструментов.

[ПРИМЕЧАНИЕ]
====
Текущая реализация Spring AI поддерживает базовое мышление и использование инструментов отдельно, но пока не поддерживает чередующееся мышление с использованием инструментов (где мышление продолжается через несколько вызовов инструментов).
====

Для получения подробной информации о чередующемся мышлении с использованием инструментов смотрите https://docs.anthropic.com/en/docs/build-with-claude/extended-thinking#extended-thinking-with-tool-use[документацию Anthropic].

### Пример без потоковой передачиВот как включить мышление в запросе без потоковой передачи, используя API ChatClient:

```java
ChatClient chatClient = ChatClient.create(chatModel);

// Для Claude 3.7 Sonnet - требуется явная конфигурация мышления
ChatResponse response = chatClient.prompt()
    .options(AnthropicChatOptions.builder()
        .model("claude-3-7-sonnet-latest")
        .temperature(1.0)  // Температура должна быть установлена на 1, когда мышление включено
        .maxTokens(8192)
        .thinking(AnthropicApi.ThinkingType.ENABLED, 2048)  // Должно быть ≥1024 && < max_tokens
        .build())
    .user("Существует ли бесконечное количество простых чисел, таких что n mod 4 == 3?")
    .call()
    .chatResponse();

// Для моделей Claude 4 - мышление включено по умолчанию
ChatResponse response4 = chatClient.prompt()
    .options(AnthropicChatOptions.builder()
        .model("claude-opus-4-0")
        .maxTokens(8192)
        // Явная конфигурация мышления не требуется
        .build())
    .user("Существует ли бесконечное количество простых чисел, таких что n mod 4 == 3?")
    .call()
    .chatResponse();

// Обработайте ответ, который может содержать контент мышления
for (Generation generation : response.getResults()) {
    AssistantMessage message = generation.getOutput();
    if (message.getText() != null) {
        // Обычный текстовый ответ
        System.out.println("Текстовый ответ: " + message.getText());
    }
    else if (message.getMetadata().containsKey("signature")) {
        // Контент мышления
        System.out.println("Мышление: " + message.getMetadata().get("thinking"));
        System.out.println("Подпись: " + message.getMetadata().get("signature"));
    }
}
```

### Пример потоковой передачи

Вы также можете использовать мышление с потоковыми ответами:

```java
ChatClient chatClient = ChatClient.create(chatModel);

// Для Claude 3.7 Sonnet - явная конфигурация мышления
Flux<ChatResponse> responseFlux = chatClient.prompt()
    .options(AnthropicChatOptions.builder()
        .model("claude-3-7-sonnet-latest")
        .temperature(1.0)
        .maxTokens(8192)
        .thinking(AnthropicApi.ThinkingType.ENABLED, 2048)
        .build())
    .user("Существует ли бесконечное количество простых чисел, таких что n mod 4 == 3?")
    .stream();

// Для моделей Claude 4 - мышление включено по умолчанию
Flux<ChatResponse> responseFlux4 = chatClient.prompt()
    .options(AnthropicChatOptions.builder()
        .model("claude-opus-4-0")
        .maxTokens(8192)
        // Явная конфигурация мышления не требуется
        .build())
    .user("Существует ли бесконечное количество простых чисел, таких что n mod 4 == 3?")
    .stream();

// Для потоковой передачи вы можете собрать только текстовые ответы
String textContent = responseFlux.collectList()
    .block()
    .stream()
    .map(ChatResponse::getResults)
    .flatMap(List::stream)
    .map(Generation::getOutput)
    .map(AssistantMessage::getText)
    .filter(text -> text != null && !text.isBlank())
    .collect(Collectors.joining());
```

### Интеграция использования инструментов

Модели Claude 4 интегрируют возможности мышления и использования инструментов:

- **Claude 3.7 Sonnet**: Поддерживает как мышление, так и использование инструментов, но они работают отдельно и требуют более явной конфигурации
- **Модели Claude 4**: Естественно чередуют мышление и использование инструментов, обеспечивая более глубокое рассуждение во время взаимодействия с инструментами

### Преимущества использования мышления

Функция мышления предоставляет несколько преимуществ:

1. **Прозрачность**: Видеть процесс рассуждения модели и как она пришла к своему выводу
2. **Отладка**: Определить, где модель может допускать логические ошибки
3. **Образование**: Использовать пошаговое рассуждение в качестве учебного инструмента
4. **Решение сложных задач**: Лучшие результаты в задачах по математике, логике и рассуждению

Обратите внимание, что включение мышления требует большего бюджета токенов, так как сам процесс мышления потребляет токены из вашего выделения.

## Вызов инструментов/функцийВы можете зарегистрировать пользовательские Java-инструменты с помощью `AnthropicChatModel` и позволить модели Anthropic Claude интеллектуально выбирать вывод JSON-объекта, содержащего аргументы для вызова одной или нескольких зарегистрированных функций. Это мощная техника для соединения возможностей LLM с внешними инструментами и API. Узнайте больше о xref:api/tools.adoc[Вызов инструментов].

### Выбор инструмента

Параметр `tool_choice` позволяет вам контролировать, как модель использует предоставленные инструменты. Эта функция дает вам детальный контроль над поведением выполнения инструментов.

Для получения полной информации об API смотрите https://docs.anthropic.com/en/api/messages#body-tool-choice[документацию по выбору инструмента Anthropic].

#### Опции выбора инструмента

Spring AI предоставляет четыре стратегии выбора инструмента через интерфейс `AnthropicApi.ToolChoice`:

- **`ToolChoiceAuto`** (по умолчанию): Модель автоматически решает, использовать ли инструменты или ответить текстом
- **`ToolChoiceAny`**: Модель должна использовать хотя бы один из доступных инструментов
- **`ToolChoiceTool`**: Модель должна использовать конкретный инструмент по имени
- **`ToolChoiceNone`**: Модель не может использовать никакие инструменты

#### Отключение параллельного использования инструментов

Все опции выбора инструмента (кроме `ToolChoiceNone`) поддерживают параметр `disableParallelToolUse`. Когда он установлен в `true`, модель будет выводить не более одного использования инструмента.

#### Примеры использования

##### Автоматический режим (поведение по умолчанию)

Позвольте модели решить, использовать ли инструменты:

```java
ChatResponse response = chatModel.call(
    new Prompt(
        "Какова погода в Сан-Франциско?",
        AnthropicChatOptions.builder()
            .toolChoice(new AnthropicApi.ToolChoiceAuto())
            .toolCallbacks(weatherToolCallback)
            .build()
    )
);
```

##### Принудительное использование инструмента (любой)

Требуйте от модели использовать хотя бы один инструмент:

```java
ChatResponse response = chatModel.call(
    new Prompt(
        "Какова погода?",
        AnthropicChatOptions.builder()
            .toolChoice(new AnthropicApi.ToolChoiceAny())
            .toolCallbacks(weatherToolCallback, calculatorToolCallback)
            .build()
    )
);
```

##### Принудительное использование конкретного инструмента

Требуйте от модели использовать конкретный инструмент по имени:

```java
ChatResponse response = chatModel.call(
    new Prompt(
        "Какова погода в Сан-Франциско?",
        AnthropicChatOptions.builder()
            .toolChoice(new AnthropicApi.ToolChoiceTool("get_weather"))
            .toolCallbacks(weatherToolCallback, calculatorToolCallback)
            .build()
    )
);
```

##### Отключение использования инструментов

Запретите модели использовать какие-либо инструменты:

```java
ChatResponse response = chatModel.call(
    new Prompt(
        "Какова погода в Сан-Франциско?",
        AnthropicChatOptions.builder()
            .toolChoice(new AnthropicApi.ToolChoiceNone())
            .toolCallbacks(weatherToolCallback)
            .build()
    )
);
```

##### Отключение параллельного использования инструментов

Принудите модель использовать только один инструмент за раз:

```java
ChatResponse response = chatModel.call(
    new Prompt(
        "Какова погода в Сан-Франциско и сколько будет 2+2?",
        AnthropicChatOptions.builder()
            .toolChoice(new AnthropicApi.ToolChoiceAuto(true)) // disableParallelToolUse = true
            .toolCallbacks(weatherToolCallback, calculatorToolCallback)
            .build()
    )
);
```

#### Использование API ChatClient

Вы также можете использовать выбор инструмента с помощью удобного API ChatClient:

```java
String response = ChatClient.create(chatModel)
    .prompt()
    .user("Какова погода в Сан-Франциско?")
    .options(AnthropicChatOptions.builder()
        .toolChoice(new AnthropicApi.ToolChoiceTool("get_weather"))
        .build())
    .call()
    .content();
```

#### Сценарии использования- **Валидация**: Используйте `ToolChoiceTool`, чтобы гарантировать вызов конкретного инструмента для критически важных операций
- **Эффективность**: Используйте `ToolChoiceAny`, когда вы знаете, что инструмент должен быть использован, чтобы избежать ненужной генерации текста
- **Контроль**: Используйте `ToolChoiceNone`, чтобы временно отключить доступ к инструментам, сохраняя при этом зарегистрированные определения инструментов
- **Последовательная обработка**: Используйте `disableParallelToolUse`, чтобы принудительно выполнить инструменты последовательно для зависимых операций

## Мультимодальность

Мультимодальность относится к способности модели одновременно понимать и обрабатывать информацию из различных источников, включая текст, pdf, изображения, форматы данных.

### Изображения
В настоящее время Anthropic Claude 3 поддерживает тип источника `base64` для `images`, а также медиа-типы `image/jpeg`, `image/png`, `image/gif` и `image/webp`.
Дополнительную информацию можно найти в [руководстве по Vision](https://docs.anthropic.com/claude/docs/vision).
Anthropic Claude 3.5 Sonnet также поддерживает тип источника `pdf` для файлов `application/pdf`.

Интерфейс `Message` от Spring AI поддерживает мультимодальные AI модели, вводя тип Media.
Этот тип содержит данные и информацию о медиа-вложениях в сообщениях, используя `org.springframework.util.MimeType` от Spring и `java.lang.Object` для необработанных медиа-данных.

Ниже приведен простой пример кода, извлеченный из https://github.com/spring-projects/spring-ai/blob/main/models/spring-ai-anthropic/src/test/java/org/springframework/ai/anthropic/AnthropicChatModelIT.java[AnthropicChatModelIT.java], демонстрирующий сочетание пользовательского текста с изображением.

```java
var imageData = new ClassPathResource("/multimodal.test.png");

var userMessage = new UserMessage("Объясните, что вы видите на этом изображении?",
        List.of(new Media(MimeTypeUtils.IMAGE_PNG, this.imageData)));

ChatResponse response = chatModel.call(new Prompt(List.of(this.userMessage)));

logger.info(response.getResult().getOutput().getText());
```

Входными данными является изображение `multimodal.test.png`:

![Мультимодальное тестовое изображение, 200, 200, align="left"](multimodal.test.png)

вместе с текстовым сообщением "Объясните, что вы видите на этом изображении?", и генерируется ответ, похожий на:

```
На изображении показан крупный план проволочной фруктовой корзины, содержащей несколько фруктов.
...
```

### PDF

Начиная с Sonnet 3.5 https://docs.anthropic.com/en/docs/build-with-claude/pdf-support[поддержка PDF (бета)] предоставляется.
Используйте медиа-тип `application/pdf`, чтобы прикрепить PDF-файл к сообщению:

```java
var pdfData = new ClassPathResource("/spring-ai-reference-overview.pdf");

var userMessage = new UserMessage(
        "Вы очень профессиональный специалист по резюмированию документов. Пожалуйста, резюмируйте данный документ.",
        List.of(new Media(new MimeType("application", "pdf"), pdfData)));

var response = this.chatModel.call(new Prompt(List.of(userMessage)));
```

## Цитаты

API https://docs.anthropic.com/en/docs/build-with-claude/citations[Цитаты] от Anthropic позволяет Claude ссылаться на конкретные части предоставленных документов при генерации ответов.
Когда документы для цитирования включены в подсказку, Claude может ссылаться на исходный материал, а метаданные цитирования (диапазоны символов, номера страниц или блоки контента) возвращаются в метаданных ответа.

Цитаты помогают улучшить:

- **Проверку точности**: Пользователи могут проверять ответы Claude на основе исходного материала
- **Прозрачность**: Видеть, какие части документов повлияли на ответ
- **Соответствие**: Соответствовать требованиям по атрибуции источников в регулируемых отраслях
- **Доверие**: Укреплять уверенность, показывая, откуда пришла информация

[ПРИМЕЧАНИЕ]
====
**Поддерживаемые модели**

Цитаты поддерживаются в моделях Claude 3.7 Sonnet и Claude 4 (Opus и Sonnet).

**Типы документов**

Поддерживаются три типа документов для цитирования:

- **Обычный текст**: Текстовый контент с цитатами на уровне символов
- **PDF**: PDF-документы с цитатами на уровне страниц
- **Пользовательский контент**: Определенные пользователем блоки контента с цитатами на уровне блоков
====

### Создание документов для цитированияИспользуйте сборщик `CitationDocument` для создания документов, которые можно цитировать:

#### Обычные текстовые документы

```java
CitationDocument document = CitationDocument.builder()
    .plainText("Эйфелева башня была завершена в 1889 году в Париже, Франция. " +
               "Она высотой 330 метров и была спроектирована Густавом Эйфелем.")
    .title("Факты об Эйфелевой башне")
    .citationsEnabled(true)
    .build();
```

#### PDF-документы

```java
// Из пути к файлу
CitationDocument document = CitationDocument.builder()
    .pdfFile("path/to/document.pdf")
    .title("Техническая спецификация")
    .citationsEnabled(true)
    .build();

// Из массива байтов
byte[] pdfBytes = loadPdfBytes();
CitationDocument document = CitationDocument.builder()
    .pdf(pdfBytes)
    .title("Руководство по продукту")
    .citationsEnabled(true)
    .build();
```

#### Пользовательские блоки контента

Для более точного контроля цитирования используйте пользовательские блоки контента:

```java
CitationDocument document = CitationDocument.builder()
    .customContent(
        "Великая китайская стена имеет длину примерно 21,196 километра.",
        "Она строилась на протяжении многих веков, начиная с 7 века до нашей эры.",
        "Стена была построена для защиты китайских государств от вторжений."
    )
    .title("Факты о Великой китайской стене")
    .citationsEnabled(true)
    .build();
```

### Использование цитат в запросах

Включите документы с цитатами в ваши параметры чата:

```java
ChatResponse response = chatModel.call(
    new Prompt(
        "Когда была построена Эйфелева башня и какова ее высота?",
        AnthropicChatOptions.builder()
            .model("claude-3-7-sonnet-latest")
            .maxTokens(1024)
            .citationDocuments(document)
            .build()
    )
);
```

#### Несколько документов

Вы можете предоставить несколько документов, на которые Claude может ссылаться:

```java
CitationDocument parisDoc = CitationDocument.builder()
    .plainText("Париж — столица Франции с населением 2,1 миллиона.")
    .title("Информация о Париже")
    .citationsEnabled(true)
    .build();

CitationDocument eiffelDoc = CitationDocument.builder()
    .plainText("Эйфелева башня была спроектирована Густавом Эйфелем для Всемирной выставки 1889 года.")
    .title("История Эйфелевой башни")
    .citationsEnabled(true)
    .build();

ChatResponse response = chatModel.call(
    new Prompt(
        "Какова столица Франции и кто спроектировал Эйфелеву башню?",
        AnthropicChatOptions.builder()
            .model("claude-3-7-sonnet-latest")
            .citationDocuments(parisDoc, eiffelDoc)
            .build()
    )
);
```

### Доступ к цитатам

Цитаты возвращаются в метаданных ответа:

```java
ChatResponse response = chatModel.call(prompt);

// Получить цитаты из метаданных
@SuppressWarnings("unchecked")
List<Citation> citations = (List<Citation>) response.getMetadata().get("citations");

// Необязательно: получить количество цитат напрямую из метаданных
Integer citationCount = (Integer) response.getMetadata().get("citationCount");
System.out.println("Всего цитат: " + citationCount);

// Обработать каждую цитату
for (Citation citation : citations) {
    System.out.println("Документ: " + citation.getDocumentTitle());
    System.out.println("Местоположение: " + citation.getLocationDescription());
    System.out.println("Цитируемый текст: " + citation.getCitedText());
    System.out.println("Индекс документа: " + citation.getDocumentIndex());
    System.out.println();
}
```

### Типы цитат

Цитаты содержат различную информацию о местоположении в зависимости от типа документа:

#### Символьное местоположение (Обычный текст)

Для обычных текстовых документов цитаты включают символьные индексы:

```java
Citation citation = citations.get(0);
if (citation.getType() == Citation.LocationType.CHAR_LOCATION) {
    int start = citation.getStartCharIndex();
    int end = citation.getEndCharIndex();
    String text = citation.getCitedText();
    System.out.println("Символы " + start + "-" + end + ": " + text);
}
```

#### Страничное местоположение (PDF)Для PDF-документов ссылки включают номера страниц:

```java
Citation citation = citations.get(0);
if (citation.getType() == Citation.LocationType.PAGE_LOCATION) {
    int startPage = citation.getStartPageNumber();
    int endPage = citation.getEndPageNumber();
    System.out.println("Страницы " + startPage + "-" + endPage);
}
```

#### Местоположение блока контента (Пользовательский контент)

Для пользовательского контента ссылки ссылаются на конкретные блоки контента:

```java
Citation citation = citations.get(0);
if (citation.getType() == Citation.LocationType.CONTENT_BLOCK_LOCATION) {
    int startBlock = citation.getStartBlockIndex();
    int endBlock = citation.getEndBlockIndex();
    System.out.println("Блоки контента " + startBlock + "-" + endBlock);
}
```

### Полный пример

Вот полный пример, демонстрирующий использование ссылок:

```java
// Создание документа ссылки
CitationDocument document = CitationDocument.builder()
    .plainText("Spring AI — это фреймворк приложений для инженерии ИИ. " +
               "Он предоставляет API, совместимый с Spring, для разработки приложений ИИ. " +
               "Фреймворк включает абстракции для моделей чата, моделей встраивания и " +
               "векторных баз данных.")
    .title("Обзор Spring AI")
    .citationsEnabled(true)
    .build();

// Вызов модели с документом
ChatResponse response = chatModel.call(
    new Prompt(
        "Что такое Spring AI?",
        AnthropicChatOptions.builder()
            .model("claude-3-7-sonnet-latest")
            .maxTokens(1024)
            .citationDocuments(document)
            .build()
    )
);

// Отображение ответа
System.out.println("Ответ: " + response.getResult().getOutput().getText());
System.out.println("\nСсылки:");

// Обработка ссылок
List<Citation> citations = (List<Citation>) response.getMetadata().get("citations");

if (citations != null && !citations.isEmpty()) {
    for (int i = 0; i < citations.size(); i++) {
        Citation citation = citations.get(i);
        System.out.println("\n[" + (i + 1) + "] " + citation.getDocumentTitle());
        System.out.println("    Местоположение: " + citation.getLocationDescription());
        System.out.println("    Текст: " + citation.getCitedText());
    }
} else {
    System.out.println("В ответе не было предоставлено ссылок.");
}
```

### Лучшие практики

1. **Используйте описательные заголовки**: Предоставляйте значимые заголовки для документов ссылок, чтобы помочь пользователям идентифицировать источники в ссылках.
2. **Проверяйте наличие ссылок**: Не все ответы будут включать ссылки, поэтому всегда проверяйте, существует ли метаданные ссылок, прежде чем к ним обращаться.
3. **Учитывайте размер документа**: Более крупные документы предоставляют больше контекста, но потребляют больше токенов ввода и могут повлиять на время ответа.
4. **Используйте несколько документов**: При ответах на вопросы, охватывающие несколько источников, предоставляйте все соответствующие документы в одном запросе, а не делая несколько вызовов.
5. **Используйте подходящие типы документов**: Выбирайте простой текст для простого контента, PDF для существующих документов и пользовательские блоки контента, когда вам нужен детальный контроль над гранулярностью ссылок.

### Примеры использования в реальном мире

#### Анализ юридических документов

Анализируйте контракты и юридические документы, сохраняя атрибуцию источников:

```java
CitationDocument contract = CitationDocument.builder()
    .pdfFile("merger-agreement.pdf")
    .title("Соглашение о слиянии 2024")
    .citationsEnabled(true)
    .build();

ChatResponse response = chatModel.call(
    new Prompt(
        "Каковы ключевые условия расторжения в этом контракте?",
        AnthropicChatOptions.builder()
            .model("claude-sonnet-4")
            .maxTokens(2000)
            .citationDocuments(contract)
            .build()
    )
);

// Ссылки будут указывать на конкретные страницы в PDF
```

#### База знаний службы поддержки клиентов```markdown
Предоставьте точные ответы службы поддержки клиентов с проверяемыми источниками:

```java
CitationDocument kbArticle1 = CitationDocument.builder()
    .plainText(loadKnowledgeBaseArticle("authentication"))
    .title("Руководство по аутентификации")
    .citationsEnabled(true)
    .build();

CitationDocument kbArticle2 = CitationDocument.builder()
    .plainText(loadKnowledgeBaseArticle("billing"))
    .title("Часто задаваемые вопросы по выставлению счетов")
    .citationsEnabled(true)
    .build();

ChatResponse response = chatModel.call(
    new Prompt(
        "Как мне сбросить пароль и обновить информацию о выставлении счетов?",
        AnthropicChatOptions.builder()
            .model("claude-3-7-sonnet-latest")
            .citationDocuments(kbArticle1, kbArticle2)
            .build()
    )
);

// Цитаты показывают, какие статьи базы знаний были использованы
```

#### Исследования и соблюдение норм

Генерируйте отчеты, которые требуют источников для соблюдения норм:

```java
CitationDocument clinicalStudy = CitationDocument.builder()
    .pdfFile("clinical-trial-results.pdf")
    .title("Результаты клинического испытания фазы III")
    .citationsEnabled(true)
    .build();

CitationDocument regulatoryGuidance = CitationDocument.builder()
    .plainText(loadRegulatoryDocument())
    .title("Руководство FDA")
    .citationsEnabled(true)
    .build();

ChatResponse response = chatModel.call(
    new Prompt(
        "Суммируйте результаты эффективности и регуляторные последствия.",
        AnthropicChatOptions.builder()
            .model("claude-sonnet-4")
            .maxTokens(3000)
            .citationDocuments(clinicalStudy, regulatoryGuidance)
            .build()
    )
);

// Цитаты предоставляют след для аудита соблюдения норм
```

### Опции документа цитирования

#### Поле контекста

Опционально предоставьте контекст о документе, который не будет цитироваться, но может помочь в понимании Claude:

```java
CitationDocument document = CitationDocument.builder()
    .plainText("...")
    .title("Юридический контракт")
    .context("Это соглашение о слиянии, датированное январем 2024 года, между Компанией A и Компанией B")
    .build();
```

#### Управление цитатами

По умолчанию цитаты отключены для всех документов (поведение по умолчанию).
Чтобы включить цитаты, явно установите `citationsEnabled(true)`:

```java
CitationDocument document = CitationDocument.builder()
    .plainText("Эйфелева башня была завершена в 1889 году...")
    .title("Исторические факты")
    .citationsEnabled(true)  // Явно включите цитаты для этого документа
    .build();
```

Вы также можете предоставить документы без цитат для фонового контекста:

```java
CitationDocument backgroundDoc = CitationDocument.builder()
    .plainText("Фоновая информация об отрасли...")
    .title("Контекстный документ")
    // citationsEnabled по умолчанию равно false - Claude будет использовать это, но не цитировать
    .build();
```

[ПРИМЕЧАНИЕ]
====
Anthropic требует согласованных настроек цитирования для всех документов в запросе.
Вы не можете смешивать документы с включенными и отключенными цитатами в одном запросе.
====

## Навыки

API https://platform.claude.com/docs/en/agents-and-tools/agent-skills/overview[Навыков] Anthropic расширяет возможности Claude с помощью специализированных, заранее упакованных возможностей для генерации документов.
Навыки позволяют Claude создавать фактические загружаемые файлы - электронные таблицы Excel, презентации PowerPoint, документы Word и PDF - а не просто описывать, что эти документы могут содержать.

Навыки решают основное ограничение традиционных LLM:

- **Традиционный Claude**: "Вот как будет выглядеть ваш отчет о продажах..." (только текстовое описание)
- **С навыками**: Создает фактический файл `sales_report.xlsx`, который вы можете скачать и открыть в Excel

[ПРИМЕЧАНИЕ]
====
**Поддерживаемые модели**

Навыки поддерживаются в Claude Sonnet 4, Claude Sonnet 4.5, Claude Opus 4 и более поздних моделях.

**Требования**

- Навыки требуют возможности выполнения кода (автоматически включается Spring AI)
- Максимум 8 навыков на запрос
- Сгенерированные файлы доступны для загрузки через API файлов в течение 24 часов
====
```### Предустановленные навыки Anthropic

Spring AI предоставляет типобезопасный доступ к предустановленным навыкам Anthropic через перечисление `AnthropicSkill`:

[cols="2,3,4", stripes=even]
|====
| Навык | Описание | Сгенерированный тип файла

| `XLSX`
| Генерация и манипуляция Excel-таблицами
| `.xlsx` (Microsoft Excel)

| `PPTX`
| Создание презентаций PowerPoint
| `.pptx` (Microsoft PowerPoint)

| `DOCX`
| Генерация документов Word
| `.docx` (Microsoft Word)

| `PDF`
| Создание PDF-документов
| `.pdf` (Portable Document Format)
|====

### Основное использование

Включите навыки, добавив их в ваши `AnthropicChatOptions`:

```java
ChatResponse response = chatModel.call(
    new Prompt(
        "Создайте Excel-таблицу с данными о продажах за 1 квартал 2025 года. " +
        "Включите столбцы для месяца, дохода и расходов с 3 строками примерных данных.",
        AnthropicChatOptions.builder()
            .model("claude-sonnet-4-5")
            .maxTokens(4096)
            .skill(AnthropicApi.AnthropicSkill.XLSX)
            .build()
    )
);

// Claude сгенерирует фактический Excel-файл
String responseText = response.getResult().getOutput().getText();
System.out.println(responseText);
// Вывод: "Я создал Excel-таблицу с вашими данными о продажах за 1 квартал 2025 года..."
```

### Несколько навыков

Вы можете включить несколько навыков в одном запросе (до 8):

```java
ChatResponse response = chatModel.call(
    new Prompt(
        "Создайте отчет о продажах с Excel-файлом, содержащим исходные данные, " +
        "и презентацией PowerPoint, подводящей итоги ключевых выводов.",
        AnthropicChatOptions.builder()
            .model("claude-sonnet-4-5")
            .maxTokens(8192)
            .skill(AnthropicApi.AnthropicSkill.XLSX)
            .skill(AnthropicApi.AnthropicSkill.PPTX)
            .build()
    )
);
```

### Использование SkillContainer для расширенной конфигурации

Для большего контроля используйте `SkillContainer` напрямую:

```java
AnthropicApi.SkillContainer container = AnthropicApi.SkillContainer.builder()
    .skill(AnthropicApi.AnthropicSkill.XLSX)
    .skill(AnthropicApi.AnthropicSkill.PPTX, "20251013") // Конкретная версия
    .build();

ChatResponse response = chatModel.call(
    new Prompt(
        "Сгенерируйте квартальный отчет",
        AnthropicChatOptions.builder()
            .model("claude-sonnet-4-5")
            .maxTokens(4096)
            .skillContainer(container)
            .build()
    )
);
```

### Использование Fluent API ChatClient

Навыки работают без проблем с Fluent API ChatClient:

```java
String response = ChatClient.create(chatModel)
    .prompt()
    .user("Создайте презентацию PowerPoint о Spring AI с 3 слайдами: " +
          "Название, Ключевые особенности и Начало работы")
    .options(AnthropicChatOptions.builder()
        .model("claude-sonnet-4-5")
        .maxTokens(4096)
        .skill(AnthropicApi.AnthropicSkill.PPTX)
        .build())
    .call()
    .content();
```

### Потоковая передача с навыками

Навыки работают с потоковыми ответами:

```java
Flux<ChatResponse> responseFlux = chatModel.stream(
    new Prompt(
        "Создайте документ Word, объясняющий концепции машинного обучения",
        AnthropicChatOptions.builder()
            .model("claude-sonnet-4-5")
            .maxTokens(4096)
            .skill(AnthropicApi.AnthropicSkill.DOCX)
            .build()
    )
);

responseFlux.subscribe(response -> {
    String content = response.getResult().getOutput().getText();
    System.out.print(content);
});
```

### Скачивание сгенерированных файлов

Когда Claude генерирует файлы с использованием навыков, ответ содержит идентификаторы файлов, которые можно использовать для скачивания фактических файлов через API файлов. Spring AI предоставляет утилитный класс `AnthropicSkillsResponseHelper` для извлечения идентификаторов файлов и скачивания файлов.

#### Извлечение идентификаторов файлов```java
import org.springframework.ai.anthropic.AnthropicSkillsResponseHelper;

ChatResponse response = chatModel.call(prompt);

// Извлечение всех идентификаторов файлов из ответа
List<String> fileIds = AnthropicSkillsResponseHelper.extractFileIds(response);

for (String fileId : fileIds) {
    System.out.println("Сгенерированный идентификатор файла: " + fileId);
}
```

#### Получение метаданных файла

Перед загрузкой вы можете получить метаданные файла:

```java
@Autowired
private AnthropicApi anthropicApi;

// Получить метаданные для конкретного файла
String fileId = fileIds.get(0);
AnthropicApi.FileMetadata metadata = anthropicApi.getFileMetadata(fileId);

System.out.println("Имя файла: " + metadata.filename());     // например, "sales_report.xlsx"
System.out.println("Размер: " + metadata.size() + " байт");  // например, 5082
System.out.println("MIME-тип: " + metadata.mimeType());    // например, "application/vnd..."
```

#### Загрузка содержимого файла

```java
// Загрузка содержимого файла в виде байтов
byte[] fileContent = anthropicApi.downloadFile(fileId);

// Сохранение в локальную файловую систему
Path outputPath = Path.of("downloads", metadata.filename());
Files.write(outputPath, fileContent);

System.out.println("Файл сохранен по адресу: " + outputPath);
```

#### Удобный метод: загрузка всех файлов

`AnthropicSkillsResponseHelper` предоставляет удобный метод для загрузки всех сгенерированных файлов сразу:

```java
// Загрузка всех файлов в целевую директорию
Path targetDir = Path.of("generated-files");
Files.createDirectories(targetDir);

List<Path> savedFiles = AnthropicSkillsResponseHelper.downloadAllFiles(response, anthropicApi, targetDir);

for (Path file : savedFiles) {
    System.out.println("Загружено: " + file.getFileName() +
                       " (" + Files.size(file) + " байт)");
}
```

#### Полный пример загрузки файла

Вот полный пример, показывающий использование Skills с загрузкой файла:

```java
@Service
public class DocumentGenerationService {

    private final AnthropicChatModel chatModel;
    private final AnthropicApi anthropicApi;

    public DocumentGenerationService(AnthropicChatModel chatModel, AnthropicApi anthropicApi) {
        this.chatModel = chatModel;
        this.anthropicApi = anthropicApi;
    }

    public Path generateSalesReport(String quarter, Path outputDir) throws IOException {
        // Генерация Excel отчета с использованием Skills
        ChatResponse response = chatModel.call(
            new Prompt(
                "Создайте Excel-таблицу с данными о продажах за " + quarter + ". " +
                "Включите столбцы Месяц, Доход, Расходы и Прибыль.",
                AnthropicChatOptions.builder()
                    .model("claude-sonnet-4-5")
                    .maxTokens(4096)
                    .skill(AnthropicApi.AnthropicSkill.XLSX)
                    .build()
            )
        );

        // Извлечение идентификаторов файлов из ответа
        List<String> fileIds = AnthropicSkillsResponseHelper.extractFileIds(response);

        if (fileIds.isEmpty()) {
            throw new RuntimeException("Файл не был сгенерирован");
        }

        // Загрузка сгенерированного файла
        String fileId = fileIds.get(0);
        AnthropicApi.FileMetadata metadata = anthropicApi.getFileMetadata(fileId);
        byte[] content = anthropicApi.downloadFile(fileId);

        // Сохранение в выходной директории
        Path outputPath = outputDir.resolve(metadata.filename());
        Files.write(outputPath, content);

        return outputPath;
    }
}
```

### Операции API файлов

`AnthropicApi` предоставляет прямой доступ к API файлов:

[cols="2,4", stripes=even]
|====
| Метод | Описание

| `getFileMetadata(fileId)`
| Получить метаданные, включая имя файла, размер, MIME-тип и время истечения

| `downloadFile(fileId)`
| Загрузить содержимое файла в виде массива байтов

| `listFiles(limit, page)`
| Список файлов с поддержкой постраничного отображения

| `deleteFile(fileId)`
| Удалить файл немедленно (файлы автоматически истекают через 24 часа)
|====

#### Список файлов
``````java
// Список файлов с пагинацией
AnthropicApi.FilesListResponse files = anthropicApi.listFiles(20, null);

for (AnthropicApi.FileMetadata file : files.data()) {
    System.out.println(file.id() + ": " + file.filename());
}

// Проверка наличия дополнительных страниц
if (files.hasMore()) {
    AnthropicApi.FilesListResponse nextPage = anthropicApi.listFiles(20, files.nextPage());
    // Обработка следующей страницы...
}
```

#### Извлечение ID контейнера

Для многоповоротных разговоров со Скиллами вам может понадобиться извлечь ID контейнера:

```java
String containerId = AnthropicSkillsResponseHelper.extractContainerId(response);

if (containerId != null) {
    System.out.println("ID контейнера для повторного использования: " + containerId);
}
```

### Лучшие практики

1. **Используйте подходящие модели**: Скиллы лучше всего работают с моделями Claude Sonnet 4 и более поздними. Убедитесь, что вы используете поддерживаемую модель.

2. **Установите достаточное количество максимальных токенов**: Генерация документов может требовать значительного количества токенов. Используйте `maxTokens(4096)` или больше для сложных документов.

3. **Будьте конкретными в подсказках**: Предоставьте четкие, детализированные инструкции о структуре документа, содержании и форматировании.

4. **Своевременно обрабатывайте загрузку файлов**: Сгенерированные файлы истекают через 24 часа. Загружайте файлы сразу после генерации.

5. **Проверяйте наличие ID файлов**: Всегда проверяйте, что ID файлов были возвращены перед попыткой загрузки. Некоторые подсказки могут привести к текстовым ответам без генерации файла.

6. **Используйте защитное обработку ошибок**: Оборачивайте операции с файлами в блоки try-catch, чтобы корректно обрабатывать сетевые проблемы или истекшие файлы.

```java
List<String> fileIds = AnthropicSkillsResponseHelper.extractFileIds(response);

if (fileIds.isEmpty()) {
    // Claude мог ответить текстом вместо генерации файла
    String text = response.getResult().getOutput().getText();
    log.warn("Файлы не сгенерированы. Ответ: {}", text);
    return;
}

try {
    byte[] content = anthropicApi.downloadFile(fileIds.get(0));
    // Обработка файла...
} catch (Exception e) {
    log.error("Не удалось загрузить файл: {}", e.getMessage());
}
```

### Примеры использования в реальном мире

#### Автоматизированная генерация отчетов

Генерация форматированных бизнес-отчетов на основе данных:

```java
@Service
public class ReportService {

    private final AnthropicChatModel chatModel;
    private final AnthropicApi anthropicApi;

    public byte[] generateMonthlyReport(SalesData data) throws IOException {
        String prompt = String.format(
            "Создайте презентацию PowerPoint, подводящую итоги месячной производительности продаж. " +
            "Общий доход: $%,.2f, Общие расходы: $%,.2f, Чистая прибыль: $%,.2f. " +
            "Включите графики и ключевые выводы. Создайте 5 слайдов: " +
            "1) Заголовок, 2) Обзор доходов, 3) Разбивка расходов, " +
            "4) Анализ прибыли, 5) Рекомендации.",
            data.revenue(), data.expenses(), data.profit()
        );

        ChatResponse response = chatModel.call(
            new Prompt(prompt,
                AnthropicChatOptions.builder()
                    .model("claude-sonnet-4-5")
                    .maxTokens(8192)
                    .skill(AnthropicApi.AnthropicSkill.PPTX)
                    .build()
            )
        );

        List<String> fileIds = AnthropicSkillsResponseHelper.extractFileIds(response);
        return anthropicApi.downloadFile(fileIds.get(0));
    }
}
```

#### Сервис экспорта данных
``````markdown
Экспорт структурированных данных в формат Excel:

```java
@RestController
public class ExportController {

    private final AnthropicChatModel chatModel;
    private final AnthropicApi anthropicApi;
    private final CustomerRepository customerRepository;

    @GetMapping("/export/customers")
    public ResponseEntity<byte[]> exportCustomers() throws IOException {
        List<Customer> customers = customerRepository.findAll();

        String dataDescription = customers.stream()
            .map(c -> String.format("%s, %s, %s", c.name(), c.email(), c.tier()))
            .collect(Collectors.joining("\n"));

        ChatResponse response = chatModel.call(
            new Prompt(
                "Создайте таблицу Excel с данными о клиентах. " +
                "Столбцы: Имя, Электронная почта, Уровень. Отформатируйте строку заголовка жирным текстом. " +
                "Данные:\n" + dataDescription,
                AnthropicChatOptions.builder()
                    .model("claude-sonnet-4-5")
                    .maxTokens(4096)
                    .skill(AnthropicApi.AnthropicSkill.XLSX)
                    .build()
            )
        );

        List<String> fileIds = AnthropicSkillsResponseHelper.extractFileIds(response);
        byte[] content = anthropicApi.downloadFile(fileIds.get(0));
        AnthropicApi.FileMetadata metadata = anthropicApi.getFileMetadata(fileIds.get(0));

        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION,
                    "attachment; filename=\"" + metadata.filename() + "\"")
            .contentType(MediaType.parseMediaType(metadata.mimeType()))
            .body(content);
    }
}
```

#### Генерация документов в нескольких форматах

Генерация нескольких форматов документов из одного запроса:

```java
public Map<String, byte[]> generateProjectDocumentation(ProjectInfo project) throws IOException {
    ChatResponse response = chatModel.call(
        new Prompt(
            "Создайте документацию по проекту для: " + project.name() + "\n" +
            "Описание: " + project.description() + "\n\n" +
            "Сгенерируйте:\n" +
            "1. Файл Excel с графиком проекта и ключевыми этапами\n" +
            "2. Презентацию PowerPoint (3-5 слайдов)\n" +
            "3. Документ Word с подробными спецификациями",
            AnthropicChatOptions.builder()
                .model("claude-sonnet-4-5")
                .maxTokens(16384)
                .skill(AnthropicApi.AnthropicSkill.XLSX)
                .skill(AnthropicApi.AnthropicSkill.PPTX)
                .skill(AnthropicApi.AnthropicSkill.DOCX)
                .build()
        )
    );

    Map<String, byte[]> documents = new HashMap<>();
    List<String> fileIds = AnthropicSkillsResponseHelper.extractFileIds(response);

    for (String fileId : fileIds) {
        AnthropicApi.FileMetadata metadata = anthropicApi.getFileMetadata(fileId);
        byte[] content = anthropicApi.downloadFile(fileId);
        documents.put(metadata.filename(), content);
    }

    return documents;
}
```

### Комбинирование навыков с другими функциями

Навыки могут быть объединены с другими функциями Anthropic, такими как кэширование запросов:

```java
ChatResponse response = chatModel.call(
    new Prompt(
        List.of(
            new SystemMessage("Вы эксперт по анализу данных и созданию документов..."),
            new UserMessage("Создайте таблицу финансового отчета")
        ),
        AnthropicChatOptions.builder()
            .model("claude-sonnet-4-5")
            .maxTokens(4096)
            .skill(AnthropicApi.AnthropicSkill.XLSX)
            .cacheOptions(AnthropicCacheOptions.builder()
                .strategy(AnthropicCacheStrategy.SYSTEM_ONLY)
                .build())
            .build()
    )
);
```

### Пользовательские навыки
```В дополнение к предустановленным навыкам, Anthropic поддерживает пользовательские навыки, которые вы можете создать для специализированных шаблонов документов, правил форматирования или специфического поведения в определенной области.
Пользовательские навыки — это файлы `SKILL.md` с инструкциями, которые вы загружаете в ваше рабочее пространство Anthropic.
После загрузки вы можете использовать их в Spring AI наряду с предустановленными навыками.

Пользовательские навыки идеально подходят для:

- **Корпоративного брендинга**: Применение единых заголовков, подвалов, логотипов и цветовых схем
- **Требований к соблюдению норм**: Добавление необходимых отказов от ответственности, уведомлений о конфиденциальности или следов аудита
- **Шаблонов документов**: Принуждение к определенным структурам для отчетов, предложений или спецификаций
- **Экспертизы в области**: Включение терминологии, расчетов или правил форматирования, специфичных для отрасли

Для получения подробной информации о создании пользовательских навыков обратитесь к https://platform.claude.com/docs/en/api/skills-guide[документации API навыков Anthropic].

#### Загрузка пользовательского навыка

Загрузите ваш навык с помощью API Anthropic.
Обратите внимание на конкретные требования к формату для параметра `files[]`:

```bash
curl -X POST "https://api.anthropic.com/v1/skills" \
  -H "x-api-key: $ANTHROPIC_API_KEY" \
  -H "anthropic-version: 2023-06-01" \
  -H "anthropic-beta: skills-2025-10-02" \
  -F "display_title=My Custom Skill" \
  -F "files[]=@SKILL.md;filename=my-skill-name/SKILL.md"
```

[ВАЖНО]
====
- Используйте `files[]=` (с квадратными скобками), а не `files=`
- Параметр `filename` должен включать директорию, соответствующую полю `name` в вашем YAML frontmatter SKILL.md
- После загрузки убедитесь, что ваш навык отображается в консоли Anthropic в разделе **Настройки > Возможности**
====

Ответ содержит ваш идентификатор навыка:

```json
{
  "id": "skill_01AbCdEfGhIjKlMnOpQrStUv",
  "display_title": "My Custom Skill",
  "source": "custom",
  "latest_version": "1765845644409101"
}
```

#### Использование пользовательских навыков в Spring AI

Ссылайтесь на ваш пользовательский навык по его идентификатору, используя метод `.skill()`:

```java
ChatResponse response = chatModel.call(
    new Prompt(
        "Создать квартальный отчет по продажам",
        AnthropicChatOptions.builder()
            .model("claude-sonnet-4-5")
            .maxTokens(4096)
            .skill("skill_01AbCdEfGhIjKlMnOpQrStUv")
            .build()
    )
);
```

#### Сочетание предустановленных и пользовательских навыков

Вы можете использовать как предустановленные, так и пользовательские навыки в одном запросе.
Это позволяет вам использовать возможности генерации документов Anthropic, применяя специфические требования вашей организации:

```java
ChatResponse response = chatModel.call(
    new Prompt(
        "Создать таблицу отчетов по продажам",
        AnthropicChatOptions.builder()
            .model("claude-sonnet-4-5")
            .maxTokens(4096)
            .skill(AnthropicApi.AnthropicSkill.XLSX)       // Предустановленный
            .skill("skill_01AbCdEfGhIjKlMnOpQrStUv")       // Ваш пользовательский навык
            .build()
    )
);
```

#### Использование SkillContainer с пользовательскими навыками

Для большего контроля над версиями навыков используйте `SkillContainer` напрямую:

```java
AnthropicApi.SkillContainer container = AnthropicApi.SkillContainer.builder()
    .skill(AnthropicApi.AnthropicSkill.XLSX)
    .skill("skill_01AbCdEfGhIjKlMnOpQrStUv")                    // Использует последнюю версию
    .skill("skill_02XyZaBcDeFgHiJkLmNoPq", "1765845644409101")  // Конкретная версия
    .build();

ChatResponse response = chatModel.call(
    new Prompt(
        "Сгенерировать отчет",
        AnthropicChatOptions.builder()
            .model("claude-sonnet-4-5")
            .maxTokens(8192)
            .skillContainer(container)
            .build()
    )
);
```

#### Обновление пользовательского навыкаTo update an existing skill, загрузите новую версию на конечную точку `/versions`:

```bash
curl -X POST "https://api.anthropic.com/v1/skills/YOUR_SKILL_ID/versions" \
  -H "x-api-key: $ANTHROPIC_API_KEY" \
  -H "anthropic-version: 2023-06-01" \
  -H "anthropic-beta: skills-2025-10-02" \
  -F "files[]=@SKILL.md;filename=my-skill-name/SKILL.md"
```

При использовании `latest` в качестве версии (по умолчанию) новая версия автоматически подбирается.

#### Полный пример пользовательских навыков

Вот полный пример, показывающий сервис, который опционально применяет навык с пользовательским брендингом:

```java
@Service
public class BrandedDocumentService {

    private static final String BRANDING_SKILL_ID = "skill_01AbCdEfGhIjKlMnOpQrStUv";

    private final AnthropicChatModel chatModel;
    private final AnthropicApi anthropicApi;

    public BrandedDocumentService(AnthropicChatModel chatModel, AnthropicApi anthropicApi) {
        this.chatModel = chatModel;
        this.anthropicApi = anthropicApi;
    }

    public byte[] generateReport(String prompt, boolean includeBranding) throws IOException {
        // Создание опций с документом навыка
        AnthropicChatOptions.Builder optionsBuilder = AnthropicChatOptions.builder()
                .model("claude-sonnet-4-5")
                .maxTokens(8192)
                .skill(AnthropicApi.AnthropicSkill.XLSX);

        // Добавление пользовательского навыка брендинга, если это запрашивается
        if (includeBranding) {
            optionsBuilder.skill(BRANDING_SKILL_ID);
        }

        ChatResponse response = chatModel.call(
            new Prompt(prompt, optionsBuilder.build())
        );

        // Извлечение и загрузка сгенерированного файла
        List<String> fileIds = AnthropicSkillsResponseHelper.extractFileIds(response);

        if (fileIds.isEmpty()) {
            throw new RuntimeException("Файл не был сгенерирован");
        }

        return anthropicApi.downloadFile(fileIds.get(0));
    }
}
```

## Пример контроллера

https://start.spring.io/[Создайте] новый проект Spring Boot и добавьте `spring-ai-starter-model-anthropic` в зависимости вашего pom (или gradle).

Добавьте файл `application.properties` в директорию `src/main/resources`, чтобы включить и настроить модель чата Anthropic:

```application.properties
spring.ai.anthropic.api-key=YOUR_API_KEY
spring.ai.anthropic.chat.options.model=claude-3-5-sonnet-latest
spring.ai.anthropic.chat.options.temperature=0.7
spring.ai.anthropic.chat.options.max-tokens=450
```

> **Совет:** Замените `api-key` на ваши учетные данные Anthropic.

Это создаст реализацию `AnthropicChatModel`, которую вы можете внедрить в ваш класс. Вот пример простого класса `@Controller`, который использует модель чата для генерации текста.

```java
@RestController
public class ChatController {

    private final AnthropicChatModel chatModel;

    @Autowired
    public ChatController(AnthropicChatModel chatModel) {
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

## Ручная конфигурацияThe https://github.com/spring-projects/spring-ai/blob/main/models/spring-ai-anthropic/src/main/java/org/springframework/ai/anthropic/AnthropicChatModel.java[AnthropicChatModel] реализует интерфейсы `ChatModel` и `StreamingChatModel` и использует <<low-level-api>> для подключения к сервису Anthropic.

Добавьте зависимость `spring-ai-anthropic` в файл `pom.xml` вашего проекта Maven:

```xml
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-anthropic</artifactId>
</dependency>
```

или в файл сборки Gradle `build.gradle`.

```groovy
dependencies {
    implementation 'org.springframework.ai:spring-ai-anthropic'
}
```

> **Совет:** Обратитесь к разделу xref:getting-started.adoc#dependency-management[Управление зависимостями], чтобы добавить Spring AI BOM в ваш файл сборки.

Далее создайте `AnthropicChatModel` и используйте его для генерации текста:

```java
var anthropicApi = new AnthropicApi(System.getenv("ANTHROPIC_API_KEY"));
var anthropicChatOptions = AnthropicChatOptions.builder()
            .model("claude-3-7-sonnet-20250219")
            .temperature(0.4)
            .maxTokens(200)
        .build()
var chatModel = AnthropicChatModel.builder().anthropicApi(anthropicApi)
                .defaultOptions(anthropicChatOptions).build();

ChatResponse response = this.chatModel.call(
    new Prompt("Сгенерируйте имена 5 известных пиратов."));

// Или с потоковыми ответами
Flux<ChatResponse> response = this.chatModel.stream(
    new Prompt("Сгенерируйте имена 5 известных пиратов."));
```

`AnthropicChatOptions` предоставляет информацию о конфигурации для запросов чата. `AnthropicChatOptions.Builder` является удобным строителем опций.

## Клиент низкого уровня AnthropicApi [[low-level-api]]

The https://github.com/spring-projects/spring-ai/blob/main/models/spring-ai-anthropic/src/main/java/org/springframework/ai/anthropic/api/AnthropicApi.java[AnthropicApi] предоставляет легковесный Java-клиент для [Anthropic Message API](https://docs.anthropic.com/claude/reference/messages_post).

Следующая диаграмма классов иллюстрирует интерфейсы чата и строительные блоки `AnthropicApi`:

![Диаграмма API чата AnthropicApi, width=1000, align="center"](anthropic-claude3-class-diagram.jpg)

![Модель событий AnthropicApi, width=1000, align="center"](anthropic-claude3-events-model.jpg)

Вот простой фрагмент кода, как использовать API программно:

```java
AnthropicApi anthropicApi =
    new AnthropicApi(System.getenv("ANTHROPIC_API_KEY"));

AnthropicMessage chatCompletionMessage = new AnthropicMessage(
        List.of(new ContentBlock("Расскажи мне шутку?")), Role.USER);

// Синхронный запрос
ResponseEntity<ChatCompletionResponse> response = this.anthropicApi
    .chatCompletionEntity(new ChatCompletionRequest(AnthropicApi.ChatModel.CLAUDE_3_OPUS.getValue(),
            List.of(this.chatCompletionMessage), null, 100, 0.8, false));

// Потоковый запрос
Flux<StreamResponse> response = this.anthropicApi
    .chatCompletionStream(new ChatCompletionRequest(AnthropicApi.ChatModel.CLAUDE_3_OPUS.getValue(),
            List.of(this.chatCompletionMessage), null, 100, 0.8, true));
```

Следуйте JavaDoc https://github.com/spring-projects/spring-ai/blob/main/models/spring-ai-anthropic/src/main/java/org/springframework/ai/anthropic/api/AnthropicApi.java[AnthropicApi.java] для получения дополнительной информации.

### Примеры низкоуровневого API
- Тест [AnthropicApiIT.java](https://github.com/spring-projects/spring-ai/blob/main/models/spring-ai-anthropic/src/test/java/org/springframework/ai/anthropic/chat/api/AnthropicApiIT.java) предоставляет общие примеры использования легковесной библиотеки.
