# Chroma

В этом разделе вы узнаете, как настроить Chroma VectorStore для хранения векторных представлений документов и выполнения поиска по сходству.

[Chroma](https://docs.trychroma.com/) — это база данных векторных представлений с открытым исходным кодом. Она предоставляет инструменты для хранения векторных представлений документов, контента и метаданных, а также для поиска по этим векторным представлениям, включая фильтрацию по метаданным.

## Предварительные требования

1. Доступ к ChromaDB. Совместимо с [Chroma Cloud](https://trychroma.com/signup), или [настройка локального ChromaDB](#run Chroma Locally) в приложении показывает, как настроить базу данных локально с помощью контейнера Docker.
   - Для Chroma Cloud: вам понадобятся ваш API-ключ, имя арендатора и имя базы данных из вашей панели управления Chroma Cloud.
   - Для локального ChromaDB: дополнительная конфигурация не требуется, кроме запуска контейнера.

2. Экземпляр `EmbeddingModel` для вычисления векторных представлений документов. Доступно несколько вариантов:
- При необходимости, API-ключ для xref:api/embeddings.adoc#available-implementations[EmbeddingModel], чтобы генерировать векторные представления, хранящиеся в `ChromaVectorStore`.

При запуске `ChromaVectorStore` создает необходимую коллекцию, если она еще не была создана.

## Автоконфигурация[NOTE]
====
В Spring AI произошли значительные изменения в автонастройке и названиях артефактов стартовых модулей.
Пожалуйста, обратитесь к https://docs.spring.io/spring-ai/reference/upgrade-notes.html[заметкам об обновлении] для получения дополнительной информации.
====

Spring AI предоставляет автонастройку Spring Boot для Chroma Vector Store.
Чтобы включить эту функциональность, добавьте следующую зависимость в файл `pom.xml` вашего проекта:

```xml
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-starter-vector-store-chroma</artifactId>
</dependency>
```

или в файл сборки Gradle `build.gradle`.

```groovy
dependencies {
    implementation 'org.springframework.ai:spring-ai-starter-vector-store-chroma'
}
```

> **Совет:** Обратитесь к разделу xref:getting-started.adoc#dependency-management[Управление зависимостями], чтобы добавить BOM Spring AI в ваш файл сборки.

> **Совет:** Обратитесь к разделу xref:getting-started.adoc#artifact-repositories[Артефактные репозитории], чтобы добавить Maven Central и/или Snapshot репозитории в ваш файл сборки.

Реализация векторного хранилища может инициализировать необходимую схему для вас, но вы должны согласиться, указав булевый параметр `initializeSchema` в соответствующем конструкторе или установив `...initialize-schema=true` в файле `application.properties`.

> **Примечание:** это является изменением, которое нарушает совместимость! В предыдущих версиях Spring AI инициализация схемы происходила по умолчанию.

Кроме того, вам потребуется настроенный бин `EmbeddingModel`. Обратитесь к разделу xref:api/embeddings.adoc#available-implementations[EmbeddingModel] для получения дополнительной информации.

Вот пример необходимого бина:

```java
@Bean
public EmbeddingModel embeddingModel() {
    // Может быть любая другая реализация EmbeddingModel.
    return new OpenAiEmbeddingModel(OpenAiApi.builder().apiKey(System.getenv("OPENAI_API_KEY")).build());
}
```

Чтобы подключиться к Chroma, вам нужно предоставить данные доступа для вашего экземпляра.
Простая конфигурация может быть предоставлена через _application.properties_ Spring Boot,

```properties
# Свойства подключения к Chroma Vector Store
spring.ai.vectorstore.chroma.client.host=<хост вашего экземпляра Chroma>  // для Chroma Cloud: api.trychroma.com
spring.ai.vectorstore.chroma.client.port=<порт вашего экземпляра Chroma> // для Chroma Cloud: 443
spring.ai.vectorstore.chroma.client.key-token=<ваш токен доступа (если настроен)> // для Chroma Cloud: используйте API-ключ
spring.ai.vectorstore.chroma.client.username=<ваше имя пользователя (если настроено)>
spring.ai.vectorstore.chroma.client.password=<ваш пароль (если настроен)>

# Свойства арендатора и базы данных Chroma Vector Store (обязательно для Chroma Cloud)
spring.ai.vectorstore.chroma.tenant-name=<ваше имя арендатора> // по умолчанию: SpringAiTenant
spring.ai.vectorstore.chroma.database-name=<ваше имя базы данных> // по умолчанию: SpringAiDatabase

# Свойства коллекции Chroma Vector Store
spring.ai.vectorstore.chroma.initialize-schema=<true or false>
spring.ai.vectorstore.chroma.collection-name=<ваше имя коллекции>

# Свойства конфигурации Chroma Vector Store

# Ключ API OpenAI, если используется автонастройка OpenAI.
spring.ai.openai.api.key=<OpenAI Api-key>
```

Пожалуйста, ознакомьтесь со списком xref:#_configuration_properties[параметров конфигурации] для векторного хранилища, чтобы узнать о значениях по умолчанию и параметрах конфигурации.

Теперь вы можете автоматически подключить Chroma Vector Store в вашем приложении и использовать его

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
```### Свойства конфигурации

Вы можете использовать следующие свойства в вашей конфигурации Spring Boot для настройки векторного хранилища.

[stripes=even]
| Свойство | Описание | Значение по умолчанию |
| --- | --- | --- |
| `spring.ai.vectorstore.chroma.client.host` | Хост подключения к серверу | http://localhost[http://localhost] |
| `spring.ai.vectorstore.chroma.client.port` | Порт подключения к серверу | `8000` |
| `spring.ai.vectorstore.chroma.client.key-token` | Токен доступа (если настроен) | - |
| `spring.ai.vectorstore.chroma.client.username` | Имя пользователя для доступа (если настроено) | - |
| `spring.ai.vectorstore.chroma.client.password` | Пароль для доступа (если настроен) | - |
| `spring.ai.vectorstore.chroma.tenant-name` | Арендатор (обязательно для Chroma Cloud) | `SpringAiTenant` |
| `spring.ai.vectorstore.chroma.database-name` | Имя базы данных (обязательно для Chroma Cloud) | `SpringAiDatabase` |
| `spring.ai.vectorstore.chroma.collection-name` | Имя коллекции | `SpringAiCollection` |
| `spring.ai.vectorstore.chroma.initialize-schema` | Нужно ли инициализировать требуемую схему (создает арендатора/базу данных/коллекцию, если они не существуют) | `false` |

[NOTE]
====
Для ChromaDB, защищенного [Аутентификацией с помощью статического API токена](https://docs.trychroma.com/usage-guide#static-api-token-authentication), используйте метод `ChromaApi#withKeyToken(<Ваши учетные данные токена>)` для установки ваших учетных данных. Проверьте `ChromaWhereIT` для примера.

Для ChromaDB, защищенного [Базовой аутентификацией](https://docs.trychroma.com/usage-guide#basic-authentication), используйте метод `ChromaApi#withBasicAuth(<ваш пользователь>, <ваш пароль>)` для установки ваших учетных данных. Проверьте `BasicAuthChromaWhereIT` для примера.
====

### Конфигурация Chroma Cloud

Для Chroma Cloud вам необходимо предоставить имена арендатора и базы данных из вашей инстанции Chroma Cloud. Вот пример конфигурации:

```properties
# Подключение к Chroma Cloud
spring.ai.vectorstore.chroma.client.host=api.trychroma.com
spring.ai.vectorstore.chroma.client.port=443
spring.ai.vectorstore.chroma.client.key-token=<ваш-api-ключ-chroma-cloud>

# Арендатор и база данных Chroma Cloud (обязательно)
spring.ai.vectorstore.chroma.tenant-name=<ваш-id-арендатора>
spring.ai.vectorstore.chroma.database-name=<ваше-имя-базы-данных>

# Конфигурация коллекции
spring.ai.vectorstore.chroma.collection-name=my-collection
spring.ai.vectorstore.chroma.initialize-schema=true
```

[NOTE]
====
Для Chroma Cloud:
- Хост должен быть `api.trychroma.com`
- Порт должен быть `443` (HTTPS)
- Вы должны предоставить ваш API ключ через `key-token`
- Имена арендатора и базы данных должны соответствовать вашей конфигурации Chroma Cloud
- Установите `initialize-schema=true`, чтобы автоматически создать коллекцию, если она не существует (существующий арендатор/база данных не будут пересозданы)
====

## Фильтрация метаданныхВы также можете использовать универсальные, переносимые [фильтры метаданных](https://docs.spring.io/spring-ai/reference/api/vectordbs.html#_metadata_filters) с хранилищем ChromaVector.

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
                            b.in("john", "jill"),
                            b.eq("article_type", "blog")).build()).build());
