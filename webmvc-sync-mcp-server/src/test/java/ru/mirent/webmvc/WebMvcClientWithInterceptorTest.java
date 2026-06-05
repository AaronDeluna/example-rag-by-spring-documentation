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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.stereotype.Component;
import ru.mirent.webmvc.interceptor.InterceptableHttpClient;
import ru.mirent.webmvc.interceptor.onError;
import ru.mirent.webmvc.interceptor.onRequest;
import ru.mirent.webmvc.interceptor.onResponse;

import java.util.Map;
import java.util.Optional;

import static java.lang.String.format;

/**
 * TODO: Выполнить рефакторнг, т.к. не происходит сбора тел запросо ви ответов ка ожидается.
 */
public class WebMvcClientWithInterceptorTest {
    private static final Logger LOG = LoggerFactory.getLogger(WebMvcClientTest.class);

    @LocalServerPort
    private String port;

    private String baseUrl;

    @BeforeEach
    void setUp() {
        if (baseUrl == null) {
            baseUrl = "http://127.0.0.1:8080";
        }
    }

    @Test
    void callToolFromMcpServerTest() {
        HttpClientSseClientTransport httpClientSseClientTransport = HttpClientSseClientTransport
                .builder(baseUrl)
                .clientBuilder(InterceptableHttpClient
                        .builder()
                        .interceptor(
                                new onRequest(),
                                new onResponse(),
                                new onError()
                        ))
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

    @Component
    @TestConfiguration
    static class TestConfig { }
}
