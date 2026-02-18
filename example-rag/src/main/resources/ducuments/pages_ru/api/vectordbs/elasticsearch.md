# Elasticsearch

В этом разделе описывается, как настроить `VectorStore` в Elasticsearch для хранения векторных представлений документов и выполнения поиска по сходству.

[Elasticsearch](https://www.elastic.co/elasticsearch) — это движок поиска и аналитики с открытым исходным кодом, основанный на библиотеке Apache Lucene.

## Предварительные требования

Запущенный экземпляр Elasticsearch. Доступны следующие варианты:

- [Docker](https://hub.docker.com/_/elasticsearch/)
- [Самостоятельно управляемый Elasticsearch](https://www.elastic.co/guide/en/elasticsearch/reference/current/install-elasticsearch.html#elasticsearch-install-packages)
- [Elastic Cloud](https://www.elastic.co/cloud/elasticsearch-service/signup?page=docs&placement=docs-body)

## Автонастройка[NOTE]
====
В Spring AI произошли значительные изменения в автонастройке и названиях артефактов стартовых модулей.
Пожалуйста, обратитесь к https://docs.spring.io/spring-ai/reference/upgrade-notes.html[заметкам по обновлению] для получения дополнительной информации.
====

Spring AI предоставляет автонастройку Spring Boot для Elasticsearch Vector Store.
Чтобы включить эту функциональность, добавьте следующую зависимость в файл сборки Maven `pom.xml` или Gradle `build.gradle` вашего проекта:

[tabs]
======
Maven::
+
```xml
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-starter-vector-store-elasticsearch</artifactId>
</dependency>
```

Gradle::
+
```groovy
dependencies {
    implementation 'org.springframework.ai:spring-ai-starter-vector-store-elasticsearch'
}
```
======

[NOTE]
--
Для версий spring-boot до 3.3.0 необходимо явно добавить зависимость elasticsearch-java версии > 8.13.3, иначе используемая старая версия будет несовместима с выполняемыми запросами:
[tabs]
======
Maven::
+
```xml
<dependency>
    <groupId>co.elastic.clients</groupId>
    <artifactId>elasticsearch-java</artifactId>
    <version>8.13.3</version>
</dependency>
```

Gradle::
+
```groovy
dependencies {
    implementation 'co.elastic.clients:elasticsearch-java:8.13.3'
}
```
======
--

> **Совет:** Обратитесь к разделу xref:getting-started.adoc#dependency-management[Управление зависимостями], чтобы добавить Spring AI BOM в ваш файл сборки.

> **Совет:** Обратитесь к разделу xref:getting-started.adoc#artifact-repositories[Артефактные репозитории], чтобы добавить Maven Central и/или Snapshot репозитории в ваш файл сборки.

Реализация векторного хранилища может инициализировать необходимую схему для вас, но вы должны согласиться, указав булевый параметр `initializeSchema` в соответствующем конструкторе или установив `...initialize-schema=true` в файле `application.properties`.
В качестве альтернативы вы можете отказаться от инициализации и создать индекс вручную, используя клиент Elasticsearch, что может быть полезно, если индекс требует сложного отображения или дополнительной конфигурации.

> **Примечание:** это является нарушением совместимости! В более ранних версиях Spring AI эта инициализация схемы происходила по умолчанию.

Пожалуйста, ознакомьтесь со списком [параметров конфигурации](#elasticsearchvector-properties) для векторного хранилища, чтобы узнать о значениях по умолчанию и параметрах конфигурации.
Эти свойства также могут быть установлены путем настройки бина `ElasticsearchVectorStoreOptions`.

Кроме того, вам потребуется настроенный бин `EmbeddingModel`. Обратитесь к разделу xref:api/embeddings.adoc#available-implementations[EmbeddingModel] для получения дополнительной информации.

Теперь вы можете автоматически подключить `ElasticsearchVectorStore` как векторное хранилище в вашем приложении.

```java
@Autowired VectorStore vectorStore;

// ...

List <Document> documents = List.of(
    new Document("Spring AI rocks!! Spring AI rocks!! Spring AI rocks!! Spring AI rocks!! Spring AI rocks!!", Map.of("meta1", "meta1")),
    new Document("The World is Big and Salvation Lurks Around the Corner"),
    new Document("You walk forward facing the past and you turn back toward the future.", Map.of("meta2", "meta2")));

// Добавьте документы в Elasticsearch
vectorStore.add(documents);

// Получите документы, похожие на запрос
List<Document> results = this.vectorStore.similaritySearch(SearchRequest.builder().query("Spring").topK(5).build());
```

[[elasticsearchvector-properties]]
### Параметры конфигурацииЧтобы подключиться к Elasticsearch и использовать `ElasticsearchVectorStore`, вам необходимо предоставить данные доступа к вашему экземпляру. Простую конфигурацию можно задать через `application.yml` Spring Boot:

```yaml
spring:
  elasticsearch:
    uris: <elasticsearch instance URIs>
    username: <elasticsearch username>
    password: <elasticsearch password>
  ai:
    vectorstore:
      elasticsearch:
        initialize-schema: true
        index-name: custom-index
        dimensions: 1536
        similarity: cosine
```

Свойства Spring Boot, начинающиеся с `spring.elasticsearch.*`, используются для настройки клиента Elasticsearch:

[cols="2,5,1",stripes=even]
| Свойство | Описание | Значение по умолчанию |
| --- | --- | --- |
| `spring.elasticsearch.connection-timeout` | Таймаут подключения, используемый при взаимодействии с Elasticsearch. | `1s` |
| `spring.elasticsearch.password` | Пароль для аутентификации в Elasticsearch. | - |
| `spring.elasticsearch.username` | Имя пользователя для аутентификации в Elasticsearch. | - |
| `spring.elasticsearch.uris` | Список экземпляров Elasticsearch, разделенных запятыми. | `+http://localhost:9200+` |
| `spring.elasticsearch.path-prefix` | Префикс, добавляемый к пути каждого запроса, отправляемого в Elasticsearch. | - |
| `spring.elasticsearch.restclient.sniffer.delay-after-failure` | Задержка выполнения снайфера, запланированная после сбоя. | `1m` |
| `spring.elasticsearch.restclient.sniffer.interval` | Интервал между последовательными обычными выполнениями снайфера. | `5m` |
| `spring.elasticsearch.restclient.ssl.bundle` | Имя SSL-пакета. | - |
| `spring.elasticsearch.socket-keep-alive` | Включить ли поддержку keep alive для сокета между клиентом и Elasticsearch. | `false` |
| `spring.elasticsearch.socket-timeout` | Таймаут сокета, используемый при взаимодействии с Elasticsearch. | `30s` |

Свойства, начинающиеся с `spring.ai.vectorstore.elasticsearch.*`, используются для настройки `ElasticsearchVectorStore`:

[cols="2,5,1",stripes=even]
| Свойство | Описание | Значение по умолчанию |
| --- | --- | --- |
| `spring.ai.vectorstore.elasticsearch.initialize-schema` | Нужно ли инициализировать требуемую схему | `false` |
| `spring.ai.vectorstore.elasticsearch.index-name` | Имя индекса для хранения векторов | `spring-ai-document-index` |
| `spring.ai.vectorstore.elasticsearch.dimensions` | Количество измерений в векторе | `1536` |
| `spring.ai.vectorstore.elasticsearch.similarity` | Функция сходства, которую нужно использовать | `cosine` |
| `spring.ai.vectorstore.elasticsearch.embedding-field-name` | Имя поля вектора для поиска | `embedding` |

Доступны следующие функции сходства:

- `cosine` - По умолчанию, подходит для большинства случаев использования. Измеряет косинусное сходство между векторами.
- `l2_norm` - Евклидово расстояние между векторами. Более низкие значения указывают на более высокое сходство.
- `dot_product` - Лучшая производительность для нормализованных векторов (например, встраивания OpenAI).

Более подробную информацию о каждой функции можно найти в [документации Elasticsearch](https://www.elastic.co/guide/en/elasticsearch/reference/master/dense-vector.html#dense-vector-params) по плотным векторам.

## Фильтрация метаданныхВы также можете использовать универсальные, переносимые xref:api/vectordbs.adoc#metadata-filters[фильтры метаданных] с Elasticsearch.

Например, вы можете использовать либо язык выражений текста:

```java
vectorStore.similaritySearch(SearchRequest.builder()
        .query("The World")
        .topK(TOP_K)
        .similarityThreshold(SIMILARITY_THRESHOLD)
        .filterExpression("author in ['john', 'jill'] && 'article_type' == 'blog'").build());
```

либо программно, используя `Filter.Expression` DSL:

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

> **Примечание:** Эти (переносимые) выражения фильтров автоматически преобразуются в собственный формат фильтра Elasticsearch [Query string query](https://www.elastic.co/guide/en/elasticsearch/reference/current/query-dsl-query-string-query.html).

Например, это переносимое выражение фильтра:

```sql
author in ['john', 'jill'] && 'article_type' == 'blog'
```

преобразуется в собственный формат фильтра Elasticsearch:

```text
(metadata.author:john OR jill) AND metadata.article_type:blog
```

## Ручная конфигурация

Вместо использования автонастройки Spring Boot вы можете вручную настроить векторный магазин Elasticsearch. Для этого вам нужно добавить `spring-ai-elasticsearch-store` в ваш проект:

```xml
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-elasticsearch-store</artifactId>
</dependency>
```

или в ваш файл сборки Gradle `build.gradle`.

```groovy
dependencies {
    implementation 'org.springframework.ai:spring-ai-elasticsearch-store'
}
```

Создайте бин `RestClient` для Elasticsearch.
Читайте [Документацию Elasticsearch](https://www.elastic.co/guide/en/elasticsearch/client/java-api-client/current/java-rest-low-usage-initialization.html) для получения более подробной информации о конфигурации пользовательского RestClient.

```java
@Bean
public RestClient restClient() {
    return RestClient.builder(new HttpHost("<host>", 9200, "http"))
        .setDefaultHeaders(new Header[]{
            new BasicHeader("Authorization", "Basic <encoded username and password>")
        })
        .build();
}
```

Затем создайте бин `ElasticsearchVectorStore`, используя паттерн строителя:

```java
@Bean
public VectorStore vectorStore(RestClient restClient, EmbeddingModel embeddingModel) {
    ElasticsearchVectorStoreOptions options = new ElasticsearchVectorStoreOptions();
    options.setIndexName("custom-index");    // Необязательно: по умолчанию "spring-ai-document-index"
    options.setSimilarity(COSINE);           // Необязательно: по умолчанию COSINE
    options.setDimensions(1536);             // Необязательно: по умолчанию размеры модели или 1536

    return ElasticsearchVectorStore.builder(restClient, embeddingModel)
        .options(options)                     // Необязательно: используйте пользовательские параметры
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

## Доступ к нативному клиентуРеализация Elasticsearch Vector Store предоставляет доступ к базовому нативному клиенту Elasticsearch (`ElasticsearchClient`) через метод `getNativeClient()`:

```java
ElasticsearchVectorStore vectorStore = context.getBean(ElasticsearchVectorStore.class);
Optional<ElasticsearchClient> nativeClient = vectorStore.getNativeClient();

if (nativeClient.isPresent()) {
    ElasticsearchClient client = nativeClient.get();
    // Используйте нативный клиент для операций, специфичных для Elasticsearch
}
```

Нативный клиент предоставляет доступ к функциям и операциям, специфичным для Elasticsearch, которые могут не быть доступны через интерфейс `VectorStore`.
