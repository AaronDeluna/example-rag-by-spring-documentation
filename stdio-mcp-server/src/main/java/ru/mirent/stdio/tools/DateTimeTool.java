package ru.mirent.stdio.tools;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.context.i18n.LocaleContextHolder;

import java.time.LocalDateTime;

public class DateTimeTool {
    private static final Logger LOG = LoggerFactory.getLogger(DateTimeTool.class);

    @Tool(description = "Получить текущую дату и время в таймзоне пользователя")
    public String getDateTime() {
        LOG.info("Вызван инструмент: {}", this.getClass().getSimpleName());
        return LocalDateTime.now().atZone(LocaleContextHolder.getTimeZone().toZoneId()).toString();
    }
}
