---
name: checkstyle-simple-check
description: Запускает проверку Java-кода с помощью Checkstyle без подключения зависимости к проекту. Использует загружаемый JAR и кастомные XML-правила с простыми проверками (RegexpSingleline, AvoidStarImport и т.д.). Применяйте, когда требуется статический анализ Java без изменения pom.xml или build.gradle, и нужны легковесные проверки без написания Java-модулей.
priority: 5
user-invocable: true
license: MIT
compatibility: Требуется Java 11+, curl или wget, доступ в интернет для загрузки JAR из Maven Central.
allowed-tools: Bash(java:*, curl:*, wget:*) Read
metadata:
  author: "skill-generator"
  version: "1.0.0"
  category: "code-quality"
  tags: "java, checkstyle, static-analysis, lint"
---

# Запускает проверку Java-кода с помощью Checkstyle без под...

## Шаги выполнения

### Шаг 1: Загружает указанную версию Checkstyle JAR из Maven Central во временную директорию. Использует curl или wget.

- **Тип:** execution / **Операция:** download
- **Вход:** `{{checkstyle_version}}`
- **Выход:** `jar_path`

### Шаг 2: Запускает Checkstyle с подготовленной конфигурацией и анализирует исходный код Java в указанной директории (по умолчанию ./src/main/java).

- **Тип:** execution / **Операция:** run_checkstyle
- **Вход:** `{{source_path}}`
- **Выход:** `checkstyle_report`

## Ресурсы

### Ссылки

- [Checkstyle Documentation](https://checkstyle.org/) — Официальная документация Checkstyle.

### Скрипты

- `run-checkstyle-simple-custom.sh` (bash) — Скрипт загружает Checkstyle JAR (по умолчанию 10.18.1) и выполняет проверку с простыми кастомными правилами из embedded-конфигурации.

### Встроенные файлы

- `my_simple_checks.xml` (text/xml) — Конфигурация Checkstyle с простыми кастомными проверками (RegexpSingleline, AvoidStarImport, JavadocMethod). [configuration]

## Формат ответа

Формат: `text`

```text
Результат проверки Checkstyle:
{{checkstyle_report}}
```

## Ограничения

- Таймаут выполнения: 120 секунд

## Структура скилла

```
checkstyle-simple-check/
├── SKILL.md
├── scripts/
│   └── run-checkstyle-simple-custom.sh
└── embedded/
    └── my_simple_checks.xml
```

