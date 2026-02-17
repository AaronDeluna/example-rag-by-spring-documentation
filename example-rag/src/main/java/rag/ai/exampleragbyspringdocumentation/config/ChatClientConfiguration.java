package rag.ai.exampleragbyspringdocumentation.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import rag.ai.exampleragbyspringdocumentation.advisors.rag.RagAdvisor;

@Configuration
public class ChatClientConfiguration {

    private static final PromptTemplate MY_PROMPT_TEMPLATE = new PromptTemplate(
            "Ты — языковая модель, работающая внутри Retrieval-Augmented Generation (RAG) системы.\n" +
                    "Ты взаимодействуешь с пользователем исключительно на основе извлечённого контекста.\n" +
                    "После получения пользовательского запроса ты обязан сформировать ответ, строго опираясь на предоставленный контекст.\n" +
                    "Ты не имеешь доступа к каким-либо другим источникам знаний и не должен использовать предобученные знания модели.\n\n" +

                    "Ограничения:\n" +
                    "- Ты не являешься чат-ассистентом.\n" +
                    "- Ты не должен использовать внешние знания.\n" +
                    "- Ты не должен строить предположения.\n" +
                    "- Ты не должен логически достраивать отсутствующую информацию.\n" +
                    "- Ты не должен использовать здравый смысл для заполнения пробелов.\n" +
                    "- Ты не должен перефразировать вопрос как ответ.\n\n" +

                    "Контекст был получен с помощью семантического поиска по embedding-векторному пространству.\n" +
                    "Это означает, что:\n" +
                    "- Контекст может быть частично релевантен.\n" +
                    "- Контекст может содержать шумовые или пересекающиеся фрагменты.\n" +
                    "- Контекст может не содержать полного ответа.\n" +
                    "Семантическая близость не гарантирует фактическую применимость информации к вопросу.\n\n" +

                    "Твои действия:\n" +
                    "1. Проанализируй пользовательский запрос.\n" +
                    "2. Проанализируй каждый фрагмент контекста независимо.\n" +
                    "3. Определи степень покрытия вопроса доступной информацией.\n" +
                    "4. Используй только те утверждения, которые явно присутствуют в контексте.\n" +
                    "5. Игнорируй любую информацию, не подтверждённую контекстом.\n" +
                    "6. Не объединяй фрагменты, если их логическая связь явно не указана.\n\n" +

                    "Правила генерации ответа:\n" +
                    "- Если контекст полностью покрывает вопрос — дай точный ответ.\n" +
                    "- Если контекст покрывает вопрос частично — дай частичный ответ и явно укажи, какая информация отсутствует.\n" +
                    "- Если контекст не содержит ответа — выведи строго:\n" +
                    "Недостаточно информации в предоставленном контексте для точного ответа.\n\n" +

                    "Формат ответа:\n" +
                    "Связный текст на естественном языке без списков и метакомментариев.\n\n" +

                    "Запрос пользователя:\n" +
                    "{query}\n\n" +
                    "Контекстная информация (источник знаний):\n" +
                    "---------------------\n" +
                    "{question_answer_context}\n" +
                    "---------------------\n"
    );


    @Autowired
    private VectorStore vectorStore;

    @Bean
    public ChatClient chatClient(ChatClient.Builder builder) {
        return builder.defaultAdvisors(
                        RagAdvisor.build(vectorStore).order(1).build(),
                        simpleLoggerAdvisor(2)
                )
                .build();
    }

    private Advisor getRagAdvisor(int order) {
        SearchRequest searchRequest = SearchRequest.builder()
                .topK(5)
                .similarityThreshold(0.7)
                .build();

        return QuestionAnswerAdvisor.builder(vectorStore)
                .searchRequest(searchRequest)
                .promptTemplate(MY_PROMPT_TEMPLATE)
                .order(order)
                .build();
    }

    private SimpleLoggerAdvisor simpleLoggerAdvisor(int order) {
        return SimpleLoggerAdvisor.builder().order(order).build();
    }
}
