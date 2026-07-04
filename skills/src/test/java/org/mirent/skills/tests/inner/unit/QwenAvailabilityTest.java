package org.mirent.skills.tests.inner.unit;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mirent.skills.CommandExecutor;
import org.mirent.skills.dto.command.CommandRequestDto;
import org.mirent.skills.dto.command.CommandResultDto;

import org.mirent.skills.util.WutPreparer;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;

@Tag("inner")
@Tag("unit")
@Slf4j
public class QwenAvailabilityTest {

    private static final Path WUT_SOURCE = Path.of("src/test/resources/wut-templates");

    private final CommandExecutor executor = new CommandExecutor();

    private static Path prepareWut() throws IOException {
        return WutPreparer.builder()
                .wutSourceName("default")
                .wutSourcePath(WUT_SOURCE)
                .build()
                .prepare();
    }

    @Test
    void qwenIsAvailableTest() throws Exception {
        CommandRequestDto commandRequestDto = new CommandRequestDto(
                List.of("which", findQwenPathByOs().toString()),
                prepareWut(), Duration.ofMinutes(3));

        CommandResultDto whichResult = executor.execute(commandRequestDto);
        Assertions.assertEquals(0, whichResult.getExitCode());

        String qwenPath = whichResult.getStdout().trim();
        Assertions.assertFalse(qwenPath.isEmpty());
    }

    @Test
    void qwenLinuxAndMacOSVersionTest() throws Exception {
        CommandRequestDto commandRequestDto = new CommandRequestDto(
                List.of(findQwenPathByOs().toString(), "--version"),
                prepareWut(), Duration.ofMinutes(3));

        CommandResultDto actualCommandResultDto = executor.execute(commandRequestDto);
        Assertions.assertEquals(0, actualCommandResultDto.getExitCode());
        Assertions.assertFalse(actualCommandResultDto.getStdout().isEmpty());

        String actualVersion = actualCommandResultDto.getStdout().trim();
        Assertions.assertEquals("0.19.3", actualVersion);
    }

    private static Path findQwenPathByOs() throws FileNotFoundException {
        Path qwenPath;
        String osName = System.getProperty("os.name").toLowerCase();
        String userHome = System.getProperty("user.home");

        if (osName.contains("win")) {
            qwenPath = Path.of(userHome, ".qwen", "bin", "qwen");
        } else {
            qwenPath = Path.of(userHome, ".npm-global", "lib", "node_modules", "@qwen-code", "qwen-code", "cli.js");
        }

        if (!Files.exists(qwenPath) || !Files.isRegularFile(qwenPath)) {
            throw new FileNotFoundException(qwenPath.toAbsolutePath().toString());
        }

        log.info("Исполняемый файл приложения Qwen найден по пути: {}", qwenPath);

        return qwenPath;
    }
}
