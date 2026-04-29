# TASK-004: Разделение Server.java на модули: Tool-классы

> **⚠️ Важно:** Этот файл должен быть создан **до начала** любой работы над задачей.
> Отмечайте чек-боксы `[x]` **сразу после выполнения** каждого шага для возможности продолжения с места прерывания.

## Описание

Выделить описание и вызов инструментов MCP в отдельные классы, реализующие интерфейс `Tool` из TASK-003.

**Текущая проблема:** Метод `handleListTools()` содержит хардкод описания 4 инструментов, а `handleCallTool()` — switch на 4 случая.

**Цель:** Каждый инструмент — отдельный класс с методами `getDescription()`, `getInputSchema()`, `execute()`.

## Требуемые изменения

### 1. Создать интерфейс `Tool.java`

**Путь:** `src/main/java/ru/mirent/tools/Tool.java`

```java
package ru.mirent.tools;

import java.util.Map;

/**
 * Интерфейс инструмента MCP
 */
public interface Tool {
    
    /**
     * Вернуть имя инструмента
     */
    String getName();
    
    /**
     * Вернуть описание инструмента
     */
    String getDescription();
    
    /**
     * Вернуть схему входных параметров (JSON Schema)
     */
    Map<String, Object> getInputSchema();
    
    /**
     * Выполнить инструмент
     * @param arguments аргументы вызова
     * @return результат выполнения
     */
    Object execute(Map<String, Object> arguments);
}
```

### 2. Создать абстрактный класс `AbstractTool.java`

**Путь:** `src/main/java/ru/mirent/tools/AbstractTool.java`

```java
package ru.mirent.tools;

import java.util.HashMap;
import java.util.Map;

/**
 * Базовый класс для инструментов с общей логикой создания схемы
 */
public abstract class AbstractTool implements Tool {
    
    @Override
    public Map<String, Object> getInputSchema() {
        Map<String, Object> schema = new HashMap<>();
        schema.put("type", "object");
        schema.put("properties", createProperties());
        schema.put("required", getRequiredParameters());
        return schema;
    }
    
    /**
     * Создать карту свойств схемы
     */
    protected abstract Map<String, Object> createProperties();
    
    /**
     * Вернуть список обязательных параметров
     */
    protected abstract java.util.List<String> getRequiredParameters();
    
    /**
     * Создать описание строкового параметра
     */
    protected Map<String, Object> createStringProperty(String description) {
        Map<String, Object> prop = new HashMap<>();
        prop.put("type", "string");
        prop.put("description", description);
        return prop;
    }
}
```

### 3. Создать класс `FindClassTool.java`

**Путь:** `src/main/java/ru/mirent/tools/FindClassTool.java`

```java
package ru.mirent.tools;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Инструмент: find_class_in_m2
 * Поиск Java-класса внутри JAR-файлов в ~/.m2/repository
 */
public class FindClassTool extends AbstractTool {
    
    private final JarSearchService jarSearchService;
    
    public FindClassTool(JarSearchService jarSearchService) {
        this.jarSearchService = jarSearchService;
    }
    
    @Override
    public String getName() {
        return "find_class_in_m2";
    }
    
    @Override
    public String getDescription() {
        return "Поиск Java-класса внутри JAR-файлов в ~/.m2/repository. " +
               "Принимает простое имя класса (например, 'КафкаТемплате') или " +
               "полное имя (например, 'org.springframework.kafka.core.KafkaTemplate'). " +
               "Возвращает список путей к найденным JAR-файлам.";
    }
    
    @Override
    protected Map<String, Object> createProperties() {
        Map<String, Object> props = new HashMap<>();
        props.put("class_name", createStringProperty(
            "Простое или полное имя Java-класса"
        ));
        return props;
    }
    
    @Override
    protected List<String> getRequiredParameters() {
        return Arrays.asList("class_name");
    }
    
    @Override
    public Object execute(Map<String, Object> arguments) {
        String className = (String) arguments.get("class_name");
        return jarSearchService.findClass(className);
    }
}
```

### 4. Создать класс `GetClassOutlineTool.java`

**Путь:** `src/main/java/ru/mirent/tools/GetClassOutlineTool.java`

