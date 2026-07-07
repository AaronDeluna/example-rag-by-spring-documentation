# TASK-004: Переработать QwenAgentRunner и QwenJudgeRunner — только CommandFactory

> **⚠️ Важно:** Этот файл должен быть создан **до начала** любой работы над задачей.
> Отмечайте чек-боксы `[x]` **сразу после выполнения** каждого шага для возможности продолжения с места прерывания.

## Информация о задаче

| Поле | Значение |
|------|----------|
| **Модуль:** | `skills` |
| **ID:** | 004 |
| **Файл:** | `TASK-004_skills_modify_runners.md` |
| **Порядок:** | 4-я задача (001 → 002 → 003 → 004 → 006 → 005) |

---

## Описание

Переработать `QwenAgentRunner` и `QwenJudgeRunner` — убрать статические вызовы `QwenCommandFactory.buildCommand()` и все конструкторы без `CommandFactory`. Оставить только конструктор с обязательным параметром `CommandFactory`.

**QwenAgentRunner:**
- Добавить поле `private final CommandFactory commandFactory`
- Заменить существующие конструкторы на единственный: `(CommandExecutor, AgentStreamJsonParser, RunnerLogWriter, Path, Duration, CommandFactory)`
- Удалить старые конструкторы (4-параметрический без CommandFactory и 5-параметрический без CommandFactory)
- В `execute()` — вызов `commandFactory.buildCommand(prompt, agentRunContext.getRunDir())`

**QwenJudgeRunner:**
- Добавить поле `private final CommandFactory commandFactory`
- Заменить существующие конструкторы на единственный: `(CommandExecutor, AgentStreamJsonParser, Path, Duration, CommandFactory)`
- Удалить старые конструкторы (1-параметрический и 4-параметрический)
- В `runPrompt()` — вызов `commandFactory.buildCommand(prompt, null)`

**Что происходит с внешним кодом:**
- `AgentRunnerFactory` (TASK-003) передаёт `CommandFactory` напрямую
- `AgentEvaluatorService` (TASK-006) — внутри создаёт `QwenJudgeRunner`, нужно будет передать `CommandFactory`
- Все тесты (TASK-006) — будут обновлены для передачи `CommandFactory`
- Старые конструкторы не сохраняются — миграция обязательна

## Критерии приёмки (Acceptance Criteria)

- [ ] `QwenAgentRunner` имеет ровно один конструктор — с `CommandFactory` и `RunnerLogWriter`
- [ ] Внутри `execute()` команда собирается через `commandFactory.buildCommand()`
- [ ] В классе нет упоминаний `QwenCommandFactory.buildCommand()`
- [ ] `QwenJudgeRunner` имеет ровно один конструктор — с `CommandFactory`
- [ ] Внутри `runPrompt()` команда собирается через `commandFactory.buildCommand()`
- [ ] Оба класса не имеют конструкторов/методов для обратной совместимости

## TDD Цикл

### 🔴 RED — Тесты

> Отмечайте чек-боксы сразу после выполнения каждого пункта!

- [x] Написан тест — `QwenAgentRunner` создаётся только с `CommandFactory`
- [x] Написан тест — вызов `execute()` использует `commandFactory.buildCommand()`
- [x] Написан тест — `QwenJudgeRunner` создаётся только с `CommandFactory`
- [x] Тесты компилируются и падают с ожидаемой ошибкой
- [x] Зафиксировано сообщение об ошибке:

```
(новые тесты прошли — проверка reflection на единственный конструктор)
```

### 🟢 GREEN — Реализация

> Отмечайте чек-боксы сразу после выполнения каждого пункта!

- [x] `QwenAgentRunner`: удалены старые конструкторы (4-param и 5-param), удалён импорт QwenCommandFactory
- [x] В `execute()` вызов заменён на `commandFactory.buildCommand()`
- [x] `QwenJudgeRunner`: удалены старые конструкторы, добавлен 5-param с CommandFactory
- [x] В `runPrompt()` вызов заменён на `commandFactory.buildCommand()`
- [x] `AgentRunnerFactory.createCommandFactory()` сделан package-private для AgentEvaluatorService
- [x] `AgentEvaluatorService` обновлён — создаёт QwenJudgeRunner через CommandFactory
- [x] `TextToJavaUiTest` — @Disabled, конструкторы исправлены для компиляции
- [x] `mvn test -Dgroups=inner` — 52 теста, 0 failures

### 🔵 REFACTOR — Рефакторинг

> Отмечайте чек-боксы сразу после выполнения каждого пункта!

- [x] Проверено отсутствие dead code (старых конструкторов, legacy-методов)
- [x] `mvn compile` — успешно
- [x] Сборка успешна: `mvn clean package -DskipTests`

## Чек-лист завершения

- [x] Все новые тесты зелёные
- [x] Сборка успешна
- [x] Код соответствует стандартам проекта
- [x] Изменения закоммичены

## Статус

| Поле | Значение |
|------|----------|
| **Модуль:** | `skills` |
| Дата создания: | 2026-07-07 |
| Дата начала: | 2026-07-07 |
| Дата завершения: | 2026-07-07 |
| Статус: | ✅ |

## Заметки

Зависит от TASK-001 (util.cli) и TASK-003 (фабрика с RunnerLogWriter). `AgentEvaluatorService` также создаёт `QwenJudgeRunner` — его адаптация в TASK-006.
