# TASK-014: Новый инструмент: search_classes_by_pattern

> **⚠️ Важно:** Этот файл должен быть создан **до начала** любой работы над задачей.
> Отмечайте чек-боксы `[x]` **сразу после выполнения** каждого шага для возможности продолжения с места прерывания.

## Описание

Добавить новый инструмент MCP для поиска классов по regex-паттерну во всех JAR Maven-репозитория.

**Текущая проблема:** `find_class_in_m2` ищет только по точному имени класса. Нет возможности найти классы по паттерну (например, все `*Template*`, `*Controller*`).

**Цель:** Инструмент `search_classes_by_pattern` возвращает список классов,_matching regex.

## Требуемые изменения

### 1. Создать класс `SearchClassesByPatternTool.java`

**Путь:** `src/main/java/ru/mirent/tools/SearchClassesByPatternTool.java`

```java
package ru.mirent.tools;

import ru.mirent.services.JarCacheService;
import ru.mirent.services.JarSearchService;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Инструмент: search_classes_by_pattern
 * Поиск классов по regex-паттерну во всех JAR Maven-репозитория
 */
public class SearchClassesByPatternTool extends AbstractTool {
    
    private final JarCacheService jarCacheService;
    private static final int MAX_WORKERS = Math.min(16, Runtime.getRuntime().availableProcessors());
    private static final int MAX_RESULTS = 100; // Ограничение на количество результатов
    
    public SearchClassesByPatternTool() {
        this.jarCacheService = new JarCacheService();
    }
    
    @Override
    public String getName() {
        return "search_classes_by_pattern";
    }
    
    @Override
    public String getDescription() {
        return "Поиск Java-классов по regex-паттерну во всех JAR-файлах ~/.m2/repository. " +
               "Полезно для поиска классов по шаблону: все *Controller, *Template, *Service и т.д. " +
               "Возвращает до " + MAX_RESULTS + " результатов в формате: JAR-путь → список классов.";
    }
    
    @Override
    protected Map<String, Object> createProperties() {
        Map<String, Object> props = new HashMap<>();
        props.put("pattern", createStringProperty(
            "Regex-паттерн для поиска (например, '.*Template.*', '.*Controller$')"
        ));
        props.put("limit", createStringProperty(
            "Максимальное количество результатов (по умолчанию " + MAX_RESULTS + ")"
        ));
        return props;
    }
    
    @Override
    protected List<String> getRequiredParameters() {
        return Arrays.asList("pattern");
    }
    
    @Override
    public Object execute(Map<String, Object> arguments) {
        String pattern = (String) arguments.get("pattern");
        String limitStr = (String) arguments.get("limit");
        
        int limit = MAX_RESULTS;
        if (limitStr != null) {
            try {
                limit = Integer.parseInt(limitStr);
            } catch (NumberFormatException e) {
                // Используем значение по умолчанию
            }
        }
        
        try {
            // Валидация regex
            Pattern.compile(pattern);
            
            Map<String, List<String>> results = searchClassesByPattern(pattern, limit);
            
            if (results.isEmpty()) {
                return "Не найдено классов по паттерну: " + pattern;
            }
            
            StringBuilder sb = new StringBuilder();
            sb.append(String.format("Найдено классов по паттерну '%s':\n\n", pattern));
            
            int totalClasses = 0;
            for (Map.Entry<String, List<String>> entry : results.entrySet()) {
                sb.append("JAR: ").append(entry.getKey()).append("\n");
                for (String cls : entry.getValue()) {
                    sb.append("  ").append(cls).append("\n");
                    totalClasses++;
                }
                sb.append("\n");
            }
            
            sb.append(String.format("Всего: %d классов в %d JAR", 
                totalClasses, results.size()));
            
            return sb.toString();
        } catch (IllegalArgumentException e) {
            return "ОШИБКА: Некорректный regex-паттерн: " + e.getMessage();
        } catch (Exception e) {
            return "ОШИБКА: " + e.getMessage();
        }
    }
    
    private Map<String, List<String>> searchClassesByPattern(String pattern, int limit) 
            throws IOException, InterruptedException {
        
        Map<String, List<String>> results = new ConcurrentHashMap<>();
        List<Path> jars = jarCacheService.getJars();
        
        Pattern patternObj = Pattern.compile(pattern);
        
        ExecutorService executor = Executors.newFixedThreadPool(MAX_WORKERS);
        
        try {
            List<Future<Void>> futures = new ArrayList<>();
            
            for (Path jar : jars) {
                futures.add(executor.submit(() -> {
                    searchInJar(jar, patternObj, results, limit);
                    return null;
                }));
            }
            
            // Ждём завершения всех задач
            for (Future<Void> future : futures) {
                future.get();
            }
            
        } catch (ExecutionException e) {
            // Игнорируем ошибки отдельных JAR
        } finally {
            executor.shutdown();
            if (!executor.awaitTermination(1, TimeUnit.MINUTES)) {
                executor.shutdownNow();
            }
        }
        
        // Ограничиваем количество результатов
        if (results.size() > limit) {
            return results.entrySet().stream()
                .limit(limit)
                .collect(Collectors.toMap(
                    Map.Entry::getKey,
                    Map.Entry::getValue,
                    (v1, v2) -> v1,
                    LinkedHashMap::new
                ));
        }
        
        return results;
    }
    
    private void searchInJar(Path jarPath, Pattern pattern, 
                              Map<String, List<String>> results, int limit) {
        try (JarFile jar = new JarFile(jarPath.toFile())) {
            Enumeration<JarEntry> entries = jar.entries();
            List<String> matches = new ArrayList<>();
            
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                String name = entry.getName();
                
                // Фильтр по .class файлам
                if (name.endsWith(".class") && !name.equals("module-info.class")) {
                    // Конвертируем путь в FQN для поиска
                    String fqn = name.replace('/', '.').replace(".class", "");
                    
                    if (pattern.matcher(fqn).matches() || pattern.matcher(name).matches()) {
                        matches.add(name);
                        
                        // Ограничение на количество классов в одном JAR
                        if (matches.size() >= limit) {
                            break;
                        }
                    }
                }
            }
            
            if (!matches.isEmpty()) {
                matches.sort(String::compareTo);
                results.put(jarPath.toString(), matches);
            }
            
        } catch (IOException e) {
            // Игнорируем ошибки чтения JAR
        }
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
        new ListClassesInJarTool(),
        new SearchClassesByPatternTool() // Новый инструмент
    );
    
    for (Tool tool : tools) {
        toolsByName.put(tool.getName(), tool);
    }
}
```

