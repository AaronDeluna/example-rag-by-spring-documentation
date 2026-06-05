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

@DisplayName("ListClassesInJarTool тесты")
class ListClassesInJarToolTest {

    private ListClassesInJarTool tool;

    @TempDir
    Path tempDir;

    private Path testJar;

    @BeforeEach
    void setUp() throws Exception {
        tool = new ListClassesInJarTool();
        testJar = createTestJar();
    }

    @Nested
    @DisplayName("Базовые тесты инструмента")
    class BasicTests {

        @Test
        @DisplayName("givenListClassesInJarToolWhenGetNameThenReturnsCorrectName")
        void givenListClassesInJarToolWhenGetNameThenReturnsCorrectName() {
            assertEquals("list_classes_in_jar", tool.getName());
        }

        @Test
        @DisplayName("givenListClassesInJarToolWhenGetDescriptionThenReturnsDescription")
        void givenListClassesInJarToolWhenGetDescriptionThenReturnsDescription() {
            String desc = tool.getDescription();

            assertNotNull(desc);
            assertTrue(desc.contains("список всех .class файлов"));
        }

        @Test
        @DisplayName("givenListClassesInJarToolWhenGetInputSchemaThenReturnsSchema")
        void givenListClassesInJarToolWhenGetInputSchemaThenReturnsSchema() {
            Map<String, Object> schema = tool.getInputSchema();

            assertEquals("object", schema.get("type"));
            Map<String, Object> props = (Map<String, Object>) schema.get("properties");
            assertTrue(props.containsKey("jar_path"));
            assertTrue(props.containsKey("filter"));
        }

        @Test
        @DisplayName("givenListClassesInJarToolWhenGetInputSchemaThenRequiredContainsJarPath")
        void givenListClassesInJarToolWhenGetInputSchemaThenRequiredContainsJarPath() {
            Map<String, Object> schema = tool.getInputSchema();

            @SuppressWarnings("unchecked")
            List<String> required = (List<String>) schema.get("required");
            assertTrue(required.contains("jar_path"));
            assertFalse(required.contains("filter"));
        }
    }

    @Nested
    @DisplayName("Тесты выполнения инструмента")
    class ExecuteTests {

        @Test
        @DisplayName("givenNonExistentJarWhenExecuteThenReturnsError")
        void givenNonExistentJarWhenExecuteThenReturnsError() {
            Map<String, Object> args = new HashMap<>();
            args.put("jar_path", "/nonexistent.jar");

            Object result = tool.execute(args);

            assertNotNull(result);
            String resultStr = (String) result;
            assertTrue(resultStr.contains("ОШИБКА"));
        }

        @Test
        @DisplayName("givenInvalidJarPathWhenExecuteThenReturnsSecurityError")
        void givenInvalidJarPathWhenExecuteThenReturnsSecurityError() {
            Map<String, Object> args = new HashMap<>();
            args.put("jar_path", "/etc/passwd");

            Object result = tool.execute(args);

            assertNotNull(result);
            String resultStr = (String) result;
            assertTrue(resultStr.contains("ОШИБКА"));
        }

        @Test
        @DisplayName("givenNotAJarFileWhenExecuteThenReturnsSecurityError")
        void givenNotAJarFileWhenExecuteThenReturnsSecurityError() {
            Map<String, Object> args = new HashMap<>();
            args.put("jar_path", "/home/user/test.txt");

            Object result = tool.execute(args);

            assertNotNull(result);
            String resultStr = (String) result;
            assertTrue(resultStr.contains("ОШИБКА"));
        }
    }

    @Nested
    @DisplayName("Интеграционные тесты с listClassesInJar")
    class ListClassesIntegrationTests {

        @Test
        @DisplayName("givenValidJarWhenListClassesInJarThenReturnsClassList")
        void givenValidJarWhenListClassesInJarThenReturnsClassList() throws Exception {
            List<String> classes = tool.listClassesInJar(testJar, null);

            assertEquals(3, classes.size());
            assertTrue(classes.contains("com/example/Class1.class"));
            assertTrue(classes.contains("com/example/Class2.class"));
            assertTrue(classes.contains("com/example/Class3.class"));
        }

        @Test
        @DisplayName("givenFilterWhenListClassesInJarThenReturnsFilteredClassList")
        void givenFilterWhenListClassesInJarThenReturnsFilteredClassList() throws Exception {
            List<String> classes = tool.listClassesInJar(testJar, ".*Class1.*");

            assertEquals(1, classes.size());
            assertTrue(classes.contains("com/example/Class1.class"));
        }

        @Test
        @DisplayName("givenJarWithModuleInfoWhenListClassesInJarThenExcludesModuleInfo")
        void givenJarWithModuleInfoWhenListClassesInJarThenExcludesModuleInfo() throws Exception {
            List<String> classes = tool.listClassesInJar(testJar, null);

            assertFalse(classes.contains("module-info.class"));
        }

        @Test
        @DisplayName("givenEmptyJarWhenListClassesInJarThenReturnsEmptyList")
        void givenEmptyJarWhenListClassesInJarThenReturnsEmptyList() throws Exception {
            Path emptyJar = createEmptyJar();

            List<String> classes = tool.listClassesInJar(emptyJar, null);

            assertTrue(classes.isEmpty());
        }
    }

    // Вспомогательные методы

    private Path createTestJar() throws Exception {
        Path jarFile = tempDir.resolve("test.jar");

        try (JarOutputStream jos = new JarOutputStream(new FileOutputStream(jarFile.toFile()))) {
            // Добавляем 3 класса
            addJarEntry(jos, "com/example/Class1.class");
            addJarEntry(jos, "com/example/Class2.class");
            addJarEntry(jos, "com/example/Class3.class");

            // Добавляем module-info (должен исключаться)
            addJarEntry(jos, "module-info.class");

            // Добавляем не-class файл
            addJarEntry(jos, "README.txt");
        }

        return jarFile;
    }

    private Path createEmptyJar() throws Exception {
        Path jarFile = tempDir.resolve("empty.jar");

        try (JarOutputStream jos = new JarOutputStream(new FileOutputStream(jarFile.toFile()))) {
            // Пустой JAR
        }

        return jarFile;
    }

    private void addJarEntry(JarOutputStream jos, String name) throws Exception {
        jos.putNextEntry(new JarEntry(name));
        jos.write(new byte[] {(byte) 0xCA, (byte) 0xFE, (byte) 0xBA, (byte) 0xBE}); // Fake class header
        jos.closeEntry();
    }
}
