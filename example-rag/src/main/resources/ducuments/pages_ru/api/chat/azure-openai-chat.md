# Azure OpenAI Chat

Предложение Azure OpenAI, основанное на ChatGPT, выходит за рамки традиционных возможностей OpenAI, предоставляя генерацию текста на основе ИИ с расширенной функциональностью. Azure предлагает дополнительные функции безопасности ИИ и ответственного ИИ, как указано в их недавнем обновлении https://techcommunity.microsoft.com/t5/ai-azure-ai-services-blog/announcing-new-ai-safety-amp-responsible-ai-features-in-azure/ba-p/3983686[здесь].

Azure предоставляет разработчикам Java возможность использовать весь потенциал ИИ, интегрируя его с рядом сервисов Azure, включая ресурсы, связанные с ИИ, такие как Vector Stores на Azure.

## Предварительные требования

Клиент Azure OpenAI предлагает три варианта подключения: с использованием ключа API Azure, ключа API OpenAI или Microsoft Entra ID.

### Ключ API Azure и конечная точка

Чтобы получить доступ к моделям с использованием ключа API, получите свой `endpoint` и `api-key` Azure OpenAI из раздела Azure OpenAI Service на https://portal.azure.com[Портал Azure].

Spring AI определяет два свойства конфигурации:

1. `spring.ai.azure.openai.api-key`: Установите это значение равным `API Key`, полученному от Azure.
2. `spring.ai.azure.openai.endpoint`: Установите это значение равным URL конечной точки, полученному при развертывании вашей модели в Azure.

Вы можете установить эти свойства конфигурации в вашем файле `application.properties` или `application.yml`:

```properties
spring.ai.azure.openai.api-key=<your-azure-api-key>
spring.ai.azure.openai.endpoint=<your-azure-endpoint-url>
```

Для повышения безопасности при работе с конфиденциальной информацией, такой как ключи API, вы можете использовать язык выражений Spring (SpEL) для ссылки на пользовательские переменные окружения:

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
export AZURE_OPENAI_ENDPOINT=<your-azure-openai-endpoint-url>
```

### Ключ OpenAI

Чтобы аутентифицироваться с сервисом OpenAI (не Azure), предоставьте ключ API OpenAI. Это автоматически установит конечную точку на https://api.openai.com/v1.

При использовании этого подхода установите свойство `spring.ai.azure.openai.chat.options.deployment-name` на имя модели https://platform.openai.com/docs/models[OpenAI], которую вы хотите использовать.

В вашей конфигурации приложения:

```properties
spring.ai.azure.openai.openai-api-key=<your-azure-openai-key>
spring.ai.azure.openai.chat.options.deployment-name=<openai-model-name>
```

Используя переменные окружения с SpEL:

```yaml
# В application.yml
spring:
  ai:
    azure:
      openai:
        openai-api-key: ${AZURE_OPENAI_API_KEY}
        chat:
          options:
            deployment-name: ${AZURE_OPENAI_MODEL_NAME}
