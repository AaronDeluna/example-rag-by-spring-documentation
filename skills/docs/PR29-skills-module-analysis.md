# Анализ модуля `skills` после merge PR #29

> **Дата анализа:** 7 июля 2026  
> **Контекст:** 86 изменённых файлов, +7262 / −1531 строк  
> **Ветка:** `feature/add-mcp-frap-tests` → `master`

---

## 1. Что изменилось в PR #29

### 1.1 Новая абстракция CLI-команд: `CommandFactory` / `CommandResolver` / `OsAwareCommandResolver`

Это главное архитектурное изменение PR. Ранее сборка CLI-команды была зашита непосредственно внутри `QwenAgentRunner` и `QwenJudgeRunner` — жёсткая зависимость от конкретного пути к исполняемому файлу. Теперь введена трёхслойная абстракция:

```
CommandFactory          — «что запускать» (интерфейс — строит List<String> команду)
  └─ QwenCommandFactoryImpl  — конкретная реализация для Qwen CLI
        └─ CommandResolver    — «где найти исполняемый файл» (интерфейс)
              └─ OsAwareCommandResolver — OS-aware поиск: env-var → PATH → fallback-пути
```

**Было (до PR):**  
Путь к `qwen` зашивался в properties или напрямую в код раннера. Раннеры знали об ОС, путях и аргументах одновременно.

**Стало (после PR):**  
- `CommandFactory.buildCommand(prompt, logDir)` — единственная точка входа для сборки команды; раннер не знает ни про пути, ни про ОС.  
- `CommandResolver.resolveExecutable(commandName)` — изолированная логика поиска бинарника.  
- `OsAwareCommandResolver` — реализует трёхшаговый поиск: сначала `QWEN_PATH` env-переменная, затем системный `PATH`, затем fallback-пути из конфигурации (с поддержкой Windows `.exe`/`.cmd`/`.bat`-расширений).  
- `OsType` enum — детектирует ОС через `System.getProperty("os.name")`.  
- `AgentRunnerFactory.createCommandFactory(AgentCli, Properties)` — фабричный метод, собирающий граф зависимостей `OsAwareCommandResolver → QwenCommandFactoryImpl` из `agent-runner.properties`.

### 1.2 Новые ключи `agent-runner.properties`

Добавлены три группы ключей конфигурации:

| Ключ | Назначение |
|---|---|
| `agent.cli.qwen.fallback.<os>` | Список путей (разделитель `;`) для поиска `qwen` на конкретной ОС |
| `agent.cli.qwen.args` | Базовые аргументы CLI (разделитель `,`), например `--output-format,stream-json,--approval-mode,yolo` |
| `agent.cli.qwen.prefix.windows` | Префикс команды только для Windows, например `cmd.exe,/c` |

Ключи поддерживают подстановку переменных: `${user.home}`, `${env.HOME}`, `${env.USERPROFILE}`, `$HOME`, `$USERPROFILE`.

### 1.3 Удалён пакет `spec/`

Из `src/main/java/org/mirent/skills/spec/` удалены 6 классов:

| Класс | Что делал |
|---|---|
| `SkillRenderer` | Интерфейс рендеринга `SkillSpec` → `SKILL.md` |
| `QwenSkillRenderer` | Реализация: генерировал YAML-фронтматтер + тело SKILL.md для Qwen |
| `Constraints` | DTO: ограничения скилла (maxInputLength, allowedOperations, timeoutSeconds) |
| `Example` | DTO: пара input/output для примеров в скилле |
| `Resources` | DTO: список ресурсов скилла (files, urls) |
| `ResponseTemplate` | DTO: шаблон ответа (format + content с плейсхолдерами) |

**Причина удаления:** Эти классы реализовывали программную генерацию файлов `SKILL.md` (спецификации скиллов), но на практике скиллы пишутся вручную или AI-агентом, а не генерируются Java-кодом. Пакет был мёртвым кодом.

### 1.4 Изменения в раннерах

