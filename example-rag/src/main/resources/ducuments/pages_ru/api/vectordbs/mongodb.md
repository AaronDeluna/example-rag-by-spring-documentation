# MongoDB Atlas

Этот раздел проведет вас через настройку MongoDB Atlas в качестве векторного хранилища для использования с Spring AI.

## Что такое MongoDB Atlas?

https://www.mongodb.com/products/platform/atlas-database[MongoDB Atlas] — это полностью управляемая облачная база данных от MongoDB, доступная в AWS, Azure и GCP. Atlas поддерживает нативный векторный поиск и полнотекстовый поиск по вашим данным документов MongoDB.

https://www.mongodb.com/products/platform/atlas-vector-search[MongoDB Atlas Vector Search] позволяет вам хранить ваши встраивания в документах MongoDB, создавать индексы для векторного поиска и выполнять KNN-поиски с использованием алгоритма приближенного поиска ближайших соседей (Hierarchical Navigable Small Worlds). Вы можете использовать оператор агрегации `$vectorSearch` на этапе агрегации MongoDB для выполнения поиска по вашим векторным встраиваниям.

## Предварительные требования

- Кластер Atlas, работающий на версии MongoDB 6.0.11, 7.0.2 или более поздней. Чтобы начать работу с MongoDB Atlas, вы можете следовать инструкциям https://www.mongodb.com/docs/atlas/getting-started/[здесь]. Убедитесь, что ваш IP-адрес включен в https://www.mongodb.com/docs/atlas/security/ip-access-list/#std-label-access-list[список доступа] вашего проекта Atlas.
- Запущенный экземпляр MongoDB Atlas с включенным векторным поиском
- Коллекция с настроенным индексом для векторного поиска
- Схема коллекции с полями id (строка), content (строка), metadata (документ) и embedding (вектор)
- Правильные разрешения доступа для операций с индексами и коллекциями

## Автоконфигурация[NOTE]
====
В Spring AI произошли значительные изменения в автонастройке и названиях артефактов стартовых модулей.
Пожалуйста, обратитесь к https://docs.spring.io/spring-ai/reference/upgrade-notes.html[заметкам об обновлении] для получения дополнительной информации.
====

Spring AI предоставляет автонастройку Spring Boot для MongoDB Atlas Vector Store.
Чтобы включить эту функциональность, добавьте следующую зависимость в файл `pom.xml` вашего проекта:

```xml
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-starter-vector-store-mongodb-atlas</artifactId>
</dependency>
```

или в файл сборки Gradle `build.gradle`:

```groovy
dependencies {
    implementation 'org.springframework.ai:spring-ai-starter-vector-store-mongodb-atlas'
}
```

> **Совет:** Обратитесь к разделу xref:getting-started.adoc#dependency-management[Управление зависимостями], чтобы добавить BOM Spring AI в ваш файл сборки.

> **Совет:** Обратитесь к разделу xref:getting-started.adoc#artifact-repositories[Артефактные репозитории], чтобы добавить Maven Central и/или Snapshot репозитории в ваш файл сборки.

Реализация векторного хранилища может инициализировать необходимую схему для вас, но вы должны согласиться, установив `spring.ai.vectorstore.mongodb.initialize-schema=true` в файле `application.properties`.
В качестве альтернативы вы можете отказаться от инициализации и создать индекс вручную, используя интерфейс MongoDB Atlas, Atlas Administration API или Atlas CLI, что может быть полезно, если индекс требует сложного сопоставления или дополнительной конфигурации.

> **Примечание:** это является нарушением совместимости! В предыдущих версиях Spring AI эта инициализация схемы происходила по умолчанию.

