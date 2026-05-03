package org.mirent.skills.parser;

import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import org.mirent.skills.dto.agent.AgentLogDto;

import java.util.ArrayList;
import java.util.List;

public class AgentStreamJsonParser {

    private final ObjectMapper objectMapper;
    private final ObjectReader eventReader;

    public AgentStreamJsonParser() {
        this(new ObjectMapper());
    }

    public AgentStreamJsonParser(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.eventReader = objectMapper.readerFor(JsonNode.class);
    }

    public AgentLogDto parse(String streamJson) throws Exception {
        if (streamJson == null || streamJson.isBlank()) {
            return new AgentLogDto(List.of(), "[]", null);
        }

        List<JsonNode> events = new ArrayList<>();
        try (MappingIterator<JsonNode> iterator = eventReader.readValues(streamJson)) {
            while (iterator.hasNext()) {
                events.add(iterator.next());
            }
        }

        String eventsJson = objectMapper
                .writerWithDefaultPrettyPrinter()
                .writeValueAsString(events);

        return new AgentLogDto(events, eventsJson, extractFinalResult(events));
    }

    private static String extractFinalResult(List<JsonNode> events) {
        for (int i = events.size() - 1; i >= 0; i--) {
            JsonNode event = events.get(i);
            if ("result".equals(event.path("type").asText()) && event.hasNonNull("result")) {
                return event.path("result").asText();
            }
        }
        return null;
    }
}
