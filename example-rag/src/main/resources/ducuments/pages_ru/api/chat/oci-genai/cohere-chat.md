# OCI GenAI Cohere Chat

https://www.oracle.com/artificial-intelligence/generative-ai/generative-ai-service/[OCI GenAI Service] предлагает генеративный AI-чат с моделями по запросу или выделенными AI-кластерами.

Страница https://docs.oracle.com/en-us/iaas/Content/generative-ai/chat-models.htm[OCI Chat Models Page] и https://docs.oracle.com/en-us/iaas/Content/generative-ai/use-playground-embed.htm[OCI Generative AI Playground] предоставляют подробную информацию о использовании и размещении чат-моделей на OCI.

## Предварительные требования

Вам потребуется активная учетная запись https://signup.oraclecloud.com/[Oracle Cloud Infrastructure (OCI)], чтобы использовать клиент OCI GenAI Cohere Chat. Клиент предлагает четыре различных способа подключения, включая простую аутентификацию с пользователем и закрытым ключом, идентификацию рабочей нагрузки, идентификацию экземпляра или аутентификацию с помощью конфигурационного файла OCI.

### Добавление репозиториев и BOM

Артефакты Spring AI публикуются в репозиториях Maven Central и Spring Snapshot.
Обратитесь к разделу xref:getting-started.adoc#artifact-repositories[Artifact Repositories], чтобы добавить эти репозитории в вашу систему сборки.

Чтобы помочь с управлением зависимостями, Spring AI предоставляет BOM (bill of materials), чтобы гарантировать, что одна и та же версия Spring AI используется на протяжении всего проекта. Обратитесь к разделу xref:getting-started.adoc#dependency-management[Dependency Management], чтобы добавить BOM Spring AI в вашу систему сборки.

## Автоконфигурация

[NOTE]
====
В автоконфигурации Spring AI произошли значительные изменения в названиях артефактов стартовых модулей.
Пожалуйста, обратитесь к https://docs.spring.io/spring-ai/reference/upgrade-notes.html[upgrade notes] для получения дополнительной информации.
====
Spring AI предоставляет автоконфигурацию Spring Boot для клиента OCI GenAI Cohere Chat.
Чтобы включить ее, добавьте следующую зависимость в файл `pom.xml` вашего проекта Maven:

```xml
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-starter-model-oci-genai</artifactId>
</dependency>
```

или в файл сборки Gradle `build.gradle`.

```groovy
dependencies {
    implementation 'org.springframework.ai:spring-ai-starter-model-oci-genai'
}
```

> **Совет:** Обратитесь к разделу xref:getting-started.adoc#dependency-management[Dependency Management], чтобы добавить BOM Spring AI в ваш файл сборки.

### Свойства чата

#### Свойства подключения

Префикс `spring.ai.oci.genai` — это префикс свойств для настройки подключения к OCI GenAI.

[cols="3,5,1", stripes=even]
|====
| Свойство | Описание | По умолчанию

| spring.ai.oci.genai.authenticationType |  Тип аутентификации, используемый при аутентификации в OCI. Может быть `file`, `instance-principal`, `workload-identity` или `simple`.  | file
| spring.ai.oci.genai.region | Регион службы OCI. | us-chicago-1
| spring.ai.oci.genai.tenantId | OCID арендатора OCI, используемый при аутентификации с помощью `simple` auth. | -
| spring.ai.oci.genai.userId | OCID пользователя OCI, используемый при аутентификации с помощью `simple` auth. | -
| spring.ai.oci.genai.fingerprint | Отпечаток закрытого ключа, используемый при аутентификации с помощью `simple` auth. | -
| spring.ai.oci.genai.privateKey | Содержимое закрытого ключа, используемое при аутентификации с помощью `simple` auth. | -
| spring.ai.oci.genai.passPhrase | Необязательная фраза-пароль для закрытого ключа, используемая при аутентификации с помощью `simple` auth и защищенного паролем закрытого ключа. | -
| spring.ai.oci.genai.file | Путь к конфигурационному файлу OCI. Используется при аутентификации с помощью `file` auth. | <user's home directory>/.oci/config
| spring.ai.oci.genai.profile | Имя профиля OCI. Используется при аутентификации с помощью `file` auth. | DEFAULT
| spring.ai.oci.genai.endpoint | Необязательный конечный пункт OCI GenAI. | -

