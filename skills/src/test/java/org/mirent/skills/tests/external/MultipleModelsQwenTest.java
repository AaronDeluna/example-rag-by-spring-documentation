package org.mirent.skills.tests.external;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.mirent.skills.dto.agent.AgentResultDto;
import org.mirent.skills.runner.qwen.QwenAgentRunner;
import org.mirent.skills.service.AgentRunnerFactory;
import org.mirent.skills.service.AgentRunnerProperties;
import org.mirent.skills.util.WutPreparer;
import org.mirent.skills.util.qwen.QwenSettingsUpdater;

import java.nio.file.Path;
import java.util.stream.Stream;

import static org.mirent.skills.matcher.AgentMatcher.assertSingleSkillCall;
import static org.mirent.skills.matcher.AgentMatcher.assertSuccessful;

@Tag("external")
@Slf4j
class MultipleModelsQwenTest {

    @ParameterizedTest
    @ArgumentsSource(ModelNamesProvider.class)
    @DisplayName("Выполняет пользовательский prompt с явным вызовом arithmetic для разных моделей")
    void executeUserPromptInvokesRequestedSkillsInOrder(String modelName) throws Exception {
        String prompt = "Верни 1 ответ: сколько будет 2 + 2 используй skills arithmetic";

        Path wut = WutPreparer.builder()
                .wutSourceName("default")
                .wutSourcePath(Path.of("src/test/resources/wut-templates"))
                .build()
                .prepare();

        AgentRunnerFactory agentRunnerFactory = AgentRunnerFactory.defaultFactory(wut);
        QwenAgentRunner agentRunner = agentRunnerFactory.create(AgentRunnerProperties.loadDefault());

        QwenSettingsUpdater settingsUpdater = QwenSettingsUpdater.builder()
                .agentRunContext(agentRunner.getAgentRunContext())
                .createSettingsIfMissing(true)
                .build();
        settingsUpdater.updateModelNameAndSave(modelName);

        AgentResultDto result = agentRunner.executeUserPrompt(prompt);
        log.info("Тест с моделью {} -> идентификатор сессии: {}", modelName, result.getEvents().get(0).get("uuid"));

        assertSuccessful(result);
        assertSingleSkillCall(result, "arithmetic");
    }

    static class ModelNamesProvider implements ArgumentsProvider {
        @Override
        public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
            return Stream.of(
//                    Arguments.of("qwen3.5:4b"),
//                    Arguments.of("omnicoder-9b:q4_k_m"),
//                    Arguments.of("qwen3.5:9b"),
//                    Arguments.of("qwen2.5-coder:7b"),
//                    Arguments.of("qwen3:8b"),
//                    Arguments.of("qwen3:14b"),
                    Arguments.of("openai/gpt-oss-120b")
//                    Arguments.of("Qwen/Qwen3-Coder-Next"),
//                    Arguments.of("deepseek-v4-flash")
            );
        }
    }
}
