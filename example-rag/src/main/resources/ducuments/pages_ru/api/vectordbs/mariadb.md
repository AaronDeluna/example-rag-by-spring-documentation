# MariaDB Vector Store

В этом разделе описывается, как настроить `MariaDBVectorStore` для хранения векторных представлений документов и выполнения поиска по сходству.

[MariaDB Vector](https://mariadb.org/projects/mariadb-vector/) является частью MariaDB 11.7 и позволяет хранить и выполнять поиск по векторным представлениям, сгенерированным с помощью машинного обучения. Он предоставляет эффективные возможности поиска по сходству векторов с использованием векторных индексов, поддерживая как косинусное сходство, так и метрики евклидова расстояния.

## Предварительные требования

- Запущенный экземпляр MariaDB (11.7+). Доступны следующие варианты:
** [Docker](https://hub.docker.com/_/mariadb) образ
** [MariaDB Server](https://mariadb.org/download/)
** [MariaDB SkySQL](https://mariadb.com/products/skysql/)
- При необходимости, API-ключ для xref:api/embeddings.adoc#available-implementations[EmbeddingModel], чтобы генерировать векторные представления, хранящиеся в `MariaDBVectorStore`.

## Автонастройка

[NOTE]
====
В автонастройке Spring AI произошли значительные изменения, касающиеся имен артефактов стартовых модулей. Пожалуйста, обратитесь к https://docs.spring.io/spring-ai/reference/upgrade-notes.html[заметкам об обновлении] для получения дополнительной информации.
====

Spring AI предоставляет автонастройку Spring Boot для MariaDB Vector Store. Чтобы включить ее, добавьте следующую зависимость в файл `pom.xml` вашего проекта Maven:

```xml
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-starter-vector-store-mariadb</artifactId>
</dependency>
```

или в файл сборки Gradle `build.gradle`.

```groovy
dependencies {
    implementation 'org.springframework.ai:spring-ai-starter-vector-store-mariadb'
}
```

> **Совет:** Обратитесь к разделу xref:getting-started.adoc#dependency-management[Управление зависимостями], чтобы добавить BOM Spring AI в ваш файл сборки.

Реализация векторного хранилища может инициализировать необходимую схему для вас, но вы должны согласиться, указав булевый параметр `initializeSchema` в соответствующем конструкторе или установив `...initialize-schema=true` в файле `application.properties`.

> **Примечание:** Это изменение является разрушающим! В более ранних версиях Spring AI инициализация схемы происходила по умолчанию.

Кроме того, вам потребуется настроенный бин `EmbeddingModel`. Обратитесь к разделу xref:api/embeddings.adoc#available-implementations[EmbeddingModel] для получения дополнительной информации.

Например, чтобы использовать xref:api/embeddings/openai-embeddings.adoc[OpenAI EmbeddingModel], добавьте следующую зависимость:

```xml
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-starter-model-openai</artifactId>
</dependency>
```

> **Совет:** Обратитесь к разделу xref:getting-started.adoc#artifact-repositories[Артефактные репозитории], чтобы добавить Maven Central и/или Snapshot репозитории в ваш файл сборки.

Теперь вы можете автоматически подключить `MariaDBVectorStore` в вашем приложении:

```java
@Autowired VectorStore vectorStore;

// ...

List<Document> documents = List.of(
    new Document("Spring AI rocks!! Spring AI rocks!! Spring AI rocks!! Spring AI rocks!! Spring AI rocks!!", Map.of("meta1", "meta1")),
    new Document("The World is Big and Salvation Lurks Around the Corner"),
    new Document("You walk forward facing the past and you turn back toward the future.", Map.of("meta2", "meta2")));

// Добавьте документы в MariaDB
vectorStore.add(documents);

// Извлеките документы, похожие на запрос
List<Document> results = vectorStore.similaritySearch(SearchRequest.builder().query("Spring").topK(5).build());
```

### Свойства конфигурацииЧтобы подключиться к MariaDB и использовать `MariaDBVectorStore`, вам необходимо предоставить данные доступа к вашему экземпляру. Простую конфигурацию можно задать через `application.yml` Spring Boot:

```yaml
spring:
  datasource:
    url: jdbc:mariadb://localhost/db
    username: myUser
    password: myPassword
  ai:
    vectorstore:
      mariadb:
        initialize-schema: true
        distance-type: COSINE
        dimensions: 1536
```

> **Совет:** Если вы запускаете MariaDB Vector как сервис разработки Spring Boot через [Docker Compose](https://docs.spring.io/spring-boot/reference/features/dev-services.html#features.dev-services.docker-compose) или [Testcontainers](https://docs.spring.io/spring-boot/reference/features/dev-services.html#features.dev-services.testcontainers), вам не нужно настраивать URL, имя пользователя и пароль, так как они автоматически настраиваются Spring Boot.

Свойства, начинающиеся с `spring.ai.vectorstore.mariadb.*`, используются для настройки `MariaDBVectorStore`:

| Свойство | Описание | Значение по умолчанию |
| --- | --- | --- |
| `spring.ai.vectorstore.mariadb.initialize-schema` | Нужно ли инициализировать требуемую схему | `false` |
| `spring.ai.vectorstore.mariadb.distance-type` | Тип расстояния для поиска. Используйте `COSINE` (по умолчанию) или `EUCLIDEAN`. Если векторы нормализованы до длины 1, вы можете использовать `EUCLIDEAN` для наилучшей производительности. | `COSINE` |
| `spring.ai.vectorstore.mariadb.dimensions` | Размерность встраиваний. Если не указано явно, будет получена размерность из предоставленной `EmbeddingModel`. | `1536` |
| `spring.ai.vectorstore.mariadb.remove-existing-vector-store-table` | Удаляет существующую таблицу векторного хранилища при запуске. | `false` |
| `spring.ai.vectorstore.mariadb.schema-name` | Имя схемы векторного хранилища | `null` |
| `spring.ai.vectorstore.mariadb.table-name` | Имя таблицы векторного хранилища | `vector_store` |
| `spring.ai.vectorstore.mariadb.schema-validation` | Включает проверку схемы и имени таблицы, чтобы убедиться, что они являются действительными и существующими объектами. | `false` |

> **Совет:** Если вы настраиваете пользовательское имя схемы и/или таблицы, рассмотрите возможность включения проверки схемы, установив `spring.ai.vectorstore.mariadb.schema-validation=true`. Это гарантирует правильность имен и снижает риск атак SQL-инъекций.

## Ручная конфигурация```markdown
Вместо использования автонастройки Spring Boot вы можете вручную настроить хранилище векторов MariaDB. Для этого вам нужно добавить следующие зависимости в ваш проект:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-jdbc</artifactId>
</dependency>

<dependency>
    <groupId>org.mariadb.jdbc</groupId>
    <artifactId>mariadb-java-client</artifactId>
    <scope>runtime</scope>
</dependency>

<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-mariadb-store</artifactId>
</dependency>
```

> **Совет:** Обратитесь к разделу xref:getting-started.adoc#dependency-management[Управление зависимостями], чтобы добавить BOM Spring AI в ваш файл сборки.

Затем создайте бин `MariaDBVectorStore`, используя паттерн строителя:

```java
@Bean
public VectorStore vectorStore(JdbcTemplate jdbcTemplate, EmbeddingModel embeddingModel) {
    return MariaDBVectorStore.builder(jdbcTemplate, embeddingModel)
        .dimensions(1536)                      // Необязательно: по умолчанию 1536
        .distanceType(MariaDBDistanceType.COSINE) // Необязательно: по умолчанию COSINE
        .schemaName("mydb")                    // Необязательно: по умолчанию null
        .vectorTableName("custom_vectors")     // Необязательно: по умолчанию "vector_store"
        .contentFieldName("text")             // Необязательно: по умолчанию "content"
        .embeddingFieldName("embedding")      // Необязательно: по умолчанию "embedding"
        .idFieldName("doc_id")                // Необязательно: по умолчанию "id"
        .metadataFieldName("meta")           // Необязательно: по умолчанию "metadata"
        .initializeSchema(true)               // Необязательно: по умолчанию false
        .schemaValidation(true)              // Необязательно: по умолчанию false
        .removeExistingVectorStoreTable(false) // Необязательно: по умолчанию false
        .maxDocumentBatchSize(10000)         // Необязательно: по умолчанию 10000
        .build();
}

// Это может быть любая реализация EmbeddingModel
@Bean
public EmbeddingModel embeddingModel() {
    return new OpenAiEmbeddingModel(new OpenAiApi(System.getenv("OPENAI_API_KEY")));
}
```

## Фильтрация метаданных

Вы можете использовать универсальные, переносимые xref:api/vectordbs.adoc#metadata-filters[фильтры метаданных] с хранилищем векторов MariaDB.

Например, вы можете использовать либо текстовый язык выражений:

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

> **Примечание:** Эти выражения фильтрации автоматически преобразуются в эквивалентные выражения пути JSON MariaDB.

## Оценки схожести

Хранилище векторов MariaDB автоматически вычисляет оценки схожести для документов, возвращаемых из запросов на схожесть. Эти оценки предоставляют нормализованную меру того, насколько каждый документ соответствует вашему поисковому запросу.

### Расчет оценки

Оценки схожести рассчитываются по формуле `score = 1.0 - distance`, где:

- Оценка: значение от `0.0` до `1.0`, где `1.0` указывает на идеальную схожесть, а `0.0` указывает на отсутствие схожести
- Расстояние: сырое значение расстояния, рассчитанное с использованием настроенного типа расстояния (`COSINE` или `EUCLIDEAN`)

Это означает, что документы с меньшими расстояниями (более схожие) будут иметь более высокие оценки, что делает результаты более интуитивно понятными для интерпретации.

### Доступ к оценкам
```Вы можете получить оценку схожести для каждого документа с помощью метода `getScore()`:

```java
List<Document> results = vectorStore.similaritySearch(
    SearchRequest.builder()
        .query("Spring AI")
        .topK(5)
        .build());

for (Document doc : results) {
    double score = doc.getScore();  // Значение от 0.0 до 1.0
    System.out.println("Документ: " + doc.getText());
    System.out.println("Оценка схожести: " + score);
}
```

### Упорядочение результатов поиска

Результаты поиска автоматически упорядочиваются по оценке схожести в порядке убывания (сначала наивысшая оценка). Это гарантирует, что наиболее релевантные документы будут отображаться в верхней части ваших результатов.

### Метаданные расстояния

В дополнение к оценке схожести, сырое значение расстояния все еще доступно в метаданных документа:

```java
for (Document doc : results) {
    double score = doc.getScore();
    float distance = (Float) doc.getMetadata().get("distance");

    System.out.println("Оценка: " + score + ", Расстояние: " + distance);
}
```

### Порог схожести

При использовании порогов схожести в ваших запросах поиска укажите порог в виде значения оценки (`0.0` до `1.0`), а не расстояния:

```java
List<Document> results = vectorStore.similaritySearch(
    SearchRequest.builder()
        .query("Spring AI")
        .topK(10)
        .similarityThreshold(0.8)  // Возвращать только документы с оценкой >= 0.8
        .build());
```

Это делает значения порога последовательными и интуитивно понятными - более высокие значения означают более строгие поиски, которые возвращают только высокосхожие документы.

## Доступ к нативному клиенту

Реализация MariaDB Vector Store предоставляет доступ к базовому нативному JDBC клиенту (`JdbcTemplate`) через метод `getNativeClient()`:

```java
MariaDBVectorStore vectorStore = context.getBean(MariaDBVectorStore.class);
Optional<JdbcTemplate> nativeClient = vectorStore.getNativeClient();

if (nativeClient.isPresent()) {
    JdbcTemplate jdbc = nativeClient.get();
    // Используйте нативный клиент для операций, специфичных для MariaDB
}
```

Нативный клиент предоставляет доступ к функциям и операциям, специфичным для MariaDB, которые могут не быть доступны через интерфейс `VectorStore`.
