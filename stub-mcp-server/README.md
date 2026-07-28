# stub-mcp-server

## 1. Что это и зачем

Локальный **MCP-сервер** (Model Context Protocol) с четырьмя инструментами-заглушками
(stub/mock). Ответы — детерминированные хардкоженные фикстуры, **без реальных внешних
вызовов**.

Назначение: прогонять флоу агента end-to-end и разрабатывать скиллы, пока реальных
реализаций инструментов ещё нет. Сервер построен на **Spring AI MCP Server** и работает
поверх транспорта **stdio**.

Инструменты (имена — английский snake_case, тексты ответов — на русском):

| Тулл | Вход | Выход |
|------|------|-------|
| `cluster_artifact` | `{ "artifact_uri": "..." }` | JSON: список кластеров ошибок |
| `analyze_clusters` | `{ "clusters_json": "...", "scenario": "enough\|need_more" }` | JSON: `enough_info`, `missing_data` |
| `search_storage` | `{ "errors_json": "...", "scenario": "found\|not_found" }` | ТЕКСТ: `FOUND ...` / `NOT_FOUND` |
| `aggregate_report` | `{ "input_json": "..." }` | JSON: `classification`, рекомендации, `fix_plan` |

## 2. Требования

- **Java 17+**
- **Maven 3.8+**

## 3. Сборка

```bash
mvn package -pl stub-mcp-server
```

Собранный исполняемый jar: `stub-mcp-server/target/stub-mcp-server.jar`.

## 4. Запуск

```bash
java -jar stub-mcp-server/target/stub-mcp-server.jar
```

Сервер общается по stdio (stdin/stdout заняты MCP-протоколом), поэтому все логи идут в
**stderr**. Обычно сервер не запускают вручную — его поднимает MCP-клиент (см. ниже).

## 5. Пример JSON-конфига для MCP-клиента (Claude Code)

```json
{
  "mcpServers": {
    "stub": {
      "command": "java",
      "args": ["-jar", "/path/to/stub-mcp-server.jar"]
    }
  }
}
```

Замените `/path/to/stub-mcp-server.jar` на абсолютный путь к собранному jar.

## 6. Как форсировать сценарии

По умолчанию профиль выбирается автоматически:

- по подстроке в `artifact_uri`: `...npe...` → NPE, `...timeout...` → ReadTimeout,
  `...unknown...` → неизвестная ошибка;
- `analyze_clusters`: наличие `NullPointerException`/`IllegalStateException` во входе → `enough`, иначе `need_more`;
- `search_storage`: сигнатуры `NullPointerException at OrderService` / `IllegalStateException at PaymentService` → `found`, иначе `not_found`;
- `aggregate_report`: поле `classification` во входе, иначе NPE/ISE → `bugfix`, timeout/unknown → `incident`.

Поведение по умолчанию можно перекрыть аргументом `scenario`:

- `analyze_clusters` → `scenario`: `enough` | `need_more`;
- `search_storage` → `scenario`: `found` | `not_found`.

Профили-фикстуры образуют согласованные цепочки:

- `...npe...` → `enough` → `FOUND` → `bugfix`;
- `...timeout...` → `need_more` → `NOT_FOUND`;
- `...unknown...` → `need_more` → `NOT_FOUND` → `incident`.

## 7. Как заменить заглушки на реальную логику

Вся возвращаемая «правда» вынесена в пакет `fixtures/`:

- `ClusterFixtures` — ответы `cluster_artifact`;
- `AnalysisFixtures` — ответы `analyze_clusters`;
- `StorageFixtures` — ответы `search_storage`;
- `ReportFixtures` — ответы `aggregate_report`.

Чтобы подключить реальную реализацию, замените тело методов соответствующих туллов в
пакете `tools/` (и/или методов фикстур) на настоящие вызовы. Контракт инструментов
(имена, вход/выход) при этом менять не нужно.

## Smoke-тесты

```bash
mvn test -pl stub-mcp-server -Dgroups=smoke
```

Тесты вызывают методы туллов напрямую (без подъёма сервера) и проверяют фикстуры.
