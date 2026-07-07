# Skills Test Framework

## Project Overview

**Skills Test Framework** — библиотека для автоматизированного тестирования AI-агентов (Qwen Code CLI) и их скиллов. Позволяет запускать агентов, подготавливать рабочие области, переключать модели, проверять вызовы скиллов и оценивать ответы.

### Основное назначение

- 🧪 **Запуск агента** — выполнение пользовательских промптов или прямых вызовов скиллов через CLI
- 📁 **Подготовка рабочей области (WUT)** — копирование шаблонов проектов с предустановленными скиллами
- 🔄 **Переключение моделей** — динамическая смена модели (Ollama) для параметризованных тестов
- ✅ **Встроенные проверки** — валидация успешности выполнения, вызовов скиллов, состояния файловой системы
- 📊 **Оценка ответов** — использование судьи (Judge) для выставления score и описания проблем
- 📝 **Логирование** — сохранение каждого запуска в отдельную директорию с событиями в формате JSON

### Технологии

- **Язык:** Java 17
- **Сборка:** Maven
- **Тестирование:** JUnit 5 (junit-jupiter)
- **CLI:** Qwen Code CLI
- **Парсинг:** Jackson (jackson-databind)
- **Выполнение команд:** Apache Commons Exec
- **Ломбок:** Lombok (optional)

### Архитектура

```
src/main/java/org/mirent/skills/
├── runner/                    – интерфейсы и реализации раннеров
│   ├── AgentRunner.java       # Интерфейс для запуска агентов
│   ├── AgentRunContext.java   # Контекст выполнения (рабочая директория, runId)
│   ├── JudgeRunner.java       # Интерфейс для судьи (оценка ответов)
│   ├── RunnerLogWriter.java   # Запись логов выполнения
│   └── qwen/                  # Реализация для Qwen CLI
│       ├── QwenAgentRunner.java
│       └── QwenJudgeRunner.java
├── service/                   – фабрики и сервисы
│   ├── AgentCli.java          # Enum поддерживаемых CLI
│   ├── AgentRunnerFactory.java # Фабрика раннеров
│   ├── AgentRunnerProperties.java # Загрузка agent-runner.properties
│   ├── AgentRunnerService.java # Упрощённый сервис для тестов
│   └── AgentEvaluatorService.java # Сервис оценки ответов судьёй
├── util/                      – утилиты
│   ├── WutPreparer.java       # Подготовка рабочей области (WUT)
│   ├── SkillsFileUtils.java   # Работа с файлами скиллов
│   ├── AgentSkillCallExtractorUtils.java # Извлечение вызовов скиллов из логов
│   └── qwen/
│       ├── QwenCommandFactory.java   # Сборка командной строки qwen
│       └── QwenSettingsUpdater.java  # Обновление settings.json
├── parser/                    – парсер stream-json логов
│   └── AgentStreamJsonParser.java
├── matcher/                   – статические методы проверок
│   └── AgentMatcher.java
├── spec/                      – модель и рендерер скиллов
│   ├── SkillSpec.java
│   └── QwenSkillRenderer.java
├── dto/                       – DTO
│   ├── agent/                 # AgentResultDto, AgentLogDto
│   ├── command/               # CommandRequestDto, CommandResultDto
│   ├── evaluate/              # EvaluateDto, EvaluateResultDto
│   ├── log/                   # AgentRunLogDto
│   └── module/                # ModuleLayoutDto
├── exeptions/                 – пользовательские исключения
└── CommandExecutor.java       – выполнение внешних команд
```

### Ключевые компоненты

**AgentRunner (QwenAgentRunner):**
- `executeUserPrompt(prompt)` — выполнение пользовательского запроса
- `executeSkillPrompt(skillName, prompt)` — прямой вызов скилла
- Автоматическое логирование и парсинг stream-json событий

**AgentRunnerFactory:**
- `defaultFactory(workspace)` — создание фабрики с настройками по умолчанию
- `create(properties)` — создание раннера согласно `agent.cli` из properties

**WutPreparer:**
- Копирование шаблона проекта (WUT) во временную директорию
- Поддержка перезаписи/пропуска существующих директорий

