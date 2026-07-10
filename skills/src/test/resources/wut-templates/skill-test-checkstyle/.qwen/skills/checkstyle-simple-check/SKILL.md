---
name: checkstyle-simple-check
description: Выполняет статический анализ Java-кода с помощью Checkstyle без добавления плагина в pom.xml и без написания Java-модулей. Использует скачанный JAR и кастомный XML-конфиг с простыми правилами (RegexpSingleline и др.). Используйте, когда нужно быстро проверить код на соответствие стандартам, не изменяя проект, и когда требуется гибкая конфигурация через встроенные модули Checkstyle.
user-invocable: true
license: MIT
compatibility: Требуется Java 8+, bash, curl или wget, доступ в интернет для автоматической загрузки Checkstyle JAR.
allowed-tools: Bash(curl:*, java:*, wget:*) Read Write
metadata:
  category: "code-quality"
  tags: "[checkstyle, linting, java, static-analysis]"
---

# Выполняет статический анализ Java-кода с помощью Checksty...

## Шаги выполнения

### Шаг 1: Запускает загрузку Checkstyle JAR (если отсутствует) и выполняет проверку исходного кода Java. Параметр 'checkstyle_version' (опционально) – версия Checkstyle, по умолчанию 10.18.1. 'source_directory' – путь к проверяемой директории, по умолчанию текущая рабочая директория.

- **Тип:** shell_command / **Операция:** execute
- **Вход:** `bash scripts/checkstyle-simple-check.sh {{checkstyle_version}} {{source_directory}}`
- **Выход:** `checkstyle-report`

## Инструкции для агента

Для шагов с типом `shell_command` используйте инструмент `run_shell_command` для выполнения команд, указанных в поле `input`.

## Ресурсы

### Ссылки

- [Checkstyle Documentation](https://checkstyle.org/) — Официальная документация Checkstyle, описание модулей и конфигурации.

### Скрипты

- `checkstyle-simple-check.sh` (bash) — Скрипт для загрузки Checkstyle JAR (если отсутствует) и запуска проверки с кастомным XML-конфигом.

### Встроенные файлы

- `my_simple_checks.xml` (text/xml) — Конфигурация Checkstyle с простыми проверками на основе встроенных модулей (избегание звездочек в импортах, Javadoc, запрет System.out.println и printStackTrace). [configuration]

## Формат ответа

Формат: `text`

```text
Результат проверки Checkstyle:
{{checkstyle-report}}
```

## Ограничения

- Таймаут выполнения: 180 секунд
- Разрешённые операции: execute

## Структура скилла

```
checkstyle-simple-check/
├── SKILL.md
├── scripts/
│   └── checkstyle-simple-check.sh
└── embedded/
    └── my_simple_checks.xml
```

