package org.mirent.skills.spec;

/**
 * Фабрика для создания {@link SkillRenderer} по имени CLI-агента.
 * <p>
 * Позволяет выбрать подходящий рендерер в зависимости от целевого агента
 * (Qwen Code, Claude Code и т.д.).
 * </p>
 */
public class SkillRendererFactory {

    private static final QwenSkillRenderer QWEN_RENDERER = new QwenSkillRenderer();

    private SkillRendererFactory() {
    }

    /**
     * Возвращает рендерер по умолчанию ({@link QwenSkillRenderer}).
     *
     * @return рендерер для Qwen Code CLI
     */
    public static SkillRenderer defaultRenderer() {
        return QWEN_RENDERER;
    }

    /**
     * Возвращает рендерер для указанного агента.
     * <p>
     * Поддерживаемые имена: {@code "qwen"}.
     * Для неизвестных имён возвращается рендерер по умолчанию.
     * </p>
     *
     * @param agentName имя CLI-агента (например, "qwen")
     * @return подходящий рендерер
     */
    public static SkillRenderer forAgent(String agentName) {
        if ("qwen".equalsIgnoreCase(agentName)) {
            return QWEN_RENDERER;
        }
        return QWEN_RENDERER;
    }
}
