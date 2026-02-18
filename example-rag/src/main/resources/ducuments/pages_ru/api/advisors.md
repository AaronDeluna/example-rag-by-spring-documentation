```markdown

# Advisors API

API Advisors Spring AI предоставляет гибкий и мощный способ перехвата, модификации и улучшения взаимодействий на основе ИИ в ваших приложениях Spring. 
Используя API Advisors, разработчики могут создавать более сложные, многоразовые и поддерживаемые компоненты ИИ.

Ключевые преимущества включают в себя инкапсуляцию повторяющихся паттернов генеративного ИИ, преобразование данных, отправляемых и получаемых от больших языковых моделей (LLM), а также обеспечение портативности между различными моделями и случаями использования.

Вы можете настроить существующие советники, используя xref:api/chatclient.adoc#_advisor_configuration_in_chatclient[API ChatClient], как показано в следующем примере:

```java

ChatMemory chatMemory = ... // Инициализируйте хранилище памяти чата
VectorStore vectorStore = ... // Инициализируйте векторное хранилище

var chatClient = ChatClient.builder(chatModel)
    .defaultAdvisors(
        MessageChatMemoryAdvisor.builder(chatMemory).build(), // советник памяти чата
        QuestionAnswerAdvisor.builder(vectorStore).build()    // советник RAG
    )
    .build();

var conversationId = "678";

String response = this.chatClient.prompt()
    // Установите параметры советника во время выполнения	
    .advisors(advisor -> advisor.param(ChatMemory.CONVERSATION_ID, conversationId))
    .user(userText)
    .call()
	.content();
```

Рекомендуется регистрировать советников во время сборки, используя метод `defaultAdvisors()` строителя.

Советники также участвуют в стеке наблюдаемости, поэтому вы можете просматривать метрики и трассировки, связанные с их выполнением.

- xref:ROOT:api/retrieval-augmented-generation.adoc#_questionansweradvisor[Узнайте о советнике по вопросам и ответам]
- xref:ROOT:api/chat-memory.adoc#_memory_in_chat_client[Узнайте о советнике памяти чата]

## Основные компоненты

API состоит из `CallAdvisor` и `CallAdvisorChain` для нестриминговых сценариев, а также `StreamAdvisor` и `StreamAdvisorChain` для стриминговых сценариев. 
Он также включает `ChatClientRequest`, чтобы представить незапечатанный запрос Prompt, и `ChatClientResponse` для ответа Chat Completion. Оба содержат `advise-context` для обмена состоянием между цепочкой советников.

![Классы API Advisors, width=600, align="center"](advisors-api-classes.jpg)

Методы `adviseCall()` и `adviseStream()` являются ключевыми методами советников, которые обычно выполняют такие действия, как изучение незапечатанных данных Prompt, настройка и дополнение данных Prompt, вызов следующей сущности в цепочке советников, при необходимости блокируя запрос, изучение ответа на завершение чата и выбрасывание исключений для указания ошибок обработки.

Кроме того, метод `getOrder()` определяет порядок советника в цепочке, в то время как `getName()` предоставляет уникальное имя советника.

Цепочка советников, созданная фреймворком Spring AI, позволяет последовательный вызов нескольких советников, упорядоченных по их значениям `getOrder()`. 
Советники с более низкими значениями выполняются первыми. 
Последний советник, добавленный автоматически, отправляет запрос в LLM.

Следующая диаграмма потока иллюстрирует взаимодействие между цепочкой советников и моделью чата:

![Поток API Advisors, width=400, align="center"](advisors-flow.jpg)

1. Фреймворк Spring AI создает `ChatClientRequest` из пользовательского `Prompt` вместе с пустым объектом `context` советника.
1. Каждый советник в цепочке обрабатывает запрос, потенциально модифицируя его. В противном случае он может выбрать блокировку запроса, не вызывая следующую сущность. В последнем случае советник отвечает за заполнение ответа.
1. Последний советник, предоставленный фреймворком, отправляет запрос в `Chat Model`.
1. Ответ модели чата затем передается обратно через цепочку советников и преобразуется в `ChatClientResponse`. Позже включает общий экземпляр `context` советника.
1. Каждый советник может обрабатывать или модифицировать ответ.
1. Финальный `ChatClientResponse` возвращается клиенту путем извлечения `ChatCompletion`.

### Порядок советников
Порядок выполнения советников в цепочке определяется методом `getOrder()`. Ключевые моменты для понимания:

- Советники с более низкими значениями порядка выполняются первыми.
- Цепочка советников работает как стек:
** Первый советник в цепочке является первым, кто обрабатывает запрос.
** Он также является последним, кто обрабатывает ответ.
- Чтобы контролировать порядок выполнения:
** Установите порядок близким к `Ordered.HIGHEST_PRECEDENCE`, чтобы гарантировать, что советник будет выполнен первым в цепочке (первым для обработки запроса, последним для обработки ответа).
** Установите порядок близким к `Ordered.LOWEST_PRECEDENCE`, чтобы гарантировать, что советник будет выполнен последним в цепочке (последним для обработки запроса, первым для обработки ответа).
- Более высокие значения интерпретируются как более низкий приоритет.
- Если несколько советников имеют одинаковое значение порядка, их порядок выполнения не гарантируется.

[NOTE]
====
Сложное противоречие между порядком и последовательностью выполнения связано со стекоподобной природой цепочки советников:

- Советник с наивысшим приоритетом (наименьшее значение порядка) добавляется на вершину стека.
- Он будет первым, кто обработает запрос, когда стек разворачивается.
- Он будет последним, кто обработает ответ, когда стек сворачивается.

====

В качестве напоминания, вот семантика интерфейса Spring `Ordered`:

```java
public interface Ordered {

