package org.mirent.skills.dto.command;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.nio.file.Path;
import java.time.Duration;
import java.util.List;

@Getter
@AllArgsConstructor
public class CommandRequestDto {

    private final List<String> command;
    private final Path workingDirectory;
    private final Duration timeout;
}