```

> **Примечание:** Эти (переносимые) выражения фильтров автоматически преобразуются в собственные выражения фильтров Chroma `where` [фильтры](https://docs.trychroma.com/usage-guide#using-where-filters).

Например, это переносимое выражение фильтра:

```sql
author in ['john', 'jill'] && article_type == 'blog'
```

преобразуется в собственный формат Chroma

```json
{"$and":[
	{"author": {"$in": ["john", "jill"]}},
	{"article_type":{"$eq":"blog"}}]
}
```


## Ручная конфигурация

Если вы предпочитаете настраивать Chroma Vector Store вручную, вы можете сделать это, создав бин `ChromaVectorStore` в вашем приложении Spring Boot.

Добавьте эти зависимости в ваш проект:
- Chroma VectorStore.

```xml
<dependency>
  <groupId>org.springframework.ai</groupId>
  <artifactId>spring-ai-chroma-store</artifactId>
</dependency>
```

- OpenAI: требуется для вычисления встраиваний. Вы можете использовать любую другую реализацию модели встраивания.

```xml
<dependency>
 <groupId>org.springframework.ai</groupId>
 <artifactId>spring-ai-starter-model-openai</artifactId>
</dependency>
```


> **Совет:** Обратитесь к разделу xref:getting-started.adoc#dependency-management[Управление зависимостями], чтобы добавить BOM Spring AI в ваш файл сборки.

### Пример кодаСоздайте экземпляр `RestClient.Builder` с правильными конфигурациями авторизации ChromaDB и используйте его для создания экземпляра `ChromaApi`:

```java
@Bean
public RestClient.Builder builder() {
    return RestClient.builder().requestFactory(new SimpleClientHttpRequestFactory());
}


