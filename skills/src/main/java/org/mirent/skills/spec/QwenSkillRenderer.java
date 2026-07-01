package org.mirent.skills.spec;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Рендерер для генерации SKILL.md, совместимого с Qwen Code CLI.
 * <p>
 * Преобразует {@link SkillSpec} в Markdown-файл с YAML-шапкой и инструкциями.
 * Не использует внешние библиотеки шаблонизации.
 * </p>
 */
public class QwenSkillRenderer implements SkillRenderer {

    @Override
    public String render(SkillSpec spec) {
        StringBuilder sb = new StringBuilder();
        renderFrontmatter(sb, spec);
        renderBody(sb, spec);
        return sb.toString();
    }

    private void renderFrontmatter(StringBuilder sb, SkillSpec spec) {
        sb.append("---\n");
        sb.append("name: ").append(spec.getName()).append("\n");
        sb.append("description: ").append(spec.getDescription()).append("\n");

        if (spec.getPriority() != null) {
            sb.append("priority: ").append(spec.getPriority()).append("\n");
        }
        if (spec.getPaths() != null && !spec.getPaths().isEmpty()) {
            sb.append("paths:\n");
            for (String path : spec.getPaths()) {
                sb.append("  - ").append(path).append("\n");
            }
        }
        if (spec.getUserInvocable() != null) {
            sb.append("user-invocable: ").append(spec.getUserInvocable()).append("\n");
        }
        if (spec.getDisableModelInvocation() != null) {
            sb.append("disable-model-invocation: ").append(spec.getDisableModelInvocation()).append("\n");
        }
        if (spec.getLicense() != null) {
            sb.append("license: ").append(spec.getLicense()).append("\n");
        }
        if (spec.getCompatibility() != null) {
            sb.append("compatibility: ").append(spec.getCompatibility()).append("\n");
        }
        if (spec.getAllowedTools() != null) {
            sb.append("allowed-tools: ").append(spec.getAllowedTools()).append("\n");
        }
        if (spec.getMetadata() != null && !spec.getMetadata().isEmpty()) {
            sb.append("metadata:\n");
            for (var entry : spec.getMetadata().entrySet()) {
                sb.append("  ").append(entry.getKey()).append(": \"")
                        .append(entry.getValue()).append("\"\n");
            }
        }
        sb.append("---\n");
    }

    private void renderBody(StringBuilder sb, SkillSpec spec) {
        renderTitle(sb, spec);
        renderSteps(sb, spec.getSteps());
        renderExamples(sb, spec.getResources());
        renderResponseTemplate(sb, spec.getResponseTemplate());
        renderConstraints(sb, spec.getConstraints());
    }

    private void renderTitle(StringBuilder sb, SkillSpec spec) {
        String title = spec.getDescription();
        if (title.length() > 60) {
            title = title.substring(0, 57) + "...";
        }
        sb.append("\n# ").append(title).append("\n\n");
    }

    private void renderSteps(StringBuilder sb, List<Step> steps) {
        if (steps == null || steps.isEmpty()) {
            return;
        }
        sb.append("## Шаги выполнения\n\n");
        for (int i = 0; i < steps.size(); i++) {
            Step step = steps.get(i);
            sb.append("### Шаг ").append(i + 1).append(": ").append(step.getDescription()).append("\n\n");
            if (step.getType() != null || step.getOperation() != null) {
                sb.append("- **Тип:** ").append(step.getType())
                        .append(" / **Операция:** ").append(step.getOperation()).append("\n");
            }
            if (step.getInput() != null) {
                sb.append("- **Вход:** `").append(step.getInput()).append("`\n");
            }
            if (step.getOutput() != null) {
                sb.append("- **Выход:** `").append(step.getOutput()).append("`\n");
            }
            sb.append("\n");
        }
    }

    private void renderExamples(StringBuilder sb, Resources resources) {
        if (resources == null || resources.getExamples() == null || resources.getExamples().isEmpty()) {
            return;
        }
        sb.append("## Примеры\n\n");
        for (int i = 0; i < resources.getExamples().size(); i++) {
            Example example = resources.getExamples().get(i);
            sb.append("### Пример ").append(i + 1).append("\n\n");
            sb.append("**Пользователь:**\n").append(example.getInput()).append("\n\n");
            sb.append("**Ответ:**\n").append(example.getOutput()).append("\n\n");
        }
    }

    private void renderResponseTemplate(StringBuilder sb, ResponseTemplate template) {
        if (template == null || template.getContent() == null) {
            return;
        }
        sb.append("## Формат ответа\n\n");
        if (template.getFormat() != null) {
            sb.append("Формат: `").append(template.getFormat()).append("`\n\n");
        }
        sb.append("```").append(template.getFormat() != null ? template.getFormat() : "text").append("\n");
        sb.append(template.getContent()).append("\n");
        sb.append("```\n\n");
    }

    private void renderConstraints(StringBuilder sb, Constraints constraints) {
        if (constraints == null) {
            return;
        }
        boolean hasContent = constraints.getMaxInputLength() != null
                || constraints.getTimeoutSeconds() != null
                || (constraints.getAllowedOperations() != null && !constraints.getAllowedOperations().isEmpty());
        if (!hasContent) {
            return;
        }
        sb.append("## Ограничения\n\n");
        if (constraints.getMaxInputLength() != null) {
            sb.append("- Максимальная длина ввода: ").append(constraints.getMaxInputLength()).append(" символов\n");
        }
        if (constraints.getTimeoutSeconds() != null) {
            sb.append("- Таймаут выполнения: ").append(constraints.getTimeoutSeconds()).append(" секунд\n");
        }
        if (constraints.getAllowedOperations() != null && !constraints.getAllowedOperations().isEmpty()) {
            sb.append("- Разрешённые операции: ")
                    .append(String.join(", ", constraints.getAllowedOperations())).append("\n");
        }
        sb.append("\n");
    }
}
