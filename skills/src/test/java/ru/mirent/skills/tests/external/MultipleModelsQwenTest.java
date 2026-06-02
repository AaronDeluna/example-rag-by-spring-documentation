package ru.mirent.skills.tests.external;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;
import ru.mirent.skills.dto.agent.AgentResultDto;
import ru.mirent.skills.runner.AgentRunner;
import ru.mirent.skills.service.AgentRunnerService;
import ru.mirent.skills.util.qwen.QwenSettingsUpdater;

import java.util.stream.Stream;

import static ru.mirent.skills.matcher.AgentMatcher.assertSingleSkillCall;
import static ru.mirent.skills.matcher.AgentMatcher.assertSuccessful;

/**
 * Выполнение тестирования нескольких моделей на одной задаче.
 * Предполагается, что параметры моделей, указанные в классе-поставщике данных {@link ModelNamesProvider} содержатся в
 * файле с настройками приложения Qwen по пути: /.qwen/settings.json
 * @deprecated Класс требует переработки в соответствии с реализацией работы с наборами.
 */
@Deprecated(forRemoval = true)
@Slf4j
class MultipleModelsQwenTest {

    private static QwenSettingsUpdater settingsUpdater;
    private static String originalModelNameBeforeTestClass;

    @BeforeAll
    static void setUp() throws Exception {
        settingsUpdater = new QwenSettingsUpdater();
        originalModelNameBeforeTestClass = settingsUpdater.getCurrentModelName();
    }

    @AfterAll
    static void tearDown() throws Exception {
        if (originalModelNameBeforeTestClass != null) {
            settingsUpdater.updateModelNameAndSave(originalModelNameBeforeTestClass);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(ModelNamesProvider.class)
    @DisplayName("Выполняет пользовательский prompt с явным вызовом arithmetic для разных моделей")
    void executeUserPromptInvokesRequestedSkillsInOrder(String modelName) throws Exception {
        settingsUpdater.updateModelNameAndSave(modelName);
        String prompt = "Верни 1 ответ: сколько будет 2 + 2 используй skills arithmetic";
        AgentRunner agentRunner = new AgentRunnerService("default");

        AgentResultDto result = agentRunner.executeUserPrompt(prompt);
        log.info("Тест с моделью {} -> идентификатор сессии: {}", modelName, result.getEvents().get(0).get("uuid"));

        assertSuccessful(result);
        assertSingleSkillCall(result, "arithmetic");
    }

    static class ModelNamesProvider implements ArgumentsProvider {
        @Override
        public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
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
}