```

```bash
# В вашем окружении или .env файле
export AZURE_OPENAI_API_KEY=<your-openai-key>
export AZURE_OPENAI_MODEL_NAME=<openai-model-name>
```

### Microsoft Entra ID

Для аутентификации без ключа с использованием Microsoft Entra ID (ранее Azure Active Directory) установите _только_ свойство конфигурации `spring.ai.azure.openai.endpoint` и _не_ свойство api-key, упомянутое выше.

Найдя только свойство конечной точки, ваше приложение оценит несколько различных вариантов для получения учетных данных, и экземпляр `OpenAIClient` будет создан с использованием токенов учетных данных.

> **Примечание:** Больше не нужно создавать бин `TokenCredential`; он настраивается автоматически.

### Имя развертывания

Чтобы использовать приложения Azure AI, вам необходимо создать развертывание Azure AI через [Портал Azure AI](https://oai.azure.com/portal).
В Azure каждый клиент должен указать `Deployment Name`, чтобы подключиться к сервису Azure OpenAI.
Важно отметить, что `Deployment Name` отличается от модели, которую вы выбираете для развертывания.
Например, развертывание с именем 'MyAiDeployment' может быть настроено для использования как модели GPT 3.5 Turbo, так и модели GPT 4.0.

Чтобы начать, выполните следующие шаги для создания развертывания с настройками по умолчанию:

   Имя развертывания: `gpt-4o`
   Имя модели: `gpt-4o`

Эта конфигурация Azure соответствует конфигурациям по умолчанию стартеров Spring Boot Azure AI и его функции автоконфигурации.
Если вы используете другое имя развертывания, убедитесь, что вы обновили соответствующее свойство конфигурации:

```
spring.ai.azure.openai.chat.options.deployment-name=<my deployment name>
```

Разные структуры развертывания Azure OpenAI и OpenAI приводят к свойству в библиотеке клиента Azure OpenAI с именем `deploymentOrModelName`.
Это связано с тем, что в OpenAI нет `Deployment Name`, только `Model Name`.

> **Примечание:** Свойство `spring.ai.azure.openai.chat.options.model` было переименовано в `spring.ai.azure.openai.chat.options.deployment-name`.

> **Примечание:** Если вы решите подключиться к `OpenAI` вместо `Azure OpenAI`, установив свойство `spring.ai.azure.openai.openai-api-key=<Your OpenAI Key>`,
то `spring.ai.azure.openai.chat.options.deployment-name` будет рассматриваться как имя [модели OpenAI](https://platform.openai.com/docs/models).

#### Доступ к модели OpenAI

Вы можете настроить клиент для использования непосредственно `OpenAI` вместо развернутых моделей `Azure OpenAI`.
Для этого вам нужно установить `spring.ai.azure.openai.openai-api-key=<Your OpenAI Key>` вместо `spring.ai.azure.openai.api-key=<Your Azure OpenAi Key>`.

### Добавление репозиториев и BOM

Артефакты Spring AI публикуются в репозиториях Maven Central и Spring Snapshot.
Обратитесь к разделу xref:getting-started.adoc#artifact-repositories[Артефактные репозитории], чтобы добавить эти репозитории в вашу систему сборки.

Чтобы помочь с управлением зависимостями, Spring AI предоставляет BOM (bill of materials), чтобы гарантировать, что одна и та же версия Spring AI используется на протяжении всего проекта. Обратитесь к разделу xref:getting-started.adoc#dependency-management[Управление зависимостями], чтобы добавить BOM Spring AI в вашу систему сборки.

## Автоконфигурация

[ПРИМЕЧАНИЕ]
====
В автоконфигурации Spring AI произошли значительные изменения в названиях артефактов модулей стартеров.
Пожалуйста, обратитесь к https://docs.spring.io/spring-ai/reference/upgrade-notes.html[заметкам об обновлении] для получения дополнительной информации.
====

Spring AI предоставляет автоконфигурацию Spring Boot для клиента Azure OpenAI Chat.
Чтобы включить ее, добавьте следующую зависимость в файл сборки Maven `pom.xml` или Gradle `build.gradle` вашего проекта:

[tabs]
======
Maven::
+
```xml
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-starter-model-azure-openai</artifactId>
</dependency>
```

Gradle::
+
```groovy
dependencies {
    implementation 'org.springframework.ai:spring-ai-starter-model-azure-openai'
}
```
======

> **Совет:** Обратитесь к разделу xref:getting-started.adoc#dependency-management[Управление зависимостями], чтобы добавить BOM Spring AI в ваш файл сборки.

Клиент Azure OpenAI Chat создается с использованием [OpenAIClientBuilder](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/openai/azure-ai-openai/src/main/java/com/azure/ai/openai/OpenAIClientBuilder.java), предоставленного Azure SDK. Spring AI позволяет настраивать сборщик, предоставляя бины [AzureOpenAIClientBuilderCustomizer](https://github.com/spring-projects/spring-ai/blob/main/auto-configurations/models/spring-ai-autoconfigure-model-azure-openai/src/main/java/org/springframework/ai/model/azure/openai/autoconfigure/AzureOpenAIClientBuilderCustomizer.java).

Настройщик может использоваться, например, для изменения времени ожидания ответа по умолчанию:

```java
@Configuration
public class AzureOpenAiConfig {

