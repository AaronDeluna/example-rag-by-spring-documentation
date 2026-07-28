package org.mirent.skills.tests.external;

import io.github.ivanmilovanov.agentic.cli.runner.model.AgentResultDto;
import io.github.ivanmilovanov.agentic.cli.runner.service.AgentRunnerService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mirent.skills.util.WutPreparer;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * E2E-тесты полного флоу анализа логов через stub-mcp-server.
 * <p>
 * Аналог {@link org.mirent.skills.tests.exemple.SimpleTests}, но для скилла
 * {@code log-analysis}: агент (Qwen CLI) поднимает локальный stub-MCP-сервер как
 * stdio-процесс и проходит сценарии из {@code stub-mcp-server/doc/}.
 * </p>
 * <p>
 * Перед запуском {@code @BeforeAll} собирает рабочую область из шаблона
 * {@code log-analysis-without-subagent} и подставляет реальный путь к jar-у в
 * {@code .qwen/settings.json}. Тесты помечены {@code @Tag("external")} — им нужен
 * установленный Qwen CLI и собранный {@code stub-mcp-server.jar}.
 *
 * <p>Предварительно собрать jar-приложение:</p>
 * {@code mvn -pl stub-mcp-server clean install}
 * </p>
 */
@Tag("external")
@Slf4j
class LogAnalysisTests {

    /** Плейсхолдер пути к jar-у в шаблонном {@code .qwen/settings.json}. */
    private static final String JAR_PLACEHOLDER = "__STUB_MCP_JAR_PATH__";

    /** Единый раннер агента на все тесты класса. */
    private static AgentRunnerService agentRunner;

    private static Path jar;

    @BeforeAll
    static void setup() throws Exception {
        // 1. Находим собранный jar stub-mcp-server относительно рабочей директории модуля skills.
        jar = Paths.get(System.getProperty("user.dir"))
                .resolve("../stub-mcp-server/target/stub-mcp-server.jar")
                .toRealPath();
        log.info("Путь к stub-mcp-server.jar: {}", jar);
    }

    @BeforeEach
    void setUp() throws IOException {
        // 2. Готовим одноразовую рабочую область из шаблона log-analysis-without-subagent.
        Path workspace = WutPreparer.builder()
                .wutSourceName("log-analysis-without-subagent")
                .wutSourcePath(Path.of("src/test/resources/wut-templates"))
                .build()
                .prepare();
        log.info("Рабочая область (workspace): {}", workspace.toAbsolutePath());

        // 3. Подставляем реальный путь к jar-у в .qwen/settings.json подготовленной области.
        Path settings = workspace.resolve(".qwen/settings.json");
        String content = Files.readString(settings, StandardCharsets.UTF_8);
        // Экранируем обратные слэши, чтобы путь оставался валидным JSON (актуально для Windows).
        String jarJsonValue = jar.toString().replace("\\", "\\\\");
        String patched = content.replace(JAR_PLACEHOLDER, jarJsonValue);
        Files.writeString(settings, patched, StandardCharsets.UTF_8);
        log.info("MCP-конфиг обновлён, jar подставлен в {}", settings);

        // 4. Один раннер на все тесты.
        agentRunner = new AgentRunnerService(workspace);
    }

    @Test
    @DisplayName("Сценарий A1: известное событие сразу найдено в хранилище (bugfix)")
    void scenarioKnownEventFoundInStorage() throws Exception {
        AgentResultDto result = agentRunner.executeSkill(
                "log-analysis", "Проанализируй логи: s3://runs/npe-run/logs.tar.gz");

        String answer = requireNonBlankAnswer(result);
        assertTrue(containsAny(answer, "bugfix", "FOUND", "classification"),
                "Ожидался отчёт по известному событию (bugfix/FOUND/classification), фактически: " + answer);
    }

    @Test
    @DisplayName("Сценарий A2: новое событие, данных достаточно (bugfix)")
    void scenarioNewEventEnoughDataBugfix() throws Exception {
        AgentResultDto result = agentRunner.executeSkill(
                "log-analysis", "Проанализируй логи: s3://runs/npe-run/logs.tar.gz");

        String answer = requireNonBlankAnswer(result);
        assertTrue(containsAny(answer, "bugfix", "FOUND", "classification"),
                "Ожидался отчёт с достаточностью данных (bugfix/FOUND/classification), фактически: " + answer);
    }

    @Test
    @DisplayName("Сценарий A3: нужно дообогащение, петля разрешается (Jenkins)")
    void scenarioNewEventNeedMoreInfoResolved() throws Exception {
        AgentResultDto result = agentRunner.executeSkill(
                "log-analysis", "Проанализируй логи: s3://runs/jenkins-build-456/logs.tar.gz");

        String answer = requireNonBlankAnswer(result);
        assertTrue(containsAny(answer, "classification", "bugfix", "completed", "report"),
                "Ожидался итоговый отчёт после петли дообогащения, фактически: " + answer);
    }

    @Test
    @DisplayName("Сценарий A3 (исчерпано): петля не разрешилась, инцидент")
    void scenarioNewEventNeedMoreInfoExhaustedIncident() throws Exception {
        AgentResultDto result = agentRunner.executeSkill(
                "log-analysis", "Проанализируй логи: s3://runs/unknown-service/logs.tar.gz");

        String answer = requireNonBlankAnswer(result);
        assertTrue(containsAny(answer, "incident", "NOT_FOUND"),
                "Ожидался инцидент/NOT_FOUND при неразрешённой петле, фактически: " + answer);
    }

    @Test
    @DisplayName("Сценарий B: инсайт за период")
    void scenarioPeriodInsight() throws Exception {
        AgentResultDto result = agentRunner.executeSkill(
                "log-analysis", "Проанализируй логи: s3://reports/period-2026-07/summary.tar.gz");

        String answer = requireNonBlankAnswer(result);
        String lower = answer.toLowerCase();
        assertTrue(lower.contains("insight") || lower.contains("инсайт"),
                "Ожидался инсайт за период (insight/инсайт), фактически: " + answer);
    }

    /** Извлекает финальный ответ агента и проверяет, что он не null и не пустой. */
    private String requireNonBlankAnswer(AgentResultDto result) {
        assertNotNull(result, "Результат агента не должен быть null");
        String answer = result.getFinalResult();
        log.info("Ответ агента:\n{}", answer);
        assertNotNull(answer, "Финальный ответ агента не должен быть null");
        assertFalse(answer.isBlank(), "Финальный ответ агента не должен быть пустым");
        return answer;
    }

    /** true, если строка содержит хотя бы одну из подстрок (с учётом регистра). */
    private boolean containsAny(String text, String... needles) {
        for (String needle : needles) {
            if (text.contains(needle)) {
                return true;
            }
        }
        return false;
    }
}
