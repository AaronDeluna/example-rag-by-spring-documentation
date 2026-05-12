package org.mirent.skills.service;

import org.mirent.skills.dto.agent.AgentResultDto;
import org.mirent.skills.runner.AgentRunner;

public class AgentRunnerService implements AgentRunner {

    private final AgentRunner agentRunner;

    public AgentRunnerService() {
        this(AgentRunnerFactory.defaultFactory().create(AgentRunnerProperties.loadDefault()));
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
