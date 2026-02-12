package rag.ai.exampleragbyspringdocumentation.config;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.ollama.OllamaEmbeddingModel;
import org.springframework.ai.ollama.api.OllamaApi;
import org.springframework.ai.ollama.api.OllamaEmbeddingOptions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class OllamaConfig {

    @Bean
    @Primary
    public EmbeddingModel embeddingModel() {
        OllamaApi embeddingApi = OllamaApi.builder()
                .baseUrl("http://192.168.0.62:11434")
                .build();

        return OllamaEmbeddingModel.builder()
                .ollamaApi(embeddingApi)
                .defaultOptions(
                        OllamaEmbeddingOptions.builder()
                                .model("mxbai-embed-large")
                                .build()
                )
                .build();
    }
}
