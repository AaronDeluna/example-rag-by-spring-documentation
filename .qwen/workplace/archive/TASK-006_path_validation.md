# TASK-006: Валидация путей: защита от path traversal

> **⚠️ Важно:** Этот файл должен быть создан **до начала** любой работы над задачей.
> Отмечайте чек-боксы `[x]` **сразу после выполнения** каждого шага для возможности продолжения с места прерывания.

## Описание

Добавить валидацию путей к JAR-файлам для защиты от path traversal атак.

**Текущая проблема:** Пользователь может передать путь вида `../../../etc/passwd` или `/etc/shadow`, что выходит за пределы `~/.m2/repository`.

**Цель:** Все пути к JAR должны начинаться с `~/.m2/repository`.

## Требуемые изменения

### 1. Создать класс `PathValidator.java`

**Путь:** `src/main/java/ru/mirent/security/PathValidator.java`

```java
package ru.mirent.security;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Валидатор путей для защиты от path traversal атак
 */
public class PathValidator {
    
    private static final Path M2_REPO = Paths.get(
        System.getProperty("user.home"), ".m2", "repository"
    ).toAbsolutePath().normalize();
    
    /**
     * Проверить и вернуть нормализованный путь
     * @param userInput пользовательский ввод пути
     * @return нормализованный Path
     * @throws SecurityException если путь выходит за пределы ~/.m2/repository
     */
    public static Path validateJarPath(String userInput) {
        if (userInput == null || userInput.trim().isEmpty()) {
            throw new SecurityException("Путь не может быть пустым");
        }
        
        // Нормализация пути (устранение ../ и ./)
        Path path = Paths.get(userInput).toAbsolutePath().normalize();
        
        // Проверка: путь должен начинаться с ~/.m2/repository
        if (!path.startsWith(M2_REPO)) {
            throw new SecurityException(
                "Путь выходит за пределы Maven-репозитория: " + userInput +
                ". Разрешены только пути внутри " + M2_REPO
            );
        }
        
        // Проверка: файл должен существовать
        if (!java.nio.file.Files.exists(path)) {
            throw new SecurityException("Файл не найден: " + userInput);
        }
        
        // Проверка: файл должен быть JAR
        if (!userInput.endsWith(".jar")) {
            throw new SecurityException("Файл должен быть JAR: " + userInput);
        }
        
        return path;
    }
    
    /**
     * Проверить путь без исключения (возвращает boolean)
     */
    public static boolean isValidJarPath(String userInput) {
        try {
            validateJarPath(userInput);
            return true;
        } catch (SecurityException e) {
            return false;
        }
    }
    
    /**
     * Вернуть путь к Maven-репозиторию для сообщений об ошибках
     */
    public static Path getM2RepoPath() {
        return M2_REPO;
    }
}
```

### 2. Обновить `DecompilationService.java`

Добавить валидацию в метод `ensureDecompiled()`:

```java
private Path ensureDecompiled(String jarPath, String classFqn) throws IOException {
    // Валидация пути
    Path validatedPath = PathValidator.validateJarPath(jarPath);
    
    Path javaFile = OUTPUT_DIR.resolve(
        classFqn.replace('.', File.separatorChar) + ".java"
    );
    // ... остальной код
}
```

### 3. Обновить `JarSearchService.java`

При проверке `jarContainsClass()` добавить логирование попыток доступа к недопустимым путям (опционально).

### 4. Обновить обработку ошибок в инструментах

В `GetClassOutlineTool`, `GetMethodSourceTool`, `DecompileClassTool` добавить обработку `SecurityException`:

```java
@Override
public Object execute(Map<String, Object> arguments) {
    String jarPath = (String) arguments.get("jar_path");
    String classFqn = (String) arguments.get("class_fqn");
    
    try {
        return decompilationService.getClassOutline(jarPath, classFqn);
    } catch (SecurityException e) {
        return "ОШИБКА БЕЗОПАСНОСТИ: " + e.getMessage();
    } catch (Exception e) {
        return "ОШИБКА: " + e.getMessage();
    }
}
```

## Критерии приёмки (Acceptance Criteria)

- [ ] Создан класс `PathValidator.java`
- [ ] Обновлён `DecompilationService.java` с валидацией
- [ ] Обновлены инструменты с обработкой `SecurityException`
- [ ] Написаны тесты на валидацию путей
- [ ] Все существующие тесты проходят: `mvn test`
- [ ] Сборка успешна: `mvn clean package`

## TDD Цикл

### 🔴 RED — Тесты

> Отмечайте чек-боксы сразу после выполнения каждого пункта!

