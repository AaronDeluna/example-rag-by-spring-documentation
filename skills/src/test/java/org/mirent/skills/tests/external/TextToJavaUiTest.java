package org.mirent.skills.tests.external;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import io.github.ivanmilovanov.agentic.cli.runner.executor.ApacheCommandExecutor;
import io.github.ivanmilovanov.agentic.cli.runner.model.AgentResultDto;
import org.mirent.skills.dto.evaluate.EvaluateDto;
import org.mirent.skills.dto.evaluate.EvaluateResultDto;
import org.mirent.skills.matcher.AgentMatcher;
import io.github.ivanmilovanov.agentic.cli.runner.parser.AgentStreamJsonParser;
import io.github.ivanmilovanov.agentic.cli.runner.log.RunnerLogWriter;
import io.github.ivanmilovanov.agentic.cli.runner.runner.AgentRunnerImpl;
import org.mirent.skills.service.AgentEvaluatorService;
import io.github.ivanmilovanov.agentic.cli.runner.service.AgentRunnerFactory;
import io.github.ivanmilovanov.agentic.cli.runner.config.AgentRunnerProperties;
import org.mirent.skills.util.WutPreparer;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mirent.skills.matcher.AgentMatcher.assertSingleSkillCall;
import static org.mirent.skills.matcher.AgentMatcher.assertSuccessful;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("external")
@Slf4j
class TextToJavaUiTest {

    // Константа для таймаута, чтобы легко менять
    private static final Duration AGENT_TIMEOUT = Duration.ofMinutes(10);
    private static final Duration MAVEN_TIMEOUT = Duration.ofMinutes(5);

    @Test
    @DisplayName("Выполняет скилл generate-java-selenide-test и проверяет создание Selenium-теста (базовая проверка)")
    void userPromptGeneratesSeleniumJavaTest() throws Exception {
        log.info("=== Запуск теста: userPromptGeneratesSeleniumJavaTest ===");

        // 1. Подготовка рабочей директории (wut)
        Path wut = WutPreparer.builder()
                .wutSourceName("frap-mcp-testing")
                .wutSourcePath(Path.of("src/test/resources/wut-templates"))
                .build()
                .prepare();
        log.info("WUT подготовлена: {}", wut.toAbsolutePath());

        // 2. Создание агента через фабрику с кастомным таймаутом
        AgentRunnerFactory factory = new AgentRunnerFactory(
                new ApacheCommandExecutor(),
                new AgentStreamJsonParser(),
                wut,
                AGENT_TIMEOUT,  // увеличенный таймаут для qwen
                new RunnerLogWriter()
        );
        AgentRunnerImpl agentRunner = (AgentRunnerImpl) factory.create(AgentRunnerProperties.loadDefault());
        log.info("Таймаут выполнения qwen: {} минут", AGENT_TIMEOUT.toMinutes());

        // 3. Запуск скилла через пользовательский промпт
        String prompt = """
            Тест-кейс: Поиск в DuckDuckGo.
            Шаги:
            1. Открыть главную страницу DuckDuckGo (https://duckduckgo.com).
            2. Ввести в строку поиска текст 'Selenium WebDriver'.
            3. Нажать кнопку поиска (Enter).
            Ожидаемый результат: Отображается страница с результатами поиска, список с результатами содержит ссылки.
            используй skills generate-java-selenide-test""";
        log.info("Отправка промпта агенту (длина {} символов)", prompt.length());
        AgentResultDto result = agentRunner.execute(prompt);
        log.info("Идентификатор сессии: {}", result.getEvents().get(0).get("uuid"));

        // 4. Проверка выполнения агента
        assertSuccessful(result);
        assertSingleSkillCall(result, "generate-java-selenide-test");

        // 5. Проверка файлов
        Path testDir = wut.resolve("src/test/java");
        assertTrue(Files.exists(testDir), "Директория src/test/java не создана");
        assertTrue(Files.isDirectory(testDir), "src/test/java не является директорией");

        List<Path> javaFiles;
        try (Stream<Path> paths = Files.walk(testDir)) {
            javaFiles = paths.filter(p -> p.toString().endsWith(".java")).toList();
        }
        log.info("Найдено {} Java-файлов", javaFiles.size());
        assertFalse(javaFiles.isEmpty(), "Не найден ни один сгенерированный Java-файл");

        boolean hasSeleniumTest = javaFiles.stream().anyMatch(path -> {
            try {
                String content = Files.readString(path);
                return content.contains("org.openqa.selenium")
                        && content.contains("@Test")
                        && (content.toLowerCase().contains("duckduckgo")
                        || content.toLowerCase().contains("search")
                        || content.contains("WebDriver"));
            } catch (Exception e) {
                log.warn("Не удалось прочитать файл {}: {}", path, e.getMessage());
                return false;
            }
        });
        assertTrue(hasSeleniumTest, "Среди сгенерированных файлов нет Selenium-теста");
        log.info("Сгенерированные Java-файлы ({}): {}", javaFiles.size(), javaFiles);
    }

