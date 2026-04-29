package ru.mirent.tools;

import ru.mirent.services.DecompilationService;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Инструмент: get_method_source
 * Извлечь исходный код конкретного метода
 */
public class GetMethodSourceTool extends AbstractTool {

    private final DecompilationService decompilationService = new DecompilationService();

    @Override
    public String getName() {
        return "get_method_source";
    }

    @Override
    public String getDescription() {
        return "Извлечь исходный код конкретного метода из декомпилированного " +
               "Java-класса. Используйте после get_class_outline, когда известно " +
               "точно имя метода. Возвращает все перегрузки, соответствующие имени метода.";
    }

    @Override
    protected Map<String, Object> createProperties() {
        Map<String, Object> props = new HashMap<>();
        props.put("jar_path", createStringProperty("Абсолютный путь к JAR-файлу"));
        props.put("class_fqn", createStringProperty("Полное имя класса"));
        props.put("method_name", createStringProperty("Имя метода, например 'send'"));
        return props;
    }

    @Override
    protected List<String> getRequiredParameters() {
        return Arrays.asList("jar_path", "class_fqn", "method_name");
    }

    @Override
    public Object execute(Map<String, Object> arguments) {
        String jarPath = (String) arguments.get("jar_path");
        String classFqn = (String) arguments.get("class_fqn");
        String methodName = (String) arguments.get("method_name");

        try {
            return decompilationService.getMethodSource(jarPath, classFqn, methodName);
        } catch (IllegalArgumentException e) {
            return "ОШИБКА ВАЛИДАЦИИ: " + e.getMessage();
        } catch (SecurityException e) {
            return "ОШИБКА БЕЗОПАСНОСТИ: " + e.getMessage();
        } catch (Exception e) {
            return "ОШИБКА: " + e.getMessage();
        }
    }
}
