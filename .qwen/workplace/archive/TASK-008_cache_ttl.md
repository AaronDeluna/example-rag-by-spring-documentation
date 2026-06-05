# TASK-008: Умное кэширование JAR с TTL

> **⚠️ Важно:** Этот файл должен быть создан **до начала** любой работы над задачей.
> Отмечайте чек-боксы `[x]` **сразу после выполнения** каждого шага для возможности продолжения с места прерывания.

## Описание

Добавить кэширование списка JAR-файлов с TTL (Time To Live) для автоматической инвалидации по таймеру.

**Текущая проблема:** Кэш в `JarCacheService` инвалидируется только после каждого поиска (`findClassInM2()`), что неэффективно. При частых запросах кэш постоянно сбрасывается.

**Цель:** Кэш должен автоматически инвалидироваться через 5 минут (TTL), а не после каждого запроса.

## Требуемые изменения

### 1. Обновить класс `JarCacheService.java`

Добавить TTL и проверку времени жизни кэша:

```java
package ru.mirent.services;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Сервис кэширования списка JAR-файлов в Maven-репозитории с TTL
 */
public class JarCacheService {
    
    private static final Path M2_REPO = Paths.get(
        System.getProperty("user.home"), ".m2", "repository"
    );
    
    private static final long TTL_MILLIS = 5 * 60 * 1000; // 5 минут
    
    private volatile List<Path> cachedJars;
    private volatile long cacheTime;
    
    /**
     * Вернуть список JAR-файлов в ~/.m2/repository
     * Кэшируется на TTL_MILLIS миллисекунд
     */
    public List<Path> getJars() {
        if (isCacheExpired()) {
            synchronized (this) {
                if (isCacheExpired()) {
                    cachedJars = scanMavenRepo();
                    cacheTime = System.currentTimeMillis();
                }
            }
        }
        return cachedJars;
    }
    
    private boolean isCacheExpired() {
        return cachedJars == null || 
               System.currentTimeMillis() - cacheTime > TTL_MILLIS;
    }
    
    private List<Path> scanMavenRepo() {
        try {
            return Files.walk(M2_REPO)
                    .filter(p -> p.toString().endsWith(".jar"))
                    .filter(p -> !p.toString().endsWith("-sources.jar"))
                    .filter(p -> !p.toString().endsWith("-javadoc.jar"))
                    .collect(Collectors.toUnmodifiableList());
        } catch (IOException e) {
            return Collections.emptyList();
        }
    }
    
    /**
     * Сбросить кэш (для принудительного обновления)
     */
    public void invalidateCache() {
        cachedJars = null;
        cacheTime = 0;
    }
    
    /**
     * Вернуть путь к Maven-репозиторию
     */
    public Path getM2RepoPath() {
        return M2_REPO;
    }
    
    /**
     * Вернуть оставшееся время жизни кэша в секундах
     */
    public long getCacheRemainingSeconds() {
        if (cachedJars == null) {
            return 0;
        }
        long elapsed = System.currentTimeMillis() - cacheTime;
        long remaining = TTL_MILLIS - elapsed;
        return Math.max(0, remaining / 1000);
    }
    
    /**
     * Вернуть размер кэша (количество JAR)
     */
    public int getCacheSize() {
        return cachedJars != null ? cachedJars.size() : 0;
    }
}
```

### 2. Обновить `JarSearchService.java`

Удалить вызов `jarCacheService.invalidateCache()` после поиска:

```java
// Удалить эту строку из findClass():
// jarCacheService.invalidateCache();

// Кэш теперь инвалидируется только по TTL
```

### 3. Добавить метод для мониторинга кэша

Создать новый инструмент MCP (опционально):

```java
// В DefaultToolRegistry добавить инструмент cache_status
// Для отладки и мониторинга
```

## Критерии приёмки (Acceptance Criteria)

- [ ] Обновлён `JarCacheService.java` с TTL
- [ ] Удалена принудительная инвалидация из `JarSearchService`
- [ ] Написаны тесты на TTL-кэширование
- [ ] Все существующие тесты проходят: `mvn test`
- [ ] Сборка успешна: `mvn clean package`

## TDD Цикл

### 🔴 RED — Тесты

> Отмечайте чек-боксы сразу после выполнения каждого пункта!

