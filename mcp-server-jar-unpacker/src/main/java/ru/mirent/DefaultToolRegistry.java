package ru.mirent;

import ru.mirent.logging.ToolLogger;
import ru.mirent.tools.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Реестр инструментов MCP по умолчанию
 */
public class DefaultToolRegistry implements ToolRegistry {

    private final Map<String, Tool> toolsByName = new HashMap<>();

    public DefaultToolRegistry() {
        // Создание инструментов
        List<Tool> tools = Arrays.asList(
            new FindClassTool(),
            new GetClassOutlineTool(),
            new GetMethodSourceTool(),
            new DecompileClassTool(),
            new ListClassesInJarTool(),
            new SearchClassesByPatternTool()
        );

        // Регистрация инструментов
        for (Tool tool : tools) {
            toolsByName.put(tool.getName(), tool);
        }
    }

    @Override
    public List<Map<String, Object>> getTools() {
        List<Map<String, Object>> result = new ArrayList<>();
        for (Tool tool : toolsByName.values()) {
            Map<String, Object> toolMap = new HashMap<>();
            toolMap.put("name", tool.getName());
            toolMap.put("description", tool.getDescription());
            toolMap.put("inputSchema", tool.getInputSchema());
            result.add(toolMap);
        }
        return result;
    }

    @Override
    public Object callTool(String name, Map<String, Object> arguments) {
        Tool tool = toolsByName.get(name);
        if (tool == null) {
            throw new IllegalArgumentException("Unknown tool: " + name);
        }

        ToolLogger.logDebug("Вызов инструмента: " + name);
        ToolLogger.logDebug("Аргументы: " + arguments);

        long startTime = System.currentTimeMillis();
        try {
            Object result = tool.execute(arguments);
            long elapsed = System.currentTimeMillis() - startTime;
            ToolLogger.logToolCall(name, "SUCCESS", elapsed, JsonUtils.toJson(arguments));
            return result;
        } catch (Exception e) {
            long elapsed = System.currentTimeMillis() - startTime;
            ToolLogger.logToolCall(name, "ERROR: " + e.getMessage(), elapsed, JsonUtils.toJson(arguments));
            ToolLogger.logDebug("Ошибка инструмента " + name + ": " + e.getMessage());
            throw e;
        }
    }
}
