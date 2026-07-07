# Индекс задач

## 📊 Таблица статусов

**Всего задач:** 6 (0 выполнено, 6 в работе)

| ID  | Модуль   | Название                                                                   | Статус | Дата создания | Дата завершения | Файл |
|-----|----------|----------------------------------------------------------------------------|--------|---------------|-----------------|------|
| 001 | skills   | Создать пакет util.cli (OsType, CommandResolver, CommandFactory)           | ✅     | 2026-07-07    | 2026-07-07      | [TASK-001_skills_create_cli_package.md](./archive/TASK-001_skills_create_cli_package.md) |
| 002 | skills   | Расширить agent-runner.properties и AgentRunnerProperties                  | 📋     | 2026-07-07    |                 | [TASK-002_skills_config_properties.md](./to_work/TASK-002_skills_config_properties.md) |
| 003 | skills   | Переработать AgentRunnerFactory — RunnerLogWriter + CommandFactory         | 📋     | 2026-07-07    |                 | [TASK-003_skills_adapt_factory.md](./to_work/TASK-003_skills_adapt_factory.md) |
| 004 | skills   | Переработать QwenAgentRunner/QwenJudgeRunner — только CommandFactory       | 📋     | 2026-07-07    |                 | [TASK-004_skills_modify_runners.md](./to_work/TASK-004_skills_modify_runners.md) |
| 005 | skills   | Актуализировать тесты + AgentEvaluatorService под новую архитектуру        | 📋     | 2026-07-07    |                 | [TASK-005_skills_adapt_tests.md](./to_work/TASK-005_skills_adapt_tests.md) |
| 006 | skills   | Пометка QwenCommandFactory @Deprecated(forRemoval=true) — финальный шаг    | 📋     | 2026-07-07    |                 | [TASK-006_skills_deprecate_legacy_factory.md](./to_work/TASK-006_skills_deprecate_legacy_factory.md) |

**Порядок выполнения:** `001 → 002 → 003 → 004 → 005 → 006` (строго последовательно)

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
2. **Формат имени файла:** `TASK-<ID>_<модуль>_<краткое_описание>.md`
   - `<ID>` — трёхзначный номер задачи (например, `001`, `002`)
   - `<модуль>` — код модуля (`skills`, `common`, `jar`, `webmvc`, `webflux`, `stdio`, `rag`)
   - `<краткое_описание>` — краткое описание на английском в snake_case (например, `tests_for_server`, `logging_impl`)
   - Пример: `TASK-001_skills_refactor_command_factory.md`
3. Добавьте новую строку в таблицу выше
4. Укажите статус 📋

## 🏁 Как завершить задачу

1. Убедитесь, что все чекбоксы в файле задачи отмечены `[x]`
2. Переместите файл в `archive/`
3. Обновите статус в таблице на ✅
4. Заполните дату завершения
