package rag.ai.exampleragbyspringdocumentation.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.ollama.api.OllamaChatOptions;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import rag.ai.exampleragbyspringdocumentation.advisors.rag.RagAdvisor;

@Configuration
public class ChatClientConfiguration {

    @Autowired
    private VectorStore vectorStore;

    @Bean
    public ChatClient chatClient(ChatClient.Builder builder) {
        return builder.defaultAdvisors(
                        RagAdvisor.build(vectorStore).order(1).build(),
                        simpleLoggerAdvisor(2))
                .defaultOptions(OllamaChatOptions.builder().temperature(0.7).presencePenalty(1.1).build())
                .build();
    }

    private SimpleLoggerAdvisor simpleLoggerAdvisor(int order) {
        return SimpleLoggerAdvisor.builder().order(order).build();
    }
}
