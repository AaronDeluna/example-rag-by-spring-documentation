---
name: generate-java-selenide-test
description: Преобразует текстовое описание UI тест-кейса на русском языке в Java-тест с Selenide. Проверяет и при необходимости подключает Selenide 7.16.2. Генерирует PageObject в src/main/java/io/example и тестовый класс в src/test/java/io/example. Использует Frap и chrome-devtools-mcp. Временные артефакты сохраняются в .frap-work/. Используйте, когда нужно создать UI-тест по шагам, описанным на естественном языке.
priority: 10
paths:
  - src/test/java/**/*.java
user-invocable: true
disable-model-invocation: false
license: MIT
compatibility: Требуется Java-проект с Maven или Gradle, зависимость Selenide, доступ к MCP-серверам Frap и chrome-devtools-mcp. Временные файлы создаются в .frap/ и .frap-work/. Текущая версия скилла адаптирована для Frap MCP v1.1.1.
allowed-tools: Bash(cat:*, grep:*, find:*, echo:*, cp:*, mv:*, mvn:*, gradle:*) Write
metadata:
  author: "user"
  version: "1.1"
  category: "test-automation"
  tags: "java, selenide, ui-testing, frap, chrome-devtools, page-object"
---

# Преобразует текстовое описание UI тест-кейса на русском я...

## Шаги выполнения

### Шаг 1: Проверяет, что Selenide версии 7.16.2 присутствует в зависимостях. Если отсутствует или другая версия – добавляет/обновляет в файле сборки (pom.xml или build.gradle).

- **Тип:** file_operation / **Операция:** check_and_update_dependency
- **Вход:** `{{project_dir}}/pom.xml или build.gradle`
- **Выход:** `selenide_ready`

### Шаг 2: Проверяет доступность MCP-серверов Frap и chrome-devtools-mcp. При недоступности любого – ошибка и остановка.

- **Тип:** api_call / **Операция:** check_availability
- **Вход:** `вызовы frap_help и list_pages`
- **Выход:** `servers_available`

### Шаг 3: Парсит предоставленный пользователем текст описания тест-кейса. Извлекает: имя тестового класса (например, 'LoginTest'), URL страницы, имя пакета (по умолчанию io.example) и перечень шагов. Файлы тестов на данном этапе ещё не существуют — извлечение идёт исключительно из текста.

- **Тип:** text_processing / **Операция:** parse_test_description
- **Вход:** `текстовое описание тест-кейса, полученное от пользователя`
- **Выход:** `{className, pageUrl, packageName, steps_text}`

### Шаг 4: Открывает новую страницу в браузере через chrome-devtools-mcp и переходит по целевому URL.

- **Тип:** api_call / **Операция:** open_and_navigate
- **Вход:** `{{pageUrl}}`
- **Выход:** `page_ready`

### Шаг 5: Получает JavaScript-код для захвата DOM-снимка от Frap (инструмент frap_snapshot_script). Код всегда содержит асинхронные вызовы (async/await), поэтому на следующем шаге будет преобразован в синхронную IIFE.

- **Тип:** api_call / **Операция:** frap_snapshot_script
- **Вход:** ``
- **Выход:** `snapshot_script`

### Шаг 6: Оборачивает асинхронный скрипт в синхронную самовызывающуюся функцию (IIFE) без async/await, чтобы гарантировать синхронное выполнение в chrome-devtools-mcp. Операция выполняется всегда, так как скрипт точно содержит асинхронный код.

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

### Шаг 10: Генерирует код Page Object на Java с аннотациями Selenide (language=java_selenide). Возвращает список путей к файлам.

- **Тип:** api_call / **Операция:** frap_generate_page_object
- **Вход:** `{elementMapPath: {{element_map_path}}, language: 'java_selenide', className: {{pageClassName}}, packageName: {{packageName}}}`
- **Выход:** `page_object_files`

### Шаг 11: Копирует сгенерированные Page Object файлы во временную папку .frap-work/pages/ для отладки и последующего использования.

- **Тип:** file_operation / **Операция:** copy_file
- **Вход:** `{{page_object_files}} -> .frap-work/pages/`
- **Выход:** `temp_page_object_paths`

### Шаг 12: Копирует Page Object файлы в стандартную директорию проекта src/main/java, формируя путь из packageName (по умолчанию io/example).

- **Тип:** file_operation / **Операция:** copy_file
- **Вход:** `{{temp_page_object_paths}} -> src/main/java/{{packageName с заменой точек на слеши}}/...`
- **Выход:** `project_page_object_paths`

### Шаг 13: Генерирует код тестового класса с русскими комментариями, аннотациями TestNG/JUnit и методами Selenide на основе временных Page Object и описания шагов.

- **Тип:** text_processing / **Операция:** template
- **Вход:** `{{temp_page_object_paths}} и {{steps_text}}`
- **Выход:** `test_class_code`

### Шаг 14: Сохраняет сгенерированный тестовый класс в стандартную директорию тестов проекта src/test/java, используя packageName (по умолчанию io/example).

- **Тип:** file_operation / **Операция:** write_file
- **Вход:** `{{test_class_code}} в src/test/java/{{packageName с заменой точек на слеши}}/{{className}}Test.java`
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
Создан файл src/test/java/io/example/LoginTest.java с методом testAuthorization(). Page Object LoginPage.java сохранён в .frap-work/pages/ и src/main/java/io/example/pages/.

### Пример 2

**Пользователь:**
Тест-кейс: поиск товара. Перейти на https://shop.com, ввести в поиск 'ноутбук', нажать Enter. Проверить, что в результатах есть хотя бы один товар.

**Ответ:**
Создан файл src/test/java/io/example/SearchTest.java с методом testProductSearch(). Page Object MainPage.java сохранён в .frap-work/pages/ и src/main/java/io/example/pages/.

## Ресурсы

### Ссылки

- [Документация Selenide](https://ru.selenide.org/documentation.html) — Официальное руководство по Selenide для Java.
- [Frap MCP Server](https://github.com/kotler-dev/frap) — Исходный код и документация MCP-сервера Frap.

## Формат ответа

Формат: `text`

```text
✅ Тестовый класс {{testClassName}} успешно создан: {{testFilePath}}. Page Object сохранён во временной папке .frap-work/pages/ и в проекте в {{projectPageObjectPaths}}.
```

## Ограничения

- Максимальная длина ввода: 10000 символов
- Таймаут выполнения: 300 секунд
- Разрешённые операции: check_and_update_dependency, check_availability, parse_test_description, open_and_navigate, frap_snapshot_script, wrap_iife, evaluate_script, write_file, frap_build_element_map, frap_generate_page_object, copy_file, template, close_page