    /**
     * Константа для значения наивысшего приоритета.
     * @see java.lang.Integer#MIN_VALUE
     */
    int HIGHEST_PRECEDENCE = Integer.MIN_VALUE;

    /**
     * Константа для значения наименьшего приоритета.
     * @see java.lang.Integer#MAX_VALUE
     */
    int LOWEST_PRECEDENCE = Integer.MAX_VALUE;

    /**
     * Получить значение порядка этого объекта.
     * <p>Более высокие значения интерпретируются как более низкий приоритет. В результате,
     * объект с наименьшим значением имеет наивысший приоритет (в некотором роде
     * аналогично значениям {@code load-on-startup} в Servlet).
     * <p>Одни и те же значения порядка приведут к произвольным позициям сортировки для
     * затронутых объектов.
     * @return значение порядка
     * @see #HIGHEST_PRECEDENCE
     * @see #LOWEST_PRECEDENCE
     */
    int getOrder();
}
```


[TIP]
====
Для случаев использования, которые должны быть первыми в цепочке как на входной, так и на выходной стороне:

1. Используйте отдельные советники для каждой стороны.
2. Настройте их с разными значениями порядка.
3. Используйте контекст советника для обмена состоянием между ними.
====

## Обзор API

Основные интерфейсы советников находятся в пакете `org.springframework.ai.chat.client.advisor.api`. Вот ключевые интерфейсы, с которыми вы столкнетесь при создании собственного советника:

```java
public interface Advisor extends Ordered {

	String getName();

}
```

Два подинтерфейса для синхронных и реактивных советников:

```java
public interface CallAdvisor extends Advisor {

	ChatClientResponse adviseCall(
		ChatClientRequest chatClientRequest, CallAdvisorChain callAdvisorChain);

}

```

и

```java
public interface StreamAdvisor extends Advisor {

	Flux<ChatClientResponse> adviseStream(
		ChatClientRequest chatClientRequest, StreamAdvisorChain streamAdvisorChain);

}
```

Чтобы продолжить цепочку советов, используйте `CallAdvisorChain` и `StreamAdvisorChain` в вашей реализации совета:

Интерфейсы:

```java
public interface CallAdvisorChain extends AdvisorChain {

	/**
	 * Вызывает следующий {@link CallAdvisor} в {@link CallAdvisorChain} с данным
	 * запросом.
	 */
	ChatClientResponse nextCall(ChatClientRequest chatClientRequest);

	/**
	 * Возвращает список всех экземпляров {@link CallAdvisor}, включенных в эту цепочку на
	 * момент ее создания.
	 */
	List<CallAdvisor> getCallAdvisors();

}
```

и

```java
public interface StreamAdvisorChain extends AdvisorChain {

	/**
	 * Вызывает следующий {@link StreamAdvisor} в {@link StreamAdvisorChain} с данным
	 * запросом.
	 */
	Flux<ChatClientResponse> nextStream(ChatClientRequest chatClientRequest);

