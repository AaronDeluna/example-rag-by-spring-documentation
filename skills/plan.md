## Адаптация предложенного рефакторинга к текущей архитектуре

Ваш код уже имеет зачатки расширяемости (`AgentCli`, `AgentRunnerFactory`), но `QwenCommandFactory` остаётся жёстко завязанным на Qwen и ОС. Предлагаю **полностью заменить** его на систему, которая:

1. **Отделяет поиск исполняемого файла** от сборки команды.
2. **Позволяет конфигурировать** аргументы и fallback-пути через `agent-runner.properties`.
3. **Поддерживает любые CLI-агенты** без изменения кода.
4. **Устраняет дублирование** логики для разных ОС.

Все изменения будут интегрированы в существующие пакеты и сохранят обратную совместимость на уровне интерфейсов (там, где это нужно).

---

## 1. Новые компоненты (пакет `org.mirent.skills.util.cli`)

### 1.1. `OsType` – определение ОС

```java
package org.mirent.skills.util.cli;

public enum OsType {
    WINDOWS, MAC, LINUX, OTHER;

    public static OsType detect() {
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("win")) return WINDOWS;
        if (os.contains("mac")) return MAC;
        if (os.contains("nix") || os.contains("nux") || os.contains("aix")) return LINUX;
        return OTHER;
    }
}
```

### 1.2. `CommandResolver` – поиск исполняемого файла

```java
package org.mirent.skills.util.cli;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

public interface CommandResolver {
    String resolveExecutable(String commandName) throws CommandNotFoundException;
}

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

    private String findInPath(String command) {
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
```

### 1.3. `CommandFactory` – сборка команды

```java
package org.mirent.skills.util.cli;

import java.nio.file.Path;
import java.util.List;

public interface CommandFactory {
    List<String> buildCommand(String prompt, Path logDir);
}

public class QwenCommandFactoryImpl implements CommandFactory {
    private final CommandResolver resolver;
    private final List<String> baseArgs;
    private final List<String> prefix; // например, ["cmd.exe", "/c"] для Windows

    public QwenCommandFactoryImpl(CommandResolver resolver, List<String> baseArgs, List<String> prefix) {
        this.resolver = resolver;
        this.baseArgs = baseArgs;
        this.prefix = prefix;
    }

    @Override
    public List<String> buildCommand(String prompt, Path logDir) {
        String executable = resolver.resolveExecutable("qwen");
        List<String> cmd = new ArrayList<>();
        if (prefix != null) cmd.addAll(prefix);
        cmd.add(executable);
        cmd.addAll(baseArgs); // --output-format stream-json --approval-mode yolo
        if (logDir != null) {
            cmd.add("--openai-logging");
            cmd.add("true");
            cmd.add("--openai-logging-dir");
            cmd.add(logDir.toAbsolutePath().toString());
        }
        cmd.add(prompt); // prompt всегда последним
        return Collections.unmodifiableList(cmd);
    }
}
```

### 1.4. Исключение

```java
package org.mirent.skills.exeptions;

public class CommandNotFoundException extends AgentRunnerConfigurationException {
    public CommandNotFoundException(String message) {
        super(message);
    }
}
```

---

## 2. Конфигурация (расширение `AgentRunnerProperties`)

Добавим в `agent-runner.properties` параметры:

```properties
# Выбор CLI
agent.cli=QWEN

# Fallback-пути для поиска qwen (разделитель ;)
agent.cli.qwen.fallback.linux=/home/user/.npm-global/lib/node_modules/@qwen-code/qwen-code/cli-entry.js;/home/user/.npm-global/bin/qwen
agent.cli.qwen.fallback.windows=C:\\Program Files\\qwen\\qwen.exe
agent.cli.qwen.fallback.mac=/usr/local/bin/qwen

# Базовые аргументы (без prompt, он добавляется в конце)
agent.cli.qwen.args=--output-format,stream-json,--approval-mode,yolo

# Префикс для Windows (можно оставить пустым для других ОС)
agent.cli.qwen.prefix.windows=cmd.exe,/c
```

