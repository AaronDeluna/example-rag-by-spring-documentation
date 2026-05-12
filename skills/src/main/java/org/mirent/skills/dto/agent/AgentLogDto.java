package org.mirent.skills.dto.agent;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class AgentLogDto {

    private final List<JsonNode> events;
    private final String eventsJson;
    private final String finalResult;

}
