# ZhiPuAI Генерация Изображений

Spring AI поддерживает CogView, модель генерации изображений от ZhiPuAI.

## Предварительные требования

Вам необходимо создать API с ZhiPuAI для доступа к языковым моделям ZhiPu AI.

Создайте учетную запись на https://open.bigmodel.cn/login[страница регистрации ZhiPu AI] и сгенерируйте токен на https://open.bigmodel.cn/usercenter/apikeys[страница API ключей].

Проект Spring AI определяет свойство конфигурации с именем `spring.ai.zhipuai.api-key`, которое вы должны установить в значение `API Key`, полученного на странице API ключей.

Вы можете установить это свойство конфигурации в вашем файле `application.properties`:

```properties
spring.ai.zhipuai.api-key=<ваш-zhipuai-api-key>
```

Для повышения безопасности при работе с конфиденциальной информацией, такой как API ключи, вы можете использовать Spring Expression Language (SpEL) для ссылки на пользовательскую переменную окружения:

```yaml
# В application.yml
spring:
  ai:
    zhipuai:
      api-key: ${ZHIPUAI_API_KEY}
```

```bash
# В вашем окружении или .env файле
export ZHIPUAI_API_KEY=<ваш-zhipuai-api-key>
```

Вы также можете установить эту конфигурацию программно в вашем коде приложения:

```java
// Получите API ключ из безопасного источника или переменной окружения
String apiKey = System.getenv("ZHIPUAI_API_KEY");
```

### Добавление репозиториев и BOM

Артефакты Spring AI публикуются в репозиториях Maven Central и Spring Snapshot.
Смотрите раздел xref:getting-started.adoc#artifact-repositories[Артефактные репозитории], чтобы добавить эти репозитории в вашу систему сборки.

Для упрощения управления зависимостями Spring AI предоставляет BOM (спецификация материалов), чтобы гарантировать, что одна и та же версия Spring AI используется на протяжении всего проекта. Смотрите раздел xref:getting-started.adoc#dependency-management[Управление зависимостями], чтобы добавить BOM Spring AI в вашу систему сборки.

## Автоконфигурация

[ПРИМЕЧАНИЕ]
====
В автоконфигурации Spring AI произошли значительные изменения в названиях артефактов модулей стартеров.
Пожалуйста, обратитесь к https://docs.spring.io/spring-ai/reference/upgrade-notes.html[заметкам об обновлении] для получения дополнительной информации.
====

Spring AI предоставляет автоконфигурацию Spring Boot для клиента чата ZhiPuAI.
Чтобы включить ее, добавьте следующую зависимость в файл `pom.xml` вашего проекта Maven:

```xml
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-starter-model-zhipuai</artifactId>
</dependency>
```

или в файл сборки Gradle `build.gradle`.

```groovy
dependencies {
    implementation 'org.springframework.ai:spring-ai-starter-model-zhipuai'
}
```

> **Совет:** Смотрите раздел xref:getting-started.adoc#dependency-management[Управление зависимостями], чтобы добавить BOM Spring AI в ваш файл сборки.

### Свойства генерации изображений

[ПРИМЕЧАНИЕ]
====
Включение и отключение автоконфигураций изображений теперь настраивается через свойства верхнего уровня с префиксом `spring.ai.model.image`.

Чтобы включить, spring.ai.model.image=stabilityai (по умолчанию включено)

Чтобы отключить, spring.ai.model.image=none (или любое значение, которое не соответствует stabilityai)

Это изменение сделано для того, чтобы позволить конфигурацию нескольких моделей.
====

Префикс `spring.ai.zhipuai.image` — это префикс свойства, который позволяет вам настроить реализацию `ImageModel` для ZhiPuAI.

