package ru.mirent.tools;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("GetClassOutlineTool тесты")
class GetClassOutlineToolTest {

    @Test
    @DisplayName("givenGetClassOutlineToolWhenGetNameThenReturnsCorrectName")
    void givenGetClassOutlineToolWhenGetNameThenReturnsCorrectName() {
        GetClassOutlineTool tool = new GetClassOutlineTool();
        assertEquals("get_class_outline", tool.getName());
    }

    @Test
    @DisplayName("givenGetClassOutlineToolWhenGetDescriptionThenReturnsDescription")
    void givenGetClassOutlineToolWhenGetDescriptionThenReturnsDescription() {
        GetClassOutlineTool tool = new GetClassOutlineTool();
        String desc = tool.getDescription();
        assertTrue(desc.contains("краткую схему Java-класса"));
    }

    @Test
    @DisplayName("givenGetClassOutlineToolWhenGetInputSchemaThenReturnsSchema")
    void givenGetClassOutlineToolWhenGetInputSchemaThenReturnsSchema() {
        GetClassOutlineTool tool = new GetClassOutlineTool();
        Map<String, Object> schema = tool.getInputSchema();

        assertEquals("object", schema.get("type"));
        Map<String, Object> props = (Map<String, Object>) schema.get("properties");
        assertTrue(props.containsKey("jar_path"));
        assertTrue(props.containsKey("class_fqn"));
    }

    @Test
    @DisplayName("givenGetClassOutlineToolWhenGetInputSchemaThenRequiredContainsJarPathAndClassFqn")
    void givenGetClassOutlineToolWhenGetInputSchemaThenRequiredContainsJarPathAndClassFqn() {
        GetClassOutlineTool tool = new GetClassOutlineTool();
        Map<String, Object> schema = tool.getInputSchema();

        @SuppressWarnings("unchecked")
        java.util.List<String> required = (java.util.List<String>) schema.get("required");
        assertTrue(required.contains("jar_path"));
        assertTrue(required.contains("class_fqn"));
        assertEquals(2, required.size());
    }
}
