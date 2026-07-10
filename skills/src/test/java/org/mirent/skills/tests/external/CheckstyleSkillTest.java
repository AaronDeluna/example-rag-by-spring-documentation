package org.mirent.skills.tests.external;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mirent.skills.CommandExecutor;
import org.mirent.skills.dto.agent.AgentResultDto;
import org.mirent.skills.parser.AgentStreamJsonParser;
import org.mirent.skills.runner.RunnerLogWriter;
import org.mirent.skills.runner.qwen.QwenAgentRunner;
import org.mirent.skills.service.AgentRunnerFactory;
import org.mirent.skills.service.AgentRunnerProperties;
import org.mirent.skills.util.WutPreparer;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mirent.skills.matcher.AgentMatcher.assertSingleSkillCall;
import static org.mirent.skills.matcher.AgentMatcher.assertSuccessful;

@Tag("external")
@Slf4j
class CheckstyleSkillTest {

    private static final Duration AGENT_TIMEOUT = Duration.ofMinutes(10);

    @BeforeEach
    void setUp() {
        String value = System.getenv("QWEN_CUSTOM_API_KEY_OPENAI_HTTPS_FOUNDATION_MODELS_API_CLOUD_RU_V1");
        assertNotNull(value);
    }

    @Test
    @DisplayName("Выполняет скилл checkstyle-simple-check и проверяет запуск Checkstyle с кастомными правилами (базовая проверка)")
    void userPromptRunsCheckstyleSimpleChecks() throws Exception {
        log.info("=== Запуск теста: userPromptRunsCheckstyleSimpleChecks ===");

        // 1. Подготовка рабочей директории (wut)
        Path wut = WutPreparer.builder()
                .wutSourceName("skill-test-checkstyle")
                .wutSourcePath(Path.of("src/test/resources/wut-templates"))
                .build()
                .prepare();
        log.info("WUT подготовлена: {}", wut.toAbsolutePath());

        // 2. Создание агента через фабрику с кастомным таймаутом
        AgentRunnerFactory factory = new AgentRunnerFactory(
                new CommandExecutor(),
                new AgentStreamJsonParser(),
                wut,
                AGENT_TIMEOUT,
                new RunnerLogWriter()
        );
        QwenAgentRunner agentRunner = factory.create(AgentRunnerProperties.loadDefault());
        log.info("Таймаут выполнения qwen: {} минут", AGENT_TIMEOUT.toMinutes());

        // 3. Запуск скилла через пользовательский промпт
        String prompt = "Запустить проверку проекта с помощью Checkstyle с кастомными правилами";
        log.info("Отправка промпта агенту (длина {} символов)", prompt.length());
        AgentResultDto result = agentRunner.executeUserPrompt(prompt);
        log.info("Идентификатор сессии: {}", result.getEvents().get(0).get("uuid"));

        // 4. Проверка выполнения агента
        assertSuccessful(result);
        assertSingleSkillCall(result, "checkstyle-simple-check");

        // 5. Проверка, что исходный Java-файл был создан
        Path sourceDir = wut.resolve("src/main/java");
        assertTrue(Files.exists(sourceDir), "Директория src/main/java не создана");
        assertTrue(Files.isDirectory(sourceDir), "src/main/java не является директорией");

        List<Path> javaFiles;
        try (Stream<Path> paths = Files.walk(sourceDir)) {
            javaFiles = paths.filter(p -> p.toString().endsWith(".java")).toList();
        }
        log.info("Найдено {} Java-файлов в src/main/java", javaFiles.size());
        assertFalse(javaFiles.isEmpty(), "Не найден ни один сгенерированный Java-файл в src/main/java");

        // 6. Проверка финального результата агента — в нём должны быть результаты Checkstyle
        String finalResult = result.getFinalResult();
        log.info("Финальный ответ агента:\n{}", finalResult);
        assertNotNull(finalResult, "Финальный результат не должен быть null");
        assertFalse(finalResult.isBlank(), "Финальный результат не должен быть пустым");

        // Проверяем наличие ошибки Checkstyle о System.out.println
        assertTrue(finalResult.contains("[ERROR]") && finalResult.contains("System.out.println"),
                "Финальный результат должен содержать ошибку Checkstyle о System.out.println: " + finalResult);
    }
}
