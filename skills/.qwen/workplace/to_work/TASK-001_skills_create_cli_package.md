# TASK-001: Создать пакет util.cli — OsType, CommandResolver, CommandFactory и реализации

> **⚠️ Важно:** Этот файл должен быть создан **до начала** любой работы над задачей.
> Отмечайте чек-боксы `[x]` **сразу после выполнения** каждого шага для возможности продолжения с места прерывания.

## Информация о задаче

| Поле | Значение |
|------|----------|
| **Модуль:** | `skills` |
| **ID:** | 001 |
| **Файл:** | `TASK-001_skills_create_cli_package.md` |
| **Порядок:** | 1-я задача (001 → 002 → 003 → 004 → 006 → 005) |

---

## Описание

Создать новый пакет `org.mirent.skills.util.cli` — база для новой CLI-архитектуры без жёсткой привязки к Qwen и ОС.

Состав пакета:
- `OsType` — enum определения ОС (WINDOWS, MAC, LINUX, OTHER) с методом `detect()`
- `CommandResolver` — интерфейс поиска исполняемого файла
- `OsAwareCommandResolver` — реализация: поиск в PATH, затем fallback-пути по ОС
- `CommandFactory` — интерфейс сборки команды CLI
- `QwenCommandFactoryImpl` — реализация для Qwen CLI: префикс (Windows), базовые аргументы, OpenAI-логирование
- `CommandNotFoundException` — исключение в `org.mirent.skills.exeptions`, от `AgentRunnerConfigurationException`

Все старые места (QwenAgentRunner, QwenJudgeRunner, QwenCommandFactory) будут переведены на эти новые классы в последующих задачах. Пакет проектируется как единственная точка сборки команды — дублирование исключено.

## Критерии приёмки (Acceptance Criteria)

- [ ] `OsType.detect()` корректно определяет Linux, macOS, Windows
- [ ] `OsAwareCommandResolver` находит исполняемый файл в PATH
- [ ] `OsAwareCommandResolver` использует fallback-пути, если файл не найден в PATH — при этом корректно обрабатывает как путь к конкретному файлу (например, `cli-entry.js`), так и путь к директории (ищет внутри файл с именем команды)
- [ ] `OsAwareCommandResolver` бросает `CommandNotFoundException`, если файл не найден нигде
- [ ] `QwenCommandFactoryImpl.buildCommand()` собирает команду: [префикс] executable [базовые_аргументы] [--openai-logging ...] prompt
- [ ] `QwenCommandFactoryImpl` добавляет `--openai-logging` флаги при наличии logDir
- [ ] `QwenCommandFactoryImpl` добавляет префикс для Windows
- [ ] `CommandNotFoundException` наследуется от `AgentRunnerConfigurationException`

## TDD Цикл

### 🔴 RED — Тесты

> Отмечайте чек-боксы сразу после выполнения каждого пункта!

- [x] Написан `OsTypeTest` — detect на Linux возвращает LINUX
- [x] Написан `OsTypeTest` — detect на macOS возвращает MAC
- [x] Написан `OsTypeTest` — detect на Windows возвращает WINDOWS
- [x] Написан `OsAwareCommandResolverTest` — поиск в PATH (через подмену `System.getenv("PATH")` или временный файл)
- [x] Написан `OsAwareCommandResolverTest` — fallback-путь как конкретный файл (например, /tmp/qwen-cli.js)
- [x] Написан `OsAwareCommandResolverTest` — fallback-путь как директория (создаётся /tmp/fallback/bin/qwen)
- [x] Написан `OsAwareCommandResolverTest` — `CommandNotFoundException` при ненайденном файле
- [x] Написан `QwenCommandFactoryImplTest` — сборка команды без logDir
- [x] Написан `QwenCommandFactoryImplTest` — сборка команды с logDir (флаги --openai-logging, --openai-logging-dir)
- [x] Написан `QwenCommandFactoryImplTest` — префикс cmd.exe /c для Windows
- [x] Написан `CommandNotFoundExceptionTest` — наследование от AgentRunnerConfigurationException
- [x] Тесты компилируются и падают с ожидаемой ошибкой
- [x] Зафиксировано сообщение об ошибке:

```
[ERROR] /.../OsAwareCommandResolverTest.java:[7,35] cannot find symbol
  symbol:   class CommandNotFoundException
  location: package org.mirent.skills.exeptions
[ERROR] /.../OsAwareCommandResolverTest.java:[8,34] package org.mirent.skills.util.cli does not exist
[ERROR] /.../QwenCommandFactoryImplTest.java:[7,34] package org.mirent.skills.util.cli does not exist
[ERROR] /.../OsTypeTest.java:[6,34] package org.mirent.skills.util.cli does not exist
[ERROR] /.../CommandNotFoundExceptionTest.java:[7,35] cannot find symbol
  symbol:   class CommandNotFoundException
```

### 🟢 GREEN — Реализация

> Отмечайте чек-боксы сразу после выполнения каждого пункта!

- [x] Создан enum `OsType` с методом `detect()`
- [x] Создан интерфейс `CommandResolver` и реализация `OsAwareCommandResolver`
- [x] Создан интерфейс `CommandFactory` и реализация `QwenCommandFactoryImpl`
- [x] Создано исключение `CommandNotFoundException`
- [x] Все тесты проходят: `mvn test -Dgroups=inner` (34 теста, 0 failures)

### 🔵 REFACTOR — Рефакторинг

> Отмечайте чек-боксы сразу после выполнения каждого пункта!

- [x] Проверена читаемость и именование
- [x] Устранено дублирование
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

Новый пакет не зависит от существующего кода. Все старые вызовы будут мигрированы в TASK-003–006.
