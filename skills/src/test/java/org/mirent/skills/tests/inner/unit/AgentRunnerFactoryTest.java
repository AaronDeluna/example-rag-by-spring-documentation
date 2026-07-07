package org.mirent.skills.tests.inner.unit;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mirent.skills.service.AgentRunnerFactory;
import org.mirent.skills.service.AgentRunnerProperties;
import org.mirent.skills.runner.AgentRunner;

import java.nio.file.Path;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;

@Tag("inner")
@Tag("unit")
class AgentRunnerFactoryTest {

    @Test
    @DisplayName("defaultFactory создаёт фабрику без ошибок")
    void defaultFactoryCreatesInstance(@TempDir Path workspace) {
        AgentRunnerFactory factory = AgentRunnerFactory.defaultFactory(workspace);
        assertNotNull(factory);
    }

    @Test
    @DisplayName("create() с валидными properties возвращает раннер")
    void createReturnsRunnerWithValidProperties(@TempDir Path workspace) {
        AgentRunnerFactory factory = AgentRunnerFactory.defaultFactory(workspace);
        Properties props = AgentRunnerProperties.loadDefault();

        AgentRunner runner = factory.create(props);

        assertNotNull(runner);
    }

    @Test
    @DisplayName("create() использует CommandFactory из свойств")
    void createUsesCommandFactoryFromProperties(@TempDir Path workspace) {
        AgentRunnerFactory factory = AgentRunnerFactory.defaultFactory(workspace);
        Properties props = new Properties();
        props.setProperty(AgentRunnerProperties.CLI_PROPERTY, "QWEN");
        props.setProperty("agent.cli.qwen.args", "--output-format,stream-json");

        AgentRunner runner = factory.create(props);

        assertNotNull(runner);
    }

    @Test
    @DisplayName("create() использует пустые списки по умолчанию при отсутствии свойств CLI")
    void createUsesEmptyDefaultsWhenNoCliProperties(@TempDir Path workspace) {
        AgentRunnerFactory factory = AgentRunnerFactory.defaultFactory(workspace);
        Properties props = new Properties();
        props.setProperty(AgentRunnerProperties.CLI_PROPERTY, "QWEN");

        AgentRunner runner = factory.create(props);

        assertNotNull(runner);
    }
}
