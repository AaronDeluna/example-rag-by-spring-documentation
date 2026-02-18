# Mistral AI Embeddings

Spring AI поддерживает модели текстовых эмбеддингов от Mistral AI. Эмбеддинги — это векторные представления текста, которые захватывают семантическое значение абзацев через их положение в многомерном векторном пространстве. API эмбеддингов Mistral AI предлагает передовые, современные эмбеддинги для текста, которые могут быть использованы для многих задач обработки естественного языка (NLP).

## Доступные модели

Mistral AI предоставляет две модели эмбеддингов, каждая из которых оптимизирована для различных случаев использования:

| Модель | Размерности | Случай использования | Описание |
| --- | --- | --- | --- |

| `mistral-embed` |
| --- |
| 1024 |
| Общий текст |
| Модель эмбеддингов общего назначения, подходящая для семантического поиска, кластеризации и задач текстового сходства. Идеально подходит для контента на естественном языке. |

| `codestral-embed` |
| --- |
| 1536 |
| Код |
| Специализированная модель эмбеддингов, оптимизированная для сходства кода, поиска кода и генерации с дополнением извлечения (RAG) с репозиториями кода. Обеспечивает эмбеддинги с более высокой размерностью, специально разработанные для понимания семантики кода. |

При выборе модели:

- Используйте `mistral-embed` для общего текстового контента, такого как документы, статьи или запросы пользователей
- Используйте `codestral-embed`, когда работаете с кодом, технической документацией или создаете системы RAG, осведомленные о коде

## Предварительные требования

Вам необходимо создать API с MistralAI, чтобы получить доступ к моделям эмбеддингов MistralAI.

Создайте учетную запись на https://auth.mistral.ai/ui/registration[страница регистрации MistralAI] и сгенерируйте токен на https://console.mistral.ai/api-keys/[странице API ключей].

Проект Spring AI определяет свойство конфигурации с именем `spring.ai.mistralai.api-key`, которое вы должны установить в значение `API Key`, полученное с console.mistral.ai.

Вы можете установить это свойство конфигурации в вашем файле `application.properties`:

```properties
spring.ai.mistralai.api-key=<ваш-mistralai-api-key>
```

Для повышения безопасности при работе с конфиденциальной информацией, такой как API ключи, вы можете использовать язык выражений Spring (SpEL) для ссылки на переменную окружения:

```yaml
# В application.yml
spring:
  ai:
    mistralai:
      api-key: ${MISTRALAI_API_KEY}
```

```bash
# В вашем окружении или .env файле
export MISTRALAI_API_KEY=<ваш-mistralai-api-key>
```

Вы также можете установить эту конфигурацию программно в коде вашего приложения:

```java
// Получите API ключ из безопасного источника или переменной окружения
String apiKey = System.getenv("MISTRALAI_API_KEY");
```

### Добавление репозиториев и BOM

Артефакты Spring AI публикуются в репозиториях Maven Central и Spring Snapshot. Обратитесь к разделу xref:getting-started.adoc#artifact-repositories[Артефакты репозиториев], чтобы добавить эти репозитории в вашу систему сборки.

Чтобы помочь с управлением зависимостями, Spring AI предоставляет BOM (спецификация материалов), чтобы гарантировать, что в проекте используется согласованная версия Spring AI. Обратитесь к разделу xref:getting-started.adoc#dependency-management[Управление зависимостями], чтобы добавить BOM Spring AI в вашу систему сборки.

## Автоконфигурация

[NOTE]
====
В автоконфигурации Spring AI произошли значительные изменения в названиях артефактов модулей стартеров. Пожалуйста, обратитесь к https://docs.spring.io/spring-ai/reference/upgrade-notes.html[заметкам об обновлении] для получения дополнительной информации.
====

Spring AI предоставляет автоконфигурацию Spring Boot для модели эмбеддингов MistralAI. Чтобы включить ее, добавьте следующую зависимость в файл `pom.xml` вашего проекта Maven:

```xml
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-starter-model-mistral-ai</artifactId>
</dependency>
```

или в файл сборки Gradle `build.gradle`.

```groovy
dependencies {
    implementation 'org.springframework.ai:spring-ai-starter-model-mistral-ai'
}
```

