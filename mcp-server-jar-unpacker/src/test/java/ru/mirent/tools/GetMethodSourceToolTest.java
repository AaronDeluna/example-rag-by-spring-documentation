package ru.mirent.tools;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("GetMethodSourceTool тесты")
class GetMethodSourceToolTest {

    @Test
    @DisplayName("givenGetMethodSourceToolWhenGetNameThenReturnsCorrectName")
    void givenGetMethodSourceToolWhenGetNameThenReturnsCorrectName() {
        GetMethodSourceTool tool = new GetMethodSourceTool();
        assertEquals("get_method_source", tool.getName());
    }

    @Test
    @DisplayName("givenGetMethodSourceToolWhenGetDescriptionThenReturnsDescription")
    void givenGetMethodSourceToolWhenGetDescriptionThenReturnsDescription() {
        GetMethodSourceTool tool = new GetMethodSourceTool();
        String desc = tool.getDescription();
        assertTrue(desc.contains("исходный код конкретного метода"));
    }

    @Test
    @DisplayName("givenGetMethodSourceToolWhenGetInputSchemaThenReturnsSchema")
    void givenGetMethodSourceToolWhenGetInputSchemaThenReturnsSchema() {
        GetMethodSourceTool tool = new GetMethodSourceTool();
        Map<String, Object> schema = tool.getInputSchema();

        assertEquals("object", schema.get("type"));
        Map<String, Object> props = (Map<String, Object>) schema.get("properties");
        assertTrue(props.containsKey("jar_path"));
        assertTrue(props.containsKey("class_fqn"));
        assertTrue(props.containsKey("method_name"));
    }

    @Test
    @DisplayName("givenGetMethodSourceToolWhenGetInputSchemaThenRequiredContainsAllParams")
    void givenGetMethodSourceToolWhenGetInputSchemaThenRequiredContainsAllParams() {
        GetMethodSourceTool tool = new GetMethodSourceTool();
        Map<String, Object> schema = tool.getInputSchema();

        @SuppressWarnings("unchecked")
        java.util.List<String> required = (java.util.List<String>) schema.get("required");
        assertTrue(required.contains("jar_path"));
        assertTrue(required.contains("class_fqn"));
        assertTrue(required.contains("method_name"));
        assertEquals(3, required.size());
    }
}
