# Векторные базы данных

Векторная база данных — это специализированный тип базы данных, который играет важную роль в приложениях ИИ.

В векторных базах данных запросы отличаются от традиционных реляционных баз данных. Вместо точных совпадений они выполняют поиск по сходству. При получении вектора в качестве запроса векторная база данных возвращает векторы, которые являются "`похожими`" на вектор запроса. Дополнительные сведения о том, как это сходство вычисляется на высоком уровне, приведены в xref:api/vectordbs/understand-vectordbs.adoc#vectordbs-similarity[Сходство векторов].

Векторные базы данных используются для интеграции ваших данных с моделями ИИ. Первый шаг в их использовании — загрузить ваши данные в векторную базу данных. Затем, когда пользовательский запрос отправляется в модель ИИ, сначала извлекается набор похожих документов. Эти документы затем служат контекстом для вопроса пользователя и отправляются в модель ИИ вместе с запросом пользователя. Эта техника известна как xref:concepts.adoc#concept-rag[Усиленное извлечение генерации (RAG)].

Следующие разделы описывают интерфейс Spring AI для использования нескольких реализаций векторных баз данных и некоторые примеры использования на высоком уровне.

Последний раздел предназначен для разъяснения основного подхода к поиску сходства в векторных базах данных.

## Обзор API
Этот раздел служит руководством по интерфейсу `VectorStore` и связанным с ним классам в рамках Spring AI.

Spring AI предлагает абстрагированный API для взаимодействия с векторными базами данных через интерфейс `VectorStore` и его аналог `VectorStoreRetriever`, который доступен только для чтения.

### Интерфейс VectorStoreRetriever

Spring AI предоставляет интерфейс только для чтения под названием `VectorStoreRetriever`, который открывает только функциональность извлечения документов:

```java
@FunctionalInterface
public interface VectorStoreRetriever {

    List<Document> similaritySearch(SearchRequest request);

    default List<Document> similaritySearch(String query) {
        return this.similaritySearch(SearchRequest.builder().query(query).build());
    }
}
```

Этот функциональный интерфейс предназначен для случаев, когда вам нужно только извлекать документы из векторного хранилища без выполнения каких-либо операций изменения. Он следует принципу наименьших привилегий, открывая только необходимую функциональность для извлечения документов.

### Интерфейс VectorStore

Интерфейс `VectorStore` расширяет `VectorStoreRetriever` и добавляет возможности изменения:

```java
public interface VectorStore extends DocumentWriter, VectorStoreRetriever {

    default String getName() {
		return this.getClass().getSimpleName();
	}

    void add(List<Document> documents);

    void delete(List<String> idList);

    void delete(Filter.Expression filterExpression);

    default void delete(String filterExpression) { ... }

    default <T> Optional<T> getNativeClient() {
		return Optional.empty();
	}
}
```

Интерфейс `VectorStore` сочетает в себе как операции чтения, так и записи, позволяя вам добавлять, удалять и искать документы в векторной базе данных.

### Конструктор SearchRequest```markdown
Чтобы вставить данные в векторную базу данных, заключите их в объект `Document`.
Класс `Document` инкапсулирует содержимое из источника данных, такого как PDF или Word документ, и включает текст, представленный в виде строки.
Он также содержит метаданные в виде пар ключ-значение, включая такие детали, как имя файла.

При вставке в векторную базу данных текстовое содержимое преобразуется в числовой массив, или `float[]`, известный как векторные эмбеддинги, с использованием модели эмбеддинга. Модели эмбеддинга, такие как https://en.wikipedia.org/wiki/Word2vec[Word2Vec], https://en.wikipedia.org/wiki/GloVe_(machine_learning)[GLoVE] и https://en.wikipedia.org/wiki/BERT_(language_model)[BERT], или `text-embedding-ada-002` от OpenAI, используются для преобразования слов, предложений или абзацев в эти векторные эмбеддинги.

Роль векторной базы данных заключается в хранении и облегчении поиска по сходству для этих эмбеддингов. Она не генерирует эмбеддинги сама. Для создания векторных эмбеддингов следует использовать `EmbeddingModel`.

Методы `similaritySearch` в интерфейсе позволяют извлекать документы, похожие на заданную строку запроса. Эти методы можно настроить, используя следующие параметры:

