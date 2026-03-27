package ru.mirent;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import ru.mirent.logging.ToolLogger;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Тесты флага --debug")
class DebugFlagTest {

    @AfterEach
    void tearDown() {
        System.clearProperty("jarunpacker.debug");
    }

    @Nested
    @DisplayName("Парсинг флага --debug")
    class DebugFlagParsingTests {

        @Test
        @DisplayName("givenDebugArgWhenParseArgsThenDebugPropertyIsSet")
        void givenDebugArgWhenParseArgsThenDebugPropertyIsSet() {
            String[] args = {"--debug"};

            Server.parseArgs(args);

            assertEquals("true", System.getProperty("jarunpacker.debug"));
        }

        @Test
        @DisplayName("givenNoDebugArgWhenParseArgsThenDebugPropertyIsNull")
        void givenNoDebugArgWhenParseArgsThenDebugPropertyIsNull() {
            String[] args = {};

            Server.parseArgs(args);

            assertNull(System.getProperty("jarunpacker.debug"));
        }

        @Test
        @DisplayName("givenDebugArgWhenParseArgsThenToolLoggerIsDebugEnabled")
        void givenDebugArgWhenParseArgsThenToolLoggerIsDebugEnabled() {
            String[] args = {"--debug"};

            Server.parseArgs(args);

            assertTrue(ToolLogger.isDebugEnabled());
        }

        @Test
        @DisplayName("givenNoDebugArgWhenParseArgsThenToolLoggerIsNotDebugEnabled")
        void givenNoDebugArgWhenParseArgsThenToolLoggerIsNotDebugEnabled() {
            String[] args = {};

            Server.parseArgs(args);

            assertFalse(ToolLogger.isDebugEnabled());
        }
    }
}
