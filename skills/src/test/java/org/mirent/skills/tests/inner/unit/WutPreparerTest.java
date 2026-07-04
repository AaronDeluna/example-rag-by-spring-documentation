package org.mirent.skills.tests.inner.unit;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mirent.skills.exeptions.WutPreparerException;
import org.mirent.skills.util.WutPreparer;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Тесты для {@link WutPreparer}.
 * <p>
 * Исходные директории для тестовых данных создаются во временной директории системы.
 * buildDirectory указывается явно на {@code target/wut-tests} (не через автоопределение).
 */
@Tag("inner")
@Tag("unit")
@Slf4j
class WutPreparerTest {

    private static final String DEFAULT_WUT_NAME = "my-wut";

    private Path buildDir;
    private Path tempDir;
    private Path wutSourcePath;

    @BeforeEach
    void setUp() throws WutPreparerException, IOException {
        buildDir = Path.of("target/wut-tests").toAbsolutePath();
        Files.createDirectories(buildDir);
        tempDir = Files.createTempDirectory("wut-test-");
        wutSourcePath = tempDir.resolve("wut-source");
        Files.createDirectories(wutSourcePath);
    }

    @AfterEach
    void tearDown() throws WutPreparerException {
        deleteDirectory(buildDir);
        deleteDirectory(tempDir);
    }

    private WutPreparer.Builder defaultBuilder() {
        return WutPreparer.builder()
                .wutSourcePath(wutSourcePath)
                .buildDirectory(buildDir);
    }

    @Test
    @DisplayName("Копирует шаблон в <wutSourceName>/<uuid>/sorce и возвращает путь к sorce")
    void givenSourceExistsWhenPrepareThenCopiesContents() throws WutPreparerException, IOException {
        createWutSource(DEFAULT_WUT_NAME, "hello.txt", "sub/file.txt");

        Path sorce = defaultBuilder()
                .wutSourceName(DEFAULT_WUT_NAME)
                .wutTargetPath(Path.of("wuts"))
                .build()
                .prepare();

        // sorce = .../wuts/my-wut/<uuid>/sorce
        assertTrue(sorce.endsWith("sorce"), "Должен вернуться путь к sorce");
        Path runRoot = sorce.getParent();
        assertTrue(runRoot.getParent().endsWith(DEFAULT_WUT_NAME), "Папка запуска лежит внутри <wutSourceName>");
        assertTrue(Files.isDirectory(runRoot.resolve("logs")), "Рядом с sorce должна быть папка logs");

        assertTrue(Files.isRegularFile(sorce.resolve("hello.txt")), "hello.txt должен быть скопирован в sorce");
        assertTrue(Files.isRegularFile(sorce.resolve("sub/file.txt")), "Вложенный файл sub/file.txt должен быть скопирован");
        assertTrue(Files.isDirectory(sorce.resolve("sub")), "Вложенная папка sub должна существовать");
    }

    @Test
    @DisplayName("Каждый вызов prepare создаёт отдельную папку запуска, прошлые не трогает")
    void eachPrepareCreatesSeparateRunDir() throws WutPreparerException, IOException {
        String wutName = "accumulate-wut";
        createWutSource(wutName, "file.txt");

        Path firstSorce = defaultBuilder().wutSourceName(wutName).build().prepare();
        Path secondSorce = defaultBuilder().wutSourceName(wutName).build().prepare();

        assertNotEquals(firstSorce, secondSorce, "Каждый запуск — своя папка");
        assertTrue(Files.isRegularFile(firstSorce.resolve("file.txt")), "Первый запуск должен сохраниться");
        assertTrue(Files.isRegularFile(secondSorce.resolve("file.txt")), "Второй запуск должен быть создан");

        // обе папки запуска лежат в одном <wutName>
        assertEquals(firstSorce.getParent().getParent(), secondSorce.getParent().getParent());
    }

    @Test
    @DisplayName("Создаёт структуру запуска, если целевой папки ещё нет")
    void givenNoTargetWhenPrepareThenCreatesRunLayout() throws WutPreparerException, IOException {
        String wutName = "fresh-wut";
        createWutSource(wutName, "data.txt");

        Path sorce = defaultBuilder()
                .wutSourceName(wutName)
                .wutTargetPath(Path.of("wuts"))
                .build()
                .prepare();

        assertTrue(Files.isDirectory(sorce), "sorce должна быть создана");
        assertTrue(Files.isRegularFile(sorce.resolve("data.txt")), "Файл должен быть скопирован");
    }

