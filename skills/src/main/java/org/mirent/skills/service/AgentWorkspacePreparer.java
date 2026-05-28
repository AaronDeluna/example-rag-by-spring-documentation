package org.mirent.skills.service;

import lombok.extern.slf4j.Slf4j;
import org.mirent.skills.dto.module.ModuleLayoutDto;
import org.mirent.skills.exeptions.AgentRunnerConfigurationException;

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
public class AgentWorkspacePreparer {

    private static final String QWEN_DIR = ".qwen";
    private static final String WORKSPACE_DIR_NAME = "agent-runner";

    public Path prepare() {
        ModuleLayoutDto layout = resolveModuleLayout();
        Path projectRoot = resolveProjectRoot(layout.getBasedir());
        Path source = projectRoot.resolve(QWEN_DIR);
        Path workspace = layout.getBasedir().resolve(layout.getBuildDir()).resolve(WORKSPACE_DIR_NAME);

        if (!Files.isDirectory(source)) {
            throw new AgentRunnerConfigurationException(
                    "Не найдена корневая папка .qwen для копирования: " + source
            );
        }

        cleanDirectory(workspace);
        copyDirectory(source, workspace.resolve(QWEN_DIR));

        log.info("Agent workspace подготовлен из {} -> {}", source, workspace);
        return workspace;
    }

    private static ModuleLayoutDto resolveModuleLayout() {
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

    private static Path resolveClassesLocation() {
        try {
            URL location = AgentWorkspacePreparer.class
                    .getProtectionDomain()
                    .getCodeSource()
                    .getLocation();
            return Path.of(location.toURI());
        } catch (URISyntaxException e) {
            throw new AgentRunnerConfigurationException("Не удалось определить расположение классов модуля", e);
        }
    }

    private static Path resolveProjectRoot(Path start) {
        Path current = start.toAbsolutePath();
        while (current != null) {
            if (Files.isDirectory(current.resolve(".git"))) {
                return current;
            }
            current = current.getParent();
        }
        throw new AgentRunnerConfigurationException(
                "Не удалось найти корень проекта (нет .git) начиная с " + start
        );
    }

    private static void cleanDirectory(Path dir) {
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
        } catch (IOException e) {
            throw new AgentRunnerConfigurationException("Не удалось очистить " + dir, e);
        }
    }

    private static void copyDirectory(Path source, Path target) {
        try {
            Files.createDirectories(target);
            Files.walkFileTree(source, new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                    Files.createDirectories(target.resolve(source.relativize(dir)));
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Files.copy(file, target.resolve(source.relativize(file)), StandardCopyOption.REPLACE_EXISTING);
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            throw new AgentRunnerConfigurationException(
                    "Не удалось скопировать " + source + " -> " + target, e
            );
        }
    }
}
