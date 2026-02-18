```markdown
[[upgrade-notes]]
# Заметки об обновлении

[[upgrading-to-2-0-0-M3]]
## Обновление до 2.0.0-M3

#### История разговоров удалена из ToolContext

История разговоров больше не добавляется автоматически в `ToolContext`. Константа `TOOL_CALL_HISTORY` и метод `getToolCallHistory()` были удалены из класса `ToolContext`.

##### Влияние

- Константа `ToolContext.TOOL_CALL_HISTORY` больше не существует
- Метод `ToolContext.getToolCallHistory()` больше не существует
- История разговоров больше не заполняется автоматически в `ToolContext`

##### Почему это изменение?

1. **Эффективность использования памяти**: Предотвращает неограниченный рост памяти в длинных разговорах
2. **Разделение обязанностей**: Инструменты должны работать с их параметрами, а не управлять состоянием разговора
3. **Соответствие архитектуре**: Контекст разговора должен находиться на уровне советника, а не в выполнении инструмента

##### Миграция

Если вашему приложению необходимо управление историей разговоров, используйте `ToolCallAdvisor`:

.Управление историей разговоров с помощью ToolCallAdvisor
```java
ChatClient chatClient = ChatClient.builder()
    .defaultAdvisors(
        new ToolCallAdvisor()
            .conversationHistoryEnabled(true)  // Полная история (по умолчанию)
    )
    .build();
```

**Как работает ToolCallAdvisor:**

`ToolCallAdvisor` управляет историей разговоров на уровне советника:

- **conversationHistoryEnabled=true** (по умолчанию): Полная история разговоров сохраняется и отправляется в LLM между итерациями вызовов инструмента, позволяя LLM синтезировать результаты с полным контекстом
- **conversationHistoryEnabled=false**: Только самый последний ответ инструмента отправляется в LLM (полезно, когда советник ChatMemory управляет историей отдельно)

**Ключевой момент:** История разговоров используется **LLM** для понимания контекста и формирования ответов, а не самими **инструментами**. Инструменты получают только свои входные параметры и любой пользовательский контекст, который вы явно предоставляете.

**Пользовательский контекст в инструментах:**

`ToolContext` по-прежнему доступен для передачи пользовательских, специфичных для приложения данных в инструменты:

.Передача пользовательского контекста в инструменты
```java
ChatResponse response = chatClient.prompt()
    .user("Какова погода в SF?")
    .options(ChatOptionsBuilder.builder()
        .toolContext("userId", "user123")
        .toolContext("apiKey", "secret")
        .build())
    .call()
    .chatResponse();
```

**Пример потока:**

1. Пользователь спрашивает: "Какова погода в SF и LA?"
2. LLM запрашивает вызовы инструментов: `getWeather(SF)` и `getWeather(LA)`
3. Инструменты выполняются только с их параметрами (без истории разговоров)
4. ToolCallAdvisor собирает результаты инструментов и историю разговоров
5. LLM получает контекст разговора от советника и синтезирует: "Погода в SF 72°F, а в LA 85°F"

LLM видит весь разговор через цепочку советников, а не через ToolContext.

[[upgrading-to-2-0-0-M2]]
## Обновление до 2.0.0-M2

### Ломающее изменения

#### Исправлен порядок сообщений в MongoDB Chat Memory

`MongoChatMemoryRepository` был исправлен, чтобы возвращать сообщения в порядке их отправки (от старых к новым), что соответствует всем другим реализациям репозитория памяти чата. Ранее он неправильно возвращал сообщения в обратном порядке (от новых к старым), что нарушало поток разговора для LLM.

##### Влияние

Если ваше приложение использовало `MongoChatMemoryRepository` и обрабатывало неправильный порядок (например, переворачивая сообщения после получения), вам нужно будет удалить эту доработку.

##### Миграция
``````markdown
Удалите любой код, который меняет порядок сообщений после получения из памяти чата MongoDB:

```java
// ДО (с обходным решением для ошибки):
List<Message> messages = chatMemoryRepository.findByConversationId(conversationId);
Collections.reverse(messages); // Удалите это обходное решение

// ПОСЛЕ (правильный порядок):
List<Message> messages = chatMemoryRepository.findByConversationId(conversationId);
// Сообщения теперь правильно упорядочены хронологически
```

Все репозитории памяти чата теперь последовательно возвращают сообщения в порядке их отправки (от старых к новым), что является ожидаемым форматом для истории разговоров LLM.

### Сервисы на этапе разработки

- Поддержка Docker Compose и Testcontainers для MongoDB Atlas теперь предоставляется нативно модулем Spring Boot MongoDB. Миграция должна быть прозрачной и не требовать изменений в коде. Что касается зависимостей, вам больше не нужно импортировать `org.springframework.ai:spring-ai-spring-boot-testcontainers`. Зависимость на `org.springframework.boot:spring-boot-testcontainers` будет достаточной.

[[upgrading-to-2-0-0-M1]]
## Обновление до 2.0.0-M1

### Ломающие изменения

#### Удалена конфигурация температуры по умолчанию

Spring AI больше не предоставляет значения температуры по умолчанию для свойств автоконфигурации модели чата. Ранее Spring AI устанавливал температуру по умолчанию равной `0.7` для большинства моделей чата. Это значение по умолчанию было удалено, чтобы можно было использовать родное значение температуры каждого поставщика ИИ.

##### Влияние

Если ваше приложение не настраивало значение температуры явно и полагалось на значение по умолчанию Spring AI `0.7`, вы можете заметить другое поведение после обновления. Фактическое значение по умолчанию теперь будет определяться API каждого поставщика ИИ, что может варьироваться:

- Некоторые поставщики по умолчанию устанавливают `1.0`
- Некоторые поставщики по умолчанию устанавливают `0.7`
- У некоторых поставщиков есть специфические значения по умолчанию для моделей

##### Миграция

Если вы хотите сохранить предыдущее поведение, явно установите температуру в вашей конфигурации:

```properties
# Пример для OpenAI
spring.ai.openai.chat.options.temperature=0.7

# Пример для Anthropic
spring.ai.anthropic.chat.options.temperature=0.7

# Пример для Azure OpenAI
spring.ai.azure.openai.chat.options.temperature=0.7
```

Или программно при формировании запросов:

```java
ChatResponse response = chatModel.call(
    new Prompt("Ваш запрос здесь",
        OpenAiChatOptions.builder()
            .temperature(0.7)
            .build()));
```

[[upgrading-to-1-1-0-RC1]]
## Обновление до 1.1.0-RC1

### Ломающие изменения

#### Миграция API Text-to-Speech (TTS)

Реализация Text-to-Speech OpenAI была мигрирована от классов, специфичных для поставщика, к общим интерфейсам. Это позволяет писать переносимый код, который работает с несколькими поставщиками TTS (OpenAI, ElevenLabs и будущими поставщиками).

##### Удаленные классы

Следующие устаревшие классы были удалены из пакета `org.springframework.ai.openai.audio.speech`:

- `SpeechModel` → Используйте `TextToSpeechModel` (из `org.springframework.ai.audio.tts`)
- `StreamingSpeechModel` → Используйте `StreamingTextToSpeechModel` (из `org.springframework.ai.audio.tts`)
- `SpeechPrompt` → Используйте `TextToSpeechPrompt` (из `org.springframework.ai.audio.tts`)
- `SpeechResponse` → Используйте `TextToSpeechResponse` (из `org.springframework.ai.audio.tts`)
- `SpeechMessage` → Используйте `TextToSpeechMessage` (из `org.springframework.ai.audio.tts`)
- `Speech` (в `org.springframework.ai.openai.audio.speech`) → Используйте `Speech` (из `org.springframework.ai.audio.tts`)

Кроме того, тип параметра `speed` изменился с `Float` на `Double` во всех компонентах OpenAI TTS для согласованности с другими поставщиками TTS.

##### Шаги миграции
```1. **Обновите импорты**: Замените все импорты из `org.springframework.ai.openai.audio.speech.**` на `org.springframework.ai.audio.tts.**`

