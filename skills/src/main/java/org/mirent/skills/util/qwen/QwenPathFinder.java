package org.mirent.skills.util.qwen;

import lombok.extern.slf4j.Slf4j;

import java.io.FileNotFoundException;
import java.nio.file.Files;
import java.nio.file.Path;

@Slf4j
public class QwenPathFinder {

    public static Path findQwenPathByOs() throws FileNotFoundException {
        Path qwenPath = buildPathByOs();

        if (!Files.exists(qwenPath) || !Files.isRegularFile(qwenPath)) {
            throw new FileNotFoundException(qwenPath.toAbsolutePath().toString());
        }

        log.info("Исполняемый файл приложения Qwen найден по пути: {}", qwenPath);

        return qwenPath;
    }

    private static Path buildPathByOs() {
        String osName = System.getProperty("os.name").toLowerCase();
        String userHome = System.getProperty("user.home");

        if (osName.contains("win")) {
            return Path.of(userHome, ".qwen", "bin", "qwen");
        } else {
            return Path.of(userHome, ".npm-global", "lib", "node_modules", "@qwen-code", "qwen-code", "cli.js");
        }
    }
}