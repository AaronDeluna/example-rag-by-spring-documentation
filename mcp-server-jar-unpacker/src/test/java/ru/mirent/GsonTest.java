package ru.mirent;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Тесты Gson")
class GsonTest {

    private final Server.Gson gson = new Server.Gson();

    @Nested
    @DisplayName("toJson()")
    class ToJsonTests {

        @Test
        @DisplayName("givenNullWhenToJsonThenReturnsNullString")
        void givenNullWhenToJsonThenReturnsNullString() {
            String result = gson.toJson(null);

            assertEquals("null", result);
        }

        @Test
        @DisplayName("givenStringWhenToJsonThenReturnsQuotedString")
        void givenStringWhenToJsonThenReturnsQuotedString() {
            String result = gson.toJson("hello");

            assertEquals("\"hello\"", result);
        }

        @Test
        @DisplayName("givenStringWithSpecialCharsWhenToJsonThenReturnsEscapedString")
        void givenStringWithSpecialCharsWhenToJsonThenReturnsEscapedString() {
            String result = gson.toJson("hello\nworld\t\"test\"");

            // escapeJson экранирует кавычки как \"
            assertEquals("\"hello\\nworld\\t\\\"test\\\"\"", result);
        }

        @Test
        @DisplayName("givenNumberWhenToJsonThenReturnsNumberString")
        void givenNumberWhenToJsonThenReturnsNumberString() {
            String result = gson.toJson(42);

            assertEquals("42", result);
        }

        @Test
        @DisplayName("givenDoubleWhenToJsonThenReturnsDoubleString")
        void givenDoubleWhenToJsonThenReturnsDoubleString() {
            String result = gson.toJson(3.14);

            assertEquals("3.14", result);
        }

        @Test
        @DisplayName("givenBooleanWhenToJsonThenReturnsBooleanString")
        void givenBooleanWhenToJsonThenReturnsBooleanString() {
            assertEquals("true", gson.toJson(true));
            assertEquals("false", gson.toJson(false));
        }

        @Test
        @DisplayName("givenMapWhenToJsonThenReturnsJsonObject")
        void givenMapWhenToJsonThenReturnsJsonObject() {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("name", "test");
            map.put("value", 42);

            String result = gson.toJson(map);

            assertTrue(result.contains("\"name\":\"test\""));
            assertTrue(result.contains("\"value\":42"));
            assertTrue(result.startsWith("{"));
            assertTrue(result.endsWith("}"));
        }

        @Test
        @DisplayName("givenListWhenToJsonThenReturnsJsonArray")
        void givenListWhenToJsonThenReturnsJsonArray() {
            List<String> list = new ArrayList<>();
            list.add("first");
            list.add("second");
            list.add("third");

            String result = gson.toJson(list);

            assertEquals("[\"first\",\"second\",\"third\"]", result);
        }

        @Test
        @DisplayName("givenEmptyMapWhenToJsonThenReturnsEmptyObject")
        void givenEmptyMapWhenToJsonThenReturnsEmptyObject() {
            Map<String, Object> map = new HashMap<>();

            String result = gson.toJson(map);

            assertEquals("{}", result);
        }

        @Test
        @DisplayName("givenEmptyListWhenToJsonThenReturnsEmptyArray")
        void givenEmptyListWhenToJsonThenReturnsEmptyArray() {
            List<String> list = new ArrayList<>();

            String result = gson.toJson(list);

            assertEquals("[]", result);
        }

        @Test
        @DisplayName("givenNestedMapWhenToJsonThenReturnsNestedJson")
        void givenNestedMapWhenToJsonThenReturnsNestedJson() {
            Map<String, Object> inner = new LinkedHashMap<>();
            inner.put("key", "value");

            Map<String, Object> outer = new LinkedHashMap<>();
            outer.put("inner", inner);

            String result = gson.toJson(outer);

            assertTrue(result.contains("\"inner\":{\"key\":\"value\"}"));
        }

        @Test
        @DisplayName("givenListOfMapsWhenToJsonThenReturnsCorrectJson")
        void givenListOfMapsWhenToJsonThenReturnsCorrectJson() {
            List<Map<String, Object>> list = new ArrayList<>();
            Map<String, Object> map1 = new LinkedHashMap<>();
            map1.put("id", 1);
            Map<String, Object> map2 = new LinkedHashMap<>();
            map2.put("id", 2);
            list.add(map1);
            list.add(map2);

            String result = gson.toJson(list);

            assertTrue(result.contains("{\"id\":1}"));
            assertTrue(result.contains("{\"id\":2}"));
        }
    }

    @Nested
    @DisplayName("escapeJson()")
    class EscapeJsonTests {

        @Test
        @DisplayName("givenStringWithoutSpecialCharsWhenEscapeJsonThenReturnsSameString")
        void givenStringWithoutSpecialCharsWhenEscapeJsonThenReturnsSameString() {
            String result = invokeEscapeJson("hello world");

            assertEquals("hello world", result);
        }

