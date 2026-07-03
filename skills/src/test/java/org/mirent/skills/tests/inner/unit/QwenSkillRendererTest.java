package org.mirent.skills.tests.inner.unit;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mirent.skills.spec.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class QwenSkillRendererTest {
    private static final Logger LOG = LoggerFactory.getLogger(QwenSkillRendererTest.class);

    private final SkillRenderer renderer = SkillRendererFactory.defaultRenderer();

    @Test
    @DisplayName("Рендерит минимальный SkillSpec: name и description")
    void givenMinimalSpecWhenRenderThenProducesValidFrontmatter() {
        SkillSpec spec = new SkillSpec();
        spec.setName("word-count");
        spec.setDescription("Считает количество слов в тексте.");

        String result = renderer.render(spec);

        assertTrue(result.startsWith("---\n"), "Должен начинаться с YAML-разделителя");
        assertTrue(result.contains("name: word-count"), "Должен содержать name");
        assertTrue(result.contains("description: Считает количество слов в тексте."), "Должен содержать description");
        assertTrue(result.contains("\n---\n"), "Должен содержать закрывающий YAML-разделитель");
    }

    @Test
    @DisplayName("Рендерит все опциональные поля Qwen (priority, paths, user-invocable, disable-model-invocation)")
    void givenSpecWithQwenFieldsWhenRenderThenIncludesAllFields() {
        SkillSpec spec = new SkillSpec();
        spec.setName("tsx-helper");
        spec.setDescription("React TSX component helper.");
        spec.setPriority(10);
        spec.setPaths(List.of("src/**/*.tsx"));
        spec.setUserInvocable(false);
        spec.setDisableModelInvocation(true);

        String result = renderer.render(spec);

        assertTrue(result.contains("priority: 10"));
        assertTrue(result.contains("paths:"));
        assertTrue(result.contains("- src/**/*.tsx"));
        assertTrue(result.contains("user-invocable: false"));
        assertTrue(result.contains("disable-model-invocation: true"));
    }

    @Test
    @DisplayName("Рендерит шаги (steps) как Markdown-секции")
    void givenSpecWithStepsWhenRenderThenIncludesStepsSection() {
        SkillSpec spec = new SkillSpec();
        spec.setName("text-processor");
        spec.setDescription("Обрабатывает текст.");

        Step step = new Step();
        step.setId("split");
        step.setType("text_processing");
        step.setOperation("split");
        step.setInput("{{text}}");
        step.setOutput("words");
        step.setDescription("Разделить текст на слова");
        spec.setSteps(List.of(step));

        String result = renderer.render(spec);

        assertTrue(result.contains("## Шаги выполнения"), "Должен содержать заголовок шагов");
        assertTrue(result.contains("split"), "Должен содержать ID шага");
        assertTrue(result.contains("Разделить текст на слова"), "Должен содержать описание шага");
        assertTrue(result.contains("{{text}}"), "Должен содержать шаблон ввода");
    }

    @Test
    @DisplayName("Рендерит примеры (examples) из ресурсов")
    void givenSpecWithExamplesWhenRenderThenIncludesExamples() {
        SkillSpec spec = new SkillSpec();
        spec.setName("word-count");
        spec.setDescription("Считает слова.");

        Example example = new Example("привет мир", "2");
        spec.setResources(new Resources(List.of(example), null, null, null));

        String result = renderer.render(spec);

        assertTrue(result.contains("## Примеры"), "Должен содержать заголовок примеров");
        assertTrue(result.contains("привет мир"), "Должен содержать пример ввода");
        assertTrue(result.contains("2"), "Должен содержать пример вывода");
    }

    @Test
    @DisplayName("Рендерит ограничения (constraints)")
    void givenSpecWithConstraintsWhenRenderThenIncludesConstraints() {
        SkillSpec spec = new SkillSpec();
        spec.setName("safe-processor");
        spec.setDescription("Безопасная обработка.");
        spec.setConstraints(new Constraints(1000, List.of("split", "count"), 30));

        String result = renderer.render(spec);

        assertTrue(result.contains("## Ограничения"), "Должен содержать заголовок ограничений");
        assertTrue(result.contains("1000"), "Должен содержать maxInputLength");
        assertTrue(result.contains("30"), "Должен содержать timeoutSeconds");
    }

    @Test
    @DisplayName("Рендерит шаблон ответа (responseTemplate)")
    void givenSpecWithResponseTemplateWhenRenderThenIncludesTemplate() {
        SkillSpec spec = new SkillSpec();
        spec.setName("formatter");
        spec.setDescription("Форматирует ответ.");
        spec.setResponseTemplate(new ResponseTemplate("text", "Слов: {{count}}"));

        String result = renderer.render(spec);

        assertTrue(result.contains("## Формат ответа"), "Должен содержать заголовок формата ответа");
        assertTrue(result.contains("Слов: {{count}}"), "Должен содержать шаблон ответа");
    }

    @Test
    @DisplayName("Генерирует полный SKILL.md, сравнимый с эталонным word-count")
    void givenWordCountSpecWhenRenderThenProducesValidSkill() {
        SkillSpec spec = new SkillSpec();
        spec.setName("word-count");
        spec.setDescription("Считает количество слов в тексте пользователя и возвращает только число.");

        spec.setSteps(List.of(
                createStep("tokenize", "text_processing", "tokenize",
                        "{{text}}", "words", "Разделить текст на слова"),
                createStep("count", "calculation", "length",
                        "{{words}}", "wordCount", "Посчитать количество слов")
        ));

        spec.setResources(new Resources(
                List.of(
                        new Example("сколько слов: привет мир", "Слов: 2"),
                        new Example("посчитай слова в фразе \"быстрая бурая лиса прыгает через ленивого пса\"",
                                "Слов: 7")
                ),
                null, null, null
        ));

        spec.setConstraints(new Constraints(null, null, null));
        spec.setResponseTemplate(new ResponseTemplate("text", "Слов: {{wordCount}}"));

        String result = renderer.render(spec);
        LOG.info("Результат генерации:\n{}", result);

        assertAll("Проверка структуры полного SKILL.md",
                () -> assertTrue(result.startsWith("---\n")),
                () -> assertTrue(result.contains("name: word-count")),
                () -> assertTrue(result.contains("## Шаги выполнения")),
                () -> assertTrue(result.contains("## Примеры")),
                () -> assertTrue(result.contains("## Формат ответа")),
                () -> assertTrue(result.contains("Слов: {{wordCount}}"))
        );
    }

    @Test
    @DisplayName("SkillRendererFactory возвращает QwenSkillRenderer по умолчанию")
    void givenDefaultFactoryWhenCreateThenReturnsQwenRenderer() {
        SkillRenderer defaultRenderer = SkillRendererFactory.defaultRenderer();
        assertNotNull(defaultRenderer, "Фабрика должна вернуть рендерер");
        assertTrue(defaultRenderer instanceof QwenSkillRenderer,
                "Рендерер по умолчанию должен быть QwenSkillRenderer");
    }

    @Test
    @DisplayName("SkillRendererFactory возвращает рендерер по имени агента")
    void givenAgentNameWhenCreateThenReturnsCorrectRenderer() {
        SkillRenderer qwenRenderer = SkillRendererFactory.forAgent("qwen");
        assertNotNull(qwenRenderer);
        assertTrue(qwenRenderer instanceof QwenSkillRenderer);

        SkillRenderer unknownRenderer = SkillRendererFactory.forAgent("unknown");
        assertNotNull(unknownRenderer, "Для неизвестного агента должен возвращаться рендерер по умолчанию");
    }

    private static Step createStep(String id, String type, String operation,
                                    String input, String output, String description) {
        return new Step(id, type, operation, input, output, description);
    }
}
