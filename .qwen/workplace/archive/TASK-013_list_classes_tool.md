# TASK-013: Новый инструмент: list_classes_in_jar

> **⚠️ Важно:** Этот файл должен быть создан **до начала** любой работы над задачей.
> Отмечайте чек-боксы `[x]` **сразу после выполнения** каждого шага для возможности продолжения с места прерывания.

## Описание

Добавить новый инструмент MCP для получения списка всех классов в JAR-файле без декомпиляции.

**Текущая проблема:** Нет быстрого способа узнать, какие классы есть в JAR без декомпиляции каждого.

**Цель:** Инструмент `list_classes_in_jar` возвращает список всех `.class` файлов в JAR.

## Требуемые изменения

### 1. Создать класс `ListClassesInJarTool.java`

**Путь:** `src/main/java/ru/mirent/tools/ListClassesInJarTool.java`

```java
package ru.mirent.tools;

import ru.mirent.security.PathValidator;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collectors;

/**
 * Инструмент: list_classes_in_jar
 * Вернуть список всех .class файлов в JAR
 */
public class ListClassesInJarTool extends AbstractTool {
    
    @Override
    public String getName() {
        return "list_classes_in_jar";
    }
    
    @Override
    public String getDescription() {
        return "Вернуть список всех .class файлов в JAR-файле без декомпиляции. " +
               "Полезно для изучения содержимого JAR перед выбором конкретного класса. " +
               "Возвращает список полных имён классов в формате com/example/MyClass.class";
    }
    
    @Override
    protected Map<String, Object> createProperties() {
        Map<String, Object> props = new HashMap<>();
        props.put("jar_path", createStringProperty(
            "Абсолютный путь к JAR-файлу"
        ));
        props.put("filter", createStringProperty(
            "Опциональный фильтр по имени класса (regex)"
        ));
        return props;
    }
    
    @Override
    protected List<String> getRequiredParameters() {
        return Arrays.asList("jar_path");
    }
    
    @Override
    public Object execute(Map<String, Object> arguments) {
        String jarPath = (String) arguments.get("jar_path");
        String filter = (String) arguments.get("filter");
        
        try {
            // Валидация пути
            Path validatedPath = PathValidator.validateJarPath(jarPath);
            
            List<String> classes = listClassesInJar(validatedPath, filter);
            
            if (classes.isEmpty()) {
                return "В JAR не найдено .class файлов: " + jarPath;
            }
            
            StringBuilder sb = new StringBuilder();
            sb.append(String.format("Найдено классов: %d в %s:\n\n", classes.size(), jarPath));
            for (String cls : classes) {
                sb.append("  ").append(cls).append("\n");
            }
            
            return sb.toString();
        } catch (SecurityException e) {
            return "ОШИБКА БЕЗОПАСНОСТИ: " + e.getMessage();
        } catch (IOException e) {
            return "ОШИБКА: " + e.getMessage();
        }
    }
    
    private List<String> listClassesInJar(Path jarPath, String filter) throws IOException {
        List<String> classes = new ArrayList<>();
        
        try (JarFile jar = new JarFile(jarPath.toFile())) {
            Enumeration<JarEntry> entries = jar.entries();
            
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                String name = entry.getName();
                
                // Фильтр по .class файлам
                if (name.endsWith(".class")) {
                    // Исключаем модули (module-info.class)
                    if ("module-info.class".equals(name)) {
                        continue;
                    }
                    
                    // Применяем фильтр по имени
                    if (filter != null && !name.matches(filter)) {
                        continue;
                    }
                    
                    classes.add(name);
                }
            }
        }
        
        // Сортировка
        classes.sort(String::compareTo);
        
        return classes;
    }
}
```

### 2. Обновить `DefaultToolRegistry.java`

Добавить новый инструмент в реестр:

```java
public DefaultToolRegistry() {
    JarSearchService jarSearchService = new JarSearchService();
    DecompilationService decompilationService = new DecompilationService();
    
    List<Tool> tools = Arrays.asList(
        new FindClassTool(jarSearchService),
        new GetClassOutlineTool(decompilationService),
        new GetMethodSourceTool(decompilationService),
        new DecompileClassTool(decompilationService),
        new ListClassesInJarTool() // Новый инструмент
    );
    
    for (Tool tool : tools) {
        toolsByName.put(tool.getName(), tool);
    }
}
```

### 3. Обновить `handleListTools()` или `JsonRpcHandler`

Новый инструмент автоматически появится в списке через `DefaultToolRegistry`.

## Критерии приёмки (Acceptance Criteria)

- [x] Создан класс `ListClassesInJarTool.java`
- [x] Обновлён `DefaultToolRegistry.java`
- [x] Написаны тесты на новый инструмент
- [x] Все существующие тесты проходят: `mvn test`
- [x] Сборка успешна: `mvn clean package`
- [x] MCP-сервер возвращает 5 инструментов (было 4)

## TDD Цикл

### 🔴 RED — Тесты

> Отмечайте чек-боксы сразу после выполнения каждого пункта!

- [x] Создан тест `ListClassesInJarToolTest.java`
- [x] Написан тест `givenListClassesInJarToolWhenGetNameThenReturnsCorrectName()`
- [x] Написан тест `givenListClassesInJarToolWhenGetDescriptionThenReturnsDescription()`
- [x] Написан тест `givenListClassesInJarToolWhenGetInputSchemaThenReturnsSchema()`
- [x] Написан тест `givenValidJarWhenExecuteThenReturnsClassList()`
- [x] Написан тест `givenInvalidJarPathWhenExecuteThenReturnsSecurityError()`
- [x] Написан тест `givenFilterWhenExecuteThenReturnsFilteredClassList()`
- [x] Тесты компилируются и падают

