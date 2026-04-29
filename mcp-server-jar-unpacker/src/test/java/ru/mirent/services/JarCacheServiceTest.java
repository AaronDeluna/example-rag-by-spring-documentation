package ru.mirent.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Тесты для JarCacheService
 */
class JarCacheServiceTest {

    @TempDir
    Path tempRepo;

    private JarCacheService cacheService;

    @BeforeEach
    void setUp() {
        cacheService = new JarCacheService();
        cacheService.invalidateCache(); // Сброс перед тестом
    }

    /**
     * Создать сервис для тестирования с кастомным путём репозитория
     */
    private JarCacheService createServiceWithRepo(Path repo) {
        return new JarCacheService(repo);
    }

    @Test
    void givenEmptyCacheWhenGetJarsThenScansRepo() {
        List<Path> jars = cacheService.getJars();

        assertNotNull(jars);
        // Кэш должен быть заполнен
        assertTrue(cacheService.getCacheSize() >= 0);
    }

    @Test
    void givenCacheWhenTTLNotExpiredThenReturnsSameList() throws Exception {
        List<Path> firstCall = cacheService.getJars();

        // Ждём 1 секунду (меньше TTL)
        Thread.sleep(1000);

        List<Path> secondCall = cacheService.getJars();

        assertSame(firstCall, secondCall, "Кэш должен возвращать тот же список до истечения TTL");
    }

    @Test
    void givenCacheWhenTTLExpiredThenReturnsNewList() throws Exception {
        List<Path> firstCall = cacheService.getJars();

        // Принудительно устанавливаем старое время кэша через рефлексию
        Field cacheTimeField = JarCacheService.class.getDeclaredField("cacheTime");
        cacheTimeField.setAccessible(true);

        // Устанавливаем время 6 минут назад (больше TTL 5 мин)
        long oldTime = System.currentTimeMillis() - (6 * 60 * 1000);
        cacheTimeField.set(cacheService, oldTime);

        // Даём потоку захватить synchronized
        Thread.sleep(100);

        List<Path> secondCall = cacheService.getJars();

        assertNotSame(firstCall, secondCall, "После истечения TTL должен возвращаться новый список");
    }

    @Test
    void givenCacheWhenGetCacheRemainingSecondsThenReturnsCorrectValue() {
        cacheService.getJars(); // Инициализация кэша

        long remaining = cacheService.getCacheRemainingSeconds();

        assertTrue(remaining > 0, "Оставшееся время должно быть больше 0");
        assertTrue(remaining <= 300, "Оставшееся время должно быть не больше 5 минут");
    }

    @Test
    void givenEmptyCacheWhenGetCacheRemainingSecondsThenReturnsZero() {
        cacheService.invalidateCache();

        long remaining = cacheService.getCacheRemainingSeconds();

        assertEquals(0, remaining, "Для пустого кэша остаток должен быть 0");
    }

    @Test
    void givenCacheWhenGetCacheSizeThenReturnsCorrectSize() {
        List<Path> jars = cacheService.getJars();

        int size = cacheService.getCacheSize();

        assertEquals(jars.size(), size, "Размер кэша должен совпадать с размером списка");
    }

    @Test
    void givenEmptyCacheWhenGetCacheSizeThenReturnsZero() {
        cacheService.invalidateCache();

        int size = cacheService.getCacheSize();

        assertEquals(0, size, "Для пустого кэша размер должен быть 0");
    }

    @Test
    void givenInvalidateCacheWhenGetJarsThenReturnsNewList() throws Exception {
        List<Path> firstCall = cacheService.getJars();

        cacheService.invalidateCache();

        List<Path> secondCall = cacheService.getJars();

        assertNotSame(firstCall, secondCall, "После инвалидации должен возвращаться новый список");
    }

    @Test
    void givenGetM2RepoPathThenReturnsCorrectPath() {
        Path m2Path = cacheService.getM2RepoPath();

        assertTrue(m2Path.endsWith("repository"), "Путь должен заканчиваться на 'repository'");
        assertTrue(m2Path.startsWith(System.getProperty("user.home")),
            "Путь должен начинаться с домашней директории пользователя");
    }

    // =========================================
    // Тесты на проверку изменения репозитория
    // =========================================

