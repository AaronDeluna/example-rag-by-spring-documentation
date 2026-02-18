# Oracle Cloud Infrastructure (OCI) GenAI Embeddings

https://www.oracle.com/artificial-intelligence/generative-ai/generative-ai-service/[OCI GenAI Service] предлагает текстовые встраивания с моделями по запросу или выделенными AI-кластерами.

Страница https://docs.oracle.com/en-us/iaas/Content/generative-ai/embed-models.htm[OCI Embedding Models Page] и https://docs.oracle.com/en-us/iaas/Content/generative-ai/use-playground-embed.htm[OCI Text Embeddings Page] предоставляют подробную информацию о использовании и размещении моделей встраивания на OCI.

## Предварительные требования

### Добавление репозиториев и BOM

Артефакты Spring AI публикуются в репозиториях Maven Central и Spring Snapshot.
Обратитесь к разделу xref:getting-started.adoc#artifact-repositories[Artifact Repositories], чтобы добавить эти репозитории в вашу систему сборки.

Чтобы помочь с управлением зависимостями, Spring AI предоставляет BOM (bill of materials), чтобы гарантировать, что одна и та же версия Spring AI используется на протяжении всего проекта. Обратитесь к разделу xref:getting-started.adoc#dependency-management[Dependency Management], чтобы добавить BOM Spring AI в вашу систему сборки.

## Автоконфигурация

[NOTE]
====
В автоконфигурации Spring AI произошли значительные изменения, касающиеся имен артефактов стартовых модулей.
Пожалуйста, обратитесь к https://docs.spring.io/spring-ai/reference/upgrade-notes.html[upgrade notes] для получения дополнительной информации.
====

Spring AI предоставляет автоконфигурацию Spring Boot для OCI GenAI Embedding Client.
Чтобы включить ее, добавьте следующую зависимость в файл `pom.xml` вашего проекта:

```xml
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-starter-model-oci-genai</artifactId>
</dependency>
```

или в файл сборки Gradle `build.gradle`.

```groovy
dependencies {
    implementation 'org.springframework.ai:spring-ai-starter-model-oci-genai'
}
```

> **Совет:** Обратитесь к разделу xref:getting-started.adoc#dependency-management[Dependency Management], чтобы добавить BOM Spring AI в ваш файл сборки.

### Свойства встраивания

Префикс `spring.ai.oci.genai` — это префикс свойств для настройки соединения с OCI GenAI.

| Свойство | Описание | По умолчанию |
| --- | --- | --- |

| spring.ai.oci.genai.authenticationType | Тип аутентификации, используемый при аутентификации в OCI. Может быть `file`, `instance-principal`, `workload-identity` или `simple`. | file |
| --- | --- | --- |
| spring.ai.oci.genai.region | Регион службы OCI. | us-chicago-1 |
| spring.ai.oci.genai.tenantId | OCID арендатора OCI, используемый при аутентификации с `simple` auth. | - |
| spring.ai.oci.genai.userId | OCID пользователя OCI, используемый при аутентификации с `simple` auth. | - |
| spring.ai.oci.genai.fingerprint | Отпечаток закрытого ключа, используемый при аутентификации с `simple` auth. | - |
| spring.ai.oci.genai.privateKey | Содержимое закрытого ключа, используемое при аутентификации с `simple` auth. | - |
| spring.ai.oci.genai.passPhrase | Необязательная фраза-пароль для закрытого ключа, используемая при аутентификации с `simple` auth и защищенным паролем закрытым ключом. | - |
| spring.ai.oci.genai.file | Путь к файлу конфигурации OCI. Используется при аутентификации с `file` auth. | <user's home directory>/.oci/config |
| spring.ai.oci.genai.profile | Имя профиля OCI. Используется при аутентификации с `file` auth. | DEFAULT |
| spring.ai.oci.genai.endpoint | Необязательный конечный пункт OCI GenAI. | - |


[NOTE]
====
Включение и отключение автоконфигураций встраивания теперь настраивается через свойства верхнего уровня с префиксом `spring.ai.model.embedding`.

Чтобы включить, используйте spring.ai.model.embedding=oci-genai (по умолчанию включено)

Чтобы отключить, используйте spring.ai.model.embedding=none (или любое значение, которое не соответствует oci-genai)

