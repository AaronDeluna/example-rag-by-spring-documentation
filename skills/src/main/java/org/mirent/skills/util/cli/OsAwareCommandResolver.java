package org.mirent.skills.util.cli;

import org.mirent.skills.exeptions.CommandNotFoundException;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

public class OsAwareCommandResolver implements CommandResolver {

    private final OsType os;
    private final Map<OsType, List<Path>> fallbackPaths;

    public OsAwareCommandResolver(Map<OsType, List<Path>> fallbackPaths) {
        this.os = OsType.detect();
        this.fallbackPaths = fallbackPaths;
    }

    @Override
    public String resolveExecutable(String commandName) {
        // 1. Проверка в PATH
        String inPath = findInPath(commandName);
        if (inPath != null) return inPath;

        // 2. Fallback-пути для текущей ОС
        List<Path> paths = fallbackPaths.getOrDefault(os, List.of());
        for (Path p : paths) {
            // Если p — файл (например, cli-entry.js), проверяем его напрямую
            if (Files.isRegularFile(p) && Files.isExecutable(p)) {
                return p.toString();
            }
            // Если p — директория, ищем внутри файл с именем commandName
            Path candidate = p.resolve(commandName);
            if (Files.isRegularFile(candidate) && Files.isExecutable(candidate)) {
                return candidate.toString();
            }
        }

        throw new CommandNotFoundException(
                "Не найден исполняемый файл для команды: " + commandName +
                        " (ОС: " + os + ", fallback-пути: " + paths + ")"
        );
    }

    private static String findInPath(String command) {
        String pathEnv = System.getenv("PATH");
        if (pathEnv == null) return null;
        String[] dirs = pathEnv.split(File.pathSeparator);
        for (String dir : dirs) {
            Path file = Paths.get(dir).resolve(command);
            if (Files.isRegularFile(file) && Files.isExecutable(file)) {
                return file.toString();
            }
        }
        return null;
    }
}
