package org.mirent.skills.tests.external;

import lombok.extern.slf4j.Slf4j;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mirent.skills.matcher.AgentMatcher.assertAtLeastOneSkillCall;
import static org.mirent.skills.matcher.AgentMatcher.assertSuccessful;

@Tag("external")
@Slf4j
class TextToJavaUiFrapPlaywrightTest {

    private static final Duration AGENT_TIMEOUT = Duration.ofMinutes(10);
    private static final String SKILL_NAME = "generate-java-playwright-test";
    private static final String WUT_TEMPLATE = "frap-mcp-testing-playwright";

    @Test
    @DisplayName("Генерирует Playwright-тест и сохраняет временные артефакты в .frap-work")
    void userPromptGeneratesPlaywrightJavaTestAndFrapWork() throws Exception {
        log.info("=== Запуск теста: userPromptGeneratesPlaywrightJavaTestAndFrapWork ===");

        // 1. Подготовка рабочей директории (wut) из шаблона для Playwright
        Path wut = WutPreparer.builder()
                .wutSourceName(WUT_TEMPLATE)
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
        String prompt = """
                Тест-кейс: Поиск в DuckDuckGo.
                Шаги:
                1. Открыть главную страницу DuckDuckGo (https://duckduckgo.com).
                2. Ввести в строку поиска текст 'Selenium WebDriver'.
                3. Нажать кнопку поиска (Enter).
                Ожидаемый результат: Отображается страница с результатами поиска, список с результатами содержит ссылки.
                используй skills """ + SKILL_NAME;
        log.info("Отправка промпта агенту (длина {} символов)", prompt.length());
        AgentResultDto result = agentRunner.executeUserPrompt(prompt);
        log.info("Идентификатор сессии: {}", result.getEvents().get(0).get("uuid"));

        // 4. Проверка выполнения агента
        assertSuccessful(result);
        assertAtLeastOneSkillCall(result, SKILL_NAME);

        // 5. Проверка наличия .frap-work и его содержимого
        Path frapWork = wut.resolve(".frap-work");
        assertTrue(Files.exists(frapWork) && Files.isDirectory(frapWork),
                ".frap-work не создан или не является директорией");

        // 5.1. Проверка папки pages
        Path pagesDir = frapWork.resolve("pages");
        assertTrue(Files.exists(pagesDir) && Files.isDirectory(pagesDir),
                "Папка pages не создана или не является директорией");

        List<Path> javaFiles;
        try (Stream<Path> files = Files.list(pagesDir)) {
            javaFiles = files.filter(p -> p.toString().endsWith(".java")).toList();
        }
        assertEquals(2, javaFiles.size(),
                "В папке pages должно быть ровно 2 Java-файла (найдено: " + javaFiles.size() + ")");
        log.info("Найдены Java-файлы в pages: {}", javaFiles);

        // 5.2. Проверка папки snapshot
        Path snapshotDir = frapWork.resolve("snapshot");
        assertTrue(Files.exists(snapshotDir) && Files.isDirectory(snapshotDir),
                "Папка snapshot не создана или не является директорией");

        List<Path> jsonFiles;
        try (Stream<Path> files = Files.list(snapshotDir)) {
            jsonFiles = files.filter(p -> p.toString().endsWith(".json")).toList();
        }
        assertEquals(2, jsonFiles.size(),
                "В папке snapshot должно быть ровно 2 JSON-файла (найдено: " + jsonFiles.size() + ")");
        log.info("Найдены JSON-файлы в snapshot: {}", jsonFiles);

        // 6. Дополнительная проверка: тестовый класс в src/test/java (как было ранее)
        Path testDir = wut.resolve("src/test/java");
        assertTrue(Files.exists(testDir) && Files.isDirectory(testDir),
                "Директория src/test/java не создана или не является директорией");

        List<Path> testJavaFiles;
        try (Stream<Path> files = Files.walk(testDir)) {
            testJavaFiles = files.filter(p -> p.toString().endsWith(".java")).toList();
        }
        assertTrue(!testJavaFiles.isEmpty(), "Не найден ни один сгенерированный тестовый Java-файл");
        log.info("Найдено {} тестовых Java-файлов", testJavaFiles.size());
    }
}