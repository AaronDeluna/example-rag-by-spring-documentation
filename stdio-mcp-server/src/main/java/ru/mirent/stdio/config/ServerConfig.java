package ru.mirent.stdio.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.mirent.stdio.tools.DateTimeTool;
import ru.mirent.stdio.tools.DocCreator;
import ru.mirent.stdio.tools.FileOpenTool;

@Configuration
public class ServerConfig {
    private static final Logger LOG = LoggerFactory.getLogger(ServerConfig.class);

    public ServerConfig() {
        LOG.info("Инициализация: {}", this.hashCode());
    }

    @Bean
    public ToolCallbackProvider getToolCallbackProvider() {
        LOG.info("Настройка всех инструментов сервера");
        return MethodToolCallbackProvider
                .builder()
                .toolObjects(
                        new FileOpenTool(),
                        new DateTimeTool(),
                        new DocCreator()
                ).build();
    }
}