- [ ] Создан тест `PathValidatorTest.java`
- [ ] Написан тест `givenValidPathWhenValidateThenReturnsNormalizedPath()`
- [ ] Написан тест `givenPathTraversalWhenValidateThenThrowsSecurityException()`
- [ ] Написан тест `givenPathOutsideM2WhenValidateThenThrowsSecurityException()`
- [ ] Написан тест `givenNullPathWhenValidateThenThrowsSecurityException()`
- [ ] Написан тест `givenEmptyPathWhenValidateThenThrowsSecurityException()`
- [ ] Написан тест `givenNonJarFileWhenValidateThenThrowsSecurityException()`
- [ ] Тесты компилируются и падают (класс ещё не существует)

**Пример теста:**

```java
package ru.mirent.security;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class PathValidatorTest {
    
    @Test
    void givenValidPathWhenValidateThenReturnsNormalizedPath() {
        String validPath = System.getProperty("user.home") + 
            "/.m2/repository/com/example/test.jar";
        
        // Создаём тестовый файл
        Path testFile = createTestFile(validPath);
        
        Path result = PathValidator.validateJarPath(testFile.toString());
        
        assertNotNull(result);
        assertTrue(result.endsWith("test.jar"));
    }
    
    @Test
    void givenPathTraversalWhenValidateThenThrowsSecurityException() {
        String maliciousPath = "../../../etc/passwd";
        
        assertThrows(SecurityException.class, () -> {
            PathValidator.validateJarPath(maliciousPath);
        });
    }
    
    @Test
    void givenAbsoluteSystemPathWhenValidateThenThrowsSecurityException() {
        String systemPath = "/etc/shadow";
        
        assertThrows(SecurityException.class, () -> {
            PathValidator.validateJarPath(systemPath);
        });
    }
    
    @Test
    void givenNullPathWhenValidateThenThrowsSecurityException() {
        assertThrows(SecurityException.class, () -> {
            PathValidator.validateJarPath(null);
        });
    }
    
    @Test
    void givenEmptyPathWhenValidateThenThrowsSecurityException() {
        assertThrows(SecurityException.class, () -> {
            PathValidator.validateJarPath("");
        });
    }
    
    @Test
    void givenNonJarFileWhenValidateThenThrowsSecurityException() {
        String nonJar = System.getProperty("user.home") + 
            "/.m2/repository/com/example/test.class";
        
        assertThrows(SecurityException.class, () -> {
            PathValidator.validateJarPath(nonJar);
        });
    }
    
    @Test
    void givenPathWithDotDotInsideM2WhenValidateThenReturnsNormalizedPath() {
        // Путь с ../ внутри M2_REPO должен работать после нормализации
        String validPath = System.getProperty("user.home") + 
            "/.m2/repository/com/example/../example/test.jar";
        
        Path testFile = createTestFile(validPath);
        
        Path result = PathValidator.validateJarPath(testFile.toString());
        
        assertNotNull(result);
        assertTrue(result.endsWith("test.jar"));
    }
    
    @Test
    void givenIsValidJarPathWithValidPathThenReturnsTrue() {
        String validPath = System.getProperty("user.home") + 
            "/.m2/repository/com/example/test.jar";
        
        Path testFile = createTestFile(validPath);
        
        assertTrue(PathValidator.isValidJarPath(testFile.toString()));
    }
    
    @Test
    void givenIsValidJarPathWithInvalidPathThenReturnsFalse() {
        assertFalse(PathValidator.isValidJarPath("/etc/passwd"));
    }
    
    // Вспомогательный метод для создания тестового файла
    private Path createTestFile(String path) {
        try {
            Path testPath = Paths.get(path);
            java.nio.file.Files.createDirectories(testPath.getParent());
            java.nio.file.Files.createFile(testPath);
            return testPath;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
```

### 🟢 GREEN — Реализация

> Отмечайте чек-боксы сразу после выполнения каждого пункта!

- [ ] Создан `PathValidator.java`
- [ ] Обновлён `DecompilationService.java`
- [ ] Обновлены инструменты
- [ ] Все тесты проходят: `mvn test`

### 🔵 REFACTOR — Рефакторинг

> Отмечайте чек-боксы сразу после выполнения каждого пункта!

- [ ] Устранено дублирование кода валидации
- [ ] Добавлены JavaDoc к публичным методам
- [ ] Все тесты проходят после рефакторинга
- [ ] Сборка успешна: `mvn clean package`

## Работа с существующим кодом (если применимо)

- [ ] Написан characterization test для текущего поведения `ensureDecompiled()`
- [ ] Тест проходит (фиксация поведения)
- [ ] Проверена регрессия после добавления валидации

## Чек-лист завершения

- [ ] Все тесты зелёные
- [ ] Сборка успешна
- [ ] Код соответствует стандартам проекта
- [ ] Изменения закоммичены

## Статус

| Поле | Значение |
|------|----------|
| Дата создания: | 2026-03-26 |
| Дата начала: | 2026-03-26 |
| Дата завершения: | |
| Статус: | 📋 Pending |

## Заметки

- Зависит от TASK-005 (сервисы)
- В следующей задаче (TASK-007) будет добавлена валидация FQN класса
