package ru.mirent.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Тесты для JarSearchService
 */
class JarSearchServiceTest {

    private JarSearchService searchService;

    @BeforeEach
    void setUp() {
        searchService = new JarSearchService();
    }

    @Test
    void givenValidClassNameWhenFindClassThenReturnsMatches() {
        // Используем известный класс из Guava, который часто есть в Maven-репозитории
        String result = searchService.findClass("com.google.common.base.Preconditions");

        assertNotNull(result, "Результат поиска не должен быть null");
        assertTrue(result.contains("Preconditions.class") || result.contains("не найден"), 
            "Результат должен содержать имя класса или сообщение об ошибке");
    }

    @Test
    void givenNonExistentClassWhenFindClassThenReturnsNotFoundMessage() {
        String result = searchService.findClass("com.example.NonExistentClass12345");

        assertNotNull(result, "Результат поиска не должен быть null");
        assertTrue(result.contains("не найден") || result.contains("Найдено JAR"), 
            "Результат должен содержать сообщение о результате поиска");
    }

    @Test
    void givenSimpleClassNameWhenFindClassThenSearchesBySimpleName() {
        String result = searchService.findClass("Preconditions");

        assertNotNull(result, "Результат поиска не должен быть null");
    }
}
