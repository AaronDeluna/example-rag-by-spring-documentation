package org.mirent.skills.tests.inner.unit;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mirent.skills.CommandExecutor;
import org.mirent.skills.dto.command.CommandRequestDto;
import org.mirent.skills.dto.command.CommandResultDto;
import org.mirent.skills.service.AgentRunnerProperties;
import org.mirent.skills.util.WutPreparer;
import org.mirent.skills.util.cli.CommandFactory;
import org.mirent.skills.util.cli.OsAwareCommandResolver;
import org.mirent.skills.util.cli.OsType;
import org.mirent.skills.util.cli.QwenCommandFactoryImpl;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Properties;

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
        // Строим CommandFactory из конфигурации
        CommandFactory factory = createCommandFactoryFromProperties();

        // Собираем команду для --version
        List<String> command = factory.buildCommand("--version", null);

        // Запускаем и проверяем результат
        CommandRequestDto versionRequest = new CommandRequestDto(
                command,
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

    /**
     * Создаёт CommandFactory на основе agent-runner.properties (через classpath),
     * с fallback-путями для текущей ОС. Если properties не загружены,
     * использует fallback-пути по умолчанию.
     */
    private static CommandFactory createCommandFactoryFromProperties() {
        Properties props = AgentRunnerProperties.loadDefault();
        Map<OsType, List<Path>> fallbacks = buildFallbackMap(props);

        OsAwareCommandResolver resolver = new OsAwareCommandResolver(fallbacks);

        List<String> baseArgs = AgentRunnerProperties.getBaseArgs(props);
        List<String> prefix = AgentRunnerProperties.getPrefix(props, OsType.detect());

        return new QwenCommandFactoryImpl(resolver, baseArgs, prefix);
    }

    private static Map<OsType, List<Path>> buildFallbackMap(Properties props) {
        Map<OsType, List<Path>> fallbacks = new java.util.HashMap<>();
        for (OsType os : OsType.values()) {
            List<Path> paths = AgentRunnerProperties.getFallbackPaths(props, os);
            if (!paths.isEmpty()) {
                fallbacks.put(os, paths);
            }
        }
        // Если properties не загрузились — добавляем fallback-пути по умолчанию для Linux
        if (fallbacks.isEmpty()) {
            String userHome = System.getProperty("user.home");
            fallbacks.put(OsType.LINUX, List.of(
                    Path.of(userHome, ".npm-global", "bin", "qwen"),
                    Path.of(userHome, ".npm-global", "lib", "node_modules", "@qwen-code", "qwen-code", "cli-entry.js")
            ));
        }
        return fallbacks;
    }
}
