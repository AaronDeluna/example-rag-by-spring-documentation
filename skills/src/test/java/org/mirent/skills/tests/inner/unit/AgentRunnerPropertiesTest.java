package org.mirent.skills.tests.inner.unit;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mirent.skills.service.AgentRunnerProperties;
import org.mirent.skills.util.cli.OsType;

import java.nio.file.Path;
import java.util.List;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;

@Tag("inner")
@Tag("unit")
class AgentRunnerPropertiesTest {

    @Test
    @DisplayName("Читает fallback-пути для Linux из properties")
    void readsFallbackPathsForLinux() {
        Properties props = new Properties();
        props.setProperty("agent.cli.qwen.fallback.linux", "/usr/local/bin/qwen;/home/user/.npm-global/bin/qwen");

        List<Path> paths = AgentRunnerProperties.getFallbackPaths(props, OsType.LINUX);

        assertEquals(2, paths.size());
        assertEquals(Path.of("/usr/local/bin/qwen"), paths.get(0));
        assertEquals(Path.of("/home/user/.npm-global/bin/qwen"), paths.get(1));
    }

    @Test
    @DisplayName("Читает fallback-пути для Windows из properties")
    void readsFallbackPathsForWindows() {
        Properties props = new Properties();
        props.setProperty("agent.cli.qwen.fallback.windows", "C:\\Program Files\\qwen\\qwen.exe");

        List<Path> paths = AgentRunnerProperties.getFallbackPaths(props, OsType.WINDOWS);

        assertEquals(1, paths.size());
        assertEquals(Path.of("C:\\Program Files\\qwen\\qwen.exe"), paths.get(0));
    }

    @Test
    @DisplayName("Возвращает пустой список, если fallback-пути не заданы")
    void returnsEmptyListWhenNoFallbackProperty() {
        Properties props = new Properties();

        List<Path> paths = AgentRunnerProperties.getFallbackPaths(props, OsType.LINUX);

        assertTrue(paths.isEmpty());
    }

    @Test
    @DisplayName("Читает базовые аргументы из properties")
    void readsBaseArgs() {
        Properties props = new Properties();
        props.setProperty("agent.cli.qwen.args", "--output-format,stream-json,--approval-mode,yolo");

        List<String> args = AgentRunnerProperties.getBaseArgs(props);

        assertEquals(4, args.size());
        assertEquals("--output-format", args.get(0));
        assertEquals("stream-json", args.get(1));
        assertEquals("--approval-mode", args.get(2));
        assertEquals("yolo", args.get(3));
    }

    @Test
    @DisplayName("Возвращает пустой список, если базовые аргументы не заданы")
    void returnsEmptyListWhenNoArgsProperty() {
        Properties props = new Properties();

        List<String> args = AgentRunnerProperties.getBaseArgs(props);

        assertTrue(args.isEmpty());
    }

    @Test
    @DisplayName("Читает префикс для Windows из properties")
    void readsPrefixForWindows() {
        Properties props = new Properties();
        props.setProperty("agent.cli.qwen.prefix.windows", "cmd.exe,/c");

        List<String> prefix = AgentRunnerProperties.getPrefix(props, OsType.WINDOWS);

        assertEquals(2, prefix.size());
        assertEquals("cmd.exe", prefix.get(0));
        assertEquals("/c", prefix.get(1));
    }

    @Test
    @DisplayName("Возвращает пустой список для префикса Linux (нет свойства)")
    void returnsEmptyListForLinuxPrefix() {
        Properties props = new Properties();

        List<String> prefix = AgentRunnerProperties.getPrefix(props, OsType.LINUX);

        assertTrue(prefix.isEmpty());
    }

    @Test
    @DisplayName("Обрабатывает пустые строки в properties как пустые списки")
    void handlesEmptyStringAsEmptyList() {
        Properties props = new Properties();
        props.setProperty("agent.cli.qwen.args", "");
        props.setProperty("agent.cli.qwen.fallback.linux", "  ");
        props.setProperty("agent.cli.qwen.prefix.windows", "");

        assertTrue(AgentRunnerProperties.getBaseArgs(props).isEmpty());
        assertTrue(AgentRunnerProperties.getFallbackPaths(props, OsType.LINUX).isEmpty());
        assertTrue(AgentRunnerProperties.getPrefix(props, OsType.WINDOWS).isEmpty());
    }

    @Test
    @DisplayName("Игнорирует пробелы вокруг значений при split")
    void trimsValuesAroundSeparator() {
        Properties props = new Properties();
        props.setProperty("agent.cli.qwen.args", " --output-format , stream-json ");

        List<String> args = AgentRunnerProperties.getBaseArgs(props);

        assertEquals(2, args.size());
        assertEquals("--output-format", args.get(0));
        assertEquals("stream-json", args.get(1));
    }
}
