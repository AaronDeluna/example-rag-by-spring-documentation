# Правила проекта example-rag-by-spring-documentation

## 📁 Структура рабочего пространства

```
.qwen/workplace/
├── PROJECT_RULES.md      # Этот файл — правила проекта
├── TASK_INDEX.md         # Индекс всех задач с таблицей статусов
├── task_template.md      # Шаблон для создания новых задач
├── to_work/              # Активные задачи (в работе)
└── archive/              # Выполненные задачи
```

**📋 Обзор проекта:** [README.md](../../README.md) — модули, архитектура, технологии

---

## 🔄 Рабочий процесс (Extreme Programming + TDD)

### Создание новой задачи

**Важно:** Файл задачи должен быть создан **до начала** любой работы над задачей. Это позволяет продолжить работу с места прерывания в случае сбоя или остановки.

1. Скопируйте `task_template.md` в папку `to_work/`
2. Назовите файл по формату: `TASK-<ID>_<модуль>_<краткое-имя>.md`
   - `<ID>` — трёхзначный номер задачи (001, 002, ...)
   - `<модуль>` — код модуля (см. ниже)
   - `<краткое-имя>` — описание на английском в snake_case
   - Примеры:
     - `TASK-001_jar_tests_for_server.md`
     - `TASK-018_webmvc_add_new_tool.md`
     - `TASK-019_common_update_docs.md`
3. Заполните описание и список действий
4. Добавьте запись в `TASK_INDEX.md`
5. **Перед началом работы** установите статус задачи в `🔧 In Progress` в `TASK_INDEX.md`

### Коды модулей

| Код       | Модуль                          | Путь                                                     |
|-----------|---------------------------------|----------------------------------------------------------|
| `common`  | Общепроектные задачи            | корень проекта                                           |
| `jar`     | mcp-server-jar-unpacker         | `mcp-server-jar-unpacker/`                               |
| `webmvc`  | webmvc-sync-mcp-server/client   | `webmvc-sync-mcp-server/`, `webmvc-sync-mcp-client/`     |
| `webflux` | webflux-async-mcp-server/client | `webflux-async-mcp-server/`, `webflux-async-mcp-client/` |
| `stdio`   | stdio-sync-mcp-server/client    | `stdio-sync-mcp-server/`, `stdio-sync-mcp-client/`       |
| `rag`     | example-rag                     | `example-rag/`                                           |

**Примеры использования:**
- `TASK-001_jar_...` — задача по модулю mcp-server-jar-unpacker
- `TASK-018_webmvc_...` — задача по модулю webmvc-sync-mcp-server/client
- `TASK-019_common_...` — общепроектная задача (документация, настройка)

### Работа над задачей — TDD цикл

Каждая задача выполняется по принципу **Red-Green-Refactor**:

```
┌─────────────┐
│     RED     │ ──→ Пишем тест, который падает
└──────┬──────┘
       │
       ▼
┌─────────────┐
│    GREEN    │ ──→ Пишем минимальный код для прохождения теста
└──────┬──────┘
       │
       ▼
┌─────────────┐
│  REFACTOR   │ ──→ Улучшаем код, сохраняя тесты зелёными
└──────┬──────┘
       │
       ▼
   Повторить
```

**Важно:** Отмечайте чек-боксы `[x]` **сразу после выполнения** каждого шага задачи. Это позволяет точно отслеживать прогресс и продолжить работу с места прерывания.

#### Шаг 1: RED — Красный тест

- [ ] Определите ожидаемое поведение в виде теста
- [ ] Напишите тест **до** реализации
- [ ] Убедитесь, что тест падает (ошибка или assertion failure)
- [ ] Зафиксируйте результат в файле задачи
- [ ] **Отметьте чек-бокс шага сразу после выполнения**

#### Шаг 2: GREEN — Зелёный тест

- [ ] Напишите **минимальный** код для прохождения теста
- [ ] Не заботьтесь о качестве кода на этом этапе
- [ ] Запустите тесты: `mvn test`
- [ ] Убедитесь, что все тесты проходят
- [ ] **Отметьте чек-бокс шага сразу после выполнения**

#### Шаг 3: REFACTOR — Рефакторинг

- [ ] Устраните дублирование кода (DRY)
- [ ] Улучшите читаемость (имена переменных, методы)
- [ ] Проверьте, что тесты всё ещё проходят
- [ ] Запустите полную сборку: `mvn clean package`
- [ ] **Отметьте чек-бокс шага сразу после выполнения**

### Работа с существующим кодом (Legacy Code)

При модификации существующего кода без тестов:

1. **Напишите characterization test** — тест, который фиксирует текущее поведение
2. **Убедитесь, что тест проходит** — это ваш "зелёный" старт
3. **Внесите изменения** — следуйте TDD циклу для новой функциональности
4. **Проверьте регрессию** — старые тесты должны остаться зелёными

