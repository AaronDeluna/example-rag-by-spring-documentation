package io.github.aarondeluna.stub.mcp;

import io.github.aarondeluna.stub.mcp.tools.AggregateReportTool;
import io.github.aarondeluna.stub.mcp.tools.AnalyzeClustersTool;
import io.github.aarondeluna.stub.mcp.tools.ClusterArtifactTool;
import io.github.aarondeluna.stub.mcp.tools.SearchStorageTool;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Smoke-тесты stub-инструментов: вызывают методы туллов напрямую (без подъёма
 * HTTP/stdio-сервера) и проверяют детерминированные ответы на нескольких фикстурах.
 */
@Tag("smoke")
class SmokeTests {

    private final ClusterArtifactTool clusterArtifactTool = new ClusterArtifactTool();
    private final AnalyzeClustersTool analyzeClustersTool = new AnalyzeClustersTool();
    private final SearchStorageTool searchStorageTool = new SearchStorageTool();
    private final AggregateReportTool aggregateReportTool = new AggregateReportTool();

    @Test
    void clusterArtifact_npeUri_returnsNpeCluster() {
        String response = clusterArtifactTool.clusterArtifact("s3://runs/npe-run/logs.tar.gz");
        assertThat(response).contains("NullPointerException");
    }

    @Test
    void analyzeClusters_npeInput_isEnough() {
        String npeClusters = clusterArtifactTool.clusterArtifact("s3://runs/npe-run/logs.tar.gz");
        String response = analyzeClustersTool.analyzeClusters(npeClusters, null);
        assertThat(response).contains("\"enough_info\": true");
    }

    @Test
    void analyzeClusters_timeoutInput_needsMore() {
        String timeoutClusters = clusterArtifactTool.clusterArtifact("s3://runs/timeout-run/logs.tar.gz");
        String response = analyzeClustersTool.analyzeClusters(timeoutClusters, null);
        assertThat(response).contains("\"enrichment_needed\": true");
    }

    @Test
    void searchStorage_npeInput_found() {
        String npeClusters = clusterArtifactTool.clusterArtifact("s3://runs/npe-run/logs.tar.gz");
        String response = searchStorageTool.searchStorage(npeClusters, null);
        assertThat(response).startsWith("FOUND");
    }

    @Test
    void searchStorage_unknownInput_notFound() {
        String unknownClusters = clusterArtifactTool.clusterArtifact("s3://runs/unknown-run/logs.tar.gz");
        String response = searchStorageTool.searchStorage(unknownClusters, null);
        assertThat(response).isEqualTo("NOT_FOUND");
    }

    @Test
    void aggregateReport_npeInput_isBugfix() {
        String npeClusters = clusterArtifactTool.clusterArtifact("s3://runs/npe-run/logs.tar.gz");
        String response = aggregateReportTool.aggregateReport(npeClusters);
        assertThat(response).contains("\"classification\": \"bugfix\"");
    }
}
