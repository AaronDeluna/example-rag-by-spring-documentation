---
name: checkstyle-simple-checks
description: Запускает Checkstyle с пользовательскими простыми проверками (на основе регулярных выражений, без Java-кода) для исходных файлов Java с использованием предоставленного bash-скрипта. Используйте, когда необходимо обеспечить соблюдение стандартов кодирования, таких как запрет звёздных импортов, System.out.println, printStackTrace и требование Javadoc для публичных методов.
priority: 10
paths:
  - src/**/*.java
user-invocable: true
disable-model-invocation: false
license: MIT
compatibility: Требуется Java 11+ и checkstyle-10.18.1-all.jar, расположенный в каталоге lib/. Необходима оболочка Bash.
allowed-tools: Bash(run-checkstyle-simple-custom.sh:*) Read
metadata:
  author: "user"
  version: "1.0"
  tags: "checkstyle, java, линтинг, качество кода"
---

# Запускает Checkstyle с пользовательскими простыми проверк...

## Шаги выполнения

### Шаг 1: Проверяет наличие JAR-файла Checkstyle и пользовательского XML-конфига.

- **Тип:** bash / **Операция:** check
- **Вход:** `{{checkstyle_jar}} {{config_file}}`
- **Выход:** `dependencies_ok`

### Шаг 2: Запускает Checkstyle с конфигурацией простых пользовательских проверок для целевых исходных файлов Java.

- **Тип:** bash / **Операция:** execute
- **Вход:** `{{source_path}}`
- **Выход:** `report`

## Примеры

### Пример 1

**Пользователь:**
Запустить checkstyle для src/main/java с пользовательскими простыми правилами

**Ответ:**
Checkstyle завершён: 0 нарушений найдено.

## Формат ответа

Формат: `text`

```text
Отчёт Checkstyle:
{{report}}
```

## Ограничения

- Таймаут выполнения: 120 секунд

