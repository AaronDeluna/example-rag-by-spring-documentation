# Настройка Qwen Code для работы со всем проектом

> **📋 Обзор проекта:** См. [README.md](./README.md) — модули, архитектура, технологии.

---

## 🔧 Конфигурация Qwen Code

### 1. Правила проекта

**Применяются ко всем модулям:**

- **TDD (Red-Green-Refactor):** Тесты пишутся до кода
- **Extreme Programming:** Простой дизайн, частые коммиты, рефакторинг
- **Именование тестов:** `given<Условие>When<Действие>Then<ОжидаемыйРезультат>()`
- **AAA Pattern:** Arrange-Act-Assert в структуре тестов
- **Задачи:** Файл задачи создаётся ДО начала работы

**Подробнее:** [.qwen/workplace/PROJECT_RULES.md](./.qwen/workplace/PROJECT_RULES.md)

### 2. Рабочее пространство

Для работы с любым модулем проекта используйте единую структуру задач в `.qwen/workplace/`:

```bash
# Создание новой задачи для любого модуля
cp .qwen/workplace/task_template.md .qwen/workplace/to_work/TASK-XXX_описание.md
```

### 3. Контекст модулей

При работе с конкретным модулем применяйте соответствующую документацию:

| Модуль | Документация |
|--------|--------------|
| `mcp-server-jar-unpacker` | `mcp-server-jar-unpacker/QWEN.md` |
| Остальные модули | [README.md](./README.md) + код модуля |

---

## 📝 Рабочий процесс

### Создание задачи для любого модуля

1. **Создайте файл задачи** (ДО начала работы):
```bash
cd /home/vadim/IdeaProjects/java/my-projects/ai/example-rag-by-spring-documentation
cp .qwen/workplace/task_template.md .qwen/workplace/to_work/TASK-016_описание.md
```

2. **Заполните описание:**
   - Укажите модуль, с которым работаете
   - Опишите контекст и цель
   - Добавьте критерии приёмки

3. **Обновите TASK_INDEX.md:**
   - Добавьте новую строку в таблицу
   - Установите статус 📋 Pending

4. **Перед началом работы:**
   - Установите статус 🔧 In Progress

### TDD цикл

```
┌─────────────┐
│     RED     │ → Пишем тест в модуле
└──────┬──────┘
       ▼
┌─────────────┐
│    GREEN    │ → Пишем минимальный код
└──────┬──────┘
       ▼
┌─────────────┐
│  REFACTOR   │ → Улучшаем код
└─────────────┘
```

**Подробнее:** [.qwen/workplace/PROJECT_RULES.md](./.qwen/workplace/PROJECT_RULES.md)

### Запуск тестов

```bash
# Запуск тестов в конкретном модуле
mvn test -pl <module-name>

# Запуск тестов во всём проекте
mvn test
```

**Подробнее:** [CHEATSHEET.md](./CHEATSHEET.md)

---

## 🚀 Команды для работы с модулями

**Краткая шпаргалка:** [CHEATSHEET.md](./CHEATSHEET.md)

### example-rag

```bash
mvn spring-boot:run -pl example-rag
mvn clean package -pl example-rag
```

### STDIO MCP

```bash
mvn clean install -pl stdio-sync-mcp-server -DskipTests
mvn test -pl stdio-sync-mcp-server -Dtest=ru.mirent.stdio.StdioClientTest
mvn spring-boot:run -pl stdio-sync-mcp-client
```

### WebMVC MCP

```bash
mvn test -pl webmvc-sync-mcp-server -Dtest=ru.mirent.webmvc.WebMvcClientTest
mvn spring-boot:run -pl webmvc-sync-mcp-server -Dspring-boot.run.profiles=stdio
```

### WebFlux MCP

```bash
mvn test -pl webflux-async-mcp-server -Dtest=ru.mirent.webflux.WebFluxClientTest
mvn spring-boot:run -pl webflux-async-mcp-server -Dspring-boot.run.profiles=stdio
```

### mcp-server-jar-unpacker

```bash
mvn clean package -pl mcp-server-jar-unpacker
java -jar mcp-server-jar-unpacker/target/mcp-server-jar-unpacker-1.0-SNAPSHOT.jar
mvn test -pl mcp-server-jar-unpacker
cd mcp-server-jar-unpacker && python3 test_all_tools.py
```

---

## 📊 Зависимости между модулями

