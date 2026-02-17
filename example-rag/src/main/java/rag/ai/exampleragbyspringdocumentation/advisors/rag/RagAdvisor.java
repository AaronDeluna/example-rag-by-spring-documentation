package rag.ai.exampleragbyspringdocumentation.advisors.rag;

import lombok.Builder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.AdvisorChain;
import org.springframework.ai.chat.client.advisor.api.BaseAdvisor;
import org.springframework.ai.chat.model.ChatModel;
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

    private static final PromptTemplate template = PromptTemplate.builder().template("""
            Context: {context}
            Question: {question}
            """).build();

    @Getter
    private final int order;

    private final VectorStore vectorStore;

    public static RagAdvisorBuilder build(VectorStore vectorStore) {
        return new RagAdvisorBuilder().vectorStore(vectorStore);
    }

    @Override
    public ChatClientRequest before(ChatClientRequest chatClientRequest, AdvisorChain advisorChain) {
        String userQuestin = chatClientRequest.prompt().getUserMessage().getText();
        List<Document> documents = vectorStore.similaritySearch(buildSearchRequest(userQuestin,4, 0.60));

        if (documents.isEmpty()) {
            return chatClientRequest;
        }

        String llmContext = documents.stream().map(Document::getText).collect(Collectors.joining());

        String finalPrompt = template.render(
                Map.of("context", llmContext, "question", userQuestin)
        );
        return chatClientRequest.mutate().prompt(chatClientRequest.prompt().augmentUserMessage(finalPrompt)).build();
    }

    @Override
    public ChatClientResponse after(ChatClientResponse chatClientResponse, AdvisorChain advisorChain) {
        return chatClientResponse;
    }

    private SearchRequest buildSearchRequest(String userQuestin, int topK, double similarityThreshold) {
        return SearchRequest.builder().query(userQuestin).topK(topK).similarityThreshold(similarityThreshold).build();
    }
}
