# Генерация изображений Stability AI

Spring AI поддерживает модель генерации изображений из текста Stability AI по адресу https://platform.stability.ai/docs/api-reference#tag/v1generation.

## Предварительные требования

Вам необходимо создать API-ключ в Stability AI для доступа к их AI-моделям. Следуйте их https://platform.stability.ai/docs/getting-started/authentication[документации по началу работы], чтобы получить ваш API-ключ.

Проект Spring AI определяет свойство конфигурации с именем `spring.ai.stabilityai.api-key`, которое вы должны установить в значение `API Key`, полученное от Stability AI.

Вы можете установить это свойство конфигурации в вашем файле `application.properties`:

```properties
spring.ai.stabilityai.api-key=<ваш-api-ключ-stabilityai>
```

Для повышения безопасности при работе с конфиденциальной информацией, такой как API-ключи, вы можете использовать язык выражений Spring (SpEL) для ссылки на пользовательскую переменную окружения:

```yaml
# В application.yml
spring:
  ai:
    stabilityai:
      api-key: ${STABILITYAI_API_KEY}
```

```bash
# В вашем окружении или .env файле
export STABILITYAI_API_KEY=<ваш-api-ключ-stabilityai>
```

Вы также можете установить эту конфигурацию программно в вашем коде приложения:

```java
// Получите API-ключ из безопасного источника или переменной окружения
String apiKey = System.getenv("STABILITYAI_API_KEY");
```

## Автоконфигурация

[ПРИМЕЧАНИЕ]
====
В автоконфигурации Spring AI произошли значительные изменения в названиях артефактов стартовых модулей.
Пожалуйста, обратитесь к https://docs.spring.io/spring-ai/reference/upgrade-notes.html[заметкам об обновлении] для получения дополнительной информации.
====

Spring AI предоставляет автоконфигурацию Spring Boot для клиента генерации изображений Stability AI.
Чтобы включить ее, добавьте следующую зависимость в файл `pom.xml` вашего проекта Maven:

```xml
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-starter-model-stability-ai</artifactId>
</dependency>
```

или в файл сборки Gradle `build.gradle`.

```groovy
dependencies {
    implementation 'org.springframework.ai:spring-ai-starter-model-stability-ai'
}
```

> **Совет:** Обратитесь к разделу xref:getting-started.adoc#dependency-management[Управление зависимостями], чтобы добавить BOM Spring AI в ваш файл сборки.


### Свойства генерации изображенийThe prefix `spring.ai.stabilityai` используется как префикс свойств, который позволяет вам подключиться к Stability AI.

| Свойство | Описание | По умолчанию |
| --- | --- | --- |

| spring.ai.stabilityai.base-url | URL для подключения | https://api.stability.ai/v1 |
| --- | --- | --- |
| spring.ai.stabilityai.api-key | API-ключ | - |

[NOTE]
====
Включение и отключение автонастроек изображений теперь настраивается через свойства верхнего уровня с префиксом `spring.ai.model.image`.

Чтобы включить, используйте spring.ai.model.image=stabilityai (по умолчанию включено)

Чтобы отключить, используйте spring.ai.model.image=none (или любое значение, которое не соответствует stabilityai)

Это изменение сделано для возможности конфигурации нескольких моделей.
====

Префикс `spring.ai.stabilityai.image` — это префикс свойств, который позволяет вам настроить реализацию `ImageModel` для Stability AI.

| Свойство | Описание | По умолчанию |
| --- | --- | --- |

| spring.ai.stabilityai.image.enabled (Удалено и больше не актуально) | Включить модель изображения Stability AI. | true |
| --- | --- | --- |
| spring.ai.model.image | Включить модель изображения Stability AI. | stabilityai |
| spring.ai.stabilityai.image.base-url | Необязательный параметр, переопределяющий spring.ai.openai.base-url для предоставления конкретного URL | `+https://api.stability.ai/v1+` |
| spring.ai.stabilityai.image.api-key | Необязательный параметр, переопределяющий spring.ai.openai.api-key для предоставления конкретного api-ключа | - |
| spring.ai.stabilityai.image.option.n | Количество изображений для генерации. Должно быть от 1 до 10. | 1 |
| spring.ai.stabilityai.image.option.model | Двигатель/модель, используемая в Stability AI. Модель передается в URL как параметр пути. | `stable-diffusion-v1-6` |
| spring.ai.stabilityai.image.option.width | Ширина генерируемого изображения в пикселях, кратная 64. Применяется валидация размеров, специфичная для движка. | 512 |
| spring.ai.stabilityai.image.option.height | Высота генерируемого изображения в пикселях, кратная 64. Применяется валидация размеров, специфичная для движка. | 512 |
| spring.ai.stabilityai.image.option.responseFormat | Формат, в котором возвращаются сгенерированные изображения. Должен быть "application/json" или "image/png". | - |
| spring.ai.stabilityai.image.option.cfg_scale | Уровень строгости соблюдения процесса диффузии тексту подсказки. Диапазон: от 0 до 35. | 7 |
| spring.ai.stabilityai.image.option.clip_guidance_preset | Передайте стильный пресет, чтобы направить модель изображения к определенному стилю. Этот список стильных пресетов может изменяться. | `NONE` |
| spring.ai.stabilityai.image.option.sampler | Какой сэмплер использовать для процесса диффузии. Если это значение опущено, будет автоматически выбран подходящий сэмплер. | - |
| spring.ai.stabilityai.image.option.seed | Случайное семя шума (опустите этот параметр или используйте 0 для случайного семени). Допустимый диапазон: от 0 до 4294967295. | 0 |
| spring.ai.stabilityai.image.option.steps | Количество шагов диффузии для выполнения. Допустимый диапазон: от 10 до 50. | 30 |
| spring.ai.stabilityai.image.option.style_preset | Передайте стильный пресет, чтобы направить модель изображения к определенному стилю. Этот список стильных пресетов может изменяться. | - |



При запуске параметры по умолчанию можно настроить с помощью конструктора `StabilityAiImageModel(StabilityAiApi stabilityAiApi, StabilityAiImageOptions options)`. В качестве альтернативы можно использовать свойства `spring.ai.openai.image.options.*`, описанные ранее.

Во время выполнения вы можете переопределить параметры по умолчанию, добавив новые, специфичные для запроса, параметры в вызов `ImagePrompt`. Например, чтобы переопределить специфичные для Stability AI параметры, такие как качество и количество создаваемых изображений, используйте следующий пример кода:

```java
ImageResponse response = stabilityaiImageModel.call(
        new ImagePrompt("A light cream colored mini golden doodle",
        StabilityAiImageOptions.builder()
                .stylePreset("cinematic")
                .N(4)
                .height(1024)
                .width(1024).build())

);
```

> **Совет:** В дополнение к специфичным для модели [StabilityAiImageOptions](https://github.com/spring-projects/spring-ai/blob/main/models/spring-ai-stabilityai/src/main/java/org/springframework/ai/stabilityai/api/StabilityAiImageOptions.java) вы можете использовать переносимый экземпляр [ImageOptions](https://github.com/spring-projects/spring-ai/blob/main/spring-ai-model/src/main/java/org/springframework/ai/image/ImageOptions.java), созданный с помощью [ImageOptionsBuilder#builder()](https://github.com/spring-projects/spring-ai/blob/main/spring-ai-model/src/main/java/org/springframework/ai/image/ImageOptionsBuilder.java).
