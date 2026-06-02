package ru.mirent.skills.tests.inner;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import ru.mirent.skills.CommandExecutor;
import ru.mirent.skills.dto.command.CommandRequestDto;
import ru.mirent.skills.dto.command.CommandResultDto;

import java.io.FileNotFoundException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;

import static ru.mirent.skills.runner.qwen.QwenAgentRunner.resolveDefaultWorkingDirectory;

@Slf4j
public class QwenAvailabilityTest {

    private final CommandExecutor executor = new CommandExecutor();

    @Test
    void qwenIsAvailableTest() throws Exception {
        CommandRequestDto commandRequestDto = new CommandRequestDto(
                List.of("which", findQwenPathByOs().toString()),
                resolveDefaultWorkingDirectory(), Duration.ofMinutes(3));

        CommandResultDto whichResult = executor.execute(commandRequestDto);
        Assertions.assertEquals(0, whichResult.getExitCode());

        String qwenPath = whichResult.getStdout().trim();
        Assertions.assertFalse(qwenPath.isEmpty());
    }

    @Test
    void qwenLinuxAndMacOSVersionTest() throws Exception {
        CommandRequestDto commandRequestDto = new CommandRequestDto(
                List.of(findQwenPathByOs().toString(), "--version"),
                resolveDefaultWorkingDirectory(), Duration.ofMinutes(3));

        CommandResultDto actualCommandResultDto = executor.execute(commandRequestDto);
        Assertions.assertEquals(0, actualCommandResultDto.getExitCode());
        Assertions.assertFalse(actualCommandResultDto.getStdout().isEmpty());

        String actualVersion = actualCommandResultDto.getStdout().trim();
        Assertions.assertEquals("0.15.10", actualVersion);
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
