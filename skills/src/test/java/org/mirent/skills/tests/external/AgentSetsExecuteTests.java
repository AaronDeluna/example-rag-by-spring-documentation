package org.mirent.skills.tests.external;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mirent.skills.dto.agent.AgentResultDto;
import org.mirent.skills.dto.evaluate.EvaluateDto;
import org.mirent.skills.dto.evaluate.EvaluateResultDto;
import org.mirent.skills.runner.AgentRunner;
import org.mirent.skills.service.AgentEvaluatorService;
import org.mirent.skills.service.AgentRunnerService;

import static org.mirent.skills.matcher.AgentMatcher.assertSingleSkillCall;
import static org.mirent.skills.matcher.AgentMatcher.assertSuccessful;
import static org.mirent.skills.matcher.AgentMatcher.evaluate;

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

    @Test
    @DisplayName("Ответ агента из набора default оценивается судьёй на score не ниже 0.7")
    void defaultSetAnswerIsEvaluatedAsCorrect() throws Exception {
        String query = "сколько будет 2 + 2 используй skills arithmetic";

        AgentRunner agent = new AgentRunnerService("default");
        AgentResultDto agentResult = agent.executeUserPrompt(query);
        assertSuccessful(agentResult);

        AgentEvaluatorService evaluator = new AgentEvaluatorService("default");
        EvaluateResultDto evaluation = evaluator.evaluate(new EvaluateDto(
                query,
                agentResult.getEventsJson()
        ));

        evaluate(evaluation, 0.7);
    }

    @ParameterizedTest
    @ValueSource(strings = {"case-1", "case-2", "case-3"})
    @DisplayName("Параметризованный запуск разных кейсов набора case-sets")
    void runsAcrossCases(String caseName) throws Exception {
        AgentRunner agent = new AgentRunnerService("case-sets", caseName);
        AgentResultDto result = agent.executeUserPrompt(
                "Посчитай количество слов в фразе \"быстрая бурая лиса прыгает через ленивого пса\" используй skills word-count"
        );
        assertSuccessful(result);
        assertSingleSkillCall(result, "word-count");
    }
}
