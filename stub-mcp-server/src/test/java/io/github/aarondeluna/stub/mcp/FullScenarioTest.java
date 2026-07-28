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

    /**
     * Сценарий A1: известное событие найдено в Хранилище.
     *
     * <p>Счастливый путь: кластеризатор → поисковик (Нашёл: Да) → агрегатор.
     * Анализатор не вызывается — событие уже знакомо.
     */
    @DisplayName("Сценарий A1: известное событие найдено в Хранилище — без анализа")
    @Test
    void scenarioAKnownEventFoundInStorage() {
        // Кластеризация артефакта с известной ошибкой (NPE в OrderService).
        String clusters = clusterArtifactTool.clusterArtifact("s3://runs/npe-run/logs.tar.gz");
        assertThat(clusters).contains("NullPointerException at OrderService.calcTotal");

        // Поисковик находит совпадение в Хранилище — доанализ не нужен.
        String searchResult = searchStorageTool.searchStorage(clusters, null);
        assertThat(searchResult).startsWith("FOUND");
        assertThat(searchResult).contains("match_id: kb-042");

        // Агрегатор сразу собирает отчёт по известному решению.
        String report = aggregateReportTool.aggregateReport(clusters);
        assertThat(report).contains("\"classification\": \"bugfix\"");
        assertThat(report).contains("\"fix_plan\"");
    }

    /**
     * Сценарий A2: новое событие, данных достаточно.
     *
     * <p>Кластеризатор → поисковик (Нашёл: Нет) → анализатор (Достаточно: Да) →
     * агрегатор. Петля дозапроса не запускается.
     */
    @DisplayName("Сценарий A2: новое событие, данных достаточно — отчёт без петли дозапроса")
    @Test
    void scenarioANewEventWithEnoughDataProducesBugfixReport() {
        // Новая ошибка (ISE в InventoryService), которой ещё нет в Хранилище.
        String clusters = clusterArtifactTool.clusterArtifact("s3://runs/ise-run/logs.tar.gz");
        assertThat(clusters).contains("IllegalStateException at InventoryService.reserve");

        // Поисковик не находит совпадения — событие новое.
        String searchResult = searchStorageTool.searchStorage(clusters, null);
        assertThat(searchResult).isEqualTo("NOT_FOUND");

        // Анализатор решает, что данных в кластерах достаточно.
        String analyzeResult = analyzeClustersTool.analyzeClusters(clusters, null);
        assertThat(analyzeResult).contains("\"enough_info\": true");

        // Агрегатор собирает итоговый отчёт по новому событию.
        String report = aggregateReportTool.aggregateReport(clusters);
        assertThat(report).contains("\"classification\": \"bugfix\"");
    }

    /**
     * Сценарий A3 (петля исчерпана): данных так и не хватило.
     *
     * <p>Кластеризатор → поисковик (Нет) → анализатор (Достаточно: Нет) → добор логов
     * через Оркестратор, но контекст не проясняется. По достижении лимита итераций
     * фиксируем результат как инцидент без готового фикса.
     */
    @DisplayName("Сценарий A3 (петля исчерпана): данных так и не хватило — инцидент")
    @Test
    void scenarioANewEventDataNeverSufficientEndsAsIncident() {
        // Неизвестная ошибка без внятной сигнатуры.
        String clusters = clusterArtifactTool.clusterArtifact("s3://runs/unknown-run/logs.tar.gz");
        assertThat(clusters).contains("Unknown error in worker pool");

        // Поисковик ничего не находит.
        String searchResult = searchStorageTool.searchStorage(clusters, null);
        assertThat(searchResult).isEqualTo("NOT_FOUND");

        // Анализатор: данных не хватает — запускается петля дозапроса.
        String firstAnalyze = analyzeClustersTool.analyzeClusters(clusters, null);
        assertThat(firstAnalyze).contains("\"enrichment_needed\": true");

        // Оркестратор добирает логи по смежным MCP, но они не проясняют причину.
        String additionalLogs = "Доп. логи из смежных MCP без новой сигнатуры";
        String enriched = clusters + "\n" + additionalLogs;
        String secondAnalyze = analyzeClustersTool.analyzeClusters(enriched, null);
        assertThat(secondAnalyze).contains("\"enrichment_needed\": true");

        // Достигнут лимит итераций петли — фиксируем как инцидент.
        String report = aggregateReportTool.aggregateReport(clusters);
        assertThat(report).contains("\"classification\": \"incident\"");
    }

    /**
     * Сценарий B: инсайт за период.
     *
     * <p>Без артефакта и без кластеризации: агрегатор читает накопленные в Хранилище
     * события прошлых прогонов и сводит их в HTML-сводку.
     */
    @DisplayName("Сценарий B: инсайт за период — агрегатор читает Хранилище, без кластеризации")
    @Test
    void scenarioBPeriodInsightReadFromStorage() {
        // Запрос без артефакта: «предоставить инсайт за период».
        String periodRequest = """
                { "request": "предоставить инсайт за период", "period": "2026-07-01..2026-07-28" }
                """;
        String insight = aggregateReportTool.aggregateReport(periodRequest);

        // Возвращается HTML-сводка, собранная из накопленных в Хранилище событий.
        assertThat(insight).contains("<html>");
        assertThat(insight).contains("Инсайт за период");
        assertThat(insight).doesNotContain("\"classification\"");
    }

    /**
     * Edge-case: локальный self-heal поисковика по кривому JSON.
     *
     * <p>«Цикл обработки JSON с ошибками» из доки: поисковик не должен падать на
     * невалидном JSON и обязан отдавать детерминированный ответ.
     */
    @DisplayName("Edge-case: поисковик устойчив к кривому JSON и не падает")
    @Test
    void scenarioASearcherSelfHealsMalformedJson() {
        // Битый JSON, но сигнатура в тексте есть — поисковик всё равно матчит.
        String brokenButHasSignature = "{ broken json ,,, NullPointerException at OrderService.calcTotal ";
        String found = searchStorageTool.searchStorage(brokenButHasSignature, null);
        assertThat(found).startsWith("FOUND");

        // Битый JSON без сигнатуры — детерминированный NOT_FOUND, без исключений.
        String brokenNoSignature = "}{ not a json at all ??? ";
        String notFound = searchStorageTool.searchStorage(brokenNoSignature, null);
        assertThat(notFound).isEqualTo("NOT_FOUND");
    }
}
