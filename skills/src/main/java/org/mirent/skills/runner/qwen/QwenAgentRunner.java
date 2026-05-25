package org.mirent.skills.runner.qwen;

import lombok.extern.slf4j.Slf4j;
import org.mirent.skills.CommandExecutor;
import org.mirent.skills.dto.agent.AgentLogDto;
import org.mirent.skills.dto.agent.AgentResultDto;
import org.mirent.skills.dto.command.CommandRequestDto;
import org.mirent.skills.dto.command.CommandResultDto;
import org.mirent.skills.exeptions.InvalidSkillNameException;
import org.mirent.skills.parser.AgentStreamJsonParser;
import org.mirent.skills.runner.AgentRunner;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;

@Slf4j
public class QwenAgentRunner implements AgentRunner {

    public static final Duration DEFAULT_TIMEOUT = Duration.ofMinutes(3);

    private final CommandExecutor commandExecutor;
    private final AgentStreamJsonParser agentStreamJsonParser;
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
        this.commandExecutor = commandExecutor;
        this.agentStreamJsonParser = agentStreamJsonParser;
        this.workingDirectory = workingDirectory;
        this.timeout = timeout;
    }

    @Override
    public AgentResultDto executeUserPrompt(String prompt) throws Exception {
        log.info("[USER_QUERY]: {}", prompt);
        List<String> command;
        String osName = System.getProperty("os.name").toLowerCase();
        String userHome = System.getProperty("user.home");

        if (osName.contains("win")) {
            command = List.of(
                    // Для Windows требуется alias для запуска приложения
                    // C:\Users\<login>\.qwen\bin\qwen
                    Path.of(userHome, ".qwen", "bin", "qwen").toString(),
                    "--output-format", "stream-json",
                    "--approval-mode", "yolo",
                    prompt
            );
        } else {
            command = List.of(
                    // Для Linux/MacOS требуется путь к исполняемому js файлу.
                    // Файл запустится т.к. в нем имеется шебанг.
                    // /home/<login>/.npm-global/lib/node_modules/@qwen-code/qwen-code/cli.js
                    Path.of(userHome, ".npm-global", "lib", "node_modules", "@qwen-code", "qwen-code", "cli.js").toString(),
                    "--output-format", "stream-json",
                    "--approval-mode", "yolo",
                    prompt
            );
        }

        CommandResultDto result = commandExecutor.execute(new CommandRequestDto(
                command,
                workingDirectory,
                timeout
        ));

        AgentLogDto agentLog = agentStreamJsonParser.parse(result.getStdout());
        log.info("[AGENT_RESPONSE]: \n{}", agentLog.getEventsJson());
        return new AgentResultDto(
                result.getStdout(),
                result.getStderr(),
                result.getExitCode(),
                result.isTimedOut(),
                agentLog.getEvents(),
                agentLog.getEventsJson(),
                agentLog.getFinalResult()
        );
    }

    @Override
    public AgentResultDto executeSkillPrompt(String skillName, String prompt) throws Exception {
        log.info("[SKILL_EXECUTION]: {}", skillName);
        validateSkillName(skillName);
        return executeUserPrompt("/" + skillName + " " + prompt);
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
