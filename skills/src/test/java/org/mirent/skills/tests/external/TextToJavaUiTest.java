package org.mirent.skills.tests.external;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Disabled;
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

/**
 * Интеграционный тест для скилла <b>generate-java-selenide-test</b>.
 * <p>
 * Скилл предназначен для генерации исполняемых Selenium-тестов на Java на основе
 * текстового описания тест-кейса. Тестовый класс содержит два сценария,
 * различающихся по глубине проверки:
 * <ul>
 *   <li><b>{@link #userPromptGeneratesSeleniumJavaTest()}</b> – базовый сценарий:
 *       проверяет, что агент вызывает нужный скилл и создаёт хотя бы один Java-файл,
 *       содержащий типичные элементы Selenium-теста (импорты {@code org.openqa.selenium},
 *       аннотация {@code @Test}, работа с {@code WebDriver}).</li>
 *   <li><b>{@link #userPromptGeneratesSeleniumJavaWithEvaluatorTest()}</b> – расширенный сценарий:
 *       помимо базовой проверки дополнительно выполняет:
 *       <ul>
 *         <li>компиляцию проекта (фазы {@code compile} и {@code test-compile}) для
 *             подтверждения синтаксической корректности сгенерированного кода;</li>
 *         <li>оценку качества ответа агента через {@link AgentEvaluatorService}
 *             с пороговым значением 0.7;</li>
 *         <li>запуск сгенерированного теста {@code DuckDuckGoSearchTest}
 *             (с игнорированием возможных падений, чтобы проверить сам факт выполнения).</li>
 *       </ul>
 *       Этот тест обеспечивает более строгую валидацию, приближенную к реальному CI-пайплайну.
 * </ul>
 * Оба теста используют общую подготовку рабочего окружения (WUT) из шаблона
 * {@code frap-mcp-testing} и запускают агента {@link QwenAgentRunner} с увеличенным таймаутом.
 * </p>
 */
@Tag("external")
@Slf4j
class TextToJavaUiTest {

    /**
     * Базовый тест: генерация Selenium-теста и простая проверка содержимого.
     * <p>
     * После успешного выполнения агента проверяется наличие сгенерированного Java-файла
     * в директории {@code src/test/java}, содержащего ключевые признаки Selenium-кода.
     * Этот тест служит быстрой проверкой работоспособности скилла.
     * </p>
     */
    @Disabled("TASK-006: требуется CommandFactory после TASK-004")
    @Test
    @DisplayName("Выполняет скилл generate-java-selenide-test и проверяет создание Selenium-теста (базовая проверка)")
    void userPromptGeneratesSeleniumJavaTest() throws Exception {
        log.info("=== Запуск теста: userPromptGeneratesSeleniumJavaTest ===");

        // 1. Подготовка рабочей директории (wut)
        log.debug("Подготовка WUT из шаблона 'frap-mcp-testing'...");
        Path wut = WutPreparer.builder()
                .wutSourceName("frap-mcp-testing")
                .wutSourcePath(Path.of("src/test/resources/wut-templates"))
                .build()
                .prepare();
        log.info("WUT подготовлена: {}", wut.toAbsolutePath());

        // 2. Запуск агента с увеличенным таймаутом
        log.debug("Создание агента QwenAgentRunner с таймаутом 10 мин...");
        QwenAgentRunner agentRunner = new QwenAgentRunner(
                new CommandExecutor(),
                new AgentStreamJsonParser(),
                new org.mirent.skills.runner.RunnerLogWriter(),
                wut,
                Duration.ofMinutes(10),
                (prompt, logDir) -> java.util.List.of("qwen", prompt)
        );

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
        AgentResultDto result = agentRunner.executeUserPrompt(prompt);
        log.info("Идентификатор сессии: {}", result.getEvents().get(0).get("uuid"));
        log.debug("Полный результат: {}", result);

        // 4. Проверка выполнения агента
        log.debug("Проверка успешности выполнения агента...");
        assertSuccessful(result);
        assertSingleSkillCall(result, "generate-java-selenide-test");
        log.info("Агент отработал успешно, скилл вызван.");

        // 5. Проверка результата работы скилла — создан Java-файл с Selenium-тестом
        Path testDir = wut.resolve("src/test/java");
        log.debug("Проверка наличия директории {}", testDir);
        assertTrue(Files.exists(testDir), "Директория src/test/java не создана");
        assertTrue(Files.isDirectory(testDir), "src/test/java не является директорией");

        // 5.1. Собираем все сгенерированные .java файлы
        log.debug("Поиск всех .java файлов в {}", testDir);
        List<Path> javaFiles;
        try (Stream<Path> paths = Files.walk(testDir)) {
            javaFiles = paths.filter(p -> p.toString().endsWith(".java")).toList();
        }
        log.info("Найдено {} Java-файлов", javaFiles.size());
        if (log.isDebugEnabled()) {
            javaFiles.forEach(p -> log.debug("  -> {}", p.getFileName()));
        }
        assertFalse(javaFiles.isEmpty(), "Не найден ни один сгенерированный Java-файл");

        // 5.2. Проверяем, что хотя бы один файл содержит Selenium-код
        log.debug("Проверка содержимого файлов на наличие Selenium-кода...");
        boolean hasSeleniumTest = javaFiles.stream().anyMatch(path -> {
            try {
                String content = Files.readString(path);
                boolean matches = content.contains("org.openqa.selenium")
                        && content.contains("@Test")
                        && (content.toLowerCase().contains("duckduckgo")
                        || content.toLowerCase().contains("search")
                        || content.contains("WebDriver"));
                if (matches) {
                    log.debug("Файл {} соответствует Selenium-тесту", path.getFileName());
                }
                return matches;
            } catch (Exception e) {
                log.warn("Не удалось прочитать файл {}: {}", path, e.getMessage());
                return false;
            }
        });
        assertTrue(hasSeleniumTest, "Среди сгенерированных файлов нет Selenium-теста");

        log.info("Сгенерированные Java-файлы ({}): {}", javaFiles.size(), javaFiles);
    }

