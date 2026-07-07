package org.mirent.skills.tests.inner.unit;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mirent.skills.exeptions.AgentRunnerConfigurationException;
import org.mirent.skills.exeptions.CommandNotFoundException;

import static org.junit.jupiter.api.Assertions.*;

@Tag("inner")
@Tag("unit")
class CommandNotFoundExceptionTest {

    @Test
    @DisplayName("CommandNotFoundException наследуется от AgentRunnerConfigurationException")
    void extendsAgentRunnerConfigurationException() {
        CommandNotFoundException exception = new CommandNotFoundException("test error");
        assertInstanceOf(AgentRunnerConfigurationException.class, exception);
        assertInstanceOf(RuntimeException.class, exception);
    }

    @Test
    @DisplayName("CommandNotFoundException содержит переданное сообщение")
    void containsMessage() {
        String message = "Не найден исполняемый файл для команды: qwen";
        CommandNotFoundException exception = new CommandNotFoundException(message);
        assertEquals(message, exception.getMessage());
    }
}
