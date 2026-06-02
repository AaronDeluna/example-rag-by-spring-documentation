package org.mirent.skills.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mirent.skills.dto.module.ModuleLayoutDto;
import org.mirent.skills.exeptions.AgentRunnerConfigurationException;
import org.mirent.skills.exeptions.AgentSetNotFoundException;
import org.mirent.skills.exeptions.AgentSetsDirectoryNotFoundException;
import org.mirent.skills.exeptions.MissingAgentSetNameException;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.mirent.skills.util.SkillsFileUtils.cleanDirectory;
import static org.mirent.skills.util.SkillsFileUtils.copyDirectory;
import static org.mirent.skills.util.SkillsFileUtils.resolveClassesLocation;

@Slf4j
@RequiredArgsConstructor
public class AgentWorkspacePreparer {

    private static final String AGENT_SETS_DIR = "src/test/resources/agent-sets";
    private static final String QWEN_DIR = ".qwen";
    private static final String SKILLS_DIR = "skills";
    private static final String WORKSPACE_DIR_NAME = "agent-runner";

    private final String agentSetName;

    public Path prepare() {
        if (agentSetName == null || agentSetName.isBlank()) {
            throw new MissingAgentSetNameException();
        }

        ModuleLayoutDto layout = resolveModuleLayout();
        Path agentSetsDir = layout.getBasedir().resolve(AGENT_SETS_DIR);
        Path agentSet = agentSetsDir.resolve(agentSetName);

        if (!Files.isDirectory(agentSetsDir)) {
            throw new AgentSetsDirectoryNotFoundException(
                    "Не найдена директория с наборами для агента: " + agentSetsDir
                            + ". Создайте её и положите внутрь подпапки с наборами."
            );
        }
        if (!Files.isDirectory(agentSet)) {
            throw new AgentSetNotFoundException(
                    "Не найден набор агента '" + agentSetName + "' по пути " + agentSet
            );
        }

        Path workspace = layout.getBasedir().resolve(layout.getBuildDir()).resolve(WORKSPACE_DIR_NAME);
        Path skillsTarget = workspace.resolve(QWEN_DIR).resolve(SKILLS_DIR);

        cleanDirectory(workspace);
        copyDirectory(agentSet, skillsTarget);

        log.info("Agent workspace подготовлен из набора '{}': {} -> {}", agentSetName, agentSet, workspace);
        return workspace;
    }

    private static ModuleLayoutDto resolveModuleLayout() {
        Path classesDir = resolveClassesLocation();
        Path current = classesDir;
        while (current != null) {
            if (Files.isRegularFile(current.resolve("pom.xml"))) {
                return new ModuleLayoutDto(current, "target");
            }
            if (Files.isRegularFile(current.resolve("build.gradle"))
                    || Files.isRegularFile(current.resolve("build.gradle.kts"))) {
                return new ModuleLayoutDto(current, "build");
            }
            current = current.getParent();
        }
        throw new AgentRunnerConfigurationException(
                "Не удалось найти basedir модуля (нет pom.xml/build.gradle) начиная с " + classesDir
        );
    }
}
