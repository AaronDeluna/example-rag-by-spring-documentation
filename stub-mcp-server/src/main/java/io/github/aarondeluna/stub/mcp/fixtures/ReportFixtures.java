package io.github.aarondeluna.stub.mcp.fixtures;

/**
 * Хардкоженные фикстуры для инструмента {@code aggregate_report}.
 *
 * <p>Классификация {@code bugfix} — устранён баг с известным решением,
 * {@code incident} — инцидент без готового фикса.
 *
 * <p>Чтобы заменить заглушку на реальную логику — правьте методы этого класса.
 */
public final class ReportFixtures {

    private ReportFixtures() {
    }

    /** Итоговый отчёт для устранённого бага. */
    public static String bugfix() {
        return """
                {
                  "report_id": "rep-20260728-001",
                  "generated_at": "2026-07-28T10:05:00Z",
                  "fixed_error": {
                    "error_type": "NullPointerException",
                    "signature": "java.lang.NullPointerException at OrderService.calcTotal",
                    "component": "order-service"
                  },
                  "classification": "bugfix",
                  "recommendations": [
                    {"step": 1, "action": "Добавить null-check на discount перед вызовом calcTotal", "confidence": 0.9}
                  ],
                  "fix_plan": {
                    "summary": "Защититься от null discount и покрыть тестом",
                    "changes": [{"file": "OrderService.java", "change": "guard-clause на discount == null"}],
                    "estimated_effort": "S"
                  },
                  "references": ["kb-042", "PR #1234"]
                }""";
    }

    /**
     * Сценарий B: инсайт за период.
     *
     * <p>Без кластеризации и анализа — агрегатор читает уже накопленные в Хранилище
     * события прошлых прогонов и сводит их в HTML-отчёт «инсайт за период».
     */
    public static String periodInsight() {
        return """
                <html>
                  <head><title>Инсайт за период</title></head>
                  <body>
                    <h1>Инсайт за период 2026-07-01 — 2026-07-28</h1>
                    <ul>
                      <li>Всего инцидентов: 12</li>
                      <li>bugfix: 7</li>
                      <li>incident: 4</li>
                      <li>доработка: 1</li>
                    </ul>
                    <h2>Топ ошибок</h2>
                    <ol>
                      <li>NullPointerException at OrderService.calcTotal — 37</li>
                      <li>ReadTimeout on tengri-gw — 14</li>
                      <li>IllegalStateException at InventoryService.reserve — 21</li>
                    </ol>
                  </body>
                </html>""";
    }

    /** Итоговый отчёт для инцидента без готового решения. */
    public static String incident() {
        return """
                {
                  "report_id": "rep-20260728-002",
                  "generated_at": "2026-07-28T10:05:00Z",
                  "fixed_error": {
                    "error_type": "UnknownError",
                    "signature": "Unknown error in worker pool",
                    "component": "batch-worker"
                  },
                  "classification": "incident",
                  "recommendations": [
                    {"step": 1, "action": "Собрать trace-id и профилировать worker-pool под нагрузкой", "confidence": 0.5}
                  ],
                  "fix_plan": {
                    "summary": "Диагностика: причина падения worker неясна, нужен сбор данных",
                    "changes": [],
                    "estimated_effort": "M"
                  },
                  "references": []
                }""";
    }
}