**`QwenAgentRunner`** и **`QwenJudgeRunner`** — удалена вся логика сборки команды из конструктора и метода `execute`. Теперь:
- В конструктор добавлен параметр `CommandFactory commandFactory` — обязательный, без дефолта.
- Метод `execute` вызывает `commandFactory.buildCommand(prompt, logDir)` — единственный вызов.
- Статические константы путей к `qwen` убраны полностью.
- Добавлена рефлекторная проверка в тестах: у обоих раннеров ровно один публичный конструктор, и он принимает `CommandFactory`.

### 1.5 Добавленные тесты

**Unit-тесты (тег `inner`, `unit`):**

| Тест-класс | Что покрывает |
|---|---|
| `OsTypeTest` | `OsType.detect()` — возвращает корректный тип, не null |
| `OsAwareCommandResolverTest` | Поиск файла как конкретного пути, поиск по имени в директории, `CommandNotFoundException` при отсутствии, корректный выбор fallback по текущей ОС |
| `QwenCommandFactoryImplTest` | Сборка команды без logDir, с logDir (→ `--openai-logging`), с Windows-префиксом, неизменяемость результирующего списка |
| `CommandNotFoundExceptionTest` | Иерархия исключений, содержание сообщения |
| `RunnersConstructorTest` | Конструктор раннеров принимает `CommandFactory`; ровно 1 публичный конструктор у обоих раннеров |
| `AgentRunnerFactoryTest` | `defaultFactory` создаётся без ошибок; `create(props)` возвращает раннер при валидных и минимальных свойствах |
| `AgentRunnerPropertiesTest` | Чтение fallback-путей для Linux/Windows; чтение baseArgs; чтение prefix для Windows; обработка пустых строк; trim вокруг разделителей |
| `AgentCliTest` | `fromProperty("")` → `MissingAgentCliException`; `fromProperty("unknown")` → `UnsupportedAgentCliException`; парсинг без учёта регистра |
| `AgentEvaluatorServiceTest` | Парсинг чистого JSON; парсинг JSON в markdown-фенсах; исключение на пустой ответ; исключение на невалидный JSON |
| `WutPreparerTest` | 8 сценариев: копирование с вложенными папками, создание `sorce`+`logs`, накопление запусков, ошибки при отсутствии/неверном источнике, явные buildDirectory и wutTargetPath |
| `QwenAvailabilityTest` | Smoke: запускает `qwen --version`, проверяет exitCode=0 и версию `0.19.6` |

**Интеграционные тесты (тег `inner`, `integration`):**

| Тест-класс | Что покрывает |
|---|---|
| `AgentRunContextTest` | Вычисление `workspace`, `runDir`, `runId` из реальной структуры WUT; `.qwen` лежит только в `sorce` |
| `AgentRunnerTest` | Запуск агента с шаблонами `default`, `text-utils`, `case-1/2/3`; прямой вызов скилла через `executeSkillPrompt`; оценка через `AgentEvaluatorService` (score ≥ 0.7) |
| `WorkspacePreparerTest` | Подготовка workspace из шаблонов `default` и `text-utils`; замена скиллов при смене шаблона |

**Внешние тесты (тег `external`, требуют реального CLI):**

| Тест-класс | Что покрывает |
|---|---|
| `MultipleModelsQwenTest` | Прогон `arithmetic` скилла на 9 разных моделях |
| `MultipleModelsSkillTest` | Прогон `maven-checkstyle-setup` скилла, проверка файловой системы (создан `checkstyle.xml`, обновлён `pom.xml`) |
| `TextToJavaUiTest` | Генерация Selenide-теста через скилл `generate-java-selenide-test`, компиляция через Maven, оценка судьёй, запуск теста |

### 1.6 Добавленная документация (`skills/docs/`)

| Файл | Содержание |
|---|---|
| `docs/other/sdk.md` | Архитектурные рекомендации по выделению SDK (`agentic-cli-runner`): модульность, SPI, DTO-дизайн, конфигурация |
| `docs/skills/agent-skills-spec.md` | Спецификация формата скиллов для Qwen Code CLI |
| `docs/skills/qwen-code-skill-doc.md` | Документация по написанию скиллов |
| `docs/skills/skill-spec-schema.json` | JSON Schema для валидации `SKILL.md` |
| `docs/checkstyle/doc.md` | Документация по checkstyle-скиллу |
| `docs/frap-mcp/FRAP-README.md` | Документация по FRAP MCP-тестированию |

