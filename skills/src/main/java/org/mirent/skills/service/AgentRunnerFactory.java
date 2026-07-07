package org.mirent.skills.service;

import lombok.extern.slf4j.Slf4j;
import org.mirent.skills.CommandExecutor;
import org.mirent.skills.parser.AgentStreamJsonParser;
import org.mirent.skills.runner.RunnerLogWriter;
import org.mirent.skills.runner.qwen.QwenAgentRunner;
import org.mirent.skills.util.cli.CommandFactory;
import org.mirent.skills.util.cli.OsAwareCommandResolver;
import org.mirent.skills.util.cli.OsType;
import org.mirent.skills.util.cli.QwenCommandFactoryImpl;

import java.nio.file.Path;
import java.time.Duration;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

@Slf4j
public class AgentRunnerFactory {

    private final CommandExecutor commandExecutor;
    private final AgentStreamJsonParser agentStreamJsonParser;
    private final Path workingDirectory;
    private final Duration timeout;
    private final RunnerLogWriter runnerLogWriter;

    public static AgentRunnerFactory defaultFactory(Path workspace) {
        return new AgentRunnerFactory(
                new CommandExecutor(),
                new AgentStreamJsonParser(),
                workspace,
                QwenAgentRunner.DEFAULT_TIMEOUT,
                new RunnerLogWriter()
        );
    }

    AgentRunnerFactory(
            CommandExecutor commandExecutor,
            AgentStreamJsonParser agentStreamJsonParser,
            Path workingDirectory,
            Duration timeout,
            RunnerLogWriter runnerLogWriter
    ) {
        this.commandExecutor = commandExecutor;
        this.agentStreamJsonParser = agentStreamJsonParser;
        this.workingDirectory = workingDirectory;
        this.timeout = timeout;
        this.runnerLogWriter = runnerLogWriter;
    }

    public QwenAgentRunner create(Properties properties) {
        AgentCli cli = AgentCli.fromProperty(properties.getProperty(AgentRunnerProperties.CLI_PROPERTY));
        log.info("Запуск через CLI: {}", cli);

        CommandFactory commandFactory = createCommandFactory(cli, properties);

        return switch (cli) {
            case QWEN -> new QwenAgentRunner(
                    commandExecutor,
                    agentStreamJsonParser,
                    runnerLogWriter,
                    workingDirectory,
                    timeout,
                    commandFactory
            );
        };
    }

    static CommandFactory createCommandFactory(AgentCli cli, Properties props) {
        return switch (cli) {
            case QWEN -> {
                // Строим fallback-карту для всех ОС
                Map<OsType, List<Path>> fallbacks = new HashMap<>();
                for (OsType os : OsType.values()) {
                    List<Path> paths = AgentRunnerProperties.getFallbackPaths(props, os);
                    if (!paths.isEmpty()) {
                        fallbacks.put(os, paths);
                    }
                }
                OsAwareCommandResolver resolver = new OsAwareCommandResolver(fallbacks);

                // Базовые аргументы
                List<String> baseArgs = AgentRunnerProperties.getBaseArgs(props);

                // Префикс (только для Windows)
                List<String> prefix = AgentRunnerProperties.getPrefix(props, OsType.detect());

                yield new QwenCommandFactoryImpl(resolver, baseArgs, prefix);
            }
        };
    }
}