- [ ] Обновлен тест `JarCacheServiceTest.java`
- [ ] Написан тест `givenCacheWhenTTLNotExpiredThenReturnsSameList()`
- [ ] Написан тест `givenCacheWhenTTLExpiredThenReturnsNewList()`
- [ ] Написан тест `givenCacheWhenGetCacheRemainingSecondsThenReturnsCorrectValue()`
- [ ] Написан тест `givenEmptyCacheWhenGetCacheSizeThenReturnsZero()`
- [ ] Написан тест `givenPopulatedCacheWhenGetCacheSizeThenReturnsCorrectSize()`
- [ ] Тесты компилируются и падают

**Пример теста:**

```java
package ru.mirent.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class JarCacheServiceTest {
    
    private JarCacheService cacheService;
    
    @BeforeEach
    void setUp() {
        cacheService = new JarCacheService();
        cacheService.invalidateCache(); // Сброс перед тестом
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
        
        assertSame(firstCall, secondCall);
    }
    
    @Test
    void givenCacheWhenTTLExpiredThenReturnsNewList() throws Exception {
        List<Path> firstCall = cacheService.getJars();
        
        // Принудительно устанавливаем старое время кэша
        // Используем рефлексию для доступа к private полю
        java.lang.reflect.Field cacheTimeField = 
            JarCacheService.class.getDeclaredField("cacheTime");
        cacheTimeField.setAccessible(true);
        
        // Устанавливаем время 6 минут назад (больше TTL 5 мин)
        long oldTime = System.currentTimeMillis() - (6 * 60 * 1000);
        cacheTimeField.set(cacheService, oldTime);
        
        // Даём потоку захватить synchronized
        Thread.sleep(100);
        
        List<Path> secondCall = cacheService.getJars();
        
        assertNotSame(firstCall, secondCall);
    }
    
    @Test
    void givenCacheWhenGetCacheRemainingSecondsThenReturnsCorrectValue() {
        cacheService.getJars(); // Инициализация кэша
        
        long remaining = cacheService.getCacheRemainingSeconds();
        
        assertTrue(remaining > 0);
        assertTrue(remaining <= 300); // Максимум 5 минут
    }
    
    @Test
    void givenEmptyCacheWhenGetCacheRemainingSecondsThenReturnsZero() {
        cacheService.invalidateCache();
        
        long remaining = cacheService.getCacheRemainingSeconds();
        
        assertEquals(0, remaining);
    }
    
    @Test
    void givenCacheWhenGetCacheSizeThenReturnsCorrectSize() {
        List<Path> jars = cacheService.getJars();
        
        int size = cacheService.getCacheSize();
        
        assertEquals(jars.size(), size);
    }
    
    @Test
    void givenEmptyCacheWhenGetCacheSizeThenReturnsZero() {
        cacheService.invalidateCache();
        
        int size = cacheService.getCacheSize();
        
        assertEquals(0, size);
    }
    
    @Test
    void givenInvalidateCacheWhenGetJarsThenReturnsNewList() throws Exception {
        List<Path> firstCall = cacheService.getJars();
        
        cacheService.invalidateCache();
        
        List<Path> secondCall = cacheService.getJars();
        
        assertNotSame(firstCall, secondCall);
    }
    
    @Test
    void givenGetM2RepoPathThenReturnsCorrectPath() {
        Path m2Path = cacheService.getM2RepoPath();
        
        assertTrue(m2Path.endsWith("repository"));
        assertTrue(m2Path.startsWith(System.getProperty("user.home")));
    }
}
```

### 🟢 GREEN — Реализация

> Отмечайте чек-боксы сразу после выполнения каждого пункта!

- [ ] Обновлён `JarCacheService.java` с TTL
- [ ] Удалена принудительная инвалидация из `JarSearchService`
- [ ] Все тесты проходят: `mvn test`

### 🔵 REFACTOR — Рефакторинг

> Отмечайте чек-боксы сразу после выполнения каждого пункта!

- [ ] Выделен метод `isCacheExpired()`
- [ ] Выделен метод `scanMavenRepo()`
- [ ] Добавлены JavaDoc к публичным методам
- [ ] Все тесты проходят после рефакторинга
- [ ] Сборка успешна: `mvn clean package`

## Работа с существующим кодом (если применимо)

- [ ] Написан characterization test для текущего поведения кэша
- [ ] Тест проходит (фиксация поведения)
- [ ] Проверена регрессия после изменений

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

- Зависит от TASK-005 (сервис `JarCacheService`)
- TTL = 5 минут (настраивается через константу)
- В следующей задаче (TASK-009) будет добавлена инвалидация при изменении ~/.m2