	@Bean
	public AzureOpenAIClientBuilderCustomizer responseTimeoutCustomizer() {
		return openAiClientBuilder -> {
			HttpClientOptions clientOptions = new HttpClientOptions()
					.setResponseTimeout(Duration.ofMinutes(5));
			openAiClientBuilder.httpClient(HttpClient.createDefault(clientOptions));
		};
	}

}
```

### Свойства чата

Префикс `spring.ai.azure.openai` — это префикс свойства для настройки подключения к Azure OpenAI.

[cols="3,5,1", stripes=even]
|====
| Свойство | Описание | По умолчанию

| spring.ai.azure.openai.api-key |  Ключ из раздела Azure AI OpenAI `Keys and Endpoint` в разделе `Resource Management`  | -
| spring.ai.azure.openai.endpoint | Конечная точка из раздела Azure AI OpenAI `Keys and Endpoint` в разделе `Resource Management` | -
| spring.ai.azure.openai.openai-api-key |  (не Azure) Ключ API OpenAI. Используется для аутентификации с сервисом OpenAI, вместо Azure OpenAI.
Это автоматически устанавливает конечную точку на https://api.openai.com/v1. Используйте либо свойство `api-key`, либо `openai-api-key`.
С этой конфигурацией `spring.ai.azure.openai.chat.options.deployment-name` рассматривается как имя https://platform.openai.com/docs/models[модели OpenAi].| -
| spring.ai.azure.openai.custom-headers | Карта пользовательских заголовков, которые будут включены в API-запросы. Каждая запись в карте представляет заголовок, где ключ — это имя заголовка, а значение — значение заголовка. | Пустая карта
|====

[ПРИМЕЧАНИЕ]
====
Включение и отключение автоконфигураций чата теперь настраивается через свойства верхнего уровня с префиксом `spring.ai.model.chat`.

Чтобы включить, используйте spring.ai.model.chat=azure-openai (по умолчанию включено)

Чтобы отключить, используйте spring.ai.model.chat=none (или любое значение, которое не соответствует azure-openai)

Это изменение сделано для того, чтобы позволить конфигурацию нескольких моделей.
====

Префикс `spring.ai.azure.openai.chat` — это префикс свойства, который настраивает реализацию `ChatModel` для Azure OpenAI.

[cols="3,5,1", stripes=even]
|====
| Свойство | Описание | По умолчанию

| spring.ai.azure.openai.chat.enabled (Удалено и больше не действительно) | Включить модель чата Azure OpenAI.  | true
| spring.ai.model.chat | Включить модель чата Azure OpenAI.  | azure-openai
| spring.ai.azure.openai.chat.options.deployment-name | В использовании с Azure это относится к "Deployment Name" вашей модели, которую вы можете найти на https://oai.azure.com/portal.
Важно отметить, что в развертывании Azure OpenAI "Deployment Name" отличается от самой модели.
Путаница вокруг этих терминов возникает из-за намерения сделать библиотеку клиента Azure OpenAI совместимой с оригинальной конечной точкой OpenAI.
Структуры развертывания, предлагаемые Azure OpenAI и OpenAI Сэма Альтмана, значительно различаются.
Имя модели развертывания, которое необходимо предоставить в этом запросе на завершение. | gpt-4o
| spring.ai.azure.openai.chat.options.maxTokens | Максимальное количество токенов, которые могут быть сгенерированы в завершении чата. Общая длина входных токенов и сгенерированных токенов ограничена длиной контекста модели. **Используйте для моделей без рассуждений (например, gpt-4o, gpt-3.5-turbo). Нельзя использовать с maxCompletionTokens.** | -
| spring.ai.azure.openai.chat.options.maxCompletionTokens | Верхний предел для количества токенов, которые могут быть сгенерированы для завершения, включая видимые выходные токены и токены рассуждений. **Обязательно для моделей рассуждений (например, o1, o3, o4-mini series). Нельзя использовать с maxTokens.** | -
| spring.ai.azure.openai.chat.options.temperature | Температура выборки, которая контролирует очевидную креативность сгенерированных завершений. Более высокие значения сделают вывод более случайным, в то время как более низкие значения сделают результаты более сосредоточенными и детерминированными. Не рекомендуется изменять температуру и top_p для одного и того же запроса на завершение, так как взаимодействие этих двух настроек трудно предсказать. | -
| spring.ai.azure.openai.chat.options.topP | Альтернатива выборке с температурой, называемая ядерной выборкой. Это значение заставляет модель учитывать результаты токенов с предоставленной вероятностной массой. | -
| spring.ai.azure.openai.chat.options.logitBias | Карта между идентификаторами токенов GPT и оценками смещения, которые влияют на вероятность появления конкретных токенов в ответе на завершение. Идентификаторы токенов вычисляются с помощью внешних инструментов токенизации, в то время как оценки смещения находятся в диапазоне от -100 до 100, при этом минимальные и максимальные значения соответствуют полному запрету или исключительному выбору токена соответственно. Точное поведение данного значения смещения варьируется в зависимости от модели. | -
| spring.ai.azure.openai.chat.options.user | Идентификатор вызывающего или конечного пользователя операции. Это может использоваться для отслеживания или ограничения частоты. | -
| spring.ai.azure.openai.chat.options.stream-usage | (Только для потоковой передачи) Установите, чтобы добавить дополнительный фрагмент с статистикой использования токенов для всего запроса. Поле `choices` для этого фрагмента является пустым массивом, и все другие фрагменты также будут включать поле использования, но с нулевым значением. | false
| spring.ai.azure.openai.chat.options.n | Количество вариантов завершений чата, которые должны быть сгенерированы для ответа на завершение чата. | -
| spring.ai.azure.openai.chat.options.stop | Коллекция текстовых последовательностей, которые завершат генерацию завершений. | -
| spring.ai.azure.openai.chat.options.presencePenalty |  Значение, которое влияет на вероятность появления сгенерированных токенов на основе их существующего присутствия в сгенерированном тексте. Положительные значения сделают токены менее вероятными для появления, когда они уже существуют, и увеличат вероятность того, что модель выдаст новые темы. | -
| spring.ai.azure.openai.chat.options.responseFormat.type | Совместимо с `GPT-4o`, `GPT-4o mini`, `GPT-4 Turbo` и всеми моделями `GPT-3.5 Turbo`, новее `gpt-3.5-turbo-1106`.
Тип `JSON_OBJECT` включает режим JSON, который гарантирует, что сообщение, сгенерированное моделью, является допустимым JSON.
Тип `JSON_SCHEMA` включает структурированные выходные данные, которые гарантируют, что модель будет соответствовать предоставленной вами JSON-схеме. Тип `JSON_SCHEMA` также требует установки свойства `responseFormat.schema`. | -
| spring.ai.azure.openai.chat.options.responseFormat.schema | Схема JSON формата ответа. Применимо только для `responseFormat.type=JSON_SCHEMA` | -
| spring.ai.azure.openai.chat.options.frequencyPenalty | Значение, которое влияет на вероятность появления сгенерированных токенов на основе их кумулятивной частоты в сгенерированном тексте. Положительные значения сделают токены менее вероятными для появления по мере увеличения их частоты и уменьшат вероятность того, что модель будет повторять одни и те же утверждения дословно. | -
| spring.ai.azure.openai.chat.options.tool-names | Список инструментов, идентифицированных по их именам, которые будут включены для вызова функций в одном запросе. Инструменты с этими именами должны существовать в реестре ToolCallback. | -
| spring.ai.azure.openai.chat.options.tool-callbacks | Обратные вызовы инструментов для регистрации с ChatModel. | -
| spring.ai.azure.openai.chat.options.internal-tool-execution-enabled | Если false, Spring AI не будет обрабатывать вызовы инструментов внутренне, а будет проксировать их клиенту. Тогда клиент будет нести ответственность за обработку вызовов инструментов, их распределение на соответствующую функцию и возврат результатов. Если true (по умолчанию), Spring AI будет обрабатывать вызовы функций внутренне. Применимо только для моделей чата с поддержкой вызова функций | true
|====

> **Совет:** Все свойства с префиксом `spring.ai.azure.openai.chat.options` могут быть переопределены во время выполнения, добавив специфичные для запроса <<chat-options>> в вызов `Prompt`.

### Параметры ограничения токенов: использование, специфичное для модели

Azure OpenAI имеет специфические для модели требования к параметрам ограничения токенов:

[cols="1,1,2", options="header"]
|====
| Семейство моделей | Обязательный параметр | Примечания

| **Модели рассуждений** +
(o1, o3, o4-mini series)
| `maxCompletionTokens`
| Эти модели принимают только `maxCompletionTokens`. Использование `maxTokens` приведет к ошибке API.

| **Модели без рассуждений** +
(gpt-4o, gpt-3.5-turbo и т.д.)
| `maxTokens`
| Традиционные модели используют `maxTokens` для ограничения вывода. Использование `maxCompletionTokens` может привести к ошибке API.
|====

> **Важно:** Параметры `maxTokens` и `maxCompletionTokens` являются **взаимоисключающими**. Установка обоих параметров одновременно приведет к ошибке API от Azure OpenAI. Клиент Spring AI Azure OpenAI автоматически очистит ранее установленный параметр, когда вы установите другой, с предупреждающим сообщением.

.Пример: Использование maxCompletionTokens для моделей рассуждений
```java
var options = AzureOpenAiChatOptions.builder()
    .deploymentName("o1-preview")
    .maxCompletionTokens(500)  // Обязательно для моделей рассуждений
    .build();
