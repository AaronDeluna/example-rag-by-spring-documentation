package ru.mirent.security;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Тесты для PathValidator
 */
class PathValidatorTest {

    private Path testDir;
    private Path m2RepoPath;

    @BeforeEach
    void setUp() throws IOException {
        // Используем поддиректорию в ~/.m2/repository для тестов
        m2RepoPath = PathValidator.getM2RepoPath();
        testDir = m2RepoPath.resolve("test-path-validator-" + System.nanoTime());
        Files.createDirectories(testDir);
    }

    @AfterEach
    void tearDown() throws IOException {
        // Очищаем тестовые файлы
        if (testDir != null && Files.exists(testDir)) {
            Files.walk(testDir)
                .sorted((a, b) -> b.compareTo(a))
                .forEach(path -> {
                    try {
                        Files.deleteIfExists(path);
                    } catch (IOException e) {
                        // Игнорируем ошибки удаления
                    }
                });
        }
    }

    @Test
    void givenValidPathWhenValidateThenReturnsNormalizedPath() throws IOException {
        Path testFile = createTestJarFile("test.jar");

        Path result = PathValidator.validateJarPath(testFile.toString());

        assertNotNull(result);
        assertTrue(result.endsWith("test.jar"));
        assertTrue(Files.exists(result));
    }

    @Test
    void givenPathTraversalWhenValidateThenThrowsSecurityException() {
        String maliciousPath = "../../../etc/passwd";

        SecurityException exception = assertThrows(SecurityException.class, () -> {
            PathValidator.validateJarPath(maliciousPath);
        });

        assertTrue(exception.getMessage().contains("Путь выходит за пределы"));
    }

    @Test
    void givenAbsoluteSystemPathWhenValidateThenThrowsSecurityException() {
        String systemPath = "/etc/shadow";

        SecurityException exception = assertThrows(SecurityException.class, () -> {
            PathValidator.validateJarPath(systemPath);
        });

        assertTrue(exception.getMessage().contains("Путь выходит за пределы"));
    }

    @Test
    void givenNullPathWhenValidateThenThrowsSecurityException() {
        SecurityException exception = assertThrows(SecurityException.class, () -> {
            PathValidator.validateJarPath(null);
        });

        assertTrue(exception.getMessage().contains("Путь не может быть пустым"));
    }

    @Test
    void givenEmptyPathWhenValidateThenThrowsSecurityException() {
        SecurityException exception = assertThrows(SecurityException.class, () -> {
            PathValidator.validateJarPath("");
        });

        assertTrue(exception.getMessage().contains("Путь не может быть пустым"));
    }

    @Test
    void givenWhitespacePathWhenValidateThenThrowsSecurityException() {
        SecurityException exception = assertThrows(SecurityException.class, () -> {
            PathValidator.validateJarPath("   ");
        });

        assertTrue(exception.getMessage().contains("Путь не может быть пустым"));
    }

    @Test
    void givenNonJarFileWhenValidateThenThrowsSecurityException() throws IOException {
        Path testFile = createTestFile("test.class");

        SecurityException exception = assertThrows(SecurityException.class, () -> {
            PathValidator.validateJarPath(testFile.toString());
        });

        assertTrue(exception.getMessage().contains("Файл должен быть JAR"));
    }

    @Test
    void givenNonExistentJarWhenValidateThenThrowsSecurityException() {
        String nonExistentPath = System.getProperty("user.home") +
            "/.m2/repository/com/example/nonexistent.jar";

        SecurityException exception = assertThrows(SecurityException.class, () -> {
            PathValidator.validateJarPath(nonExistentPath);
        });

        assertTrue(exception.getMessage().contains("Файл не найден"));
    }

