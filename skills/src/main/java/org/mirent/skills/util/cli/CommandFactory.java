package org.mirent.skills.util.cli;

import java.nio.file.Path;
import java.util.List;

public interface CommandFactory {
    List<String> buildCommand(String prompt, Path logDir);
}