- `k`: Целое число, которое указывает максимальное количество похожих документов для возврата. Это часто называется поиском 'top K' или 'K ближайших соседей' (KNN).
- `threshold`: Дробное значение в диапазоне от 0 до 1, где значения, близкие к 1, указывают на более высокое сходство. По умолчанию, если вы установите порог 0.75, например, будут возвращены только документы с сходством выше этого значения.
- `Filter.Expression`: Класс, используемый для передачи выражения на языке, специфичном для предметной области (DSL), которое функционирует аналогично оператору 'where' в SQL, но применяется исключительно к метаданным пар ключ-значение объекта `Document`.
- `filterExpression`: Внешний DSL на основе ANTLR4, который принимает фильтрующие выражения в виде строк. Например, с метаданными, такими как страна, год и `isActive`, вы можете использовать выражение, такое как: `country == 'UK' && year >= 2020 && isActive == true.`

Дополнительную информацию о `Filter.Expression` можно найти в разделе <<metadata-filters>>.
```## Инициализация схемы

Некоторые векторные хранилища требуют инициализации своей схемы бэкенда перед использованием. По умолчанию она не будет инициализирована автоматически. Вы должны явно выбрать эту опцию, передав `boolean` в соответствующий аргумент конструктора или, если вы используете Spring Boot, установив соответствующее свойство `initialize-schema` в `true` в `application.properties` или `application.yml`. Ознакомьтесь с документацией для используемого вами векторного хранилища, чтобы узнать конкретное имя свойства.

## Стратегия пакетной обработки

При работе с векторными хранилищами часто необходимо встраивать большое количество документов. Хотя может показаться простым сделать один вызов для встраивания всех документов сразу, этот подход может привести к проблемам. Модели встраивания обрабатывают текст в виде токенов и имеют максимальный лимит токенов, часто называемый размером контекстного окна. Этот лимит ограничивает количество текста, которое может быть обработано в одном запросе на встраивание. Попытка встроить слишком много токенов за один раз может привести к ошибкам или усеченным встраиваниям.

Чтобы решить эту проблему с лимитом токенов, Spring AI реализует стратегию пакетной обработки. Этот подход разбивает большие наборы документов на меньшие пакеты, которые помещаются в максимальное контекстное окно модели встраивания. Пакетная обработка не только решает проблему лимита токенов, но также может привести к улучшению производительности и более эффективному использованию лимитов API.

Spring AI предоставляет эту функциональность через интерфейс `BatchingStrategy`, который позволяет обрабатывать документы в подпакетах на основе их количества токенов.

Основной интерфейс `BatchingStrategy` определен следующим образом:

```java
public interface BatchingStrategy {
    List<List<Document>> batch(List<Document> documents);
}
```

Этот интерфейс определяет единственный метод `batch`, который принимает список документов и возвращает список пакетов документов.

### Реализация по умолчаниюSpring AI предоставляет реализацию по умолчанию, называемую `TokenCountBatchingStrategy`. Эта стратегия группирует документы на основе их количества токенов, обеспечивая, чтобы каждая партия не превышала рассчитанное максимальное количество входных токенов.

Ключевые особенности `TokenCountBatchingStrategy`:

1. Использует https://platform.openai.com/docs/guides/embeddings/embedding-models[максимальное количество входных токенов OpenAI] (8191) в качестве верхнего предела по умолчанию.
2. Включает резервный процент (по умолчанию 10%) для создания буфера на случай потенциальных накладных расходов.
3. Вычисляет фактическое максимальное количество входных токенов как: `actualMaxInputTokenCount = originalMaxInputTokenCount * (1 - RESERVE_PERCENTAGE)`

Стратегия оценивает количество токенов для каждого документа, группирует их в партии, не превышающие максимальное количество входных токенов, и выбрасывает исключение, если один документ превышает этот лимит.

Вы также можете настроить `TokenCountBatchingStrategy`, чтобы лучше соответствовать вашим конкретным требованиям. Это можно сделать, создав новый экземпляр с пользовательскими параметрами в классе `@Configuration` Spring Boot.

Вот пример того, как создать пользовательский бин `TokenCountBatchingStrategy`:

```java
@Configuration
public class EmbeddingConfig {
    @Bean
    public BatchingStrategy customTokenCountBatchingStrategy() {
        return new TokenCountBatchingStrategy(
            EncodingType.CL100K_BASE,  // Укажите тип кодирования
            8000,                      // Установите максимальное количество входных токенов
            0.1                        // Установите резервный процент
        );
    }
}
```

В этой конфигурации:

1. `EncodingType.CL100K_BASE`: Указывает тип кодирования, используемый для токенизации. Этот тип кодирования используется `JTokkitTokenCountEstimator` для точной оценки количества токенов.
2. `8000`: Устанавливает максимальное количество входных токенов. Это значение должно быть меньше или равно максимальному размеру контекстного окна вашей модели встраивания.
3. `0.1`: Устанавливает резервный процент. Процент токенов, который нужно зарезервировать от максимального количества входных токенов. Это создает буфер для потенциального увеличения количества токенов во время обработки.

По умолчанию этот конструктор использует `Document.DEFAULT_CONTENT_FORMATTER` для форматирования содержимого и `MetadataMode.NONE` для обработки метаданных. Если вам нужно настроить эти параметры, вы можете использовать полный конструктор с дополнительными параметрами.

После определения этот пользовательский бин `TokenCountBatchingStrategy` будет автоматически использоваться реализациями `EmbeddingModel` в вашем приложении, заменяя стратегию по умолчанию.

`TokenCountBatchingStrategy` внутренне использует `TokenCountEstimator` (в частности, `JTokkitTokenCountEstimator`) для расчета количества токенов для эффективной пакетной обработки. Это обеспечивает точную оценку токенов на основе указанного типа кодирования.

Кроме того, `TokenCountBatchingStrategy` предоставляет гибкость, позволяя вам передавать свою собственную реализацию интерфейса `TokenCountEstimator`. Эта функция позволяет использовать пользовательские стратегии подсчета токенов, адаптированные к вашим конкретным потребностям. Например:

```java
TokenCountEstimator customEstimator = new YourCustomTokenCountEstimator();
TokenCountBatchingStrategy strategy = new TokenCountBatchingStrategy(
	 this.customEstimator,
    8000,  // maxInputTokenCount
    0.1,   // reservePercentage
    Document.DEFAULT_CONTENT_FORMATTER,
    MetadataMode.NONE
);
```

### Работа с автоусечением

Некоторые модели встраивания, такие как текстовое встраивание Vertex AI, поддерживают функцию `auto_truncate`. При включении модель тихо усечет текстовые входы, которые превышают максимальный размер, и продолжит обработку; при отключении она выбрасывает явную ошибку для слишком больших входов.

При использовании автоусечения с пакетной стратегией вы должны настроить свою пакетную стратегию с гораздо более высоким количеством входных токенов, чем фактический максимум модели. Это предотвращает возникновение исключений в пакетной стратегии для больших документов, позволяя модели встраивания обрабатывать усечение внутренне.

#### Конфигурация для автоусеченияКогда вы включаете автоматическое усечение, установите максимальное количество входных токенов в вашей стратегии пакетирования значительно выше фактического лимита модели. Это предотвращает возникновение исключений в стратегии пакетирования для больших документов, позволяя модели встраивания обрабатывать усечение внутренне.

Вот пример конфигурации для использования Vertex AI с автоматическим усечением и пользовательской `BatchingStrategy`, а затем использования их в PgVectorStore:

```java
@Configuration
public class AutoTruncationEmbeddingConfig {

