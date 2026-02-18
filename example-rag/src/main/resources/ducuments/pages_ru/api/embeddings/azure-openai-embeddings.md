# Azure OpenAI Embeddings

Azure OpenAI расширяет возможности OpenAI, предлагая безопасное генерирование текста и модели вычисления встраиваний для различных задач:

- Встраивания для схожести хорошо захватывают семантическое сходство между двумя или более фрагментами текста.
- Встраивания для поиска текста помогают оценить, насколько длинные документы соответствуют короткому запросу.
- Встраивания для поиска кода полезны для встраивания фрагментов кода и естественно-языковых поисковых запросов.

Встраивания Azure OpenAI полагаются на `косинусное сходство` для вычисления схожести между документами и запросом.

## Предварительные требования

Клиент Azure OpenAI предлагает три варианта подключения: с использованием ключа API Azure, ключа API OpenAI или идентификатора Microsoft Entra.

### Ключ API Azure и конечная точка

Получите свою `конечную точку` и `api-key` Azure OpenAI в разделе Azure OpenAI Service на https://portal.azure.com[Портал Azure].

Spring AI определяет два свойства конфигурации:

1. `spring.ai.azure.openai.api-key`: Установите это значение равным `API Key`, полученному от Azure.
2. `spring.ai.azure.openai.endpoint`: Установите это значение равным URL конечной точки, полученному при развертывании вашей модели в Azure.

Вы можете установить эти свойства конфигурации в вашем файле `application.properties` или `application.yml`:

```properties
spring.ai.azure.openai.api-key=<ваш-azure-api-key>
spring.ai.azure.openai.endpoint=<ваш-azure-endpoint-url>
```

Если вы предпочитаете использовать переменные окружения для конфиденциальной информации, такой как ключи API, вы можете использовать язык выражений Spring (SpEL) в вашей конфигурации:

```yaml
# В application.yml
spring:
  ai:
    azure:
      openai:
        api-key: ${AZURE_OPENAI_API_KEY}
        endpoint: ${AZURE_OPENAI_ENDPOINT}
```

```bash
# В вашем окружении или .env файле
export AZURE_OPENAI_API_KEY=<ваш-azure-openai-api-key>
export AZURE_OPENAI_ENDPOINT=<ваш-azure-endpoint-url>
```

### Ключ OpenAI

Чтобы аутентифицироваться с сервисом OpenAI (не Azure), предоставьте ключ API OpenAI. Это автоматически установит конечную точку на https://api.openai.com/v1.

При использовании этого подхода установите свойство `spring.ai.azure.openai.chat.options.deployment-name` на имя https://platform.openai.com/docs/models[модели OpenAI], которую вы хотите использовать.

В вашей конфигурации приложения:

```properties
spring.ai.azure.openai.openai-api-key=<ваш-azure-openai-key>
spring.ai.azure.openai.chat.options.deployment-name=<имя-модели-openai>
```

Используя переменные окружения с SpEL:

```yaml
# В application.yml
spring:
  ai:
    azure:
      openai:
        openai-api-key: ${AZURE_OPENAI_API_KEY}
        chat:
          options:
            deployment-name: ${OPENAI_MODEL_NAME}
```

```bash
# В вашем окружении или .env файле
export AZURE_OPENAI_API_KEY=<ваш-openai-key>
export OPENAI_MODEL_NAME=<имя-модели-openai>
```

### Microsoft Entra ID

Для аутентификации без ключа с использованием Microsoft Entra ID (ранее Azure Active Directory) установите _только_ свойство конфигурации `spring.ai.azure.openai.endpoint` и _не_ свойство api-key, упомянутое выше.

Найдя только свойство конечной точки, ваше приложение оценит несколько различных вариантов для получения учетных данных, и экземпляр `OpenAIClient` будет создан с использованием токенов учетных данных.

> **Примечание:** больше не нужно создавать бин `TokenCredential`; он настраивается автоматически.

### Добавление репозиториев и BOM

Артефакты Spring AI публикуются в репозиториях Maven Central и Spring Snapshot. Обратитесь к разделу xref:getting-started.adoc#artifact-repositories[Артефактные репозитории], чтобы добавить эти репозитории в вашу систему сборки.

Чтобы помочь с управлением зависимостями, Spring AI предоставляет BOM (спецификация материалов), чтобы гарантировать, что одна и та же версия Spring AI используется на протяжении всего проекта. Обратитесь к разделу xref:getting-started.adoc#dependency-management[Управление зависимостями], чтобы добавить BOM Spring AI в вашу систему сборки.

## Автоконфигурация

[NOTE]
====
В автоконфигурации Spring AI произошли значительные изменения, касающиеся имен артефактов модулей стартеров. Пожалуйста, обратитесь к https://docs.spring.io/spring-ai/reference/upgrade-notes.html[заметкам об обновлении] для получения дополнительной информации.
====

Spring AI предоставляет автоконфигурацию Spring Boot для модели встраивания Azure OpenAI. Чтобы включить ее, добавьте следующую зависимость в файл `pom.xml` вашего проекта Maven:

```xml
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-starter-model-azure-openai</artifactId>
</dependency>
```

или в файл сборки Gradle `build.gradle`.

```groovy
dependencies {
    implementation 'org.springframework.ai:spring-ai-starter-model-azure-openai'
}
```

> **Совет:** Обратитесь к разделу xref:getting-started.adoc#dependency-management[Управление зависимостями], чтобы добавить BOM Spring AI в ваш файл сборки.

### Свойства встраивания

Префикс `spring.ai.azure.openai` — это префикс свойства для настройки подключения к Azure OpenAI.

| Свойство | Описание | По умолчанию |
| --- | --- | --- |

