package ru.mirent.tools;

import ru.mirent.security.PathValidator;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * Инструмент: list_classes_in_jar
 * <p>
 * Вернуть список всех .class файлов в JAR-файле без декомпиляции.
 * Полезно для изучения содержимого JAR перед выбором конкретного класса.
 */
public class ListClassesInJarTool extends AbstractTool {

    @Override
    public String getName() {
        return "list_classes_in_jar";
    }

    @Override
    public String getDescription() {
        return "Вернуть список всех .class файлов в JAR-файле без декомпиляции. " +
               "Полезно для изучения содержимого JAR перед выбором конкретного класса. " +
               "Возвращает список полных имён классов в формате com/example/MyClass.class";
    }

    @Override
    protected Map<String, Object> createProperties() {
        Map<String, Object> props = new HashMap<>();
        props.put("jar_path", createStringProperty(
            "Абсолютный путь к JAR-файлу"
        ));
        props.put("filter", createStringProperty(
            "Опциональный фильтр по имени класса (regex)"
        ));
        return props;
    }

    @Override
    protected List<String> getRequiredParameters() {
        return Arrays.asList("jar_path");
    }

    @Override
    public Object execute(Map<String, Object> arguments) {
        String jarPath = (String) arguments.get("jar_path");
        String filter = (String) arguments.get("filter");

        try {
            // Валидация пути
            Path validatedPath = PathValidator.validateJarPath(jarPath);

            List<String> classes = listClassesInJar(validatedPath, filter);

            if (classes.isEmpty()) {
                return "В JAR не найдено .class файлов: " + jarPath;
            }

            StringBuilder sb = new StringBuilder();
            sb.append(String.format("Найдено классов: %d в %s:\n\n", classes.size(), jarPath));
            for (String cls : classes) {
                sb.append("  ").append(cls).append("\n");
            }

            return sb.toString();
        } catch (SecurityException e) {
            return "ОШИБКА БЕЗОПАСНОСТИ: " + e.getMessage();
        } catch (IOException e) {
            return "ОШИБКА: " + e.getMessage();
        }
    }

    /**
     * Получить список всех .class файлов в JAR
     *
     * @param jarPath путь к JAR-файлу
     * @param filter  опциональный regex-фильтр
     * @return отсортированный список имён классов
     * @throws IOException при ошибке чтения JAR
     */
    List<String> listClassesInJar(Path jarPath, String filter) throws IOException {
        List<String> classes = new ArrayList<>();

        try (JarFile jar = new JarFile(jarPath.toFile())) {
            Enumeration<JarEntry> entries = jar.entries();

            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                String name = entry.getName();

                // Фильтр по .class файлам
                if (name.endsWith(".class")) {
                    // Исключаем модули (module-info.class)
                    if ("module-info.class".equals(name)) {
                        continue;
                    }

                    // Применяем фильтр по имени
                    if (filter != null && !name.matches(filter)) {
                        continue;
                    }

                    classes.add(name);
                }
            }
        }

        // Сортировка
        classes.sort(String::compareTo);

        return classes;
    }
}
