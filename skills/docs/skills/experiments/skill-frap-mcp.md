---
name: text-to-java-ui-test
description: Преобразует описание тест-кейса на естественном языке в автоматизированный Java UI-тест с использованием MCP-Frap и chrome-devtools-mcp. Генерирует файл теста в папке тестов проекта. Используйте, когда необходимо создать исполняемый Java-тест из сценария тест-кейса, используя взаимодействие с браузером через Chrome DevTools Protocol.
priority: 10
user-invocable: true
license: MIT
compatibility: Требуется запущенный MCP-Frap сервер (stdio/http), chrome-devtools-mcp, Java 11+, Maven/Gradle и доступ в интернет для веб-тестирования. Скилл автоматически проверяет наличие зависимости Selenium в проекте (через pom.xml или build.gradle) и доступность обоих MCP-серверов перед генерацией теста.
allowed-tools: Bash(java:*) Bash(mvn:*) Bash(gradle:*) Bash(grep:*) Read Write
metadata:
  author: "user"
  version: "1.0"
  category: "тестовая автоматизация"
  tags: "java, selenium, mcp-frap, chrome-devtools-mcp, генерация-тестов"
---

# Преобразует описание тест-кейса на естественном языке в а...

## Шаги выполнения

### Шаг 1: Проверяет, что в проекте подключена зависимость Selenium WebDriver (анализирует pom.xml или build.gradle). Если зависимость отсутствует, генерирует сообщение об ошибке и останавливает выполнение.

- **Тип:** conditional / **Операция:** check_dependency
- **Вход:** `project_build_file`
- **Выход:** `dependency_ok`

### Шаг 2: Проверяет доступность MCP-серверов chrome-devtools-mcp и Frap, выполняя простые вызовы инструментов (например, frap_help и list_pages). Если хотя бы один сервер не отвечает, прерывает выполнение с сообщением об ошибке.

- **Тип:** api_call / **Операция:** verify_mcp_connections
- **Вход:** ``
- **Выход:** `mcp_ok`

### Шаг 3: Анализирует описание тест-кейса на естественном языке, извлекая шаги, ожидаемые результаты и задействованные элементы страницы. Также извлекает предлагаемое имя класса теста (например, из названия тест-кейса).

- **Тип:** text_processing / **Операция:** extract
- **Вход:** `{{user_input}}`
- **Выход:** `parsed_test_case`

### Шаг 4: Получает от MCP-Frap JavaScript-код для захвата DOM-снимка. ВАЖНО: код возвращается в асинхронном виде (с async/await), поэтому его необходимо обернуть в синхронную самовызывающуюся функцию (IIFE) без async/await, чтобы chrome-devtools-mcp смог выполнить его через evaluate_script.

- **Тип:** api_call / **Операция:** frap_snapshot_script
- **Вход:** ``
- **Выход:** `frap_script`

### Шаг 5: Преобразует асинхронный скрипт в синхронный вид, удаляя async/await и оборачивая в IIFE, возвращающую готовый объект DOM-снимка. Это необходимо, потому что chrome-devtools-mcp поддерживает только синхронное выполнение.

- **Тип:** text_processing / **Операция:** transform
- **Вход:** `{{frap_script}}`
- **Выход:** `sync_snapshot_script`

### Шаг 6: Выполняет подготовленный синхронный скрипт через chrome-devtools-mcp (инструмент evaluate_script) для захвата DOM-снимка текущей страницы.

- **Тип:** api_call / **Операция:** evaluate_script
- **Вход:** `{{sync_snapshot_script}}`
- **Выход:** `dom_snapshot`

### Шаг 7: Строит карту элементов (element map) из DOM-снимка с помощью Frap.

- **Тип:** api_call / **Операция:** frap_build_element_map
- **Вход:** `{{dom_snapshot}}`
- **Выход:** `element_map`

### Шаг 8: Генерирует Java-класс Page Object, используя генератор Frap (нацеленный на Selenium WebDriver).

- **Тип:** api_call / **Операция:** frap_generate_page_object
- **Вход:** `element_map={{element_map}} language=java_selenium class_name=GeneratedPage package_name=com.example.tests`
- **Выход:** `page_object_code`

### Шаг 9: Объединяет проанализированный тест-кейс с сгенерированным Page Object для получения полного Java-тестового метода на JUnit.

- **Тип:** text_processing / **Операция:** template
- **Вход:** `test_case={{parsed_test_case}} page_object={{page_object_code}}`
- **Выход:** `final_java_test`

### Шаг 10: Записывает сгенерированный код теста в файл внутри стандартной директории тестов проекта. Использует извлечённое имя класса для формирования имени файла.

- **Тип:** text_processing / **Операция:** write
- **Вход:** `file_path=src/test/java/com/example/tests/{{test_class_name}}.java content={{final_java_test}}`
- **Выход:** `file_written`

### Шаг 11: Закрывает текущую страницу или браузер через chrome-devtools-mcp (инструмент close_page), чтобы освободить ресурсы после завершения генерации теста.

- **Тип:** api_call / **Операция:** close_page
- **Вход:** ``
- **Выход:** ``

## Примеры

### Пример 1

**Пользователь:**
Тест-кейс: Вход с валидными учетными данными. Шаги: 1. Перейти на страницу входа. 2. Ввести имя пользователя 'admin'. 3. Ввести пароль 'secret'. 4. Нажать кнопку входа. Ожидаемый результат: Отображается страница дашборда.

**Ответ:**
Файл src/test/java/com/example/tests/LoginTest.java с готовым Selenium-тестом.

## Формат ответа

Формат: `text`

```text
// Сгенерированный Java UI тест (сохранён в {{file_path}})
{{final_java_test}}
```

## Ограничения

- Максимальная длина ввода: 5000 символов
- Таймаут выполнения: 300 секунд