package org.mirent.skills.runner.qwen;

import lombok.extern.slf4j.Slf4j;
import org.mirent.skills.CommandExecutor;
import org.mirent.skills.dto.agent.AgentResultDto;
import org.mirent.skills.dto.command.CommandRequestDto;
import org.mirent.skills.dto.command.CommandResultDto;
import org.mirent.skills.runner.AgentRunner;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;

@Slf4j
public class QwenAgentRunner implements AgentRunner {

    private static final Duration DEFAULT_TIMEOUT = Duration.ofMinutes(3);

    private final CommandExecutor commandExecutor;
    private final Path workingDirectory;
    private final Duration timeout;

    public QwenAgentRunner() {
        this(new CommandExecutor(), resolveDefaultWorkingDirectory(), DEFAULT_TIMEOUT);
    }

    public QwenAgentRunner(CommandExecutor commandExecutor, Path workingDirectory, Duration timeout) {
        this.commandExecutor = commandExecutor;
        this.workingDirectory = workingDirectory;
        this.timeout = timeout;
    }

    @Override
    public AgentResultDto executeUserPrompt(String prompt) throws Exception {
        log.info("[USER_QUERY]: {}", prompt);
        CommandResultDto result = commandExecutor.execute(new CommandRequestDto(
                List.of(
                        "qwen",
                        "--output-format", "stream-json",
                        "--approval-mode", "yolo",
                        prompt
                ),
                workingDirectory,
                timeout
        ));

        log.info("[AGENT_RESPONSE]: {}", result.getStdout());
        return new AgentResultDto(
                result.getStdout(),
                result.getStderr(),
                result.getExitCode(),
                result.isTimedOut()
        );
    }

    @Override
    public AgentResultDto executeSkillPrompt(String skillName, String prompt) throws Exception {
        validateSkillName(skillName);
        return executeUserPrompt("/" + skillName + " " + prompt);
    }

    private static Path resolveDefaultWorkingDirectory() {
        Path currentDirectory = Path.of("").toAbsolutePath();
        Path skillsDirectory = currentDirectory.resolve("skills");
        if (Files.isDirectory(skillsDirectory)) {
            return skillsDirectory;
        }
        return currentDirectory;
    }

    private static void validateSkillName(String skillName) {
        if (skillName == null || skillName.isBlank()) {
            throw new IllegalArgumentException("Skill name must not be blank");
        }
        if (skillName.contains("/") || skillName.contains("\\") || skillName.contains("..")) {
            throw new IllegalArgumentException("Skill name must not contain path fragments: " + skillName);
        }
    }
}