| spring.ai.azure.openai.api-key | Ключ из раздела Azure AI OpenAI `Keys and Endpoint` в `Resource Management` | - |
| --- | --- | --- |
| spring.ai.azure.openai.endpoint | Конечная точка из раздела Azure AI OpenAI `Keys and Endpoint` в `Resource Management` | - |
| spring.ai.azure.openai.openai-api-key | (не Azure) Ключ API OpenAI. Используется для аутентификации с сервисом OpenAI, вместо Azure OpenAI. Это автоматически устанавливает конечную точку на https://api.openai.com/v1. Используйте либо свойство `api-key`, либо `openai-api-key`. С этой конфигурацией свойство `spring.ai.azure.openai.embedding.options.deployment-name` рассматривается как имя https://platform.openai.com/docs/models[модели OpenAi]. | - |

[NOTE]
====
Включение и отключение автоконфигураций встраивания теперь настраивается через свойства верхнего уровня с префиксом `spring.ai.model.embedding`.

Чтобы включить, используйте spring.ai.model.embedding=azure-openai (по умолчанию включено)

Чтобы отключить, используйте spring.ai.model.embedding=none (или любое значение, которое не соответствует azure-openai)

Это изменение сделано для возможности конфигурации нескольких моделей.
====

Префикс `spring.ai.azure.openai.embedding` — это префикс свойства, который настраивает реализацию `EmbeddingModel` для Azure OpenAI.

| Свойство | Описание | По умолчанию |
| --- | --- | --- |

| spring.ai.azure.openai.embedding.enabled (Удалено и больше не актуально) | Включить модель встраивания Azure OpenAI. | true |
| --- | --- | --- |
| spring.ai.model.embedding | Включить модель встраивания Azure OpenAI. | azure-openai |
| spring.ai.azure.openai.embedding.metadata-mode | Режим извлечения содержимого документа | EMBED |
| spring.ai.azure.openai.embedding.options.deployment-name | Это значение 'Deployment Name', как представлено в портале Azure AI | text-embedding-ada-002 |
| spring.ai.azure.openai.embedding.options.user | Идентификатор для вызывающего или конечного пользователя операции. Это может быть использовано для отслеживания или ограничения частоты. | - |

> **Совет:** Все свойства с префиксом `spring.ai.azure.openai.embedding.options` могут быть переопределены во время выполнения, добавив специфичные для запроса <<embedding-options>> в вызов `EmbeddingRequest`.


`AzureOpenAiEmbeddingOptions` предоставляет информацию о конфигурации для запросов встраивания. `AzureOpenAiEmbeddingOptions` предлагает строителя для создания параметров.

При старте используйте конструктор `AzureOpenAiEmbeddingModel`, чтобы установить параметры по умолчанию, используемые для всех запросов встраивания. Во время выполнения вы можете переопределить параметры по умолчанию, передав экземпляр `AzureOpenAiEmbeddingOptions` в запрос `EmbeddingRequest`.

Например, чтобы переопределить имя модели по умолчанию для конкретного запроса:

```java
EmbeddingResponse embeddingResponse = embeddingModel.call(
    new EmbeddingRequest(List.of("Hello World", "World is big and salvation is near"),
        AzureOpenAiEmbeddingOptions.builder()
        .model("Different-Embedding-Model-Deployment-Name")
        .build()));
```

## Пример кода

Это создаст реализацию `EmbeddingModel`, которую вы можете внедрить в свой класс. Вот пример простого класса `@Controller`, который использует реализацию `EmbeddingModel`.

```application.properties
spring.ai.azure.openai.api-key=YOUR_API_KEY
spring.ai.azure.openai.endpoint=YOUR_ENDPOINT
spring.ai.azure.openai.embedding.options.model=text-embedding-ada-002
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

Если вы предпочитаете не использовать автоконфигурацию Spring Boot, вы можете вручную настроить `AzureOpenAiEmbeddingModel` в вашем приложении. Для этого добавьте зависимость `spring-ai-azure-openai` в файл `pom.xml` вашего проекта Maven:
```xml
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-azure-openai</artifactId>
</dependency>
```

или в файл сборки Gradle `build.gradle`.

```gradle
dependencies {
    implementation 'org.springframework.ai:spring-ai-azure-openai'
}
```

> **Совет:** Обратитесь к разделу xref:getting-started.adoc#dependency-management[Управление зависимостями], чтобы добавить BOM Spring AI в ваш файл сборки.

> **Примечание:** Зависимость `spring-ai-azure-openai` также предоставляет доступ к `AzureOpenAiEmbeddingModel`. Для получения дополнительной информации о `AzureOpenAiChatModel` обратитесь к разделу [Azure OpenAI Embeddings](../embeddings/azure-openai-embeddings.html).

Далее создайте экземпляр `AzureOpenAiEmbeddingModel` и используйте его для вычисления схожести между двумя входными текстами:

```java
var openAIClient = OpenAIClientBuilder()
        .credential(new AzureKeyCredential(System.getenv("AZURE_OPENAI_API_KEY")))
		.endpoint(System.getenv("AZURE_OPENAI_ENDPOINT"))
		.buildClient();

var embeddingModel = new AzureOpenAiEmbeddingModel(this.openAIClient)
    .withDefaultOptions(AzureOpenAiEmbeddingOptions.builder()
        .model("text-embedding-ada-002")
        .user("user-6")
        .build());

EmbeddingResponse embeddingResponse = this.embeddingModel
	.embedForResponse(List.of("Hello World", "World is big and salvation is near"));
```

> **Примечание:** `text-embedding-ada-002` на самом деле является `Deployment Name`, как представлено в портале Azure AI.
