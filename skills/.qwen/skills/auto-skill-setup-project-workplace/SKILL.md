---
name: setup-project-workplace
description: Настраивает или адаптирует .qwen/workplace/ для нового модуля: PROJECT_RULES.md, TASK_INDEX.md, task_template.md, settings.json, QWEN.md
source: auto-skill
extracted_at: '2026-07-07T07:38:58.264Z'
---

# Настройка рабочего пространства модуля (.qwen/workplace/)

Используйте этот навык, когда нужно инициализировать или адаптировать правила работы проекта (`PROJECT_RULES.md`, `TASK_INDEX.md`, `task_template.md`, `QWEN.md`, `settings.json`) для нового или существующего Maven-модуля в multi-module проекте.

## Предварительные требования

Убедитесь, что в корне модуля уже есть структура:
```
.qwen/
├── settings.json
└── workplace/
    ├── PROJECT_RULES.md      # правила проекта (Extreme Programming + TDD)
    ├── TASK_INDEX.md         # индекс задач
    ├── task_template.md      # шаблон задачи
    ├── to_work/              # активные задачи
    └── archive/              # выполненные задачи
```

Если директории или файлов нет — создать по образцу из соседнего настроенного модуля (например, `mcp-server-jar-unpacker/.qwen/workplace/`).

## Процесс

### 1. Прочитать текущее состояние
- Загрузить `.qwen/workplace/PROJECT_RULES.md` — таблицу модулей, коды, примеры
- Загрузить `.qwen/workplace/TASK_INDEX.md` — формат именования, таблицу задач
- Загрузить `.qwen/workplace/task_template.md` — список допустимых модулей
- Загрузить `.qwen/settings.json` — текущие настройки
- Прочитать QWEN.md из соседнего настроенного модуля (например, `../mcp-server-jar-unpacker/QWEN.md`) для понимания формата

### 2. Обновить PROJECT_RULES.md
Добавить новый код модуля в таблицу:

```markdown
| `<код>` | `<название-модуля>`           | `<путь-к-модулю/>`             |
```

Добавить пример использования:
```markdown
- `TASK-<ID>_<код>_...` — задача по модулю <название-модуля>
```

### 3. Обновить TASK_INDEX.md
- Очистить устаревшие счётчики: `**Всего задач:** N` -> `**Всего задач:** 0 (0 выполнено, 0 в работе)`
- Проверить и поправить формат имени файла — добавить `<модуль>`:
  ```
  TASK-<ID>_<модуль>_<краткое_описание>.md
  ```
- Добавить новый код модуля в описание

### 4. Обновить task_template.md
Добавить код модуля в перечень в секции "Информация о задаче":
```markdown
| **Модуль:** | `... / ... / <код>` |
```

Добавить строку с кодом в список "Коды модулей":
```markdown
- `<код>` — <название-модуля>
```

### 5. Обновить settings.json
Добавить секцию `projectRulesFile` и `workplace`:

```json
{
  "projectRulesFile": ".qwen/workplace/PROJECT_RULES.md",
  "permissions": {
    "allow": [
      "Read(//<корень-проекта>/**)"
    ]
  },
  "workplace": {
    "taskIndexFile": ".qwen/workplace/TASK_INDEX.md",
    "taskTemplateFile": ".qwen/workplace/task_template.md",
    "taskDir": ".qwen/workplace/to_work",
    "archiveDir": ".qwen/workplace/archive",
    "activeDir": ".qwen/workplace/to_work"
  },
  "$version": 4
}
```

### 6. Создать QWEN.md
Создать в корне модуля файл `QWEN.md` по образцу соседнего модуля со следующими секциями:
- Project Overview — назначение модуля, основные возможности, технологии
- Architecture — пакетная структура, ключевые компоненты
- Building and Running — сборка, тестирование, требования к окружению
- Development Conventions — TDD (Red-Green-Refactor), AAA Pattern, именование, язык
- Qwen Added Memories — фраза-напоминание:
  ```
  - В проекте <модуль> автоматически применяю правила из .qwen/workplace/PROJECT_RULES.md: TDD (Red-Green-Refactor), именование тестов given-when-then CamelCase, AAA Pattern, практики Extreme Programming
  - Код модуля <модуль> в таблице задач — <код>
  ```

### 7. Сохранить reference-память
Создать файл `.qwen/memories/reference/project_rules_location.md`:

```markdown
---
name: Project rules location
description: Правила проекта (TDD, XP) хранятся в .qwen/workplace/PROJECT_RULES.md; QWEN.md даёт полный обзор модуля
type: reference
---

Правила работы над проектом (Extreme Programming + TDD) находятся в `.qwen/workplace/PROJECT_RULES.md`.  
Файл настроек `.qwen/settings.json` содержит `projectRulesFile: ".qwen/workplace/PROJECT_RULES.md"`.

Полная документация модуля — `QWEN.md` в корне <модуль>/.

Код модуля в таблице задач: `<код>`.
```

Добавить ссылку в `.qwen/memories/MEMORY.md`:
```
- [Project rules location](reference/project_rules_location.md) — правила проекта в .qwen/workplace/PROJECT_RULES.md
```
