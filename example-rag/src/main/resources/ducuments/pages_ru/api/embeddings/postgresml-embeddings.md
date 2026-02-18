# PostgresML Embeddings

Spring AI поддерживает модели текстовых эмбеддингов PostgresML.

Эмбеддинги — это числовое представление текста. 
Они используются для представления слов и предложений в виде векторов, массива чисел. 
Эмбеддинги могут быть использованы для поиска похожих фрагментов текста, сравнивая схожесть числовых векторов с помощью меры расстояния, или могут быть использованы в качестве входных признаков для других моделей машинного обучения, поскольку большинство алгоритмов не может работать с текстом напрямую.

Многие предобученные LLM могут быть использованы для генерации эмбеддингов из текста в PostgresML. 
Вы можете просмотреть все доступные [модели](https://huggingface.co/models?library=sentence-transformers) на Hugging Face, чтобы найти лучшее решение.

## Добавление репозиториев и BOM

Артефакты Spring AI публикуются в репозиториях Maven Central и Spring Snapshot. 
Обратитесь к разделу xref:getting-started.adoc#artifact-repositories[Артефакты репозиториев], чтобы добавить эти репозитории в вашу систему сборки.

Чтобы помочь с управлением зависимостями, Spring AI предоставляет BOM (спецификация материалов), чтобы гарантировать, что в проекте используется согласованная версия Spring AI. Обратитесь к разделу xref:getting-started.adoc#dependency-management[Управление зависимостями], чтобы добавить BOM Spring AI в вашу систему сборки.

## Автоконфигурация

[NOTE]
====
В автоконфигурации Spring AI произошли значительные изменения в названиях артефактов стартовых модулей. 
Пожалуйста, обратитесь к [заметкам об обновлении](https://docs.spring.io/spring-ai/reference/upgrade-notes.html) для получения дополнительной информации.
====

Spring AI предоставляет автоконфигурацию Spring Boot для модели эмбеддингов Azure PostgresML. 
Чтобы включить её, добавьте следующую зависимость в файл `pom.xml` вашего проекта Maven:

```xml
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-starter-model-postgresml-embedding</artifactId>
</dependency>
```

или в файл сборки Gradle `build.gradle`.

```groovy
dependencies {
    implementation 'org.springframework.ai:spring-ai-starter-model-postgresml-embedding'
}
```

> **Совет:** Обратитесь к разделу xref:getting-started.adoc#dependency-management[Управление зависимостями], чтобы добавить BOM Spring AI в ваш файл сборки.

Используйте свойства `spring.ai.postgresml.embedding.options.*` для настройки вашей `PostgresMlEmbeddingModel`.

### Свойства эмбеддингов

[NOTE]
====
Включение и отключение автоконфигураций эмбеддингов теперь настраивается через свойства верхнего уровня с префиксом `spring.ai.model.embedding`.

Чтобы включить, используйте spring.ai.model.embedding=postgresml (по умолчанию включено).

Чтобы отключить, используйте spring.ai.model.embedding=none (или любое значение, которое не соответствует postgresml).

Это изменение сделано для возможности конфигурации нескольких моделей.
====

Префикс `spring.ai.postgresml.embedding` — это префикс свойств, который настраивает реализацию `EmbeddingModel` для эмбеддингов PostgresML.

[cols="3,5,1", stripes=even]
|====
| Свойство | Описание | По умолчанию
| spring.ai.postgresml.embedding.enabled (Удалено и больше не актуально) | Включить модель эмбеддингов PostgresML.  | true
| spring.ai.model.embedding | Включить модель эмбеддингов PostgresML.  | postgresml
| spring.ai.postgresml.embedding.create-extension | Выполнить SQL 'CREATE EXTENSION IF NOT EXISTS pgml' для включения расширения | false
| spring.ai.postgresml.embedding.options.transformer  | Модель трансформера Hugging Face, используемая для эмбеддинга.  | distilbert-base-uncased
| spring.ai.postgresml.embedding.options.kwargs   | Дополнительные параметры, специфичные для трансформера.  | пустая карта
| spring.ai.postgresml.embedding.options.vectorType   | Тип вектора PostgresML, используемый для эмбеддинга. Поддерживаются два варианта: `PG_ARRAY` и `PG_VECTOR`. | PG_ARRAY
| spring.ai.postgresml.embedding.options.metadataMode   | Режим агрегации метаданных документа  | EMBED
|====

> **Совет:** Все свойства с префиксом `spring.ai.postgresml.embedding.options` могут быть переопределены во время выполнения, добавив специфичные для запроса <<embedding-options>> в вызов `EmbeddingRequest`.## Runtime Options [[embedding-options]]

Используйте https://github.com/spring-projects/spring-ai/blob/main/models/spring-ai-openai/src/main/java/org/springframework/ai/postgresml/PostgresMlEmbeddingOptions.java[PostgresMlEmbeddingOptions.java] для настройки `PostgresMlEmbeddingModel` с параметрами, такими как используемая модель и т.д.

При запуске вы можете передать `PostgresMlEmbeddingOptions` в конструктор `PostgresMlEmbeddingModel`, чтобы настроить параметры по умолчанию, используемые для всех запросов на встраивание.

Во время выполнения вы можете переопределить параметры по умолчанию, используя `PostgresMlEmbeddingOptions` в вашем `EmbeddingRequest`.

Например, чтобы переопределить имя модели по умолчанию для конкретного запроса:

```java

EmbeddingResponse embeddingResponse = embeddingModel.call(
    new EmbeddingRequest(List.of("Hello World", "World is big and salvation is near"),
            PostgresMlEmbeddingOptions.builder()
                .transformer("intfloat/e5-small")
                .vectorType(VectorType.PG_ARRAY)
                .kwargs(Map.of("device", "gpu"))
                .build()));
```

## Sample Controller

Это создаст реализацию `EmbeddingModel`, которую вы можете внедрить в свой класс. Вот пример простого класса `@Controller`, который использует реализацию `EmbeddingModel`.

```application.properties
spring.ai.postgresml.embedding.options.transformer=distilbert-base-uncased
spring.ai.postgresml.embedding.options.vectorType=PG_ARRAY
spring.ai.postgresml.embedding.options.metadataMode=EMBED
spring.ai.postgresml.embedding.options.kwargs.device=cpu
```

```java
@RestController
public class EmbeddingController {

    private final EmbeddingModel embeddingModel;

    @Autowired
    public EmbeddingController(EmbeddingModel embeddingModel) {
        this.embeddingModel = embeddingModel;
    }

    @GetMapping("/ai/embedding")
    public Map embed(@RequestParam(value = "message", defaultValue = "Tell me a joke") String message) {
        EmbeddingResponse embeddingResponse = this.embeddingModel.embedForResponse(List.of(message));
        return Map.of("embedding", embeddingResponse);
    }
}
```

## Manual configurationВместо использования автонастройки Spring Boot, вы можете создать `PostgresMlEmbeddingModel` вручную. Для этого добавьте зависимость `spring-ai-postgresml` в файл `pom.xml` вашего проекта на Maven:

```xml
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-postgresml</artifactId>
</dependency>
```

или в файл сборки Gradle `build.gradle`.

```groovy
dependencies {
    implementation 'org.springframework.ai:spring-ai-postgresml'
}
```

> **Совет:** Обратитесь к разделу xref:getting-started.adoc#dependency-management[Управление зависимостями], чтобы добавить Spring AI BOM в ваш файл сборки.

Далее создайте экземпляр `PostgresMlEmbeddingModel` и используйте его для вычисления сходства между двумя входными текстами:

```java
var jdbcTemplate = new JdbcTemplate(dataSource); // ваш источник данных posgresml

PostgresMlEmbeddingModel embeddingModel = new PostgresMlEmbeddingModel(this.jdbcTemplate,
        PostgresMlEmbeddingOptions.builder()
            .transformer("distilbert-base-uncased") // название модели трансформера huggingface.
            .vectorType(VectorType.PG_VECTOR) // тип вектора в PostgreSQL.
            .kwargs(Map.of("device", "cpu")) // необязательные аргументы.
            .metadataMode(MetadataMode.EMBED) // Режим метаданных документа.
            .build());

embeddingModel.afterPropertiesSet(); // инициализируйте jdbc template и базу данных.

EmbeddingResponse embeddingResponse = this.embeddingModel
	.embedForResponse(List.of("Hello World", "World is big and salvation is near"));
```

> **Примечание:** При создании вручную вы должны вызвать `afterPropertiesSet()` после установки свойств и перед использованием клиента. Более удобно (и предпочтительно) создавать PostgresMlEmbeddingModel как `@Bean`. Тогда вам не нужно будет вызывать `afterPropertiesSet()` вручную:

```java
@Bean
public EmbeddingModel embeddingModel(JdbcTemplate jdbcTemplate) {
    return new PostgresMlEmbeddingModel(jdbcTemplate,
        PostgresMlEmbeddingOptions.builder()
             ....
            .build());
}
```
