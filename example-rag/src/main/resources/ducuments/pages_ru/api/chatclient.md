```markdown
[[ChatClient]]
# API клиента чата

`ChatClient` предлагает удобный API для общения с AI моделью.
Он поддерживает как синхронную, так и потоковую модель программирования.

[NOTE]
====
Смотрите xref:api/chatclient.adoc#_implementation_notes[Примечания по реализации] внизу этого документа, касающиеся совместного использования императивных и реактивных моделей программирования в `ChatClient`
====

Удобный API имеет методы для построения составных частей xref:api/prompt.adoc#_prompt[Запроса], который передается AI модели в качестве входных данных.
`Запрос` содержит инструктивный текст, который направляет вывод и поведение AI модели. С точки зрения API, запросы состоят из коллекции сообщений.

AI модель обрабатывает два основных типа сообщений: сообщения от пользователя, которые являются прямыми входными данными от пользователя, и системные сообщения, которые генерируются системой для управления разговором.

Эти сообщения часто содержат заполнители, которые заменяются во время выполнения на основе ввода пользователя, чтобы настроить ответ AI модели на ввод пользователя.

Также есть параметры Запроса, которые можно указать, такие как имя используемой AI модели и настройка температуры, которая контролирует случайность или креативность сгенерированного вывода.

## Создание ChatClient

`ChatClient` создается с использованием объекта `ChatClient.Builder`.
Вы можете получить автоматически сконфигурированный экземпляр `ChatClient.Builder` для любой xref:api/chatmodel.adoc[ChatModel] Spring Boot автоконфигурации или создать его программно.

### Использование автоматически сконфигурированного ChatClient.Builder

В самом простом случае Spring AI предоставляет автоконфигурацию Spring Boot, создавая прототип бина `ChatClient.Builder`, который вы можете внедрить в свой класс.
Вот простой пример получения `String` ответа на простой запрос пользователя.

```java
@RestController
class MyController {

    private final ChatClient chatClient;

    public MyController(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    @GetMapping("/ai")
    String generation(String userInput) {
        return this.chatClient.prompt()
            .user(userInput)
            .call()
            .content();
    }
}
```

В этом простом примере ввод пользователя задает содержимое сообщения от пользователя.
Метод `call()` отправляет запрос к AI модели, а метод `content()` возвращает ответ AI модели в виде `String`.

### Работа с несколькими моделями чата

Существует несколько сценариев, когда вам может понадобиться работать с несколькими моделями чата в одном приложении:

- Использование различных моделей для разных типов задач (например, мощная модель для сложного рассуждения и более быстрая, дешевая модель для простых задач)
- Реализация механизмов резервирования, когда одна модель недоступна
- A/B тестирование различных моделей или конфигураций
- Предоставление пользователям выбора моделей в зависимости от их предпочтений
- Сочетание специализированных моделей (одна для генерации кода, другая для креативного контента и т.д.)

По умолчанию Spring AI автоматически настраивает один бин `ChatClient.Builder`.
Тем не менее, вам может понадобиться работать с несколькими моделями чата в вашем приложении.
Вот как справиться с этой ситуацией:

Во всех случаях вам нужно отключить автоконфигурацию `ChatClient.Builder`, установив свойство `spring.ai.chat.client.enabled=false`.

Это позволит вам вручную создать несколько экземпляров `ChatClient`.

#### Несколько ChatClients с одним типом модели

Этот раздел охватывает распространенный случай, когда вам нужно создать несколько экземпляров ChatClient, которые все используют один и тот же базовый тип модели, но с различными конфигурациями.

```java
// Создание экземпляров ChatClient программно
ChatModel myChatModel = ... // уже автоматически сконфигурирован Spring Boot
ChatClient chatClient = ChatClient.create(myChatModel);

// Или используйте билдера для большего контроля
ChatClient.Builder builder = ChatClient.builder(myChatModel);
ChatClient customChatClient = builder
    .defaultSystemPrompt("Вы полезный помощник.")
    .build();
```#### ChatClients для различных типов моделей

При работе с несколькими AI моделями вы можете определить отдельные бины `ChatClient` для каждой модели:

```java
import org.springframework.ai.chat.ChatClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ChatClientConfig {
    
    @Bean
    public ChatClient openAiChatClient(OpenAiChatModel chatModel) {
        return ChatClient.create(chatModel);
    }
    
    @Bean
    public ChatClient anthropicChatClient(AnthropicChatModel chatModel) {
        return ChatClient.create(chatModel);
    }
}
```

Затем вы можете внедрить эти бины в компоненты вашего приложения, используя аннотацию `@Qualifier`:

```java

@Configuration
public class ChatClientExample {
    