@Bean
public ChromaApi chromaApi(RestClient.Builder restClientBuilder) {
   String chromaUrl = "http://localhost:8000";
   ChromaApi chromaApi = new ChromaApi(chromaUrl, restClientBuilder);
   return chromaApi;
}
```

Интегрируйтесь с эмбеддингами OpenAI, добавив стартер Spring Boot OpenAI в ваш проект. Это предоставит вам реализацию клиента Embeddings:

```java
@Bean
public VectorStore chromaVectorStore(EmbeddingModel embeddingModel, ChromaApi chromaApi) {
 return ChromaVectorStore.builder(chromaApi, embeddingModel)
    .tenantName("your-tenant-name") // по умолчанию: SpringAiTenant
    .databaseName("your-database-name") // по умолчанию: SpringAiDatabase
    .collectionName("TestCollection")
    .initializeSchema(true)
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

Добавьте документы в ваше векторное хранилище:

```java
vectorStore.add(documents);
```

И, наконец, получите документы, похожие на запрос:

```java
List<Document> results = vectorStore.similaritySearch("Spring");
```

Если все пройдет успешно, вы должны получить документ, содержащий текст "Spring AI rocks!!".


### Запустите Chroma локально

```shell
docker run -it --rm --name chroma -p 8000:8000 ghcr.io/chroma-core/chroma:1.0.0
```

Запускает хранилище chroma по адресу <http://localhost:8000/api/v1>
