package ru.mirent;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ServerArgs тесты")
class ServerArgsTest {

    @Nested
    @DisplayName("Тесты парсинга аргументов")
    class ParseArgsTests {

        @Test
        @DisplayName("givenNoUsageStatisticsArgWhenParseArgsThenReturnsFalse")
        void givenNoUsageStatisticsArgWhenParseArgsThenReturnsFalse() {
            String[] args = {"--no-usage-statistics"};

            boolean result = Server.parseArgs(args);

            assertFalse(result);
        }

        @Test
        @DisplayName("givenNoArgsWhenParseArgsThenReturnsTrue")
        void givenNoArgsWhenParseArgsThenReturnsTrue() {
            String[] args = {};

            boolean result = Server.parseArgs(args);

            assertTrue(result);
        }

        @Test
        @DisplayName("givenUnknownArgsWhenParseArgsThenReturnsTrue")
        void givenUnknownArgsWhenParseArgsThenReturnsTrue() {
            String[] args = {"--unknown-flag", "--another-flag"};

            boolean result = Server.parseArgs(args);

            assertTrue(result);
        }

        @Test
        @DisplayName("givenMultipleArgsWithNoUsageStatisticsWhenParseArgsThenReturnsFalse")
        void givenMultipleArgsWithNoUsageStatisticsWhenParseArgsThenReturnsFalse() {
            String[] args = {"--some-flag", "--no-usage-statistics", "--other-flag"};

            boolean result = Server.parseArgs(args);

            assertFalse(result);
        }

        @Test
        @DisplayName("givenNullArgsWhenParseArgsThenReturnsTrue")
        void givenNullArgsWhenParseArgsThenReturnsTrue() {
            String[] args = null;

            boolean result = Server.parseArgs(args);

            assertTrue(result);
        }
    }
}
