package org.mirent.skills.tests.external;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mirent.skills.CommandExecutor;
import org.mirent.skills.dto.agent.AgentResultDto;
import org.mirent.skills.dto.evaluate.EvaluateDto;
import org.mirent.skills.dto.evaluate.EvaluateResultDto;
import org.mirent.skills.matcher.AgentMatcher;
import org.mirent.skills.parser.AgentStreamJsonParser;
import org.mirent.skills.runner.qwen.QwenAgentRunner;
import org.mirent.skills.service.AgentEvaluatorService;
import org.mirent.skills.util.WutPreparer;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mirent.skills.matcher.AgentMatcher.assertSingleSkillCall;
import static org.mirent.skills.matcher.AgentMatcher.assertSuccessful;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("external")
@Slf4j
class TextToJavaUiTest {

    @Test
    @DisplayName("Выполняет скилл text-to-java-ui-test и проверяет создание Selenium-теста")
    void userPromptGeneratesSeleniumJavaTest() throws Exception {
        // 1. Подготовка рабочей директории (wut)
        Path wut = WutPreparer.builder()
                .wutSourceName("frap-mcp-testing")
                .wutSourcePath(Path.of("src/test/resources/wut-templates"))
                .overwriteTarget(true)
                .build()
                .prepare();

        // 2. Запуск агента с увеличенным таймаутом
        QwenAgentRunner agentRunner = new QwenAgentRunner(
                new CommandExecutor(),
                new AgentStreamJsonParser(),
                wut,
                Duration.ofMinutes(10)
        );

        // 3. Запуск скилла через пользовательский промпт
        // Модель сама решает вызвать tool_use с name="skill" и input.skill="text-to-java-ui-test"
        String prompt = """
                Тест-кейс: Поиск в DuckDuckGo.
                Шаги:
                1. Открыть главную страницу DuckDuckGo (https://duckduckgo.com).
                2. Ввести в строку поиска текст 'Selenium WebDriver'.
                3. Нажать кнопку поиска (Enter).
                Ожидаемый результат: Отображается страница с результатами поиска, список с результатами содержит ссылки.
                используй skills text-to-java-ui-test""";
        AgentResultDto result = agentRunner.executeUserPrompt(prompt);
        log.info("Идентификатор сессии: {}", result.getEvents().get(0).get("uuid"));

        // 4. Проверка выполнения агента
        assertSuccessful(result);
        assertSingleSkillCall(result, "text-to-java-ui-test");

        // 5. Проверка результата работы скилла — создан Java-файл с Selenium-тестом
        Path testDir = wut.resolve("src/test/java");
        assertTrue(Files.exists(testDir), "Директория src/test/java не создана");
        assertTrue(Files.isDirectory(testDir), "src/test/java не является директорией");

        // 5.1. Собираем все сгенерированные .java файлы
        List<Path> javaFiles;
        try (Stream<Path> paths = Files.walk(testDir)) {
            javaFiles = paths.filter(p -> p.toString().endsWith(".java")).toList();
        }
        assertFalse(javaFiles.isEmpty(), "Не найден ни один сгенерированный Java-файл");

        // 5.2. Проверяем, что хотя бы один файл содержит Selenium-код
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
    void userPromptGeneratesSeleniumJavaWithEvaluatorTest() throws Exception {
        Path wut = WutPreparer.builder()
                .wutSourceName("frap-mcp-testing")
                .wutSourcePath(Path.of("src/test/resources/wut-templates"))
                .overwriteTarget(true)
                .build()
                .prepare();

        QwenAgentRunner agentRunner = new QwenAgentRunner(
                new CommandExecutor(),
                new AgentStreamJsonParser(),
                wut,
                Duration.ofMinutes(10)
        );

        String prompt = "Тест-кейс: Поиск в DuckDuckGo... используй skills text-to-java-ui-test";
        AgentResultDto result = agentRunner.executeUserPrompt(prompt);

        // 1. Проверка процесса
        assertSuccessful(result);
        assertSingleSkillCall(result, "text-to-java-ui-test");

        // 2. Проверка файлов
        Path testDir = wut.resolve("src/test/java");
        assertTrue(Files.exists(testDir));

        List<Path> javaFiles;
        try (Stream<Path> paths = Files.walk(testDir)) {
            javaFiles = paths.filter(p -> p.toString().endsWith(".java")).toList();
        }
        assertFalse(javaFiles.isEmpty());

        // 3. Проверка содержимого
        boolean hasSeleniumTest = javaFiles.stream().anyMatch(path -> {
            try {
                String content = Files.readString(path);
                return content.contains("org.openqa.selenium")
                        && content.contains("@Test")
                        && content.contains("WebDriver")
                        && content.contains("searchInput");
            } catch (Exception e) {
                return false;
            }
        });
        assertTrue(hasSeleniumTest, "Сгенерированный код должен содержать Selenium-элементы");

        // 4. Проверка компиляции (уже есть)
        Process compile = Runtime.getRuntime().exec("mvn compile test-compile", null, wut.toFile());
        assertEquals(0, compile.waitFor());

        // 5. Оценка судьи
        AgentEvaluatorService evaluator = new AgentEvaluatorService(wut);
        EvaluateResultDto evaluation = evaluator.evaluate(
                new EvaluateDto(prompt, result.getEventsJson())
        );
        AgentMatcher.evaluate(evaluation, 0.7);

        // 6. Проверка, что тест запустился (не обязательно проходит)
        Process testRun = Runtime.getRuntime().exec(
                "mvn test -Dtest=DuckDuckGoSearchTest -Dmaven.test.failure.ignore=true",
                null, wut.toFile()
        );
        assertEquals(0, testRun.waitFor()); // exitCode 0 даже при падении тестов
    }
}
