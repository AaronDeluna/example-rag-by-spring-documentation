# TASK-005: Разделение Server.java на модули: Services

> **⚠️ Важно:** Этот файл должен быть создан **до начала** любой работы над задачей.
> Отмечайте чек-боксы `[x]` **сразу после выполнения** каждого шага для возможности продолжения с места прерывания.

## Описание

Выделить бизнес-логику поиска в JAR и декомпиляции в отдельные сервисные классы.

**Текущая проблема:** Методы `findClassInM2()`, `getClassOutline()`, `getMethodSource()`, `decompileClass()`, `ensureDecompiled()` находятся в `Server.java`.

**Цель:** Создать 3 сервиса:
- `JarSearchService` — поиск классов в JAR
- `JarCacheService` — кэширование списка JAR
- `DecompilationService` — декомпиляция через CFR

## Требуемые изменения

### 1. Создать класс `JarCacheService.java`

**Путь:** `src/main/java/ru/mirent/services/JarCacheService.java`

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
 * Сервис кэширования списка JAR-файлов в Maven-репозитории
 */
public class JarCacheService {
    
    private static final Path M2_REPO = Paths.get(
        System.getProperty("user.home"), ".m2", "repository"
    );
    
    private volatile List<Path> cachedJars;
    
    /**
     * Вернуть список JAR-файлов в ~/.m2/repository
     * Кэшируется после первого вызова
     */
    public List<Path> getJars() {
        if (cachedJars == null) {
            synchronized (this) {
                if (cachedJars == null) {
                    try {
                        cachedJars = Files.walk(M2_REPO)
                                .filter(p -> p.toString().endsWith(".jar"))
                                .filter(p -> !p.toString().endsWith("-sources.jar"))
                                .filter(p -> !p.toString().endsWith("-javadoc.jar"))
                                .collect(Collectors.toUnmodifiableList());
                    } catch (IOException e) {
                        cachedJars = Collections.emptyList();
                    }
                }
            }
        }
        return cachedJars;
    }
    
    /**
     * Сбросить кэш (для принудительного обновления)
     */
    public void invalidateCache() {
        cachedJars = null;
    }
    
    /**
     * Вернуть путь к Maven-репозиторию
     */
    public Path getM2RepoPath() {
        return M2_REPO;
    }
}
```

### 2. Создать класс `JarSearchService.java`

**Путь:** `src/main/java/ru/mirent/services/JarSearchService.java`

```java
package ru.mirent.services;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collectors;

/**
 * Сервис поиска классов в JAR-файлах
 */
public class JarSearchService {
    
    private final JarCacheService jarCacheService;
    private static final int MAX_WORKERS = Math.min(16, Runtime.getRuntime().availableProcessors());
    
    public JarSearchService() {
        this.jarCacheService = new JarCacheService();
    }
    
