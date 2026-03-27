package ru.mirent.logging;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.format.DateTimeFormatter;

/**
 * Логгер вызовов инструментов MCP с поддержкой DEBUG-режима и возможностью отключения.
 * <p>
 * Поддерживает:
 * <ul>
 *   <li>Логирование вызовов инструментов с таймингом</li>
 *   <li>DEBUG-режим через системное свойство {@code -Djarunpacker.debug=true}</li>
 *   <li>Ротацию логов при достижении 10 MB</li>
 *   <li>Отключение логирования через {@code --no-usage-statistics}</li>
 * </ul>
 */
public class ToolLogger {

    private static final String LOG_FILE_NAME = "jar-unpacker.log";
    private static final int MAX_LOG_BYTES = 10 * 1024 * 1024;
    private static final DateTimeFormatter LOG_FORMATTER = DateTimeFormatter.ISO_INSTANT;

    private static volatile boolean usageStatisticsEnabled = true;

    private ToolLogger() {
        // утилитный класс, не предназначен для создания экземпляров
    }

    private static String getLogFilePath() {
        return System.getProperty("user.dir") + "/" + LOG_FILE_NAME;
    }

    private static boolean isDebugMode() {
        return "true".equals(System.getProperty("jarunpacker.debug"));
    }

    /**
     * Включить или отключить логирование статистики использования.
     *
     * @param enabled true если логирование включено, false если отключено
     */
    public static void setUsageStatisticsEnabled(boolean enabled) {
        usageStatisticsEnabled = enabled;
    }

    /**
     * Проверить, включено ли логирование статистики использования.
     *
     * @return true если логирование включено
     */
    public static boolean isUsageStatisticsEnabled() {
        return usageStatisticsEnabled;
    }

    /**
     * Записать лог вызова инструмента.
     *
     * @param toolName   имя инструмента
     * @param status     статус выполнения (SUCCESS или ERROR: сообщение)
     * @param elapsedMs  время выполнения в миллисекундах
     * @param arguments  аргументы инструмента в формате JSON
     */
    public static void logToolCall(String toolName, String status, long elapsedMs, String arguments) {
        if (!usageStatisticsEnabled) {
            return;
        }
        try {
            rotateLogIfNeeded();

            String timestamp = LOG_FORMATTER.format(Instant.now());
            String logEntry = String.format(
                    "%s | %s | %s | %dms | arguments=%s%n",
                    timestamp,
                    toolName,
                    status,
                    elapsedMs,
                    arguments
            );

            try (FileWriter writer = new FileWriter(getLogFilePath(), true)) {
                writer.write(logEntry);
            }
        } catch (IOException e) {
            System.err.println("Failed to write log: " + e.getMessage());
        }
    }

    /**
     * Записать DEBUG-сообщение (только если включён DEBUG-режим и логирование не отключено).
     *
     * @param message сообщение для логирования
     */
    public static void logDebug(String message) {
        if (!usageStatisticsEnabled) {
            return;
        }
        if (isDebugMode()) {
            try {
                rotateLogIfNeeded();

                String timestamp = LOG_FORMATTER.format(Instant.now());
                String logEntry = String.format(
                        "%s | DEBUG | %s%n",
                        timestamp,
                        message
                );

                try (FileWriter writer = new FileWriter(getLogFilePath(), true)) {
                    writer.write(logEntry);
                }
            } catch (IOException e) {
                System.err.println("Failed to write debug log: " + e.getMessage());
            }
        }
    }

    /**
     * Проверить необходимость ротации лога.
     */
    private static void rotateLogIfNeeded() throws IOException {
        Path logPath = Paths.get(getLogFilePath());
        if (Files.exists(logPath) && Files.size(logPath) >= MAX_LOG_BYTES) {
            rotateLog();
        }
    }

    /**
     * Выполнить ротацию логов.
     * <p>
     * Файлы сдвигаются: .log → .log.1 → .log.2 → .log.3
     */
    private static void rotateLog() throws IOException {
        String logFilePath = getLogFilePath();
        Path log3 = Paths.get(logFilePath + ".3");
        Path log2 = Paths.get(logFilePath + ".2");
        Path log1 = Paths.get(logFilePath + ".1");
        Path logPath = Paths.get(logFilePath);

        if (Files.exists(log2)) {
            Files.move(log2, log3, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        }
        if (Files.exists(log1)) {
            Files.move(log1, log2, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        }
        Files.move(logPath, log1, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
    }

    /**
     * Вернуть путь к файлу лога.
     *
     * @return абсолютный путь к файлу лога
     */
    public static String getLogFile() {
        return getLogFilePath();
    }

    /**
     * Проверить включён ли DEBUG-режим.
     *
     * @return true если DEBUG-режим включён
     */
    public static boolean isDebugEnabled() {
        return isDebugMode();
    }
}
