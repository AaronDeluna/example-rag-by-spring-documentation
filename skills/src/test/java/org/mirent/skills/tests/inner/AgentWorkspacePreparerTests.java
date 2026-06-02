package org.mirent.skills.tests.inner;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mirent.skills.exeptions.AgentRunnerConfigurationException;
import org.mirent.skills.exeptions.AgentSetNotFoundException;
import org.mirent.skills.service.AgentWorkspacePreparer;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AgentWorkspacePreparerTests {

    @Test
    @DisplayName("Готовит workspace из набора default")
    void preparesDefaultSet() {
        Path workspace = new AgentWorkspacePreparer("default").prepare();

        Path skillsDir = workspace.resolve(".qwen").resolve("skills");
        assertTrue(Files.isDirectory(skillsDir), "Должна быть папка .qwen/skills/");
        assertTrue(Files.isRegularFile(skillsDir.resolve("arithmetic").resolve("SKILL.md")));
        assertTrue(Files.isRegularFile(skillsDir.resolve("chain-check").resolve("SKILL.md")));
    }

    @Test
    @DisplayName("Готовит workspace из набора text-utils")
    void preparesTextUtilsSet() {
        Path workspace = new AgentWorkspacePreparer("text-utils").prepare();

        Path skillsDir = workspace.resolve(".qwen").resolve("skills");
        assertTrue(Files.isDirectory(skillsDir));
        assertTrue(Files.isRegularFile(skillsDir.resolve("reverse-text").resolve("SKILL.md")));
        assertTrue(Files.isRegularFile(skillsDir.resolve("word-count").resolve("SKILL.md")));
    }

    @Test
    @DisplayName("При переключении набора удаляет скилы предыдущего")
    void replacesSkillsWhenSwitchingSets() {
        new AgentWorkspacePreparer("default").prepare();
        Path workspace = new AgentWorkspacePreparer("text-utils").prepare();

        Path skillsDir = workspace.resolve(".qwen").resolve("skills");
        assertTrue(Files.isRegularFile(skillsDir.resolve("reverse-text").resolve("SKILL.md")));
        assertFalse(Files.exists(skillsDir.resolve("arithmetic")),
                "Скилы предыдущего набора должны быть удалены");
        assertFalse(Files.exists(skillsDir.resolve("chain-check")),
                "Скилы предыдущего набора должны быть удалены");
    }

    @Test
    @DisplayName("Бросает исключение, если набор не найден")
    void throwsWhenAgentSetMissing() {
        AgentSetNotFoundException exception = assertThrows(
                AgentSetNotFoundException.class,
                () -> new AgentWorkspacePreparer("no-such-set").prepare()
        );
        assertTrue(exception.getMessage().contains("no-such-set"));
    }

    @Test
    @DisplayName("Бросает исключение, если имя набора пустое")
    void throwsWhenAgentSetNameBlank() {
        assertThrows(
                AgentRunnerConfigurationException.class,
                () -> new AgentWorkspacePreparer("").prepare()
        );
        assertThrows(
                AgentRunnerConfigurationException.class,
                () -> new AgentWorkspacePreparer(null).prepare()
        );
    }
}
