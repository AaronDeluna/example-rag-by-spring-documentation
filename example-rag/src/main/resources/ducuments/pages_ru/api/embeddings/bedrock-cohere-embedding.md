# Cohere Embeddings

Предоставляет модель встраивания Cohere от Bedrock. Интегрируйте возможности генеративного ИИ в основные приложения и рабочие процессы, которые улучшают бизнес-результаты.

Страница модели AWS Bedrock Cohere и [Руководство пользователя Amazon Bedrock](https://docs.aws.amazon.com/bedrock/latest/userguide/what-is-bedrock.html) содержат подробную информацию о том, как использовать модель, размещенную в AWS.

## Предварительные требования

Обратитесь к [документации Spring AI по Amazon Bedrock](xref:api/bedrock.adoc) для настройки доступа к API.

### Добавление репозиториев и BOM

Артефакты Spring AI публикуются в репозиториях Maven Central и Spring Snapshot. Обратитесь к разделу [Артефакты репозиториев](xref:getting-started.adoc#artifact-repositories), чтобы добавить эти репозитории в вашу систему сборки.

Чтобы помочь с управлением зависимостями, Spring AI предоставляет BOM (спецификация материалов), чтобы гарантировать, что одна и та же версия Spring AI используется на протяжении всего проекта. Обратитесь к разделу [Управление зависимостями](xref:getting-started.adoc#dependency-management), чтобы добавить BOM Spring AI в вашу систему сборки.

## Автоконфигурация

[ПРИМЕЧАНИЕ]
====
В автоконфигурации Spring AI произошли значительные изменения, касающиеся имен артефактов стартовых модулей. Пожалуйста, обратитесь к [заметкам об обновлении](https://docs.spring.io/spring-ai/reference/upgrade-notes.html) для получения дополнительной информации.
====

Добавьте зависимость `spring-ai-starter-model-bedrock` в файл `pom.xml` вашего проекта Maven:

```xml
<dependency>
  <groupId>org.springframework.ai</groupId>
  <artifactId>spring-ai-starter-model-bedrock</artifactId>
</dependency>
```

или в файл сборки Gradle `build.gradle`.

```gradle
dependencies {
    implementation 'org.springframework.ai:spring-ai-starter-model-bedrock'
}
```

> **Совет:** Обратитесь к разделу [Управление зависимостями](xref:getting-started.adoc#dependency-management), чтобы добавить BOM Spring AI в ваш файл сборки.

### Включение поддержки встраивания Cohere

По умолчанию модель встраивания Cohere отключена. Чтобы включить ее, установите свойство `spring.ai.model.embedding` в значение `bedrock-cohere` в конфигурации вашего приложения:

```properties
spring.ai.model.embedding=bedrock-cohere
```

В качестве альтернативы вы можете использовать язык выражений Spring (SpEL) для ссылки на переменную окружения:

```yaml
# В application.yml
spring:
  ai:
    model:
      embedding: ${AI_MODEL_EMBEDDING}
```

```bash
# В вашей среде или .env файле
export AI_MODEL_EMBEDDING=bedrock-cohere
```

Вы также можете установить это свойство, используя системные свойства Java при запуске вашего приложения:

```shell
java -Dspring.ai.model.embedding=bedrock-cohere -jar your-application.jar
```

### Свойства встраивания

Префикс `spring.ai.bedrock.aws` — это префикс свойств для настройки подключения к AWS Bedrock.

[cols="3,4,1", stripes=even]
|====
| Свойство | Описание | По умолчанию

| spring.ai.bedrock.aws.region     | Регион AWS для использования. | us-east-1
| spring.ai.bedrock.aws.access-key | Ключ доступа AWS.  | -
| spring.ai.bedrock.aws.secret-key | Секретный ключ AWS.  | -
| spring.ai.bedrock.aws.profile.name | Имя профиля AWS.  | -
| spring.ai.bedrock.aws.profile.credentials-path | Путь к файлу учетных данных AWS.  | -
| spring.ai.bedrock.aws.profile.configuration-path | Путь к файлу конфигурации AWS.  | -
|====

[ПРИМЕЧАНИЕ]
====
Включение и отключение автоконфигураций встраивания теперь настраивается через свойства верхнего уровня с префиксом `spring.ai.model.embedding`.

Чтобы включить, установите `spring.ai.model.embedding=bedrock-cohere` (по умолчанию включено).

Чтобы отключить, установите `spring.ai.model.embedding=none` (или любое значение, которое не соответствует bedrock-cohere).

Это изменение сделано для того, чтобы позволить конфигурацию нескольких моделей.
====

Префикс `spring.ai.bedrock.cohere.embedding` (определенный в `BedrockCohereEmbeddingProperties`) — это префикс свойств, который настраивает реализацию модели встраивания для Cohere.

[cols="3,4,1", stripes=even]
|====
| Свойство | Описание | По умолчанию
| spring.ai.model.embedding           | Включить или отключить поддержку для Cohere  | bedrock-cohere
| spring.ai.bedrock.cohere.embedding.enabled (Удалено и больше не актуально)             | Включить или отключить поддержку для Cohere  | false
| spring.ai.bedrock.cohere.embedding.model                | Идентификатор модели для использования. См. [CohereEmbeddingModel](https://github.com/spring-projects/spring-ai/blob/056b95a00efa5b014a1f488329fbd07a46c02378/models/spring-ai-bedrock/src/main/java/org/springframework/ai/bedrock/cohere/api/CohereEmbeddingBedrockApi.java#L150) для поддерживаемых моделей.  | cohere.embed-multilingual-v3
| spring.ai.bedrock.cohere.embedding.options.input-type  | Добавляет специальные токены для различения каждого типа друг от друга. Вы не должны смешивать разные типы вместе, за исключением случаев, когда смешиваете типы для поиска и извлечения. В этом случае встраивайте ваш корпус с типом search_document и встроенные запросы с типом search_query.  | SEARCH_DOCUMENT
| spring.ai.bedrock.cohere.embedding.options.truncate  | Указывает, как API обрабатывает входные данные, превышающие максимальную длину токена. Если вы укажете LEFT или RIGHT, модель отбросит входные данные до тех пор, пока оставшиеся данные не станут точно максимальной длиной токена для модели.  | NONE
|====

> **Примечание:** При доступе к Cohere через Amazon Bedrock функция обрезки недоступна. Это проблема с Amazon Bedrock. Класс Spring AI `BedrockCohereEmbeddingModel` будет обрезать до 2048 символов, что является максимальным, поддерживаемым моделью.

Посмотрите [CohereEmbeddingModel](https://github.com/spring-projects/spring-ai/blob/056b95a00efa5b014a1f488329fbd07a46c02378/models/spring-ai-bedrock/src/main/java/org/springframework/ai/bedrock/cohere/api/CohereEmbeddingBedrockApi.java#L150) для других идентификаторов моделей. Поддерживаемые значения: `cohere.embed-multilingual-v3` и `cohere.embed-english-v3`. Значения идентификаторов моделей также можно найти в [документации AWS Bedrock по базовым идентификаторам моделей](https://docs.aws.amazon.com/bedrock/latest/userguide/model-ids-arns.html).

> **Совет:** Все свойства с префиксом `spring.ai.bedrock.cohere.embedding.options` могут быть переопределены во время выполнения, добавив специфичные для запроса <<embedding-options>> в вызов `EmbeddingRequest`.

## Параметры времени выполнения [[embedding-options]]

[BedrockCohereEmbeddingOptions.java](https://github.com/spring-projects/spring-ai/blob/main/models/spring-ai-bedrock/src/main/java/org/springframework/ai/bedrock/cohere/BedrockCohereEmbeddingOptions.java) предоставляет конфигурации модели, такие как `input-type` или `truncate`.

При запуске параметры по умолчанию могут быть настроены с помощью конструктора `BedrockCohereEmbeddingModel(api, options)` или свойств `spring.ai.bedrock.cohere.embedding.options.*`.

Во время выполнения вы можете переопределить параметры по умолчанию, добавив новые, специфичные для запроса, параметры в вызов `EmbeddingRequest`. Например, чтобы переопределить тип входных данных по умолчанию для конкретного запроса:

```java
EmbeddingResponse embeddingResponse = embeddingModel.call(
    new EmbeddingRequest(List.of("Hello World", "World is big and salvation is near"),
        BedrockCohereEmbeddingOptions.builder()
        .inputType(InputType.SEARCH_DOCUMENT)
        .build()));
```

## Пример контроллера

[Создайте](https://start.spring.io/) новый проект Spring Boot и добавьте `spring-ai-starter-model-bedrock` в зависимости вашего pom (или gradle).

Добавьте файл `application.properties` в директорию `src/main/resources`, чтобы включить и настроить модель встраивания Cohere:

[source]
```
spring.ai.bedrock.aws.region=eu-central-1
spring.ai.bedrock.aws.access-key=${AWS_ACCESS_KEY_ID}
spring.ai.bedrock.aws.secret-key=${AWS_SECRET_ACCESS_KEY}

spring.ai.model.embedding=bedrock-cohere
spring.ai.bedrock.cohere.embedding.options.input-type=search-document
```

> **Совет:** замените `regions`, `access-key` и `secret-key` на ваши учетные данные AWS.

Это создаст реализацию `BedrockCohereEmbeddingModel`, которую вы можете внедрить в ваш класс. Вот пример простого класса `@Controller`, который использует модель чата для генерации текста.

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

## Ручная конфигурация

[BedrockCohereEmbeddingModel](https://github.com/spring-projects/spring-ai/blob/main/models/spring-ai-bedrock/src/main/java/org/springframework/ai/bedrock/cohere/BedrockCohereEmbeddingModel.java) реализует `EmbeddingModel` и использует <<low-level-api>> для подключения к сервису Bedrock Cohere.

Добавьте зависимость `spring-ai-bedrock` в файл `pom.xml` вашего проекта Maven:

```xml
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-bedrock</artifactId>
</dependency>
```

или в файл сборки Gradle `build.gradle`.

```gradle
dependencies {
    implementation 'org.springframework.ai:spring-ai-bedrock'
}
```

> **Совет:** Обратитесь к разделу [Управление зависимостями](xref:getting-started.adoc#dependency-management), чтобы добавить BOM Spring AI в ваш файл сборки.

Затем создайте [BedrockCohereEmbeddingModel](https://github.com/spring-projects/spring-ai/blob/main/models/spring-ai-bedrock/src/main/java/org/springframework/ai/bedrock/cohere/BedrockCohereEmbeddingModel.java) и используйте его для встраивания текста:

```java
var cohereEmbeddingApi =new CohereEmbeddingBedrockApi(
		CohereEmbeddingModel.COHERE_EMBED_MULTILINGUAL_V1.id(),
		EnvironmentVariableCredentialsProvider.create(), Region.US_EAST_1.id(), new ObjectMapper());


var embeddingModel = new BedrockCohereEmbeddingModel(this.cohereEmbeddingApi);

EmbeddingResponse embeddingResponse = this.embeddingModel
	.embedForResponse(List.of("Hello World", "World is big and salvation is near"));
```

## Клиент низкого уровня CohereEmbeddingBedrockApi [[low-level-api]]

[CohereEmbeddingBedrockApi](https://github.com/spring-projects/spring-ai/blob/main/models/spring-ai-bedrock/src/main/java/org/springframework/ai/bedrock/cohere/api/CohereEmbeddingBedrockApi.java) предоставляет легковесный Java-клиент для AWS Bedrock [Cohere Command models](https://docs.aws.amazon.com/bedrock/latest/userguide/model-parameters-cohere-command.html).

Следующая диаграмма классов иллюстрирует интерфейс CohereEmbeddingBedrockApi и его строительные блоки:

![align="center", width="800px"](bedrock/bedrock-cohere-embedding-low-level-api.jpg)

CohereEmbeddingBedrockApi поддерживает модели `cohere.embed-english-v3` и `cohere.embed-multilingual-v3` для вычисления встраивания по одному и пакетного.

Вот простой фрагмент, как использовать API программно:

```java
CohereEmbeddingBedrockApi api = new CohereEmbeddingBedrockApi(
		CohereEmbeddingModel.COHERE_EMBED_MULTILINGUAL_V1.id(),
		EnvironmentVariableCredentialsProvider.create(),
		Region.US_EAST_1.id(), new ObjectMapper());

CohereEmbeddingRequest request = new CohereEmbeddingRequest(
		List.of("I like to eat apples", "I like to eat oranges"),
		CohereEmbeddingRequest.InputType.search_document,
		CohereEmbeddingRequest.Truncate.NONE);

CohereEmbeddingResponse response = this.api.embedding(this.request);
```
