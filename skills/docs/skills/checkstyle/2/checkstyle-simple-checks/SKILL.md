---
name: checkstyle-simple-checks
description: Запускает проверку стиля кода Java с помощью Checkstyle, используя только встроенные модули и XML-конфигурацию без написания Java-кода. Идеально для проверки стандартных правил (запрет звездного импорта, System.out.println и т.д.) без подключения зависимости к проекту. Используйте при ревью кода или настройке CI.
priority: 50
user-invocable: true
license: MIT
compatibility: Требуется Java 11+, утилита curl или wget, доступ в интернет для загрузки Checkstyle JAR.
allowed-tools: Bash(curl:*) Bash(wget:*) Bash(java:*)
metadata:
  author: "generated"
  version: "1.0.0"
  category: "code-quality"
  tags: "[checkstyle, java, lint, code-style]"
---

# Запускает проверку стиля кода Java с помощью Checkstyle, ...

## Примеры

### Пример 1

**Пользователь:**
Запуск скилла в Java-проекте со стандартной структурой Maven/Gradle

**Ответ:**
Starting audit...
[WARN] src/main/java/com/example/App.java:5: Запрещён звёздчатый импорт - java.util.*.
Audit done.

## Ресурсы

### Ссылки

- [Checkstyle Official Site](https://checkstyle.sourceforge.io/) — Официальный сайт и документация Checkstyle

### Скрипты

- `run-checkstyle-simple-custom.sh` (bash) — Загружает Checkstyle JAR и запускает проверку кода с XML-конфигурацией, содержащей простые правила. Принимает опциональный путь к исходникам (по умолчанию ./src/main/java).

### Встроенные файлы

- `my_simple_checks.xml` (text/xml) — XML-конфигурация Checkstyle с кастомными правилами: запрет звездного импорта, System.out.println, printStackTrace(), требование JavaDoc для публичных методов. [configuration]

## Формат ответа

Формат: `text`

```text
Результат проверки Checkstyle:
{{output}}
```

## Ограничения

- Таймаут выполнения: 300 секунд

## Структура скилла

```
checkstyle-simple-checks/
├── SKILL.md
├── scripts/
│   └── run-checkstyle-simple-custom.sh
└── embedded/
    └── my_simple_checks.xml
```

