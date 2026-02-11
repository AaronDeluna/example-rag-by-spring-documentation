package ru.mirent.stdio.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.model.ollama.autoconfigure.OllamaConnectionDetails;
import org.springframework.ai.ollama.api.OllamaApi;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestClient;
import org.springframework.web.reactive.function.client.WebClient;
import ru.mirent.stdio.interceptors.LoggingInterceptor;

/**
 * TODO Данная конфигурация временная и необходима только для обхода проблемы с тем, что бин
 * org.springframework.ai.model.ollama.autoconfigure.OllamaApiAutoConfiguration не запускается, возможно это баг.
 * После перехода на стабильную версию - эту конфигурацию убрать, предварительно проверив работоспособность.
 */
@Configuration
public class OllamaConfig {
    private static final Logger LOG = LoggerFactory.getLogger(OllamaConfig.class);

    public OllamaConfig() {
        LOG.info("Инициализация: {}", this.hashCode());
    }

    @Bean
    @ConditionalOnMissingBean
    public OllamaApi ollamaApi(OllamaConnectionDetails connectionDetails,
                               ObjectProvider<RestClient.Builder> restClientBuilderProvider,
                               ObjectProvider<WebClient.Builder> webClientBuilderProvider,
                               ObjectProvider<ResponseErrorHandler> responseErrorHandlerProvider) {

        RestClient.Builder restClientBuilder = restClientBuilderProvider.getObject();
        restClientBuilder.requestInterceptor(new LoggingInterceptor());

        return OllamaApi.builder()
                .baseUrl(connectionDetails.getBaseUrl())
                .restClientBuilder(restClientBuilder)
                .webClientBuilder(webClientBuilderProvider.getIfAvailable(WebClient::builder))
                .responseErrorHandler(responseErrorHandlerProvider.getIfAvailable())
                .build();
    }

    @Bean
    @ConditionalOnMissingBean
    public OllamaConnectionDetails ollamaConnectionDetails(@Value("${spring.ai.ollama.base-url}") String baseUrl) {
        return () -> baseUrl;
    }
}