---

## 2. Полный каталог Java-классов модуля `skills`

### 2.1 Корневой пакет `org.mirent.skills`

#### `CommandExecutor`
**Назначение:** Низкоуровневый исполнитель системных команд через Apache Commons Exec.  
**Публичный API:**
```java
CommandResultDto execute(CommandRequestDto request) throws Exception
```
Принимает команду, рабочую директорию и таймаут; возвращает stdout, stderr, exitCode, timedOut.

---

### 2.2 Пакет `util/cli` — Абстракция CLI-команд

#### `CommandFactory` _(интерфейс)_
**Назначение:** Стратегия сборки команды для запуска конкретного CLI-агента.  
**Публичный API:**
```java
List<String> buildCommand(String prompt, Path logDir)
```
`logDir == null` → без logging-флагов.

#### `CommandResolver` _(интерфейс)_
**Назначение:** Стратегия поиска исполняемого файла CLI на файловой системе.  
**Публичный API:**
```java
String resolveExecutable(String commandName) throws CommandNotFoundException
```

#### `OsAwareCommandResolver` _(implements CommandResolver)_
**Назначение:** Трёхшаговый поиск бинарника: env-var `<COMMAND>_PATH` → системный `PATH` → fallback-пути из конфигурации. Поддерживает расширения `.exe`/`.cmd`/`.bat`/`.com` для Windows.  
**Конструктор:**
```java
OsAwareCommandResolver(Map<OsType, List<Path>> fallbackPaths)
```
OS детектируется автоматически при создании через `OsType.detect()`.

#### `QwenCommandFactoryImpl` _(implements CommandFactory)_
**Назначение:** Конкретная реализация `CommandFactory` для Qwen CLI. Собирает команду в виде: `[prefix...] <executable> [baseArgs...] [--openai-logging true --openai-logging-dir <logDir>] <prompt>`.  
**Конструктор:**
```java
QwenCommandFactoryImpl(CommandResolver resolver, List<String> baseArgs, List<String> prefix)
```

#### `OsType` _(enum)_
**Назначение:** Перечисление типов ОС с автодетектом.  
**Значения:** `WINDOWS`, `MAC`, `LINUX`, `OTHER`  
**Публичный API:**
```java
static OsType detect()   // читает System.getProperty("os.name")
```

---

### 2.3 Пакет `service` — Точки входа и конфигурация

#### `AgentRunnerService` _(implements AgentRunner)_
**Назначение:** Главная точка входа для конечного пользователя библиотеки. Делегирует к `QwenAgentRunner`, созданному через фабрику.  
**Конструктор:**
```java
AgentRunnerService(Path workspace)   // читает agent-runner.properties из classpath или файловой системы
```
**Публичный API:**
```java
AgentResultDto executeUserPrompt(String prompt) throws Exception
AgentResultDto executeSkillPrompt(String skillName, String prompt) throws Exception
```

#### `AgentRunnerFactory`
**Назначение:** Фабрика раннеров. Принимает `CommandExecutor`, парсер, рабочую директорию, таймаут, `RunnerLogWriter`; на основе `Properties` создаёт нужный раннер с правильной `CommandFactory`.  
**Публичный API:**
```java
static AgentRunnerFactory defaultFactory(Path workspace)
QwenAgentRunner create(Properties properties)
static CommandFactory createCommandFactory(AgentCli cli, Properties props)   // package-private
```

#### `AgentRunnerProperties`
**Назначение:** Загрузчик и парсер `agent-runner.properties`. Поддерживает classpath, файловую систему, подстановку переменных окружения и системных свойств.  
**Константы:**
```java
String DEFAULT_PROPERTIES_FILE = "agent-runner.properties"
String CLI_PROPERTY = "agent.cli"
```
**Публичный API:**
```java
static Properties loadDefault()
static List<Path> getFallbackPaths(Properties props, OsType os)
static List<String> getBaseArgs(Properties props)
static List<String> getPrefix(Properties props, OsType os)
```

#### `AgentCli` _(enum)_
**Назначение:** Перечисление поддерживаемых CLI-агентов.  
**Значения:** `QWEN`  
**Публичный API:**
```java
static AgentCli fromProperty(String value)   // throws MissingAgentCliException / UnsupportedAgentCliException
```

