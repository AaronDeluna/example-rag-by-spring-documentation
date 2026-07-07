package org.mirent.skills.tests.inner.unit;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mirent.skills.CommandExecutor;
import org.mirent.skills.dto.command.CommandRequestDto;
import org.mirent.skills.dto.command.CommandResultDto;
import org.mirent.skills.util.WutPreparer;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;

@Tag("inner")
@Tag("unit")
@Slf4j
public class QwenAvailabilityTest {

    private static final Path WUT_SOURCE = Path.of("src/test/resources/wut-templates");
    private final CommandExecutor executor = new CommandExecutor();

    private static Path prepareWut() throws IOException {
        return WutPreparer.builder()
                .wutSourceName("default")
                .wutSourcePath(WUT_SOURCE)
                .build()
                .prepare();
    }

    @Test
    void qwenIsAvailableAndVersionTest() throws Exception {
        // Получаем путь к qwen (если файла нет – тест упадёт с FileNotFoundException)
        Path qwenPath = findQwenPathByOs();

        // Запускаем qwen --version и проверяем результат
        CommandRequestDto versionRequest = new CommandRequestDto(
                List.of(qwenPath.toString(), "--version"),
                prepareWut(),
                Duration.ofMinutes(3)
        );
        CommandResultDto result = executor.execute(versionRequest);
        Assertions.assertEquals(0, result.getExitCode(), "Команда --version завершилась с ошибкой");
        Assertions.assertFalse(result.getStdout().isEmpty(), "Вывод --version пуст");

        String actualVersion = result.getStdout().trim();
        Assertions.assertEquals("0.19.6", actualVersion,
                "Версия qwen не соответствует ожидаемой");
    }

    private static Path findQwenPathByOs() throws FileNotFoundException {
        Path qwenPath;
        String osName = System.getProperty("os.name").toLowerCase();
        String userHome = System.getProperty("user.home");

        if (osName.contains("win")) {
            qwenPath = Path.of(userHome, ".qwen", "bin", "qwen");
        } else {
            // Не работает, т.к. в файле cli.js нет шебанга (раньше он был, но с обновлением убрали)
//            qwenPath = Path.of(userHome, ".npm-global", "lib", "node_modules", "@qwen-code", "qwen-code", "cli.js");
            // Ниже оба рабочих варианта
//            qwenPath = Path.of(userHome, ".npm-global", "lib", "node_modules", "@qwen-code", "qwen-code", "cli-entry.js");
            qwenPath = Path.of(userHome, ".npm-global", "bin", "qwen");
        }

        if (!Files.exists(qwenPath) || !Files.isRegularFile(qwenPath)) {
            throw new FileNotFoundException(qwenPath.toAbsolutePath().toString());
        }

        log.info("Исполняемый файл приложения Qwen найден по пути: {}", qwenPath);
        return qwenPath;
    }
}