# Оценка тестирования

Тестирование AI-приложений требует оценки сгенерированного контента, чтобы убедиться, что AI-модель не выдала ошибочный ответ.

Один из методов оценки ответа — использовать саму AI-модель для оценки. Выберите лучшую AI-модель для оценки, которая может отличаться от модели, использованной для генерации ответа.

Интерфейс Spring AI для оценки ответов — это `Evaluator`, определенный как:

```java
@FunctionalInterface
public interface Evaluator {
    EvaluationResponse evaluate(EvaluationRequest evaluationRequest);
}
```

Входные данные для оценки — это `EvaluationRequest`, определенный как

```java
public class EvaluationRequest {

	private final String userText;

	private final List<Content> dataList;

	private final String responseContent;

	public EvaluationRequest(String userText, List<Content> dataList, String responseContent) {
		this.userText = userText;
		this.dataList = dataList;
		this.responseContent = responseContent;
	}

  ...
}
```

- `userText`: Исходный ввод от пользователя в виде `String`
- `dataList`: Контекстные данные, такие как данные из Retrieval Augmented Generation, добавленные к исходному вводу.
- `responseContent`: Ответ AI-модели в виде `String`

## Оценка релевантности

`RelevancyEvaluator` — это реализация интерфейса `Evaluator`, предназначенная для оценки релевантности ответов, сгенерированных AI, по сравнению с предоставленным контекстом. Этот оценщик помогает оценить качество потока RAG, определяя, является ли ответ AI-модели релевантным для ввода пользователя с учетом извлеченного контекста.

Оценка основана на вводе пользователя, ответе AI-модели и информации о контексте. Она использует шаблон запроса, чтобы спросить AI-модель, является ли ответ релевантным для ввода пользователя и контекста.

Это стандартный шаблон запроса, используемый `RelevancyEvaluator`:

```text
Ваша задача — оценить, соответствует ли ответ на запрос
предоставленной информации о контексте.

У вас есть два варианта ответа. Либо ДА, либо НЕТ.

Ответьте ДА, если ответ на запрос
соответствует информации о контексте, в противном случае НЕТ.

Запрос:
{query}

Ответ:
{response}

Контекст:
{context}

Ответ:
```

> **Примечание:** Вы можете настроить шаблон запроса, предоставив свой собственный объект `PromptTemplate` через метод сборки `.promptTemplate()`. См. xref:_custom_template[Пользовательский шаблон] для получения подробной информации.

## Использование в интеграционных тестах```markdown
Вот пример использования `RelevancyEvaluator` в интеграционном тесте, который проверяет результат потока RAG с использованием `RetrievalAugmentationAdvisor`:

```java
@Test
void evaluateRelevancy() {
    String question = "Где происходит приключение Анаклета и Бирбы?";

    RetrievalAugmentationAdvisor ragAdvisor = RetrievalAugmentationAdvisor.builder()
        .documentRetriever(VectorStoreDocumentRetriever.builder()
            .vectorStore(pgVectorStore)
            .build())
        .build();

    ChatResponse chatResponse = ChatClient.builder(chatModel).build()
        .prompt(question)
        .advisors(ragAdvisor)
        .call()
        .chatResponse();

    EvaluationRequest evaluationRequest = new EvaluationRequest(
        // Исходный вопрос пользователя
        question,
        // Извлеченный контекст из потока RAG
        chatResponse.getMetadata().get(RetrievalAugmentationAdvisor.DOCUMENT_CONTEXT),
        // Ответ AI модели
        chatResponse.getResult().getOutput().getText()
    );

    RelevancyEvaluator evaluator = new RelevancyEvaluator(ChatClient.builder(chatModel));

    EvaluationResponse evaluationResponse = evaluator.evaluate(evaluationRequest);

    assertThat(evaluationResponse.isPass()).isTrue();
}
```

Вы можете найти несколько интеграционных тестов в проекте Spring AI, которые используют `RelevancyEvaluator` для тестирования функциональности `QuestionAnswerAdvisor` (см. https://github.com/spring-projects/spring-ai/blob/main/spring-ai-integration-tests/src/test/java/org/springframework/ai/integration/tests/client/advisor/QuestionAnswerAdvisorIT.java[тесты]) и `RetrievalAugmentationAdvisor` (см. https://github.com/spring-projects/spring-ai/blob/main/spring-ai-integration-tests/src/test/java/org/springframework/ai/integration/tests/client/advisor/RetrievalAugmentationAdvisorIT.java[тесты]).

### Пользовательский шаблон

`RelevancyEvaluator` использует шаблон по умолчанию для запроса к AI модели для оценки. Вы можете настроить это поведение, предоставив свой собственный объект `PromptTemplate` через метод сборки `.promptTemplate()`.

Пользовательский `PromptTemplate` может использовать любую реализацию `TemplateRenderer` (по умолчанию используется `StPromptTemplate`, основанный на https://www.stringtemplate.org/[StringTemplate] движке). Важным требованием является то, что шаблон должен содержать следующие заполнители:

- заполнитель `query` для получения вопроса пользователя.
- заполнитель `response` для получения ответа AI модели.
- заполнитель `context` для получения информации о контексте.

## FactCheckingEvaluator

FactCheckingEvaluator — это еще одна реализация интерфейса Evaluator, предназначенная для оценки фактической точности ответов, сгенерированных AI, по сравнению с предоставленным контекстом. Этот оценщик помогает обнаруживать и уменьшать галлюцинации в выводах AI, проверяя, поддерживается ли данное утверждение (заявление) логически предоставленным контекстом (документом).

'Заявление' и 'документ' представляются AI модели для оценки. Доступны более мелкие и эффективные AI модели, специально предназначенные для этой цели, такие как Minicheck от Bespoke, которые помогают снизить стоимость выполнения этих проверок по сравнению с флагманскими моделями, такими как GPT-4. Minicheck также доступен для использования через Ollama.

### Использование
Конструктор FactCheckingEvaluator принимает в качестве параметра ChatClient.Builder:
```java
public FactCheckingEvaluator(ChatClient.Builder chatClientBuilder) {
  this.chatClientBuilder = chatClientBuilder;
}
```
Оценщик использует следующий шаблон запроса для проверки фактов:
```text
Документ: {document}
Заявление: {claim}
```
Где `+{document}+` — это информация о контексте, а `+{claim}+` — это ответ AI модели, который необходимо оценить.

### Пример
```Вот пример того, как использовать FactCheckingEvaluator с ChatModel на основе Ollama, в частности, моделью Bespoke-Minicheck:

```java
@Test
void testFactChecking() {
  // Настройка API Ollama
  OllamaApi ollamaApi = new OllamaApi("http://localhost:11434");

  ChatModel chatModel = new OllamaChatModel(ollamaApi,
				OllamaChatOptions.builder().model(BESPOKE_MINICHECK).numPredict(2).temperature(0.0d).build())


  // Создание FactCheckingEvaluator
  var factCheckingEvaluator = new FactCheckingEvaluator(ChatClient.builder(chatModel));

  // Пример контекста и утверждения
  String context = "Земля — третья планета от Солнца и единственный астрономический объект, известный как место обитания жизни.";
  String claim = "Земля — четвертая планета от Солнца.";

  // Создание EvaluationRequest
  EvaluationRequest evaluationRequest = new EvaluationRequest(context, Collections.emptyList(), claim);

  // Проведение оценки
  EvaluationResponse evaluationResponse = factCheckingEvaluator.evaluate(evaluationRequest);

  assertFalse(evaluationResponse.isPass(), "Утверждение не должно поддерживаться контекстом");

}
```