        @Test
        @DisplayName("givenStringWithQuotesWhenEscapeJsonThenEscapesQuotes")
        void givenStringWithQuotesWhenEscapeJsonThenEscapesQuotes() {
            String result = invokeEscapeJson("say \"hello\"");

            assertEquals("say \\\"hello\\\"", result);
        }

        @Test
        @DisplayName("givenStringWithBackslashWhenEscapeJsonThenEscapesBackslash")
        void givenStringWithBackslashWhenEscapeJsonThenEscapesBackslash() {
            String result = invokeEscapeJson("path\\to\\file");

            assertEquals("path\\\\to\\\\file", result);
        }

        @Test
        @DisplayName("givenStringWithNewlineWhenEscapeJsonThenEscapesNewline")
        void givenStringWithNewlineWhenEscapeJsonThenEscapesNewline() {
            String result = invokeEscapeJson("line1\nline2");

            assertEquals("line1\\nline2", result);
        }

        @Test
        @DisplayName("givenStringWithCarriageReturnWhenEscapeJsonThenEscapesCR")
        void givenStringWithCarriageReturnWhenEscapeJsonThenEscapesCR() {
            String result = invokeEscapeJson("text\rmore");

            assertEquals("text\\rmore", result);
        }

        @Test
        @DisplayName("givenStringWithTabWhenEscapeJsonThenEscapesTab")
        void givenStringWithTabWhenEscapeJsonThenEscapesTab() {
            String result = invokeEscapeJson("col1\tcol2");

            assertEquals("col1\\tcol2", result);
        }

        @Test
        @DisplayName("givenStringWithMultipleSpecialCharsWhenEscapeJsonThenEscapesAll")
        void givenStringWithMultipleSpecialCharsWhenEscapeJsonThenEscapesAll() {
            String result = invokeEscapeJson("line1\nline2\t\"quoted\"\\backslash");

            assertEquals("line1\\nline2\\t\\\"quoted\\\"\\\\backslash", result);
        }
    }

    @Nested
    @DisplayName("toJsonMap()")
    class ToJsonMapTests {

        @Test
        @DisplayName("givenSimpleMapWhenToJsonMapThenReturnsCorrectJson")
        void givenSimpleMapWhenToJsonMapThenReturnsCorrectJson() {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("key1", "value1");
            map.put("key2", "value2");

            String result = invokeToJsonMap(map);

            assertTrue(result.contains("\"key1\":\"value1\""));
            assertTrue(result.contains("\"key2\":\"value2\""));
        }

        @Test
        @DisplayName("givenMapWithMixedTypesWhenToJsonMapThenReturnsCorrectJson")
        void givenMapWithMixedTypesWhenToJsonMapThenReturnsCorrectJson() {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("string", "text");
            map.put("number", 42);
            map.put("boolean", true);
            map.put("null", null);

            String result = invokeToJsonMap(map);

            assertTrue(result.contains("\"string\":\"text\""));
            assertTrue(result.contains("\"number\":42"));
            assertTrue(result.contains("\"boolean\":true"));
            assertTrue(result.contains("\"null\":null"));
        }
    }

    @Nested
    @DisplayName("toJsonList()")
    class ToJsonListTests {

        @Test
        @DisplayName("givenSimpleListWhenToJsonListThenReturnsCorrectJson")
        void givenSimpleListWhenToJsonListThenReturnsCorrectJson() {
            List<String> list = new ArrayList<>();
            list.add("item1");
            list.add("item2");

            String result = invokeToJsonList(list);

            assertEquals("[\"item1\",\"item2\"]", result);
        }

        @Test
        @DisplayName("givenListWithMixedTypesWhenToJsonListThenReturnsCorrectJson")
        void givenListWithMixedTypesWhenToJsonListThenReturnsCorrectJson() {
            List<Object> list = new ArrayList<>();
            list.add("text");
            list.add(42);
            list.add(true);
            list.add(null);

            String result = invokeToJsonList(list);

            assertEquals("[\"text\",42,true,null]", result);
        }
    }

    // Вспомогательные методы для вызова приватных методов через рефлексию
    private String invokeEscapeJson(String input) {
        try {
            java.lang.reflect.Method method = Server.Gson.class.getDeclaredMethod("escapeJson", String.class);
            method.setAccessible(true);
            return (String) method.invoke(gson, input);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private String invokeToJsonMap(Map<?, ?> map) {
        try {
            java.lang.reflect.Method method = Server.Gson.class.getDeclaredMethod("toJsonMap", Map.class);
            method.setAccessible(true);
            return (String) method.invoke(gson, map);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private String invokeToJsonList(List<?> list) {
        try {
            java.lang.reflect.Method method = Server.Gson.class.getDeclaredMethod("toJsonList", List.class);
            method.setAccessible(true);
            return (String) method.invoke(gson, list);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
