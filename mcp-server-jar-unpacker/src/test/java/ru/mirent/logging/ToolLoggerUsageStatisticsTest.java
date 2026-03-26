package ru.mirent.logging;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ToolLogger Usage Statistics тесты")
class ToolLoggerUsageStatisticsTest {

    @TempDir
    Path tempDir;

    private String originalUserDir;

    @BeforeEach
    void setUp() {
        originalUserDir = System.getProperty("user.dir");
        System.setProperty("user.dir", tempDir.toString());
        ToolLogger.setUsageStatisticsEnabled(true);
    }

    @AfterEach
    void tearDown() {
        System.setProperty("user.dir", originalUserDir);
        ToolLogger.setUsageStatisticsEnabled(true);
    }

    @Nested
    @DisplayName("Тесты флага usage statistics")
    class UsageStatisticsFlagTests {

        @Test
        @DisplayName("givenUsageStatisticsDisabledWhenLogToolCallThenNoLogFileCreated")
        void givenUsageStatisticsDisabledWhenLogToolCallThenNoLogFileCreated() {
            ToolLogger.setUsageStatisticsEnabled(false);

            ToolLogger.logToolCall("test_tool", "SUCCESS", 100, "{}");

            Path logFile = Paths.get(tempDir.toString(), "jar-unpacker.log");
            assertFalse(Files.exists(logFile), "Лог-файл не должен быть создан");
        }

        @Test
        @DisplayName("givenUsageStatisticsEnabledWhenLogToolCallThenLogFileCreated")
        void givenUsageStatisticsEnabledWhenLogToolCallThenLogFileCreated() throws IOException {
            ToolLogger.setUsageStatisticsEnabled(true);

            ToolLogger.logToolCall("test_tool", "SUCCESS", 100, "{}");

            Path logFile = Paths.get(tempDir.toString(), "jar-unpacker.log");
            assertTrue(Files.exists(logFile), "Лог-файл должен быть создан");

            String content = Files.readString(logFile);
            assertTrue(content.contains("test_tool"));
            assertTrue(content.contains("SUCCESS"));
        }

        @Test
        @DisplayName("givenUsageStatisticsDisabledWhenLogDebugThenNoLogFileCreated")
        void givenUsageStatisticsDisabledWhenLogDebugThenNoLogFileCreated() {
            ToolLogger.setUsageStatisticsEnabled(false);

            ToolLogger.logDebug("Test debug message");

            Path logFile = Paths.get(tempDir.toString(), "jar-unpacker.log");
            assertFalse(Files.exists(logFile), "Лог-файл не должен быть создан");
        }

        @Test
        @DisplayName("givenUsageStatisticsEnabledWhenLogDebugThenLogFileCreated")
        void givenUsageStatisticsEnabledWhenLogDebugThenLogFileCreated() throws IOException {
            // Включаем DEBUG-режим через системное свойство
            System.setProperty("jarunpacker.debug", "true");
            ToolLogger.setUsageStatisticsEnabled(true);

            ToolLogger.logDebug("Test debug message");

            Path logFile = Paths.get(tempDir.toString(), "jar-unpacker.log");
            assertTrue(Files.exists(logFile), "Лог-файл должен быть создан");

            String content = Files.readString(logFile);
            assertTrue(content.contains("DEBUG"));
            assertTrue(content.contains("Test debug message"));

            // Очищаем системное свойство
            System.clearProperty("jarunpacker.debug");
        }

        @Test
        @DisplayName("givenUsageStatisticsDisabledWhenMultipleLogCallsThenNoLogFileCreated")
        void givenUsageStatisticsDisabledWhenMultipleLogCallsThenNoLogFileCreated() {
            ToolLogger.setUsageStatisticsEnabled(false);

            ToolLogger.logToolCall("tool1", "SUCCESS", 50, "{}");
            ToolLogger.logToolCall("tool2", "ERROR: test", 120, "{\"key\":\"value\"}");
            ToolLogger.logDebug("Debug message 1");
            ToolLogger.logDebug("Debug message 2");

            Path logFile = Paths.get(tempDir.toString(), "jar-unpacker.log");
            assertFalse(Files.exists(logFile), "Лог-файл не должен быть создан после множественных вызовов");
        }

        @Test
        @DisplayName("givenIsUsageStatisticsEnabledWhenFlagSetThenReturnsCorrectValue")
        void givenIsUsageStatisticsEnabledWhenFlagSetThenReturnsCorrectValue() {
            ToolLogger.setUsageStatisticsEnabled(true);
            assertTrue(ToolLogger.isUsageStatisticsEnabled());

            ToolLogger.setUsageStatisticsEnabled(false);
            assertFalse(ToolLogger.isUsageStatisticsEnabled());
        }
    }
}
