package ru.mirent.tools;

import ru.mirent.services.DecompilationService;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Инструмент: get_class_outline
 * Получить краткую схему Java-класса из JAR
 */
public class GetClassOutlineTool extends AbstractTool {

    private final DecompilationService decompilationService = new DecompilationService();

    @Override
    public String getName() {
        return "get_class_outline";
    }

    @Override
    public String getDescription() {
        return "Получить краткую схему Java-класса из JAR: пакет, импорты, " +
               "объявление класса, поля и сигнатуры методов — БЕЗ тел методов. " +
               "Требует примерно в 10 раз меньше токенов, чем полный исходник.";
    }

    @Override
    protected Map<String, Object> createProperties() {
        Map<String, Object> props = new HashMap<>();
        props.put("jar_path", createStringProperty("Абсолютный путь к JAR-файлу"));
        props.put("class_fqn", createStringProperty("Полное имя класса"));
        return props;
    }

    @Override
    protected List<String> getRequiredParameters() {
        return Arrays.asList("jar_path", "class_fqn");
    }

    @Override
    public Object execute(Map<String, Object> arguments) {
        String jarPath = (String) arguments.get("jar_path");
        String classFqn = (String) arguments.get("class_fqn");

        try {
            return decompilationService.getClassOutline(jarPath, classFqn);
        } catch (IllegalArgumentException e) {
            return "ОШИБКА ВАЛИДАЦИИ: " + e.getMessage();
        } catch (SecurityException e) {
            return "ОШИБКА БЕЗОПАСНОСТИ: " + e.getMessage();
        } catch (Exception e) {
            return "ОШИБКА: " + e.getMessage();
        }
    }
}
