package ru.mirent;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Тесты JsonUtils")
class JsonUtilsTest {

    @Nested
    @DisplayName("parse()")
    class ParseTests {

        @Test
        @DisplayName("givenValidJsonWithNumericIdWhenParseThenReturnsCorrectMessage")
        void givenValidJsonWithNumericIdWhenParseThenReturnsCorrectMessage() throws IOException {
            String json = "{\"jsonrpc\":\"2.0\",\"id\":1,\"method\":\"initialize\",\"params\":{}}";

            Server.JsonUtils.Parsed parsed = Server.JsonUtils.parse(json);

            assertEquals(1, parsed.id);
            assertEquals("initialize", parsed.method);
            assertNotNull(parsed.params);
        }

        @Test
        @DisplayName("givenValidJsonWithStringIdWhenParseThenReturnsCorrectMessage")
        void givenValidJsonWithStringIdWhenParseThenReturnsCorrectMessage() throws IOException {
            String json = "{\"jsonrpc\":\"2.0\",\"id\":\"abc-123\",\"method\":\"tools/list\",\"params\":{}}";

            Server.JsonUtils.Parsed parsed = Server.JsonUtils.parse(json);

            assertEquals("abc-123", parsed.id);
            assertEquals("tools/list", parsed.method);
        }

        @Test
        @DisplayName("givenJsonWithParamsWhenParseThenReturnsParamsMap")
        void givenJsonWithParamsWhenParseThenReturnsParamsMap() throws IOException {
            String json = "{\"jsonrpc\":\"2.0\",\"id\":5,\"method\":\"tools/call\"," +
                    "\"params\":{\"name\":\"find_class_in_m2\",\"arguments\":{\"class_name\":\"MyClass\"}}}";

            Server.JsonUtils.Parsed parsed = Server.JsonUtils.parse(json);

            assertEquals("tools/call", parsed.method);
            assertNotNull(parsed.params);
            @SuppressWarnings("unchecked")
            Map<String, Object> params = (Map<String, Object>) parsed.params;
            assertEquals("find_class_in_m2", params.get("name"));
            @SuppressWarnings("unchecked")
            Map<String, Object> arguments = (Map<String, Object>) params.get("arguments");
            assertEquals("MyClass", arguments.get("class_name"));
        }

        @Test
        @DisplayName("givenJsonWithoutParamsWhenParseThenParamsIsNull")
        void givenJsonWithoutParamsWhenParseThenParamsIsNull() throws IOException {
            String json = "{\"jsonrpc\":\"2.0\",\"id\":1,\"method\":\"notifications/initialized\"}";

            Server.JsonUtils.Parsed parsed = Server.JsonUtils.parse(json);

            assertNull(parsed.params);
        }

        @Test
        @DisplayName("givenEmptyJsonWhenParseThenReturnsMessageWithNulls")
        void givenEmptyJsonWhenParseThenReturnsMessageWithNulls() throws IOException {
            // Пустая строка не выбрасывает исключение, а возвращает объект с null полями
            Server.JsonUtils.Parsed parsed = Server.JsonUtils.parse("");

            assertNull(parsed.id);
            assertNull(parsed.method);
            assertNull(parsed.params);
        }
    }

    @Nested
    @DisplayName("extractJsonObject()")
    class ExtractJsonObjectTests {

        @Test
        @DisplayName("givenSimpleJsonObjectWhenExtractJsonObjectThenReturnsCorrectObject")
        void givenSimpleJsonObjectWhenExtractJsonObjectThenReturnsCorrectObject() throws IOException {
            String json = "{\"key\":\"value\",\"number\":42}";
            String fullJson = "{\"params\":" + json + "}";

            String extracted = invokeExtractJsonObject(fullJson, 10);

            assertEquals(json, extracted);
        }

        @Test
        @DisplayName("givenNestedJsonObjectWhenExtractJsonObjectThenReturnsCorrectObject")
        void givenNestedJsonObjectWhenExtractJsonObjectThenReturnsCorrectObject() throws IOException {
            String nested = "{\"inner\":{\"deep\":\"value\"}}";
            String fullJson = "{\"params\":" + nested + "}";

            String extracted = invokeExtractJsonObject(fullJson, 10);

            assertEquals(nested, extracted);
        }

