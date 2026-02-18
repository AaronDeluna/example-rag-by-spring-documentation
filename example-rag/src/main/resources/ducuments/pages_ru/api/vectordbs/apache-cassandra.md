# Apache Cassandra Vector Store

В этом разделе описывается, как настроить `CassandraVectorStore` для хранения векторных представлений документов и выполнения поиска по сходству.

## Что такое Apache Cassandra?

[Apache Cassandra®](https://cassandra.apache.org) — это настоящая распределенная база данных с открытым исходным кодом, известная своей линейной масштабируемостью, доказанной устойчивостью к сбоям и низкой задержкой, что делает ее идеальной платформой для критически важных транзакционных данных.

Ее поиск по векторному сходству (VSS) основан на библиотеке JVector, которая обеспечивает производительность и релевантность на высшем уровне.

Поиск векторов в Apache Cassandra выполняется так же просто, как:
```sql
SELECT content FROM table ORDER BY content_vector ANN OF query_embedding;
```

Больше документации по этому вопросу можно прочитать [здесь](https://cassandra.apache.org/doc/latest/cassandra/getting-started/vector-search-quickstart.html).

Этот Spring AI Vector Store разработан как для новых приложений RAG, так и для возможности доработки на основе существующих данных и таблиц.

Магазин также может использоваться для не-RAG случаев в существующей базе данных, например, для семантического поиска, поиска по географической близости и т. д.

Магазин автоматически создаст или улучшит схему по мере необходимости в соответствии с его конфигурацией. Если вы не хотите модификаций схемы, настройте магазин с помощью `initializeSchema`.

При использовании spring-boot-autoconfigure `initializeSchema` по умолчанию равно `false`, в соответствии со стандартами Spring Boot, и вы должны согласиться на создание/модификации схемы, установив `...initialize-schema=true` в файле `application.properties`.

## Что такое JVector?

[JVector](https://github.com/jbellis/jvector) — это встроенный векторный поисковый движок на чистом Java.

Он выделяется среди других реализаций поиска по векторному сходству HNSW тем, что:

- Быстрый алгоритмически. JVector использует современные графовые алгоритмы, вдохновленные DiskANN и сопутствующими исследованиями, которые обеспечивают высокий уровень полноты и низкую задержку.
- Быстрый в реализации. JVector использует API Panama SIMD для ускорения построения индекса и запросов.
- Эффективный по памяти. JVector сжимает векторы с помощью продуктовой квантизации, чтобы они могли оставаться в памяти во время поиска.
- Осведомленный о диске. Макет диска JVector разработан для минимизации необходимых операций ввода-вывода во время запроса.
- Параллельный. Построение индекса масштабируется линейно как минимум на 32 потока. Удвоив количество потоков, вы уменьшаете время сборки вдвое.
- Инкрементальный. Запрашивайте свой индекс по мере его построения. Нет задержки между добавлением вектора и возможностью найти его в результатах поиска.
- Легкий для встраивания. API разработан для простого встраивания, людьми, использующими его в производстве.

## Предварительные требования

1. Экземпляр `EmbeddingModel` для вычисления векторных представлений документов. Обычно он настраивается как Spring Bean. Доступно несколько вариантов:

- `Transformers Embedding` — вычисляет векторное представление в вашей локальной среде. По умолчанию используется ONNX и все-MiniLM-L6-v2 Sentence Transformers. Это просто работает.
- Если вы хотите использовать векторные представления OpenAI — используйте конечную точку векторных представлений OpenAI. Вам нужно создать учетную запись на [OpenAI Signup](https://platform.openai.com/signup) и сгенерировать токен api-key на [API Keys](https://platform.openai.com/account/api-keys).
- Есть много других вариантов, смотрите документацию `Embeddings API`.

2. Экземпляр Apache Cassandra, начиная с версии 5.0-beta1
a. [DIY Quick Start](https://cassandra.apache.org/_/quickstart.html)
b. Для управляемого предложения [Astra DB](https://astra.datastax.com/) предлагает хороший бесплатный тариф. 

## Зависимости[NOTE]
====
В Spring AI произошли значительные изменения в автонастройке и названиях артефактов стартовых модулей.
Пожалуйста, обратитесь к https://docs.spring.io/spring-ai/reference/upgrade-notes.html[заметкам об обновлении] для получения дополнительной информации.
====

> **Совет:** Для управления зависимостями мы рекомендуем использовать Spring AI BOM, как объяснено в разделе xref:getting-started.adoc#dependency-management[Управление зависимостями].

Добавьте эти зависимости в ваш проект:

- Для использования только Cassandra Vector Store:

```xml
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-cassandra-store</artifactId>
</dependency>
```

- Или, для всего необходимого в приложении RAG (с использованием модели встраивания ONNX по умолчанию):

```xml
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-starter-vector-store-cassandra</artifactId>
</dependency>
```

## Свойства конфигурации

Вы можете использовать следующие свойства в вашей конфигурации Spring Boot для настройки векторного хранилища Apache Cassandra.

[cols="2,1",stripes=even]
| Свойство | Значение по умолчанию |
| --- | --- |
| `spring.ai.vectorstore.cassandra.keyspace` | springframework |
| `spring.ai.vectorstore.cassandra.table` | ai_vector_store |
| `spring.ai.vectorstore.cassandra.initialize-schema` | false |
| `spring.ai.vectorstore.cassandra.index-name` |  |
| `spring.ai.vectorstore.cassandra.content-column-name` | content |
| `spring.ai.vectorstore.cassandra.embedding-column-name` | embedding |
| `spring.ai.vectorstore.cassandra.fixed-thread-pool-executor-size` | 16 |

## Использование

### Основное использование

Создайте экземпляр CassandraVectorStore как Spring Bean:

```java
@Bean
public VectorStore vectorStore(CqlSession session, EmbeddingModel embeddingModel) {
    return CassandraVectorStore.builder(embeddingModel)
        .session(session)
        .keyspace("my_keyspace")
        .table("my_vectors")
        .build();
}
```

После того как у вас есть экземпляр векторного хранилища, вы можете добавлять документы и выполнять поиск:

```java
// Добавить документы
vectorStore.add(List.of(
    new Document("1", "content1", Map.of("key1", "value1")),
    new Document("2", "content2", Map.of("key2", "value2"))
));

// Поиск с фильтрами
List<Document> results = vectorStore.similaritySearch(
    SearchRequest.query("search text")
        .withTopK(5)
        .withSimilarityThreshold(0.7f)
        .withFilterExpression("metadata.key1 == 'value1'")
);
```

### Расширенная конфигурация

Для более сложных случаев использования вы можете настроить дополнительные параметры в вашем Spring Bean:

```java
@Bean
public VectorStore vectorStore(CqlSession session, EmbeddingModel embeddingModel) {
    return CassandraVectorStore.builder(embeddingModel)
        .session(session)
        .keyspace("my_keyspace")
        .table("my_vectors")
        // Настройка первичных ключей
        .partitionKeys(List.of(
            new SchemaColumn("id", DataTypes.TEXT),
            new SchemaColumn("category", DataTypes.TEXT)
        ))
        .clusteringKeys(List.of(
            new SchemaColumn("timestamp", DataTypes.TIMESTAMP)
        ))
        // Добавление столбцов метаданных с необязательной индексацией
        .addMetadataColumns(
            new SchemaColumn("category", DataTypes.TEXT, SchemaColumnTags.INDEXED),
            new SchemaColumn("score", DataTypes.DOUBLE)
        )
        // Настройка имен столбцов
        .contentColumnName("text")
        .embeddingColumnName("vector")
        // Оптимизация производительности
        .fixedThreadPoolExecutorSize(32)
        // Управление схемой
        .initializeSchema(true)
        // Пользовательская стратегия пакетирования
        .batchingStrategy(new TokenCountBatchingStrategy())
        .build();
}
```

### Конфигурация подключенияСуществует два способа настроить соединение с Cassandra:

- Используя внедренный CqlSession (рекомендуется):

```java
@Bean
public VectorStore vectorStore(CqlSession session, EmbeddingModel embeddingModel) {
    return CassandraVectorStore.builder(embeddingModel)
        .session(session)
        .keyspace("my_keyspace")
        .table("my_vectors")
        .build();
}
```

- Используя детали соединения непосредственно в билдере:

```java
@Bean
public VectorStore vectorStore(EmbeddingModel embeddingModel) {
    return CassandraVectorStore.builder(embeddingModel)
        .contactPoint(new InetSocketAddress("localhost", 9042))
        .localDatacenter("datacenter1")
        .keyspace("my_keyspace")
        .build();
}
```

### Фильтрация метаданных

Вы можете использовать универсальные, переносимые фильтры метаданных с CassandraVectorStore. Чтобы метаданные столбцы были доступны для поиска, они должны быть либо первичными ключами, либо индексированными с помощью SAI. Чтобы сделать столбцы, не являющиеся первичными ключами, индексированными, настройте столбец метаданных с помощью `SchemaColumnTags.INDEXED`.

Например, вы можете использовать либо текстовый язык выражений:

```java
vectorStore.similaritySearch(
    SearchRequest.builder().query("The World")
        .topK(5)
        .filterExpression("country in ['UK', 'NL'] && year >= 2020").build());
```

либо программно, используя DSL выражений:

```java
Filter.Expression f = new FilterExpressionBuilder()
    .and(
        f.in("country", "UK", "NL"), 
        f.gte("year", 2020)
    ).build();

vectorStore.similaritySearch(
    SearchRequest.builder().query("The World")
        .topK(5)
        .filterExpression(f).build());
```

Переносимые выражения фильтров автоматически преобразуются в [CQL-запросы](https://cassandra.apache.org/doc/latest/cassandra/developing/cql/index.html).

## Расширенный пример: Векторное хранилище на основе набора данных WikipediaСледующий пример демонстрирует, как использовать хранилище на существующей схеме. Здесь мы используем схему из проекта https://github.com/datastax-labs/colbert-wikipedia-data, который поставляется с полным набором данных Википедии, готовым к векторизации.

Сначала создайте схему в базе данных Cassandra:

```bash
wget https://s.apache.org/colbert-wikipedia-schema-cql -O colbert-wikipedia-schema.cql
cqlsh -f colbert-wikipedia-schema.cql
```

Затем настройте хранилище, используя паттерн строителя:

```java
@Bean
public VectorStore vectorStore(CqlSession session, EmbeddingModel embeddingModel) {
    List<SchemaColumn> partitionColumns = List.of(
        new SchemaColumn("wiki", DataTypes.TEXT),
        new SchemaColumn("language", DataTypes.TEXT),
        new SchemaColumn("title", DataTypes.TEXT)
    );

    List<SchemaColumn> clusteringColumns = List.of(
        new SchemaColumn("chunk_no", DataTypes.INT),
        new SchemaColumn("bert_embedding_no", DataTypes.INT)
    );

    List<SchemaColumn> extraColumns = List.of(
        new SchemaColumn("revision", DataTypes.INT),
        new SchemaColumn("id", DataTypes.INT)
    );

    return CassandraVectorStore.builder()
        .session(session)
        .embeddingModel(embeddingModel)
        .keyspace("wikidata")
        .table("articles")
        .partitionKeys(partitionColumns)
        .clusteringKeys(clusteringColumns)
        .contentColumnName("body")
        .embeddingColumnName("all_minilm_l6_v2_embedding")
        .indexName("all_minilm_l6_v2_ann")
        .initializeSchema(false)
        .addMetadataColumns(extraColumns)
        .primaryKeyTranslator((List<Object> primaryKeys) -> {
            if (primaryKeys.isEmpty()) {
                return "test§¶0";
            }
            return String.format("%s§¶%s", primaryKeys.get(2), primaryKeys.get(3));
        })
        .documentIdTranslator((id) -> {
            String[] parts = id.split("§¶");
            String title = parts[0];
            int chunk_no = parts.length > 1 ? Integer.parseInt(parts[1]) : 0;
            return List.of("simplewiki", "en", title, chunk_no, 0);
        })
        .build();
}

@Bean
public EmbeddingModel embeddingModel() {
    // по умолчанию используется ONNX all-MiniLM-L6-v2, что нам и нужно
    return new TransformersEmbeddingModel();
}
```

### Загрузка полного набора данных Википедии

Чтобы загрузить полный набор данных Википедии:

1. Скачайте `simplewiki-sstable.tar` с https://s.apache.org/simplewiki-sstable-tar (это займет некоторое время, файл весит десятки гигабайт)

2. Загрузите данные:
```bash
tar -xf simplewiki-sstable.tar -C ${CASSANDRA_DATA}/data/wikidata/articles-*/
nodetool import wikidata articles ${CASSANDRA_DATA}/data/wikidata/articles-*/
```

[ПРИМЕЧАНИЕ]
====
- Если у вас уже есть данные в этой таблице, проверьте, чтобы файлы из tar-архива не перезаписывали существующие sstable при выполнении `tar`.
- Альтернативой `nodetool import` является просто перезапуск Cassandra.
- Если возникнут какие-либо ошибки в индексах, они будут автоматически восстановлены.
====

## Доступ к нативному клиенту

Реализация Cassandra Vector Store предоставляет доступ к базовому нативному клиенту Cassandra (`CqlSession`) через метод `getNativeClient()`:

```java
CassandraVectorStore vectorStore = context.getBean(CassandraVectorStore.class);
Optional<CqlSession> nativeClient = vectorStore.getNativeClient();

if (nativeClient.isPresent()) {
    CqlSession session = nativeClient.get();
    // Используйте нативный клиент для операций, специфичных для Cassandra
}
```

Нативный клиент предоставляет доступ к функциям и операциям, специфичным для Cassandra, которые могут не быть доступны через интерфейс `VectorStore`.