Это изменение сделано для того, чтобы позволить конфигурацию нескольких моделей.
====

Префикс `spring.ai.oci.genai.embedding` — это префикс свойств, который настраивает реализацию `EmbeddingModel` для OCI GenAI.

| Свойство | Описание | По умолчанию |
| --- | --- | --- |

| spring.ai.oci.genai.embedding.enabled (Удалено и больше не актуально) | Включить модель встраивания OCI GenAI. | true |
| --- | --- | --- |
| spring.ai.model.embedding | Включить модель встраивания OCI GenAI. | oci-genai |
| spring.ai.oci.genai.embedding.compartment | OCID модели. | - |
| spring.ai.oci.genai.embedding.servingMode | Режим обслуживания модели, который будет использоваться. Может быть `on-demand` или `dedicated`. | on-demand |
| spring.ai.oci.genai.embedding.truncate | Как обрезать текст, если он превышает контекст встраивания. Может быть `START` или `END`. | END |
| spring.ai.oci.genai.embedding.model | Модель или конечная точка модели, используемая для встраиваний. | - |

> **Совет:** Все свойства с префиксом `spring.ai.oci.genai.embedding.options` могут быть переопределены во время выполнения, добавив специфичные для запроса <<embedding-options>> в вызов `EmbeddingRequest`.


`OCIEmbeddingOptions` предоставляет информацию о конфигурации для запросов встраивания.
`OCIEmbeddingOptions` предлагает строителя для создания параметров.

При запуске используйте конструктор `OCIEmbeddingOptions`, чтобы установить параметры по умолчанию, используемые для всех запросов встраивания.
Во время выполнения вы можете переопределить параметры по умолчанию, передав экземпляр `OCIEmbeddingOptions` в запрос `EmbeddingRequest`.

Например, чтобы переопределить имя модели по умолчанию для конкретного запроса:

```java
EmbeddingResponse embeddingResponse = embeddingModel.call(
    new EmbeddingRequest(List.of("Hello World", "World is big and salvation is near"),
        OCIEmbeddingOptions.builder()
            .model("my-other-embedding-model")
            .build()
));
```

## Пример кода

Это создаст реализацию `EmbeddingModel`, которую вы можете внедрить в свой класс.
Вот пример простого класса `@Controller`, который использует реализацию `EmbeddingModel`.

```application.properties
spring.ai.oci.genai.embedding.model=<ваша модель>
spring.ai.oci.genai.embedding.compartment=<ваш отдел модели>
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

## Ручная конфигурация

Если вы предпочитаете не использовать автоконфигурацию Spring Boot, вы можете вручную настроить `OCIEmbeddingModel` в вашем приложении.
Для этого добавьте зависимость `spring-oci-genai-openai` в файл `pom.xml` вашего проекта:
```xml
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-oci-genai-openai</artifactId>
</dependency>
```

или в файл сборки Gradle `build.gradle`.

```gradle
dependencies {
    implementation 'org.springframework.ai:spring-oci-genai-openai'
}
```

> **Совет:** Обратитесь к разделу xref:getting-started.adoc#dependency-management[Dependency Management], чтобы добавить BOM Spring AI в ваш файл сборки.

Затем создайте экземпляр `OCIEmbeddingModel` и используйте его для вычисления сходства между двумя входными текстами:

```java
final String EMBEDDING_MODEL = "cohere.embed-english-light-v2.0";
final String CONFIG_FILE = Paths.get(System.getProperty("user.home"), ".oci", "config").toString();
final String PROFILE = "DEFAULT";
final String REGION = "us-chicago-1";
final String COMPARTMENT_ID = System.getenv("OCI_COMPARTMENT_ID");

var authProvider = new ConfigFileAuthenticationDetailsProvider(
		this.CONFIG_FILE, this.PROFILE);
var aiClient = GenerativeAiInferenceClient.builder()
    .region(Region.valueOf(this.REGION))
    .build(this.authProvider);
var options = OCIEmbeddingOptions.builder()
    .model(this.EMBEDDING_MODEL)
    .compartment(this.COMPARTMENT_ID)
    .servingMode("on-demand")
    .build();
var embeddingModel = new OCIEmbeddingModel(this.aiClient, this.options);
List<Double> embedding = this.embeddingModel.embed(new Document("How many provinces are in Canada?"));
```