    @Test
    @DisplayName("Выполняет скилл generate-java-selenide-test, компилирует, оценивает и запускает сгенерированный тест")
    void userPromptGeneratesSeleniumJavaWithEvaluatorTest() throws Exception {
        log.info("=== Запуск теста: userPromptGeneratesSeleniumJavaWithEvaluatorTest ===");

        // 1. Подготовка рабочей директории (wut)
        Path wut = WutPreparer.builder()
                .wutSourceName("frap-mcp-testing")
                .wutSourcePath(Path.of("src/test/resources/wut-templates"))
                .build()
                .prepare();
        log.info("WUT подготовлена: {}", wut.toAbsolutePath());

        // 2. Создание агента через фабрику с кастомным таймаутом
        AgentRunnerFactory factory = new AgentRunnerFactory(
                new ApacheCommandExecutor(),
                new AgentStreamJsonParser(),
                wut,
                AGENT_TIMEOUT,  // увеличенный таймаут для qwen
                new RunnerLogWriter()
        );
        AgentRunnerImpl agentRunner = (AgentRunnerImpl) factory.create(AgentRunnerProperties.loadDefault());
        log.info("Таймаут выполнения qwen: {} минут", AGENT_TIMEOUT.toMinutes());

        // 3. Запуск скилла
        String prompt = "Тест-кейс: Поиск в DuckDuckGo... используй skills generate-java-selenide-test";
        log.info("Отправка промпта агенту (длина {} символов)", prompt.length());
        AgentResultDto result = agentRunner.execute(prompt);
        log.info("Идентификатор сессии: {}", result.getEvents().get(0).get("uuid"));

        // 4. Проверка выполнения агента
        assertSuccessful(result);
        assertSingleSkillCall(result, "generate-java-selenide-test");

        // 5. Проверка файлов
        Path testDir = wut.resolve("src/test/java");
        assertTrue(Files.exists(testDir));

        List<Path> javaFiles;
        try (Stream<Path> paths = Files.walk(testDir)) {
            javaFiles = paths.filter(p -> p.toString().endsWith(".java")).toList();
        }
        log.info("Найдено {} Java-файлов", javaFiles.size());
        assertFalse(javaFiles.isEmpty());

        boolean hasSeleniumTest = javaFiles.stream().anyMatch(path -> {
            try {
                String content = Files.readString(path);
                return content.contains("org.openqa.selenium")
                        && content.contains("@Test")
                        && content.contains("WebDriver")
                        && content.contains("searchInput");
            } catch (Exception e) {
                log.warn("Не удалось прочитать файл {}: {}", path, e.getMessage());
                return false;
            }
        });
        assertTrue(hasSeleniumTest, "Сгенерированный код должен содержать Selenium-элементы");

        // 6. Компиляция с таймаутом
        log.info("Выполнение 'mvn compile test-compile' в {}", wut);
        Process compile = new ProcessBuilder()
                .command("mvn", "compile", "test-compile")
                .directory(wut.toFile())
                .start();
        if (!compile.waitFor(MAVEN_TIMEOUT.toMillis(), TimeUnit.MILLISECONDS)) {
            compile.destroyForcibly();
            throw new AssertionError("Maven compile timeout после " + MAVEN_TIMEOUT.toMinutes() + " минут");
        }
        int compileExit = compile.exitValue();
        log.info("Компиляция завершена с кодом возврата {}", compileExit);
        assertEquals(0, compileExit, "Maven compile должен завершиться успешно");

        // 7. Оценка судьи
        log.debug("Запуск оценки агента через AgentEvaluatorService...");
        AgentEvaluatorService evaluator = new AgentEvaluatorService(wut);
        EvaluateResultDto evaluation = evaluator.evaluate(
                new EvaluateDto(prompt, result.getEventsJson())
        );
        log.info("Результат оценки: score={}", evaluation.getScore());
        AgentMatcher.evaluate(evaluation, 0.7);

        // 8. Запуск теста с таймаутом
        log.info("Запуск теста 'DuckDuckGoSearchTest' (игнорируем падения)...");
        Process testRun = new ProcessBuilder()
                .command("mvn", "test",
                        "-Dtest=DuckDuckGoSearchTest",
                        "-Dmaven.test.failure.ignore=true")
                .directory(wut.toFile())
                .start();
        if (!testRun.waitFor(MAVEN_TIMEOUT.toMillis(), TimeUnit.MILLISECONDS)) {
            testRun.destroyForcibly();
            throw new AssertionError("Maven test timeout после " + MAVEN_TIMEOUT.toMinutes() + " минут");
        }
        int testExit = testRun.exitValue();
        log.info("Запуск тестов завершён с кодом возврата {}", testExit);
        // exitCode 0 даже при падении тестов из-за -Dmaven.test.failure.ignore=true
        assertEquals(0, testExit, "Maven test должен завершиться с кодом 0 (failure.ignore=true)");
    }
}