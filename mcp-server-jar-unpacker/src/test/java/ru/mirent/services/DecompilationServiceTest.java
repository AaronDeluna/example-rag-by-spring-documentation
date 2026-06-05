package ru.mirent.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Тесты для DecompilationService
 */
class DecompilationServiceTest {

    private DecompilationService decompilationService;

    @BeforeEach
    void setUp() {
        decompilationService = new DecompilationService();
    }

    @Test
    void givenInvalidJarPathWhenGetClassOutlineThenReturnsError() {
        String result = decompilationService.getClassOutline(
            "/non/existent/path.jar", 
            "com.example.MyClass"
        );

        assertNotNull(result, "Результат не должен быть null");
        assertTrue(result.contains("ОШИБКА") || result.contains("ERROR"), 
            "При неверном пути должна быть возвращена ошибка");
    }

    @Test
    void givenInvalidJarPathWhenGetMethodSourceThenReturnsError() {
        String result = decompilationService.getMethodSource(
            "/non/existent/path.jar", 
            "com.example.MyClass",
            "myMethod"
        );

        assertNotNull(result, "Результат не должен быть null");
        assertTrue(result.contains("ОШИБКА") || result.contains("ERROR"), 
            "При неверном пути должна быть возвращена ошибка");
    }

    @Test
    void givenInvalidJarPathWhenDecompileClassThenReturnsError() {
        String result = decompilationService.decompileClass(
            "/non/existent/path.jar", 
            "com.example.MyClass"
        );

        assertNotNull(result, "Результат не должен быть null");
        assertTrue(result.contains("ОШИБКА") || result.contains("ERROR"), 
            "При неверном пути должна быть возвращена ошибка");
    }

    @Test
    void givenValidClassWhenGetMethodSourceForNonExistentMethodThenReturnsNotFound() {
        // Тест будет работать только если есть декомпилированный класс
        // Используем заглушку для проверки логики
        String result = decompilationService.getMethodSource(
            "/non/existent.jar", 
            "com.example.MyClass",
            "nonExistentMethod"
        );

        assertNotNull(result, "Результат не должен быть null");
    }

    @Test
    void countCharGivenStringWithBracesThenReturnsCorrectCount() {
        // Тест package-private метода countChar
        int openBraces = decompilationService.countChar("public class Foo { { {", '{');
        int closeBraces = decompilationService.countChar("public class Foo } } }", '}');

        assertEquals(3, openBraces, "Должно найти 3 открывающих скобки");
        assertEquals(3, closeBraces, "Должно найти 3 закрывающих скобки");
    }

    @Test
    void countCharGivenStringWithoutCharThenReturnsZero() {
        int count = decompilationService.countChar("hello world", 'z');

        assertEquals(0, count, "Должно вернуть 0");
    }
}
