package org.mirent.skills.tests.exemple;

import io.github.ivanmilovanov.agentic.cli.runner.model.AgentResultDto;
import io.github.ivanmilovanov.agentic.cli.runner.service.AgentRunnerService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mirent.skills.util.WutPreparer;

import java.nio.file.Path;

import static org.mirent.skills.matcher.AgentMatcher.assertSingleSkillCall;

public class SimpleTests {

    @Test
    @DisplayName("Неявный вызов скилла: агент сам решает вызвать arithmetic по пользовательскому запросу")
    void test() throws Exception {
        // 1. Подготовка рабочей директории (wut) — в ней находится набор скиллов для тестирования
        Path workspace = WutPreparer.builder()
                .wutSourceName("default")
                .wutSourcePath(Path.of("src/test/resources/wut-templates"))
                .build()
                .prepare();

        // 2. Создание раннера агента через фасад SDK
        AgentRunnerService agentRunner = new AgentRunnerService(workspace);

        AgentResultDto result = agentRunner.execute("Сколько будет 2 + 2");

        assertSingleSkillCall(result, "arithmetic");
    }

    @Test
    @DisplayName("Явный вызов скилла: arithmetic вызывается напрямую через executeSkill")
    void test2() throws Exception {
        // 1. Подготовка рабочей директории (wut) — в ней находится набор скиллов для тестирования
        Path workspace = WutPreparer.builder()
                .wutSourceName("default")
                .wutSourcePath(Path.of("src/test/resources/wut-templates"))
                .build()
                .prepare();

        // 2. Создание раннера агента через фасад SDK
        AgentRunnerService agentRunner = new AgentRunnerService(workspace);

        AgentResultDto result = agentRunner.executeSkill("arithmetic", "Сколько будет 2 + 2?");

        assertSingleSkillCall(result, "arithmetic");
    }
}
