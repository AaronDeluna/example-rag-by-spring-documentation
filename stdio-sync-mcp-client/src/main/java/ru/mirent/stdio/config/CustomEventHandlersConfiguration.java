package ru.mirent.stdio.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.mirent.stdio.listeners.TestEventListener;
import ru.mirent.stdio.providers.ApplicationContextProvider;

@Configuration
public class CustomEventHandlersConfiguration {
    private static final Logger LOG = LoggerFactory.getLogger(CustomEventHandlersConfiguration.class);

    public CustomEventHandlersConfiguration() {
        LOG.info("Инициализация: {}", this.hashCode());
    }

    @Bean
    public TestEventListener testEventRunService(ApplicationEventPublisher applicationEventPublisher) {
        LOG.info("Инициализация Bean testEventRunService");
        return new TestEventListener(applicationEventPublisher);
    }

    @Bean
    public ApplicationContextAware applicationContextAware() {
        LOG.info("Инициализация Bean applicationContextAware");
        return new ApplicationContextProvider();
    }
}
