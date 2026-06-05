package ru.mirent.services;

import ru.mirent.security.ClassNameValidator;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * Сервис поиска классов в JAR-файлах
 */
public class JarSearchService {

    private final JarCacheService jarCacheService;
    private static final int MAX_WORKERS = Math.min(16, Runtime.getRuntime().availableProcessors());

    public JarSearchService() {
        this.jarCacheService = new JarCacheService();
    }

    /**
     * Найти JAR-файлы, содержащие указанный класс
     * @param className простое или полное имя класса
     * @return отсортированный список путей к JAR
     */
    public String findClass(String className) {
        // Валидация имени класса
        ClassNameValidator.validateFQN(className);

        String simple = className.contains(".") ?
            className.substring(className.lastIndexOf('.') + 1) : className;
        String classFilename = simple + ".class";

        List<String> matches = new CopyOnWriteArrayList<>();
        List<Path> jars = jarCacheService.getJars();

        ExecutorService executor = Executors.newFixedThreadPool(MAX_WORKERS);

        try {
            List<Future<Boolean>> futures = new ArrayList<>();
            for (Path jar : jars) {
                futures.add(executor.submit(() -> jarContainsClass(jar, classFilename)));
            }

            for (int i = 0; i < futures.size(); i++) {
                try {
                    if (futures.get(i).get()) {
                        matches.add(jars.get(i).toString());
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } catch (ExecutionException e) {
                    // Игнорируем ошибки отдельных JAR
                }
            }
        } finally {
            executor.shutdown();
            try {
                if (!executor.awaitTermination(1, TimeUnit.MINUTES)) {
                    executor.shutdownNow();
                }
            } catch (InterruptedException e) {
                executor.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }

        // Кэш теперь инвалидируется только по TTL (автоматически)

        if (matches.isEmpty()) {
            return String.format(
                "%s не найден в JAR-файлах в %s.\nОтвет: для внутренних классов ищите имя внешнего класса.",
                classFilename, jarCacheService.getM2RepoPath()
            );
        }

        matches.sort(String::compareTo);
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Найдено JAR-файлов: %d для %s:\n\n", matches.size(), classFilename));
        for (String m : matches) {
            sb.append(" ").append(m).append("\n");
        }
        sb.append("\nСледующий шаг: вызовите get_class_outline с наиболее подходящим путём к JAR.");

        return sb.toString();
    }

    boolean jarContainsClass(Path jarPath, String className) {
        try (JarFile jar = new JarFile(jarPath.toFile())) {
            Enumeration<JarEntry> entries = jar.entries();
            while (entries.hasMoreElements()) {
                String name = entries.nextElement().getName();
                if (name.endsWith(className)) {
                    return true;
                }
            }
        } catch (IOException e) {
            // Игнорируем ошибки чтения JAR
        }
        return false;
    }
}
