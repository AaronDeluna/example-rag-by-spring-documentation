package org.mirent.skills.service;

import lombok.extern.slf4j.Slf4j;
import org.mirent.skills.exeptions.AgentRunnerConfigurationException;
import org.mirent.skills.util.cli.OsType;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

@Slf4j
public final class AgentRunnerProperties {

    public static final String DEFAULT_PROPERTIES_FILE = "agent-runner.properties";
    public static final String CLI_PROPERTY = "agent.cli";

    // Qwen CLI fallback keys
    private static final String CLI_FALLBACK_PREFIX = "agent.cli.qwen.fallback.";
    private static final String CLI_ARGS = "agent.cli.qwen.args";
    private static final String CLI_PREFIX_WINDOWS = "agent.cli.qwen.prefix.windows";

    private AgentRunnerProperties() {
    }

    /**
     * Возвращает список fallback-путей для указанной ОС.
     * Пути в properties разделяются точкой с запятой {@code ;}.
     *
     * @param props загруженные properties
     * @param os    тип ОС
     * @return список путей (может быть пустым)
     */
    public static List<Path> getFallbackPaths(Properties props, OsType os) {
        String key = CLI_FALLBACK_PREFIX + os.name().toLowerCase();
        String value = props.getProperty(key);
        if (value == null || value.isBlank()) {
            return List.of();
        }
        return Arrays.stream(value.split(";"))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(Path::of)
                .collect(Collectors.toList());
    }

    /**
     * Возвращает список базовых аргументов для запуска CLI.
     * Аргументы в properties разделяются запятой {@code ,}.
     *
     * @param props загруженные properties
     * @return список аргументов (может быть пустым)
     */
    public static List<String> getBaseArgs(Properties props) {
        String value = props.getProperty(CLI_ARGS);
        if (value == null || value.isBlank()) {
            return List.of();
        }
        return Arrays.stream(value.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }

    /**
     * Возвращает список префиксов команды для указанной ОС.
     * Аргументы в properties разделяются запятой {@code ,}.
     *
     * @param props загруженные properties
     * @param os    тип ОС
     * @return список префиксов (может быть пустым)
     */
    public static List<String> getPrefix(Properties props, OsType os) {
        // Префикс определён только для Windows
        if (os != OsType.WINDOWS) {
            return List.of();
        }
        String value = props.getProperty(CLI_PREFIX_WINDOWS);
        if (value == null || value.isBlank()) {
            return List.of();
        }
        return Arrays.stream(value.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }

    public static Properties loadDefault() {
        Properties classpathProperties = loadClasspathProperties();
        if (classpathProperties != null) {
            log.info("Agent runner properties loaded from classpath: {}", DEFAULT_PROPERTIES_FILE);
            return classpathProperties;
        }

        Path propertiesPath = resolveDefaultPropertiesPath();
        log.info("Agent runner properties loaded from file: {}", propertiesPath.toAbsolutePath());
        return loadFromFile(propertiesPath);
    }

    private static Properties loadFromFile(Path propertiesPath) {
        if (!Files.isRegularFile(propertiesPath)) {
            throw new AgentRunnerConfigurationException(
                    "Agent runner properties file not found: "
                            + propertiesPath.toAbsolutePath()
                            + ". Set property '"
                            + CLI_PROPERTY
                            + "' to select CLI."
            );
        }

        Properties properties = new Properties();
        try (InputStream inputStream = Files.newInputStream(propertiesPath)) {
            properties.load(inputStream);
        } catch (IOException e) {
            throw new AgentRunnerConfigurationException("Failed to read agent runner properties: " + propertiesPath, e);
        }
        return properties;
    }

    private static Properties loadClasspathProperties() {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        if (classLoader == null) {
            classLoader = AgentRunnerProperties.class.getClassLoader();
        }

        try (InputStream inputStream = classLoader.getResourceAsStream(DEFAULT_PROPERTIES_FILE)) {
            if (inputStream == null) {
                return null;
            }

            Properties properties = new Properties();
            properties.load(inputStream);
            return properties;
        } catch (IOException e) {
            throw new AgentRunnerConfigurationException(
                    "Failed to read agent runner properties from classpath: " + DEFAULT_PROPERTIES_FILE,
                    e
            );
        }
    }

    private static Path resolveDefaultPropertiesPath() {
        Path currentDirectory = Path.of("").toAbsolutePath();
        Path propertiesPath = currentDirectory.resolve(DEFAULT_PROPERTIES_FILE);
        if (Files.isRegularFile(propertiesPath)) {
            return propertiesPath;
        }

        Path modulePropertiesPath = currentDirectory.resolve("skills").resolve(DEFAULT_PROPERTIES_FILE);
        if (Files.isRegularFile(modulePropertiesPath)) {
            return modulePropertiesPath;
        }

        return propertiesPath;
    }
}
