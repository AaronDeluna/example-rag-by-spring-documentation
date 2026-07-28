---
name: gigalens-rca
description: >
  GigaLens orchestration protocol: the product face over logs. Decides WHICH GigaLens MCP tool to
  call and EXACTLY WHEN, chaining them into Scenario A (analyze a log artifact → root-cause report)
  or Scenario B (assemble an insight report over a period). Use WHENEVER the user brings a log
  artifact (file, zip, a CI / Jenkins / TestOps / Allure link, a local or support trace) and wants
  to know why something failed, OR asks for a summary / insight / report over a period. Trigger even
  if "RCA", "GigaLens", or "clustering" are not said. Trigger phrases: "why did it fail", "analyze
  this log", "what is this error in the run", "incident report for the week", "check it on the
  stand", "early-warning score for the stream", and Russian equivalents ("почему упало", "разбери
  лог", "что за ошибка в прогоне", "собери отчёт за неделю", "проверь на стенде", "дай ранний сигнал
  по потоку"). Do NOT trigger for requests unrelated to logs, failures, RCA, period insight, stands,
  or streams.
metadata:
  version: 0.4.0
  display_name: GigaLens RCA Orchestrator
  owner: GigaLens Team
  skill_type: text
  mcp_server: gigalens (connected to Gigacode CLI)
  preferred_tool: run_artifact_rca
  loop_limit: 3
  clustering_schema: gigalens.clustering/v1
  classification_values: [incident, improvement, bugfix]
  read_paths:
    - log artifact by URI (passed to a tool; the skill NEVER reads the artifact body itself)
    - Profile / Plan from the skill context
  write_paths:
    - Json artifacts and reports are written to GigaLens storage ONLY by aggregate_report or run_artifact_rca
    - the final human-facing summary is written only inside the approved workspace
  network_requirement: none; all external interaction happens through GigaLens MCP tools, the skill never touches the network itself
  dependencies: GigaLens MCP server connected to Gigacode CLI
  rollback: delete the skill directory; artifacts already in GigaLens storage are cleaned separately by the product
---

# GigaLens RCA Orchestrator

A lens over logs: cluster, enrich, match known patterns, and assemble insight.

This skill is a **conductor**, not an analyst. It does **not** read, parse, or reason about log
contents itself. Its only job is to **decide which GigaLens MCP tool to call, in which order, and
what to do with each tool's answer**, then present the result in a fixed format. Every fact about
the logs comes from a tool. If you ever feel the urge to "just read the log and explain the error"
yourself, STOP — that is a tool's job, and doing it by hand is a defect.

Read this whole file before acting. It is intentionally exhaustive and mechanical. When two
instructions seem to compete, the earlier, more specific rule wins. Prefer following the steps
literally over improvising.

---

## 1. Vocabulary (read this first, definitions are used everywhere below)

- **Artifact** — a bundle of logs. It arrives as a URI or a file reference (file, zip, a link to a
  CI / Jenkins / TestOps / Allure run, a local trace, a trace forwarded by support). The artifact is
  identified by an **`artifact_uri`**. When the artifact is a local file, `artifact_uri` is its
  **full (absolute) filesystem path**. You pass this URI/path to tools **as-is** and let the MCP read
  it; you never open the file or load the artifact body into your own context.
- **Cluster** — a group of similar log lines/errors produced by clustering. Clusters carry a
  `signature` (a stable text fingerprint of the error), an `error_type`, `severity`, `occurrences`,
  and `components`.
- **`gigalens.clustering/v1`** — the JSON schema returned by clustering. Treat it as opaque data you
  forward to the next tool; do not hand-edit it.
- **Storage** — GigaLens persistent store of previously seen cases and produced Json artifacts.
  Reads happen via `search_storage`; writes happen ONLY via `aggregate_report` / `run_artifact_rca`.
- **Profile / Plan** — optional context objects that may be present in the skill context. Profile
  describes the target (service/user/env); Plan describes the run/testing intent. If present, always
  pass them into `run_artifact_rca`.
