package io.github.aarondeluna.stub.mcp.tools;

import io.github.aarondeluna.stub.mcp.fixtures.ClusterFixtures;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

/**
 * Инструмент {@code cluster_artifact}: кластеризует ошибки из артефакта логов.
 *
 * <p>Заглушка: возвращает детерминированную фикстуру, выбранную по подстроке
 * в {@code artifact_uri} (npe / timeout / unknown).
 */
@Component
public class ClusterArtifactTool {

    private static final Logger log = LoggerFactory.getLogger(ClusterArtifactTool.class);

    @Tool(name = "cluster_artifact",
            description = "Кластеризует ошибки из артефакта логов и возвращает JSON со списком кластеров")
    public String clusterArtifact(
            @ToolParam(description = "URI артефакта с логами, например s3://runs/npe-run/logs.tar.gz")
            String artifact_uri) {
        ClusterFixtures.Profile profile = ClusterFixtures.profileForUri(artifact_uri);
        log.info("[cluster_artifact] artifact_uri={} -> профиль {}", artifact_uri, profile);
        return ClusterFixtures.forUri(artifact_uri);
    }
}
