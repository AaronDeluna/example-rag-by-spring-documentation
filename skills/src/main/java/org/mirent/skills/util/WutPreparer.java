package org.mirent.skills.util;

import lombok.extern.slf4j.Slf4j;
import org.mirent.skills.exeptions.WutPreparerException;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.UUID;

/**
 * Подготавливает рабочую область под тестом (WUT – Workspace Under Test).
 * <p>
 * Под каждый вызов {@link #prepare()} создаётся отдельная папка запуска с UUID
 * и копирует шаблон {@code wutSourcePath / wutSourceName} прямо в её {@code sorce}:
 * <pre>
 * buildDirectory / wutTargetPath / wutSourceName / &lt;uuid&gt; / sorce   ← копия шаблона (.qwen со скилами и т.п.)
 * buildDirectory / wutTargetPath / wutSourceName / &lt;uuid&gt; / logs    ← сюда пишутся логи запуска
 * </pre>
 * Возвращается путь к {@code sorce}. В корне {@code .../wutSourceName/} лежат только
 * папки запусков (с UUID) — они не очищаются и накапливаются.
 * </p>
 * <p>
 * По умолчанию:
 * <ul>
 *     <li>{@code wutSourcePath} = {@code "wut-source"} (папка с шаблонами)</li>
 *     <li>{@code wutTargetPath} = {@code "wut-target"} (подпапка внутри сборки)</li>
 *     <li>{@code buildDirectory} определяется автоматически: приоритет {@code target}, затем {@code build}, иначе {@code target}</li>
 * </ul>
 * </p>
 */
@Slf4j
public class WutPreparer {

    private static final String SOURCE_DIR = "sorce";
    private static final String LOGS_DIR = "logs";

    private final String wutSourceName;
    private final Path wutSourcePath;
    private final Path buildDirectory;
    private final Path wutTargetPath;

    private WutPreparer(Builder builder) {
        this.wutSourceName = builder.wutSourceName;
        this.wutSourcePath = builder.wutSourcePath;
        this.buildDirectory = builder.buildDirectory;
        this.wutTargetPath = builder.wutTargetPath;
    }

    /**
     * Выполняет подготовку WUT для одного запуска.
     * <p>
     * Создаёт свежую папку запуска {@code .../wutSourceName/<uuid>/} с подпапками
     * {@code sorce} и {@code logs}, копирует шаблон в {@code sorce} и возвращает путь к нему.
     * Папки прошлых запусков не трогаются и накапливаются рядом.
     * </p>
     *
     * @return путь к {@code sorce} этого запуска (рабочая область агента)
     * @throws WutPreparerException если источник не существует, не является директорией,
     *                     или произошла ошибка ввода-вывода при создании папок/копировании шаблона
     */
    public Path prepare() {
        log.info("Начинаем подготовку WUT с именем '{}'", wutSourceName);

        Path source = resolveSource();
        validateSource(source);
        log.info("Исходная директория валидна: {}", source);

        Path runRoot = resolveTarget().resolve(UUID.randomUUID().toString());
        Path sourceDir = runRoot.resolve(SOURCE_DIR);
        Path logsDir = runRoot.resolve(LOGS_DIR);
        try {
            Files.createDirectories(sourceDir);
            Files.createDirectories(logsDir);
            copyContents(source, sourceDir);
        } catch (IOException e) {
            throw new WutPreparerException("Не удалось подготовить рабочую область WUT в " + runRoot, e);
        }
        log.info("Шаблон {} скопирован в {}", source, sourceDir);
        log.info("WUT подготовлена: {}", runRoot);
        return sourceDir;
    }

    private Path resolveSource() {
        if (wutSourceName == null) {
            throw new WutPreparerException("Имя WUT (wutSourceName) не задано");
        }
        return wutSourcePath.resolve(wutSourceName);
    }

    private void validateSource(Path source) {
        if (!Files.exists(source)) {
            throw new WutPreparerException("Исходная директория WUT не существует: " + source);
        }
        if (!Files.isDirectory(source)) {
            throw new WutPreparerException("Источник WUT не является директорией: " + source
                    + " (это " + (Files.isRegularFile(source) ? "файл" : "специальный объект") + ")");
        }
    }

    private Path resolveTarget() {
        if (wutSourceName == null) {
            throw new WutPreparerException("Имя WUT (wutSourceName) не задано");
        }
        return buildDirectory.resolve(wutTargetPath).resolve(wutSourceName);
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
                throw new WutPreparerException("Не определена директория сборки проекта");
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
         * Строит {@link WutPreparer}.
         *
         * @return новый экземпляр {@link WutPreparer}
         * @throws IllegalStateException если не задано имя рабочей области
         */
        public WutPreparer build() {
            if (wutSourceName == null) {
                throw new WutPreparerException("Необходимо указать wutSourceName");
            }
            return new WutPreparer(this);
        }
    }
}