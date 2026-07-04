package org.mirent.skills.runner;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.extern.slf4j.Slf4j;
import org.mirent.skills.dto.log.AgentRunLogDto;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Slf4j
public class RunnerLogWriter {

    private static final String LOG_FILE = "log.json";

    private final ObjectMapper objectMapper = new ObjectMapper()
            .enable(SerializationFeature.INDENT_OUTPUT);

    public void write(AgentRunContext context, AgentRunLogDto entry) {
        try {
            Files.createDirectories(context.getRunDir());
            Path logPath = context.getRunDir().resolve(LOG_FILE);
            objectMapper.writeValue(logPath.toFile(), entry);
            log.info("Лог запуска агента сохранён: {}", logPath);
        } catch (IOException e) {
            log.warn("Не удалось записать лог запуска агента", e);
        }
    }
}
