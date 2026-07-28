package io.github.aarondeluna.stub.mcp.fixtures;

/**
 * Хардкоженные фикстуры для инструмента {@code analyze_clusters}.
 *
 * <p>Ветка {@code enough} — данных достаточно, кластеры обогащены.
 * Ветка {@code need_more} — нужны дополнительные данные из других MCP.
 *
 * <p>Чтобы заменить заглушку на реальную логику — правьте методы этого класса.
 */
public final class AnalysisFixtures {

    private AnalysisFixtures() {
    }

    /** Данных достаточно: кластеры обогащены, дополнительных запросов не требуется. */
    public static String enough() {
        return """
                {
                  "enrichment_needed": false,
                  "enough_info": true,
                  "enriched_clusters": [
                    {
                      "cluster_id": "cl-001",
                      "signature": "java.lang.NullPointerException at OrderService.calcTotal",
                      "enrichment": {
                        "source_mcp": ["dbp-logger", "tengri"],
                        "correlated_events": ["payment-service 500 at 09:41", "retry storm 09:42"],
                        "root_cause_hint": "downstream 5xx from payment-service вызывает null в discount"
                      }
                    }
                  ],
                  "missing_data": []
                }""";
    }

    /** Данных недостаточно: нужен дозапрос в другие MCP. */
    public static String needMore() {
        return """
                {
                  "enrichment_needed": true,
                  "enough_info": false,
                  "enriched_clusters": [
                    {
                      "cluster_id": "cl-002",
                      "signature": "ReadTimeout on tengri-gw",
                      "enrichment": {}
                    }
                  ],
                  "missing_data": [
                    {
                      "cluster_id": "cl-002",
                      "need": "trace-id логи из payment-service",
                      "suggested_mcp": "tengri"
                    }
                  ]
                }""";
    }
}
