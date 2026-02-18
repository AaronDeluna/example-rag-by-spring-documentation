# Couchbase

В этом разделе вы узнаете, как настроить `CouchbaseSearchVectorStore` для хранения векторных представлений документов и выполнения поисков по сходству с использованием Couchbase.

[Couchbase](https://docs.couchbase.com/server/current/vector-search/vector-search.html) — это распределенная база данных JSON-документов, обладающая всеми необходимыми возможностями реляционной СУБД. Среди прочих функций она позволяет пользователям запрашивать информацию с использованием векторного хранения и извлечения.

## Предварительные требования

Работающий экземпляр Couchbase. Доступны следующие варианты:
Couchbase
- [Docker](https://hub.docker.com/_/couchbase/)
- [Capella - Couchbase как услуга](https://cloud.couchbase.com/)
- [Установить Couchbase локально](https://www.couchbase.com/downloads/?family=couchbase-server)
- [Couchbase Kubernetes Operator](https://www.couchbase.com/downloads/?family=open-source-kubernetes)

## Автоконфигурация

[ПРИМЕЧАНИЕ]
====
В автоконфигурации Spring AI произошли значительные изменения, касающиеся имен артефактов стартовых модулей.
Пожалуйста, обратитесь к https://docs.spring.io/spring-ai/reference/upgrade-notes.html[заметкам об обновлении] для получения дополнительной информации.
====

Spring AI предоставляет автоконфигурацию Spring Boot для Couchbase Vector Store.
Чтобы включить ее, добавьте следующую зависимость в файл `pom.xml` вашего проекта Maven:

```xml
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-starter-vector-store-couchbase</artifactId>
</dependency>
```

или в файл сборки Gradle `build.gradle`.

```groovy
dependencies {
    implementation 'org.springframework.ai:spring-ai-couchbase-store-spring-boot-starter'
}
```
> **Примечание:** Поиск векторов Couchbase доступен только начиная с версии 7.6 и версии Java SDK 3.6.0.

> **Совет:** Обратитесь к разделу xref:getting-started.adoc#dependency-management[Управление зависимостями], чтобы добавить BOM Spring AI в ваш файл сборки.

> **Совет:** Обратитесь к разделу xref:getting-started.adoc#artifact-repositories[Артефактные репозитории], чтобы добавить репозитории Milestone и/или Snapshot в ваш файл сборки.

Реализация векторного хранилища может инициализировать настроенный бакет, область, коллекцию и индекс поиска для вас с параметрами по умолчанию, но вы должны согласиться, указав булевый параметр `initializeSchema` в соответствующем конструкторе.

> **Примечание:** Это изменение является разрушающим! В более ранних версиях Spring AI эта инициализация схемы происходила по умолчанию.

Пожалуйста, ознакомьтесь со списком [параметров конфигурации](#couchbasevector-properties), чтобы узнать о значениях по умолчанию и параметрах конфигурации для векторного хранилища.

Кроме того, вам потребуется настроенный бин `EmbeddingModel`. Обратитесь к разделу xref:api/embeddings.adoc#available-implementations[EmbeddingModel] для получения дополнительной информации.

Теперь вы можете автоматически подключить `CouchbaseSearchVectorStore` как векторное хранилище в вашем приложении.

```java
@Autowired VectorStore vectorStore;

// ...

List <Document> documents = List.of(
    new Document("Spring AI rocks!! Spring AI rocks!! Spring AI rocks!! Spring AI rocks!! Spring AI rocks!!", Map.of("meta1", "meta1")),
    new Document("The World is Big and Salvation Lurks Around the Corner"),
    new Document("You walk forward facing the past and you turn back toward the future.", Map.of("meta2", "meta2")));

// Добавьте документы в Qdrant
vectorStore.add(documents);

// Извлеките документы, похожие на запрос
List<Document> results = vectorStore.similaritySearch(SearchRequest.query("Spring").withTopK(5));
```

### Параметры конфигурацииЧтобы подключиться к Couchbase и использовать `CouchbaseSearchVectorStore`, вам необходимо предоставить данные доступа к вашему экземпляру. Конфигурацию можно задать через `application.properties` Spring Boot:

```properties
spring.ai.openai.api-key=<key>
spring.couchbase.connection-string=<conn_string>
spring.couchbase.username=<username>
spring.couchbase.password=<password>
```

Если вы предпочитаете использовать переменные окружения для конфиденциальной информации, такой как пароли или API-ключи, у вас есть несколько вариантов:

#### Вариант 1: Использование языка выражений Spring (SpEL)

Вы можете использовать пользовательские имена переменных окружения и ссылаться на них в вашей конфигурации приложения с помощью SpEL:

```yaml
# В application.yml
spring:
  ai:
    openai:
      api-key: ${OPENAI_API_KEY}
  couchbase:
    connection-string: ${COUCHBASE_CONN_STRING}
    username: ${COUCHBASE_USER}
    password: ${COUCHBASE_PASSWORD}
```

```bash
# В вашем окружении или .env файле
export OPENAI_API_KEY=<api-key>
export COUCHBASE_CONN_STRING=<couchbase connection string like couchbase://localhost>
export COUCHBASE_USER=<couchbase username>
export COUCHBASE_PASSWORD=<couchbase password>
```

#### Вариант 2: Доступ к переменным окружения программноАльтернативно, вы можете получить доступ к переменным окружения в вашем Java-коде:

```java
String apiKey = System.getenv("OPENAI_API_KEY");
```

Этот подход дает вам гибкость в именовании ваших переменных окружения, сохраняя при этом конфиденциальную информацию вне файлов конфигурации вашего приложения.

> **Примечание:** Если вы решите создать shell-скрипт для упрощения будущей работы, обязательно выполните его перед запуском вашего приложения, "подключив" файл, т.е. `source <your_script_name>.sh`.

Функция автоматической конфигурации Spring Boot для кластера Couchbase создаст экземпляр бина, который будет использоваться `CouchbaseSearchVectorStore`.

Свойства Spring Boot, начинающиеся с `spring.couchbase.*`, используются для настройки экземпляра кластера Couchbase:

| Свойство | Описание | Значение по умолчанию |
| --- | --- | --- |
| `spring.couchbase.connection-string` | Строка подключения к Couchbase | `couchbase://localhost` |
| `spring.couchbase.password` | Пароль для аутентификации с Couchbase. | - |
| `spring.couchbase.username` | Имя пользователя для аутентификации с Couchbase. | - |
| `spring.couchbase.env.io.minEndpoints` | Минимальное количество сокетов на узел. | 1 |
| `spring.couchbase.env.io.maxEndpoints` | Максимальное количество сокетов на узел. | 12 |
| `spring.couchbase.env.io.idleHttpConnectionTimeout` | Время, в течение которого HTTP-соединение может оставаться неактивным, прежде чем оно будет закрыто и удалено из пула. | 1s |
| `spring.couchbase.env.ssl.enabled` | Включить ли поддержку SSL. Включается автоматически, если предоставлен "bundle", если не указано иное. | - |
| `spring.couchbase.env.ssl.bundle` | Имя SSL bundle. | - |
| `spring.couchbase.env.timeouts.connect` | Таймаут подключения к бакету. | 10s |
| `spring.couchbase.env.timeouts.disconnect` | Таймаут отключения от бакета. | 10s |
| `spring.couchbase.env.timeouts.key-value` | Таймаут для операций с конкретным ключом-значением. | 2500ms |
| `spring.couchbase.env.timeouts.key-value` | Таймаут для операций с конкретным ключом-значением с уровнем надежности. | 10s |
| `spring.couchbase.env.timeouts.key-value-durable` | Таймаут для операций с конкретным ключом-значением с уровнем надежности. | 10s |
| `spring.couchbase.env.timeouts.query` | Таймаут операций SQL++ запросов. | 75s |
| `spring.couchbase.env.timeouts.view` | Таймаут операций с обычными и геопространственными представлениями. | 75s |
| `spring.couchbase.env.timeouts.search` | Таймаут для службы поиска. | 75s |
| `spring.couchbase.env.timeouts.analytics` | Таймаут для аналитической службы. | 75s |
| `spring.couchbase.env.timeouts.management` | Таймаут для операций управления. | 75s |

Свойства, начинающиеся с префикса `spring.ai.vectorstore.couchbase.*`, используются для настройки `CouchbaseSearchVectorStore`.

| Свойство | Описание | Значение по умолчанию |
| --- | --- | --- |
| `spring.ai.vectorstore.couchbase.index-name` | Имя индекса для хранения векторов. | spring-ai-document-index |
| `spring.ai.vectorstore.couchbase.bucket-name` | Имя бакета Couchbase, родителя области. | default |
| `spring.ai.vectorstore.couchbase.scope-name` | Имя области Couchbase, родителя коллекции. Поисковые запросы будут выполняться в контексте области. | _default_ |
| `spring.ai.vectorstore.couchbase.collection-name` | Имя коллекции Couchbase для хранения документов. | _default_ |
| `spring.ai.vectorstore.couchbase.dimensions` | Количество измерений в векторе. | 1536 |
| `spring.ai.vectorstore.couchbase.similarity` | Функция сходства для использования. | `dot_product` |
| `spring.ai.vectorstore.couchbase.optimization` | Функция сходства для использования. | `recall` |
| `spring.ai.vectorstore.couchbase.initialize-schema` | инициализировать ли необходимую схему | `false` |

Доступны следующие функции сходства:

- l2_norm
- dot_product

Доступны следующие оптимизации индекса:

- recall
- latency

Более подробную информацию о каждой из них можно найти в [документации Couchbase](https://docs.couchbase.com/server/current/search/child-field-options-reference.html) по векторным поискам.## Фильтрация метаданных

Вы можете использовать универсальные, переносимые [фильтры метаданных](https://docs.spring.io/spring-ai/reference/api/vectordbs.html#_metadata_filters) с хранилищем Couchbase.

Например, вы можете использовать либо текстовый язык выражений:

```java
vectorStore.similaritySearch(
    SearchRequest.defaults()
    .query("The World")
    .topK(TOP_K)
    .filterExpression("author in ['john', 'jill'] && article_type == 'blog'"));
```

либо программно, используя DSL `Filter.Expression`:

```java
FilterExpressionBuilder b = new FilterExpressionBuilder();

vectorStore.similaritySearch(SearchRequest.defaults()
    .query("The World")
    .topK(TOP_K)
    .filterExpression(b.and(
        b.in("author","john", "jill"),
        b.eq("article_type", "blog")).build()));
```

> **Примечание:** Эти выражения фильтрации преобразуются в эквивалентные фильтры Couchbase SQL++.


## Ручная конфигурация

Вместо использования автонастройки Spring Boot вы можете вручную настроить хранилище векторов Couchbase. Для этого вам нужно добавить `spring-ai-couchbase-store` в ваш проект:

```xml
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-couchbase-store</artifactId>
</dependency>
```

или в файл сборки Gradle `build.gradle`.

```groovy
dependencies {
    implementation 'org.springframework.ai:spring-ai-couchbase-store'
}
```

Создайте бин `Cluster` Couchbase.
Читайте [Документацию Couchbase](https://docs.couchbase.com/java-sdk/current/hello-world/start-using-sdk.html) для получения более подробной информации о конфигурации пользовательского экземпляра Cluster.

```java
@Bean
public Cluster cluster() {
    return Cluster.connect("couchbase://localhost", "username", "password");
}

```

а затем создайте бин `CouchbaseSearchVectorStore`, используя паттерн строителя:

```java
@Bean
public VectorStore couchbaseSearchVectorStore(Cluster cluster,
                                              EmbeddingModel embeddingModel,
                                              Boolean initializeSchema) {
    return CouchbaseSearchVectorStore
            .builder(cluster, embeddingModel)
            .bucketName("test")
            .scopeName("test")
            .collectionName("test")
            .initializeSchema(initializeSchema)
            .build();
}

// Это может быть любая реализация EmbeddingModel.
@Bean
public EmbeddingModel embeddingModel() {
    return new OpenAiEmbeddingModel(OpenAiApi.builder().apiKey(this.openaiKey).build());
}
```

## Ограничения

> **Примечание:** Обязательно активируйте следующие службы Couchbase: Data, Query, Index, Search. Хотя Data и Search могут быть достаточными, Query и Index необходимы для поддержки полного механизма фильтрации метаданных.
