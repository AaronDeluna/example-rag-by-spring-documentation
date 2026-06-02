package ru.mirent.skills.exeptions;

public class MissingAgentCliException extends AgentRunnerConfigurationException {

    public MissingAgentCliException(String propertyName) {
        super("Не передано название CLI для запуска: " + propertyName);
    }
}
