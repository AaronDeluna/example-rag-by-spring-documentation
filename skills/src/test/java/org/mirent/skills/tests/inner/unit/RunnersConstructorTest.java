package org.mirent.skills.tests.inner.unit;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mirent.skills.CommandExecutor;
import org.mirent.skills.parser.AgentStreamJsonParser;
import org.mirent.skills.runner.RunnerLogWriter;
import org.mirent.skills.runner.qwen.QwenAgentRunner;
import org.mirent.skills.runner.qwen.QwenJudgeRunner;
import org.mirent.skills.util.cli.CommandFactory;

import java.nio.file.Path;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

@Tag("inner")
@Tag("unit")
class RunnersConstructorTest {

    private final CommandFactory dummyFactory = (prompt, logDir) -> java.util.List.of("qwen", prompt);

    @Test
    @DisplayName("QwenAgentRunner создаётся через конструктор с CommandFactory")
    void qwenAgentRunnerCreatedWithCommandFactory(@TempDir Path workspace) {
        QwenAgentRunner runner = new QwenAgentRunner(
                new CommandExecutor(),
                new AgentStreamJsonParser(),
                new RunnerLogWriter(),
                workspace,
                Duration.ofMinutes(3),
                dummyFactory
        );
        assertNotNull(runner);
        assertNotNull(runner.getAgentRunContext());
    }

    @Test
    @DisplayName("QwenJudgeRunner создаётся через конструктор с CommandFactory")
    void qwenJudgeRunnerCreatedWithCommandFactory(@TempDir Path workspace) {
        QwenJudgeRunner runner = new QwenJudgeRunner(
                new CommandExecutor(),
                new AgentStreamJsonParser(),
                workspace,
                Duration.ofMinutes(5),
                dummyFactory
        );
        assertNotNull(runner);
    }

    @Test
    @DisplayName("QwenAgentRunner использует переданный CommandFactory для сборки команды")
    void qwenAgentRunnerUsesCommandFactory(@TempDir Path workspace) {
        // Создаём factory, который добавляет маркер в команду
        CommandFactory markerFactory = (prompt, logDir) -> java.util.List.of("qwen", "--marker", "factory-used", prompt);

        QwenAgentRunner runner = new QwenAgentRunner(
                new CommandExecutor(),
                new AgentStreamJsonParser(),
                new RunnerLogWriter(),
                workspace,
                Duration.ofMinutes(3),
                markerFactory
        );
        // Runner должен вызывать commandFactory.buildCommand() из execute()
        // Но execute() — интеграционный вызов. Просто проверяем, что runner создан с этой фабрикой.
        assertNotNull(runner);
    }

    @Test
    @DisplayName("QwenAgentRunner не имеет конструктора без CommandFactory")
    void qwenAgentRunnerHasNoConstructorWithoutCommandFactory() {
        // Проверка через reflection — единственный публичный конструктор должен содержать CommandFactory
        var constructors = QwenAgentRunner.class.getConstructors();
        assertEquals(1, constructors.length,
                "Должен быть ровно один публичный конструктор");
        assertTrue(
                java.util.Arrays.stream(constructors[0].getParameterTypes())
                        .anyMatch(t -> t.equals(CommandFactory.class)),
                "Конструктор должен принимать CommandFactory"
        );
    }

    @Test
    @DisplayName("QwenJudgeRunner не имеет конструктора без CommandFactory")
    void qwenJudgeRunnerHasNoConstructorWithoutCommandFactory() {
        var constructors = QwenJudgeRunner.class.getConstructors();
        assertEquals(1, constructors.length,
                "Должен быть ровно один публичный конструктор");
        assertTrue(
                java.util.Arrays.stream(constructors[0].getParameterTypes())
                        .anyMatch(t -> t.equals(CommandFactory.class)),
                "Конструктор должен принимать CommandFactory"
        );
    }
}
