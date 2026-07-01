package org.mirent.skills.util;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Comparator;
import java.util.stream.Stream;

/**
 * Подготавливает рабочую область под тестом (WUT – Workspace Under Test).
 * <p>
 * Копирует содержимое шаблона из {@code wutSourcePath / wutSourceName}
 * в директорию {@code buildDirectory / wutTargetPath / wutSourceName}.
 * Все пути настраиваются через билдер, статических констант нет.
 * </p>
 * <p>
 * По умолчанию:
 * <ul>
 *     <li>{@code wutSourcePath} = {@code "wut-source"} (папка с шаблонами)</li>
 *     <li>{@code wutTargetPath} = {@code "wut-target"} (подпапка внутри сборки)</li>
 *     <li>{@code buildDirectory} определяется автоматически: приоритет {@code target}, затем {@code build}, иначе {@code target}</li>
 *     <li>{@code overwriteTarget} = {@code false} (без перезаписи)</li>
 * </ul>
 * </p>
 */
@Slf4j
public class WutPreparer {

    private final String wutSourceName;
    private final Path wutSourcePath;
    private final Path buildDirectory;
    private final Path wutTargetPath;
    private final boolean overwriteTarget;

    private WutPreparer(Builder builder) {
        this.wutSourceName = builder.wutSourceName;
        this.wutSourcePath = builder.wutSourcePath;
        this.buildDirectory = builder.buildDirectory;
        this.wutTargetPath = builder.wutTargetPath;
        this.overwriteTarget = builder.overwriteTarget;
    }

    /**
     * Выполняет подготовку WUT.
     * <p>
     * Если целевая папка уже существует и {@code overwriteTarget} = {@code true},
     * она будет полностью удалена и создана заново.
     * Если {@code overwriteTarget} = {@code false}, существующая папка остаётся нетронутой
     * (полезно для параметризованных тестов, использующих одну область).
     * </p>
     *
     * @return путь к подготовленной рабочей папке
     * @throws IOException если источник не существует, не является директорией,
     *                     или произошла другая ошибка ввода-вывода
     */
    public Path prepare() throws IOException {
        log.info("Начинаем подготовку WUT с именем '{}'", wutSourceName);

        Path source = resolveSource();
        validateSource(source);
        log.info("Исходная директория валидна: {}", source);

        Path target = resolveTarget();
        ensureTargetDirectory(target);

        copyContents(source, target);
        log.info("Копирование содержимого из {} в {} завершено", source, target);
        log.info("WUT подготовлена: {}", target);
        return target;
    }

    private Path resolveSource() {
        if (wutSourceName == null) {
            throw new IllegalStateException("Имя WUT (wutSourceName) не задано");
        }
        return wutSourcePath.resolve(wutSourceName);
    }

    private void validateSource(Path source) throws IOException {
        if (!Files.exists(source)) {
            throw new IOException("Исходная директория WUT не существует: " + source);
        }
        if (!Files.isDirectory(source)) {
            throw new IOException("Источник WUT не является директорией: " + source
                    + " (это " + (Files.isRegularFile(source) ? "файл" : "специальный объект") + ")");
        }
    }

    private Path resolveTarget() {
        if (wutSourceName == null) {
            throw new IllegalStateException("Имя WUT (wutSourceName) не задано");
        }
        return buildDirectory.resolve(wutTargetPath).resolve(wutSourceName);
    }

    private void ensureTargetDirectory(Path target) throws IOException {
        if (Files.exists(target)) {
            if (overwriteTarget) {
                log.info("Перезапись целевой директории (удаление всего содержимого): {}", target);
                cleanDirectory(target);
                Files.createDirectories(target);
            } else {
                log.info("Целевая папка уже существует и не будет перезаписана: {}", target);
            }
        } else {
            log.info("Создаём целевую директорию: {}", target);
            Files.createDirectories(target);
        }
    }

