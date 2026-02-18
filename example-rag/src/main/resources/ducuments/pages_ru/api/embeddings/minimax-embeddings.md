# MiniMax Chat

Spring AI поддерживает различные языковые модели AI от MiniMax. Вы можете взаимодействовать с языковыми моделями MiniMax и создать многоязычного разговорного помощника на основе моделей MiniMax.

## Предварительные требования

Вам необходимо создать API с MiniMax для доступа к языковым моделям MiniMax.

Создайте учетную запись на https://www.minimaxi.com/login[страница регистрации MiniMax] и сгенерируйте токен на https://www.minimaxi.com/user-center/basic-information/interface-key[странице API ключей].

Проект Spring AI определяет свойство конфигурации с именем `spring.ai.minimax.api-key`, которое вы должны установить в значение `API Key`, полученное со страницы API ключей.

Вы можете установить это свойство конфигурации в вашем файле `application.properties`:

```properties
spring.ai.minimax.api-key=<ваш-minimax-api-key>
```

Для повышения безопасности при работе с конфиденциальной информацией, такой как API ключи, вы можете использовать язык выражений Spring (SpEL) для ссылки на переменную окружения:

```yaml
# В application.yml
spring:
  ai:
    minimax:
      api-key: ${MINIMAX_API_KEY}
```

```bash
# В вашем окружении или .env файле
export MINIMAX_API_KEY=<ваш-minimax-api-key>
```

Вы также можете установить эту конфигурацию программно в коде вашего приложения:

```java
// Получите API ключ из безопасного источника или переменной окружения
String apiKey = System.getenv("MINIMAX_API_KEY");
```

### Добавление репозиториев и BOM

Артефакты Spring AI публикуются в репозиториях Maven Central и Spring Snapshot.
Обратитесь к разделу xref:getting-started.adoc#artifact-repositories[Артефактные репозитории], чтобы добавить эти репозитории в вашу систему сборки.

Чтобы помочь с управлением зависимостями, Spring AI предоставляет BOM (спецификация материалов), чтобы гарантировать, что одна и та же версия Spring AI используется на протяжении всего проекта. Обратитесь к разделу xref:getting-started.adoc#dependency-management[Управление зависимостями], чтобы добавить BOM Spring AI в вашу систему сборки.

## Автоконфигурация

[ПРИМЕЧАНИЕ]
====
В автоконфигурации Spring AI произошли значительные изменения в названиях артефактов модулей стартеров.
Пожалуйста, обратитесь к https://docs.spring.io/spring-ai/reference/upgrade-notes.html[заметкам об обновлении] для получения дополнительной информации.
====

Spring AI предоставляет автоконфигурацию Spring Boot для модели встраивания Azure MiniMax.
Чтобы включить ее, добавьте следующую зависимость в файл `pom.xml` вашего проекта Maven:

```xml
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-starter-model-minimax</artifactId>
</dependency>
```

или в файл сборки Gradle `build.gradle`.

```groovy
dependencies {
    implementation 'org.springframework.ai:spring-ai-starter-model-minimax'
}
```

> **Совет:** Обратитесь к разделу xref:getting-started.adoc#dependency-management[Управление зависимостями], чтобы добавить BOM Spring AI в ваш файл сборки.

### Свойства встраивания

#### Свойства повторной попытки

Префикс `spring.ai.retry` используется как префикс свойства, который позволяет вам настроить механизм повторной попытки для модели встраивания MiniMax.

[cols="3,5,1", stripes=even]
|====
| Свойство | Описание | По умолчанию

| spring.ai.retry.max-attempts   | Максимальное количество попыток повторной попытки. |  10
| spring.ai.retry.backoff.initial-interval | Начальная продолжительность ожидания для политики экспоненциального отката. |  2 сек.
| spring.ai.retry.backoff.multiplier | Множитель интервала отката. |  5
| spring.ai.retry.backoff.max-interval | Максимальная продолжительность отката. |  3 мин.
| spring.ai.retry.on-client-errors | Если false, выбросить NonTransientAiException и не пытаться повторить для кодов ошибок клиента `4xx` | false
| spring.ai.retry.exclude-on-http-codes | Список кодов состояния HTTP, которые не должны вызывать повторную попытку (например, для выброса NonTransientAiException). | пусто
| spring.ai.retry.on-http-codes | Список кодов состояния HTTP, которые должны вызывать повторную попытку (например, для выброса TransientAiException). | пусто
|====

#### Свойства подключения

Префикс `spring.ai.minimax` используется как префикс свойства, который позволяет вам подключиться к MiniMax.

[cols="3,5,1", stripes=even]
|====
| Свойство | Описание | По умолчанию

| spring.ai.minimax.base-url   | URL для подключения |  https://api.minimax.chat
| spring.ai.minimax.api-key    | API ключ           |  -
|====

#### Свойства конфигурации

[ПРИМЕЧАНИЕ]
====
Включение и отключение автоконфигураций встраивания теперь настраивается через свойства верхнего уровня с префиксом `spring.ai.model.embedding`.

