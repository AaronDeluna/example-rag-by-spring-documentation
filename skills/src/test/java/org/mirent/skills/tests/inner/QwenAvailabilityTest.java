package org.mirent.skills.tests.inner;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mirent.skills.CommandExecutor;
import org.mirent.skills.dto.command.CommandRequestDto;
import org.mirent.skills.dto.command.CommandResultDto;

import java.time.Duration;
import java.util.List;

import static org.mirent.skills.runner.qwen.QwenAgentRunner.resolveDefaultWorkingDirectory;
import static org.mirent.skills.util.qwen.QwenPathFinder.findQwenPathByOs;

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
}
