package org.mirent.skills.service;

import org.mirent.skills.dto.agent.AgentResultDto;
import org.mirent.skills.runner.AgentRunner;

import java.nio.file.Path;

public class AgentRunnerService implements AgentRunner {

    private final AgentRunner agentRunner;

    /**
     * Запускает агента в указанной рабочей области (workspace).
     * Внутри workspace должна находиться директория {@code .qwen/} со скилами.
     *
     * @param workspace путь к рабочей области
     */
    public AgentRunnerService(Path workspace) {
        this(AgentRunnerFactory.defaultFactory(workspace).create(AgentRunnerProperties.loadDefault()));
    }

    AgentRunnerService(AgentRunner delegate) {
        this.agentRunner = delegate;
    }

    @Override
    public AgentResultDto executeUserPrompt(String prompt) throws Exception {
        return agentRunner.executeUserPrompt(prompt);
    }

    @Override
    public AgentResultDto executeSkillPrompt(String skillName, String prompt) throws Exception {
        return agentRunner.executeSkillPrompt(skillName, prompt);
    }
}
