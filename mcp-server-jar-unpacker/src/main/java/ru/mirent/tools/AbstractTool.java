package ru.mirent.tools;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Базовый класс для инструментов с общей логикой создания схемы
 */
public abstract class AbstractTool implements Tool {

    @Override
    public Map<String, Object> getInputSchema() {
        Map<String, Object> schema = new HashMap<>();
        schema.put("type", "object");
        schema.put("properties", createProperties());
        schema.put("required", getRequiredParameters());
        return schema;
    }

    /**
     * Создать карту свойств схемы
     */
    protected abstract Map<String, Object> createProperties();

    /**
     * Вернуть список обязательных параметров
     */
    protected abstract List<String> getRequiredParameters();

    /**
     * Создать описание строкового параметра
     */
    protected Map<String, Object> createStringProperty(String description) {
        Map<String, Object> prop = new HashMap<>();
        prop.put("type", "string");
        prop.put("description", description);
        return prop;
    }
}