#### `AgentEvaluatorService`
**Назначение:** Оценщик качества ответа агента через CLI-судью. Отправляет LLM структурированный промпт с запросом пользователя и трейсом событий агента; парсит JSON-ответ (`score` + `problemMessage`). Умеет снимать markdown-фенсы ` ```json ``` ` из ответа.  
**Конструкторы:**
```java
AgentEvaluatorService(Path workspace)           // создаёт QwenJudgeRunner из properties
AgentEvaluatorService(JudgeRunner judgeRunner)  // для тестов — подменить судью
```
**Публичный API:**
```java
EvaluateResultDto evaluate(EvaluateDto agentEvaluateRequestDto) throws Exception
```

---

### 2.4 Пакет `runner` — Раннеры и контекст

#### `AgentRunner` _(интерфейс)_
**Назначение:** Основной контракт запуска агента.  
**Публичный API:**
```java
AgentResultDto executeUserPrompt(String prompt) throws Exception
AgentResultDto executeSkillPrompt(String skillName, String prompt) throws Exception
```

#### `JudgeRunner` _(интерфейс)_
**Назначение:** Контракт запуска агента в режиме судьи (оценщика).  
**Публичный API:**
```java
String runPrompt(String prompt) throws Exception
```

#### `AgentRunContext`
**Назначение:** Типизированный вид над структурой одного запуска WUT. Из пути к `sorce` вычисляет `runId`, `logsDir`.  
**Конструктор:**
```java
AgentRunContext(Path sourceDir)
```
**Публичный API:**
```java
String getRunId()       // UUID папки запуска
Path getWorkspace()     // путь к sorce (рабочая директория агента)
Path getRunDir()        // путь к logs
```

#### `RunnerLogWriter`
**Назначение:** Записывает `AgentRunLogDto` в файл `logs/log.json` в JSON с отступами.  
**Публичный API:**
```java
void write(AgentRunContext context, AgentRunLogDto entry)
```

#### `runner/qwen/QwenAgentRunner` _(implements AgentRunner)_
**Назначение:** Раннер для Qwen CLI. Создаёт `AgentRunContext`, вызывает `commandFactory.buildCommand()`, исполняет через `CommandExecutor`, парсит stdout через `AgentStreamJsonParser`, пишет лог.  
**Константа:** `Duration DEFAULT_TIMEOUT = 3 минуты`  
**Конструктор:**
```java
QwenAgentRunner(CommandExecutor, AgentStreamJsonParser, RunnerLogWriter, Path workingDirectory, Duration timeout, CommandFactory)
```
**Публичный API:**
```java
AgentResultDto executeUserPrompt(String prompt) throws Exception
AgentResultDto executeSkillPrompt(String skillName, String prompt) throws Exception
AgentRunContext getAgentRunContext()   // @Getter Lombok
```

#### `runner/qwen/QwenJudgeRunner` _(implements JudgeRunner)_
**Назначение:** Раннер-судья для Qwen CLI. Не пишет логи, возвращает только `finalResult`.  
**Константа:** `Duration DEFAULT_TIMEOUT = 5 минут`  
**Конструктор:**
```java
QwenJudgeRunner(CommandExecutor, AgentStreamJsonParser, Path workingDirectory, Duration timeout, CommandFactory)
```
**Публичный API:**
```java
String runPrompt(String prompt) throws Exception
```

---

### 2.5 Пакет `parser`

#### `AgentStreamJsonParser`
**Назначение:** Парсер stream-json вывода Qwen CLI. Каждая строка stdout — отдельный JSON-объект. Ищет событие `{type: "result"}` и извлекает `finalResult`.  
**Конструктор:**
```java
AgentStreamJsonParser()
AgentStreamJsonParser(ObjectMapper objectMapper)
```
**Публичный API:**
```java
AgentLogDto parse(String streamJson) throws Exception
```

---

### 2.6 Пакет `matcher`

