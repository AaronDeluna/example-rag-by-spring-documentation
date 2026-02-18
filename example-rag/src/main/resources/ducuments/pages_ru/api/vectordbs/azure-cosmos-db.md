# Azure Cosmos DB

В этом разделе описывается, как настроить `CosmosDBVectorStore` для хранения векторных представлений документов и выполнения поисков по сходству.

## Что такое Azure Cosmos DB?

[Azure Cosmos DB](https://azure.microsoft.com/en-us/services/cosmos-db/) — это облачный сервис базы данных от Microsoft, который распределен по всему миру и предназначен для критически важных приложений.
Он предлагает высокую доступность, низкую задержку и возможность горизонтального масштабирования для удовлетворения современных требований приложений.
Сервис был создан с нуля с акцентом на глобальное распределение, детализированную многопользовательскую архитектуру и горизонтальное масштабирование.
Это основополагающий сервис в Azure, который используется большинством критически важных приложений Microsoft в глобальном масштабе, включая Teams, Skype, Xbox Live, Office 365, Bing, Azure Active Directory, Azure Portal, Microsoft Store и многие другие.
Он также используется тысячами внешних клиентов, включая OpenAI для ChatGPT и других критически важных AI-приложений, которые требуют эластичного масштабирования, готового глобального распределения, а также низкой задержки и высокой доступности по всему миру.

## Что такое DiskANN?

DiskANN (поиск ближайших соседей на основе диска) — это инновационная технология, используемая в Azure Cosmos DB для повышения производительности векторных поисков.
Она позволяет эффективно и масштабируемо выполнять поиски по сходству в высокоразмерных данных, индексируя векторные представления, хранящиеся в Cosmos DB.

DiskANN предоставляет следующие преимущества:

- **Эффективность**: Используя структуры на основе диска, DiskANN значительно сокращает время, необходимое для поиска ближайших соседей по сравнению с традиционными методами.
- **Масштабируемость**: Он может обрабатывать большие наборы данных, превышающие емкость памяти, что делает его подходящим для различных приложений, включая машинное обучение и решения на основе AI.
- **Низкая задержка**: DiskANN минимизирует задержку во время операций поиска, обеспечивая быстрое получение результатов даже при значительных объемах данных.

В контексте Spring AI для Azure Cosmos DB векторные поиски будут создавать и использовать индексы DiskANN для обеспечения оптимальной производительности запросов по сходству.

## Настройка Azure Cosmos DB Vector Store с автоматической конфигурацией```markdown
Следующий код демонстрирует, как настроить `CosmosDBVectorStore` с автонастройкой:

```java
package com.example.demo;

import io.micrometer.observation.ObservationRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootApplication
@EnableAutoConfiguration
public class DemoApplication implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DemoApplication.class);

    @Lazy
    @Autowired
    private VectorStore vectorStore;

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        Document document1 = new Document(UUID.randomUUID().toString(), "Sample content1", Map.of("key1", "value1"));
        Document document2 = new Document(UUID.randomUUID().toString(), "Sample content2", Map.of("key2", "value2"));
		this.vectorStore.add(List.of(document1, document2));
        List<Document> results = this.vectorStore.similaritySearch(SearchRequest.builder().query("Sample content").topK(1).build());

        log.info("Search results: {}", results);

        // Удалите документы из векторного хранилища
		this.vectorStore.delete(List.of(document1.getId(), document2.getId()));
    }

    @Bean
    public ObservationRegistry observationRegistry() {
        return ObservationRegistry.create();
    }
}
```

## Автонастройка

[ПРИМЕЧАНИЕ]
====
В автонастройке Spring AI произошли значительные изменения в названиях артефактов модулей-стартеров.
Пожалуйста, обратитесь к https://docs.spring.io/spring-ai/reference/upgrade-notes.html[заметкам об обновлении] для получения дополнительной информации.
====

Добавьте следующую зависимость в ваш проект Maven:

```xml
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-starter-vector-store-azure-cosmos-db</artifactId>
</dependency>
```

## Свойства конфигурации

Следующие свойства конфигурации доступны для векторного хранилища Cosmos DB:

[stripes=even]
| Свойство | Описание |
| --- | --- |
| spring.ai.vectorstore.cosmosdb.databaseName | Имя базы данных Cosmos DB, которую нужно использовать. |
| spring.ai.vectorstore.cosmosdb.containerName | Имя контейнера Cosmos DB, который нужно использовать. |
| spring.ai.vectorstore.cosmosdb.partitionKeyPath | Путь для ключа раздела. |
| spring.ai.vectorstore.cosmosdb.metadataFields | Список полей метаданных, разделенных запятыми. |
| spring.ai.vectorstore.cosmosdb.vectorStoreThroughput | Пропускная способность для векторного хранилища. |
| spring.ai.vectorstore.cosmosdb.vectorDimensions | Количество измерений для векторов. |
| spring.ai.vectorstore.cosmosdb.endpoint | Конечная точка для Cosmos DB. |
| spring.ai.vectorstore.cosmosdb.key | Ключ для Cosmos DB (если ключ отсутствует, будет использован [DefaultAzureCredential](https://learn.microsoft.com/azure/developer/java/sdk/authentication/credential-chains#defaultazurecredential-overview)). |


## Сложные поиски с фильтрами
```Вы можете выполнять более сложные поисковые запросы, используя фильтры в хранилище векторов Cosmos DB. Ниже приведен пример, демонстрирующий, как использовать фильтры в ваших поисковых запросах.

```java
Map<String, Object> metadata1 = new HashMap<>();
metadata1.put("country", "UK");
metadata1.put("year", 2021);
metadata1.put("city", "London");

Map<String, Object> metadata2 = new HashMap<>();
metadata2.put("country", "NL");
metadata2.put("year", 2022);
metadata2.put("city", "Amsterdam");

Document document1 = new Document("1", "Документ о Великобритании", this.metadata1);
Document document2 = new Document("2", "Документ о Нидерландах", this.metadata2);

vectorStore.add(List.of(document1, document2));

FilterExpressionBuilder builder = new FilterExpressionBuilder();
List<Document> results = vectorStore.similaritySearch(SearchRequest.builder().query("The World")
    .topK(10)
    .filterExpression((this.builder.in("country", "UK", "NL")).build()).build());
```

## Настройка Azure Cosmos DB Vector Store без автонастройки

Следующий код демонстрирует, как настроить `CosmosDBVectorStore` без использования автонастройки. Рекомендуется использовать [DefaultAzureCredential](https://learn.microsoft.com/azure/developer/java/sdk/authentication/credential-chains#defaultazurecredential-overview) для аутентификации в Azure Cosmos DB.

```java
@Bean
public VectorStore vectorStore(ObservationRegistry observationRegistry) {
    // Создание клиента Cosmos DB
    CosmosAsyncClient cosmosClient = new CosmosClientBuilder()
            .endpoint(System.getenv("COSMOSDB_AI_ENDPOINT"))
            .credential(new DefaultAzureCredentialBuilder().build())
            .userAgentSuffix("SpringAI-CDBNoSQL-VectorStore")
            .gatewayMode()
            .buildAsyncClient();

    // Создание и настройка хранилища векторов
    return CosmosDBVectorStore.builder(cosmosClient, embeddingModel)
            .databaseName("test-database")
            .containerName("test-container")
            // Настройка полей метаданных для фильтрации
            .metadataFields(List.of("country", "year", "city"))
            // Установка пути к ключу раздела (необязательно)
            .partitionKeyPath("/id")
            // Настройка параметров производительности
            .vectorStoreThroughput(1000)
            .vectorDimensions(1536)  // Соответствует размерностям вашей модели встраивания
            // Добавление пользовательской стратегии пакетирования (необязательно)
            .batchingStrategy(new TokenCountBatchingStrategy())
            // Добавление реестра наблюдений для метрик
            .observationRegistry(observationRegistry)
            .build();
}

@Bean
public EmbeddingModel embeddingModel() {
    return new TransformersEmbeddingModel();
}
```

Эта конфигурация показывает все доступные параметры сборки:

- `databaseName`: Имя вашей базы данных Cosmos DB
- `containerName`: Имя вашего контейнера в базе данных
- `partitionKeyPath`: Путь для ключа раздела (например, "/id")
- `metadataFields`: Список полей метаданных, которые будут использоваться для фильтрации
- `vectorStoreThroughput`: Пропускная способность (RU/s) для контейнера хранилища векторов
- `vectorDimensions`: Количество размерностей для ваших векторов (должно соответствовать вашей модели встраивания)
- `batchingStrategy`: Стратегия пакетирования операций с документами (необязательно)

## Ручная настройка зависимостей

Добавьте следующую зависимость в ваш проект Maven:

```xml
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-azure-cosmos-db-store</artifactId>
</dependency>
```

## Доступ к нативному клиентуРеализация хранилища векторов Azure Cosmos DB предоставляет доступ к базовому нативному клиенту Azure Cosmos DB (`CosmosClient`) через метод `getNativeClient()`:

```java
CosmosDBVectorStore vectorStore = context.getBean(CosmosDBVectorStore.class);
Optional<CosmosClient> nativeClient = vectorStore.getNativeClient();

if (nativeClient.isPresent()) {
    CosmosClient client = nativeClient.get();
    // Используйте нативный клиент для операций, специфичных для Azure Cosmos DB
}
```

Нативный клиент предоставляет доступ к функциям и операциям, специфичным для Azure Cosmos DB, которые могут не быть доступны через интерфейс `VectorStore`.