```

.Пример: Использование maxTokens для моделей без рассуждений
```java
var options = AzureOpenAiChatOptions.builder()
    .deploymentName("gpt-4o")
    .maxTokens(500)  // Обязательно для моделей без рассуждений
    .build();
```

## Опции времени выполнения [[chat-options]]

[AzureOpenAiChatOptions.java](https://github.com/spring-projects/spring-ai/blob/main/models/spring-ai-azure-openai/src/main/java/org/springframework/ai/azure/openai/AzureOpenAiChatOptions.java) предоставляет конфигурации модели, такие как используемая модель, температура, штраф за частоту и т.д.

При запуске параметры по умолчанию могут быть настроены с помощью конструктора `AzureOpenAiChatModel(api, options)` или свойств `spring.ai.azure.openai.chat.options.*`.

Во время выполнения вы можете переопределить параметры по умолчанию, добавив новые, специфичные для запроса, параметры в вызов `Prompt`.
Например, чтобы переопределить модель и температуру по умолчанию для конкретного запроса:

```java
ChatResponse response = chatModel.call(
    new Prompt(
        "Сгенерируйте имена 5 известных пиратов.",
        AzureOpenAiChatOptions.builder()
            .deploymentName("gpt-4o")
            .temperature(0.4)
        .build()
    ));
