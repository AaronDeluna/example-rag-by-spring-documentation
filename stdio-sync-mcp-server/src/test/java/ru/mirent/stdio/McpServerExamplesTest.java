package ru.mirent.stdio;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.client.transport.ServerParameters;
import io.modelcontextprotocol.client.transport.StdioClientTransport;
import io.modelcontextprotocol.spec.McpSchema.*;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;

/**
 * Тест для демонстрации вызова всех методов MCP-сервера.
 * Результат сохраняется в файл MCP_SERVER_EXAMPLES.md в корне проекта.
 */
public class McpServerExamplesTest {
    private static final Logger LOG = LoggerFactory.getLogger(McpServerExamplesTest.class);
    private static final String OUTPUT_FILE = "MCP_SERVER_EXAMPLES.md";
    private static StringBuilder output = new StringBuilder();

    @BeforeAll
    static void beforeAll() {
        output.append("# Примеры вызова методов MCP-сервера (STDIO)\n\n");
        output.append("**Дата выполнения:** ").append(java.time.LocalDateTime.now()).append("\n\n");
        output.append("---\n\n");
    }

    @AfterAll
    static void afterAll() throws IOException {
        output.append("---\n\n");
        output.append("## Заключение\n\n");
        output.append("Все примеры успешно выполнены. MCP-сервер предоставляет 3 инструмента:\n");
        output.append("1. `getDateTime` — получение текущей даты и времени\n");
        output.append("2. `getFile` — чтение файла\n");
        output.append("3. `generateDocs` — генерация описания кода\n\n");

        File outputFile = new File(OUTPUT_FILE);
        try (FileWriter writer = new FileWriter(outputFile)) {
            writer.write(output.toString());
        }
        LOG.info("Результат сохранён в файл: {}", outputFile.getAbsolutePath());
    }

    @Test
    void example1ListTools() {
        output.append("## 1. Получение списка доступных инструментов\n\n");
        output.append("```java\n");
        output.append("ListToolsResult tools = mcpSyncClient.listTools();\n");
        output.append("```\n\n");
        output.append("**Результат:**\n\n");

        File jarFile = new File("target/stdio-sync-mcp-server-1.0-SNAPSHOT.jar");
        ServerParameters serverParameters = ServerParameters
                .builder("java")
                .args("-jar", jarFile.getAbsolutePath())
                .build();

        StdioClientTransport transport = new StdioClientTransport(serverParameters, new ObjectMapper());
        try (McpSyncClient client = McpClient.sync(transport).build()) {
            client.initialize();
            ListToolsResult result = client.listTools();

            output.append("| Инструмент | Описание |\n");
            output.append("|------------|----------|\n");
            for (Tool tool : result.tools()) {
                output.append(String.format("| `%s` | %s |\n", tool.name(), tool.description().replace("\n", " ")));
            }
            output.append("\n");
            LOG.info("Список инструментов получен");
        }
    }

    @Test
    void example2CallGetDateTime() {
        output.append("## 2. Вызов метода getDateTime()\n\n");
        output.append("```java\n");
        output.append("CallToolRequest request = new CallToolRequest(\"getDateTime\", Map.of());\n");
        output.append("CallToolResult result = mcpSyncClient.callTool(request);\n");
        output.append("```\n\n");
        output.append("**Результат:**\n\n");

        File jarFile = new File("target/stdio-sync-mcp-server-1.0-SNAPSHOT.jar");
        ServerParameters serverParameters = ServerParameters
                .builder("java")
                .args("-jar", jarFile.getAbsolutePath())
                .build();

        StdioClientTransport transport = new StdioClientTransport(serverParameters, new ObjectMapper());
        try (McpSyncClient client = McpClient.sync(transport).build()) {
            client.initialize();
            CallToolRequest request = new CallToolRequest("getDateTime", Map.of());
            CallToolResult result = client.callTool(request);

            for (Content content : result.content()) {
                output.append("```\n").append(content.toString()).append("\n```\n\n");
            }
            LOG.info("Метод getDateTime() вызван");
        }
    }

    @Test
    void example3CallGetFile() {
        output.append("## 3. Вызов метода getFile()\n\n");
        output.append("```java\n");
        output.append("CallToolRequest request = new CallToolRequest(\"getFile\", Map.of());\n");
        output.append("CallToolResult result = mcpSyncClient.callTool(request);\n");
        output.append("```\n\n");
        output.append("**Результат:**\n\n");

        File jarFile = new File("target/stdio-sync-mcp-server-1.0-SNAPSHOT.jar");
        ServerParameters serverParameters = ServerParameters
                .builder("java")
                .args("-jar", jarFile.getAbsolutePath())
                .build();

        StdioClientTransport transport = new StdioClientTransport(serverParameters, new ObjectMapper());
        try (McpSyncClient client = McpClient.sync(transport).build()) {
            client.initialize();
            CallToolRequest request = new CallToolRequest("getFile", Map.of());
            CallToolResult result = client.callTool(request);

            for (Content content : result.content()) {
                output.append("```\n").append(content.toString()).append("\n```\n\n");
            }
            LOG.info("Метод getFile() вызван");
        }
    }

    @Test
    void example4CallGenerateDocs() {
        output.append("## 4. Вызов метода generateDocs(programmingLanguage=\"Java\")\n\n");
        output.append("```java\n");
        output.append("Map<String, Object> params = Map.of(\"programmingLanguage\", \"Java\");\n");
        output.append("CallToolRequest request = new CallToolRequest(\"generateDocs\", params);\n");
        output.append("CallToolResult result = mcpSyncClient.callTool(request);\n");
        output.append("```\n\n");
        output.append("**Результат:**\n\n");

        File jarFile = new File("target/stdio-sync-mcp-server-1.0-SNAPSHOT.jar");
        ServerParameters serverParameters = ServerParameters
                .builder("java")
                .args("-jar", jarFile.getAbsolutePath())
                .build();

        StdioClientTransport transport = new StdioClientTransport(serverParameters, new ObjectMapper());
        try (McpSyncClient client = McpClient.sync(transport).build()) {
            client.initialize();
            CallToolRequest request = new CallToolRequest("generateDocs", Map.of("programmingLanguage", "Java"));
            CallToolResult result = client.callTool(request);

            for (Content content : result.content()) {
                output.append("```\n").append(content.toString()).append("\n```\n\n");
            }
            LOG.info("Метод generateDocs() вызван");
        }
    }
}
