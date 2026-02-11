package ru.mirent.stdio.listeners;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationPreparedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

@Component
public class ApplicationPreparedEventListener implements ApplicationListener<ApplicationPreparedEvent> {
    private static final Logger LOG = LoggerFactory.getLogger(ApplicationPreparedEventListener.class);

    public ApplicationPreparedEventListener() {
        LOG.info("Инициализация: {}", this.hashCode());
    }

    @Override
    public void onApplicationEvent(ApplicationPreparedEvent event) {
        String[] beanNames = event.getApplicationContext().getBeanDefinitionNames();
        LOG.info("Список проинициализированных бинов:");
        for (String beanName : beanNames) {
            LOG.info("  " + beanName);
        }
    }
}