    @Bean
    public VertexAiTextEmbeddingModel vertexAiEmbeddingModel(
            VertexAiEmbeddingConnectionDetails connectionDetails) {

        VertexAiTextEmbeddingOptions options = VertexAiTextEmbeddingOptions.builder()
                .model(VertexAiTextEmbeddingOptions.DEFAULT_MODEL_NAME)
                .autoTruncate(true)  // Включить автоматическое усечение
                .build();

        return new VertexAiTextEmbeddingModel(connectionDetails, options);
    }

    @Bean
    public BatchingStrategy batchingStrategy() {
        // Используйте высокий лимит токенов только если автоматическое усечение включено в вашей модели встраивания.
        // Установите значительно более высокое количество токенов, чем фактически поддерживает модель
        // (например, 132900, когда Vertex AI поддерживает только до 20000)
        return new TokenCountBatchingStrategy(
                EncodingType.CL100K_BASE,
                132900,  // Искусственно высокий лимит
                0.1      // 10% резерв
        );
    }

    @Bean
    public VectorStore vectorStore(JdbcTemplate jdbcTemplate, EmbeddingModel embeddingModel, BatchingStrategy batchingStrategy) {
        return PgVectorStore.builder(jdbcTemplate, embeddingModel)
            // другие свойства опущены здесь
            .build();
    }
}
```

В этой конфигурации:

1. Модель встраивания имеет включенное автоматическое усечение, что позволяет ей обрабатывать слишком большие входные данные корректно.
2. Стратегия пакетирования использует искусственно высокий лимит токенов (132900), который значительно больше фактического лимита модели (20000).
3. Векторный магазин использует настроенную модель встраивания и пользовательский бин `BatchingStrategy`.

#### Почему это работает

Этот подход работает, потому что:

1. `TokenCountBatchingStrategy` проверяет, превышает ли какой-либо отдельный документ установленный максимум, и выбрасывает `IllegalArgumentException`, если это так.
2. Установив очень высокий лимит в стратегии пакетирования, мы гарантируем, что эта проверка никогда не провалится.
3. Документы или пакеты, превышающие лимит модели, тихо усечены и обрабатываются функцией автоматического усечения модели встраивания.

#### Рекомендации

При использовании автоматического усечения:

- Установите максимальное количество входных токенов в стратегии пакетирования как минимум в 5-10 раз больше фактического лимита модели, чтобы избежать преждевременных исключений от стратегии пакетирования.
- Следите за своими логами на предмет предупреждений об усечении от модели встраивания (обратите внимание: не все модели регистрируют события усечения).
- Учитывайте последствия тихого усечения для качества ваших встраиваний.
- Тестируйте с образцами документов, чтобы убедиться, что усеченные встраивания все еще соответствуют вашим требованиям.
- Документируйте эту конфигурацию для будущих поддерживающих, так как она нестандартная.

> **Осторожно:** Хотя автоматическое усечение предотвращает ошибки, оно может привести к неполным встраиваниям. Важная информация в конце длинных документов может быть утеряна. Если ваше приложение требует, чтобы весь контент был встроен, разбивайте документы на более мелкие части перед встраиванием.

#### Авто-конфигурация Spring BootЕсли вы используете автонастройку Spring Boot, вам необходимо предоставить собственный бин `BatchingStrategy`, чтобы переопределить значение по умолчанию, которое поставляется с Spring AI:

```java
@Bean
public BatchingStrategy customBatchingStrategy() {
    // Этот бин переопределит стандартный BatchingStrategy
    return new TokenCountBatchingStrategy(
            EncodingType.CL100K_BASE,
            132900,  // Значение значительно выше фактического лимита модели
            0.1
    );
}
```

Наличие этого бина в вашем контексте приложения автоматически заменит стандартную стратегию пакетирования, используемую всеми векторными хранилищами.

### Пользовательская реализация

Хотя `TokenCountBatchingStrategy` предоставляет надежную реализацию по умолчанию, вы можете настроить стратегию пакетирования в соответствии с вашими конкретными потребностями. Это можно сделать с помощью автонастройки Spring Boot.

Чтобы настроить стратегию пакетирования, определите бин `BatchingStrategy` в вашем приложении Spring Boot:

```java
@Configuration
public class EmbeddingConfig {
    @Bean
    public BatchingStrategy customBatchingStrategy() {
        return new CustomBatchingStrategy();
    }
}
```

Эта пользовательская `BatchingStrategy` будет автоматически использоваться реализациями `EmbeddingModel` в вашем приложении.

> **Примечание:** Векторные хранилища, поддерживаемые Spring AI, настроены на использование стандартного `TokenCountBatchingStrategy`. Векторное хранилище SAP Hana в настоящее время не настроено для пакетирования.

## Реализации VectorStoreЭти реализации интерфейса `VectorStore` доступны:

- xref:api/vectordbs/azure.adoc[Azure Vector Search] - Векторное хранилище https://learn.microsoft.com/en-us/azure/search/vector-search-overview[Azure].
- xref:api/vectordbs/apache-cassandra.adoc[Apache Cassandra] - Векторное хранилище https://cassandra.apache.org/doc/latest/cassandra/vector-search/overview.html[Apache Cassandra].
- xref:api/vectordbs/chroma.adoc[Chroma Vector Store] - Векторное хранилище https://www.trychroma.com/[Chroma].
- xref:api/vectordbs/elasticsearch.adoc[Elasticsearch Vector Store] - Векторное хранилище https://www.elastic.co/[Elasticsearch].
- xref:api/vectordbs/gemfire.adoc[GemFire Vector Store] - Векторное хранилище https://tanzu.vmware.com/content/blog/vmware-gemfire-vector-database-extension[GemFire].
- xref:api/vectordbs/mariadb.adoc[MariaDB Vector Store] - Векторное хранилище https://mariadb.com/[MariaDB].
- xref:api/vectordbs/milvus.adoc[Milvus Vector Store] - Векторное хранилище https://milvus.io/[Milvus].
- xref:api/vectordbs/mongodb.adoc[MongoDB Atlas Vector Store] - Векторное хранилище https://www.mongodb.com/atlas/database[MongoDB Atlas].
- xref:api/vectordbs/neo4j.adoc[Neo4j Vector Store] - Векторное хранилище https://neo4j.com/[Neo4j].
- xref:api/vectordbs/opensearch.adoc[OpenSearch Vector Store] - Векторное хранилище https://opensearch.org/platform/search/vector-database.html[OpenSearch].
- xref:api/vectordbs/oracle.adoc[Oracle Vector Store] - Векторное хранилище https://docs.oracle.com/en/database/oracle/oracle-database/23/vecse/overview-ai-vector-search.html[Oracle Database].
- xref:api/vectordbs/pgvector.adoc[PgVector Store] - Векторное хранилище https://github.com/pgvector/pgvector[PostgreSQL/PGVector].
- xref:api/vectordbs/pinecone.adoc[Pinecone Vector Store] - Векторное хранилище https://www.pinecone.io/[Pinecone].
- xref:api/vectordbs/qdrant.adoc[Qdrant Vector Store] - Векторное хранилище https://www.qdrant.tech/[Qdrant].
- xref:api/vectordbs/redis.adoc[Redis Vector Store] - Векторное хранилище https://redis.io/[Redis].
- xref:api/vectordbs/hana.adoc[SAP Hana Vector Store] - Векторное хранилище https://news.sap.com/2024/04/sap-hana-cloud-vector-engine-ai-with-business-context/[SAP HANA].
- xref:api/vectordbs/typesense.adoc[Typesense Vector Store] - Векторное хранилище https://typesense.org/docs/0.24.0/api/vector-search.html[Typesense].
- xref:api/vectordbs/weaviate.adoc[Weaviate Vector Store] - Векторное хранилище https://weaviate.io/[Weaviate].
- xref:api/vectordbs/s3-vector-store.adoc[S3 Vector Store] - Векторное хранилище https://aws.amazon.com/s3/features/vectors/[AWS S3].
- [SimpleVectorStore](https://github.com/spring-projects/spring-ai/blob/main/spring-ai-vector-store/src/main/java/org/springframework/ai/vectorstore/SimpleVectorStore.java) - Простая реализация постоянного векторного хранилища, подходящая для образовательных целей.

В будущих релизах могут быть поддержаны дополнительные реализации.

Если у вас есть векторная база данных, которую необходимо поддерживать в Spring AI, откройте проблему на GitHub или, что еще лучше, отправьте запрос на внесение изменений с реализацией.

Информация о каждой из реализаций `VectorStore` доступна в подразделах этой главы.

## Пример использования

Чтобы вычислить встраивания для векторной базы данных, вам нужно выбрать модель встраивания, которая соответствует более высокоуровневой модели ИИ, используемой в данный момент.

Например, с ChatGPT от OpenAI мы используем `OpenAiEmbeddingModel` и модель с именем `text-embedding-ada-002`.

Автонастройка стартеров Spring Boot для OpenAI делает реализацию `EmbeddingModel` доступной в контексте приложения Spring для внедрения зависимостей.

### Запись в векторное хранилище```markdown
Общее использование загрузки данных в векторное хранилище — это то, что вы делаете в пакетной задаче, сначала загружая данные в класс `Document` от Spring AI, а затем вызывая метод `add` интерфейса `VectorStore`.