    @Test
    void givenPathWithDotDotInsideM2WhenValidateThenReturnsNormalizedPath() throws IOException {
        // Создаём структуру: testdir/subdir/test.jar
        Path subDir = testDir.resolve("subdir");
        Files.createDirectories(subDir);
        Path testFile = subDir.resolve("test.jar");
        Files.createFile(testFile);
        
        // Путь с ../ внутри допустимой директории
        String pathWithDotDot = testDir.toString() + "/subdir/../subdir/test.jar";

        Path result = PathValidator.validateJarPath(pathWithDotDot);

        assertNotNull(result);
        assertTrue(result.endsWith("test.jar"));
    }

    @Test
    void givenIsValidJarPathWithValidPathThenReturnsTrue() throws IOException {
        Path testFile = createTestJarFile("valid.jar");

        assertTrue(PathValidator.isValidJarPath(testFile.toString()));
    }

    @Test
    void givenIsValidJarPathWithInvalidPathThenReturnsFalse() {
        assertFalse(PathValidator.isValidJarPath("/etc/passwd"));
    }

    @Test
    void givenIsValidJarPathWithNullThenReturnsFalse() {
        assertFalse(PathValidator.isValidJarPath(null));
    }

    @Test
    void givenIsValidJarPathWithEmptyThenReturnsFalse() {
        assertFalse(PathValidator.isValidJarPath(""));
    }

    @Test
    void givenGetM2RepoPathThenReturnsCorrectPath() {
        Path m2Path = PathValidator.getM2RepoPath();

        assertNotNull(m2Path);
        assertTrue(m2Path.endsWith("repository"));
        assertTrue(m2Path.startsWith(System.getProperty("user.home")));
    }

    @Test
    void givenPathWithWindowsStyleTraversalWhenValidateThenThrowsSecurityException() {
        // Тест на случай, если путь содержит ..\ (хотя на Linux это не актуально)
        String maliciousPath = System.getProperty("user.home") + "/.m2/repository/../../../etc/passwd";

        assertThrows(SecurityException.class, () -> {
            PathValidator.validateJarPath(maliciousPath);
        });
    }

    @Test
    void givenRealMavenJarWhenValidateThenReturnsNormalizedPath() throws IOException {
        // Проверяем с реальным JAR из Maven-репозитория
        // Если JAR нет, тест просто создаёт тестовый файл
        String mavenRepo = System.getProperty("user.home") + "/.m2/repository";
        Path mavenPath = Paths.get(mavenRepo);

        if (Files.exists(mavenPath)) {
            // Ищем любой JAR в репозитории
            try {
                var jarOpt = Files.walk(mavenPath)
                    .filter(p -> p.toString().endsWith(".jar"))
                    .filter(p -> !p.toString().endsWith("-sources.jar"))
                    .filter(p -> !p.toString().endsWith("-javadoc.jar"))
                    .findFirst();

                if (jarOpt.isPresent()) {
                    Path jar = jarOpt.get();
                    // Просто проверяем, что валидация не бросает исключение
                    assertDoesNotThrow(() -> PathValidator.validateJarPath(jar.toString()));
                    return;
                }
            } catch (IOException e) {
                // Игнорируем, если нет доступа к файлам
            }
        }
        
        // Если JAR не найден, создаём тестовый файл в ~/.m2/repository
        Path testJar = createTestJarFile("fallback-test.jar");
        // Просто проверяем, что валидация не бросает исключение
        assertDoesNotThrow(() -> PathValidator.validateJarPath(testJar.toString()));
    }

    // ==================== Вспомогательные методы ====================

    /**
     * Создать тестовый JAR-файл во временной директории
     */
    private Path createTestJarFile(String filename) throws IOException {
        Path jarPath = testDir.resolve(filename);
        Files.createDirectories(jarPath.getParent());
        Files.createFile(jarPath);
        return jarPath;
    }

    /**
     * Создать тестовый файл с произвольным расширением
     */
    private Path createTestFile(String filename) throws IOException {
        Path filePath = testDir.resolve(filename);
        Files.createDirectories(filePath.getParent());
        Files.createFile(filePath);
        return filePath;
    }
}
