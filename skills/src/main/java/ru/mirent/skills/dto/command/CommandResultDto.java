package ru.mirent.skills.dto.command;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CommandResultDto {

    private final String stdout;
    private final String stderr;
    private final int exitCode;
    private final boolean timedOut;
}