#### `AgentMatcher`
**Назначение:** Набор assertion-методов для тестирования агентов (не JUnit-специфичных — бросают `AssertionError`).  
**Публичный API:**
```java
static void evaluate(EvaluateResultDto result, double threshold)
static void assertSuccessful(AgentResultDto result)
static void assertSingleSkillCall(AgentResultDto result, String expectedSkillName)
static void assertSkillCallsInOrder(AgentResultDto result, List<String> expectedSkillNames)
static void assertSkillCallsIgnoringOrder(AgentResultDto result, List<String> expectedSkillNames)
```

---

### 2.7 Пакет `util`

#### `WutPreparer`
**Назначение:** Подготовка «Workspace Under Test» — изолированной копии шаблона для одного запуска агента. Структура: `<buildDir>/<wutTargetPath>/<wutSourceName>/<UUID>/sorce` + `../logs`. Каждый вызов `prepare()` создаёт свежую папку с новым UUID.  
**Builder API:**
```java
static Builder builder()
Builder.wutSourceName(String)       // обязательный
Builder.wutSourcePath(Path)         // default: "wut-source"
Builder.buildDirectory(Path)        // default: autodetect (target / build)
Builder.wutTargetPath(Path)         // default: "wut-target"
WutPreparer build()
```
**Публичный API:**
```java
Path prepare() throws IOException   // возвращает путь к sorce
```

#### `AgentSkillCallExtractorUtils`
**Назначение:** Извлекает из событий агента список вызовов инструмента `skill`. Поддерживает как плоский формат событий Qwen CLI (сам event — это `tool_use`), так и вложенный (через `event.message.content[]`).  
**Публичный API:**
```java
static List<String> extractSkillCalls(AgentResultDto result)
```

#### `SkillsFileUtils`
**Назначение:** Файловые утилиты: определение `basedir` и `buildDir` модуля (поднимается вверх, ищет `pom.xml`/`build.gradle`), очистка и копирование директорий.  
**Публичный API:**
```java
static ModuleLayoutDto resolveModuleLayout()
static Path resolveClassesLocation()
static void cleanDirectory(Path dir)
static void copyDirectory(Path source, Path target)
```

#### `util/qwen/QwenSettingsUpdater`
**Назначение:** Обновляет `model.name` в файле `.qwen/settings.json` и восстанавливает предыдущее значение. Опционально создаёт файл если отсутствует (`createSettingsIfMissing`).  
**Builder API:**
```java
static Builder builder()
Builder.agentRunContext(AgentRunContext)         // обязательный
Builder.createSettingsIfMissing(boolean)
```
**Публичный API:**
```java
void updateModelNameAndSave(String newModelName) throws Exception
void restoreOriginalModelName() throws Exception
String getCurrentModelName() throws Exception
String getPreviousModelName()
```

---

### 2.8 Пакеты DTO

| Класс | Поля |
|---|---|
| `dto/agent/AgentLogDto` | `List<JsonNode> events`, `String eventsJson`, `String finalResult` |
| `dto/agent/AgentResultDto` | `stdout`, `stderr`, `exitCode`, `timedOut`, `events`, `eventsJson`, `finalResult` |
| `dto/command/CommandRequestDto` | `List<String> command`, `Path workingDirectory`, `Duration timeout` |
| `dto/command/CommandResultDto` | `stdout`, `stderr`, `exitCode`, `timedOut` |
| `dto/evaluate/EvaluateDto` | `String query`, `String agentTrace` |
| `dto/evaluate/EvaluateResultDto` | `double score`, `String problemMessage` — @JsonCreator |
| `dto/log/AgentRunLogDto` | `runId`, `agentSet`, `startedAt`, `finishedAt`, `skillName`, `finalResult`, `List<JsonNode> events` — @Builder |
| `dto/module/ModuleLayoutDto` | `Path basedir`, `String buildDir` |

---

### 2.9 Пакет `exeptions`

