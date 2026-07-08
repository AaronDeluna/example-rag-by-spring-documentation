package org.mirent.skills.tests.external;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;
import io.github.ivanmilovanov.agentic.cli.runner.model.AgentResultDto;
import io.github.ivanmilovanov.agentic.cli.runner.runner.AgentRunnerImpl;
import io.github.ivanmilovanov.agentic.cli.runner.service.AgentRunnerFactory;
import io.github.ivanmilovanov.agentic.cli.runner.config.AgentRunnerProperties;
import org.mirent.skills.util.WutPreparer;
import org.mirent.skills.util.qwen.QwenSettingsUpdater;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

import static org.mirent.skills.matcher.AgentMatcher.assertSingleSkillCall;
import static org.mirent.skills.matcher.AgentMatcher.assertSuccessful;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("external")
@Slf4j
class MultipleModelsSkillTest {

    @ParameterizedTest
    @ArgumentsSource(ModelNamesProvider.class)
    @DisplayName("Выполняет скилл maven-checkstyle-setup и проверяет изменения в файловой системе")
    void executeUserPromptInvokesRequestedSkillsInOrder(String modelName) throws Exception {
        // 1. Подготовка рабочей директории (wut)
        Path wut = WutPreparer.builder()
                .wutSourceName("skill-test-checkstyle")
                .wutSourcePath(Path.of("src/test/resources/wut-templates"))
                .build()
                .prepare();

        // 2. Настройка агента
        AgentRunnerFactory agentRunnerFactory = AgentRunnerFactory.defaultFactory(wut);
        AgentRunnerImpl agentRunner = (AgentRunnerImpl) agentRunnerFactory.create(AgentRunnerProperties.loadDefault());

        QwenSettingsUpdater settingsUpdater = QwenSettingsUpdater.builder()
                .agentRunContext(agentRunner.getAgentRunContext())
                .createSettingsIfMissing(true)
                .build();
        settingsUpdater.updateModelNameAndSave(modelName);

        // 3. Запуск скилла через пользовательский промпт
        // Модель сама решает вызвать tool_use с name="skill" и input.skill="maven-checkstyle-setup"
        String prompt = "Настрой Checkstyle в Maven-проекте используй skills maven-checkstyle-setup";
        AgentResultDto result = agentRunner.execute(prompt);
        log.info("Тест с моделью {} -> идентификатор сессии: {}", modelName, result.getEvents().get(0).get("uuid"));

        // 4. Проверка выполнения агента
        assertSuccessful(result);
        // Проверяем, что был вызван именно наш скилл (исправлено имя)
        assertSingleSkillCall(result, "maven-checkstyle-setup");

        // 5. Проверка результата работы скилла — изменения в файловой системе
        Path pomXml = wut.resolve("pom.xml");
        Path checkstyleXml = wut.resolve("checkstyle.xml");

        // 5.1. Проверяем, что checkstyle.xml создан
        assertTrue(Files.exists(checkstyleXml), "Файл checkstyle.xml не создан в корне проекта");
        assertTrue(Files.isRegularFile(checkstyleXml), "checkstyle.xml не является обычным файлом");

        // 5.2. Проверяем, что в pom.xml добавлен плагин Checkstyle
        String pomContent = Files.readString(pomXml);
        assertTrue(pomContent.contains("maven-checkstyle-plugin"),
                "В pom.xml отсутствует плагин maven-checkstyle-plugin");
        assertTrue(pomContent.contains("<configLocation>checkstyle.xml</configLocation>"),
                "В конфигурации плагина не указан configLocation=checkstyle.xml");
        // Можно добавить дополнительные проверки (версия, фаза, goals и т.д.)
    }

    static class ModelNamesProvider implements ArgumentsProvider {
        @Override
        public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
            return Stream.of(
                    // Раскомментируйте нужные модели для запуска
                    // Arguments.of("qwen3.5:9b"),
                    // Arguments.of("qwen2.5-coder:7b"),
                    // Arguments.of("qwen3:8b"),
                    // Arguments.of("qwen3:14b"),
                    // Arguments.of("qwen2.5:1.5b"),
                    // Arguments.of("gemma3:12b")
                    Arguments.of("deepseek-v4-flash")
                    // Arguments.of("gemma3:4b-it-qat"),
                    // Arguments.of("gemma3:4b"),
                    // Arguments.of("gemma3:12b-it-qat")
            );
        }
    }
}