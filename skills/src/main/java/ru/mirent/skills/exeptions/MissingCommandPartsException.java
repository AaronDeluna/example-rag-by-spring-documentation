package ru.mirent.skills.exeptions;

public class MissingCommandPartsException extends AgentRunnerConfigurationException {

    public MissingCommandPartsException() {
        super("Команда не может быть пустой");
    }
}
