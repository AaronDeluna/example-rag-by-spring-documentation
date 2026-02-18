# Titan Embeddings

Предоставляет модель встраивания Bedrock Titan.
[Amazon Titan](https://aws.amazon.com/bedrock/titan/) базовые модели (FMs) предоставляют клиентам широкий выбор высокопроизводительных моделей для работы с изображениями, мультимодальными встраиваниями и текстом через полностью управляемый API.
Модели Amazon Titan созданы AWS и предварительно обучены на больших наборах данных, что делает их мощными, универсальными моделями, предназначенными для поддержки различных случаев использования, а также для ответственного использования ИИ.
Используйте их как есть или настраивайте под свои данные.

> **Примечание:** Bedrock Titan Embedding поддерживает встраивание текста и изображений.

> **Примечание:** Bedrock Titan Embedding НЕ поддерживает пакетное встраивание.

Страница https://aws.amazon.com/bedrock/titan/[AWS Bedrock Titan Model Page] и https://docs.aws.amazon.com/bedrock/latest/userguide/what-is-bedrock.html[Amazon Bedrock User Guide] содержат подробную информацию о том, как использовать модель, размещенную в AWS.

## Предварительные требования

Обратитесь к xref:api/bedrock.adoc[документации Spring AI по Amazon Bedrock] для настройки доступа к API.

### Добавление репозиториев и BOM

Артефакты Spring AI публикуются в репозиториях Maven Central и Spring Snapshot.
Обратитесь к разделу xref:getting-started.adoc#artifact-repositories[Artifact Repositories], чтобы добавить эти репозитории в вашу систему сборки.

Чтобы помочь с управлением зависимостями, Spring AI предоставляет BOM (bill of materials), чтобы гарантировать, что в проекте используется согласованная версия Spring AI. Обратитесь к разделу xref:getting-started.adoc#dependency-management[Dependency Management], чтобы добавить BOM Spring AI в вашу систему сборки.

## Автоконфигурация

[NOTE]
====
В автоконфигурации Spring AI произошли значительные изменения, касающиеся имен артефактов стартовых модулей.
Пожалуйста, обратитесь к https://docs.spring.io/spring-ai/reference/upgrade-notes.html[заметкам об обновлении] для получения дополнительной информации.
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

> **Совет:** Обратитесь к разделу xref:getting-started.adoc#dependency-management[Dependency Management], чтобы добавить BOM Spring AI в ваш файл сборки.

### Включение поддержки Titan Embedding

По умолчанию модель встраивания Titan отключена.
Чтобы включить ее, установите свойство `spring.ai.model.embedding` в `bedrock-titan` в конфигурации вашего приложения:

```properties
spring.ai.model.embedding=bedrock-titan
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
export AI_MODEL_EMBEDDING=bedrock-titan
```

Вы также можете установить это свойство, используя системные свойства Java при запуске вашего приложения:

```shell
java -Dspring.ai.model.embedding=bedrock-titan -jar your-application.jar
```

### Свойства встраивания

Префикс `spring.ai.bedrock.aws` — это префикс свойств для настройки соединения с AWS Bedrock.

| Свойство | Описание | По умолчанию |
| --- | --- | --- |

| spring.ai.bedrock.aws.region | Регион AWS для использования. | us-east-1 |
| --- | --- | --- |
| spring.ai.bedrock.aws.access-key | Ключ доступа AWS. | - |
| spring.ai.bedrock.aws.secret-key | Секретный ключ AWS. | - |
| spring.ai.bedrock.aws.profile.name | Имя профиля AWS. | - |
| spring.ai.bedrock.aws.profile.credentials-path | Путь к файлу учетных данных AWS. | - |
| spring.ai.bedrock.aws.profile.configuration-path | Путь к файлу конфигурации AWS. | - |

[NOTE]
====
Включение и отключение автоконфигураций встраивания теперь настраивается через свойства верхнего уровня с префиксом `spring.ai.model.embedding`.

Чтобы включить, spring.ai.model.embedding=bedrock-titan (по умолчанию включено)

Чтобы отключить, spring.ai.model.embedding=none (или любое значение, которое не соответствует bedrock-titan)

Это изменение сделано для возможности конфигурации нескольких моделей.
====

Префикс `spring.ai.bedrock.titan.embedding` (определенный в `BedrockTitanEmbeddingProperties`) — это префикс свойств, который настраивает реализацию модели встраивания для Titan.

| Свойство | Описание | По умолчанию |
| --- | --- | --- |
| spring.ai.bedrock.titan.embedding.enabled (Удалено и больше не актуально) | Включить или отключить поддержку встраивания Titan | false |
| spring.ai.model.embedding | Включить или отключить поддержку встраивания Titan | bedrock-titan |
| spring.ai.bedrock.titan.embedding.model | Идентификатор модели для использования. См. `TitanEmbeddingModel` для поддерживаемых моделей. | amazon.titan-embed-image-v1 |

Поддерживаемые значения: `amazon.titan-embed-image-v1`, `amazon.titan-embed-text-v1` и `amazon.titan-embed-text-v2:0`.
Идентификаторы моделей также можно найти в https://docs.aws.amazon.com/bedrock/latest/userguide/model-ids-arns.html[документации AWS Bedrock по базовым идентификаторам моделей].


Файл https://github.com/spring-projects/spring-ai/blob/main/models/spring-ai-bedrock/src/main/java/org/springframework/ai/bedrock/titan/BedrockTitanEmbeddingOptions.java[BedrockTitanEmbeddingOptions.java] предоставляет конфигурации модели, такие как `input-type`.
При запуске параметры по умолчанию можно настроить с помощью метода `BedrockTitanEmbeddingOptions.builder().inputType(type).build()` или свойств `spring.ai.bedrock.titan.embedding.input-type`.

Во время выполнения вы можете переопределить параметры по умолчанию, добавив новые, специфические для запроса, параметры в вызов `EmbeddingRequest`.
Например, чтобы переопределить температуру по умолчанию для конкретного запроса:

```java
EmbeddingResponse embeddingResponse = embeddingModel.call(
    new EmbeddingRequest(List.of("Hello World", "World is big and salvation is near"),
        BedrockTitanEmbeddingOptions.builder()
        .inputType(InputType.TEXT)
        .build()));
```

## Пример контроллера

https://start.spring.io/[Создайте] новый проект Spring Boot и добавьте `spring-ai-starter-model-bedrock` в зависимости вашего pom (или gradle).

Добавьте файл `application.properties` в директорию `src/main/resources`, чтобы включить и настроить модель встраивания Titan:

[source]
```
spring.ai.bedrock.aws.region=eu-central-1
spring.ai.bedrock.aws.access-key=${AWS_ACCESS_KEY_ID}
spring.ai.bedrock.aws.secret-key=${AWS_SECRET_ACCESS_KEY}

spring.ai.model.embedding=bedrock-titan
```

> **Совет:** замените `regions`, `access-key` и `secret-key` на ваши учетные данные AWS.

Это создаст реализацию `EmbeddingController`, которую вы можете внедрить в свой класс.
Вот пример простого класса `@Controller`, который использует модель чата для генерации текста.

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

Файл https://github.com/spring-projects/spring-ai/blob/main/models/spring-ai-bedrock/src/main/java/org/springframework/ai/bedrock/titan/BedrockTitanEmbeddingModel.java[BedrockTitanEmbeddingModel] реализует `EmbeddingModel` и использует <<low-level-api>> для подключения к сервису Bedrock Titan.

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

> **Совет:** Обратитесь к разделу xref:getting-started.adoc#dependency-management[Dependency Management], чтобы добавить BOM Spring AI в ваш файл сборки.

Затем создайте https://github.com/spring-projects/spring-ai/blob/main/models/spring-ai-bedrock/src/main/java/org/springframework/ai/bedrock/titan/BedrockTitanEmbeddingModel.java[BedrockTitanEmbeddingModel] и используйте его для встраивания текста:

```java
var titanEmbeddingApi = new TitanEmbeddingBedrockApi(
	TitanEmbeddingModel.TITAN_EMBED_IMAGE_V1.id(), Region.US_EAST_1.id());

var embeddingModel = new BedrockTitanEmbeddingModel(this.titanEmbeddingApi);

EmbeddingResponse embeddingResponse = this.embeddingModel
	.embedForResponse(List.of("Hello World")); // ЗАМЕТКА: titan не поддерживает пакетное встраивание.
```


Файл https://github.com/spring-projects/spring-ai/blob/main/models/spring-ai-bedrock/src/main/java/org/springframework/ai/bedrock/titan/api/TitanEmbeddingBedrockApi.java[TitanEmbeddingBedrockApi] предоставляет легковесный Java-клиент для AWS Bedrock https://docs.aws.amazon.com/bedrock/latest/userguide/titan-multiemb-models.html[Titan Embedding models].

Следующая диаграмма классов иллюстрирует интерфейс TitanEmbeddingBedrockApi и его строительные блоки:

![align="center", width="500px"](bedrock/bedrock-titan-embedding-low-level-api.jpg)

TitanEmbeddingBedrockApi поддерживает модели `amazon.titan-embed-image-v1` и `amazon.titan-embed-image-v1` для вычисления встраивания по одному и пакетного встраивания.

Вот простой фрагмент кода, как использовать API программно:

```java
TitanEmbeddingBedrockApi titanEmbedApi = new TitanEmbeddingBedrockApi(
		TitanEmbeddingModel.TITAN_EMBED_TEXT_V1.id(), Region.US_EAST_1.id());

TitanEmbeddingRequest request = TitanEmbeddingRequest.builder()
	.withInputText("I like to eat apples.")
	.build();

TitanEmbeddingResponse response = this.titanEmbedApi.embedding(this.request);
```

Чтобы встроить изображение, вам нужно преобразовать его в формат `base64`:

```java
TitanEmbeddingBedrockApi titanEmbedApi = new TitanEmbeddingBedrockApi(
		TitanEmbeddingModel.TITAN_EMBED_IMAGE_V1.id(), Region.US_EAST_1.id());

byte[] image = new DefaultResourceLoader()
	.getResource("classpath:/spring_framework.png")
	.getContentAsByteArray();


TitanEmbeddingRequest request = TitanEmbeddingRequest.builder()
	.withInputImage(Base64.getEncoder().encodeToString(this.image))
	.build();

TitanEmbeddingResponse response = this.titanEmbedApi.embedding(this.request);
```
