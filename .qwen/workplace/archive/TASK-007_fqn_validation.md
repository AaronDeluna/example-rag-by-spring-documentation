# TASK-007: Валидация FQN класса: защита от инъекций

> **⚠️ Важно:** Этот файл должен быть создан **до начала** любой работы над задачей.
> Отмечайте чек-боксы `[x]` **сразу после выполнения** каждого шага для возможности продолжения с места прерывания.

## Описание

Добавить валидацию полного имени класса (FQN — Fully Qualified Name) для защиты от инъекций и некорректных путей.

**Текущая проблема:** Пользователь может передать:
- Путь с `/` или `\`: `com/example/Class` вместо `com.example.Class`
- Пустую строку или `null`
- Имя с недопустимыми символами: `com.example.Class; rm -rf /`
- Путь с `..`: `com.example..Class`

**Цель:** Валидировать FQN по правилам Java: только буквы, цифры, точка, `$` (вложенные классы).

## Требуемые изменения

### 1. Обновить класс `PathValidator.java` или создать `ClassNameValidator.java`

**Путь:** `src/main/java/ru/mirent/security/ClassNameValidator.java`

```java
package ru.mirent.security;

import java.util.regex.Pattern;

/**
 * Валидатор имён Java-классов (FQN — Fully Qualified Name)
 */
public class ClassNameValidator {
    
    // Разрешены: буквы, цифры, точка, $ (вложенные классы), _ (допустимо в Java)
    // Запрещены: /, \, пробелы, специальные символы
    private static final Pattern FQN_PATTERN = Pattern.compile(
        "^[a-zA-Z_][a-zA-Z0-9_$]*(\\.[a-zA-Z_][a-zA-Z0-9_$]*)*$"
    );
    
    /**
     * Проверить полное имя класса (FQN)
     * @param fqn полное имя класса, например "com.example.MyClass"
     * @return true если имя корректно
     */
    public static boolean isValidFQN(String fqn) {
        if (fqn == null || fqn.isEmpty()) {
            return false;
        }
        
        // Проверка на наличие недопустимых последовательностей
        if (fqn.contains("..")) {
            return false;
        }
        
        if (fqn.contains("/") || fqn.contains("\\")) {
            return false;
        }
        
        if (fqn.contains(" ")) {
            return false;
        }
        
        // Проверка по regex
        return FQN_PATTERN.matcher(fqn).matches();
    }
    
    /**
     * Проверить и выбросить исключение при ошибке
     * @param fqn полное имя класса
     * @throws IllegalArgumentException если имя некорректно
     */
    public static void validateFQN(String fqn) {
        if (fqn == null || fqn.isEmpty()) {
            throw new IllegalArgumentException(
                "Имя класса не может быть пустым"
            );
        }
        
        if (fqn.contains("..")) {
            throw new IllegalArgumentException(
                "Недопустимая последовательность '..' в имени класса: " + fqn
            );
        }
        
        if (fqn.contains("/") || fqn.contains("\\")) {
            throw new IllegalArgumentException(
                "Имя класса должно использовать точку (.) вместо разделителей пути: " + fqn
            );
        }
        
        if (fqn.contains(" ")) {
            throw new IllegalArgumentException(
                "Имя класса не должно содержать пробелы: " + fqn
            );
        }
        
        if (!FQN_PATTERN.matcher(fqn).matches()) {
            throw new IllegalArgumentException(
                "Недопустимые символы в имени класса. Разрешены только буквы, цифры, " +
                "точка (.), подчёркивание (_) и доллар ($): " + fqn
            );
        }
    }
    
    /**
     * Проверить простое имя класса (без пакета)
     * @param simpleName простое имя, например "MyClass"
     * @return true если имя корректно
     */
    public static boolean isValidSimpleClassName(String simpleName) {
        if (simpleName == null || simpleName.isEmpty()) {
            return false;
        }
        
        // Простое имя не должно содержать точку
        if (simpleName.contains(".")) {
            return false;
        }
        
        return FQN_PATTERN.matcher(simpleName).matches();
    }
}
```

### 2. Обновить `JarSearchService.java`

Добавить валидацию в метод `findClass()`:

```java
public String findClass(String className) {
    // Валидация имени класса
    ClassNameValidator.validateFQN(className);
    
    String simple = className.contains(".") ? 
        className.substring(className.lastIndexOf('.') + 1) : className;
    // ... остальной код
}
```

### 3. Обновить `DecompilationService.java`

Добавить валидацию в публичные методы:

```java
public String getClassOutline(String jarPath, String className) {
    // Валидация FQN
    ClassNameValidator.validateFQN(className);
    
    try {
        // ... остальной код
    }
}

public String getMethodSource(String jarPath, String className, String methodName) {
    ClassNameValidator.validateFQN(className);
    ClassNameValidator.validateFQN(methodName);
    // ... остальной код
}

