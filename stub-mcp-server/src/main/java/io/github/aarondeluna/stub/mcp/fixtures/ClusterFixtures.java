package io.github.aarondeluna.stub.mcp.fixtures;

/**
 * Хардкоженные фикстуры для инструмента {@code cluster_artifact}.
 *
 * <p>Профиль выбирается по подстроке в {@code artifact_uri}:
 * {@code npe} → кластер NPE, {@code timeout} → кластер ReadTimeout,
 * {@code unknown} → неизвестная ошибка. По умолчанию — профиль NPE.
 *
 * <p>Чтобы заменить заглушку на реальную логику — правьте методы этого класса.
 */
public final class ClusterFixtures {

    private ClusterFixtures() {
    }

    /** Имя профиля-фикстуры, выбранного по URI. */
    public enum Profile {
        NPE, TIMEOUT, UNKNOWN, JENKINS, ISE
    }

    /** Определяет профиль по подстроке в URI артефакта. */
    public static Profile profileForUri(String artifactUri) {
        String uri = artifactUri == null ? "" : artifactUri.toLowerCase();
        if (uri.contains("timeout")) {
            return Profile.TIMEOUT;
        }
        if (uri.contains("unknown")) {
            return Profile.UNKNOWN;
        }
        if (uri.contains("jenkins")) {
            return Profile.JENKINS;
        }
        if (uri.contains("ise")) {
            return Profile.ISE;
        }
        return Profile.NPE;
    }

    /** Возвращает JSON-ответ {@code cluster_artifact} для указанного URI. */
    public static String forUri(String artifactUri) {
        return switch (profileForUri(artifactUri)) {
            case TIMEOUT -> timeout(artifactUri);
            case UNKNOWN -> unknown(artifactUri);
            case NPE -> npe(artifactUri);
            case JENKINS -> jenkins();
            case ISE -> ise(artifactUri);
        };
    }

    private static String npe(String artifactUri) {
        return """
                {
                  "artifact_uri": "%s",
                  "generated_at": "2026-07-28T10:00:00Z",
                  "clusters": [
                    {
                      "cluster_id": "cl-001",
                      "signature": "java.lang.NullPointerException at OrderService.calcTotal",
                      "error_type": "NullPointerException",
                      "severity": "ERROR",
                      "occurrences": 37,
                      "components": ["order-service"],
                      "sample_messages": ["NPE at com.sber.order.OrderService.calcTotal(OrderService.java:88)"],
                      "first_seen": "2026-07-28T09:41:02Z",
                      "last_seen": "2026-07-28T09:58:50Z"
                    }
                  ],
                  "meta": { "source_lines": 5231, "clusters_count": 3, "compressed": true }
                }""".formatted(artifactUri);
    }

    private static String timeout(String artifactUri) {
        return """
                {
                  "artifact_uri": "%s",
                  "generated_at": "2026-07-28T10:00:00Z",
                  "clusters": [
                    {
                      "cluster_id": "cl-002",
                      "signature": "ReadTimeout on tengri-gw",
                      "error_type": "ReadTimeout",
                      "severity": "WARN",
                      "occurrences": 14,
                      "components": ["payment-service", "tengri-gw"],
                      "sample_messages": ["ReadTimeout: GET https://tengri-gw/pay timed out after 3000ms"],
                      "first_seen": "2026-07-28T09:44:11Z",
                      "last_seen": "2026-07-28T09:59:30Z"
                    }
                  ],
                  "meta": { "source_lines": 4120, "clusters_count": 2, "compressed": true }
                }""".formatted(artifactUri);
    }

    private static String unknown(String artifactUri) {
        return """
                {
                  "artifact_uri": "%s",
                  "generated_at": "2026-07-28T10:00:00Z",
                  "clusters": [
                    {
                      "cluster_id": "cl-003",
                      "signature": "Unknown error in worker pool",
                      "error_type": "UnknownError",
                      "severity": "ERROR",
                      "occurrences": 8,
                      "components": ["batch-worker"],
                      "sample_messages": ["ERROR worker-7 exited with code 137, cause unclear"],
                      "first_seen": "2026-07-28T09:47:00Z",
                      "last_seen": "2026-07-28T09:57:12Z"
                    }
                  ],
                  "meta": { "source_lines": 3980, "clusters_count": 1, "compressed": true }
                }""".formatted(artifactUri);
    }

