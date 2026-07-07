package org.mirent.skills.tests.inner.integration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
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

import static org.mirent.skills.matcher.AgentMatcher.*;

@Tag("inner")
@Tag("integration")
@DisplayName("Интеграционные тесты AgentRunner")
class AgentRunnerTest {

    private static final Path WUT_SOURCE = Path.of("src/test/resources/wut-templates");

    private static Path prepareWut(String name) throws IOException {
        return WutPreparer.builder()
                .wutSourceName(name)
                .wutSourcePath(WUT_SOURCE)
                .build()
                .prepare();
    }

    // === Группа тестов на выполнение пользовательских промптов ===
    @Nested
    @DisplayName("Выполнение пользовательских промптов")
    class UserPromptExecution {

        @Test
        @DisplayName("Шаблон default: вызывает arithmetic")
        void defaultTemplate_callsArithmetic() throws Exception {
            AgentRunner agent = new AgentRunnerService(prepareWut("default"));
            AgentResultDto result = agent.executeUserPrompt(
                    "Верни 1 ответ: сколько будет 2 + 2 используй skills arithmetic"
            );
            assertSuccessful(result);
            assertSingleSkillCall(result, "arithmetic");
        }

        @Test
        @DisplayName("Шаблон text-utils: вызывает word-count")
        void textUtilsTemplate_callsWordCount() throws Exception {
            AgentRunner agent = new AgentRunnerService(prepareWut("text-utils"));
            AgentResultDto result = agent.executeUserPrompt(
                    "Посчитай количество слов в фразе \"быстрая бурая лиса прыгает через ленивого пса\" используй skills word-count"
            );
            assertSuccessful(result);
            assertSingleSkillCall(result, "word-count");
        }

        @ParameterizedTest
        @ValueSource(strings = {"case-1", "case-2", "case-3"})
        @DisplayName("Шаблоны case-*: вызывают word-count")
        void caseTemplates_callWordCount(String caseName) throws Exception {
            AgentRunner agent = new AgentRunnerService(prepareWut(caseName));
            AgentResultDto result = agent.executeUserPrompt(
                    "Посчитай количество слов в фразе \"быстрая бурая лиса прыгает через ленивого пса\" используй skills word-count"
            );
            assertSuccessful(result);
            assertSingleSkillCall(result, "word-count");
        }
    }

    // === Группа тестов на прямой вызов скиллов ===
    @Nested
    @DisplayName("Прямой вызов скиллов через executeSkillPrompt")
    class DirectSkillCall {

        @Test
        @DisplayName("Вызов arithmetic напрямую")
        void directCall_arithmetic() throws Exception {
            AgentRunner agent = new AgentRunnerService(prepareWut("default"));
            AgentResultDto result = agent.executeSkillPrompt(
                    "arithmetic",
                    "Верни 1 ответ: сколько будет 2 + 2 используй skills arithmetic"
            );
            assertSuccessful(result);
            assertSingleSkillCall(result, "arithmetic");
        }
    }

    // === Группа тестов на оценку ответов ===
    @Nested
    @DisplayName("Оценка ответов судьёй")
    class Evaluation {

        @Test
        @DisplayName("Ответ агента из шаблона default оценивается с score >= 0.7")
        void defaultTemplate_scoreAtLeastThreshold() throws Exception {
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
    }
}