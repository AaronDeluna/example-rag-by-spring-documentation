package org.mirent.skills.tests.inner.unit;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mirent.skills.exeptions.CommandNotFoundException;
import org.mirent.skills.util.cli.OsAwareCommandResolver;
import org.mirent.skills.util.cli.OsType;

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
class OsAwareCommandResolverTest {

    @Test
    @DisplayName("Находит исполняемый файл по абсолютному пути как конкретный файл")
    void resolvesExactFileFromFallback(@TempDir Path tempDir) throws IOException {
        // Создаём временный исполняемый файл
        Path executable = tempDir.resolve("my-cli.js");
        Files.createFile(executable);
        makeExecutable(executable);

        Map<OsType, List<Path>> fallbacks = Map.of(
                OsType.LINUX, List.of(executable)
        );
        OsAwareCommandResolver resolver = new OsAwareCommandResolver(fallbacks);

        String result = resolver.resolveExecutable("my-cli.js");
        assertEquals(executable.toString(), result);
    }

    @Test
    @DisplayName("Находит исполняемый файл внутри fallback-директории по имени команды")
    void resolvesFileInsideFallbackDir(@TempDir Path tempDir) throws IOException {
        // Создаём /tempDir/bin/custom-cli (уникальное имя, которого нет в PATH)
        Path binDir = tempDir.resolve("bin");
        Files.createDirectories(binDir);
        Path executable = binDir.resolve("custom-cli");
        Files.createFile(executable);
        makeExecutable(executable);

        Map<OsType, List<Path>> fallbacks = Map.of(
                OsType.LINUX, List.of(binDir) // передаём директорию, не файл
        );
        OsAwareCommandResolver resolver = new OsAwareCommandResolver(fallbacks);

        String result = resolver.resolveExecutable("custom-cli");
        assertEquals(executable.toString(), result);
    }

    @Test
    @DisplayName("Бросает CommandNotFoundException, если файл не найден нигде")
    void throwsWhenNotFound(@TempDir Path tempDir) {
        Map<OsType, List<Path>> fallbacks = Map.of(
                OsType.LINUX, List.of(tempDir.resolve("nonexistent"))
        );
        OsAwareCommandResolver resolver = new OsAwareCommandResolver(fallbacks);

        assertThrows(CommandNotFoundException.class,
                () -> resolver.resolveExecutable("no-such-command"));
    }

    @Test
    @DisplayName("Использует fallback-пути для текущей ОС, игнорируя другие ОС")
    void usesOnlyCurrentOsFallbacks(@TempDir Path tempDir) throws IOException {
        Path executable = tempDir.resolve("unique-cli-tool");
        Files.createFile(executable);
        makeExecutable(executable);

        // fallback только для LINUX, остальные пустые
        Map<OsType, List<Path>> fallbacks = Map.of(
                OsType.LINUX, List.of(executable),
                OsType.WINDOWS, List.of(Path.of("C:\\nonexistent\\qwen.exe")),
                OsType.MAC, List.of(Path.of("/usr/local/bin/qwen"))
        );
        OsAwareCommandResolver resolver = new OsAwareCommandResolver(fallbacks);

        String result = resolver.resolveExecutable("unique-cli-tool");
        assertEquals(executable.toString(), result);
    }

    private static void makeExecutable(Path file) throws IOException {
        try {
            Set<PosixFilePermission> perms = Set.of(
                    PosixFilePermission.OWNER_READ,
                    PosixFilePermission.OWNER_WRITE,
                    PosixFilePermission.OWNER_EXECUTE
            );
            Files.setPosixFilePermissions(file, perms);
        } catch (UnsupportedOperationException e) {
            // Windows — setExecutable
            file.toFile().setExecutable(true);
        }
    }
}
