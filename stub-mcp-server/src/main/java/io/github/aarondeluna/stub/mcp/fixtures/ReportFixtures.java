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
