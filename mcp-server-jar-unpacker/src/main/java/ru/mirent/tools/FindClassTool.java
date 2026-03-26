package ru.mirent.tools;

import ru.mirent.services.JarSearchService;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Инструмент: find_class_in_m2
 * Поиск Java-класса внутри JAR-файлов в ~/.m2/repository
 */
public class FindClassTool extends AbstractTool {

    private final JarSearchService jarSearchService = new JarSearchService();

    @Override
    public String getName() {
        return "find_class_in_m2";
    }

    @Override
    public String getDescription() {
        return "Поиск Java-класса внутри JAR-файлов в ~/.m2/repository. " +
               "Принимает простое имя класса (например, 'КафкаТемплате') или " +
               "полное имя (например, 'org.springframework.kafka.core.KafkaTemplate'). " +
               "Возвращает список путей к найденным JAR-файлам.";
    }

    @Override
    protected Map<String, Object> createProperties() {
        Map<String, Object> props = new HashMap<>();
        props.put("class_name", createStringProperty(
            "Простое или полное имя Java-класса"
        ));
        return props;
    }

    @Override
    protected List<String> getRequiredParameters() {
        return Arrays.asList("class_name");
    }

    @Override
    public Object execute(Map<String, Object> arguments) {
        String className = (String) arguments.get("class_name");

        try {
            return jarSearchService.findClass(className);
        } catch (IllegalArgumentException e) {
            return "ОШИБКА ВАЛИДАЦИИ: " + e.getMessage();
        } catch (Exception e) {
            return "ОШИБКА: " + e.getMessage();
        }
    }
}