|====


#### Свойства конфигурации

[NOTE]
====
Включение и отключение автоконфигураций чата теперь настраивается через свойства верхнего уровня с префиксом `spring.ai.model.chat`.
Чтобы включить, используйте spring.ai.model.chat=oci-genai (по умолчанию включено)
Чтобы отключить, используйте spring.ai.model.chat=none (или любое значение, которое не соответствует oci-genai)
Это изменение сделано для возможности конфигурации нескольких моделей.
====

Префикс `spring.ai.oci.genai.cohere.chat` — это префикс свойств, который настраивает реализацию `ChatModel` для OCI GenAI Cohere Chat.

[cols="3,5,1", stripes=even]
|====
| Свойство | Описание | По умолчанию

| spring.ai.model.chat | Включить модель чата OCI GenAI Cohere.  | oci-genai
| spring.ai.oci.genai.cohere.chat.enabled (больше не действительно) | Включить модель чата OCI GenAI Cohere.  | true
| spring.ai.oci.genai.cohere.chat.options.model | OCID модели или конечный пункт | -
| spring.ai.oci.genai.cohere.chat.options.compartment | OCID раздела модели.  | -
| spring.ai.oci.genai.cohere.chat.options.servingMode | Режим обслуживания модели, который будет использоваться. Может быть `on-demand` или `dedicated`.  | on-demand
| spring.ai.oci.genai.cohere.chat.options.preambleOverride | Переопределить предисловие подсказки модели чата | -
| spring.ai.oci.genai.cohere.chat.options.temperature | Температура вывода | -
| spring.ai.oci.genai.cohere.chat.options.topP | Параметр Top P | -
| spring.ai.oci.genai.cohere.chat.options.topK | Параметр Top K | -
| spring.ai.oci.genai.cohere.chat.options.frequencyPenalty | Более высокие значения уменьшат повторяющиеся токены, и вывод будет более случайным. | -
| spring.ai.oci.genai.cohere.chat.options.presencePenalty | Более высокие значения способствуют генерации выводов с токенами, которые не использовались. | -
| spring.ai.oci.genai.cohere.chat.options.stop | Список текстовых последовательностей, которые завершат генерацию завершений. | -
| spring.ai.oci.genai.cohere.chat.options.documents | Список документов, используемых в контексте чата. | -
|====

> **Совет:** Все свойства с префиксом `spring.ai.oci.genai.cohere.chat.options` могут быть переопределены во время выполнения, добавив специфичные для запроса <<chat-options>> в вызов `Prompt`.

## Опции времени выполнения [[chat-options]]

