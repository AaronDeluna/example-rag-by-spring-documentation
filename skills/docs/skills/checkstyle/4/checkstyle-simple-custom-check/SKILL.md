---
name: checkstyle-simple-custom-check
description: Запускает проверку Checkstyle с простыми кастомными правилами без подключения зависимости к проекту. Использует загружаемый JAR и XML-конфигурацию. Применяйте для проверки стиля Java-кода, запрета System.out.println, printStackTrace и других правил.
user-invocable: true
license: MIT
compatibility: Требуется Java 11+, curl или wget, доступ в интернет для загрузки JAR.
allowed-tools: Bash(java:*) Bash(curl:*) Bash(wget:*) Read Write
metadata:
  author: "user"
  category: "code-quality"
  tags: "[checkstyle, java, linting, custom-checks]"
---

# Запускает проверку Checkstyle с простыми кастомными прави...

## Ресурсы

### Ссылки

- [Checkstyle Documentation](https://checkstyle.org/) — Документация Checkstyle

### Скрипты

- `run-checkstyle-simple-custom.sh` (bash) — Загружает Checkstyle JAR и запускает проверку с конфигурацией my_simple_checks.xml.

### Встроенные файлы

- `my_simple_checks.xml` (text/xml) — Конфигурация Checkstyle с простыми кастомными проверками (запрет System.out.println, printStackTrace и т.д.) [configuration]

## Ограничения

- Таймаут выполнения: 120 секунд

## Структура скилла

```
checkstyle-simple-custom-check/
├── SKILL.md
├── scripts/
│   └── run-checkstyle-simple-custom.sh
└── embedded/
    └── my_simple_checks.xml
```

