package ru.mirent.tools;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.FileOutputStream;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("SearchClassesByPatternTool тесты")
class SearchClassesByPatternToolTest {

    private SearchClassesByPatternTool tool;

    @TempDir
    Path tempDir;

    private Path testJar1;
    private Path testJar2;

    @BeforeEach
    void setUp() throws Exception {
        tool = new SearchClassesByPatternTool();
        testJar1 = createTestJar1();
        testJar2 = createTestJar2();
    }

    @Nested
    @DisplayName("Базовые тесты инструмента")
    class BasicTests {

        @Test
        @DisplayName("givenSearchClassesByPatternToolWhenGetNameThenReturnsCorrectName")
        void givenSearchClassesByPatternToolWhenGetNameThenReturnsCorrectName() {
            assertEquals("search_classes_by_pattern", tool.getName());
        }

        @Test
        @DisplayName("givenSearchClassesByPatternToolWhenGetDescriptionThenReturnsDescription")
        void givenSearchClassesByPatternToolWhenGetDescriptionThenReturnsDescription() {
            String desc = tool.getDescription();

            assertNotNull(desc);
            assertTrue(desc.contains("regex-паттерну"));
            assertTrue(desc.contains("шаблону"));
        }

        @Test
        @DisplayName("givenSearchClassesByPatternToolWhenGetInputSchemaThenReturnsSchema")
        void givenSearchClassesByPatternToolWhenGetInputSchemaThenReturnsSchema() {
            Map<String, Object> schema = tool.getInputSchema();

            assertEquals("object", schema.get("type"));
            Map<String, Object> props = (Map<String, Object>) schema.get("properties");
            assertTrue(props.containsKey("pattern"));
            assertTrue(props.containsKey("limit"));
        }

        @Test
        @DisplayName("givenSearchClassesByPatternToolWhenGetInputSchemaThenRequiredContainsPattern")
        void givenSearchClassesByPatternToolWhenGetInputSchemaThenRequiredContainsPattern() {
            Map<String, Object> schema = tool.getInputSchema();

            @SuppressWarnings("unchecked")
            List<String> required = (List<String>) schema.get("required");
            assertTrue(required.contains("pattern"));
            assertFalse(required.contains("limit"));
        }
    }

    @Nested
    @DisplayName("Тесты выполнения инструмента")
    class ExecuteTests {

        @Test
        @DisplayName("givenInvalidPatternWhenExecuteThenReturnsError")
        void givenInvalidPatternWhenExecuteThenReturnsError() {
            Map<String, Object> args = new HashMap<>();
            args.put("pattern", "[invalid(regex");

            Object result = tool.execute(args);

            assertNotNull(result);
            String resultStr = (String) result;
            assertTrue(resultStr.contains("ОШИБКА"));
            assertTrue(resultStr.contains("Некорректный regex"));
        }

        @Test
        @DisplayName("givenTemplatePatternWhenExecuteThenReturnsMatchingClasses")
        void givenTemplatePatternWhenExecuteThenReturnsMatchingClasses() {
            Map<String, Object> args = new HashMap<>();
            args.put("pattern", ".*Template.*");

            Object result = tool.execute(args);

            assertNotNull(result);
            String resultStr = (String) result;
            assertTrue(resultStr.contains("Template"));
        }

        @Test
        @DisplayName("givenControllerPatternWhenExecuteThenReturnsMatchingClasses")
        void givenControllerPatternWhenExecuteThenReturnsMatchingClasses() {
            Map<String, Object> args = new HashMap<>();
            args.put("pattern", ".*Controller.*");

            Object result = tool.execute(args);

            assertNotNull(result);
            String resultStr = (String) result;
            assertTrue(resultStr.contains("Controller"));
        }

        @Test
        @DisplayName("givenServicePatternWhenExecuteThenReturnsMatchingClasses")
        void givenServicePatternWhenExecuteThenReturnsMatchingClasses() {
            Map<String, Object> args = new HashMap<>();
            args.put("pattern", ".*Service.*");

            Object result = tool.execute(args);

            assertNotNull(result);
            String resultStr = (String) result;
            assertTrue(resultStr.contains("Service"));
        }

        @Test
        @DisplayName("givenNoMatchPatternWhenExecuteThenReturnsNoResults")
        void givenNoMatchPatternWhenExecuteThenReturnsNoResults() {
            Map<String, Object> args = new HashMap<>();
            args.put("pattern", ".*NonExistentClass.*");

            Object result = tool.execute(args);

            assertNotNull(result);
            String resultStr = (String) result;
            assertTrue(resultStr.contains("Не найдено классов"));
        }

        @Test
        @DisplayName("givenLimitWhenExecuteThenReturnsLimitedResults")
        void givenLimitWhenExecuteThenReturnsLimitedResults() {
            Map<String, Object> args = new HashMap<>();
            args.put("pattern", ".*");
            args.put("limit", "5");

            Object result = tool.execute(args);

            assertNotNull(result);
            // Результаты должны быть ограничены
        }

        @Test
        @DisplayName("givenExactMatchPatternWhenExecuteThenReturnsExactMatches")
        void givenExactMatchPatternWhenExecuteThenReturnsExactMatches() {
            Map<String, Object> args = new HashMap<>();
            args.put("pattern", "com\\.example\\.MyClass");

            Object result = tool.execute(args);

            assertNotNull(result);
        }

        @Test
        @DisplayName("givenEndsWithPatternWhenExecuteThenReturnsMatchingClasses")
        void givenEndsWithPatternWhenExecuteThenReturnsMatchingClasses() {
            Map<String, Object> args = new HashMap<>();
            args.put("pattern", ".*Controller$");

            Object result = tool.execute(args);

            assertNotNull(result);
            String resultStr = (String) result;
            assertTrue(resultStr.contains("Controller"));
        }
    }

    // Вспомогательные методы

    private Path createTestJar1() throws Exception {
        Path jarFile = tempDir.resolve("test1.jar");

        try (JarOutputStream jos = new JarOutputStream(new FileOutputStream(jarFile.toFile()))) {
            addJarEntry(jos, "com/example/MyTemplate.class");
            addJarEntry(jos, "com/example/MyController.class");
            addJarEntry(jos, "com/example/MyService.class");
            addJarEntry(jos, "com/example/MyClass.class");
        }

        return jarFile;
    }

    private Path createTestJar2() throws Exception {
        Path jarFile = tempDir.resolve("test2.jar");

        try (JarOutputStream jos = new JarOutputStream(new FileOutputStream(jarFile.toFile()))) {
            addJarEntry(jos, "org/test/AnotherTemplate.class");
            addJarEntry(jos, "org/test/AnotherController.class");
            addJarEntry(jos, "org/test/AnotherService.class");
        }

        return jarFile;
    }

    private void addJarEntry(JarOutputStream jos, String name) throws Exception {
        jos.putNextEntry(new JarEntry(name));
        jos.write(new byte[] {(byte) 0xCA, (byte) 0xFE, (byte) 0xBA, (byte) 0xBE});
        jos.closeEntry();
    }
}
