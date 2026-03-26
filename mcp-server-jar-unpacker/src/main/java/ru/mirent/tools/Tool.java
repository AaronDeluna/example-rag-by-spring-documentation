package ru.mirent.tools;

import java.util.Map;

/**
 * Интерфейс инструмента MCP
 */
public interface Tool {

    /**
     * Вернуть имя инструмента
     */
    String getName();

    /**
     * Вернуть описание инструмента
     */
    String getDescription();

    /**
     * Вернуть схему входных параметров (JSON Schema)
     */
    Map<String, Object> getInputSchema();

    /**
     * Выполнить инструмент
     * @param arguments аргументы вызова
     * @return результат выполнения
     */
    Object execute(Map<String, Object> arguments);
}