2. **Обновите ссылки на типы**: Замените все вхождения старых имен классов на новые:
+
```text
Найти:    SpeechModel
Заменить: TextToSpeechModel

Найти:    StreamingSpeechModel
Заменить: StreamingTextToSpeechModel

Найти:    SpeechPrompt
Заменить: TextToSpeechPrompt

Найти:    SpeechResponse
Заменить: TextToSpeechResponse

Найти:    SpeechMessage
Заменить: TextToSpeechMessage
```

3. **Обновите параметр скорости**: Измените с `Float` на `Double`:
+
```text
Найти:    .speed(1.0f)
Заменить: .speed(1.0)

Найти:    Float speed
Заменить: Double speed
```

4. **Обновите внедрение зависимостей**: Если вы внедряете `SpeechModel`, обновите на `TextToSpeechModel`:
+
```java
// До
public MyService(SpeechModel speechModel) { ... }

// После
public MyService(TextToSpeechModel textToSpeechModel) { ... }
```

##### Преимущества

- **Портативность**: Пишите код один раз, легко переключайтесь между OpenAI, ElevenLabs или другими поставщиками TTS
- **Согласованность**: Те же шаблоны, что и у ChatModel и других абстракций Spring AI
- **Безопасность типов**: Улучшенная иерархия типов с правильными реализациями интерфейсов
- **Будущее**: Новые поставщики TTS будут автоматически работать с вашим существующим кодом

##### Дополнительные ресурсы

Для получения подробного руководства по миграции с примерами кода смотрите:

- xref:api/audio/speech/openai-speech.adoc#_migration_guide[Руководство по миграции OpenAI TTS]
- xref:api/audio/speech.adoc#_writing_provider_agnostic_code[Написание кода TTS, независимого от поставщика]


[[upgrading-to-1-0-0-snapshot]]
## Обновление до 1.0.0-SNAPSHOT

### Обзор
Версия 1.0.0-SNAPSHOT включает значительные изменения в идентификаторах артефактов, именах пакетов и структуре модулей. Этот раздел предоставляет рекомендации, специфичные для использования версии SNAPSHOT.

### Добавьте репозитории снимков

Чтобы использовать версию 1.0.0-SNAPSHOT, вам необходимо добавить репозитории снимков в ваш файл сборки.
Для получения подробных инструкций смотрите раздел xref:getting-started.adoc#snapshots-add-snapshot-repositories[Снимки - Добавить репозитории снимков] в руководстве по началу работы.

### Обновите управление зависимостями

Обновите вашу версию Spring AI BOM до `1.0.0-SNAPSHOT` в вашей конфигурации сборки.
Для получения подробных инструкций по настройке управления зависимостями смотрите раздел xref:getting-started.adoc#dependency-management[Управление зависимостями] в руководстве по началу работы.

### Изменения идентификаторов артефактов, пакетов и модулей
Версия 1.0.0-SNAPSHOT включает изменения в идентификаторах артефактов, именах пакетов и структуре модулей.

Для получения подробной информации смотрите:
- xref:upgrade-notes.adoc#common-artifact-id-changes[Общие изменения идентификаторов артефактов]
- xref:upgrade-notes.adoc#common-package-changes[Общие изменения пакетов]
- xref:upgrade-notes.adoc#common-module-structure[Общая структура модулей]


[[upgrading-to-1-0-0-RC1]]
## Обновление до 1.0.0-RC1

Вы можете автоматизировать процесс обновления до 1.0.0-RC1, используя рецепт OpenRewrite.
Этот рецепт помогает применить многие необходимые изменения кода для этой версии.
Найдите рецепт и инструкции по его использованию на https://github.com/arconia-io/arconia-migrations/blob/main/docs/spring-ai.md[Arconia Spring AI Migrations].

### Ломающее изменения


#### Чат-клиент и советники

Основные изменения, которые влияют на код конечного пользователя:

- В `VectorStoreChatMemoryAdvisor`:
** Константа `CHAT_MEMORY_RETRIEVE_SIZE_KEY` была переименована в `TOP_K`.
** Константа `DEFAULT_CHAT_MEMORY_RESPONSE_SIZE` (значение: 100) была переименована в `DEFAULT_TOP_K` с новым значением по умолчанию 20.

- Константа `CHAT_MEMORY_CONVERSATION_ID_KEY` была переименована в `CONVERSATION_ID` и перемещена из `AbstractChatMemoryAdvisor` в интерфейс `ChatMemory`. Обновите ваши импорты, чтобы использовать `org.springframework.ai.chat.memory.ChatMemory.CONVERSATION_ID`.

##### Самодостаточные шаблоны в советникахВстроенные советники, которые выполняют дополнение запросов, были обновлены для использования самодостаточных шаблонов. Цель состоит в том, чтобы каждый советник мог выполнять операции с шаблонами, не влияя и не подвергаясь влиянию решений по шаблонам и запросам в других советниках.

**Если вы предоставляли пользовательские шаблоны для следующих советников, вам необходимо обновить их, чтобы убедиться, что все ожидаемые заполнители включены.**

