package org.mirent.skills.util;

import com.fasterxml.jackson.databind.JsonNode;
import io.github.ivanmilovanov.agentic.cli.runner.model.AgentResultDto;

import java.util.ArrayList;
import java.util.List;

public class AgentSkillCallExtractorUtils {

    public static List<String> extractSkillCalls(AgentResultDto result) {
        requireNotNull(result, "Результат агента не должен быть null");
        requireNotNull(result.getEvents(), "События агента не должны быть null");

        List<String> skillCalls = new ArrayList<>();
        for (JsonNode event : result.getEvents()) {
            // Плоская структура: сам event — это tool_use (формат qwen CLI)
            if (isSkillToolUse(event)) {
                skillCalls.add(event.path("input").path("skill").asText());
                continue;
            }

            // Вложенная структура: event.message.content[].tool_use
            JsonNode contentItems = event.path("message").path("content");
            if (!contentItems.isArray()) {
                continue;
            }
            for (JsonNode contentItem : contentItems) {
                if (isSkillToolUse(contentItem)) {
                    skillCalls.add(contentItem.path("input").path("skill").asText());
                }
            }
        }
        return skillCalls;
    }

    private static boolean isSkillToolUse(JsonNode contentItem) {
        return "tool_use".equals(contentItem.path("type").asText())
                && "skill".equals(contentItem.path("name").asText())
                && contentItem.path("input").hasNonNull("skill");
    }

    private static void requireNotNull(Object value, String message) {
        if (value == null) {
            throw new AssertionError(message);
        }
    }
}