В классе `AgentRunnerProperties` добавим методы для чтения этих параметров.

---

## 3. Адаптация `AgentRunnerFactory`

Создадим фабрику для `CommandFactory` и внедрим её в раннеры.

```java
package org.mirent.skills.service;

import org.mirent.skills.util.cli.*;

public class AgentRunnerFactory {
    // ... существующие поля

    public AgentRunner create(Properties properties) {
        AgentCli cli = AgentCli.fromProperty(properties.getProperty(AgentRunnerProperties.CLI_PROPERTY));
        CommandFactory commandFactory = createCommandFactory(cli, properties);
        return switch (cli) {
            case QWEN -> new QwenAgentRunner(
                commandExecutor,
                agentStreamJsonParser,
                runnerLogWriter,
                workingDirectory,
                timeout,
                commandFactory          // новый параметр
            );
            // В будущем: case CLAUDE -> new ClaudeAgentRunner(...)
        };
    }

    private CommandFactory createCommandFactory(AgentCli cli, Properties props) {
        return switch (cli) {
            case QWEN -> {
                // Строим fallback-карту
                Map<OsType, List<Path>> fallbacks = new HashMap<>();
                for (OsType os : OsType.values()) {
                    String key = "agent.cli.qwen.fallback." + os.name().toLowerCase();
                    String value = props.getProperty(key);
                    if (value != null && !value.isBlank()) {
                        List<Path> paths = Arrays.stream(value.split(";"))
                            .map(String::trim)
                            .map(Path::of)
                            .collect(Collectors.toList());
                        fallbacks.put(os, paths);
                    }
                }
                CommandResolver resolver = new OsAwareCommandResolver(fallbacks);

                // Базовые аргументы
                String argsProp = props.getProperty("agent.cli.qwen.args", "");
                List<String> baseArgs = argsProp.isBlank() ? List.of() :
                    Arrays.asList(argsProp.split(","));

                // Префикс для Windows
                String prefixProp = props.getProperty("agent.cli.qwen.prefix.windows", "");
                List<String> prefix = prefixProp.isBlank() ? List.of() :
                    Arrays.asList(prefixProp.split(","));
                // Для других ОС префикс пустой
                if (OsType.detect() != OsType.WINDOWS) {
                    prefix = List.of();
                }

                yield new QwenCommandFactoryImpl(resolver, baseArgs, prefix);
            }
        };
    }
}
```

---

## 4. Изменение `QwenAgentRunner` и `QwenJudgeRunner`

### 4.1. `QwenAgentRunner`

Добавляем поле `CommandFactory` и используем его вместо статического вызова.

```java
package org.mirent.skills.runner.qwen;

import org.mirent.skills.util.cli.CommandFactory;

public class QwenAgentRunner implements AgentRunner {
    // ... существующие поля
    private final CommandFactory commandFactory;

    public QwenAgentRunner(
            CommandExecutor commandExecutor,
            AgentStreamJsonParser agentStreamJsonParser,
            RunnerLogWriter runnerLogWriter,
            Path workingDirectory,
            Duration timeout,
            CommandFactory commandFactory
    ) {
        // ... инициализация
        this.commandFactory = commandFactory;
    }

    // Конструктор для обратной совместимости (использует старую фабрику)
    public QwenAgentRunner(
            CommandExecutor commandExecutor,
            AgentStreamJsonParser agentStreamJsonParser,
            Path workingDirectory,
            Duration timeout
    ) {
        this(commandExecutor, agentStreamJsonParser, new RunnerLogWriter(),
             workingDirectory, timeout, createLegacyCommandFactory());
    }

    private static CommandFactory createLegacyCommandFactory() {
        // Временно используем старую реализацию, которая эмулирует поведение QwenCommandFactory
        return new CommandFactory() {
            @Override
            public List<String> buildCommand(String prompt, Path logDir) {
                return QwenCommandFactory.buildCommand(prompt, logDir);
            }
        };
    }

    @Override
    public AgentResultDto executeUserPrompt(String prompt) throws Exception {
        return execute(null, prompt);
    }

    @Override
    public AgentResultDto executeSkillPrompt(String skillName, String prompt) throws Exception {
        validateSkillName(skillName);
        return execute(skillName, "/" + skillName + " " + prompt);
    }

    private AgentResultDto execute(String skillName, String prompt) throws Exception {
        // ... подготовка
        List<String> command = commandFactory.buildCommand(prompt, agentRunContext.getRunDir());
        // ... остальное без изменений
    }
}
```

