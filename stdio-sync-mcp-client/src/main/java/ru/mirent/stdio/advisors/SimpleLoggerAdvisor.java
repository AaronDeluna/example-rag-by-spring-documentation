package ru.mirent.stdio.advisors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.CallAdvisor;
import org.springframework.ai.chat.client.advisor.api.CallAdvisorChain;

public class SimpleLoggerAdvisor implements CallAdvisor {
    private static final Logger LOG = LoggerFactory.getLogger(SimpleLoggerAdvisor.class);

    public SimpleLoggerAdvisor() {
        LOG.info("Инициализация: {}", this.hashCode());
    }

    @Override
    public String getName() {
        return this.getClass().getSimpleName();
    }

    @Override
    public int getOrder() {
        return 0;
    }

    @Override
    public ChatClientResponse adviseCall(ChatClientRequest chatClientRequest, CallAdvisorChain callAdvisorChain) {
        logRequest(chatClientRequest);

        ChatClientResponse chatClientResponse = callAdvisorChain.nextCall(chatClientRequest);

        logResponse(chatClientResponse);

        return chatClientResponse;
    }

    private void logRequest(ChatClientRequest request) {
        LOG.info("request: {}", request);
    }

    private void logResponse(ChatClientResponse chatClientResponse) {
        LOG.info("response: {}", chatClientResponse);
    }
}
