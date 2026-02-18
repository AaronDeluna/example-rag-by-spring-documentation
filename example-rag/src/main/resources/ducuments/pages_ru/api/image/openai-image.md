# OpenAI Image Generation

Spring AI поддерживает DALL-E, модель генерации изображений от OpenAI.

## Предварительные требования

Вам необходимо создать API-ключ в OpenAI для доступа к моделям ChatGPT.

Создайте учетную запись на https://platform.openai.com/signup[страница регистрации OpenAI] и сгенерируйте токен на https://platform.openai.com/account/api-keys[страница API-ключей].

Проект Spring AI определяет свойство конфигурации с именем `spring.ai.openai.api-key`, которое вы должны установить в значение `API Key`, полученного с openai.com.

Вы можете установить это свойство конфигурации в вашем файле `application.properties`:

```properties
spring.ai.openai.api-key=<ваш-openai-api-ключ>
```

Для повышения безопасности при работе с конфиденциальной информацией, такой как API-ключи, вы можете использовать язык выражений Spring (SpEL) для ссылки на пользовательскую переменную окружения:

```yaml
# В application.yml
spring:
  ai:
    openai:
      api-key: ${OPENAI_API_KEY}
```

```bash
# В вашей среде или .env файле
export OPENAI_API_KEY=<ваш-openai-api-ключ>
```

Вы также можете установить эту конфигурацию программно в коде вашего приложения:

```java
// Получите API-ключ из безопасного источника или переменной окружения
String apiKey = System.getenv("OPENAI_API_KEY");
```

## Автоконфигурация

[ПРИМЕЧАНИЕ]
====
В автоконфигурации Spring AI произошли значительные изменения, касающиеся имен артефактов модулей-стартеров.
Пожалуйста, обратитесь к https://docs.spring.io/spring-ai/reference/upgrade-notes.html[заметкам об обновлении] для получения дополнительной информации.
====

Spring AI предоставляет автоконфигурацию Spring Boot для клиента генерации изображений OpenAI.
Чтобы включить ее, добавьте следующую зависимость в файл `pom.xml` вашего проекта Maven:

```xml
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-starter-model-openai</artifactId>
</dependency>
```

или в файл сборки Gradle `build.gradle`.

```groovy
dependencies {
    implementation 'org.springframework.ai:spring-ai-starter-model-openai'
}
```

> **Совет:** Обратитесь к разделу xref:getting-started.adoc#dependency-management[Управление зависимостями], чтобы добавить BOM Spring AI в ваш файл сборки.

### Свойства генерации изображений

#### Свойства подключения

Префикс `spring.ai.openai` используется в качестве префикса свойств, который позволяет вам подключаться к OpenAI.

[cols="3,5,1"]
| Свойство | Описание | По умолчанию
| spring.ai.openai.base-url   | URL для подключения |  https://api.openai.com
| spring.ai.openai.api-key    | API-ключ           |  -
| spring.ai.openai.organization-id | Опционально вы можете указать, какая организация используется для API-запроса. |  -
| spring.ai.openai.project-id      | Опционально вы можете указать, какой проект используется для API-запроса. |  -

> **Совет:** Для пользователей, которые принадлежат нескольким организациям (или получают доступ к своим проектам через свой устаревший пользовательский API-ключ), вы можете опционально указать, какая организация и проект используются для API-запроса.
Использование этих API-запросов будет учитываться как использование для указанной организации и проекта.

#### Свойства повторных попыток

Префикс `spring.ai.retry` используется в качестве префикса свойств, который позволяет вам настроить механизм повторных попыток для клиента изображений OpenAI.

[cols="3,5,1"]
| Свойство | Описание | По умолчанию

| spring.ai.retry.max-attempts   | Максимальное количество попыток повторного запроса. |  10
| spring.ai.retry.backoff.initial-interval | Начальная продолжительность ожидания для политики экспоненциального отката. |  2 сек.
| spring.ai.retry.backoff.multiplier | Множитель интервала отката. |  5
| spring.ai.retry.backoff.max-interval | Максимальная продолжительность отката. |  3 мин.
| spring.ai.retry.on-client-errors | Если false, выбросить NonTransientAiException и не пытаться повторить для кодов ошибок клиента `4xx` | false
| spring.ai.retry.exclude-on-http-codes | Список кодов состояния HTTP, которые не должны вызывать повторную попытку (например, для выброса NonTransientAiException). | пусто
| spring.ai.retry.on-http-codes | Список кодов состояния HTTP, которые должны вызывать повторную попытку (например, для выброса TransientAiException). | пусто
#### Свойства конфигурации

[ПРИМЕЧАНИЕ]
====
Включение и отключение автоматической конфигурации изображений теперь настраивается через свойства верхнего уровня с префиксом `spring.ai.model.image`.

Чтобы включить, используйте spring.ai.model.image=openai (по умолчанию включено)

Чтобы отключить, используйте spring.ai.model.image=none (или любое значение, не совпадающее с openai)

Это изменение сделано для возможности конфигурации нескольких моделей.
====

Префикс `spring.ai.openai.image` — это префикс свойства, который позволяет вам настраивать реализацию `ImageModel` для OpenAI.