| Исключение | Иерархия | Назначение |
|---|---|---|
| `AgentRunnerConfigurationException` | `RuntimeException` | Базовый класс конфигурационных ошибок |
| `CommandNotFoundException` | `AgentRunnerConfigurationException` | Не найден исполняемый файл CLI |
| `MissingAgentCliException` | `AgentRunnerConfigurationException` | Не задано свойство `agent.cli` |
| `UnsupportedAgentCliException` | `AgentRunnerConfigurationException` | Неизвестное значение `agent.cli` |
| `MissingCommandPartsException` | `AgentRunnerConfigurationException` | Передана пустая команда в `CommandExecutor` |
| `EvaluatorResponseParseException` | `RuntimeException` | Не удалось распарсить JSON-ответ судьи |
| `InvalidSkillNameException` | `RuntimeException` | Имя скилла содержит `/`, `\` или `..` |
| `NotFoundSaveModelNameException` | `RuntimeException` | Попытка `restoreOriginalModelName` без предшествующего `update` |
| `QwenCommandFactoryException` | `RuntimeException` | Ошибка при создании команды (задел под будущее) |
| `WutPreparerException` | `RuntimeException` | Ошибки подготовки WUT |

---

## 3. Что должно войти в standalone SDK `agentic-cli-runner`

На основе кода и документации `docs/other/sdk.md` рекомендуется следующая структура модулей:

### `sdk-core` — Публичные интерфейсы и контракты

Берётся напрямую из текущего кода без изменений:

- `AgentRunner` (интерфейс)
- `JudgeRunner` (интерфейс)
- `CommandFactory` (интерфейс)
- `CommandResolver` (интерфейс)
- Все классы из `dto/` (кроме Qwen-специфичных `AgentLogDto` с `JsonNode`)
- `AgentRunnerConfigurationException` и прямые наследники
- `AgentMatcher` (утилиты для тестирования)

**Что требует доработки для SDK:**  
`AgentResultDto` сейчас содержит `List<JsonNode>` — Jackson-специфичный тип. В SDK лучше заменить на `List<Map<String, Object>>` или параметризованный тип.

### `sdk-common` — OS-aware инфраструктура

Переносятся без изменений:

- `CommandExecutor` (Apache Commons Exec — единственная внешняя зависимость на уровне исполнения)
- `OsType`
- `OsAwareCommandResolver`
- `AgentRunnerProperties` (с небольшим рефакторингом: убрать Qwen-специфичные константы в `sdk-qwen`)
- `AgentRunContext`
- `RunnerLogWriter`
- `WutPreparer`
- `SkillsFileUtils`

### `sdk-qwen` — Реализация для Qwen CLI

Переносятся без изменений:

- `QwenCommandFactoryImpl`
- `QwenAgentRunner`
- `QwenJudgeRunner`
- `AgentStreamJsonParser`
- `QwenSettingsUpdater`
- `AgentSkillCallExtractorUtils`
- `AgentRunnerFactory` (или переименовать в `QwenAgentRunnerFactory`)
- `AgentCli` enum (или обобщить в `sdk-core` как расширяемый интерфейс)

### `sdk-test` — Утилиты для тестирования

- `WutPreparer` (дублируется из `sdk-common`, или ссылается)
- `AgentMatcher`
- `AgentSkillCallExtractorUtils`

### Что нужно добавить для полноценного SDK

1. **`AgentRunnerBuilder`** — fluent API для программного конфигурирования без файла `agent-runner.properties`:
   ```java
   AgentRunnerBuilder.forCli(AgentCli.QWEN)
       .executable("/usr/local/bin/qwen")
       .args("--output-format", "stream-json")
       .workspace(workspacePath)
       .timeout(Duration.ofMinutes(5))
       .build();
   ```
2. **SPI через `ServiceLoader`** — для подключения реализаций `CommandFactory`/`CommandResolver` сторонними библиотеками.
3. **Поддержка Claude Code и Codex** — достаточно создать `ClaudeCommandFactoryImpl` и `CodexCommandFactoryImpl`, реализующие `CommandFactory`; остальная инфраструктура переиспользуется без изменений.

---

## 4. Паттерн Strategy для построения команд — детальный разбор

Да, в новом коде **реализован паттерн Strategy**. Он разделён на два уровня:

### Уровень 1: `CommandResolver` — Strategy поиска бинарника

```
Context (QwenCommandFactoryImpl)
    strategy: CommandResolver
        └── OsAwareCommandResolver   — конкретная стратегия
