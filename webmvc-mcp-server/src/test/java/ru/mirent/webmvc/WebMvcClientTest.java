package ru.mirent.webmvc;

import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.client.transport.HttpClientSseClientTransport;
import io.modelcontextprotocol.spec.McpSchema.ListToolsResult;
import io.modelcontextprotocol.spec.McpSchema.Tool;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import io.modelcontextprotocol.spec.McpSchema.CallToolRequest;
import io.modelcontextprotocol.spec.McpSchema.Content;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Optional;

import static java.lang.String.format;

/**
 * Тест в модуле MCP-сервера, который имитирует клиента.
 * Предварительно необходимо собрать JAR MCP-сервера.
 */
public class WebMvcClientTest {
    private static final Logger LOG = LoggerFactory.getLogger(WebMvcClientTest.class);

    @Test
    void showInstrumentsListTest() {
        HttpClientSseClientTransport httpClientSseClientTransport = HttpClientSseClientTransport
                .builder("http://127.0.0.1:8080/")
                .build();
        ListToolsResult listToolsResult;
        try (McpSyncClient mcpSyncClient = McpClient.sync(httpClientSseClientTransport).build()) {
            mcpSyncClient.initialize();
            listToolsResult = mcpSyncClient.listTools();
            Assertions.assertFalse(listToolsResult.tools().isEmpty(), "На MCP-сервере отсутствуют инструменты");
            Assertions.assertEquals(3, listToolsResult.tools().size(), "Отличается количество инструментов");

            LOG.info("Доступные инструменты на MCP-сервере:");
            for (Tool tool : listToolsResult.tools()) {
                LOG.info(" {} - {}", tool.name(), tool.description());
                Map<String, Object> params = tool.inputSchema().properties();
                if (!params.isEmpty()) {
                    LOG.info("  Параметры инструмента:");
                    for (Map.Entry<String, Object> entry : tool.inputSchema().properties().entrySet()) {
                        LOG.info("   {} = {}", entry.getKey(), entry.getValue());
                    }
                }
            }
        }
    }

    @Test
    void callToolFromMcpServerTest() {
        HttpClientSseClientTransport httpClientSseClientTransport = HttpClientSseClientTransport
                .builder("http://127.0.0.1:8080/")
                .build();
        ListToolsResult listToolsResult;
        try (McpSyncClient mcpSyncClient = McpClient.sync(httpClientSseClientTransport).build()) {
            mcpSyncClient.initialize();
            listToolsResult = mcpSyncClient.listTools();

            String findToolName = "getDateTime";
            Optional<Tool> optionalTool = listToolsResult
                    .tools()
                    .stream()
                    .filter(t -> t.name().equals(findToolName))
                    .findFirst();
            Assertions.assertTrue(optionalTool.isPresent(), format("Не найден инструмент с именем: %s", findToolName));

            Tool tool = optionalTool.get();
            String toolName = tool.name();
            CallToolRequest callToolRequest = new CallToolRequest(toolName, Map.of());
            CallToolResult callToolResult = mcpSyncClient.callTool(callToolRequest);
            Assertions.assertEquals(1, callToolResult.content().size());

            LOG.info("Результат вызова инструмента:");
            for (Content content : callToolResult.content()) {
                LOG.info(content.toString());
            }
        }
    }
}
