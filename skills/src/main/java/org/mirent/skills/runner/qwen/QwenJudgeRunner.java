package org.mirent.skills.runner.qwen;

import lombok.extern.slf4j.Slf4j;
import org.mirent.skills.CommandExecutor;
import org.mirent.skills.dto.agent.AgentLogDto;
import org.mirent.skills.dto.command.CommandRequestDto;
import org.mirent.skills.dto.command.CommandResultDto;
import org.mirent.skills.parser.AgentStreamJsonParser;
import org.mirent.skills.runner.JudgeRunner;
import org.mirent.skills.util.cli.CommandFactory;

import java.nio.file.Path;
import java.time.Duration;
import java.util.List;

@Slf4j
public class QwenJudgeRunner implements JudgeRunner {

    public static final Duration DEFAULT_TIMEOUT = Duration.ofMinutes(5);

    private final CommandExecutor commandExecutor;
    private final AgentStreamJsonParser agentStreamJsonParser;
    private final Path workingDirectory;
    private final Duration timeout;
    private final CommandFactory commandFactory;

    public QwenJudgeRunner(
            CommandExecutor commandExecutor,
            AgentStreamJsonParser agentStreamJsonParser,
            Path workingDirectory,
            Duration timeout,
            CommandFactory commandFactory
    ) {
        this.commandExecutor = commandExecutor;
        this.agentStreamJsonParser = agentStreamJsonParser;
        this.workingDirectory = workingDirectory;
        this.timeout = timeout;
        this.commandFactory = commandFactory;
    }

    @Override
    public String runPrompt(String prompt) throws Exception {
        List<String> command = commandFactory.buildCommand(prompt, null);

        CommandResultDto result = commandExecutor.execute(new CommandRequestDto(
                command,
                workingDirectory,
                timeout
        ));

        AgentLogDto judgeLog = agentStreamJsonParser.parse(result.getStdout());
        return judgeLog.getFinalResult();
    }
}