> **Совет:** Обратитесь к разделу xref:getting-started.adoc#dependency-management[Управление зависимостями], чтобы добавить BOM Spring AI в ваш файл сборки.

### Свойства эмбеддингов

#### Свойства повторной попытки

Префикс `spring.ai.retry` используется как префикс свойства, который позволяет вам настроить механизм повторной попытки для модели эмбеддингов Mistral AI.

| Свойство | Описание | По умолчанию |
| --- | --- | --- |

| spring.ai.retry.max-attempts | Максимальное количество попыток повторной попытки. | 10 |
| --- | --- | --- |
| spring.ai.retry.backoff.initial-interval | Начальная продолжительность ожидания для политики экспоненциального увеличения. | 2 сек. |
| spring.ai.retry.backoff.multiplier | Множитель интервала ожидания. | 5 |
| spring.ai.retry.backoff.max-interval | Максимальная продолжительность ожидания. | 3 мин. |
| spring.ai.retry.on-client-errors | Если false, выбросить NonTransientAiException и не пытаться повторить для кодов ошибок клиента `4xx` | false |
| spring.ai.retry.exclude-on-http-codes | Список кодов состояния HTTP, которые не должны вызывать повторную попытку (например, для выброса NonTransientAiException). | пусто |
| spring.ai.retry.on-http-codes | Список кодов состояния HTTP, которые должны вызывать повторную попытку (например, для выброса TransientAiException). | пусто |

#### Свойства подключения

Префикс `spring.ai.mistralai` используется как префикс свойства, который позволяет вам подключиться к MistralAI.

| Свойство | Описание | По умолчанию |
| --- | --- | --- |

| spring.ai.mistralai.base-url | URL для подключения | https://api.mistral.ai |
| --- | --- | --- |
| spring.ai.mistralai.api-key | API ключ | - |

#### Свойства конфигурации

[NOTE]
====
Включение и отключение автоконфигураций эмбеддингов теперь настраивается через свойства верхнего уровня с префиксом `spring.ai.model.embedding`.

Чтобы включить, spring.ai.model.embedding=mistral (по умолчанию включено)

Чтобы отключить, spring.ai.model.embedding=none (или любое значение, которое не соответствует mistral)

Это изменение сделано для того, чтобы позволить конфигурацию нескольких моделей.
====

Префикс `spring.ai.mistralai.embedding` — это префикс свойства, который настраивает реализацию `EmbeddingModel` для MistralAI.

| Свойство | Описание | По умолчанию |
| --- | --- | --- |

| spring.ai.mistralai.embedding.enabled (Удалено и больше не действительно) | Включить модель эмбеддингов OpenAI. | true |
| --- | --- | --- |
| spring.ai.model.embedding | Включить модель эмбеддингов OpenAI. | mistral |
| spring.ai.mistralai.embedding.base-url | Необязательный переопределяет spring.ai.mistralai.base-url для предоставления специфического URL для эмбеддингов | - |
| spring.ai.mistralai.embedding.api-key | Необязательный переопределяет spring.ai.mistralai.api-key для предоставления специфического API ключа для эмбеддингов | - |
| spring.ai.mistralai.embedding.metadata-mode | Режим извлечения содержимого документа. | EMBED |
| spring.ai.mistralai.embedding.options.model | Модель для использования | mistral-embed |
| spring.ai.mistralai.embedding.options.encodingFormat | Формат для возврата эмбеддингов. Может быть либо float, либо base64. | - |

> **Примечание:** Вы можете переопределить общие `spring.ai.mistralai.base-url` и `spring.ai.mistralai.api-key` для реализаций `ChatModel` и `EmbeddingModel`. Свойства `spring.ai.mistralai.embedding.base-url` и `spring.ai.mistralai.embedding.api-key`, если установлены, имеют приоритет над общими свойствами. Аналогично, свойства `spring.ai.mistralai.chat.base-url` и `spring.ai.mistralai.chat.api-key`, если установлены, имеют приоритет над общими свойствами. Это полезно, если вы хотите использовать разные учетные записи MistralAI для различных моделей и различных конечных точек моделей.

