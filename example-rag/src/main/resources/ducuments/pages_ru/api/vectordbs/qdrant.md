# Qdrant

В этом разделе описывается, как настроить `VectorStore` Qdrant для хранения векторных представлений документов и выполнения операций поиска по сходству.

[Qdrant](https://www.qdrant.tech/) — это открытый, высокопроизводительный движок/база данных для векторного поиска. Он использует алгоритм HNSW (Hierarchical Navigable Small World) для эффективных операций поиска k-NN и предоставляет расширенные возможности фильтрации для запросов на основе метаданных.

## Предварительные требования

- Экземпляр Qdrant: Настройте экземпляр Qdrant, следуя [инструкциям по установке](https://qdrant.tech/documentation/guides/installation/) в документации Qdrant.
- При необходимости, API-ключ для xref:api/embeddings.adoc#available-implementations[EmbeddingModel] для генерации векторных представлений, хранящихся в `QdrantVectorStore`.

> **Примечание:** Рекомендуется заранее [создать](https://qdrant.tech/documentation/concepts/collections/#create-a-collection) коллекцию Qdrant с соответствующими размерами и конфигурациями. Если коллекция не создана, `QdrantVectorStore` попытается создать одну, используя сходство `Cosine` и размерность настроенной `EmbeddingModel`.

## Автонастройка

[NOTE]
====
В Spring AI произошли значительные изменения в автонастройке, названия артефактов стартовых модулей.
Пожалуйста, обратитесь к https://docs.spring.io/spring-ai/reference/upgrade-notes.html[заметкам об обновлении] для получения дополнительной информации.
====

Spring AI предоставляет автонастройку Spring Boot для Qdrant Vector Store. Чтобы включить ее, добавьте следующую зависимость в файл `pom.xml` вашего проекта Maven:

```xml
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-starter-vector-store-qdrant</artifactId>
</dependency>
```

или в файл сборки Gradle `build.gradle`.

```groovy
dependencies {
    implementation 'org.springframework.ai:spring-ai-starter-vector-store-qdrant'
}
```

> **Совет:** Ознакомьтесь с разделом xref:getting-started.adoc#dependency-management[Управление зависимостями], чтобы добавить BOM Spring AI в ваш файл сборки.

Пожалуйста, ознакомьтесь со списком xref:#qdrant-vectorstore-properties[параметров конфигурации] для векторного хранилища, чтобы узнать о значениях по умолчанию и вариантах конфигурации.

> **Совет:** Ознакомьтесь с разделом xref:getting-started.adoc#artifact-repositories[Артефактные репозитории], чтобы добавить Maven Central и/или Snapshot репозитории в ваш файл сборки.

Реализация векторного хранилища может инициализировать необходимую схему для вас, но вы должны согласиться, указав булевое значение `initializeSchema` в строителе или установив `...initialize-schema=true` в файле `application.properties`.

> **Примечание:** это является изменением, которое нарушает совместимость! В предыдущих версиях Spring AI эта инициализация схемы происходила по умолчанию.

Кроме того, вам потребуется настроенный бин `EmbeddingModel`. Ознакомьтесь с разделом xref:api/embeddings.adoc#available-implementations[EmbeddingModel] для получения дополнительной информации.

Теперь вы можете автоматически подключить `QdrantVectorStore` как векторное хранилище в вашем приложении.

```java
@Autowired VectorStore vectorStore;

// ...

List<Document> documents = List.of(
    new Document("Spring AI rocks!! Spring AI rocks!! Spring AI rocks!! Spring AI rocks!! Spring AI rocks!!", Map.of("meta1", "meta1")),
    new Document("The World is Big and Salvation Lurks Around the Corner"),
    new Document("You walk forward facing the past and you turn back toward the future.", Map.of("meta2", "meta2")));

// Добавьте документы в Qdrant
vectorStore.add(documents);

// Получите документы, похожие на запрос
List<Document> results = vectorStore.similaritySearch(SearchRequest.builder().query("Spring").topK(5).build());
```

[[qdrant-vectorstore-properties]]
### Параметры конфигурацииЧтобы подключиться к Qdrant и использовать `QdrantVectorStore`, вам необходимо предоставить данные для доступа к вашему экземпляру. Простую конфигурацию можно задать через `application.yml` Spring Boot:

```yaml
spring:
  ai:
    vectorstore:
      qdrant:
        host: <хост qdrant>
        port: <grpc порт qdrant>
        api-key: <ключ api qdrant>
        collection-name: <имя коллекции>
        content-field-name: <имя поля контента>
        use-tls: false
        initialize-schema: true
```

Свойства, начинающиеся с `spring.ai.vectorstore.qdrant.*`, используются для настройки `QdrantVectorStore`:

[cols="2,5,1",stripes=even]
| Свойство | Описание | Значение по умолчанию |
| --- | --- | --- |
| `spring.ai.vectorstore.qdrant.host` | Хост сервера Qdrant | `localhost` |
| `spring.ai.vectorstore.qdrant.port` | gRPC порт сервера Qdrant | `6334` |
| `spring.ai.vectorstore.qdrant.api-key` | Ключ API для аутентификации | - |
| `spring.ai.vectorstore.qdrant.collection-name` | Имя коллекции для использования | `vector_store` |
| `spring.ai.vectorstore.qdrant.content-field-name` | Имя поля, хранящего содержимое документа в полезной нагрузке Qdrant. Полезно при интеграции с существующими коллекциями, использующими разные имена полей (например, "page_content", "text", "content"). | `doc_content` |
| `spring.ai.vectorstore.qdrant.use-tls` | Использовать ли TLS(HTTPS) | `false` |
| `spring.ai.vectorstore.qdrant.initialize-schema` | Инициализировать ли схему | `false` |

## Ручная конфигурация

Вместо использования автонастройки Spring Boot вы можете вручную настроить векторный магазин Qdrant. Для этого вам нужно добавить `spring-ai-qdrant-store` в ваш проект:

```xml
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-qdrant-store</artifactId>
</dependency>
```

или в файл сборки Gradle `build.gradle`.

```groovy
dependencies {
    implementation 'org.springframework.ai:spring-ai-qdrant-store'
}
```

> **Совет:** Обратитесь к разделу xref:getting-started.adoc#dependency-management[Управление зависимостями], чтобы добавить Spring AI BOM в ваш файл сборки.

Создайте бин клиента Qdrant:

```java
@Bean
public QdrantClient qdrantClient() {
    QdrantGrpcClient.Builder grpcClientBuilder =
        QdrantGrpcClient.newBuilder(
            "<QDRANT_HOSTNAME>",
            <QDRANT_GRPC_PORT>,
            <IS_TLS>);
    grpcClientBuilder.withApiKey("<QDRANT_API_KEY>");

    return new QdrantClient(grpcClientBuilder.build());
}
```

Затем создайте бин `QdrantVectorStore`, используя паттерн строителя:

```java
@Bean
public VectorStore vectorStore(QdrantClient qdrantClient, EmbeddingModel embeddingModel) {
    return QdrantVectorStore.builder(qdrantClient, embeddingModel)
        .collectionName("custom-collection")     // Необязательно: по умолчанию "vector_store"
        .contentFieldName("page_content")        // Необязательно: по умолчанию "doc_content"
        .initializeSchema(true)                  // Необязательно: по умолчанию false
        .batchingStrategy(new TokenCountBatchingStrategy()) // Необязательно: по умолчанию TokenCountBatchingStrategy
        .build();
}

// Это может быть любая реализация EmbeddingModel
@Bean
public EmbeddingModel embeddingModel() {
    return new OpenAiEmbeddingModel(new OpenAiApi(System.getenv("OPENAI_API_KEY")));
}
```

## Работа с существующими коллекциями

При интеграции Spring AI с уже существующими коллекциями Qdrant вам может потребоваться настроить имя поля контента, чтобы оно соответствовало уже используемой схеме.

По умолчанию `QdrantVectorStore` хранит содержимое документа в поле с именем `doc_content`. Однако существующие коллекции могут использовать разные соглашения об именах, такие как `page_content`, `text`, `content` или другие пользовательские имена.

### Использование пользовательских имен полей контентаВы можете настроить имя поля содержимого, чтобы оно соответствовало вашей существующей схеме коллекции:

**Через свойства:**
```yaml
spring:
  ai:
    vectorstore:
      qdrant:
        collection-name: my_existing_collection
        content-field-name: page_content  # Соответствует существующей схеме
```

**Программно:**
```java
@Bean
public VectorStore vectorStore(QdrantClient qdrantClient, EmbeddingModel embeddingModel) {
    return QdrantVectorStore.builder(qdrantClient, embeddingModel)
        .collectionName("my_existing_collection")
        .contentFieldName("text")  // Используйте существующее имя поля
        .initializeSchema(false)   // Не пересоздавать существующую схему
        .build();
}
```

## Фильтрация метаданных

Вы также можете использовать универсальные, переносимые xref:api/vectordbs.adoc#metadata-filters[фильтры метаданных] с хранилищем Qdrant.

Например, вы можете использовать либо язык выражений текста:

```java
vectorStore.similaritySearch(
    SearchRequest.builder()
        .query("The World")
        .topK(TOP_K)
        .similarityThreshold(SIMILARITY_THRESHOLD)
        .filterExpression("author in ['john', 'jill'] && article_type == 'blog'").build());
```

или программно, используя DSL `Filter.Expression`:

```java
FilterExpressionBuilder b = new FilterExpressionBuilder();

vectorStore.similaritySearch(SearchRequest.builder()
    .query("The World")
    .topK(TOP_K)
    .similarityThreshold(SIMILARITY_THRESHOLD)
    .filterExpression(b.and(
        b.in("author", "john", "jill"),
        b.eq("article_type", "blog")).build()).build());
```

> **Примечание:** Эти (переносимые) выражения фильтров автоматически преобразуются в собственные [выражения фильтров](https://qdrant.tech/documentation/concepts/filtering/) Qdrant.

## Доступ к нативному клиенту

Реализация Qdrant Vector Store предоставляет доступ к базовому нативному клиенту Qdrant (`QdrantClient`) через метод `getNativeClient()`:

```java
QdrantVectorStore vectorStore = context.getBean(QdrantVectorStore.class);
Optional<QdrantClient> nativeClient = vectorStore.getNativeClient();

if (nativeClient.isPresent()) {
    QdrantClient client = nativeClient.get();
    // Используйте нативный клиент для операций, специфичных для Qdrant
}
```

Нативный клиент предоставляет доступ к функциям и операциям, специфичным для Qdrant, которые могут не быть доступны через интерфейс `VectorStore`.
