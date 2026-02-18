# Hugging Face Chat

Hugging Face Text Generation Inference (TGI) — это специализированное решение для развертывания, предназначенное для обслуживания больших языковых моделей (LLM) в облаке, что делает их доступными через API. TGI обеспечивает оптимизированную производительность для задач генерации текста благодаря таким функциям, как непрерывная пакетная обработка, потоковая передача токенов и эффективное управление памятью.

> **Важно:** Text Generation Inference требует, чтобы модели были совместимы с его архитектурно-специфическими оптимизациями. Хотя многие популярные LLM поддерживаются, не все модели на Hugging Face Hub могут быть развернуты с использованием TGI. Если вам нужно развернуть другие типы моделей, рассмотрите возможность использования стандартных конечных точек Hugging Face Inference.

> **Совет:** Для получения полного и актуального списка поддерживаемых моделей и архитектур смотрите [документацию по поддерживаемым моделям Text Generation Inference](https://huggingface.co/docs/text-generation-inference/en/supported_models).

## Предварительные требования

Вам необходимо создать конечную точку Inference на Hugging Face и создать токен API для доступа к конечной точке. Дополнительные сведения можно найти [здесь](https://huggingface.co/docs/inference-endpoints/index).

Проект Spring AI определяет два свойства конфигурации:

1. `spring.ai.huggingface.chat.api-key`: Установите это значение равным токену API, полученному от Hugging Face.
2. `spring.ai.huggingface.chat.url`: Установите это значение равным URL конечной точки inference, полученному при развертывании вашей модели в Hugging Face.

Вы можете найти URL вашей конечной точки inference в пользовательском интерфейсе конечной точки [здесь](https://ui.endpoints.huggingface.co/).

Вы можете установить эти свойства конфигурации в вашем файле `application.properties`:

```properties
spring.ai.huggingface.chat.api-key=<your-huggingface-api-key>
spring.ai.huggingface.chat.url=<your-inference-endpoint-url>
```

Для повышения безопасности при работе с конфиденциальной информацией, такой как ключи API, вы можете использовать язык выражений Spring (SpEL) для ссылки на пользовательские переменные окружения:

```yaml
# В application.yml
spring:
  ai:
    huggingface:
      chat:
        api-key: ${HUGGINGFACE_API_KEY}
        url: ${HUGGINGFACE_ENDPOINT_URL}
```

```bash
# В вашем окружении или .env файле
export HUGGINGFACE_API_KEY=<your-huggingface-api-key>
export HUGGINGFACE_ENDPOINT_URL=<your-inference-endpoint-url>
```

Вы также можете установить эти конфигурации программно в коде вашего приложения:

```java
// Получите ключ API и URL конечной точки из безопасных источников или переменных окружения
String apiKey = System.getenv("HUGGINGFACE_API_KEY");
String endpointUrl = System.getenv("HUGGINGFACE_ENDPOINT_URL");
```

### Добавление репозиториев и BOM

Артефакты Spring AI публикуются в репозиториях Maven Central и Spring Snapshot. Обратитесь к разделу xref:getting-started.adoc#artifact-repositories[Артефакты репозиториев], чтобы добавить эти репозитории в вашу систему сборки.

Чтобы помочь с управлением зависимостями, Spring AI предоставляет BOM (спецификация материалов), чтобы гарантировать, что одна и та же версия Spring AI используется на протяжении всего проекта. Обратитесь к разделу xref:getting-started.adoc#dependency-management[Управление зависимостями], чтобы добавить BOM Spring AI в вашу систему сборки.

## Автоконфигурация

[NOTE]
====
В автоконфигурации Spring AI произошли значительные изменения в названиях артефактов стартовых модулей. Пожалуйста, обратитесь к https://docs.spring.io/spring-ai/reference/upgrade-notes.html[заметкам об обновлении] для получения дополнительной информации.
====

Spring AI предоставляет автоконфигурацию Spring Boot для клиента Hugging Face Chat. Чтобы включить ее, добавьте следующую зависимость в файл `pom.xml` вашего проекта Maven:

```xml
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-starter-model-huggingface</artifactId>
</dependency>
```

или в файл сборки Gradle `build.gradle`.

```groovy
dependencies {
    implementation 'org.springframework.ai:spring-ai-starter-model-huggingface'
}
```

> **Совет:** Обратитесь к разделу xref:getting-started.adoc#dependency-management[Управление зависимостями], чтобы добавить BOM Spring AI в ваш файл сборки.

### Свойства чата

[NOTE]
====
Включение и отключение автоконфигураций чата теперь настраивается через свойства верхнего уровня с префиксом `spring.ai.model.chat`.

Чтобы включить, установите spring.ai.model.chat=huggingface (по умолчанию включено).

Чтобы отключить, установите spring.ai.model.chat=none (или любое значение, которое не соответствует huggingface).

Это изменение сделано для того, чтобы позволить конфигурацию нескольких моделей.
====

Префикс `spring.ai.huggingface` — это префикс свойства, который позволяет вам настраивать реализацию модели чата для Hugging Face.

[cols="3,5,1", stripes=even]
|====
| Свойство | Описание | По умолчанию
| spring.ai.huggingface.chat.api-key    | Ключ API для аутентификации с конечной точкой Inference.  |  -
| spring.ai.huggingface.chat.url        | URL конечной точки Inference для подключения           |  -
| spring.ai.huggingface.chat.enabled (Удалено и больше не актуально)   | Включить модель чата Hugging Face.                       | true
| spring.ai.model.chat                  | Включить модель чата Hugging Face.                       | huggingface
|====

## Пример контроллера (Автоконфигурация)

https://start.spring.io/[Создайте] новый проект Spring Boot и добавьте `spring-ai-starter-model-huggingface` в зависимости вашего pom (или gradle).

Добавьте файл `application.properties` в директорию `src/main/resources`, чтобы включить и настроить модель чата Hugging Face:

```application.properties
spring.ai.huggingface.chat.api-key=YOUR_API_KEY
spring.ai.huggingface.chat.url=YOUR_INFERENCE_ENDPOINT_URL
```

> **Совет:** замените `api-key` и `url` на ваши значения Hugging Face.

Это создаст реализацию `HuggingfaceChatModel`, которую вы можете внедрить в ваш класс. Вот пример простого класса `@Controller`, который использует модель чата для генерации текста.

```java
@RestController
public class ChatController {

    private final HuggingfaceChatModel chatModel;

    @Autowired
    public ChatController(HuggingfaceChatModel chatModel) {
        this.chatModel = chatModel;
    }

    @GetMapping("/ai/generate")
    public Map generate(@RequestParam(value = "message", defaultValue = "Расскажи мне шутку") String message) {
        return Map.of("generation", this.chatModel.call(message));
    }
}
```

## Ручная конфигурация

Модель [HuggingfaceChatModel](https://github.com/spring-projects/spring-ai/blob/main/models/spring-ai-huggingface/src/main/java/org/springframework/ai/huggingface/HuggingfaceChatModel.java) реализует интерфейс `ChatModel` и использует <<low-level-api>> для подключения к конечным точкам inference Hugging Face.

Добавьте зависимость `spring-ai-huggingface` в файл `pom.xml` вашего проекта Maven:

```xml
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-huggingface</artifactId>
</dependency>
```

или в файл сборки Gradle `build.gradle`.

```groovy
dependencies {
    implementation 'org.springframework.ai:spring-ai-huggingface'
}
```

> **Совет:** Обратитесь к разделу xref:getting-started.adoc#dependency-management[Управление зависимостями], чтобы добавить BOM Spring AI в ваш файл сборки.

Затем создайте `HuggingfaceChatModel` и используйте его для генерации текста:

```java
HuggingfaceChatModel chatModel = new HuggingfaceChatModel(apiKey, url);

ChatResponse response = this.chatModel.call(
    new Prompt("Сгенерируйте имена 5 известных пиратов."));

System.out.println(response.getResult().getOutput().getText());
```
