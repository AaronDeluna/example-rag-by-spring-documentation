package ru.mirent.logging;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Тесты ToolLogger")
class ToolLoggerTest {

    @TempDir
    Path tempDir;

    private String originalUserDir;

    @BeforeEach
    void setUp() {
        originalUserDir = System.getProperty("user.dir");
        System.setProperty("user.dir", tempDir.toString());
    }

    @AfterEach
    void tearDown() {
        System.setProperty("user.dir", originalUserDir);
        System.clearProperty("jarunpacker.debug");
    }

    @Nested
    @DisplayName("logToolCall()")
    class LogToolCallTests {

        @Test
        @DisplayName("givenLogToolCallWhenWriteThenWritesToLog")
        void givenLogToolCallWhenWriteThenWritesToLog() throws Exception {
            ToolLogger.logToolCall("find_class_in_m2", "SUCCESS", 100,
                    "{\"class_name\":\"test.Test\"}");

            Path logFile = tempDir.resolve("jar-unpacker.log");
            assertTrue(Files.exists(logFile));

            List<String> lines = Files.readAllLines(logFile);
            assertEquals(1, lines.size());

            String logLine = lines.get(0);
            assertTrue(logLine.contains("find_class_in_m2"));
            assertTrue(logLine.contains("SUCCESS"));
            assertTrue(logLine.contains("100ms"));
            assertTrue(logLine.contains("arguments={\"class_name\":\"test.Test\"}"));
        }

        @Test
        @DisplayName("givenMultipleToolCallsWhenWriteThenAppendsToLog")
        void givenMultipleToolCallsWhenWriteThenAppendsToLog() throws Exception {
            ToolLogger.logToolCall("find_class_in_m2", "SUCCESS", 100, "{}");
            ToolLogger.logToolCall("get_class_outline", "SUCCESS", 50, "{}");

            Path logFile = tempDir.resolve("jar-unpacker.log");
            List<String> lines = Files.readAllLines(logFile);

            assertEquals(2, lines.size());
            assertTrue(lines.get(0).contains("find_class_in_m2"));
            assertTrue(lines.get(1).contains("get_class_outline"));
        }

        @Test
        @DisplayName("givenToolCallWithErrorWhenWriteThenLogsErrorStatus")
        void givenToolCallWithErrorWhenWriteThenLogsErrorStatus() throws Exception {
            ToolLogger.logToolCall("get_method_source", "ERROR: Method not found", 12, "{}");

            Path logFile = tempDir.resolve("jar-unpacker.log");
            List<String> lines = Files.readAllLines(logFile);

            assertEquals(1, lines.size());
            assertTrue(lines.get(0).contains("ERROR: Method not found"));
        }
    }

    @Nested
    @DisplayName("logDebug()")
    class LogDebugTests {

        @Test
        @DisplayName("givenDebugModeWhenLogDebugThenWritesToLog")
        void givenDebugModeWhenLogDebugThenWritesToLog() throws Exception {
            System.setProperty("jarunpacker.debug", "true");

            ToolLogger.logDebug("Test debug message");

            Path logFile = tempDir.resolve("jar-unpacker.log");
            assertTrue(Files.exists(logFile));

            List<String> lines = Files.readAllLines(logFile);
            assertEquals(1, lines.size());

            String logLine = lines.get(0);
            assertTrue(logLine.contains("DEBUG"));
            assertTrue(logLine.contains("Test debug message"));
        }

        @Test
        @DisplayName("givenDebugModeDisabledWhenLogDebugThenDoesNotWriteToLog")
        void givenDebugModeDisabledWhenLogDebugThenDoesNotWriteToLog() throws Exception {
            System.clearProperty("jarunpacker.debug");

            ToolLogger.logDebug("Test debug message");

            Path logFile = tempDir.resolve("jar-unpacker.log");

            assertFalse(Files.exists(logFile));
        }

