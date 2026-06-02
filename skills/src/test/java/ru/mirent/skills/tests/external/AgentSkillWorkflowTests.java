package ru.mirent.skills.tests.external;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.mirent.skills.dto.agent.AgentResultDto;
import ru.mirent.skills.runner.AgentRunner;
import ru.mirent.skills.service.AgentRunnerService;

import static ru.mirent.skills.matcher.AgentMatcher.assertSingleSkillCall;
import static ru.mirent.skills.matcher.AgentMatcher.assertSuccessful;

class AgentSkillWorkflowTests {

    @Test
    @DisplayName("Выполняет пользовательский prompt с явным вызовом arithmetic")
    void executeUserPromptInvokesRequestedSkillsInOrder() throws Exception {
        AgentRunner agentRunner = new AgentRunnerService("default");

        AgentResultDto result = agentRunner.executeUserPrompt("Верни 1 ответ: сколько будет 2 + 2 используй skills arithmetic");

        assertSuccessful(result);
        assertSingleSkillCall(result, "arithmetic");
    }

    @Test
    @DisplayName("Явный вызов скилла arithmetic")
    void executeSkillPromptDelegatesToArithmeticSkill() throws Exception {
        AgentRunner agentRunner = new AgentRunnerService("default");

        AgentResultDto result = agentRunner.executeSkillPrompt(
                "arithmetic",
                "Верни 1 ответ: сколько будет 2 + 2 используй skills arithmetic"
        );

        assertSuccessful(result);
        assertSingleSkillCall(result, "arithmetic");
    }
}
