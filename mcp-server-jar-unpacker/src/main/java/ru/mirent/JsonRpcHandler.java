package ru.mirent;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Обработчик JSON-RPC 2.0 сообщений протокола MCP.
 * Обрабатывает: initialize, notifications/initialized, tools/list, tools/call
 */
public class JsonRpcHandler {

    private final ToolRegistry toolRegistry;

    public JsonRpcHandler(ToolRegistry toolRegistry) {
        this.toolRegistry = toolRegistry;
    }

    /**
     * Обработка входящего JSON-RPC сообщения
     */
    public JsonMessage handle(String json) throws IOException {
        JsonMessage msg = parseJson(json);

        if ("initialize".equals(msg.method)) {
            return handleInitialize(msg);
        } else if ("notifications/initialized".equals(msg.method)) {
            return null; // уведомление, ответ не требуется
        } else if ("tools/list".equals(msg.method)) {
            return handleListTools(msg);
        } else if ("tools/call".equals(msg.method)) {
            return handleCallTool(msg);
        } else {
            return createError(msg.id, "Unknown method: " + msg.method);
        }
    }

    private JsonMessage handleInitialize(JsonMessage msg) {
        Map<String, Object> result = new HashMap<>();
        result.put("protocolVersion", "2024-11-05");

        Map<String, Object> serverInfo = new HashMap<>();
        serverInfo.put("name", "jar-unpacker");
        serverInfo.put("version", "1.0.0");
        result.put("serverInfo", serverInfo);

        Map<String, Object> capabilities = new HashMap<>();
        capabilities.put("tools", new HashMap<String, Object>());
        result.put("capabilities", capabilities);

        return createResponse(msg.id, result);
    }

    private JsonMessage handleListTools(JsonMessage msg) {
        List<Map<String, Object>> tools = toolRegistry.getTools();
        Map<String, Object> result = new HashMap<>();
        result.put("tools", tools);
        return createResponse(msg.id, result);
    }

    private JsonMessage handleCallTool(JsonMessage msg) {
        Map<String, Object> params = (Map<String, Object>) msg.params;
        String name = (String) params.get("name");
        Map<String, Object> arguments = (Map<String, Object>) params.get("arguments");

        Object result = toolRegistry.callTool(name, arguments);
        return createResponse(msg.id, result);
    }

    private JsonMessage createResponse(Object id, Object result) {
        JsonMessage response = new JsonMessage();
        response.id = id;
        response.result = result;
        return response;
    }

    private JsonMessage createError(Object id, String message) {
        JsonMessage error = new JsonMessage();
        error.id = id;
        error.error = Map.of("code", -32603, "message", message);
        return error;
    }

    private JsonMessage parseJson(String json) throws IOException {
        try {
            JsonUtils.Parsed parsed = JsonUtils.parse(json);
            JsonMessage msg = new JsonMessage();
            msg.id = parsed.id;
            msg.method = parsed.method;
            msg.params = parsed.params;
            return msg;
        } catch (Exception e) {
            throw new IOException("Failed to parse JSON: " + e.getMessage(), e);
        }
    }

    /**
     * Сериализация JsonMessage в JSON
     */
    public String toJson(JsonMessage msg) {
        return JsonUtils.toJson(msg);
    }
}
