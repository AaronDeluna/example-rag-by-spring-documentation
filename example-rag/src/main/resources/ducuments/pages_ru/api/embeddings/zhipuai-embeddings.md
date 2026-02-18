# ZhiPuAI Embeddings

Spring AI поддерживает модели текстовых эмбеддингов ZhiPuAI. Эмбеддинги текста ZhiPuAI измеряют связанность текстовых строк. Эмбеддинг — это вектор (список) чисел с плавающей запятой. Расстояние между двумя векторами измеряет их связанность. Небольшие расстояния указывают на высокую связанность, а большие расстояния — на низкую связанность.

## Предварительные требования

Вам необходимо создать API с ZhiPuAI для доступа к языковым моделям ZhiPuAI.

Создайте учетную запись на https://open.bigmodel.cn/login[страница регистрации ZhiPu AI] и сгенерируйте токен на https://open.bigmodel.cn/usercenter/apikeys[страница API ключей].

Проект Spring AI определяет свойство конфигурации с именем `spring.ai.zhipuai.api-key`, которое вы должны установить в значение `API Key`, полученного на странице API ключей.

Вы можете установить это свойство конфигурации в вашем файле `application.properties`:

```properties
spring.ai.zhipuai.api-key=<ваш-zhipuai-api-key>
```

Для повышения безопасности при работе с конфиденциальной информацией, такой как API ключи, вы можете использовать язык выражений Spring (SpEL) для ссылки на переменную окружения:

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

Артефакты Spring AI публикуются в репозиториях Maven Central и Spring Snapshot. Обратитесь к разделу xref:getting-started.adoc#artifact-repositories[Артефактные репозитории], чтобы добавить эти репозитории в вашу систему сборки.

Чтобы помочь с управлением зависимостями, Spring AI предоставляет BOM (спецификация материалов), чтобы гарантировать, что в проекте используется согласованная версия Spring AI. Обратитесь к разделу xref:getting-started.adoc#dependency-management[Управление зависимостями], чтобы добавить BOM Spring AI в вашу систему сборки.

## Автоконфигурация

[NOTE]
====
В автоконфигурации Spring AI произошли значительные изменения в названиях артефактов модулей стартеров. Пожалуйста, обратитесь к https://docs.spring.io/spring-ai/reference/upgrade-notes.html[заметкам об обновлении] для получения дополнительной информации.
====

Spring AI предоставляет автоконфигурацию Spring Boot для модели эмбеддингов Azure ZhiPuAI. Чтобы включить ее, добавьте следующую зависимость в файл `pom.xml` вашего проекта Maven:

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

> **Совет:** Обратитесь к разделу xref:getting-started.adoc#dependency-management[Управление зависимостями], чтобы добавить BOM Spring AI в ваш файл сборки.

### Свойства эмбеддингов

#### Свойства повторных попыток

Префикс `spring.ai.retry` используется как префикс свойства, который позволяет вам настроить механизм повторных попыток для модели эмбеддингов ZhiPuAI.

[cols="3,5,1", stripes=even]
| Свойство | Описание | По умолчанию

| spring.ai.retry.max-attempts   | Максимальное количество попыток повторного запроса. |  10
| spring.ai.retry.backoff.initial-interval | Начальная продолжительность ожидания для политики экспоненциального увеличения. |  2 сек.
| spring.ai.retry.backoff.multiplier | Множитель интервала ожидания. |  5
| spring.ai.retry.backoff.max-interval | Максимальная продолжительность ожидания. |  3 мин.
| spring.ai.retry.on-client-errors | Если false, выбросить NonTransientAiException и не пытаться повторить запрос для кодов ошибок клиента `4xx` | false
| spring.ai.retry.exclude-on-http-codes | Список кодов состояния HTTP, которые не должны вызывать повторный запрос (например, для выброса NonTransientAiException). | пусто
| spring.ai.retry.on-http-codes | Список кодов состояния HTTP, которые должны вызывать повторный запрос (например, для выброса TransientAiException). | пусто
#### Свойства подключения

Префикс `spring.ai.zhipuai` используется в качестве префикса свойств, который позволяет вам подключаться к ZhiPuAI.

[cols="3,5,1", stripes=even]
| Свойство | Описание | По умолчанию

| spring.ai.zhipuai.base-url   | URL для подключения |  https://open.bigmodel.cn/api/paas
| spring.ai.zhipuai.api-key    | API-ключ           |  -

#### Свойства конфигурации

[NOTE]
====
Включение и отключение автонастроек встраивания теперь настраивается через свойства верхнего уровня с префиксом `spring.ai.model.embedding`.

Чтобы включить, используйте spring.ai.model.embedding=zhipuai (по умолчанию включено)

Чтобы отключить, используйте spring.ai.model.embedding=none (или любое значение, которое не соответствует zhipuai)

Это изменение сделано для того, чтобы позволить конфигурацию нескольких моделей.
====

Префикс `spring.ai.zhipuai.embedding` — это префикс свойств, который настраивает реализацию `EmbeddingModel` для ZhiPuAI.

