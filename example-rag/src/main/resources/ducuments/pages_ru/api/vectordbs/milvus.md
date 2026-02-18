# Milvus

[Milvus](https://milvus.io/) — это открытая векторная база данных, которая привлекла значительное внимание в области науки о данных и машинного обучения. Одной из её выдающихся особенностей является надежная поддержка векторной индексации и запросов. Milvus использует современные, передовые алгоритмы для ускорения процесса поиска, что делает его исключительно эффективным при извлечении похожих векторов, даже при работе с обширными наборами данных.

## Предварительные требования

- Запущенный экземпляр Milvus. Доступны следующие варианты:
** [Milvus Standalone](https://milvus.io/docs/install_standalone-docker.md): Docker, Operator, Helm, DEB/RPM, Docker Compose.
** [Milvus Cluster](https://milvus.io/docs/install_cluster-milvusoperator.md): Operator, Helm.
- При необходимости, API-ключ для xref:api/embeddings.adoc#available-implementations[EmbeddingModel], чтобы генерировать встраивания, хранящиеся в `MilvusVectorStore`.

## Зависимости

[NOTE]
====
В Spring AI произошли значительные изменения в автонастройке и названиях артефактов стартовых модулей.
Пожалуйста, обратитесь к https://docs.spring.io/spring-ai/reference/upgrade-notes.html[заметкам об обновлении] для получения дополнительной информации.
====

Затем добавьте зависимость Milvus VectorStore boot starter в ваш проект:

```xml
<dependency>
	<groupId>org.springframework.ai</groupId>
	<artifactId>spring-ai-starter-vector-store-milvus</artifactId>
</dependency>
```

или в ваш файл сборки Gradle `build.gradle`.

```groovy
dependencies {
    implementation 'org.springframework.ai:spring-ai-starter-vector-store-milvus'
}
```


> **Совет:** Обратитесь к разделу xref:getting-started.adoc#dependency-management[Управление зависимостями], чтобы добавить BOM Spring AI в ваш файл сборки.
Обратитесь к разделу xref:getting-started.adoc#artifact-repositories[Артефактные репозитории], чтобы добавить Maven Central и/или Snapshot репозитории в ваш файл сборки.


Реализация векторного хранилища может инициализировать необходимую схему для вас, но вы должны согласиться, указав булевый параметр `initializeSchema` в соответствующем конструкторе или установив `...initialize-schema=true` в файле `application.properties`.

> **Примечание:** это является изменением, нарушающим совместимость! В более ранних версиях Spring AI эта инициализация схемы происходила по умолчанию.



Векторному хранилищу также требуется экземпляр `EmbeddingModel` для вычисления встраиваний для документов.
Вы можете выбрать одну из доступных xref:api/embeddings.adoc#available-implementations[реализаций EmbeddingModel].


Чтобы подключиться и настроить `MilvusVectorStore`, вам необходимо предоставить данные доступа к вашему экземпляру.
Простая конфигурация может быть предоставлена через `application.yml` Spring Boot

[yml]
```
spring:
	ai:
		vectorstore:
			milvus:
				client:
					host: "localhost"
					port: 19530
					username: "root"
					password: "milvus"
				databaseName: "default"
				collectionName: "vector_store"
				embeddingDimension: 1536
				indexType: IVF_FLAT
				metricType: COSINE
```

> **Совет:** Ознакомьтесь со списком xref:#milvus-properties[параметров конфигурации], чтобы узнать о значениях по умолчанию и параметрах конфигурации.

Теперь вы можете автоматически подключить Milvus Vector Store в вашем приложении и использовать его

```java
@Autowired VectorStore vectorStore;

// ...

List <Document> documents = List.of(
    new Document("Spring AI rocks!! Spring AI rocks!! Spring AI rocks!! Spring AI rocks!! Spring AI rocks!!", Map.of("meta1", "meta1")),
    new Document("The World is Big and Salvation Lurks Around the Corner"),
    new Document("You walk forward facing the past and you turn back toward the future.", Map.of("meta2", "meta2")));

// Добавьте документы в Milvus Vector Store
vectorStore.add(documents);

// Извлеките документы, похожие на запрос
List<Document> results = this.vectorStore.similaritySearch(SearchRequest.builder().query("Spring").topK(5).build());
```

### Ручная конфигурацияВместо использования автонастройки Spring Boot, вы можете вручную настроить `MilvusVectorStore`. Чтобы добавить следующие зависимости в ваш проект:

```xml
<dependency>
	<groupId>org.springframework.ai</groupId>
	<artifactId>spring-ai-milvus-store</artifactId>
</dependency>
```

> **Совет:** Обратитесь к разделу xref:getting-started.adoc#dependency-management[Управление зависимостями], чтобы добавить Spring AI BOM в ваш файл сборки.

Чтобы настроить MilvusVectorStore в вашем приложении, вы можете использовать следующую конфигурацию:

```java
	@Bean
	public VectorStore vectorStore(MilvusServiceClient milvusClient, EmbeddingModel embeddingModel) {
		return MilvusVectorStore.builder(milvusClient, embeddingModel)
				.collectionName("test_vector_store")
				.databaseName("default")
				.indexType(IndexType.IVF_FLAT)
				.metricType(MetricType.COSINE)
				.batchingStrategy(new TokenCountBatchingStrategy())
				.initializeSchema(true)
				.build();
	}

	@Bean
	public MilvusServiceClient milvusClient() {
		return new MilvusServiceClient(ConnectParam.newBuilder()
			.withAuthorization("minioadmin", "minioadmin")
			.withUri(milvusContainer.getEndpoint())
			.build());
	}
```

## Фильтрация метаданных

Вы можете использовать универсальные, переносимые [фильтры метаданных](https://docs.spring.io/spring-ai/reference/api/vectordbs.html#_metadata_filters) с хранилищем Milvus.

Например, вы можете использовать либо язык выражений текста:

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

> **Примечание:** Эти выражения фильтров преобразуются в эквивалентные фильтры Milvus.

## Использование MilvusSearchRequest

MilvusSearchRequest расширяет SearchRequest, позволяя вам использовать специфические для Milvus параметры поиска, такие как нативные выражения и JSON-параметры поиска.

```java
MilvusSearchRequest request = MilvusSearchRequest.milvusBuilder()
    .query("sample query")
    .topK(5)
    .similarityThreshold(0.7)
    .nativeExpression("metadata[\"age\"] > 30") // Переопределяет filterExpression, если оба установлены
    .filterExpression("age <= 30") // Игнорируется, если установлено nativeExpression
    .searchParamsJson("{\"nprobe\":128}")
    .build();
List results = vectorStore.similaritySearch(request);
```
Это обеспечивает большую гибкость при использовании специфических для Milvus функций поиска.

## Важность `nativeExpression` и `searchParamsJson` в `MilvusSearchRequest`Эти два параметра повышают точность поиска в Milvus и обеспечивают оптимальную производительность запросов:

**nativeExpression**: Включает дополнительные возможности фильтрации с использованием нативных выражений фильтрации Milvus.  
https://milvus.io/docs/boolean.md[Фильтрация Milvus]

Пример:
```java
MilvusSearchRequest request = MilvusSearchRequest.milvusBuilder()
    .query("sample query")
    .topK(5)
    .nativeExpression("metadata['category'] == 'science'")
    .build();
```

**searchParamsJson**: Необходим для настройки поведения поиска при использовании IVF_FLAT, стандартного индекса Milvus.  
https://milvus.io/docs/index.md?tab=floating[Индекс векторов Milvus]

По умолчанию `IVF_FLAT` требует установки `nprobe` для получения точных результатов. Если не указано, `nprobe` по умолчанию равен `1`, что может привести к плохой полноте или даже отсутствию результатов поиска.

Пример:
```java
MilvusSearchRequest request = MilvusSearchRequest.milvusBuilder()
    .query("sample query")
    .topK(5)
    .searchParamsJson("{\"nprobe\":128}")
    .build();
```

Использование `nativeExpression` обеспечивает продвинутую фильтрацию, в то время как `searchParamsJson` предотвращает неэффективные поиски, вызванные низким значением по умолчанию для `nprobe`.

## Свойства Milvus VectorStoreВы можете использовать следующие свойства в конфигурации Spring Boot для настройки хранилища векторов Milvus.

| Свойство | Описание | Значение по умолчанию |
| --- | --- | --- |
| spring.ai.vectorstore.milvus.database-name | Имя базы данных Milvus, которую нужно использовать. | default |
| spring.ai.vectorstore.milvus.collection-name | Имя коллекции Milvus для хранения векторов | vector_store |
| spring.ai.vectorstore.milvus.initialize-schema | нужно ли инициализировать бэкенд Milvus | false |
| spring.ai.vectorstore.milvus.embedding-dimension | Размерность векторов, которые будут храниться в коллекции Milvus. | 1536 |
| spring.ai.vectorstore.milvus.index-type | Тип индекса, который будет создан для коллекции Milvus. | IVF_FLAT |
| spring.ai.vectorstore.milvus.metric-type | Тип метрики, который будет использоваться для коллекции Milvus. | COSINE |
| spring.ai.vectorstore.milvus.index-parameters | Параметры индекса, которые будут использоваться для коллекции Milvus. | {"nlist":1024} |
| spring.ai.vectorstore.milvus.id-field-name | Имя поля ID для коллекции | doc_id |
| spring.ai.vectorstore.milvus.auto-id | Булевый флаг, указывающий, используется ли авто-ID для поля ID | false |
| spring.ai.vectorstore.milvus.content-field-name | Имя поля содержимого для коллекции | content |
| spring.ai.vectorstore.milvus.metadata-field-name | Имя поля метаданных для коллекции | metadata |
| spring.ai.vectorstore.milvus.embedding-field-name | Имя поля встраивания для коллекции | embedding |
| spring.ai.vectorstore.milvus.client.host | Имя или адрес хоста. | localhost |
| spring.ai.vectorstore.milvus.client.port | Порт подключения. | 19530 |
| spring.ai.vectorstore.milvus.client.uri | URI экземпляра Milvus | - |
| spring.ai.vectorstore.milvus.client.token | Токен, служащий ключом для идентификации и аутентификации. | - |
| spring.ai.vectorstore.milvus.client.connect-timeout-ms | Значение таймаута подключения клиентского канала. Значение таймаута должно быть больше нуля. | 10000 |
| spring.ai.vectorstore.milvus.client.keep-alive-time-ms | Значение времени поддержания соединения клиентского канала. Значение keep-alive должно быть больше нуля. | 55000 |
| spring.ai.vectorstore.milvus.client.keep-alive-timeout-ms | Значение таймаута поддержания соединения клиентского канала. Значение таймаута должно быть больше нуля. | 20000 |
| spring.ai.vectorstore.milvus.client.rpc-deadline-ms | Срок, в течение которого вы готовы ждать ответа от сервера. При установке срока клиент будет ждать, когда возникнет быстрая ошибка RPC, вызванная колебаниями сети. Значение срока должно быть больше или равно нулю. | 0 |
| spring.ai.vectorstore.milvus.client.client-key-path | Путь к client.key для двухсторонней аутентификации TLS, действует только при "secure" = true | - |
| spring.ai.vectorstore.milvus.client.client-pem-path | Путь к client.pem для двухсторонней аутентификации TLS, действует только при "secure" = true | - |
| spring.ai.vectorstore.milvus.client.ca-pem-path | Путь к ca.pem для двухсторонней аутентификации TLS, действует только при "secure" = true | - |
| spring.ai.vectorstore.milvus.client.server-pem-path | Путь к server.pem для односторонней аутентификации TLS, действует только при "secure" = true. | - |
| spring.ai.vectorstore.milvus.client.server-name | Устанавливает переопределение целевого имени для проверки имени хоста SSL, действует только при "secure" = True. Примечание: это значение передается в grpc.ssl_target_name_override | - |
| spring.ai.vectorstore.milvus.client.secure | Обеспечивает авторизацию для этого соединения, установите в True для включения TLS. | false |
| spring.ai.vectorstore.milvus.client.idle-timeout-ms | Значение таймаута простоя клиентского канала. Значение таймаута должно быть больше нуля. | 24h |
| spring.ai.vectorstore.milvus.client.username | Имя пользователя и пароль для этого соединения. | root |
| spring.ai.vectorstore.milvus.client.password | Пароль для этого соединения. | milvus | ## Запуск Milvus Store |

Из папки `src/test/resources/` выполните:

```bash
docker-compose up
```

Чтобы очистить окружение:

```bash
docker-compose down; rm -Rf ./volumes
```

Затем подключитесь к векторному хранилищу по адресу [http://localhost:19530](http://localhost:19530) или для управления [http://localhost:9001](http://localhost:9001) (пользователь: `minioadmin`, пароль: `minioadmin`)

## Устранение неполадок

Если Docker жалуется на ресурсы, выполните:

```bash
docker system prune --all --force --volumes
```

## Доступ к нативному клиенту

Реализация Milvus Vector Store предоставляет доступ к базовому нативному клиенту Milvus (`MilvusServiceClient`) через метод `getNativeClient()`:

```java
MilvusVectorStore vectorStore = context.getBean(MilvusVectorStore.class);
Optional<MilvusServiceClient> nativeClient = vectorStore.getNativeClient();

if (nativeClient.isPresent()) {
    MilvusServiceClient client = nativeClient.get();
    // Используйте нативный клиент для операций, специфичных для Milvus
}
```

Нативный клиент предоставляет доступ к функциям и операциям, специфичным для Milvus, которые могут не быть доступны через интерфейс `VectorStore`.