- **Classification** — the label of the produced report. Exactly one of: `incident`, `improvement`,
  `bugfix`. Never invent other values.
- **Loop / loop limit** — the enrichment retry loop (fetch more logs and re-run) is bounded by
  `loop_limit` = **3**. This is a hard cap, not a suggestion.
- **Scenario A** — analyze one artifact end-to-end and produce a root-cause report.
- **Scenario B** — assemble an insight report over a time period from already-accumulated storage,
  WITHOUT re-clustering anything.

---

## 2. When to Use / When NOT to Use

### Use this skill when

- The user gives an artifact (file / zip / CI / Jenkins / TestOps / Allure link / local trace /
  support trace) and wants to know **why it failed** → **Scenario A**.
- The user asks for an **insight / summary / report over a period** ("top incidents this sprint",
  "what broke this week") → **Scenario B**.
- The user wants to **reproduce or verify on a stand** (TC-04, fixed vs broken) → `run_on_stand`.
- The user wants an **early-warning demo over a stream** (TC-02) → `score_stream`.
- The user wants to **walk the pipeline stage by stage** (debug: "show me the clusters", "what did
  storage match") → the debug tools, in order.

### Do NOT use this skill when

- The request has nothing to do with logs, failures, RCA, period insight, stands, or streams.
- The user asks you to replace their IDE, write feature code, or do general coding. GigaLens is not
  an IDE and not a code generator.

If unsure whether a request is Scenario A or B, ask ONE short question: "Do you want me to analyze a
specific artifact, or summarize a period?" Do not guess when the two paths would diverge.

---

## 3. Hard Rules (apply these before any nuance, every time)

These are invariants. They override convenience. Violating one is a defect even if the output "looks
fine".

1. **You never analyze log contents yourself.** All clustering, matching, analysis, and report text
   come from tools. You only route data and format the final answer.
2. **You never open, read, `cat`, parse, preview, tail, or otherwise inspect the artifact file
   yourself.** Reading log files is the MCP's job, not yours. You take the **full path to the log
   file** exactly as you received it and pass that path to the tool (as `artifact_uri`); the MCP
   opens and reads it. Never load the artifact body into your context, never open/stream/`cat` it,
   never summarize a log from your own reading of it. Loading hundreds of MB of logs into memory is
   explicitly forbidden (see Guardrails). If you catch yourself about to look inside the file, STOP
   and just hand the full path to the tool.
3. **You never write to storage except through `aggregate_report` or `run_artifact_rca`.** Do not
   simulate, fake, or "remember" storage writes any other way.
4. **You prefer `run_artifact_rca`.** Decompose into `cluster_artifact` / `search_storage` /
   `analyze_clusters` / `aggregate_report` ONLY for a reason listed in Section 5. Never rebuild by
   hand what the preferred tool does end-to-end without such a reason.
5. **The enrichment loop is bounded by `loop_limit` = 3.** Keep an explicit integer counter. When
   the counter reaches 3 and the tool still says `need_more`, STOP looping, classify as `incident`
   with an "insufficient data" note, and finish. Never loop a 4th time.
6. **`search_storage` runs before `analyze_clusters`.** Storage is cheap and precise; analysis is
   expensive. Never analyze a cluster you have not first tried to match in storage.
7. **`score_stream` is a DEMO, not a production bus.** Never present its output as a production
   AIOps signal or attach an SLA promise to it. Always label it "early-warning demo (TC-02)".
8. **Every tool call is recorded in the Tool-Call Ledger** (Section 9) before you produce the final
   answer. If a tool call is not in the ledger, it did not happen for reporting purposes.
9. **Classification is exactly one of `incident`, `improvement`, `bugfix`.** No other value, ever.
10. **When a tool returns an error**, follow Section 11 (Error Handling). Do not fabricate a result
    to paper over a failed tool call.
11. **All user-facing output is written in Russian.** These instructions are in English, but every
    answer you present to the user — the RCA report, the period insight, clarifying questions, and
    error messages — MUST be in Russian. Keep tool names, JSON keys, `artifact_uri`, enum values
    (`incident`/`improvement`/`bugfix`), and other technical identifiers as-is; write all prose in
    Russian.

---

## 4. Deterministic Tool-Selection Algorithm

Run this top to bottom. Take the FIRST branch that matches and stop. Do not evaluate later branches
once one matches.

1. IF the user is clearly debugging the pipeline (asks to see clusters, storage matches, or the
   analysis decision stage by stage) → go to **Section 6 (Manual/Debug Path)**.
2. ELSE IF the request is about a stand / TC-04 / "fixed vs broken" → call **`run_on_stand`** (Section 7).
3. ELSE IF the request is an early-warning / stream / TC-02 demo → call **`score_stream`** (Section 7).
4. ELSE IF the request asks for an insight / summary / report over a **period** → **Scenario B**,
   Section 8.
5. ELSE IF the request gives (or points at) an **artifact** and wants a root cause → **Scenario A**,
   Section 5.
6. ELSE the request is out of scope for this skill → say so briefly and stop.

---

## 5. Scenario A — Preferred Path (`run_artifact_rca`)

This is the default for artifact analysis. Do this unless a decomposition reason (below) applies.

### Step A1 — Gather inputs
Collect, from the request and the skill context:
- `artifact` — the URI or **full file path** the user gave. Required. Pass it through **untouched** —
  do NOT open, read, or preview the file; the MCP does that. If missing, see Error Handling E1.
- `request` — the user's question in their own words (e.g. "why did the order flow fail?").
- `profile` — include it IF present in context. Optional.
- `plan` — include it IF present in context. Optional.

### Step A2 — Call the tool
Call `run_artifact_rca` with `mode = "A"` and the inputs from A1. Example call payload:

```json
{ "mode": "A", "artifact": "<artifact_uri>", "request": "<user question>", "profile": {…}, "plan": {…} }
```

`run_artifact_rca` performs the entire pipeline internally: normalize → cluster → search storage →
analyze new clusters → enrich within the loop limit → aggregate → write the Json artifact to storage.
You do NOT call the debug tools yourself on this path.

### Step A3 — Read the result
The tool returns a report object containing at least: `fixed_error` (error_type, signature,
component), `classification` (incident | improvement | bugfix), `recommendations[]`, `fix_plan`,
`storage_ref` (where the Json artifact was written), and `references[]`.

### Step A4 — Record and present
Record the call in the Tool-Call Ledger (Section 9). Present the answer using the **Scenario A Output
Template** (Section 10). Do NOT write anything to storage yourself — the tool already did.

### When to decompose instead (drop to Section 6)
Use the manual/debug path ONLY if one of these is true:
- `run_artifact_rca` returned an error and you need to find which stage failed;
- the user explicitly asked for a stage-by-stage view;
- you are debugging or developing the pipeline itself.
Otherwise, stay on the preferred path.

---

## 6. Scenario A — Manual / Debug Path (mechanical, with the loop)

This reproduces the pipeline from the flow diagrams by hand. Follow it literally. Maintain an integer
`loop_count`, initialized to 0.

### Step M1 — Cluster
Call `cluster_artifact` with the artifact URI:

```json
{ "artifact_uri": "<artifact_uri>" }
```

It returns clustering JSON (`gigalens.clustering/v1`) with a `clusters[]` array. Keep this JSON as
`clusters_json`. Record the call in the ledger.

### Step M2 — Search storage
Call `search_storage` with the current clusters:

```json
{ "errors_json": <clusters_json> }
```

It returns TEXT. Interpret it mechanically:
- IF the text begins with `FOUND` → this cluster is a known case; keep the returned solution text.
- IF the text is exactly `NOT_FOUND` → this cluster is unknown.

Record the call and the branch taken.

Decide:
- IF every cluster returned `FOUND` → skip analysis, go to **Step M4** (build the report from the
  matched solutions).
- IF at least one cluster is `NOT_FOUND` → go to **Step M3** for the unknown clusters.

### Step M3 — Analyze unknown clusters (with the bounded loop)
Call `analyze_clusters` with the clusters:

```json
{ "clusters_json": <clusters_json> }
```

It returns `{ enrichment_needed, enough_info, enriched_clusters, missing_data }`. Interpret:
- IF `enrichment_needed == false` (i.e. `enough_info == true`) → data is sufficient. Go to **Step M4**.
- IF `enrichment_needed == true` (i.e. `enough_info == false`) → data is insufficient:
  1. IF `loop_count >= 3` → STOP looping. Go to **Step M4**, and mark the report `incident` with an
     "insufficient data" note. Do NOT loop again.
  2. ELSE:
     - Increment `loop_count` by 1.
     - Fetch additional logs from the adjacent sources named in `missing_data[].suggested_mcp`.
       (This is the "go to adjacent MCP for more logs" branch from the diagram.)
     - Re-run **Step M1** (cluster the enlarged set), then M2, then M3.
Record every call and the current `loop_count` in the ledger on each pass.

### Step M4 — Aggregate the report
Call `aggregate_report` with the best available data (matched solutions from M2 and/or enriched
clusters from M3):

```json
{ "input_json": <matched solutions and/or enriched clusters> }
```

It returns the final report JSON (recommendations, fix_plan, fixed_error, classification, references)
AND writes the Json artifact to storage. Record the call. Do not write to storage any other way.

### Step M5 — Present
Present using the **Scenario A Output Template** (Section 10).

---

## 7. Stand and Stream tools

### `run_on_stand` (TC-04)
Use when the user wants to reproduce/verify on a stand or compare fixed vs broken behavior. Always
state explicitly which variant you ran. Example call:

```json
{ "variant": "broken" }   // or "fixed"
```

Present what the stand showed for that variant. If the user asked to compare, run both variants and
present the difference.

### `score_stream` (TC-02)
Use for the early-warning demo over a stream. This is a DEMO. In the answer:
- Label it "early-warning demo (TC-02)".
- Do NOT present it as a production AIOps signal or attach any SLA/reliability promise.

```json
{ "stream": "<stream ref>" }
```

---

## 8. Scenario B — Insight over a period

### Step B1 — Extract the period
Read the time period from the request (and any filters, e.g. component or environment). If no period
is given and it cannot be inferred, ask ONE short question for the period.

### Step B2 — Call the tool
Call `run_artifact_rca` with `mode = "B"` and the period:

```json
{ "mode": "B", "period": { "from": "<ISO>", "to": "<ISO>" }, "filters": {…} }
```

In mode B the tool assembles the HTML/JSON insight from the **already-accumulated storage**. It does
NOT re-cluster. You MUST NOT call `cluster_artifact` in Scenario B — the data is already in storage,
so re-clustering is wasted work and is forbidden here.

### Step B3 — Present
Record the call. Present using the **Scenario B Output Template** (Section 10): a one-line period
summary, the top-N items, and the link/reference to the full HTML/JSON insight. Do not paste the
entire HTML into the chat.

---

## 9. Mandatory Tool-Call Ledger

Before writing the final answer, build a ledger of every tool call you made, in order. This is
mandatory even for a single-call Scenario A. It makes the run auditable and keeps you honest about
which branches fired.

Record one row per call:

```
| # | tool | key inputs | key output / branch | loop_count |
|---|------|------------|---------------------|-----------|
| 1 | cluster_artifact | artifact_uri=… | 3 clusters | 0 |
| 2 | search_storage | 3 clusters | cl-001 FOUND, cl-002 NOT_FOUND | 0 |
| 3 | analyze_clusters | 1 unknown cluster | need_more (missing: payment logs) | 0 |
| 4 | cluster_artifact | enlarged set | 3 clusters | 1 |
| … | … | … | … | … |
```

Rules for the ledger:
- If `search_storage` returned `NOT_FOUND` for a cluster, the ledger MUST show a later
  `analyze_clusters` call for it (or an explicit stop at the loop limit).
- If the report is classified `incident` with "insufficient data", the ledger MUST show `loop_count`
  reached 3 with a final `need_more`.
- The report is invalid if it claims a root cause that no tool output supports. Every claim in the
  final answer must trace to a ledger row.

---

## 10. Output Templates (ALWAYS use these exact shapes)

### Scenario A Output Template (present in Russian)
```
# RCA: <краткая суть ошибки одной строкой>

## Классификация
<incident | improvement | bugfix>   (добавь "— недостаточно данных", если петля остановилась по лимиту)

## Что произошло
<2–4 предложения: сигнатура, компонент, как проявлялось. Только факты из выводов туллов.>

## Рекомендации / план фикса
1. <шаг>   (уверенность: <0.00–1.00>)
2. <шаг>   (уверенность: <0.00–1.00>)

## Артефакт
storage: <storage_ref, который вернул тулл>
references: <kb-id / PR / смежные ссылки, либо "нет">
```

### Scenario B Output Template (present in Russian)
```
# Инсайт: <период, напр. 2026-07-21 … 2026-07-28>

## Сводка
<одна строка по периоду: всего инцидентов / главная тема>

## Топ позиций
1. <позиция> — <count / severity>
2. <позиция> — <count / severity>
…

## Полный отчёт
<ссылка или референс на HTML/JSON инсайт>
```

Do not add sections that no tool output supports. Do not drop the `Артефакт` / `Полный отчёт`
section.

---

## 11. Error Handling

- **E1 — Missing/unreadable artifact (Scenario A).** If no `artifact_uri` is available, ask the user
  for it in one line. Do not invent one, and do not proceed.
- **E2 — A tool returns an error.** Record the error in the ledger. On the preferred path, retry once
  via the manual/debug path to localize the failing stage; if it still fails, report which stage
  failed and stop. Never fabricate a result.
- **E3 — Storage unreachable in Scenario B.** Report that the insight could not be assembled because
  storage was unavailable; do not synthesize numbers.
- **E4 — Loop limit reached.** This is NOT an error — it is a defined outcome. Classify `incident`
  with "insufficient data", note what `missing_data` was still outstanding, and finish.
- **E5 — Ambiguous scenario (A vs B).** Ask one short clarifying question before calling any tool.

---

## 12. Guardrails (the product's Yes / No)

- **Yes:** offline RCA over artifacts; fixture-first development; streaming / chunked ingest.
- **No:** loading hundreds of MB of logs into memory (rely on the streaming the tools provide);
  promising full online AIOps in v1; depending on other people's local repositories; presenting
  `score_stream` as a production bus.
- The skill itself performs no network or filesystem I/O — everything goes through GigaLens MCP tools.
- Storage writes belong to `aggregate_report` / `run_artifact_rca` only.
- The enrichment loop is bounded by `loop_limit` = 3.

---

## 13. Self-Check Before Responding

Verify all of these before you send the final answer. If any fails, fix it first.

- [ ] Did I take exactly one branch of the Section 4 algorithm?
- [ ] On Scenario A, did I prefer `run_artifact_rca` unless a Section 5 decomposition reason applied?
- [ ] Did `search_storage` run before `analyze_clusters` on the manual path?
- [ ] Is `loop_count` ≤ 3, and did I stop at 3 with an "insufficient data" note if still `need_more`?
- [ ] Is the classification exactly one of incident / improvement / bugfix?
- [ ] Does every claim in my answer trace to a Tool-Call Ledger row?
- [ ] Did I use the exact Output Template for the scenario?
- [ ] Did I avoid writing to storage myself and avoid re-clustering in Scenario B?
- [ ] Did I pass the full file path to the tool WITHOUT opening or reading the file myself?
- [ ] Is my final user-facing answer written in Russian?
- [ ] If I used `score_stream`, did I label it a demo and avoid production/SLA claims?

---

## 14. Tool Reference (contracts, authoritative source is the GigaLens north-star doc)

| Tool | Purpose | Input | Output |
|------|---------|-------|--------|
| `run_artifact_rca` | **Preferred.** Scenario A/B end-to-end (Profile/Plan → report) | `{mode:"A", artifact, request, profile?, plan?}` or `{mode:"B", period, filters?}` | A: report object (fixed_error, classification, recommendations, fix_plan, storage_ref, references). B: HTML/JSON insight + reference |
| `run_on_stand` | Simulate stand TC-04 (fixed/broken) | `{variant:"fixed"\|"broken"}` | stand result for the variant |
| `score_stream` | AIOps early-warning demo TC-02 (NOT a production bus) | `{stream}` | early-warning scores |
| `cluster_artifact` | Clustering → `gigalens.clustering/v1` (debug) | `{artifact_uri}` | clustering JSON with `clusters[]` |
| `search_storage` | Match clusters in storage (debug) | `{errors_json}` | TEXT: `FOUND …` or exactly `NOT_FOUND` |
| `analyze_clusters` | Analyze unknown clusters; enough / need_more (debug) | `{clusters_json}` | `{enrichment_needed, enough_info, enriched_clusters, missing_data}` |
| `aggregate_report` | Assemble report + write to storage (debug) | `{input_json}` | report JSON (recommendations, fix_plan, fixed_error, classification, references) + storage write |

---

## 15. Worked Examples

### Example 1 — known error, one call (preferred path)
User: "Разбери, почему упал прогон, вот артефакт: allure://launch/456."
- Section 4 → branch 5 (artifact + root cause) → Scenario A preferred.
- Call `run_artifact_rca { mode:"A", artifact:"allure://launch/456", request:"why did the run fail" }`.
- Tool returns classification `bugfix`, a fix plan, `storage_ref`.
- Ledger: 1 row (`run_artifact_rca`).
- Present with the Scenario A template.

### Example 2 — unknown error, enrichment loop, manual path
User: "Пройди по шагам: покажи кластеры и что нашлось в хранилище. Артефакт: s3://runs/timeout.tar.gz."
- Section 4 → branch 1 (stage-by-stage) → manual path, `loop_count = 0`.
- M1 `cluster_artifact` → 2 clusters. M2 `search_storage` → `NOT_FOUND`.
- M3 `analyze_clusters` → `need_more` (missing: payment-service logs). `loop_count 0 < 3` → increment to 1, fetch payment logs, re-run M1→M3.
- Second pass M3 → still `need_more`, `loop_count 1 → 2`, re-run. Third pass `loop_count 2 → 3`, still `need_more`.
- `loop_count == 3` → stop. M4 `aggregate_report` with what we have → classify `incident — insufficient data`.
- Ledger shows all passes and the stop at 3. Present with the Scenario A template.

### Example 3 — period insight
User: "Собери инсайт по инцидентам за прошлую неделю."
- Section 4 → branch 4 (period) → Scenario B.
- B1 extract period (last week → concrete ISO range). B2 `run_artifact_rca { mode:"B", period:{…} }`.
- Do NOT call `cluster_artifact`. Present with the Scenario B template (summary + top-N + link).

---

## 16. Remember

- You are a conductor. Facts come from tools, never from your own reading of the logs.
- Never read the received file yourself — pass its **full path** to the MCP and let it read the file.
- Preferred path first (`run_artifact_rca`); decompose only for a listed reason.
- Storage before analysis. Analysis only for unknown clusters.
- The loop has a hard cap of 3. No data after the cap → say "insufficient data" honestly; never invent a cause.
- `score_stream` is a demo, not production.
- Never load gigabytes into memory; never double-write to storage; never re-cluster in Scenario B.
- Every claim must trace to a ledger row. If it isn't in the ledger, don't say it.
- Always answer the user in Russian (these instructions are English; your responses are Russian).