	/**
	 * Возвращает список всех экземпляров {@link StreamAdvisor}, включенных в эту цепочку
	 * на момент ее создания.
	 */
	List<StreamAdvisor> getStreamAdvisors();

}
```

## Реализация советника

Чтобы создать советника, реализуйте либо `CallAdvisor`, либо `StreamAdvisor` (или оба). Ключевой метод для реализации — `nextCall()` для нестриминговых или `nextStream()` для стриминговых советников.

### Примеры

Мы предоставим несколько практических примеров, чтобы проиллюстрировать, как реализовать советников для наблюдения и дополнения случаев использования.

#### Советник логирования

Мы можем реализовать простой советник логирования, который регистрирует `ChatClientRequest` до и `ChatClientResponse` после вызова следующего советника в цепочке.
Обратите внимание, что советник только наблюдает за запросом и ответом и не модифицирует их.
Эта реализация поддерживает как нестриминговые, так и стриминговые сценарии.

```java
public class SimpleLoggerAdvisor implements CallAdvisor, StreamAdvisor {

	private static final Logger logger = LoggerFactory.getLogger(SimpleLoggerAdvisor.class);

	@Override
	public String getName() { // <1>
		return this.getClass().getSimpleName();
	}

	@Override
	public int getOrder() { // <2>
		return 0; 
	}


	@Override
	public ChatClientResponse adviseCall(ChatClientRequest chatClientRequest, CallAdvisorChain callAdvisorChain) {
		logRequest(chatClientRequest);

		ChatClientResponse chatClientResponse = callAdvisorChain.nextCall(chatClientRequest);

		logResponse(chatClientResponse);

		return chatClientResponse;
	}

	@Override
	public Flux<ChatClientResponse> adviseStream(ChatClientRequest chatClientRequest,
			StreamAdvisorChain streamAdvisorChain) {
		logRequest(chatClientRequest);

		Flux<ChatClientResponse> chatClientResponses = streamAdvisorChain.nextStream(chatClientRequest);

		return new ChatClientMessageAggregator().aggregateChatClientResponse(chatClientResponses, this::logResponse); // <3>
	}

	private void logRequest(ChatClientRequest request) {
		logger.debug("request: {}", request);
	}

	private void logResponse(ChatClientResponse chatClientResponse) {
		logger.debug("response: {}", chatClientResponse);
	}

}
```
<1> Предоставляет уникальное имя для советника.
<2> Вы можете контролировать порядок выполнения, установив значение порядка. Более низкие значения выполняются первыми.
<3> `MessageAggregator` — это утилитный класс, который агрегирует ответы Flux в один ChatClientResponse.
Это может быть полезно для логирования или другой обработки, которая наблюдает за всем ответом, а не отдельными элементами в потоке.
Обратите внимание, что вы не можете изменять ответ в `MessageAggregator`, так как это операция только для чтения.

#### Советник повторного чтения (Re2)

Статья "https://arxiv.org/pdf/2309.06275[Повторное чтение улучшает рассуждения в больших языковых моделях]" вводит технику, называемую повторным чтением (Re2), которая улучшает способности рассуждения больших языковых моделей.
Техника Re2 требует дополнения входного запроса следующим образом:

```
{Input_Query}
Прочитайте вопрос еще раз: {Input_Query}
```

Реализация советника, который применяет технику Re2 к запросу пользователя, может выглядеть так:

```java

public class ReReadingAdvisor implements BaseAdvisor {

	private static final String DEFAULT_RE2_ADVISE_TEMPLATE = """
			{re2_input_query}
			Прочитайте вопрос еще раз: {re2_input_query}
			""";

	private final String re2AdviseTemplate;

	private int order = 0;

	public ReReadingAdvisor() {
		this(DEFAULT_RE2_ADVISE_TEMPLATE);
	}

	public ReReadingAdvisor(String re2AdviseTemplate) {
		this.re2AdviseTemplate = re2AdviseTemplate;
	}

	@Override
	public ChatClientRequest before(ChatClientRequest chatClientRequest, AdvisorChain advisorChain) { // <1>
		String augmentedUserText = PromptTemplate.builder()
			.template(this.re2AdviseTemplate)
			.variables(Map.of("re2_input_query", chatClientRequest.prompt().getUserMessage().getText()))
			.build()
			.render();

		return chatClientRequest.mutate()
			.prompt(chatClientRequest.prompt().augmentUserMessage(augmentedUserText))
			.build();
	}

	@Override
	public ChatClientResponse after(ChatClientResponse chatClientResponse, AdvisorChain advisorChain) {
		return chatClientResponse;
	}

	@Override
	public int getOrder() { // <2>
		return this.order;
	}