    /**
     * Новое событие: IllegalStateException НЕ в PaymentService.
     *
     * <p>Такой кластер отсутствует в Хранилище (поисковик вернёт {@code not_found}),
     * но данных для вывода анализатору достаточно ({@code enough}). Моделирует
     * сценарий A2 «новое событие, данных хватает».
     */
    private static String ise(String artifactUri) {
        return """
                {
                  "artifact_uri": "%s",
                  "generated_at": "2026-07-28T10:00:00Z",
                  "clusters": [
                    {
                      "cluster_id": "cl-004",
                      "signature": "java.lang.IllegalStateException at InventoryService.reserve",
                      "error_type": "IllegalStateException",
                      "severity": "ERROR",
                      "occurrences": 21,
                      "components": ["inventory-service"],
                      "sample_messages": ["ISE at com.sber.inv.InventoryService.reserve(InventoryService.java:142): stock already reserved"],
                      "first_seen": "2026-07-28T09:45:10Z",
                      "last_seen": "2026-07-28T09:59:01Z"
                    }
                  ],
                  "meta": { "source_lines": 4700, "clusters_count": 2, "compressed": true }
                }""".formatted(artifactUri);
    }

    private static String jenkins() {
        return """
                {
                  "schema": "gigalens.clustering/v1",
                  "source": {
                    "path": "artifacts/jenkins-payments-e2e-1842.log",
                    "source_type": "jenkins",
                    "line_count": 9
                  },
                  "stats": {
                    "cluster_count": 6,
                    "total_events": 9,
                    "min_cluster_size": 1,
                    "max_cluster_size": 3,
                    "avg_cluster_size": 1.5
                  },
                  "clusters": [
                    {
                      "id": "C1",
                      "template": "WARN [app] Connection pool exhausted, retrying attempt=<*>",
                      "event_count": 3,
                      "event_ids": [2, 3, 4],
                      "sample_lines": [
                        "2026-07-28 10:14:12.008 WARN  [app]     Connection pool exhausted, retrying attempt=1"
                      ],
                      "parameters": ["attempt"]
                    },
                    {
                      "id": "C2",
                      "template": "ERROR [testops] AssertionError: expected status 200 but was 503 path=<*>",
                      "event_count": 2,
                      "event_ids": [5, 6],
                      "sample_lines": [
                        "2026-07-28 10:14:18.331 ERROR [testops] AssertionError: expected status 200 but was 503 path=/api/v1/payments/42"
                      ],
                      "parameters": ["path"]
                    },
                    {
                      "id": "C3",
                      "template": "INFO [jenkins] Starting build #<*> job=payments-e2e",
                      "event_count": 1,
                      "event_ids": [0],
                      "sample_lines": [
                        "2026-07-28 10:14:01.120 INFO  [jenkins] Starting build #1842 job=payments-e2e"
                      ],
                      "parameters": ["build"]
                    },
                    {
                      "id": "C4",
                      "template": "INFO [agent] Checkout scm revision=<*>",
                      "event_count": 1,
                      "event_ids": [1],
                      "sample_lines": [
                        "2026-07-28 10:14:03.441 INFO  [agent]   Checkout scm revision=a1b2c3d"
                      ],
                      "parameters": ["revision"]
                    },
                    {
                      "id": "C5",
                      "template": "INFO [jenkins] Archiving artifacts for build #<*>",
                      "event_count": 1,
                      "event_ids": [8],
                      "sample_lines": [
                        "2026-07-28 10:14:19.015 INFO  [jenkins] Archiving artifacts for build #1842"
                      ],
                      "parameters": ["build"]
                    },
                    {
                      "id": "C6",
                      "template": "ERROR [jenkins] Job failed: payments-e2e #<*>",
                      "event_count": 1,
                      "event_ids": [7],
                      "sample_lines": [
                        "2026-07-28 10:14:19.002 ERROR [jenkins] Job failed: payments-e2e #1842"
                      ],
                      "parameters": ["build"]
                    }
                  ],
                  "assignments": [
                    { "line": 0, "cluster_id": "C3" },
                    { "line": 1, "cluster_id": "C4" },
                    { "line": 2, "cluster_id": "C1" },
                    { "line": 3, "cluster_id": "C1" },
                    { "line": 4, "cluster_id": "C1" },
                    { "line": 5, "cluster_id": "C2" },
                    { "line": 6, "cluster_id": "C2" },
                    { "line": 7, "cluster_id": "C6" },
                    { "line": 8, "cluster_id": "C5" }
                  ]
                }
               """;
    }
}
