# OpenAI Embeddings

Spring AI поддерживает модели текстовых эмбеддингов OpenAI. Эмбеддинги текста OpenAI измеряют взаимосвязь текстовых строк. Эмбеддинг — это вектор (список) чисел с плавающей запятой. Расстояние между двумя векторами измеряет их взаимосвязь. Небольшие расстояния указывают на высокую взаимосвязь, а большие расстояния — на низкую взаимосвязь.

## Предварительные требования

Вам необходимо создать API в OpenAI для доступа к моделям эмбеддингов OpenAI.

Создайте учетную запись на https://platform.openai.com/signup[страница регистрации OpenAI] и сгенерируйте токен на https://platform.openai.com/account/api-keys[странице API-ключей].

Проект Spring AI определяет свойство конфигурации с именем `spring.ai.openai.api-key`, которое вы должны установить в значение `API Key`, полученное с openai.com.

Вы можете установить это свойство конфигурации в вашем файле `application.properties`:

```properties
spring.ai.openai.api-key=<ваш-openai-api-ключ>
```

Для повышения безопасности при работе с конфиденциальной информацией, такой как API-ключи, вы можете использовать язык выражений Spring (SpEL) для ссылки на переменную окружения:

```yaml
# В application.yml
spring:
  ai:
    openai:
      api-key: ${OPENAI_API_KEY}
```

```bash
# В вашем окружении или .env файле
export OPENAI_API_KEY=<ваш-openai-api-ключ>
```

Вы также можете установить эту конфигурацию программно в коде вашего приложения:

```java
// Получите API-ключ из безопасного источника или переменной окружения
String apiKey = System.getenv("OPENAI_API_KEY");
```

### Добавление репозиториев и BOM

Артефакты Spring AI публикуются в репозиториях Maven Central и Spring Snapshot. Обратитесь к разделу xref:getting-started.adoc#artifact-repositories[Артефакты репозиториев], чтобы добавить эти репозитории в вашу систему сборки.

Для упрощения управления зависимостями Spring AI предоставляет BOM (спецификация материалов), чтобы гарантировать использование согласованной версии Spring AI на протяжении всего проекта. Обратитесь к разделу xref:getting-started.adoc#dependency-management[Управление зависимостями], чтобы добавить BOM Spring AI в вашу систему сборки.

## Автоконфигурация

[NOTE]
====
В автоконфигурации Spring AI произошли значительные изменения в названиях артефактов модулей стартеров. Пожалуйста, обратитесь к https://docs.spring.io/spring-ai/reference/upgrade-notes.html[заметкам об обновлении] для получения дополнительной информации.
====

Spring AI предоставляет автоконфигурацию Spring Boot для модели эмбеддингов OpenAI. Чтобы включить ее, добавьте следующую зависимость в файл `pom.xml` вашего проекта Maven:

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

### Свойства эмбеддингов

#### Свойства повторных попыток

Префикс `spring.ai.retry` используется как префикс свойства, который позволяет вам настроить механизм повторных попыток для модели эмбеддингов OpenAI.

[cols="3,5,1", stripes=even]
| Свойство | Описание | По умолчанию

| spring.ai.retry.max-attempts   | Максимальное количество попыток повторного запроса. |  10
| spring.ai.retry.backoff.initial-interval | Начальная продолжительность ожидания для политики экспоненциального увеличения. |  2 сек.
| spring.ai.retry.backoff.multiplier | Множитель интервала ожидания. |  5
| spring.ai.retry.backoff.max-interval | Максимальная продолжительность ожидания. |  3 мин.
| spring.ai.retry.on-client-errors | Если false, выбросить NonTransientAiException и не пытаться повторить запрос для кодов ошибок клиента `4xx` | false
| spring.ai.retry.exclude-on-http-codes | Список кодов состояния HTTP, которые не должны вызывать повторный запрос (например, для выброса NonTransientAiException). | пусто
| spring.ai.retry.on-http-codes | Список кодов состояния HTTP, которые должны вызывать повторный запрос (например, для выброса TransientAiException). | пусто

#### Свойства подключенияThe prefix `spring.ai.openai` используется как префикс свойств, который позволяет вам подключаться к OpenAI.

[cols="3,5,1", stripes=even]
| Свойство | Описание | По умолчанию

| spring.ai.openai.base-url   | URL для подключения |  +https://api.openai.com+
| spring.ai.openai.api-key    | API-ключ           |  -
| spring.ai.openai.organization-id | При желании вы можете указать, какая организация используется для API-запроса. |  -
| spring.ai.openai.project-id      | При желании вы можете указать, какой проект используется для API-запроса. |  -

> **Совет:** Для пользователей, которые принадлежат нескольким организациям (или получают доступ к своим проектам через свой устаревший API-ключ пользователя), при желании вы можете указать, какая организация и проект используются для API-запроса. Использование этих API-запросов будет учитываться как использование для указанной организации и проекта.

#### Свойства конфигурации

[NOTE]
====
Включение и отключение автонастроек встраивания теперь настраивается через свойства верхнего уровня с префиксом `spring.ai.model.embedding`.

Чтобы включить, используйте spring.ai.model.embedding=openai (по умолчанию включено)

Чтобы отключить, используйте spring.ai.model.embedding=none (или любое значение, которое не соответствует openai)

Это изменение сделано для того, чтобы позволить конфигурацию нескольких моделей.
====

Префикс `spring.ai.openai.embedding` — это префикс свойств, который настраивает реализацию `EmbeddingModel` для OpenAI.

[cols="3,5,1", stripes=even]
| Свойство | Описание | По умолчанию

