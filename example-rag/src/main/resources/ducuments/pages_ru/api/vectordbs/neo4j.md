# Neo4j

В этом разделе описывается, как настроить `Neo4jVectorStore` для хранения векторных представлений документов и выполнения поиска по сходству.

[Neo4j](https://neo4j.com) — это открытая NoSQL графовая база данных.
Это полностью транзакционная база данных (ACID), которая хранит данные, структурированные в виде графов, состоящих из узлов, связанных отношениями.
Вдохновленная структурой реального мира, она обеспечивает высокую производительность запросов к сложным данным, оставаясь интуитивно понятной и простой для разработчика.

[Поиск векторов в Neo4j](https://neo4j.com/docs/cypher-manual/current/indexes-for-vector-search/) позволяет пользователям запрашивать векторные представления из больших наборов данных.
Векторное представление — это числовое представление объекта данных, такого как текст, изображение, аудио или документ.
Векторные представления могут храниться в свойствах _Node_ и могут запрашиваться с помощью функции `db.index.vector.queryNodes()`.
Эти индексы работают на основе Lucene, используя иерархическую навигационную маломировую графовую структуру (HNSW) для выполнения запроса k ближайших соседей (k-ANN) по векторным полям.

## Предварительные требования

- Запущенный экземпляр Neo4j (5.15+). Доступны следующие варианты:
** [Docker](https://hub.docker.com/_/neo4j) образ
** [Neo4j Desktop](https://neo4j.com/download/)
** [Neo4j Aura](https://neo4j.com/cloud/aura-free/)
** [Neo4j Server](https://neo4j.com/deployment-center/) экземпляр
- При необходимости, API-ключ для xref:api/embeddings.adoc#available-implementations[EmbeddingModel] для генерации векторных представлений, хранящихся в `Neo4jVectorStore`.

## Автоконфигурация[NOTE]
====
В Spring AI произошли значительные изменения в автонастройке и названиях артефактов стартовых модулей.
Пожалуйста, обратитесь к https://docs.spring.io/spring-ai/reference/upgrade-notes.html[заметкам по обновлению] для получения дополнительной информации.
====

Spring AI предоставляет автонастройку Spring Boot для хранилища векторов Neo4j.
Чтобы включить эту функциональность, добавьте следующую зависимость в файл `pom.xml` вашего проекта Maven:

```xml
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-starter-vector-store-neo4j</artifactId>
</dependency>
```

или в файл сборки Gradle `build.gradle`.

```groovy
dependencies {
    implementation 'org.springframework.ai:spring-ai-starter-vector-store-neo4j'
}
```

> **Совет:** Обратитесь к разделу xref:getting-started.adoc#dependency-management[Управление зависимостями], чтобы добавить BOM Spring AI в ваш файл сборки.

Пожалуйста, ознакомьтесь со списком xref:#neo4jvector-properties[Свойства конфигурации] для хранилища векторов, чтобы узнать о значениях по умолчанию и параметрах конфигурации.

> **Совет:** Обратитесь к разделу xref:getting-started.adoc#artifact-repositories[Артефактные репозитории], чтобы добавить Maven Central и/или репозитории снимков в ваш файл сборки.

Реализация хранилища векторов может инициализировать необходимую схему для вас, но вы должны согласиться, указав булевое значение `initializeSchema` в соответствующем конструкторе или установив `...initialize-schema=true` в файле `application.properties`.

> **Примечание:** это является изменением, которое нарушает совместимость! В предыдущих версиях Spring AI эта инициализация схемы происходила по умолчанию.

Кроме того, вам потребуется настроенный бин `EmbeddingModel`. Обратитесь к разделу xref:api/embeddings.adoc#available-implementations[EmbeddingModel] для получения дополнительной информации.

Теперь вы можете автоматически подключить `Neo4jVectorStore` как хранилище векторов в вашем приложении.

```java
@Autowired VectorStore vectorStore;

// ...

List<Document> documents = List.of(
    new Document("Spring AI rocks!! Spring AI rocks!! Spring AI rocks!! Spring AI rocks!! Spring AI rocks!!", Map.of("meta1", "meta1")),
    new Document("The World is Big and Salvation Lurks Around the Corner"),
    new Document("You walk forward facing the past and you turn back toward the future.", Map.of("meta2", "meta2")));

// Добавьте документы в Neo4j
vectorStore.add(documents);

// Получите документы, похожие на запрос
List<Document> results = vectorStore.similaritySearch(SearchRequest.builder().query("Spring").topK(5).build());
```

[[neo4jvector-properties]]
### Свойства конфигурацииЧтобы подключиться к Neo4j и использовать `Neo4jVectorStore`, вам необходимо предоставить данные доступа к вашему экземпляру. Простую конфигурацию можно задать через `application.yml` Spring Boot:

```yaml
spring:
  neo4j:
    uri: <neo4j instance URI>
    authentication:
      username: <neo4j username>
      password: <neo4j password>
  ai:
    vectorstore:
      neo4j:
        initialize-schema: true
        database-name: neo4j
        index-name: custom-index
        embedding-dimension: 1536
        distance-type: cosine
```

Свойства Spring Boot, начинающиеся с `spring.neo4j.*`, используются для настройки клиента Neo4j:

[cols="2,5,1",stripes=even]
| Свойство | Описание | Значение по умолчанию |
| --- | --- | --- |
| `spring.neo4j.uri` | URI для подключения к экземпляру Neo4j | `neo4j://localhost:7687` |
| `spring.neo4j.authentication.username` | Имя пользователя для аутентификации в Neo4j | `neo4j` |
| `spring.neo4j.authentication.password` | Пароль для аутентификации в Neo4j | - |

Свойства, начинающиеся с `spring.ai.vectorstore.neo4j.*`, используются для настройки `Neo4jVectorStore`:

[cols="2,5,1",stripes=even]
| Свойство | Описание | Значение по умолчанию |
| --- | --- | --- |
| `spring.ai.vectorstore.neo4j.initialize-schema` | Нужно ли инициализировать необходимую схему | `false` |
| `spring.ai.vectorstore.neo4j.database-name` | Имя базы данных Neo4j для использования | `neo4j` |
| `spring.ai.vectorstore.neo4j.index-name` | Имя индекса для хранения векторов | `spring-ai-document-index` |
| `spring.ai.vectorstore.neo4j.embedding-dimension` | Количество измерений во векторе | `1536` |
| `spring.ai.vectorstore.neo4j.distance-type` | Функция расстояния для использования | `cosine` |
| `spring.ai.vectorstore.neo4j.label` | Метка, используемая для узлов документов | `Document` |
| `spring.ai.vectorstore.neo4j.embedding-property` | Имя свойства, используемое для хранения встраиваний | `embedding` |

Доступны следующие функции расстояния:

- `cosine` - По умолчанию, подходит для большинства случаев. Измеряет косинусное сходство между векторами.
- `euclidean` - Евклидово расстояние между векторами. Более низкие значения указывают на более высокое сходство.

## Ручная конфигурацияВместо использования автонастройки Spring Boot вы можете вручную настроить хранилище векторов Neo4j. Для этого вам нужно добавить `spring-ai-neo4j-store` в ваш проект:

```xml
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-neo4j-store</artifactId>
</dependency>
```

или в файл сборки Gradle `build.gradle`.

```groovy
dependencies {
    implementation 'org.springframework.ai:spring-ai-neo4j-store'
}
```

> **Совет:** Обратитесь к разделу xref:getting-started.adoc#dependency-management[Управление зависимостями], чтобы добавить Spring AI BOM в ваш файл сборки.

Создайте бин `Driver` для Neo4j.
Читайте [Документацию Neo4j](https://neo4j.com/docs/java-manual/current/client-applications/) для получения более подробной информации о настройке пользовательского драйвера.

```java
@Bean
public Driver driver() {
    return GraphDatabase.driver("neo4j://<host>:<bolt-port>",
            AuthTokens.basic("<username>", "<password>"));
}
```

Затем создайте бин `Neo4jVectorStore`, используя паттерн строителя:

```java
@Bean
public VectorStore vectorStore(Driver driver, EmbeddingModel embeddingModel) {
    return Neo4jVectorStore.builder(driver, embeddingModel)
        .databaseName("neo4j")                // Необязательно: по умолчанию "neo4j"
        .distanceType(Neo4jDistanceType.COSINE) // Необязательно: по умолчанию COSINE
        .embeddingDimension(1536)                      // Необязательно: по умолчанию 1536
        .label("Document")                     // Необязательно: по умолчанию "Document"
        .embeddingProperty("embedding")        // Необязательно: по умолчанию "embedding"
        .indexName("custom-index")             // Необязательно: по умолчанию "spring-ai-document-index"
        .initializeSchema(true)                // Необязательно: по умолчанию false
        .batchingStrategy(new TokenCountBatchingStrategy()) // Необязательно: по умолчанию TokenCountBatchingStrategy
        .build();
}

// Это может быть любая реализация EmbeddingModel
@Bean
public EmbeddingModel embeddingModel() {
    return new OpenAiEmbeddingModel(new OpenAiApi(System.getenv("OPENAI_API_KEY")));
}
```

## Фильтрация метаданных

Вы также можете использовать универсальные, переносимые xref:api/vectordbs.adoc#metadata-filters[фильтры метаданных] с хранилищем Neo4j.

Например, вы можете использовать либо текстовый язык выражений:

```java
vectorStore.similaritySearch(
    SearchRequest.builder()
        .query("The World")
        .topK(TOP_K)
        .similarityThreshold(SIMILARITY_THRESHOLD)
        .filterExpression("author in ['john', 'jill'] && 'article_type' == 'blog'").build());
```

либо программно, используя DSL `Filter.Expression`:

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

> **Примечание:** Эти (переносимые) выражения фильтров автоматически преобразуются в собственные выражения фильтров Neo4j `WHERE` [фильтрации результатов запросов](https://neo4j.com/developer/cypher/filtering-query-results/).

Например, это переносимое выражение фильтра:

```sql
author in ['john', 'jill'] && 'article_type' == 'blog'
```

преобразуется в собственный формат фильтра Neo4j:

```text
node.`metadata.author` IN ["john","jill"] AND node.`metadata.'article_type'` = "blog"
```

## Доступ к нативному клиентуРеализация Neo4j Vector Store предоставляет доступ к базовому нативному клиенту Neo4j (`Driver`) через метод `getNativeClient()`:

```java
Neo4jVectorStore vectorStore = context.getBean(Neo4jVectorStore.class);
Optional<Driver> nativeClient = vectorStore.getNativeClient();

if (nativeClient.isPresent()) {
    Driver driver = nativeClient.get();
    // Используйте нативный клиент для операций, специфичных для Neo4j
}
```

Нативный клиент предоставляет доступ к функциям и операциям, специфичным для Neo4j, которые могут не быть доступны через интерфейс `VectorStore`.