| Свойство | Описание | По умолчанию |
| --- | --- | --- |
| spring.ai.zhipuai.image.enabled (Удалено и больше не актуально) | Включить модель изображения ZhiPuAI. | true |
| spring.ai.model.image | Включить модель изображения ZhiPuAI. | zhipuai |
| spring.ai.zhipuai.image.base-url | Необязательный параметр, переопределяющий spring.ai.zhipuai.base-url для предоставления специфического URL для чата | - |
| spring.ai.zhipuai.image.api-key | Необязательный параметр, переопределяющий spring.ai.zhipuai.api-key для предоставления специфического API ключа для чата | - |
| spring.ai.zhipuai.image.options.model | Модель, используемая для генерации изображений. | cogview-3 |
| spring.ai.zhipuai.image.options.user | Уникальный идентификатор, представляющий вашего конечного пользователя, который может помочь ZhiPuAI отслеживать и обнаруживать злоупотребления. | - |
#### Свойства подключения

Префикс `spring.ai.zhipuai` используется в качестве префикса свойств, который позволяет вам подключиться к ZhiPuAI.

| Свойство | Описание | По умолчанию |
| --- | --- | --- |
| spring.ai.zhipuai.base-url | URL для подключения | https://open.bigmodel.cn/api/paas |
| spring.ai.zhipuai.api-key | API-ключ | - |

#### Свойства конфигурации


#### Свойства повторных попыток

Префикс `spring.ai.retry` используется в качестве префикса свойств, который позволяет вам настроить механизм повторных попыток для клиента ZhiPuAI Image.

| Свойство | Описание | По умолчанию |
| --- | --- | --- |

| spring.ai.retry.max-attempts | Максимальное количество попыток повторного запроса. | 10 |
| --- | --- | --- |
| spring.ai.retry.backoff.initial-interval | Начальная продолжительность ожидания для политики экспоненциального увеличения. | 2 сек. |
| spring.ai.retry.backoff.multiplier | Множитель для интервала увеличения. | 5 |
| spring.ai.retry.backoff.max-interval | Максимальная продолжительность увеличения. | 3 мин. |
| spring.ai.retry.on-client-errors | Если false, выбрасывается NonTransientAiException, и повторная попытка не осуществляется для кодов ошибок клиента `4xx` | false |
| spring.ai.retry.exclude-on-http-codes | Список кодов состояния HTTP, которые не должны вызывать повторную попытку (например, для выброса NonTransientAiException). | пусто |
| spring.ai.retry.on-http-codes | Список кодов состояния HTTP, которые должны вызывать повторную попытку (например, для выброса TransientAiException). | пусто |



Файл https://github.com/spring-projects/spring-ai/blob/main/models/spring-ai-zhipuai/src/main/java/org/springframework/ai/zhipuai/ZhiPuAiImageOptions.java[ZhiPuAiImageOptions.java] предоставляет конфигурации модели, такие как используемая модель, качество, размер и т. д.

При запуске параметры по умолчанию могут быть настроены с помощью конструктора `ZhiPuAiImageModel(ZhiPuAiImageApi zhiPuAiImageApi)` и метода `withDefaultOptions(ZhiPuAiImageOptions defaultOptions)`. В качестве альтернативы используйте свойства `spring.ai.zhipuai.image.options.*`, описанные ранее.

Во время выполнения вы можете переопределить параметры по умолчанию, добавив новые, специфичные для запроса, параметры в вызов `ImagePrompt`.
Например, чтобы переопределить специфичные для ZhiPuAI параметры, такие как качество и количество создаваемых изображений, используйте следующий пример кода:

```java
ImageResponse response = zhiPuAiImageModel.call(
        new ImagePrompt("Светло-кремового цвета мини-золотистый дудль",
        ZhiPuAiImageOptions.builder()
                .quality("hd")
                .N(4)
                .height(1024)
                .width(1024).build())

);
```

> **Совет:** В дополнение к специфичным для модели https://github.com/spring-projects/spring-ai/blob/main/models/spring-ai-zhipuai/src/main/java/org/springframework/ai/zhipuai/ZhiPuAiImageOptions.java[ZhiPuAiImageOptions] вы можете использовать переносимый экземпляр https://github.com/spring-projects/spring-ai/blob/main/spring-ai-model/src/main/java/org/springframework/ai/image/ImageOptions.java[ImageOptions], созданный с помощью https://github.com/spring-projects/spring-ai/blob/main/spring-ai-model/src/main/java/org/springframework/ai/image/ImageOptionsBuilder.java[ImageOptionsBuilder#builder()].
