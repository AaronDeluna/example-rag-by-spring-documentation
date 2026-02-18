# Chat Model API

API Chat Model предоставляет разработчикам возможность интегрировать возможности завершения чата на основе ИИ в свои приложения. Он использует предварительно обученные языковые модели, такие как GPT (Generative Pre-trained Transformer), для генерации ответов, похожих на человеческие, на пользовательские запросы на естественном языке.

API обычно работает путем отправки подсказки или частичного разговора в ИИ-модель, которая затем генерирует завершение или продолжение разговора на основе своих обучающих данных и понимания паттернов естественного языка. Завершенный ответ затем возвращается в приложение, которое может представить его пользователю или использовать для дальнейшей обработки.

`Spring AI Chat Model API` разработан как простой и переносимый интерфейс для взаимодействия с различными xref:concepts.adoc#_models[AI Models], позволяя разработчикам переключаться между различными моделями с минимальными изменениями в коде. Этот дизайн соответствует философии модульности и взаимозаменяемости Spring.

Также с помощью вспомогательных классов, таких как `Prompt` для инкапсуляции ввода и `ChatResponse` для обработки вывода, API Chat Model унифицирует взаимодействие с AI Models. Он управляет сложностью подготовки запросов и разбора ответов, предлагая прямое и упрощенное взаимодействие с API.

Вы можете узнать больше о доступных реализациях в разделе xref:api/chatmodel.adoc#_available_implementations[Available Implementations], а также ознакомиться с подробным сравнением в разделе xref:api/chat/comparison.adoc[Chat Models Comparison].

## Обзор API

Этот раздел предоставляет руководство по интерфейсу Spring AI Chat Model API и связанным классам.

### ChatModel

