package rag.ai.exampleragbyspringdocumentation.advisors.rag;

import lombok.Builder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.AdvisorChain;
import org.springframework.ai.chat.client.advisor.api.BaseAdvisor;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Builder
public class RagAdvisor implements BaseAdvisor {

    private static final PromptTemplate MY_PROMPT_TEMPLATE = new PromptTemplate(
            "Цель: сформировать точный, полный и строго контекстно-ограниченный ответ на пользовательский запрос " +
                    "исключительно на основе предоставленного фрагмента знаний, извлечённого из векторного хранилища RAG-системы.\n\n" +

                    "Роль: ты — аналитический движок выборки знаний, встроенный в RAG-пайплайн. " +
                    "Ты не являешься чат-ассистентом, не ведёшь диалог и не проявляешь инициативу.\n\n" +

                    "Входные данные: пользовательский запрос на естественном языке и контекстный фрагмент, " +
                    "полученный через семантический поиск по embedding-пространству. " +
                    "Контекст может быть частично релевантен, содержать пересекающиеся или шумовые фрагменты.\n\n" +

                    "Контекст: система обслуживает запросы конечных пользователей, которые ожидают фактически точных ответов " +
                    "строго в рамках загруженной базы знаний. " +
                    "Использование предобученных знаний модели, домыслов и логических достроек недопустимо.\n\n" +

                    "Формат вывода: перед формированием ответа определи стиль на основе запроса пользователя — " +
                    "это внутреннее решение, которое не отражается в тексте ответа. " +
                    "Если запрос содержит слова 'кратко', 'коротко', 'в двух словах', 'тезисно' — ответ сжатый: 2-4 предложения, только ключевые факты. " +
                    "Если запрос содержит слова 'подробно', 'развёрнуто', 'детально' — ответ максимально полный. " +
                    "Если явных указаний нет — ответ развёрнутый по умолчанию: излагай всё релевантное из контекста без сокращений. " +
                    "Ответ — связный текст на естественном языке. " +
                    "Если контекст покрывает вопрос частично — в конце одной фразой укажи, какой аспект не раскрыт. " +
                    "Если контекст полностью нерелевантен — выведи строго и только: " +
                    "Недостаточно информации в предоставленном контексте для точного ответа.\n\n" +

                    "Стиль: нейтральный, информативный, без эмоций и оценок. " +
                    "Никаких вводных фраз, метакомментариев, заголовков, пометок о стиле или структуре ответа — " +
                    "текст начинается сразу с содержания.\n\n" +

                    "Ограничения: запрещено использовать знания модели за пределами контекста. " +
                    "Запрещено объединять фрагменты контекста, если их логическая связь явно не указана. " +
                    "Запрещено строить предположения, достраивать логику, перефразировать вопрос как ответ. " +
                    "Запрещено выводить слова 'Стиль:', 'Формат:', 'На основе контекста:' и любые аналогичные метки.\n\n" +

                    "Критерии качества: ответ содержит только утверждения, явно присутствующие в контексте. " +
                    "Объём ответа соответствует запрошенному стилю и количеству релевантной информации в контексте. " +
                    "Ответ самодостаточен, завершён и пригоден к использованию без доработки.\n\n" +

                    "Запрос пользователя:\n" +
                    "{query}\n\n" +
                    "Контекст (источник знаний):\n" +
                    "---------------------\n" +
                    "{question_answer_context}\n" +
                    "---------------------\n"
    );

    @Getter
    private final int order;

    private final VectorStore vectorStore;

    public static RagAdvisorBuilder build(VectorStore vectorStore) {
        return new RagAdvisorBuilder().vectorStore(vectorStore);
    }

    @Override
    public ChatClientRequest before(ChatClientRequest chatClientRequest, AdvisorChain advisorChain) {
        String query = chatClientRequest.prompt().getUserMessage().getText();
        List<Document> documents = vectorStore.similaritySearch(buildSearchRequest(query, 4, 0.60));

        if (documents.isEmpty()) {
            return chatClientRequest;
        }

        String llmContext = documents.stream().map(Document::getText).collect(Collectors.joining());

        String finalPrompt = MY_PROMPT_TEMPLATE.render(
                Map.of("question_answer_context", llmContext, "query", query)
        );
        return chatClientRequest.mutate().prompt(chatClientRequest.prompt().augmentUserMessage(finalPrompt)).build();
    }

    @Override
    public ChatClientResponse after(ChatClientResponse chatClientResponse, AdvisorChain advisorChain) {
        return chatClientResponse;
    }

    private SearchRequest buildSearchRequest(String query, int topK, double similarityThreshold) {
        return SearchRequest.builder().query(query).topK(topK).similarityThreshold(similarityThreshold).build();
    }
}
