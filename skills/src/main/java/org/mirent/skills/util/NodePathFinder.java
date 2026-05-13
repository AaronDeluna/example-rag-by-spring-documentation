package org.mirent.skills.util;

import lombok.extern.slf4j.Slf4j;

import java.io.FileNotFoundException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * @deprecated Файл временно оставлен, но в будущем если не потребуется - удалить.
 */
@Deprecated
@Slf4j
public class NodePathFinder {

    public static Path findPathToExecNodeForLinux() throws FileNotFoundException {
        Path nodePath = Path.of("/", "usr", "bin", "node");

        // Проверка наличия папки по указанному пути
        if (!Files.exists(nodePath) || !Files.isRegularFile(nodePath)) {
            throw new FileNotFoundException(nodePath.toAbsolutePath().toString());
        }
        log.info("Приложение Node.js найдено по пути: {}", nodePath);

        return nodePath;
    }
}