Учитывая ссылку `String` на исходный файл, представляющий JSON-файл с данными, которые мы хотим загрузить в векторную базу данных, мы используем `JsonReader` от Spring AI для загрузки конкретных полей в JSON, который разбивает их на небольшие части и затем передает эти небольшие части в реализацию векторного хранилища. Реализация `VectorStore` вычисляет встраивания и хранит JSON и встраивание в векторной базе данных:

```java
@Autowired
VectorStore vectorStore;

void load(String sourceFile) {
    JsonReader jsonReader = new JsonReader(new FileSystemResource(sourceFile),
            "price", "name", "shortDescription", "description", "tags");
    List<Document> documents = jsonReader.get();
    this.vectorStore.add(documents);
}
```

### Чтение из векторного хранилища

Позже, когда вопрос пользователя передается в модель ИИ, выполняется поиск по сходству для извлечения похожих документов, которые затем "встраиваются" в подсказку как контекст для вопроса пользователя.

Для операций только для чтения вы можете использовать либо интерфейс `VectorStore`, либо более специализированный интерфейс `VectorStoreRetriever`:

```java
@Autowired
VectorStoreRetriever retriever; // Также можно использовать VectorStore здесь

String question = "<вопрос от пользователя>";
List<Document> similarDocuments = retriever.similaritySearch(question);

// Или с более специфическими параметрами поиска
SearchRequest request = SearchRequest.builder()
    .query(question)
    .topK(5)                       // Вернуть 5 лучших результатов
    .similarityThreshold(0.7)      // Возвращать только результаты с оценкой сходства >= 0.7
    .build();

List<Document> filteredDocuments = retriever.similaritySearch(request);
```

