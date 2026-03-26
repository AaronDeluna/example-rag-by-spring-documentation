package ru.mirent.security;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Валидатор путей для защиты от path traversal атак
 * <p>
 * Обеспечивает проверку путей к JAR-файлам, гарантируя,
 * что они находятся в пределах ~/.m2/repository
 */
public class PathValidator {

    private static final Path M2_REPO = Paths.get(
        System.getProperty("user.home"), ".m2", "repository"
    ).toAbsolutePath().normalize();

    private PathValidator() {
        // Утилитный класс, не должен инстанцироваться
    }

    /**
     * Проверить и вернуть нормализованный путь
     *
     * @param userInput пользовательский ввод пути
     * @return нормализованный Path
     * @throws SecurityException если путь выходит за пределы ~/.m2/repository
     */
    public static Path validateJarPath(String userInput) {
        if (userInput == null || userInput.trim().isEmpty()) {
            throw new SecurityException("Путь не может быть пустым");
        }

        // Нормализация пути (устранение ../ и ./)
        Path path = Paths.get(userInput).toAbsolutePath().normalize();

        // Проверка: путь должен начинаться с ~/.m2/repository
        if (!path.startsWith(M2_REPO)) {
            throw new SecurityException(
                "Путь выходит за пределы Maven-репозитория: " + userInput +
                    ". Разрешены только пути внутри " + M2_REPO
            );
        }

        // Проверка: файл должен существовать
        if (!Files.exists(path)) {
            throw new SecurityException("Файл не найден: " + userInput);
        }

        // Проверка: файл должен быть JAR
        if (!userInput.endsWith(".jar")) {
            throw new SecurityException("Файл должен быть JAR: " + userInput);
        }

        return path;
    }

    /**
     * Проверить путь без исключения (возвращает boolean)
     *
     * @param userInput пользовательский ввод пути
     * @return true если путь валиден, false иначе
     */
    public static boolean isValidJarPath(String userInput) {
        try {
            validateJarPath(userInput);
            return true;
        } catch (SecurityException e) {
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Вернуть путь к Maven-репозиторию для сообщений об ошибках
     *
     * @return путь к ~/.m2/repository
     */
    public static Path getM2RepoPath() {
        return M2_REPO;
    }
}
