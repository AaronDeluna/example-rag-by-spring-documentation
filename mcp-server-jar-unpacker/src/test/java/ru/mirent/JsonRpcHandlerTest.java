package ru.mirent;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("JsonRpcHandler - обработка JSON-RPC сообщений MCP")
class JsonRpcHandlerTest {

    private JsonRpcHandler handler;

    @BeforeEach
    void setUp() {
        ToolRegistry mockRegistry = new MockToolRegistry();
        handler = new JsonRpcHandler(mockRegistry);
    }

    @Nested
    @DisplayName("initialize")
    class InitializeTests {

        @Test
        @DisplayName("givenInitializeRequestWhenHandleThenReturnsProtocolVersion()")
        void givenInitializeRequestWhenHandleThenReturnsProtocolVersion() throws IOException {
            String request = "{\"jsonrpc\":\"2.0\",\"id\":0,\"method\":\"initialize\",\"params\":{}}";

            JsonMessage response = handler.handle(request);

            assertNotNull(response, "Response should not be null");
            assertEquals(0, response.id, "Response id should match request id");
            assertNotNull(response.result, "Response should contain result");
            @SuppressWarnings("unchecked")
            Map<String, Object> result = (Map<String, Object>) response.result;
            assertEquals("2024-11-05", result.get("protocolVersion"), "Protocol version should be 2024-11-05");
        }

        @Test
        @DisplayName("givenInitializeRequestWhenHandleThenReturnsServerInfo()")
        void givenInitializeRequestWhenHandleThenReturnsServerInfo() throws IOException {
            String request = "{\"jsonrpc\":\"2.0\",\"id\":1,\"method\":\"initialize\",\"params\":{}}";

            JsonMessage response = handler.handle(request);

            assertNotNull(response);
            @SuppressWarnings("unchecked")
            Map<String, Object> result = (Map<String, Object>) response.result;
            assertNotNull(result.get("serverInfo"), "Result should contain serverInfo");
            @SuppressWarnings("unchecked")
            Map<String, Object> serverInfo = (Map<String, Object>) result.get("serverInfo");
            assertEquals("jar-unpacker", serverInfo.get("name"), "Server name should be jar-unpacker");
            assertEquals("1.0.0", serverInfo.get("version"), "Server version should be 1.0.0");
        }

        @Test
        @DisplayName("givenInitializeRequestWhenHandleThenReturnsCapabilities()")
        void givenInitializeRequestWhenHandleThenReturnsCapabilities() throws IOException {
            String request = "{\"jsonrpc\":\"2.0\",\"id\":2,\"method\":\"initialize\",\"params\":{}}";

            JsonMessage response = handler.handle(request);

            assertNotNull(response);
            @SuppressWarnings("unchecked")
            Map<String, Object> result = (Map<String, Object>) response.result;
            assertNotNull(result.get("capabilities"), "Result should contain capabilities");
            @SuppressWarnings("unchecked")
            Map<String, Object> capabilities = (Map<String, Object>) result.get("capabilities");
            assertNotNull(capabilities.get("tools"), "Capabilities should contain tools");
        }
    }

    @Nested
    @DisplayName("tools/list")
    class ToolsListTests {

        @Test
        @DisplayName("givenToolsListRequestWhenHandleThenReturnsTools()")
        void givenToolsListRequestWhenHandleThenReturnsTools() throws IOException {
            String request = "{\"jsonrpc\":\"2.0\",\"id\":1,\"method\":\"tools/list\",\"params\":{}}";

            JsonMessage response = handler.handle(request);

            assertNotNull(response, "Response should not be null");
            assertEquals(1, response.id, "Response id should match request id");
            assertNotNull(response.result, "Response should contain result");
            @SuppressWarnings("unchecked")
            Map<String, Object> result = (Map<String, Object>) response.result;
            assertNotNull(result.get("tools"), "Result should contain tools");
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> tools = (List<Map<String, Object>>) result.get("tools");
            assertTrue(tools.isEmpty(), "Mock registry should return empty tools list");
        }
    }

    @Nested
    @DisplayName("tools/call")
    class ToolsCallTests {

        @Test
        @DisplayName("givenToolCallWhenHandleThenReturnsMockResult()")
        void givenToolCallWhenHandleThenReturnsMockResult() throws IOException {
            String request = "{\"jsonrpc\":\"2.0\",\"id\":3,\"method\":\"tools/call\",\"params\":{\"name\":\"test_tool\",\"arguments\":{\"key\":\"value\"}}}";

            JsonMessage response = handler.handle(request);

            assertNotNull(response, "Response should not be null");
            assertEquals(3, response.id, "Response id should match request id");
            assertNotNull(response.result, "Response should contain result");
            @SuppressWarnings("unchecked")
            Map<String, Object> result = (Map<String, Object>) response.result;
            assertEquals("mock", result.get("result"), "Mock registry should return mock result");
        }
    }

    @Nested
    @DisplayName("notifications/initialized")
    class NotificationTests {

        @Test
        @DisplayName("givenInitializedNotificationWhenHandleThenReturnsNull()")
        void givenInitializedNotificationWhenHandleThenReturnsNull() throws IOException {
            String request = "{\"jsonrpc\":\"2.0\",\"method\":\"notifications/initialized\",\"params\":{}}";

            JsonMessage response = handler.handle(request);

            assertNull(response, "Notification should not return response");
        }
    }

    @Nested
    @DisplayName("unknown method")
    class UnknownMethodTests {

        @Test
        @DisplayName("givenUnknownMethodWhenHandleThenReturnsError()")
        void givenUnknownMethodWhenHandleThenReturnsError() throws IOException {
            String request = "{\"jsonrpc\":\"2.0\",\"id\":5,\"method\":\"unknown/method\",\"params\":{}}";

            JsonMessage response = handler.handle(request);

            assertNotNull(response, "Response should not be null");
            assertEquals(5, response.id, "Response id should match request id");
            assertNotNull(response.error, "Response should contain error");
            @SuppressWarnings("unchecked")
            Map<String, Object> error = (Map<String, Object>) response.error;
            assertEquals(-32603, error.get("code"), "Error code should be -32603");
            assertTrue(((String) error.get("message")).contains("Unknown method"), "Error message should mention unknown method");
        }
    }

    @Nested
    @DisplayName("invalid JSON")
    class InvalidJsonTests {

        @Test
        @DisplayName("givenEmptyJsonWhenHandleThenReturnsError()")
        void givenEmptyJsonWhenHandleThenReturnsError() throws IOException {
            String invalidJson = "{}";

            JsonMessage response = handler.handle(invalidJson);

            assertNotNull(response, "Response should not be null");
            assertNotNull(response.error, "Response should contain error for invalid request");
        }
    }

    // Mock-реализация реестра инструментов для тестов
    private static class MockToolRegistry implements ToolRegistry {
        @Override
        public List<Map<String, Object>> getTools() {
            return new ArrayList<>();
        }

        @Override
        public Object callTool(String name, Map<String, Object> arguments) {
            return Map.of("result", "mock");
        }
    }
}
