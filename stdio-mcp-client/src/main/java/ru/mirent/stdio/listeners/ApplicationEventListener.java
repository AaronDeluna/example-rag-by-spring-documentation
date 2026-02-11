package ru.mirent.stdio.listeners;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

@Component
public class ApplicationEventListener implements ApplicationListener<ApplicationEvent> {
    private static final Logger LOG = LoggerFactory.getLogger(ApplicationEventListener.class);

    public ApplicationEventListener() {
        LOG.info("Инициализация: " + this.hashCode());
    }

    @Override
    public void onApplicationEvent(ApplicationEvent event) {
        LOG.info("{}: {}", this.hashCode(), event.getClass().getName());
    }
}
