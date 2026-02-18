# Weaviate

Этот раздел проведет вас через процесс настройки Weaviate VectorStore для хранения векторных представлений документов и выполнения поиска по сходству.

[Weaviate](https://weaviate.io/) — это открытая векторная база данных, которая позволяет хранить объекты данных и векторные представления из ваших любимых моделей машинного обучения и без проблем масштабироваться до миллиардов объектов данных. Она предоставляет инструменты для хранения векторных представлений документов, контента и метаданных, а также для поиска по этим векторным представлениям, включая фильтрацию по метаданным.

## Предварительные требования

- Запущенный экземпляр Weaviate. Доступны следующие варианты:
** [Weaviate Cloud Service](https://console.weaviate.cloud/) (требуется создание учетной записи и API-ключ)
** [Docker-контейнер](https://weaviate.io/developers/weaviate/installation/docker)
- При необходимости, API-ключ для xref:api/embeddings.adoc#available-implementations[EmbeddingModel], чтобы генерировать векторные представления, хранящиеся в `WeaviateVectorStore`.

## Зависимости

[NOTE]
====
В Spring AI произошли значительные изменения в автонастройке и названиях артефактов стартовых модулей. Пожалуйста, обратитесь к https://docs.spring.io/spring-ai/reference/upgrade-notes.html[заметкам об обновлении] для получения дополнительной информации.
====

Добавьте зависимость Weaviate Vector Store в ваш проект:

```xml
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-weaviate-store</artifactId>
</dependency>
```

или в ваш файл сборки Gradle `build.gradle`.

```groovy
dependencies {
    implementation 'org.springframework.ai:spring-ai-weaviate-store'
}
```

> **Совет:** Обратитесь к xref:getting-started.adoc#dependency-management[разделу управления зависимостями], чтобы добавить Spring AI BOM в ваш файл сборки.

## Конфигурация

Чтобы подключиться к Weaviate и использовать `WeaviateVectorStore`, вам необходимо предоставить данные доступа к вашему экземпляру. Конфигурацию можно предоставить через _application.properties_ Spring Boot:

```properties
spring.ai.vectorstore.weaviate.host=<host_of_your_weaviate_instance>
spring.ai.vectorstore.weaviate.scheme=<http_or_https>
spring.ai.vectorstore.weaviate.api-key=<your_api_key>
# API-ключ, если требуется, например, OpenAI
spring.ai.openai.api-key=<api-key>
```

Если вы предпочитаете использовать переменные окружения для конфиденциальной информации, такой как API-ключи, у вас есть несколько вариантов:

### Вариант 1: Использование языка выражений Spring (SpEL)

Вы можете использовать пользовательские имена переменных окружения и ссылаться на них в вашей конфигурации приложения:

```yaml
# В application.yml
spring:
  ai:
    vectorstore:
      weaviate:
        host: ${WEAVIATE_HOST}
        scheme: ${WEAVIATE_SCHEME}
        api-key: ${WEAVIATE_API_KEY}
    openai:
      api-key: ${OPENAI_API_KEY}
```

```bash
# В вашем окружении или .env файле
export WEAVIATE_HOST=<host_of_your_weaviate_instance>
export WEAVIATE_SCHEME=<http_or_https>
export WEAVIATE_API_KEY=<your_api_key>
export OPENAI_API_KEY=<api-key>
```

### Вариант 2: Доступ к переменным окружения программно

В качестве альтернативы, вы можете получить доступ к переменным окружения в вашем Java-коде:

```java
String weaviateApiKey = System.getenv("WEAVIATE_API_KEY");
String openAiApiKey = System.getenv("OPENAI_API_KEY");
```

> **Примечание:** Если вы решите создать shell-скрипт для управления вашими переменными окружения, обязательно выполните его перед запуском вашего приложения, "подгрузив" файл, т.е. `source <your_script_name>.sh`.

## АвтонастройкаSpring AI предоставляет автонастройку Spring Boot для Weaviate Vector Store. Чтобы включить её, добавьте следующую зависимость в файл `pom.xml` вашего проекта:

```xml
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-starter-vector-store-weaviate</artifactId>
</dependency>
```

или в файл сборки Gradle `build.gradle`.

```groovy
dependencies {
    implementation 'org.springframework.ai:spring-ai-starter-vector-store-weaviate'
}
```

> **Совет:** Ознакомьтесь с разделом xref:getting-started.adoc#dependency-management[Управление зависимостями], чтобы добавить Spring AI BOM в ваш файл сборки.

Пожалуйста, ознакомьтесь со списком xref:#_weaviatevectorstore_properties[параметров конфигурации] для векторного хранилища, чтобы узнать о значениях по умолчанию и параметрах конфигурации.

> **Совет:** Ознакомьтесь с разделом xref:getting-started.adoc#artifact-repositories[Артефактные репозитории], чтобы добавить Maven Central и/или Snapshot репозитории в ваш файл сборки.

Кроме того, вам потребуется настроенный бин `EmbeddingModel`. Ознакомьтесь с разделом xref:api/embeddings.adoc#available-implementations[EmbeddingModel] для получения дополнительной информации.

Вот пример необходимого бина:

```java
@Bean
public EmbeddingModel embeddingModel() {
    // Получите API-ключ из безопасного источника или переменной окружения
    String apiKey = System.getenv("OPENAI_API_KEY");

    // Может быть любая другая реализация EmbeddingModel
    return new OpenAiEmbeddingModel(OpenAiApi.builder().apiKey(apiKey).build());
}
```

Теперь вы можете автоматически подключить `WeaviateVectorStore` как векторное хранилище в вашем приложении.

## Ручная конфигурация

Вместо использования автонастройки Spring Boot вы можете вручную настроить `WeaviateVectorStore`, используя паттерн строителя:

```java
@Bean
public WeaviateClient weaviateClient() {
    return new WeaviateClient(new Config("http", "localhost:8080"));
}

@Bean
public VectorStore vectorStore(WeaviateClient weaviateClient, EmbeddingModel embeddingModel) {
    return WeaviateVectorStore.builder(weaviateClient, embeddingModel)
        .options(options)                              // Необязательно: используйте пользовательские параметры
        .consistencyLevel(ConsistentLevel.QUORUM)      // Необязательно: по умолчанию ConsistentLevel.ONE
        .filterMetadataFields(List.of(                 // Необязательно: поля, которые могут использоваться в фильтрах
            MetadataField.text("country"),
            MetadataField.number("year")))
        .build();
}
```

## Фильтрация метаданныхВы также можете использовать универсальные, переносимые xref:api/vectordbs.adoc#metadata-filters[фильтры метаданных] с хранилищем Weaviate.

Например, вы можете использовать либо язык выражений текста:

```java
vectorStore.similaritySearch(
    SearchRequest.builder()
        .query("The World")
        .topK(TOP_K)
        .similarityThreshold(SIMILARITY_THRESHOLD)
        .filterExpression("country in ['UK', 'NL'] && year >= 2020").build());
```

либо программно, используя `Filter.Expression` DSL:

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

> **Примечание:** Эти (переносимые) выражения фильтров автоматически преобразуются в собственные Weaviate [где фильтры](https://weaviate.io/developers/weaviate/api/graphql/filters).

Например, это переносимое выражение фильтра:

```sql
country in ['UK', 'NL'] && year >= 2020
```

преобразуется в собственный формат фильтра GraphQL Weaviate:

```graphql
operator: And
operands:
    [{
        operator: Or
        operands:
            [{
                path: ["meta_country"]
                operator: Equal
                valueText: "UK"
            },
            {
                path: ["meta_country"]
                operator: Equal
                valueText: "NL"
            }]
    },
    {
        path: ["meta_year"]
        operator: GreaterThanEqual
        valueNumber: 2020
    }]
```

## Запуск Weaviate в Docker

Чтобы быстро начать работу с локальным экземпляром Weaviate, вы можете запустить его в Docker:

```bash
docker run -it --rm --name weaviate \
    -e AUTHENTICATION_ANONYMOUS_ACCESS_ENABLED=true \
    -e PERSISTENCE_DATA_PATH=/var/lib/weaviate \
    -e QUERY_DEFAULTS_LIMIT=25 \
    -e DEFAULT_VECTORIZER_MODULE=none \
    -e CLUSTER_HOSTNAME=node1 \
    -p 8080:8080 \
    semitechnologies/weaviate:1.22.4
```

Это запускает экземпляр Weaviate, доступный по адресу http://localhost:8080.

## Свойства WeaviateVectorStore

Вы можете использовать следующие свойства в вашей конфигурации Spring Boot для настройки векторного хранилища Weaviate.

[stripes=even]
| Свойство | Описание | Значение по умолчанию |
| --- | --- | --- |
| `spring.ai.vectorstore.weaviate.host` | Хост сервера Weaviate | localhost:8080 |
| `spring.ai.vectorstore.weaviate.scheme` | Схема подключения | http |
| `spring.ai.vectorstore.weaviate.api-key` | API-ключ для аутентификации |  |
| `spring.ai.vectorstore.weaviate.object-class` | Имя класса для хранения документов. | SpringAiWeaviate |
| `spring.ai.vectorstore.weaviate.content-field-name` | Имя поля для содержимого | content |
| `spring.ai.vectorstore.weaviate.meta-field-prefix` | Префикс поля для метаданных | meta_ |
| `spring.ai.vectorstore.weaviate.consistency-level` | Желаемый компромисс между согласованностью и скоростью | ConsistentLevel.ONE |
| `spring.ai.vectorstore.weaviate.filter-field` | Настраивает поля метаданных, которые могут использоваться в фильтрах. Формат: spring.ai.vectorstore.weaviate.filter-field.<field-name>=<field-type> |  |

> **Совет:** Имена классов объектов должны начинаться с заглавной буквы, а имена полей — с маленькой буквы.
Смотрите [data-object-concepts](https://weaviate.io/developers/weaviate/concepts/data#data-object-concepts)

## Доступ к нативному клиентуРеализация Weaviate Vector Store предоставляет доступ к базовому нативному клиенту Weaviate (`WeaviateClient`) через метод `getNativeClient()`:

```java
WeaviateVectorStore vectorStore = context.getBean(WeaviateVectorStore.class);
Optional<WeaviateClient> nativeClient = vectorStore.getNativeClient();

if (nativeClient.isPresent()) {
    WeaviateClient client = nativeClient.get();
    // Используйте нативный клиент для операций, специфичных для Weaviate
}
```

Нативный клиент предоставляет доступ к функциям и операциям, специфичным для Weaviate, которые могут не быть доступны через интерфейс `VectorStore`.
