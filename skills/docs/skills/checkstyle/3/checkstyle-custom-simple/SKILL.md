---
name: checkstyle-custom-simple
description: Запускает проверки Checkstyle с простыми кастомными правилами, описанными в XML-конфигурации, без добавления зависимости Checkstyle в проект. Используйте, когда нужно проверить Java-код на соответствие стилю с собственными правилами, не изменяя build-файлы.
paths:
  - src/**/*.java
user-invocable: true
license: MIT
compatibility: Требуется Java 11+, curl или wget, доступ к Maven Central.
allowed-tools: Bash(curl:*) Bash(wget:*) Bash(mktemp:*) Bash(rm:*) Bash(java:*) Read
metadata:
  category: "code-quality"
  tags: "java, checkstyle, static-analysis, linting"
---

# Запускает проверки Checkstyle с простыми кастомными прави...

## Ресурсы

### Ссылки

- [Checkstyle Documentation](https://checkstyle.org/) — Официальная документация Checkstyle по конфигурации и встроенным модулям.

### Скрипты

- `run-checkstyle-simple-custom.sh` (bash) — Загружает Checkstyle JAR во временную директорию и запускает проверку с простыми кастомными правилами.

### Встроенные файлы

- `my_simple_checks.xml` (text/xml) — Конфигурация Checkstyle с простыми кастомными правилами (RegexpSingleline, избегание звёздных импортов, Javadoc). [configuration]

## Структура скилла

```
checkstyle-custom-simple/
├── SKILL.md
├── scripts/
│   └── run-checkstyle-simple-custom.sh
└── embedded/
    └── my_simple_checks.xml
```