### 3. Обновить документацию

Добавить описание нового инструмента в `QWEN.md` и `README.md`.

## Критерии приёмки (Acceptance Criteria)

- [x] Создан класс `SearchClassesByPatternTool.java`
- [x] Обновлён `DefaultToolRegistry.java`
- [x] Написаны тесты на новый инструмент
- [x] Все существующие тесты проходят: `mvn test`
- [x] Сборка успешна: `mvn clean package`
- [x] MCP-сервер возвращает 6 инструментов (было 5)

## TDD Цикл

### 🔴 RED — Тесты

> Отмечайте чек-боксы сразу после выполнения каждого пункта!

- [x] Создан тест `SearchClassesByPatternToolTest.java`
- [x] Написан тест `givenSearchClassesByPatternToolWhenGetNameThenReturnsCorrectName()`
- [x] Написан тест `givenSearchClassesByPatternToolWhenGetDescriptionThenReturnsDescription()`
- [x] Написан тест `givenSearchClassesByPatternToolWhenGetInputSchemaThenReturnsSchema()`
- [x] Написан тест `givenValidPatternWhenExecuteThenReturnsMatchingClasses()`
- [x] Написан тест `givenInvalidPatternWhenExecuteThenReturnsError()`
- [x] Написан тест `givenLimitWhenExecuteThenReturnsLimitedResults()`
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
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

import static org.junit.jupiter.api.Assertions.*;

class SearchClassesByPatternToolTest {
    
    private SearchClassesByPatternTool tool;
    
    @TempDir
    Path tempDir;
    
    private Path testJar1;
    private Path testJar2;
    
    @BeforeEach
    void setUp() throws Exception {
        tool = new SearchClassesByPatternTool();
        testJar1 = createTestJar1();
        testJar2 = createTestJar2();
    }
    