    @Bean
    CommandLineRunner cli(
            @Qualifier("openAiChatClient") ChatClient openAiChatClient,
            @Qualifier("anthropicChatClient") ChatClient anthropicChatClient) {
        
        return args -> {
            var scanner = new Scanner(System.in);
            ChatClient chat;
            
            // Выбор модели
            System.out.println("\nВыберите вашу AI модель:");
            System.out.println("1. OpenAI");
            System.out.println("2. Anthropic");
            System.out.print("Введите ваш выбор (1 или 2): ");
            
            String choice = scanner.nextLine().trim();
            
            if (choice.equals("1")) {
                chat = openAiChatClient;
                System.out.println("Используется модель OpenAI");
            } else {
                chat = anthropicChatClient;
                System.out.println("Используется модель Anthropic");
            }
            
            // Используйте выбранный клиент чата
            System.out.print("\nВведите ваш вопрос: ");
            String input = scanner.nextLine();
            String response = chat.prompt(input).call().content();
            System.out.println("ASSISTANT: " + response);
            
            scanner.close();
        };
    }
}
```

#### Несколько API-эндпоинтов, совместимых с OpenAI```markdown
Классы `OpenAiApi` и `OpenAiChatModel` предоставляют метод `mutate()`, который позволяет создавать вариации существующих экземпляров с различными свойствами. Это особенно полезно, когда вам нужно работать с несколькими API, совместимыми с OpenAI.

```java
@Service
public class MultiModelService {
    
    private static final Logger logger = LoggerFactory.getLogger(MultiModelService.class);
    
    @Autowired
    private OpenAiChatModel baseChatModel;
    
    @Autowired
    private OpenAiApi baseOpenAiApi;
    
    public void multiClientFlow() {
        try {
            // Создание нового OpenAiApi для Groq (Llama3)
            OpenAiApi groqApi = baseOpenAiApi.mutate()
                .baseUrl("https://api.groq.com/openai")
                .apiKey(System.getenv("GROQ_API_KEY"))
                .build();
            
            // Создание нового OpenAiApi для OpenAI GPT-4
            OpenAiApi gpt4Api = baseOpenAiApi.mutate()
                .baseUrl("https://api.openai.com")
                .apiKey(System.getenv("OPENAI_API_KEY"))
                .build();
            
            // Создание нового OpenAiChatModel для Groq
            OpenAiChatModel groqModel = baseChatModel.mutate()
                .openAiApi(groqApi)
                .defaultOptions(OpenAiChatOptions.builder().model("llama3-70b-8192").temperature(0.5).build())
                .build();
            
            // Создание нового OpenAiChatModel для GPT-4
            OpenAiChatModel gpt4Model = baseChatModel.mutate()
                .openAiApi(gpt4Api)
                .defaultOptions(OpenAiChatOptions.builder().model("gpt-4").temperature(0.7).build())
                .build();
            
            // Простой запрос для обеих моделей
            String prompt = "Какова столица Франции?";
            
            String groqResponse = ChatClient.builder(groqModel).build().prompt(prompt).call().content();
            String gpt4Response = ChatClient.builder(gpt4Model).build().prompt(prompt).call().content();
            
            logger.info("Ответ Groq (Llama3): {}", groqResponse);
            logger.info("Ответ OpenAI GPT-4: {}", gpt4Response);
        }
        catch (Exception e) {
            logger.error("Ошибка в многопользовательском потоке", e);
        }
    }
}
```

## Fluent API ChatClient

Fluent API `ChatClient` позволяет вам создавать запрос тремя различными способами, используя перегруженный метод `prompt` для инициации fluent API:

- `prompt()`: Этот метод без аргументов позволяет вам начать использовать fluent API, позволяя вам настраивать части запроса, такие как пользователь, система и другие.

- `prompt(Prompt prompt)`: Этот метод принимает аргумент `Prompt`, позволяя вам передать экземпляр `Prompt`, который вы создали с помощью непотоковых API Prompt.

- `prompt(String content)`: Это удобный метод, аналогичный предыдущей перегрузке. Он принимает текстовое содержимое пользователя.

## Ответы ChatClient

API `ChatClient` предлагает несколько способов форматирования ответа от AI модели с использованием fluent API.

### Возврат ChatResponse

Ответ от AI модели представляет собой сложную структуру, определяемую типом `xref:api/chatmodel.adoc#ChatResponse[ChatResponse]`. Он включает метаданные о том, как был сгенерирован ответ, и может также содержать несколько ответов, известных как xref:api/chatmodel.adoc#Generation[Generation]s, каждый из которых имеет свои собственные метаданные. Метаданные включают количество токенов (каждый токен составляет примерно 3/4 слова), использованных для создания ответа. Эта информация важна, поскольку хостинг AI моделей взимает плату на основе количества токенов, использованных за запрос.

Пример возврата объекта `ChatResponse`, который содержит метаданные, показан ниже с вызовом `chatResponse()` после метода `call()`.

```java
ChatResponse chatResponse = chatClient.prompt()
    .user("Расскажи мне шутку")
    .call()
    .chatResponse();
```

### Возврат сущности
```Вы часто хотите вернуть класс сущности, который сопоставлен с возвращаемой строкой `String`. Метод `entity()` предоставляет эту функциональность.

Например, учитывая Java-рекорд:

```java
record ActorFilms(String actor, List<String> movies) {}
```

Вы можете легко сопоставить вывод модели ИИ с этим рекордом, используя метод `entity()`, как показано ниже:

```java
ActorFilms actorFilms = chatClient.prompt()
    .user("Сгенерируйте фильмографию для случайного актера.")
    .call()
    .entity(ActorFilms.class);
```

Существует также перегруженный метод `entity` с сигнатурой `entity(ParameterizedTypeReference<T> type)`, который позволяет вам указывать такие типы, как обобщенные списки:

```java
List<ActorFilms> actorFilms = chatClient.prompt()
    .user("Сгенерируйте фильмографию из 5 фильмов для Тома Хэнкса и Билла Мюррея.")
    .call()
    .entity(new ParameterizedTypeReference<List<ActorFilms>>() {});
```

#### Нативный структурированный вывод

Поскольку все больше моделей ИИ поддерживают нативный структурированный вывод, вы можете воспользоваться этой функцией, используя параметр советника `AdvisorParams.ENABLE_NATIVE_STRUCTURED_OUTPUT` при вызове `ChatClient`. Вы можете использовать метод `defaultAdvisors()` на `ChatClient.Builder`, чтобы установить этот параметр глобально для всех вызовов или установить его для каждого вызова, как показано ниже:

```java
ActorFilms actorFilms = chatClient.prompt()
    .advisors(AdvisorParams.ENABLE_NATIVE_STRUCTURED_OUTPUT)
    .user("Сгенерируйте фильмографию для случайного актера.")
    .call()
    .entity(ActorFilms.class);
```

> **Примечание:** Некоторые модели ИИ, такие как OpenAI, не поддерживают массивы объектов нативно. В таких случаях вы можете использовать стандартное преобразование структурированного вывода Spring AI.

### Потоковые ответы

Метод `stream()` позволяет вам получить асинхронный ответ, как показано ниже:

```java
Flux<String> output = chatClient.prompt()
    .user("Расскажи мне шутку")
    .stream()
    .content();
```

Вы также можете потоково получать `ChatResponse`, используя метод `Flux<ChatResponse> chatResponse()`.

В будущем мы предложим удобный метод, который позволит вам вернуть Java-сущность с реактивным методом `stream()`. В то же время вы должны использовать xref:api/structured-output-converter.adoc#StructuredOutputConverter[Конвертер структурированного вывода], чтобы явно преобразовать агрегированный ответ, как показано ниже. Это также демонстрирует использование параметров во флюентном API, которые будут обсуждены более подробно в следующем разделе документации.

```java
var converter = new BeanOutputConverter<>(new ParameterizedTypeReference<List<ActorsFilms>>() {});

Flux<String> flux = this.chatClient.prompt()
    .user(u -> u.text("""
                        Сгенерируйте фильмографию для случайного актера.
                        {format}
                      """)
            .param("format", this.converter.getFormat()))
    .stream()
    .content();

String content = this.flux.collectList().block().stream().collect(Collectors.joining());

List<ActorsFilms> actorFilms = this.converter.convert(this.content);
```

## Шаблоны запросов`ChatClient` флюентный API позволяет вам предоставлять текст пользователя и системы в виде шаблонов с переменными, которые заменяются во время выполнения.

```java
String answer = ChatClient.create(chatModel).prompt()
    .user(u -> u
            .text("Назовите мне названия 5 фильмов, чей саундтрек был написан {composer}")
            .param("composer", "John Williams"))
    .call()
    .content();
```

Внутренне `ChatClient` использует класс `PromptTemplate` для обработки текста пользователя и системы и замены переменных на значения, предоставленные во время выполнения, полагаясь на реализацию `TemplateRenderer`.
По умолчанию Spring AI использует реализацию `StTemplateRenderer`, которая основана на открытом https://www.stringtemplate.org/[StringTemplate] движке, разработанном Терренсом Парром.

Spring AI также предоставляет `NoOpTemplateRenderer` для случаев, когда обработка шаблонов не требуется.

> **Примечание:** `TemplateRenderer`, настроенный непосредственно на `ChatClient` (через `.templateRenderer()`), применяется только к содержимому подсказки, определенному непосредственно в цепочке сборки `ChatClient` (например, через `.user()`, `.system()`).
Он **не** влияет на шаблоны, используемые внутренне xref:api/retrieval-augmented-generation.adoc#_questionansweradvisor[Советниками], такими как `QuestionAnswerAdvisor`, у которых есть свои механизмы настройки шаблонов (см. xref:api/retrieval-augmented-generation.adoc#_custom_template[Пользовательские шаблоны советников]).

Если вы предпочитаете использовать другой движок шаблонов, вы можете предоставить собственную реализацию интерфейса `TemplateRenderer` непосредственно в `ChatClient`. Вы также можете продолжать использовать стандартный `StTemplateRenderer`, но с пользовательской конфигурацией.

Например, по умолчанию переменные шаблона идентифицируются с помощью синтаксиса `{}`.
Если вы планируете включить JSON в вашу подсказку, вам может понадобиться использовать другой синтаксис, чтобы избежать конфликтов с синтаксисом JSON. Например, вы можете использовать разделители `<` и `>`.

```java
String answer = ChatClient.create(chatModel).prompt()
    .user(u -> u
            .text("Назовите мне названия 5 фильмов, чей саундтрек был написан <composer>")
            .param("composer", "John Williams"))
    .templateRenderer(StTemplateRenderer.builder().startDelimiterToken('<').endDelimiterToken('>').build())
    .call()
    .content();
```

## Возвращаемые значения call()После указания метода `call()` в `ChatClient` есть несколько различных вариантов типа ответа.

- `String content()`: возвращает строковое содержимое ответа.
- `ChatResponse chatResponse()`: возвращает объект `ChatResponse`, который содержит несколько генераций, а также метаданные о ответе, например, сколько токенов было использовано для создания ответа.
- `ChatClientResponse chatClientResponse()`: возвращает объект `ChatClientResponse`, который содержит объект `ChatResponse` и контекст выполнения ChatClient, предоставляя доступ к дополнительным данным, использованным во время выполнения советников (например, соответствующие документы, извлеченные в потоке RAG).
- `entity()` для возврата типа Java.
** `entity(ParameterizedTypeReference<T> type)`: используется для возврата `Collection` типов сущностей.
** `entity(Class<T> type)`: используется для возврата конкретного типа сущности.
** `entity(StructuredOutputConverter<T> structuredOutputConverter)`: используется для указания экземпляра `StructuredOutputConverter` для преобразования `String` в тип сущности.
- `responseEntity()` для возврата как `ChatResponse`, так и типа Java. Это полезно, когда вам нужен доступ как к полному ответу модели ИИ (с метаданными и генерациями), так и к структурированному выходному объекту в одном вызове.
** `responseEntity(Class<T> type)`: используется для возврата `ResponseEntity`, содержащего как полный объект `ChatResponse`, так и конкретный тип сущности.
** `responseEntity(ParameterizedTypeReference<T> type)`: используется для возврата `ResponseEntity`, содержащего как полный объект `ChatResponse`, так и `Collection` типов сущностей.
** `responseEntity(StructuredOutputConverter<T> structuredOutputConverter)`: используется для возврата `ResponseEntity`, содержащего как полный объект `ChatResponse`, так и сущность, преобразованную с использованием указанного `StructuredOutputConverter`.

Вы также можете вызвать метод `stream()` вместо `call()`.

> **Примечание:** Вызов метода `call()` на самом деле не запускает выполнение модели ИИ. Вместо этого он просто инструктирует Spring AI, использовать синхронные или потоковые вызовы.
Фактический вызов модели ИИ происходит, когда вызываются такие методы, как `content()`, `chatResponse()` и `responseEntity()`.

## Возвращаемые значения stream()

После указания метода `stream()` в `ChatClient` есть несколько вариантов типа ответа:

- `Flux<String> content()`: возвращает `Flux` строки, генерируемой моделью ИИ.
- `Flux<ChatResponse> chatResponse()`: возвращает `Flux` объекта `ChatResponse`, который содержит дополнительные метаданные о ответе.
- `Flux<ChatClientResponse> chatClientResponse()`: возвращает `Flux` объекта `ChatClientResponse`, который содержит объект `ChatResponse` и контекст выполнения ChatClient, предоставляя доступ к дополнительным данным, использованным во время выполнения советников (например, соответствующие документы, извлеченные в потоке RAG).

## Метаданные сообщения

ChatClient поддерживает добавление метаданных как к пользовательским, так и к системным сообщениям.
Метаданные предоставляют дополнительный контекст и информацию о сообщениях, которые могут быть использованы моделью ИИ или для последующей обработки.

### Добавление метаданных к пользовательским сообщениям

Вы можете добавить метаданные к пользовательским сообщениям, используя методы `metadata()`:

```java
// Добавление отдельных пар ключ-значение метаданных
String response = chatClient.prompt()
    .user(u -> u.text("Какова погода?")
        .metadata("messageId", "msg-123")
        .metadata("userId", "user-456")
        .metadata("priority", "high"))
    .call()
    .content();

// Добавление нескольких записей метаданных сразу
Map<String, Object> userMetadata = Map.of(
    "messageId", "msg-123",
    "userId", "user-456",
    "timestamp", System.currentTimeMillis()
);

String response = chatClient.prompt()
    .user(u -> u.text("Какова погода?")
        .metadata(userMetadata))
    .call()
    .content();
```

### Добавление метаданных к системным сообщениям```markdown
Аналогично, вы можете добавить метаданные к системным сообщениям:

```java
// Добавление метаданных к системным сообщениям
String response = chatClient.prompt()
    .system(s -> s.text("Вы полезный помощник.")
        .metadata("version", "1.0")
        .metadata("model", "gpt-4"))
    .user("Расскажи мне шутку")
    .call()
    .content();
```

### Поддержка метаданных по умолчанию

Вы также можете настроить метаданные по умолчанию на уровне сборщика ChatClient:

```java
@Configuration
class Config {
    @Bean
    ChatClient chatClient(ChatClient.Builder builder) {
        return builder
            .defaultSystem(s -> s.text("Вы полезный помощник")
                .metadata("assistantType", "general")
                .metadata("version", "1.0"))
            .defaultUser(u -> u.text("Контекст пользователя по умолчанию")
                .metadata("sessionId", "default-session"))
            .build();
    }
}
```

### Валидация метаданных

ChatClient проверяет метаданные для обеспечения целостности данных:

- Ключи метаданных не могут быть null или пустыми
- Значения метаданных не могут быть null
- При передаче Map ни ключи, ни значения не могут содержать элементы null

```java
// Это вызовет IllegalArgumentException
chatClient.prompt()
    .user(u -> u.text("Привет")
        .metadata(null, "value"))  // Неверно: ключ null
    .call()
    .content();

// Это также вызовет IllegalArgumentException
chatClient.prompt()
    .user(u -> u.text("Привет")
        .metadata("key", null))    // Неверно: значение null
    .call()
    .content();
```

### Доступ к метаданным

Метаданные включены в сгенерированные объекты UserMessage и SystemMessage и могут быть доступны через метод `getMetadata()` сообщения. Это особенно полезно при обработке сообщений в советниках или при изучении истории беседы.

## Использование значений по умолчанию

Создание `ChatClient` с текстом системы по умолчанию в классе `@Configuration` упрощает код во время выполнения. Установив значения по умолчанию, вам нужно только указать текст пользователя при вызове `ChatClient`, что исключает необходимость устанавливать текст системы для каждого запроса в вашем коде во время выполнения.

### Текст системы по умолчанию

В следующем примере мы настроим текст системы так, чтобы он всегда отвечал голосом пирата. Чтобы избежать повторения текста системы в коде во время выполнения, мы создадим экземпляр `ChatClient` в классе `@Configuration`.

```java
@Configuration
class Config {

    @Bean
    ChatClient chatClient(ChatClient.Builder builder) {
        return builder.defaultSystem("Вы дружелюбный чат-бот, который отвечает на вопросы голосом пирата")
                .build();
    }

}
```

и `@RestController`, чтобы вызвать его:

```java
@RestController
class AIController {

	private final ChatClient chatClient;

	AIController(ChatClient chatClient) {
		this.chatClient = chatClient;
	}

	@GetMapping("/ai/simple")
	public Map<String, String> completion(@RequestParam(value = "message", defaultValue = "Расскажи мне шутку") String message) {
		return Map.of("completion", this.chatClient.prompt().user(message).call().content());
	}
}
```

При вызове конечной точки приложения через curl результат будет:

```bash
❯ curl localhost:8080/ai/simple
{"completion":"Почему пират пошел в комедийный клуб? Чтобы услышать несколько шуток с арр-рейтингом! Арр, матрос!"}
```

### Текст системы по умолчанию с параметрами
```В следующем примере мы будем использовать заполнитель в системном тексте, чтобы указать голос завершения во время выполнения, а не на этапе проектирования.

```java
@Configuration
class Config {

