package org.mirent.skills.tests.external;

import io.github.ivanmilovanov.agentic.cli.runner.model.AgentResultDto;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mirent.skills.service.AgentRunnerService;
import org.mirent.skills.util.WutPreparer;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mirent.skills.matcher.AgentMatcher.assertSuccessful;

/**
 * Демонстрационный E2E-тест для сценария «агент объясняет концепцию».
 * В отличие от {@link TextToJavaUiTest}, здесь агент ничего не генерирует в файловой
 * системе и не вызывает скилы — он должен дать текстовый ответ на вопрос пользователя.
 * Проверяется, что прогон завершился успешно и агент действительно что-то ответил.
 */
@Tag("external")
@Slf4j
class ExplainConceptTest {

    @Test
    @DisplayName("Агент объясняет концепцию полиморфизма и возвращает непустой текстовый ответ")
    void userPromptAsksAgentToExplainConcept() throws Exception {
        log.info("=== Запуск теста: userPromptAsksAgentToExplainConcept ===");

        // 1. Подготовка рабочей директории (wut)
        Path workspace = WutPreparer.builder()
                .wutSourceName("frap-mcp-testing")
                .wutSourcePath(Path.of("src/test/resources/wut-templates"))
                .build()
                .prepare();
        log.info("Рабочая область (workspace): {}", workspace.toAbsolutePath());

        // 2. Создание раннера агента через фасад SDK
        AgentRunnerService agentRunner = new AgentRunnerService(workspace);

        // 3. Запуск пользовательского промпта с просьбой объяснить концепцию (без генерации кода)
        String prompt = """
            Объясни простыми словами, что такое полиморфизм в объектно-ориентированном программировании.
            Ответь текстом на русском языке, код писать не нужно.""";
        log.info("Отправка промпта агенту (длина {} символов)", prompt.length());
        AgentResultDto result = agentRunner.executeUserPrompt(prompt);
        log.info("Идентификатор сессии: {}", result.getEvents().get(0).get("uuid"));

        // 4. Проверка успешного завершения агента
        assertSuccessful(result);

        // 5. Проверка того, что агент действительно что-то ответил
        String answer = result.getFinalResult();
        log.info("Ответ агента:\n{}", answer);
        assertNotNull(answer, "Финальный ответ агента не должен быть null");
        assertFalse(answer.isBlank(), "Агент должен вернуть непустое объяснение");
        assertTrue(answer.strip().length() > 20,
                "Ответ агента слишком короткий, похоже объяснение отсутствует");
    }
}
