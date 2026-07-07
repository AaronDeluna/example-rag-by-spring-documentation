package org.mirent.skills.service;

import lombok.extern.slf4j.Slf4j;
import org.mirent.skills.exeptions.AgentRunnerConfigurationException;
import org.mirent.skills.util.cli.OsType;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
public final class AgentRunnerProperties {

    public static final String DEFAULT_PROPERTIES_FILE = "agent-runner.properties";
    public static final String CLI_PROPERTY = "agent.cli";

    // Qwen CLI configuration keys
    private static final String CLI_FALLBACK_PREFIX = "agent.cli.qwen.fallback.";
    private static final String CLI_ARGS = "agent.cli.qwen.args";
    private static final String CLI_PREFIX_WINDOWS = "agent.cli.qwen.prefix.windows";

    private static final Pattern ENV_VAR_PATTERN = Pattern.compile("\\$\\{env\\.([^}]+)\\}");
    private static final Pattern SYS_PROP_PATTERN = Pattern.compile("\\$\\{([^}]+)\\}");

    private AgentRunnerProperties() {
    }

    /**
     * Загружает настройки из classpath (агент-раннер.properties) или из файловой системы.
     * @return загруженные Properties
     */
    public static Properties loadDefault() {
        Properties classpathProperties = loadClasspathProperties();
        if (classpathProperties != null) {
            log.info("Настройки агента-раннера загружены из classpath: {}", DEFAULT_PROPERTIES_FILE);
            return classpathProperties;
        }

        Path propertiesPath = resolveDefaultPropertiesPath();
        log.info("Настройки агента-раннера загружены из файла: {}", propertiesPath.toAbsolutePath());
        return loadFromFile(propertiesPath);
    }

    private static Properties loadFromFile(Path propertiesPath) {
        if (!Files.isRegularFile(propertiesPath)) {
            throw new AgentRunnerConfigurationException(
                    "Файл настроек агента-раннера не найден: "
                            + propertiesPath.toAbsolutePath()
                            + ". Укажите свойство '"
                            + CLI_PROPERTY
                            + "' для выбора CLI."
            );
        }

        Properties properties = new Properties();
        try (InputStream inputStream = Files.newInputStream(propertiesPath)) {
            properties.load(inputStream);
        } catch (IOException e) {
            throw new AgentRunnerConfigurationException("Ошибка чтения файла настроек агента-раннера: " + propertiesPath, e);
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
                    "Ошибка чтения настроек агента-раннера из classpath: " + DEFAULT_PROPERTIES_FILE,
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

    /**
     * Возвращает список fallback-путей для указанной ОС с подстановкой переменных.
     * Пути в настройках разделяются точкой с запятой {@code ;}.
     *
     * @param props загруженные настройки
     * @param os    тип ОС
     * @return список путей (может быть пустым)
     */
    public static List<Path> getFallbackPaths(Properties props, OsType os) {
        String key = CLI_FALLBACK_PREFIX + os.name().toLowerCase();
        String raw = props.getProperty(key);
        if (raw == null || raw.isBlank()) {
            return List.of();
        }

        String expanded = expandVariables(raw);
        if (expanded.isBlank()) {
            return List.of();
        }

        return Arrays.stream(expanded.split(";"))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(Path::of)
                .collect(Collectors.toList());
    }

    /**
     * Возвращает список базовых аргументов для запуска CLI.
     * Аргументы в настройках разделяются запятой {@code ,}.
     *
     * @param props загруженные настройки
     * @return список аргументов (может быть пустым)
     */
    public static List<String> getBaseArgs(Properties props) {
        String value = props.getProperty(CLI_ARGS);
        if (value == null || value.isBlank()) {
            log.debug("Базовые аргументы CLI не заданы, используется пустой список");
            return List.of();
        }
        List<String> args = Arrays.stream(value.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
        log.debug("Загружены базовые аргументы CLI: {}", args);
        return args;
    }

    /**
     * Возвращает список префиксов команды для указанной ОС.
     * Аргументы в настройках разделяются запятой {@code ,}.
     *
     * @param props загруженные настройки
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
            log.debug("Префикс команды для Windows не задан");
            return List.of();
        }
        List<String> prefix = Arrays.stream(value.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
        log.debug("Загружен префикс команды для Windows: {}", prefix);
        return prefix;
    }

    /**
     * Подставляет системные свойства и переменные окружения в строку.
     * Поддерживаются:
     *   ${user.home}            -> System.getProperty("user.home")
     *   ${env.HOME}             -> System.getenv("HOME")
     *   ${env.USERPROFILE}      -> System.getenv("USERPROFILE")
     *   ${HOME}                 -> System.getenv("HOME")  (упрощённый синтаксис)
     *
     * @param value исходная строка с переменными
     * @return строка с заменёнными переменными
     */
    private static String expandVariables(String value) {
        if (value == null) {
            return null;
        }

        log.debug("Подстановка переменных в строку: {}", value);

        // 1. Заменяем ${env.XXX} на переменные окружения
        Matcher envMatcher = ENV_VAR_PATTERN.matcher(value);
        StringBuffer sb = new StringBuffer();
        while (envMatcher.find()) {
            String envVar = envMatcher.group(1);
            String envValue = System.getenv(envVar);
            if (envValue != null) {
                log.debug("Переменная окружения ${env.{}} заменена на '{}'", envVar, envValue);
                // Экранируем обратные слеши для Windows
                envMatcher.appendReplacement(sb, envValue.replace("\\", "\\\\"));
            } else {
                log.debug("Переменная окружения ${env.{}} не найдена, удаляем", envVar);
                envMatcher.appendReplacement(sb, "");
            }
        }
        envMatcher.appendTail(sb);
        String expanded = sb.toString();

        // 2. Заменяем ${XXX} на системные свойства (кроме уже обработанных env.)
        Matcher propMatcher = SYS_PROP_PATTERN.matcher(expanded);
        sb = new StringBuffer();
        while (propMatcher.find()) {
            String key = propMatcher.group(1);
            String valueReplacement = System.getProperty(key);
            if (valueReplacement != null) {
                log.debug("Системное свойство ${{}} заменено на '{}'", key, valueReplacement);
                propMatcher.appendReplacement(sb, valueReplacement.replace("\\", "\\\\"));
            } else {
                log.debug("Системное свойство ${{}} не найдено, удаляем", key);
                propMatcher.appendReplacement(sb, "");
            }
        }
        propMatcher.appendTail(sb);
        expanded = sb.toString();

        // 3. Упрощённые замены для часто используемых переменных (без фигурных скобок)
        if (expanded.contains("$HOME")) {
            String home = System.getenv("HOME");
            if (home != null) {
                log.debug("Переменная $HOME заменена на '{}'", home);
                expanded = expanded.replace("$HOME", home);
            }
        }
        if (expanded.contains("$USERPROFILE")) {
            String userProfile = System.getenv("USERPROFILE");
            if (userProfile != null) {
                log.debug("Переменная $USERPROFILE заменена на '{}'", userProfile);
                expanded = expanded.replace("$USERPROFILE", userProfile);
            }
        }

        log.debug("Результат подстановки переменных: {}", expanded);
        return expanded;
    }
}