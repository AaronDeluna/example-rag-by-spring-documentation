package org.mirent.skills.tests.inner.integration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mirent.skills.runner.AgentRunContext;
import org.mirent.skills.util.WutPreparer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("inner")
@Tag("integration")
class AgentRunContextTest {

    private static final Path WUT_SOURCE = Path.of("src/test/resources/wut-templates");

    private static Path prepareSorce(String name) throws IOException {
        return WutPreparer.builder()
                .wutSourceName(name)
                .wutSourcePath(WUT_SOURCE)
                .build()
                .prepare();
    }

    @Test
    @DisplayName("Вычисляет sorce, logs и runId из готовой структуры запуска")
    void derivesPathsFromRunLayout() throws IOException {
        Path sorce = prepareSorce("default");

        AgentRunContext context = new AgentRunContext(sorce);

        Path runRoot = sorce.getParent();
        assertEquals(sorce, context.getWorkspace());
        assertEquals(runRoot.resolve("logs"), context.getRunDir());
        assertEquals(runRoot.getFileName().toString(), context.getRunId());

        // runId — валидный UUID
        UUID.fromString(context.getRunId());

        // скилы шаблона доступны из рабочей области (sorce)
        Path skill = context.getWorkspace()
                .resolve(".qwen").resolve("skills").resolve("arithmetic").resolve("SKILL.md");
        assertTrue(Files.isRegularFile(skill), "Скил должен быть в sorce: " + skill);
    }

    @Test
    @DisplayName(".qwen лежит только внутри sorce, а не в корне <wut-name>")
    void qwenLivesOnlyInsideSorce() throws IOException {
        Path sorce = prepareSorce("default");
        new AgentRunContext(sorce);

        // корень <wut-name> = .../wut-target/default
        Path wutNameRoot = sorce.getParent().getParent();
        assertFalse(Files.exists(wutNameRoot.resolve(".qwen")),
                "В корне <wut-name> не должно быть .qwen: " + wutNameRoot);
        assertTrue(Files.isDirectory(sorce.resolve(".qwen")),
                ".qwen должен быть внутри sorce");
    }
}
