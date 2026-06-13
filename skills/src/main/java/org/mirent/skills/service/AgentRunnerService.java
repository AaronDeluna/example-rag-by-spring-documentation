package org.mirent.skills.service;

import org.mirent.skills.dto.agent.AgentResultDto;
import org.mirent.skills.runner.AgentRunner;

public class AgentRunnerService implements AgentRunner {

    private final AgentRunner agentRunner;

    /**
     * Запускает агента на едином наборе.
     * В workspace копируется содержимое {@code src/test/resources/agent-sets/<agentSetName>/}.
     *
     * @param agentSetName имя папки набора в {@code agent-sets/}
     */
    public AgentRunnerService(String agentSetName) {
        this(agentSetName, null);
    }

    /**
     * Запускает агента на конкретном кейсе внутри набора.
     * В workspace копируется содержимое {@code src/test/resources/agent-sets/<agentSetName>/<caseName>/}.
     * Нужно для параметризованных тестов, когда в одном наборе лежит несколько вариантов конфигурации.
     *
     * @param agentSetName имя папки набора в {@code agent-sets/}
     * @param caseName     имя подпапки-кейса внутри набора
     */
    public AgentRunnerService(String agentSetName, String caseName) {
        this(AgentRunnerFactory.defaultFactory(agentSetName, caseName).create(AgentRunnerProperties.loadDefault()));
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