**Пример теста:**

```java
package ru.mirent.tools;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.FileOutputStream;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

import static org.junit.jupiter.api.Assertions.*;

class ListClassesInJarToolTest {
    
    private ListClassesInJarTool tool;
    
    @TempDir
    Path tempDir;
    
    private Path testJar;
    
    @BeforeEach
    void setUp() throws Exception {
        tool = new ListClassesInJarTool();
        testJar = createTestJar();
    }
    
    @Test
    void givenListClassesInJarToolWhenGetNameThenReturnsCorrectName() {
        assertEquals("list_classes_in_jar", tool.getName());
    }
    
    @Test
    void givenListClassesInJarToolWhenGetDescriptionThenReturnsDescription() {
        String desc = tool.getDescription();
        
        assertNotNull(desc);
        assertTrue(desc.contains("список всех .class файлов"));
    }
    
    @Test
    void givenListClassesInJarToolWhenGetInputSchemaThenReturnsSchema() {
        Map<String, Object> schema = tool.getInputSchema();
        
        assertEquals("object", schema.get("type"));
        Map<String, Object> props = (Map<String, Object>) schema.get("properties");
        assertTrue(props.containsKey("jar_path"));
        // filter опционален
        assertTrue(props.containsKey("filter"));
    }
    
    @Test
    void givenValidJarWhenExecuteThenReturnsClassList() {
        Map<String, Object> args = new HashMap<>();
        args.put("jar_path", testJar.toString());
        
        Object result = tool.execute(args);
        
        assertNotNull(result);
        String resultStr = (String) result;
        assertTrue(resultStr.contains("Найдено классов: 3"));
        assertTrue(resultStr.contains("com/example/Class1.class"));
        assertTrue(resultStr.contains("com/example/Class2.class"));
        assertTrue(resultStr.contains("com/example/Class3.class"));
    }
    
    @Test
    void givenFilterWhenExecuteThenReturnsFilteredClassList() {
        Map<String, Object> args = new HashMap<>();
        args.put("jar_path", testJar.toString());
        args.put("filter", ".*Class1.*");
        
        Object result = tool.execute(args);
        
        assertNotNull(result);
        String resultStr = (String) result;
        assertTrue(resultStr.contains("Найдено классов: 1"));
        assertTrue(resultStr.contains("com/example/Class1.class"));
    }
    
    @Test
    void givenInvalidJarPathWhenExecuteThenReturnsSecurityError() {
        Map<String, Object> args = new HashMap<>();
        args.put("jar_path", "/etc/passwd");
        
        Object result = tool.execute(args);
        
        assertNotNull(result);
        String resultStr = (String) result;
        assertTrue(resultStr.contains("ОШИБКА"));
    }
    
    @Test
    void givenNonExistentJarWhenExecuteThenReturnsError() {
        Map<String, Object> args = new HashMap<>();
        args.put("jar_path", "/nonexistent.jar");
        
        Object result = tool.execute(args);
        
        assertNotNull(result);
        String resultStr = (String) result;
        assertTrue(resultStr.contains("ОШИБКА"));
    }
    
    @Test
    void givenEmptyJarWhenExecuteThenReturnsEmptyMessage() {
        Path emptyJar = createEmptyJar();
        
        Map<String, Object> args = new HashMap<>();
        args.put("jar_path", emptyJar.toString());
        
        Object result = tool.execute(args);
        
        assertNotNull(result);
        String resultStr = (String) result;
        assertTrue(resultStr.contains("не найдено .class файлов"));
    }
    
    // Вспомогательные методы
    
    private Path createTestJar() throws Exception {
        Path jarFile = tempDir.resolve("test.jar");
        
        try (JarOutputStream jos = new JarOutputStream(new FileOutputStream(jarFile.toFile()))) {
            // Добавляем 3 класса
            addJarEntry(jos, "com/example/Class1.class");
            addJarEntry(jos, "com/example/Class2.class");
            addJarEntry(jos, "com/example/Class3.class");
            
            // Добавляем module-info (должен исключаться)
            addJarEntry(jos, "module-info.class");
            
            // Добавляем не-class файл
            addJarEntry(jos, "README.txt");
        }
        
        return jarFile;
    }
    
    private Path createEmptyJar() throws Exception {
        Path jarFile = tempDir.resolve("empty.jar");
        
        try (JarOutputStream jos = new JarOutputStream(new FileOutputStream(jarFile.toFile()))) {
            // Пустой JAR
        }
        
        return jarFile;
    }
    
    private void addJarEntry(JarOutputStream jos, String name) throws Exception {
        jos.putNextEntry(new JarEntry(name));
        jos.write(new byte[] {0xCA, 0xFE, 0xBA, 0xBE}); // Fake class header
        jos.closeEntry();
    }
}
```

### 🟢 GREEN — Реализация

> Отмечайте чек-боксы сразу после выполнения каждого пункта!

- [x] Создан `ListClassesInJarTool.java`
- [x] Обновлён `DefaultToolRegistry.java`
- [x] Все тесты проходят: `mvn test`

### 🔵 REFACTOR — Рефакторинг

> Отмечайте чек-боксы сразу после выполнения каждого пункта!

- [x] Устранено дублирование кода
- [x] Добавлены JavaDoc к публичным методам
- [x] Все тесты проходят после рефакторинга
- [x] Сборка успешна: `mvn clean package`

## Работа с существующим кодом (если применимо)

- [x] Проверена регрессия существующих инструментов
- [x] Все старые тесты проходят

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

- Зависит от TASK-006 (PathValidator)
- Инструмент не требует декомпиляции — быстрый просмотр содержимого JAR
- Поддерживает опциональный regex-фильтр
- В следующей задаче (TASK-014) будет добавлен инструмент поиска по regex
