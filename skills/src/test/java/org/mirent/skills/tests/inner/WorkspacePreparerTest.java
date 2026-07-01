package org.mirent.skills.tests.inner;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mirent.skills.util.WutPreparer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WorkspacePreparerTest {

    private static final Path WUT_SOURCE = Path.of("src/test/resources/wut-templates");

    @Test
    @DisplayName("Готовит workspace из шаблона default")
    void preparesDefaultWorkspace() throws IOException {
        Path workspace = WutPreparer.builder()
                .wutSourceName("default")
                .wutSourcePath(WUT_SOURCE)
                .overwriteTarget(true)
                .build()
                .prepare();

        Path skillsDir = workspace.resolve(".qwen").resolve("skills");
        assertTrue(Files.isDirectory(skillsDir), "Должна быть папка .qwen/skills/");
        assertTrue(Files.isRegularFile(skillsDir.resolve("arithmetic").resolve("SKILL.md")));
    }

    @Test
    @DisplayName("Готовит workspace из шаблона text-utils")
    void preparesTextUtilsWorkspace() throws IOException {
        Path workspace = WutPreparer.builder()
                .wutSourceName("text-utils")
                .wutSourcePath(WUT_SOURCE)
                .overwriteTarget(true)
                .build()
                .prepare();

        Path skillsDir = workspace.resolve(".qwen").resolve("skills");
        assertTrue(Files.isDirectory(skillsDir));
        assertTrue(Files.isRegularFile(skillsDir.resolve("reverse-text").resolve("SKILL.md")));
        assertTrue(Files.isRegularFile(skillsDir.resolve("word-count").resolve("SKILL.md")));
    }

    @Test
    @DisplayName("При переключении шаблона удаляет скилы предыдущего")
    void replacesSkillsWhenSwitchingTemplates() throws IOException {
        WutPreparer.builder()
                .wutSourceName("default")
                .wutSourcePath(WUT_SOURCE)
                .overwriteTarget(true)
                .build()
                .prepare();

        Path workspace = WutPreparer.builder()
                .wutSourceName("text-utils")
                .wutSourcePath(WUT_SOURCE)
                .overwriteTarget(true)
                .build()
                .prepare();

        Path skillsDir = workspace.resolve(".qwen").resolve("skills");
        assertTrue(Files.isRegularFile(skillsDir.resolve("reverse-text").resolve("SKILL.md")));
        assertFalse(Files.exists(skillsDir.resolve("arithmetic")),
                "Скилы предыдущего шаблона должны быть удалены");
    }

    @Test
    @DisplayName("Бросает IOException, если шаблон не найден")
    void throwsWhenTemplateMissing() {
        IOException exception = assertThrows(
                IOException.class,
                () -> WutPreparer.builder()
                        .wutSourceName("no-such-template")
                        .wutSourcePath(WUT_SOURCE)
                        .overwriteTarget(true)
                        .build()
                        .prepare()
        );
        assertTrue(exception.getMessage().contains("no-such-template"));
    }
}
