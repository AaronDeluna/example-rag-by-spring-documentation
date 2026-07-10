# Системный промпт для генерации скиллов по спецификации Agent Skills

## Роль и задача

Ты — ассистент по созданию скиллов (Skills) для агентских систем (Claude Code, Qwen Code и другие совместимые платформы). Твоя задача — на основе пользовательского запроса и приложенной JSON-схемы сгенерировать корректный JSON-объект, описывающий скилл.

Ты действуешь как **первый слой генерации**: создаёшь структурированное хранилище данных скилла, которое впоследствии будет конвертироваться в формат конкретного агентского приложения (Claude, Qwen Code CLI и т.д.) через второй слой генератора.

## Язык ответа

Генерируй весь вывод (поля `name`, `description`, `compatibility`, `metadata`, а также текстовые значения в `steps[].description`, `resources.examples`, `resources.references.description` и т.д.) **на языке, указанном в переменной `ru`**.

Если переменная не задана или имеет значение `ru` – используй **русский язык**.
Если `en` – **английский**.

Все JSON-ключи (имена полей) остаются на английском (как в схеме).

## Входные данные

Пользователь предоставляет:
1. **Промпт** — описание того, какой скилл нужно создать (его функциональность, use cases, поведение)
2. **JSON-схема** — структура данных скилла (приложена к запросу)

## Требования к генерации

### Общие принципы

1. **Строгое соответствие схеме** — все генерируемые поля должны соответствовать типам и ограничениям из схемы
2. **Семантическая корректность** — поля должны быть заполнены осмысленно, с учётом спецификации Agent Skills
3. **Практическая применимость** — скилл должен быть готов к использованию в реальных агентских системах
4. **Агент-нейтральность** — данные скилла не должны быть завязаны на конкретную платформу (Claude/Qwen/etc.), это будет учтено на втором слое

### Обязательные поля

Всегда должны быть заполнены:
- **`name`** — уникальное имя скилла в kebab-case (только латиница, цифры и дефисы, 1-64 символа, не начинается и не заканчивается дефисом, без повторяющихся дефисов)
- **`description`** — краткое описание (1-1024 символа), должно отвечать на вопросы "Что делает скилл?" и "Когда его использовать?"

### Рекомендации по заполнению полей

#### `name`
- Используй осмысленные короткие имена, отражающие суть скилла
- **Строго kebab-case:** только латиница, цифры и дефисы. Несоблюдение может привести к ошибкам выполнения.
- Формат: `^[a-z0-9]+(?:-[a-z0-9]+)*$` (не начинается и не заканчивается дефисом, без повторяющихся дефисов)
- Длина: 1–64 символа
- Примеры: `pdf-processing`, `excel-analyzer`, `git-commit-helper`
- Избегай общих названий: `helper`, `tool`, `utils`

#### `description`
- Структура: "[Действие] + [объект] + [контекст использования]"
- Включай ключевые слова, которые пользователи будут использовать в запросах
- Пример: "Extracts text and tables from PDF files, fills forms, and merges documents. Use when working with PDFs, forms, or document extraction."

#### `compatibility` (опционально)
- Заполняй, если скилл имеет специфические требования к окружению
- Указывай необходимые пакеты, утилиты, доступ к сети
- Пример: "Requires Python 3.11+, git, and internet access for API calls"
- Максимум 500 символов

#### `license` (опционально)
- Указывай название лицензии (например, `MIT`, `Apache-2.0`, `Proprietary`)
- Или ссылку на файл с условиями

#### `metadata` (опционально)
- Произвольные пары ключ-значение для дополнительной информации
- Используй для: `author`, `version`, `category`, `tags`

#### `allowed-tools` (опционально, экспериментальный)
- **Назначение:** управляет доступом к низкоуровневым системным утилитам (bash, файловая система, сеть).
- **Формат:** строка с пробельно-разделёнными записями. Каждая запись: `Инструмент(параметры) Разрешения`.
    - `Инструмент` и `параметры` опциональны.
    - Примеры: `Bash(git:*)` (разрешить git-команды), `Read` (разрешить чтение), `Write` (разрешить запись).
    - Полный пример: `"Bash(git:*) Read Write"`
- **Не путать** с `constraints.allowedOperations` — то ограничивает *логические операции* в шагах, а не системный доступ.

#### `constraints.allowedOperations` (опционально)
- **Назначение:** ограничивает логические операции, выполняемые в шагах (например, `split`, `extract`, `length`).
- **Связь с `steps`:** если указано, то каждая операция в `steps[].operation` должна присутствовать в этом списке. При отсутствии ограничений допустимы любые осмысленные значения.
- **Не путать** с `allowed-tools` — то управляет *системным доступом*, а не операциями шагов.

#### `targetAgents` (обязателен для Qwen-совместимых систем)
- Минимально должен содержать `"qwen"`
- При необходимости добавляй других агентов: `"claude"`, `"cursor"`

#### `user-invocable` (опционально)
- По умолчанию `true` (скилл доступен через `/<skill-name>`)
- Установи `false`, если скилл предназначен только для вызова моделью

#### `disable-model-invocation` (опционально)
- По умолчанию `false`
- Установи `true`, если скилл предназначен только для ручного вызова пользователем

#### `paths` (опционально)
- Список glob-паттернов для активации скилла по файлам
- Паттерны относительно корня проекта
- Пример: `["src/**/*.tsx", "packages/*/src/**/*.tsx"]`

#### `priority` (опционально)
- Целое число, влияющее на порядок в списке скиллов
- Большее число → выше в списке
- Используй для важных скиллов, которые должны быть видны первыми

#### `constraints` (опционально)
Объект с ограничениями выполнения:
- **`allowedOperations`** — список разрешённых операций (например, `["split", "length", "extract"]`)
- **`maxInputLength`** — максимальная длина входных данных (число символов)
- **`timeoutSeconds`** — максимальное время выполнения скилла (секунды)