Вот определение интерфейса [ChatModel](https://github.com/spring-projects/spring-ai/blob/main/spring-ai-model/src/main/java/org/springframework/ai/chat/model/ChatModel.java):

```java
public interface ChatModel extends Model<Prompt, ChatResponse>, StreamingChatModel {

	default String call(String message) {...}

    @Override
	ChatResponse call(Prompt prompt);
}
```

Метод `call()` с параметром `String` упрощает начальное использование, избегая сложностей более сложных классов `Prompt` и `ChatResponse`. В реальных приложениях чаще используется метод `call()`, который принимает экземпляр `Prompt` и возвращает `ChatResponse`.

### StreamingChatModel

Вот определение интерфейса [StreamingChatModel](https://github.com/spring-projects/spring-ai/blob/main/spring-ai-model/src/main/java/org/springframework/ai/chat/model/StreamingChatModel.java):

```java
public interface StreamingChatModel extends StreamingModel<Prompt, ChatResponse> {

    default Flux<String> stream(String message) {...}

    @Override
	Flux<ChatResponse> stream(Prompt prompt);
}
```

Метод `stream()` принимает параметр `String` или `Prompt`, аналогично `ChatModel`, но он передает ответы с использованием реактивного Flux API.

### Prompt

Класс https://github.com/spring-projects/spring-ai/blob/main/spring-ai-client-chat/src/main/java/org/springframework/ai/chat/prompt/Prompt.java[Prompt] является `ModelRequest`, который инкапсулирует список объектов https://github.com/spring-projects/spring-ai/blob/main/spring-ai-model/src/main/java/org/springframework/ai/chat/messages/Message.java[Message] и необязательные параметры запроса модели. Следующий список показывает сокращенную версию класса `Prompt`, исключая конструкторы и другие утилитарные методы:

```java
public class Prompt implements ModelRequest<List<Message>> {

    private final List<Message> messages;

    private ChatOptions modelOptions;

	@Override
	public ChatOptions getOptions() {...}

	@Override
	public List<Message> getInstructions() {...}

    // constructors and utility methods omitted
}
```

#### Message

Интерфейс `Message` инкапсулирует текстовое содержимое `Prompt`, коллекцию атрибутов метаданных и категорию, известную как `MessageType`.

Интерфейс определен следующим образом:

```java
public interface Content {

	String getText();

	Map<String, Object> getMetadata();
}

public interface Message extends Content {

	MessageType getMessageType();
}
```

Мультимодальные типы сообщений также реализуют интерфейс `MediaContent`, предоставляя список объектов `Media`.

```java
public interface MediaContent extends Content {

	Collection<Media> getMedia();

}
```

Интерфейс `Message` имеет различные реализации, которые соответствуют категориям сообщений, которые может обрабатывать ИИ-модель:

![Spring AI Message API, width=800, align="center"](spring-ai-message-api.jpg)

Конечная точка завершения чата различает категории сообщений на основе разговорных ролей, эффективно сопоставленных с `MessageType`.

Например, OpenAI распознает категории сообщений для различных разговорных ролей, таких как `system`, `user`, `function` или `assistant`.

Хотя термин `MessageType` может подразумевать конкретный формат сообщения, в этом контексте он фактически обозначает роль, которую сообщение играет в диалоге.

Для ИИ-моделей, которые не используют конкретные роли, реализация `UserMessage` действует как стандартная категория, обычно представляющая запросы или инструкции, созданные пользователем. Чтобы понять практическое применение и взаимосвязь между `Prompt` и `Message`, особенно в контексте этих ролей или категорий сообщений, смотрите подробные объяснения в разделе xref:api/prompt.adoc[Prompts].

#### Chat Options

Представляет параметры, которые могут быть переданы ИИ-модели. Класс `ChatOptions` является подклассом `ModelOptions` и используется для определения нескольких переносимых параметров, которые могут быть переданы ИИ-модели. Класс `ChatOptions` определен следующим образом:

```java
public interface ChatOptions extends ModelOptions {

	String getModel();
	Float getFrequencyPenalty();
	Integer getMaxTokens();
	Float getPresencePenalty();
	List<String> getStopSequences();
	Float getTemperature();
	Integer getTopK();
	Float getTopP();
	ChatOptions copy();

}
```

Кроме того, каждая конкретная реализация ChatModel/StreamingChatModel может иметь свои собственные параметры, которые могут быть переданы ИИ-модели. Например, модель OpenAI Chat Completion имеет свои собственные параметры, такие как `logitBias`, `seed` и `user`.

Это мощная функция, которая позволяет разработчикам использовать специфические для модели параметры при запуске приложения, а затем переопределять их во время выполнения с помощью запроса `Prompt`.

Spring AI предоставляет сложную систему для настройки и использования Chat Models. Она позволяет устанавливать параметры по умолчанию при запуске, а также предоставляет гибкость для переопределения этих настроек на уровне каждого запроса. Этот подход позволяет разработчикам легко работать с различными ИИ-моделями и настраивать параметры по мере необходимости, все в рамках единого интерфейса, предоставляемого фреймворком Spring AI.

Следующая диаграмма потока иллюстрирует, как Spring AI обрабатывает конфигурацию и выполнение Chat Models, сочетая параметры запуска и времени выполнения:

![align="center", width="800px"](chat-options-flow.jpg)

1. Конфигурация при запуске - ChatModel/StreamingChatModel инициализируется с "Параметрами запуска" Chat Options. Эти параметры устанавливаются во время инициализации ChatModel и предназначены для предоставления конфигураций по умолчанию.
2. Конфигурация во время выполнения - Для каждого запроса Prompt может содержать параметры Runtime Chat Options: они могут переопределять параметры запуска.
3. Процесс объединения параметров - Шаг "Объединить параметры" сочетает параметры запуска и времени выполнения. Если предоставлены параметры времени выполнения, они имеют приоритет над параметрами запуска.
4. Обработка ввода - Шаг "Преобразовать ввод" преобразует входные инструкции в нативные, специфические для модели форматы.
5. Обработка вывода - Шаг "Преобразовать вывод" преобразует ответ модели в стандартизированный формат `ChatResponse`.

Разделение параметров запуска и времени выполнения позволяет как глобальные конфигурации, так и настройки, специфичные для запроса.

### ChatResponse

Структура класса `ChatResponse` выглядит следующим образом:

```java
public class ChatResponse implements ModelResponse<Generation> {

    private final ChatResponseMetadata chatResponseMetadata;
	private final List<Generation> generations;

	@Override
	public ChatResponseMetadata getMetadata() {...}

    @Override
	public List<Generation> getResults() {...}

    // other methods omitted
}
```

Класс https://github.com/spring-projects/spring-ai/blob/main/spring-ai-model/src/main/java/org/springframework/ai/chat/model/ChatResponse.java[ChatResponse] хранит вывод ИИ-модели, при этом каждый экземпляр `Generation` содержит один из потенциально нескольких выводов, полученных в результате одного запроса.

Класс `ChatResponse` также содержит `ChatResponseMetadata`, метаданные о ответе ИИ-модели.

### Generation

Наконец, класс https://github.com/spring-projects/spring-ai/blob/main/spring-ai-model/src/main/java/org/springframework/ai/chat/model/Generation.java[Generation] наследуется от `ModelResult`, чтобы представлять вывод модели (сообщение помощника) и связанные метаданные:

```java
public class Generation implements ModelResult<AssistantMessage> {

	private final AssistantMessage assistantMessage;
	private ChatGenerationMetadata chatGenerationMetadata;

	@Override
	public AssistantMessage getOutput() {...}

	@Override
	public ChatGenerationMetadata getMetadata() {...}

    // other methods omitted
}
```

## Доступные реализации

Эта диаграмма иллюстрирует, что унифицированные интерфейсы `ChatModel` и `StreamingChatModel` используются для взаимодействия с различными ИИ-моделями чата от разных поставщиков, что позволяет легко интегрировать и переключаться между различными ИИ-сервисами, сохраняя при этом единый API для клиентского приложения.

![align="center", width="1000px"](spring-ai-chat-completions-clients.jpg)

- xref:api/chat/openai-chat.adoc[OpenAI Chat Completion] (поддержка потоковой передачи, мультимодальности и вызова функций)
- xref:api/chat/azure-openai-chat.adoc[Microsoft Azure Open AI Chat Completion] (поддержка потоковой передачи и вызова функций)
- xref:api/chat/ollama-chat.adoc[Ollama Chat Completion] (поддержка потоковой передачи, мультимодальности и вызова функций)
- xref:api/chat/huggingface.adoc[Hugging Face Chat Completion] (без поддержки потоковой передачи)
- xref:api/chat/vertexai-gemini-chat.adoc[Google Vertex AI Gemini Chat Completion] (поддержка потоковой передачи, мультимодальности и вызова функций)
- xref:api/bedrock.adoc[Amazon Bedrock]
- xref:api/chat/mistralai-chat.adoc[Mistral AI Chat Completion] (поддержка потоковой передачи и вызова функций)
- xref:api/chat/anthropic-chat.adoc[Anthropic Chat Completion] (поддержка потоковой передачи и вызова функций)

> **Совет:** Найдите подробное сравнение доступных моделей чата в разделе xref:api/chat/comparison.adoc[Chat Models Comparison].

## Chat Model API

Spring AI Chat Model API построен на основе `Generic Model API` Spring, предоставляя специфические для чата абстракции и реализации. Это позволяет легко интегрировать и переключаться между различными ИИ-сервисами, сохраняя при этом единый API для клиентского приложения. Следующая диаграмма классов иллюстрирует основные классы и интерфейсы Spring AI Chat Model API.

![align="center", width="1000px"](spring-ai-chat-api.jpg)

// == Лучшие практики
//
// TBD
//
// == Устранение неполадок
//
// TBD

// == Связанные ресурсы
//
// TBD