    @Test
    @DisplayName("Бросает WutPreparerException, если исходная директория не существует")
    void givenNonExistentSourceWhenPrepareThenThrows() {
        WutPreparer preparer = defaultBuilder()
                .wutSourceName("no-such-wut")
                .build();

        WutPreparerException exception = assertThrows(WutPreparerException.class, preparer::prepare);
        assertTrue(exception.getMessage().contains("no-such-wut"), "Сообщение должно содержать имя отсутствующей WUT");
    }

    @Test
    @DisplayName("Бросает WutPreparerException, если источник — файл, а не директория")
    void givenFileSourceWhenPrepareThenThrows() throws WutPreparerException, IOException {
        Path fileWut = wutSourcePath.resolve("not-a-dir.txt");
        Files.writeString(fileWut, "это файл, а не папка");

        WutPreparer preparer = defaultBuilder()
                .wutSourceName("not-a-dir.txt")
                .build();

        WutPreparerException exception = assertThrows(WutPreparerException.class, preparer::prepare);
        assertTrue(exception.getMessage().contains("файл") || exception.getMessage().contains("file"),
                "Сообщение должно указывать, что источник — не директория");
    }

    @Test
    @DisplayName("Бросает IllegalStateException, если wutSourceName не задан")
    void givenNoSourceNameWhenBuildThenThrows() {
        WutPreparerException exception = assertThrows(WutPreparerException.class,
                () -> defaultBuilder().build());

        assertTrue(exception.getMessage().contains("wutSourceName"), "Сообщение должно содержать 'wutSourceName'");
    }

    @Test
    @DisplayName("Использует явно заданные buildDirectory и wutTargetPath")
    void givenExplicitBuildDirAndTargetPathWhenPrepareThenUsesThem() throws WutPreparerException, IOException {
        String wutName = "explicit-wut";
        createWutSource(wutName, "config.yaml");

        Path workspace = defaultBuilder()
                .wutSourceName(wutName)
                .wutTargetPath(Path.of("custom/path"))
                .build()
                .prepare();

        assertTrue(workspace.startsWith(buildDir), "Путь должен начинаться с buildDirectory");
        assertTrue(workspace.endsWith("sorce"), "Путь должен заканчиваться sorce");
        assertTrue(workspace.getParent().getParent().endsWith(wutName), "Папка запуска внутри wutSourceName");
        assertTrue(Files.isRegularFile(workspace.resolve("config.yaml")), "Файл должен быть скопирован");
    }

    @Test
    @DisplayName("Сохраняет вложенную структуру директорий при копировании")
    void givenSourceWithDeepNestingWhenPrepareThenPreservesStructure() throws WutPreparerException, IOException {
        String wutName = "deep-wut";
        Path deepDir = wutSourcePath.resolve(wutName);
        Files.createDirectories(deepDir.resolve("a/b/c/d"));
        Files.writeString(deepDir.resolve("a/b/readme.md"), "глубоко");
        Files.writeString(deepDir.resolve("a/b/c/d/data.json"), "{\"ok\": true}");

        Path workspace = defaultBuilder()
                .wutSourceName(wutName)
                .build()
                .prepare();

        assertTrue(Files.isRegularFile(workspace.resolve("a/b/readme.md")), "Вложенный файл a/b/readme.md должен быть скопирован");
        assertTrue(Files.isRegularFile(workspace.resolve("a/b/c/d/data.json")), "Глубоко вложенный файл a/b/c/d/data.json должен быть скопирован");
        assertTrue(Files.isDirectory(workspace.resolve("a/b/c/d")), "Вложенная папка a/b/c/d должна существовать");
    }

    private Path createWutSource(String name, String... files) throws WutPreparerException, IOException {
        Path source = wutSourcePath.resolve(name);
        for (String file : files) {
            Path filePath = source.resolve(file);
            Files.createDirectories(filePath.getParent());
            Files.writeString(filePath, "содержимое " + file);
        }
        return source;
    }

    private static void deleteDirectory(Path dir) throws WutPreparerException {
        if (Files.notExists(dir)) {
            return;
        }
        try (Stream<Path> files = Files.walk(dir)) {
            files.sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(File::delete);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}