    @Test
    void givenFirstCallWhenGetJarsThenInitializesRepoMtime() throws Exception {
        // Создаём сервис с тестовым репозиторием
        JarCacheService testService = createServiceWithRepo(tempRepo);
        testService.invalidateCache();

        // Создаём JAR-файл в репозитории
        Path jar = tempRepo.resolve("test.jar");
        Files.createFile(jar);

        // Первое сканирование
        testService.getJars();

        // Проверка, что mtime репозитория инициализировано
        long repoMtime = testService.getRepoLastModified();
        assertTrue(repoMtime > 0, "Время модификации репозитория должно быть инициализировано");
    }

    @Test
    void givenRepoModifiedWhenGetJarsThenReturnsNewList() throws Exception {
        // Создаём сервис с тестовым репозиторием
        JarCacheService testService = createServiceWithRepo(tempRepo);
        testService.invalidateCache();

        // Создаём первый JAR-файл
        Path firstJar = tempRepo.resolve("first-artifact.jar");
        Files.createFile(firstJar);

        // Первое сканирование
        List<Path> firstCall = testService.getJars();
        int firstSize = firstCall.size();

        // Создаём новый JAR-файл в репозитории
        Path newJar = tempRepo.resolve("new-artifact.jar");
        Files.createFile(newJar);

        // Ждём немного для обновления mtime (на некоторых файловых системах mtime может иметь низкую точность)
        Thread.sleep(100);

        // Кэш должен обновиться из-за изменения репозитория
        List<Path> secondCall = testService.getJars();

        assertNotSame(firstCall, secondCall, "После изменения репозитория должен возвращаться новый список");
        assertEquals(firstSize + 1, secondCall.size(), "Размер должен увеличиться на 1");
    }

    @Test
    void givenRepoNotModifiedWhenGetJarsThenReturnsSameList() throws Exception {
        // Создаём сервис с тестовым репозиторием
        JarCacheService testService = createServiceWithRepo(tempRepo);
        testService.invalidateCache();

        // Создаём JAR-файл
        Path jar = tempRepo.resolve("test-artifact.jar");
        Files.createFile(jar);

        // Первое сканирование
        List<Path> firstCall = testService.getJars();

        // Ждём немного (но меньше TTL)
        Thread.sleep(100);

        // Не создаём новых файлов

        List<Path> secondCall = testService.getJars();

        assertSame(firstCall, secondCall, "Если репозиторий не изменён, должен возвращаться тот же список");
    }

    @Test
    void givenInvalidateCacheWhenGetJarsThenResetsRepoMtime() throws Exception {
        // Создаём сервис с тестовым репозиторием
        JarCacheService testService = createServiceWithRepo(tempRepo);
        testService.invalidateCache();

        // Создаём JAR-файл
        Path jar = tempRepo.resolve("test.jar");
        Files.createFile(jar);

        // Первое сканирование
        testService.getJars();
        long oldMtime = testService.getRepoLastModified();

        // Инвалидация кэша
        testService.invalidateCache();

        // Проверка, что mtime сброшено
        assertEquals(0, testService.getRepoLastModified(), "После инвалидации кэша mtime должен быть сброшен в 0");
    }

    @Test
    void givenRepoModifiedWithinTTLWhenGetJarsThenInvalidatesCache() throws Exception {
        // Создаём сервис с тестовым репозиторием
        JarCacheService testService = createServiceWithRepo(tempRepo);
        testService.invalidateCache();

        // Создаём первый JAR-файл
        Path firstJar = tempRepo.resolve("first.jar");
        Files.createFile(firstJar);

        // Первое сканирование
        List<Path> firstCall = testService.getJars();

        // Ждём немного (меньше TTL)
        Thread.sleep(50);

        // Создаём новый JAR-файл
        Path secondJar = tempRepo.resolve("second.jar");
        Files.createFile(secondJar);
        Thread.sleep(100);

        // Кэш должен обновиться, даже если TTL не истёк
        List<Path> secondCall = testService.getJars();

        assertNotSame(firstCall, secondCall, "Кэш должен обновиться при изменении репозитория, даже если TTL не истёк");
        assertEquals(firstCall.size() + 1, secondCall.size());
    }
}