Дополнительные параметры могут быть переданы в метод `similaritySearch`, чтобы определить, сколько документов извлекать и порог для поиска по сходству.

### Разделение операций чтения и записи

Использование отдельных интерфейсов позволяет четко определить, какие компоненты нуждаются в доступе на запись, а какие только на чтение:

```java
// Операции записи в сервисе, который нуждается в полном доступе
@Service
class DocumentIndexer {
    private final VectorStore vectorStore;

    DocumentIndexer(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }

    public void indexDocuments(List<Document> documents) {
        vectorStore.add(documents);
    }
}

// Операции только для чтения в сервисе, который нуждается только в извлечении
@Service
class DocumentRetriever {
    private final VectorStoreRetriever retriever;

    DocumentRetriever(VectorStoreRetriever retriever) {
        this.retriever = retriever;
    }

    public List<Document> findSimilar(String query) {
        return retriever.similaritySearch(query);
    }
}
```

Это разделение обязанностей помогает создавать более поддерживаемые и безопасные приложения, ограничивая доступ к операциям изменения только для компонентов, которые действительно в них нуждаются.

## Операции извлечения с VectorStoreRetriever

Интерфейс `VectorStoreRetriever` предоставляет только для чтения представление векторного хранилища, открывая только функциональность поиска по сходству. Это соответствует принципу наименьших привилегий и особенно полезно в приложениях RAG (Retrieval-Augmented Generation), где вам нужно только извлекать документы, не изменяя исходные данные.

