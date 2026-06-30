package org.mirent.skills.service;

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
import static org.mirent.skills.util.SkillsFileUtils.resolveModuleLayout;

@Slf4j
public class AgentWorkspacePreparer {

    private static final String AGENT_SETS_DIR = "src/test/resources/agent-sets";
    private static final String QWEN_DIR = ".qwen";
    private static final String WORKSPACE_DIR_NAME = "agent-runner";

    private final String agentSetName;
    private final String caseName;

    public AgentWorkspacePreparer(String agentSetName) {
        this(agentSetName, null);
    }

    public AgentWorkspacePreparer(String agentSetName, String caseName) {
        this.agentSetName = agentSetName;
        this.caseName = caseName;
    }

    public Path prepare() {
        Path agentSet = resolveAgentSet();
        Path source = (caseName == null) ? agentSet : resolveCase(agentSet);
        String label = (caseName == null) ? agentSetName : agentSetName + "/" + caseName;
        return copyToWorkspace(source, label);
    }

    private Path resolveAgentSet() {
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
        return agentSet;
    }

    private Path resolveCase(Path agentSet) {
        if (caseName.isBlank()) {
            throw new AgentRunnerConfigurationException("Не передано имя кейса caseName");
        }
        Path caseDir = agentSet.resolve(caseName);
        if (!Files.isDirectory(caseDir)) {
            throw new AgentSetNotFoundException(
                    "Не найден кейс '" + caseName + "' в наборе '" + agentSetName + "' по пути " + caseDir
            );
        }
        return caseDir;
    }

    private Path copyToWorkspace(Path source, String sourceLabel) {
        ModuleLayoutDto layout = resolveModuleLayout();
        Path workspace = layout.getBasedir().resolve(layout.getBuildDir()).resolve(WORKSPACE_DIR_NAME);
        Path qwenTarget = workspace.resolve(QWEN_DIR);

        cleanDirectory(workspace);
        copyDirectory(source, qwenTarget);

        log.info("Agent workspace подготовлен из '{}': {} -> {}", sourceLabel, source, workspace);
        return workspace;
    }

}
