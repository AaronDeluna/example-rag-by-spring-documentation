# Индекс задач

## 📊 Таблица статусов

| ID  | Модуль                  | Название                                              | Статус | Дата создания | Дата завершения | Файл                               |
|-----|-------------------------|-------------------------------------------------------|--------|---------------|-----------------|------------------------------------|
| 001 | mcp-server-jar-unpacker | Реализация тестов для Server.java с Mockito           | ✅      | 2026-03-26    | 2026-03-26      | TASK_001_tests_for_server.md       |
| 002 | mcp-server-jar-unpacker | Логирование вызовов инструментов MCP-сервера          | ✅      | 2026-03-26    | 2026-03-26      | TASK_002_logging_task.md           |
| 003 | mcp-server-jar-unpacker | Разделение Server.java на модули: JsonRpcHandler      | ✅      | 2026-03-26    | 2026-03-26      | TASK-003_json_rpc_handler.md       |
| 011 | mcp-server-jar-unpacker | Characterization tests для legacy-кода                | ✅      | 2026-03-26    | 2026-03-26      | TASK-011_characterization_tests.md |
| 004 | mcp-server-jar-unpacker | Разделение Server.java на модули: Tool-классы         | ✅      | 2026-03-26    | 2026-03-26      | TASK-004_tool_classes.md           |
| 005 | mcp-server-jar-unpacker | Разделение Server.java на модули: Services            | ✅      | 2026-03-26    | 2026-03-26      | TASK-005_services.md               |
| 006 | mcp-server-jar-unpacker | Валидация путей: защита от path traversal             | ✅      | 2026-03-26    | 2026-03-26      | TASK-006_path_validation.md        |
| 007 | mcp-server-jar-unpacker | Валидация FQN класса: защита от инъекций              | ✅      | 2026-03-26    | 2026-03-26      | TASK-007_fqn_validation.md         |
| 008 | mcp-server-jar-unpacker | Умное кэширование JAR с TTL                           | ✅      | 2026-03-26    | 2026-03-26      | TASK-008_cache_ttl.md              |
| 009 | mcp-server-jar-unpacker | Инвалидация кэша при изменении ~/.m2                  | ✅      | 2026-03-26    | 2026-03-26      | TASK-009_cache_invalidation.md     |
| 010 | mcp-server-jar-unpacker | Интеграционные тесты с реальными JAR                  | ✅      | 2026-03-26    | 2026-03-26      | TASK-010_integration_tests.md      |
| 012 | mcp-server-jar-unpacker | Улучшение логирования: DEBUG-режим                    | ✅      | 2026-03-26    | 2026-03-26      | TASK-012_logging_improvements.md   |
| 013 | mcp-server-jar-unpacker | Новый инструмент: list_classes_in_jar                 | ✅      | 2026-03-26    | 2026-03-26      | TASK-013_list_classes_tool.md      |
| 014 | mcp-server-jar-unpacker | Новый инструмент: search_classes_by_pattern           | ✅      | 2026-03-26    | 2026-03-26      | TASK-014_search_pattern_tool.md    |
| 015 | mcp-server-jar-unpacker | Параметр --no-usage-statistics для отключения логов   | ✅      | 2026-03-26    | 2026-03-26      | TASK-015_no_usage_statistics.md    |
| 016 | common                  | Настройка Qwen Code для работы со всем проектом       | ✅      | 2026-03-26    | 2026-03-26      | TASK-016_qwen_code_project_setup.md    |
| 017 | common                  | Оптимизация документации проекта                      | ✅      | 2026-03-26    | 2026-03-26      | TASK-017_documentation_optimization.md |
| 018 | common                  | Добавить распределение задач по модулям               | ✅      | 2026-03-26    | 2026-03-26      | TASK-018_common_add_module_distribution.md |

## 📁 Легенда

| Значок | Статус                   |
|--------|--------------------------|
| 📋     | Pending (запланирована)  |
| 🔧     | In Progress (в работе)   |
| ✅     | Done (выполнена)         |

## 📂 Папки

- **Активные задачи:** [`to_work/`](./to_work/)
- **Архив:** [`archive/`](./archive/)

## 📝 Как добавить задачу

1. Создайте файл задачи в `to_work/` по шаблону `task_template.md`
2. **Формат имени файла:** `TASK-<ID>_<short_description>.md`
   - `<ID>` — трёхзначный номер задачи (например, `001`, `002`)
   - `<short_description>` — краткое описание на английском в snake_case (например, `tests_for_server`, `logging_impl`)
   - Пример: `TASK-001_tests_for_server.md`, `TASK-002_logging_impl.md`
3. Добавьте новую строку в таблицу выше
4. Укажите статус 📋

## 🏁 Как завершить задачу

1. Убедитесь, что все чекбоксы в файле задачи отмечены `[x]`
2. Переместите файл в `archive/`
3. Обновите статус в таблице на ✅
4. Заполните дату завершения
