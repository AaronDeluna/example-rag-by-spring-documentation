package ru.mirent.stdio.providers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

public class ApplicationContextProvider implements ApplicationContextAware {
    private static final Logger LOG = LoggerFactory.getLogger(ApplicationContextProvider.class);

    private static ApplicationContext applicationContext;

    public ApplicationContextProvider() {
        LOG.info("Инициализация: {}", this.hashCode());
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        ApplicationContextProvider.applicationContext = applicationContext;
    }

    public static ApplicationContext getApplicationContext() {
        return applicationContext;
    }
}