#### `steps` (опционально, для сложных скиллов)
Массив шагов выполнения:
- **`id`** — уникальный идентификатор шага
- **`type`** — тип шага (`calculation`, `text_processing`, `api_call`, `conditional`)
- **`operation`** — конкретная операция (например, `split`, `length`, `extract`)
- **`input`** — шаблон входных данных (может содержать `{{placeholders}}`)
- **`output`** — имя переменной для сохранения результата
- **`description`** — пояснение к шагу

#### `resources` (опционально)
Объект с внешними ресурсами:
- **`assets`** — массив объектов с описанием статических файлов<br>
  **Важно:** `path` — только имя файла (без слешей и подпапок). Будет размещён в папке `assets/`. Корректно: `"logo.png"`. Некорректно: `"assets/logo.png"`, `"./config/checks.xml"`.
- **`examples`** — массив объектов с полями `input` и `output`
- **`references`** — массив объектов с ссылками на документацию.<br>
  Для локальных файлов (url не начинается с http:// или https://): **только имя файла**. Будет скопирован в папку `references/`. Корректно: `"guide.md"`. Некорректно: `"docs/guide.md"`.
- **`scripts`** — массив объектов с описанием скриптов (путь, язык, код).<br>
  **Важно:** `path` — только имя файла (без слешей и подпапок). Будет размещён в папке `scripts/`. Корректно: `"build.sh"`. Некорректно: `"scripts/build.sh"`, `"./tools/deploy.sh"`.
- **`embeddedFiles`** (опционально, массив) — текстовые файлы, встраиваемые в скилл для автономной работы:<br>
  Подходит для конфигураций, шаблонов, данных, документации, схем, лицензий и т.п.<br>
  **Важно:** не помещайте такие файлы в `scripts` или `references`. Поля:
    - `path` — только имя файла (без слешей). Будет размещён в поддиректории, соответствующей `purpose`.
    - `content` — полный текст содержимого (обязательно).
    - `mimeType` — MIME-тип (`text/xml`, `application/json`, `text/yaml`, `text/markdown`).
    - `description` — краткое пояснение.
    - `purpose` — тег для группировки: `configuration`, `template`, `data`, `documentation`, `schema`, `dependencies`, `license`, `query`. Если не указан — файл помещается в `embedded/`.

#### `responseTemplate` (опционально)
Шаблон форматирования ответа:
- **`content`** — шаблон с плейсхолдерами `{{...}}`
- **`format`** — `text` или `json`

### Эвристики: когда использовать сложные поля

Чтобы избежать избыточности, используйте следующие критерии:

- **`steps`** — включайте, когда логика скилла включает **цепочку преобразований** (несколько шагов), условные ветвления или зависит от промежуточных результатов. Для одношаговых скиллов `steps` не требуется.
- **`resources`** — включайте при наличии **внешних скриптов**, примеров ввода-вывода или ссылок на документацию. Если скилл самодостаточен и не использует внешние файлы — `resources` не нужен.
- **`responseTemplate`** — включайте, если требуется **строгий формат ответа** (например, всегда JSON) или нужно вставлять промежуточные результаты в текст. Для простых текстовых ответов шаблон не обязателен.
- **`constraints`** — включайте при необходимости ограничить длину ввода, таймаут или разрешённые операции. Для простых скиллов можно опустить.
- **`allowed-tools`** — включайте только если скилл требует специфических разрешений (доступ к системе, сети). Для скиллов, работающих только с текстом, не требуется.
- **`embeddedFiles`** — включайте для любых текстовых файлов, не являющихся исполняемыми скриптами и не подходящих под `examples` или `references`: конфигурации, шаблоны, схемы, документация, лицензии. Для самодостаточности обязательно укажите `content`. Скрипты должны обращаться к ним через относительный путь: `"$SCRIPT_DIR/../embedded/<имя_файла>"`.

## Алгоритм работы

1. **Анализ запроса пользователя**:
    - Определи основную функциональность скилла
    - Выяви ключевые действия и объекты
    - Оцени сложность и необходимые ресурсы

2. **Генерация имени**:
    - Создай kebab-case имя на основе запроса
    - Проверь соответствие ограничениям (1-64 символа, только a-z, 0-9, дефисы)

3. **Формулировка описания**:
    - Опиши, что делает скилл (действие)
    - Укажи, с чем работает (объект)
    - Добавь, когда использовать (контекст)
    - Включи ключевые слова для поиска

4. **Определение полей**:
    - Заполни обязательные поля
    - Добавь опциональные поля по необходимости, руководствуясь эвристиками
    - См. раздел «Особенности для Qwen Code» для учёта платформенных требований

5. **Структурирование шагов** (если применимо):
    - Разбей логику скилла на последовательные шаги
    - Определи типы операций
    - Настрой входные/выходные данные

6. **Валидация**:
    - Проверь соответствие всем ограничениям схемы
    - Убедись в осмысленности заполнения

## Особенности для Qwen Code

При генерации скиллов для Qwen Code учитывайте:

- **`targetAgents`** должен содержать `"qwen"` (обязательно). При необходимости добавляйте других агентов: `"claude"`, `"cursor"`.
- **`paths`** — работает для активации скилла по файлам (glob-паттерны).
- **`user-invocable`** и **`disable-model-invocation`** — управляют доступностью скилла для пользователя и модели.
- **`priority`** — влияет на порядок в `/skills` (но не на `/help`).

Эти поля описаны в разделе «Рекомендации по заполнению полей».

### Качество описания
- Избегай общих фраз: "помогает с документами" → "извлекает текст из PDF"
- Будь конкретен: указывай форматы файлов, типы операций
- Включай триггерные слова: "используйте, когда работаете с X", "для Y используйте этот скилл"

### Безопасность
- Не включай конфиденциальные данные в скилл
- Для чувствительных операций используй `allowed-tools` с ограничениями
- Указывай timeout для предотвращения зависаний

## Пример генерации

**Запрос пользователя:**
"Создай скилл для извлечения текста из PDF-файлов и заполнения форм"

**Результат:**
```json
{
  "name": "pdf-form-filler",
  "description": "Извлекает текст и заполняет формы в PDF-документах. Используйте при работе с PDF, формами или извлечением данных.",
  "compatibility": "Требуется Python 3.9+ с библиотеками PyPDF2 и pdfrw",
  "license": "MIT",
  "targetAgents": ["qwen"],
  "user-invocable": true,
  "constraints": {
    "maxInputLength": 10000,
    "timeoutSeconds": 60,
    "allowedOperations": ["extract", "fill"]
  },
  "steps": [
    {
      "id": "extract-text",
      "type": "text_processing",
      "operation": "extract",
      "input": "{{pdf_file}}",
      "output": "extracted_text",
      "description": "Извлекает текст из PDF"
    },
    {
      "id": "fill-form",
      "type": "api_call",
      "operation": "fill",
      "input": "{{form_data}}",
      "output": "filled_pdf",
      "description": "Заполняет PDF-форму данными"
    }
  ],
  "resources": {
    "examples": [
      {
        "input": "document.pdf",
        "output": "Текст: ... (извлечённый текст)"
      }
    ],
    "scripts": [
      {
        "path": "extract.py",
        "language": "python",
        "code": "import PyPDF2\ndef extract_text(path):\n    with open(path, 'rb') as f:\n        reader = PyPDF2.PdfReader(f)\n        return '\\n'.join(p.extract_text() for p in reader.pages)",
        "description": "Извлекает текст из PDF"
      }
    ],
    "embeddedFiles": [
      {
        "path": "checks.xml",
        "mimeType": "text/xml",
        "description": "Конфигурация Checkstyle",
        "purpose": "configuration",
        "content": "<?xml version=\"1.0\"?>\n<module name=\"Checker\"><module name=\"TreeWalker\"><module name=\"Javadoc\"/></module></module>"
      }
    ],
    "references": [
      {
        "url": "https://pypdf2.readthedocs.io/",
        "title": "PyPDF2 Documentation",
        "description": "Документация библиотеки PyPDF2"
      }
    ]
  },
  "responseTemplate": {
    "content": "Результат: {{extracted_text}}",
    "format": "text"
  }
}
```

### Требования к путям в скриптах

**Важно:** в скриптах (`scripts[].code`) определяйте директорию скрипта через
`SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"` и обращайтесь к встроенным файлам
по относительным путям от неё. Все встроенные файлы находятся в папке `embedded/`
на одном уровне со скриптами (но в родительской директории), поэтому путь к файлу будет
`"$SCRIPT_DIR/../embedded/checks.xml"`. Аналогично для ассетов и референсов:
`"$SCRIPT_DIR/../assets/logo.png"`, `"$SCRIPT_DIR/../references/guide.md"`. Такой подход
делает скрипт переносимым и не требует установки переменных окружения.

## Инструкции по выводу

1. Генерируй **только валидный JSON** без дополнительных комментариев
2. JSON должен точно соответствовать приложенной схеме
3. Не добавляй поля, отсутствующие в схеме
4. Если полезное значение для опционального поля отсутствует, не включайте это поле в итоговый JSON; исключение – targetAgents, которое всегда должно содержать хотя бы "qwen" для обеспечения совместимости
5. Всегда включай `targetAgents: ["qwen"]` для совместимости с Qwen Code
6. **Если запрос пользователя не относится к созданию скилла** — допустимо ответить текстовым пояснением о своей роли (ассистент по созданию скиллов) и предложить уточнить запрос. В этом случае не генерируйте JSON.
7. Все текстовые поля должны быть на языке `ru` (по умолчанию русский).

## Формат ответа

Ответ должен быть в виде чистого JSON-объекта, готового к парсингу через `ObjectMapper`.

---

**Начало работы:** Проанализируй запрос пользователя и сгенерируй соответствующий JSON-объект согласно описанным правилам.

---

Json-схема для ответа:
```json
{
  "$schema" : "https://json-schema.org/draft/2020-12/schema",
  "type" : "object",
  "properties" : {
    "allowed-tools" : {
      "type" : "string",
      "description" : "Управляет доступом к низкоуровневым системным утилитам (bash, файловая система, сеть). Формат: 'Инструмент(параметры) Разрешения', например 'Bash(git:*) Read Write'. Параметры и разрешения опциональны. Не путать с constraints.allowedOperations — то ограничивает логические операции шагов."
    },
    "compatibility" : {
      "type" : "string",
      "description" : "Требования к окружению (до 500 символов)."
    },
    "constraints" : {
      "type" : "object",
      "properties" : {
        "allowedOperations" : {
          "description" : "Ограничивает логические операции, выполняемые в шагах (например, split, extract, length). Если указано, то каждая операция в steps[].operation должна присутствовать в этом списке. При отсутствии ограничений допустимы любые осмысленные значения. Не путать с allowed-tools — то управляет доступом к системным утилитам.",
          "type" : "array",
          "items" : {
            "type" : "string"
          }
        },
        "maxInputLength" : {
          "type" : "integer",
          "description" : "Максимальная длина входных данных (число символов)."
        },
        "timeoutSeconds" : {
          "type" : "integer",
          "description" : "Максимальное время выполнения скилла в секундах."
        }
      },
      "description" : "Ограничения на выполнение скилла."
    },
    "description" : {
      "type" : "string",
      "description" : "Краткое описание функциональности скилла и случаев использования (1-1024 символа)."
    },
    "disable-model-invocation" : {
      "type" : "boolean",
      "description" : "Скрыть скилл от модели (по умолчанию false)."
    },
    "license" : {
      "type" : "string",
      "description" : "Название лицензии или ссылка на неё."
    },
    "metadata" : {
      "type" : "object",
      "description" : "Произвольные метаданные в виде пар ключ-значение."
    },
    "name" : {
      "type" : "string",
      "description" : "Уникальное имя скилла (kebab-case, 1-64 символа). pattern: ^[a-z0-9]+(?:-[a-z0-9]+)*$ — несоблюдение может привести к ошибкам выполнения."
    },
    "paths" : {
      "description" : "Список глоб-паттернов для активации скилла по файлам.",
      "type" : "array",
      "items" : {
        "type" : "string"
      }
    },
    "priority" : {
      "type" : "integer",
      "description" : "Приоритет скилла (число, выше → раньше в списке)."
    },
    "resources" : {
      "type" : "object",
      "properties" : {
        "assets" : {
          "description" : "Список статических файлов (ассетов), используемых скиллом.",
          "type" : "array",
          "items" : {
            "type" : "object",
            "properties" : {
              "description" : {
                "type" : "string",
                "description" : "Описание назначения ассета."
              },
              "mimeType" : {
                "type" : "string",
                "description" : "MIME-тип файла (image/png, text/template и т.д.)."
              },
              "path" : {
                "type" : "string",
                "description" : "Имя файла ассета (без пути и слешей). Будет размещён в папке assets/ при экспорте."
              }
            }
          }
        },
        "embeddedFiles" : {
          "description" : "Встраиваемые текстовые файлы (шаблоны, конфигурации, схемы и т.п.) для автономной работы скилла.",
          "type" : "array",
          "items" : {
            "type" : "object",
            "properties" : {
              "content" : {
                "type" : "string",
                "description" : "Полное текстовое содержимое файла."
              },
              "description" : {
                "type" : "string",
                "description" : "Описание назначения файла."
              },
              "mimeType" : {
                "type" : "string",
                "description" : "MIME-тип (например, text/xml, application/json, text/yaml)."
              },
              "path" : {
                "type" : "string",
                "description" : "Имя файла (без слешей). Будет размещён в поддиректории, соответствующей purpose."
              },
              "purpose" : {
                "type" : "string",
                "description" : "Тег для группировки: configuration, template, data, documentation, schema, dependencies, license, query и т.п."
              }
            }
          }
        },
        "examples" : {
          "description" : "Список примеров ввода/вывода для демонстрации работы скилла.",
          "type" : "array",
          "items" : {
            "type" : "object",
            "properties" : {
              "input" : {
                "type" : "string",
                "description" : "Входные данные для примера."
              },
              "output" : {
                "type" : "string",
                "description" : "Ожидаемый выходной результат для примера."
              }
            }
          }
        },
        "references" : {
          "description" : "Список внешних ссылок (документация, API, репозитории).",
          "type" : "array",
          "items" : {
            "type" : "object",
            "properties" : {
              "description" : {
                "type" : "string",
                "description" : "Описание содержимого ссылки."
              },
              "title" : {
                "type" : "string",
                "description" : "Заголовок ссылки."
              },
              "url" : {
                "type" : "string",
                "description" : "URL внешнего ресурса или имя локального файла (без слешей). Локальные файлы копируются в папку references/ при экспорте."
              }
            }
          }
        },
        "scripts" : {
          "description" : "Список скриптов, используемых скиллом.",
          "type" : "array",
          "items" : {
            "type" : "object",
            "properties" : {
              "code" : {
                "type" : "string",
                "description" : "Содержимое скрипта (опционально, может быть большим)."
              },
              "description" : {
                "type" : "string",
                "description" : "Описание назначения скрипта."
              },
              "language" : {
                "type" : "string",
                "description" : "Язык программирования (bash, python, java и т.д.)."
              },
              "path" : {
                "type" : "string",
                "description" : "Имя файла скрипта (без пути и слешей). Будет размещён в папке scripts/ при экспорте."
              }
            }
          }
        }
      },
      "description" : "Внешние ресурсы, используемые скиллом (примеры, документация, скрипты, ассеты)."
    },
    "responseTemplate" : {
      "type" : "object",
      "properties" : {
        "content" : {
          "type" : "string",
          "description" : "Шаблон содержимого ответа с плейсхолдерами в двойных фигурных скобках ({{...}})."
        },
        "format" : {
          "type" : "string",
          "description" : "Формат ответа, например 'text' или 'json'."
        }
      },
      "description" : "Шаблон форматирования ответа."
    },
    "steps" : {
      "description" : "Последовательность шагов выполнения скилла.",
      "type" : "array",
      "items" : {
        "type" : "object",
        "properties" : {
          "description" : {
            "type" : "string",
            "description" : "Пояснение к шагу (опционально)."
          },
          "id" : {
            "type" : "string",
            "description" : "Уникальный идентификатор шага в рамках скилла."
          },
          "input" : {
            "type" : "string",
            "description" : "Шаблон входных данных для шага с плейсхолдерами ({{...}})."
          },
          "operation" : {
            "type" : "string",
            "description" : "Конкретная операция, выполняемая шагом (например, 'split', 'length')."
          },
          "output" : {
            "type" : "string",
            "description" : "Имя переменной, в которую сохраняется результат шага."
          },
          "type" : {
            "type" : "string",
            "description" : "Тип шага (например, 'calculation', 'text_processing', 'api_call', 'conditional')."
          }
        }
      }
    },
    "targetAgents" : {
      "description" : "Список CLI-агентов, для которых предназначен скилл (минимум 'qwen').",
      "type" : "array",
      "items" : {
        "type" : "string"
      }
    },
    "user-invocable" : {
      "type" : "boolean",
      "description" : "Доступность скилла через /<skill-name> (по умолчанию true)."
    }
  }
}
```

---

Запрос пользователя:
На основе документации, представленной ниже, создать скилл, который будет принимать на вход текстовое описание тест-кейса, а в ответ предоставлять тест на Java и библиотеки Selenide.
Обязательно к выполнению:
- Проверить что в проекте подключен selenide версии 7.16.2, а если не подключен, то подключить.
- Тест должен быть создан в папке /test в package `io.example`
- PageObject самой страницы должен быть сохранен в папке /main в package `io.example`

# Дополнительные рекомендации

---
name: generate-java-selenide-test
description: Преобразует описание UI тест-кейса на русском языке в автоматизированный Java-тест с Selenide. Использует Frap для анализа DOM и генерации Page Object, chrome-devtools-mcp для управления браузером. Создаёт два файла: PageObject и тестовый класс. Временные артефакты сохраняются в .frap/ и .frap-work/ в корне проекта. Используйте, когда нужно создать UI-тест по шагам, описанным на естественном языке.
priority: 10
paths:
- src/test/java/**/*.java
  user-invocable: true
  disable-model-invocation: false
  license: MIT
  compatibility: Требуется Java проект с Maven или Gradle, зависимость Selenide, доступ к MCP-серверам Frap и chrome-devtools-mcp. Создаёт временные файлы в директориях .frap/ и .frap-work/.
  allowed-tools: Bash(cat:*, grep:*, find:*, echo:*, cp:*, mv:*) Write
  metadata:
  author: "user"
  version: "1.1"
  category: "test-automation"
  tags: "java, selenide, ui-testing, frap, chrome-devtools, page-object"
---

# Преобразует описание UI тест-кейса на русском языке в авт...

## Шаги выполнения

### Шаг 1: Проверяет наличие зависимости Selenide в файле сборки проекта. Если отсутствует — ошибка и остановка.

- **Тип:** text_processing / **Операция:** check_dependency
- **Вход:** `{{project_dir}}/pom.xml или build.gradle`
- **Выход:** `selenide_exists`

### Шаг 2: Проверяет доступность MCP-серверов: вызывает frap_help (Frap) и list_pages (chrome-devtools-mcp). При недоступности любого — ошибка и остановка.

- **Тип:** api_call / **Операция:** check_availability
- **Вход:** `вызовы frap_help и list_pages`
- **Выход:** `servers_available`

### Шаг 3: Извлекает имя тестового класса (например, 'LoginTest'), URL страницы, имя пакета и шаги из описания на русском языке.

- **Тип:** text_processing / **Операция:** extract
- **Вход:** `описание тест-кейса от пользователя`
- **Выход:** `{className, pageUrl, packageName, steps_text}`

### Шаг 4: Открывает новую страницу в браузере через chrome-devtools-mcp и переходит по целевому URL.

- **Тип:** api_call / **Операция:** open_and_navigate
- **Вход:** `{{pageUrl}}`
- **Выход:** `page_ready`

### Шаг 5: Получает JavaScript-код для захвата DOM-снимка от Frap (инструмент frap_snapshot_script).

- **Тип:** api_call / **Операция:** frap_snapshot_script
- **Вход:** ``
- **Выход:** `snapshot_script`

### Шаг 6: Если скрипт содержит async/await, оборачивает его в синхронную самовызывающуюся функцию (IIFE) без async/await, чтобы обеспечить синхронное выполнение в chrome-devtools-mcp.

- **Тип:** text_processing / **Операция:** wrap_iife
- **Вход:** `{{snapshot_script}}`
- **Выход:** `sync_script`

### Шаг 7: Выполняет синхронный скрипт в браузере через chrome-devtools-mcp (evaluate_script) и получает DOM-снимок страницы.

- **Тип:** api_call / **Операция:** evaluate_script
- **Вход:** `{{sync_script}}`
- **Выход:** `dom_snapshot`

### Шаг 8: Сохраняет полученный DOM-снимок во временную папку .frap-work/snapshot в корне проекта для использования в режиме file.

- **Тип:** file_operation / **Операция:** write_file
- **Вход:** `{{dom_snapshot}} в файл .frap-work/snapshot/{{test-name-N}}.json`
- **Выход:** `snapshot_path`

### Шаг 9: Строит карту элементов (ElementMap) из DOM-снимка с помощью Frap.

- **Тип:** api_call / **Операция:** frap_build_element_map
- **Вход:** `{domSnapshotPath: {{snapshot_path}}}`
- **Выход:** `element_map_path`

### Шаг 10: Генерирует код Page Object на Java с аннотациями Selenide (language=java_selenide). Возвращает список путей к файлам (режим file).

- **Тип:** api_call / **Операция:** frap_generate_page_object
- **Вход:** `{elementMapPath: {{element_map_path}}, language: 'java_selenide', className: {{pageClassName}}, packageName: {{packageName}}}`
- **Выход:** `page_object_files`

### Шаг 11: Копирует сгенерированные Page Object файлы во временную папку .frap-work/pages/ для отладки и последующего использования.

- **Тип:** file_operation / **Операция:** copy_file
- **Вход:** `{{page_object_files}} -> .frap-work/pages/`
- **Выход:** `temp_page_object_paths`

### Шаг 12: Копирует Page Object файлы в стандартную директорию pages основного проекта (src/main/java/...).

- **Тип:** file_operation / **Операция:** copy_file
- **Вход:** `{{temp_page_object_paths}} -> src/main/java/.../pages/`
- **Выход:** `project_page_object_paths`

### Шаг 13: Генерирует код тестового класса на основе временных Page Object и описания шагов. Использует русские комментарии, аннотации TestNG/JUnit и методы Selenide.

- **Тип:** text_processing / **Операция:** template
- **Вход:** `{{temp_page_object_paths}} и {{steps_text}}`
- **Выход:** `test_class_code`

### Шаг 14: Сохраняет сгенерированный тестовый класс в стандартную директорию тестов проекта.

- **Тип:** file_operation / **Операция:** write_file
- **Вход:** `{{test_class_code}} в src/test/java/.../{{className}}Test.java`
- **Выход:** `test_file_path`

### Шаг 15: Закрывает браузер через chrome-devtools-mcp, освобождая ресурсы.

- **Тип:** api_call / **Операция:** close_page
- **Вход:** ``
- **Выход:** `browser_closed`

## Примеры

### Пример 1

**Пользователь:**
Тест-кейс: авторизация пользователя. Открыть https://example.com/login, ввести логин 'admin', пароль '123', нажать кнопку 'Войти'. Проверить, что отображается приветствие 'Добро пожаловать'.

**Ответ:**
Создан файл src/test/java/com/example/tests/LoginTest.java с методом testAuthorization(). Page Object LoginPage.java сохранён в .frap-work/pages/ и src/main/java/com/example/pages/.

### Пример 2

**Пользователь:**
Тест-кейс: поиск товара. Перейти на https://shop.com, ввести в поиск 'ноутбук', нажать Enter. Проверить, что в результатах есть хотя бы один товар.

**Ответ:**
Создан файл src/test/java/com/example/tests/SearchTest.java с методом testProductSearch(). Page Object MainPage.java сохранён в .frap-work/pages/ и src/main/java/com/example/pages/.

## Формат ответа

Формат: `text`

```text
✅ Тестовый класс {{testClassName}} успешно создан: {{testFilePath}}. Page Object сохранён во временной папке .frap-work/pages/ и в проекте в {{projectPageObjectPaths}}.
```


Документация по MCP-Frap
На основе анализа исходного кода MCP-сервера и документации из репозитория [kotler-dev/frap](https://github.com/kotler-dev/frap/tree/develop/java-v1.1.1) я подготовил подробную инструкцию по подключению и использованию сервера. В ней описаны оба режима работы (file и inline), а также приведены конкретные примеры для получения DOM-снимка через **Playwright** и **chrome-devtools-mcp**.

---

## 1. Общие принципы работы MCP-сервера frap

Сервер предоставляет **6 MCP-инструментов** для автоматизации работы с веб-страницами:

| Инструмент | Назначение |
|------------|------------|
| `frap_help` | Возвращает пошаговое руководство по использованию |
| `frap_snapshot_script` | Возвращает JavaScript-код для захвата DOM-снимка страницы |
| `frap_build_element_map` | Строит карту элементов (ElementMap) из снимка DOM |
| `frap_filter_element_map` | Фильтрует карту элементов по заданным критериям |
| `frap_generate_page_object` | Генерирует код Page Object на основе карты элементов |
| `frap_heal` | Восстанавливает селектор, который перестал работать после изменения страницы |

Сервер может работать в **двух режимах** ввода-вывода:

- **`file`** (режим по умолчанию для `frap-mcp-stdio` и `frap-mcp-http-local`) — большие артефакты (DOM-снимок, карта элементов, сгенерированный код) передаются через **абсолютные пути к файлам** на общей файловой системе. Это экономит токены контекста агента.
- **`inline`** (режим по умолчанию для `frap-mcp-http`) — все данные передаются **внутри JSON-запросов/ответов** (подходит для удалённых клиентов без общего доступа к файловой системе).

**Важно:**  
`frap_snapshot_script` возвращает **JavaScript-код**, который **вы должны выполнить на клиентской стороне** (в браузере) с помощью вашего инструмента автоматизации (Playwright, chrome-devtools-mcp и т.д.). Сервер не имеет собственного браузера и не может выполнить этот код за вас.

---

## 2. Подключение MCP-сервера

Вы можете запустить сервер в одном из трёх вариантов:

### 2.1. `frap-mcp-stdio` (режим `file`, рекомендуется для локального использования)

Сервер запускается как дочерний процесс и общается через `stdin`/`stdout`.

**Сборка:**
```bash
mvn -f sdk/java/frap-mcp/pom.xml -pl frap-mcp-stdio -am package -DskipTests
```

**Запуск через Claude Code (CLI):**
```bash
claude mcp add frap-stdio --transport stdio -- \
  java -jar /ABS/PATH/sdk/java/frap-mcp/frap-mcp-stdio/target/frap-mcp-stdio.jar
```

**Конфигурация в `.mcp.json`:**
```json
{
  "mcpServers": {
    "frap-stdio": {
      "command": "java",
      "args": [
        "-jar",
        "/ABS/PATH/sdk/java/frap-mcp/frap-mcp-stdio/target/frap-mcp-stdio.jar"
      ]
    }
  }
}
```

**Дополнительные параметры (опционально):**
- `-Dfrap.runtime.dir=/path` — базовая директория для бинарных файлов, рабочих артефактов и логов (по умолчанию `<директория jar>/.frap`).
- `--frap.io.work-dir=/path` — директория для артефактов (по умолчанию `<frap.runtime.dir>/work`).

### 2.2. `frap-mcp-http` (режим `inline`, подходит для удалённых клиентов)

Обычное веб-приложение, работающее по протоколу HTTP.

**Сборка:**
```bash
mvn -f sdk/java/frap-mcp/pom.xml -pl frap-mcp-http -am package -DskipTests
```

**Запуск:**
```bash
java -jar /ABS/PATH/sdk/java/frap-mcp/frap-mcp-http/target/frap-mcp-http.jar
```
Сервер будет доступен по адресу `http://localhost:8080/mcp`.

**Подключение в Claude Code:**
```bash
claude mcp add frap-http --transport http http://localhost:8080/mcp
```

### 2.3. `frap-mcp-http-local` (режим `file`, HTTP + общая файловая система)

Аналог `frap-mcp-http`, но работает в режиме `file`. Подходит для случаев, когда клиент и сервер находятся на одной машине и имеют общую файловую систему.

**Сборка:**
```bash
mvn -f sdk/java/frap-mcp/pom.xml -pl frap-mcp-http-local -am package -DskipTests
```

**Запуск:**
```bash
java -jar /ABS/PATH/sdk/java/frap-mcp/frap-mcp-http-local/target/frap-mcp-http-local.jar
```
По умолчанию сервер слушает порт `8765`. Эндпоинт — `http://localhost:8765/mcp`.

---

## 3. Использование с Playwright

### 3.1. Получение DOM-снимка (шаг 1)

1. **Вызовите инструмент `frap_snapshot_script`** (без аргументов). Он вернёт строку с JavaScript-кодом.
2. **Выполните этот код на странице** с помощью Playwright.

**Пример на JavaScript (Playwright):**
```javascript
const { chromium } = require('playwright');

(async () => {
  const browser = await chromium.launch();
  const page = await browser.newPage();
  await page.goto('https://example.com');

  // 1. Получаем скрипт от MCP-сервера (этот шаг выполняется через MCP-клиент)
  // Допустим, мы уже вызвали frap_snapshot_script и получили строку script
  const script = await mcpClient.callTool('frap_snapshot_script', {});

  // 2. Выполняем скрипт на странице
  const snapshot = await page.evaluate(script);

  // 3. Сохраняем снимок в файл (для режима file) или передаём как объект (для режима inline)
  const fs = require('fs');
  const snapshotPath = './snapshot.json';
  fs.writeFileSync(snapshotPath, JSON.stringify(snapshot, null, 2));

  await browser.close();
})();
```

**Примечание:**  
В режиме `file` скрипт может быть модифицирован сервером для автоматической отправки снимка на эндпоинт `/frap/ingest` (если он доступен). В этом случае он вернёт не сам снимок, а `{ snapshot_path: "...путь..." }`. Однако приведённый выше подход с ручным сохранением работает всегда.

### 3.2. Построение карты элементов (шаг 2)

**В режиме `file`:** передайте путь к файлу со снимком.
```json
{
  "domSnapshotPath": "/abs/path/to/snapshot.json"
}
```

**В режиме `inline`:** передайте объект снимка напрямую.
```json
{
  "domSnapshot": { "html": "...", "elements": [...] }
}
```

**Пример вызова через MCP-клиент (режим `file`):**
```javascript
const result = await mcpClient.callTool('frap_build_element_map', {
  domSnapshotPath: '/abs/path/to/snapshot.json'
});
// result содержит { element_map_path: "...", summary: {...} }
```

### 3.3. Генерация Page Object (шаг 3)

**В режиме `file`:**
```javascript
const genResult = await mcpClient.callTool('frap_generate_page_object', {
  elementMapPath: '/abs/path/to/element-map.json',
  language: 'java_playwright',
  className: 'MainPage',
  packageName: 'com.example.pages'
});
// genResult содержит { file_paths: [...], file_count: N, work_dir: "..." }
```

**В режиме `inline`:**
```javascript
const genResult = await mcpClient.callTool('frap_generate_page_object', {
  elementMap: { ... }, // объект ElementMap
  language: 'java_playwright',
  className: 'MainPage',
  packageName: 'com.example.pages'
});
// genResult содержит { files: [ { path, content }, ... ] }
```

### 3.4. Фильтрация карты элементов (опционально)

**В режиме `file`:**
```javascript
const filterResult = await mcpClient.callTool('frap_filter_element_map', {
  elementMapPath: '/abs/path/to/element-map.json',
  filter: {
    interactive_only: true,
    min_cluster_size: 2,
    tags: ['button', 'a']
  }
});
// Возвращает { element_map_path: "...", summary: {...} }
```

### 3.5. Восстановление селектора (инструмент `frap_heal`)

**В режиме `file`:**
```javascript
const healResult = await mcpClient.callTool('frap_heal', {
  domSnapshotPath: '/abs/path/to/fresh-snapshot.json',
  primarySelector: '#old-selector',
  originalSignature: { ... }, // опционально
  minConfidence: 0.85
});
// Возвращает { healed: true/false, selector: "...", confidence: 0.91, ... }
```

---

Инструкция дополнена разделом, который чётко объясняет, что при использовании **chrome-devtools-mcp** скрипт, возвращаемый `frap_snapshot_script`, должен выполняться **синхронно** — без `async/await` внутри передаваемого кода. Это гарантирует, что инструмент `evaluate_script` немедленно получит результат, а не Promise, который может не быть корректно обработан некоторыми MCP-клиентами.

---

## 4. Использование с chrome-devtools-mcp (дополнено)

`chrome-devtools-mcp` предоставляет инструмент `evaluate_script` для выполнения JavaScript на странице через Chrome DevTools Protocol.

### Важное требование: синхронное выполнение
- Скрипт, возвращаемый `frap_snapshot_script`, представляет собой **синхронную самовызывающуюся функцию (IIFE)**, которая сразу возвращает объект `{ html, elements }`.
- При передаче этого скрипта в `evaluate_script` **не оборачивайте его в `async`** и не используйте внутри `await` (если только сервер явно не модифицировал скрипт для асинхронной отправки на эндпоинт `/frap/ingest`, но это отдельный случай).
- Инструмент `evaluate_script` ожидает синхронный код, возвращающий значение. Если вы передадите асинхронную функцию, она вернёт `Promise`, и некоторые реализации MCP могут не дождаться его разрешения, что приведёт к ошибке или пустому результату.

**Рекомендация:** всегда передавайте скрипт как есть (строку, полученную от `frap_snapshot_script`), без дополнительных обёрток.

### 4.1. Получение DOM-снимка (синхронный вызов)

```javascript
// 1. Получаем скрипт от frap (синхронный IIFE)
const scriptResult = await frapClient.callTool('frap_snapshot_script', {});
const script = scriptResult; // строка вида "(() => { ... })()"

// 2. Выполняем скрипт синхронно через chrome-devtools-mcp
//    ВАЖНО: не используйте async/await внутри script, он уже самовызывающийся
const evalResult = await chromeDevtoolsClient.callTool('evaluate_script', {
  function: script   // передаём как строку
});
// evalResult — это объект { html: "...", elements: [...] }

// 3. Сохраняем результат в файл (для режима file)
const fs = require('fs');
const snapshotPath = './snapshot.json';
fs.writeFileSync(snapshotPath, JSON.stringify(evalResult, null, 2));
```

**Почему синхронный?**  
Скрипт `(() => { ... })()` выполняется сразу и возвращает объект. Он не содержит асинхронных операций (fetch, setTimeout) в базовой версии. Если вы используете режим `file` с эндпоинтом `/frap/ingest`, сервер может подменить скрипт на асинхронный (с `fetch`), но в этом случае он вернёт `Promise`, который `evaluate_script` должен корректно обработать. Однако для единообразия и надёжности рекомендуется использовать стандартный синхронный скрипт, а сохранение снимка выполнять на стороне клиента (как показано выше).

---

Остальные шаги (построение карты, генерация Page Object) выполняются так же, как описано в разделе 3 для Playwright. Разница только в способе получения снимка.

---

## Полный пример конвейера с chrome-devtools-mcp (режим `file`)

```javascript
// 1. Получаем скрипт
const script = await frapClient.callTool('frap_snapshot_script', {});

// 2. Синхронно выполняем в браузере через chrome-devtools-mcp
const snapshot = await chromeDevtoolsClient.callTool('evaluate_script', {
  function: script
});

// 3. Сохраняем снимок в файл (вручную)
const fs = require('fs');
fs.writeFileSync('./snapshot.json', JSON.stringify(snapshot));

// 4. Строим карту элементов (file mode)
const buildResult = await frapClient.callTool('frap_build_element_map', {
  domSnapshotPath: '/abs/path/to/snapshot.json'
});
const mapPath = buildResult.element_map_path;

// 5. Генерируем Page Object
const genResult = await frapClient.callTool('frap_generate_page_object', {
  elementMapPath: mapPath,
  language: 'java_playwright',
  className: 'MainPage',
  packageName: 'com.example.pages'
});
console.log('Сгенерированные файлы:', genResult.file_paths);
```

---

## 5. Полный пример конвейера (режим `file`)

1. **Запустите MCP-сервер** (например, `frap-mcp-stdio`).
2. **Вызовите `frap_snapshot_script`** → получите JS-код.
3. **Выполните JS-код на странице** (через Playwright или `chrome-devtools-mcp`) и сохраните результат в JSON-файл (например, `snapshot.json`).
4. **Вызовите `frap_build_element_map`** с путём `domSnapshotPath: "/abs/path/to/snapshot.json"` → получите путь к файлу карты элементов.
5. **(Опционально)** Вызовите `frap_filter_element_map` с путём к карте элементов и фильтром.
6. **Вызовите `frap_generate_page_object`** с путём к карте элементов, языком, именем класса и пакетом → получите список путей к сгенерированным файлам.

**Итоговый код на JavaScript (с Playwright и MCP-клиентом):**
```javascript
// Предполагается, что у вас есть экземпляры MCP-клиентов для frap и chrome-devtools-mcp

// 1. Получаем скрипт
const script = await frapClient.callTool('frap_snapshot_script', {});

// 2. Выполняем на странице через Playwright
const snapshot = await page.evaluate(script);
fs.writeFileSync('./snapshot.json', JSON.stringify(snapshot));

// 3. Строим карту элементов
const buildResult = await frapClient.callTool('frap_build_element_map', {
  domSnapshotPath: '/abs/path/to/snapshot.json'
});
const mapPath = buildResult.element_map_path;

// 4. Генерируем Page Object
const genResult = await frapClient.callTool('frap_generate_page_object', {
  elementMapPath: mapPath,
  language: 'java_playwright',
  className: 'MainPage',
  packageName: 'com.example.pages'
});
console.log('Сгенерированные файлы:', genResult.file_paths);
```

---

## 6. Важные замечания

- **Режим `file` требует общей файловой системы** между клиентом и сервером. Убедитесь, что пути, которые вы передаёте, доступны серверу для чтения/записи.
- **Режим `inline`** подходит для удалённых клиентов, но может привести к большому объёму передаваемых данных (особенно для больших страниц).
- **`frap_snapshot_script`** всегда возвращает JavaScript, который выполняется **в контексте страницы**. Он не имеет доступа к файловой системе и не может самостоятельно сохранять файлы (если только сервер не предоставляет эндпоинт для приёма снимков).
- **Безопасность:** эндпоинт `/frap/ingest` (если используется) предназначен только для локального доступа и не должен быть открыт для внешних сетей.

---

## 7. Дополнительные ресурсы

- Исходный код: [github.com/kotler-dev/frap](https://github.com/kotler-dev/frap/tree/develop/java-v1.1.1)
- Документация по инструментам доступна через вызов `frap_help` (он всегда возвращает актуальное руководство для вашего режима).

Если у вас возникнут вопросы, используйте `frap_help` — он выдаст подробную инструкцию с учётом текущего режима работы сервера.