**AgentEvaluatorService:**
- Оценка ответа агента через независимого судью (QwenJudgeRunner)
- Выставление score (0.0–1.0) и описание проблем

### Структура тестов

```
src/test/java/org/mirent/skills/tests/
├── inner/                     # Внутренние тесты (без внешних зависимостей)
│   ├── unit/                  # Модульные тесты
│   │   ├── AgentCliTest.java
│   │   ├── AgentEvaluatorServiceTest.java
│   │   ├── QwenAvailabilityTest.java
│   │   └── WutPreparerTest.java
│   └── integration/           # Интеграционные тесты
│       ├── AgentRunnerTest.java
│       ├── AgentRunContextTest.java
│       └── WorkspacePreparerTest.java
└── external/                  # Внешние тесты (требуют реального CLI/модели)
    ├── MultipleModelsQwenTest.java
    ├── MultipleModelsSkillTest.java
    └── TextToJavaUiTest.java
```

## Building and Running

### Сборка проекта

```bash
# Из корня модуля skills
mvn clean package

# Из корня родительского проекта
mvn clean package -pl skills
```

### Запуск тестов

```bash
# Все тесты
mvn test

# Только внутренние тесты (без внешних зависимостей)
mvn test -Dgroups=inner

# Только модульные тесты
mvn test -Dgroups=unit

# Только интеграционные тесты
mvn test -Dgroups=integration

# Только внешние тесты
mvn test -Dgroups=external
```

### Требования

- **Java 17+**
- **Maven**
- **Qwen Code CLI** — установлен глобально (`npm install -g @qwen-code/qwen-code`)
- **Модель** — доступная через Ollama (или другой провайдер)

### Конфигурация

Файл `agent-runner.properties` в `src/test/resources/`:

```properties
agent.cli=QWEN
```

Шаблоны WUT в `src/test/resources/wut-templates/`:
- `default/` — базовый шаблон со скиллом arithmetic
- `text-utils/` — шаблон со скиллами word-count, text-utils
- `case-1/`, `case-2/`, `case-3/` — кастомные шаблоны для параметризованных тестов
- `skill-test-checkstyle/` — шаблон для проверки скилла maven-checkstyle-setup

## Файловая структура

```
skills/
├── pom.xml                        # Maven конфигурация
├── QWEN.md                        # Полная документация проекта
├── README.md                      # Краткая документация (для GitHub)
├── plan.md                        # План рефакторинга CLI-архитектуры
├── .qwen/                         # Директория настроек и задач
│   ├── settings.json
│   ├── pending-skills/            # Ожидающие скиллы (для тестов)
│   └── workplace/                 # Рабочее пространство задач
│       ├── TASK_INDEX.md          # Индекс всех задач
│       ├── PROJECT_RULES.md       # Правила проекта (XP, TDD)
│       ├── task_template.md       # Шаблон для новых задач
│       ├── to_work/               # Активные задачи
│       └── archive/               # Выполненные задачи
├── docs/                          # Документация
│   ├── README.md
│   ├── frap-mcp/                  # Документация FRAP MCP
│   ├── skills/                    # Документация по скиллам
│   └── vesion&helps/              # Версии Qwen CLI
├── src/
│   ├── main/java/org/mirent/skills/   # Исходный код
│   ├── main/resources/                # Ресурсы
│   └── test/java/org/mirent/skills/   # Тесты
└── target/                            # Выходная директория сборки
```

## Development Conventions

- **TDD (Red-Green-Refactor):** тесты пишутся до кода
- **AAA Pattern:** Arrange → Act → Assert
- **Именование тестов:** `given<Условие>When<Действие>Then<Результат>`
- **Имена классов/методов:** английский
- **Документация и сообщения:** русский
- **Ломбок:** использовать `@Slf4j` для логирования, `@Getter` при необходимости

## Qwen Added Memories
- В проекте skills автоматически применяю правила из .qwen/workplace/PROJECT_RULES.md: TDD (Red-Green-Refactor), именование тестов given-when-then CamelCase, AAA Pattern, практики Extreme Programming
- Код модуля skills в таблице задач — `skills`
