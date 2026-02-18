# Pinecone

Этот раздел проведет вас через процесс настройки `VectorStore` Pinecone для хранения векторных представлений документов и выполнения поиска по сходству.

[Pinecone](https://www.pinecone.io/) — это популярная облачная векторная база данных, которая позволяет эффективно хранить и искать векторы.

## Предварительные требования

1. Учетная запись Pinecone: перед началом зарегистрируйтесь на [Pinecone](https://app.pinecone.io/).
2. Проект Pinecone: после регистрации сгенерируйте API-ключ и создайте индекс. Эти данные понадобятся для конфигурации.
3. Экземпляр `EmbeddingModel` для вычисления векторных представлений документов. Доступно несколько вариантов:
- При необходимости, API-ключ для xref:api/embeddings.adoc#available-implementations[EmbeddingModel] для генерации векторных представлений, хранящихся в `PineconeVectorStore`.

Чтобы настроить `PineconeVectorStore`, соберите следующие данные из вашей учетной записи Pinecone:

- API-ключ Pinecone
- Имя индекса Pinecone
- Пространство имен Pinecone

[ПРИМЕЧАНИЕ]
====
Эта информация доступна вам в портале пользовательского интерфейса Pinecone.
Поддержка пространств имен недоступна в бесплатном тарифе Pinecone.
====

## Автоконфигурация

[ПРИМЕЧАНИЕ]
====
В автоконфигурации Spring AI произошли значительные изменения, изменились названия артефактов стартовых модулей.
Пожалуйста, обратитесь к https://docs.spring.io/spring-ai/reference/upgrade-notes.html[заметкам об обновлении] для получения дополнительной информации.
====

Spring AI предоставляет автоконфигурацию Spring Boot для векторного хранилища Pinecone.
Чтобы включить ее, добавьте следующую зависимость в файл Maven `pom.xml` вашего проекта:

```xml
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-starter-vector-store-pinecone</artifactId>
</dependency>
```

или в файл сборки Gradle `build.gradle`.

```groovy
dependencies {
    implementation 'org.springframework.ai:spring-ai-starter-vector-store-pinecone'
}
```

> **Совет:** Обратитесь к разделу xref:getting-started.adoc#dependency-management[Управление зависимостями], чтобы добавить BOM Spring AI в ваш файл сборки.

> **Совет:** Обратитесь к разделу xref:getting-started.adoc#artifact-repositories[Артефактные репозитории], чтобы добавить Maven Central и/или репозитории снимков в ваш файл сборки.

Кроме того, вам понадобится настроенный бин `EmbeddingModel`. Обратитесь к разделу xref:api/embeddings.adoc#available-implementations[EmbeddingModel] для получения дополнительной информации.

Вот пример необходимого бина:

```java
@Bean
public EmbeddingModel embeddingModel() {
    // Может быть любая другая реализация EmbeddingModel.
    return new OpenAiEmbeddingModel(new OpenAiApi(System.getenv("OPENAI_API_KEY")));
}
```

Чтобы подключиться к Pinecone, вам нужно предоставить данные доступа для вашего экземпляра.
Простая конфигурация может быть предоставлена через _application.properties_ Spring Boot,

```properties
spring.ai.vectorstore.pinecone.apiKey=<ваш api ключ>
spring.ai.vectorstore.pinecone.index-name=<ваше имя индекса>

# API-ключ, если необходимо, например, OpenAI
spring.ai.openai.api.key=<api-ключ>
```

Пожалуйста, ознакомьтесь со списком xref:#_configuration_properties[параметров конфигурации] для векторного хранилища, чтобы узнать о значениях по умолчанию и параметрах конфигурации.

Теперь вы можете автоматически подключить векторное хранилище Pinecone в вашем приложении и использовать его

```java
@Autowired VectorStore vectorStore;

// ...

List <Document> documents = List.of(
    new Document("Spring AI rocks!! Spring AI rocks!! Spring AI rocks!! Spring AI rocks!! Spring AI rocks!!", Map.of("meta1", "meta1")),
    new Document("The World is Big and Salvation Lurks Around the Corner"),
    new Document("You walk forward facing the past and you turn back toward the future.", Map.of("meta2", "meta2")));

// Добавьте документы
vectorStore.add(documents);

// Извлеките документы, похожие на запрос
List<Document> results = this.vectorStore.similaritySearch(SearchRequest.builder().query("Spring").topK(5).build());
```

### Параметры конфигурацииВы можете использовать следующие свойства в вашей конфигурации Spring Boot для настройки хранилища векторов Pinecone.

[stripes=even]
| Свойство | Описание | Значение по умолчанию |
| --- | --- | --- |
| `spring.ai.vectorstore.pinecone.api-key` | API-ключ Pinecone | - |
| `spring.ai.vectorstore.pinecone.index-name` | Имя индекса Pinecone | - |
| `spring.ai.vectorstore.pinecone.namespace` | Пространство имен Pinecone | - |
| `spring.ai.vectorstore.pinecone.content-field-name` | Имя поля метаданных Pinecone, используемое для хранения оригинального текстового содержимого. | `document_content` |
| `spring.ai.vectorstore.pinecone.distance-metadata-field-name` | Имя поля метаданных Pinecone, используемое для хранения вычисленного расстояния. | `distance` |
| `spring.ai.vectorstore.pinecone.server-side-timeout` | 20 сек. |  |

## Фильтрация метаданных

Вы можете использовать универсальные, переносимые [фильтры метаданных](https://docs.spring.io/spring-ai/reference/api/vectordbs.html#_metadata_filters) с хранилищем Pinecone.

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

> **Примечание:** Эти выражения фильтров преобразуются в эквивалентные фильтры Pinecone.


## Ручная конфигурация

Если вы предпочитаете настраивать `PineconeVectorStore` вручную, вы можете сделать это, используя `PineconeVectorStore#Builder`.

Добавьте эти зависимости в ваш проект:

- OpenAI: требуется для вычисления встраиваний.

```xml
<dependency>
	<groupId>org.springframework.ai</groupId>
	<artifactId>spring-ai-starter-model-openai</artifactId>
</dependency>
```

- Pinecone

```xml
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-pinecone-store</artifactId>
</dependency>
```

> **Совет:** Обратитесь к разделу xref:getting-started.adoc#dependency-management[Управление зависимостями], чтобы добавить Spring AI BOM в ваш файл сборки.

### Пример кода

Чтобы настроить Pinecone в вашем приложении, вы можете использовать следующую конфигурацию:

```java
@Bean
public VectorStore pineconeVectorStore(EmbeddingModel embeddingModel) {
    return PineconeVectorStore.builder(embeddingModel)
            .apiKey(PINECONE_API_KEY)
            .indexName(PINECONE_INDEX_NAME)
            .namespace(PINECONE_NAMESPACE) // бесплатный тариф не поддерживает пространства имен.
            .contentFieldName(CUSTOM_CONTENT_FIELD_NAME) // необязательное поле для хранения оригинального содержимого. По умолчанию `document_content`
            .build();
}
```

В вашем основном коде создайте несколько документов:

```java
List<Document> documents = List.of(
	new Document("Spring AI rocks!! Spring AI rocks!! Spring AI rocks!! Spring AI rocks!! Spring AI rocks!!", Map.of("meta1", "meta1")),
	new Document("The World is Big and Salvation Lurks Around the Corner"),
	new Document("You walk forward facing the past and you turn back toward the future.", Map.of("meta2", "meta2")));
```

Добавьте документы в Pinecone:

```java
vectorStore.add(documents);
```

И, наконец, получите документы, похожие на запрос:

```java
List<Document> results = vectorStore.similaritySearch(SearchRequest.query("Spring").topK(5).build());
```

Если все пройдет успешно, вы должны получить документ, содержащий текст "Spring AI rocks!!".

## Доступ к нативному клиентуРеализация хранилища векторов Pinecone предоставляет доступ к базовому нативному клиенту Pinecone (`PineconeConnection`) через метод `getNativeClient()`:

```java
PineconeVectorStore vectorStore = context.getBean(PineconeVectorStore.class);
Optional<PineconeConnection> nativeClient = vectorStore.getNativeClient();

if (nativeClient.isPresent()) {
    PineconeConnection client = nativeClient.get();
    // Используйте нативный клиент для операций, специфичных для Pinecone
}
```

Нативный клиент предоставляет доступ к функциям и операциям, специфичным для Pinecone, которые могут не быть доступны через интерфейс `VectorStore`.