Пожалуйста, ознакомьтесь со списком [параметров конфигурации](#mongodbvector-properties) для векторного хранилища, чтобы узнать о значениях по умолчанию и параметрах конфигурации.

Кроме того, вам потребуется настроенный бин `EmbeddingModel`. Обратитесь к разделу xref:api/embeddings.adoc#available-implementations[EmbeddingModel] для получения дополнительной информации.

Теперь вы можете автоматически подключить `MongoDBAtlasVectorStore` как векторное хранилище в вашем приложении:

```java
@Autowired VectorStore vectorStore;

// ...

List<Document> documents = List.of(
    new Document("Spring AI rocks!! Spring AI rocks!! Spring AI rocks!! Spring AI rocks!! Spring AI rocks!!", Map.of("meta1", "meta1")),
    new Document("The World is Big and Salvation Lurks Around the Corner"),
    new Document("You walk forward facing the past and you turn back toward the future.", Map.of("meta2", "meta2")));

// Добавьте документы в MongoDB Atlas
vectorStore.add(documents);

// Извлеките документы, похожие на запрос
List<Document> results = vectorStore.similaritySearch(SearchRequest.builder().query("Spring").topK(5).build());
```

[[mongodbvector-properties]]
### Параметры конфигурацииЧтобы подключиться к MongoDB Atlas и использовать `MongoDBAtlasVectorStore`, вам необходимо предоставить данные доступа к вашему экземпляру. Простую конфигурацию можно задать через `application.yml` Spring Boot:

```yaml
spring:
  data:
    mongodb:
      uri: <mongodb atlas connection string>
      database: <database name>
  ai:
    vectorstore:
      mongodb:
        initialize-schema: true
        collection-name: custom_vector_store
        index-name: custom_vector_index
        path-name: custom_embedding
        metadata-fields-to-filter: author,year
```

Свойства, начинающиеся с `spring.ai.vectorstore.mongodb.*`, используются для настройки `MongoDBAtlasVectorStore`:

[cols="2,5,1",stripes=even]
| Свойство | Описание | Значение по умолчанию |
| --- | --- | --- |
| `spring.ai.vectorstore.mongodb.initialize-schema` | Нужно ли инициализировать требуемую схему | `false` |
| `spring.ai.vectorstore.mongodb.collection-name` | Имя коллекции для хранения векторов | `vector_store` |
| `spring.ai.vectorstore.mongodb.index-name` | Имя индекса поиска векторов | `vector_index` |
| `spring.ai.vectorstore.mongodb.path-name` | Путь, по которому хранятся векторы | `embedding` |
| `spring.ai.vectorstore.mongodb.metadata-fields-to-filter` | Список полей метаданных, разделенных запятыми, которые можно использовать для фильтрации | пустой список |

## Ручная конфигурация

Вместо использования автонастройки Spring Boot вы можете вручную настроить векторное хранилище MongoDB Atlas. Для этого вам нужно добавить `spring-ai-mongodb-atlas-store` в ваш проект:

```xml
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-mongodb-atlas-store</artifactId>
</dependency>
```

или в файл сборки Gradle `build.gradle`:

```groovy
dependencies {
    implementation 'org.springframework.ai:spring-ai-mongodb-atlas-store'
}
```

Создайте бин `MongoTemplate`:

```java
@Bean
public MongoTemplate mongoTemplate() {
    return new MongoTemplate(MongoClients.create("<mongodb atlas connection string>"), "<database name>");
}
```

Затем создайте бин `MongoDBAtlasVectorStore`, используя паттерн строителя:

```java
@Bean
public VectorStore vectorStore(MongoTemplate mongoTemplate, EmbeddingModel embeddingModel) {
    return MongoDBAtlasVectorStore.builder(mongoTemplate, embeddingModel)
        .collectionName("custom_vector_store")           // Необязательно: по умолчанию "vector_store"
        .vectorIndexName("custom_vector_index")          // Необязательно: по умолчанию "vector_index"
        .pathName("custom_embedding")                    // Необязательно: по умолчанию "embedding"
        .numCandidates(500)                             // Необязательно: по умолчанию 200
        .metadataFieldsToFilter(List.of("author", "year")) // Необязательно: по умолчанию пустой список
        .initializeSchema(true)                         // Необязательно: по умолчанию false
        .batchingStrategy(new TokenCountBatchingStrategy()) // Необязательно: по умолчанию TokenCountBatchingStrategy
        .build();
}

// Это может быть любая реализация EmbeddingModel
@Bean
public EmbeddingModel embeddingModel() {
    return new OpenAiEmbeddingModel(new OpenAiApi(System.getenv("OPENAI_API_KEY")));
}
```

## Фильтрация метаданныхВы также можете использовать универсальные, переносимые xref:api/vectordbs.adoc#metadata-filters[фильтры метаданных] с MongoDB Atlas.

Например, вы можете использовать либо язык выражений текста:

```java
vectorStore.similaritySearch(SearchRequest.builder()
        .query("The World")
        .topK(5)
        .similarityThreshold(0.7)
        .filterExpression("author in ['john', 'jill'] && article_type == 'blog'").build());
```

либо программно, используя `Filter.Expression` DSL:

```java
FilterExpressionBuilder b = new FilterExpressionBuilder();

vectorStore.similaritySearch(SearchRequest.builder()
        .query("The World")
        .topK(5)
        .similarityThreshold(0.7)
        .filterExpression(b.and(
                b.in("author", "john", "jill"),
                b.eq("article_type", "blog")).build()).build());
```

> **Примечание:** Эти (переносимые) выражения фильтров автоматически преобразуются в собственные выражения фильтров MongoDB Atlas.

Например, это переносимое выражение фильтра:

```sql
author in ['john', 'jill'] && article_type == 'blog'
```

преобразуется в собственный формат фильтра MongoDB Atlas:

```json
{
  "$and": [
    {
      "$or": [
        { "metadata.author": "john" },
        { "metadata.author": "jill" }
      ]
    },
    {
      "metadata.article_type": "blog"
    }
  ]
}
```

## Учебные пособия и примеры кода

Чтобы начать работу с Spring AI и MongoDB:

- Ознакомьтесь с https://www.mongodb.com/docs/atlas/atlas-vector-search/ai-integrations/spring-ai/#std-label-spring-ai[руководством по началу работы с интеграцией Spring AI].
- Для получения подробного примера кода, демонстрирующего Retrieval Augmented Generation (RAG) с Spring AI и MongoDB, обратитесь к этому https://www.mongodb.com/developer/languages/java/retrieval-augmented-generation-spring-ai/[подробному учебнику].

## Доступ к нативному клиенту

Реализация MongoDB Atlas Vector Store предоставляет доступ к базовому нативному клиенту MongoDB (`MongoClient`) через метод `getNativeClient()`:

```java
MongoDBAtlasVectorStore vectorStore = context.getBean(MongoDBAtlasVectorStore.class);
Optional<MongoClient> nativeClient = vectorStore.getNativeClient();

if (nativeClient.isPresent()) {
    MongoClient client = nativeClient.get();
    // Используйте нативный клиент для операций, специфичных для MongoDB
}
```

Нативный клиент предоставляет доступ к функциям и операциям, специфичным для MongoDB, которые могут не быть доступны через интерфейс `VectorStore`.
