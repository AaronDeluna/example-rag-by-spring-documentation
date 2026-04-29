package ru.mirent;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("DefaultToolRegistry тесты")
class DefaultToolRegistryTest {

    @Test
    @DisplayName("givenDefaultToolRegistryWhenGetToolsThenReturnsSixTools")
    void givenDefaultToolRegistryWhenGetToolsThenReturnsSixTools() {
        DefaultToolRegistry registry = new DefaultToolRegistry();
        List<Map<String, Object>> tools = registry.getTools();

        assertEquals(6, tools.size());
    }

    @Test
    @DisplayName("givenDefaultToolRegistryWhenGetToolsThenContainsListClassesInJar")
    void givenDefaultToolRegistryWhenGetToolsThenContainsListClassesInJar() {
        DefaultToolRegistry registry = new DefaultToolRegistry();
        List<Map<String, Object>> tools = registry.getTools();

        boolean hasListClassesTool = tools.stream()
            .anyMatch(t -> "list_classes_in_jar".equals(t.get("name")));
        assertTrue(hasListClassesTool);
    }

    @Test
    @DisplayName("givenDefaultToolRegistryWhenGetToolsThenContainsFindClassInM2")
    void givenDefaultToolRegistryWhenGetToolsThenContainsFindClassInM2() {
        DefaultToolRegistry registry = new DefaultToolRegistry();
        List<Map<String, Object>> tools = registry.getTools();

        boolean hasFindTool = tools.stream()
            .anyMatch(t -> "find_class_in_m2".equals(t.get("name")));
        assertTrue(hasFindTool);
    }

    @Test
    @DisplayName("givenDefaultToolRegistryWhenGetToolsThenContainsGetClassOutline")
    void givenDefaultToolRegistryWhenGetToolsThenContainsGetClassOutline() {
        DefaultToolRegistry registry = new DefaultToolRegistry();
        List<Map<String, Object>> tools = registry.getTools();

        boolean hasOutlineTool = tools.stream()
            .anyMatch(t -> "get_class_outline".equals(t.get("name")));
        assertTrue(hasOutlineTool);
    }

    @Test
    @DisplayName("givenDefaultToolRegistryWhenGetToolsThenContainsGetMethodSource")
    void givenDefaultToolRegistryWhenGetToolsThenContainsGetMethodSource() {
        DefaultToolRegistry registry = new DefaultToolRegistry();
        List<Map<String, Object>> tools = registry.getTools();

        boolean hasMethodTool = tools.stream()
            .anyMatch(t -> "get_method_source".equals(t.get("name")));
        assertTrue(hasMethodTool);
    }

    @Test
    @DisplayName("givenDefaultToolRegistryWhenGetToolsThenContainsDecompileClass")
    void givenDefaultToolRegistryWhenGetToolsThenContainsDecompileClass() {
        DefaultToolRegistry registry = new DefaultToolRegistry();
        List<Map<String, Object>> tools = registry.getTools();

        boolean hasDecompileTool = tools.stream()
            .anyMatch(t -> "decompile_class".equals(t.get("name")));
        assertTrue(hasDecompileTool);
    }

    @Test
    @DisplayName("givenDefaultToolRegistryWhenGetToolsThenContainsSearchClassesByPattern")
    void givenDefaultToolRegistryWhenGetToolsThenContainsSearchClassesByPattern() {
        DefaultToolRegistry registry = new DefaultToolRegistry();
        List<Map<String, Object>> tools = registry.getTools();

        boolean hasSearchPatternTool = tools.stream()
            .anyMatch(t -> "search_classes_by_pattern".equals(t.get("name")));
        assertTrue(hasSearchPatternTool);
    }

    @Test
    @DisplayName("givenDefaultToolRegistryWhenCallToolWithUnknownNameThenThrowsException")
    void givenDefaultToolRegistryWhenCallToolWithUnknownNameThenThrowsException() {
        DefaultToolRegistry registry = new DefaultToolRegistry();

        assertThrows(IllegalArgumentException.class, () -> {
            registry.callTool("unknown_tool", Map.of());
        });
    }

    @Test
    @DisplayName("givenDefaultToolRegistryWhenGetToolsThenEachToolHasNameDescriptionAndSchema")
    void givenDefaultToolRegistryWhenGetToolsThenEachToolHasNameDescriptionAndSchema() {
        DefaultToolRegistry registry = new DefaultToolRegistry();
        List<Map<String, Object>> tools = registry.getTools();

        for (Map<String, Object> tool : tools) {
            assertTrue(tool.containsKey("name"));
            assertTrue(tool.containsKey("description"));
            assertTrue(tool.containsKey("inputSchema"));
        }
    }
}
