package org.mirent.skills.tests.inner.unit;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mirent.skills.util.cli.OsType;
import org.mirent.skills.util.cli.QwenCommandFactoryImpl;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermission;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@Tag("inner")
@Tag("unit")
class QwenCommandFactoryImplTest {

    @Test
    @DisplayName("buildCommand собирает команду с executable и аргументами без logDir")
    void buildsCommandWithoutLogDir(@TempDir Path tempDir) throws IOException {
        Path executable = createExecutable(tempDir, "qwen");

        QwenCommandFactoryImpl factory = new QwenCommandFactoryImpl(
                name -> executable.toString(),
                List.of("--output-format", "stream-json", "--approval-mode", "yolo"),
                List.of()
        );

        List<String> command = factory.buildCommand("test prompt", null);

        assertAll(
                () -> assertEquals(executable.toString(), command.get(0)),
                () -> assertTrue(command.contains("--output-format")),
                () -> assertTrue(command.contains("stream-json")),
                () -> assertTrue(command.contains("--approval-mode")),
                () -> assertTrue(command.contains("yolo")),
                () -> assertEquals("test prompt", command.get(command.size() - 1)),
                () -> assertFalse(command.contains("--openai-logging"))
        );
    }

    @Test
    @DisplayName("buildCommand добавляет --openai-logging флаги при наличии logDir")
    void addsOpenAiLoggingFlagsWhenLogDirProvided(@TempDir Path tempDir) throws IOException {
        Path executable = createExecutable(tempDir, "qwen");
        Path logDir = tempDir.resolve("logs");

        QwenCommandFactoryImpl factory = new QwenCommandFactoryImpl(
                name -> executable.toString(),
                List.of("--output-format", "stream-json"),
                List.of()
        );

        List<String> command = factory.buildCommand("test prompt", logDir);

        assertAll(
                () -> assertTrue(command.contains("--openai-logging")),
                () -> assertTrue(command.contains("true")),
                () -> assertTrue(command.contains("--openai-logging-dir")),
                () -> assertTrue(command.contains(logDir.toAbsolutePath().toString())),
                () -> assertEquals("test prompt", command.get(command.size() - 1))
        );
    }

    @Test
    @DisplayName("buildCommand добавляет префикс для Windows")
    void addsPrefixForWindows() throws IOException {
        // Используем Path.of(), чтобы не создавать файл — resolver не будет вызван,
        // так как тестируем только префикс. Но factory требует resolver.
        // Создадим заглушку.
        QwenCommandFactoryImpl factory = new QwenCommandFactoryImpl(
                name -> "qwen",
                List.of("--output-format", "stream-json"),
                List.of("cmd.exe", "/c")
        );

        List<String> command = factory.buildCommand("test", null);

        assertAll(
                () -> assertEquals("cmd.exe", command.get(0)),
                () -> assertEquals("/c", command.get(1)),
                () -> assertEquals("qwen", command.get(2)),
                () -> assertEquals("test", command.get(command.size() - 1))
        );
    }

    @Test
    @DisplayName("buildCommand возвращает неизменяемый список")
    void returnsUnmodifiableList(@TempDir Path tempDir) throws IOException {
        Path executable = createExecutable(tempDir, "qwen");

        QwenCommandFactoryImpl factory = new QwenCommandFactoryImpl(
                name -> executable.toString(),
                List.of(),
                List.of()
        );

        List<String> command = factory.buildCommand("test", null);
        assertThrows(UnsupportedOperationException.class, () -> command.add("extra"));
    }

    private static Path createExecutable(Path dir, String name) throws IOException {
        Path file = dir.resolve(name);
        Files.createFile(file);
        try {
            Files.setPosixFilePermissions(file, Set.of(
                    PosixFilePermission.OWNER_READ,
                    PosixFilePermission.OWNER_WRITE,
                    PosixFilePermission.OWNER_EXECUTE
            ));
        } catch (UnsupportedOperationException e) {
            file.toFile().setExecutable(true);
        }
        return file;
    }
}
