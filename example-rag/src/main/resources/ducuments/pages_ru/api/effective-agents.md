# Создание эффективных агентов

В недавней исследовательской публикации, https://www.anthropic.com/research/building-effective-agents[Создание эффективных агентов], компания Anthropic поделилась ценными идеями о создании эффективных агентов на основе больших языковых моделей (LLM). Особенно интересен этот исследовательский труд тем, что он акцентирует внимание на простоте и составляемости, а не на сложных структурах. Давайте рассмотрим, как эти принципы переводятся в практические реализации с использованием https://docs.spring.io/spring-ai/reference/index.html[Spring AI].

![Системы агентов, ширина=350](https://raw.githubusercontent.com/spring-io/spring-io-static/refs/heads/main/blog/tzolov/spring-ai-agentic-systems.jpg)

Хотя описания паттернов и диаграммы взяты из оригинальной публикации Anthropic, мы сосредоточимся на том, как реализовать эти паттерны с помощью возможностей Spring AI для портируемости моделей и структурированного вывода. Рекомендуем сначала ознакомиться с оригинальной статьей.

Директория https://github.com/spring-projects/spring-ai-examples/tree/main/agentic-patterns[agentic-patterns] в репозитории spring-ai-examples содержит весь код для следующих примеров.

## Агентные системы

В исследовательской публикации делается важное архитектурное различие между двумя типами агентных систем:

1. **Рабочие процессы**: Системы, в которых LLM и инструменты организованы через предопределенные кодовые пути (например, предписывающие системы)
1. **Агенты**: Системы, в которых LLM динамически управляют своими процессами и использованием инструментов

Ключевое понимание заключается в том, что, хотя полностью автономные агенты могут казаться привлекательными, рабочие процессы часто обеспечивают лучшую предсказуемость и согласованность для четко определенных задач. Это идеально соответствует требованиям предприятий, где надежность и поддерживаемость имеют решающее значение.

Давайте рассмотрим, как Spring AI реализует эти концепции через пять основных паттернов, каждый из которых служит конкретным случаям использования:

### 1. https://github.com/spring-projects/spring-ai-examples/tree/main/agentic-patterns/chain-workflow[Цепочка рабочего процесса]

Паттерн Цепочка рабочего процесса иллюстрирует принцип разбиения сложных задач на более простые и управляемые шаги.

![Цепочка запросов](https://www.anthropic.com/_next/image?url=https%3A%2F%2Fwww-cdn.anthropic.com%2Fimages%2F4zrzovbb%2Fwebsite%2F7418719e3dab222dccb379b8879e1dc08ad34c78-2401x1000.png&w=3840&q=75)

**Когда использовать:**
- Задачи с четкими последовательными шагами
- Когда вы хотите обменять задержку на более высокую точность
- Когда каждый шаг основывается на выводе предыдущего шага

Вот практический пример из реализации Spring AI:

```java
public class ChainWorkflow {
    private final ChatClient chatClient;
    private final String[] systemPrompts;

    public String chain(String userInput) {
        String response = userInput;
        for (String prompt : systemPrompts) {
            String input = String.format("{%s}\n {%s}", prompt, response);
            response = chatClient.prompt(input).call().content();
        }
        return response;
    }
}
```

Эта реализация демонстрирует несколько ключевых принципов:

- Каждый шаг имеет четкую ответственность
- Вывод одного шага становится входом для следующего
- Цепочка легко расширяема и поддерживаема

### 2. https://github.com/spring-projects/spring-ai-examples/tree/main/agentic-patterns/parallelization-workflow[Параллельный рабочий процесс]

LLM могут одновременно работать над задачами и программно агрегировать их выводы.

![Параллельный рабочий процесс](https://www.anthropic.com/_next/image?url=https%3A%2F%2Fwww-cdn.anthropic.com%2Fimages%2F4zrzovbb%2Fwebsite%2F406bb032ca007fd1624f261af717d70e6ca86286-2401x1000.png&w=3840&q=75)

**Когда использовать:**
- Обработка больших объемов похожих, но независимых элементов
- Задачи, требующие нескольких независимых точек зрения
- Когда время обработки критично, и задачи могут выполняться параллельно

```java
List<String> parallelResponse = new ParallelizationWorkflow(chatClient)
    .parallel(
        "Анализируйте, как изменения на рынке повлияют на эту группу заинтересованных сторон.",
        List.of(
            "Клиенты: ...",
            "Сотрудники: ...",
            "Инвесторы: ...",
            "Поставщики: ..."
        ),
        4
    );
```

### 3. https://github.com/spring-projects/spring-ai-examples/tree/main/agentic-patterns/routing-workflow[Маршрутизация рабочего процесса]

Паттерн Маршрутизация реализует интеллектуальное распределение задач, позволяя специализированную обработку для различных типов входных данных.

![Маршрутизация рабочего процесса](https://www.anthropic.com/_next/image?url=https%3A%2F%2Fwww-cdn.anthropic.com%2Fimages%2F4zrzovbb%2Fwebsite%2F5c0c0e9fe4def0b584c04d37849941da55e5e71c-2401x1000.png&w=3840&q=75)

**Когда использовать:**
- Сложные задачи с различными категориями входных данных
- Когда разные входные данные требуют специализированной обработки
- Когда классификация может быть выполнена точно

```java
@Autowired
private ChatClient chatClient;

RoutingWorkflow workflow = new RoutingWorkflow(chatClient);

Map<String, String> routes = Map.of(
    "billing", "Вы специалист по выставлению счетов. Помогите решить проблемы с выставлением счетов...",
    "technical", "Вы инженер технической поддержки. Помогите решить технические проблемы...",
    "general", "Вы представитель службы поддержки клиентов. Помогите с общими запросами..."
);

String input = "С моего счета дважды списали деньги на прошлой неделе";
String response = workflow.route(input, routes);
```

### 4. https://github.com/spring-projects/spring-ai-examples/tree/main/agentic-patterns/orchestrator-workers[Оркестратор-Работники]

![Оркестрация рабочего процесса](https://www.anthropic.com/_next/image?url=https%3A%2F%2Fwww-cdn.anthropic.com%2Fimages%2F4zrzovbb%2Fwebsite%2F8985fc683fae4780fb34eab1365ab78c7e51bc8e-2401x1000.png&w=3840&q=75)

**Когда использовать:**
- Сложные задачи, когда подзадачи не могут быть предсказаны заранее
- Задачи, требующие различных подходов или точек зрения
- Ситуации, требующие адаптивного решения проблем

```java
public class OrchestratorWorkersWorkflow {
    public WorkerResponse process(String taskDescription) {
        // 1. Оркестратор анализирует задачу и определяет подзадачи
        OrchestratorResponse orchestratorResponse = // ...

        // 2. Работники обрабатывают подзадачи параллельно
        List<String> workerResponses = // ...

        // 3. Результаты объединяются в окончательный ответ
        return new WorkerResponse(/*...*/);
    }
}
```

Пример использования:

```java
ChatClient chatClient = // ... инициализация клиента чата
OrchestratorWorkersWorkflow workflow = new OrchestratorWorkersWorkflow(chatClient);

WorkerResponse response = workflow.process(
    "Создайте как техническую, так и удобную для пользователя документацию для конечной точки REST API"
);

System.out.println("Анализ: " + response.analysis());
System.out.println("Выводы работников: " + response.workerResponses());
```

### 5. https://github.com/spring-projects/spring-ai-examples/tree/main/agentic-patterns/evaluator-optimizer[Оценка-Оптимизация]

![Оценка-Оптимизация рабочего процесса](https://www.anthropic.com/_next/image?url=https%3A%2F%2Fwww-cdn.anthropic.com%2Fimages%2F4zrzovbb%2Fwebsite%2F14f51e6406ccb29e695da48b17017e899a6119c7-2401x1000.png&w=3840&q=75)

**Когда использовать:**
- Существуют четкие критерии оценки
- Итеративное уточнение приносит измеримую ценность
- Задачи выигрывают от нескольких раундов критики

```java
public class EvaluatorOptimizerWorkflow {
    public RefinedResponse loop(String task) {
        Generation generation = generate(task, context);
        EvaluationResponse evaluation = evaluate(generation.response(), task);
        return new RefinedResponse(finalSolution, chainOfThought);
    }
}
```

Пример использования:

```java
ChatClient chatClient = // ... инициализация клиента чата
EvaluatorOptimizerWorkflow workflow = new EvaluatorOptimizerWorkflow(chatClient);

RefinedResponse response = workflow.loop(
    "Создайте класс Java, реализующий потокобезопасный счетчик"
);

System.out.println("Окончательное решение: " + response.solution());
System.out.println("Эволюция: " + response.chainOfThought());
```

## Преимущества реализации Spring AI

Реализация этих паттернов в Spring AI предлагает несколько преимуществ, которые соответствуют рекомендациям Anthropic:

### https://docs.spring.io/spring-ai/reference/api/chat/comparison.html[Портируемость моделей]

```xml
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-openai-spring-boot-starter</artifactId>
</dependency>
```

### https://docs.spring.io/spring-ai/reference/api/structured-output-converter.html[Структурированный вывод]

```java
EvaluationResponse response = chatClient.prompt(prompt)
    .call()
    .entity(EvaluationResponse.class);
```

### https://docs.spring.io/spring-ai/reference/api/chatclient.html[Единый API]

- Единый интерфейс для различных поставщиков LLM
- Встроенная обработка ошибок и повторные попытки
- Гибкое управление запросами

## Лучшие практики и рекомендации

- **Начинайте с простого**
- Начинайте с базовых рабочих процессов, прежде чем добавлять сложность
- Используйте самый простой паттерн, который соответствует вашим требованиям
- Добавляйте сложность только при необходимости

- **Проектируйте для надежности**
- Реализуйте четкую обработку ошибок
- Используйте безопасные по типу ответы, где это возможно
- Внедряйте валидацию на каждом этапе

- **Учитывайте компромиссы**
- Балансируйте задержку и точность
- Оцените, когда использовать параллельную обработку
- Выбирайте между фиксированными рабочими процессами и динамическими агентами

## Будущая работа

Эти руководства будут обновлены, чтобы исследовать, как создавать более продвинутых агентов, которые объединяют эти основные паттерны с более сложными функциями:

**Композиция паттернов**
- Объединение нескольких паттернов для создания более мощных рабочих процессов
- Создание гибридных систем, которые используют сильные стороны каждого паттерна
- Создание гибких архитектур, которые могут адаптироваться к изменяющимся требованиям

**Управление памятью агентов**
- Реализация постоянной памяти в рамках разговоров
- Эффективное управление контекстными окнами
- Разработка стратегий для долгосрочного сохранения знаний

**Интеграция инструментов и протокола контекста модели (MCP)**
- Использование внешних инструментов через стандартизированные интерфейсы
- Реализация MCP для улучшения взаимодействия моделей
- Создание расширяемых архитектур агентов

## Заключение

Сочетание исследовательских идей Anthropic и практических реализаций Spring AI предоставляет мощную основу для создания эффективных систем на основе LLM.

Следуя этим паттернам и принципам, разработчики могут создавать надежные, поддерживаемые и эффективные AI-приложения, которые приносят реальную ценность, избегая ненужной сложности.

Ключевым моментом является то, что иногда самое простое решение является самым эффективным. Начинайте с базовых паттернов, тщательно изучайте свой случай использования и добавляйте сложность только тогда, когда это явно улучшает производительность или возможности вашей системы.
