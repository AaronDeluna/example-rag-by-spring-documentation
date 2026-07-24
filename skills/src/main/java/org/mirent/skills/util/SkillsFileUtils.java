package org.mirent.skills.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.ivanmilovanov.agentic.cli.runner.exception.AgentRunnerConfigurationException;
import lombok.extern.slf4j.Slf4j;
import org.mirent.skills.dto.module.ModuleLayoutDto;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
public class SkillsFileUtils {

    /** Относительный путь к каталогу скиллов внутри рабочей области (qwen-конвенция). */
    private static final String SKILLS_DIR = ".qwen/skills";

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

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
            URL location = SkillsFileUtils.class
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

    /**
     * Читает скилл целиком — весь каталог {@code .qwen/skills/<skillName>/} из рабочей области.
     * Возвращает конкатенацию всех файлов скилла (SKILL.md + скрипты/ресурсы) с их
     * относительными путями-заголовками, чтобы контент был самодостаточным (например, для судьи).
     *
     * @param workspace рабочая область агента (то, что вернул {@code WutPreparer.prepare()})
     * @param skillName имя скилла (папка внутри {@code .qwen/skills})
     * @return полный контент скилла
     * @throws AgentRunnerConfigurationException если скилл не найден или произошла ошибка чтения
     */
    public static String readSkill(Path workspace, String skillName) {
        Path skillDir = workspace.resolve(SKILLS_DIR).resolve(skillName);
        if (!Files.isDirectory(skillDir)) {
            throw new AgentRunnerConfigurationException("Скилл не найден: " + skillDir);
        }
        try (Stream<Path> files = Files.walk(skillDir)) {
            return files
                    .filter(Files::isRegularFile)
                    .sorted()
                    .map(file -> "=== " + skillDir.relativize(file) + " ===\n" + readFileContent(file))
                    .collect(Collectors.joining("\n\n"));
        } catch (IOException e) {
            throw new AgentRunnerConfigurationException(
                    "Не удалось прочитать скилл '" + skillName + "' из " + skillDir, e
            );
        }
    }

    /**
     * Главный метод для матчера: по логу прогона возвращает все вызванные скиллы вместе с их
     * полным контентом. Если в логе не было ни одного вызова скилла — вернёт пустую карту.
     * <p>
     * Лог — это JSON-<b>объект</b> {@code { "skillName":..., "events":[...] }} (как пишет
     * {@code RunnerLogWriter}). Имена скиллов собираются из двух источников:
     * <ul>
     *     <li>верхнеуровневое поле {@code skillName} — явный вызов ({@code executeSkill});</li>
     *     <li>{@code tool_use} со {@code skill} в {@code events} — неявный вызов ({@code execute}).</li>
     * </ul>
     * Рабочая область берётся из {@code cwd} внутри {@code events}.
     *
     * @param logJson JSON лога прогона (объект)
     * @return карта «имя скилла → полный контент», пустая — если скиллов не было
     * @throws AgentRunnerConfigurationException если JSON не разобрать, нет {@code cwd} или скилл не найден
     */
    public static Map<String, String> readSkillsFromLog(String logJson) {
        JsonNode log;
        try {
            log = OBJECT_MAPPER.readTree(logJson);
        } catch (IOException e) {
            throw new AgentRunnerConfigurationException("Не удалось разобрать JSON лога прогона", e);
        }
        if (log == null) {
            throw new AgentRunnerConfigurationException("Лог прогона пустой");
        }
        List<JsonNode> events = new ArrayList<>();
        log.path("events").forEach(events::add);
        Path workspace = resolveWorkspace(events);

        // Имена скиллов: явный вызов (top-level skillName) + неявные (tool_use в events)
        Set<String> skillNames = new LinkedHashSet<>();
        String explicitSkill = log.path("skillName").asText("");
        if (!explicitSkill.isBlank()) {
            skillNames.add(explicitSkill);
        }
        skillNames.addAll(AgentSkillCallExtractorUtils.extractSkillCalls(events));

        Map<String, String> result = new LinkedHashMap<>();
        for (String skillName : skillNames) {
            result.put(skillName, readSkill(workspace, skillName));
        }
        return result;
    }

    /**
     * Достаёт рабочую область прогона из {@code cwd} первого события, где он задан
     * (обычно событие {@code system/init}).
     */
    private static Path resolveWorkspace(List<JsonNode> events) {
        if (events == null || events.isEmpty()) {
            throw new AgentRunnerConfigurationException("В логе нет событий — не удалось определить рабочую область");
        }
        for (JsonNode event : events) {
            String cwd = event.path("cwd").asText("");
            if (!cwd.isBlank()) {
                return Path.of(cwd);
            }
        }
        throw new AgentRunnerConfigurationException(
                "В событиях прогона нет 'cwd' — не удалось определить рабочую область"
        );
    }

    private static String readFileContent(Path file) {
        try {
            return Files.readString(file);
        } catch (IOException e) {
            throw new AgentRunnerConfigurationException("Не удалось прочитать файл скилла " + file, e);
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
