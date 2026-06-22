package org.mirent.skills.util;

import lombok.extern.slf4j.Slf4j;
import org.mirent.skills.dto.module.ModuleLayoutDto;
import org.mirent.skills.exeptions.AgentRunnerConfigurationException;
import org.mirent.skills.service.AgentWorkspacePreparer;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Comparator;
import java.util.stream.Stream;

@Slf4j
public class SkillsFileUtils {

    /**
     * Определяет basedir текущего модуля и его build-каталог (target для Maven, build для Gradle).
     * Поднимается вверх от папки с классами и ищет маркер модуля (pom.xml / build.gradle / build.gradle.kts).
     */
    public static ModuleLayoutDto resolveModuleLayout() {
        Path classesDir = resolveClassesLocation();
        Path current = classesDir;
        while (current != null) {
            if (Files.isRegularFile(current.resolve("pom.xml"))) {
                return new ModuleLayoutDto(current, "target");
            }
            if (Files.isRegularFile(current.resolve("build.gradle"))
                    || Files.isRegularFile(current.resolve("build.gradle.kts"))) {
                return new ModuleLayoutDto(current, "build");
            }
            current = current.getParent();
        }
        throw new AgentRunnerConfigurationException(
                "Не удалось найти basedir модуля (нет pom.xml/build.gradle) начиная с " + classesDir
        );
    }

    public static Path resolveClassesLocation() {
        try {
            URL location = AgentWorkspacePreparer.class
                    .getProtectionDomain()
                    .getCodeSource()
                    .getLocation();
            Path path = Path.of(location.toURI());
            log.info("Определен путь к папке со скомпилированными классами проекта: {}", path);
            return path;
        } catch (URISyntaxException e) {
            throw new AgentRunnerConfigurationException("Не удалось определить расположение классов модуля", e);
        }
    }

    public static void cleanDirectory(Path dir) {
        if (!Files.exists(dir)) {
            return;
        }
        try (Stream<Path> paths = Files.walk(dir)) {
            paths.sorted(Comparator.reverseOrder())
                    .forEach(path -> {
                        try {
                            Files.delete(path);
                        } catch (IOException e) {
                            throw new AgentRunnerConfigurationException(
                                    "Не удалось удалить " + path, e
                            );
                        }
                    });
            log.info("Успешная очистка директории: {}", dir);
        } catch (IOException e) {
            throw new AgentRunnerConfigurationException("Не удалось очистить " + dir, e);
        }
    }

    public static void copyDirectory(Path source, Path target) {
        try {
            Files.createDirectories(target);
            Files.walkFileTree(source, new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                    Files.createDirectories(target.resolve(source.relativize(dir).toString()));
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Files.copy(file, target.resolve(source.relativize(file).toString()), StandardCopyOption.REPLACE_EXISTING);
                    return FileVisitResult.CONTINUE;
                }
            });
            log.info("Содержимое директории: {} скопировано в директорию: {}", source, target);
        } catch (IOException e) {
            throw new AgentRunnerConfigurationException(
                    "Не удалось скопировать " + source + " -> " + target, e
            );
        }
    }
}
