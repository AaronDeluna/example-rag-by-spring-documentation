package org.mirent.skills.service;

import org.mirent.skills.exeptions.MissingAgentCliException;
import org.mirent.skills.exeptions.UnsupportedAgentCliException;

import java.util.Locale;

public enum AgentCli {

    QWEN;

    public static AgentCli fromProperty(String value) {
        if (value == null || value.isBlank()) {
            throw new MissingAgentCliException(AgentRunnerProperties.CLI_PROPERTY);
        }

        String normalizedValue = value.trim().toUpperCase(Locale.ROOT);
        try {
            return AgentCli.valueOf(normalizedValue);
        } catch (IllegalArgumentException e) {
            throw new UnsupportedAgentCliException(value, e);
        }
    }
}