```java
package ru.mirent.tools;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Инструмент: get_class_outline
 * Получить краткую схему Java-класса из JAR
 */
public class GetClassOutlineTool extends AbstractTool {
    
    private final DecompilationService decompilationService;
    
    public GetClassOutlineTool(DecompilationService decompilationService) {
        this.decompilationService = decompilationService;
    }
    
    @Override
    public String getName() {
        return "get_class_outline";
    }
    
    @Override
    public String getDescription() {
        return "Получить краткую схему Java-класса из JAR: пакет, импорты, " +
               "объявление класса, поля и сигнатуры методов — БЕЗ тел методов. " +
               "Требует примерно в 10 раз меньше токенов, чем полный исходник.";
    }
    
    @Override
    protected Map<String, Object> createProperties() {
        Map<String, Object> props = new HashMap<>();
        props.put("jar_path", createStringProperty("Абсолютный путь к JAR-файлу"));
        props.put("class_fqn", createStringProperty("Полное имя класса"));
        return props;
    }
    
    @Override
    protected List<String> getRequiredParameters() {
        return Arrays.asList("jar_path", "class_fqn");
    }
    
    @Override
    public Object execute(Map<String, Object> arguments) {
        String jarPath = (String) arguments.get("jar_path");
        String classFqn = (String) arguments.get("class_fqn");
        return decompilationService.getClassOutline(jarPath, classFqn);
    }
}
```

### 5. Создать класс `GetMethodSourceTool.java`

**Путь:** `src/main/java/ru/mirent/tools/GetMethodSourceTool.java`

```java
package ru.mirent.tools;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Инструмент: get_method_source
 * Извлечь исходный код конкретного метода
 */
public class GetMethodSourceTool extends AbstractTool {
    
    private final DecompilationService decompilationService;
    
    public GetMethodSourceTool(DecompilationService decompilationService) {
        this.decompilationService = decompilationService;
    }
    
    @Override
    public String getName() {
        return "get_method_source";
    }
    
    @Override
    public String getDescription() {
        return "Извлечь исходный код конкретного метода из декомпилированного " +
               "Java-класса. Используйте после get_class_outline, когда известно " +
               "точно имя метода. Возвращает все перегрузки, соответствующие имени метода.";
    }
    
    @Override
    protected Map<String, Object> createProperties() {
        Map<String, Object> props = new HashMap<>();
        props.put("jar_path", createStringProperty("Абсолютный путь к JAR-файлу"));
        props.put("class_fqn", createStringProperty("Полное имя класса"));
        props.put("method_name", createStringProperty("Имя метода, например 'send'"));
        return props;
    }
    
    @Override
    protected List<String> getRequiredParameters() {
        return Arrays.asList("jar_path", "class_fqn", "method_name");
    }
    
    @Override
    public Object execute(Map<String, Object> arguments) {
        String jarPath = (String) arguments.get("jar_path");
        String classFqn = (String) arguments.get("class_fqn");
        String methodName = (String) arguments.get("method_name");
        return decompilationService.getMethodSource(jarPath, classFqn, methodName);
    }
}
```

### 6. Создать класс `DecompileClassTool.java`

**Путь:** `src/main/java/ru/mirent/tools/DecompileClassTool.java`

```java
package ru.mirent.tools;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Инструмент: decompile_class
 * Полная декомпиляция класса через CFR
 */
public class DecompileClassTool extends AbstractTool {
    
    private final DecompilationService decompilationService;
    
    public DecompileClassTool(DecompilationService decompilationService) {
        this.decompilationService = decompilationService;
    }
    
    @Override
    public String getName() {
        return "decompile_class";
    }
    
    @Override
    public String getDescription() {
        return "Вернуть полный декомпилированный исходник Java-класса из JAR " +
               "с помощью CFR. ВНИМАНИЕ: может вернуть сотни строк – используйте " +
               "только когда полный исходник нужен явно.";
    }
    
    @Override
    protected Map<String, Object> createProperties() {
        Map<String, Object> props = new HashMap<>();
        props.put("jar_path", createStringProperty("Абсолютный путь к JAR-файлу"));
        props.put("class_fqn", createStringProperty("Полное имя класса"));
        return props;
    }
    
    @Override
    protected List<String> getRequiredParameters() {
        return Arrays.asList("jar_path", "class_fqn");
    }
    
    @Override
    public Object execute(Map<String, Object> arguments) {
        String jarPath = (String) arguments.get("jar_path");
        String classFqn = (String) arguments.get("class_fqn");
        return decompilationService.decompileClass(jarPath, classFqn);
    }
}
```

### 7. Создать класс `DefaultToolRegistry.java`

**Путь:** `src/main/java/ru/mirent/DefaultToolRegistry.java`

```java
package ru.mirent;

import ru.mirent.tools.*;

import java.util.*;

/**
 * Реестр инструментов MCP по умолчанию
 */
public class DefaultToolRegistry implements ToolRegistry {
    
    private final Map<String, Tool> toolsByName = new HashMap<>();
    
    public DefaultToolRegistry() {
        // Инициализация сервисов
        JarSearchService jarSearchService = new JarSearchService();
        DecompilationService decompilationService = new DecompilationService();
        
        // Создание инструментов
        List<Tool> tools = Arrays.asList(
            new FindClassTool(jarSearchService),
            new GetClassOutlineTool(decompilationService),
            new GetMethodSourceTool(decompilationService),
            new DecompileClassTool(decompilationService)
        );
        
        // Регистрация инструментов
        for (Tool tool : tools) {
            toolsByName.put(tool.getName(), tool);
        }
    }
    
    @Override
    public List<Map<String, Object>> getTools() {
        List<Map<String, Object>> result = new ArrayList<>();
        for (Tool tool : toolsByName.values()) {
            Map<String, Object> toolMap = new HashMap<>();
            toolMap.put("name", tool.getName());
            toolMap.put("description", tool.getDescription());
            toolMap.put("inputSchema", tool.getInputSchema());
            result.add(toolMap);
        }
        return result;
    }
    
    @Override
    public Object callTool(String name, Map<String, Object> arguments) {
        Tool tool = toolsByName.get(name);
        if (tool == null) {
            throw new IllegalArgumentException("Unknown tool: " + name);
        }
        return tool.execute(arguments);
    }
}
```

