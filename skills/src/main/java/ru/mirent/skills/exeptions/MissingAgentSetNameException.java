package ru.mirent.skills.exeptions;

public class MissingAgentSetNameException extends AgentRunnerConfigurationException {

    public MissingAgentSetNameException() {
        super("Не передано имя набора агента agentSetName");
    }
}
