# Google VertexAI Multimodal Embeddings

> **Примечание:** ЭКСПЕРИМЕНТАЛЬНО. Используется только в экспериментальных целях. Пока не совместимо с `VectorStores`.

Vertex AI поддерживает два типа моделей встраивания: текстовые и мультимодальные. Этот документ описывает, как создать мультимодальное встраивание с помощью API [Multimodal embeddings](https://cloud.google.com/vertex-ai/generative-ai/docs/embeddings/get-multimodal-embeddings) Vertex AI.

Модель мультимодальных встраиваний генерирует векторы размерности 1408 на основе предоставленных вами данных, которые могут включать комбинацию изображений, текста и видео. Векторы встраивания затем могут быть использованы для последующих задач, таких как классификация изображений или модерация видео-контента.

Вектор встраивания изображения и вектор встраивания текста находятся в одном семантическом пространстве с одинаковой размерностью. Следовательно, эти векторы могут использоваться взаимозаменяемо для таких случаев, как поиск изображения по тексту или поиск видео по изображению.

> **Примечание:** API VertexAI Multimodal накладывает [следующие ограничения](https://cloud.google.com/vertex-ai/generative-ai/docs/embeddings/get-multimodal-embeddings#api-limits).

> **Совет:** Для случаев использования только текстовых встраиваний мы рекомендуем использовать xref:api/embeddings/vertexai-embeddings-text.adoc[модель текстовых встраиваний Vertex AI].

## Предварительные требования

- Установите [gcloud](https://cloud.google.com/sdk/docs/install) CLI, подходящий для вашей ОС.
- Аутентифицируйтесь, выполнив следующую команду. Замените `PROJECT_ID` на идентификатор вашего проекта Google Cloud и `ACCOUNT` на ваше имя пользователя Google Cloud.

[source]
```
gcloud config set project <PROJECT_ID> &&
gcloud auth application-default login <ACCOUNT>
```

### Добавление репозиториев и BOM

Артефакты Spring AI публикуются в репозиториях Maven Central и Spring Snapshot. Обратитесь к разделу xref:getting-started.adoc#artifact-repositories[Репозитории артефактов], чтобы добавить эти репозитории в вашу систему сборки.

Чтобы помочь с управлением зависимостями, Spring AI предоставляет BOM (спецификация материалов), чтобы гарантировать, что в проекте используется согласованная версия Spring AI. Обратитесь к разделу xref:getting-started.adoc#dependency-management[Управление зависимостями], чтобы добавить BOM Spring AI в вашу систему сборки.

## Автоконфигурация

[NOTE]
====
В автоконфигурации Spring AI произошли значительные изменения в названиях артефактов модулей-стартеров. Пожалуйста, обратитесь к https://docs.spring.io/spring-ai/reference/upgrade-notes.html[заметкам об обновлении] для получения дополнительной информации.
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

> **Совет:** Обратитесь к разделу xref:getting-started.adoc#dependency-management[Управление зависимостями], чтобы добавить BOM Spring AI в ваш файл сборки.

### Свойства встраиванияThe prefix `spring.ai.vertex.ai.embedding` используется как префикс свойств, который позволяет вам подключаться к VertexAI Embedding API.

| Свойство | Описание | По умолчанию |
| --- | --- | --- |

| spring.ai.vertex.ai.embedding.project-id | Идентификатор проекта Google Cloud Platform | - |
| --- | --- | --- |
| spring.ai.vertex.ai.embedding.location | Регион | - |
| spring.ai.vertex.ai.embedding.apiEndpoint | Конечная точка Vertex AI Embedding API. | - |


[NOTE]
====
Включение и отключение автонастроек встраивания теперь настраивается через свойства верхнего уровня с префиксом `spring.ai.model.embedding`.

Чтобы включить, используйте spring.ai.model.embedding.multimodal=vertexai (по умолчанию включено)

Чтобы отключить, используйте spring.ai.model.embedding.multimodal=none (или любое значение, которое не соответствует vertexai)

Это изменение сделано для того, чтобы позволить конфигурацию нескольких моделей.
====

Префикс `spring.ai.vertex.ai.embedding.multimodal` — это префикс свойств, который позволяет вам настраивать реализацию модели встраивания для VertexAI Multimodal Embedding.

| Свойство | Описание | По умолчанию |
| --- | --- | --- |

| spring.ai.vertex.ai.embedding.multimodal.enabled (Удалено и больше не актуально) | Включить модель Vertex AI Embedding API. | true |
| --- | --- | --- |
| spring.ai.model.embedding.multimodal=vertexai | Включить модель Vertex AI Embedding API. | vertexai |
| spring.ai.vertex.ai.embedding.multimodal.options.model | Вы можете получить мультимодальные встраивания, используя следующую модель: | multimodalembedding@001 |
| spring.ai.vertex.ai.embedding.multimodal.options.dimensions | Укажите встраивания с меньшей размерностью. По умолчанию запрос встраивания возвращает вектор с плавающей запятой размером 1408 для типа данных. Вы также можете указать встраивания с меньшей размерностью (128, 256 или 512 векторов с плавающей запятой) для текстовых и изображенческих данных. | 1408 |
| spring.ai.vertex.ai.embedding.multimodal.options.video-start-offset-sec | Начальное смещение сегмента видео в секундах. Если не указано, оно рассчитывается как max(0, endOffsetSec - 120). | - |
| spring.ai.vertex.ai.embedding.multimodal.options.video-end-offset-sec | Конечное смещение сегмента видео в секундах. Если не указано, оно рассчитывается как min(video length, startOffSec + 120). Если указаны и startOffSec, и endOffSec, endOffsetSec корректируется до min(startOffsetSec+120, endOffsetSec). | - |
| spring.ai.vertex.ai.embedding.multimodal.options.video-interval-sec | Интервал видео, для которого будет сгенерировано встраивание. Минимальное значение для interval_sec — 4. Если интервал меньше 4, возвращается InvalidArgumentError. Ограничений на максимальное значение интервала нет. Однако, если интервал больше min(video length, 120s), это влияет на качество сгенерированных встраиваний. Значение по умолчанию: 16. | - |

## Ручная конфигурацияДокумент [VertexAiMultimodalEmbeddingModel](https://github.com/spring-projects/spring-ai/blob/main/models/spring-ai-vertex-ai-embedding/src/main/java/org/springframework/ai/vertexai/embedding/VertexAiMultimodalEmbeddingModel.java) реализует интерфейс `DocumentEmbeddingModel`.

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

Далее создайте `VertexAiMultimodalEmbeddingModel` и используйте его для генерации эмбеддингов:

```java
VertexAiEmbeddingConnectionDetails connectionDetails = 
    VertexAiEmbeddingConnectionDetails.builder()
        .projectId(System.getenv(<VERTEX_AI_GEMINI_PROJECT_ID>))
        .location(System.getenv(<VERTEX_AI_GEMINI_LOCATION>))
        .build();

VertexAiMultimodalEmbeddingOptions options = VertexAiMultimodalEmbeddingOptions.builder()
    .model(VertexAiMultimodalEmbeddingOptions.DEFAULT_MODEL_NAME)
    .build();

var embeddingModel = new VertexAiMultimodalEmbeddingModel(this.connectionDetails, this.options);

Media imageMedial = new Media(MimeTypeUtils.IMAGE_PNG, new ClassPathResource("/test.image.png"));
Media videoMedial = new Media(new MimeType("video", "mp4"), new ClassPathResource("/test.video.mp4"));

var document = new Document("Объясните, что вы видите на этом видео?", List.of(this.imageMedial, this.videoMedial), Map.of());

EmbeddingResponse embeddingResponse = this.embeddingModel
	.embedForResponse(List.of("Hello World", "World is big and salvation is near"));

DocumentEmbeddingRequest embeddingRequest = new DocumentEmbeddingRequest(List.of(this.document),
        EmbeddingOptions.EMPTY);

EmbeddingResponse embeddingResponse = multiModelEmbeddingModel.call(this.embeddingRequest);

assertThat(embeddingResponse.getResults()).hasSize(3);
```
