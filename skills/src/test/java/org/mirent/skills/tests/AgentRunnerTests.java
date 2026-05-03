package org.mirent.skills.tests;

import org.junit.jupiter.api.Test;
import org.mirent.skills.CommandExecutor;
import org.mirent.skills.dto.agent.AgentResultDto;
import org.mirent.skills.dto.command.CommandRequestDto;
import org.mirent.skills.dto.command.CommandResultDto;
import org.mirent.skills.runner.AgentRunner;
import org.mirent.skills.runner.qwen.QwenAgentRunner;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AgentRunnerTests {

    @Test
    void executeUserPromptReturnsAgentAnswer() throws Exception {
        AgentRunner agentRunner = new QwenAgentRunner();

        AgentResultDto result = agentRunner.executeUserPrompt("Верни 1 ответ: сколько будет 2 + 2, можешь использовать скил arithmetic-delegator потмо вызови скил chain-check");

        assertFalse(result.isTimedOut());
        assertEquals(0, result.getExitCode());
    }

    @Test
    void executeSkillPromptReturnsAgentAnswer() throws Exception {
        AgentRunner agentRunner = new QwenAgentRunner();

        AgentResultDto result = agentRunner.executeSkillPrompt(
                "arithmetic-delegator",
                "как считать 2 + 2 * 2"
        );

        assertFalse(result.isTimedOut());
        assertEquals(0, result.getExitCode());
    }

    @Test
    void executeSkillPromptFollowsSkillChain() throws Exception {
        AgentRunner agentRunner = new QwenAgentRunner();

        AgentResultDto result = agentRunner.executeSkillPrompt(
                "chain-check",
                "Проверь цепочку skill workflow и верни все обязательные маркеры."
        );

        assertFalse(result.isTimedOut());
        assertEquals(0, result.getExitCode());
        assertTrue(result.getStdout().contains("CHAIN_STEP_1_READ_TASK"));
        assertTrue(result.getStdout().contains("CHAIN_STEP_2_TRANSFORM_TASK"));
        assertTrue(result.getStdout().contains("CHAIN_STEP_3_FINAL_ANSWER"));
        assertTrue(result.getStdout().contains("CHAIN_SKILL_DONE"));
    }
}
