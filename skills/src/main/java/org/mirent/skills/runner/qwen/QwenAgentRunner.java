package org.mirent.skills.runner.qwen;

import lombok.extern.slf4j.Slf4j;
import org.mirent.skills.CommandExecutor;
import org.mirent.skills.dto.agent.AgentLogDto;
import org.mirent.skills.dto.agent.AgentResultDto;
import org.mirent.skills.dto.command.CommandRequestDto;
import org.mirent.skills.dto.command.CommandResultDto;
import org.mirent.skills.dto.log.AgentRunLogDto;
import org.mirent.skills.exeptions.InvalidSkillNameException;
import org.mirent.skills.parser.AgentStreamJsonParser;
import org.mirent.skills.runner.AgentRunner;
import org.mirent.skills.runner.RunnerLogWriter;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Slf4j
public class QwenAgentRunner implements AgentRunner {

    public static final Duration DEFAULT_TIMEOUT = Duration.ofMinutes(3);

    private final CommandExecutor commandExecutor;
    private final AgentStreamJsonParser agentStreamJsonParser;
    private final RunnerLogWriter runnerLogWriter;
    private final Path workingDirectory;
    private final Duration timeout;

    public QwenAgentRunner() {
        this(new CommandExecutor(), new AgentStreamJsonParser(), resolveDefaultWorkingDirectory(), DEFAULT_TIMEOUT);
    }

    public QwenAgentRunner(CommandExecutor commandExecutor, Path workingDirectory, Duration timeout) {
        this(commandExecutor, new AgentStreamJsonParser(), workingDirectory, timeout);
    }

    public QwenAgentRunner(
            CommandExecutor commandExecutor,
            AgentStreamJsonParser agentStreamJsonParser,
            Path workingDirectory,
            Duration timeout
    ) {
        this(commandExecutor, agentStreamJsonParser, new RunnerLogWriter(), workingDirectory, timeout);
    }

    public QwenAgentRunner(
            CommandExecutor commandExecutor,
            AgentStreamJsonParser agentStreamJsonParser,
            RunnerLogWriter runnerLogWriter,
            Path workingDirectory,
            Duration timeout
    ) {
        this.commandExecutor = commandExecutor;
        this.agentStreamJsonParser = agentStreamJsonParser;
        this.runnerLogWriter = runnerLogWriter;
        this.workingDirectory = workingDirectory;
        this.timeout = timeout;
    }

    @Override
    public AgentResultDto executeUserPrompt(String prompt) throws Exception {
        return execute(null, prompt);
    }

    @Override
    public AgentResultDto executeSkillPrompt(String skillName, String prompt) throws Exception {
        log.info("[SKILL_EXECUTION]: {}", skillName);
        validateSkillName(skillName);
        return execute(skillName, "/" + skillName + " " + prompt);
    }

    private AgentResultDto execute(String skillName, String prompt) throws Exception {
        log.info("[USER_QUERY]: {}", prompt);
        boolean isWindows = System.getProperty("os.name").toLowerCase().contains("win");
        List<String> command = List.of(
                isWindows ? "qwen.cmd" : "qwen",
                "--output-format", "stream-json",
                "--approval-mode", "yolo",
                prompt
        );

        Instant startedAt = Instant.now();
        CommandResultDto result = commandExecutor.execute(new CommandRequestDto(
                command,
                workingDirectory,
                timeout
        ));
        Instant finishedAt = Instant.now();

        AgentLogDto agentLog = agentStreamJsonParser.parse(result.getStdout());
        log.info("[AGENT_RESPONSE]: \n{}", agentLog.getEventsJson());
        AgentResultDto agentResult = new AgentResultDto(
                result.getStdout(),
                result.getStderr(),
                result.getExitCode(),
                result.isTimedOut(),
                agentLog.getEvents(),
                agentLog.getEventsJson(),
                agentLog.getFinalResult()
        );

        AgentRunLogDto logEntry = AgentRunLogDto.builder()
                .runId(UUID.randomUUID().toString())
                .startedAt(startedAt.toString())
                .finishedAt(finishedAt.toString())
                .skillName(skillName)
                .finalResult(agentResult.getFinalResult())
                .events(agentResult.getEvents())
                .build();
        runnerLogWriter.write(workingDirectory, logEntry);

        return agentResult;
    }

    public static Path resolveDefaultWorkingDirectory() {
        Path currentDirectory = Path.of("").toAbsolutePath();
        Path skillsDirectory = currentDirectory.resolve("skills");
        if (Files.isDirectory(skillsDirectory)) {
            return skillsDirectory;
        }
        return currentDirectory;
    }

    private static void validateSkillName(String skillName) {
        if (skillName == null || skillName.isBlank()) {
            throw new InvalidSkillNameException("Skill name must not be blank");
        }
        if (skillName.contains("/") || skillName.contains("\\") || skillName.contains("..")) {
            throw new InvalidSkillNameException("Skill name must not contain path fragments: " + skillName);
        }
    }
}