### Преимущества использования VectorStoreRetriever

1. **Разделение обязанностей**: Четко разделяет операции чтения и записи.
2. **Сегрегация интерфейсов**: Клиенты, которым нужна только функциональность извлечения, не подвергаются воздействию методов изменения.
3. **Функциональный интерфейс**: Может быть реализован с помощью лямбда-выражений или ссылок на методы для простых случаев использования.
4. **Сниженные зависимости**: Компоненты, которым нужно только выполнять поиски, не должны зависеть от полного интерфейса `VectorStore`.

### Пример использования
``````markdown
Вы можете использовать `VectorStoreRetriever` напрямую, когда вам нужно только выполнять поиск по сходству:

```java
@Service
public class DocumentRetrievalService {

    private final VectorStoreRetriever retriever;

    public DocumentRetrievalService(VectorStoreRetriever retriever) {
        this.retriever = retriever;
    }

    public List<Document> findSimilarDocuments(String query) {
        return retriever.similaritySearch(query);
    }

    public List<Document> findSimilarDocumentsWithFilters(String query, String country) {
        SearchRequest request = SearchRequest.builder()
            .query(query)
            .topK(5)
            .filterExpression("country == '" + country + "'")
            .build();

        return retriever.similaritySearch(request);
    }
}
```

В этом примере сервис зависит только от интерфейса `VectorStoreRetriever`, что делает очевидным, что он выполняет только операции извлечения и не изменяет векторное хранилище.

### Интеграция с RAG-приложениями

Интерфейс `VectorStoreRetriever` особенно полезен в RAG-приложениях, где необходимо извлекать релевантные документы для предоставления контекста для модели ИИ:

```java
@Service
public class RagService {

    private final VectorStoreRetriever retriever;
    private final ChatModel chatModel;

    public RagService(VectorStoreRetriever retriever, ChatModel chatModel) {
        this.retriever = retriever;
        this.chatModel = chatModel;
    }

    public String generateResponse(String userQuery) {
        // Извлечение релевантных документов
        List<Document> relevantDocs = retriever.similaritySearch(userQuery);

        // Извлечение содержимого из документов для использования в качестве контекста
        String context = relevantDocs.stream()
            .map(Document::getContent)
            .collect(Collectors.joining("\n\n"));

        // Генерация ответа с использованием извлеченного контекста
        String prompt = "Информация о контексте:\n" + context + "\n\nЗапрос пользователя: " + userQuery;
        return chatModel.generate(prompt);
    }
}
```

Эта схема позволяет четко разделить компонент извлечения и компонент генерации в RAG-приложениях.


В этом разделе описываются различные фильтры, которые вы можете использовать для обработки результатов запроса.

### Строка фильтра
Вы можете передать SQL-подобные выражения фильтра в виде `String` в один из перегруженных методов `similaritySearch`.

Рассмотрим следующие примеры:

- `"country == 'BG'"`
- `"genre == 'drama' && year >= 2020"`
- `"genre in ['comedy', 'documentary', 'drama']"`

### Filter.Expression

Вы можете создать экземпляр `Filter.Expression` с помощью `FilterExpressionBuilder`, который предоставляет удобный API.
Простой пример выглядит следующим образом:

```java
FilterExpressionBuilder b = new FilterExpressionBuilder();
Expression expression = this.b.eq("country", "BG").build();
```

Вы можете создавать сложные выражения, используя следующие операторы:

```text
EQUALS: '=='
MINUS : '-'
PLUS: '+'
GT: '>'
GE: '>='
LT: '<'
LE: '<='
NE: '!='
```

Вы можете комбинировать выражения, используя следующие операторы:

```text
AND: 'AND' | 'and' | '&&';
OR: 'OR' | 'or' | '||';
```

Рассмотрим следующий пример:

```java
Expression exp = b.and(b.eq("genre", "drama"), b.gte("year", 2020)).build();
```

Вы также можете использовать следующие операторы:

```text
IN: 'IN' | 'in';
NIN: 'NIN' | 'nin';
NOT: 'NOT' | 'not';
```

Рассмотрим следующий пример:

```java
Expression exp = b.and(b.in("genre", "drama", "documentary"), b.not(b.lt("year", 2020))).build();
```

Вы также можете использовать следующие операторы:

```text
IS: 'IS' | 'is';
NULL: 'NULL' | 'null';
NOT NULL: 'NOT NULL' | 'not null';
```

Рассмотрим следующие примеры:

```java
Expression exp = b.and(b.isNull("year")).build();
Expression exp = b.and(b.isNotNull("year")).build();
```

> **Примечание:** `IS NULL` и `IS NOT NULL` еще не реализованы во всех векторных хранилищах.

## Удаление документов из векторного хранилища
```Интерфейс Vector Store предоставляет несколько методов для удаления документов, позволяя вам удалять данные как по конкретным идентификаторам документов, так и с использованием фильтров.

### Удаление по идентификаторам документов

Самый простой способ удалить документы — предоставить список идентификаторов документов:

```java
void delete(List<String> idList);
```

Этот метод удаляет все документы, идентификаторы которых совпадают с теми, что в предоставленном списке. Если какой-либо идентификатор из списка не существует в хранилище, он будет проигнорирован.

.Пример использования
```java
// Создание и добавление документа
Document document = new Document("Мир большой",
    Map.of("country", "Нидерланды"));
vectorStore.add(List.of(document));

// Удаление документа по идентификатору
vectorStore.delete(List.of(document.getId()));
```

### Удаление по фильтру

Для более сложных критериев удаления вы можете использовать фильтры:

```java
void delete(Filter.Expression filterExpression);
```

Этот метод принимает объект `Filter.Expression`, который определяет критерии, по которым должны быть удалены документы. Это особенно полезно, когда вам нужно удалить документы на основе их метаданных.

.Пример использования
```java
// Создание тестовых документов с разными метаданными
Document bgDocument = new Document("Мир большой",
    Map.of("country", "Болгария"));
Document nlDocument = new Document("Мир большой",
    Map.of("country", "Нидерланды"));

// Добавление документов в хранилище
vectorStore.add(List.of(bgDocument, nlDocument));