> **Совет:** Все свойства с префиксом `spring.ai.mistralai.embedding.options` могут быть переопределены во время выполнения, добавив специфические для запроса <<embedding-options>> в вызов `EmbeddingRequest`.


Файл https://github.com/spring-projects/spring-ai/blob/main/models/spring-ai-mistral-ai/src/main/java/org/springframework/ai/mistralai/MistralAiEmbeddingOptions.java[MistralAiEmbeddingOptions.java] предоставляет конфигурации MistralAI, такие как модель для использования и т.д.

Параметры по умолчанию также могут быть настроены с помощью свойств `spring.ai.mistralai.embedding.options`.

При старте используйте конструктор `MistralAiEmbeddingModel`, чтобы установить параметры по умолчанию, используемые для всех запросов на эмбеддинги. Во время выполнения вы можете переопределить параметры по умолчанию, используя экземпляр `MistralAiEmbeddingOptions` как часть вашего `EmbeddingRequest`.

Например, чтобы переопределить имя модели по умолчанию для конкретного запроса:

```java
// Используя mistral-embed для общего текста
EmbeddingResponse textEmbeddingResponse = embeddingModel.call(
    new EmbeddingRequest(List.of("Hello World", "World is big and salvation is near"),
        MistralAiEmbeddingOptions.builder()
            .withModel("mistral-embed")
        .build()));

// Используя codestral-embed для кода
EmbeddingResponse codeEmbeddingResponse = embeddingModel.call(
    new EmbeddingRequest(List.of("public class HelloWorld {}", "def hello_world():"),
        MistralAiEmbeddingOptions.builder()
            .withModel("codestral-embed")
        .build()));
```

## Пример контроллера

Это создаст реализацию `EmbeddingModel`, которую вы можете внедрить в свой класс. Вот пример простого класса `@Controller`, который использует реализацию `EmbeddingModel`.

```application.properties
spring.ai.mistralai.api-key=YOUR_API_KEY
spring.ai.mistralai.embedding.options.model=mistral-embed
```

```java
@RestController
public class EmbeddingController {

    private final EmbeddingModel embeddingModel;

    @Autowired
    public EmbeddingController(EmbeddingModel embeddingModel) {
        this.embeddingModel = embeddingModel;
    }

    @GetMapping("/ai/embedding")
    public Map embed(@RequestParam(value = "message", defaultValue = "Tell me a joke") String message) {
        var embeddingResponse = this.embeddingModel.embedForResponse(List.of(message));
        return Map.of("embedding", embeddingResponse);
    }
}
```

## Ручная конфигурация

Если вы не используете Spring Boot, вы можете вручную настроить модель эмбеддингов OpenAI. Для этого добавьте зависимость `spring-ai-mistral-ai` в файл `pom.xml` вашего проекта Maven:
```xml
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-mistral-ai</artifactId>
</dependency>
```

или в файл сборки Gradle `build.gradle`.

```groovy
dependencies {
    implementation 'org.springframework.ai:spring-ai-mistral-ai'
}
```

> **Совет:** Обратитесь к разделу xref:getting-started.adoc#dependency-management[Управление зависимостями], чтобы добавить BOM Spring AI в ваш файл сборки.

> **Примечание:** Зависимость `spring-ai-mistral-ai` также предоставляет доступ к `MistralAiChatModel`. Для получения дополнительной информации о `MistralAiChatModel` обратитесь к разделу [Клиент чата MistralAI](../chat/mistralai-chat.html).

Далее создайте экземпляр `MistralAiEmbeddingModel` и используйте его для вычисления сходства между двумя входными текстами:

```java
var mistralAiApi = new MistralAiApi(System.getenv("MISTRAL_AI_API_KEY"));

var embeddingModel = new MistralAiEmbeddingModel(this.mistralAiApi,
        MistralAiEmbeddingOptions.builder()
                .withModel("mistral-embed")
                .withEncodingFormat("float")
                .build());

EmbeddingResponse embeddingResponse = this.embeddingModel
        .embedForResponse(List.of("Hello World", "World is big and salvation is near"));
```

Класс `MistralAiEmbeddingOptions` предоставляет информацию о конфигурации для запросов на эмбеддинги. Класс параметров предлагает метод `builder()` для удобного создания параметров.
