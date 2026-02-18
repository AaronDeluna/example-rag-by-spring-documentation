# PGvector

В этом разделе описывается, как настроить PGvector `VectorStore` для хранения векторных представлений документов и выполнения поиска по сходству.

[PGvector](https://github.com/pgvector/pgvector) — это расширение с открытым исходным кодом для PostgreSQL, которое позволяет хранить и выполнять поиск по векторным представлениям, сгенерированным с помощью машинного обучения. Оно предоставляет различные возможности, которые позволяют пользователям определять как точных, так и приблизительных ближайших соседей. Оно разработано для бесшовной работы с другими функциями PostgreSQL, включая индексацию и запросы.

## Предварительные требования

Сначала вам нужен доступ к экземпляру PostgreSQL с включенными расширениями `vector`, `hstore` и `uuid-ossp`.

> **Совет:** Вы можете запустить базу данных PGvector как сервис разработки Spring Boot через xref:api/docker-compose.adoc[Docker Compose] или xref:api/testcontainers.adoc[Testcontainers]. В качестве альтернативы, приложение [настройка локальной Postgres/PGVector](#Run Postgres & PGVector DB locally) показывает, как настроить базу данных локально с помощью контейнера Docker.

При запуске с явно включенной функцией инициализации схемы `PgVectorStore` попытается установить необходимые расширения базы данных и создать требуемую таблицу `vector_store` с индексом, если она не существует.

При желании вы можете сделать это вручную следующим образом:

[sql]
```
CREATE EXTENSION IF NOT EXISTS vector;
CREATE EXTENSION IF NOT EXISTS hstore;
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE IF NOT EXISTS vector_store (
	id uuid DEFAULT uuid_generate_v4() PRIMARY KEY,
	content text,
	metadata json,
	embedding vector(1536) // 1536 — это размерность вектора по умолчанию
);

CREATE INDEX ON vector_store USING HNSW (embedding vector_cosine_ops);
```

> **Совет:** замените `1536` на фактическую размерность вектора, если вы используете другую размерность. PGvector поддерживает максимум 2000 размерностей для индексов HNSW.

Далее, если это необходимо, получите API-ключ для xref:api/embeddings.adoc#available-implementations[EmbeddingModel], чтобы генерировать векторные представления, хранящиеся в `PgVectorStore`.

## Автоконфигурация[NOTE]
====
В Spring AI произошли значительные изменения в автонастройке и названиях артефактов стартовых модулей.
Пожалуйста, обратитесь к https://docs.spring.io/spring-ai/reference/upgrade-notes.html[заметкам об обновлении] для получения дополнительной информации.
====

Затем добавьте зависимость PgVectorStore boot starter в ваш проект:

```xml
<dependency>
	<groupId>org.springframework.ai</groupId>
	<artifactId>spring-ai-starter-vector-store-pgvector</artifactId>
</dependency>
```

или в ваш файл сборки Gradle `build.gradle`.

```groovy
dependencies {
    implementation 'org.springframework.ai:spring-ai-starter-vector-store-pgvector'
}
```

Реализация векторного хранилища может инициализировать необходимую схему для вас, но вы должны согласиться, указав булевый параметр `initializeSchema` в соответствующем конструкторе или установив `...initialize-schema=true` в файле `application.properties`.

> **Примечание:** Это является нарушением совместимости! В более ранних версиях Spring AI инициализация схемы происходила по умолчанию.

Векторное хранилище также требует экземпляр `EmbeddingModel` для вычисления встраиваний для документов.
Вы можете выбрать одну из доступных xref:api/embeddings.adoc#available-implementations[реализаций EmbeddingModel].

Например, чтобы использовать xref:api/embeddings/openai-embeddings.adoc[OpenAI EmbeddingModel], добавьте следующую зависимость в ваш проект:

```xml
<dependency>
	<groupId>org.springframework.ai</groupId>
	<artifactId>spring-ai-starter-model-openai</artifactId>
</dependency>
```

или в ваш файл сборки Gradle `build.gradle`.

```groovy
dependencies {
    implementation 'org.springframework.ai:spring-ai-starter-model-openai'
}
```

> **Совет:** Обратитесь к разделу xref:getting-started.adoc#dependency-management[Управление зависимостями], чтобы добавить Spring AI BOM в ваш файл сборки.
Обратитесь к разделу xref:getting-started.adoc#artifact-repositories[Артефактные репозитории], чтобы добавить Maven Central и/или Snapshot репозитории в ваш файл сборки.

Чтобы подключиться и настроить `PgVectorStore`, вам необходимо предоставить данные доступа к вашему экземпляру.
Простая конфигурация может быть предоставлена через `application.yml` Spring Boot.

[yml]
```
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/postgres
    username: postgres
    password: postgres
  ai:
	vectorstore:
	  pgvector:
		index-type: HNSW
		distance-type: COSINE_DISTANCE
		dimensions: 1536
		max-document-batch-size: 10000 # Необязательно: Максимальное количество документов на партию
```

> **Совет:** Если вы запускаете PGvector как сервис разработки Spring Boot через [Docker Compose](https://docs.spring.io/spring-boot/reference/features/dev-services.html#features.dev-services.docker-compose)
или [Testcontainers](https://docs.spring.io/spring-boot/reference/features/dev-services.html#features.dev-services.testcontainers),
вам не нужно настраивать URL, имя пользователя и пароль, так как они автоматически настраиваются Spring Boot.

> **Совет:** Ознакомьтесь со списком xref:#pgvector-properties[параметров конфигурации], чтобы узнать о значениях по умолчанию и параметрах конфигурации.

Теперь вы можете автоматически подключить `VectorStore` в вашем приложении и использовать его

```java
@Autowired VectorStore vectorStore;

// ...

List<Document> documents = List.of(
    new Document("Spring AI rocks!! Spring AI rocks!! Spring AI rocks!! Spring AI rocks!! Spring AI rocks!!", Map.of("meta1", "meta1")),
    new Document("The World is Big and Salvation Lurks Around the Corner"),
    new Document("You walk forward facing the past and you turn back toward the future.", Map.of("meta2", "meta2")));

// Добавьте документы в PGVector
vectorStore.add(documents);

// Получите документы, похожие на запрос
List<Document> results = this.vectorStore.similaritySearch(SearchRequest.builder().query("Spring").topK(5).build());
```

### Параметры конфигурацииВы можете использовать следующие свойства в вашей конфигурации Spring Boot для настройки хранилища векторов PGVector.

| Свойство | Описание | Значение по умолчанию |
| --- | --- | --- |
| `spring.ai.vectorstore.pgvector.index-type` | Тип индекса для поиска ближайших соседей. Опции: `NONE` - точный поиск ближайших соседей, `IVFFlat` - индекс делит векторы на списки, а затем ищет подмножество этих списков, которые ближе всего к вектору запроса. Он имеет более быстрое время построения и использует меньше памяти, чем HNSW, но имеет более низкую производительность запросов (в терминах компромисса скорость-воспоминание). `HNSW` - создает многослойный граф. Он имеет более медленное время построения и использует больше памяти, чем IVFFlat, но имеет лучшую производительность запросов (в терминах компромисса скорость-воспоминание). Нет этапа обучения, как в IVFFlat, поэтому индекс можно создать без каких-либо данных в таблице. | HNSW |
| `spring.ai.vectorstore.pgvector.distance-type` | Тип расстояния для поиска. По умолчанию `COSINE_DISTANCE`. Но если векторы нормализованы до длины 1, вы можете использовать `EUCLIDEAN_DISTANCE` или `NEGATIVE_INNER_PRODUCT` для наилучшей производительности. | COSINE_DISTANCE |
| `spring.ai.vectorstore.pgvector.dimensions` | Размерность встраиваний. Если не указано явно, PgVectorStore извлечет размерности из предоставленной `EmbeddingModel`. Размерности устанавливаются в столбец встраивания при создании таблицы. Если вы измените размерности, вам придется заново создать таблицу vector_store. | - |
| `spring.ai.vectorstore.pgvector.remove-existing-vector-store-table` | Удаляет существующую таблицу `vector_store` при запуске. | false |
| `spring.ai.vectorstore.pgvector.initialize-schema` | Нужно ли инициализировать необходимую схему | false |
| `spring.ai.vectorstore.pgvector.schema-name` | Имя схемы хранилища векторов | `public` |
| `spring.ai.vectorstore.pgvector.table-name` | Имя таблицы хранилища векторов | `vector_store` |
| `spring.ai.vectorstore.pgvector.schema-validation` | Включает проверку схемы и имени таблицы, чтобы убедиться, что они являются действительными и существующими объектами. | false |
| `spring.ai.vectorstore.pgvector.max-document-batch-size` | Максимальное количество документов для обработки в одной партии. | 10000 |

> **Совет:** Если вы настраиваете пользовательскую схему и/или имя таблицы, рассмотрите возможность включения проверки схемы, установив `spring.ai.vectorstore.pgvector.schema-validation=true`. Это обеспечивает правильность имен и снижает риск атак SQL-инъекций.

## Фильтрация метаданных

Вы можете использовать универсальные, переносимые [фильтры метаданных](https://docs.spring.io/spring-ai/reference/api/vectordbs.html#_metadata_filters) с хранилищем PgVector.

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
        b.in("author","john", "jill"),
        b.eq("article_type", "blog")).build()).build());
```

> **Примечание:** Эти выражения фильтрации преобразуются в выражения JSON path PostgreSQL для эффективной фильтрации метаданных.

## Ручная конфигурацияВместо использования автонастройки Spring Boot, вы можете вручную настроить `PgVectorStore`. Для этого вам нужно добавить зависимости для подключения к PostgreSQL и автонастройки `JdbcTemplate` в ваш проект:

```xml
<dependency>
	<groupId>org.springframework.boot</groupId>
	<artifactId>spring-boot-starter-jdbc</artifactId>
</dependency>

<dependency>
	<groupId>org.postgresql</groupId>
	<artifactId>postgresql</artifactId>
	<scope>runtime</scope>
</dependency>

<dependency>
	<groupId>org.springframework.ai</groupId>
	<artifactId>spring-ai-pgvector-store</artifactId>
</dependency>
```

> **Совет:** Обратитесь к разделу xref:getting-started.adoc#dependency-management[Управление зависимостями], чтобы добавить Spring AI BOM в ваш файл сборки.

Чтобы настроить PgVector в вашем приложении, вы можете использовать следующую конфигурацию:

```java
@Bean
public VectorStore vectorStore(JdbcTemplate jdbcTemplate, EmbeddingModel embeddingModel) {
    return PgVectorStore.builder(jdbcTemplate, embeddingModel)
        .dimensions(1536)                    // Необязательно: по умолчанию соответствует размеру модели или 1536
        .distanceType(COSINE_DISTANCE)       // Необязательно: по умолчанию COSINE_DISTANCE
        .indexType(HNSW)                     // Необязательно: по умолчанию HNSW
        .initializeSchema(true)              // Необязательно: по умолчанию false
        .schemaName("public")                // Необязательно: по умолчанию "public"
        .vectorTableName("vector_store")     // Необязательно: по умолчанию "vector_store"
        .maxDocumentBatchSize(10000)         // Необязательно: по умолчанию 10000
        .build();
}
```

## Запуск Postgres и PGVector DB локально

```
docker run -it --rm --name postgres -p 5432:5432 -e POSTGRES_USER=postgres -e POSTGRES_PASSWORD=postgres pgvector/pgvector
```

Вы можете подключиться к этому серверу следующим образом:

```
psql -U postgres -h localhost -p 5432
```

## Доступ к нативному клиенту

Реализация PGVector Store предоставляет доступ к базовому нативному JDBC клиенту (`JdbcTemplate`) через метод `getNativeClient()`:

```java
PgVectorStore vectorStore = context.getBean(PgVectorStore.class);
Optional<JdbcTemplate> nativeClient = vectorStore.getNativeClient();

if (nativeClient.isPresent()) {
    JdbcTemplate jdbc = nativeClient.get();
    // Используйте нативный клиент для операций, специфичных для PostgreSQL
}
```

Нативный клиент предоставляет вам доступ к функциям и операциям, специфичным для PostgreSQL, которые могут не быть доступны через интерфейс `VectorStore`.
