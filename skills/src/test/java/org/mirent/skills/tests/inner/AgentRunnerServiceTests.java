package org.mirent.skills.tests.inner;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mirent.skills.exeptions.MissingAgentCliException;
import org.mirent.skills.exeptions.UnsupportedAgentCliException;
import org.mirent.skills.service.AgentCli;
import org.mirent.skills.service.AgentRunnerProperties;
import org.mirent.skills.service.AgentRunnerService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AgentRunnerServiceTests {

    @Test
    @DisplayName("Создается из default properties")
    void createsFromDefaultProperties() {
        assertDoesNotThrow(AgentRunnerService::new);
    }

    @Test
    @DisplayName("Выбрасывает ошибку, если CLI не указана")
    void throwsWhenCliPropertyIsBlank() {
        MissingAgentCliException exception = assertThrows(
                MissingAgentCliException.class,
                () -> AgentCli.fromProperty("")
        );

        assertTrue(exception.getMessage().contains("Не передано название CLI для запуска"));
        assertTrue(exception.getMessage().contains(AgentRunnerProperties.CLI_PROPERTY));
    }

    @Test
    @DisplayName("Выбрасывает ошибку для неизвестной CLI")
    void throwsWhenCliIsUnsupported() {
        UnsupportedAgentCliException exception = assertThrows(
                UnsupportedAgentCliException.class,
                () -> AgentCli.fromProperty("unknown")
        );

        assertTrue(exception.getMessage().contains("Неподдерживаемая CLI для запуска"));
        assertTrue(exception.getMessage().contains("unknown"));
    }

    @Test
    @DisplayName("Парсит Qwen CLI без учета регистра")
    void parsesQwenCliIgnoringCase() {
        assertEquals(AgentCli.QWEN, AgentCli.fromProperty("qwen"));
    }
}