// Удаление документов из Болгарии с использованием фильтра
Filter.Expression filterExpression = new Filter.Expression(
    Filter.ExpressionType.EQ,
    new Filter.Key("country"),
    new Filter.Value("Болгария")
);
vectorStore.delete(filterExpression);

// Проверка удаления с помощью поиска
SearchRequest request = SearchRequest.builder()
    .query("Мир")
    .filterExpression("country == 'Болгария'")
    .build();
List<Document> results = vectorStore.similaritySearch(request);
// результаты будут пустыми, так как болгарский документ был удален
```

### Удаление по строковому фильтру

Для удобства вы также можете удалить документы, используя строковый фильтр:

```java
void delete(String filterExpression);
```

Этот метод внутренне преобразует предоставленный строковый фильтр в объект `Filter.Expression`. Это полезно, когда у вас есть критерии фильтра в строковом формате.

.Пример использования
```java
// Создание и добавление документов
Document bgDocument = new Document("Мир большой",
    Map.of("country", "Болгария"));
Document nlDocument = new Document("Мир большой",
    Map.of("country", "Нидерланды"));
vectorStore.add(List.of(bgDocument, nlDocument));

// Удаление болгарских документов с использованием строкового фильтра
vectorStore.delete("country == 'Болгария'");

// Проверка оставшихся документов
SearchRequest request = SearchRequest.builder()
    .query("Мир")
    .topK(5)
    .build();
List<Document> results = vectorStore.similaritySearch(request);
// результаты будут содержать только документ из Нидерландов
```

### Обработка ошибок при вызове API удаления

Все методы удаления могут вызывать исключения в случае ошибок:

Лучшей практикой является обертывание операций удаления в блоки try-catch:

.Пример использования
```java
try {
    vectorStore.delete("country == 'Болгария'");
}
catch (Exception  e) {
    logger.error("Неверное выражение фильтра", e);
}
```

### Случай использования версионирования документовОбщая ситуация заключается в управлении версиями документов, когда вам необходимо загрузить новую версию документа, удалив при этом старую версию. Вот как это сделать с помощью фильтров:

.Пример использования
```java
// Создание начального документа (v1) с метаданными версии
Document documentV1 = new Document(
    "Лучшие практики в области ИИ и машинного обучения",
    Map.of(
        "docId", "AIML-001",
        "version", "1.0",
        "lastUpdated", "2024-01-01"
    )
);

// Добавление v1 в векторное хранилище
vectorStore.add(List.of(documentV1));

// Создание обновленной версии (v2) того же документа
Document documentV2 = new Document(
    "Лучшие практики в области ИИ и машинного обучения - обновлено",
    Map.of(
        "docId", "AIML-001",
        "version", "2.0",
        "lastUpdated", "2024-02-01"
    )
);

// Сначала удалите старую версию, используя фильтр
Filter.Expression deleteOldVersion = new Filter.Expression(
    Filter.ExpressionType.AND,
    new Filter.Expression(
        Filter.ExpressionType.EQ,
        new Filter.Key("docId"),
        new Filter.Value("AIML-001")
    ),
    new Filter.Expression(
        Filter.ExpressionType.EQ,
        new Filter.Key("version"),
        new Filter.Value("1.0")
    )
);
vectorStore.delete(deleteOldVersion);

// Добавьте новую версию
vectorStore.add(List.of(documentV2));

// Убедитесь, что существует только v2
SearchRequest request = SearchRequest.builder()
    .query("ИИ и машинное обучение")
    .filterExpression("docId == 'AIML-001'")
    .build();
List<Document> results = vectorStore.similaritySearch(request);
// results будет содержать только v2 документа
```

Вы также можете достичь того же результата, используя строковый фильтр:

.Пример использования
```java
// Удаление старой версии с использованием строкового фильтра
vectorStore.delete("docId == 'AIML-001' AND version == '1.0'");

// Добавление новой версии
vectorStore.add(List.of(documentV2));
```

### Учет производительности при удалении документов

- Удаление по списку ID, как правило, быстрее, когда вы точно знаете, какие документы нужно удалить.
- Удаление на основе фильтров может потребовать сканирования индекса для поиска соответствующих документов; однако это зависит от реализации векторного хранилища.
- Большие операции удаления следует группировать, чтобы избежать перегрузки системы.
- Рассмотрите возможность использования фильтров при удалении на основе свойств документа, а не собирая ID сначала.

## Понимание векторов

xref:api/vectordbs/understand-vectordbs.adoc[Понимание векторов]
