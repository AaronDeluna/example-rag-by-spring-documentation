# Google GenAI Text Embeddings

[Google GenAI Embeddings API](https://ai.google.dev/gemini-api/docs/embeddings) предоставляет генерацию текстовых эмбеддингов с использованием моделей эмбеддингов Google через Gemini Developer API или Vertex AI. Этот документ описывает, как создать текстовые эмбеддинги с помощью Google GenAI Text embeddings API.

API текстовых эмбеддингов Google GenAI использует плотные векторные представления. В отличие от разреженных векторов, которые, как правило, напрямую сопоставляют слова с числами, плотные векторы предназначены для лучшего представления смысла текста. Преимущество использования плотных векторных эмбеддингов в генеративном ИИ заключается в том, что вместо поиска прямых совпадений слов или синтаксиса вы можете лучше искать фрагменты, которые соответствуют смыслу запроса, даже если эти фрагменты не используют тот же язык.

[NOTE]
====
В настоящее время SDK Google GenAI поддерживает только текстовые эмбеддинги. Поддержка мультимодальных эмбеддингов ожидается и будет добавлена, когда она станет доступна в SDK.
====

Эта реализация предоставляет два режима аутентификации:

- **Gemini Developer API**: Используйте API-ключ для быстрого прототипирования и разработки
- **Vertex AI**: Используйте учетные данные Google Cloud для развертываний в производственной среде с корпоративными функциями

## Предварительные требования

Выберите один из следующих методов аутентификации:

### Вариант 1: Gemini Developer API (API-ключ)

- Получите API-ключ в [Google AI Studio](https://aistudio.google.com/app/apikey)
- Установите API-ключ в качестве переменной окружения или в свойствах вашего приложения

### Вариант 2: Vertex AI (Google Cloud)

- Установите [gcloud](https://cloud.google.com/sdk/docs/install) CLI, соответствующий вашей ОС.
- Аутентифицируйтесь, выполнив следующую команду. Замените `PROJECT_ID` на идентификатор вашего проекта Google Cloud и `ACCOUNT` на ваше имя пользователя Google Cloud.

[source]
```
gcloud config set project <PROJECT_ID> &&
gcloud auth application-default login <ACCOUNT>
```

### Добавление репозиториев и BOM

Артефакты Spring AI публикуются в Maven Central и Spring Snapshot репозиториях. Обратитесь к разделу xref:getting-started.adoc#artifact-repositories[Artifact Repositories], чтобы добавить эти репозитории в вашу систему сборки.

Чтобы помочь с управлением зависимостями, Spring AI предоставляет BOM (bill of materials), чтобы гарантировать, что одна и та же версия Spring AI используется на протяжении всего проекта. Обратитесь к разделу xref:getting-started.adoc#dependency-management[Dependency Management], чтобы добавить BOM Spring AI в вашу систему сборки.

## Автоконфигурация

[NOTE]
====
В автоконфигурации Spring AI произошли значительные изменения в названиях артефактов стартовых модулей. Пожалуйста, обратитесь к [заметкам об обновлении](https://docs.spring.io/spring-ai/reference/upgrade-notes.html) для получения дополнительной информации.
====

Spring AI предоставляет автоконфигурацию Spring Boot для модели эмбеддингов Google GenAI. Чтобы включить ее, добавьте следующую зависимость в файл `pom.xml` вашего проекта Maven:

```xml
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-starter-model-google-genai-embedding</artifactId>
</dependency>
```

или в файл сборки Gradle `build.gradle`.

```groovy
dependencies {
    implementation 'org.springframework.ai:spring-ai-starter-model-google-genai-embedding'
}
```

> **Совет:** Обратитесь к разделу xref:getting-started.adoc#dependency-management[Dependency Management], чтобы добавить BOM Spring AI в ваш файл сборки.

### Свойства эмбеддингов

#### Свойства подключения

Префикс `spring.ai.google.genai.embedding` используется в качестве префикса свойств, который позволяет вам подключаться к Google GenAI Embedding API.

[NOTE]
====
Свойства подключения общие для модуля Google GenAI Chat. Если вы используете как чат, так и эмбеддинги, вам нужно настроить подключение только один раз, используя либо префикс `spring.ai.google.genai` (для чата), либо префикс `spring.ai.google.genai.embedding` (для эмбеддингов).
====

[cols="3,5,1", stripes=even]
| Свойство | Описание | По умолчанию

| spring.ai.google.genai.embedding.api-key   | API-ключ для Gemini Developer API. Если он предоставлен, клиент использует Gemini Developer API вместо Vertex AI. |  -
| spring.ai.google.genai.embedding.project-id   | Идентификатор проекта Google Cloud Platform (обязателен для режима Vertex AI) |  -
| spring.ai.google.genai.embedding.location   | Регион Google Cloud (обязателен для режима Vertex AI) |  -
| spring.ai.google.genai.embedding.credentials-uri   | URI для учетных данных Google Cloud. Если он предоставлен, используется для создания экземпляра `GoogleCredentials` для аутентификации. |  -


[NOTE]
====
Включение и отключение автоконфигураций эмбеддингов теперь настраивается через свойства верхнего уровня с префиксом `spring.ai.model.embedding`.

Чтобы включить, используйте spring.ai.model.embedding.text=google-genai (по умолчанию включено)

Чтобы отключить, используйте spring.ai.model.embedding.text=none (или любое значение, которое не соответствует google-genai)

Это изменение сделано для возможности настройки нескольких моделей.
====

#### Свойства текстовых эмбеддингов

Префикс `spring.ai.google.genai.embedding.text` — это префикс свойств, который позволяет вам настраивать реализацию модели эмбеддингов для Google GenAI Text Embedding.

[cols="3,5,1", stripes=even]
| Свойство | Описание | По умолчанию

| spring.ai.model.embedding.text | Включить модель Google GenAI Embedding API. | google-genai
| spring.ai.google.genai.embedding.text.options.model | Модель [Google GenAI Text Embedding](https://ai.google.dev/gemini-api/docs/models/gemini#text-embedding), которую следует использовать. Поддерживаемые модели включают `text-embedding-004` и `text-multilingual-embedding-002` | text-embedding-004
| spring.ai.google.genai.embedding.text.options.task-type | Предполагаемое приложение, чтобы помочь модели производить более качественные эмбеддинги. Доступные [task-types](https://ai.google.dev/api/embeddings#tasktype): `RETRIEVAL_QUERY`, `RETRIEVAL_DOCUMENT`, `SEMANTIC_SIMILARITY`, `CLASSIFICATION`, `CLUSTERING`, `QUESTION_ANSWERING`, `FACT_VERIFICATION`  | `RETRIEVAL_DOCUMENT`
| spring.ai.google.genai.embedding.text.options.title | Необязательный заголовок, действителен только при task_type=RETRIEVAL_DOCUMENT.  | -
| spring.ai.google.genai.embedding.text.options.dimensions | Количество измерений, которые должны иметь результирующие выходные эмбеддинги. Поддерживается для версии модели 004 и выше. Вы можете использовать этот параметр для уменьшения размера эмбеддинга, например, для оптимизации хранения.  | -
| spring.ai.google.genai.embedding.text.options.auto-truncate | Если установлено в true, входной текст будет обрезан. Если установлено в false, будет возвращена ошибка, если входной текст длиннее максимальной длины, поддерживаемой моделью.  | true

## Пример контроллера

[Создайте](https://start.spring.io/) новый проект Spring Boot и добавьте `spring-ai-starter-model-google-genai-embedding` в зависимости вашего pom (или gradle).

Добавьте файл `application.properties` в директорию `src/main/resources`, чтобы включить и настроить модель эмбеддингов Google GenAI:

### Используя Gemini Developer API (API-ключ)

```application.properties
spring.ai.google.genai.embedding.api-key=YOUR_API_KEY
spring.ai.google.genai.embedding.text.options.model=text-embedding-004
```

### Используя Vertex AI

```application.properties
spring.ai.google.genai.embedding.project-id=YOUR_PROJECT_ID
spring.ai.google.genai.embedding.location=YOUR_PROJECT_LOCATION
spring.ai.google.genai.embedding.text.options.model=text-embedding-004
```

Это создаст реализацию `GoogleGenAiTextEmbeddingModel`, которую вы можете внедрить в свой класс. Вот пример простого класса `@Controller`, который использует модель эмбеддингов для генерации эмбеддингов.

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

[GoogleGenAiTextEmbeddingModel](https://github.com/spring-projects/spring-ai/blob/main/models/spring-ai-google-genai-embedding/src/main/java/org/springframework/ai/google/genai/text/GoogleGenAiTextEmbeddingModel.java) реализует `EmbeddingModel`.

Добавьте зависимость `spring-ai-google-genai-embedding` в файл `pom.xml` вашего проекта Maven:

```xml
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-google-genai-embedding</artifactId>
</dependency>
```

или в файл сборки Gradle `build.gradle`.

```groovy
dependencies {
    implementation 'org.springframework.ai:spring-ai-google-genai-embedding'
}
```

> **Совет:** Обратитесь к разделу xref:getting-started.adoc#dependency-management[Dependency Management], чтобы добавить BOM Spring AI в ваш файл сборки.

Затем создайте `GoogleGenAiTextEmbeddingModel` и используйте его для текстовых эмбеддингов:

### Используя API-ключ

```java
GoogleGenAiEmbeddingConnectionDetails connectionDetails =
    GoogleGenAiEmbeddingConnectionDetails.builder()
        .apiKey(System.getenv("GOOGLE_API_KEY"))
        .build();

GoogleGenAiTextEmbeddingOptions options = GoogleGenAiTextEmbeddingOptions.builder()
    .model(GoogleGenAiTextEmbeddingOptions.DEFAULT_MODEL_NAME)
    .taskType(TaskType.RETRIEVAL_DOCUMENT)
    .build();

var embeddingModel = new GoogleGenAiTextEmbeddingModel(connectionDetails, options);

EmbeddingResponse embeddingResponse = embeddingModel
	.embedForResponse(List.of("Hello World", "World is big and salvation is near"));
```

### Используя Vertex AI

```java
GoogleGenAiEmbeddingConnectionDetails connectionDetails =
    GoogleGenAiEmbeddingConnectionDetails.builder()
        .projectId(System.getenv("GOOGLE_CLOUD_PROJECT"))
        .location(System.getenv("GOOGLE_CLOUD_LOCATION"))
        .build();

GoogleGenAiTextEmbeddingOptions options = GoogleGenAiTextEmbeddingOptions.builder()
    .model(GoogleGenAiTextEmbeddingOptions.DEFAULT_MODEL_NAME)
    .taskType(TaskType.RETRIEVAL_DOCUMENT)
    .build();

var embeddingModel = new GoogleGenAiTextEmbeddingModel(connectionDetails, options);

EmbeddingResponse embeddingResponse = embeddingModel
	.embedForResponse(List.of("Hello World", "World is big and salvation is near"));
```

## Типы задач

API эмбеддингов Google GenAI поддерживает различные типы задач для оптимизации эмбеддингов для конкретных случаев использования:

- `RETRIEVAL_QUERY`: Оптимизирован для поисковых запросов в системах извлечения
- `RETRIEVAL_DOCUMENT`: Оптимизирован для документов в системах извлечения
- `SEMANTIC_SIMILARITY`: Оптимизирован для измерения семантического сходства между текстами
- `CLASSIFICATION`: Оптимизирован для задач классификации текста
- `CLUSTERING`: Оптимизирован для кластеризации схожих текстов
- `QUESTION_ANSWERING`: Оптимизирован для систем вопрос-ответ
- `FACT_VERIFICATION`: Оптимизирован для задач проверки фактов

Пример использования различных типов задач:

```java
// Для индексации документов
GoogleGenAiTextEmbeddingOptions docOptions = GoogleGenAiTextEmbeddingOptions.builder()
    .model("text-embedding-004")
    .taskType(TaskType.RETRIEVAL_DOCUMENT)
    .title("Product Documentation")  // Необязательный заголовок для документов
    .build();

// Для поисковых запросов
GoogleGenAiTextEmbeddingOptions queryOptions = GoogleGenAiTextEmbeddingOptions.builder()
    .model("text-embedding-004")
    .taskType(TaskType.RETRIEVAL_QUERY)
    .build();
```

## Снижение размерности

Для версии модели 004 и выше вы можете уменьшить размерность эмбеддингов для оптимизации хранения:

```java
GoogleGenAiTextEmbeddingOptions options = GoogleGenAiTextEmbeddingOptions.builder()
    .model("text-embedding-004")
    .dimensions(256)  // Уменьшение с 768 до 256 измерений
    .build();
```

## Миграция с текстовых эмбеддингов Vertex AI

Если вы в настоящее время используете реализацию текстовых эмбеддингов Vertex AI (`spring-ai-vertex-ai-embedding`), вы можете мигрировать на Google GenAI с минимальными изменениями:

### Ключевые различия

1. **SDK**: Google GenAI использует новый `com.google.genai.Client` вместо SDK Vertex AI
2. **Аутентификация**: Поддерживает как API-ключ, так и учетные данные Google Cloud
3. **Имена пакетов**: Классы находятся в `org.springframework.ai.google.genai.text` вместо `org.springframework.ai.vertexai.embedding`
4. **Префикс свойств**: Использует `spring.ai.google.genai.embedding` вместо `spring.ai.vertex.ai.embedding`
5. **Детали подключения**: Использует `GoogleGenAiEmbeddingConnectionDetails` вместо `VertexAiEmbeddingConnectionDetails`

### Когда использовать Google GenAI против текстовых эмбеддингов Vertex AI

**Используйте Google GenAI Embeddings, когда:**
- Вы хотите быстрое прототипирование с API-ключами
- Вам нужны последние функции эмбеддингов от Developer API
- Вы хотите гибкость в переключении между режимами API-ключа и Vertex AI
- Вы уже используете Google GenAI для чата

**Используйте текстовые эмбеддинги Vertex AI, когда:**
- У вас есть существующая инфраструктура Vertex AI
- Вам нужны мультимодальные эмбеддинги (в настоящее время доступны только в Vertex AI)
- Ваша организация требует развертывания только в Google Cloud