[cols="3,5,1"]
| Свойство | Описание | По умолчанию
| spring.ai.openai.image.enabled (Удалено и больше не актуально) | Включить модель изображений OpenAI.  | true
| spring.ai.model.image | Включить модель изображений OpenAI.  | openai
| spring.ai.openai.image.base-url              | Необязательный параметр, переопределяющий spring.ai.openai.base-url для предоставления специфического URL для чата |  -
| spring.ai.openai.image.api-key               | Необязательный параметр, переопределяющий spring.ai.openai.api-key для предоставления специфического API-ключа для чата |  -
| spring.ai.openai.image.organization-id | Опционально вы можете указать, какая организация используется для API-запроса. |  -
| spring.ai.openai.image.project-id      | Опционально вы можете указать, какой проект используется для API-запроса. |  -
| spring.ai.openai.image.options.n            | Количество изображений для генерации. Должно быть от 1 до 10. Для dall-e-3 поддерживается только n=1.  | -
| spring.ai.openai.image.options.model        | Модель, используемая для генерации изображений.  | OpenAiImageApi.DEFAULT_IMAGE_MODEL
| spring.ai.openai.image.options.quality      | Качество генерируемого изображения. HD создает изображения с более тонкими деталями и большей согласованностью. Этот параметр поддерживается только для dall-e-3. | -
| spring.ai.openai.image.options.response_format | Формат, в котором возвращаются сгенерированные изображения. Должен быть одним из URL или b64_json. | -
| `spring.ai.openai.image.options.size`       | Размер генерируемых изображений. Должен быть одним из 256x256, 512x512 или 1024x1024 для dall-e-2. Должен быть одним из 1024x1024, 1792x1024 или 1024x1792 для моделей dall-e-3. | -
| `spring.ai.openai.image.options.size_width` | Ширина генерируемых изображений. Должна быть одной из 256, 512 или 1024 для dall-e-2.  | -
| `spring.ai.openai.image.options.size_height`| Высота генерируемых изображений. Должна быть одной из 256, 512 или 1024 для dall-e-2. | -
| `spring.ai.openai.image.options.style`      | Стиль генерируемых изображений. Должен быть одним из vivid или natural. Vivid заставляет модель склоняться к созданию гиперреалистичных и драматических изображений. Natural заставляет модель создавать более естественные, менее гиперреалистичные изображения. Этот параметр поддерживается только для dall-e-3. | -
| `spring.ai.openai.image.options.user`       | Уникальный идентификатор, представляющий вашего конечного пользователя, который может помочь OpenAI отслеживать и выявлять злоупотребления. | -

> **Примечание:** Вы можете переопределить общие свойства `spring.ai.openai.base-url`, `spring.ai.openai.api-key`, `spring.ai.openai.organization-id` и `spring.ai.openai.project-id`.
Свойства `spring.ai.openai.image.base-url`, `spring.ai.openai.image.api-key`, `spring.ai.openai.image.organization-id` и `spring.ai.openai.image.project-id`, если они установлены, имеют приоритет над общими свойствами.
Это полезно, если вы хотите использовать разные учетные записи OpenAI для разных моделей и различных конечных точек моделей.

> **Совет:** Все свойства с префиксом `spring.ai.openai.image.options` могут быть переопределены во время выполнения.

## Опции времени выполнения [[image-options]]Документ [OpenAiImageOptions.java](https://github.com/spring-projects/spring-ai/blob/main/models/spring-ai-openai/src/main/java/org/springframework/ai/openai/OpenAiImageOptions.java) предоставляет конфигурации модели, такие как используемая модель, качество, размер и т.д.

При запуске параметры по умолчанию можно настроить с помощью конструктора `OpenAiImageModel(OpenAiImageApi openAiImageApi)` и метода `withDefaultOptions(OpenAiImageOptions defaultOptions)`. В качестве альтернативы можно использовать свойства `spring.ai.openai.image.options.*`, описанные ранее.

Во время выполнения вы можете переопределить параметры по умолчанию, добавив новые, специфичные для запроса, параметры в вызов `ImagePrompt`. Например, чтобы переопределить специфичные для OpenAI параметры, такие как качество и количество создаваемых изображений, используйте следующий пример кода:

```java
ImageResponse response = openaiImageModel.call(
        new ImagePrompt("Светло-кремовый мини золотистый дудль",
        OpenAiImageOptions.builder()
                .quality("hd")
                .N(4)
                .height(1024)
                .width(1024).build())

);
```

> **Совет:** В дополнение к специфичным для модели [OpenAiImageOptions](https://github.com/spring-projects/spring-ai/blob/main/models/spring-ai-openai/src/main/java/org/springframework/ai/openai/OpenAiImageOptions.java) вы можете использовать переносимый [ImageOptions](https://github.com/spring-projects/spring-ai/blob/main/spring-ai-model/src/main/java/org/springframework/ai/image/ImageOptions.java) экземпляр, созданный с помощью [ImageOptionsBuilder#builder()](https://github.com/spring-projects/spring-ai/blob/main/spring-ai-model/src/main/java/org/springframework/ai/image/ImageOptionsBuilder.java).
