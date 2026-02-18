# Redis

В этом разделе описывается, как настроить `RedisVectorStore` для хранения векторных представлений документов и выполнения поиска по сходству.

[Redis](https://redis.io) — это система хранения данных с открытым исходным кодом (лицензия BSD), работающая в памяти, используемая в качестве базы данных, кэша, брокера сообщений и движка потоковой передачи. Redis предоставляет такие структуры данных, как строки, хэши, списки, множества, отсортированные множества с диапазонными запросами, битовые карты, гиперлоглоги, геопространственные индексы и потоки.

[Redis Search and Query](https://redis.io/docs/interact/search-and-query/) расширяет основные функции Redis OSS и позволяет использовать Redis в качестве векторной базы данных:

- Хранение векторов и связанной метаданных в хэшах или JSON-документах
- Извлечение векторов
- Выполнение поиска по сходству векторов (KNN)
- Выполнение поиска векторов на основе диапазона с порогом радиуса
- Выполнение полнотекстового поиска по полям TEXT
- Поддержка нескольких метрик расстояния (COSINE, L2, IP) и векторных алгоритмов (HNSW, FLAT)

## Предварительные требования

1. Экземпляр Redis Stack
- https://app.redislabs.com/#/[Redis Cloud] (рекомендуется)
- [Docker](https://hub.docker.com/r/redis/redis-stack) образ _redis/redis-stack:latest_

2. Экземпляр `EmbeddingModel` для вычисления векторных представлений документов. Доступно несколько вариантов:
- При необходимости, API-ключ для xref:api/embeddings.adoc#available-implementations[EmbeddingModel] для генерации векторных представлений, хранящихся в `RedisVectorStore`.

## Автонастройка

[ПРИМЕЧАНИЕ]
====
В автонастройке Spring AI произошли значительные изменения, касающиеся имен артефактов стартовых модулей.
Пожалуйста, обратитесь к https://docs.spring.io/spring-ai/reference/upgrade-notes.html[заметкам об обновлении] для получения дополнительной информации.
====

Spring AI предоставляет автонастройку Spring Boot для Redis Vector Store.
Чтобы включить ее, добавьте следующую зависимость в файл `pom.xml` вашего проекта Maven:

```xml
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-starter-vector-store-redis</artifactId>
</dependency>
```

или в файл сборки Gradle `build.gradle`.

```groovy
dependencies {
    implementation 'org.springframework.ai:spring-ai-starter-vector-store-redis'
}
```

> **Совет:** Обратитесь к разделу xref:getting-started.adoc#dependency-management[Управление зависимостями], чтобы добавить BOM Spring AI в ваш файл сборки.

> **Совет:** Обратитесь к разделу xref:getting-started.adoc#artifact-repositories[Артефактные репозитории], чтобы добавить Maven Central и/или Snapshot репозитории в ваш файл сборки.

Реализация векторного хранилища может инициализировать необходимую схему для вас, но вы должны согласиться, указав булевый параметр `initializeSchema` в соответствующем конструкторе или установив `...initialize-schema=true` в файле `application.properties`.

> **Примечание:** это является изменением, которое нарушает совместимость! В предыдущих версиях Spring AI инициализация схемы происходила по умолчанию.

Пожалуйста, ознакомьтесь со списком [параметров конфигурации](#redisvector-properties), чтобы узнать о значениях по умолчанию и параметрах конфигурации для векторного хранилища.

Кроме того, вам потребуется настроенный бин `EmbeddingModel`. Обратитесь к разделу xref:api/embeddings.adoc#available-implementations[EmbeddingModel] для получения дополнительной информации.

Теперь вы можете автоматически подключить `RedisVectorStore` в качестве векторного хранилища в вашем приложении.

```java
@Autowired VectorStore vectorStore;

// ...

List <Document> documents = List.of(
    new Document("Spring AI rocks!! Spring AI rocks!! Spring AI rocks!! Spring AI rocks!! Spring AI rocks!!", Map.of("meta1", "meta1")),
    new Document("The World is Big and Salvation Lurks Around the Corner"),
    new Document("You walk forward facing the past and you turn back toward the future.", Map.of("meta2", "meta2")));

// Добавьте документы в Redis
vectorStore.add(documents);

// Извлеките документы, похожие на запрос
List<Document> results = this.vectorStore.similaritySearch(SearchRequest.builder().query("Spring").topK(5).build());
```

[[redisvector-properties]]
### Параметры конфигурацииЧтобы подключиться к Redis и использовать `RedisVectorStore`, вам необходимо предоставить данные доступа к вашему экземпляру. Простую конфигурацию можно задать через `application.yml` Spring Boot:

```yaml
spring:
  data:
    redis:
      url: <url экземпляра redis>
  ai:
    vectorstore:
      redis:
        initialize-schema: true
        index-name: custom-index
        prefix: custom-prefix
```

Для конфигурации подключения к redis, альтернативно, простую конфигурацию можно задать через _application.properties_ Spring Boot.

```properties
spring.data.redis.host=localhost
spring.data.redis.port=6379
spring.data.redis.username=default
spring.data.redis.password=
```

Свойства, начинающиеся с `spring.ai.vectorstore.redis.*`, используются для настройки `RedisVectorStore`:

[cols="2,5,1",stripes=even]
| Свойство | Описание | Значение по умолчанию |
| --- | --- | --- |
| `spring.ai.vectorstore.redis.initialize-schema` | Нужно ли инициализировать требуемую схему | `false` |
| `spring.ai.vectorstore.redis.index-name` | Имя индекса для хранения векторов | `spring-ai-index` |
| `spring.ai.vectorstore.redis.prefix` | Префикс для ключей Redis | `embedding:` |
| `spring.ai.vectorstore.redis.distance-metric` | Метрика расстояния для схожести векторов (COSINE, L2, IP) | `COSINE` |
| `spring.ai.vectorstore.redis.vector-algorithm` | Алгоритм индексации векторов (HNSW, FLAT) | `HNSW` |
| `spring.ai.vectorstore.redis.hnsw-m` | HNSW: Максимальное количество исходящих соединений | `16` |
| `spring.ai.vectorstore.redis.hnsw-ef-construction` | HNSW: Максимальное количество соединений во время построения индекса | `200` |
| `spring.ai.vectorstore.redis.hnsw-ef-runtime` | HNSW: Количество соединений, которые следует учитывать во время поиска | `10` |
| `spring.ai.vectorstore.redis.default-range-threshold` | Порог радиуса по умолчанию для диапазонных запросов | `0.8` |
| `spring.ai.vectorstore.redis.text-scorer` | Алгоритм оценки текста (BM25, TFIDF, BM25STD, DISMAX, DOCSCORE) | `BM25` |

## Фильтрация метаданных

Вы также можете использовать универсальные, переносимые xref:api/vectordbs.adoc#metadata-filters[фильтры метаданных] с Redis.

Например, вы можете использовать либо текстовый язык выражений:

```java
vectorStore.similaritySearch(SearchRequest.builder()
        .query("The World")
        .topK(TOP_K)
        .similarityThreshold(SIMILARITY_THRESHOLD)
        .filterExpression("country in ['UK', 'NL'] && year >= 2020").build());
```

либо программно, используя DSL `Filter.Expression`:

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

> **Примечание:** Эти (переносимые) выражения фильтров автоматически преобразуются в [запросы поиска Redis](https://redis.io/docs/interact/search-and-query/query/).

Например, это переносимое выражение фильтра:

```sql
country in ['UK', 'NL'] && year >= 2020
```

преобразуется в проприетарный формат фильтра Redis:

```text
@country:{UK | NL} @year:[2020 inf]
```

## Ручная конфигурацияВместо использования автонастройки Spring Boot вы можете вручную настроить хранилище векторов Redis. Для этого вам нужно добавить `spring-ai-redis-store` в ваш проект:

```xml
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-redis-store</artifactId>
</dependency>
```

или в ваш файл сборки Gradle `build.gradle`.

```groovy
dependencies {
    implementation 'org.springframework.ai:spring-ai-redis-store'
}
```

Создайте бин `JedisPooled`:

```java
@Bean
public JedisPooled jedisPooled() {
    return new JedisPooled("<host>", 6379);
}
```

Затем создайте бин `RedisVectorStore`, используя паттерн строителя:

```java
@Bean
public VectorStore vectorStore(JedisPooled jedisPooled, EmbeddingModel embeddingModel) {
    return RedisVectorStore.builder(jedisPooled, embeddingModel)
        .indexName("custom-index")                // Необязательно: по умолчанию "spring-ai-index"
        .prefix("custom-prefix")                  // Необязательно: по умолчанию "embedding:"
        .contentFieldName("content")              // Необязательно: поле для содержимого документа
        .embeddingFieldName("embedding")          // Необязательно: поле для векторных вложений
        .vectorAlgorithm(Algorithm.HNSW)          // Необязательно: HNSW или FLAT (по умолчанию HNSW)
        .distanceMetric(DistanceMetric.COSINE)    // Необязательно: COSINE, L2 или IP (по умолчанию COSINE)
        .hnswM(16)                                // Необязательно: соединения HNSW (по умолчанию 16)
        .hnswEfConstruction(200)                  // Необязательно: параметр сборки HNSW (по умолчанию 200)
        .hnswEfRuntime(10)                        // Необязательно: параметр поиска HNSW (по умолчанию 10)
        .defaultRangeThreshold(0.8)               // Необязательно: радиус по умолчанию для диапазонных запросов
        .textScorer(TextScorer.BM25)              // Необязательно: алгоритм оценки текста (по умолчанию BM25)
        .metadataFields(                          // Необязательно: определите поля метаданных для фильтрации
            MetadataField.tag("country"),
            MetadataField.numeric("year"),
            MetadataField.text("description"))
        .initializeSchema(true)                   // Необязательно: по умолчанию false
        .batchingStrategy(new TokenCountBatchingStrategy()) // Необязательно: по умолчанию TokenCountBatchingStrategy
        .build();
}

// Это может быть любая реализация EmbeddingModel
@Bean
public EmbeddingModel embeddingModel() {
    return new OpenAiEmbeddingModel(new OpenAiApi(System.getenv("OPENAI_API_KEY")));
}
```

[ПРИМЕЧАНИЕ]
====
Вы должны явно указать все имена и типы полей метаданных (`TAG`, `TEXT` или `NUMERIC`) для любых полей метаданных, используемых в выражениях фильтрации.
Параметр `metadataFields` выше регистрирует фильтруемые поля метаданных: `country` типа `TAG`, `year` типа `NUMERIC`.
====

## Доступ к нативному клиенту

Реализация хранилища векторов Redis предоставляет доступ к базовому нативному клиенту Redis (`JedisPooled`) через метод `getNativeClient()`:

```java
RedisVectorStore vectorStore = context.getBean(RedisVectorStore.class);
Optional<JedisPooled> nativeClient = vectorStore.getNativeClient();

if (nativeClient.isPresent()) {
    JedisPooled jedis = nativeClient.get();
    // Используйте нативный клиент для операций, специфичных для Redis
}
```

Нативный клиент предоставляет вам доступ к функциям и операциям, специфичным для Redis, которые могут не быть доступны через интерфейс `VectorStore`.

## Метрики расстоянияThe Redis Vector Store поддерживает три метрики расстояния для оценки схожести векторов:

- **COSINE**: Косинусное сходство (по умолчанию) - измеряет косинус угла между векторами
- **L2**: Евклидово расстояние - измеряет прямое расстояние между векторами
- **IP**: Внутреннее произведение - измеряет скалярное произведение между векторами

Каждая метрика автоматически нормализуется до оценки схожести от 0 до 1, где 1 - это наибольшая схожесть.

```java
RedisVectorStore vectorStore = RedisVectorStore.builder(jedisPooled, embeddingModel)
    .distanceMetric(DistanceMetric.COSINE)  // или L2, IP
    .build();
```

## Конфигурация алгоритма HNSW

Redis Vector Store по умолчанию использует алгоритм HNSW (Hierarchical Navigable Small World) для эффективного поиска приблизительных ближайших соседей. Вы можете настроить параметры HNSW для вашего конкретного случая использования:

```java
RedisVectorStore vectorStore = RedisVectorStore.builder(jedisPooled, embeddingModel)
    .vectorAlgorithm(Algorithm.HNSW)
    .hnswM(32)                    // Максимальное количество исходящих соединений на узел (по умолчанию: 16)
    .hnswEfConstruction(100)      // Соединения во время построения индекса (по умолчанию: 200)
    .hnswEfRuntime(50)            // Соединения во время поиска (по умолчанию: 10)
    .build();
```

Руководство по параметрам:

- **M**: Более высокие значения улучшают полноту, но увеличивают использование памяти и время индексации. Типичные значения: 12-48.
- **EF_CONSTRUCTION**: Более высокие значения улучшают качество индекса, но увеличивают время построения. Типичные значения: 100-500.
- **EF_RUNTIME**: Более высокие значения улучшают точность поиска, но увеличивают задержку. Типичные значения: 10-100.

Для небольших наборов данных или когда требуются точные результаты, используйте алгоритм FLAT:

```java
RedisVectorStore vectorStore = RedisVectorStore.builder(jedisPooled, embeddingModel)
    .vectorAlgorithm(Algorithm.FLAT)
    .build();
```

## Поиск по тексту

Redis Vector Store предоставляет возможности текстового поиска с использованием функций полнотекстового поиска Redis Query Engine. Это позволяет находить документы на основе ключевых слов и фраз в полях TEXT:

```java
// Поиск документов, содержащих определенный текст
List<Document> textResults = vectorStore.searchByText(
    "machine learning",   // поисковой запрос
    "content",            // поле для поиска (должно быть типа TEXT)
    10,                   // лимит
    "category == 'AI'"    // необязательное выражение фильтрации
);
```

Текстовый поиск поддерживает:

- Поиск по отдельным словам
- Поиск по фразам с точным совпадением, когда `inOrder` истинно
- Поиск по терминам с семантикой OR, когда `inOrder` ложно
- Фильтрацию стоп-слов для игнорирования общих слов
- Несколько алгоритмов оценки текста

Настройте поведение текстового поиска во время создания:

```java
RedisVectorStore vectorStore = RedisVectorStore.builder(jedisPooled, embeddingModel)
    .textScorer(TextScorer.TFIDF)                    // Алгоритм оценки текста
    .inOrder(true)                                   // Совпадение терминов в порядке
    .stopwords(Set.of("is", "a", "the", "and"))      // Игнорировать общие слова
    .metadataFields(MetadataField.text("description")) // Определить поля TEXT
    .build();
```

### Алгоритмы оценки текста

Доступно несколько алгоритмов оценки текста:

- **BM25**: Современная версия TF-IDF с насыщением термина (по умолчанию)
- **TFIDF**: Классическая частота термина-инверсная частота документа
- **BM25STD**: Стандартизированный BM25
- **DISMAX**: Максимум дизъюнкции
- **DOCSCORE**: Оценка документа

Оценки нормализуются в диапазоне от 0 до 1 для согласованности с оценками схожести векторов.

## Поиск по диапазонуThe range search returns all documents within a specified radius threshold, rather than a fixed number of nearest neighbors:

```java
// Поиск с явным радиусом
List<Document> rangeResults = vectorStore.searchByRange(
    "AI and machine learning",  // запрос
    0.8,                        // радиус (порог схожести)
    "category == 'AI'"          // необязательное выражение фильтра
);
```

You can also set a default range threshold at construction time:

```java
RedisVectorStore vectorStore = RedisVectorStore.builder(jedisPooled, embeddingModel)
    .defaultRangeThreshold(0.8)  // Установить порог по умолчанию
    .build();

// Использовать порог по умолчанию
List<Document> results = vectorStore.searchByRange("query");
```

Range search is useful when you want to retrieve all relevant documents above a similarity threshold, rather than limiting to a specific count.

## Semantic Caching

Semantic caching is a powerful optimization technique that leverages Redis vector search capabilities to cache and retrieve AI chat responses based on the **semantic similarity** of user queries rather than exact string matching.
This enables intelligent response reuse even when users phrase similar questions differently.

### Why Semantic Caching?

Traditional caching relies on exact key matches, which fails when users ask semantically equivalent questions with different wording:

- "Какова столица Франции?"
- "Скажи мне столицу Франции"
- "Какой город является столицей Франции?"

Все три запроса имеют одинаковый ответ, но традиционное кэширование будет рассматривать их как разные запросы, что приведет к избыточным вызовам LLM API.
Semantic caching решает эту проблему, сравнивая **значение** запросов с помощью векторных эмбеддингов.

**Преимущества:**

- **Снижение затрат на API**: Избежать избыточных вызовов дорогих LLM API
- **Низкая задержка**: Мгновенно возвращать кэшированные ответы вместо ожидания вывода модели
- **Улучшенная масштабируемость**: Обрабатывать более высокие объемы запросов без пропорционального увеличения затрат на API
- **Последовательные ответы**: Возвращать одинаковые ответы на семантически схожие вопросы

### Auto-configuration

Spring AI предоставляет автонастройку Spring Boot для Redis Semantic Cache.
Чтобы включить ее, добавьте следующую зависимость в файл `pom.xml` вашего проекта Maven:

```xml
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-starter-vector-store-redis-semantic-cache</artifactId>
</dependency>
```

или в файл сборки Gradle `build.gradle`:

```groovy
dependencies {
    implementation 'org.springframework.ai:spring-ai-starter-vector-store-redis-semantic-cache'
}
```

> **Совет:** Автонастройка предоставляет модель эмбеддинга по умолчанию, оптимизированную для семантического кэширования (`redis/langcache-embed-v1`).
Вы можете переопределить это, предоставив свой собственный бин `EmbeddingModel`.

[[semantic-cache-properties]]
### Свойства конфигурацииProperties, начинающиеся с `spring.ai.vectorstore.redis.semantic-cache.*`, настраивают семантический кэш:

[cols="2,5,1",stripes=even]
| Свойство | Описание | Значение по умолчанию |
| --- | --- | --- |
| `spring.ai.vectorstore.redis.semantic-cache.enabled` | Включить или отключить семантический кэш | `true` |
| `spring.ai.vectorstore.redis.semantic-cache.host` | Хост сервера Redis | `localhost` |
| `spring.ai.vectorstore.redis.semantic-cache.port` | Порт сервера Redis | `6379` |
| `spring.ai.vectorstore.redis.semantic-cache.similarity-threshold` | Порог схожести для попаданий в кэш (0.0-1.0). Более высокие значения требуют более близких семантических совпадений. | `0.95` |
| `spring.ai.vectorstore.redis.semantic-cache.index-name` | Имя индекса поиска Redis для записей кэша | `semantic-cache-index` |
| `spring.ai.vectorstore.redis.semantic-cache.prefix` | Префикс ключа для записей в кэше Redis | `semantic-cache:` |

Пример конфигурации в `application.yml`:

```yaml
spring:
  ai:
    vectorstore:
      redis:
        semantic-cache:
          enabled: true
          host: localhost
          port: 6379
          similarity-threshold: 0.85
          index-name: my-app-cache
          prefix: "my-app:semantic-cache:"
```

### Использование SemanticCacheAdvisor

`SemanticCacheAdvisor` бесшовно интегрируется с паттерном советников `ChatClient` в Spring AI.
Он автоматически кэширует ответы и возвращает кэшированные результаты для схожих запросов:

```java
@Autowired
private SemanticCache semanticCache;

@Autowired
private ChatModel chatModel;

public void example() {
    // Создание советника кэша
    SemanticCacheAdvisor cacheAdvisor = SemanticCacheAdvisor.builder()
        .cache(semanticCache)
        .build();

    // Первый запрос - вызывает LLM и кэширует ответ
    ChatResponse response1 = ChatClient.builder(chatModel)
        .build()
        .prompt("Какова столица Франции?")
        .advisors(cacheAdvisor)
        .call()
        .chatResponse();

    // Схожий запрос - возвращает кэшированный ответ (без вызова LLM)
    ChatResponse response2 = ChatClient.builder(chatModel)
        .build()
        .prompt("Назови столицу Франции")
        .advisors(cacheAdvisor)
        .call()
        .chatResponse();

    // response1 и response2 содержат один и тот же кэшированный ответ
}
```

Советник автоматически:

1. Проверяет кэш на наличие семантически схожих запросов перед вызовом LLM
2. Возвращает кэшированные ответы, когда совпадение найдено выше порога схожести
3. Кэширует новые ответы после успешных вызовов LLM
4. Поддерживает как синхронные, так и потоковые операции чата

### Прямое использование кэша

Вы также можете взаимодействовать с `SemanticCache` напрямую для более тонкого контроля:

```java
@Autowired
private SemanticCache semanticCache;

// Сохранить ответ с запросом
semanticCache.set("Какова столица Франции?", chatResponse);

// Сохранить с TTL (временем жизни) для автоматического истечения
semanticCache.set("Какая погода сегодня?", weatherResponse, Duration.ofHours(1));

// Получить семантически схожий ответ
Optional<ChatResponse> cached = semanticCache.get("Назови столицу Франции");

if (cached.isPresent()) {
    // Использовать кэшированный ответ
    String answer = cached.get().getResult().getOutput().getText();
}

// Очистить все кэшированные записи
semanticCache.clear();
```

### Ручная конфигурацияДля большего контроля вы можете вручную настроить компоненты семантического кэша:

```java
@Configuration
public class SemanticCacheConfig {

    @Bean
    public JedisPooled jedisPooled() {
        return new JedisPooled("localhost", 6379);
    }

    @Bean
    public SemanticCache semanticCache(JedisPooled jedisPooled, EmbeddingModel embeddingModel) {
        return DefaultSemanticCache.builder()
            .jedisClient(jedisPooled)
            .embeddingModel(embeddingModel)
            .distanceThreshold(0.3)           // Меньше = более строгая привязка
            .indexName("my-semantic-cache")
            .prefix("cache:")
            .build();
    }

    @Bean
    public SemanticCacheAdvisor semanticCacheAdvisor(SemanticCache cache) {
        return SemanticCacheAdvisor.builder()
            .cache(cache)
            .build();
    }
}
```

### Изоляция кэша с помощью пространств имен

Для многопользовательских приложений или когда вам нужны отдельные пространства кэша, используйте разные имена индексов для изоляции записей кэша:

```java
// Создание изолированных кэшей для разных пользователей или контекстов
SemanticCache user1Cache = DefaultSemanticCache.builder()
    .jedisClient(jedisPooled)
    .embeddingModel(embeddingModel)
    .indexName("user-1-cache")
    .build();

SemanticCache user2Cache = DefaultSemanticCache.builder()
    .jedisClient(jedisPooled)
    .embeddingModel(embeddingModel)
    .indexName("user-2-cache")
    .build();

// Каждый пользователь получает свое собственное изолированное пространство кэша
SemanticCacheAdvisor user1Advisor = SemanticCacheAdvisor.builder()
    .cache(user1Cache)
    .build();
```

### Изоляция системного запроса

`SemanticCacheAdvisor` автоматически изолирует кэшированные ответы на основе системного запроса. Это гарантирует, что один и тот же запрос пользователя с разными системными запросами возвращает разные кэшированные ответы, что имеет решающее значение для приложений с несколькими AI-персонами или зависимым от контекста поведением.

```java
SemanticCacheAdvisor cacheAdvisor = SemanticCacheAdvisor.builder()
    .cache(semanticCache)
    .build();

// Запрос с персоной технической поддержки
ChatResponse technicalResponse = ChatClient.builder(chatModel)
    .build()
    .prompt()
    .system("Вы специалист по технической поддержке. Предоставьте подробные технические ответы.")
    .user("Как мне сбросить пароль?")
    .advisors(cacheAdvisor)
    .call()
    .chatResponse();

// Тот же запрос с персоной службы поддержки клиентов - промах кэша (разный контекст)
ChatResponse serviceResponse = ChatClient.builder(chatModel)
    .build()
    .prompt()
    .system("Вы дружелюбный агент службы поддержки клиентов. Держите ответы краткими и полезными.")
    .user("Как мне сбросить пароль?")
    .advisors(cacheAdvisor)
    .call()
    .chatResponse();

// Тот же запрос с персоной технической поддержки снова - попадание в кэш
ChatResponse technicalAgain = ChatClient.builder(chatModel)
    .build()
    .prompt()
    .system("Вы специалист по технической поддержке. Предоставьте подробные технические ответы.")
    .user("Как мне сбросить пароль?")
    .advisors(cacheAdvisor)
    .call()
    .chatResponse();
// Возвращает кэшированный технический ответ
```

**Как это работает:**

Советник вычисляет детерминированный хэш системного запроса и использует его в качестве фильтра метаданных при хранении и извлечении кэшированных ответов:

- Один и тот же вопрос пользователя + один и тот же системный запрос → попадание в кэш
- Один и тот же вопрос пользователя + разный системный запрос → промах кэша (отдельная запись кэша)
- Запросы без системного запроса делят общее пространство кэша

### API кэша с учетом контекстаДля продвинутых случаев использования вы можете напрямую использовать методы кэширования с учетом контекста:

```java
// Сохранение с явным хешем контекста
String contextHash = "technical-support-context";
semanticCache.set("Как мне сбросить пароль?", response, contextHash);

// Извлечение с фильтрацией по контексту
Optional<ChatResponse> cached = semanticCache.get("Как мне сбросить пароль?", contextHash);

// Другой хеш контекста возвращает пустое значение (нет совпадений)
Optional<ChatResponse> otherContext = semanticCache.get("Как мне сбросить пароль?", "billing-context");
```

### Настройка порога схожести

Порог схожести определяет, насколько близко запрос должен соответствовать кэшированной записи, чтобы считаться совпадением. Порог выражается значением от 0.0 до 1.0:

- **Высокий порог (например, 0.95)**: Требует очень близких семантических совпадений. Уменьшает количество ложных срабатываний, но может пропустить действительные кэшированные совпадения.
- **Низкий порог (например, 0.70)**: Позволяет более широкие семантические совпадения. Увеличивает частоту попаданий в кэш, но может вернуть менее релевантные кэшированные ответы.

```java
// Строгое совпадение - только очень похожие запросы попадают в кэш
SemanticCache strictCache = DefaultSemanticCache.builder()
    .jedisClient(jedisPooled)
    .embeddingModel(embeddingModel)
    .distanceThreshold(0.2)  // Строгое (основанное на расстоянии, меньше = строже)
    .build();

// Либеральное совпадение - принимается более широкая семантическая схожесть
SemanticCache lenientCache = DefaultSemanticCache.builder()
    .jedisClient(jedisPooled)
    .embeddingModel(embeddingModel)
    .distanceThreshold(0.5)  // Либеральное
    .build();
```

> **Совет:** Начните с более высокого порога (строгое совпадение) и постепенно снижайте его в зависимости от допустимости семантических вариаций в вашем приложении.

### TTL и истечение срока действия кэша

Кэшированные ответы могут быть настроены с временем жизни (TTL) для автоматического истечения. Это важно для данных, чувствительных ко времени:

```java
// Кэширование данных о погоде на 1 час
semanticCache.set("Какая погода в Нью-Йорке?", weatherResponse, Duration.ofHours(1));

// Кэширование общих знаний на неопределенный срок (без TTL)
semanticCache.set("Что такое фотосинтез?", scienceResponse);

// Redis автоматически удаляет истекшие записи
```

### Как это работает

Семантический кэш работает по следующему алгоритму:

1. **Встраивание запроса**: Когда поступает запрос, он преобразуется в векторное встраивание с использованием настроенной `EmbeddingModel`

2. **Векторный поиск**: Redis выполняет поиск векторов на основе диапазона (`VECTOR_RANGE`), чтобы найти кэшированные записи в пределах порога схожести

3. **Попадание в кэш**: Если найден семантически похожий запрос, кэшированный `ChatResponse` возвращается немедленно

4. **Промах в кэше**: Если совпадение не найдено, запрос передается в LLM, и ответ кэшируется для будущего использования

Реализация использует эффективную векторную индексацию Redis (алгоритм HNSW) для быстрого поиска схожести, даже при больших размерах кэша.
