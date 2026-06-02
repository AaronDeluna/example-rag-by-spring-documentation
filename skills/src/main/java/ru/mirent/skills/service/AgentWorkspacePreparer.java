package ru.mirent.skills.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.mirent.skills.dto.module.ModuleLayoutDto;
import ru.mirent.skills.exeptions.AgentRunnerConfigurationException;
import ru.mirent.skills.exeptions.AgentSetNotFoundException;
import ru.mirent.skills.exeptions.AgentSetsDirectoryNotFoundException;
import ru.mirent.skills.exeptions.MissingAgentSetNameException;

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

// TODO Добавить логгирование
// TODO Вынести утилитные методы
@Slf4j
@RequiredArgsConstructor
public class AgentWorkspacePreparer {

    private static final String AGENT_SETS_DIR = "src/test/resources/agent-sets";
    private static final String QWEN_DIR = ".qwen";
    private static final String SKILLS_DIR = "skills";
    private static final String WORKSPACE_DIR_NAME = "agent-runner";

    private final String agentSetName;

    public Path prepare() {
        if (agentSetName == null || agentSetName.isBlank()) {
            throw new MissingAgentSetNameException();
        }

        ModuleLayoutDto layout = resolveModuleLayout();
        Path agentSetsDir = layout.getBasedir().resolve(AGENT_SETS_DIR);
        Path agentSet = agentSetsDir.resolve(agentSetName);

        if (!Files.isDirectory(agentSetsDir)) {
            throw new AgentSetsDirectoryNotFoundException(
                    "Не найдена директория с наборами для агента: " + agentSetsDir
                            + ". Создайте её и положите внутрь подпапки с наборами."
            );
        }
        if (!Files.isDirectory(agentSet)) {
            throw new AgentSetNotFoundException(
                    "Не найден набор агента '" + agentSetName + "' по пути " + agentSet
            );
        }

        Path workspace = layout.getBasedir().resolve(layout.getBuildDir()).resolve(WORKSPACE_DIR_NAME);
        Path skillsTarget = workspace.resolve(QWEN_DIR).resolve(SKILLS_DIR);

        cleanDirectory(workspace);
        copyDirectory(agentSet, skillsTarget);

        log.info("Agent workspace подготовлен из набора '{}': {} -> {}", agentSetName, agentSet, workspace);
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
            Files.walkFileTree(source, new SimpleFileVisitor<Path>() {
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
        } catch (IOException e) {
            throw new AgentRunnerConfigurationException(
                    "Не удалось скопировать " + source + " -> " + target, e
            );
        }
    }
}
