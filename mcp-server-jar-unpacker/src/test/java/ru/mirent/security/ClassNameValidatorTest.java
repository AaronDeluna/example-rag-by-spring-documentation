package ru.mirent.security;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Тесты для ClassNameValidator
 */
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

    @Test
    void givenIsValidFQNWithValidPathThenReturnsTrue() {
        assertTrue(ClassNameValidator.isValidFQN("com.google.common.base.Preconditions"));
        assertTrue(ClassNameValidator.isValidFQN("java.lang.String"));
    }

    @Test
    void givenIsValidFQNWithInvalidPathThenReturnsFalse() {
        assertFalse(ClassNameValidator.isValidFQN("com/example/Class"));
        assertFalse(ClassNameValidator.isValidFQN(""));
        assertFalse(ClassNameValidator.isValidFQN(null));
    }

    @Test
    void givenFQNStartingWithNumberWhenValidateThenReturnsFalse() {
        assertFalse(ClassNameValidator.isValidFQN("1com.example.Class"));
    }

    @Test
    void givenFQNStartingWithDotWhenValidateThenReturnsFalse() {
        assertFalse(ClassNameValidator.isValidFQN(".com.example.Class"));
    }

    @Test
    void givenFQNEndingWithDotWhenValidateThenReturnsFalse() {
        assertFalse(ClassNameValidator.isValidFQN("com.example."));
    }

    @Test
    void givenOnlyDotWhenValidateThenReturnsFalse() {
        assertFalse(ClassNameValidator.isValidFQN("."));
    }

    @Test
    void givenFQNWithConsecutiveDotsWhenValidateThenReturnsFalse() {
        assertFalse(ClassNameValidator.isValidFQN("com..example.Class"));
        assertFalse(ClassNameValidator.isValidFQN("com.example...Class"));
    }
}
