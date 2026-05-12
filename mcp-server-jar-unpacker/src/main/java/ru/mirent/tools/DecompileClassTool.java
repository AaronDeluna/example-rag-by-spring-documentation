package ru.mirent.tools;

import ru.mirent.services.DecompilationService;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Инструмент: decompile_class
 * Полная декомпиляция класса через CFR
 */
public class DecompileClassTool extends AbstractTool {

    private final DecompilationService decompilationService = new DecompilationService();

    @Override
    public String getName() {
        return "decompile_class";
    }

    @Override
    public String getDescription() {
        return "Вернуть полный декомпилированный исходник Java-класса из JAR " +
               "с помощью CFR. ВНИМАНИЕ: может вернуть сотни строк – используйте " +
               "только когда полный исходник нужен явно.";
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
            return decompilationService.decompileClass(jarPath, classFqn);
        } catch (IllegalArgumentException e) {
            return "ОШИБКА ВАЛИДАЦИИ: " + e.getMessage();
        } catch (SecurityException e) {
            return "ОШИБКА БЕЗОПАСНОСТИ: " + e.getMessage();
        } catch (Exception e) {
            return "ОШИБКА: " + e.getMessage();
        }
    }
}