- `QuestionAnswerAdvisor` ожидает шаблон со следующими заполнителями (см. xref:api/retrieval-augmented-generation.adoc#_questionansweradvisor[подробности]):
** заполнителя `query` для получения вопроса пользователя.
** заполнителя `question_answer_context` для получения извлеченного контекста.
- `PromptChatMemoryAdvisor` ожидает шаблон со следующими заполнителями (см. xref:api/chat-memory.adoc#_promptchatmemoryadvisor[подробности]):
** заполнителя `instructions` для получения оригинального системного сообщения.
** заполнителя `memory` для получения извлеченной памяти разговора.
- `VectorStoreChatMemoryAdvisor` ожидает шаблон со следующими заполнителями (см. xref:api/chat-memory.adoc#_vectorstorechatmemoryadvisor[подробности]):
** заполнителя `instructions` для получения оригинального системного сообщения.
** заполнителя `long_term_memory` для получения извлеченной памяти разговора.

#### Наблюдаемость
- Переработано наблюдение за содержимым с использованием логирования вместо трассировки (https://github.com/spring-projects/spring-ai/commit/ca843e85887aa1da6300c77550c379c103500897[ca843e8])
  ** Заменены фильтры наблюдения за содержимым на обработчики логирования
  ** Переименованы свойства конфигурации для лучшего отражения их назначения:
    *** `include-prompt` → `log-prompt`
    *** `include-completion` → `log-completion`
    *** `include-query-response` → `log-query-response`
  ** Добавлен `TracingAwareLoggingObservationHandler` для логирования с учетом трассировки
  ** Заменен `micrometer-tracing-bridge-otel` на `micrometer-tracing`
  ** Удалено трассирование на основе событий в пользу прямого логирования
  ** Удалена прямая зависимость от OTel SDK
  ** Переименован `includePrompt` в `logPrompt` в свойствах наблюдения (в `ChatClientBuilderProperties`, `ChatObservationProperties` и `ImageObservationProperties`)

#### Переименование модуля репозитория памяти чата и автоконфигурации

Мы стандартизировали шаблон именования для компонентов памяти чата, добавив суффикс репозитория по всему коду. Это изменение затрагивает реализации Cassandra, JDBC и Neo4j, влияя на идентификаторы артефактов, имена пакетов Java и имена классов для ясности.

#### Идентификаторы артефактов
Все артефакты, связанные с памятью, теперь следуют последовательному шаблону:

- `spring-ai-model-chat-memory-**` → `spring-ai-model-chat-memory-repository-**`
- `spring-ai-autoconfigure-model-chat-memory-**` → `spring-ai-autoconfigure-model-chat-memory-repository-**`
- `spring-ai-starter-model-chat-memory-**` → `spring-ai-starter-model-chat-memory-repository-**`

#### Пакеты Java

- Путь пакета теперь включает сегмент `.repository.`
- Пример: `org.springframework.ai.chat.memory.jdbc` → `org.springframework.ai.chat.memory.repository.jdbc`

#### Классы конфигурации

- Основные классы автоконфигурации теперь используют суффикс `Repository`
- Пример: `JdbcChatMemoryAutoConfiguration` → `JdbcChatMemoryRepositoryAutoConfiguration`

#### Свойства

- Свойства конфигурации переименованы с `spring.ai.chat.memory.<storage>...` на `spring.ai.chat.memory.repository.<storage>...`


**Требуется миграция:**
- Обновите ваши зависимости Maven/Gradle, чтобы использовать новые идентификаторы артефактов.
- Обновите любые импорты, ссылки на классы или конфигурацию, которые использовали старые имена пакетов или классов.

#### Рефакторинг агрегатора сообщений

##### Изменения- Класс `MessageAggregator` был перемещен из пакета `org.springframework.ai.chat.model` модуля `spring-ai-client-chat` в модуль `spring-ai-model` (тот же пакет)
- Метод `aggregateChatClientResponse` был удален из `MessageAggregator` и перемещен в новый класс `ChatClientMessageAggregator` в пакете `org.springframework.ai.chat.client`

##### Руководство по миграции

Если вы напрямую использовали метод `aggregateChatClientResponse` из `MessageAggregator`, вам нужно использовать новый класс `ChatClientMessageAggregator`:

```java
// До
new MessageAggregator().aggregateChatClientResponse(chatClientResponses, aggregationHandler);

// После
new ChatClientMessageAggregator().aggregateChatClientResponse(chatClientResponses, aggregationHandler);
```

Не забудьте добавить соответствующий импорт:

```java
import org.springframework.ai.chat.client.ChatClientMessageAggregator;
```

#### Watson
Модель ИИ Watson была удалена, так как она основывалась на устаревшей генерации текста, поскольку доступна новая модель генерации чата. Надеемся, что Watson появится в будущей версии Spring AI.

#### MoonShot и QianFan

Moonshot и Qianfan были удалены, так как они недоступны за пределами Китая. Они были перемещены в репозиторий Spring AI Community.

#### Удаленный векторный магазин
- Удалена автоконфигурация векторного магазина HanaDB (https://github.com/spring-projects/spring-ai/commit/f3b46244942c5072c2e2fa89e62cde71c61bbf25[f3b4624])

#### Управление памятью
- Удалена реализация CassandraChatMemory (https://github.com/spring-projects/spring-ai/commit/11e3c8f9a6636d77f203968b83625d3e5694c408[11e3c8f])
- Упрощена иерархия советников по памяти чата и удален устаревший API (https://github.com/spring-projects/spring-ai/commit/848a3fd31fadd07c9ba77f6dc30425389d095e9a[848a3fd])
- Удалены устаревшие методы в JdbcChatMemory (https://github.com/spring-projects/spring-ai/commit/356a68f15eea07a040bd27c66442472fc55e6475[356a68f])
- Рефакторинг артефактов репозитория памяти чата для ясности (https://github.com/spring-projects/spring-ai/commit/2d517eec5cd7ce5f88149b876ed57a06ad353e11[2d517ee])
- Рефакторинг автоконфигураций репозитория памяти чата и стартеров Spring Boot для ясности (https://github.com/spring-projects/spring-ai/commit/f6dba1bf083d847cdc07888ba62746683e3d61bb[f6dba1b])

#### API сообщений и шаблонов
- Удалены устаревшие конструкторы UserMessage (https://github.com/spring-projects/spring-ai/commit/06edee406978d172a1f87f4c7b255282f9d55e4c[06edee4])
- Удалены устаревшие конструкторы PromptTemplate (https://github.com/spring-projects/spring-ai/commit/722c77e812f3f3ea40cf2258056fcf1578b15c62[722c77e])
- Удалены устаревшие методы из Media (https://github.com/spring-projects/spring-ai/commit/228ef10bfbfe279d7d09f2a7ba166db873372118[228ef10])
- Рефакторинг StTemplateRenderer: переименован supportStFunctions в validateStFunctions (https://github.com/spring-projects/spring-ai/commit/0e15197298c0848b78a746f3d740191e6a6aee7a[0e15197])
- Удален оставшийся интерфейс TemplateRender после его перемещения (https://github.com/spring-projects/spring-ai/commit/52675d854ccecbc702cec24c4f070520eca64938[52675d8])

#### Дополнительные изменения в API клиента
- Удалены устаревшие методы в ChatClient и Advisors (https://github.com/spring-projects/spring-ai/commit/4fe74d886e26d52abf6f2f5545264d422a0be4b2[4fe74d8])
- Удалены устаревшие методы из OllamaApi и AnthropicApi (https://github.com/spring-projects/spring-ai/commit/46be8987d6bc385bf74b9296aa4308c7a8658d2f[46be898])

#### Изменения в структуре пакетов
- Удалены циклы зависимостей между пакетами в spring-ai-model (https://github.com/spring-projects/spring-ai/commit/ebfa5b9b2cc2ab0d20e25dc6128c4b1c9c327f89[ebfa5b9])
- Перемещен MessageAggregator в модуль spring-ai-model (https://github.com/spring-projects/spring-ai/commit/54e5c07428909ceec248e3bbd71e2df4b0812e49[54e5c07])

#### Зависимости- Удалена неиспользуемая зависимость json-path в spring-ai-openai (https://github.com/spring-projects/spring-ai/commit/9de13d1b2fdb67219dc7afbf319ade789784f2b9[9de13d1])

### Изменения в поведении

#### Azure OpenAI
- Добавлено управление идентификацией Entra ID для Azure OpenAI с чистой автоконфигурацией (https://github.com/spring-projects/spring-ai/commit/3dc86d33ce90ebd68ec3997a0eb4704ab7774e99[3dc86d3])

### Общая очистка
- Удалены все устаревшие коды (https://github.com/spring-projects/spring-ai/commit/76bee8ceb2854839f93a6c52876f50bb24219355[76bee8c]) и (https://github.com/spring-projects/spring-ai/commit/b6ce7f3e4a7aafe6b9031043f63813dde6e73605[b6ce7f3])

[[upgrading-to-1-0-0-m8]]
## Обновление до 1.0.0-M8

Вы можете автоматизировать процесс обновления до 1.0.0-M8, используя рецепт OpenRewrite.
Этот рецепт помогает применить многие необходимые изменения кода для этой версии.
Найдите рецепт и инструкции по использованию на https://github.com/arconia-io/arconia-migrations/blob/main/docs/spring-ai.md[Arconia Spring AI Migrations].

### Ломающее изменения

При обновлении с Spring AI 1.0 M7 до 1.0 M8 пользователи, которые ранее регистрировали обратные вызовы инструментов, сталкиваются с ломающе изменениями, которые приводят к молчаливому сбою функциональности вызова инструментов. Это особенно затрагивает код, который использовал устаревший метод `tools()`.

#### Пример

Вот пример кода, который работал в M7, но больше не функционирует должным образом в M8:

```java
// Это работало в M7, но молчаливо сбоит в M8
ChatClient chatClient = new OpenAiChatClient(api)
    .tools(List.of(
        new Tool("get_current_weather", "Получить текущую погоду в заданном месте", 
            new ToolSpecification.ToolParameter("location", "Город и штат, например, Сан-Франциско, Калифорния", true))
    ))
    .toolCallbacks(List.of(
        new ToolCallback("get_current_weather", (toolName, params) -> {
            // Логика получения погоды
            return Map.of("temperature", 72, "unit", "fahrenheit", "description", "Солнечно");
        })
    ));
```

#### Решение

Решение заключается в использовании метода `toolSpecifications()` вместо устаревшего метода `tools()`:

```java
// Это работает в M8
ChatClient chatClient = new OpenAiChatClient(api)
    .toolSpecifications(List.of(
        new Tool("get_current_weather", "Получить текущую погоду в заданном месте", 
            new ToolSpecification.ToolParameter("location", "Город и штат, например, Сан-Франциско, Калифорния", true))
    ))
    .toolCallbacks(List.of(
        new ToolCallback("get_current_weather", (toolName, params) -> {
            // Логика получения погоды
            return Map.of("temperature", 72, "unit", "fahrenheit", "description", "Солнечно");
        })
    ));
```

### Удаленные реализации и API

#### Управление памятью
- Удалена реализация CassandraChatMemory (https://github.com/spring-projects/spring-ai/commit/11e3c8f9a6636d77f203968b83625d3e5694c408[11e3c8f])
- Упрощена иерархия советников по памяти чата и удален устаревший API (https://github.com/spring-projects/spring-ai/commit/848a3fd31fadd07c9ba77f6dc30425389d095e9a[848a3fd])
- Удалены устаревания в JdbcChatMemory (https://github.com/spring-projects/spring-ai/commit/356a68f15eea07a040bd27c66442472fc55e6475[356a68f])
- Рефакторинг артефактов репозитория памяти чата для ясности (https://github.com/spring-projects/spring-ai/commit/2d517eec5cd7ce5f88149b876ed57a06ad353e11[2d517ee])
- Рефакторинг автоконфигураций репозитория памяти чата и стартеров Spring Boot для ясности (https://github.com/spring-projects/spring-ai/commit/f6dba1bf083d847cdc07888ba62746683e3d61bb[f6dba1b])

#### Клиентские API- Удалены устаревшие элементы в ChatClient и Advisors (https://github.com/spring-projects/spring-ai/commit/4fe74d886e26d52abf6f2f5545264d422a0be4b2[4fe74d8])
- Ломающие изменения в вызове инструмента chatclient (https://github.com/spring-projects/spring-ai/commit/5b7849de088b3c93c7ec894fcaddc85a611a8572[5b7849d])
- Удалены устаревшие элементы из OllamaApi и AnthropicApi (https://github.com/spring-projects/spring-ai/commit/46be8987d6bc385bf74b9296aa4308c7a8658d2f[46be898])

#### Message and Template APIs
- Удалены устаревшие конструкторы UserMessage (https://github.com/spring-projects/spring-ai/commit/06edee406978d172a1f87f4c7b255282f9d55e4c[06edee4])
- Удалены устаревшие конструкторы PromptTemplate (https://github.com/spring-projects/spring-ai/commit/722c77e812f3f3ea40cf2258056fcf1578b15c62[722c77e])
- Удалены устаревшие методы из Media (https://github.com/spring-projects/spring-ai/commit/228ef10bfbfe279d7d09f2a7ba166db873372118[228ef10])
- Рефакторинг StTemplateRenderer: переименован supportStFunctions в validateStFunctions (https://github.com/spring-projects/spring-ai/commit/0e15197298c0848b78a746f3d740191e6a6aee7a[0e15197])
- Удален оставшийся интерфейс TemplateRender после его перемещения (https://github.com/spring-projects/spring-ai/commit/52675d854ccecbc702cec24c4f070520eca64938[52675d8])

#### Model Implementations
- Удалена модель генерации текста Watson (https://github.com/spring-projects/spring-ai/commit/9e71b163e315199fe7b46495d87a0828a807b88f[9e71b16])
- Удален код Qianfan (https://github.com/spring-projects/spring-ai/commit/bfcaad7b5495c5927a62b44169e8713e044c2497[bfcaad7])
- Удалена автоконфигурация векторного хранилища HanaDB (https://github.com/spring-projects/spring-ai/commit/f3b46244942c5072c2e2fa89e62cde71c61bbf25[f3b4624])
- Удалены параметры deepseek из OpenAiApi (https://github.com/spring-projects/spring-ai/commit/59b36d14dab72d76f2f3d49ce9385a69faaabbba[59b36d1])

#### Package Structure Changes
- Удалены циклы зависимостей между пакетами в spring-ai-model (https://github.com/spring-projects/spring-ai/commit/ebfa5b9b2cc2ab0d20e25dc6128c4b1c9c327f89[ebfa5b9])
- Перемещен MessageAggregator в модуль spring-ai-model (https://github.com/spring-projects/spring-ai/commit/54e5c07428909ceec248e3bbd71e2df4b0812e49[54e5c07])

#### Dependencies
- Удалена неиспользуемая зависимость json-path в spring-ai-openai (https://github.com/spring-projects/spring-ai/commit/9de13d1b2fdb67219dc7afbf319ade789784f2b9[9de13d1])

### Behavior Changes

#### Observability
- Рефакторинг наблюдения за содержимым для использования логирования вместо трассировки (https://github.com/spring-projects/spring-ai/commit/ca843e85887aa1da6300c77550c379c103500897[ca843e8])
  ** Заменены фильтры наблюдения за содержимым на обработчики логирования
  ** Переименованы свойства конфигурации для лучшего отражения их назначения:
    *** `include-prompt` → `log-prompt`
    *** `include-completion` → `log-completion`
    *** `include-query-response` → `log-query-response`
  ** Добавлен `TracingAwareLoggingObservationHandler` для логирования с учетом трассировки
  ** Заменен `micrometer-tracing-bridge-otel` на `micrometer-tracing`
  ** Удалено трассирование на основе событий в пользу прямого логирования
  ** Удалена прямая зависимость от OTel SDK
  ** Переименован `includePrompt` в `logPrompt` в свойствах наблюдения (в `ChatClientBuilderProperties`, `ChatObservationProperties` и `ImageObservationProperties`)

#### Azure OpenAI
- Добавлено управление идентификацией Entra ID для Azure OpenAI с чистой автоконфигурацией (https://github.com/spring-projects/spring-ai/commit/3dc86d33ce90ebd68ec3997a0eb4704ab7774e99[3dc86d3])

### General Cleanup
- Удалены все устаревшие элементы из 1.0.0-M8 (https://github.com/spring-projects/spring-ai/commit/76bee8ceb2854839f93a6c52876f50bb24219355[76bee8c])
- Общая очистка устаревших элементов (https://github.com/spring-projects/spring-ai/commit/b6ce7f3e4a7aafe6b9031043f63813dde6e73605[b6ce7f3])

[[upgrading-to-1-0-0-m7]]
## Обновление до 1.0.0-M7

### Обзор измененийSpring AI 1.0.0-M7 является последним релизом вехи перед RC1 и GA релизами. Он вводит несколько важных изменений в идентификаторы артефактов, названия пакетов и структуру модулей, которые будут сохранены в финальном релизе.

### Изменения идентификаторов артефактов, пакетов и модулей
Версия 1.0.0-M7 включает те же структурные изменения, что и 1.0.0-SNAPSHOT.

Для получения подробной информации обратитесь к:
- xref:upgrade-notes.adoc#common-artifact-id-changes[Общие изменения идентификаторов артефактов]
- xref:upgrade-notes.adoc#common-package-changes[Общие изменения пакетов]
- xref:upgrade-notes.adoc#common-module-structure[Общая структура модулей]

### Обновление MCP Java SDK до 0.9.0

Spring AI 1.0.0-M7 теперь использует версию MCP Java SDK 0.9.0, которая включает значительные изменения по сравнению с предыдущими версиями. Если вы используете MCP в своих приложениях, вам нужно будет обновить свой код, чтобы учесть эти изменения.

Ключевые изменения включают:

#### Переименование интерфейсов

- `ClientMcpTransport` → `McpClientTransport`
- `ServerMcpTransport` → `McpServerTransport`
- `DefaultMcpSession` → `McpClientSession` или `McpServerSession`
- Все классы `**Registration` → классы `**Specification`

#### Изменения в создании сервера

- Используйте `McpServerTransportProvider` вместо `ServerMcpTransport`

```java
// До
ServerMcpTransport transport = new WebFluxSseServerTransport(objectMapper, "/mcp/message");
var server = McpServer.sync(transport)
    .serverInfo("my-server", "1.0.0")
    .build();

// После
McpServerTransportProvider transportProvider = new WebFluxSseServerTransportProvider(objectMapper, "/mcp/message");
var server = McpServer.sync(transportProvider)
    .serverInfo("my-server", "1.0.0")
    .build();
```

#### Изменения в сигнатурах обработчиков

Все обработчики теперь принимают параметр `exchange` в качестве первого аргумента:

```java
// До
.tool(calculatorTool, args -> new CallToolResult("Result: " + calculate(args)))

// После
.tool(calculatorTool, (exchange, args) -> new CallToolResult("Result: " + calculate(args)))
```

#### Взаимодействие с клиентом через Exchange

Методы, ранее доступные на сервере, теперь доступны через объект exchange:

```java
// До
ClientCapabilities capabilities = server.getClientCapabilities();
CreateMessageResult result = server.createMessage(new CreateMessageRequest(...));

// После
ClientCapabilities capabilities = exchange.getClientCapabilities();
CreateMessageResult result = exchange.createMessage(new CreateMessageRequest(...));
```

#### Изменения обработчиков Roots

```java
// До
.rootsChangeConsumers(List.of(
    roots -> System.out.println("Roots changed: " + roots)
))

// После
.rootsChangeHandlers(List.of(
    (exchange, roots) -> System.out.println("Roots changed: " + roots)
))
```

Для получения полного руководства по миграции кода MCP обратитесь к https://github.com/spring-projects/spring-ai/blob/main/spring-ai-docs/src/main/antora/modules/ROOT/pages/mcp-migration.adoc[Руководству по миграции MCP].

### Включение/выключение автонастройки моделиThe previous configuration properties for enabling/disabling model auto-configuration have been removed:

- `spring.ai.<provider>.chat.enabled`
- `spring.ai.<provider>.embedding.enabled`
- `spring.ai.<provider>.image.enabled`
- `spring.ai.<provider>.moderation.enabled`

По умолчанию, если провайдер модели (например, OpenAI, Ollama) найден в classpath, его соответствующая авто-конфигурация для релевантных типов моделей (чат, встраивание и т.д.) включена. Если присутствует несколько провайдеров для одного и того же типа модели (например, как `spring-ai-openai-spring-boot-starter`, так и `spring-ai-ollama-spring-boot-starter`), вы можете использовать следующие свойства, чтобы выбрать, **какая** авто-конфигурация провайдера должна быть активной, эффективно отключая другие для этого конкретного типа модели.

Чтобы полностью отключить авто-конфигурацию для конкретного типа модели, даже если присутствует только один провайдер, установите соответствующее свойство в значение, которое не соответствует ни одному провайдеру в classpath (например, `none` или `disabled`).

Вы можете обратиться к https://github.com/spring-projects/spring-ai/blob/main/spring-ai-model/src/main/java/org/springframework/ai/model/SpringAIModels.java[`SpringAIModels`] перечислению для получения списка известных значений провайдеров.

- `spring.ai.model.audio.speech=<model-provider|none>`
- `spring.ai.model.audio.transcription=<model-provider|none>`
- `spring.ai.model.chat=<model-provider|none>`
- `spring.ai.model.embedding=<model-provider|none>`
- `spring.ai.model.embedding.multimodal=<model-provider|none>`
- `spring.ai.model.embedding.text=<model-provider|none>`
- `spring.ai.model.image=<model-provider|none>`
- `spring.ai.model.moderation=<model-provider|none>`

### Автоматизация обновления с использованием ИИ

Вы можете автоматизировать процесс обновления до 1.0.0-M7 с помощью инструмента Claude Code CLI с предоставленным запросом:

1. Скачайте https://docs.anthropic.com/en/docs/agents-and-tools/claude-code/overview[инструмент Claude Code CLI]
2. Скопируйте запрос из файла https://github.com/spring-projects/spring-ai/blob/main/src/prompts/update-to-m7.txt[update-to-m7.txt]
3. Вставьте запрос в Claude Code CLI
4. ИИ проанализирует ваш проект и внесет необходимые изменения

> **Примечание:** Автоматизированный запрос на обновление в настоящее время обрабатывает изменения идентификаторов артефактов, перемещения пакетов и изменения структуры модулей, но пока не включает автоматические изменения для обновления до MCP 0.9.0. Если вы используете MCP, вам нужно будет вручную обновить ваш код, следуя рекомендациям в разделе xref:upgrade-notes.adoc#mcp-java-sdk-upgrade-to-0-9-0[Обновление MCP Java SDK].

[[common-sections]]
## Общие изменения между версиями

[[common-artifact-id-changes]]
### Изменения идентификаторов артефактов

Шаблон именования артефактов Spring AI стартеров изменился.
Вам нужно будет обновить ваши зависимости в соответствии со следующими шаблонами:

- Стартеры моделей: `spring-ai-\{model\}-spring-boot-starter` → `spring-ai-starter-model-\{model\}`
- Стартеры векторных хранилищ: `spring-ai-\{store\}-store-spring-boot-starter` → `spring-ai-starter-vector-store-\{store\}`
- Стартеры MCP: `spring-ai-mcp-\{type\}-spring-boot-starter` → `spring-ai-starter-mcp-\{type\}`

#### Примеры

[tabs]
======
Maven::
+
```xml,indent=0,subs="verbatim,quotes"
<!-- ДО -->
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-openai-spring-boot-starter</artifactId>
</dependency>

<!-- ПОСЛЕ -->
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-starter-model-openai</artifactId>
</dependency>
```

Gradle::
+
```groovy,indent=0,subs="verbatim,quotes"
// ДО
implementation 'org.springframework.ai:spring-ai-openai-spring-boot-starter'
implementation 'org.springframework.ai:spring-ai-redis-store-spring-boot-starter'

// ПОСЛЕ
implementation 'org.springframework.ai:spring-ai-starter-model-openai'
implementation 'org.springframework.ai:spring-ai-starter-vector-store-redis'
```
======

#### Изменения в артефактах авто-конфигурации Spring AIThe Spring AI автоконфигурация изменилась с единого монолитного артефакта на отдельные артефакты автоконфигурации для каждой модели, векторного хранилища и других компонентов. Это изменение было сделано для минимизации влияния конфликтующих версий зависимых библиотек, таких как Google Protocol Buffers, Google RPC и других. Разделяя автоконфигурацию на артефакты, специфичные для компонентов, вы можете избежать подключения ненужных зависимостей и снизить риск конфликтов версий в вашем приложении.

Исходный монолитный артефакт больше недоступен:

```xml,indent=0,subs="verbatim,quotes"
<!-- БОЛЬШЕ НЕ ДОСТУПЕН -->
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-spring-boot-autoconfigure</artifactId>
    <version>${project.version}</version>
</dependency>
```

Вместо этого каждый компонент теперь имеет свой собственный артефакт автоконфигурации, следуя этим шаблонам:

- Автоконфигурация модели: `spring-ai-autoconfigure-model-\{model\}`
- Автоконфигурация векторного хранилища: `spring-ai-autoconfigure-vector-store-\{store\}`
- Автоконфигурация MCP: `spring-ai-autoconfigure-mcp-\{type\}`

#### Примеры новых артефактов автоконфигурации

[tabs]
======
Модели::
+
```xml,indent=0,subs="verbatim,quotes"
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-autoconfigure-model-openai</artifactId>
</dependency>

<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-autoconfigure-model-anthropic</artifactId>
</dependency>

<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-autoconfigure-model-vertex-ai</artifactId>
</dependency>
```

Векторные хранилища::
+
```xml,indent=0,subs="verbatim,quotes"
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-autoconfigure-vector-store-redis</artifactId>
</dependency>

<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-autoconfigure-vector-store-pgvector</artifactId>
</dependency>

<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-autoconfigure-vector-store-chroma</artifactId>
</dependency>
```

MCP::
+
```xml,indent=0,subs="verbatim,quotes"
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-autoconfigure-mcp-client</artifactId>
</dependency>

<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-autoconfigure-mcp-server</artifactId>
</dependency>
```
======

> **Примечание:** В большинстве случаев вам не нужно будет явно добавлять эти зависимости автоконфигурации. Они включаются транзитивно при использовании соответствующих стартовых зависимостей.

[[common-package-changes]]
### Изменения в названиях пакетов

Ваш IDE должен помочь с рефакторингом на новые местоположения пакетов.

- `KeywordMetadataEnricher` и `SummaryMetadataEnricher` были перемещены из `org.springframework.ai.transformer` в `org.springframework.ai.chat.transformer`.
- `Content`, `MediaContent` и `Media` были перемещены из `org.springframework.ai.model` в `org.springframework.ai.content`.

[[common-module-structure]]
### Структура модуля

Проект претерпел значительные изменения в своей структуре модулей и артефактов. Ранее `spring-ai-core` содержал все центральные интерфейсы, но теперь он был разделен на специализированные доменные модули, чтобы уменьшить ненужные зависимости в ваших приложениях.

![Spring AI Dependencies, width=1000, align="center"](spring-ai-dependencies.png)

#### spring-ai-commons

Базовый модуль без зависимостей от других модулей Spring AI. Содержит:
- Основные доменные модели (`Document`, `TextSplitter`)
- Утилиты JSON и обработка ресурсов
- Поддержка структурированного логирования и наблюдаемости

#### spring-ai-modelProvides AI capability abstractions:
- Интерфейсы, такие как `ChatModel`, `EmbeddingModel` и `ImageModel`
- Типы сообщений и шаблоны запросов
- Фреймворк вызова функций (`ToolDefinition`, `ToolCallback`)
- Поддержка фильтрации контента и наблюдения

#### spring-ai-vector-store

Унифицированная абстракция векторной базы данных:
- Интерфейс `VectorStore` для поиска по сходству
- Расширенная фильтрация с помощью выражений, похожих на SQL
- `SimpleVectorStore` для использования в памяти
- Поддержка пакетной обработки для встраиваний

#### spring-ai-client-chat

API для высокоуровневого разговорного ИИ:
- Интерфейс `ChatClient`
- Сохранение разговоров через `ChatMemory`
- Преобразование ответов с помощью `OutputConverter`
- Перехват на основе советников
- Поддержка синхронной и реактивной потоковой передачи

#### spring-ai-advisors-vector-store

Связывает чат с векторными хранилищами для RAG:
- `QuestionAnswerAdvisor`: внедряет контекст в запросы
- `VectorStoreChatMemoryAdvisor`: сохраняет/извлекает историю разговоров

#### spring-ai-model-chat-memory-cassandra

Постоянство Apache Cassandra для `ChatMemory`:
- Реализация `CassandraChatMemory`
- Безопасный по типу CQL с помощью QueryBuilder Cassandra

#### spring-ai-model-chat-memory-neo4j

Постоянство графовой базы данных Neo4j для разговоров в чате.

#### spring-ai-rag

Комплексный фреймворк для Retrieval Augmented Generation:
- Модульная архитектура для RAG-пайплайнов
- `RetrievalAugmentationAdvisor` в качестве основной точки входа
- Принципы функционального программирования с составными компонентами

### Структура зависимостей

Иерархию зависимостей можно резюмировать следующим образом:

- `spring-ai-commons` (основа)
- `spring-ai-model` (зависит от commons)
- `spring-ai-vector-store` и `spring-ai-client-chat` (обе зависят от model)
- `spring-ai-advisors-vector-store` и `spring-ai-rag` (зависят от client-chat и vector-store)
- Модули `spring-ai-model-chat-memory-*` (зависят от client-chat)

[[common-toolcontext-changes]]
### Изменения в ToolContext

Класс `ToolContext` был улучшен для поддержки как явного, так и неявного разрешения инструментов. Инструменты теперь могут быть:

1. **Явно включены**: Инструменты, которые явно запрашиваются в запросе и включаются в вызов модели.
2. **Неявно доступны**: Инструменты, которые становятся доступными для динамического разрешения во время выполнения, но никогда не включаются в вызов модели, если не запрашиваются явно.

Начиная с версии 1.0.0-M7, инструменты включаются в вызов модели только в том случае, если они явно запрашиваются в запросе или явно включены в вызов.

Кроме того, класс `ToolContext` теперь помечен как final и не может быть расширен. Он никогда не предназначался для наследования. Вы можете добавить все необходимые контекстные данные при создании `ToolContext` в виде `Map<String, Object>`. Для получения дополнительной информации смотрите [документацию](https://docs.spring.io/spring-ai/reference/api/tools.html#_tool_context).

[[upgrading-to-1-0-0-m6]]
## Обновление до 1.0.0-M6

### Изменения в интерфейсе использования и реализации DefaultUsage

Интерфейс `Usage` и его реализация по умолчанию `DefaultUsage` претерпели следующие изменения:

1. Переименование метода:
- `getGenerationTokens()` теперь `getCompletionTokens()`

2. Изменения типов:
- Все поля подсчета токенов в `DefaultUsage` изменены с `Long` на `Integer`:
** `promptTokens`
** `completionTokens` (ранее `generationTokens`)
** `totalTokens`

#### Необходимые действия

- Замените все вызовы `getGenerationTokens()` на `getCompletionTokens()`

- Обновите вызовы конструктора `DefaultUsage`:
```java
// Старый (M5)
new DefaultUsage(Long promptTokens, Long generationTokens, Long totalTokens)

// Новый (M6)
new DefaultUsage(Integer promptTokens, Integer completionTokens, Integer totalTokens)
```

> **Примечание:** Для получения дополнительной информации о работе с использованием, смотрите xref:api/usage-handling.adoc[здесь]

#### Изменения в JSON Ser/DeserWhile M6 сохраняет обратную совместимость для десериализации JSON поля `generationTokens`, это поле будет удалено в M7. Все сохраненные JSON документы, использующие старое имя поля, должны быть обновлены для использования `completionTokens`.

Пример нового формата JSON:
```json
{
  "promptTokens": 100,
  "completionTokens": 50,
  "totalTokens": 150
}
```

### Изменения в использовании FunctionCallingOptions для вызова инструментов

Каждый экземпляр `ChatModel` при создании принимает необязательный экземпляр `ChatOptions` или `FunctionCallingOptions`, который можно использовать для настройки стандартных инструментов, используемых для вызова модели.

До 1.0.0-M6:

- любой инструмент, переданный через метод `functions()` стандартного экземпляра `FunctionCallingOptions`, включался в каждый вызов модели из этого экземпляра `ChatModel`, возможно, переопределенный параметрами времени выполнения.
- любой инструмент, переданный через метод `functionCallbacks()` стандартного экземпляра `FunctionCallingOptions`, был доступен только для динамического разрешения во время выполнения (см. xref:api/tools.adoc#_tool_resolution[Разрешение инструментов]), но никогда не включался в вызов модели, если это не запрашивалось явно.

Начиная с 1.0.0-M6:

- любой инструмент, переданный через метод `functions()` или `functionCallbacks()` стандартного экземпляра `FunctionCallingOptions`, теперь обрабатывается одинаково: он включается в каждый вызов модели из этого экземпляра `ChatModel`, возможно, переопределенный параметрами времени выполнения. Это обеспечивает согласованность в том, как инструменты включаются в вызовы модели и предотвращает путаницу из-за различий в поведении между `functionCallbacks()` и всеми другими опциями.

Если вы хотите сделать инструмент доступным для динамического разрешения во время выполнения и включить его в запрос чата к модели только по явному запросу, вы можете использовать одну из стратегий, описанных в xref:api/tools.adoc#_tool_resolution[Разрешение инструментов].

> **Примечание:** 1.0.0-M6 представил новые API для обработки вызовов инструментов. Обратная совместимость сохраняется для старых API во всех сценариях, кроме описанного выше. Старые API все еще доступны, но они устарели и будут удалены в 1.0.0-M7.

### Удаление устаревших моделей чата Amazon Bedrock

Начиная с 1.0.0-M6, Spring AI перешел на использование API Converse от Amazon Bedrock для всех реализаций чата в Spring AI. Все модели чата Amazon Bedrock удалены, кроме моделей Embedding для Cohere и Titan.

> **Примечание:** Обратитесь к документации xref:api/chat/bedrock-converse.adoc[Bedrock Converse] для использования моделей чата.

### Изменения в использовании Spring Boot 3.4.2 для управления зависимостями

Spring AI обновляется для использования Spring Boot 3.4.2 для управления зависимостями. Вы можете обратиться к https://github.com/spring-projects/spring-boot/blob/v3.4.2/spring-boot-project/spring-boot-dependencies/build.gradle[здесь] для получения информации о зависимостях, управляемых Spring Boot 3.4.2.

#### Необходимые действия

- Если вы обновляетесь до Spring Boot 3.4.2, пожалуйста, убедитесь, что вы ознакомились с https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-3.4-Release-Notes#upgrading-from-spring-boot-33[этой] документацией для изменений, необходимых для настройки REST-клиента. В частности, если у вас нет библиотеки HTTP-клиента в classpath, это, вероятно, приведет к использованию `JdkClientHttpRequestFactory`, где ранее использовался `SimpleClientHttpRequestFactory`. Чтобы переключиться на использование `SimpleClientHttpRequestFactory`, вам нужно установить `spring.http.client.factory=simple`.
- Если вы используете другую версию Spring Boot (например, Spring Boot 3.3.x) и вам нужна конкретная версия зависимости, вы можете переопределить ее в вашей конфигурации сборки.

### Изменения в API Vector StoreВ версии 1.0.0-M6 метод `delete` в интерфейсе `VectorStore` был изменен на операцию без возвращаемого значения вместо возврата `Optional<Boolean>`. Если ваш код ранее проверял возвращаемое значение операции удаления, вам необходимо удалить эту проверку. Теперь операция выбрасывает исключение, если удаление не удалось, что обеспечивает более прямую обработку ошибок.

#### До 1.0.0-M6:
```java
Optional<Boolean> result = vectorStore.delete(ids);
if (result.isPresent() && result.get()) {
    // обработка успешного удаления
}
```

#### В 1.0.0-M6 и позже:
```java
vectorStore.delete(ids);
// удаление успешно, если исключение не выброшено
```

## Обновление до 1.0.0.M5

- Конструкторы текущей реализации VectorStore были переработаны для обеспечения согласованности.
- Конструкторы текущей реализации VectorStore устарели, используйте паттерн строителя.
- Пакеты реализации VectorStore были перемещены в уникальные имена пакетов, чтобы избежать конфликтов между артефактами. Например, `org.springframework.ai.vectorstore` стал `org.springframework.ai.pgvector.vectorstore`.

## Обновление до 1.0.0.RC3

- Тип параметров чата (`frequencyPenalty`, `presencePenalty`, `temperature`, `topP`) был изменен с `Float` на `Double`.

## Обновление до 1.0.0.M2

- Префикс конфигурации для Chroma Vector Store был изменен с `spring.ai.vectorstore.chroma.store` на `spring.ai.vectorstore.chroma`, чтобы соответствовать соглашениям об именовании других векторных хранилищ.

- Значение по умолчанию для свойства `initialize-schema` в векторных хранилищах, способных инициализировать схему, теперь установлено в `false`. Это подразумевает, что приложения теперь должны явно выбирать инициализацию схемы в поддерживаемых векторных хранилищах, если ожидается создание схемы при запуске приложения. Не все векторные хранилища поддерживают это свойство. См. соответствующую документацию векторного хранилища для получения дополнительной информации. Следующие векторные хранилища в настоящее время не поддерживают свойство `initialize-schema`.

1. Hana
2. Pinecone
3. Weaviate

- В Bedrock Jurassic 2 параметры чата `countPenalty`, `frequencyPenalty` и `presencePenalty` были переименованы в `countPenaltyOptions`, `frequencyPenaltyOptions` и `presencePenaltyOptions`. Кроме того, тип параметра чата `stopSequences` был изменен с `String[]` на `List<String>`.

- В Azure OpenAI тип параметров чата `frequencyPenalty` и `presencePenalty` был изменен с `Double` на `Float`, что согласуется со всеми другими реализациями.

## Обновление до 1.0.0.M1

На нашем пути к выпуску 1.0.0 M1 мы внесли несколько разрушающих изменений. Приносим извинения, это к лучшему!

### Изменения ChatClientA major change was made that took the 'old' `ChatClient` and moved the functionality into `ChatModel`.  The 'new' `ChatClient` now takes an instance of `ChatModel`. This was done to support a fluent API for creating and executing prompts in a style similar to other client classes in the Spring ecosystem, such as `RestClient`, `WebClient`, and `JdbcClient`.  Refer to the [JavaDoc](https://docs.spring.io/spring-ai/docs/api) for more information on the Fluent API, proper reference documentation is coming shortly.

Мы переименовали 'старый' `ModelClient` в `Model` и переименовали классы-реализаторы, например, `ImageClient` был переименован в `ImageModel`.  Реализация `Model` представляет собой уровень портируемости, который преобразует между API Spring AI и базовым API AI Model.

Создан новый пакет `model`, который содержит интерфейсы и базовые классы для поддержки создания AI Model Clients для любых комбинаций типов входных/выходных данных. В данный момент пакеты chat и image model реализуют это. Мы скоро обновим пакет embedding до этой новой модели.

Новый шаблон проектирования "портативные опции". Мы хотели предоставить как можно больше портируемости в `ModelCall` для различных AI моделей на основе чата. Существует общий набор опций генерации, а также те, которые специфичны для поставщика модели. Используется своего рода подход "утиного типирования". `ModelOptions` в пакете model является маркерным интерфейсом, указывающим, что реализации этого класса будут предоставлять опции для модели. См. `ImageOptions`, подинтерфейс, который определяет портативные опции для всех реализаций `ImageModel` от текста к изображению. Затем `StabilityAiImageOptions` и `OpenAiImageOptions` предоставляют опции, специфичные для каждого поставщика модели. Все классы опций создаются с помощью строителя во флюентном API, все они могут быть переданы в портативный API `ImageModel`. Эти типы данных опций используются в автоконфигурации/свойствах конфигурации для реализаций `ImageModel`.

### Изменения в названиях артефактов

Переименованы названия POM артефактов:
- spring-ai-qdrant -> spring-ai-qdrant-store
- spring-ai-cassandra -> spring-ai-cassandra-store
- spring-ai-pinecone -> spring-ai-pinecone-store
- spring-ai-redis -> spring-ai-redis-store
- spring-ai-qdrant -> spring-ai-qdrant-store
- spring-ai-gemfire -> spring-ai-gemfire-store
- spring-ai-azure-vector-store-spring-boot-starter -> spring-ai-azure-store-spring-boot-starter
- spring-ai-redis-spring-boot-starter -> spring-ai-starter-vector-store-redis

## Обновление до 0.8.1

Бывший `spring-ai-vertex-ai` был переименован в `spring-ai-vertex-ai-palm2`, а `spring-ai-vertex-ai-spring-boot-starter` был переименован в `spring-ai-vertex-ai-palm2-spring-boot-starter`.

Таким образом, вам нужно изменить зависимость с

```xml
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-vertex-ai</artifactId>
</dependency>
```

на

```xml
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-vertex-ai-palm2</artifactId>
</dependency>
```

и связанный Boot стартер для модели Palm2 изменился с

```xml
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-vertex-ai-spring-boot-starter</artifactId>
</dependency>
```

на

```xml
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-vertex-ai-palm2-spring-boot-starter</artifactId>
</dependency>
```

- Переименованные классы (01.03.2024)

** VertexAiApi -> VertexAiPalm2Api  
** VertexAiClientChat -> VertexAiPalm2ChatClient  
** VertexAiEmbeddingClient -> VertexAiPalm2EmbeddingClient  
** VertexAiChatOptions -> VertexAiPalm2ChatOptions  

## Обновление до 0.8.0

### Обновление 24 января 2024 года- Перемещение пакетов `prompt`, `messages` и `metadata` в подпакеты `org.springframework.ai.chat`
- Новая функциональность — **клиенты текст в изображение**. Классы: `OpenAiImageModel` и `StabilityAiImageModel`. Смотрите интеграционные тесты для использования, документация скоро будет доступна.
- Новый пакет `model`, который содержит интерфейсы и базовые классы для поддержки создания AI Model Clients для любых комбинаций типов входных/выходных данных. В данный момент пакеты моделей чата и изображений реализуют это. Мы скоро обновим пакет embedding до этой новой модели.
- Новый шаблон проектирования "портативные опции". Мы хотели обеспечить максимальную портативность в `ModelCall` для различных AI моделей на основе чата. Существует общий набор опций генерации и те, которые специфичны для поставщика модели. Используется своего рода подход "утиного типирования". `ModelOptions` в пакете model — это маркерный интерфейс, указывающий, что реализации этого класса будут предоставлять опции для модели. Смотрите `ImageOptions`, подинтерфейс, который определяет портативные опции для всех реализаций `ImageModel` текст->изображение. Затем `StabilityAiImageOptions` и `OpenAiImageOptions` предоставляют опции, специфичные для каждого поставщика модели. Все классы опций создаются с помощью флюентного API билдера, все могут быть переданы в портативный API `ImageModel`. Эти типы данных опций используются в автоконфигурации/свойствах конфигурации для реализаций `ImageModel`.

### Обновление 13 января 2024 года

Следующие свойства автоконфигурации OpenAi для чата изменились

- с `spring.ai.openai.model` на `spring.ai.openai.chat.options.model`.
- с `spring.ai.openai.temperature` на `spring.ai.openai.chat.options.temperature`.

Найдите обновленную документацию о свойствах OpenAi: https://docs.spring.io/spring-ai/reference/api/chat/openai-chat.html

### Обновление 27 декабря 2023 года

Объединение SimplePersistentVectorStore и InMemoryVectorStore в SimpleVectorStore
- Заменить InMemoryVectorStore на SimpleVectorStore

### Обновление 20 декабря 2023 года

Рефакторинг клиента Ollama и связанных классов и названий пакетов

- Заменить org.springframework.ai.ollama.client.OllamaClient на org.springframework.ai.ollama.OllamaModelCall.
- Подписи методов OllamaChatClient изменились.
- Переименовать org.springframework.ai.autoconfigure.ollama.OllamaProperties в org.springframework.ai.model.ollama.autoconfigure.OllamaChatProperties и изменить суффикс на: `spring.ai.ollama.chat`. Некоторые свойства также изменились.

### Обновление 19 декабря 2023 года

Переименование AiClient и связанных классов и названий пакетов

- Переименовать AiClient в ChatClient
- Переименовать AiResponse в ChatResponse
- Переименовать AiStreamClient в StreamingChatClient
- Переименовать пакет org.sf.ai.client в org.sf.ai.chat

Переименовать артефакт ID

- `transformers-embedding` в `spring-ai-transformers`

Перемещение модулей Maven из верхнего уровня и подпапки `embedding-clients` в единую папку `models`.

[ПРЕДУПРЕЖДЕНИЕ]

### 1 декабря 2023 года

Мы переходим на новый Group ID проекта:

- **ИЗ**: `org.springframework.experimental.ai`
- **В**: `org.springframework.ai`

Артефакты по-прежнему будут размещены в репозитории снимков, как показано ниже.

Основная ветка перейдет на версию `0.8.0-SNAPSHOT`.
Она будет нестабильной в течение недели или двух.
Пожалуйста, используйте 0.7.1-SNAPSHOT, если не хотите быть на переднем крае.

Вы можете получить доступ к артефактам `0.7.1-SNAPSHOT`, как и прежде, и по-прежнему получить доступ к https://markpollack.github.io/spring-ai-0.7.1/[Документация 0.7.1-SNAPSHOT].

### Зависимости 0.7.1-SNAPSHOT- Azure OpenAI
+
```xml
<dependency>
    <groupId>org.springframework.experimental.ai</groupId>
    <artifactId>spring-ai-azure-openai-spring-boot-starter</artifactId>
    <version>0.7.1-SNAPSHOT</version>
</dependency>
```

- OpenAI
+
```xml
<dependency>
    <groupId>org.springframework.experimental.ai</groupId>
    <artifactId>spring-ai-openai-spring-boot-starter</artifactId>
    <version>0.7.1-SNAPSHOT</version>
</dependency>
```
