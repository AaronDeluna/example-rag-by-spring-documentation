package ru.mirent.tools;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("DecompileClassTool тесты")
class DecompileClassToolTest {

    @Test
    @DisplayName("givenDecompileClassToolWhenGetNameThenReturnsCorrectName")
    void givenDecompileClassToolWhenGetNameThenReturnsCorrectName() {
        DecompileClassTool tool = new DecompileClassTool();
        assertEquals("decompile_class", tool.getName());
    }

    @Test
    @DisplayName("givenDecompileClassToolWhenGetDescriptionThenReturnsDescription")
    void givenDecompileClassToolWhenGetDescriptionThenReturnsDescription() {
        DecompileClassTool tool = new DecompileClassTool();
        String desc = tool.getDescription();
        assertTrue(desc.contains("полный декомпилированный исходник"));
    }

    @Test
    @DisplayName("givenDecompileClassToolWhenGetInputSchemaThenReturnsSchema")
    void givenDecompileClassToolWhenGetInputSchemaThenReturnsSchema() {
        DecompileClassTool tool = new DecompileClassTool();
        Map<String, Object> schema = tool.getInputSchema();

        assertEquals("object", schema.get("type"));
        Map<String, Object> props = (Map<String, Object>) schema.get("properties");
        assertTrue(props.containsKey("jar_path"));
        assertTrue(props.containsKey("class_fqn"));
    }

    @Test
    @DisplayName("givenDecompileClassToolWhenGetInputSchemaThenRequiredContainsJarPathAndClassFqn")
    void givenDecompileClassToolWhenGetInputSchemaThenRequiredContainsJarPathAndClassFqn() {
        DecompileClassTool tool = new DecompileClassTool();
        Map<String, Object> schema = tool.getInputSchema();

        @SuppressWarnings("unchecked")
        java.util.List<String> required = (java.util.List<String>) schema.get("required");
        assertTrue(required.contains("jar_path"));
        assertTrue(required.contains("class_fqn"));
        assertEquals(2, required.size());
    }
}
