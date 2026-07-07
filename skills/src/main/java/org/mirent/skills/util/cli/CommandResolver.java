package org.mirent.skills.util.cli;

import org.mirent.skills.exeptions.CommandNotFoundException;

public interface CommandResolver {
    String resolveExecutable(String commandName) throws CommandNotFoundException;
}