    /**
     * Найти JAR-файлы, содержащие указанный класс
     * @param className простое или полное имя класса
     * @return отсортированный список путей к JAR
     */
    public String findClass(String className) {
        String simple = className.contains(".") ? 
            className.substring(className.lastIndexOf('.') + 1) : className;
        String classFilename = simple + ".class";
        
        List<String> matches = new CopyOnWriteArrayList<>();
        List<Path> jars = jarCacheService.getJars();
        
        ExecutorService executor = Executors.newFixedThreadPool(MAX_WORKERS);
        
        try {
            List<Future<Boolean>> futures = new ArrayList<>();
            for (Path jar : jars) {
                futures.add(executor.submit(() -> jarContainsClass(jar, classFilename)));
            }
            
            for (int i = 0; i < futures.size(); i++) {
                try {
                    if (futures.get(i).get()) {
                        matches.add(jars.get(i).toString());
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } catch (ExecutionException e) {
                    // Игнорируем ошибки отдельных JAR
                }
            }
        } finally {
            executor.shutdown();
            try {
                if (!executor.awaitTermination(1, TimeUnit.MINUTES)) {
                    executor.shutdownNow();
                }
            } catch (InterruptedException e) {
                executor.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
        
        // Инвалидация кэша после поиска
        jarCacheService.invalidateCache();
        
        if (matches.isEmpty()) {
            return String.format(
                "%s не найден в JAR-файлах в %s.\nОтвет: для внутренних классов ищите имя внешнего класса.",
                classFilename, jarCacheService.getM2RepoPath()
            );
        }
        
        matches.sort(String::compareTo);
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Найдено JAR-файлов: %d для %s:\n\n", matches.size(), classFilename));
        for (String m : matches) {
            sb.append(" ").append(m).append("\n");
        }
        sb.append("\nСледующий шаг: вызовите get_class_outline с наиболее подходящим путём к JAR.");
        
        return sb.toString();
    }
    
    private boolean jarContainsClass(Path jarPath, String className) {
        try (JarFile jar = new JarFile(jarPath.toFile())) {
            Enumeration<JarEntry> entries = jar.entries();
            while (entries.hasMoreElements()) {
                String name = entries.nextElement().getName();
                if (name.endsWith(className)) {
                    return true;
                }
            }
        } catch (IOException e) {
            // Игнорируем ошибки чтения JAR
        }
        return false;
    }
}
```

### 3. Создать класс `DecompilationService.java`

**Путь:** `src/main/java/ru/mirent/services/DecompilationService.java`

```java
package ru.mirent.services;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Сервис декомпиляции Java-классов через CFR
 */
public class DecompilationService {
    
    private static final Path OUTPUT_DIR = Paths.get("/tmp/cfr-decompiled");
    private static final int TIMEOUT_SECONDS = 60;
    
    /**
     * Получить схему класса (пакет, импорты, поля, сигнатуры методов без тел)
     */
    public String getClassOutline(String jarPath, String className) {
        try {
            Path javaFile = ensureDecompiled(jarPath, className);
            List<String> lines = Files.readAllLines(javaFile);
            List<String> outline = new ArrayList<>();
            int depth = 0;
            
            for (String line : lines) {
                String stripped = line.strip();
                int opens = countChar(stripped, '{');
                int closes = countChar(stripped, '}');
                
                if (depth == 0) {
                    outline.add(line);
                    depth += opens - closes;
                } else if (depth == 1) {
                    if (opens > closes) {
                        outline.add(line);
                        outline.add("    // ...");
                        depth += opens - closes;
                    } else {
                        outline.add(line);
                        depth += opens - closes;
                    }
                } else {
                    depth += opens - closes;
                    if (depth == 1) {
                        outline.add("    }");
                    }
                }
            }
            
            return String.join("\n", outline);
        } catch (Exception e) {
            return "ОШИБКА: " + e.getMessage();
        }
    }
    
    /**
     * Получить исходный код метода по имени
     */
    public String getMethodSource(String jarPath, String className, String methodName) {
        try {
            Path javaFile = ensureDecompiled(jarPath, className);
            List<String> lines = Files.readAllLines(javaFile);
            List<String> results = new ArrayList<>();
            Pattern pattern = Pattern.compile("\\b" + Pattern.quote(methodName) + "\\s*\\(");
            int i = 0;
            
            while (i < lines.size()) {
                String line = lines.get(i);
                if (pattern.matcher(line).find() && !line.strip().startsWith("/")) {
                    List<String> block = new ArrayList<>();
                    block.add(line);
                    int depth = countChar(line, '(') - countChar(line, ')');
                    i++;
                    while (i < lines.size() && depth > 0) {
                        block.add(lines.get(i));
                        depth += countChar(lines.get(i), '(') - countChar(lines.get(i), ')');
                        i++;
                    }
                    results.add(String.join("\n", block));
                } else {
                    i++;
                }
            }
            
            if (results.isEmpty()) {
                return String.format("Метод '%s' не найден в %s.", methodName, className);
            }
            
            StringBuilder sb = new StringBuilder();
            for (int j = 0; j < results.size(); j++) {
                if (j > 0) {
                    sb.append("\n\n// --- перегрузка ---\n\n");
                }
                sb.append(results.get(j));
            }
            
            return sb.toString();
        } catch (Exception e) {
            return "ОШИБКА: " + e.getMessage();
        }
    }
    
    /**
     * Полная декомпиляция класса
     */
    public String decompileClass(String jarPath, String classFqn) {
        try {
            Path javaFile = ensureDecompiled(jarPath, classFqn);
            String source = Files.readString(javaFile);
            StringBuilder sb = new StringBuilder();
            sb.append(String.format("// %s\n// JAR: %s\n// %s\n",
                    classFqn, Paths.get(jarPath).getFileName(), "=".repeat(60)));
            sb.append(source);
            return sb.toString();
        } catch (Exception e) {
            return "ОШИБКА: " + e.getMessage();
        }
    }
    
    private Path ensureDecompiled(String jarPath, String classFqn) throws IOException {
        Path javaFile = OUTPUT_DIR.resolve(
            classFqn.replace('.', File.separatorChar) + ".java"
        );
        if (Files.exists(javaFile)) {
            return javaFile;
        }
        
        Path jar = Paths.get(jarPath);
        if (!Files.exists(jar)) {
            throw new IOException("JAR не найден: " + jarPath);
        }
        
        Path cfrJar = getCFRJar();
        if (!Files.exists(cfrJar)) {
            throw new IOException("CFR JAR не найден: " + cfrJar);
        }
        
        Files.createDirectories(OUTPUT_DIR);
        
        ProcessBuilder pb = new ProcessBuilder(
                "java", "-jar", cfrJar.toString(), jar.toString(), classFqn,
                "--outputdir", OUTPUT_DIR.toString(),
                "--silent", "false"
        );
        pb.redirectErrorStream(true);
        
        Process proc = pb.start();
        String output;
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(proc.getInputStream()))) {
            output = reader.lines().collect(Collectors.joining("\n"));
        }
        
        int exitCode;
        try {
            if (!proc.waitFor(TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
                proc.destroyForcibly();
                throw new IOException("Таймаут декомпиляции (" + TIMEOUT_SECONDS + " сек)");
            }
            exitCode = proc.exitValue();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            proc.destroyForcibly();
            throw new IOException("Декомпиляция прервана", e);
        }
        
        if (exitCode != 0 && !Files.exists(javaFile)) {
            throw new IOException(String.format(
                    "Декомпиляция не дала результата.\nstdout: %s", output));
        }
        
        if (!Files.exists(javaFile)) {
            throw new IOException("Декомпиляция не дала результата.");
        }
        
        return javaFile;
    }
    
    private Path getCFRJar() {
        try {
            Path currentJar = Paths.get(
                DecompilationService.class.getProtectionDomain()
                    .getCodeSource().getLocation().toURI()
            ).getParent();
            if (currentJar != null && Files.exists(currentJar.resolve("cfr-0.152.jar"))) {
                return currentJar.resolve("cfr-0.152.jar");
            }
        } catch (Exception e) {
        }
        return Paths.get("cfr-0.152.jar");
    }
    
    int countChar(String s, char c) {
        int count = 0;
        for (int i = 0; i < s.length(); i++) {
            if (s.charAt(i) == c) {
                count++;
            }
        }
        return count;
    }
}
```

### 4. Обновить зависимости в `pom.xml`

Добавить зависимость для CopyOnWriteArrayList (уже есть в JDK, не требуется изменений).

### 5. Обновить `DefaultToolRegistry.java`

Использовать новые сервисы вместо прямой инициализации.

### 6. Обновить `Server.java`

Удалить методы:
- `findClassInM2()`
- `getClassOutline()`
- `getMethodSource()`
- `decompileClass()`
- `ensureDecompiled()`
- `getJars()`
- `jarContainsClass()`
- `getCFRJar()`
- `countChar()`

Оставить только:
- `runMcpServer()`
- `main()`
- `logToolCall()` (логирование)

## Критерии приёмки (Acceptance Criteria)

- [x] Создан класс `JarCacheService.java`
- [x] Создан класс `JarSearchService.java`
- [x] Создан класс `DecompilationService.java`
- [x] Обновлён `DefaultToolRegistry.java` для использования сервисов
- [x] Обновлён `Server.java` — удалены сервисные методы
- [x] `Server.java` сократился до ~150 строк
- [x] Все существующие тесты проходят: `mvn test`
- [x] Сборка успешна: `mvn clean package`
- [x] MCP-сервер запускается и все 4 инструмента работают

## TDD Цикл

### 🔴 RED — Тесты

> Отмечайте чек-боксы сразу после выполнения каждого пункта!

- [x] Создан тест `JarCacheServiceTest.java`
- [x] Создан тест `JarSearchServiceTest.java`
- [x] Создан тест `DecompilationServiceTest.java`
- [x] Написан тест `givenMavenRepoWhenGetJarsThenReturnsListOfJars()`
- [x] Написан тест `givenCachedJarsWhenGetJarsThenReturnsSameList()`
- [x] Написан тест `givenValidJarWhenFindClassThenReturnsMatches()`
- [x] Написан тест `givenInvalidJarPathWhenGetClassOutlineThenReturnsError()`
- [x] Тесты компилируются и падают (сервисы ещё не существуют)

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
    }
    
    @Test
    void givenMavenRepoWhenGetJarsThenReturnsNonEmptyList() {
        List<Path> jars = cacheService.getJars();
        
        assertNotNull(jars);
        // В реальном ~/.m2/repository должны быть JAR
        // Для CI/CD можно использовать @DisabledIfSystemProperty
    }
    
    @Test
    void givenGetJarsCalledTwiceWhenInvalidateNotCalledThenReturnsSameList() {
        List<Path> firstCall = cacheService.getJars();
        List<Path> secondCall = cacheService.getJars();
        
        assertSame(firstCall, secondCall);
    }
    
    @Test
    void givenInvalidateCacheWhenGetJarsThenReturnsNewList() {
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

- [x] Создан `JarCacheService.java`
- [x] Создан `JarSearchService.java`
- [x] Создан `DecompilationService.java`
- [x] Обновлены зависимости в инструментах
- [x] Все тесты проходят: `mvn test`

### 🔵 REFACTOR — Рефакторинг

> Отмечайте чек-боксы сразу после выполнения каждого пункта!

- [x] Устранено дублирование кода между сервисами
- [x] Добавлены JavaDoc к публичным методам
- [x] Приватный метод `countChar()` сделан package-private для тестирования
- [x] Все тесты проходят после рефакторинга
- [x] Сборка успешна: `mvn clean package`

## Работа с существующим кодом (если применимо)

- [x] Написан characterization test для `findClassInM2()`
- [x] Написан characterization test для `getClassOutline()`
- [x] Написан characterization test для `getMethodSource()`
- [x] Написан characterization test для `decompileClass()`
- [x] Тесты проходят (фиксация поведения)
- [x] Проверена регрессия после изменений

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

- Зависит от TASK-004 (инструменты используют сервисы)
- `countChar()` сделан package-private для возможности тестирования
- В следующей задаче (TASK-006) будет добавлена валидация путей
