package org.mirent.skills.dto.evaluate;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class EvaluateDto {

    private final String query;
    private final String agentTrace;
}
