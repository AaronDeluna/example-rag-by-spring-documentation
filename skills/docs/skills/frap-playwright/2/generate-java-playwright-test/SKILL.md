---
name: generate-java-playwright-test
description: Преобразует описание UI тест-кейса на русском языке в автоматизированный Java-тест с Playwright. Проверяет наличие com.microsoft.playwright.playwright 1.57.0 и подключает при необходимости. Использует Frap для анализа DOM и генерации Page Object, playwright-mcp для управления браузером. Создаёт два файла: PageObject в src/main/java/io/example и тестовый класс в src/test/java/io/example. Временные артефакты сохраняются в .frap/ и .frap-work/ в корне проекта. Используйте, когда нужно создать UI-тест по шагам, описанным на естественном языке.
priority: 10
paths:
  - src/test/java/**/*.java
user-invocable: true
disable-model-invocation: false
license: MIT
compatibility: Требуется Java проект с Maven или Gradle, зависимость com.microsoft.playwright.playwright 1.57.0, доступ к MCP-серверам Frap и playwright-mcp. Создаёт временные файлы в директориях .frap/ и .frap-work/.
allowed-tools: Bash(cat:*, grep:*, find:*, echo:*, cp:*, mv:*, sed:*, awk:*) Write
metadata:
  author: "user"
  version: "1.0"
  category: "test-automation"
  tags: "java, playwright, ui-testing, frap, page-object"
---

# Преобразует описание UI тест-кейса на русском языке в авт...

## Шаги выполнения

### Шаг 1: Проверяет наличие com.microsoft.playwright.playwright версии 1.57.0 в файле сборки. Если отсутствует, добавляет её.

- **Тип:** text_processing / **Операция:** check_and_add_dependency
- **Вход:** `{{project_dir}}/pom.xml или build.gradle`
- **Выход:** `playwright_dependency_status`

### Шаг 2: Проверяет доступность MCP-серверов: frap_help (Frap) и playwright_navigate (playwright-mcp). При недоступности любого — ошибка и остановка.

- **Тип:** api_call / **Операция:** check_availability
- **Вход:** `вызовы frap_help и playwright_navigate`
- **Выход:** `servers_available`

### Шаг 3: Извлекает имя тестового класса, список URL страниц, пакет (io.example) и шаги из текстового описания на русском языке.

- **Тип:** text_processing / **Операция:** extract
- **Вход:** `описание тест-кейса от пользователя`
- **Выход:** `{className, pageUrls[], steps_text}`

### Шаг 4: Цикл: для каждой страницы из списка pageUrls выполняются шаги 5-12.

- **Тип:** loop / **Операция:** for_each
- **Вход:** `{{pageUrls}}`
- **Выход:** `current_url`

### Шаг 5: Открывает новую страницу в Playwright через playwright-mcp и переходит по URL.

- **Тип:** api_call / **Операция:** navigate
- **Вход:** `{{current_url}}`
- **Выход:** `page_ready`

### Шаг 6: Получает JavaScript-код для захвата DOM-снимка от Frap.

- **Тип:** api_call / **Операция:** frap_snapshot_script
- **Вход:** ``
- **Выход:** `snapshot_script`

### Шаг 7: Если скрипт содержит async/await, оборачивает его в синхронную самовызывающуюся функцию (IIFE) без async/await для совместимости с playwright-mcp.

- **Тип:** text_processing / **Операция:** wrap_iife
- **Вход:** `{{snapshot_script}}`
- **Выход:** `sync_script`

### Шаг 8: Выполняет синхронный скрипт в браузере через playwright-mcp и получает DOM-снимок страницы.

- **Тип:** api_call / **Операция:** evaluate_script
- **Вход:** `{{sync_script}}`
- **Выход:** `dom_snapshot`

### Шаг 9: Сохраняет DOM-снимок во временную папку .frap-work/snapshot/ для использования в режиме file.

- **Тип:** file_operation / **Операция:** write_file
- **Вход:** `{{dom_snapshot}} в файл .frap-work/snapshot/{{test-name}}-{{page_index}}.json`
- **Выход:** `snapshot_path`

### Шаг 10: Строит карту элементов (ElementMap) из DOM-снимка с помощью Frap.

- **Тип:** api_call / **Операция:** frap_build_element_map
- **Вход:** `{domSnapshotPath: {{snapshot_path}}}`
- **Выход:** `element_map_path`

### Шаг 11: Генерирует код Page Object на Java с аннотациями Playwright для пакета io.example. Возвращает список путей к файлам.

- **Тип:** api_call / **Операция:** frap_generate_page_object
- **Вход:** `{elementMapPath: {{element_map_path}}, language: 'java_playwright', className: '{{page_class_name}}', packageName: 'io.example'}`
- **Выход:** `page_object_files`

### Шаг 12: Копирует сгенерированные Page Object файлы во временную папку .frap-work/pages/ для отладки и последующего использования.

- **Тип:** file_operation / **Операция:** copy_file
- **Вход:** `{{page_object_files}} в директорию .frap-work/pages/`
- **Выход:** `temp_page_paths`

### Шаг 13: Копирует все накопленные Page Object файлы из временной папки в стандартную директорию проекта src/main/java/io/example/.

- **Тип:** file_operation / **Операция:** copy_file
- **Вход:** `.frap-work/pages/* -> src/main/java/io/example/`
- **Выход:** `project_page_object_paths`

### Шаг 14: Генерирует Java-код тестового класса с методами Playwright, используя сгенерированные Page Object и русские комментарии, пакет io.example. Применяет TestNG/JUnit аннотации.

- **Тип:** text_processing / **Операция:** template
- **Вход:** `{{temp_page_paths}} и {{steps_text}}`
- **Выход:** `test_class_code`

### Шаг 15: Сохраняет сгенерированный тестовый класс в src/test/java/io/example/.

- **Тип:** file_operation / **Операция:** write_file
- **Вход:** `{{test_class_code}} в src/test/java/io/example/{{className}}Test.java`
- **Выход:** `test_file_path`

### Шаг 16: Закрывает браузер через playwright-mcp, освобождая ресурсы.

- **Тип:** api_call / **Операция:** close_browser
- **Вход:** ``
- **Выход:** `browser_closed`

## Примеры

### Пример 1

**Пользователь:**
Тест-кейс: авторизация пользователя. Открыть https://example.com/login, ввести логин 'admin', пароль '123', нажать кнопку 'Войти'. Проверить, что отображается приветствие 'Добро пожаловать'.

**Ответ:**
Создан файл src/test/java/io/example/LoginTest.java с методом testAuthorization(). Page Object LoginPage.java сохранён в .frap-work/pages/ и src/main/java/io/example/.

### Пример 2

**Пользователь:**
Тест-кейс: поиск товара. Перейти на https://shop.com, ввести в поиск 'ноутбук', нажать Enter. Проверить, что в результатах есть хотя бы один товар.

**Ответ:**
Создан файл src/test/java/io/example/SearchTest.java с методом testProductSearch(). Page Object MainPage.java сохранён в .frap-work/pages/ и src/main/java/io/example/.

## Ресурсы

### Ссылки

- [Frap MCP Server Documentation](https://github.com/kotler-dev/frap/tree/develop/java-v1.1.1) — Документация по использованию Frap MCP-сервера для генерации Page Object.

## Формат ответа

Формат: `text`

```text
✅ Тестовый класс {{testClassName}} успешно создан: {{testFilePath}}. Page Object сохранён во временной папке .frap-work/pages/ и в проекте в {{projectPageObjectPaths}}.
```

## Ограничения

- Таймаут выполнения: 120 секунд