**Подробнее:** [README.md](./README.md#архитектура)

---

## 🎯 Конвенции

### Именование файлов задач

```
TASK-<ID>_<краткое-описание>.md
```

**Примеры:** `TASK-016_add_new_mcp_tool.md`, `TASK-017_fix_webflux_async_bug.md`

### Именование тестов

```java
@Test
@DisplayName("Описание сценария")
void given<Условие>When<Действие>Then<ОжидаемыйРезультат>() {
    // Arrange, Act, Assert
}
```

### Коммиты в Git

```bash
# Формат: "TASK-XXX: краткое описание (#N)"
git commit -m "TASK-016: добавить новый MCP-инструмент (#16)"
```

### Ветвление Git

```bash
git checkout -b task/016-add-new-mcp-tool
```

**Подробнее:** [.qwen/workplace/PROJECT_RULES.md](./.qwen/workplace/PROJECT_RULES.md)

---

## 🔍 Поиск и навигация

Используйте инструменты Qwen Code:
- `glob` — поиск файлов по паттерну
- `grep_search` — поиск по содержимому файлов
- `read_file` — чтение конкретного файла
- `agent` — делегирование сложных задач

---

## 📁 Управление задачами

```bash
# Просмотр активных задач
ls -la .qwen/workplace/to_work/

# Просмотр выполненных задач
ls -la .qwen/workplace/archive/

# Индекс задач (общий)
cat .qwen/workplace/TASK_INDEX.md

# Индекс задач модуля mcp-server-jar-unpacker
cat mcp-server-jar-unpacker/.qwen/workplace/TASK_INDEX.md
```

**Статусы задач:** 📋 Pending → 🔧 In Progress → 🧪 Testing → ✅ Done

**Подробнее:** [.qwen/workplace/PROJECT_RULES.md](./.qwen/workplace/PROJECT_RULES.md)

### Бэклог задач mcp-server-jar-unpacker

| № | Задача | Приоритет | Оценка | Файл |
|---|--------|-----------|--------|------|
| TASK-030 | Fuzzy-search в find_class_in_m2 | P1 | 3ч | TASK-030_mcp_fuzzy_search.md |
| TASK-031 | Maven-координаты при отсутствии класса | P1 | 2ч | TASK-031_mcp_maven_coordinates.md |
| TASK-032 | Параметр limit в list_classes_in_jar | P1 | 1ч | TASK-032_mcp_list_classes_limit.md |
| TASK-033 | Флаг --debug для логирования | P2 | 2ч | TASK-033_mcp_debug_flag.md |
| TASK-034 | Логирование cache hit/miss | P2 | 1ч | TASK-034_mcp_cache_logging.md |
| TASK-035 | Переменная окружения CFR_PATH | P2 | 1ч | TASK-035_mcp_cfr_path_env.md |
| TASK-036 | Переменная окружения CACHE_TTL_SECONDS | P2 | 1ч | TASK-036_mcp_cache_ttl_env.md |
| TASK-037 | Автозагрузка CFR | P3 | 4ч | TASK-037_mcp_cfr_autodownload.md |
| TASK-038 | Пагинация результатов | P3 | 2ч | TASK-038_mcp_pagination.md |
| TASK-039 | Команда invalidate_cache | P3 | 1ч | TASK-039_mcp_invalidate_cache.md |
| TASK-040 | Поддержка JMOD/JIMAGE | P3 | 8ч | TASK-040_mcp_jmod_support.md |

**Всего в бэклоге:** 11 задач, ~21 час

**Файлы задач:** `.qwen/workplace/to_work/`

---

## 🧪 Тестирование

```bash
# Запуск всех тестов проекта
mvn clean test

# Запуск тестов конкретного модуля
mvn clean test -pl mcp-server-jar-unpacker

# Запуск конкретного теста
mvn test -pl mcp-server-jar-unpacker -Dtest=JsonUtilsTest
```

**Подробнее:** [CHEATSHEET.md](./CHEATSHEET.md)

---

## 🛠️ Отладка и логирование

### Включение DEBUG-логов

Для модулей с Logbook:

```properties
# application.properties
logging.level.org.zalando.logbook=DEBUG
logging.level.ru.mirent=DEBUG
```

### Просмотр логов MCP-сервера

```bash
# Для mcp-server-jar-unpacker
tail -f mcp-server-jar-unpacker/jar-unpacker.log
```

---

## 📚 Документация по модулям

### mcp-server-jar-unpacker

**Полная документация:** [`mcp-server-jar-unpacker/QWEN.md`](./mcp-server-jar-unpacker/QWEN.md)

**Инструменты:**
- `find_class_in_m2` — поиск JAR по имени класса
- `get_class_outline` — схема класса
- `get_method_source` — код метода
- `decompile_class` — полная декомпиляция
- `list_classes_in_jar` — список классов в JAR
- `search_classes_by_pattern` — поиск по regex-паттерну

### Остальные модули

**Документация:** [README.md](./README.md)

---

## 🎓 Best Practices

1. **Всегда создавайте файл задачи ДО начала работы** — позволяет продолжить с места прерывания
2. **Следуйте TDD циклу** — RED → GREEN → REFACTOR
3. **Characterization тесты для legacy-кода** — фиксируйте текущее поведение перед изменениями
4. **Простой дизайн** — избегайте излишней абстракции, устраняйте дублирование
5. **Частые коммиты** — после каждого TDD-цикла

---

## 🔗 Полезные ссылки

- [Spring AI Documentation](https://docs.spring.io/spring-ai/docs/current/reference/html/)
- [MCP Specification](https://modelcontextprotocol.io/)
- [JUnit 5 User Guide](https://junit.org/junit5/docs/current/user-guide/)
- [Mockito Documentation](https://javadoc.io/doc/org.mockito/mockito-core/latest/)

---

## 📞 Поддержка

1. Проверьте [README.md](./README.md) — обзор проекта
2. Посмотрите [CHEATSHEET.md](./CHEATSHEET.md) — команды
3. Изучите задачи в `.qwen/workplace/archive/` — примеры решений

---

**Версия:** 1.0  
**Дата:** 2026-03-26  
**Основано на:** `mcp-server-jar-unpacker/QWEN.md`
