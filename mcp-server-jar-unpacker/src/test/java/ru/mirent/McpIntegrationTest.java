package ru.mirent;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import ru.mirent.services.DecompilationService;
import ru.mirent.services.JarSearchService;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Интеграционные тесты MCP-сервера с реальными JAR из ~/.m2/repository
 * <p>
 * Запуск: mvn test -DrunIntegrationTests=true
 * или: mvn test -Dit
 */
class McpIntegrationTest {

    private static JarSearchService jarSearchService;
    private static DecompilationService decompilationService;
    private static ToolRegistry toolRegistry;

    private static String guavaJarPath;
    private static String commonsLangJarPath;

    @BeforeAll
    static void setUpAll() {
        jarSearchService = new JarSearchService();
        decompilationService = new DecompilationService();
        toolRegistry = new DefaultToolRegistry();

        // Поиск тестовых JAR
        guavaJarPath = findGuavaJar();
        commonsLangJarPath = findCommonsLangJar();
    }

    @BeforeEach
    void setUp() {
        // Очистка выходной директории декомпиляции перед каждым тестом
        try {
            Path outputDir = Paths.get("/tmp/cfr-decompiled");
            if (Files.exists(outputDir)) {
                Files.walk(outputDir)
                    .filter(Files::isRegularFile)
                    .forEach(p -> {
                        try {
                            Files.delete(p);
                        } catch (Exception e) {
                            // Игнорируем ошибки удаления
                        }
                    });
            }
        } catch (Exception e) {
            // Игнорируем ошибки очистки
        }
    }

    // ==================== Тесты поиска ====================

    @Test
    @DisplayName("Поиск класса Preconditions из Guava")
    @EnabledIfSystemProperty(named = "runIntegrationTests", matches = "true")
    void givenPreconditionsClassWhenFindClassThenReturnsGuavaJar() {
        String result = jarSearchService.findClass(
            "com.google.common.base.Preconditions"
        );

        assertNotNull(result);
        assertTrue(result.contains("guava"), "Результат должен содержать 'guava': " + result);
        assertTrue(result.contains("Найдено JAR-файлов"), "Результат должен содержать количество найденных JAR");
    }

    @Test
    @DisplayName("Поиск класса StringUtils из Commons Lang")
    @EnabledIfSystemProperty(named = "runIntegrationTests", matches = "true")
    void givenStringUtilsClassWhenFindClassThenReturnsCommonsLangJar() {
        String result = jarSearchService.findClass(
            "org.apache.commons.lang3.StringUtils"
        );

        assertNotNull(result);
        assertTrue(result.contains("commons-lang"), "Результат должен содержать 'commons-lang': " + result);
    }

    @Test
    @DisplayName("Поиск по простому имени класса")
    @EnabledIfSystemProperty(named = "runIntegrationTests", matches = "true")
    void givenSimpleClassNameWhenFindClassThenReturnsMatches() {
        String result = jarSearchService.findClass("Preconditions");

        assertNotNull(result);
        // Может найти в нескольких версиях Guava
        assertTrue(result.contains("Найдено JAR-файлов"), "Результат должен содержать количество найденных JAR");
    }

    @Test
    @DisplayName("Поиск несуществующего класса")
    @EnabledIfSystemProperty(named = "runIntegrationTests", matches = "true")
    void givenNonExistentClassWhenFindClassThenReturnsNotFound() {
        String result = jarSearchService.findClass(
            "com.example.NonExistentClass12345"
        );

        assertNotNull(result);
        assertTrue(result.contains("не найден в JAR-файлах"), "Результат должен содержать сообщение о ненахождении: " + result);
    }

    // ==================== Тесты декомпиляции ====================

    @Test
    @DisplayName("Получение схемы класса Preconditions")
    @EnabledIfSystemProperty(named = "runIntegrationTests", matches = "true")
    void givenPreconditionsClassWhenGetClassOutlineThenReturnsOutline() {
        String outline = decompilationService.getClassOutline(
            guavaJarPath,
            "com.google.common.base.Preconditions"
        );

        assertNotNull(outline);
        assertTrue(outline.contains("class Preconditions"), "Схема должна содержать объявление класса: " + outline);
        assertTrue(outline.contains("checkNotNull"), "Схема должна содержать метод checkNotNull");
        // Тела методов должны быть заменены на "..."
        assertFalse(outline.contains("throw new NullPointerException"), "Тела методов не должно быть в схеме");
    }

    @Test
    @DisplayName("Получение метода checkNotNull из Preconditions")
    @EnabledIfSystemProperty(named = "runIntegrationTests", matches = "true")
    void givenCheckNotNullMethodWhenGetMethodSourceThenReturnsSource() {
        String source = decompilationService.getMethodSource(
            guavaJarPath,
            "com.google.common.base.Preconditions",
            "checkNotNull"
        );

        assertNotNull(source);
        assertTrue(source.contains("checkNotNull"), "Исходник должен содержать checkNotNull: " + source);
        // Метод возвращает сигнатуры перегрузок checkNotNull
        assertTrue(source.contains("public static"), "Должен содержать модификатор public static");
        // Может содержать несколько перегрузок
        assertTrue(source.contains("--- перегрузка ---") || source.contains("checkNotNull"), "Должен содержать перегрузки или сигнатуру");
    }

    @Test
    @DisplayName("Полная декомпиляция класса Preconditions")
    @EnabledIfSystemProperty(named = "runIntegrationTests", matches = "true")
    void givenPreconditionsClassWhenDecompileClassThenReturnsFullSource() {
        String source = decompilationService.decompileClass(
            guavaJarPath,
            "com.google.common.base.Preconditions"
        );

        assertNotNull(source);
        assertTrue(source.contains("class Preconditions"), "Полный исходник должен содержать объявление класса");
        assertTrue(source.contains("checkNotNull"), "Полный исходник должен содержать метод checkNotNull");
        assertTrue(source.contains("throw new NullPointerException"), "Полный исходник должен содержать тело метода");
        assertTrue(source.contains("JAR: guava"), "Полный исходник должен содержать информацию о JAR");
    }

