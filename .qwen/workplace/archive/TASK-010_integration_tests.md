# TASK-010: Интеграционные тесты с реальными JAR

> **⚠️ Важно:** Этот файл должен быть создан **до начала** любой работы над задачей.
> Отмечайте чек-боксы `[x]` **сразу после выполнения** каждого шага для возможности продолжения с места прерывания.

## Описание

Написать интеграционные тесты с реальными JAR-файлами из локального Maven-репозитория.

**Текущая проблема:** Все тесты изолированные (unit), нет проверки работы с реальными JAR из `~/.m2/repository`.

**Цель:** Проверить работу всех 4 инструментов MCP с реальными классами из популярных библиотек (Guava, Commons Lang, Spring).

## Требуемые изменения

### 1. Создать класс `McpIntegrationTest.java`

**Путь:** `src/test/java/ru/mirent/McpIntegrationTest.java`

```java
package ru.mirent;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import ru.mirent.services.*;
import ru.mirent.tools.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Интеграционные тесты MCP-сервера с реальными JAR из ~/.m2/repository
 * 
 * Запуск: mvn test -DrunIntegrationTests=true
 * или: mvn test -Dit
 */
class McpIntegrationTest {
    
    private static JarSearchService jarSearchService;
    private static DecompilationService decompilationService;
    private static DefaultToolRegistry toolRegistry;
    
    private static String guavaJarPath;
    private static String commonsLangJarPath;
    
    @BeforeAll
    static void setUpAll() {
        jarSearchService = new JarSearchService();
        decompilationService = new DecompilationService();
        toolRegistry = new DefaultToolRegistry();
        
        // Поиск тестовых JAR
        guavaJarPath = findGuavaJar();
        commonsLangJarPath = findCommonsLangJar();
    }
    
    @BeforeEach
    void setUp() {
        // Очистка выходной директории декомпиляции перед каждым тестом
        try {
            java.nio.file.Path outputDir = java.nio.file.Paths.get("/tmp/cfr-decompiled");
            if (java.nio.file.Files.exists(outputDir)) {
                java.nio.file.Files.walk(outputDir)
                    .filter(java.nio.file.Files::isRegularFile)
                    .forEach(p -> {
                        try {
                            java.nio.file.Files.delete(p);
                        } catch (Exception e) {
                            // Игнорируем ошибки удаления
                        }
                    });
            }
        } catch (Exception e) {
            // Игнорируем ошибки очистки
        }
    }
    
    // ==================== Тесты поиска ====================
    
    @Test
    @DisplayName("Поиск класса Preconditions из Guava")
    @EnabledIfSystemProperty(named = "runIntegrationTests", matches = "true")
    void givenPreconditionsClassWhenFindClassThenReturnsGuavaJar() {
        String result = jarSearchService.findClass(
            "com.google.common.base.Preconditions"
        );
        
        assertNotNull(result);
        assertTrue(result.contains("guava"));
        assertTrue(result.contains("Найдено JAR-файлов"));
    }
    
    @Test
    @DisplayName("Поиск класса StringUtils из Commons Lang")
    @EnabledIfSystemProperty(named = "runIntegrationTests", matches = "true")
    void givenStringUtilsClassWhenFindClassThenReturnsCommonsLangJar() {
        String result = jarSearchService.findClass(
            "org.apache.commons.lang3.StringUtils"
        );
        
        assertNotNull(result);
        assertTrue(result.contains("commons-lang"));
    }
    
    @Test
    @DisplayName("Поиск по простому имени класса")
    @EnabledIfSystemProperty(named = "runIntegrationTests", matches = "true")
    void givenSimpleClassNameWhenFindClassThenReturnsMatches() {
        String result = jarSearchService.findClass("Preconditions");
        
        assertNotNull(result);
        // Может найти в нескольких версиях Guava
        assertTrue(result.contains("Найдено JAR-файлов"));
    }
    
    @Test
    @DisplayName("Поиск несуществующего класса")
    @EnabledIfSystemProperty(named = "runIntegrationTests", matches = "true")
    void givenNonExistentClassWhenFindClassThenReturnsNotFound() {
        String result = jarSearchService.findClass(
            "com.example.NonExistentClass12345"
        );
        
        assertNotNull(result);
        assertTrue(result.contains("не найден в JAR-файлах"));
    }
    
    // ==================== Тесты декомпиляции ====================
    
    @Test
    @DisplayName("Получение схемы класса Preconditions")
    @EnabledIfSystemProperty(named = "runIntegrationTests", matches = "true")
    void givenPreconditionsClassWhenGetClassOutlineThenReturnsOutline() {
        String outline = decompilationService.getClassOutline(
            guavaJarPath,
            "com.google.common.base.Preconditions"
        );
        
        assertNotNull(outline);
        assertTrue(outline.contains("class Preconditions"));
        assertTrue(outline.contains("checkNotNull"));
        // Тела методов должны быть заменены на "..."
        assertFalse(outline.contains("throw new NullPointerException"));
    }
    
    @Test
    @DisplayName("Получение метода checkNotNull из Preconditions")
    @EnabledIfSystemProperty(named = "runIntegrationTests", matches = "true")
    void givenCheckNotNullMethodWhenGetMethodSourceThenReturnsSource() {
        String source = decompilationService.getMethodSource(
            guavaJarPath,
            "com.google.common.base.Preconditions",
            "checkNotNull"
        );
        
        assertNotNull(source);
        assertTrue(source.contains("checkNotNull"));
        // Должен содержать тело метода
        assertTrue(source.contains("throw new NullPointerException"));
    }
    
    @Test
    @DisplayName("Полная декомпиляция класса Preconditions")
    @EnabledIfSystemProperty(named = "runIntegrationTests", matches = "true")
    void givenPreconditionsClassWhenDecompileClassThenReturnsFullSource() {
        String source = decompilationService.decompileClass(
            guavaJarPath,
            "com.google.common.base.Preconditions"
        );
        
        assertNotNull(source);
        assertTrue(source.contains("class Preconditions"));
        assertTrue(source.contains("checkNotNull"));
        assertTrue(source.contains("throw new NullPointerException"));
        assertTrue(source.contains("JAR: guava"));
    }
    
    // ==================== Тесты инструментов ====================
    
    @Test
    @DisplayName("Инструмент find_class_in_m2 через реестр")
    @EnabledIfSystemProperty(named = "runIntegrationTests", matches = "true")
    void givenFindClassToolWhenCallThenReturnsResult() {
        Map<String, Object> args = new HashMap<>();
        args.put("class_name", "com.google.common.base.Preconditions");
        
        Object result = toolRegistry.callTool("find_class_in_m2", args);
        
        assertNotNull(result);
        assertTrue(result instanceof String);
        String resultStr = (String) result;
        assertTrue(resultStr.contains("guava"));
    }
    
    @Test
    @DisplayName("Инструмент get_class_outline через реестр")
    @EnabledIfSystemProperty(named = "runIntegrationTests", matches = "true")
    void givenGetClassOutlineToolWhenCallThenReturnsOutline() {
        Map<String, Object> args = new HashMap<>();
        args.put("jar_path", guavaJarPath);
        args.put("class_fqn", "com.google.common.base.Preconditions");
        
        Object result = toolRegistry.callTool("get_class_outline", args);
        
        assertNotNull(result);
        assertTrue(result instanceof String);
        String resultStr = (String) result;
        assertTrue(resultStr.contains("class Preconditions"));
    }
    
    @Test
    @DisplayName("Инструмент get_method_source через реестр")
    @EnabledIfSystemProperty(named = "runIntegrationTests", matches = "true")
    void givenGetMethodSourceToolWhenCallThenReturnsSource() {
        Map<String, Object> args = new HashMap<>();
        args.put("jar_path", guavaJarPath);
        args.put("class_fqn", "com.google.common.base.Preconditions");
        args.put("method_name", "checkNotNull");
        
        Object result = toolRegistry.callTool("get_method_source", args);
        
        assertNotNull(result);
        assertTrue(result instanceof String);
        String resultStr = (String) result;
        assertTrue(resultStr.contains("checkNotNull"));
    }
    
    @Test
    @DisplayName("Инструмент decompile_class через реестр")
    @EnabledIfSystemProperty(named = "runIntegrationTests", matches = "true")
    void givenDecompileClassToolWhenCallThenReturnsFullSource() {
        Map<String, Object> args = new HashMap<>();
        args.put("jar_path", guavaJarPath);
        args.put("class_fqn", "com.google.common.base.Preconditions");
        
        Object result = toolRegistry.callTool("decompile_class", args);
        
        assertNotNull(result);
        assertTrue(result instanceof String);
        String resultStr = (String) result;
        assertTrue(resultStr.contains("class Preconditions"));
    }
    
    // ==================== Тесты безопасности ====================
    
    @Test
    @DisplayName("Попытка path traversal блокируется")
    @EnabledIfSystemProperty(named = "runIntegrationTests", matches = "true")
    void givenPathTraversalWhenCallToolThenReturnsSecurityError() {
        Map<String, Object> args = new HashMap<>();
        args.put("jar_path", "../../../etc/passwd");
        args.put("class_fqn", "test.Test");
        
        Object result = toolRegistry.callTool("get_class_outline", args);
        
        assertNotNull(result);
        assertTrue(result instanceof String);
        String resultStr = (String) result;
        assertTrue(resultStr.contains("ОШИБКА"));
    }
    
    @Test
    @DisplayName("Попытка инъекции FQN блокируется")
    @EnabledIfSystemProperty(named = "runIntegrationTests", matches = "true")
    void givenInvalidFQNWhenCallToolThenReturnsValidationError() {
        Map<String, Object> args = new HashMap<>();
        args.put("jar_path", guavaJarPath);
        args.put("class_fqn", "com/example/Class; rm -rf /");
        
        Object result = toolRegistry.callTool("get_class_outline", args);
        
        assertNotNull(result);
        assertTrue(result instanceof String);
        String resultStr = (String) result;
        assertTrue(resultStr.contains("ОШИБКА"));
    }
    
    // ==================== Вспомогательные методы ====================
    
    private static String findGuavaJar() {
        String result = jarSearchService.findClass(
            "com.google.common.base.Preconditions"
        );
        
        // Извлекаем первый найденный JAR с "guava" в пути
        String[] lines = result.split("\n");
        for (String line : lines) {
            line = line.trim();
            if (line.contains("guava") && line.contains(".jar")) {
                return line;
            }
        }
        
        throw new IllegalStateException(
            "Guava не найдена в локальном Maven-репозитории. " +
            "Запустите: mvn dependency:get -Dartifact=com.google.guava:guava:33.4.0-jre"
        );
    }
    
    private static String findCommonsLangJar() {
        String result = jarSearchService.findClass(
            "org.apache.commons.lang3.StringUtils"
        );
        
        String[] lines = result.split("\n");
        for (String line : lines) {
            line = line.trim();
            if (line.contains("commons-lang") && line.contains(".jar")) {
                return line;
            }
        }
        
        throw new IllegalStateException(
            "Commons Lang не найдена в локальном Maven-репозитории. " +
            "Запустите: mvn dependency:get -Dartifact=org.apache.commons:commons-lang3:3.14.0"
        );
    }
}
```

