# Typesense

Этот раздел проведет вас через процесс настройки `TypesenseVectorStore` для хранения векторных представлений документов и выполнения поиска по сходству.

[Typesense](https://typesense.org) — это поисковая система с открытым исходным кодом, устойчивая к опечаткам, оптимизированная для мгновенных поисков с задержкой менее 50 мс, при этом обеспечивая интуитивно понятный опыт для разработчиков. Она предоставляет возможности векторного поиска, которые позволяют хранить и запрашивать многомерные векторы наряду с вашими обычными данными поиска.

## Предварительные требования

- Запущенный экземпляр Typesense. Доступны следующие варианты:
** [Typesense Cloud](https://typesense.org/docs/guide/install-typesense.html) (рекомендуется)
** Образ [Docker](https://hub.docker.com/r/typesense/typesense/) _typesense/typesense:latest_
- При необходимости, API-ключ для xref:api/embeddings.adoc#available-implementations[EmbeddingModel], чтобы генерировать векторные представления, хранящиеся в `TypesenseVectorStore`.

## Автонастройка

[ПРИМЕЧАНИЕ]
====
В Spring AI произошли значительные изменения в автонастройке, названиях артефактов стартовых модулей.
Пожалуйста, обратитесь к https://docs.spring.io/spring-ai/reference/upgrade-notes.html[заметкам об обновлении] для получения дополнительной информации.
====

Spring AI предоставляет автонастройку Spring Boot для Typesense Vector Store.
Чтобы включить ее, добавьте следующую зависимость в файл `pom.xml` вашего проекта Maven:

```xml
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-starter-vector-store-typesense</artifactId>
</dependency>
```

или в файл сборки Gradle `build.gradle`.

```groovy
dependencies {
    implementation 'org.springframework.ai:spring-ai-starter-vector-store-typesense'
}
```

> **Совет:** Обратитесь к разделу xref:getting-started.adoc#dependency-management[Управление зависимостями], чтобы добавить BOM Spring AI в ваш файл сборки.

Пожалуйста, ознакомьтесь со списком xref:#_configuration_properties[параметров конфигурации] для векторного хранилища, чтобы узнать о значениях по умолчанию и параметрах конфигурации.

> **Совет:** Обратитесь к разделу xref:getting-started.adoc#artifact-repositories[Артефактные репозитории], чтобы добавить Maven Central и/или Snapshot репозитории в ваш файл сборки.

Реализация векторного хранилища может инициализировать необходимую схему для вас, но вы должны согласиться, установив `...initialize-schema=true` в файле `application.properties`.

Кроме того, вам потребуется настроенный бин `EmbeddingModel`. Обратитесь к разделу xref:api/embeddings.adoc#available-implementations[EmbeddingModel] для получения дополнительной информации.

Теперь вы можете автоматически подключить `TypesenseVectorStore` как векторное хранилище в вашем приложении:

```java
@Autowired VectorStore vectorStore;

// ...

List<Document> documents = List.of(
    new Document("Spring AI rocks!! Spring AI rocks!! Spring AI rocks!! Spring AI rocks!! Spring AI rocks!!", Map.of("meta1", "meta1")),
    new Document("The World is Big and Salvation Lurks Around the Corner"),
    new Document("You walk forward facing the past and you turn back toward the future.", Map.of("meta2", "meta2")));

// Добавьте документы в Typesense
vectorStore.add(documents);

// Получите документы, похожие на запрос
List<Document> results = vectorStore.similaritySearch(SearchRequest.builder().query("Spring").topK(5).build());
```

### Параметры конфигурацииЧтобы подключиться к Typesense и использовать `TypesenseVectorStore`, вам необходимо предоставить данные для доступа к вашему экземпляру. Простую конфигурацию можно задать через `application.yml` Spring Boot:

```yaml
spring:
  ai:
    vectorstore:
      typesense:
        initialize-schema: true
        collection-name: vector_store
        embedding-dimension: 1536
        client:
          protocol: http
          host: localhost
          port: 8108
          api-key: xyz
```

Свойства, начинающиеся с `spring.ai.vectorstore.typesense.*`, используются для настройки `TypesenseVectorStore`:

| Свойство | Описание | Значение по умолчанию |
| --- | --- | --- |
| `spring.ai.vectorstore.typesense.initialize-schema` |  |  |
| Нужно ли инициализировать необходимую схему |  |  |
| `false` |  |  |
| `spring.ai.vectorstore.typesense.collection-name` |  |  |
| Имя коллекции для хранения векторов |  |  |
| `vector_store` |  |  |
| `spring.ai.vectorstore.typesense.embedding-dimension` |  |  |
| Количество измерений в векторе |  |  |
| `1536` |  |  |
| `spring.ai.vectorstore.typesense.client.protocol` |  |  |
| Протокол HTTP |  |  |
| `http` |  |  |
| `spring.ai.vectorstore.typesense.client.host` |  |  |
| Имя хоста |  |  |
| `localhost` |  |  |
| `spring.ai.vectorstore.typesense.client.port` |  |  |
| Порт |  |  |
| `8108` |  |  |
| `spring.ai.vectorstore.typesense.client.api-key` |  |  |
| API-ключ |  |  |
| `xyz` |  |  |

## Ручная конфигурация

Вместо использования автонастройки Spring Boot вы можете вручную настроить векторное хранилище Typesense. Для этого вам нужно добавить `spring-ai-typesense-store` в ваш проект:

```xml
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-typesense-store</artifactId>
</dependency>
```

или в файл сборки Gradle `build.gradle`.

```groovy
dependencies {
    implementation 'org.springframework.ai:spring-ai-typesense-store'
}
```

> **Совет:** Обратитесь к разделу xref:getting-started.adoc#dependency-management[Управление зависимостями], чтобы добавить Spring AI BOM в ваш файл сборки.

Создайте бин `Client` для Typesense:

```java
@Bean
public Client typesenseClient() {
    List<Node> nodes = new ArrayList<>();
    nodes.add(new Node("http", "localhost", "8108"));
    Configuration configuration = new Configuration(nodes, Duration.ofSeconds(5), "xyz");
    return new Client(configuration);
}
```

Затем создайте бин `TypesenseVectorStore`, используя паттерн строителя:

```java
@Bean
public VectorStore vectorStore(Client client, EmbeddingModel embeddingModel) {
    return TypesenseVectorStore.builder(client, embeddingModel)
        .collectionName("custom_vectors")     // Необязательно: по умолчанию "vector_store"
        .embeddingDimension(1536)            // Необязательно: по умолчанию 1536
        .initializeSchema(true)              // Необязательно: по умолчанию false
        .batchingStrategy(new TokenCountBatchingStrategy()) // Необязательно: по умолчанию TokenCountBatchingStrategy
        .build();
}

// Это может быть любая реализация EmbeddingModel
@Bean
public EmbeddingModel embeddingModel() {
    return new OpenAiEmbeddingModel(new OpenAiApi(System.getenv("OPENAI_API_KEY")));
}
```

## Фильтрация метаданныхВы также можете использовать универсальные переносимые xref:api/vectordbs.adoc#metadata-filters[фильтры метаданных] с хранилищем Typesense.

Например, вы можете использовать либо язык выражений текста:

```java
vectorStore.similaritySearch(
    SearchRequest.builder()
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

> **Примечание:** Эти (переносимые) выражения фильтров автоматически преобразуются в [Фильтры поиска Typesense](https://typesense.org/docs/0.24.0/api/search.html#filter-parameters).

Например, это переносимое выражение фильтра:

```sql
country in ['UK', 'NL'] && year >= 2020
```

преобразуется в собственный формат фильтра Typesense:

```text
country: ['UK', 'NL'] && year: >=2020
```

[NOTE]
====
Если вы не получаете документы в ожидаемом порядке или результаты поиска не соответствуют ожиданиям, проверьте модель встраивания, которую вы используете.

Модели встраивания могут значительно повлиять на результаты поиска (т.е. убедитесь, что если ваши данные на испанском, вы используете испанскую или многоязычную модель встраивания).
====

## Доступ к нативному клиенту

Реализация хранилища векторов Typesense предоставляет доступ к базовому нативному клиенту Typesense (`Client`) через метод `getNativeClient()`:

```java
TypesenseVectorStore vectorStore = context.getBean(TypesenseVectorStore.class);
Optional<Client> nativeClient = vectorStore.getNativeClient();

if (nativeClient.isPresent()) {
    Client client = nativeClient.get();
    // Используйте нативный клиент для операций, специфичных для Typesense
}
```

Нативный клиент предоставляет вам доступ к функциям и операциям, специфичным для Typesense, которые могут не быть доступны через интерфейс `VectorStore`.
