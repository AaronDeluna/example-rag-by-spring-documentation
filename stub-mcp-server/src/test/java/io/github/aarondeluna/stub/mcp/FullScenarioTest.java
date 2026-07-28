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

        // Шаг 2: повторный анализ уже с обогащённым контекстом — данных достаточно.
        // Дозапрос закрыл пробел, поэтому форсируем ветку enough.
        String enrichedClusters = clustersCallResult + additionalLogs;
        String secondAnalyzeResult = analyzeClustersTool.analyzeClusters(enrichedClusters, "enough");
        assertThat(secondAnalyzeResult).contains("\"enough_info\": true");
        assertThat(secondAnalyzeResult).contains("\"enrichment_needed\": false");

        // Шаг 3: по обогащённым кластерам ищем готовое решение в базе знаний — оно найдено.
        String searchResult = searchStorageTool.searchStorage(secondAnalyzeResult, "found");
        assertThat(searchResult).startsWith("FOUND");
        assertThat(searchResult).contains("solution:");

        // Шаг 4: собираем итоговый отчёт с классификацией и планом исправления.
        String report = aggregateReportTool.aggregateReport(secondAnalyzeResult);
        assertThat(report).contains("\"classification\": \"bugfix\"");
        assertThat(report).contains("\"fix_plan\"");
    }
}
