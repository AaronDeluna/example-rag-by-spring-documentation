package org.mirent.skills.tests.external;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mirent.skills.dto.agent.AgentResultDto;
import org.mirent.skills.runner.AgentRunner;
import org.mirent.skills.service.AgentRunnerService;
import org.mirent.skills.util.qwen.QwenSettingsUpdater;

import java.util.stream.Stream;

import static org.mirent.skills.matcher.AgentMatcher.assertSingleSkillCall;
import static org.mirent.skills.matcher.AgentMatcher.assertSuccessful;

@Slf4j
class MultipleModelsQwenTest {

    private static QwenSettingsUpdater settingsUpdater;
    private static String originalModelNameBeforeTestClass;  // для восстановления после теста

    @BeforeAll
    static void setUp() throws Exception {
        settingsUpdater = new QwenSettingsUpdater();
        originalModelNameBeforeTestClass = settingsUpdater.getCurrentModelName();
    }

    @AfterAll
    static void tearDown() throws Exception {
        // Восстанавливаем исходное имя модели
        if (originalModelNameBeforeTestClass != null) {
            settingsUpdater.updateModelNameAndSave(originalModelNameBeforeTestClass);
        }
    }

    @ParameterizedTest
    @MethodSource("modelNamesProvider")
    @DisplayName("Выполняет пользовательский prompt с явным вызовом arithmetic для разных моделей")
    void executeUserPromptInvokesRequestedSkillsInOrder(String modelName) throws Exception {
        // given
        settingsUpdater.updateModelNameAndSave(modelName);
        String prompt = "Верни 1 ответ: сколько будет 2 + 2 используй skills arithmetic";
        AgentRunner agentRunner = new AgentRunnerService();

        // when
        AgentResultDto result = agentRunner.executeUserPrompt(prompt);
        log.info("Тест с моделью {} -> идентификатор сессии: {}", modelName, result.getEvents().get(0).get("uuid"));

        // then
        assertSuccessful(result);
        assertSingleSkillCall(result, "arithmetic");
    }

    private static Stream<Arguments> modelNamesProvider() {
        return Stream.of(
                Arguments.of("carstenuhlig/omnicoder-9b:q4_k_m"),
                Arguments.of("qwen3.5:9b"),
                Arguments.of("qwen2.5-coder:7b"),
                Arguments.of("qwen3:8b"),
                Arguments.of("qwen3:14b"),
                Arguments.of("qwen2.5:1.5b"),
                Arguments.of("gemma3:12b"),
                Arguments.of("gemma3:4b-it-qat"),
                Arguments.of("gemma3:4b"),
                Arguments.of("gemma3:12b-it-qat")
        );
    }
}