        @Test
        @DisplayName("givenJsonObjectWithEscapedQuotesWhenExtractJsonObjectThenReturnsCorrectObject")
        void givenJsonObjectWithEscapedQuotesWhenExtractJsonObjectThenReturnsCorrectObject() throws IOException {
            String json = "{\"message\":\"Hello \\\"World\\\"\"}";
            String fullJson = "{\"params\":" + json + "}";

            String extracted = invokeExtractJsonObject(fullJson, 10);

            assertEquals(json, extracted);
        }
    }

    @Nested
    @DisplayName("extractJsonArray()")
    class ExtractJsonArrayTests {

        @Test
        @DisplayName("givenSimpleJsonArrayWhenExtractJsonArrayThenReturnsCorrectArray")
        void givenSimpleJsonArrayWhenExtractJsonArrayThenReturnsCorrectArray() throws IOException {
            String json = "[1,2,3]";
            String fullJson = "{\"params\":" + json + "}";

            String extracted = invokeExtractJsonArray(fullJson, 10);

            assertEquals(json, extracted);
        }

        @Test
        @DisplayName("givenJsonArrayWithObjectsWhenExtractJsonArrayThenReturnsCorrectArray")
        void givenJsonArrayWithObjectsWhenExtractJsonArrayThenReturnsCorrectArray() throws IOException {
            String json = "[{\"name\":\"first\"},{\"name\":\"second\"}]";
            String fullJson = "{\"params\":" + json + "}";

            String extracted = invokeExtractJsonArray(fullJson, 10);

            assertEquals(json, extracted);
        }

        @Test
        @DisplayName("givenNestedJsonArrayWhenExtractJsonArrayThenReturnsCorrectArray")
        void givenNestedJsonArrayWhenExtractJsonArrayThenReturnsCorrectArray() throws IOException {
            String json = "[1,[2,3],4]";
            String fullJson = "{\"params\":" + json + "}";

            String extracted = invokeExtractJsonArray(fullJson, 10);

            assertEquals(json, extracted);
        }
    }

    @Nested
    @DisplayName("parseJsonObject()")
    class ParseJsonObjectTests {

        @Test
        @DisplayName("givenEmptyJsonObjectWhenParseJsonObjectThenReturnsEmptyMap")
        void givenEmptyJsonObjectWhenParseJsonObjectThenReturnsEmptyMap() {
            Map<String, Object> result = invokeParseJsonObject("{}");

            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("givenSimpleJsonObjectWhenParseJsonObjectThenReturnsCorrectMap")
        void givenSimpleJsonObjectWhenParseJsonObjectThenReturnsCorrectMap() {
            String json = "{\"name\":\"test\",\"value\":42}";

            Map<String, Object> result = invokeParseJsonObject(json);

            assertEquals("test", result.get("name"));
            assertEquals(42L, result.get("value"));
        }

        @Test
        @DisplayName("givenJsonObjectWithBooleanValuesWhenParseJsonObjectThenReturnsCorrectBooleans")
        void givenJsonObjectWithBooleanValuesWhenParseJsonObjectThenReturnsCorrectBooleans() {
            String json = "{\"active\":true,\"deleted\":false}";

            Map<String, Object> result = invokeParseJsonObject(json);

            assertEquals(true, result.get("active"));
            assertEquals(false, result.get("deleted"));
        }

        @Test
        @DisplayName("givenJsonObjectWithNullValueWhenParseJsonObjectThenReturnsNull")
        void givenJsonObjectWithNullValueWhenParseJsonObjectThenReturnsNull() {
            String json = "{\"data\":null}";

            Map<String, Object> result = invokeParseJsonObject(json);

            assertNull(result.get("data"));
        }

        @Test
        @DisplayName("givenJsonObjectWithNestedObjectWhenParseJsonObjectThenReturnsNestedMap")
        void givenJsonObjectWithNestedObjectWhenParseJsonObjectThenReturnsNestedMap() {
            String json = "{\"user\":{\"name\":\"John\",\"age\":30}}";

            Map<String, Object> result = invokeParseJsonObject(json);

            @SuppressWarnings("unchecked")
            Map<String, Object> user = (Map<String, Object>) result.get("user");
            assertEquals("John", user.get("name"));
            assertEquals(30L, user.get("age"));
        }

        @Test
        @DisplayName("givenJsonObjectWithArrayWhenParseJsonObjectThenReturnsArray")
        void givenJsonObjectWithArrayWhenParseJsonObjectThenReturnsArray() {
            String json = "{\"items\":[1,2,3]}";

            Map<String, Object> result = invokeParseJsonObject(json);

            @SuppressWarnings("unchecked")
            List<Object> items = (List<Object>) result.get("items");
            assertEquals(3, items.size());
            assertEquals(1L, items.get(0));
            assertEquals(2L, items.get(1));
            assertEquals(3L, items.get(2));
        }

        @Test
        @DisplayName("givenJsonObjectWithDoubleWhenParseJsonObjectThenReturnsDouble")
        void givenJsonObjectWithDoubleWhenParseJsonObjectThenReturnsDouble() {
            String json = "{\"price\":19.99}";

            Map<String, Object> result = invokeParseJsonObject(json);

            assertEquals(19.99, result.get("price"));
        }

        @Test
        @DisplayName("givenJsonObjectWithNegativeNumberWhenParseJsonObjectThenReturnsNegativeNumber")
        void givenJsonObjectWithNegativeNumberWhenParseJsonObjectThenReturnsNegativeNumber() {
            String json = "{\"offset\":-5}";

            Map<String, Object> result = invokeParseJsonObject(json);

            assertEquals(-5L, result.get("offset"));
        }
    }