```

> **Совет:** В дополнение к специфичным для модели [AzureOpenAiChatOptions.java](https://github.com/spring-projects/spring-ai/blob/main/models/spring-ai-azure-openai/src/main/java/org/springframework/ai/azure/openai/AzureOpenAiChatOptions.java) вы можете использовать переносимый экземпляр [ChatOptions](https://github.com/spring-projects/spring-ai/blob/main/spring-ai-model/src/main/java/org/springframework/ai/chat/prompt/ChatOptions.java), созданный с помощью [ChatOptions#builder()](https://github.com/spring-projects/spring-ai/blob/main/spring-ai-model/src/main/java/org/springframework/ai/chat/prompt/DefaultChatOptionsBuilder.java).

## Вызов функций

Вы можете зарегистрировать пользовательские функции Java с AzureOpenAiChatModel и позволить модели интеллектуально выбирать выводить JSON-объект, содержащий аргументы для вызова одной или нескольких зарегистрированных функций.
Это мощная техника для соединения возможностей LLM с внешними инструментами и API.
Читать далее о xref:api/tools.adoc[Вызов инструментов].

## Мультимодальность

Мультимодальность относится к способности модели одновременно понимать и обрабатывать информацию из различных источников, включая текст, изображения, аудио и другие форматы данных.
В настоящее время модель Azure OpenAI `gpt-4o` предлагает поддержку мультимодальности.

Azure OpenAI может включать список изображений, закодированных в base64, или URL-адресов изображений с сообщением.
Интерфейс Spring AI [Message](https://github.com/spring-projects/spring-ai/blob/main/spring-ai-model/src/main/java/org/springframework/ai/chat/messages/Message.java) облегчает мультимодальные ИИ-модели, вводя тип [Media](https://github.com/spring-projects/spring-ai/blob/main/spring-ai-commons/src/main/java/org/springframework/ai/content/Media.java).
Этот тип охватывает данные и детали о медиа-вложениях в сообщениях, используя `org.springframework.util.MimeType` и `java.lang.Object` для необработанных медиа-данных.

Ниже приведен пример кода, взятый из [OpenAiChatModelIT.java](https://github.com/spring-projects/spring-ai/blob/c9a3e66f90187ce7eae7eb78c462ec622685de6c/models/spring-ai-openai/src/test/java/org/springframework/ai/openai/chat/OpenAiChatModelIT.java#L293), иллюстрирующий слияние текста пользователя с изображением, используя модель `GPT_4_O`.

```java
URL url = new URL("https://docs.spring.io/spring-ai/reference/_images/multimodal.test.png");
String response = ChatClient.create(chatModel).prompt()
        .options(AzureOpenAiChatOptions.builder().deploymentName("gpt-4o").build())
        .user(u -> u.text("Объясните, что вы видите на этой картинке?").media(MimeTypeUtils.IMAGE_PNG, this.url))
        .call()
        .content();
