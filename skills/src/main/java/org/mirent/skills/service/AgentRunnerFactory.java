package org.mirent.skills.service;

import lombok.extern.slf4j.Slf4j;
import org.mirent.skills.CommandExecutor;
import org.mirent.skills.parser.AgentStreamJsonParser;
import org.mirent.skills.runner.AgentRunner;
import org.mirent.skills.runner.qwen.QwenAgentRunner;

import java.nio.file.Path;
import java.time.Duration;
import java.util.Properties;

@Slf4j
class AgentRunnerFactory {

    private final CommandExecutor commandExecutor;
    private final AgentStreamJsonParser agentStreamJsonParser;
    private final Path workingDirectory;
    private final Duration timeout;
    private final String agentSetName;

    static AgentRunnerFactory defaultFactory(String agentSetName, String caseName) {
        String logicalSet = (caseName == null) ? agentSetName : agentSetName + "/" + caseName;
        return new AgentRunnerFactory(
                new CommandExecutor(),
                new AgentStreamJsonParser(),
                new AgentWorkspacePreparer(agentSetName, caseName).prepare(),
                QwenAgentRunner.DEFAULT_TIMEOUT,
                logicalSet
        );
    }

    AgentRunnerFactory(
            CommandExecutor commandExecutor,
            AgentStreamJsonParser agentStreamJsonParser,
            Path workingDirectory,
            Duration timeout,
            String agentSetName
    ) {
        this.commandExecutor = commandExecutor;
        this.agentStreamJsonParser = agentStreamJsonParser;
        this.workingDirectory = workingDirectory;
        this.timeout = timeout;
        this.agentSetName = agentSetName;
    }

    AgentRunner create(Properties properties) {
        AgentCli cli = AgentCli.fromProperty(properties.getProperty(AgentRunnerProperties.CLI_PROPERTY));
        log.info("Запуск через CLI: {}", cli);

        return switch (cli) {
            case QWEN -> new QwenAgentRunner(commandExecutor, agentStreamJsonParser, workingDirectory, timeout, agentSetName);
        };
    }
}