```

**Как работает:**  
`QwenCommandFactoryImpl` вызывает `resolver.resolveExecutable("qwen")` и не знает, где именно на файловой системе лежит бинарник. Стратегия инкапсулирует трёхшаговый алгоритм поиска: env-var → PATH → fallback.

**Замена стратегии:**  
В тестах `CommandResolver` подменяется лямбдой:
```java
CommandResolver mockResolver = name -> "/usr/local/bin/qwen";
QwenCommandFactoryImpl factory = new QwenCommandFactoryImpl(mockResolver, args, prefix);
```

### Уровень 2: `CommandFactory` — Strategy сборки команды

```
Context (QwenAgentRunner / QwenJudgeRunner)
    strategy: CommandFactory
        └── QwenCommandFactoryImpl   — конкретная стратегия для Qwen
        // (будущие: ClaudeCommandFactoryImpl, CodexCommandFactoryImpl)
```

**Как работает:**  
Оба раннера `QwenAgentRunner` и `QwenJudgeRunner` принимают `CommandFactory` в конструкторе и вызывают:
```java
List<String> command = commandFactory.buildCommand(prompt, agentRunContext.getRunDir());
```
Раннеры полностью изолированы от деталей: пути к бинарнику, аргументы CLI, Windows-специфичный префикс — всё это в стратегии.

**Контекст выбора стратегии:**  
`AgentRunnerFactory.create(Properties)` — это фактически `StrategyContext` (или фабрика контекстов). `switch (cli)` выбирает нужную стратегию:

```java
CommandFactory commandFactory = switch (cli) {
    case QWEN -> new QwenCommandFactoryImpl(resolver, baseArgs, prefix);
    // case CLAUDE -> new ClaudeCommandFactoryImpl(resolver, baseArgs);
    // case CODEX  -> new CodexCommandFactoryImpl(resolver, args);
};
```

### Полная цепочка создания для Qwen

```
agent-runner.properties
        │
        ▼
AgentRunnerProperties.loadDefault()
        │
        ▼
AgentRunnerFactory.createCommandFactory(AgentCli.QWEN, props)
        │
        ├─► AgentRunnerProperties.getFallbackPaths(props, os) ──► Map<OsType, List<Path>>
        │                                                              │
        │                                    OsAwareCommandResolver ◄─┘
        │                                           (Strategy L1)
        │
        ├─► AgentRunnerProperties.getBaseArgs(props)
        ├─► AgentRunnerProperties.getPrefix(props, OsType.detect())
        │
        ▼
QwenCommandFactoryImpl(resolver, baseArgs, prefix)
        (Strategy L2)
        │
        ▼
QwenAgentRunner(executor, parser, logWriter, workspace, timeout, commandFactory)
        │
        ▼
commandFactory.buildCommand(prompt, logDir)
  └── resolver.resolveExecutable("qwen")
        └── env QWEN_PATH → PATH → fallback-paths[currentOS]
```

### Почему это важно для `agentic-cli-runner`

Паттерн уже реализован правильно и готов к расширению. Для добавления поддержки **Claude Code**:
1. Создать `ClaudeCommandFactoryImpl implements CommandFactory` — строит `claude --prompt "..." --output-format stream-json`.
2. Добавить `CLAUDE` в enum `AgentCli`.
3. Добавить ключ `case CLAUDE ->` в switch в `AgentRunnerFactory`.
4. Добавить ключи конфигурации `agent.cli.claude.fallback.*` в properties.

**Для Codex (OpenAI)** — аналогично. Инфраструктура `OsAwareCommandResolver`, `AgentRunContext`, `RunnerLogWriter` переиспользуется без изменений.

---

## Итоговая сводка

| Аспект | До PR #29 | После PR #29 |
|---|---|---|
| Сборка CLI-команды | Зашита в раннер | Изолирована в `CommandFactory` (Strategy) |
| Поиск бинарника | Строка из properties | `OsAwareCommandResolver` — 3 шага, поддержка Windows |
| Конфигурация путей | Одна строка | Per-OS fallback-пути с подстановкой переменных |
| `spec/` пакет | 6 классов генерации SKILL.md | Удалён (мёртвый код) |
| Тестовое покрытие | Минимальное | 11 unit-классов + 3 integration + 3 external |
| Готовность к multi-CLI | Нет | Да — добавить `case CLAUDE/CODEX` в switch |
