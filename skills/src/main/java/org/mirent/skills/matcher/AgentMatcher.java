package org.mirent.skills.matcher;

import org.mirent.skills.dto.agent.AgentResultDto;
import org.mirent.skills.dto.evaluate.EvaluateResultDto;
import org.mirent.skills.util.AgentSkillCallExtractorUtils;

import java.util.List;

public final class AgentMatcher {

    private AgentMatcher() {
    }

    /**
     * Проверяет, что score результата оценки не ниже порога.
     * При провале сообщение содержит фактический score и problemMessage от судьи.
     *
     * @param result    результат оценки
     * @param threshold минимальный допустимый score в [0.0, 1.0]
     */
    public static void evaluate(EvaluateResultDto result, double threshold) {
        if (result == null) {
            throw new AssertionError("Результат оценки не должен быть null");
        }
        if (result.getScore() < threshold) {
            throw new AssertionError(
                    "Score " + result.getScore() + " ниже порога " + threshold
                            + ". Проблемы: " + result.getProblemMessage()
            );
        }
    }

    /**
     * Проверяет успешное завершение процесса агента.
     *
     * @param result результат выполнения агента
     */
    public static void assertSuccessful(AgentResultDto result) {
        if (result == null) {
            throw new AssertionError("Результат агента не должен быть null");
        }
        if (result.isTimedOut()) {
            throw new AssertionError("Выполнение агента завершилось по timeout");
        }
        if (result.getExitCode() != 0) {
            throw new AssertionError("Процесс агента завершился с ненулевым exitCode");
        }
    }

    /**
     * Проверяет, что журнал событий содержит единственный {@code tool_use}
     * для инструмента {@code skill} с указанным именем скила.
     *
     * @param result результат выполнения агента с нормализованным списком событий
     * @param expectedSkillName ожидаемое значение поля {@code input.skill}
     */
    public static void assertSingleSkillCall(AgentResultDto result, String expectedSkillName) {
        List<String> actual = AgentSkillCallExtractorUtils.extractSkillCalls(result);
        List<String> expected = List.of(expectedSkillName);
        if (!expected.equals(actual)) {
            throw new AssertionError(
                    "Ожидался 1 вызов скила: %s, фактически: %s".formatted(expectedSkillName, actual)
            );
        }
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
        List<String> actualSkillNames = AgentSkillCallExtractorUtils.extractSkillCalls(result);

        int searchFrom = 0;
        for (String expectedSkillName : expectedSkillNames) {
            int index = actualSkillNames.subList(searchFrom, actualSkillNames.size()).indexOf(expectedSkillName);
            if (index < 0) {
                throw new AssertionError(
                        "Ожидалась последовательность скилов: %s, фактически: %s, отсутствует: %s".formatted(
                            expectedSkillNames,
                            actualSkillNames,
                            expectedSkillName
                        )
                );
            }
            searchFrom += index + 1;
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
        List<String> expected = expectedSkillNames.stream().sorted().toList();
        List<String> actual = AgentSkillCallExtractorUtils.extractSkillCalls(result).stream().sorted().toList();

        if (!expected.equals(actual)) {
            throw new AssertionError("Ожидались скилы: %s, фактически: %s".formatted(expected, actual));
        }
    }
}
