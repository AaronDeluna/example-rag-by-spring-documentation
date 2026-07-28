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
        NPE, TIMEOUT, UNKNOWN
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
        // NPE — профиль по умолчанию
        return Profile.NPE;
    }

    /** Возвращает JSON-ответ {@code cluster_artifact} для указанного URI. */
    public static String forUri(String artifactUri) {
        return switch (profileForUri(artifactUri)) {
            case TIMEOUT -> timeout(artifactUri);
            case UNKNOWN -> unknown(artifactUri);
            case NPE -> npe(artifactUri);
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
}
