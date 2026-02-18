# OpenAI SDK Генерация Изображений (Официальная)

Spring AI поддерживает модели генерации изображений DALL-E от OpenAI через OpenAI Java SDK, предоставляя надежную и официально поддерживаемую интеграцию с сервисами OpenAI, включая Microsoft Foundry и GitHub Models.

> **Примечание:** Эта реализация использует официальный [OpenAI Java SDK](https://github.com/openai/openai-java) от OpenAI. Для альтернативной реализации Spring AI смотрите xref:api/image/openai-image.adoc[Генерация изображений OpenAI].

DALL-E — это современная модель генерации изображений от OpenAI, которая может создавать реалистичные изображения и искусство на основе описаний на естественном языке.

Модуль OpenAI SDK автоматически определяет поставщика услуг (OpenAI, Microsoft Foundry или GitHub Models) на основе базового URL, который вы предоставляете.

## Аутентификация

Аутентификация осуществляется с использованием базового URL и API-ключа. Реализация предоставляет гибкие параметры конфигурации через свойства Spring Boot или переменные окружения.

### Использование OpenAI

Если вы используете OpenAI напрямую, создайте учетную запись на https://platform.openai.com/signup[страница регистрации OpenAI] и сгенерируйте API-ключ на https://platform.openai.com/account/api-keys[страница API-ключей].

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

GitHub Models автоматически определяется при использовании базового URL GitHub Models. Вам нужно создать Личный Токен Доступа (PAT) GitHub с областью `models:read`.

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

Чтобы помочь с управлением зависимостями, Spring AI предоставляет BOM (bill of materials), чтобы гарантировать использование согласованной версии Spring AI на протяжении всего проекта. Обратитесь к разделу xref:getting-started.adoc#dependency-management[Управление зависимостями], чтобы добавить Spring AI BOM в вашу систему сборки.

## Автоконфигурация

Spring AI предоставляет автоконфигурацию Spring Boot для OpenAI SDK Image Model. Чтобы включить её, добавьте следующую зависимость в файл сборки Maven `pom.xml` или Gradle `build.gradle` вашего проекта:

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

| Свойство | Описание | По умолчанию |
| --- | --- | --- |

| spring.ai.openai-sdk.base-url | URL для подключения. Автоматически определяет из переменной окружения `OPENAI_BASE_URL`, если не задано. | https://api.openai.com/v1 |
| --- | --- | --- |
| spring.ai.openai-sdk.api-key | API-ключ. Автоматически определяет из переменной окружения `OPENAI_API_KEY`, если не задано. | - |
| spring.ai.openai-sdk.organization-id | Опционально укажите, какую организацию использовать для API-запросов. | - |
| spring.ai.openai-sdk.timeout | Длительность таймаута запроса. | - |
| spring.ai.openai-sdk.max-retries | Максимальное количество попыток повторного запроса для неудачных запросов. | - |
| spring.ai.openai-sdk.proxy | Настройки прокси для клиента OpenAI (объект Java `Proxy`). | - |
| spring.ai.openai-sdk.custom-headers | Пользовательские HTTP-заголовки, которые следует включить в запросы. Карта имени заголовка к значению заголовка. | - |

#### Свойства Microsoft Foundry

Реализация OpenAI SDK предоставляет нативную поддержку Microsoft Foundry с автоматической конфигурацией:

| Свойство | Описание | По умолчанию |
| --- | --- | --- |

| spring.ai.openai-sdk.microsoft-foundry | Включить режим Microsoft Foundry. Автоматически определяется, если базовый URL содержит `openai.azure.com`, `cognitiveservices.azure.com` или `.openai.microsoftFoundry.com`. | false |
| --- | --- | --- |
| spring.ai.openai-sdk.microsoft-deployment-name | Имя развертывания Microsoft Foundry. Если не указано, будет использоваться имя модели. Также доступно через псевдоним `deployment-name`. | - |
| spring.ai.openai-sdk.microsoft-foundry-service-version | Версия API-сервиса Microsoft Foundry. | - |
| spring.ai.openai-sdk.credential | Объект учетных данных для аутентификации без пароля (требуется зависимость `com.azure:azure-identity`). | - |

> **Совет:** Microsoft Foundry поддерживает аутентификацию без пароля. Добавьте зависимость `com.azure:azure-identity`, и реализация автоматически попытается использовать учетные данные Azure из окружения, когда API-ключ не предоставлен.

#### Свойства моделей GitHub

Нативная поддержка моделей GitHub доступна:

| Свойство | Описание | По умолчанию |
| --- | --- | --- |

| spring.ai.openai-sdk.github-models | Включить режим моделей GitHub. Автоматически определяется, если базовый URL содержит `models.github.ai` или `models.inference.ai.azure.com`. | false |
| --- | --- | --- |

> **Совет:** Модели GitHub требуют токен доступа с областью `models:read`. Установите его через переменную окружения `OPENAI_API_KEY` или свойство `spring.ai.openai-sdk.api-key`.

#### Свойства моделей изображенийThe prefix `spring.ai.openai-sdk.image` — это префикс свойств для настройки реализации модели генерации изображений:

| Свойство | Описание | По умолчанию |
| --- | --- | --- |

| spring.ai.openai-sdk.image.options.model | Модель, используемая для генерации изображений. Доступные модели: `dall-e-2`, `dall-e-3`. Дополнительную информацию см. на странице https://platform.openai.com/docs/models[модели]. | `dall-e-3` |
| --- | --- | --- |
| spring.ai.openai-sdk.image.options.n | Количество изображений для генерации. Должно быть от 1 до 10. Для `dall-e-3` поддерживается только n=1. | - |
| spring.ai.openai-sdk.image.options.quality | Качество генерируемого изображения. `hd` создает изображения с более тонкими деталями и большей согласованностью. Этот параметр поддерживается только для `dall-e-3`. Доступные значения: `standard`, `hd`. | - |
| spring.ai.openai-sdk.image.options.response-format | Формат, в котором возвращаются сгенерированные изображения. Должен быть одним из `url` или `b64_json`. | - |
| spring.ai.openai-sdk.image.options.size | Размер генерируемых изображений. Должен быть одним из `256x256`, `512x512` или `1024x1024` для `dall-e-2`. Должен быть одним из `1024x1024`, `1792x1024` или `1024x1792` для моделей `dall-e-3`. | - |
| spring.ai.openai-sdk.image.options.width | Ширина генерируемых изображений. Должна быть одной из 256, 512 или 1024 для `dall-e-2`. | - |
| spring.ai.openai-sdk.image.options.height | Высота генерируемых изображений. Должна быть одной из 256, 512 или 1024 для `dall-e-2`. | - |
| spring.ai.openai-sdk.image.options.style | Стиль генерируемых изображений. Должен быть одним из `vivid` или `natural`. Vivid заставляет модель склоняться к созданию гиперреалистичных и драматических изображений. Natural заставляет модель создавать более естественные, менее гиперреалистичные изображения. Этот параметр поддерживается только для `dall-e-3`. | - |
| spring.ai.openai-sdk.image.options.user | Уникальный идентификатор, представляющий вашего конечного пользователя, который может помочь OpenAI отслеживать и выявлять злоупотребления. | - |

> **Совет:** Все свойства с префиксом `spring.ai.openai-sdk.image.options` могут быть переопределены во время выполнения, добавив специфические для запроса <<image-options>> в вызов `ImagePrompt`.


Файл https://github.com/spring-projects/spring-ai/blob/main/models/spring-ai-openai-sdk/src/main/java/org/springframework/ai/openaisdk/OpenAiSdkImageOptions.java[OpenAiSdkImageOptions.java] предоставляет конфигурации OpenAI, такие как модель для использования, качество, размер, стиль и количество изображений для генерации.

Параметры по умолчанию также могут быть настроены с помощью свойств `spring.ai.openai-sdk.image.options`.

При старте используйте конструктор `OpenAiSdkImageModel`, чтобы установить параметры по умолчанию, используемые для всех запросов на генерацию изображений. Во время выполнения вы можете переопределить параметры по умолчанию, используя экземпляр `OpenAiSdkImageOptions` в вашем `ImagePrompt`.

Например, чтобы переопределить модель и качество по умолчанию для конкретного запроса:

```java
ImageResponse response = imageModel.call(
    new ImagePrompt("Мини-золотистый дудль светлого кремового цвета",
        OpenAiSdkImageOptions.builder()
            .model("dall-e-3")
            .quality("hd")
            .N(1)
            .width(1024)
            .height(1024)
            .style("vivid")
        .build()));
```

> **Совет:** В дополнение к специфическим для модели https://github.com/spring-projects/spring-ai/blob/main/models/spring-ai-openai-sdk/src/main/java/org/springframework/ai/openaisdk/OpenAiSdkImageOptions.java[OpenAiSdkImageOptions] вы можете использовать переносимый [ImageOptions](https://github.com/spring-projects/spring-ai/blob/main/spring-ai-model/src/main/java/org/springframework/ai/image/ImageOptions.java) экземпляр, созданный с помощью [ImageOptionsBuilder#builder()](https://github.com/spring-projects/spring-ai/blob/main/spring-ai-model/src/main/java/org/springframework/ai/image/ImageOptionsBuilder.java).

## Пример контроллераhttps://start.spring.io/[Создайте] новый проект Spring Boot и добавьте `spring-ai-openai-sdk` в зависимости вашего pom (или gradle).

Добавьте файл `application.properties` в директорию `src/main/resources`, чтобы настроить модель изображений OpenAI SDK:

```application.properties
spring.ai.openai-sdk.api-key=YOUR_API_KEY
spring.ai.openai-sdk.image.options.model=dall-e-3
```

> **Совет:** Замените `api-key` на ваши учетные данные OpenAI.

Это создаст реализацию `OpenAiSdkImageModel`, которую вы сможете внедрить в ваши классы. Вот пример простого класса `@RestController`, который использует модель изображений.

```java
@RestController
public class ImageController {

    private final ImageModel imageModel;

    @Autowired
    public ImageController(ImageModel imageModel) {
        this.imageModel = imageModel;
    }

    @GetMapping("/ai/image")
    public Map<String, Object> generateImage(
            @RequestParam(value = "prompt", defaultValue = "A light cream colored mini golden doodle") String prompt) {
        ImageResponse response = this.imageModel.call(
            new ImagePrompt(prompt,
                OpenAiSdkImageOptions.builder()
                    .quality("hd")
                    .N(1)
                    .width(1024)
                    .height(1024)
                .build()));
        
        String imageUrl = response.getResult().getOutput().getUrl();
        return Map.of("url", imageUrl);
    }
}
```

## Ручная конфигурация

https://github.com/spring-projects/spring-ai/blob/main/models/spring-ai-openai-sdk/src/main/java/org/springframework/ai/openaisdk/OpenAiSdkImageModel.java[OpenAiSdkImageModel] реализует `ImageModel` и использует официальный Java SDK OpenAI для подключения к сервису OpenAI.

Если вы не используете автонастройку Spring Boot, вы можете вручную настроить модель изображений OpenAI SDK. Для этого добавьте зависимость `spring-ai-openai-sdk` в файл Maven `pom.xml` вашего проекта:

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

> **Примечание:** Зависимость `spring-ai-openai-sdk` также предоставляет доступ к `OpenAiSdkChatModel` и `OpenAiSdkEmbeddingModel`. Для получения дополнительной информации о `OpenAiSdkChatModel` обратитесь к разделу xref:api/chat/openai-sdk-chat.adoc[OpenAI SDK Chat].

Далее создайте экземпляр `OpenAiSdkImageModel` и используйте его для генерации изображений:

```java
var imageOptions = OpenAiSdkImageOptions.builder()
    .model("dall-e-3")
    .quality("hd")
    .apiKey(System.getenv("OPENAI_API_KEY"))
    .build();

var imageModel = new OpenAiSdkImageModel(imageOptions);

ImageResponse response = imageModel.call(
    new ImagePrompt("A light cream colored mini golden doodle",
        OpenAiSdkImageOptions.builder()
            .N(1)
            .width(1024)
            .height(1024)
        .build()));
```

`OpenAiSdkImageOptions` предоставляет информацию о конфигурации для запросов на генерацию изображений. Класс опций предлагает `builder()` для удобного создания опций.

### Конфигурация Microsoft FoundryДля Microsoft Foundry:

```java
var imageOptions = OpenAiSdkImageOptions.builder()
    .baseUrl("https://your-resource.openai.azure.com")
    .apiKey(System.getenv("OPENAI_API_KEY"))
    .deploymentName("dall-e-3")
    .azureOpenAIServiceVersion(AzureOpenAIServiceVersion.V2024_10_01_PREVIEW)
    .azure(true)  // Включает режим Microsoft Foundry
    .build();

var imageModel = new OpenAiSdkImageModel(imageOptions);
```

> **Совет:** Microsoft Foundry поддерживает аутентификацию без пароля. Добавьте зависимость `com.azure:azure-identity` в ваш проект. Если вы не предоставите API-ключ, реализация автоматически попытается использовать учетные данные Azure из вашей среды.

### Конфигурация моделей GitHub

Для моделей GitHub:

```java
var imageOptions = OpenAiSdkImageOptions.builder()
    .baseUrl("https://models.inference.ai.azure.com")
    .apiKey(System.getenv("GITHUB_TOKEN"))
    .model("dall-e-3")
    .githubModels(true)
    .build();

var imageModel = new OpenAiSdkImageModel(imageOptions);
```

## Наблюдаемость

Реализация OpenAI SDK поддерживает функции наблюдаемости Spring AI через Micrometer. Все операции с моделью изображений инструментированы для мониторинга и трассировки.

## Дополнительные ресурсы

- [Официальный OpenAI Java SDK](https://github.com/openai/openai-java)
- [Документация по API изображений OpenAI](https://platform.openai.com/docs/api-reference/images)
- [Руководство по генерации изображений OpenAI](https://platform.openai.com/docs/guides/images)
- [Модели OpenAI](https://platform.openai.com/docs/models)
- [Документация Microsoft Foundry](https://learn.microsoft.com/en-us/azure/ai-foundry/)
- [Модели GitHub](https://github.com/marketplace/models)
