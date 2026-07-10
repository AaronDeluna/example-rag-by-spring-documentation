На основе документации, представленной ниже, создать скилл, который будет принимать на вход текстовое описание тест-кейса, а в ответ предоставлять тест на Java и библиотеки Selenide.
Обязательно к выполнению:
- Проверить что в проекте подключен selenide версии 7.16.2, а если не подключен, то подключить.
- Тест должен быть создан в папке /test в package `io.example`
- PageObject самой страницы должен быть сохранен в папке /main в package `io.example`

# Дополнительные рекомендации

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


Документация по MCP-Frap
На основе анализа исходного кода MCP-сервера и документации из репозитория [kotler-dev/frap](https://github.com/kotler-dev/frap/tree/develop/java-v1.1.1) я подготовил подробную инструкцию по подключению и использованию сервера. В ней описаны оба режима работы (file и inline), а также приведены конкретные примеры для получения DOM-снимка через **Playwright** и **chrome-devtools-mcp**.

---

## 1. Общие принципы работы MCP-сервера frap

Сервер предоставляет **6 MCP-инструментов** для автоматизации работы с веб-страницами:

| Инструмент | Назначение |
|------------|------------|
| `frap_help` | Возвращает пошаговое руководство по использованию |
| `frap_snapshot_script` | Возвращает JavaScript-код для захвата DOM-снимка страницы |
| `frap_build_element_map` | Строит карту элементов (ElementMap) из снимка DOM |
| `frap_filter_element_map` | Фильтрует карту элементов по заданным критериям |
| `frap_generate_page_object` | Генерирует код Page Object на основе карты элементов |
| `frap_heal` | Восстанавливает селектор, который перестал работать после изменения страницы |

Сервер может работать в **двух режимах** ввода-вывода:

- **`file`** (режим по умолчанию для `frap-mcp-stdio` и `frap-mcp-http-local`) — большие артефакты (DOM-снимок, карта элементов, сгенерированный код) передаются через **абсолютные пути к файлам** на общей файловой системе. Это экономит токены контекста агента.
- **`inline`** (режим по умолчанию для `frap-mcp-http`) — все данные передаются **внутри JSON-запросов/ответов** (подходит для удалённых клиентов без общего доступа к файловой системе).

**Важно:**  
`frap_snapshot_script` возвращает **JavaScript-код**, который **вы должны выполнить на клиентской стороне** (в браузере) с помощью вашего инструмента автоматизации (Playwright, chrome-devtools-mcp и т.д.). Сервер не имеет собственного браузера и не может выполнить этот код за вас.

---

## 2. Подключение MCP-сервера

Вы можете запустить сервер в одном из трёх вариантов:

### 2.1. `frap-mcp-stdio` (режим `file`, рекомендуется для локального использования)

Сервер запускается как дочерний процесс и общается через `stdin`/`stdout`.

**Сборка:**
```bash
mvn -f sdk/java/frap-mcp/pom.xml -pl frap-mcp-stdio -am package -DskipTests
```

**Запуск через Claude Code (CLI):**
```bash
claude mcp add frap-stdio --transport stdio -- \
  java -jar /ABS/PATH/sdk/java/frap-mcp/frap-mcp-stdio/target/frap-mcp-stdio.jar
```

**Конфигурация в `.mcp.json`:**
```json
{
  "mcpServers": {
    "frap-stdio": {
      "command": "java",
      "args": [
        "-jar",
        "/ABS/PATH/sdk/java/frap-mcp/frap-mcp-stdio/target/frap-mcp-stdio.jar"
      ]
    }
  }
}
```

**Дополнительные параметры (опционально):**
- `-Dfrap.runtime.dir=/path` — базовая директория для бинарных файлов, рабочих артефактов и логов (по умолчанию `<директория jar>/.frap`).
- `--frap.io.work-dir=/path` — директория для артефактов (по умолчанию `<frap.runtime.dir>/work`).

### 2.2. `frap-mcp-http` (режим `inline`, подходит для удалённых клиентов)

Обычное веб-приложение, работающее по протоколу HTTP.

**Сборка:**
```bash
mvn -f sdk/java/frap-mcp/pom.xml -pl frap-mcp-http -am package -DskipTests
```

**Запуск:**
```bash
java -jar /ABS/PATH/sdk/java/frap-mcp/frap-mcp-http/target/frap-mcp-http.jar
```
Сервер будет доступен по адресу `http://localhost:8080/mcp`.

**Подключение в Claude Code:**
```bash
claude mcp add frap-http --transport http http://localhost:8080/mcp
```

### 2.3. `frap-mcp-http-local` (режим `file`, HTTP + общая файловая система)

Аналог `frap-mcp-http`, но работает в режиме `file`. Подходит для случаев, когда клиент и сервер находятся на одной машине и имеют общую файловую систему.

**Сборка:**
```bash
mvn -f sdk/java/frap-mcp/pom.xml -pl frap-mcp-http-local -am package -DskipTests
```

**Запуск:**
```bash
java -jar /ABS/PATH/sdk/java/frap-mcp/frap-mcp-http-local/target/frap-mcp-http-local.jar
```
По умолчанию сервер слушает порт `8765`. Эндпоинт — `http://localhost:8765/mcp`.

---

## 3. Использование с Playwright

### 3.1. Получение DOM-снимка (шаг 1)

1. **Вызовите инструмент `frap_snapshot_script`** (без аргументов). Он вернёт строку с JavaScript-кодом.
2. **Выполните этот код на странице** с помощью Playwright.

**Пример на JavaScript (Playwright):**
```javascript
const { chromium } = require('playwright');

(async () => {
  const browser = await chromium.launch();
  const page = await browser.newPage();
  await page.goto('https://example.com');

  // 1. Получаем скрипт от MCP-сервера (этот шаг выполняется через MCP-клиент)
  // Допустим, мы уже вызвали frap_snapshot_script и получили строку script
  const script = await mcpClient.callTool('frap_snapshot_script', {});

  // 2. Выполняем скрипт на странице
  const snapshot = await page.evaluate(script);

  // 3. Сохраняем снимок в файл (для режима file) или передаём как объект (для режима inline)
  const fs = require('fs');
  const snapshotPath = './snapshot.json';
  fs.writeFileSync(snapshotPath, JSON.stringify(snapshot, null, 2));

  await browser.close();
})();
```

**Примечание:**  
В режиме `file` скрипт может быть модифицирован сервером для автоматической отправки снимка на эндпоинт `/frap/ingest` (если он доступен). В этом случае он вернёт не сам снимок, а `{ snapshot_path: "...путь..." }`. Однако приведённый выше подход с ручным сохранением работает всегда.

### 3.2. Построение карты элементов (шаг 2)

**В режиме `file`:** передайте путь к файлу со снимком.
```json
{
  "domSnapshotPath": "/abs/path/to/snapshot.json"
}
```

**В режиме `inline`:** передайте объект снимка напрямую.
```json
{
  "domSnapshot": { "html": "...", "elements": [...] }
}
```

**Пример вызова через MCP-клиент (режим `file`):**
```javascript
const result = await mcpClient.callTool('frap_build_element_map', {
  domSnapshotPath: '/abs/path/to/snapshot.json'
});
// result содержит { element_map_path: "...", summary: {...} }
```

### 3.3. Генерация Page Object (шаг 3)

**В режиме `file`:**
```javascript
const genResult = await mcpClient.callTool('frap_generate_page_object', {
  elementMapPath: '/abs/path/to/element-map.json',
  language: 'java_playwright',
  className: 'MainPage',
  packageName: 'com.example.pages'
});
// genResult содержит { file_paths: [...], file_count: N, work_dir: "..." }
```

**В режиме `inline`:**
```javascript
const genResult = await mcpClient.callTool('frap_generate_page_object', {
  elementMap: { ... }, // объект ElementMap
  language: 'java_playwright',
  className: 'MainPage',
  packageName: 'com.example.pages'
});
// genResult содержит { files: [ { path, content }, ... ] }
```

### 3.4. Фильтрация карты элементов (опционально)

**В режиме `file`:**
```javascript
const filterResult = await mcpClient.callTool('frap_filter_element_map', {
  elementMapPath: '/abs/path/to/element-map.json',
  filter: {
    interactive_only: true,
    min_cluster_size: 2,
    tags: ['button', 'a']
  }
});
// Возвращает { element_map_path: "...", summary: {...} }
```

### 3.5. Восстановление селектора (инструмент `frap_heal`)

**В режиме `file`:**
```javascript
const healResult = await mcpClient.callTool('frap_heal', {
  domSnapshotPath: '/abs/path/to/fresh-snapshot.json',
  primarySelector: '#old-selector',
  originalSignature: { ... }, // опционально
  minConfidence: 0.85
});
// Возвращает { healed: true/false, selector: "...", confidence: 0.91, ... }
```

---

Инструкция дополнена разделом, который чётко объясняет, что при использовании **chrome-devtools-mcp** скрипт, возвращаемый `frap_snapshot_script`, должен выполняться **синхронно** — без `async/await` внутри передаваемого кода. Это гарантирует, что инструмент `evaluate_script` немедленно получит результат, а не Promise, который может не быть корректно обработан некоторыми MCP-клиентами.

---

## 4. Использование с chrome-devtools-mcp (дополнено)

`chrome-devtools-mcp` предоставляет инструмент `evaluate_script` для выполнения JavaScript на странице через Chrome DevTools Protocol.

### Важное требование: синхронное выполнение
- Скрипт, возвращаемый `frap_snapshot_script`, представляет собой **синхронную самовызывающуюся функцию (IIFE)**, которая сразу возвращает объект `{ html, elements }`.
- При передаче этого скрипта в `evaluate_script` **не оборачивайте его в `async`** и не используйте внутри `await` (если только сервер явно не модифицировал скрипт для асинхронной отправки на эндпоинт `/frap/ingest`, но это отдельный случай).
- Инструмент `evaluate_script` ожидает синхронный код, возвращающий значение. Если вы передадите асинхронную функцию, она вернёт `Promise`, и некоторые реализации MCP могут не дождаться его разрешения, что приведёт к ошибке или пустому результату.

**Рекомендация:** всегда передавайте скрипт как есть (строку, полученную от `frap_snapshot_script`), без дополнительных обёрток.

### 4.1. Получение DOM-снимка (синхронный вызов)

```javascript
// 1. Получаем скрипт от frap (синхронный IIFE)
const scriptResult = await frapClient.callTool('frap_snapshot_script', {});
const script = scriptResult; // строка вида "(() => { ... })()"

// 2. Выполняем скрипт синхронно через chrome-devtools-mcp
//    ВАЖНО: не используйте async/await внутри script, он уже самовызывающийся
const evalResult = await chromeDevtoolsClient.callTool('evaluate_script', {
  function: script   // передаём как строку
});
// evalResult — это объект { html: "...", elements: [...] }

// 3. Сохраняем результат в файл (для режима file)
const fs = require('fs');
const snapshotPath = './snapshot.json';
fs.writeFileSync(snapshotPath, JSON.stringify(evalResult, null, 2));
```

**Почему синхронный?**  
Скрипт `(() => { ... })()` выполняется сразу и возвращает объект. Он не содержит асинхронных операций (fetch, setTimeout) в базовой версии. Если вы используете режим `file` с эндпоинтом `/frap/ingest`, сервер может подменить скрипт на асинхронный (с `fetch`), но в этом случае он вернёт `Promise`, который `evaluate_script` должен корректно обработать. Однако для единообразия и надёжности рекомендуется использовать стандартный синхронный скрипт, а сохранение снимка выполнять на стороне клиента (как показано выше).

---

Остальные шаги (построение карты, генерация Page Object) выполняются так же, как описано в разделе 3 для Playwright. Разница только в способе получения снимка.

---

## Полный пример конвейера с chrome-devtools-mcp (режим `file`)

```javascript
// 1. Получаем скрипт
const script = await frapClient.callTool('frap_snapshot_script', {});

// 2. Синхронно выполняем в браузере через chrome-devtools-mcp
const snapshot = await chromeDevtoolsClient.callTool('evaluate_script', {
  function: script
});

// 3. Сохраняем снимок в файл (вручную)
const fs = require('fs');
fs.writeFileSync('./snapshot.json', JSON.stringify(snapshot));

// 4. Строим карту элементов (file mode)
const buildResult = await frapClient.callTool('frap_build_element_map', {
  domSnapshotPath: '/abs/path/to/snapshot.json'
});
const mapPath = buildResult.element_map_path;

// 5. Генерируем Page Object
const genResult = await frapClient.callTool('frap_generate_page_object', {
  elementMapPath: mapPath,
  language: 'java_playwright',
  className: 'MainPage',
  packageName: 'com.example.pages'
});
console.log('Сгенерированные файлы:', genResult.file_paths);
```

---

## 5. Полный пример конвейера (режим `file`)

1. **Запустите MCP-сервер** (например, `frap-mcp-stdio`).
2. **Вызовите `frap_snapshot_script`** → получите JS-код.
3. **Выполните JS-код на странице** (через Playwright или `chrome-devtools-mcp`) и сохраните результат в JSON-файл (например, `snapshot.json`).
4. **Вызовите `frap_build_element_map`** с путём `domSnapshotPath: "/abs/path/to/snapshot.json"` → получите путь к файлу карты элементов.
5. **(Опционально)** Вызовите `frap_filter_element_map` с путём к карте элементов и фильтром.
6. **Вызовите `frap_generate_page_object`** с путём к карте элементов, языком, именем класса и пакетом → получите список путей к сгенерированным файлам.

**Итоговый код на JavaScript (с Playwright и MCP-клиентом):**
```javascript
// Предполагается, что у вас есть экземпляры MCP-клиентов для frap и chrome-devtools-mcp

// 1. Получаем скрипт
const script = await frapClient.callTool('frap_snapshot_script', {});

// 2. Выполняем на странице через Playwright
const snapshot = await page.evaluate(script);
fs.writeFileSync('./snapshot.json', JSON.stringify(snapshot));

// 3. Строим карту элементов
const buildResult = await frapClient.callTool('frap_build_element_map', {
  domSnapshotPath: '/abs/path/to/snapshot.json'
});
const mapPath = buildResult.element_map_path;

// 4. Генерируем Page Object
const genResult = await frapClient.callTool('frap_generate_page_object', {
  elementMapPath: mapPath,
  language: 'java_playwright',
  className: 'MainPage',
  packageName: 'com.example.pages'
});
console.log('Сгенерированные файлы:', genResult.file_paths);
```

---

## 6. Важные замечания

- **Режим `file` требует общей файловой системы** между клиентом и сервером. Убедитесь, что пути, которые вы передаёте, доступны серверу для чтения/записи.
- **Режим `inline`** подходит для удалённых клиентов, но может привести к большому объёму передаваемых данных (особенно для больших страниц).
- **`frap_snapshot_script`** всегда возвращает JavaScript, который выполняется **в контексте страницы**. Он не имеет доступа к файловой системе и не может самостоятельно сохранять файлы (если только сервер не предоставляет эндпоинт для приёма снимков).
- **Безопасность:** эндпоинт `/frap/ingest` (если используется) предназначен только для локального доступа и не должен быть открыт для внешних сетей.

---

## 7. Дополнительные ресурсы

- Исходный код: [github.com/kotler-dev/frap](https://github.com/kotler-dev/frap/tree/develop/java-v1.1.1)
- Документация по инструментам доступна через вызов `frap_help` (он всегда возвращает актуальное руководство для вашего режима).

Если у вас возникнут вопросы, используйте `frap_help` — он выдаст подробную инструкцию с учётом текущего режима работы сервера.