[cols="3,5,1", stripes=even]
| Свойство | Описание | По умолчанию

| spring.ai.zhipuai.embedding.enabled (Удалено и больше не актуально) | Включить модель встраивания ZhiPuAI.  | true
| spring.ai.model.embedding | Включить модель встраивания ZhiPuAI.  | zhipuai
| spring.ai.zhipuai.embedding.base-url   | Необязательный параметр, переопределяющий spring.ai.zhipuai.base-url для предоставления специфического URL для встраивания | -
| spring.ai.zhipuai.embedding.api-key    | Необязательный параметр, переопределяющий spring.ai.zhipuai.api-key для предоставления специфического API-ключа для встраивания  | -
| spring.ai.zhipuai.embedding.options.model      | Модель для использования      | embedding-2
| spring.ai.zhipuai.embedding.options.dimensions      | Количество измерений, значение по умолчанию — 2048, когда модель — embedding-3 | -

> **Примечание:** Вы можете переопределить общие `spring.ai.zhipuai.base-url` и `spring.ai.zhipuai.api-key` для реализаций `ChatModel` и `EmbeddingModel`.
Свойства `spring.ai.zhipuai.embedding.base-url` и `spring.ai.zhipuai.embedding.api-key`, если они установлены, имеют приоритет над общими свойствами.
Аналогично, свойства `spring.ai.zhipuai.chat.base-url` и `spring.ai.zhipuai.chat.api-key`, если они установлены, имеют приоритет над общими свойствами.
Это полезно, если вы хотите использовать разные учетные записи ZhiPuAI для разных моделей и различных конечных точек моделей.

> **Совет:** Все свойства с префиксом `spring.ai.zhipuai.embedding.options` могут быть переопределены во время выполнения, добавив специфические для запроса <<embedding-options>> в вызов `EmbeddingRequest`.

## Опции времени выполнения [[embedding-options]]

Файл https://github.com/spring-projects/spring-ai/blob/main/models/spring-ai-zhipuai/src/main/java/org/springframework/ai/zhipuai/ZhiPuAiEmbeddingOptions.java[ZhiPuAiEmbeddingOptions.java] предоставляет конфигурации ZhiPuAI, такие как используемая модель и т.д.

Опции по умолчанию также могут быть настроены с помощью свойств `spring.ai.zhipuai.embedding.options`.

При старте используйте конструктор `ZhiPuAiEmbeddingModel`, чтобы установить параметры по умолчанию, используемые для всех запросов на встраивание.
Во время выполнения вы можете переопределить параметры по умолчанию, используя экземпляр `ZhiPuAiEmbeddingOptions` как часть вашего `EmbeddingRequest`.

Например, чтобы переопределить имя модели по умолчанию для конкретного запроса:

```java
EmbeddingResponse embeddingResponse = embeddingModel.call(
    new EmbeddingRequest(List.of("Hello World", "World is big and salvation is near"),
        ZhiPuAiEmbeddingOptions.builder()
            .model("Different-Embedding-Model-Deployment-Name")
        .build()));
```

## Пример контроллераЭто создаст реализацию `EmbeddingModel`, которую вы можете внедрить в свой класс. Вот пример простого класса `@Controller`, который использует реализацию `EmbeddingModel`.

```application.properties
spring.ai.zhipuai.api-key=YOUR_API_KEY
spring.ai.zhipuai.embedding.options.model=embedding-2
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

Если вы не используете Spring Boot, вы можете вручную настроить модель встраивания ZhiPuAI. Для этого добавьте зависимость `spring-ai-zhipuai` в файл `pom.xml` вашего проекта Maven:
```xml
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-zhipuai</artifactId>
</dependency>
```

или в файл сборки Gradle `build.gradle`.

```groovy
dependencies {
    implementation 'org.springframework.ai:spring-ai-zhipuai'
}
```

> **Совет:** Обратитесь к разделу xref:getting-started.adoc#dependency-management[Управление зависимостями], чтобы добавить BOM Spring AI в ваш файл сборки.

> **Примечание:** Зависимость `spring-ai-zhipuai` также предоставляет доступ к `ZhiPuAiChatModel`. Для получения дополнительной информации о `ZhiPuAiChatModel` обратитесь к разделу [ZhiPuAI Chat Client](../chat/zhipuai-chat.html).

Далее создайте экземпляр `ZhiPuAiEmbeddingModel` и используйте его для вычисления сходства между двумя входными текстами:

```java
var zhiPuAiApi = new ZhiPuAiApi(System.getenv("ZHIPUAI_API_KEY"));

var embeddingModel = new ZhiPuAiEmbeddingModel(api, MetadataMode.EMBED,
				ZhiPuAiEmbeddingOptions.builder()
						.model("embedding-3")
						.dimensions(1536)
						.build());

EmbeddingResponse embeddingResponse = this.embeddingModel
	.embedForResponse(List.of("Hello World", "World is big and salvation is near"));
```

`ZhiPuAiEmbeddingOptions` предоставляет информацию о конфигурации для запросов на встраивание. Класс опций предлагает метод `builder()` для удобного создания опций.
