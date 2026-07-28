package io.github.aarondeluna.stub.mcp;

import io.github.aarondeluna.stub.mcp.tools.AggregateReportTool;
import io.github.aarondeluna.stub.mcp.tools.AnalyzeClustersTool;
import io.github.aarondeluna.stub.mcp.tools.ClusterArtifactTool;
import io.github.aarondeluna.stub.mcp.tools.SearchStorageTool;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class FullScenarioTest {

    private final ClusterArtifactTool clusterArtifactTool = new ClusterArtifactTool();
    private final AnalyzeClustersTool analyzeClustersTool = new AnalyzeClustersTool();
    private final SearchStorageTool searchStorageTool = new SearchStorageTool();
    private final AggregateReportTool aggregateReportTool = new AggregateReportTool();

    @DisplayName("Тест проверки флоу с эмуляцией вызовов внутри скилла")
    @Test
    void jenkensErrorNeedInfoTest() {
        String clustersCallResult = clusterArtifactTool.clusterArtifact("s3://runs/jenkins-run/logs.tar.gz");
        assertThat(clustersCallResult).contains("WARN [app] Connection pool exhausted, retrying attempt=<*>");

        String analizeCallResult = analyzeClustersTool.analyzeClusters(clustersCallResult, "");
        assertThat(analizeCallResult).contains("\"need\": \"trace-id логи из payment-service\"");

        // Оркестратор (CLI Agent) на основе инструкции в скилле вызывает сторонние MCP-сервере
        String additionalLogs = """
                Дополнительный контекст, полученный от MCP сервера
                """;
        // TODO Реализовать последующие шаги
    }
}
