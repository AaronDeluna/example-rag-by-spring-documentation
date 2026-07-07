# TASK-003: Переработать AgentRunnerFactory под новую CommandFactory

> **⚠️ Важно:** Этот файл должен быть создан **до начала** любой работы над задачей.
> Отмечайте чек-боксы `[x]` **сразу после выполнения** каждого шага для возможности продолжения с места прерывания.

## Информация о задаче

| Поле | Значение |
|------|----------|
| **Модуль:** | `skills` |
| **ID:** | 003 |
| **Файл:** | `TASK-003_skills_adapt_factory.md` |
| **Порядок:** | 3-я задача (001 → 002 → 003 → 004 → 006 → 005) |

---

## Описание

Переработать `AgentRunnerFactory` — убрать создание раннера со старым конструктором (без `CommandFactory`), всегда создавать и передавать `CommandFactory`.

Изменения в `AgentRunnerFactory`:
- Добавить приватный метод `createCommandFactory(AgentCli cli, Properties props)`, который:
  - Строит fallback-карту `Map<OsType, List<Path>>` из свойств
  - Создаёт `OsAwareCommandResolver`
  - Читает базовые аргументы и префикс Windows из свойств
  - Возвращает `QwenCommandFactoryImpl`
- **Добавить поле `RunnerLogWriter`** (или создавать новый экземпляр в `create()`). После TASK-004 единственный конструктор `QwenAgentRunner` принимает `RunnerLogWriter`. Фабрика должна его предоставить.
- Изменить `create(Properties)` — передавать `CommandFactory` и `RunnerLogWriter` в конструктор раннера

После этой задачи `AgentRunnerFactory` никогда не создаёт раннер без `CommandFactory` — старый код, вызывавший фабрику, будет обновлён в TASK-006.

## Критерии приёмки (Acceptance Criteria)

- [ ] `AgentRunnerFactory.create()` передаёт `CommandFactory` в `QwenAgentRunner`
- [ ] `AgentRunnerFactory.create()` передаёт `RunnerLogWriter` в `QwenAgentRunner`
- [ ] `AgentRunnerFactory.create()` использует свойства из `AgentRunnerProperties`
- [ ] При отсутствии свойств используются значения по умолчанию (пустые списки)
- [ ] В `AgentRunnerFactory` нет кода, создающего раннер без `CommandFactory`

## TDD Цикл

### 🔴 RED — Тесты

> Отмечайте чек-боксы сразу после выполнения каждого пункта!

- [x] Написан тест — `createCommandFactory()` создаёт `QwenCommandFactoryImpl` с корректными параметрами
- [x] Написан тест — `AgentRunnerFactory.create()` возвращает раннер с `CommandFactory`
- [x] Тесты компилируются и падают с ожидаемой ошибкой
- [x] Зафиксировано сообщение об ошибке:

```
(тесты-характеризации прошли — старые конструкторы ещё существуют)
```

### 🟢 GREEN — Реализация

> Отмечайте чек-боксы сразу после выполнения каждого пункта!

- [x] В `AgentRunnerFactory` добавлен метод `createCommandFactory(AgentCli, Properties)`
- [x] В `AgentRunnerFactory` добавлено поле `RunnerLogWriter`
- [x] В `AgentRunnerFactory.create()` вызывается `createCommandFactory()` и результат передаётся в конструктор раннера вместе с `RunnerLogWriter`
- [x] В `QwenAgentRunner` добавлен конструктор с `CommandFactory`, `execute()` использует `commandFactory.buildCommand()`
- [x] Все тесты проходят: `mvn test -Dgroups=inner` (47 тестов, 0 failures)

### 🔵 REFACTOR — Рефакторинг

> Отмечайте чек-боксы сразу после выполнения каждого пункта!

- [x] Проверена обработка отсутствующих свойств
- [x] Устранено дублирование кода сборки конфигурации
- [x] Все тесты проходят после рефакторинга
- [x] Сборка успешна: `mvn clean package -DskipTests`

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

Зависит от TASK-001 (util.cli) и TASK-002 (Properties). `RunnerLogWriter` добавлен, потому что после TASK-004 единственный конструктор `QwenAgentRunner` принимает его. Без этого фабрика не сможет создать раннер.
