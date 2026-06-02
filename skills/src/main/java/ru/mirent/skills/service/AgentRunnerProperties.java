package ru.mirent.skills.service;

import lombok.extern.slf4j.Slf4j;
import ru.mirent.skills.exeptions.AgentRunnerConfigurationException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

@Slf4j
public final class AgentRunnerProperties {

    public static final String DEFAULT_PROPERTIES_FILE = "agent-runner.properties";
    public static final String CLI_PROPERTY = "agent.cli";

    private AgentRunnerProperties() {
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
