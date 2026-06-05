package org.mirent.skills.exeptions;

public class AgentRunnerConfigurationException extends RuntimeException {

    public AgentRunnerConfigurationException(String message) {
        super(message);
    }

    public AgentRunnerConfigurationException(String message, Throwable cause) {
        super(message, cause);
    }
}