```
Legacy Code → Characterization Test → Refactor (если нужно) → TDD для нового
```

### Завершение задачи

1. Убедитесь, что все чек-боксы в файле задачи отмечены `[x]`
   - **Проверьте, что каждый шаг был отмечен сразу после выполнения**
2. Все тесты проходят: `mvn test`
3. Сборка успешна: `mvn clean package`
4. Заполните дату завершения в файле задачи
5. Переместите файл в `archive/`
6. Обновите статус в `TASK_INDEX.md` на ✅

## 📊 Статусы задач

| Статус         | Описание                        | Папка      |
|----------------|---------------------------------|------------|
| 📋 Pending     | Задача запланирована            | `to_work/` |
| 🔧 In Progress | Задача в работе (TDD цикл)      | `to_work/` |
| 🧪 Testing     | Тесты написаны, код в работе    | `to_work/` |
| ✅ Done         | Задача выполнена, тесты зелёные | `archive/` |

## 📝 Таблица задач

Актуальная таблица всех задач находится в файле [`TASK_INDEX.md`](./TASK_INDEX.md)

## 🎯 Конвенции

### Именование файлов задач

**Формат:** `TASK-<ID>_<модуль>_<краткое_описание>.md`

| Часть                | Описание                           | Пример                             |
|----------------------|------------------------------------|------------------------------------|
| `<ID>`               | Трёхзначный номер задачи           | `001`, `018`                       |
| `<модуль>`           | Код модуля                         | `jar`, `webmvc`, `common`          |
| `<краткое_описание>` | Описание на английском, snake_case | `tests_for_server`, `add_new_tool` |

**Примеры:**
- `TASK-001_jar_tests_for_server.md`
- `TASK-018_webmvc_add_new_tool.md`
- `TASK-019_common_update_docs.md`

**Подробнее:** [Коды модулей](#коды-модулей)

### Именование тестов

- Класс: `<Класс>Test.java` или `Test<Класс>.java`
- Метод: `given<Условие>When<Действие>Then<ОжидаемыйРезультат>()`
- Стиль: CamelCase, без подчёркиваний, формат given-when-then
- Пример: `givenNullValueWhenToJsonThenReturnsNullString()`

### Структура теста (AAA Pattern)

```java
@Test
@DisplayName("Описание сценария")
void testMethod() {
    // Arrange — подготовка данных
    Map<String, Object> map = new HashMap<>();
    
    // Act — выполнение действия
    String result = jsonUtils.toJson(map);
    
    // Assert — проверка результата
    assertEquals("{}", result);
}
```

### Форматирование описания

- Используйте Markdown
- Код — в блоках с указанием языка
- Ссылки на файлы — относительные пути от корня проекта

### Ветвление Git (если применяется)

- Ветка под задачу: `task/XXX-краткое-имя`
- Пример: `task/001-add-new-feature`
- Коммиты: `"TASK-XXX: описание изменения (#N)"`

## 🧪 Практики Extreme Programming

| Практика                   | Описание                                               |
|----------------------------|--------------------------------------------------------|
| **TDD**                    | Тесты пишутся до кода. Красный → Зелёный → Рефакторинг |
| **Continuous Integration** | Частые коммиты, сборка после каждого изменения         |
| **Simple Design**          | Код должен быть простым, без излишней абстракции       |
| **Refactoring**            | Постоянное улучшение кода без изменения поведения      |
| **Collective Ownership**   | Любой может изменить любой код при необходимости       |

---

## 🚀 Команды

**Полная шпаргалка:** [CHEATSHEET.md](../../CHEATSHEET.md)

### Сборка и тестирование

```bash
# Сборка всего проекта
mvn clean package

# Тесты всего проекта
mvn test

# Сборка с пропуском тестов
mvn clean install -pl <module-name> -DskipTests
```

### Запуск приложений

```bash
# example-rag
mvn spring-boot:run -pl example-rag

# MCP-серверы с STDIO-профилем
mvn spring-boot:run -pl webmvc-sync-mcp-server -Dspring-boot.run.profiles=stdio
mvn spring-boot:run -pl webflux-async-mcp-server -Dspring-boot.run.profiles=stdio

# mcp-server-jar-unpacker
java -jar mcp-server-jar-unpacker/target/mcp-server-jar-unpacker-1.0-SNAPSHOT.jar
```

### Тесты для MCP-серверов

```bash
# stdio-sync-mcp-server
mvn test -pl stdio-sync-mcp-server -Dtest=ru.mirent.stdio.StdioClientTest

# webmvc-sync-mcp-server
mvn test -pl webmvc-sync-mcp-server -Dtest=ru.mirent.webmvc.WebMvcClientTest

# webflux-async-mcp-server
mvn test -pl webflux-async-mcp-server -Dtest=ru.mirent.webflux.WebFluxClientTest

# mcp-server-jar-unpacker (полное тестирование)
cd mcp-server-jar-unpacker && python3 test_all_tools.py
```