```

> **Совет:** вы также можете передавать несколько изображений.

Он принимает в качестве входных данных изображение `multimodal.test.png`:

![Мультимодальное тестовое изображение, 200, 200, align="left"](multimodal.test.png)

вместе с текстовым сообщением "Объясните, что вы видите на этой картинке?", и генерирует ответ, подобный этому:

```
Это изображение фруктовой миски с простым дизайном. Миска сделана из металла с изогнутыми проволочными краями, которые
создают открытую структуру, позволяя фруктам быть видимыми со всех сторон. Внутри миски находятся два
желтых банана, лежащих на красном яблоке. Бананы слегка перезрелые, о чем свидетельствуют коричневые пятна на их кожуре. Миска имеет металлическое кольцо вверху, вероятно, для использования в качестве ручки
для переноски. Миска стоит на ровной поверхности с нейтральным фоном, который обеспечивает четкий
вид на фрукты внутри.
```

Вы также можете передать ресурс classpath вместо URL, как показано в следующем примере

```java
Resource resource = new ClassPathResource("multimodality/multimodal.test.png");

String response = ChatClient.create(chatModel).prompt()
    .options(AzureOpenAiChatOptions.builder()
    .deploymentName("gpt-4o").build())
    .user(u -> u.text("Объясните, что вы видите на этой картинке?")
    .media(MimeTypeUtils.IMAGE_PNG, this.resource))
    .call()
    .content();
