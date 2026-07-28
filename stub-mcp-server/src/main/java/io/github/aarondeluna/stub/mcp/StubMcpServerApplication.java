package io.github.aarondeluna.stub.mcp;

import io.github.aarondeluna.stub.mcp.tools.AggregateReportTool;
import io.github.aarondeluna.stub.mcp.tools.AnalyzeClustersTool;
import io.github.aarondeluna.stub.mcp.tools.ClusterArtifactTool;
import io.github.aarondeluna.stub.mcp.tools.SearchStorageTool;
import org.springframework.ai.support.ToolCallbacks;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

/**
 * Точка входа stub-MCP-сервера.
 *
 * <p>Поднимает MCP-сервер поверх stdio-транспорта и регистрирует четыре
 * инструмента-заглушки. Ответы детерминированные (фикстуры), внешних вызовов нет —
 * сервер нужен для сквозной отладки флоу агента и разработки скиллов.
 */
@SpringBootApplication
public class StubMcpServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(StubMcpServerApplication.class, args);
    }

    /**
     * Регистрирует все stub-инструменты как MCP-туллы.
     */
    @Bean
    public ToolCallbackProvider stubTools(ClusterArtifactTool clusterArtifactTool,
                                          AnalyzeClustersTool analyzeClustersTool,
                                          SearchStorageTool searchStorageTool,
                                          AggregateReportTool aggregateReportTool) {
        ToolCallback[] callbacks = ToolCallbacks.from(
                clusterArtifactTool,
                analyzeClustersTool,
                searchStorageTool,
                aggregateReportTool);
        return ToolCallbackProvider.from(callbacks);
    }
}
