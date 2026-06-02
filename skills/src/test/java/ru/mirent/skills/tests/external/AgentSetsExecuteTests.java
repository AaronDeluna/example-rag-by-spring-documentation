package ru.mirent.skills.tests.external;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.mirent.skills.dto.agent.AgentResultDto;
import ru.mirent.skills.runner.AgentRunner;
import ru.mirent.skills.service.AgentRunnerService;

import static ru.mirent.skills.matcher.AgentMatcher.assertSingleSkillCall;
import static ru.mirent.skills.matcher.AgentMatcher.assertSuccessful;

class AgentSetsExecuteTests {

    @Test
    @DisplayName("Набор default: вызывает arithmetic из пользовательского prompt")
    void defaultSetInvokesArithmeticSkill() throws Exception {
        AgentRunner agentRunner = new AgentRunnerService("default");

        AgentResultDto result = agentRunner.executeUserPrompt(
                "Верни 1 ответ: сколько будет 2 + 2 используй skills arithmetic"
        );

        assertSuccessful(result);
        assertSingleSkillCall(result, "arithmetic");
    }

    @Test
    @DisplayName("Набор text-utils: вызывает word-count из пользовательского prompt")
    void textUtilsSetInvokesWordCountSkill() throws Exception {
        AgentRunner agentRunner = new AgentRunnerService("text-utils");

        AgentResultDto result = agentRunner.executeUserPrompt(
                "Посчитай количество слов в фразе \"быстрая бурая лиса прыгает через ленивого пса\" используй skills word-count"
        );

        assertSuccessful(result);
        assertSingleSkillCall(result, "word-count");
    }
}
