# Azure AI Service

В этом разделе вы узнаете, как настроить `AzureVectorStore` для хранения векторных представлений документов и выполнения поиска по сходству с использованием Azure AI Search Service.

[Azure AI Search](https://azure.microsoft.com/en-us/products/ai-services/ai-search/) — это универсальная облачная система извлечения информации, которая является частью более широкой платформы ИИ от Microsoft. Среди прочих функций она позволяет пользователям запрашивать информацию с использованием векторного хранения и извлечения.

## Предварительные требования

1. Подписка на Azure: вам потребуется [подписка на Azure](https://azure.microsoft.com/en-us/free/), чтобы использовать любую службу Azure.
2. Служба Azure AI Search: создайте [службу AI Search](https://portal.azure.com/#create/Microsoft.Search). После создания службы получите admin apiKey в разделе `Keys` под `Settings` и извлеките конечную точку из поля `Url` в разделе `Overview`.
3. (Необязательно) Служба Azure OpenAI: создайте службу Azure [OpenAI](https://portal.azure.com/#create/Microsoft.AIServicesOpenAI). **ПРИМЕЧАНИЕ:** Возможно, вам придется заполнить отдельную форму, чтобы получить доступ к службам Azure Open AI. После создания службы получите конечную точку и apiKey в разделе `Keys and Endpoint` под `Resource Management`.

## Конфигурация

При запуске `AzureVectorStore` может попытаться создать новый индекс в вашем экземпляре службы AI Search, если вы согласились, установив соответствующее свойство `initialize-schema` в `true` в конструкторе или, если вы используете Spring Boot, установив `...initialize-schema=true` в вашем файле `application.properties`.

> **Примечание:** это является изменением, которое нарушает совместимость! В более ранних версиях Spring AI инициализация схемы происходила по умолчанию.

В качестве альтернативы вы можете создать индекс вручную.

Чтобы настроить AzureVectorStore, вам понадобятся настройки, полученные из вышеуказанных предварительных требований, а также имя вашего индекса:

- Конечная точка Azure AI Search
- Ключ Azure AI Search
- (необязательно) Конечная точка Azure OpenAI API
- (необязательно) Ключ Azure OpenAI API

Вы можете предоставить эти значения в виде переменных окружения ОС.

```bash
export AZURE_AI_SEARCH_API_KEY=<My AI Search API Key>
export AZURE_AI_SEARCH_ENDPOINT=<My AI Search Index>
export OPENAI_API_KEY=<My Azure AI API Key> (Optional)
```

[ПРИМЕЧАНИЕ]
====
Вы можете заменить реализацию Azure Open AI на любую действительную реализацию OpenAI, которая поддерживает интерфейс Embeddings. Например, вы можете использовать реализации Open AI от Spring AI или `TransformersEmbedding` для векторных представлений вместо реализации Azure.
====

## Зависимости

[ПРИМЕЧАНИЕ]
====
В Spring AI произошли значительные изменения в автонастройке и названиях артефактов стартовых модулей.
Пожалуйста, обратитесь к https://docs.spring.io/spring-ai/reference/upgrade-notes.html[заметкам об обновлении] для получения дополнительной информации.
====

Добавьте эти зависимости в ваш проект:

### 1. Выберите реализацию интерфейса Embeddings. Вы можете выбрать между:

[tabs]
======
OpenAI Embedding::
+
```xml
<dependency>
   <groupId>org.springframework.ai</groupId>
   <artifactId>spring-ai-starter-model-openai</artifactId>
</dependency>
```

Azure AI Embedding::
+
```xml
<dependency>
 <groupId>org.springframework.ai</groupId>
 <artifactId>spring-ai-starter-model-azure-openai</artifactId>
</dependency>
```

Local Sentence Transformers Embedding::
+
```xml
<dependency>
 <groupId>org.springframework.ai</groupId>
 <artifactId>spring-ai-starter-model-transformers</artifactId>
</dependency>
```
======

### 2. Azure (AI Search) Vector Store

```xml
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-azure-store</artifactId>
</dependency>
```

> **Совет:** Обратитесь к разделу xref:getting-started.adoc#dependency-management[Управление зависимостями], чтобы добавить Spring AI BOM в ваш файл сборки.

## Свойства конфигурацииВы можете использовать следующие свойства в вашей конфигурации Spring Boot для настройки хранилища векторов Azure.

[stripes=even]
| Свойство | Значение по умолчанию |
| --- | --- |
| `spring.ai.vectorstore.azure.url` |  |
| `spring.ai.vectorstore.azure.api-key` |  |
| `spring.ai.vectorstore.azure.useKeylessAuth` | false |
| `spring.ai.vectorstore.azure.initialize-schema` | false |
| `spring.ai.vectorstore.azure.index-name` | spring_ai_azure_vector_store |
| `spring.ai.vectorstore.azure.default-top-k` | 4 |
| `spring.ai.vectorstore.azure.default-similarity-threshold` | 0.0 |
| `spring.ai.vectorstore.azure.content-field-name` | content |
| `spring.ai.vectorstore.azure.embedding-field-name` | embedding |
| `spring.ai.vectorstore.azure.metadata-field-name` | metadata |


## Пример кода

Чтобы настроить `SearchIndexClient` Azure в вашем приложении, вы можете использовать следующий код:

```java
@Bean
public SearchIndexClient searchIndexClient() {
  return new SearchIndexClientBuilder().endpoint(System.getenv("AZURE_AI_SEARCH_ENDPOINT"))
    .credential(new AzureKeyCredential(System.getenv("AZURE_AI_SEARCH_API_KEY")))
    .buildClient();
}
```

Чтобы создать хранилище векторов, вы можете использовать следующий код, внедрив созданный выше бин `SearchIndexClient` вместе с `EmbeddingModel`, предоставленным библиотекой Spring AI, которая реализует необходимый интерфейс Embeddings.

```java
@Bean
public VectorStore vectorStore(SearchIndexClient searchIndexClient, EmbeddingModel embeddingModel) {

  return AzureVectorStore.builder(searchIndexClient, embeddingModel)
    .initializeSchema(true)
    // Определите поля метаданных, которые будут использоваться
    // в фильтрах поиска по сходству.
    .filterMetadataFields(List.of(MetadataField.text("country"), MetadataField.int64("year"),
            MetadataField.date("activationDate")))
    .defaultTopK(5)
    .defaultSimilarityThreshold(0.7)
    .indexName("spring-ai-document-index")
    .build();
}
```

[ПРИМЕЧАНИЕ]
====
Вы должны явно перечислить все имена и типы полей метаданных для любого ключа метаданных, используемого в выражении фильтра. Список выше регистрирует фильтруемые поля метаданных: `country` типа `TEXT`, `year` типа `INT64` и `active` типа `BOOLEAN`.

Если фильтруемые поля метаданных будут расширены новыми записями, вам необходимо (пере)загрузить/обновить документы с этими метаданными.
====

В вашем основном коде создайте несколько документов:

```java
List<Document> documents = List.of(
	new Document("Spring AI rocks!! Spring AI rocks!! Spring AI rocks!! Spring AI rocks!! Spring AI rocks!!", Map.of("country", "BG", "year", 2020)),
	new Document("The World is Big and Salvation Lurks Around the Corner"),
	new Document("You walk forward facing the past and you turn back toward the future.", Map.of("country", "NL", "year", 2023)));
```

Добавьте документы в ваше хранилище векторов:

```java
vectorStore.add(documents);
```

И, наконец, получите документы, похожие на запрос:

```java
List<Document> results = vectorStore.similaritySearch(
    SearchRequest.builder()
      .query("Spring")
      .topK(5).build());
```

Если все пройдет успешно, вы должны получить документ, содержащий текст "Spring AI rocks!!".

### Фильтрация метаданныхВы также можете использовать универсальные, переносимые [фильтры метаданных](https://docs.spring.io/spring-ai/reference/api/vectordbs.html#_metadata_filters) с AzureVectorStore.

Например, вы можете использовать либо текстовый язык выражений:

```java
vectorStore.similaritySearch(
   SearchRequest.builder()
      .query("The World")
      .topK(TOP_K)
      .similarityThreshold(SIMILARITY_THRESHOLD)
      .filterExpression("country in ['UK', 'NL'] && year >= 2020").build());
```

либо программно, используя DSL выражений:

```java
FilterExpressionBuilder b = new FilterExpressionBuilder();

vectorStore.similaritySearch(
    SearchRequest.builder()
      .query("The World")
      .topK(TOP_K)
      .similarityThreshold(SIMILARITY_THRESHOLD)
      .filterExpression(b.and(
         b.in("country", "UK", "NL"),
         b.gte("year", 2020)).build()).build());
```

Переносимые выражения фильтров автоматически преобразуются в собственные фильтры Azure Search [OData](https://learn.microsoft.com/en-us/azure/search/search-query-odata-filter). Например, следующее переносимое выражение фильтра:

```sql
country in ['UK', 'NL'] && year >= 2020
```

преобразуется в следующее выражение фильтра Azure OData [filter expression](https://learn.microsoft.com/en-us/azure/search/search-query-odata-filter):

```graphql
$filter search.in(meta_country, 'UK,NL', ',') and meta_year ge 2020
```

## Пользовательские имена полей

По умолчанию Azure Vector Store использует следующие имена полей в индексе Azure AI Search:

- `content` - для текста документа
- `embedding` - для векторных эмбеддингов
- `metadata` - для метаданных документа

Однако, работая с существующими индексами Azure AI Search, которые используют другие имена полей, вы можете настроить пользовательские имена полей, чтобы они соответствовали вашей схеме индекса. Это позволяет интегрировать Spring AI с уже существующими индексами без необходимости их изменения.

### Сценарии использования

Пользовательские имена полей особенно полезны, когда:

- **Интеграция с существующими индексами**: Ваша организация уже имеет индексы Azure AI Search с установленными соглашениями об именах полей (например, `chunk_text`, `vector`, `meta_data`).
- **Соблюдение стандартов именования**: Ваша команда придерживается определенных соглашений об именах, которые отличаются от стандартных.
- **Миграция с других систем**: Вы мигрируете с другой векторной базы данных или поисковой системы и хотите сохранить согласованные имена полей.

### Конфигурация через свойства

Вы можете настроить пользовательские имена полей, используя свойства приложения Spring Boot:

```properties
spring.ai.vectorstore.azure.url=${AZURE_AI_SEARCH_ENDPOINT}
spring.ai.vectorstore.azure.api-key=${AZURE_AI_SEARCH_API_KEY}
spring.ai.vectorstore.azure.index-name=my-existing-index
spring.ai.vectorstore.azure.initialize-schema=false

# Пользовательские имена полей для соответствия существующей схеме индекса
spring.ai.vectorstore.azure.content-field-name=chunk_text
spring.ai.vectorstore.azure.embedding-field-name=vector
spring.ai.vectorstore.azure.metadata-field-name=meta_data
```

> **Важно:** При использовании существующего индекса с пользовательскими именами полей установите `initialize-schema=false`, чтобы предотвратить попытку Spring AI создать новый индекс с использованием схемы по умолчанию.

### Конфигурация через API Builder

В качестве альтернативы вы можете настроить пользовательские имена полей программно, используя API builder:

```java
@Bean
public VectorStore vectorStore(SearchIndexClient searchIndexClient, EmbeddingModel embeddingModel) {

	return AzureVectorStore.builder(searchIndexClient, embeddingModel)
		.indexName("my-existing-index")
		.initializeSchema(false) // Не создавать схему - использовать существующий индекс
		// Настройка пользовательских имен полей для соответствия существующему индексу
		.contentFieldName("chunk_text")
		.embeddingFieldName("vector")
		.metadataFieldName("meta_data")
		.filterMetadataFields(List.of(
			MetadataField.text("category"),
			MetadataField.text("source")))
		.build();
}
```

### Полный пример: Работа с существующим индексом```markdown
Вот полный пример, показывающий, как использовать Spring AI с существующим индексом Azure AI Search, который имеет пользовательские имена полей:

```java
@Configuration
public class VectorStoreConfig {

	@Bean
	public SearchIndexClient searchIndexClient() {
		return new SearchIndexClientBuilder()
			.endpoint(System.getenv("AZURE_AI_SEARCH_ENDPOINT"))
			.credential(new AzureKeyCredential(System.getenv("AZURE_AI_SEARCH_API_KEY")))
			.buildClient();
	}

	@Bean
	public VectorStore vectorStore(SearchIndexClient searchIndexClient,
			EmbeddingModel embeddingModel) {

		return AzureVectorStore.builder(searchIndexClient, embeddingModel)
			.indexName("production-documents-index")
			.initializeSchema(false) // Использовать существующий индекс
			// Сопоставить с существующими именами полей индекса
			.contentFieldName("document_text")
			.embeddingFieldName("text_vector")
			.metadataFieldName("document_metadata")
			// Определить фильтруемые метаданные из существующей схемы
			.filterMetadataFields(List.of(
				MetadataField.text("department"),
				MetadataField.int64("year"),
				MetadataField.date("created_date")))
			.defaultTopK(10)
			.defaultSimilarityThreshold(0.75)
			.build();
	}
}
```

Вы можете использовать векторный магазин как обычно:

```java
// Поиск с использованием существующего индекса с пользовательскими именами полей
List<Document> results = vectorStore.similaritySearch(
	SearchRequest.builder()
		.query("artificial intelligence")
		.topK(5)
		.filterExpression("department == 'Engineering' && year >= 2023")
		.build());

// Результаты содержат документы с текстом из поля 'document_text'
results.forEach(doc -> System.out.println(doc.getText()));
```

### Создание нового индекса с пользовательскими именами полей

Вы также можете создать новый индекс с пользовательскими именами полей, установив `initializeSchema=true`:

```java
@Bean
public VectorStore vectorStore(SearchIndexClient searchIndexClient,
		EmbeddingModel embeddingModel) {

	return AzureVectorStore.builder(searchIndexClient, embeddingModel)
		.indexName("new-custom-index")
		.initializeSchema(true) // Создать новый индекс с пользовательскими именами полей
		.contentFieldName("text_content")
		.embeddingFieldName("content_vector")
		.metadataFieldName("doc_metadata")
		.filterMetadataFields(List.of(
			MetadataField.text("category"),
			MetadataField.text("author")))
		.build();
}
```

Это создаст новый индекс Azure AI Search с вашими пользовательскими именами полей, позволяя вам установить собственные соглашения об именовании с самого начала.

## Доступ к нативному клиенту

Реализация Azure Vector Store предоставляет доступ к основному нативному клиенту Azure Search (`SearchClient`) через метод `getNativeClient()`:

```java
AzureVectorStore vectorStore = context.getBean(AzureVectorStore.class);
Optional<SearchClient> nativeClient = vectorStore.getNativeClient();

if (nativeClient.isPresent()) {
    SearchClient client = nativeClient.get();
    // Используйте нативный клиент для операций, специфичных для Azure Search
}
```

Нативный клиент предоставляет доступ к функциям и операциям, специфичным для Azure Search, которые могут не быть доступны через интерфейс `VectorStore`.
```