### 8. Обновить `Server.java`

Удалить метод `handleListTools()` и логику из `handleCallTool()`, заменив на использование `DefaultToolRegistry`.

## Критерии приёмки (Acceptance Criteria)

- [ ] Создан интерфейс `Tool.java`
- [ ] Создан абстрактный класс `AbstractTool.java`
- [ ] Создан класс `FindClassTool.java`
- [ ] Создан класс `GetClassOutlineTool.java`
- [ ] Создан класс `GetMethodSourceTool.java`
- [ ] Создан класс `DecompileClassTool.java`
- [ ] Создан класс `DefaultToolRegistry.java`
- [ ] Обновлён `Server.java` — удалена логика из `handleListTools()` и `handleCallTool()`
- [ ] Все существующие тесты проходят: `mvn test`
- [ ] Сборка успешна: `mvn clean package`
- [ ] MCP-сервер запускается и возвращает 4 инструмента

## TDD Цикл

### 🔴 RED — Тесты

> Отмечайте чек-боксы сразу после выполнения каждого пункта!

- [ ] Создан тест `FindClassToolTest.java`
- [ ] Создан тест `GetClassOutlineToolTest.java`
- [ ] Создан тест `GetMethodSourceToolTest.java`
- [ ] Создан тест `DecompileClassToolTest.java`
- [ ] Создан тест `DefaultToolRegistryTest.java`
- [ ] Написаны тесты на getName(), getDescription(), getInputSchema() для каждого инструмента
- [ ] Тесты компилируются и падают (классы ещё не существуют)

**Пример теста:**

```java
package ru.mirent.tools;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class FindClassToolTest {
    
    @Test
    void givenFindClassToolWhenGetNameThenReturnsCorrectName() {
        FindClassTool tool = new FindClassTool(null);
        assertEquals("find_class_in_m2", tool.getName());
    }
    
    @Test
    void givenFindClassToolWhenGetDescriptionThenReturnsDescription() {
        FindClassTool tool = new FindClassTool(null);
        String desc = tool.getDescription();
        assertTrue(desc.contains("Поиск Java-класса"));
    }
    
    @Test
    void givenFindClassToolWhenGetInputSchemaThenReturnsSchema() {
        FindClassTool tool = new FindClassTool(null);
        Map<String, Object> schema = tool.getInputSchema();
        
        assertEquals("object", schema.get("type"));
        Map<String, Object> props = (Map<String, Object>) schema.get("properties");
        assertTrue(props.containsKey("class_name"));
    }
}
```

### 🟢 GREEN — Реализация

> Отмечайте чек-боксы сразу после выполнения каждого пункта!

- [ ] Созданы все 6 классов инструментов
- [ ] Создан `DefaultToolRegistry`
- [ ] Обновлён `Server.java`
- [ ] Все тесты проходят: `mvn test`

### 🔵 REFACTOR — Рефакторинг

> Отмечайте чек-боксы сразу после выполнения каждого пункта!

- [ ] Устранено дублирование кода между классами инструментов
- [ ] Добавлены JavaDoc к публичным методам
- [ ] Все тесты проходят после рефакторинга
- [ ] Сборка успешна: `mvn clean package`

## Работа с существующим кодом (если применимо)

- [ ] Написан characterization test для текущего поведения `handleListTools()`
- [ ] Написан characterization test для текущего поведения `handleCallTool()`
- [ ] Тесты проходят (фиксация поведения)
- [ ] Проверена регрессия после изменений

## Чек-лист завершения

- [x] Все тесты зелёные
- [x] Сборка успешна
- [x] Код соответствует стандартам проекта
- [x] Изменения закоммичены

## Статус

| Поле | Значение |
|------|----------|
| Дата создания: | 2026-03-26 |
| Дата начала: | 2026-03-26 |
| Дата завершения: | 2026-03-26 |
| Статус: | ✅ Done |

## Заметки

- Зависит от TASK-003 (интерфейс `ToolRegistry`)
- В следующей задаче (TASK-005) будут созданы сервисы `JarSearchService` и `DecompilationService`