    @Nested
    @DisplayName("parseJsonArray()")
    class ParseJsonArrayTests {

        @Test
        @DisplayName("givenEmptyJsonArrayWhenParseJsonArrayThenReturnsEmptyList")
        void givenEmptyJsonArrayWhenParseJsonArrayThenReturnsEmptyList() {
            List<Object> result = invokeParseJsonArray("[]");

            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("givenJsonArrayOfNumbersWhenParseJsonArrayThenReturnsNumberList")
        void givenJsonArrayOfNumbersWhenParseJsonArrayThenReturnsNumberList() {
            String json = "[1,2,3,4,5]";

            List<Object> result = invokeParseJsonArray(json);

            assertEquals(5, result.size());
            assertEquals(1L, result.get(0));
            assertEquals(5L, result.get(4));
        }

        @Test
        @DisplayName("givenJsonArrayOfStringsWhenParseJsonArrayThenReturnsStringList")
        void givenJsonArrayOfStringsWhenParseJsonArrayThenReturnsStringList() {
            String json = "[\"a\",\"b\",\"c\"]";

            List<Object> result = invokeParseJsonArray(json);

            assertEquals(3, result.size());
            assertEquals("a", result.get(0));
            assertEquals("c", result.get(2));
        }

        @Test
        @DisplayName("givenJsonArrayWithMixedTypesWhenParseJsonArrayThenReturnsMixedList")
        void givenJsonArrayWithMixedTypesWhenParseJsonArrayThenReturnsMixedList() {
            String json = "[1,\"text\",true,null,3.14]";

            List<Object> result = invokeParseJsonArray(json);

            assertEquals(5, result.size());
            assertEquals(1L, result.get(0));
            assertEquals("text", result.get(1));
            assertEquals(true, result.get(2));
            assertNull(result.get(3));
            assertEquals(3.14, result.get(4));
        }

        @Test
        @DisplayName("givenJsonArrayWithNestedObjectsWhenParseJsonArrayThenReturnsNestedMaps")
        void givenJsonArrayWithNestedObjectsWhenParseJsonArrayThenReturnsNestedMaps() {
            String json = "[{\"name\":\"first\"},{\"name\":\"second\"}]";

            List<Object> result = invokeParseJsonArray(json);

            assertEquals(2, result.size());
            @SuppressWarnings("unchecked")
            Map<String, Object> first = (Map<String, Object>) result.get(0);
            assertEquals("first", first.get("name"));
            @SuppressWarnings("unchecked")
            Map<String, Object> second = (Map<String, Object>) result.get(1);
            assertEquals("second", second.get("name"));
        }

        @Test
        @DisplayName("givenJsonArrayWithNestedArrayWhenParseJsonArrayThenReturnsNestedArray")
        void givenJsonArrayWithNestedArrayWhenParseJsonArrayThenReturnsNestedArray() {
            String json = "[1,[2,3],4]";

            List<Object> result = invokeParseJsonArray(json);

            assertEquals(3, result.size());
            assertEquals(1L, result.get(0));
            @SuppressWarnings("unchecked")
            List<Object> nested = (List<Object>) result.get(1);
            assertEquals(2, nested.size());
            assertEquals(4L, result.get(2));
        }
    }