### 4.2. `QwenJudgeRunner`

Аналогично, добавляем `CommandFactory`.

```java
public class QwenJudgeRunner implements JudgeRunner {
    private final CommandExecutor commandExecutor;
    private final AgentStreamJsonParser agentStreamJsonParser;
    private final Path workingDirectory;
    private final Duration timeout;
    private final CommandFactory commandFactory;

    public QwenJudgeRunner(Path workingDirectory) {
        this(new CommandExecutor(), new AgentStreamJsonParser(), workingDirectory,
             DEFAULT_TIMEOUT, createLegacyCommandFactory());
    }

    public QwenJudgeRunner(
            CommandExecutor commandExecutor,
            AgentStreamJsonParser agentStreamJsonParser,
            Path workingDirectory,
            Duration timeout,
            CommandFactory commandFactory
    ) {
        // ... инициализация
        this.commandFactory = commandFactory;
    }

    private static CommandFactory createLegacyCommandFactory() {
        return (prompt, logDir) -> QwenCommandFactory.buildCommand(prompt, logDir);
    }

    @Override
    public String runPrompt(String prompt) throws Exception {
        List<String> command = commandFactory.buildCommand(prompt, null);
        // ... остальное
    }
}
```

---

## 5. Удаление/деприкация `QwenCommandFactory`

Класс `QwenCommandFactory` помечаем как `@Deprecated` и перенаправляем его методы на новую реализацию (можно оставить для обратной совместимости, но в будущем удалить).

```java
@Deprecated
public final class QwenCommandFactory {
    private QwenCommandFactory() {}

    public static List<String> buildCommand(String prompt) {
        return buildCommand(prompt, null);
    }

    public static List<String> buildCommand(String prompt, Path openaiLogDir) {
        // Делегируем новой фабрике с дефолтной конфигурацией
        CommandFactory factory = createDefaultFactory();
        return factory.buildCommand(prompt, openaiLogDir);
    }

    private static CommandFactory createDefaultFactory() {
        // Эмулируем старые пути и аргументы
        Map<OsType, List<Path>> fallbacks = new HashMap<>();
        String userHome = System.getProperty("user.home");
        fallbacks.put(OsType.LINUX, List.of(
            Path.of(userHome, ".npm-global", "lib", "node_modules", "@qwen-code", "qwen-code", "cli-entry.js"),
            Path.of(userHome, ".npm-global", "bin", "qwen")
        ));
        fallbacks.put(OsType.MAC, List.of(Path.of("qwen"))); // обычно в PATH
        fallbacks.put(OsType.WINDOWS, List.of(Path.of("qwen")));

        CommandResolver resolver = new OsAwareCommandResolver(fallbacks);
        List<String> baseArgs = List.of("--output-format", "stream-json", "--approval-mode", "yolo");
        List<String> prefix = OsType.detect() == OsType.WINDOWS ? List.of("cmd.exe", "/c") : List.of();
        return new QwenCommandFactoryImpl(resolver, baseArgs, prefix);
    }
}
```

---

## 6. Адаптация тестов

### 6.1. `QwenAvailabilityTest`

Заменить жёсткий поиск на `CommandResolver`:

