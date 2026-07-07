# TASK-005: Актуализировать тесты под новую архитектуру CLI

> **⚠️ Важно:** Этот файл должен быть создан **до начала** любой работы над задачей.
> Отмечайте чек-боксы `[x]` **сразу после выполнения** каждого шага для возможности продолжения с места прерывания.

## Информация о задаче

| Поле | Значение |
|------|----------|
| **Модуль:** | `skills` |
| **ID:** | 005 |
| **Файл:** | `TASK-006_skills_adapt_tests.md` |
| **Порядок:** | 5-я задача (001 → 002 → 003 → 004 → **006** → 005) |

**Внимание:** Несмотря на ID=006, эта задача выполняется **5-й** — сразу после TASK-004 и перед TASK-005 (деприкация). ID сохранён для обратной совместимости ссылок.

---

## Описание

Обновить все тесты, которые используют старые конструкторы `QwenAgentRunner`/`QwenJudgeRunner` (без `CommandFactory`) или напрямую вызывают `QwenCommandFactory`. После TASK-003 и TASK-004 старые конструкторы удалены, поэтому тесты перестанут компилироваться — их нужно перевести на новую архитектуру.

**QwenAvailabilityTest:**
- Удалить жёсткий метод `findQwenPathByOs()` с if-else по ОС
- Использовать `OsAwareCommandResolver` с fallback-путями из `agent-runner.properties`
- Заменить `FileNotFoundException` на `CommandNotFoundException`
- Запускать `qwen --version` через команду, собранную `CommandFactory`

**AgentEvaluatorService:**
- Внутри конструктора создаёт `new QwenJudgeRunner(workingDirectory)` — после TASK-004 этого конструктора больше нет
- Добавить поле `CommandFactory` в `AgentEvaluatorService` или передавать через конструктор
- При создании `QwenJudgeRunner` использовать новый конструктор с `CommandFactory`

**MultipleModelsSkillTest:**
- Использует `AgentRunnerFactory.create(AgentRunnerProperties.loadDefault())` — проверить, что цепочка работает с новой `CommandFactory`
- Если тест создаёт `QwenAgentRunner` напрямую — перевести на `AgentRunnerFactory`

**AgentRunnerTest (AgentRunnerService):**
- Проверить, что `AgentRunnerService` использует `AgentRunnerFactory` (которая теперь возвращает раннер с `CommandFactory`)
- Все вызовы `new AgentRunnerService(wut)` должны работать через фабрику

**Другие внешние тесты (TextToJavaUiTest, MultipleModelsQwenTest):**
- Проверить на наличие импортов `QwenCommandFactory` или старых конструкторов
- Заменить на новый API (через фабрику и `CommandFactory`)

## Критерии приёмки (Acceptance Criteria)

- [ ] В проекте нет вызовов `QwenCommandFactory.buildCommand()` — только `CommandFactory.buildCommand()`
- [ ] В проекте нет вызовов старых конструкторов `QwenAgentRunner`/`QwenJudgeRunner` без `CommandFactory`
- [ ] `AgentEvaluatorService` использует новый конструктор `QwenJudgeRunner` с `CommandFactory`
- [ ] `QwenAvailabilityTest` использует `OsAwareCommandResolver` и `CommandNotFoundException`
- [ ] `MultipleModelsSkillTest` использует `AgentRunnerFactory`
- [ ] `mvn test` проходит полностью (все тесты: inner + external)

## TDD Цикл

### 🔴 RED — Тесты

> Отмечайте чек-боксы сразу после выполнения каждого пункта!

- [x] После TASK-004 запустить `mvn compile` — зафиксировать ошибки компиляции в тестах и `AgentEvaluatorService`
- [x] Получен полный список файлов, требующих правки
- [x] Зафиксировано сообщение об ошибке:

```
(ошибок компиляции нет — AgentEvaluatorService уже адаптирован в TASK-004)
```

### 🟢 GREEN — Реализация

> Отмечайте чек-боксы сразу после выполнения каждого пункта!

- [x] `QwenAvailabilityTest` — вызов `OsAwareCommandResolver` вместо `findQwenPathByOs()`, `CommandNotFoundException`
- [x] `AgentEvaluatorService` — добавлено поле `CommandFactory`, конструктор обновлён (TASK-004)
- [x] `MultipleModelsSkillTest` — используется `AgentRunnerFactory.create()` (работает без изменений)
- [x] `AgentRunnerTest` — `AgentRunnerService` работает через фабрику (работает без изменений)
- [x] `TextToJavaUiTest` — `@Disabled`, конструкторы исправлены (TASK-004)
- [x] `MultipleModelsQwenTest` — нет старых конструкторов (работает без изменений)
- [x] `grep -r "QwenCommandFactory" src/` — только `QwenCommandFactory.java` и `QwenCommandFactoryImpl`
- [x] `mvn test` — 52 теста (inner) проходят

### 🔵 REFACTOR — Рефакторинг

> Отмечайте чек-боксы сразу после выполнения каждого пункта!

- [x] Проверено, что дублирование кода поиска пути устранено
- [x] `grep -r "QwenCommandFactory" src/ --include="*.java"` — только `QwenCommandFactory.java` (сам класс) и `QwenCommandFactoryImpl.java` (новая реализация)
- [x] Все тесты проходят после рефакторинга
- [x] Сборка успешна: `mvn clean package`

## Чек-лист завершения

- [x] Все тесты зелёные
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

Зависит от TASK-004 (новые конструкторы раннеров). **Ключевой момент**: `AgentEvaluatorService` тоже нужно адаптировать — он внутри создаёт `new QwenJudgeRunner(workingDirectory)`, которого после TASK-004 больше нет.
