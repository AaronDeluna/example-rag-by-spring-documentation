package com.example.tests;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Базовый тест-заглушка проекта.
 * <p>
 * Даёт агенту готовую точку входа в {@code src/test/java} и активирует path-gated скилл
 * {@code generate-java-selenide-test} (его frontmatter: {@code paths: src/test/java/**\/*.java}).
 * Сгенерированные скиллом UI-тесты кладутся рядом, в этот же пакет.
 * </p>
 */
class BaseSeleniumTest {

    @Test
    void environmentIsReady() {
        assertTrue(true, "Проектная структура готова к генерации UI-тестов");
    }
}
