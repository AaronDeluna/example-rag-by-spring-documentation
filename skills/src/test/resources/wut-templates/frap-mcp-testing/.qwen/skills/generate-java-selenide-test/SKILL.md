---
name: generate-java-selenide-test
description: Преобразует описание UI тест-кейса на русском языке в автоматизированный Java-тест с Selenide. Использует Frap для анализа DOM и генерации Page Object, chrome-devtools-mcp для управления браузером. Создаёт два файла: PageObject и тестовый класс. Временные артефакты сохраняются в .frap/ и .frap-work/ в корне проекта. Используйте, когда нужно создать UI-тест по шагам, описанным на естественном языке.
priority: 10
paths:
  - src/test/java/**/*.java
user-invocable: true
disable-model-invocation: false
license: MIT
compatibility: Требуется Java проект с Maven или Gradle, зависимость Selenide, доступ к MCP-серверам Frap и chrome-devtools-mcp. Создаёт временные файлы в директориях .frap/ и .frap-work/.
allowed-tools: Bash(cat:*, grep:*, find:*, echo:*, cp:*, mv:*) Write
metadata:
  author: "user"
  version: "1.1"
  category: "test-automation"
  tags: "java, selenide, ui-testing, frap, chrome-devtools, page-object"
---

# Преобразует описание UI тест-кейса на русском языке в авт...

## Шаги выполнения

### Шаг 1: Проверяет наличие зависимости Selenide в файле сборки проекта. Если отсутствует — ошибка и остановка.

- **Тип:** text_processing / **Операция:** check_dependency
- **Вход:** `{{project_dir}}/pom.xml или build.gradle`
- **Выход:** `selenide_exists`

### Шаг 2: Проверяет доступность MCP-серверов: вызывает frap_help (Frap) и list_pages (chrome-devtools-mcp). При недоступности любого — ошибка и остановка.

- **Тип:** api_call / **Операция:** check_availability
- **Вход:** `вызовы frap_help и list_pages`
- **Выход:** `servers_available`

### Шаг 3: Извлекает имя тестового класса (например, 'LoginTest'), URL страницы, имя пакета и шаги из описания на русском языке.

- **Тип:** text_processing / **Операция:** extract
- **Вход:** `описание тест-кейса от пользователя`
- **Выход:** `{className, pageUrl, packageName, steps_text}`

### Шаг 4: Открывает новую страницу в браузере через chrome-devtools-mcp и переходит по целевому URL.

- **Тип:** api_call / **Операция:** open_and_navigate
- **Вход:** `{{pageUrl}}`
- **Выход:** `page_ready`

### Шаг 5: Получает JavaScript-код для захвата DOM-снимка от Frap (инструмент frap_snapshot_script).

- **Тип:** api_call / **Операция:** frap_snapshot_script
- **Вход:** ``
- **Выход:** `snapshot_script`

### Шаг 6: Если скрипт содержит async/await, оборачивает его в синхронную самовызывающуюся функцию (IIFE) без async/await, чтобы обеспечить синхронное выполнение в chrome-devtools-mcp.

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

### Шаг 10: Генерирует код Page Object на Java с аннотациями Selenide (language=java_selenide). Возвращает список путей к файлам (режим file).

- **Тип:** api_call / **Операция:** frap_generate_page_object
- **Вход:** `{elementMapPath: {{element_map_path}}, language: 'java_selenide', className: {{pageClassName}}, packageName: {{packageName}}}`
- **Выход:** `page_object_files`

### Шаг 11: Копирует сгенерированные Page Object файлы во временную папку .frap-work/pages/ для отладки и последующего использования.

- **Тип:** file_operation / **Операция:** copy_file
- **Вход:** `{{page_object_files}} -> .frap-work/pages/`
- **Выход:** `temp_page_object_paths`

### Шаг 12: Копирует Page Object файлы в стандартную директорию pages основного проекта (src/main/java/...).

- **Тип:** file_operation / **Операция:** copy_file
- **Вход:** `{{temp_page_object_paths}} -> src/main/java/.../pages/`
- **Выход:** `project_page_object_paths`

### Шаг 13: Генерирует код тестового класса на основе временных Page Object и описания шагов. Использует русские комментарии, аннотации TestNG/JUnit и методы Selenide.

- **Тип:** text_processing / **Операция:** template
- **Вход:** `{{temp_page_object_paths}} и {{steps_text}}`
- **Выход:** `test_class_code`

### Шаг 14: Сохраняет сгенерированный тестовый класс в стандартную директорию тестов проекта.

- **Тип:** file_operation / **Операция:** write_file
- **Вход:** `{{test_class_code}} в src/test/java/.../{{className}}Test.java`
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
Создан файл src/test/java/com/example/tests/LoginTest.java с методом testAuthorization(). Page Object LoginPage.java сохранён в .frap-work/pages/ и src/main/java/com/example/pages/.

### Пример 2

**Пользователь:**
Тест-кейс: поиск товара. Перейти на https://shop.com, ввести в поиск 'ноутбук', нажать Enter. Проверить, что в результатах есть хотя бы один товар.

**Ответ:**
Создан файл src/test/java/com/example/tests/SearchTest.java с методом testProductSearch(). Page Object MainPage.java сохранён в .frap-work/pages/ и src/main/java/com/example/pages/.

## Формат ответа

Формат: `text`

```text
✅ Тестовый класс {{testClassName}} успешно создан: {{testFilePath}}. Page Object сохранён во временной папке .frap-work/pages/ и в проекте в {{projectPageObjectPaths}}.
```

## Ограничения

- Максимальная длина ввода: 5000 символов
- Таймаут выполнения: 180 секунд
- Разрешённые операции: frap_snapshot_script, frap_build_element_map, frap_generate_page_object, frap_help, evaluate_script, list_pages, close_page, new_page, navigate_page, read_file, write_file, move_file, copy_file, grep

