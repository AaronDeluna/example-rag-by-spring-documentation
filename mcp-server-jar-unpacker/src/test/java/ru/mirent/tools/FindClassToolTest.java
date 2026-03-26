package ru.mirent.tools;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("FindClassTool тесты")
class FindClassToolTest {

    @Test
    @DisplayName("givenFindClassToolWhenGetNameThenReturnsCorrectName")
    void givenFindClassToolWhenGetNameThenReturnsCorrectName() {
        FindClassTool tool = new FindClassTool();
        assertEquals("find_class_in_m2", tool.getName());
    }

    @Test
    @DisplayName("givenFindClassToolWhenGetDescriptionThenReturnsDescription")
    void givenFindClassToolWhenGetDescriptionThenReturnsDescription() {
        FindClassTool tool = new FindClassTool();
        String desc = tool.getDescription();
        assertTrue(desc.contains("Поиск Java-класса"));
    }

    @Test
    @DisplayName("givenFindClassToolWhenGetInputSchemaThenReturnsSchema")
    void givenFindClassToolWhenGetInputSchemaThenReturnsSchema() {
        FindClassTool tool = new FindClassTool();
        Map<String, Object> schema = tool.getInputSchema();

        assertEquals("object", schema.get("type"));
        Map<String, Object> props = (Map<String, Object>) schema.get("properties");
        assertTrue(props.containsKey("class_name"));
    }

    @Test
    @DisplayName("givenFindClassToolWhenGetInputSchemaThenRequiredContainsClassName")
    void givenFindClassToolWhenGetInputSchemaThenRequiredContainsClassName() {
        FindClassTool tool = new FindClassTool();
        Map<String, Object> schema = tool.getInputSchema();

        @SuppressWarnings("unchecked")
        java.util.List<String> required = (java.util.List<String>) schema.get("required");
        assertTrue(required.contains("class_name"));
        assertEquals(1, required.size());
    }
}
