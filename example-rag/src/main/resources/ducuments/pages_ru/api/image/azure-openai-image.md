# Azure OpenAI Генерация Изображений

Spring AI поддерживает DALL-E, модель генерации изображений от Azure OpenAI.

## Предварительные требования

Получите ваш `endpoint` и `api-key` Azure OpenAI в разделе Azure OpenAI Service на [Azure Portal](https://portal.azure.com).

Spring AI определяет два свойства конфигурации:

1. `spring.ai.azure.openai.api-key`: Установите это значение равным `API Key`, полученному от Azure.
2. `spring.ai.azure.openai.endpoint`: Установите это значение равным URL-адресу конечной точки, полученному при развертывании вашей модели в Azure.

Вы можете установить эти свойства конфигурации в вашем файле `application.properties`:

```properties
spring.ai.azure.openai.api-key=<your-azure-openai-api-key>
spring.ai.azure.openai.endpoint=<your-azure-openai-endpoint>
```

Для повышения безопасности при работе с конфиденциальной информацией, такой как API ключи, вы можете использовать Spring Expression Language (SpEL) для ссылки на пользовательские переменные окружения:

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
export AZURE_OPENAI_API_KEY=<your-azure-openai-api-key>
export AZURE_OPENAI_ENDPOINT=<your-azure-openai-endpoint>
```

Вы также можете установить эти конфигурации программно в вашем коде приложения:

```java
// Получите API ключ и конечную точку из безопасных источников или переменных окружения
String apiKey = System.getenv("AZURE_OPENAI_API_KEY");
String endpoint = System.getenv("AZURE_OPENAI_ENDPOINT");
```

### Имя Развертывания

Чтобы использовать приложения Azure AI, создайте развертывание Azure AI через [Azure AI Portal](https://oai.azure.com/portal).

В Azure каждый клиент должен указать `Deployment Name`, чтобы подключиться к сервису Azure OpenAI.

Важно понимать, что `Deployment Name` отличается от модели, которую вы выбираете для развертывания.

Например, развертывание с именем 'MyImgAiDeployment' может быть настроено для использования как модели `Dalle3`, так и модели `Dalle2`.

На данный момент, чтобы упростить задачу, вы можете создать развертывание, используя следующие настройки:

Имя Развертывания: `MyImgAiDeployment`  
Имя Модели: `Dalle3`

Эта конфигурация Azure будет соответствовать настройкам по умолчанию для Spring Boot Azure AI Starter и его функции автоконфигурации.

Если вы используете другое имя развертывания, обновите соответствующее свойство конфигурации:

```
spring.ai.azure.openai.image.options.deployment-name=<my deployment name>
```

Разные структуры развертывания Azure OpenAI и OpenAI приводят к свойству в библиотеке клиента Azure OpenAI с именем `deploymentOrModelName`. Это связано с тем, что в OpenAI нет `Deployment Name`, только `Model Name`.

### Добавление Репозиториев и BOM

Артефакты Spring AI публикуются в репозиториях Maven Central и Spring Snapshot. Обратитесь к разделу xref:getting-started.adoc#artifact-repositories[Artifact Repositories], чтобы добавить эти репозитории в вашу систему сборки.

Чтобы помочь с управлением зависимостями, Spring AI предоставляет BOM (спецификация материалов), чтобы гарантировать, что одна и та же версия Spring AI используется на протяжении всего проекта. Обратитесь к разделу xref:getting-started.adoc#dependency-management[Dependency Management], чтобы добавить BOM Spring AI в вашу систему сборки.

## Автоконфигурация[NOTE]
====
В Spring AI произошли значительные изменения в автонастройке и названиях артефактов стартовых модулей.
Пожалуйста, обратитесь к https://docs.spring.io/spring-ai/reference/upgrade-notes.html[заметкам об обновлении] для получения дополнительной информации.
====

Spring AI предоставляет автонастройку Spring Boot для клиента Azure OpenAI Chat.
Чтобы включить её, добавьте следующую зависимость в файл `pom.xml` вашего проекта Maven:

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

### Свойства генерации изображений

[NOTE]
====
Включение и отключение автонастроек изображений теперь настраивается через свойства верхнего уровня с префиксом `spring.ai.model.image`.

Чтобы включить, используйте spring.ai.model.image=azure-openai (по умолчанию включено)

Чтобы отключить, используйте spring.ai.model.image=none (или любое значение, которое не соответствует azure-openai)

Это изменение сделано для возможности настройки нескольких моделей.
====

Префикс `spring.ai.openai.image` — это префикс свойства, который позволяет вам настраивать реализацию `ImageModel` для OpenAI.

| Свойство | Описание | По умолчанию |
| --- | --- | --- |
| spring.ai.azure.openai.image.enabled (Удалено и больше не актуально) | Включить модель изображения OpenAI. | true |
| spring.ai.model.image | Включить модель изображения OpenAI. | azure-openai |
| spring.ai.azure.openai.image.options.n | Количество изображений для генерации. Должно быть от 1 до 10. Для dall-e-3 поддерживается только n=1. | - |
| spring.ai.azure.openai.image.options.model | Модель, используемая для генерации изображений. | AzureOpenAiImageOptions.DEFAULT_IMAGE_MODEL |
| spring.ai.azure.openai.image.options.quality | Качество генерируемого изображения. HD создает изображения с более тонкими деталями и большей согласованностью. Этот параметр поддерживается только для dall-e-3. | - |
| spring.ai.azure.openai.image.options.response_format | Формат, в котором возвращаются сгенерированные изображения. Должен быть одним из URL или b64_json. | - |
| `spring.ai.openai.image.options.size` | Размер генерируемых изображений. Должен быть одним из 256x256, 512x512 или 1024x1024 для dall-e-2. Должен быть одним из 1024x1024, 1792x1024 или 1024x1792 для моделей dall-e-3. | - |
| `spring.ai.openai.image.options.size_width` | Ширина генерируемых изображений. Должна быть одной из 256, 512 или 1024 для dall-e-2. | - |
| `spring.ai.openai.image.options.size_height` | Высота генерируемых изображений. Должна быть одной из 256, 512 или 1024 для dall-e-2. | - |
| `spring.ai.openai.image.options.style` | Стиль генерируемых изображений. Должен быть одним из vivid или natural. Vivid заставляет модель склоняться к созданию гиперреалистичных и драматических изображений. Natural заставляет модель создавать более естественные, менее гиперреалистичные изображения. Этот параметр поддерживается только для dall-e-3. | - |
| `spring.ai.openai.image.options.user` | Уникальный идентификатор, представляющий вашего конечного пользователя, который может помочь Azure OpenAI отслеживать и обнаруживать злоупотребления. | - |

#### Свойства подключения

Префикс `spring.ai.openai` используется как префикс свойства, который позволяет вам подключаться к Azure OpenAI.

| Свойство | Описание | По умолчанию |
| --- | --- | --- |
| spring.ai.azure.openai.endpoint | URL для подключения | https://my-dalle3.openai.azure.com/ |
| spring.ai.azure.openai.apiKey | API-ключ | - |


При запуске параметры по умолчанию можно настроить с помощью конструктора `AzureOpenAiImageModel(OpenAiImageApi openAiImageApi)` и метода `withDefaultOptions(OpenAiImageOptions defaultOptions)`. В качестве альтернативы можно использовать свойства `spring.ai.azure.openai.image.options.*`, описанные ранее.

Во время выполнения вы можете переопределить параметры по умолчанию, добавив новые, специфичные для запроса, параметры в вызов `ImagePrompt`. Например, чтобы переопределить специфичные для OpenAI параметры, такие как качество и количество создаваемых изображений, используйте следующий пример кода:

```java
ImageResponse response = azureOpenaiImageModel.call(
        new ImagePrompt("Светло-кремовый мини-золотистый дудль",
        OpenAiImageOptions.builder()
                .quality("hd")
                .N(4)
                .height(1024)
                .width(1024).build())

);
```

> **Совет:** В дополнение к специфичным для модели [AzureOpenAiImageOptions](https://github.com/spring-projects/spring-ai/blob/main/models/spring-ai-azure-openai/src/main/java/org/springframework/ai/azure/openai/AzureOpenAiImageOptions.java) вы можете использовать переносимый экземпляр [ImageOptions](https://github.com/spring-projects/spring-ai/blob/main/spring-ai-model/src/main/java/org/springframework/ai/image/ImageOptions.java), созданный с помощью [ImageOptionsBuilder#builder()](https://github.com/spring-projects/spring-ai/blob/main/spring-ai-model/src/main/java/org/springframework/ai/image/ImageOptionsBuilder.java).