    /**
     * Расширенный тест: генерация Selenium-теста с последующей компиляцией, оценкой и запуском.
     * <p>
     * В дополнение к базовой проверке данный тест выполняет:
     * <ul>
     *   <li>компиляцию проекта через Maven, чтобы убедиться в синтаксической корректности;</li>
     *   <li>оценку качества ответа агента через {@code AgentEvaluatorService} с порогом 0.7;</li>
     *   <li>запуск сгенерированного теста {@code DuckDuckGoSearchTest} (даже если он падает,
     *       это позволяет проверить, что тест вообще был выполнен).</li>
     * </ul>
     * Этот сценарий имитирует более строгую проверку в CI-среде.
     * </p>
     */
    @Disabled
    @Test
    @DisplayName("Выполняет скилл generate-java-selenide-test, компилирует, оценивает и запускает сгенерированный тест")
    void userPromptGeneratesSeleniumJavaWithEvaluatorTest() throws Exception {
        log.info("=== Запуск теста: userPromptGeneratesSeleniumJavaWithEvaluatorTest ===");

        log.debug("Подготовка WUT из шаблона 'frap-mcp-testing'...");
        Path wut = WutPreparer.builder()
                .wutSourceName("frap-mcp-testing")
                .wutSourcePath(Path.of("src/test/resources/wut-templates"))
                .build()
                .prepare();
        log.info("WUT подготовлена: {}", wut.toAbsolutePath());

        log.debug("Создание агента QwenAgentRunner...");
        QwenAgentRunner agentRunner = new QwenAgentRunner(
                new CommandExecutor(),
                new AgentStreamJsonParser(),
                new org.mirent.skills.runner.RunnerLogWriter(),
                wut,
                Duration.ofMinutes(10),
                (prompt, logDir) -> java.util.List.of("qwen", prompt)
        );

        String prompt = "Тест-кейс: Поиск в DuckDuckGo... используй skills generate-java-selenide-test";
        log.info("Отправка промпта агенту (длина {} символов)", prompt.length());
        AgentResultDto result = agentRunner.executeUserPrompt(prompt);
        log.info("Идентификатор сессии: {}", result.getEvents().get(0).get("uuid"));
        log.debug("Полный результат: {}", result);

        // 1. Проверка процесса
        log.debug("Проверка успешности выполнения агента...");
        assertSuccessful(result);
        assertSingleSkillCall(result, "generate-java-selenide-test");
        log.info("Агент отработал успешно, скилл вызван.");

        // 2. Проверка файлов
        Path testDir = wut.resolve("src/test/java");
        log.debug("Проверка наличия директории {}", testDir);
        assertTrue(Files.exists(testDir));

        log.debug("Поиск всех .java файлов в {}", testDir);
        List<Path> javaFiles;
        try (Stream<Path> paths = Files.walk(testDir)) {
            javaFiles = paths.filter(p -> p.toString().endsWith(".java")).toList();
        }
        log.info("Найдено {} Java-файлов", javaFiles.size());
        if (log.isDebugEnabled()) {
            javaFiles.forEach(p -> log.debug("  -> {}", p.getFileName()));
        }
        assertFalse(javaFiles.isEmpty());

        // 3. Проверка содержимого
        log.debug("Проверка содержимого файлов на наличие Selenium-элементов...");
        boolean hasSeleniumTest = javaFiles.stream().anyMatch(path -> {
            try {
                String content = Files.readString(path);
                boolean matches = content.contains("org.openqa.selenium")
                        && content.contains("@Test")
                        && content.contains("WebDriver")
                        && content.contains("searchInput");
                if (matches) {
                    log.debug("Файл {} соответствует ожидаемому Selenium-тесту", path.getFileName());
                }
                return matches;
            } catch (Exception e) {
                log.warn("Не удалось прочитать файл {}: {}", path, e.getMessage());
                return false;
            }
        });
        assertTrue(hasSeleniumTest, "Сгенерированный код должен содержать Selenium-элементы");
        log.info("Selenium-тест найден.");

        // 4. Проверка компиляции (уже есть)
        log.info("Выполнение 'mvn compile test-compile' в {}", wut);
        Process compile = Runtime.getRuntime().exec("mvn compile test-compile", null, wut.toFile());
        int compileExit = compile.waitFor();
        log.info("Компиляция завершена с кодом возврата {}", compileExit);
        assertEquals(0, compileExit);

        // 5. Оценка судьи
        log.debug("Запуск оценки агента через AgentEvaluatorService...");
        AgentEvaluatorService evaluator = new AgentEvaluatorService(wut);
        EvaluateResultDto evaluation = evaluator.evaluate(
                new EvaluateDto(prompt, result.getEventsJson())
        );
        log.info("Результат оценки: score={}", evaluation.getScore());
        AgentMatcher.evaluate(evaluation, 0.7);
        log.info("Оценка пройдена (порог 0.7)");

        // 6. Проверка, что тест запустился (не обязательно проходит)
        log.info("Запуск теста 'DuckDuckGoSearchTest' (игнорируем падения)...");
        Process testRun = Runtime.getRuntime().exec(
                "mvn test -Dtest=DuckDuckGoSearchTest -Dmaven.test.failure.ignore=true",
                null, wut.toFile()
        );
        int testExit = testRun.waitFor();
        log.info("Запуск тестов завершён с кодом возврата {}", testExit);
        assertEquals(0, testRun.waitFor()); // exitCode 0 даже при падении тестов
    }
}
