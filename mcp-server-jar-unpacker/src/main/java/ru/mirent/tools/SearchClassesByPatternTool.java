package ru.mirent.tools;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Инструмент: search_classes_by_pattern
 * Поиск классов по regex-паттерну во всех JAR Maven-репозитория
 */
public class SearchClassesByPatternTool extends AbstractTool {

    private static final int MAX_WORKERS = Math.min(16, Runtime.getRuntime().availableProcessors());
    private static final int MAX_RESULTS = 100;

    public SearchClassesByPatternTool() {
    }

    @Override
    public String getName() {
        return "search_classes_by_pattern";
    }

    @Override
    public String getDescription() {
        return "Поиск Java-классов по regex-паттерну во всех JAR-файлах ~/.m2/repository. " +
               "Полезно для поиска классов по шаблону: все *Controller, *Template, *Service и т.д. " +
               "Возвращает до " + MAX_RESULTS + " результатов в формате: JAR-путь → список классов.";
    }

    @Override
    protected Map<String, Object> createProperties() {
        Map<String, Object> props = new HashMap<>();
        props.put("pattern", createStringProperty(
            "Regex-паттерн для поиска (например, '.*Template.*', '.*Controller$')"
        ));
        props.put("limit", createStringProperty(
            "Максимальное количество результатов (по умолчанию " + MAX_RESULTS + ")"
        ));
        return props;
    }

    @Override
    protected List<String> getRequiredParameters() {
        return Arrays.asList("pattern");
    }

    @Override
    public Object execute(Map<String, Object> arguments) {
        String pattern = (String) arguments.get("pattern");
        String limitStr = (String) arguments.get("limit");

        int limit = MAX_RESULTS;
        if (limitStr != null) {
            try {
                limit = Integer.parseInt(limitStr);
            } catch (NumberFormatException e) {
                // Используем значение по умолчанию
            }
        }

        try {
            Pattern.compile(pattern);

            Map<String, List<String>> results = searchClassesByPattern(pattern, limit);

            if (results.isEmpty()) {
                return "Не найдено классов по паттерну: " + pattern;
            }

            StringBuilder sb = new StringBuilder();
            sb.append(String.format("Найдено классов по паттерну '%s':\n\n", pattern));

            int totalClasses = 0;
            for (Map.Entry<String, List<String>> entry : results.entrySet()) {
                sb.append("JAR: ").append(entry.getKey()).append("\n");
                for (String cls : entry.getValue()) {
                    sb.append("  ").append(cls).append("\n");
                    totalClasses++;
                }
                sb.append("\n");
            }

            sb.append(String.format("Всего: %d классов в %d JAR",
                totalClasses, results.size()));

            return sb.toString();
        } catch (IllegalArgumentException e) {
            return "ОШИБКА: Некорректный regex-паттерн: " + e.getMessage();
        } catch (Exception e) {
            return "ОШИБКА: " + e.getMessage();
        }
    }

    private Map<String, List<String>> searchClassesByPattern(String pattern, int limit)
            throws IOException, InterruptedException {

        Map<String, List<String>> results = new ConcurrentHashMap<>();
        List<Path> jars = getJarsFromM2();

        Pattern patternObj = Pattern.compile(pattern);

        ExecutorService executor = Executors.newFixedThreadPool(MAX_WORKERS);

        try {
            List<Future<Void>> futures = new ArrayList<>();

            for (Path jar : jars) {
                futures.add(executor.submit(() -> {
                    searchInJar(jar, patternObj, results, limit);
                    return null;
                }));
            }

            for (Future<Void> future : futures) {
                future.get();
            }

        } catch (ExecutionException e) {
            // Игнорируем ошибки отдельных JAR
        } finally {
            executor.shutdown();
            if (!executor.awaitTermination(1, TimeUnit.MINUTES)) {
                executor.shutdownNow();
            }
        }

        if (results.size() > limit) {
            return results.entrySet().stream()
                .limit(limit)
                .collect(Collectors.toMap(
                    Map.Entry::getKey,
                    Map.Entry::getValue,
                    (v1, v2) -> v1,
                    LinkedHashMap::new
                ));
        }

        return results;
    }

    private void searchInJar(Path jarPath, Pattern pattern,
                              Map<String, List<String>> results, int limit) {
        try (JarFile jar = new JarFile(jarPath.toFile())) {
            Enumeration<JarEntry> entries = jar.entries();
            List<String> matches = new ArrayList<>();

            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                String name = entry.getName();

                if (name.endsWith(".class") && !name.equals("module-info.class")) {
                    String fqn = name.replace('/', '.').replace(".class", "");

                    if (pattern.matcher(fqn).matches() || pattern.matcher(name).matches()) {
                        matches.add(name);

                        if (matches.size() >= limit) {
                            break;
                        }
                    }
                }
            }

            if (!matches.isEmpty()) {
                matches.sort(String::compareTo);
                results.put(jarPath.toString(), matches);
            }

        } catch (IOException e) {
            // Игнорируем ошибки чтения JAR
        }
    }

    private List<Path> getJarsFromM2() throws IOException {
        String m2Repo = System.getProperty("user.home") + "/.m2/repository";
        Path m2Path = Path.of(m2Repo);

        if (!java.nio.file.Files.exists(m2Path)) {
            return new ArrayList<>();
        }

        List<Path> jars = new ArrayList<>();
        java.nio.file.Files.walk(m2Path)
            .filter(p -> p.toString().endsWith(".jar"))
            .filter(p -> !p.toString().endsWith("-sources.jar"))
            .filter(p -> !p.toString().endsWith("-javadoc.jar"))
            .forEach(jars::add);

        return jars;
    }
}
