package org.mirent.skills.dto.agent;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AgentResultDto {

    private final String stdout;
    private final String stderr;
    private final int exitCode;
    private final boolean timedOut;
}
