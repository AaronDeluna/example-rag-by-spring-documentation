# Bedrock Converse API

[Amazon Bedrock Converse API](https://docs.aws.amazon.com/bedrock/latest/userguide/conversation-inference.html) предоставляет унифицированный интерфейс для моделей разговорного ИИ с расширенными возможностями, включая вызов функций/инструментов, мультимодальные входы и потоковые ответы.

API Bedrock Converse имеет следующие основные функции:

- Вызов инструментов/функций: Поддержка определения функций и использования инструментов во время разговоров
- Мультимодальный ввод: Возможность обрабатывать как текстовые, так и изображенческие входы в разговорах
- Поддержка потоковой передачи: Потоковая передача ответов модели в реальном времени
- Системные сообщения: Поддержка системных инструкций и установки контекста

> **Совет:** API Bedrock Converse предоставляет унифицированный интерфейс для нескольких поставщиков моделей, одновременно решая вопросы аутентификации и инфраструктуры, специфичные для AWS. В настоящее время поддерживаемые модели Converse API включают:
`Amazon Titan`, `Amazon Nova`, `AI21 Labs`, `Anthropic Claude`, `Cohere Command`, `Meta Llama`, `Mistral AI`.

[NOTE]
====
Согласно рекомендациям Bedrock, Spring AI переходит на использование API Converse от Amazon Bedrock для всех реализаций разговоров в Spring AI. Хотя существующий xref:api/bedrock-chat.adoc[InvokeModel API] поддерживает приложения для разговоров, мы настоятельно рекомендуем использовать API Converse для всех моделей разговоров Chat.

API Converse не поддерживает операции встраивания, поэтому они останутся в текущем API, а функциональность модели встраивания в существующем `InvokeModel API` будет поддерживаться.
====

## Предварительные требования

Смотрите https://docs.aws.amazon.com/bedrock/latest/userguide/getting-started.html[Начало работы с Amazon Bedrock] для настройки доступа к API.

- Получите учетные данные AWS: Если у вас еще нет учетной записи AWS и настроенного AWS CLI, это видео поможет вам настроить его: [Настройка AWS CLI и SDK менее чем за 4 минуты!](https://youtu.be/gswVHTrRX8I?si=buaY7aeI0l3-bBVb). Вы должны получить свои ключи доступа и безопасности.

- Включите модели для использования: Перейдите в [Amazon Bedrock](https://us-east-1.console.aws.amazon.com/bedrock/home) и в меню [Доступ к моделям](https://us-east-1.console.aws.amazon.com/bedrock/home?region=us-east-1#/modelaccess) слева настройте доступ к моделям, которые вы собираетесь использовать.

## Автонастройка

[NOTE]
====
В Spring AI произошли значительные изменения в автонастройке, названия артефактов стартовых модулей. Пожалуйста, обратитесь к https://docs.spring.io/spring-ai/reference/upgrade-notes.html[заметкам об обновлении] для получения дополнительной информации.
====

Добавьте зависимость `spring-ai-starter-model-bedrock-converse` в файл сборки Maven `pom.xml` или Gradle `build.gradle` вашего проекта:

[tabs]
======
Maven::
+
```xml
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-starter-model-bedrock-converse</artifactId>
</dependency>
```

Gradle::
+
```gradle
dependencies {
    implementation 'org.springframework.ai:spring-ai-starter-model-bedrock-converse'
}
```
======

> **Совет:** Обратитесь к разделу xref:getting-started.adoc#dependency-management[Управление зависимостями], чтобы добавить BOM Spring AI в ваш файл сборки.

### Свойства чатаThe prefix `spring.ai.bedrock.aws` — это префикс свойств для настройки подключения к AWS Bedrock.

| Свойство | Описание | По умолчанию |
| --- | --- | --- |

| spring.ai.bedrock.aws.region | Регион AWS для использования | us-east-1 |
| --- | --- | --- |
| spring.ai.bedrock.aws.timeout | Максимальная продолжительность для всего API вызова | 5m |
| spring.ai.bedrock.aws.connectionTimeout | Максимальная продолжительность ожидания при установлении соединения | 5s |
| spring.ai.bedrock.aws.connectionAcquisitionTimeout | Максимальная продолжительность ожидания нового соединения из пула | 30s |
| spring.ai.bedrock.aws.asyncReadTimeout | Максимальная продолжительность чтения асинхронных ответов | 30s |
| spring.ai.bedrock.aws.access-key | Ключ доступа AWS | - |
| spring.ai.bedrock.aws.secret-key | Секретный ключ AWS | - |
| spring.ai.bedrock.aws.session-token | Токен сессии AWS для временных учетных данных | - |
| spring.ai.bedrock.aws.profile.name | Имя профиля AWS. | - |
| spring.ai.bedrock.aws.profile.credentials-path | Путь к файлу учетных данных AWS. | - |
| spring.ai.bedrock.aws.profile.configuration-path | Путь к файлу конфигурации AWS. | - |

[NOTE]
====
Включение и отключение автонастроек чата теперь настраивается через свойства верхнего уровня с префиксом `spring.ai.model.chat`.

Чтобы включить, используйте spring.ai.model.chat=bedrock-converse (по умолчанию включено)

Чтобы отключить, используйте spring.ai.model.chat=none (или любое значение, которое не соответствует bedrock-converse)

Это изменение сделано для возможности настройки нескольких моделей.
====

Префикс `spring.ai.bedrock.converse.chat` — это префикс свойств, который настраивает реализацию модели чата для API Converse.

| Свойство | Описание | По умолчанию |
| --- | --- | --- |

| spring.ai.bedrock.converse.chat.enabled (Удалено и больше не актуально) | Включить модель чата Bedrock Converse. | true |
| --- | --- | --- |
| spring.ai.model.chat | Включить модель чата Bedrock Converse. | bedrock-converse |
| spring.ai.bedrock.converse.chat.options.model | Идентификатор модели для использования. Вы можете использовать https://docs.aws.amazon.com/bedrock/latest/userguide/conversation-inference-supported-models-features.html[Поддерживаемые модели и функции моделей] | Нет. Выберите ваш https://us-east-1.console.aws.amazon.com/bedrock/home?region=us-east-1#/models[modelId] из консоли AWS Bedrock. |
| spring.ai.bedrock.converse.chat.options.temperature | Управляет случайностью вывода. Значения могут варьироваться от [0.0,1.0] | 0.8 |
| spring.ai.bedrock.converse.chat.options.top-p | Максимальная кумулятивная вероятность токенов для учета при выборке. | По умолчанию AWS Bedrock |
| spring.ai.bedrock.converse.chat.options.top-k | Количество вариантов токенов для генерации следующего токена. | По умолчанию AWS Bedrock |
| spring.ai.bedrock.converse.chat.options.max-tokens | Максимальное количество токенов в сгенерированном ответе. | 500 |


Используйте переносимые `ChatOptions` или `BedrockChatOptions` переносимые сборщики для создания конфигураций модели, таких как температура, maxToken, topP и т.д.

При запуске параметры по умолчанию могут быть настроены с помощью конструктора `BedrockConverseProxyChatModel(api, options)` или свойств `spring.ai.bedrock.converse.chat.options.*`.

Во время выполнения вы можете переопределить параметры по умолчанию, добавив новые, специфичные для запроса, параметры в вызов `Prompt`:

```java
var options = BedrockChatOptions.builder()
        .model("anthropic.claude-3-5-sonnet-20240620-v1:0")
        .temperature(0.6)
        .maxTokens(300)
        .toolCallbacks(List.of(FunctionToolCallback.builder("getCurrentWeather", new WeatherService())
            .description("Получить погоду в указанном месте. Вернуть температуру в формате 36°F или 36°C. Используйте многоходовые запросы, если необходимо.")
            .inputType(WeatherService.Request.class)
            .build()))
        .build();

String response = ChatClient.create(this.chatModel)
    .prompt("Какая сейчас погода в Амстердаме?")
    .options(options)
    .call()
    .content();
```

## Кэширование запросовAWS Bedrock's https://docs.aws.amazon.com/bedrock/latest/userguide/prompt-caching.html[функция кэширования подсказок] позволяет кэшировать часто используемые подсказки, чтобы снизить затраты и улучшить время отклика при повторных взаимодействиях. Когда вы кэшируете подсказку, последующие идентичные запросы могут повторно использовать кэшированное содержимое, что значительно снижает количество обрабатываемых входных токенов.

[NOTE]
====
**Поддерживаемые модели**

Кэширование подсказок поддерживается в моделях Claude 3.x, Claude 4.x и Amazon Nova, доступных через AWS Bedrock.

**Требования к токенам**

Разные модели имеют разные минимальные пороги токенов для эффективности кэширования:
- Claude Sonnet 4 и большинство моделей: 1024+ токенов
- Требования могут варьироваться в зависимости от модели - обратитесь к документации AWS Bedrock
====

### Стратегии кэширования

Spring AI предоставляет стратегическое размещение кэша через перечисление `BedrockCacheStrategy`:

- `NONE`: Полностью отключает кэширование подсказок (по умолчанию)
- `SYSTEM_ONLY`: Кэширует только содержимое системного сообщения
- `TOOLS_ONLY`: Кэширует только определения инструментов (только модели Claude)
- `SYSTEM_AND_TOOLS`: Кэширует как системное сообщение, так и определения инструментов (только модели Claude)
- `CONVERSATION_HISTORY`: Кэширует всю историю разговора в сценариях с памятью чата

Этот стратегический подход обеспечивает оптимальное размещение контрольных точек кэша, оставаясь в пределах лимита в 4 контрольные точки AWS Bedrock.

[NOTE]
====
**Ограничения Amazon Nova**

Модели Amazon Nova (Nova Micro, Lite, Pro, Premier) поддерживают кэширование только для содержимого `system` и `messages`. 
Они **не** поддерживают кэширование для `tools`.

Если вы попытаетесь использовать стратегии `TOOLS_ONLY` или `SYSTEM_AND_TOOLS` с моделями Nova, AWS вернет `ValidationException`. 
Используйте стратегию `SYSTEM_ONLY` для моделей Amazon Nova.
====

### Включение кэширования подсказок

Включите кэширование подсказок, установив `cacheOptions` в `BedrockChatOptions` и выбрав `strategy`.

#### Кэширование только системных сообщений

Наиболее распространенный случай использования - кэширование системных инструкций при нескольких запросах:

```java
// Кэширование содержимого системного сообщения
ChatResponse response = chatModel.call(
    new Prompt(
        List.of(
            new SystemMessage("Вы - полезный AI-ассистент с обширными знаниями..."),
            new UserMessage("Что такое машинное обучение?")
        ),
        BedrockChatOptions.builder()
            .model("us.anthropic.claude-3-7-sonnet-20250219-v1:0")
            .cacheOptions(BedrockCacheOptions.builder()
                .strategy(BedrockCacheStrategy.SYSTEM_ONLY)
                .build())
            .maxTokens(500)
            .build()
    )
);
```

#### Кэширование только инструментов

Кэшируйте большие определения инструментов, сохраняя системные подсказки динамичными (только модели Claude):

```java
// Кэширование только определений инструментов
ChatResponse response = chatModel.call(
    new Prompt(
        "Какова погода в Сан-Франциско?",
        BedrockChatOptions.builder()
            .model("us.anthropic.claude-3-7-sonnet-20250219-v1:0")
            .cacheOptions(BedrockCacheOptions.builder()
                .strategy(BedrockCacheStrategy.TOOLS_ONLY)
                .build())
            .toolCallbacks(weatherToolCallbacks)  // Большие определения инструментов
            .maxTokens(500)
            .build()
    )
);
```

> **Примечание:** Эта стратегия поддерживается только в моделях Claude. 
Модели Amazon Nova вернут `ValidationException`.

#### Кэширование системных сообщений и инструментовCache как системные инструкции, так и определения инструментов для максимального повторного использования (только модели Claude):

```java
// Кэширование системного сообщения и определений инструментов
ChatResponse response = chatModel.call(
    new Prompt(
        List.of(
            new SystemMessage("Вы помощник по анализу погоды..."),
            new UserMessage("Какова погода в Токио?")
        ),
        BedrockChatOptions.builder()
            .model("us.anthropic.claude-3-7-sonnet-20250219-v1:0")
            .cacheOptions(BedrockCacheOptions.builder()
                .strategy(BedrockCacheStrategy.SYSTEM_AND_TOOLS)
                .build())
            .toolCallbacks(weatherToolCallbacks)
            .maxTokens(500)
            .build()
    )
);
```

> **Примечание:** Эта стратегия использует 2 точки прерывания кэша (одну для инструментов, одну для системы). Поддерживается только в моделях Claude.

#### Кэширование истории беседы

Кэширование растущей истории беседы для многоповоротных чат-ботов и помощников:

```java
// Кэширование истории беседы с ChatClient и памятью
ChatClient chatClient = ChatClient.builder(chatModel)
    .defaultSystem("Вы персонализированный карьерный консультант...")
    .defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory)
        .conversationId(conversationId)
        .build())
    .build();

String response = chatClient.prompt()
    .user("Какой карьерный совет вы бы мне дали?")
    .options(BedrockChatOptions.builder()
        .model("us.anthropic.claude-3-7-sonnet-20250219-v1:0")
        .cacheOptions(BedrockCacheOptions.builder()
            .strategy(BedrockCacheStrategy.CONVERSATION_HISTORY)
            .build())
        .maxTokens(500)
        .build())
    .call()
    .content();
```

#### Использование Fluent API ChatClient

```java
String response = ChatClient.create(chatModel)
    .prompt()
    .system("Вы эксперт по анализу документов...")
    .user("Проанализируйте этот большой документ: " + document)
    .options(BedrockChatOptions.builder()
        .model("us.anthropic.claude-3-7-sonnet-20250219-v1:0")
        .cacheOptions(BedrockCacheOptions.builder()
            .strategy(BedrockCacheStrategy.SYSTEM_ONLY)
            .build())
        .build())
    .call()
    .content();
```

### Пример использованияВот полный пример, демонстрирующий кэширование запросов с отслеживанием затрат:

```java
// Создайте системный контент, который будет использоваться несколько раз
String largeSystemPrompt = "Вы эксперт в области проектирования программного обеспечения, специализирующийся на распределенных системах...";
// (Убедитесь, что это 1024+ токенов для эффективности кэша)

// Первый запрос - создает кэш
ChatResponse firstResponse = chatModel.call(
    new Prompt(
        List.of(
            new SystemMessage(largeSystemPrompt),
            new UserMessage("Что такое архитектура микросервисов?")
        ),
        BedrockChatOptions.builder()
            .model("us.anthropic.claude-3-7-sonnet-20250219-v1:0")
            .cacheOptions(BedrockCacheOptions.builder()
                .strategy(BedrockCacheStrategy.SYSTEM_ONLY)
                .build())
            .maxTokens(500)
            .build()
    )
);

// Получите информацию о расходах токенов, связанных с кэшем, из метаданных
Integer cacheWrite1 = (Integer) firstResponse.getMetadata()
    .getMetadata()
    .get("cacheWriteInputTokens");
Integer cacheRead1 = (Integer) firstResponse.getMetadata()
    .getMetadata()
    .get("cacheReadInputTokens");

System.out.println("Токены создания кэша: " + cacheWrite1);
System.out.println("Токены чтения кэша: " + cacheRead1);

// Второй запрос с тем же системным запросом - чтение из кэша
ChatResponse secondResponse = chatModel.call(
    new Prompt(
        List.of(
            new SystemMessage(largeSystemPrompt),  // Тот же запрос - попадание в кэш
            new UserMessage("Каковы преимущества событийного источника?")
        ),
        BedrockChatOptions.builder()
            .model("us.anthropic.claude-3-7-sonnet-20250219-v1:0")
            .cacheOptions(BedrockCacheOptions.builder()
                .strategy(BedrockCacheStrategy.SYSTEM_ONLY)
                .build())
            .maxTokens(500)
            .build()
    )
);

Integer cacheWrite2 = (Integer) secondResponse.getMetadata()
    .getMetadata()
    .get("cacheWriteInputTokens");
Integer cacheRead2 = (Integer) secondResponse.getMetadata()
    .getMetadata()
    .get("cacheReadInputTokens");

System.out.println("Токены создания кэша: " + cacheWrite2); // Должно быть 0
System.out.println("Токены чтения кэша: " + cacheRead2);      // Должно быть > 0
```

### Отслеживание использования токенов

AWS Bedrock предоставляет метрики, специфичные для кэша, через ответ.
Метрики кэша доступны через два метода:

#### Объект использования токенов (Рекомендуется для наблюдаемости)

Для обработчиков наблюдаемости и сбора метрик получайте метрики кэша через родной объект `TokenUsage`:

```java
import software.amazon.awssdk.services.bedrockruntime.model.TokenUsage;

ChatResponse response = chatModel.call(/* ... */);

// Получите метрики кэша из родного объекта TokenUsage
TokenUsage tokenUsage = (TokenUsage) response.getMetadata()
    .getUsage()
    .getNativeUsage();

if (tokenUsage != null) {
    Integer cacheWrite = tokenUsage.cacheWriteInputTokens();
    Integer cacheRead = tokenUsage.cacheReadInputTokens();
    System.out.println("Запись в кэш: " + cacheWrite + ", Чтение из кэша: " + cacheRead);
}
```

#### Карта метаданных (Совместимость с предыдущими версиями)Cache-метрики также доступны через карту метаданных для обратной совместимости:

```java
ChatResponse response = chatModel.call(/* ... */);

// Доступ к метрикам кэша из карты метаданных
Integer cacheWrite = (Integer) response.getMetadata()
    .getMetadata()
    .get("cacheWriteInputTokens");
Integer cacheRead = (Integer) response.getMetadata()
    .getMetadata()
    .get("cacheReadInputTokens");
```

Метрики, специфичные для кэша, включают:

- `cacheWriteInputTokens`: Возвращает количество токенов, использованных при создании записи в кэше
- `cacheReadInputTokens`: Возвращает количество токенов, прочитанных из существующей записи в кэше

Когда вы впервые отправляете кэшированный запрос:
- `cacheWriteInputTokens` будет больше 0
- `cacheReadInputTokens` будет 0

Когда вы снова отправляете тот же кэшированный запрос (в пределах 5 минут TTL):
- `cacheWriteInputTokens` будет 0
- `cacheReadInputTokens` будет больше 0

### Примеры использования в реальном мире

#### Анализ юридических документов

Эффективно анализируйте большие юридические контракты или документы по соблюдению норм, кэшируя содержимое документов для нескольких вопросов:

```java
// Загрузите юридический контракт (PDF или текст)
String legalContract = loadDocument("merger-agreement.pdf"); // ~3000 токенов

// Системный запрос с юридической экспертизой
String legalSystemPrompt = "Вы являетесь экспертом-аналитиком в области права, специализирующимся на корпоративном праве. " +
    "Проанализируйте следующий контракт и предоставьте точные ответы о терминах, обязательствах и рисках: " +
    legalContract;

// Первый анализ - создает кэш
ChatResponse riskAnalysis = chatModel.call(
    new Prompt(
        List.of(
            new SystemMessage(legalSystemPrompt),
            new UserMessage("Каковы ключевые условия расторжения и связанные с ними штрафы?")
        ),
        BedrockChatOptions.builder()
            .model("us.anthropic.claude-3-7-sonnet-20250219-v1:0")
            .cacheOptions(BedrockCacheOptions.builder()
                .strategy(BedrockCacheStrategy.SYSTEM_ONLY)
                .build())
            .maxTokens(1000)
            .build()
    )
);

// Последующие вопросы повторно используют кэшированный документ - экономия 90% затрат
ChatResponse obligationAnalysis = chatModel.call(
    new Prompt(
        List.of(
            new SystemMessage(legalSystemPrompt), // То же содержимое - попадание в кэш
            new UserMessage("Перечислите все финансовые обязательства и графики платежей.")
        ),
        BedrockChatOptions.builder()
            .model("us.anthropic.claude-3-7-sonnet-20250219-v1:0")
            .cacheOptions(BedrockCacheOptions.builder()
                .strategy(BedrockCacheStrategy.SYSTEM_ONLY)
                .build())
            .maxTokens(1000)
            .build()
    )
);
```

#### Пакетный обзор кода```markdown
Обработка нескольких файлов кода с едиными критериями ревью и кэшированием руководства по ревью:

```java
// Определите всеобъемлющие критерии ревью кода
String reviewGuidelines = """
    Вы старший инженер-программист, проводящий ревью кода. Применяйте эти критерии:
    - Уязвимости безопасности и лучшие практики
    - Оптимизация производительности и использование памяти
    - Поддерживаемость и читаемость кода
    - Покрытие тестами и крайние случаи
    - Соответствие шаблонам проектирования и архитектуры
    """;

List<String> codeFiles = Arrays.asList(
    "UserService.java", "PaymentController.java", "SecurityConfig.java"
);

List<String> reviews = new ArrayList<>();

for (String filename : codeFiles) {
    String sourceCode = loadSourceFile(filename);

    ChatResponse review = chatModel.call(
        new Prompt(
            List.of(
                new SystemMessage(reviewGuidelines), // Кэшируется для всех ревью
                new UserMessage("Просмотрите этот код " + filename + ":\n\n" + sourceCode)
            ),
            BedrockChatOptions.builder()
                .model("us.anthropic.claude-3-7-sonnet-20250219-v1:0")
                .cacheOptions(BedrockCacheOptions.builder()
                    .strategy(BedrockCacheStrategy.SYSTEM_ONLY)
                    .build())
                .maxTokens(800)
                .build()
        )
    );

    reviews.add(review.getResult().getOutput().getText());
}

// Руководство кэшируется после первого запроса, последующие ревью проходят быстрее и дешевле
```

#### Служба поддержки клиентов с базой знаний

Создайте систему поддержки клиентов, которая кэширует вашу базу знаний о продукте для последовательных и точных ответов:

```java
// Загрузите всеобъемлющую базу знаний о продукте
String knowledgeBase = """
    ДОКУМЕНТАЦИЯ ПРОДУКТА:
    - API-эндпоинты и методы аутентификации
    - Общие процедуры устранения неполадок
    - Подробности о выставлении счетов и подписках
    - Руководства по интеграции и примеры
    - Известные проблемы и обходные пути
    """ + loadProductDocs(); // ~2500 токенов

@Service
public class CustomerSupportService {

    public String handleCustomerQuery(String customerQuery, String customerId) {
        ChatResponse response = chatModel.call(
            new Prompt(
                List.of(
                    new SystemMessage("Вы полезный агент службы поддержки клиентов. " +
                        "Используйте эту базу знаний для предоставления точных решений: " + knowledgeBase),
                    new UserMessage("Клиент " + customerId + " спрашивает: " + customerQuery)
                ),
                BedrockChatOptions.builder()
                    .model("us.anthropic.claude-3-7-sonnet-20250219-v1:0")
                    .cacheOptions(BedrockCacheOptions.builder()
                        .strategy(BedrockCacheStrategy.SYSTEM_ONLY)
                        .build())
                    .maxTokens(600)
                    .build()
            )
        );

        return response.getResult().getOutput().getText();
    }
}

// База знаний кэшируется для всех запросов клиентов
// Несколько агентов поддержки могут использовать одно и то же кэшированное содержимое
```

#### Многоарендное SaaS-приложение
```Cache shared tool definitions across different tenants while customizing system prompts per tenant:

```java
// Общие определения инструментов (кэшируются один раз, используются всеми арендаторами)
List<FunctionToolCallback> sharedTools = createLargeToolRegistry(); // ~2000 токенов

// Конфигурация, специфичная для арендатора
@Service
public class MultiTenantAIService {

    public String processRequest(String tenantId, String userQuery) {
        // Загрузка системного запроса, специфичного для арендатора (меняется для каждого арендатора)
        String tenantPrompt = loadTenantSystemPrompt(tenantId);

        ChatResponse response = chatModel.call(
            new Prompt(
                List.of(
                    new SystemMessage(tenantPrompt), // Специфично для арендатора, не кэшируется
                    new UserMessage(userQuery)
                ),
                BedrockChatOptions.builder()
                    .model("us.anthropic.claude-3-7-sonnet-20250219-v1:0")
                    .cacheOptions(BedrockCacheOptions.builder()
                        .strategy(BedrockCacheStrategy.TOOLS_ONLY)
                        .build())
                    .toolCallbacks(sharedTools) // Общие инструменты - кэшируются
                    .maxTokens(500)
                    .build()
            )
        );

        return response.getResult().getOutput().getText();
    }
}

// Инструменты кэшируются один раз, каждый арендатор получает индивидуальный системный запрос
```

### Лучшие практики

1. **Выбор правильной стратегии**:
   - Используйте `SYSTEM_ONLY` для повторно используемых системных запросов и инструкций (работает со всеми моделями)
   - Используйте `TOOLS_ONLY`, когда у вас есть большие стабильные инструменты, но динамические системные запросы (только Claude)
   - Используйте `SYSTEM_AND_TOOLS`, когда и система, и инструменты большие и стабильные (только Claude)
   - Используйте `CONVERSATION_HISTORY` с памятью ChatClient для многократных разговоров
   - Используйте `NONE`, чтобы явно отключить кэширование

2. **Соответствие требованиям к токенам**: Сосредоточьтесь на кэшировании контента, который соответствует минимальным требованиям к токенам (1024+ токена для большинства моделей).

3. **Повторное использование идентичного контента**: Кэширование работает лучше всего с точными совпадениями содержимого запроса. Даже небольшие изменения потребуют новой записи в кэше.

4. **Мониторинг использования токенов**: Отслеживайте эффективность кэша, используя метрики метаданных:

   Integer cacheWrite = (Integer) response.getMetadata().getMetadata().get("cacheWriteInputTokens");
   Integer cacheRead = (Integer) response.getMetadata().getMetadata().get("cacheReadInputTokens");
   if (cacheRead != null && cacheRead > 0) {
       System.out.println("Попадание в кэш: " + cacheRead + " токенов сэкономлено");
   }

5. **Стратегическое размещение кэша**: Реализация автоматически размещает точки прерывания кэша в оптимальных местах в зависимости от выбранной стратегии, обеспечивая соответствие лимиту в 4 точки прерывания AWS Bedrock.

6. **Срок действия кэша**: Кэши AWS Bedrock имеют фиксированный срок действия 5 минут (Time To Live). Каждый доступ к кэшу сбрасывает таймер.

7. **Совместимость моделей**: Обратите внимание на ограничения, специфичные для моделей:
   - **Модели Claude**: Поддерживают все стратегии кэширования
   - **Модели Amazon Nova**: Поддерживают только `SYSTEM_ONLY` и `CONVERSATION_HISTORY` (кэширование инструментов не поддерживается)

8. **Стабильность инструментов**: При использовании стратегий `TOOLS_ONLY`, `SYSTEM_AND_TOOLS` или `CONVERSATION_HISTORY` убедитесь, что инструменты остаются стабильными. Изменение определений инструментов приведет к недействительности всех последующих точек прерывания кэша из-за каскадной недействительности.

### Недействительность кэша и каскадное поведениеAWS Bedrock использует иерархическую модель кэширования с каскадной недействительностью:

**Иерархия кэша**: `Инструменты → Система → Сообщения`

Изменения на каждом уровне делают недействительными этот уровень и все последующие уровни:

| Что изменилось | Кэш инструментов | Кэш системы | Кэш сообщений |
| --- | --- | --- | --- |

| Инструменты | ❌ Недействителен | ❌ Недействителен | ❌ Недействителен |
| --- | --- | --- | --- |
| Система | ✅ Действителен | ❌ Недействителен | ❌ Недействителен |
| Сообщения | ✅ Действителен | ✅ Действителен | ❌ Недействителен |

**Пример с стратегией `SYSTEM_AND_TOOLS`**:

```java
// Запрос 1: Кэширование как инструментов, так и системы
ChatResponse r1 = chatModel.call(
    new Prompt(
        List.of(new SystemMessage("Системный запрос"), new UserMessage("Вопрос")),
        BedrockChatOptions.builder()
            .cacheOptions(BedrockCacheOptions.builder()
                .strategy(BedrockCacheStrategy.SYSTEM_AND_TOOLS)
                .build())
            .toolCallbacks(tools)
            .build()
    )
);
// Результат: Созданы оба кэша

// Запрос 2: Изменение только системного запроса (инструменты те же)
ChatResponse r2 = chatModel.call(
    new Prompt(
        List.of(new SystemMessage("ДРУГОЙ системный запрос"), new UserMessage("Вопрос")),
        BedrockChatOptions.builder()
            .cacheOptions(BedrockCacheOptions.builder()
                .strategy(BedrockCacheStrategy.SYSTEM_AND_TOOLS)
                .build())
            .toolCallbacks(tools) // ТЕ ЖЕ инструменты
            .build()
    )
);
// Результат: Кэш инструментов HIT (повторно использован), кэш системы MISS (восстановлен)

// Запрос 3: Изменение инструментов (система такая же, как в Запросе 2)
ChatResponse r3 = chatModel.call(
    new Prompt(
        List.of(new SystemMessage("ДРУГОЙ системный запрос"), new UserMessage("Вопрос")),
        BedrockChatOptions.builder()
            .cacheOptions(BedrockCacheOptions.builder()
                .strategy(BedrockCacheStrategy.SYSTEM_AND_TOOLS)
                .build())
            .toolCallbacks(newTools) // ДРУГИЕ инструменты
            .build()
    )
);
// Результат: Оба кэша MISS (изменение инструментов делает недействительными все последующие уровни)
```

### Подробности реализации

Реализация кэширования запросов в Spring AI основывается на следующих ключевых принципах проектирования:

1. **Стратегическое размещение кэша**: Точки разрыва кэша автоматически размещаются в оптимальных местах в зависимости от выбранной стратегии, что обеспечивает соответствие лимиту в 4 точки разрыва AWS Bedrock.

2. **Портативность провайдеров**: Конфигурация кэша осуществляется через `BedrockChatOptions`, а не отдельные сообщения, что сохраняет совместимость при переходе между различными провайдерами ИИ.

3. **Безопасность потоков**: Отслеживание точек разрыва кэша реализовано с использованием потокобезопасных механизмов для корректной обработки параллельных запросов.

4. **Шаблон типа UNION**: AWS SDK использует типы UNION, где точки кэша добавляются как отдельные блоки, а не свойства. Это отличается от прямых подходов API, но обеспечивает безопасность типов и соответствие API.

5. **Инкрементальное кэширование**: Стратегия `CONVERSATION_HISTORY` размещает точки разрыва кэша на последнем сообщении пользователя, позволяя инкрементальное кэширование, при котором каждый поворот разговора строится на предыдущем кэшированном префиксе.

### Стоимость

Цены AWS Bedrock на кэширование запросов (приблизительно, варьируется в зависимости от модели):

- **Записи в кэш**: ~25% дороже, чем базовые токены ввода
- **Чтения из кэша**: ~90% дешевле (только 10% от цены базового токена ввода)
- **Точка безубыточности**: После всего лишь 1 чтения из кэша вы сэкономили деньги

**Пример расчета стоимости**:

```java
// Системный запрос: 2000 токенов
// Вопрос пользователя: 50 токенов

// Без кэширования (5 запросов):
// Стоимость: 5 × (2000 + 50) = 10,250 токенов по базовой ставке

// С кэшированием (5 запросов):
// Запрос 1: 2000 токенов × 1.25 (запись в кэш) + 50 = 2,550 токенов
// Запросы 2-5: 4 × (2000 × 0.10 (чтение из кэша) + 50) = 4 × 250 = 1,000 токенов
// Всего: 2,550 + 1,000 = 3,550 токенов эквивалентно

// Экономия: (10,250 - 3,550) / 10,250 = 65% снижение затрат
```

## Вызов инструментаThe Bedrock Converse API поддерживает возможности вызова инструментов, позволяя моделям использовать инструменты во время разговоров. Вот пример того, как определить и использовать инструменты на основе @Tool:

```java

public class WeatherService {

    @Tool(description = "Получить погоду в местоположении")
    public String weatherByLocation(@ToolParam(description= "Название города или штата") String location) {
        ...
    }
}

String response = ChatClient.create(this.chatModel)
        .prompt("Какая погода в Бостоне?")
        .tools(new WeatherService())
        .call()
        .content();
```

Вы также можете использовать бины java.util.function в качестве инструментов:

```java
@Bean
@Description("Получить погоду в местоположении. Вернуть температуру в формате 36°F или 36°C.")
public Function<Request, Response> weatherFunction() {
    return new MockWeatherService();
}

String response = ChatClient.create(this.chatModel)
        .prompt("Какая погода в Бостоне?")
        .toolNames("weatherFunction")
        .inputType(Request.class)
        .call()
        .content();
```

Дополнительную информацию можно найти в документации xref:api/tools.adoc[Инструменты].

## Мультимодальность

Мультимодальность относится к способности модели одновременно понимать и обрабатывать информацию из различных источников, включая текст, изображения, видео, pdf, doc, html, md и другие форматы данных.

API Bedrock Converse поддерживает мультимодальные входные данные, включая текстовые и изображенческие входы, и может генерировать текстовый ответ на основе комбинированного ввода.

Вам нужна модель, которая поддерживает мультимодальные входные данные, такие как модели Anthropic Claude или Amazon Nova.

### Изображения

Для [моделей](https://docs.aws.amazon.com/bedrock/latest/userguide/conversation-inference-supported-models-features.html), которые поддерживают визуальную мультимодальность, таких как Amazon Nova, Anthropic Claude, Llama 3.2, API Bedrock Converse позволяет включать несколько изображений в полезную нагрузку. Эти модели могут анализировать переданные изображения и отвечать на вопросы, классифицировать изображение, а также обобщать изображения на основе предоставленных инструкций.

В настоящее время Bedrock Converse поддерживает изображения, закодированные в `base64`, с типами MIME `image/jpeg`, `image/png`, `image/gif` и `image/webp`.

Интерфейс `Message` Spring AI поддерживает мультимодальные AI модели, вводя тип `Media`. Он содержит данные и информацию о медиа-вложениях в сообщениях, используя `org.springframework.util.MimeType` от Spring и `java.lang.Object` для необработанных медиа-данных.

Ниже приведен простой пример кода, демонстрирующий сочетание текста пользователя с изображением.

```java
String response = ChatClient.create(chatModel)
    .prompt()
    .user(u -> u.text("Объясните, что вы видите на этом изображении?")
        .media(Media.Format.IMAGE_PNG, new ClassPathResource("/test.png")))
    .call()
    .content();

logger.info(response);
```

Он принимает в качестве входа изображение `test.png`:

![Мультимодальное тестовое изображение, 200, 200, align="left"](multimodal.test.png)

вместе с текстовым сообщением "Объясните, что вы видите на этом изображении?", и генерирует ответ, похожий на:

```
Изображение показывает крупный план проволочной фруктовой корзины, содержащей несколько фруктов.
...
```

### Видео
Модели [Amazon Nova](https://docs.aws.amazon.com/nova/latest/userguide/modalities-video.html) позволяют включать одно видео в полезную нагрузку, которое может быть предоставлено либо в формате base64, либо через URI Amazon S3.

В настоящее время Bedrock Nova поддерживает видео с MIME-типами `video/x-matroska`, `video/quicktime`, `video/mp4`, `video/webm`, `video/x-flv`, `video/mpeg`, `video/x-ms-wmv` и `video/3gpp`.

Интерфейс `Message` от Spring AI поддерживает мультимодальные AI модели, вводя тип `Media`. Он содержит данные и информацию о медиа-вложениях в сообщениях, используя `org.springframework.util.MimeType` от Spring и `java.lang.Object` для необработанных медиа-данных.

Ниже приведен простой пример кода, демонстрирующий сочетание пользовательского текста с видео.

```java
String response = ChatClient.create(chatModel)
    .prompt()
    .user(u -> u.text("Объясните, что вы видите в этом видео?")
        .media(Media.Format.VIDEO_MP4, new ClassPathResource("/test.video.mp4")))
    .call()
    .content();

logger.info(response);
```

Он принимает в качестве входных данных изображение `test.video.mp4`:

![Мультимодальное тестовое видео, 200, 200, align="left"](test.video.jpeg)

вместе с текстовым сообщением "Объясните, что вы видите в этом видео?", и генерирует ответ, похожий на:

```
Видео показывает группу цыплят, также известных как птенцы, собравшихся вместе на поверхности
...
```

### Документы

Для некоторых моделей Bedrock позволяет включать документы в полезную нагрузку через поддержку документов API Converse, которые могут быть предоставлены в байтах. Поддержка документов имеет два различных варианта, как объяснено ниже:

- **Типы текстовых документов** (txt, csv, html, md и так далее), где акцент делается на понимании текста. Эти случаи использования включают ответы на основе текстовых элементов документа.
- **Типы медиа-документов** (pdf, docx, xlsx), где акцент делается на понимании на основе визуальных данных для ответов на вопросы. Эти случаи использования включают ответы на вопросы на основе диаграмм, графиков и так далее.

В настоящее время поддержка [PDF (бета)](https://docs.anthropic.com/en/docs/build-with-claude/pdf-support) от Anthropic и модели Amazon Bedrock Nova поддерживают мультимодальность документов.

Ниже приведен простой пример кода, демонстрирующий сочетание пользовательского текста с медиа-документом.

```java
String response = ChatClient.create(chatModel)
    .prompt()
    .user(u -> u.text(
            "Вы очень профессиональный специалист по суммированию документов. Пожалуйста, подведите итог данному документу.")
        .media(Media.Format.DOC_PDF, new ClassPathResource("/spring-ai-reference-overview.pdf")))
    .call()
    .content();

logger.info(response);
```

Он принимает в качестве входных данных документ `spring-ai-reference-overview.pdf`:

![Мультимодальное тестовое PNG, 200, 200, align="left"](test.pdf.png)

вместе с текстовым сообщением "Вы очень профессиональный специалист по суммированию документов. Пожалуйста, подведите итог данному документу.", и генерирует ответ, похожий на:

```
**Введение:**
- Spring AI разработан для упрощения разработки приложений с возможностями искусственного интеллекта (AI), стремясь избежать ненужной сложности.
...
```


## Пример контроллераСоздайте новый проект Spring Boot и добавьте `spring-ai-starter-model-bedrock-converse` в ваши зависимости.

Добавьте файл `application.properties` в папку `src/main/resources`:

```properties
spring.ai.bedrock.aws.region=eu-central-1
spring.ai.bedrock.aws.timeout=10m
spring.ai.bedrock.aws.access-key=${AWS_ACCESS_KEY_ID}
spring.ai.bedrock.aws.secret-key=${AWS_SECRET_ACCESS_KEY}
# токен сессии требуется только для временных учетных данных
spring.ai.bedrock.aws.session-token=${AWS_SESSION_TOKEN}

spring.ai.bedrock.converse.chat.options.temperature=0.8
spring.ai.bedrock.converse.chat.options.top-k=15
```

Вот пример контроллера, использующего модель чата:

```java
@RestController
public class ChatController {

    private final ChatClient chatClient;

    @Autowired
    public ChatController(ChatClient.Builder builder) {
        this.chatClient = builder.build();
    }

    @GetMapping("/ai/generate")
    public Map generate(@RequestParam(value = "message", defaultValue = "Tell me a joke") String message) {
        return Map.of("generation", this.chatClient.prompt(message).call().content());
    }

    @GetMapping("/ai/generateStream")
    public Flux<ChatResponse> generateStream(@RequestParam(value = "message", defaultValue = "Tell me a joke") String message) {
        return this.chatClient.prompt(message).stream().content();
    }
}
```
