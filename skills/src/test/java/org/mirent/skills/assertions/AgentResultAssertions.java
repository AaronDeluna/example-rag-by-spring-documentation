package org.mirent.skills.assertions;

import com.fasterxml.jackson.databind.JsonNode;
import org.mirent.skills.dto.agent.AgentResultDto;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public final class AgentResultAssertions {

    private AgentResultAssertions() {
    }

    /**
     * Проверяет успешное завершение процесса агента.
     *
     * @param result результат выполнения агента
     */
    public static void assertSuccessful(AgentResultDto result) {
        assertNotNull(result, "Результат агента не должен быть null");
        assertFalse(result.isTimedOut(), "Выполнение агента завершилось по timeout");
        assertEquals(0, result.getExitCode(), "Процесс агента завершился с ненулевым exitCode");
    }

    /**
     * Проверяет, что журнал событий содержит единственный {@code tool_use}
     * для инструмента {@code skill} с указанным именем скила.
     *
     * @param result результат выполнения агента с нормализованным списком событий
     * @param expectedSkillName ожидаемое значение поля {@code input.skill}
     */
    public static void assertSingleSkillCall(AgentResultDto result, String expectedSkillName) {
        List<String> actual = extractSkillCalls(result);

        assertEquals(
                List.of(expectedSkillName),
                actual,
                () -> "Должен быть ровно один вызов скила. Ожидалось="
                        + expectedSkillName
                        + ", фактически="
                        + actual
        );
    }

    /**
     * Проверяет, что журнал событий содержит указанные вызовы {@code skill}
     * в заданной последовательности.
     * Дополнительные вызовы между ожидаемыми элементами допускаются.
     *
     * @param result результат выполнения агента с нормализованным списком событий
     * @param expectedSkillNames ожидаемые значения поля {@code input.skill} в требуемом порядке
     */
    public static void assertSkillCallsInOrder(AgentResultDto result, List<String> expectedSkillNames) {
        List<String> actualSkillNames = extractSkillCalls(result);

        int searchFrom = 0;
        for (String expectedSkillName : expectedSkillNames) {
            int index = indexOf(actualSkillNames, expectedSkillName, searchFrom);
            assertTrue(
                    index >= 0,
                    () -> "Ожидаемая последовательность вызовов скилов не найдена. Ожидалось="
                            + expectedSkillNames
                            + ", фактически="
                            + actualSkillNames
                            + ", отсутствует="
                            + expectedSkillName
            );
            searchFrom = index + 1;
        }
    }

    /**
     * Проверяет, что журнал событий содержит тот же набор вызовов {@code skill},
     * что и ожидаемый список, без учета порядка элементов.
     *
     * @param result результат выполнения агента с нормализованным списком событий
     * @param expectedSkillNames ожидаемые значения поля {@code input.skill}
     */
    public static void assertSkillCallsIgnoringOrder(AgentResultDto result, List<String> expectedSkillNames) {
        List<String> expected = sorted(expectedSkillNames);
        List<String> actual = sorted(extractSkillCalls(result));

        assertEquals(
                expected,
                actual,
                () -> "Вызовы скилов должны совпадать без учета порядка. Ожидалось="
                        + expected
                        + ", фактически="
                        + actual
        );
    }

    private static List<String> extractSkillCalls(AgentResultDto result) {
        assertNotNull(result, "Результат агента не должен быть null");
        assertNotNull(result.getEvents(), "События агента не должны быть null");

        List<String> skillCalls = new ArrayList<>();
        for (JsonNode event : result.getEvents()) {
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

    private static List<String> sorted(List<String> values) {
        return values.stream()
                .sorted(Comparator.naturalOrder())
                .toList();
    }

    private static int indexOf(List<String> values, String expectedValue, int fromIndex) {
        for (int i = fromIndex; i < values.size(); i++) {
            if (expectedValue.equals(values.get(i))) {
                return i;
            }
        }
        return -1;
    }
}