| spring.ai.openai.embedding.enabled (Обязательно и больше не действительно) | Включить модель встраивания OpenAI.  | true
| spring.ai.model.embedding | Включить модель встраивания OpenAI.  | openai
| spring.ai.openai.embedding.base-url   | Необязательно переопределяет spring.ai.openai.base-url для предоставления специфического URL для встраивания | -
| spring.ai.openai.embedding.embeddings-path   | Путь, который нужно добавить к базовому URL  |  `/v1/embeddings`
| spring.ai.openai.embedding.api-key    | Необязательно переопределяет spring.ai.openai.api-key для предоставления специфического API-ключа для встраивания  | -
| spring.ai.openai.embedding.organization-id | При желании вы можете указать, какая организация используется для API-запроса. |  -
| spring.ai.openai.embedding.project-id      | При желании вы можете указать, какой проект используется для API-запроса. |  -
| spring.ai.openai.embedding.metadata-mode      | Режим извлечения содержимого документа.      | EMBED
| spring.ai.openai.embedding.options.model      | Модель для использования      | text-embedding-ada-002 (другие варианты: text-embedding-3-large, text-embedding-3-small)
| spring.ai.openai.embedding.options.encodingFormat   | Формат, в котором будут возвращены встраивания. Может быть либо float, либо base64.  | -
| spring.ai.openai.embedding.options.user   | Уникальный идентификатор, представляющий вашего конечного пользователя, который может помочь OpenAI отслеживать и выявлять злоупотребления.  | -
| spring.ai.openai.embedding.options.dimensions   | Количество измерений, которые должны иметь результирующие выходные встраивания. Поддерживается только в моделях `text-embedding-3` и более поздних.  | -

> **Примечание:** Вы можете переопределить общие `spring.ai.openai.base-url` и `spring.ai.openai.api-key` для реализаций `ChatModel` и `EmbeddingModel`. Свойства `spring.ai.openai.embedding.base-url` и `spring.ai.openai.embedding.api-key`, если они установлены, имеют приоритет над общими свойствами. Аналогично, свойства `spring.ai.openai.chat.base-url` и `spring.ai.openai.chat.api-key`, если они установлены, имеют приоритет над общими свойствами. Это полезно, если вы хотите использовать разные учетные записи OpenAI для разных моделей и различных конечных точек моделей.

> **Совет:** Все свойства с префиксом `spring.ai.openai.embedding.options` могут быть переопределены во время выполнения, добавив специфические для запроса <<embedding-options>> в вызов `EmbeddingRequest`.

## Параметры времени выполнения [[embedding-options]]The https://github.com/spring-projects/spring-ai/blob/main/models/spring-ai-openai/src/main/java/org/springframework/ai/openai/OpenAiEmbeddingOptions.java[OpenAiEmbeddingOptions.java] предоставляет конфигурации OpenAI, такие как используемая модель и т.д.

Настройки по умолчанию также можно настроить с помощью свойств `spring.ai.openai.embedding.options`.

При запуске используйте конструктор `OpenAiEmbeddingModel`, чтобы установить параметры по умолчанию, используемые для всех запросов на встраивание. Во время выполнения вы можете переопределить параметры по умолчанию, используя экземпляр `OpenAiEmbeddingOptions` в качестве части вашего `EmbeddingRequest`.

Например, чтобы переопределить имя модели по умолчанию для конкретного запроса:

```java
EmbeddingResponse embeddingResponse = embeddingModel.call(
    new EmbeddingRequest(List.of("Hello World", "World is big and salvation is near"),
        OpenAiEmbeddingOptions.builder()
            .model("Different-Embedding-Model-Deployment-Name")
        .build()));
```

## Пример контроллера

Это создаст реализацию `EmbeddingModel`, которую вы можете внедрить в свой класс. Вот пример простого класса `@Controller`, который использует реализацию `EmbeddingModel`.

```application.properties
spring.ai.openai.api-key=YOUR_API_KEY
spring.ai.openai.embedding.options.model=text-embedding-ada-002
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
        EmbeddingResponse embeddingResponse = this.embeddingModel.embedForResponse(List.of(message));
        return Map.of("embedding", embeddingResponse);
    }
}
```

## Ручная конфигурация

Если вы не используете Spring Boot, вы можете вручную настроить модель встраивания OpenAI. Для этого добавьте зависимость `spring-ai-openai` в файл `pom.xml` вашего проекта Maven:
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

> **Совет:** Обратитесь к разделу xref:getting-started.adoc#dependency-management[Управление зависимостями], чтобы добавить BOM Spring AI в ваш файл сборки.

> **Примечание:** Зависимость `spring-ai-openai` также предоставляет доступ к `OpenAiChatModel`. Для получения дополнительной информации о `OpenAiChatModel` обратитесь к разделу [OpenAI Chat Client](../chat/openai-chat.html).

Далее создайте экземпляр `OpenAiEmbeddingModel` и используйте его для вычисления сходства между двумя входными текстами:

```java
var openAiApi = OpenAiApi.builder()
                .apiKey(System.getenv("OPENAI_API_KEY"))
                .build();

var embeddingModel = new OpenAiEmbeddingModel(
		this.openAiApi,
        MetadataMode.EMBED,
        OpenAiEmbeddingOptions.builder()
                .model("text-embedding-ada-002")
                .user("user-6")
                .build(),
        RetryUtils.DEFAULT_RETRY_TEMPLATE);

EmbeddingResponse embeddingResponse = this.embeddingModel
        .embedForResponse(List.of("Hello World", "World is big and salvation is near"));
```

`OpenAiEmbeddingOptions` предоставляет информацию о конфигурации для запросов на встраивание. Класс api и options предлагает метод `builder()` для удобного создания опций.