    private void copyContents(Path source, Path target) throws IOException {
        Files.walkFileTree(source, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                Path rel = source.relativize(dir);
                Path targetDir = target.resolve(rel.toString());
                Files.createDirectories(targetDir);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Path rel = source.relativize(file);
                Path targetFile = target.resolve(rel.toString());
                Files.copy(file, targetFile, StandardCopyOption.REPLACE_EXISTING);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    private static void cleanDirectory(Path dir) throws IOException {
        try (Stream<Path> paths = Files.walk(dir)) {
            paths.sorted(Comparator.reverseOrder())
                    .forEach(path -> {
                        try {
                            Files.delete(path);
                        } catch (IOException e) {
                            throw new RuntimeException("Не удалось удалить " + path, e);
                        }
                    });
        }
    }

    /**
     * Определяет директорию сборки автоматически.
     */
    private static Path detectBuildDirectory() {
        Path cwd = Path.of("").toAbsolutePath();
        Path pom = cwd.resolve("pom.xml");
        Path gradle = cwd.resolve("build.gradle");
        Path gradleKts = cwd.resolve("build.gradle.kts");

        if (Files.exists(pom)) {
            return Path.of("target");
        } else if (Files.exists(gradle) || Files.exists(gradleKts)) {
            return Path.of("build");
        } else {
            Path target = cwd.resolve("target");
            Path build = cwd.resolve("build");
            if (Files.exists(target) && Files.isDirectory(target)) {
                return Path.of("target");
            } else if (Files.exists(build) && Files.isDirectory(build)) {
                return Path.of("build");
            } else {
                throw new RuntimeException("Не определена директория сборки проекта");
            }
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String wutSourceName;
        private Path wutSourcePath = Path.of("wut-source");
        private Path buildDirectory = detectBuildDirectory();
        private Path wutTargetPath = Path.of("wut-target");
        private boolean overwriteTarget = false;

        private Builder() {
        }

        /**
         * Задаёт имя рабочей области (подпапка внутри корневой директории шаблонов WUT).
         *
         * @param wutSourceName имя папки-шаблона
         * @return этот билдер
         */
        public Builder wutSourceName(String wutSourceName) {
            this.wutSourceName = wutSourceName;
            return this;
        }

        /**
         * Задаёт корневую директорию, в которой находятся папки-шаблоны WUT.
         * По умолчанию: {@code "wut-source"}.
         *
         * @param wutSourcePath путь к корневой папке с шаблонами
         * @return этот билдер
         */
        public Builder wutSourcePath(Path wutSourcePath) {
            this.wutSourcePath = wutSourcePath;
            return this;
        }

        /**
         * Задаёт директорию сборки (target или build) вручную.
         * Если не задана, определяется автоматически.
         *
         * @param buildDirectory директория сборки
         * @return этот билдер
         */
        public Builder buildDirectory(Path buildDirectory) {
            this.buildDirectory = buildDirectory;
            return this;
        }

        /**
         * Задаёт подпапку внутри {@code buildDirectory}, куда будет скопирована WUT.
         * По умолчанию: {@code "wut-target"}.
         *
         * @param wutTargetPath относительный путь внутри buildDirectory
         * @return этот билдер
         */
        public Builder wutTargetPath(Path wutTargetPath) {
            this.wutTargetPath = wutTargetPath;
            return this;
        }

        /**
         * Задаёт флаг перезаписи существующей целевой папки.
         * <p>
         * Если {@code true} и целевая папка уже существует, она будет полностью удалена
         * (все файлы и подпапки) и создана заново перед копированием.
         * Если {@code false} и папка существует, она остаётся нетронутой (без ошибок),
         * что удобно для запуска нескольких параметризованных тестов в одной области.
         * </p>
         *
         * @param overwriteTarget {@code true} — удалять существующую целевую папку и создавать заново;
         *                         {@code false} — оставлять существующую папку (по умолчанию)
         * @return этот билдер
         */
        public Builder overwriteTarget(boolean overwriteTarget) {
            this.overwriteTarget = overwriteTarget;
            return this;
        }

        /**
         * Строит {@link WutPreparer}.
         *
         * @return новый экземпляр {@link WutPreparer}
         * @throws IllegalStateException если не задано имя рабочей области
         */
        public WutPreparer build() {
            if (wutSourceName == null) {
                throw new IllegalStateException("Необходимо указать wutSourceName");
            }
            return new WutPreparer(this);
        }
    }
}