# OpenAI SDK Embeddings (Официальный)

Spring AI поддерживает текстовые эмбеддинги OpenAI через OpenAI Java SDK, предоставляя надежную и официально поддерживаемую интеграцию с сервисами OpenAI, включая Microsoft Foundry и GitHub Models.

> **Примечание:** Эта реализация использует официальный [OpenAI Java SDK](https://github.com/openai/openai-java) от OpenAI. Для альтернативной реализации Spring AI смотрите xref:api/embeddings/openai-embeddings.adoc[OpenAI Embeddings].

Текстовые эмбеддинги OpenAI измеряют связанность текстовых строк. Эмбеддинг — это вектор (список) чисел с плавающей запятой. Расстояние между двумя векторами измеряет их связанность. Небольшие расстояния указывают на высокую связанность, а большие расстояния — на низкую связанность.

Модуль OpenAI SDK автоматически определяет поставщика услуг (OpenAI, Microsoft Foundry или GitHub Models) на основе базового URL, который вы предоставляете.

## Аутентификация

Аутентификация осуществляется с использованием базового URL и API-ключа. Реализация предоставляет гибкие параметры конфигурации через свойства Spring Boot или переменные окружения.

### Использование OpenAI

Если вы используете OpenAI напрямую, создайте учетную запись на https://platform.openai.com/signup[страница регистрации OpenAI] и сгенерируйте API-ключ на https://platform.openai.com/account/api-keys[странице API-ключей].

Базовый URL не нужно устанавливать, так как по умолчанию он равен `https://api.openai.com/v1`:

```properties
spring.ai.openai-sdk.api-key=<ваш-openai-api-ключ>
# base-url является необязательным, по умолчанию https://api.openai.com/v1
```

Или с использованием переменных окружения:

```bash
export OPENAI_API_KEY=<ваш-openai-api-ключ>
# OPENAI_BASE_URL является необязательным, по умолчанию https://api.openai.com/v1
```

### Использование Microsoft Foundry

Microsoft Foundry автоматически определяется при использовании URL Microsoft Foundry. Вы можете настроить его с помощью свойств:

```properties
spring.ai.openai-sdk.base-url=https://<ваш-url-развертывания>.openai.azure.com
spring.ai.openai-sdk.api-key=<ваш-api-ключ>
spring.ai.openai-sdk.microsoft-deployment-name=<ваше-имя-развертывания>
```

Или с использованием переменных окружения:

```bash
export OPENAI_BASE_URL=https://<ваш-url-развертывания>.openai.azure.com
export OPENAI_API_KEY=<ваш-api-ключ>
```

**Аутентификация без пароля (Рекомендуется для Azure):**

Microsoft Foundry поддерживает аутентификацию без пароля без предоставления API-ключа, что более безопасно при работе в Azure.

Чтобы включить аутентификацию без пароля, добавьте зависимость `com.azure:azure-identity`:

```xml
<dependency>
    <groupId>com.azure</groupId>
    <artifactId>azure-identity</artifactId>
</dependency>
```

Затем настройте без API-ключа:

```properties
spring.ai.openai-sdk.base-url=https://<ваш-url-развертывания>.openai.azure.com
spring.ai.openai-sdk.microsoft-deployment-name=<ваше-имя-развертывания>
# API-ключ не нужен - будут использоваться учетные данные Azure из окружения
```

### Использование GitHub Models

GitHub Models автоматически определяется при использовании базового URL GitHub Models. Вам нужно создать токен доступа GitHub (PAT) с областью `models:read`.

```properties
spring.ai.openai-sdk.base-url=https://models.inference.ai.azure.com
spring.ai.openai-sdk.api-key=github_pat_XXXXXXXXXXX
```

Или с использованием переменных окружения:

```bash
export OPENAI_BASE_URL=https://models.inference.ai.azure.com
export OPENAI_API_KEY=github_pat_XXXXXXXXXXX
```

> **Совет:** Для повышения безопасности при работе с конфиденциальной информацией, такой как API-ключи, вы можете использовать язык выражений Spring (SpEL) в ваших свойствах:

```properties
spring.ai.openai-sdk.api-key=${OPENAI_API_KEY}
```

### Добавление репозиториев и BOMSpring AI артефакты публикуются в репозиториях Maven Central и Spring Snapshot. Обратитесь к разделу xref:getting-started.adoc#artifact-repositories[Артефакты репозиториев], чтобы добавить эти репозитории в вашу систему сборки.

Для упрощения управления зависимостями Spring AI предоставляет BOM (bill of materials), чтобы гарантировать использование согласованной версии Spring AI на протяжении всего проекта. Обратитесь к разделу xref:getting-started.adoc#dependency-management[Управление зависимостями], чтобы добавить Spring AI BOM в вашу систему сборки.

## Автоконфигурация

Spring AI предоставляет автоконфигурацию Spring Boot для модели встраивания OpenAI SDK. Чтобы включить её, добавьте следующую зависимость в файл сборки Maven `pom.xml` или Gradle `build.gradle` вашего проекта:

[tabs]
======
Maven::
+
```xml
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-starter-model-openai-sdk</artifactId>
</dependency>
```

Gradle::
+
```groovy
dependencies {
    implementation 'org.springframework.ai:spring-ai-starter-model-openai-sdk'
}
```
======

> **Совет:** Обратитесь к разделу xref:getting-started.adoc#dependency-management[Управление зависимостями], чтобы добавить Spring AI BOM в ваш файл сборки.

### Свойства конфигурации

#### Свойства подключения

Префикс `spring.ai.openai-sdk` используется в качестве префикса свойств, который позволяет вам настроить клиент OpenAI SDK.

[cols="3,5,1", stripes=even]
|====
| Свойство | Описание | По умолчанию

| spring.ai.openai-sdk.base-url        | URL для подключения. Автоматически определяет из переменной окружения `OPENAI_BASE_URL`, если не задано. |  https://api.openai.com/v1
| spring.ai.openai-sdk.api-key         | API-ключ. Автоматически определяет из переменной окружения `OPENAI_API_KEY`, если не задано. |  -
| spring.ai.openai-sdk.organization-id | Опционально укажите, какую организацию использовать для API-запросов. |  -
| spring.ai.openai-sdk.timeout         | Длительность таймаута запроса. |  -
| spring.ai.openai-sdk.max-retries     | Максимальное количество попыток повторного запроса для неудачных запросов. |  -
| spring.ai.openai-sdk.proxy           | Настройки прокси для клиента OpenAI (объект Java `Proxy`). |  -
| spring.ai.openai-sdk.custom-headers  | Пользовательские HTTP-заголовки, которые следует включить в запросы. Карта имени заголовка к значению заголовка. |  -
|====

#### Свойства Microsoft Foundry

Реализация OpenAI SDK предоставляет нативную поддержку Microsoft Foundry с автоматической конфигурацией:

[cols="3,5,1", stripes=even]
|====
| Свойство | Описание | По умолчанию

| spring.ai.openai-sdk.microsoft-foundry           | Включить режим Microsoft Foundry. Автоматически определяется, если базовый URL содержит `openai.azure.com`, `cognitiveservices.azure.com` или `.openai.microsoftFoundry.com`. |  false
| spring.ai.openai-sdk.microsoft-deployment-name | Имя развертывания Microsoft Foundry. Если не указано, будет использоваться имя модели. Также доступно через псевдоним `deployment-name`. |  -
| spring.ai.openai-sdk.microsoft-foundry-service-version | Версия API-сервиса Microsoft Foundry. |  -
| spring.ai.openai-sdk.credential      | Объект учетных данных для аутентификации без пароля (требуется зависимость `com.azure:azure-identity`). |  -
|====

> **Совет:** Microsoft Foundry поддерживает аутентификацию без пароля. Добавьте зависимость `com.azure:azure-identity`, и реализация автоматически попытается использовать учетные данные Azure из окружения, когда API-ключ не предоставлен.

#### Свойства моделей GitHub

Нативная поддержка моделей GitHub доступна:

[cols="3,5,1", stripes=even]
|====
| Свойство | Описание | По умолчанию

| spring.ai.openai-sdk.github-models   | Включить режим моделей GitHub. Автоматически определяется, если базовый URL содержит `models.github.ai` или `models.inference.ai.azure.com`. |  false
|====

> **Совет:** Модели GitHub требуют токен доступа с областью `models:read`. Установите его через переменную окружения `OPENAI_API_KEY` или свойство `spring.ai.openai-sdk.api-key`.

#### Свойства модели встраиванияThe prefix `spring.ai.openai-sdk.embedding` — это префикс свойств для настройки реализации модели встраивания:

[cols="3,5,1", stripes=even]
|====
| Свойство | Описание | По умолчанию

| spring.ai.openai-sdk.embedding.metadata-mode      | Режим извлечения содержимого документа.      | EMBED
| spring.ai.openai-sdk.embedding.options.model      | Модель для использования. Вы можете выбрать между моделями, такими как: `text-embedding-ada-002`, `text-embedding-3-small`, `text-embedding-3-large`. Дополнительную информацию смотрите на странице https://platform.openai.com/docs/models[модели]. | `text-embedding-ada-002`
| spring.ai.openai-sdk.embedding.options.user   | Уникальный идентификатор, представляющий вашего конечного пользователя, который может помочь OpenAI отслеживать и выявлять злоупотребления.  | -
| spring.ai.openai-sdk.embedding.options.dimensions   | Количество измерений, которые должны иметь результирующие встраивания. Поддерживается только в моделях `text-embedding-3` и более поздних.  | -
|====

> **Совет:** Все свойства с префиксом `spring.ai.openai-sdk.embedding.options` могут быть переопределены во время выполнения, добавив специфические для запроса <<embedding-options>> в вызов `EmbeddingRequest`.

## Опции во время выполнения [[embedding-options]]

Файл https://github.com/spring-projects/spring-ai/blob/main/models/spring-ai-openai-sdk/src/main/java/org/springframework/ai/openaisdk/OpenAiSdkEmbeddingOptions.java[OpenAiSdkEmbeddingOptions.java] предоставляет конфигурации OpenAI, такие как модель для использования, размеры и идентификатор пользователя.

Параметры по умолчанию также могут быть настроены с помощью свойств `spring.ai.openai-sdk.embedding.options`.

При старте используйте конструктор `OpenAiSdkEmbeddingModel`, чтобы установить параметры по умолчанию, используемые для всех запросов на встраивание. Во время выполнения вы можете переопределить параметры по умолчанию, используя экземпляр `OpenAiSdkEmbeddingOptions` в качестве части вашего `EmbeddingRequest`.

Например, чтобы переопределить имя модели по умолчанию для конкретного запроса:

```java
EmbeddingResponse embeddingResponse = embeddingModel.call(
    new EmbeddingRequest(List.of("Hello World", "World is big and salvation is near"),
        OpenAiSdkEmbeddingOptions.builder()
            .model("text-embedding-3-large")
            .dimensions(1024)
        .build()));
```

> **Совет:** В дополнение к специфическим для модели https://github.com/spring-projects/spring-ai/blob/main/models/spring-ai-openai-sdk/src/main/java/org/springframework/ai/openaisdk/OpenAiSdkEmbeddingOptions.java[OpenAiSdkEmbeddingOptions] вы можете использовать переносимый [EmbeddingOptions](https://github.com/spring-projects/spring-ai/blob/main/spring-ai-model/src/main/java/org/springframework/ai/embedding/EmbeddingOptions.java) экземпляр, созданный с помощью билдера.

## Пример контроллера

https://start.spring.io/[Создайте] новый проект Spring Boot и добавьте `spring-ai-openai-sdk` в зависимости вашего pom (или gradle).

Добавьте файл `application.properties` в директорию `src/main/resources` для настройки модели встраивания OpenAI SDK:

```application.properties
spring.ai.openai-sdk.api-key=YOUR_API_KEY
spring.ai.openai-sdk.embedding.options.model=text-embedding-ada-002
```

> **Совет:** Замените `api-key` на ваши учетные данные OpenAI.

Это создаст реализацию `OpenAiSdkEmbeddingModel`, которую вы можете внедрить в ваши классы. Вот пример простого класса `@RestController`, который использует модель встраивания.

```java
@RestController
public class EmbeddingController {

    private final EmbeddingModel embeddingModel;

    @Autowired
    public EmbeddingController(EmbeddingModel embeddingModel) {
        this.embeddingModel = embeddingModel;
    }

    @GetMapping("/ai/embedding")
    public Map<String, Object> embed(
            @RequestParam(value = "message", defaultValue = "Tell me a joke") String message) {
        EmbeddingResponse embeddingResponse = this.embeddingModel.embedForResponse(List.of(message));
        return Map.of("embedding", embeddingResponse);
    }
}
```

## Ручная конфигурацияThe https://github.com/spring-projects/spring-ai/blob/main/models/spring-ai-openai-sdk/src/main/java/org/springframework/ai/openaisdk/OpenAiSdkEmbeddingModel.java[OpenAiSdkEmbeddingModel] реализует `EmbeddingModel` и использует официальный OpenAI Java SDK для подключения к сервису OpenAI.

Если вы не используете автоматическую конфигурацию Spring Boot, вы можете вручную настроить OpenAI SDK Embedding Model. Для этого добавьте зависимость `spring-ai-openai-sdk` в файл `pom.xml` вашего проекта Maven:

```xml
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-openai-sdk</artifactId>
</dependency>
```

или в файл сборки Gradle `build.gradle`:

```groovy
dependencies {
    implementation 'org.springframework.ai:spring-ai-openai-sdk'
}
```

> **Совет:** Обратитесь к разделу xref:getting-started.adoc#dependency-management[Управление зависимостями], чтобы добавить Spring AI BOM в ваш файл сборки.

> **Примечание:** Зависимость `spring-ai-openai-sdk` также предоставляет доступ к `OpenAiSdkChatModel` и `OpenAiSdkImageModel`. Для получения дополнительной информации о `OpenAiSdkChatModel` обратитесь к разделу xref:api/chat/openai-sdk-chat.adoc[OpenAI SDK Chat].

Далее создайте экземпляр `OpenAiSdkEmbeddingModel` и используйте его для вычисления сходства между двумя входными текстами:

```java
var embeddingOptions = OpenAiSdkEmbeddingOptions.builder()
    .model("text-embedding-ada-002")
    .apiKey(System.getenv("OPENAI_API_KEY"))
    .build();

var embeddingModel = new OpenAiSdkEmbeddingModel(embeddingOptions);

EmbeddingResponse embeddingResponse = embeddingModel
    .embedForResponse(List.of("Hello World", "World is big and salvation is near"));
```

`OpenAiSdkEmbeddingOptions` предоставляет информацию о конфигурации для запросов на встраивание. Класс опций предлагает `builder()` для удобного создания опций.

### Конфигурация Microsoft Foundry

Для Microsoft Foundry:

```java
var embeddingOptions = OpenAiSdkEmbeddingOptions.builder()
    .baseUrl("https://your-resource.openai.azure.com")
    .apiKey(System.getenv("OPENAI_API_KEY"))
    .deploymentName("text-embedding-ada-002")
    .azureOpenAIServiceVersion(AzureOpenAIServiceVersion.V2024_10_01_PREVIEW)
    .azure(true)  // Включает режим Microsoft Foundry
    .build();

var embeddingModel = new OpenAiSdkEmbeddingModel(embeddingOptions);
```

> **Совет:** Microsoft Foundry поддерживает аутентификацию без пароля. Добавьте зависимость `com.azure:azure-identity` в ваш проект. Если вы не предоставите API-ключ, реализация автоматически попытается использовать учетные данные Azure из вашей среды.

### Конфигурация моделей GitHub

Для моделей GitHub:

```java
var embeddingOptions = OpenAiSdkEmbeddingOptions.builder()
    .baseUrl("https://models.inference.ai.azure.com")
    .apiKey(System.getenv("GITHUB_TOKEN"))
    .model("text-embedding-3-large")
    .githubModels(true)
    .build();

var embeddingModel = new OpenAiSdkEmbeddingModel(embeddingOptions);
```

## Наблюдаемость

Реализация OpenAI SDK поддерживает функции наблюдаемости Spring AI через Micrometer. Все операции модели встраивания инструментированы для мониторинга и трассировки.

## Дополнительные ресурсы

- [Официальный OpenAI Java SDK](https://github.com/openai/openai-java)
- [Документация по API встраивания OpenAI](https://platform.openai.com/docs/api-reference/embeddings)
- [Модели OpenAI](https://platform.openai.com/docs/models)
- [Документация Microsoft Foundry](https://learn.microsoft.com/en-us/azure/ai-foundry/)
- [Модели GitHub](https://github.com/marketplace/models)
