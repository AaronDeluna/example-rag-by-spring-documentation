package org.mirent.skills.spec;

/**
 * Интерфейс для конвертации {@link SkillSpec} в файл скилла (SKILL.md).
 * <p>
 * Каждая реализация отвечает за генерацию форматированного вывода,
 * совместимого с конкретным CLI-агентом (Qwen Code, Claude Code и т.д.).
 * </p>
 */
public interface SkillRenderer {

    /**
     * Преобразует спецификацию скилла в строку формата SKILL.md.
     *
     * @param spec спецификация скилла
     * @return содержимое SKILL.md
     */
    String render(SkillSpec spec);
}