        @Test
        @DisplayName("givenDebugModeWhenMultipleLogDebugThenWritesAllToLog")
        void givenDebugModeWhenMultipleLogDebugThenWritesAllToLog() throws Exception {
            System.setProperty("jarunpacker.debug", "true");

            ToolLogger.logDebug("Debug message 1");
            ToolLogger.logDebug("Debug message 2");
            ToolLogger.logDebug("Debug message 3");

            Path logFile = tempDir.resolve("jar-unpacker.log");
            List<String> lines = Files.readAllLines(logFile);

            assertEquals(3, lines.size());
            assertTrue(lines.get(0).contains("Debug message 1"));
            assertTrue(lines.get(1).contains("Debug message 2"));
            assertTrue(lines.get(2).contains("Debug message 3"));
        }
    }

    @Nested
    @DisplayName("Ротация логов")
    class LogRotationTests {

        @Test
        @DisplayName("givenLogExceedsMaxSizeWhenWriteThenRotatesLog")
        void givenLogExceedsMaxSizeWhenWriteThenRotatesLog() throws Exception {
            Path logFile = tempDir.resolve("jar-unpacker.log");

            try (FileWriter writer = new FileWriter(logFile.toFile())) {
                for (int i = 0; i < 10 * 1024 * 1024; i++) {
                    writer.write('x');
                }
            }

            ToolLogger.logToolCall("test", "SUCCESS", 1, "{}");

            Path log1 = tempDir.resolve("jar-unpacker.log.1");
            assertTrue(Files.exists(log1));
        }

        @Test
        @DisplayName("givenLogRotationWhenRotateThenShiftsOldLogs")
        void givenLogRotationWhenRotateThenShiftsOldLogs() throws Exception {
            Path logFile = tempDir.resolve("jar-unpacker.log");
            Path log1 = tempDir.resolve("jar-unpacker.log.1");
            Path log2 = tempDir.resolve("jar-unpacker.log.2");

            // Создаём лог размером 10 MB и записываем (триггерим ротацию)
            try (FileWriter writer = new FileWriter(logFile.toFile())) {
                for (int i = 0; i < 10 * 1024 * 1024; i++) {
                    writer.write('a');
                }
            }
            ToolLogger.logToolCall("test1", "SUCCESS", 1, "{}");

            // Теперь logFile -> log.1, создаём новый logFile размером 10 MB
            try (FileWriter writer = new FileWriter(logFile.toFile())) {
                for (int i = 0; i < 10 * 1024 * 1024; i++) {
                    writer.write('b');
                }
            }
            ToolLogger.logToolCall("test2", "SUCCESS", 1, "{}");

            // После второй ротации: старый log.1 -> log.2, текущий log -> log.1
            assertTrue(Files.exists(log1));
            assertTrue(Files.exists(log2));
        }
    }

    @Nested
    @DisplayName("getLogFile()")
    class GetLogFileTests {

        @Test
        @DisplayName("givenGetLogFileThenReturnsCorrectPath")
        void givenGetLogFileThenReturnsCorrectPath() {
            String logFile = ToolLogger.getLogFile();

            assertTrue(logFile.endsWith("jar-unpacker.log"));
        }
    }

    @Nested
    @DisplayName("isDebugEnabled()")
    class IsDebugEnabledTests {

        @Test
        @DisplayName("givenDebugModeEnabledWhenIsDebugEnabledThenReturnsTrue")
        void givenDebugModeEnabledWhenIsDebugEnabledThenReturnsTrue() {
            System.setProperty("jarunpacker.debug", "true");

            assertTrue(ToolLogger.isDebugEnabled());
        }

        @Test
        @DisplayName("givenDebugModeDisabledWhenIsDebugEnabledThenReturnsFalse")
        void givenDebugModeDisabledWhenIsDebugEnabledThenReturnsFalse() {
            System.clearProperty("jarunpacker.debug");

            assertFalse(ToolLogger.isDebugEnabled());
        }
    }
}
