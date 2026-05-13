package org.mirent.skills.util;

import lombok.extern.slf4j.Slf4j;

import java.nio.file.Files;
import java.nio.file.Path;

/**
 * @deprecated Файл временно оставлен, но в будущем если не потребуется - удалить.
 */
@Deprecated
@Slf4j
public class NodePathFinder {

    public static Path findPathToExecNodeForLinux() {
        Path nodePath = Path.of("/", "usr", "bin", "node");

        // Проверка наличия папки по указанному пути
        if (!Files.exists(nodePath) || !Files.isRegularFile(nodePath)) {
            throw new IllegalStateException(
                    "Node not found at expected path: " + nodePath.toAbsolutePath()
            );
        }
        log.info("Приложение Node.js найдено по пути: {}", nodePath);

        return nodePath;
    }
}
