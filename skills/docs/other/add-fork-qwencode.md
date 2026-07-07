## Инструкция по добавлению форка QwenCode CLI

Если новый CLI-агент является **полным форком QwenCode** (аналогичный формат вывода, те же флаги, но другое имя исполняемого файла и пути), то добавление сводится к **минимальному конфигурированию** без написания нового кода парсинга или раннера. Основные шаги:

1. **Добавить новое значение в `AgentCli`**.
2. **Создать параметризованную `CommandFactory`** (или расширить существующую), чтобы можно было задать имя исполняемого файла.
3. **Добавить ветку в `AgentRunnerFactory`** для создания раннера с этой фабрикой.
4. **Настроить конфигурацию** в `agent-runner.properties`.
5. **Запустить тесты**, изменив `agent.cli`.

Ниже подробно описан каждый шаг.

---

### 1. Добавить новый тип в перечисление `AgentCli`

**Файл:** `src/main/java/org/mirent/skills/service/AgentCli.java`

Добавьте значение, например `FORKED_QWEN`:

```java
public enum AgentCli {
    QWEN,
    FORKED_QWEN;   // <-- ваш форк
}
```

---

### 2. Создать фабрику команд для форка с параметризуемым именем

Поскольку имя исполняемого файла у форка может отличаться (например, `my-qwen-cli`), нужно создать реализацию `CommandFactory`, которая принимает имя в конструкторе. Можно скопировать `QwenCommandFactoryImpl` и добавить поле `commandName`.

**Создать класс:** `src/main/java/org/mirent/skills/util/cli/ForkedQwenCommandFactoryImpl.java`

```java
package org.mirent.skills.util.cli;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ForkedQwenCommandFactoryImpl implements CommandFactory {

    private final CommandResolver resolver;
    private final List<String> baseArgs;
    private final List<String> prefix;
    private final String commandName;  // имя исполняемого файла

    public ForkedQwenCommandFactoryImpl(CommandResolver resolver, List<String> baseArgs, List<String> prefix, String commandName) {
        this.resolver = resolver;
        this.baseArgs = baseArgs;
        this.prefix = prefix;
        this.commandName = commandName;
    }

    @Override
    public List<String> buildCommand(String prompt, Path logDir) {
        // Теперь ищем по commandName
        String executable = resolver.resolveExecutable(commandName);
        List<String> cmd = new ArrayList<>();
        if (prefix != null) cmd.addAll(prefix);
        cmd.add(executable);
        if (baseArgs != null) cmd.addAll(baseArgs);
        if (logDir != null) {
            cmd.add("--openai-logging");
            cmd.add("true");
            cmd.add("--openai-logging-dir");
            cmd.add(logDir.toAbsolutePath().toString());
        }
        cmd.add(prompt);
        return Collections.unmodifiableList(cmd);
    }
}
```

---

### 3. Расширить `AgentRunnerFactory` для поддержки форка

**Файл:** `src/main/java/org/mirent/skills/service/AgentRunnerFactory.java`

#### 3.1. Добавить ветку в `createCommandFactory()`

Создайте вспомогательный метод для чтения свойств форка и возврата `CommandFactory`:

```java
static CommandFactory createCommandFactory(AgentCli cli, Properties props) {
    return switch (cli) {
        case QWEN -> {
            // текущая логика для Qwen
            ...
        }
        case FORKED_QWEN -> {
            String commandName = props.getProperty("agent.forked-qwen.command", "my-qwen-cli");
            Map<OsType, List<Path>> fallbacks = buildFallbackMap(props, "forked-qwen");
            OsAwareCommandResolver resolver = new OsAwareCommandResolver(fallbacks);
            List<String> baseArgs = getBaseArgs(props, "forked-qwen");
            List<String> prefix = getPrefix(props, "forked-qwen", OsType.detect());
            yield new ForkedQwenCommandFactoryImpl(resolver, baseArgs, prefix, commandName);
        }
    };
}

// Вспомогательные методы (можно вынести в отдельный утилитный класс)
private static Map<OsType, List<Path>> buildFallbackMap(Properties props, String agentPrefix) {
    Map<OsType, List<Path>> map = new HashMap<>();
    for (OsType os : OsType.values()) {
        String key = "agent." + agentPrefix + ".fallback." + os.name().toLowerCase();
        String raw = props.getProperty(key);
        if (raw != null && !raw.isBlank()) {
            List<Path> paths = Arrays.stream(raw.split(";"))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .map(Path::of)
                    .collect(Collectors.toList());
            if (!paths.isEmpty()) map.put(os, paths);
        }
    }
    return map;
}

private static List<String> getBaseArgs(Properties props, String agentPrefix) {
    String value = props.getProperty("agent." + agentPrefix + ".args");
    if (value == null || value.isBlank()) return List.of();
    return Arrays.stream(value.split(","))
            .map(String::trim)
            .filter(s -> !s.isEmpty())
            .collect(Collectors.toList());
}

private static List<String> getPrefix(Properties props, String agentPrefix, OsType os) {
    if (os != OsType.WINDOWS) return List.of();
    String value = props.getProperty("agent." + agentPrefix + ".prefix.windows");
    if (value == null || value.isBlank()) return List.of();
    return Arrays.stream(value.split(","))
            .map(String::trim)
            .filter(s -> !s.isEmpty())
            .collect(Collectors.toList());
}
```

