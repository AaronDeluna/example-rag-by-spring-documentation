# S3 Vector Store

В этом разделе описывается, как настроить `S3VectorStore` для хранения векторных представлений документов и выполнения поиска по сходству.

[AWS S3 Vector Store](https://aws.amazon.com/s3/features/vectors/) — это безсерверное объектное хранилище, которое поддерживает хранение и запросы векторов в масштабах.

[API S3 Vector Store](https://docs.aws.amazon.com/AmazonS3/latest/userguide/s3-vectors.html) расширяет основные функции AWS S3 Bucket и позволяет использовать S3 в качестве векторной базы данных:

- Хранение векторов и связанных метаданных в хэшах или JSON-документах
- Извлечение векторов
- Выполнение поиска по вектору

## Предварительные требования

1. Корзина S3 Vector Store
- https://docs.aws.amazon.com/AmazonS3/latest/userguide/s3-vectors-buckets-create.html[Как создать S3 Vector Bucket]

2. Экземпляр `EmbeddingModel` для вычисления векторных представлений документов. Доступно несколько вариантов:
- При необходимости, API-ключ для xref:api/embeddings.adoc#available-implementations[EmbeddingModel] для генерации векторов, хранящихся в `S3VectorStore`.

## Автоконфигурация

Spring AI предоставляет автоконфигурацию Spring Boot для S3 Vector Store. Чтобы включить её, добавьте следующую зависимость в файл `pom.xml` вашего проекта Maven:

```xml
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-starter-vector-store-s3</artifactId>
</dependency>
```

или в файл сборки Gradle `build.gradle`.

```groovy
dependencies {
    implementation 'org.springframework.ai:spring-ai-starter-vector-store-s3'
}
```

> **Совет:** Ознакомьтесь с разделом xref:getting-started.adoc#dependency-management[Управление зависимостями], чтобы добавить BOM Spring AI в ваш файл сборки.

> **Совет:** Ознакомьтесь с разделом xref:getting-started.adoc#artifact-repositories[Артефактные репозитории], чтобы добавить Maven Central и/или Snapshot репозитории в ваш файл сборки.

Пожалуйста, ознакомьтесь со списком [параметров конфигурации](#s3-properties) для векторного хранилища, чтобы узнать о значениях по умолчанию и параметрах конфигурации.

Кроме того, вам потребуется настроенный бин `EmbeddingModel`. Для получения дополнительной информации обратитесь к разделу xref:api/embeddings.adoc#available-implementations[EmbeddingModel].

Теперь вы можете автоматически подключить `S3VectorStore` в качестве векторного хранилища в вашем приложении.

```java
@Autowired VectorStore vectorStore;

// ...

List <Document> documents = List.of(
    new Document("Spring AI rocks!! Spring AI rocks!! Spring AI rocks!! Spring AI rocks!! Spring AI rocks!!", Map.of("meta1", "meta1")),
    new Document("The World is Big and Salvation Lurks Around the Corner"),
    new Document("You walk forward facing the past and you turn back toward the future.", Map.of("meta2", "meta2")));

// Добавьте документы в корзину S3 Vector Store
vectorStore.add(documents);

// Извлечение документов, похожих на запрос
List<Document> results = this.vectorStore.similaritySearch(SearchRequest.builder().query("Spring").topK(5).build());
```

[[s3-properties]]
### Параметры конфигурации

Чтобы подключиться к AWS S3 Vector Store и использовать `S3VectorStore`, вам необходимо создать `Bean` `S3VectorsClient`, который должен быть обеспечен правильными учетными данными и регионом.

Свойства, начинающиеся с `spring.ai.vectorstore.s3.*`, используются для настройки `S3VectorStore`:

[cols="2,5,1",stripes=even]
| Свойство | Описание | Значение по умолчанию |
| --- | --- | --- |
| `spring.ai.vectorstore.s3.index-name` | Имя индекса для хранения векторов | `spring-ai-index` |
| `spring.ai.vectorstore.s3.vector-bucket-name` | Имя корзины, где находятся векторы | `my-vector-bucket-on-aws` |

## Фильтрация метаданныхВы также можете использовать универсальные, переносимые xref:api/vectordbs.adoc#metadata-filters[фильтры метаданных] с S3 Vector Store.

Например, вы можете использовать либо язык выражений текста:

```java
vectorStore.similaritySearch(SearchRequest.builder()
        .query("The World")
        .topK(TOP_K)
        .similarityThreshold(SIMILARITY_THRESHOLD)
        .filterExpression("country in ['UK', 'NL'] && year >= 2020").build());
```

либо программно, используя `Filter.Expression` DSL:

```java
FilterExpressionBuilder b = new FilterExpressionBuilder();

vectorStore.similaritySearch(SearchRequest.builder()
        .query("The World")
        .topK(TOP_K)
        .similarityThreshold(SIMILARITY_THRESHOLD)
        .filterExpression(b.and(
                b.in("country", "UK", "NL"),
                b.gte("year", 2020)).build()).build());
```

> **Примечание:** Эти (переносимые) выражения фильтров автоматически преобразуются в [объект фильтра документа AWS SDK Java V2](https://sdk.amazonaws.com/java/api/latest/software/amazon/awssdk/core/document/Document.html).

Например, это переносимое выражение фильтра:

```sql
country in ['UK', 'NL'] && year >= 2020
```

преобразуется в проприетарный формат фильтра S3 Vector Store:

```text
@country:{UK | NL} @year:[2020 inf]
```

## Ручная конфигурация

Вместо использования автонастройки Spring Boot вы можете вручную настроить S3 Vector Store. Для этого вам нужно добавить `spring-ai-s3-vector-store` в ваш проект:

```xml
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-s3-vector-store</artifactId>
</dependency>
```

или в ваш файл сборки Gradle `build.gradle`.

```groovy
dependencies {
    implementation 'org.springframework.ai:spring-ai-s3-vector-store'
}
```

Затем создайте бин `S3VectorStore`, используя паттерн строителя:

```java
@Bean
VectorStore s3VectorStore(S3VectorsClient s3VectorsClient, EmbeddingModel embeddingModel) {
    S3VectorStore.Builder builder = new S3VectorStore.Builder(s3VectorsClient, embeddingModel); // Обязательно
    builder.indexName(properties.getIndexName()) // Обязательно необходимо указать indexName
            .vectorBucketName(properties.getVectorBucketName()) // Обязательно необходимо указать vectorBucketName
            .filterExpressionConverter(yourConverter);  // Необязательно, если вы хотите переопределить стандартный filterConverter
    return builder.build();
}

// Это может быть любая реализация EmbeddingModel
@Bean
public EmbeddingModel embeddingModel() {
    return new OpenAiEmbeddingModel(new OpenAiApi(System.getenv("OPENAI_API_KEY")));
}
```

## Доступ к нативному клиенту

Реализация S3 Vector Store предоставляет доступ к базовому нативному клиенту S3VectorsClient:

```java
S3VectorStore vectorStore = context.getBean(S3VectorStore.class);
Optional<S3VectorsClient> nativeClient = vectorStore.getNativeClient();

if (nativeClient.isPresent()) {
    S3VectorsClient s3Client = nativeClient.get();
    // Используйте нативный клиент для операций, специфичных для S3-Vector-Store
}
```

Нативный клиент предоставляет доступ к функциям и операциям, специфичным для S3-Vector-Store, которые могут не быть доступны через интерфейс `VectorStore`.
