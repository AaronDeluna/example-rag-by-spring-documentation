package ru.mirent;

import java.util.List;
import java.util.Map;

/**
 * Реестр инструментов MCP
 */
public interface ToolRegistry {

    /**
     * Вернуть список доступных инструментов
     */
    List<Map<String, Object>> getTools();

    /**
     * Вызвать инструмент по имени
     * @param name имя инструмента
     * @param arguments аргументы вызова
     * @return результат выполнения
     */
    Object callTool(String name, Map<String, Object> arguments);
}