public String decompileClass(String jarPath, String classFqn) {
    ClassNameValidator.validateFQN(classFqn);
    // ... остальной код
}
```

### 4. Обновить обработку ошибок в инструментах

Добавить обработку `IllegalArgumentException`:

```java
@Override
public Object execute(Map<String, Object> arguments) {
    String jarPath = (String) arguments.get("jar_path");
    String classFqn = (String) arguments.get("class_fqn");
    
    try {
        return decompilationService.getClassOutline(jarPath, classFqn);
    } catch (IllegalArgumentException e) {
        return "ОШИБКА ВАЛИДАЦИИ: " + e.getMessage();
    } catch (SecurityException e) {
        return "ОШИБКА БЕЗОПАСНОСТИ: " + e.getMessage();
    } catch (Exception e) {
        return "ОШИБКА: " + e.getMessage();
    }
}
```

## Критерии приёмки (Acceptance Criteria)

- [ ] Создан класс `ClassNameValidator.java`
- [ ] Обновлён `JarSearchService.java` с валидацией
- [ ] Обновлён `DecompilationService.java` с валидацией
- [ ] Обновлены инструменты с обработкой `IllegalArgumentException`
- [ ] Написаны тесты на валидацию FQN
- [ ] Все существующие тесты проходят: `mvn test`
- [ ] Сборка успешна: `mvn clean package`

## TDD Цикл

### 🔴 RED — Тесты

> Отмечайте чек-боксы сразу после выполнения каждого пункта!

- [ ] Создан тест `ClassNameValidatorTest.java`
- [ ] Написан тест `givenValidFQNWhenValidateThenReturnsTrue()`
- [ ] Написан тест `givenFQNWithSlashWhenValidateThenThrowsException()`
- [ ] Написан тест `givenFQNWithDoubleDotWhenValidateThenThrowsException()`
- [ ] Написан тест `givenFQNWithSpaceWhenValidateThenThrowsException()`
- [ ] Написан тест `givenNullFQNWhenValidateThenThrowsException()`
- [ ] Написан тест `givenEmptyFQNWhenValidateThenThrowsException()`
- [ ] Написан тест `givenValidNestedClassWhenValidateThenReturnsTrue()`
- [ ] Написан тест `givenSimpleClassNameWhenValidateSimpleThenReturnsTrue()`
- [ ] Тесты компилируются и падают (класс ещё не существует)

**Пример теста:**

```java
package ru.mirent.security;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class ClassNameValidatorTest {
    
    @Test
    void givenValidFQNWhenValidateThenReturnsTrue() {
        assertTrue(ClassNameValidator.isValidFQN("com.example.MyClass"));
        assertTrue(ClassNameValidator.isValidFQN("java.util.List"));
        assertTrue(ClassNameValidator.isValidFQN("org.springframework.boot.SpringApplication"));
    }
    
    @Test
    void givenValidFQNWithDollarWhenValidateThenReturnsTrue() {
        assertTrue(ClassNameValidator.isValidFQN("com.example.Outer$Inner"));
        assertTrue(ClassNameValidator.isValidFQN("java.util.Map$Entry"));
    }
    
    @Test
    void givenValidFQNWithUnderscoreWhenValidateThenReturnsTrue() {
        assertTrue(ClassNameValidator.isValidFQN("com.example.My_Class"));
        assertTrue(ClassNameValidator.isValidFQN("org.test.My_1Class"));
    }
    
    @ParameterizedTest
    @ValueSource(strings = {
        "com/example/Class",
        "com\\example\\Class",
        "com.example.Class;",
        "com.example.Class$var",
        "com.example.Class rm -rf /"
    })
    void givenFQNWithInvalidCharsWhenValidateThenThrowsException(String invalidFQN) {
        assertThrows(IllegalArgumentException.class, () -> {
            ClassNameValidator.validateFQN(invalidFQN);
        });
    }
    
    @Test
    void givenFQNWithSlashWhenValidateThenThrowsException() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            ClassNameValidator.validateFQN("com/example/Class");
        });
        assertTrue(ex.getMessage().contains("разделителей пути"));
    }
    
    @Test
    void givenFQNWithDoubleDotWhenValidateThenThrowsException() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            ClassNameValidator.validateFQN("com.example..Class");
        });
        assertTrue(ex.getMessage().contains("'..'"));
    }
    
    @Test
    void givenFQNWithSpaceWhenValidateThenThrowsException() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            ClassNameValidator.validateFQN("com.example.My Class");
        });
        assertTrue(ex.getMessage().contains("пробелы"));
    }
    
    @Test
    void givenNullFQNWhenValidateThenThrowsException() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            ClassNameValidator.validateFQN(null);
        });
        assertTrue(ex.getMessage().contains("пустым"));
    }
    
    @Test
    void givenEmptyFQNWhenValidateThenThrowsException() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            ClassNameValidator.validateFQN("");
        });
        assertTrue(ex.getMessage().contains("пустым"));
    }
    
    @Test
    void givenValidNestedClassWhenValidateThenReturnsTrue() {
        assertTrue(ClassNameValidator.isValidFQN("com.example.Outer.Inner"));
    }
    
    @Test
    void givenSimpleClassNameWhenValidateSimpleThenReturnsTrue() {
        assertTrue(ClassNameValidator.isValidSimpleClassName("MyClass"));
        assertTrue(ClassNameValidator.isValidSimpleClassName("My_Class"));
        assertTrue(ClassNameValidator.isValidSimpleClassName("MyClass123"));
    }
    
    @Test
    void givenSimpleClassWithDotWhenValidateSimpleThenReturnsFalse() {
        assertFalse(ClassNameValidator.isValidSimpleClassName("com.example.MyClass"));
    }
    
    @Test
    void givenValidFQNWithNumbersWhenValidateThenReturnsTrue() {
        assertTrue(ClassNameValidator.isValidFQN("com.example.MyClass123"));
        assertTrue(ClassNameValidator.isValidFQN("org.test.Class2Impl"));
    }
}
```

### 🟢 GREEN — Реализация

> Отмечайте чек-боксы сразу после выполнения каждого пункта!

- [ ] Создан `ClassNameValidator.java`
- [ ] Обновлён `JarSearchService.java`
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

- [ ] Написан characterization test для текущего поведения `findClass()`
- [ ] Написан characterization test для текущего поведения `getClassOutline()`
- [ ] Тесты проходят (фиксация поведения)
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

- Зависит от TASK-006 (валидация путей)
- Используется `Pattern` для валидации (стандартная Java)
- В следующей задаче (TASK-008) будет добавлено умное кэширование с TTL
