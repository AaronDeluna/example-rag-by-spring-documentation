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
 * <p>
 * Кэш автоматически инвалидируется через 5 минут (TTL) или при изменении репозитория
 */
public class JarCacheService {

    private static final Path DEFAULT_M2_REPO = Paths.get(
        System.getProperty("user.home"), ".m2", "repository"
    );

    private static final long TTL_MILLIS = 5 * 60 * 1000; // 5 минут

    private final Path m2Repo;
    private volatile List<Path> cachedJars;
    private volatile long cacheTime;
    private volatile long repoMtime; // Modification time репозитория

    /**
     * Конструктор по умолчанию.
     * Использует ~/.m2/repository в качестве Maven-репозитория
     */
    public JarCacheService() {
        this.m2Repo = DEFAULT_M2_REPO;
    }

    /**
     * Конструктор для тестирования с кастомным путём репозитория.
     * Package-private для использования только в тестах
     *
     * @param customM2Repo тестовый путь к репозиторию
     */
    JarCacheService(Path customM2Repo) {
        this.m2Repo = customM2Repo;
    }

    /**
     * Вернуть список JAR-файлов в ~/.m2/repository.
     * Кэшируется на TTL_MILLIS миллисекунд или до изменения репозитория
     */
    public List<Path> getJars() {
        if (isCacheExpired() || isRepoModified()) {
            synchronized (this) {
                if (isCacheExpired() || isRepoModified()) {
                    cachedJars = scanMavenRepo();
                    cacheTime = System.currentTimeMillis();
                    repoMtime = getRepoMtime();
                }
            }
        }
        return cachedJars;
    }

    /**
     * Проверить, истёк ли срок жизни кэша
     */
    private boolean isCacheExpired() {
        return cachedJars == null ||
               System.currentTimeMillis() - cacheTime > TTL_MILLIS;
    }

    /**
     * Проверить, был ли изменён репозиторий с момента последнего сканирования
     */
    private boolean isRepoModified() {
        if (repoMtime == 0) {
            return true; // Первое сканирование
        }
        long currentMtime = getRepoMtime();
        return currentMtime > repoMtime;
    }

    /**
     * Получить время последней модификации репозитория
     */
    private long getRepoMtime() {
        try {
            return Files.getLastModifiedTime(m2Repo).toMillis();
        } catch (IOException e) {
            return System.currentTimeMillis();
        }
    }

    /**
     * Отсканировать Maven-репозиторий и вернуть список JAR
     */
    private List<Path> scanMavenRepo() {
        try {
            return Files.walk(m2Repo)
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
        repoMtime = 0;
    }

    /**
     * Вернуть путь к Maven-репозиторию
     */
    public Path getM2RepoPath() {
        return m2Repo;
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

    /**
     * Вернуть время последней модификации репозитория
     * <p>
     * Используется для отслеживания изменений в ~/.m2/repository
     *
     * @return время последней модификации в миллисекундах или 0, если кэш не инициализирован
     */
    public long getRepoLastModified() {
        return repoMtime;
    }
}
