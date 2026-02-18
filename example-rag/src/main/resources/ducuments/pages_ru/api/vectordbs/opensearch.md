# OpenSearch

Этот раздел проведет вас через настройку `OpenSearchVectorStore` для хранения векторных представлений документов и выполнения поиска по сходству.

[OpenSearch](https://opensearch.org) — это движок поиска и аналитики с открытым исходным кодом, изначально созданный на основе Elasticsearch, распространяемый под лицензией Apache 2.0. Он улучшает разработку AI-приложений, упрощая интеграцию и управление активами, созданными с помощью AI. OpenSearch поддерживает векторные, лексические и гибридные возможности поиска, используя функции продвинутой векторной базы данных для обеспечения запросов с низкой задержкой и поиска по сходству, как подробно описано на [странице векторной базы данных](https://opensearch.org/platform/search/vector-database.html).

Функциональность [OpenSearch k-NN](https://opensearch.org/docs/latest/search-plugins/knn/index/) позволяет пользователям запрашивать векторные представления из больших наборов данных. Векторное представление — это числовое представление объекта данных, такого как текст, изображение, аудио или документ. Векторные представления могут храниться в индексе и запрашиваться с использованием различных функций сходства.

## Предварительные требования

- Запущенный экземпляр OpenSearch. Доступны следующие варианты:
** [Самостоятельно управляемый OpenSearch](https://opensearch.org/docs/latest/opensearch/install/index/)
** [Amazon OpenSearch Service](https://docs.aws.amazon.com/opensearch-service/)
- При необходимости, API-ключ для xref:api/embeddings.adoc#available-implementations[EmbeddingModel], чтобы генерировать векторные представления, хранящиеся в `OpenSearchVectorStore`.

## Автонастройка

[ПРИМЕЧАНИЕ]
====
В автонастройке Spring AI произошли значительные изменения, касающиеся имен артефактов стартовых модулей.
Пожалуйста, обратитесь к https://docs.spring.io/spring-ai/reference/upgrade-notes.html[заметкам об обновлении] для получения дополнительной информации.
====

Spring AI предоставляет автонастройку Spring Boot для OpenSearch Vector Store.
Чтобы включить ее, добавьте следующую зависимость в файл `pom.xml` вашего проекта Maven:

```xml
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-starter-vector-store-opensearch</artifactId>
</dependency>
```

или в файл сборки Gradle `build.gradle`:

```groovy
dependencies {
    implementation 'org.springframework.ai:spring-ai-starter-vector-store-opensearch'
}
```

> **Совет:** Для как самостоятельно управляемого, так и Amazon OpenSearch Service используйте одну и ту же зависимость.
Обратитесь к разделу xref:getting-started.adoc#dependency-management[Управление зависимостями], чтобы добавить BOM Spring AI в ваш файл сборки.

Пожалуйста, ознакомьтесь со списком xref:#_configuration_properties[параметров конфигурации] для векторного хранилища, чтобы узнать о значениях по умолчанию и параметрах конфигурации.

Кроме того, вам потребуется настроенный бин `EmbeddingModel`. Обратитесь к разделу xref:api/embeddings.adoc#available-implementations[EmbeddingModel] для получения дополнительной информации.

Теперь вы можете автоматически подключить `OpenSearchVectorStore` как векторное хранилище в вашем приложении:

```java
@Autowired VectorStore vectorStore;

// ...

List<Document> documents = List.of(
    new Document("Spring AI rocks!! Spring AI rocks!! Spring AI rocks!!", Map.of("meta1", "meta1")),
    new Document("The World is Big and Salvation Lurks Around the Corner"),
    new Document("You walk forward facing the past and you turn back toward the future.", Map.of("meta2", "meta2")));

// Добавьте документы в OpenSearch
vectorStore.add(documents);

// Получите документы, похожие на запрос
List<Document> results = vectorStore.similaritySearch(SearchRequest.builder().query("Spring").topK(5).build());
```

### Параметры конфигурацииЧтобы подключиться к OpenSearch и использовать `OpenSearchVectorStore`, вам необходимо предоставить данные доступа к вашему экземпляру. Простую конфигурацию можно задать через `application.yml` Spring Boot:

```yaml
spring:
  ai:
    vectorstore:
      opensearch:
        uris: <uris экземпляра opensearch>
        username: <имя пользователя opensearch>
        password: <пароль opensearch>
        index-name: spring-ai-document-index
        initialize-schema: true
        similarity-function: cosinesimil
        read-timeout: <время ожидания ответа>
        connect-timeout: <время ожидания до установления соединения>
        path-prefix: <пользовательский префикс пути>
        ssl-bundle: <имя SSL-пакета>
        aws:  # Только для Amazon OpenSearch Service
          host: <хост aws opensearch>
          service-name: <имя сервиса aws>
          access-key: <ключ доступа aws>
          secret-key: <секретный ключ aws>
          region: <регион aws>
```

Свойства, начинающиеся с `spring.ai.vectorstore.opensearch.*`, используются для настройки `OpenSearchVectorStore`:

[cols="2,5,1",stripes=even]
| Свойство | Описание | Значение по умолчанию |
| --- | --- | --- |
| `spring.ai.vectorstore.opensearch.uris` | URIs конечных точек кластера OpenSearch | - |
| `spring.ai.vectorstore.opensearch.username` | Имя пользователя для доступа к кластеру OpenSearch | - |
| `spring.ai.vectorstore.opensearch.password` | Пароль для указанного имени пользователя | - |
| `spring.ai.vectorstore.opensearch.index-name` | Имя индекса для хранения векторов | `spring-ai-document-index` |
| `spring.ai.vectorstore.opensearch.initialize-schema` | Нужно ли инициализировать необходимую схему | `false` |
| `spring.ai.vectorstore.opensearch.similarity-function` | Функция сходства для использования (cosinesimil, l1, l2, linf, innerproduct) | `cosinesimil` |
| `spring.ai.vectorstore.opensearch.use-approximate-knn` | Нужно ли использовать приближенный k-NN для более быстрых поисков. Если true, используется приближенный поиск на основе HNSW. Если false, используется точный брутфорс k-NN. См. ссылку: https://opensearch.org/docs/latest/search-plugins/knn/approximate-knn/[Приближенный k-NN] и ссылку: https://opensearch.org/docs/latest/search-plugins/knn/knn-score-script/[Точный k-NN] | `false` |
| `spring.ai.vectorstore.opensearch.dimensions` | Количество измерений для векторных вложений. Используется при создании индексного отображения для приближенного k-NN. Если не задано, используются размеры модели вложений. | `1536` |
| `spring.ai.vectorstore.opensearch.mapping-json` | Пользовательское JSON-отображение для индекса. Перезаписывает генерацию отображения по умолчанию. | - |
| `spring.ai.vectorstore.opensearch.read-timeout` | Время ожидания ответа от противоположной конечной точки. 0 - бесконечность. | - |
| `spring.ai.vectorstore.opensearch.connect-timeout` | Время ожидания до установления соединения. 0 - бесконечность. | - |
| `spring.ai.vectorstore.opensearch.path-prefix` | Префикс пути для конечных точек API OpenSearch. Полезно, когда OpenSearch находится за обратным прокси с не корневым путем. | - |
| `spring.ai.vectorstore.opensearch.ssl-bundle` | Имя SSL-пакета для использования в случае SSL-соединения | - |
| `spring.ai.vectorstore.opensearch.aws.host` | Имя хоста экземпляра OpenSearch | - |
| `spring.ai.vectorstore.opensearch.aws.service-name` | Имя сервиса AWS | - |
| `spring.ai.vectorstore.opensearch.aws.access-key` | Ключ доступа AWS | - |
| `spring.ai.vectorstore.opensearch.aws.secret-key` | Секретный ключ AWS | - |
| `spring.ai.vectorstore.opensearch.aws.region` | Регион AWS | - |

[NOTE]
====
Вы можете контролировать, включена ли автоматическая конфигурация OpenSearch для AWS, с помощью свойства `spring.ai.vectorstore.opensearch.aws.enabled`.

- Если это свойство установлено в `false`, активируется конфигурация OpenSearch, не относящаяся к AWS, даже если классы AWS SDK присутствуют в classpath. Это позволяет использовать самостоятельно управляемые или сторонние кластеры OpenSearch в средах, где присутствуют AWS SDK для других сервисов.
- Если классы AWS SDK отсутствуют, всегда используется конфигурация, не относящаяся к AWS.
- Если классы AWS SDK присутствуют, а свойство не установлено или установлено в `true`, по умолчанию используется конфигурация, специфичная для AWS.

Эта логика резервирования гарантирует, что пользователи имеют явный контроль над типом интеграции OpenSearch, предотвращая случайное включение логики, специфичной для AWS, когда это не требуется.
====

[NOTE]
====
Свойство `path-prefix` позволяет вам указать пользовательский префикс пути, когда OpenSearch работает за обратным прокси, использующим не корневой путь. Например, если ваш экземпляр OpenSearch доступен по адресу `https://example.com/opensearch/` вместо `https://example.com/`, вы должны установить `path-prefix: /opensearch`.
====

Доступны следующие функции сходства:

- `cosinesimil` - По умолчанию, подходит для большинства случаев использования. Измеряет косинусное сходство между векторами.
- `l1` - Манхэттенское расстояние между векторами.
- `l2` - Евклидово расстояние между векторами.
- `linf` - Расстояние Чебышёва между векторами.## Ручная конфигурация

Вместо использования автоконфигурации Spring Boot вы можете вручную настроить хранилище векторов OpenSearch. Для этого вам нужно добавить `spring-ai-opensearch-store` в ваш проект:

```xml
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-opensearch-store</artifactId>
</dependency>
```

или в файл сборки Gradle `build.gradle`:

```groovy
dependencies {
    implementation 'org.springframework.ai:spring-ai-opensearch-store'
}
```

> **Совет:** Обратитесь к разделу xref:getting-started.adoc#dependency-management[Управление зависимостями], чтобы добавить BOM Spring AI в ваш файл сборки.

Создайте бин клиента OpenSearch:

```java
@Bean
public OpenSearchClient openSearchClient() {
    RestClient restClient = RestClient.builder(
        HttpHost.create("http://localhost:9200"))
        .build();
    
    return new OpenSearchClient(new RestClientTransport(
        restClient, new JacksonJsonpMapper()));
}
```

Затем создайте бин `OpenSearchVectorStore`, используя паттерн строителя:

```java
@Bean
public VectorStore vectorStore(OpenSearchClient openSearchClient, EmbeddingModel embeddingModel) {
    return OpenSearchVectorStore.builder(openSearchClient, embeddingModel)
        .index("custom-index")                // Необязательно: по умолчанию "spring-ai-document-index"
        .similarityFunction("l2")             // Необязательно: по умолчанию "cosinesimil"
        .useApproximateKnn(true)              // Необязательно: по умолчанию false (точный k-NN)
        .dimensions(1536)                     // Необязательно: по умолчанию 1536 или размеры модели встраивания
        .initializeSchema(true)               // Необязательно: по умолчанию false
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

Вы также можете использовать универсальные, переносимые xref:api/vectordbs.adoc#metadata-filters[фильтры метаданных] с OpenSearch.

Например, вы можете использовать либо язык выражений текста:

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

> **Примечание:** Эти (переносимые) выражения фильтров автоматически преобразуются в проприетарный формат фильтра OpenSearch [Query string query](https://opensearch.org/docs/latest/query-dsl/full-text/query-string/).

Например, это переносимое выражение фильтра:

```sql
author in ['john', 'jill'] && 'article_type' == 'blog'
```

преобразуется в проприетарный формат фильтра OpenSearch:

```text
(metadata.author:john OR jill) AND metadata.article_type:blog
```

## Доступ к нативному клиентуРеализация OpenSearch Vector Store предоставляет доступ к базовому нативному клиенту OpenSearch (`OpenSearchClient`) через метод `getNativeClient()`:

```java
OpenSearchVectorStore vectorStore = context.getBean(OpenSearchVectorStore.class);
Optional<OpenSearchClient> nativeClient = vectorStore.getNativeClient();

if (nativeClient.isPresent()) {
    OpenSearchClient client = nativeClient.get();
    // Используйте нативный клиент для операций, специфичных для OpenSearch
}
```

Нативный клиент предоставляет доступ к функциям и операциям, специфичным для OpenSearch, которые могут не быть доступны через интерфейс `VectorStore`.