#### 3.2. Добавить ветку в `create()`

```java
public AgentRunner create(Properties properties) {
    AgentCli cli = AgentCli.fromProperty(properties.getProperty(AgentRunnerProperties.CLI_PROPERTY));
    log.info("Запуск через CLI: {}", cli);

    CommandFactory commandFactory = createCommandFactory(cli, properties);

    return switch (cli) {
        case QWEN -> new QwenAgentRunner(
                commandExecutor,
                agentStreamJsonParser,
                runnerLogWriter,
                workingDirectory,
                timeout,
                commandFactory
        );
        case FORKED_QWEN -> new QwenAgentRunner(  // используем тот же раннер
                commandExecutor,
                agentStreamJsonParser,
                runnerLogWriter,
                workingDirectory,
                timeout,
                commandFactory
        );
    };
}
```

---

### 4. Добавить конфигурацию в `agent-runner.properties`

Добавьте секцию для форка в файлы `src/main/resources/agent-runner.properties` и `src/test/resources/agent-runner.properties`:

```properties
# Forked Qwen CLI configuration
agent.forked-qwen.command=my-qwen-cli
agent.forked-qwen.args=--output-format,stream-json,--approval-mode,yolo
agent.forked-qwen.fallback.linux=${user.home}/.my-qwen/bin/my-qwen-cli
agent.forked-qwen.fallback.mac=${user.home}/.my-qwen/bin/my-qwen-cli
agent.forked-qwen.fallback.windows=${env.USERPROFILE}\\.my-qwen\\bin\\my-qwen-cli.exe
agent.forked-qwen.prefix.windows=cmd.exe,/c
```

Измените `agent.cli=FORKED_QWEN`, чтобы активировать новый агент.

---

### 5. Написать тест

Создайте тестовый класс, аналогичный существующим, но не создавайте новых парсеров или раннеров – всё работает через фабрику.

```java
package org.mirent.skills.tests.external;

import org.junit.jupiter.api.Test;
import org.mirent.skills.dto.agent.AgentResultDto;
import org.mirent.skills.service.AgentRunnerFactory;
import org.mirent.skills.service.AgentRunnerProperties;
import org.mirent.skills.util.WutPreparer;
import static org.mirent.skills.matcher.AgentMatcher.*;

class ForkedQwenTest {

    @Test
    void testArithmetic() throws Exception {
        Path wut = WutPreparer.builder()
                .wutSourceName("default")
                .wutSourcePath(Path.of("src/test/resources/wut-templates"))
                .build()
                .prepare();

        // Фабрика прочитает agent.cli=FORKED_QWEN и подставит нужную CommandFactory
        AgentRunner runner = AgentRunnerFactory.defaultFactory(wut)
                .create(AgentRunnerProperties.loadDefault());

        AgentResultDto result = runner.executeUserPrompt(
                "Верни 1 ответ: сколько будет 2 + 2 используй skills arithmetic"
        );

        assertSuccessful(result);
        assertSingleSkillCall(result, "arithmetic");
        // Можно также проверить finalResult
        assertTrue(result.getFinalResult().contains("4"));
    }
}
```

---

### 6. Адаптация существующих тестов для запуска с форком

Вместо создания нового теста можно параметризовать существующие тесты, передавая имя CLI через системное свойство или через `@ParameterizedTest` с разными значениями. Например, в `MultipleModelsQwenTest` можно заменить модель на имя форка, но это уже вопрос дизайна тестов.

---

### 7. Что делать, если путь к папкам отличается?

Если форк использует другие пути для скилов (например, не `.qwen`, а `.my-agent`), то нужно адаптировать `QwenSettingsUpdater` и структуру WUT. Но по условию «ничем не отличается по сути», следовательно, пути остаются теми же (`.qwen`). Если же отличаются, потребуется создать аналог `QwenSettingsUpdater` с другими именами папок.

---

### 8. Итог

- **Нет необходимости** писать новый раннер или парсер – используется `QwenAgentRunner` и `AgentStreamJsonParser`.
- **Единственное изменение** – создание `ForkedQwenCommandFactoryImpl` с параметром имени команды.
- **Настройка** – через properties задаётся имя исполняемого файла, fallback-пути и аргументы.
- **Тестирование** – просто изменяем `agent.cli` и запускаем тесты.

Это минимальный путь для интеграции форка QwenCode.