```

## Пример контроллера

https://start.spring.io/[Создайте] новый проект Spring Boot и добавьте `spring-ai-starter-model-azure-openai` в зависимости вашего pom (или gradle).

Добавьте файл `application.properties` в директорию `src/main/resources`, чтобы включить и настроить модель чата OpenAi:

```application.properties
spring.ai.azure.openai.api-key=YOUR_API_KEY
spring.ai.azure.openai.endpoint=YOUR_ENDPOINT
spring.ai.azure.openai.chat.options.deployment-name=gpt-4o
spring.ai.azure.openai.chat.options.temperature=0.7
```

> **Совет:** замените `api-key` и `endpoint` на ваши учетные данные Azure OpenAI.

Это создаст реализацию `AzureOpenAiChatModel`, которую вы можете внедрить в свой класс.
Вот пример простого класса `@Controller`, который использует модель чата для генерации текста.

```java
@RestController
public class ChatController {

    private final AzureOpenAiChatModel chatModel;

    @Autowired
    public ChatController(AzureOpenAiChatModel chatModel) {
        this.chatModel = chatModel;
    }

    @GetMapping("/ai/generate")
    public Map generate(@RequestParam(value = "message", defaultValue = "Расскажи мне шутку") String message) {
        return Map.of("generation", this.chatModel.call(message));
    }

    @GetMapping("/ai/generateStream")
	public Flux<ChatResponse> generateStream(@RequestParam(value = "message", defaultValue = "Расскажи мне шутку") String message) {
        Prompt prompt = new Prompt(new UserMessage(message));
        return this.chatModel.stream(prompt);
    }
}
```

## Ручная конфигурация

[AzureOpenAiChatModel](https://github.com/spring-projects/spring-ai/blob/main/models/spring-ai-azure-openai/src/main/java/org/springframework/ai/azure/openai/AzureOpenAiChatModel.java) реализует `ChatModel` и `StreamingChatModel` и использует [Azure OpenAI Java Client](https://learn.microsoft.com/en-us/java/api/overview/azure/ai-openai-readme?view=azure-java-preview).

Чтобы включить его, добавьте зависимость `spring-ai-azure-openai` в файл Maven `pom.xml` вашего проекта:
```xml
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-azure-openai</artifactId>
</dependency>
```

или в файл сборки Gradle `build.gradle`.

```gradle
dependencies {
    implementation 'org.springframework.ai:spring-ai-azure-openai'
}
```

> **Совет:** Обратитесь к разделу xref:getting-started.adoc#dependency-management[Управление зависимостями], чтобы добавить BOM Spring AI в ваш файл сборки.

> **Совет:** Зависимость `spring-ai-azure-openai` также предоставляет доступ к `AzureOpenAiChatModel`. Для получения дополнительной информации о `AzureOpenAiChatModel` обратитесь к разделу [Azure OpenAI Chat](../chat/azure-openai-chat.html).

Далее создайте экземпляр `AzureOpenAiChatModel` и используйте его для генерации текстовых ответов:

```java
var openAIClientBuilder = new OpenAIClientBuilder()
  .credential(new AzureKeyCredential(System.getenv("AZURE_OPENAI_API_KEY")))
  .endpoint(System.getenv("AZURE_OPENAI_ENDPOINT"));

var openAIChatOptions = AzureOpenAiChatOptions.builder()
  .deploymentName("gpt-5")
  .temperature(0.4)
  .maxCompletionTokens(200)
  .build();

var chatModel = AzureOpenAiChatModel.builder()
				.openAIClientBuilder(openAIClientBuilder)
				.defaultOptions(openAIChatOptions)
				.build();

ChatResponse response = chatModel.call(
  new Prompt("Сгенерируйте имена 5 известных пиратов."));

// Или с потоковыми ответами
Flux<ChatResponse> streamingResponses = chatModel.stream(
  new Prompt("Сгенерируйте имена 5 известных пиратов."));

```

> **Примечание:** `gpt-4o` на самом деле является `Deployment Name`, как представлено в Портале Azure AI.
