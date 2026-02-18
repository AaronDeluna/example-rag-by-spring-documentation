# Google VertexAI Text Embeddings

Vertex AI поддерживает два типа моделей встраивания: текстовые и мультимодальные. Этот документ описывает, как создать текстовое встраивание с помощью Vertex AI [Text embeddings API](https://cloud.google.com/vertex-ai/generative-ai/docs/model-reference/text-embeddings-api).

API текстовых встраиваний Vertex AI использует плотные векторные представления. В отличие от разреженных векторов, которые, как правило, напрямую сопоставляют слова с числами, плотные векторы предназначены для лучшего представления смысла текста. Преимущество использования плотных встраиваний в генеративном ИИ заключается в том, что вместо поиска прямых совпадений слов или синтаксиса вы можете лучше искать фрагменты, которые соответствуют смыслу запроса, даже если эти фрагменты не используют тот же язык.

## Предварительные требования

- Установите [gcloud](https://cloud.google.com/sdk/docs/install) CLI, подходящий для вашей операционной системы.
- Аутентифицируйтесь, выполнив следующую команду. Замените `PROJECT_ID` на идентификатор вашего проекта Google Cloud и `ACCOUNT` на ваше имя пользователя Google Cloud.

[source]
```
gcloud config set project <PROJECT_ID> &&
gcloud auth application-default login <ACCOUNT>
```

### Добавление репозиториев и BOM

Артефакты Spring AI публикуются в репозиториях Maven Central и Spring Snapshot. Обратитесь к разделу xref:getting-started.adoc#artifact-repositories[Artifact Repositories], чтобы добавить эти репозитории в вашу систему сборки.

Чтобы помочь с управлением зависимостями, Spring AI предоставляет BOM (bill of materials), чтобы гарантировать, что в проекте используется согласованная версия Spring AI. Обратитесь к разделу xref:getting-started.adoc#dependency-management[Dependency Management], чтобы добавить BOM Spring AI в вашу систему сборки.

## Автоконфигурация

[NOTE]
====
В автоконфигурации Spring AI произошли значительные изменения в названиях артефактов модулей-стартеров. Пожалуйста, обратитесь к https://docs.spring.io/spring-ai/reference/upgrade-notes.html[upgrade notes] для получения дополнительной информации.
====

Spring AI предоставляет автоконфигурацию Spring Boot для модели встраивания VertexAI. Чтобы включить ее, добавьте следующую зависимость в файл `pom.xml` вашего проекта Maven:

```xml
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-starter-model-vertex-ai-embedding</artifactId>
</dependency>
```

или в файл сборки Gradle `build.gradle`.

```groovy
dependencies {
    implementation 'org.springframework.ai:spring-ai-starter-model-vertex-ai-embedding'
}
```

> **Совет:** Обратитесь к разделу xref:getting-started.adoc#dependency-management[Dependency Management], чтобы добавить BOM Spring AI в ваш файл сборки.

### Свойства встраиванияThe prefix `spring.ai.vertex.ai.embedding` используется в качестве префикса свойств, который позволяет вам подключиться к VertexAI Embedding API.

[cols="3,5,1", stripes=even]
|====
| Свойство | Описание | По умолчанию

| spring.ai.vertex.ai.embedding.project-id   | Идентификатор проекта Google Cloud Platform |  -
| spring.ai.vertex.ai.embedding.location   | Регион |  -
| spring.ai.vertex.ai.embedding.apiEndpoint   | Конечная точка Vertex AI Embedding API. |  -

|====

[NOTE]
====
Включение и отключение авто-конфигураций встраивания теперь настраивается через свойства верхнего уровня с префиксом `spring.ai.model.embedding`.

Чтобы включить, используйте spring.ai.model.embedding.text=vertexai (по умолчанию включено)

Чтобы отключить, используйте spring.ai.model.embedding.text=none (или любое значение, которое не соответствует vertexai)

Это изменение сделано для возможности конфигурации нескольких моделей.
====

Префикс `spring.ai.vertex.ai.embedding.text` — это префикс свойств, который позволяет вам настроить реализацию модели встраивания для VertexAI Text Embedding.

[cols="3,5,1", stripes=even]
|====
| Свойство | Описание | По умолчанию

| spring.ai.vertex.ai.embedding.text.enabled (Удалено и больше не актуально) | Включить модель Vertex AI Embedding API. | true
| spring.ai.model.embedding.text | Включить модель Vertex AI Embedding API. | vertexai
| spring.ai.vertex.ai.embedding.text.options.model | Это [модель встраивания текста Vertex](https://cloud.google.com/vertex-ai/generative-ai/docs/embeddings/get-text-embeddings#supported-models), которую следует использовать | text-embedding-004
| spring.ai.vertex.ai.embedding.text.options.task-type | Предназначенное приложение для улучшения качества встраиваний модели. Доступные [типы задач](https://cloud.google.com/vertex-ai/generative-ai/docs/model-reference/text-embeddings-api#request_body)  | `RETRIEVAL_DOCUMENT`
| spring.ai.vertex.ai.embedding.text.options.title | Необязательный заголовок, действителен только при task_type=RETRIEVAL_DOCUMENT.  | -
| spring.ai.vertex.ai.embedding.text.options.dimensions | Количество измерений, которые должны иметь полученные встраивания. Поддерживается для версии модели 004 и выше. Вы можете использовать этот параметр для уменьшения размера встраивания, например, для оптимизации хранения.  | -
| spring.ai.vertex.ai.embedding.text.options.auto-truncate | Если установлено в true, входной текст будет обрезан. Если установлено в false, будет возвращена ошибка, если входной текст длиннее максимальной длины, поддерживаемой моделью.  | true
|====

## Пример контроллера

https://start.spring.io/[Создайте] новый проект Spring Boot и добавьте `spring-ai-starter-model-vertex-ai-embedding` в зависимости вашего pom (или gradle).

Добавьте файл `application.properties` в директорию `src/main/resources`, чтобы включить и настроить модель чата VertexAi:

```application.properties
spring.ai.vertex.ai.embedding.project-id=<YOUR_PROJECT_ID>
spring.ai.vertex.ai.embedding.location=<YOUR_PROJECT_LOCATION>
spring.ai.vertex.ai.embedding.text.options.model=text-embedding-004
```

Это создаст реализацию `VertexAiTextEmbeddingModel`, которую вы можете внедрить в свой класс.
Вот пример простого класса `@Controller`, который использует модель встраивания для генерации встраиваний.

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

## Ручная конфигурацияМодель [VertexAiTextEmbeddingModel](https://github.com/spring-projects/spring-ai/blob/main/models/spring-ai-vertex-ai-embedding/src/main/java/org/springframework/ai/vertexai/embedding/VertexAiTextEmbeddingModel.java) реализует интерфейс `EmbeddingModel`.

Добавьте зависимость `spring-ai-vertex-ai-embedding` в файл `pom.xml` вашего проекта Maven:

```xml
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-vertex-ai-embedding</artifactId>
</dependency>
```

или в файл сборки Gradle `build.gradle`.

```groovy
dependencies {
    implementation 'org.springframework.ai:spring-ai-vertex-ai-embedding'
}
```

> **Совет:** Обратитесь к разделу xref:getting-started.adoc#dependency-management[Управление зависимостями], чтобы добавить BOM Spring AI в ваш файл сборки.

Далее создайте `VertexAiTextEmbeddingModel` и используйте его для генерации текста:

```java
VertexAiEmbeddingConnectionDetails connectionDetails =
    VertexAiEmbeddingConnectionDetails.builder()
        .projectId(System.getenv(<VERTEX_AI_GEMINI_PROJECT_ID>))
        .location(System.getenv(<VERTEX_AI_GEMINI_LOCATION>))
        .build();

VertexAiTextEmbeddingOptions options = VertexAiTextEmbeddingOptions.builder()
    .model(VertexAiTextEmbeddingOptions.DEFAULT_MODEL_NAME)
    .build();

var embeddingModel = new VertexAiTextEmbeddingModel(this.connectionDetails, this.options);

EmbeddingResponse embeddingResponse = this.embeddingModel
	.embedForResponse(List.of("Hello World", "World is big and salvation is near"));
```

### Загрузка учетных данных из учетной записи Google Service Account

Чтобы программно загрузить GoogleCredentials из json-файла учетной записи сервиса, вы можете использовать следующее:

```java
GoogleCredentials credentials = GoogleCredentials.fromStream(<INPUT_STREAM_TO_CREDENTIALS_JSON>)
        .createScoped("https://www.googleapis.com/auth/cloud-platform");
credentials.refreshIfExpired();

VertexAiEmbeddingConnectionDetails connectionDetails =
    VertexAiEmbeddingConnectionDetails.builder()
        .projectId(System.getenv(<VERTEX_AI_GEMINI_PROJECT_ID>))
        .location(System.getenv(<VERTEX_AI_GEMINI_LOCATION>))
        .apiEndpoint(endpoint)
        .predictionServiceSettings(
            PredictionServiceSettings.newBuilder()
                .setEndpoint(endpoint)
                .setCredentialsProvider(FixedCredentialsProvider.create(credentials))
                .build());
```
