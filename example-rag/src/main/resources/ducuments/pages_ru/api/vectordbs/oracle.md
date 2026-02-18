# Oracle Database 23ai - AI Vector Search

Возможности [AI Vector Search](https://docs.oracle.com/en/database/oracle/oracle-database/23/vecse/overview-ai-vector-search.html) в Oracle Database 23ai (23.4+) доступны как Spring AI `VectorStore`, чтобы помочь вам хранить векторные представления документов и выполнять поиск по сходству. Конечно, все другие функции также доступны.

> **Совет:** В приложении [Запуск Oracle Database 23ai локально](#Run Oracle Database 23ai locally) показано, как запустить базу данных с помощью легковесного Docker-контейнера.

## Автоконфигурация

[ПРИМЕЧАНИЕ]
====
В автоконфигурации Spring AI произошли значительные изменения, а также изменились названия артефактов стартовых модулей.
Пожалуйста, обратитесь к https://docs.spring.io/spring-ai/reference/upgrade-notes.html[замечаниям по обновлению] для получения дополнительной информации.
====

Начните с добавления зависимости стартового модуля Oracle Vector Store в ваш проект:

```xml
<dependency>
	<groupId>org.springframework.ai</groupId>
	<artifactId>spring-ai-starter-vector-store-oracle</artifactId>
</dependency>
```

или в файл сборки Gradle `build.gradle`.

```groovy
dependencies {
    implementation 'org.springframework.ai:spring-ai-starter-vector-store-oracle'
}
```

Если вам нужно, чтобы этот векторный магазин инициализировал схему для вас, вам нужно передать true для параметра `initializeSchema` в соответствующем конструкторе или установив `...initialize-schema=true` в файле `application.properties`.

> **Примечание:** это является изменением, которое нарушает совместимость! В более ранних версиях Spring AI инициализация схемы происходила по умолчанию.

Векторный магазин также требует экземпляр `EmbeddingModel` для вычисления векторных представлений документов.
Вы можете выбрать один из доступных xref:api/embeddings.adoc#available-implementations[реализаций EmbeddingModel].

Например, чтобы использовать xref:api/embeddings/openai-embeddings.adoc[OpenAI EmbeddingModel], добавьте следующую зависимость в ваш проект:

```xml
<dependency>
	<groupId>org.springframework.ai</groupId>
	<artifactId>spring-ai-starter-model-openai</artifactId>
</dependency>
```

или в файл сборки Gradle `build.gradle`.

```groovy
dependencies {
    implementation 'org.springframework.ai:spring-ai-starter-model-openai'
}
```

> **Совет:** Обратитесь к разделу xref:getting-started.adoc#dependency-management[Управление зависимостями], чтобы добавить BOM Spring AI в ваш файл сборки.
Обратитесь к разделу xref:getting-started.adoc#artifact-repositories[Артефактные репозитории], чтобы добавить Maven Central и/или репозитории Snapshot в ваш файл сборки.

Чтобы подключиться и настроить `OracleVectorStore`, вам нужно предоставить данные для доступа к вашей базе данных.
Простая конфигурация может быть предоставлена через `application.yml` Spring Boot

[yml]
```
spring:
  datasource:
    url: jdbc:oracle:thin:@//localhost:1521/freepdb1
    username: mlops
    password: mlops
  ai:
	vectorstore:
	  oracle:
		index-type: IVF
		distance-type: COSINE
		dimensions: 1536
```

> **Совет:** Ознакомьтесь со списком xref:#oracle-properties[параметров конфигурации], чтобы узнать о значениях по умолчанию и параметрах конфигурации.

Теперь вы можете автоматически подключить `OracleVectorStore` в вашем приложении и использовать его:

```java
@Autowired VectorStore vectorStore;

// ...

List<Document> documents = List.of(
    new Document("Spring AI rocks!! Spring AI rocks!! Spring AI rocks!! Spring AI rocks!! Spring AI rocks!!", Map.of("meta1", "meta1")),
    new Document("The World is Big and Salvation Lurks Around the Corner"),
    new Document("You walk forward facing the past and you turn back toward the future.", Map.of("meta2", "meta2")));

// Добавьте документы в Oracle Vector Store
vectorStore.add(documents);

// Получите документы, похожие на запрос
List<Document> results = this.vectorStore.similaritySearch(SearchRequest.builder().query("Spring").topK(5).build());
```

### Параметры конфигурацииВы можете использовать следующие свойства в вашей конфигурации Spring Boot для настройки `OracleVectorStore`.

| Свойство | Описание | Значение по умолчанию |
| --- | --- | --- |
| `spring.ai.vectorstore.oracle.index-type` | Тип индекса для поиска ближайших соседей. Опции: `NONE` - точный поиск ближайших соседей, `IVF` - индекс инвертированного плоского файла. Он имеет более быстрое время сборки и использует меньше памяти, чем HNSW, но имеет более низкую производительность запросов (в терминах компромисса скорость-отзыв). `HNSW` - создает многослойный граф. Он имеет более медленное время сборки и использует больше памяти, чем IVF, но имеет лучшую производительность запросов (в терминах компромисса скорость-отзыв). | NONE |
| `spring.ai.vectorstore.oracle.distance-type` | Тип расстояния для поиска среди `COSINE` (по умолчанию), `DOT`, `EUCLIDEAN`, `EUCLIDEAN_SQUARED` и `MANHATTAN`. |  |
| `spring.ai.vectorstore.oracle.forced-normalization` | Позволяет включить нормализацию векторов (если true) перед вставкой и для поиска по сходству. |  |
| `spring.ai.vectorstore.oracle.dimensions` | Размерность встраиваний. Если не указано явно, OracleVectorStore позволит максимальное значение: 65535. Размерности устанавливаются для столбца встраивания при создании таблицы. Если вы измените размерности, вам придется также пересоздать таблицу. | 65535 |
| `spring.ai.vectorstore.oracle.remove-existing-vector-store-table` | Удаляет существующую таблицу при запуске. | false |
| `spring.ai.vectorstore.oracle.initialize-schema` | Нужно ли инициализировать требуемую схему. | false |
| `spring.ai.vectorstore.oracle.search-accuracy` | Указывает запрашиваемую целевую точность в присутствии индекса. Отключено по умолчанию. Вам нужно указать целое число в диапазоне [1,100], чтобы переопределить точность индекса по умолчанию (95). Использование более низкой точности обеспечивает приближенный поиск по сходству, балансируя скорость и точность. | -1 (`DEFAULT_SEARCH_ACCURACY`) |

## Фильтрация метаданных

Вы можете использовать универсальные, переносимые [фильтры метаданных](https://docs.spring.io/spring-ai/reference/api/vectordbs.html#_metadata_filters) с `OracleVectorStore`.

Например, вы можете использовать либо текстовый язык выражений:

```java
vectorStore.similaritySearch(
    SearchRequest.builder()
    .query("The World")
    .topK(TOP_K)
    .similarityThreshold(SIMILARITY_THRESHOLD)
    .filterExpression("author in ['john', 'jill'] && article_type == 'blog'").build());
```

либо программно, используя DSL `Filter.Expression`:

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

> **Примечание:** Эти выражения фильтров преобразуются в эквивалентные фильтры `OracleVectorStore`.

## Ручная конфигурацияВместо использования автонастройки Spring Boot, вы можете вручную настроить `OracleVectorStore`. Для этого вам необходимо добавить драйвер JDBC Oracle и зависимости автонастройки `JdbcTemplate` в ваш проект:

```xml
<dependency>
	<groupId>org.springframework.boot</groupId>
	<artifactId>spring-boot-starter-jdbc</artifactId>
</dependency>

<dependency>
	<groupId>com.oracle.database.jdbc</groupId>
	<artifactId>ojdbc11</artifactId>
	<scope>runtime</scope>
</dependency>

<dependency>
	<groupId>org.springframework.ai</groupId>
	<artifactId>spring-ai-oracle-store</artifactId>
</dependency>
```

> **Совет:** Обратитесь к разделу xref:getting-started.adoc#dependency-management[Управление зависимостями], чтобы добавить BOM Spring AI в ваш файл сборки.

Чтобы настроить `OracleVectorStore` в вашем приложении, вы можете использовать следующую конфигурацию:

```java
@Bean
public VectorStore vectorStore(JdbcTemplate jdbcTemplate, EmbeddingModel embeddingModel) {
    return OracleVectorStore.builder(jdbcTemplate, embeddingModel)
        .tableName("my_vectors")
        .indexType(OracleVectorStoreIndexType.IVF)
        .distanceType(OracleVectorStoreDistanceType.COSINE)
        .dimensions(1536)
        .searchAccuracy(95)
        .initializeSchema(true)
        .build();
}
```

## Запуск Oracle Database 23ai локально

```
docker run --rm --name oracle23ai -p 1521:1521 -e APP_USER=mlops -e APP_USER_PASSWORD=mlops -e ORACLE_PASSWORD=mlops gvenzl/oracle-free:23-slim
```

Затем вы можете подключиться к базе данных, используя:

```
sql mlops/mlops@localhost/freepdb1
```

## Доступ к нативному клиенту

Реализация Oracle Vector Store предоставляет доступ к базовому нативному клиенту Oracle (`OracleConnection`) через метод `getNativeClient()`:

```java
OracleVectorStore vectorStore = context.getBean(OracleVectorStore.class);
Optional<OracleConnection> nativeClient = vectorStore.getNativeClient();

if (nativeClient.isPresent()) {
    OracleConnection connection = nativeClient.get();
    // Используйте нативный клиент для операций, специфичных для Oracle
}
```

Нативный клиент предоставляет доступ к функциям и операциям, специфичным для Oracle, которые могут не быть доступны через интерфейс `VectorStore`.
