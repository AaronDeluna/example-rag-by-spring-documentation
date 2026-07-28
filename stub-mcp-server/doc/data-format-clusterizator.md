### 1. Входной артефакт (`jenkins-payments-e2e-1842.log`)

```text
2026-07-28 10:14:01.120 INFO  [jenkins] Starting build #1842 job=payments-e2e
2026-07-28 10:14:03.441 INFO  [agent]   Checkout scm revision=a1b2c3d
2026-07-28 10:14:12.008 WARN  [app]     Connection pool exhausted, retrying attempt=1
2026-07-28 10:14:12.510 WARN  [app]     Connection pool exhausted, retrying attempt=2
2026-07-28 10:14:13.022 WARN  [app]     Connection pool exhausted, retrying attempt=3
2026-07-28 10:14:18.331 ERROR [testops] AssertionError: expected status 200 but was 503 path=/api/v1/payments/42
2026-07-28 10:14:18.340 ERROR [testops] AssertionError: expected status 200 but was 503 path=/api/v1/payments/77
2026-07-28 10:14:19.002 ERROR [jenkins] Job failed: payments-e2e #1842
2026-07-28 10:14:19.015 INFO  [jenkins] Archiving artifacts for build #1842
```

### 2. Кластеризация

| Cluster | event_count | template |
|---|---|---|
| C1 | 3 | `WARN [app] Connection pool exhausted, retrying attempt=<*>` |
| C2 | 2 | `ERROR [testops] AssertionError: expected status 200 but was 503 path=<*>` |
| C3 | 1 | `INFO [jenkins] Starting build #<*> job=payments-e2e` |
| C4 | 1 | `INFO [agent] Checkout scm revision=<*>` |
| C5 | 1 | `INFO [jenkins] Archiving artifacts for build #<*>` |
| C6 | 1 | `ERROR [jenkins] Job failed: payments-e2e #<*>` |

### 3. Выходной файл (`clusters.json`)

```json
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
```