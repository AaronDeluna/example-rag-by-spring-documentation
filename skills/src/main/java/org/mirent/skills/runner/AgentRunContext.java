package org.mirent.skills.runner;

import java.nio.file.Path;

/**
 * Контекст однократного запуска агента — типизированный вид над готовой структурой запуска,
 * которую создал {@link org.mirent.skills.util.WutPreparer}:
 * <pre>
 * &lt;workspace&gt;/&lt;uuid&gt;/sorce/   — рабочая область запуска (.qwen со скилами, структура проекта)
 * &lt;workspace&gt;/&lt;uuid&gt;/logs/    — логи запуска (log.json и openai-логи)
 * </pre>
 * Конструктор принимает путь к {@code sorce}; остальные пути и runId вычисляются от него.
 */
public class AgentRunContext {

    private static final String LOGS_DIR = "logs";

    private final String runId;
    private final Path sourceDir;
    private final Path logsDir;

    public AgentRunContext(Path sourceDir) {
        this.sourceDir = sourceDir;
        Path runRoot = sourceDir.getParent();
        this.runId = runRoot.getFileName().toString();
        this.logsDir = runRoot.resolve(LOGS_DIR);
    }

    public String getRunId() {
        return runId;
    }

    /**
     * Рабочая директория запуска ({@code sorce}): здесь лежит {@code .qwen} со скилами,
     * отсюда стартует CLI, сюда же пишутся правки settings.json.
     */
    public Path getWorkspace() {
        return sourceDir;
    }

    /**
     * Директория логов запуска ({@code logs}): сюда пишется {@code log.json} и openai-логи.
     */
    public Path getRunDir() {
        return logsDir;
    }
}
