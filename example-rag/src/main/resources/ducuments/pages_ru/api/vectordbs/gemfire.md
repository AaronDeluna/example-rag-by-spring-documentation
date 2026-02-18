# GemFire Vector Store

В этом разделе описывается, как настроить `GemFireVectorStore` для хранения векторных представлений документов и выполнения поиска по сходству.

[GemFire](https://tanzu.vmware.com/gemfire) — это распределенное хранилище ключ-значение в памяти, выполняющее операции чтения и записи с невероятно высокой скоростью. Оно предлагает высокодоступные параллельные очереди сообщений, непрерывную доступность и событийно-ориентированную архитектуру, которую можно динамически масштабировать без простоя. По мере увеличения требований к объему данных для поддержки высокопроизводительных приложений в реальном времени GemFire может легко масштабироваться линейно.

[GemFire VectorDB](https://docs.vmware.com/en/VMware-GemFire-VectorDB/1.0/gemfire-vectordb/overview.html) расширяет возможности GemFire, выступая в качестве универсальной векторной базы данных, которая эффективно хранит, извлекает и выполняет поиск по векторному сходству.

## Предварительные требования

1. Кластер GemFire с включенным расширением GemFire VectorDB
- [Установите расширение GemFire VectorDB](https://docs.vmware.com/en/VMware-GemFire-VectorDB/1.0/gemfire-vectordb/install.html)

2. Бин `EmbeddingModel` для вычисления векторных представлений документов. Обратитесь к разделу xref:api/embeddings.adoc#available-implementations[EmbeddingModel] для получения дополнительной информации.
Опция, которая работает локально на вашем компьютере, — это xref:api/embeddings/onnx.adoc[ONNX] и все-MiniLM-L6-v2 Sentence Transformers.

## Автоконфигурация

[NOTE]
====
В автоконфигурации Spring AI произошли значительные изменения в названиях артефактов стартовых модулей.
Пожалуйста, обратитесь к https://docs.spring.io/spring-ai/reference/upgrade-notes.html[заметкам об обновлении] для получения дополнительной информации.
====

Добавьте стартовый модуль GemFire VectorStore Spring Boot в файл сборки Maven вашего проекта `pom.xml`:

```xml
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-starter-vector-store-gemfire</artifactId>
</dependency>
```

или в файл Gradle `build.gradle`

```xml
dependencies {
    implementation 'org.springframework.ai:spring-ai-starter-vector-store-gemfire'
}
```

### Свойства конфигурации

Вы можете использовать следующие свойства в конфигурации Spring Boot для дальнейшей настройки `GemFireVectorStore`.

[stripes=even]
| Свойство | Значение по умолчанию |
| --- | --- |
| `spring.ai.vectorstore.gemfire.host` | localhost |
| `spring.ai.vectorstore.gemfire.port` | 8080 |
| `spring.ai.vectorstore.gemfire.initialize-schema` | `false` |
| `spring.ai.vectorstore.gemfire.index-name` | spring-ai-gemfire-store |
| `spring.ai.vectorstore.gemfire.beam-width` | 100 |
| `spring.ai.vectorstore.gemfire.max-connections` | 16 |
| `spring.ai.vectorstore.gemfire.vector-similarity-function` | COSINE |
| `spring.ai.vectorstore.gemfire.fields` | [] |
| `spring.ai.vectorstore.gemfire.buckets` | 0 |
| `spring.ai.vectorstore.gemfire.username` | null |
| `spring.ai.vectorstore.gemfire.password` | null |
| `spring.ai.vectorstore.gemfire.token` | null |


## Ручная конфигурация

Чтобы использовать только `GemFireVectorStore`, без автоконфигурации Spring Boot, добавьте следующую зависимость в файл Maven `pom.xml` вашего проекта:

```xml
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-gemfire-store</artifactId>
</dependency>
```

Для пользователей Gradle добавьте следующее в файл `build.gradle` в блок зависимостей, чтобы использовать только `GemFireVectorStore`:

[souce, xml]
```
dependencies {
    implementation 'org.springframework.ai:spring-ai-gemfire-store'
}
```

## ИспользованиеВот пример, который создает экземпляр `GemfireVectorStore` вместо использования автонастройки

```java
@Bean
public GemFireVectorStore vectorStore(EmbeddingModel embeddingModel) {
    return GemFireVectorStore.builder(embeddingModel)
        .host("localhost")
        .port(7071)
        .username("my-user-name")
        .password("my-password")
        .indexName("my-vector-index")
        .fields(new String[] {"country", "year", "activationDate"}) // Необязательно: поля для фильтрации метаданных
        .initializeSchema(true)
        .build();
}
```

[ПРИМЕЧАНИЕ]
====
Настройка по умолчанию подключается к кластеру GemFire по адресу `localhost:8080`
====

- В вашем приложении создайте несколько документов:

```java
List<Document> documents = List.of(
   new Document("Spring AI rocks!! Spring AI rocks!! Spring AI rocks!! Spring AI rocks!! Spring AI rocks!!", Map.of("country", "UK", "year", 2020)),
   new Document("The World is Big and Salvation Lurks Around the Corner", Map.of()),
   new Document("You walk forward facing the past and you turn back toward the future.", Map.of("country", "NL", "year", 2023)));
```

- Добавьте документы в векторное хранилище:

```java
vectorStore.add(documents);
```

- И для извлечения документов с использованием поиска по сходству:

```java
List<Document> results = vectorStore.similaritySearch(
   SearchRequest.builder().query("Spring").topK(5).build());
```

Вы должны получить документ, содержащий текст "Spring AI rocks!!".

Вы также можете ограничить количество результатов, используя порог сходства:
```java
List<Document> results = vectorStore.similaritySearch(
   SearchRequest.builder().query("Spring").topK(5)
      .similarityThreshold(0.5d).build());
```

## Фильтрация метаданныхВы также можете использовать универсальные, переносимые xref:api/vectordbs.adoc#metadata-filters[фильтры метаданных] с GemFire VectorStore.

Например, вы можете использовать либо язык выражений текста:

```java
vectorStore.similaritySearch(SearchRequest.builder()
        .query("The World")
        .topK(5)
        .similarityThreshold(0.7)
        .filterExpression("country == 'BG' && year >= 2020").build());
```

либо программно, используя `Filter.Expression` DSL:

```java
FilterExpressionBuilder b = new FilterExpressionBuilder();

vectorStore.similaritySearch(SearchRequest.builder()
        .query("The World")
        .topK(5)
        .similarityThreshold(0.7)
        .filterExpression(b.and(
                b.eq("country", "BG"),
                b.gte("year", 2020)).build()).build());
```

> **Примечание:** Эти (переносимые) выражения фильтров автоматически преобразуются в проприетарный формат запроса GemFire VectorDB.

Например, это переносимое выражение фильтра:

```sql
country == 'BG' && year >= 2020
```

преобразуется в проприетарный формат фильтра GemFire VectorDB:

```
country:BG AND year:[2020 TO *]
```

GemFire VectorStore поддерживает широкий спектр операций фильтрации:

- **Равенство**: `country == 'BG'` → `country:BG`
- **Неравенство**: `city != 'Sofia'` → `city: NOT Sofia`
- **Больше чем**: `year > 2020` → `year:{2020 TO *]`
- **Больше или равно**: `year >= 2020` → `year:[2020 TO *]`
- **Меньше чем**: `year < 2025` → `year:[* TO 2025}`
- **Меньше или равно**: `year <= 2025` → `year:[* TO 2025]`
- **IN**: `country in ['BG', 'NL']` → `country:(BG OR NL)`
- **NOT IN**: `country nin ['BG', 'NL']` → `NOT country:(BG OR NL)`
- **AND/OR**: Логические операторы для комбинирования условий
- **Группировка**: Используйте скобки для сложных выражений
- **Фильтрация по дате**: Дата в формате ISO 8601 (например, `2024-01-07T14:29:12Z`)

[ВАЖНО]
====
Чтобы использовать фильтрацию метаданных с GemFire VectorStore, вы должны указать поля метаданных, которые могут быть отфильтрованы, при создании векторного хранилища. Это делается с помощью параметра `fields` в билдере:

```java
GemFireVectorStore.builder(embeddingModel)
    .fields(new String[] {"country", "year", "activationDate"})
    .build();
```

Или через свойства конфигурации:

```properties
spring.ai.vectorstore.gemfire.fields=country,year,activationDate
```
====
