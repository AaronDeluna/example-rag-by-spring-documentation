package org.mirent.skills.runner;

import java.nio.file.Path;
import java.util.UUID;

/**
 * Контекст однократного запуска агента Qwen.
 * Хранит runId, workspace и вычисленный путь к директории логов.
 */
public class AgentRunContext {

    private static final String RUNS_DIR_SUFFIX = "-runs";

    private final String runId;
    private final Path workspace;
    private final Path runDir;

    public AgentRunContext(Path workspace) {
        this.runId = UUID.randomUUID().toString();
        this.workspace = workspace;
        this.runDir = workspace
                .resolveSibling(workspace.getFileName() + RUNS_DIR_SUFFIX)
                .resolve(runId);
    }

    public String getRunId() {
        return runId;
    }

    public Path getWorkspace() {
        return workspace;
    }

    public Path getRunDir() {
        return runDir;
    }
}
