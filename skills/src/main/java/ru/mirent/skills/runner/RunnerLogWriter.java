package ru.mirent.skills.runner;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.extern.slf4j.Slf4j;
import ru.mirent.skills.dto.log.AgentRunLogDto;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Slf4j
public class RunnerLogWriter {

    private static final String RUNS_DIR_SUFFIX = "-runs";
    private static final String LOG_FILE = "runner-log.json";

    private final ObjectMapper objectMapper = new ObjectMapper()
            .enable(SerializationFeature.INDENT_OUTPUT);

    public void write(Path workspace, AgentRunLogDto entry) {
        try {
            Path runDir = workspace
                    .resolveSibling(workspace.getFileName() + RUNS_DIR_SUFFIX)
                    .resolve(entry.getRunId());
            Files.createDirectories(runDir);
            Path logPath = runDir.resolve(LOG_FILE);
            objectMapper.writeValue(logPath.toFile(), entry);
            log.info("Лог запуска агента сохранён: {}", logPath);
        } catch (IOException e) {
            log.warn("Не удалось записать лог запуска агента", e);
        }
    }
}
