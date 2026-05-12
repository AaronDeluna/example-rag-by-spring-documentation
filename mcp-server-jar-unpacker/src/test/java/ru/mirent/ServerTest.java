package ru.mirent;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import ru.mirent.logging.ToolLogger;

import java.io.File;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Интеграционные тесты Server")
class ServerTest {

    @Nested
    @DisplayName("logToolCall()")
    class LogToolCallTests {

        @Test
        @DisplayName("givenToolCallWhenLogToolCallThenWriteToLogFile()")
        void givenToolCallWhenLogToolCallThenWriteToLogFile() {
            // Очищаем старый лог
            File logFile = new File("jar-unpacker.log");
            if (logFile.exists()) {
                logFile.delete();
            }

            // Вызываем логирование через ToolLogger
            ToolLogger.logToolCall("test_tool", "SUCCESS", 100L, "{\"key\":\"value\"}");

            // Проверяем, что файл создан
            assertTrue(logFile.exists(), "Log file should be created");

            // Проверяем содержимое
            try {
                Scanner scanner = new Scanner(logFile);
                String content = scanner.useDelimiter("\\A").next();
                scanner.close();
                assertTrue(content.contains("test_tool"), "Log should contain tool name");
                assertTrue(content.contains("SUCCESS"), "Log should contain status");
                assertTrue(content.contains("100ms"), "Log should contain elapsed time");
                assertTrue(content.contains("arguments={\"key\":\"value\"}"), "Log should contain arguments");
            } catch (java.io.FileNotFoundException e) {
                fail("Log file not found: " + e.getMessage());
            }

            // Очищаем после теста
            logFile.delete();
        }

        @Test
        @DisplayName("givenErrorStatusWhenLogToolCallThenWriteErrorToLogFile()")
        void givenErrorStatusWhenLogToolCallThenWriteErrorToLogFile() {
            File logFile = new File("jar-unpacker.log");
            if (logFile.exists()) {
                logFile.delete();
            }

            // Вызываем логирование через ToolLogger
            ToolLogger.logToolCall("test_tool", "ERROR: Something went wrong", 50L, "{}");

            assertTrue(logFile.exists(), "Log file should be created");

            try {
                Scanner scanner = new Scanner(logFile);
                String content = scanner.useDelimiter("\\A").next();
                scanner.close();
                assertTrue(content.contains("ERROR"), "Log should contain ERROR status");
                assertTrue(content.contains("Something went wrong"), "Log should contain error message");
            } catch (java.io.FileNotFoundException e) {
                fail("Log file not found: " + e.getMessage());
            }

            logFile.delete();
        }
    }
}
