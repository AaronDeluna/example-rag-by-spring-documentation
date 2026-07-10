---
name: checkstyle-custom-checks
description: Запускает проверки Checkstyle с кастомным XML-конфигом, используя автономный JAR без интеграции в проект (не требует pom.xml). Используйте для статического анализа Java-кода с собственными правилами, такими как запрет System.out.println или printStackTrace.
priority: 50
paths:
  - **/*.java
user-invocable: true
license: MIT
compatibility: Требуется Java 8+, Bash, curl (или wget) для загрузки JAR. Проверки выполняются локально, без подключения плагина к проекту.
allowed-tools: Bash(java:*) Bash(curl:*) Bash(mkdir:*) Read Write
metadata:
  author: "user"
  version: "1.0.0"
  category: "code-quality"
  tags: "[checkstyle, java, linting, static-analysis]"
---

# Запускает проверки Checkstyle с кастомным XML-конфигом, и...

## Шаги выполнения

### Шаг 1: Проверяет, существует ли Checkstyle JAR по пути ~/.cache/checkstyle/checkstyle-10.18.0-all.jar.

- **Тип:** conditional / **Операция:** exists
- **Вход:** `{{jar_path}}`
- **Выход:** `jar_exists`

### Шаг 2: Загружает JAR Checkstyle версии 10.18.0, если он отсутствует в кэше.

- **Тип:** api_call / **Операция:** download
- **Вход:** `https://github.com/checkstyle/checkstyle/releases/download/checkstyle-10.18.0/checkstyle-10.18.0-all.jar`
- **Выход:** `jar_path`

### Шаг 3: Запускает Checkstyle с кастомной конфигурацией на указанной директории исходников.

- **Тип:** api_call / **Операция:** run
- **Вход:** `java -jar {{jar_path}} -c {{config_path}} {{source_directory}}`
- **Выход:** `raw_report`

### Шаг 4: Преобразует вывод Checkstyle в структурированный список проблем для отображения пользователю.

- **Тип:** text_processing / **Операция:** parse
- **Вход:** `{{raw_report}}`
- **Выход:** `issues`

## Примеры

### Пример 1

**Пользователь:**
src/main/java

**Ответ:**
[ERROR] Main.java:5: Использование System.out.println() запрещено.
[ERROR] Util.java:12: Использование printStackTrace() запрещено.

## Ресурсы

### Ссылки

- [Checkstyle Documentation](https://checkstyle.org/) — Официальная документация Checkstyle.
- [Checkstyle Configuration](https://checkstyle.org/config.html) — Описание синтаксиса конфигураций Checkstyle.

### Скрипты

- `run_checkstyle.sh` (bash) — Основной скрипт, проверяющий наличие JAR, скачивающий его при необходимости и запускающий проверку Checkstyle.

### Встроенные файлы

- `my_simple_checks.xml` (text/xml) — Конфигурация Checkstyle с простыми правилами: AvoidStarImport, JavadocMethod, запрет System.out.println и printStackTrace. [configuration]

## Формат ответа

Формат: `text`

```text
Результат проверки Checkstyle:
{{issues}}
```

## Ограничения

- Максимальная длина ввода: 5000000 символов
- Таймаут выполнения: 180 секунд

## Структура скилла

```
checkstyle-custom-checks/
├── SKILL.md
├── scripts/
│   └── run_checkstyle.sh
└── embedded/
    └── my_simple_checks.xml
```

