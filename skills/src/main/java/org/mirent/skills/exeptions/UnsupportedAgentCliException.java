package org.mirent.skills.exeptions;

public class UnsupportedAgentCliException extends AgentRunnerConfigurationException {

    public UnsupportedAgentCliException(String cliName, Throwable cause) {
        super("Неподдерживаемая CLI для запуска: " + cliName, cause);
    }
}
