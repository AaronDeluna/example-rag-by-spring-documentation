package org.mirent.skills.tests.external;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mirent.skills.dto.agent.AgentResultDto;
import org.mirent.skills.dto.evaluate.EvaluateDto;
import org.mirent.skills.dto.evaluate.EvaluateResultDto;
import org.mirent.skills.runner.AgentRunner;
import org.mirent.skills.service.AgentEvaluatorService;
import org.mirent.skills.service.AgentRunnerService;
import org.mirent.skills.util.WutPreparer;

import java.io.IOException;
import java.nio.file.Path;

import static org.mirent.skills.matcher.AgentMatcher.assertSingleSkillCall;
import static org.mirent.skills.matcher.AgentMatcher.assertSuccessful;
import static org.mirent.skills.matcher.AgentMatcher.evaluate;

@Tag("integration")
class AgentSetsExecuteTests {

    private static final Path WUT_SOURCE = Path.of("src/test/resources/wut-templates");

    private static Path prepareWut(String name) throws IOException {
        return WutPreparer.builder()
                .wutSourceName(name)
                .wutSourcePath(WUT_SOURCE)
                .overwriteTarget(true)
                .build()
                .prepare();
    }

    @Test
    @DisplayName("Шаблон default: вызывает arithmetic из пользовательского prompt")
    void defaultTemplateInvokesArithmeticSkill() throws Exception {
        AgentRunner agentRunner = new AgentRunnerService(prepareWut("default"));

        AgentResultDto result = agentRunner.executeUserPrompt(
                "Верни 1 ответ: сколько будет 2 + 2 используй skills arithmetic"
        );

        assertSuccessful(result);
        assertSingleSkillCall(result, "arithmetic");
    }

    @Test
    @DisplayName("Шаблон text-utils: вызывает word-count из пользовательского prompt")
    void textUtilsTemplateInvokesWordCountSkill() throws Exception {
        AgentRunner agentRunner = new AgentRunnerService(prepareWut("text-utils"));

        AgentResultDto result = agentRunner.executeUserPrompt(
                "Посчитай количество слов в фразе \"быстрая бурая лиса прыгает через ленивого пса\" используй skills word-count"
        );

        assertSuccessful(result);
        assertSingleSkillCall(result, "word-count");
    }

    @Test
    @DisplayName("Ответ агента из шаблона default оценивается судьёй на score не ниже 0.7")
    void defaultTemplateAnswerIsEvaluatedAsCorrect() throws Exception {
        String query = "сколько будет 2 + 2 используй skills arithmetic";

        AgentRunner agent = new AgentRunnerService(prepareWut("default"));
        AgentResultDto agentResult = agent.executeUserPrompt(query);
        assertSuccessful(agentResult);

        AgentEvaluatorService evaluator = new AgentEvaluatorService(prepareWut("default"));
        EvaluateResultDto evaluation = evaluator.evaluate(new EvaluateDto(
                query,
                agentResult.getEventsJson()
        ));

        evaluate(evaluation, 0.7);
    }

    @ParameterizedTest
    @ValueSource(strings = {"case-1", "case-2", "case-3"})
    @DisplayName("Параметризованный запуск разных WUT-шаблонов case-*")
    void runsAcrossCases(String caseName) throws Exception {
        AgentRunner agent = new AgentRunnerService(prepareWut(caseName));
        AgentResultDto result = agent.executeUserPrompt(
                "Посчитай количество слов в фразе \"быстрая бурая лиса прыгает через ленивого пса\" используй skills word-count"
        );
        assertSuccessful(result);
        assertSingleSkillCall(result, "word-count");
    }
}