[OCICohereChatOptions.java](https://github.com/spring-projects/spring-ai/blob/main/models/spring-ai-oci-genai/src/main/java/org/springframework/ai/oci/cohere/OCICohereChatOptions.java) предоставляет конфигурации модели, такие как используемая модель, температура, штраф за частоту и т. д.

При запуске параметры по умолчанию могут быть настроены с помощью конструктора `OCICohereChatModel(api, options)` или свойств `spring.ai.oci.genai.cohere.chat.options.*`.

Во время выполнения вы можете переопределить параметры по умолчанию, добавив новые, специфичные для запроса, параметры в вызов `Prompt`.
Например, чтобы переопределить модель и температуру по умолчанию для конкретного запроса:

```java
ChatResponse response = chatModel.call(
    new Prompt(
        "Сгенерируйте имена 5 известных пиратов.",
        OCICohereChatOptions.builder()
            .model("my-model-ocid")
            .compartment("my-compartment-ocid")
            .temperature(0.5)
        .build()
    ));
```

## Пример контроллера

https://start.spring.io/[Создайте] новый проект Spring Boot и добавьте `spring-ai-starter-model-oci-genai` в зависимости вашего pom (или gradle).

Добавьте файл `application.properties` в директорию `src/main/resources`, чтобы включить и настроить модель чата OCI GenAI Cohere:

```application.properties
spring.ai.oci.genai.authenticationType=file
spring.ai.oci.genai.file=/path/to/oci/config/file
spring.ai.oci.genai.cohere.chat.options.compartment=my-compartment-ocid
spring.ai.oci.genai.cohere.chat.options.servingMode=on-demand
spring.ai.oci.genai.cohere.chat.options.model=my-chat-model-ocid
```

> **Совет:** замените `file`, `compartment` и `model` на ваши значения из вашей учетной записи OCI.

Это создаст реализацию `OCICohereChatModel`, которую вы можете внедрить в ваш класс.
Вот пример простого класса `@Controller`, который использует модель чата для генерации текста.

```java
@RestController
public class ChatController {

    private final OCICohereChatModel chatModel;

    @Autowired
    public ChatController(OCICohereChatModel chatModel) {
        this.chatModel = chatModel;
    }

    @GetMapping("/ai/generate")
    public Map generate(@RequestParam(value = "message", defaultValue = "Расскажи мне шутку") String message) {
        return Map.of("generation", chatModel.call(message));
    }

    @GetMapping("/ai/generateStream")
	public Flux<ChatResponse> generateStream(@RequestParam(value = "message", defaultValue = "Расскажи мне шутку") String message) {
        var prompt = new Prompt(new UserMessage(message));
        return chatModel.stream(prompt);
    }
}
```

## Ручная конфигурация
[OCICohereChatModel](https://github.com/spring-projects/spring-ai/blob/main/models/spring-ai-oci-genai/src/main/java/org/springframework/ai/oci/cohere/OCICohereChatModel.java) реализует `ChatModel` и использует OCI Java SDK для подключения к службе OCI GenAI.

Добавьте зависимость `spring-ai-oci-genai` в файл `pom.xml` вашего проекта Maven:

```xml
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-oci-genai</artifactId>
</dependency>
```

или в файл сборки Gradle `build.gradle`.

```groovy
dependencies {
    implementation 'org.springframework.ai:spring-ai-oci-genai'
}
```

> **Совет:** Обратитесь к разделу xref:getting-started.adoc#dependency-management[Dependency Management], чтобы добавить BOM Spring AI в ваш файл сборки.

Затем создайте `OCICohereChatModel` и используйте его для генерации текста:

```java
var CONFIG_FILE = Paths.get(System.getProperty("user.home"), ".oci", "config").toString();
var COMPARTMENT_ID = System.getenv("OCI_COMPARTMENT_ID");
var MODEL_ID = System.getenv("OCI_CHAT_MODEL_ID");

ConfigFileAuthenticationDetailsProvider authProvider = new ConfigFileAuthenticationDetailsProvider(
        CONFIG_FILE,
        "DEFAULT"
);
var genAi = GenerativeAiInferenceClient.builder()
        .region(Region.valueOf("us-chicago-1"))
        .build(authProvider);

var chatModel = new OCICohereChatModel(genAi, OCICohereChatOptions.builder()
        .model(MODEL_ID)
        .compartment(COMPARTMENT_ID)
        .servingMode("on-demand")
        .build());

ChatResponse response = chatModel.call(
        new Prompt("Сгенерируйте имена 5 известных пиратов."));
```

`OCICohereChatOptions` предоставляет информацию о конфигурации для запросов чата.
`OCICohereChatOptions.Builder` — это удобный строитель параметров.