    @Bean
    ChatClient chatClient(ChatClient.Builder builder) {
        return builder.defaultSystem("You are a friendly chat bot that answers question in the voice of a {voice}")
                .build();
    }

}
```

```java
@RestController
class AIController {
	private final ChatClient chatClient;

	AIController(ChatClient chatClient) {
		this.chatClient = chatClient;
	}

	@GetMapping("/ai")
	Map<String, String> completion(@RequestParam(value = "message", defaultValue = "Tell me a joke") String message, String voice) {
		return Map.of("completion",
				this.chatClient.prompt()
						.system(sp -> sp.param("voice", voice))
						.user(message)
						.call()
						.content());
	}

}
```

При вызове конечной точки приложения через httpie результат будет следующим:

[source.bash]
```
http localhost:8080/ai voice=='Robert DeNiro'
{
    "completion": "You talkin' to me? Okay, here's a joke for ya: Why couldn't the bicycle stand up by itself? Because it was two tired! Classic, right?"
}
```

### Другие значения по умолчанию

На уровне `ChatClient.Builder` вы можете указать конфигурацию подсказки по умолчанию.

- `defaultOptions(ChatOptions chatOptions)`: Передайте либо переносимые параметры, определенные в классе `ChatOptions`, либо специфические для модели параметры, такие как те, что в `OpenAiChatOptions`.
Для получения дополнительной информации о специфических для модели реализациях `ChatOptions` обратитесь к JavaDocs.

- `defaultFunction(String name, String description, java.util.function.Function<I, O> function)`: `name` используется для ссылки на функцию в пользовательском тексте.
`description` объясняет цель функции и помогает модели ИИ выбрать правильную функцию для точного ответа.
Аргумент `function` — это экземпляр функции Java, которую модель будет выполнять при необходимости.

- `defaultFunctions(String... functionNames)`: Имена бинов `java.util.Function`, определенные в контексте приложения.

- `defaultUser(String text)`, `defaultUser(Resource text)`, `defaultUser(Consumer<UserSpec> userSpecConsumer)`: Эти методы позволяют вам определить текст пользователя.
`Consumer<UserSpec>` позволяет использовать лямбду для указания текста пользователя и любых параметров по умолчанию.

- `defaultAdvisors(Advisor... advisor)`: Советники позволяют изменять данные, используемые для создания `Prompt`.
Реализация `QuestionAnswerAdvisor` позволяет использовать шаблон `Retrieval Augmented Generation`, добавляя к подсказке информацию о контексте, связанную с текстом пользователя.

- `defaultAdvisors(Consumer<AdvisorSpec> advisorSpecConsumer)`: Этот метод позволяет вам определить `Consumer` для настройки нескольких советников с использованием `AdvisorSpec`. Советники могут изменять данные, используемые для создания окончательной `Prompt`.
`Consumer<AdvisorSpec>` позволяет вам указать лямбду для добавления советников, таких как `QuestionAnswerAdvisor`, который поддерживает `Retrieval Augmented Generation`, добавляя к подсказке соответствующую информацию о контексте на основе текста пользователя.

Вы можете переопределить эти значения по умолчанию во время выполнения, используя соответствующие методы без префикса `default`.

- `options(ChatOptions chatOptions)`

- `function(String name, String description,
java.util.function.Function<I, O> function)`

- `functions(String... functionNames)`

- `user(String text)`, `user(Resource text)`, `user(Consumer<UserSpec> userSpecConsumer)`

- `advisors(Advisor... advisor)`

- `advisors(Consumer<AdvisorSpec> advisorSpecConsumer)`

## СоветникиThe xref:api/advisors.adoc[Advisors API] предоставляет гибкий и мощный способ перехвата, изменения и улучшения взаимодействий на основе ИИ в ваших приложениях Spring.

Распространённым подходом при вызове модели ИИ с текстом пользователя является добавление или дополнение запроса контекстными данными.

Эти контекстные данные могут быть разных типов. Распространённые типы включают:

- **Ваши собственные данные**: Это данные, на которых модель ИИ не была обучена. Даже если модель видела похожие данные, добавленные контекстные данные имеют приоритет при генерации ответа.

- **История общения**: API модели чата является статeless. Если вы скажете модели ИИ ваше имя, она не запомнит его в последующих взаимодействиях. История общения должна отправляться с каждым запросом, чтобы гарантировать, что предыдущие взаимодействия учитываются при генерации ответа.

### Конфигурация советников в ChatClient

Fluent API ChatClient предоставляет интерфейс `AdvisorSpec` для настройки советников. Этот интерфейс предлагает методы для добавления параметров, установки нескольких параметров одновременно и добавления одного или нескольких советников в цепочку.

```java
interface AdvisorSpec {
    AdvisorSpec param(String k, Object v);
    AdvisorSpec params(Map<String, Object> p);
    AdvisorSpec advisors(Advisor... advisors);
    AdvisorSpec advisors(List<Advisor> advisors);
}
```

> **Важно:** Порядок, в котором советники добавляются в цепочку, имеет решающее значение, так как он определяет последовательность их выполнения. Каждый советник изменяет запрос или контекст определённым образом, и изменения, внесённые одним советником, передаются следующему в цепочке.

```java
ChatClient.builder(chatModel)
    .build()
    .prompt()
    .advisors(
        MessageChatMemoryAdvisor.builder(chatMemory).build(),
        QuestionAnswerAdvisor.builder(vectorStore).build()
    )
    .user(userText)
    .call()
    .content();
```

В этой конфигурации `MessageChatMemoryAdvisor` будет выполнен первым, добавляя историю общения к запросу. Затем `QuestionAnswerAdvisor` выполнит поиск на основе вопроса пользователя и добавленной истории общения, потенциально предоставляя более релевантные результаты.

xref:ROOT:api/retrieval-augmented-generation.adoc#_questionansweradvisor[Узнайте о Question Answer Advisor]

### Генерация с использованием извлечения

Смотрите руководство xref:ROOT:api/retrieval-augmented-generation.adoc[Генерация с использованием извлечения].

### Логирование

`SimpleLoggerAdvisor` — это советник, который логирует данные `request` и `response` `ChatClient`. Это может быть полезно для отладки и мониторинга ваших взаимодействий с ИИ.

> **Совет:** Spring AI поддерживает наблюдаемость для взаимодействий с LLM и векторным хранилищем. Смотрите руководство xref:observability/index.adoc[Наблюдаемость] для получения дополнительной информации.

Чтобы включить логирование, добавьте `SimpleLoggerAdvisor` в цепочку советников при создании вашего ChatClient. Рекомендуется добавлять его ближе к концу цепочки:

```java
ChatResponse response = ChatClient.create(chatModel).prompt()
        .advisors(new SimpleLoggerAdvisor())
        .user("Расскажи мне шутку?")
        .call()
        .chatResponse();
```

Чтобы увидеть логи, установите уровень логирования для пакета советников на `DEBUG`:

```
logging.level.org.springframework.ai.chat.client.advisor=DEBUG
```

Добавьте это в ваш файл `application.properties` или `application.yaml`.

Вы можете настроить, какие данные из `AdvisedRequest` и `ChatResponse` будут логироваться, используя следующий конструктор:

```java
SimpleLoggerAdvisor(
    Function<ChatClientRequest, String> requestToString,
    Function<ChatResponse, String> responseToString,
    int order
)
```

Пример использования:

```java
SimpleLoggerAdvisor customLogger = new SimpleLoggerAdvisor(
    request -> "Пользовательский запрос: " + request.prompt().getUserMessage(),
    response -> "Пользовательский ответ: " + response.getResult(),
    0
);
```

Это позволяет вам адаптировать логируемую информацию под ваши конкретные нужды.

> **Совет:** Будьте осторожны с логированием конфиденциальной информации в производственных средах.

## Память чатаИнтерфейс `ChatMemory` представляет собой хранилище для памяти чата. Он предоставляет методы для добавления сообщений в разговор, извлечения сообщений из разговора и очистки истории разговоров.

В настоящее время существует одна встроенная реализация: `MessageWindowChatMemory`.

`MessageWindowChatMemory` — это реализация памяти чата, которая поддерживает окно сообщений до указанного максимального размера (по умолчанию: 20 сообщений). Когда количество сообщений превышает этот лимит, более старые сообщения удаляются, но системные сообщения сохраняются. Если добавляется новое системное сообщение, все предыдущие системные сообщения удаляются из памяти. Это гарантирует, что самый последний контекст всегда доступен для разговора, при этом использование памяти остается ограниченным.

`MessageWindowChatMemory` поддерживается абстракцией `ChatMemoryRepository`, которая предоставляет реализации хранения для памяти чата. Доступно несколько реализаций, включая `InMemoryChatMemoryRepository`, `JdbcChatMemoryRepository`, `CassandraChatMemoryRepository`, `Neo4jChatMemoryRepository`, `CosmosDBChatMemoryRepository`, `MongoChatMemoryRepository` и `RedisChatMemoryRepository`.

Для получения дополнительных сведений и примеров использования смотрите документацию xref:api/chat-memory.adoc[Chat Memory].

## Примечания по реализации

Совместное использование императивных и реактивных моделей программирования в `ChatClient` является уникальным аспектом API. Часто приложение будет либо реактивным, либо императивным, но не обоими.

- При настройке взаимодействия HTTP-клиента реализации Model необходимо настроить как RestClient, так и WebClient.

[ВАЖНО]
====
Из-за ошибки в Spring Boot 3.4 свойство "spring.http.client.factory=jdk" должно быть установлено. В противном случае оно по умолчанию устанавливается на "reactor", что нарушает определенные рабочие процессы ИИ, такие как ImageModel.
====

- Потоковая передача поддерживается только через реактивный стек. Императивные приложения должны включать реактивный стек по этой причине (например, spring-boot-starter-webflux).
- Непотоковая передача поддерживается только через сервлетный стек. Реактивные приложения должны включать сервлетный стек по этой причине (например, spring-boot-starter-web) и ожидать, что некоторые вызовы будут блокирующими.
- Вызов инструментов является императивным, что приводит к блокирующим рабочим процессам. Это также приводит к частичным/прерванным наблюдениям Micrometer (например, спаны ChatClient и спаны вызова инструментов не связаны, и первый остается неполным по этой причине).
- Встроенные советники выполняют блокирующие операции для стандартных вызовов и неблокирующие операции для потоковых вызовов. Планировщик Reactor, используемый для потоковых вызовов советников, может быть настроен через Builder в каждом классе советника.
