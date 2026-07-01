package org.mirent.skills.tests.inner.unit;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mirent.skills.dto.evaluate.EvaluateDto;
import org.mirent.skills.dto.evaluate.EvaluateResultDto;
import org.mirent.skills.exeptions.EvaluatorResponseParseException;
import org.mirent.skills.runner.JudgeRunner;
import org.mirent.skills.service.AgentEvaluatorService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("inner")
@Tag("unit")
class AgentEvaluatorServiceTest {

    private static final EvaluateDto ANY = new EvaluateDto("query", "[]");

    @Test
    @DisplayName("Парсит чистый JSON-ответ судьи в EvaluateResultDto")
    void parsesPlainJsonResponse() throws Exception {
        AgentEvaluatorService evaluator = new AgentEvaluatorService(
                stubRunner("{\"score\": 0.85, \"problemMessage\": \"\"}")
        );

        EvaluateResultDto result = evaluator.evaluate(ANY);

        assertEquals(0.85, result.getScore(), 0.0001);
        assertEquals("", result.getProblemMessage());
    }

    @Test
    @DisplayName("Парсит JSON, обёрнутый в ```-фенсы")
    void parsesJsonWrappedInMarkdownFences() throws Exception {
        AgentEvaluatorService evaluator = new AgentEvaluatorService(
                stubRunner("""
                        ```json
                        {"score": 0.4, "problemMessage": "Не покрыты все аспекты"}
                        ```""")
        );

        EvaluateResultDto result = evaluator.evaluate(ANY);

        assertEquals(0.4, result.getScore(), 0.0001);
        assertTrue(result.getProblemMessage().contains("аспекты"));
    }

    @Test
    @DisplayName("Бросает EvaluatorResponseParseException на пустой ответ")
    void throwsOnEmptyResponse() {
        AgentEvaluatorService evaluator = new AgentEvaluatorService(stubRunner(""));
        assertThrows(EvaluatorResponseParseException.class, () -> evaluator.evaluate(ANY));
    }

    @Test
    @DisplayName("Бросает EvaluatorResponseParseException на невалидный JSON")
    void throwsOnInvalidJson() {
        AgentEvaluatorService evaluator = new AgentEvaluatorService(stubRunner("не JSON вообще"));
        assertThrows(EvaluatorResponseParseException.class, () -> evaluator.evaluate(ANY));
    }

    private static JudgeRunner stubRunner(String finalResult) {
        return prompt -> finalResult;
    }
}
