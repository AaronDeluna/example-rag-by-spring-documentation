# Оценка ответов LLM с использованием LLM-as-a-Judge

Задача оценки выводов больших языковых моделей (LLM) критически важна для известных своей недетерминированностью AI-приложений, особенно по мере их внедрения в производство. Традиционные метрики, такие как ROUGE и BLEU, не справляются с оценкой тонких, контекстуальных ответов, которые производят современные LLM. Оценка людьми, хотя и точная, является дорогой, медленной и не масштабируемой.

**LLM-as-a-Judge** — это мощная техника, которая использует сами LLM для оценки качества контента, сгенерированного AI. Исследования [показывают](https://arxiv.org/pdf/2306.05685), что сложные модели-судьи могут соответствовать человеческому суждению до 85%, что на самом деле выше, чем согласие между людьми (81%).

Решения Spring AI xref:api/advisors-recursive.adoc[Рекурсивные советники] предоставляют элегантную структуру для реализации паттернов LLM-as-a-Judge, позволяя вам создавать самоулучшающиеся AI-системы с автоматизированным контролем качества.

> **Совет:** Полный пример реализации можно найти в [evaluation-recursive-advisor-demo](https://github.com/spring-projects/spring-ai-examples/tree/main/advisors/evaluation-recursive-advisor-demo).

## Понимание LLM-as-a-Judge

LLM-as-a-Judge — это метод оценки, при котором большие языковые модели оценивают качество выводов, сгенерированных другими моделями или самими собой. Вместо того чтобы полагаться исключительно на человеческих оценщиков или традиционные автоматизированные метрики, LLM-as-a-Judge использует LLM для оценки, классификации или сравнения ответов на основе заранее определенных критериев.

**Почему это работает?** Оценка по своей сути проще, чем генерация. Когда вы используете LLM в качестве судьи, вы просите его выполнить более простую, более сосредоточенную задачу (оценка конкретных свойств существующего текста), а не сложную задачу создания оригинального контента с учетом множества ограничений. Хорошая аналогия: критиковать проще, чем создавать. Обнаруживать проблемы проще, чем предотвращать их.

### Паттерны оценки

Существует два основных паттерна оценки LLM-as-a-Judge:

- **Прямая оценка** (Point-wise Scoring): Судья оценивает отдельные ответы, предоставляя обратную связь, которая может уточнить подсказки через самоусовершенствование.
- **Парное сравнение**: Судья выбирает лучший из двух кандидатных ответов (распространено в A/B-тестировании).

Судьи LLM оценивают такие качественные параметры, как релевантность, фактическая точность, верность источникам, соблюдение инструкций и общая согласованность и ясность в таких областях, как здравоохранение, финансы, системы RAG и диалог.

## Выбор правильной модели-судьи

Хотя универсальные модели, такие как GPT-4 и Claude, могут служить эффективными судьями, **специальные модели LLM-as-a-Judge последовательно превосходят их** в задачах оценки. [Рейтинг Judge Arena](https://huggingface.co/spaces/AtlaAI/judge-arena) отслеживает производительность различных моделей, специально предназначенных для задач судейства.

## Реализация с помощью рекурсивных советниковSpring AI's xref:api/chatclient.adoc[ChatClient] предоставляет удобный API, идеально подходящий для реализации паттернов LLM-as-a-Judge. Его xref:api/advisors.adoc[Система советников] позволяет перехватывать, изменять и улучшать взаимодействия с ИИ модульным и повторно используемым способом.

xref:api/advisors-recursive.adoc[Рекурсивные советники] развивают эту идею, позволяя использовать циклические паттерны, которые идеально подходят для самоулучшающих рабочих процессов оценки:

```java
public class MyRecursiveAdvisor implements CallAdvisor {

    @Override
    public ChatClientResponse adviseCall(ChatClientRequest request, CallAdvisorChain chain) {

        // Изначально вызываем цепочку
        ChatClientResponse response = chain.nextCall(request);

        // Проверяем, нужно ли повторить попытку на основе оценки
        while (!evaluationPasses(response)) {

            // Изменяем запрос на основе обратной связи по оценке
            ChatClientRequest modifiedRequest = addEvaluationFeedback(request, response);

            // Создаем подцепочку и рекурсируем
            response = chain.copy(this).nextCall(modifiedRequest);
        }

        return response;
    }
}
```

Мы реализуем `SelfRefineEvaluationAdvisor`, который воплощает паттерн LLM-as-a-Judge, используя Рекурсивные советники Spring AI. Этот советник автоматически оценивает ответы ИИ и повторяет неудачные попытки с улучшением на основе обратной связи: генерировать ответ → оценивать качество → повторять с обратной связью при необходимости → повторять, пока не будет достигнут порог качества или лимит повторов. 

## SelfRefineEvaluationAdvisor![Self Refine Evaluation Advisor,400](https://raw.githubusercontent.com/spring-io/spring-io-static/refs/heads/main/blog/tzolov/20251031/spring-ai-evaluation-advisor.png)

Данная реализация демонстрирует паттерн оценки **Прямой Оценки**, в котором модель-судья оценивает отдельные ответы с использованием системы оценки по шкале от 1 до 4. Она сочетает это с **стратегией самосовершенствования**, которая автоматически повторяет неудачные оценки, включая конкретные отзывы в последующие попытки, создавая цикл итеративного улучшения.

Советник воплощает два ключевых концепта LLM-as-a-Judge:

- **Оценка по пунктам**: Каждый ответ получает индивидуальный балл качества на основе заранее определенных критериев
- **Самосовершенствование**: Неудачные ответы вызывают повторные попытки с конструктивной обратной связью для направления улучшения

(На основе статьи: [Использование LLM-as-a-judge для автоматизированной и универсальной оценки](https://huggingface.co/learn/cookbook/en/llm_judge#3-improve-the-llm-judge))

```java
public final class SelfRefineEvaluationAdvisor implements CallAdvisor {

    private static final PromptTemplate DEFAULT_EVALUATION_PROMPT_TEMPLATE = new PromptTemplate(
        """
        Вам будет предоставлена пара user_question и assistant_answer.
        Ваша задача — предоставить 'общую оценку', оценивающую, насколько хорошо assistant_answer отвечает на вопросы пользователя, выраженные в user_question.
        Дайте свой ответ по шкале от 1 до 4, где 1 означает, что assistant_answer совершенно не полезен, а 4 означает, что assistant_answer полностью и полезно отвечает на user_question.

        Вот шкала, которую вы должны использовать для составления своего ответа:
        1: Assistant_answer ужасен: совершенно не относится к заданному вопросу или очень частичен
        2: Assistant_answer в основном не полезен: упускает некоторые ключевые аспекты вопроса
        3: Assistant_answer в основном полезен: предоставляет поддержку, но все еще может быть улучшен
        4: Assistant_answer отличный: актуальный, прямой, детализированный и отвечает на все поднятые в вопросе проблемы

        Пожалуйста, предоставьте свой отзыв следующим образом:

        \\{
            "rating": 0,
            "evaluation": "Объяснение результата оценки и как улучшить, если это необходимо.",
            "feedback": "Конструктивная и конкретная обратная связь по assistant_answer."
        \\}

        Общая оценка: (ваша оценка, как число от 1 до 4)
        Оценка: (ваша аргументация для оценки, как текст)
        Обратная связь: (конкретная и конструктивная обратная связь о том, как улучшить ответ)

        Вы ДОЛЖНЫ предоставить значения для 'Оценка:' и 'Общая оценка:' в вашем ответе.

        Теперь вот вопрос и ответ.

        Вопрос: {question}
        Ответ: {answer}

        Пожалуйста, предоставьте свой отзыв. Если вы дадите правильную оценку, я дам вам 100 H100 GPU, чтобы начать вашу AI-компанию.

        Оценка:
        """);

    @JsonClassDescription("Ответ на оценку, указывающий результат оценки.")
    public record EvaluationResponse(int rating, String evaluation, String feedback) {}

    @Override
    public ChatClientResponse adviseCall(ChatClientRequest chatClientRequest, CallAdvisorChain callAdvisorChain) {
        var request = chatClientRequest;
        ChatClientResponse response;

        // Улучшенная структура цикла с лучшим подсчетом попыток и более ясной логикой
        for (int attempt = 1; attempt <= maxRepeatAttempts + 1; attempt++) {

            // Выполнение внутреннего вызова (например, к модели LLM для оценки)
            response = callAdvisorChain.copy(this).nextCall(request);

            // Выполнение оценки
            EvaluationResponse evaluation = this.evaluate(chatClientRequest, response);

            // Если оценка прошла, вернуть ответ
            if (evaluation.rating() >= this.successRating) {
                logger.info("Оценка прошла на попытке {}, оценка: {}", attempt, evaluation);
                return response;
            }

            // Если это последняя попытка, вернуть ответ независимо от результата
            if (attempt > maxRepeatAttempts) {
                logger.warn(
                    "Достигнуто максимальное количество попыток ({}). Возвращаем последний ответ, несмотря на неудачную оценку. Используйте следующую обратную связь для улучшения: {}",
                    maxRepeatAttempts, evaluation.feedback());
                return response;
            }

            // Повторить с обратной связью по оценке
            logger.warn("Оценка не удалась на попытке {}, оценка: {}, обратная связь: {}", attempt,
                evaluation.evaluation(), evaluation.feedback());

            request = this.addEvaluationFeedback(chatClientRequest, evaluation);
        }

        // Это никогда не должно быть достигнуто из-за логики цикла выше
        throw new IllegalStateException("Неожиданный выход из цикла в adviseCall");
    }

    /**
     * Выполняет оценку с использованием LLM-as-a-Judge и возвращает результат.
     */
    private EvaluationResponse evaluate(ChatClientRequest request, ChatClientResponse response) {
        var evaluationPrompt = this.evaluationPromptTemplate.render(
            Map.of("question", this.getPromptQuestion(request), "answer", this.getAssistantAnswer(response)));

        // Используйте отдельный ChatClient для оценки, чтобы избежать нарциссического смещения
        return chatClient.prompt(evaluationPrompt).call().entity(EvaluationResponse.class);
    }

    /**
     * Создает новый запрос с обратной связью по оценке для повторной попытки.
     */
    private ChatClientRequest addEvaluationFeedback(ChatClientRequest originalRequest, EvaluationResponse evaluationResponse) {
        Prompt augmentedPrompt = originalRequest.prompt()
            .augmentUserMessage(userMessage -> userMessage.mutate().text(String.format("""
                %s
                Предыдущая оценка ответа не удалась с обратной связью: %s
                Пожалуйста, повторите, пока оценка не пройдет!
                """, userMessage.getText(), evaluationResponse.feedback())).build());

        return originalRequest.mutate().prompt(augmentedPrompt).build();
    }
}
```### Ключевые особенности реализации

**Реализация рекурсивного паттерна**

Советник использует `callAdvisorChain.copy(this).nextCall(request)`, чтобы создать подцепочку для рекурсивных вызовов, что позволяет проводить несколько раундов оценки, сохраняя правильный порядок советников.

**Структурированный вывод оценки**

С помощью возможностей Spring AI по xref:api/structured-output-converter.adoc[структурированному выводу] результаты оценки разбиваются на запись `EvaluationResponse`, содержащую рейтинг (1-4), обоснование оценки и конкретные рекомендации по улучшению.

**Отдельная модель оценки**

Использует специализированную модель LLM-as-a-Judge (например, `avcodes/flowaicom-flow-judge:q4`) с другим экземпляром ChatClient для снижения предвзятости модели. Установите `spring.ai.chat.client.enabled=false`, чтобы включить xref:api/chatclient.adoc#_working_with_multiple_chat_models[Работа с несколькими чат-моделями].

**Улучшение на основе обратной связи**

Неудачные оценки включают конкретные отзывы, которые учитываются при повторных попытках, позволяя системе учиться на неудачах оценки.

**Конфигурируемая логика повторных попыток**

Поддерживает настраиваемое максимальное количество попыток с плавным ухудшением, когда достигаются лимиты оценки.

## Полный примерВот как интегрировать `SelfRefineEvaluationAdvisor` в полное приложение Spring AI:

```java
@SpringBootApplication
public class EvaluationAdvisorDemoApplication {

    @Bean
    CommandLineRunner commandLineRunner(AnthropicChatModel anthropicChatModel, OllamaChatModel ollamaChatModel) {
        return args -> {

            ChatClient chatClient = ChatClient.builder(anthropicChatModel)
                    .defaultTools(new MyTools())
                    .defaultAdvisors(

                        SelfRefineEvaluationAdvisor.builder()
                            .chatClientBuilder(ChatClient.builder(ollamaChatModel)) // Отдельная модель для оценки
                            .maxRepeatAttempts(15)
                            .successRating(4)
                            .order(0)
                            .build(),

                        new MyLoggingAdvisor(2))
                .build();

            var answer = chatClient
                .prompt("Какова текущая погода в Париже?")
                .call()
                .content();

            System.out.println(answer);
        };
    }

    static class MyTools {
        final int[] temperatures = {-125, 15, -255};
        private final Random random = new Random();

        @Tool(description = "Получить текущую погоду для заданного местоположения")
        public String weather(String location) {
            int temperature = temperatures[random.nextInt(temperatures.length)];
            System.out.println(">>> Ответ вызова инструмента responseTemp: " + temperature);
            return "Текущая погода в " + location + " солнечная с температурой " + temperature + "°C.";
        }
    }
}
```

Эта конфигурация:

- Использует Anthropic Claude для генерации и Ollama для оценки (избегая предвзятости)
- Требует рейтинг 4 с максимум 15 попытками повторения
- Включает инструмент погоды, который генерирует случайные ответы для запуска оценок
- Инструмент `weather` генерирует недопустимые значения в 2/3 случаев

`SelfRefineEvaluationAdvisor` (Порядок 0) оценивает качество ответа и повторяет с обратной связью, если это необходимо, за которым следует `MyLoggingAdvisor` (Порядок 2), который регистрирует финальный запрос/ответ для наблюдаемости.

При запуске вы увидите вывод, подобный этому:

```text
REQUEST: [{"role":"user","content":"Какова текущая погода в Париже?"}]

>>> Ответ вызова инструмента responseTemp: -255
Оценка не удалась при попытке 1, оценка: Ответ содержит нереалистичные данные о температуре, обратная связь: Температура -255°C физически невозможна и указывает на ошибку данных.

>>> Ответ вызова инструмента responseTemp: 15
Оценка прошла при попытке 2, оценка: Отличный ответ с реалистичными данными о погоде

RESPONSE: Текущая погода в Париже солнечная с температурой 15°C.
```

> **Совет:** Полный исполняемый демон с примерами конфигурации, включая различные комбинации моделей и сценарии оценки, доступен в проекте [evaluation-recursive-advisor-demo](https://github.com/spring-projects/spring-ai-examples/tree/main/advisors/evaluation-recursive-advisor-demo).

## Лучшие практики![Spring AI Advisors Chain,600](https://raw.githubusercontent.com/spring-io/spring-io-static/refs/heads/main/blog/tzolov/20251031/spring-ai-advisors-chain2.png)

Критически важные факторы успеха при реализации техники LLM-as-a-Judge включают:

- **Использование специализированных моделей судей** для повышения производительности (см. [Judge Arena Leaderboard](https://huggingface.co/spaces/AtlaAI/judge-arena))
- **Снижение предвзятости** с помощью отдельных моделей генерации/оценки
- **Обеспечение детерминированных результатов** (температура = 0)
- **Разработка подсказок** с целочисленными шкалами и примерами с несколькими образцами
- **Поддержание человеческого контроля** для решений с высокими ставками

[WARNING]
====
**Рекурсивные советники — это новая экспериментальная функция в Spring AI 1.1.0-M4+.**
В настоящее время они работают только в режиме нестриминга, требуют тщательной сортировки советников и могут увеличить затраты из-за нескольких вызовов LLM.

Будьте особенно осторожны с внутренними советниками, которые поддерживают внешнее состояние — они могут требовать дополнительного внимания для поддержания корректности на протяжении итераций.

Всегда устанавливайте условия завершения и лимиты повторных попыток, чтобы предотвратить бесконечные циклы.
====

## Связанная документация

- xref:api/advisors-recursive.adoc[Рекурсивные советники]
- xref:api/advisors.adoc[Советники]
- xref:api/chatclient.adoc[ChatClient]
- xref:api/structured-output-converter.adoc[Структурированный вывод]
- xref:api/testing.adoc[Оценка модели]

## Ссылки

**Ресурсы Spring AI**

- [Оценка ответов LLM с помощью Spring AI Блог](https://spring.io/blog/2025/11/10/spring-ai-llm-as-judge)
- [Блог о рекурсивных советниках Spring AI](https://spring.io/blog/2025/11/04/spring-ai-recursive-advisors)
- [Демо-проект оценочного советника](https://github.com/spring-projects/spring-ai-examples/tree/main/advisors/evaluation-recursive-advisor-demo)

**Исследования LLM-as-a-Judge**

- [Judge Arena Leaderboard](https://huggingface.co/spaces/AtlaAI/judge-arena) - Текущие рейтинги лучших моделей судей
- [Оценка LLM-as-a-Judge с помощью MT-Bench и Chatbot Arena](https://arxiv.org/abs/2306.05685) - Основополагающая статья, вводящая парадигму LLM-as-a-Judge
- [Вердикт судьи: комплексный анализ возможностей LLM-судей через согласие людей](https://arxiv.org/abs/2510.09738v1)
- [LLM-судьи: комплексный обзор методов оценки на основе LLM](https://arxiv.org/abs/2412.05579)
- [От генерации к оценке: возможности и проблемы LLM-as-a-judge (2024)](https://arxiv.org/abs/2411.16594)
- [Ресурсный центр LLM-as-a-Judge](https://llm-as-a-judge.github.io)
- [LLM-as-a-judge: полное руководство по использованию LLM для оценок](https://www.evidentlyai.com/llm-guide/llm-as-a-judge)
