package org.mirent.skills.tests;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mirent.skills.dto.agent.AgentResultDto;
import org.mirent.skills.runner.AgentRunner;
import org.mirent.skills.service.AgentRunnerService;

import java.util.List;

import static org.mirent.skills.matcher.AgentMatcher.assertSingleSkillCall;
import static org.mirent.skills.matcher.AgentMatcher.assertSkillCallsIgnoringOrder;
import static org.mirent.skills.matcher.AgentMatcher.assertSuccessful;

class AgentSkillWorkflowTests {

    @Test
    @DisplayName("Выполняет пользовательский prompt с явным вызовом arithmetic")
    void executeUserPromptInvokesRequestedSkillsInOrder() throws Exception {
        AgentRunner agentRunner = new AgentRunnerService();

        AgentResultDto result = agentRunner.executeUserPrompt("Верни 1 ответ: сколько будет 2 + 2 используй skills arithmetic");

        assertSuccessful(result);
        assertSingleSkillCall(result, "arithmetic");
    }

    @Test
    @DisplayName("Делегирует arithmetic-delegator во внутренний arithmetic")
    void executeSkillPromptDelegatesToArithmeticSkill() throws Exception {
        AgentRunner agentRunner = new AgentRunnerService();

        AgentResultDto result = agentRunner.executeSkillPrompt(
                "arithmetic-delegator",
                "как считать 2 + 2 * 2"
        );

        assertSuccessful(result);
        assertSingleSkillCall(result, "arithmetic");
    }

    @Test
    @DisplayName("Выполняет chain-check без дополнительных skill-вызовов")
    void executeSkillPromptDoesNotInvokeAdditionalSkills() throws Exception {
        AgentRunner agentRunner = new AgentRunnerService();

        AgentResultDto result = agentRunner.executeSkillPrompt(
                "chain-check",
                "Проверь цепочку skill workflow и верни все обязательные маркеры."
        );

        assertSuccessful(result);
        assertSkillCallsIgnoringOrder(result, List.of());
    }

    @Test
    @DisplayName("Выбирает arithmetic по арифметическому запросу пользователя")
    void executeUserPromptSelectsSkillByUserIntent() throws Exception {
        AgentRunner agentRunner = new AgentRunnerService();

        AgentResultDto result = agentRunner.executeUserPrompt("2 + 3");
        assertSuccessful(result);
        assertSingleSkillCall(result, "arithmetic");
    }

    @Test
    @DisplayName("Выполняет пользовательский prompt через AgentRunnerService")
    void executeUserPromptThroughAgentRunnerService() throws Exception {
        AgentRunnerService agentRunnerService = new AgentRunnerService();

        AgentResultDto result = agentRunnerService.executeUserPrompt("Верни 1 ответ: сколько будет 2 + 2 используй skills arithmetic");
        assertSuccessful(result);
        assertSingleSkillCall(result, "arithmetic");
    }
}