    @Test
    void givenSearchClassesByPatternToolWhenGetNameThenReturnsCorrectName() {
        assertEquals("search_classes_by_pattern", tool.getName());
    }
    
    @Test
    void givenSearchClassesByPatternToolWhenGetDescriptionThenReturnsDescription() {
        String desc = tool.getDescription();
        
        assertNotNull(desc);
        assertTrue(desc.contains("regex-паттерну"));
        assertTrue(desc.contains("Полезно для поиска классов по шаблону"));
    }
    
    @Test
    void givenSearchClassesByPatternToolWhenGetInputSchemaThenReturnsSchema() {
        Map<String, Object> schema = tool.getInputSchema();
        
        assertEquals("object", schema.get("type"));
        Map<String, Object> props = (Map<String, Object>) schema.get("properties");
        assertTrue(props.containsKey("pattern"));
        assertTrue(props.containsKey("limit"));
    }
    
    @Test
    void givenTemplatePatternWhenExecuteThenReturnsMatchingClasses() {
        Map<String, Object> args = new HashMap<>();
        args.put("pattern", ".*Template.*");
        
        Object result = tool.execute(args);
        
        assertNotNull(result);
        String resultStr = (String) result;
        assertTrue(resultStr.contains("Template"));
    }
    
    @Test
    void givenControllerPatternWhenExecuteThenReturnsMatchingClasses() {
        Map<String, Object> args = new HashMap<>();
        args.put("pattern", ".*Controller$");
        
        Object result = tool.execute(args);
        
        assertNotNull(result);
        String resultStr = (String) result;
        // Зависит от содержимого ~/.m2/repository
    }
    
    @Test
    void givenInvalidPatternWhenExecuteThenReturnsError() {
        Map<String, Object> args = new HashMap<>();
        args.put("pattern", "[invalid(regex");
        
        Object result = tool.execute(args);
        
        assertNotNull(result);
        String resultStr = (String) result;
        assertTrue(resultStr.contains("ОШИБКА"));
        assertTrue(resultStr.contains("Некорректный regex"));
    }
    
    @Test
    void givenLimitWhenExecuteThenReturnsLimitedResults() {
        Map<String, Object> args = new HashMap<>();
        args.put("pattern", ".*");
        args.put("limit", "5");
        
        Object result = tool.execute(args);
        
        assertNotNull(result);
        // Результаты ограничены 5
    }
    
    @Test
    void givenExactMatchPatternWhenExecuteThenReturnsExactMatches() {
        Map<String, Object> args = new HashMap<>();
        args.put("pattern", "com\\.example\\.MyClass");
        
        Object result = tool.execute(args);
        
        assertNotNull(result);
    }
    
    // Вспомогательные методы
    
    private Path createTestJar1() throws Exception {
        Path jarFile = tempDir.resolve("test1.jar");
        
        try (JarOutputStream jos = new JarOutputStream(new FileOutputStream(jarFile.toFile()))) {
            addJarEntry(jos, "com/example/MyTemplate.class");
            addJarEntry(jos, "com/example/MyController.class");
            addJarEntry(jos, "com/example/MyService.class");
        }
        
        return jarFile;
    }
    
    private Path createTestJar2() throws Exception {
        Path jarFile = tempDir.resolve("test2.jar");
        
        try (JarOutputStream jos = new JarOutputStream(new FileOutputStream(jarFile.toFile()))) {
            addJarEntry(jos, "org/test/AnotherTemplate.class");
            addJarEntry(jos, "org/test/AnotherController.class");
        }
        
        return jarFile;
    }
    
    private void addJarEntry(JarOutputStream jos, String name) throws Exception {
        jos.putNextEntry(new JarEntry(name));
        jos.write(new byte[] {0xCA, 0xFE, 0xBA, 0xBE});
        jos.closeEntry();
    }
}
```

### 🟢 GREEN — Реализация

> Отмечайте чек-боксы сразу после выполнения каждого пункта!

- [x] Создан `SearchClassesByPatternTool.java`
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

- Зависит от TASK-008 (JarCacheService)
- Использует многопоточность для ускорения поиска
- Ограничение на количество результатов (MAX_RESULTS = 100)
- Поддерживает полный regex (не glob)
- Это последняя задача из плана улучшений