    // ==================== Тесты инструментов ====================

    @Test
    @DisplayName("Инструмент find_class_in_m2 через реестр")
    @EnabledIfSystemProperty(named = "runIntegrationTests", matches = "true")
    void givenFindClassToolWhenCallThenReturnsResult() {
        Map<String, Object> args = new HashMap<>();
        args.put("class_name", "com.google.common.base.Preconditions");

        Object result = toolRegistry.callTool("find_class_in_m2", args);

        assertNotNull(result);
        assertTrue(result instanceof String);
        String resultStr = (String) result;
        assertTrue(resultStr.contains("guava"), "Результат должен содержать 'guava': " + resultStr);
    }

    @Test
    @DisplayName("Инструмент get_class_outline через реестр")
    @EnabledIfSystemProperty(named = "runIntegrationTests", matches = "true")
    void givenGetClassOutlineToolWhenCallThenReturnsOutline() {
        Map<String, Object> args = new HashMap<>();
        args.put("jar_path", guavaJarPath);
        args.put("class_fqn", "com.google.common.base.Preconditions");

        Object result = toolRegistry.callTool("get_class_outline", args);

        assertNotNull(result);
        assertTrue(result instanceof String);
        String resultStr = (String) result;
        assertTrue(resultStr.contains("class Preconditions"), "Результат должен содержать объявление класса: " + resultStr);
    }

    @Test
    @DisplayName("Инструмент get_method_source через реестр")
    @EnabledIfSystemProperty(named = "runIntegrationTests", matches = "true")
    void givenGetMethodSourceToolWhenCallThenReturnsSource() {
        Map<String, Object> args = new HashMap<>();
        args.put("jar_path", guavaJarPath);
        args.put("class_fqn", "com.google.common.base.Preconditions");
        args.put("method_name", "checkNotNull");

        Object result = toolRegistry.callTool("get_method_source", args);

        assertNotNull(result);
        assertTrue(result instanceof String);
        String resultStr = (String) result;
        assertTrue(resultStr.contains("checkNotNull"), "Результат должен содержать checkNotNull: " + resultStr);
    }

    @Test
    @DisplayName("Инструмент decompile_class через реестр")
    @EnabledIfSystemProperty(named = "runIntegrationTests", matches = "true")
    void givenDecompileClassToolWhenCallThenReturnsFullSource() {
        Map<String, Object> args = new HashMap<>();
        args.put("jar_path", guavaJarPath);
        args.put("class_fqn", "com.google.common.base.Preconditions");

        Object result = toolRegistry.callTool("decompile_class", args);

        assertNotNull(result);
        assertTrue(result instanceof String);
        String resultStr = (String) result;
        assertTrue(resultStr.contains("class Preconditions"), "Результат должен содержать объявление класса: " + resultStr);
    }

    // ==================== Тесты безопасности ====================

    @Test
    @DisplayName("Попытка path traversal блокируется")
    @EnabledIfSystemProperty(named = "runIntegrationTests", matches = "true")
    void givenPathTraversalWhenCallToolThenReturnsSecurityError() {
        Map<String, Object> args = new HashMap<>();
        args.put("jar_path", "../../../etc/passwd");
        args.put("class_fqn", "test.Test");

        Object result = toolRegistry.callTool("get_class_outline", args);

        assertNotNull(result);
        assertTrue(result instanceof String);
        String resultStr = (String) result;
        assertTrue(resultStr.contains("ОШИБКА"), "Результат должен содержать ошибку: " + resultStr);
    }

    @Test
    @DisplayName("Попытка инъекции FQN блокируется")
    @EnabledIfSystemProperty(named = "runIntegrationTests", matches = "true")
    void givenInvalidFQNWhenCallToolThenReturnsValidationError() {
        Map<String, Object> args = new HashMap<>();
        args.put("jar_path", guavaJarPath);
        args.put("class_fqn", "com/example/Class; rm -rf /");

        Object result = toolRegistry.callTool("get_class_outline", args);

        assertNotNull(result);
        assertTrue(result instanceof String);
        String resultStr = (String) result;
        assertTrue(resultStr.contains("ОШИБКА"), "Результат должен содержать ошибку валидации: " + resultStr);
    }

    // ==================== Вспомогательные методы ====================

    private static String findGuavaJar() {
        String result = jarSearchService.findClass(
            "com.google.common.base.Preconditions"
        );

        // Извлекаем первый найденный JAR с "guava" в пути
        String[] lines = result.split("\n");
        for (String line : lines) {
            line = line.trim();
            if (line.contains("guava") && line.contains(".jar")) {
                return line;
            }
        }

        throw new IllegalStateException(
            "Guava не найдена в локальном Maven-репозитории. " +
            "Запустите: mvn dependency:get -Dartifact=com.google.guava:guava:33.4.0-jre"
        );
    }

    private static String findCommonsLangJar() {
        String result = jarSearchService.findClass(
            "org.apache.commons.lang3.StringUtils"
        );

        String[] lines = result.split("\n");
        for (String line : lines) {
            line = line.trim();
            if (line.contains("commons-lang") && line.contains(".jar")) {
                return line;
            }
        }

        throw new IllegalStateException(
            "Commons Lang не найдена в локальном Maven-репозитории. " +
            "Запустите: mvn dependency:get -Dartifact=org.apache.commons:commons-lang3:3.14.0"
        );
    }
}
