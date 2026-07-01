package org.mirent.skills.tests.inner;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mirent.skills.util.WutPreparer;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Тесты для {@link WutPreparer}.
 * <p>
 * Исходные директории для тестовых данных создаются во временной директории системы.
 * buildDirectory указывается явно на {@code target/wut-tests} (не через автоопределение).
 */
@Slf4j
class WutPreparerTest {

    private static final String DEFAULT_WUT_NAME = "my-wut";
    private static final String DEFAULT_TARGET_PATH = "wut-target";

    private Path buildDir;
    private Path tempDir;
    private Path wutSourcePath;

    @BeforeEach
    void setUp() throws IOException {
        buildDir = Path.of("target/wut-tests").toAbsolutePath();
        Files.createDirectories(buildDir);
        tempDir = Files.createTempDirectory("wut-test-");
        wutSourcePath = tempDir.resolve("wut-source");
        Files.createDirectories(wutSourcePath);
    }

    @AfterEach
    void tearDown() throws IOException {
        deleteDirectory(buildDir);
        deleteDirectory(tempDir);
    }

    private WutPreparer.Builder defaultBuilder() {
        return WutPreparer.builder()
                .wutSourcePath(wutSourcePath)
                .buildDirectory(buildDir);
    }

    @Test
    @DisplayName("Копирует содержимое из wutSourcePath/wutSourceName в buildDir/wutTargetPath/wutSourceName")
    void givenSourceExistsWhenPrepareThenCopiesContents() throws IOException {
        createWutSource(DEFAULT_WUT_NAME, "hello.txt", "sub/file.txt");

        Path workspace = defaultBuilder()
                .wutSourceName(DEFAULT_WUT_NAME)
                .wutTargetPath(Path.of("wuts"))
                .build()
                .prepare();

        assertTrue(workspace.endsWith(DEFAULT_WUT_NAME), "Должна вернуться целевая папка");
        assertTrue(Files.isDirectory(workspace), "Рабочая папка должна существовать");
        assertTrue(Files.isRegularFile(workspace.resolve("hello.txt")), "hello.txt должен быть скопирован");
        assertTrue(Files.isRegularFile(workspace.resolve("sub/file.txt")), "Вложенный файл sub/file.txt должен быть скопирован");
        assertTrue(Files.isDirectory(workspace.resolve("sub")), "Вложенная папка sub должна существовать");
    }

    @Test
    @DisplayName("Не удаляет старые файлы, если overwriteTarget = false и целевая папка существует")
    void givenExistingTargetWhenOverwriteFalseThenPreservesOldContent() throws IOException {
        String wutName = "merge-wut";
        createWutSource(wutName, "new-file.txt");

        Path oldFile = buildDir.resolve(DEFAULT_TARGET_PATH)
                .resolve(wutName)
                .resolve("old-file.txt");
        Files.createDirectories(oldFile.getParent());
        Files.writeString(oldFile, "старый файл");

        Path workspace = defaultBuilder()
                .wutSourceName(wutName)
                .build()
                .prepare();

        assertTrue(Files.isRegularFile(workspace.resolve("new-file.txt")), "Новый файл должен быть скопирован");
        assertTrue(Files.isRegularFile(workspace.resolve("old-file.txt")), "Старый файл должен сохраниться (overwriteTarget = false)");
    }

    @Test
    @DisplayName("Удаляет старые файлы, если overwriteTarget = true и целевая папка существует")
    void givenExistingTargetWhenOverwriteTrueThenReplaces() throws IOException {
        String wutName = "replace-wut";
        createWutSource(wutName, "new-file.txt");

        Path oldDir = buildDir.resolve(DEFAULT_TARGET_PATH)
                .resolve(wutName)
                .resolve("old-content");
        Files.createDirectories(oldDir);

        Path workspace = defaultBuilder()
                .wutSourceName(wutName)
                .overwriteTarget(true)
                .build()
                .prepare();

        assertTrue(Files.isRegularFile(workspace.resolve("new-file.txt")), "Новый файл должен присутствовать");
        assertFalse(Files.exists(workspace.resolve("old-content")), "Старое содержимое должно быть удалено");
    }

    @Test
    @DisplayName("Создаёт новую целевую папку, если её ещё нет")
    void givenNoTargetWhenPrepareThenCreatesTargetDirectory() throws IOException {
        String wutName = "fresh-wut";
        createWutSource(wutName, "data.txt");

        Path workspace = defaultBuilder()
                .wutSourceName(wutName)
                .wutTargetPath(Path.of("wuts"))
                .build()
                .prepare();

        assertTrue(Files.isDirectory(workspace), "Рабочая папка должна быть создана");
        assertTrue(Files.isRegularFile(workspace.resolve("data.txt")), "Файл должен быть скопирован");
    }

    @Test
    @DisplayName("Бросает IOException, если исходная директория не существует")
    void givenNonExistentSourceWhenPrepareThenThrows() {
        WutPreparer preparer = defaultBuilder()
                .wutSourceName("no-such-wut")
                .build();

        IOException exception = assertThrows(IOException.class, preparer::prepare);
        assertTrue(exception.getMessage().contains("no-such-wut"), "Сообщение должно содержать имя отсутствующей WUT");
    }

    @Test
    @DisplayName("Бросает IOException, если источник — файл, а не директория")
    void givenFileSourceWhenPrepareThenThrows() throws IOException {
        Path fileWut = wutSourcePath.resolve("not-a-dir.txt");
        Files.writeString(fileWut, "это файл, а не папка");

        WutPreparer preparer = defaultBuilder()
                .wutSourceName("not-a-dir.txt")
                .build();

        IOException exception = assertThrows(IOException.class, preparer::prepare);
        assertTrue(exception.getMessage().contains("файл") || exception.getMessage().contains("file"),
                "Сообщение должно указывать, что источник — не директория");
    }

    @Test
    @DisplayName("Бросает IllegalStateException, если wutSourceName не задан")
    void givenNoSourceNameWhenBuildThenThrows() {
        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> defaultBuilder().build());

        assertTrue(exception.getMessage().contains("wutSourceName"), "Сообщение должно содержать 'wutSourceName'");
    }

    @Test
    @DisplayName("Использует явно заданные buildDirectory и wutTargetPath")
    void givenExplicitBuildDirAndTargetPathWhenPrepareThenUsesThem() throws IOException {
        String wutName = "explicit-wut";
        createWutSource(wutName, "config.yaml");

        Path workspace = defaultBuilder()
                .wutSourceName(wutName)
                .wutTargetPath(Path.of("custom/path"))
                .build()
                .prepare();

        assertTrue(workspace.startsWith(buildDir), "Путь должен начинаться с buildDirectory");
        assertTrue(workspace.endsWith(wutName), "Путь должен заканчиваться wutSourceName");
        assertTrue(Files.isRegularFile(workspace.resolve("config.yaml")), "Файл должен быть скопирован");
    }

    @Test
    @DisplayName("Сохраняет вложенную структуру директорий при копировании")
    void givenSourceWithDeepNestingWhenPrepareThenPreservesStructure() throws IOException {
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

    private Path createWutSource(String name, String... files) throws IOException {
        Path source = wutSourcePath.resolve(name);
        for (String file : files) {
            Path filePath = source.resolve(file);
            Files.createDirectories(filePath.getParent());
            Files.writeString(filePath, "содержимое " + file);
        }
        return source;
    }

    private static void deleteDirectory(Path dir) throws IOException {
        if (Files.notExists(dir)) {
            return;
        }
        try (var files = Files.walk(dir)) {
            files.sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(File::delete);
        }
    }
}