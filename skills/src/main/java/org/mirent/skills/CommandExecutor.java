package org.mirent.skills;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteWatchdog;
import org.apache.commons.exec.PumpStreamHandler;
import org.mirent.skills.dto.command.CommandRequestDto;
import org.mirent.skills.dto.command.CommandResultDto;
import org.mirent.skills.exeptions.MissingCommandPartsException;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Slf4j
public class CommandExecutor {

    public CommandResultDto execute(CommandRequestDto request) throws Exception {
        List<String> commandParts = request.getCommand();
        if (commandParts == null || commandParts.isEmpty()) {
            throw new MissingCommandPartsException();
        }

        CommandLine command = new CommandLine(commandParts.get(0));
        for (int i = 1; i < commandParts.size(); i++) {
            command.addArgument(commandParts.get(i), false);
        }

        ByteArrayOutputStream stdout = new ByteArrayOutputStream();
        ByteArrayOutputStream stderr = new ByteArrayOutputStream();

        ExecuteWatchdog watchdog = ExecuteWatchdog.builder()
                .setTimeout(request.getTimeout())
                .get();

        DefaultExecutor executor = DefaultExecutor.builder()
                .get();

        executor.setWatchdog(watchdog);
        executor.setStreamHandler(new PumpStreamHandler(stdout, stderr));
        executor.setExitValues(null);
        if (request.getWorkingDirectory() != null) {
            executor.setWorkingDirectory(request.getWorkingDirectory().toFile());
        }

        log.info("Executing command: {}, workingDirectory={}, timeout={}",
                command,
                request.getWorkingDirectory(),
                request.getTimeout());

        int exitCode = executor.execute(command);

        return new CommandResultDto(
                stdout.toString(StandardCharsets.UTF_8),
                stderr.toString(StandardCharsets.UTF_8),
                exitCode,
                watchdog.killedProcess()
        );
    }
}
