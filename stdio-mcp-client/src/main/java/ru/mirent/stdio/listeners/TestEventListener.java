package ru.mirent.stdio.listeners;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import ru.mirent.stdio.providers.ApplicationContextProvider;
import ru.mirent.stdio.events.TestEvent;

public class TestEventListener {
    private static final Logger LOG = LoggerFactory.getLogger(TestEventListener.class);

    @Deprecated(forRemoval = true)
    private ApplicationEventPublisher applicationEventPublisher;

    public TestEventListener(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
        LOG.info("Инициализация: {}", this.hashCode());
    }

    @EventListener
    public void testEventCollector(TestEvent event) {
        LOG.info("Получено тестовое событие: " + event.toString());

        ApplicationContext applicationContext = ApplicationContextProvider.getApplicationContext();
        String[] beanNames = applicationContext.getBeanDefinitionNames();
//        LOG.info("Список проинициализированных бинов:");
//        for (String beanName : beanNames) {
//            LOG.info("  " + beanName);
//        }
    }
}
