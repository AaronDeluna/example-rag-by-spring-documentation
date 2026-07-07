# TASK-002: Расширить agent-runner.properties и AgentRunnerProperties

> **⚠️ Важно:** Этот файл должен быть создан **до начала** любой работы над задачей.
> Отмечайте чек-боксы `[x]` **сразу после выполнения** каждого шага для возможности продолжения с места прерывания.

## Информация о задаче

| Поле | Значение |
|------|----------|
| **Модуль:** | `skills` |
| **ID:** | 002 |
| **Файл:** | `TASK-002_skills_config_properties.md` |
| **Порядок:** | 2-я задача (001 → 002 → 003 → 004 → 006 → 005) |

---

## Описание

Добавить в `agent-runner.properties` новые параметры конфигурации CLI. Дополнить класс `AgentRunnerProperties` методами чтения — это единственный источник конфигурации для новой архитектуры, старые захардкоженные пути в `QwenCommandFactory` больше не используются.

Новые параметры:
- `agent.cli.qwen.fallback.linux` — fallback-пути для Linux (разделитель `;`)
- `agent.cli.qwen.fallback.windows` — fallback-пути для Windows
- `agent.cli.qwen.fallback.mac` — fallback-пути для macOS
- `agent.cli.qwen.args` — базовые аргументы (без prompt)
- `agent.cli.qwen.prefix.windows` — префикс команды для Windows (например, `cmd.exe,/c`)

Файлы конфигурации:
1. **`src/main/resources/agent-runner.properties`** — шаблон для основного приложения со всеми новыми параметрами и комментариями (значения по умолчанию, актуальные для текущей системы разработчика)
2. **`src/test/resources/agent-runner.properties`** — тестовый файл, переопределяет значения при необходимости

## Критерии приёмки (Acceptance Criteria)

- [ ] Создан `src/main/resources/agent-runner.properties` со всеми новыми параметрами и комментариями
- [ ] В `src/test/resources/agent-runner.properties` добавлены новые параметры (если нужны переопределения для тестов)
- [ ] `AgentRunnerProperties` читает все новые параметры
- [ ] Методы возвращают пустые списки по умолчанию, если параметры отсутствуют
- [ ] Значения по умолчанию не дублируют логику из `QwenCommandFactory` — конфигурация полностью в properties

## TDD Цикл

### 🔴 RED — Тесты

> Отмечайте чек-боксы сразу после выполнения каждого пункта!

- [x] Написан тест `AgentRunnerPropertiesTest` — чтение fallback-путей, аргументов, префикса
- [x] Написан тест — значения по умолчанию при отсутствии параметров
- [x] Тесты компилируются и падают с ожидаемой ошибкой
- [x] Зафиксировано сообщение об ошибке:

```
[ERROR] AgentRunnerPropertiesTest.java:[25,49] cannot find symbol
  symbol:   method getFallbackPaths(Properties, OsType)
[ERROR] AgentRunnerPropertiesTest.java:[60,50] cannot find symbol
  symbol:   method getBaseArgs(Properties)
[ERROR] AgentRunnerPropertiesTest.java:[85,52] cannot find symbol
  symbol:   method getPrefix(Properties, OsType)
```

### 🟢 GREEN — Реализация

> Отмечайте чек-боксы сразу после выполнения каждого пункта!

- [x] Создан `src/main/resources/agent-runner.properties` с новыми параметрами и комментариями
- [x] Обновлён `src/test/resources/agent-runner.properties`
- [x] В `AgentRunnerProperties` добавлены константы ключей и методы чтения: `getFallbackPaths()`, `getBaseArgs()`, `getPrefix()`
- [x] Все тесты проходят: `mvn test -Dgroups=inner` (43 теста, 0 failures)

### 🔵 REFACTOR — Рефакторинг

> Отмечайте чек-боксы сразу после выполнения каждого пункта!

- [x] Проверена обработка краевых случаев (null, пустые строки)
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

Методы stateless — читают из Properties при каждом вызове. `src/main/resources/agent-runner.properties` создаётся как шаблон с комментариями; тестовый файл может переопределять значения.
