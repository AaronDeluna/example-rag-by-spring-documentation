package ru.mirent.webflux;

import io.modelcontextprotocol.client.McpAsyncClient;
import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.client.transport.HttpClientSseClientTransport;
import io.modelcontextprotocol.spec.McpSchema.CallToolRequest;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import io.modelcontextprotocol.spec.McpSchema.Content;
import io.modelcontextprotocol.spec.McpSchema.ListToolsResult;
import io.modelcontextprotocol.spec.McpSchema.Tool;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Map;
import java.util.Optional;

import static java.lang.String.format;

/**
 * Тест в модуле MCP-сервера, который имитирует клиента.
 * Предварительно необходимо собрать JAR MCP-сервера и запустить его.
 * Используется асинхронный клиент для Spring WebFlux.
 */
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@Import(WebFluxClientTest.TestConfig.class)
public class WebFluxClientTest {
    private static final Logger LOG = LoggerFactory.getLogger(WebFluxClientTest.class);

    @LocalServerPort
    private String port;

    private String baseUrl= null;

    @BeforeEach
    void setUp() {
        if (baseUrl == null) {
            baseUrl = "http://127.0.0.1:" + port;
        }
    }

    /**
     * Синхронный тест оставлен для примера. Он проходит, но для WebFlux сервера требуется асинхронный клиент.
     */
    @Test
    void showInstrumentsListSyncClientTest() {
        HttpClientSseClientTransport httpClientSseClientTransport = HttpClientSseClientTransport
                .builder(baseUrl)
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
    void showInstrumentsListAsyncClientTest() {
        HttpClientSseClientTransport httpClientSseClientTransport = HttpClientSseClientTransport
                .builder(baseUrl)
                .build();

        McpAsyncClient mcpAsyncClient = McpClient.async(httpClientSseClientTransport).build();
        mcpAsyncClient.initialize().block(Duration.ofSeconds(10L));
        ListToolsResult listToolsResult = mcpAsyncClient.listTools().block(Duration.ofSeconds(10L));
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

    @Test
    void callToolFromMcpServerTest() {
        HttpClientSseClientTransport httpClientSseClientTransport = HttpClientSseClientTransport
                .builder(baseUrl)
                .build();

        McpAsyncClient mcpAsyncClient = McpClient.async(httpClientSseClientTransport).build();
        mcpAsyncClient.initialize().block(Duration.ofSeconds(10L));
        ListToolsResult listToolsResult = mcpAsyncClient.listTools().block(Duration.ofSeconds(10L));

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
            CallToolResult callToolResult = mcpAsyncClient.callTool(callToolRequest).block(Duration.ofSeconds(10L));
            Assertions.assertEquals(1, callToolResult.content().size());

            LOG.info("Результат вызова инструмента:");
            for (Content content : callToolResult.content()) {
                LOG.info(content.toString());
            }
    }

    @Component
    @TestConfiguration
    static class TestConfig { }
}
