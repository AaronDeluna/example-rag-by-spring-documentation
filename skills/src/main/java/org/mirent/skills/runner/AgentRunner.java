package org.mirent.skills.runner;

import org.mirent.skills.dto.agent.AgentResultDto;

public interface AgentRunner {
    AgentResultDto executeUserPrompt(String prompt) throws Exception;

    AgentResultDto executeSkillPrompt(String skillName, String prompt) throws Exception;
}
