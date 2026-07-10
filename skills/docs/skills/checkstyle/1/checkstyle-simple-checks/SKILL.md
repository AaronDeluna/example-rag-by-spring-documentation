---
name: checkstyle-simple-checks
description: Запускает проверку Checkstyle с кастомными простыми правилами (без написания Java-кода) через bash-скрипт, автоматически загружая Checkstyle JAR из Maven Central во временную папку. Не требует добавления Checkstyle как зависимости проекта. Используйте для статического анализа Java-кода с собственными запретами через регулярные выражения.
paths:
  - src/main/java/**/*.java
  - **/*.java
user-invocable: true
license: MIT
compatibility: Requires Java 11+, bash, and curl or wget for downloading Checkstyle JAR.
allowed-tools: Bash Read Write
metadata:
  author: "user"
  category: "code-quality"
  tags: "checkstyle, java, static-analysis, custom-checks"
---

# Запускает проверку Checkstyle с кастомными простыми прави...

## Ресурсы

### Ссылки

- [Checkstyle Documentation](https://checkstyle.org/) — Официальная документация Checkstyle

### Скрипты

- `run-checkstyle-simple-custom.sh` (bash) — Загружает Checkstyle JAR из Maven Central во временную папку и запускает проверку с конфигурацией простых проверок.

## Формат ответа

Формат: `text`

```text
Результат проверки:
{{output}}
```

