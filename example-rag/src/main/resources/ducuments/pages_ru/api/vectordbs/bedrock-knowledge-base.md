# База знаний Amazon Bedrock

Этот раздел проведет вас через настройку базы знаний Amazon Bedrock `VectorStore` для выполнения поиска по сходству в предварительно настроенной базе знаний.

[Базы знаний Amazon Bedrock](https://docs.aws.amazon.com/bedrock/latest/userguide/knowledge-base.html) — это полностью управляемая возможность RAG (Retrieval-Augmented Generation), которая позволяет вам подключать базовые модели к вашим источникам данных. В отличие от других векторных хранилищ, база знаний Bedrock обрабатывает загрузку документов, их разбиение и встраивание внутренне.

## Предварительные требования

1. Учетная запись AWS с включенным доступом к Bedrock
2. Настроенная база знаний Bedrock с как минимум одним синхронизированным источником данных
3. Настроенные учетные данные AWS (через переменные окружения, файл конфигурации AWS или IAM-роль)

[ПРИМЕЧАНИЕ]
====
Это векторное хранилище доступно только для чтения. Документы управляются через процесс синхронизации источников данных базы знаний, а не через методы `add()` или `delete()`.
====

## Автоконфигурация

Spring AI предоставляет автоконфигурацию Spring Boot для векторного хранилища базы знаний Bedrock. Чтобы включить ее, добавьте следующую зависимость в файл `pom.xml` вашего проекта Maven:

```xml
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-starter-vector-store-bedrock-knowledgebase</artifactId>
</dependency>
```

или в файл сборки Gradle `build.gradle`:

```groovy
dependencies {
    implementation 'org.springframework.ai:spring-ai-starter-vector-store-bedrock-knowledgebase'
}
```

> **Совет:** Обратитесь к разделу xref:getting-started.adoc#dependency-management[Управление зависимостями], чтобы добавить BOM Spring AI в ваш файл сборки.

[ПРИМЕЧАНИЕ]
====
В отличие от других векторных хранилищ, база знаний Bedrock не требует бина `EmbeddingModel`. База знаний обрабатывает встраивания внутренне во время синхронизации источников данных.
====

Чтобы подключиться к вашей базе знаний, укажите идентификатор базы знаний через `application.properties` Spring Boot:

```properties
spring.ai.vectorstore.bedrock-knowledge-base.knowledge-base-id=YOUR_KNOWLEDGE_BASE_ID
spring.ai.vectorstore.bedrock-knowledge-base.region=us-east-1
```

Или через переменные окружения:

```bash
export SPRING_AI_VECTORSTORE_BEDROCK_KNOWLEDGE_BASE_KNOWLEDGE_BASE_ID=YOUR_KNOWLEDGE_BASE_ID
```

Теперь вы можете автоматически подключить векторное хранилище в вашем приложении:

```java
@Autowired VectorStore vectorStore;

// ...

// Получить документы, похожие на запрос
List<Document> results = vectorStore.similaritySearch(
    SearchRequest.builder()
        .query("Какова политика возврата?")
        .topK(5)
        .build());
```

### Свойства конфигурации

Вы можете использовать следующие свойства в вашей конфигурации Spring Boot для настройки векторного хранилища базы знаний Bedrock.

[stripes=even]
| Свойство | Описание | Значение по умолчанию |
| --- | --- | --- |
| `spring.ai.vectorstore.bedrock-knowledge-base.knowledge-base-id` | Идентификатор базы знаний Bedrock для запроса | - |
| `spring.ai.vectorstore.bedrock-knowledge-base.region` | Регион AWS для сервиса Bedrock | Значение по умолчанию SDK |
| `spring.ai.vectorstore.bedrock-knowledge-base.top-k` | Количество возвращаемых результатов | 5 |
| `spring.ai.vectorstore.bedrock-knowledge-base.similarity-threshold` | Минимальный балл сходства (от 0.0 до 1.0) | 0.0 |
| `spring.ai.vectorstore.bedrock-knowledge-base.search-type` | Тип поиска: SEMANTIC или HYBRID | null (по умолчанию KB) |
| `spring.ai.vectorstore.bedrock-knowledge-base.reranking-model-arn` | ARN модели повторной сортировки Bedrock | null (отключено) |

## Типы поиска

База знаний Bedrock поддерживает два типа поиска:

- `SEMANTIC` - Поиск по векторному сходству только (по умолчанию)
- `HYBRID` - Сочетает семантический поиск с поиском по ключевым словам

[ПРИМЕЧАНИЕ]
====
Поиск HYBRID доступен только с векторными хранилищами на основе OpenSearch. Векторы S3, Aurora PostgreSQL и другие типы векторных хранилищ поддерживают только семантический поиск.
====

```properties
spring.ai.vectorstore.bedrock-knowledge-base.search-type=HYBRID
```

## Повторная сортировкаВы можете улучшить релевантность поиска, включив модель повторной оценки Bedrock:

```properties
spring.ai.vectorstore.bedrock-knowledge-base.reranking-model-arn=arn:aws:bedrock:us-west-2::foundation-model/amazon.rerank-v1:0
```

Доступные модели повторной оценки:

- Amazon Rerank 1.0 - Доступна в us-west-2, ap-northeast-1, ca-central-1, eu-central-1
- Cohere Rerank 3.5 - Требует подписки на AWS Marketplace

## Фильтрация метаданных

Вы можете использовать универсальные, переносимые [фильтры метаданных](https://docs.spring.io/spring-ai/reference/api/vectordbs.html#_metadata_filters) с хранилищем знаний Bedrock.

Например, вы можете использовать язык выражений текста:

```java
vectorStore.similaritySearch(
    SearchRequest.builder()
        .query("travel policy")
        .topK(5)
        .similarityThreshold(0.5)
        .filterExpression("department == 'HR' && year >= 2024")
        .build());
```

или программно, используя DSL `Filter.Expression`:

```java
FilterExpressionBuilder b = new FilterExpressionBuilder();

vectorStore.similaritySearch(
    SearchRequest.builder()
        .query("travel policy")
        .topK(5)
        .filterExpression(b.and(
            b.eq("department", "HR"),
            b.gte("year", 2024)).build())
        .build());
```

### Поддерживаемые операторы фильтрации

[stripes=even]
| Spring AI | Bedrock | Описание |
| --- | --- | --- |
| EQ | equals | Равно |
| NE | notEquals | Не равно |
| GT | greaterThan | Больше чем |
| GTE | greaterThanOrEquals | Больше или равно |
| LT | lessThan | Меньше чем |
| LTE | lessThanOrEquals | Меньше или равно |
| IN | in | Значение в списке |
| NIN | notIn | Значение не в списке |
| AND | andAll | Логическое И |
| OR | orAll | Логическое ИЛИ |
| NOT | (negation) | Логическое НЕ |

[NOTE]
====
Фильтрация метаданных требует, чтобы документы в вашей базе знаний имели атрибуты метаданных. Для источников данных S3 создайте файлы `.metadata.json` рядом с вашими документами.
====

## Ручная конфигурация

Если вы предпочитаете настраивать векторное хранилище вручную, вы можете сделать это, создав бины напрямую.

Добавьте эту зависимость в ваш проект:

```xml
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-bedrock-knowledgebase-store</artifactId>
</dependency>
```

> **Совет:** Обратитесь к разделу xref:getting-started.adoc#dependency-management[Управление зависимостями], чтобы добавить BOM Spring AI в ваш файл сборки.

### Пример кода

```java
@Bean
public BedrockAgentRuntimeClient bedrockAgentRuntimeClient() {
    return BedrockAgentRuntimeClient.builder()
        .region(Region.US_EAST_1)
        .build();
}

@Bean
public VectorStore vectorStore(BedrockAgentRuntimeClient client) {
    return BedrockKnowledgeBaseVectorStore.builder(client, "YOUR_KNOWLEDGE_BASE_ID")
        .topK(10)
        .similarityThreshold(0.5)
        .searchType(SearchType.SEMANTIC)
        .build();
}
```

Затем используйте векторное хранилище:

```java
List<Document> results = vectorStore.similaritySearch(
    SearchRequest.builder()
        .query("What are the company holidays?")
        .topK(3)
        .build());

for (Document doc : results) {
    System.out.println("Content: " + doc.getText());
    System.out.println("Score: " + doc.getScore());
    System.out.println("Source: " + doc.getMetadata().get("source"));
}
```

## Доступ к нативному клиенту

Векторное хранилище знаний Bedrock предоставляет доступ к базовому нативному клиенту через метод `getNativeClient()`:

```java
BedrockKnowledgeBaseVectorStore vectorStore = context.getBean(BedrockKnowledgeBaseVectorStore.class);
Optional<BedrockAgentRuntimeClient> nativeClient = vectorStore.getNativeClient();

if (nativeClient.isPresent()) {
    BedrockAgentRuntimeClient client = nativeClient.get();
    // Используйте нативный клиент для операций, специфичных для Bedrock
}
```

## Ограничения- **Только для чтения**: Методы `add()` и `delete()` выбрасывают `UnsupportedOperationException`. Документы управляются через процесс синхронизации источника данных Базы Знаний.
- **ГИБРИДНЫЙ поиск**: Доступен только с векторными хранилищами на основе OpenSearch.
- **Доступность повторной сортировки**: Доступность модели варьируется в зависимости от региона AWS.

## Поддерживаемые источники данных

База Знаний Bedrock поддерживает несколько типов источников данных. Местоположение источника включено в метаданные документа:

[stripes=even]
| Источник данных | Поле метаданных | Пример |
| --- | --- | --- |
| S3 | `source` | `s3://bucket/path/document.pdf` |
| Confluence | `source` | `https://confluence.example.com/page/123` |
| SharePoint | `source` | `https://sharepoint.example.com/doc/456` |
| Salesforce | `source` | `https://salesforce.example.com/record/789` |
| Веб-краулер | `source` | `https://example.com/page` |
| Пользовательский | `source` | Пользовательский идентификатор документа |
