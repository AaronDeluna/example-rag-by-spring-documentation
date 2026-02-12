package rag.ai.exampleragbyspringdocumentation.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ChatClientConfiguration {

    private static final PromptTemplate MY_PROMPT_TEMPLATE = new PromptTemplate(
            "Запрос пользователя:\n" +
                    "{query}\n\n" +
                    "Контекстная информация (источник знаний):\n" +
                    "---------------------\n" +
                    "{question_answer_context}\n" +
                    "---------------------\n\n" +
                    "Инструкции:\n" +
                    "1. Используй **только** информацию из контекста.\n" +
                    "2. **Запрещено** использовать внешние знания или делать предположения.\n" +
                    "3. Ответ должен быть точным, обоснованным и строго следовать контексту.\n" +
                    "4. Если ответ не найден в контексте — **честно сообщи, что информации недостаточно**.\n\n" +
                    "Формат ответа:\n" +
                    "- Краткий, точный ответ (если найден в контексте).\n" +
                    "- При отсутствии данных — текст: \"Недостаточно информации в предоставленном контексте для точного ответа.\"\n"
    );

    @Autowired
    private VectorStore vectorStore;

    @Bean
    public ChatClient chatClient(ChatClient.Builder builder) {
        return builder
                .defaultAdvisors(simpleLoggerAdvisor(0))
                .defaultAdvisors(getRagAdvisor(1))
                .defaultAdvisors(simpleLoggerAdvisor(2))
                .build();
    }

    private Advisor getRagAdvisor(int order) {
        return QuestionAnswerAdvisor.builder(vectorStore)
                .promptTemplate(MY_PROMPT_TEMPLATE)
                .order(order)
                .build();
    }

    private SimpleLoggerAdvisor simpleLoggerAdvisor(int order) {
        return SimpleLoggerAdvisor.builder().order(order).build();
    }
}