	public ReReadingAdvisor withOrder(int order) {
		this.order = order;
		return this;
	}

}
```
<1> Метод `before` дополняет запрос пользователя, применяя технику повторного чтения.
<2> Вы можете контролировать порядок выполнения, установив значение порядка. Более низкие значения выполняются первыми.


#### Встроенные советники Spring AI

Фреймворк Spring AI предоставляет несколько встроенных советников для улучшения ваших взаимодействий с ИИ. Вот обзор доступных советников:

##### Советники памяти чата
Эти советники управляют историей беседы в хранилище памяти чата:

- `MessageChatMemoryAdvisor`
+
Извлекает память и добавляет ее в виде коллекции сообщений к запросу. Этот подход сохраняет структуру истории беседы. Обратите внимание, что не все модели ИИ поддерживают этот подход.

- `PromptChatMemoryAdvisor`
+
Извлекает память и включает ее в системный текст запроса.

- `VectorStoreChatMemoryAdvisor`
+
Извлекает память из VectorStore и добавляет ее в системный текст запроса. Этот советник полезен для эффективного поиска и извлечения релевантной информации из больших наборов данных.

##### Советник по вопросам и ответам
- `QuestionAnswerAdvisor`
+
Этот советник использует векторное хранилище для предоставления возможностей вопросов и ответов, реализуя наивный паттерн RAG (Retrieval-Augmented Generation).

- `RetrievalAugmentationAdvisor`
+
 Советник, который реализует общие потоки Retrieval Augmented Generation (RAG), используя строительные блоки, определенные в пакете `org.springframework.ai.rag`, и следуя модульной архитектуре RAG.


##### Советник по рассуждениям
- `ReReadingAdvisor`
+
Реализует стратегию повторного чтения для рассуждений LLM, названную RE2, для улучшения понимания на этапе ввода. 
Основывается на статье: [Повторное чтение улучшает рассуждения в LLM](https://arxiv.org/pdf/2309.06275).


##### Советник по безопасности контента
- `SafeGuardAdvisor`
+
Простой советник, предназначенный для предотвращения генерации моделью вредного или неприемлемого контента.


### Стриминг против нестриминга

![Поток Advisors Streaming vs Non-Streaming, width=800, align="center"](advisors-non-stream-vs-stream.jpg)

- Нестриминговые советники работают с полными запросами и ответами.
- Стриминговые советники обрабатывают запросы и ответы как непрерывные потоки, используя концепции реактивного программирования (например, Flux для ответов).


// TODO - Добавить раздел о том, как реализовать стриминговый советник с блокирующим и неблокирующим кодом.

```java
@Override
public Flux<ChatClientResponse> adviseStream(ChatClientRequest chatClientRequest, StreamAdvisorChain chain) {
    
    return  Mono.just(chatClientRequest)
            .publishOn(Schedulers.boundedElastic())
            .map(request -> {
                // Это может выполняться блокирующими и неблокирующими потоками.
                // Советник перед следующим разделом
            })
            .flatMapMany(request -> chain.nextStream(request))
            .map(response -> {
                // Советник после следующего раздела
            });
}
```

### Рекомендации по лучшим практикам

1. Держите советников сосредоточенными на конкретных задачах для лучшей модульности.
1. Используйте `adviseContext` для обмена состоянием между советниками, когда это необходимо.
1. Реализуйте как стриминговые, так и нестриминговые версии вашего советника для максимальной гибкости.
1. Тщательно продумайте порядок советников в вашей цепочке, чтобы обеспечить правильный поток данных.

## Изменения в API

### Интерфейсы советников

- В 1.0 M2 существовали отдельные интерфейсы `RequestAdvisor` и `ResponseAdvisor`.
** `RequestAdvisor` вызывался перед методами `ChatModel.call` и `ChatModel.stream`.
** `ResponseAdvisor` вызывался после этих методов.
- В 1.0 M3 эти интерфейсы были заменены на:
** `CallAroundAdvisor`
** `StreamAroundAdvisor`
- Режим ответа потока, ранее входивший в `ResponseAdvisor`, был удален.
- В 1.0.0 эти интерфейсы были заменены:
** `CallAroundAdvisor` -> `CallAdvisor`, `StreamAroundAdvisor` -> `StreamAdvisor`, `CallAroundAdvisorChain` -> `CallAdvisorChain` и `StreamAroundAdvisorChain` -> `StreamAdvisorChain`. 
** `AdvisedRequest` -> `ChatClientRequest` и `AdivsedResponse` -> `ChatClientResponse`.

### Обработка карты контекста

- В 1.0 M2:
** Карта контекста была отдельным аргументом метода.
** Карта была изменяемой и передавалась по цепочке.
- В 1.0 M3:
** Карта контекста теперь является частью записей `AdvisedRequest` и `AdvisedResponse`.
** Карта является неизменяемой.
** Чтобы обновить контекст, используйте метод `updateContext`, который создает новую неизменяемую карту с обновленным содержимым.
```
