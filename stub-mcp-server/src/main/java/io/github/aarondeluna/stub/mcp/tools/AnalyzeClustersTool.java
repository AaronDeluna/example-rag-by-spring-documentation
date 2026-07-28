package io.github.aarondeluna.stub.mcp.tools;

import io.github.aarondeluna.stub.mcp.fixtures.AnalysisFixtures;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * Инструмент {@code analyze_clusters}: анализирует кластеры и решает,
 * достаточно ли данных для вывода или нужен дозапрос в другие MCP.
 *
 * <p>Заглушка. Поведение по умолчанию: если во входе встречается
 * {@code NullPointerException} или {@code IllegalStateException} — ветка
 * {@code enough}, иначе — {@code need_more}. Аргумент {@code scenario}
 * ({@code enough}|{@code need_more}) перекрывает поведение по умолчанию.
 */
@Component
public class AnalyzeClustersTool {

    private static final Logger log = LoggerFactory.getLogger(AnalyzeClustersTool.class);

    @Tool(name = "analyze_clusters",
            description = "Анализирует кластеры ошибок; возвращает JSON с признаком enough_info и списком недостающих данных")
    public String analyzeClusters(
            @ToolParam(description = "Строка с JSON кластеров (обычно вывод cluster_artifact)")
            String clusters_json,
            @ToolParam(required = false,
                    description = "Форсировать ветку: enough | need_more. Если не задано — определяется по содержимому")
            String scenario) {

        String branch = resolveBranch(clusters_json, scenario);
        log.info("[analyze_clusters] scenario={} inputLen={} -> ветка {}",
                scenario, clusters_json == null ? 0 : clusters_json.length(), branch);

        return "enough".equals(branch) ? AnalysisFixtures.enough() : AnalysisFixtures.needMore();
    }

    /** Выбирает ветку: явный scenario приоритетнее эвристики по содержимому. */
    private String resolveBranch(String clustersJson, String scenario) {
        if (StringUtils.hasText(scenario)) {
            return "enough".equalsIgnoreCase(scenario.trim()) ? "enough" : "need_more";
        }
        String input = clustersJson == null ? "" : clustersJson;
        boolean enough = input.contains("NullPointerException") || input.contains("IllegalStateException");
        return enough ? "enough" : "need_more";
    }
}