### 2. Обновить `pom.xml` для запуска интеграционных тестов

Добавить профиль для интеграционных тестов:

```xml
<profiles>
    <profile>
        <id>integration-tests</id>
        <activation>
            <property>
                <name>runIntegrationTests</name>
                <value>true</value>
            </property>
        </activation>
        <build>
            <plugins>
                <plugin>
                    <groupId>org.apache.maven.plugins</groupId>
                    <artifactId>maven-surefire-plugin</artifactId>
                    <version>3.0.0</version>
                    <configuration>
                        <systemPropertyVariables>
                            <runIntegrationTests>true</runIntegrationTests>
                        </systemPropertyVariables>
                    </configuration>
                </plugin>
            </plugins>
        </build>
    </profile>
</profiles>
```

## Критерии приёмки (Acceptance Criteria)

- [x] Создан класс `McpIntegrationTest.java`
- [x] Написано минимум 12 интеграционных тестов (написано 13)
- [x] Тесты используют аннотацию `@EnabledIfSystemProperty`
- [x] Тесты работают с реальными JAR из ~/.m2
- [x] Сборка успешна: `mvn clean package`
- [x] Интеграционные тесты запускаются: `mvn test -DrunIntegrationTests=true`

## TDD Цикл

### 🔴 RED — Тесты