Чтобы включить, spring.ai.model.embedding=minimax (по умолчанию включено)

Чтобы отключить, spring.ai.model.embedding=none (или любое значение, которое не соответствует minimax)

Это изменение сделано для того, чтобы позволить конфигурацию нескольких моделей.
====

Префикс `spring.ai.minimax.embedding` — это префикс свойства, который настраивает реализацию `EmbeddingModel` для MiniMax.

[cols="3,5,1", stripes=even]
|====
| Свойство | Описание | По умолчанию

| spring.ai.minimax.embedding.enabled (Удалено и больше не действительно) | Включить модель встраивания MiniMax.  | true
| spring.ai.model.embedding | Включить модель встраивания MiniMax.  | minimax
| spring.ai.minimax.embedding.base-url   | Необязательный переопределяет spring.ai.minimax.base-url для предоставления специфического для встраивания URL | -
| spring.ai.minimax.embedding.api-key    | Необязательный переопределяет spring.ai.minimax.api-key для предоставления специфического для встраивания api-key  | -
| spring.ai.minimax.embedding.options.model      | Модель для использования      | embo-01
|====

> **Примечание:** Вы можете переопределить общие `spring.ai.minimax.base-url` и `spring.ai.minimax.api-key` для реализаций `ChatModel` и `EmbeddingModel`.
Свойства `spring.ai.minimax.embedding.base-url` и `spring.ai.minimax.embedding.api-key`, если установлены, имеют приоритет над общими свойствами.
Аналогично, свойства `spring.ai.minimax.chat.base-url` и `spring.ai.minimax.chat.api-key`, если установлены, имеют приоритет над общими свойствами.
Это полезно, если вы хотите использовать разные учетные записи MiniMax для разных моделей и разные конечные точки моделей.

> **Совет:** Все свойства с префиксом `spring.ai.minimax.embedding.options` могут быть переопределены во время выполнения, добавив специфичные для запроса <<embedding-options>> в вызов `EmbeddingRequest`.

## Опции времени выполнения [[embedding-options]]

Файл https://github.com/spring-projects/spring-ai/blob/main/models/spring-ai-minimax/src/main/java/org/springframework/ai/minimax/MiniMaxEmbeddingOptions.java[MiniMaxEmbeddingOptions.java] предоставляет конфигурации MiniMax, такие как модель для использования и т.д.

Параметры по умолчанию также могут быть настроены с помощью свойств `spring.ai.minimax.embedding.options`.

При старте используйте конструктор `MiniMaxEmbeddingModel`, чтобы установить параметры по умолчанию, используемые для всех запросов на встраивание.
Во время выполнения вы можете переопределить параметры по умолчанию, используя экземпляр `MiniMaxEmbeddingOptions` как часть вашего `EmbeddingRequest`.

Например, чтобы переопределить имя модели по умолчанию для конкретного запроса:

```java
EmbeddingResponse embeddingResponse = embeddingModel.call(
    new EmbeddingRequest(List.of("Hello World", "World is big and salvation is near"),
        MiniMaxEmbeddingOptions.builder()
            .model("Different-Embedding-Model-Deployment-Name")
        .build()));
```

## Пример контроллера

Это создаст реализацию `EmbeddingModel`, которую вы можете внедрить в ваш класс.
Вот пример простого класса `@Controller`, который использует реализацию `EmbeddingC`.

```application.properties
spring.ai.minimax.api-key=YOUR_API_KEY
spring.ai.minimax.embedding.options.model=embo-01
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

Если вы не используете Spring Boot, вы можете вручную настроить модель встраивания MiniMax.
Для этого добавьте зависимость `spring-ai-minimax` в файл `pom.xml` вашего проекта Maven:
```xml
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-minimax</artifactId>
</dependency>
```

или в файл сборки Gradle `build.gradle`.

```groovy
dependencies {
    implementation 'org.springframework.ai:spring-ai-minimax'
}
```

> **Совет:** Обратитесь к разделу xref:getting-started.adoc#dependency-management[Управление зависимостями], чтобы добавить BOM Spring AI в ваш файл сборки.

> **Примечание:** Зависимость `spring-ai-minimax` также предоставляет доступ к `MiniMaxChatModel`.
Для получения дополнительной информации о `MiniMaxChatModel` обратитесь к разделу [MiniMax Chat Client](../chat/minimax-chat.html).

Далее создайте экземпляр `MiniMaxEmbeddingModel` и используйте его для вычисления сходства между двумя входными текстами:

```java
var miniMaxApi = new MiniMaxApi(System.getenv("MINIMAX_API_KEY"));

var embeddingModel = new MiniMaxEmbeddingModel(minimaxApi, MetadataMode.EMBED,
MiniMaxEmbeddingOptions.builder().model("embo-01").build());

EmbeddingResponse embeddingResponse = this.embeddingModel
	.embedForResponse(List.of("Hello World", "World is big and salvation is near"));
```

Класс `MiniMaxEmbeddingOptions` предоставляет информацию о конфигурации для запросов на встраивание.
Класс опций предлагает метод `builder()` для удобного создания опций.