    @Nested
    @DisplayName("toJson()")
    class ToJsonTests {

        @Test
        @DisplayName("givenNullWhenToJsonThenReturnsNullString")
        void givenNullWhenToJsonThenReturnsNullString() {
            String result = Server.JsonUtils.toJson(null);

            assertEquals("null", result);
        }

        @Test
        @DisplayName("givenEmptyMapWhenToJsonThenReturnsEmptyObject")
        void givenEmptyMapWhenToJsonThenReturnsEmptyObject() {
            Map<String, Object> map = new java.util.HashMap<>();
            String result = Server.JsonUtils.toJson(map);

            assertEquals("{}", result);
        }

        @Test
        @DisplayName("givenMapWithStringsWhenToJsonThenReturnsCorrectJson")
        void givenMapWithStringsWhenToJsonThenReturnsCorrectJson() {
            Map<String, Object> map = new java.util.LinkedHashMap<>();
            map.put("name", "test");
            map.put("value", "data");

            String result = Server.JsonUtils.toJson(map);

            assertTrue(result.contains("\"name\":\"test\""));
            assertTrue(result.contains("\"value\":\"data\""));
        }

        @Test
        @DisplayName("givenListWhenToJsonThenReturnsCorrectJsonArray")
        void givenListWhenToJsonThenReturnsCorrectJsonArray() {
            List<String> list = new java.util.ArrayList<>();
            list.add("first");
            list.add("second");

            String result = Server.JsonUtils.toJson(list);

            assertEquals("[\"first\",\"second\"]", result);
        }

        @Test
        @DisplayName("givenMapWithNumbersWhenToJsonThenReturnsCorrectJson")
        void givenMapWithNumbersWhenToJsonThenReturnsCorrectJson() {
            Map<String, Object> map = new java.util.LinkedHashMap<>();
            map.put("count", 42);
            map.put("price", 19.99);

            String result = Server.JsonUtils.toJson(map);

            assertTrue(result.contains("\"count\":42"));
            assertTrue(result.contains("\"price\":19.99"));
        }

        @Test
        @DisplayName("givenMapWithBooleanWhenToJsonThenReturnsCorrectJson")
        void givenMapWithBooleanWhenToJsonThenReturnsCorrectJson() {
            Map<String, Object> map = new java.util.LinkedHashMap<>();
            map.put("active", true);
            map.put("deleted", false);

            String result = Server.JsonUtils.toJson(map);

            assertTrue(result.contains("\"active\":true"));
            assertTrue(result.contains("\"deleted\":false"));
        }
    }

    // Вспомогательные методы для вызова приватных методов через рефлексию

    private String invokeExtractJsonObject(String json, int start) {
        try {
            java.lang.reflect.Method method = Server.JsonUtils.class.getDeclaredMethod("extractJsonObject", String.class, int.class);
            method.setAccessible(true);
            return (String) method.invoke(null, json, start);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private String invokeExtractJsonArray(String json, int start) {
        try {
            java.lang.reflect.Method method = Server.JsonUtils.class.getDeclaredMethod("extractJsonArray", String.class, int.class);
            method.setAccessible(true);
            return (String) method.invoke(null, json, start);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> invokeParseJsonObject(String json) {
        try {
            java.lang.reflect.Method method = Server.JsonUtils.class.getDeclaredMethod("parseJsonObject", String.class);
            method.setAccessible(true);
            return (Map<String, Object>) method.invoke(null, json);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    private List<Object> invokeParseJsonArray(String json) {
        try {
            java.lang.reflect.Method method = Server.JsonUtils.class.getDeclaredMethod("parseJsonArray", String.class);
            method.setAccessible(true);
            return (List<Object>) method.invoke(null, json);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
