package org.mirent.skills.service;

import io.github.ivanmilovanov.agentic.cli.runner.api.AgentRunner;
import io.github.ivanmilovanov.agentic.cli.runner.config.AgentRunnerProperties;
import io.github.ivanmilovanov.agentic.cli.runner.model.AgentResultDto;
import io.github.ivanmilovanov.agentic.cli.runner.service.AgentRunnerFactory;

import java.nio.file.Path;

/**
 * Фасад над раннером агента из SDK {@code agentic-cli-runner}.
 * Сохраняет привычные для модуля {@code skills} имена методов
 * ({@code executeUserPrompt}/{@code executeSkillPrompt}), делегируя в SDK
 * ({@code execute}/{@code executeSkill}).
 */
public class AgentRunnerService {

    private final AgentRunner agentRunner;

    /**
     * Запускает агента в указанной рабочей области (workspace).
     * Внутри workspace может находиться директория {@code .qwen/} со скилами.
     *
     * @param workspace путь к рабочей области
     */
    public AgentRunnerService(Path workspace) {
        this(AgentRunnerFactory.defaultFactory(workspace).create(AgentRunnerProperties.loadDefault()));
    }

    AgentRunnerService(AgentRunner delegate) {
        this.agentRunner = delegate;
    }

    public AgentResultDto executeUserPrompt(String prompt) throws Exception {
        return agentRunner.execute(prompt);
    }

    public AgentResultDto executeSkillPrompt(String skillName, String prompt) throws Exception {
        return agentRunner.executeSkill(skillName, prompt);
    }
}