> Отмечайте чек-боксы сразу после выполнения каждого пункта!

- [x] Создан файл `McpIntegrationTest.java`
- [x] Написан тест `givenPreconditionsClassWhenFindClassThenReturnsGuavaJar()`
- [x] Написан тест `givenPreconditionsClassWhenGetClassOutlineThenReturnsOutline()`
- [x] Написан тест `givenCheckNotNullMethodWhenGetMethodSourceThenReturnsSource()`
- [x] Написан тест `givenPreconditionsClassWhenDecompileClassThenReturnsFullSource()`
- [x] Написан тест `givenPathTraversalWhenCallToolThenReturnsSecurityError()`
- [x] Написано 13 интеграционных тестов
- [x] Тесты компилируются

### 🟢 GREEN — Реализация

> Отмечайте чек-боксы сразу после выполнения каждого пункта!

- [x] Все интеграционные тесты написаны
- [x] Тесты проходят при наличии JAR в ~/.m2
- [x] Тесты пропускаются без флага `-DrunIntegrationTests=true`

### 🔵 REFACTOR — Рефакторинг

> Отмечайте чек-боксы сразу после выполнения каждого пункта!

- [x] Выделены вспомогательные методы `findGuavaJar()`, `findCommonsLangJar()`
- [x] Добавлены `@DisplayName` к тестам
- [x] Все тесты проходят после рефакторинга
- [x] Сборка успешна: `mvn clean package`

## Работа с существующим кодом (если применимо)

- [x] Проверена регрессия существующих unit-тестов
- [x] Все unit-тесты проходят

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

- Зависит от TASK-003 — TASK-009 (рефакторинг архитектуры)
- Для запуска тестов требуется Guava в ~/.m2
- Тесты помечены `@EnabledIfSystemProperty` для селективного запуска
- В следующей задаче (TASK-011) будут написаны characterization tests