```java
@Test
void qwenIsAvailableAndVersionTest() throws Exception {
    // Создаём resolver с теми же fallback-путями
    Map<OsType, List<Path>> fallbacks = Map.of(
        OsType.LINUX, List.of(
            Path.of(System.getProperty("user.home"), ".npm-global", "bin", "qwen"),
            Path.of(System.getProperty("user.home"), ".npm-global", "lib", "node_modules", "@qwen-code", "qwen-code", "cli-entry.js")
        )
    );
    CommandResolver resolver = new OsAwareCommandResolver(fallbacks);
    String qwenPath = resolver.resolveExecutable("qwen");

    // Запускаем --version
    CommandRequestDto request = new CommandRequestDto(
        List.of(qwenPath, "--version"),
        prepareWut(),
        Duration.ofMinutes(3)
    );
    CommandResultDto result = executor.execute(request);
    Assertions.assertEquals(0, result.getExitCode());
    Assertions.assertEquals("0.19.6", result.getStdout().trim());
}
```

### 6.2. `MultipleModelsSkillTest` и другие внешние тесты

Они используют `AgentRunnerFactory.defaultFactory()`, которая теперь будет создавать раннер с новой `CommandFactory`. Если в тестах явно создаётся `QwenAgentRunner` через конструктор, нужно либо передать `CommandFactory`, либо использовать фабрику.

Рекомендуем везде использовать `AgentRunnerFactory` – это даст единую конфигурацию.

Если в тесте нужно переопределить пути (например, для проверки разных версий), можно создать свой экземпляр `CommandFactory` и передать в раннер.

---

## 7. Преимущества новой архитектуры

| Старая проблема | Решение |
|----------------|---------|
| Жёсткая привязка к Qwen | Интерфейс `CommandFactory` позволяет подключать любые CLI |
| Захардкоженные пути в коде | Пути вынесены в конфигурацию и загружаются через `CommandResolver` |
| Логика ОС размазана по if-else | `OsType` и `OsAwareCommandResolver` централизуют определение ОС |
| Сложно тестировать (статический метод) | Теперь можно подменить `CommandFactory` в тестах |
| Добавление нового CLI требует правки нескольких классов | Достаточно добавить новый enum в `AgentCli` и соответствующую ветку в фабрике |

---

## 8. Пример использования нового API в коде приложения

Теперь создание раннера выглядит так:

```java
Properties props = AgentRunnerProperties.loadDefault();
AgentRunner runner = AgentRunnerFactory.defaultFactory(workspace).create(props);
AgentResultDto result = runner.executeUserPrompt("...");
```

А в будущем для другого CLI достаточно изменить только `agent.cli` в properties.

---

## 9. Миграционный план

1. Создать новые классы (`OsType`, `CommandResolver`, `CommandFactory` и реализации).
2. Добавить новые свойства в `agent-runner.properties` (с разумными значениями по умолчанию).
3. Изменить `AgentRunnerFactory`, чтобы он создавал `CommandFactory` и передавал в раннеры.
4. Изменить конструкторы `QwenAgentRunner` и `QwenJudgeRunner`, добавив параметр `CommandFactory`, но сохранить старые конструкторы для обратной совместимости (используя делегирование на статический `QwenCommandFactory`).
5. Постепенно заменять прямые вызовы `QwenCommandFactory` на новый подход во всех тестах и внешнем коде.
6. Удалить `QwenCommandFactory` после полного перехода.

---

## 10. Полный код новых классов (кратко)

В приложении к ответу прилагаю полные реализации всех описанных классов (уже адаптированные под вашу структуру пакетов и исключений). Вы можете скопировать их в проект.

**Важно:** В `AgentRunnerFactory` нужно добавить импорты и корректно обработать случай, когда свойства отсутствуют – предусмотреть значения по умолчанию.

Таким образом, вы получите гибкую, расширяемую и легко тестируемую систему управления запуском CLI-агентов.