---
name: checkstyle-simple-check
description: Запускает проверку стиля кода с помощью Checkstyle без интеграции с Maven. Для выполнения необходимо выполнить скрипт `scripts/run_checkstyle.sh` с параметрами: [версия Checkstyle] [имя конфига] [директория исходников]. По умолчанию: 10.18.1, my_simple_checks.xml, src. Используйте, когда нужно проверить код на соответствие правилам, описанным в XML-конфиге, без изменения pom.xml.
user-invocable: true
license: Apache-2.0
compatibility: Требуется Java 11+, bash, curl и доступ в интернет для скачивания Checkstyle JAR.
allowed-tools: Bash(*) Read Write
---

# Запускает проверку стиля кода с помощью Checkstyle без ин...

## Шаги выполнения

### Шаг 1: Проверить наличие и содержимое скрипта запуска Checkstyle.

- **Тип:** file_operation / **Операция:** read
- **Вход:** `scripts/run_checkstyle.sh`
- **Выход:** `script_content`

### Шаг 2: Запустить Checkstyle с заданными параметрами. По умолчанию: версия 10.18.1, конфиг my_simple_checks.xml, директория src. Замените параметры на актуальные, если они отличаются от значений по умолчанию.

- **Тип:** shell_command / **Операция:** execute
- **Вход:** `bash scripts/run_checkstyle.sh {{checkstyle_version}} {{config_file}} {{source_dir}}`
- **Выход:** `checkstyle_result`

### Шаг 3: Извлечь из вывода Checkstyle список ошибок для итогового ответа.

- **Тип:** text_processing / **Операция:** extract_errors
- **Вход:** `{{checkstyle_result}}`
- **Выход:** `errors`

## Примеры

### Пример 1

**Пользователь:**
Запусти проверку Checkstyle

**Ответ:**
Запускаю скрипт: bash scripts/run_checkstyle.sh 10.18.1 my_simple_checks.xml src
[результат проверки]

## Ресурсы

### Ссылки

- [Checkstyle Configuration](https://checkstyle.org/config.html) — Документация по встроенным модулям и конфигурации Checkstyle.
- [RegexpSingleline](https://checkstyle.org/config_regexp.html) — Документация по использованию проверок на основе регулярных выражений.

### Скрипты

- `run_checkstyle.sh` (bash) — Скачивает Checkstyle JAR в assets/ и запускает проверку с переданным XML-конфигом из embedded/.

### Ассеты

- `.gitkeep` (text/plain) — Заглушка для сохранения папки assets. Сюда будет скачан checkstyle JAR.

### Встроенные файлы

- `my_simple_checks.xml` (text/xml) — Конфигурация Checkstyle с правилами: AvoidStarImport, JavadocMethod, запрет System.out.println и printStackTrace. [configuration]

## Формат ответа

Формат: `text`

```text
Результат проверки Checkstyle:

{{errors}}
```

## Ограничения

- Таймаут выполнения: 300 секунд
- Разрешённые операции: read, execute, extract_errors

## Структура скилла

```
checkstyle-simple-check/
├── SKILL.md
├── scripts/
│   └── run_checkstyle.sh
├── embedded/
│   └── my_simple_checks.xml
└── assets/
    └── .gitkeep
```

