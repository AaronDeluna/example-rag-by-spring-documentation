# AgentRunner

`AgentRunner` это интерфейс, который предоставляет возможность запускать CLI-агента из Java.

Идея простая: Java-код не должен знать детали конкретной CLI. Он просто говорит агенту:

- выполни обычный пользовательский prompt
- выполни prompt через явно указанный skill

Сейчас контракт такой:

```java
public interface AgentRunner {
    AgentResultDto executeUserPrompt(String prompt) throws Exception;

    AgentResultDto executeSkillPrompt(String skillName, String prompt) throws Exception;
}
```

## Что тут изобретено

Текущая реализация называется `QwenAgentRunner`.

Она запускает `qwen` как внешний процесс и возвращает результат выполнения в `AgentResultDto`.

Команда собирается примерно так:

```bash
qwen --output-format stream-json --approval-mode yolo "<prompt>"
```

Почему так:

- `stream-json` нужен, чтобы потом можно было парсить события агента: thinking, tool_use, tool_result, result, ошибки
- `yolo` нужен, чтобы CLI не зависала на подтверждениях действий
- каждый вызов сейчас независимый: один prompt = один запуск процесса

## CommandExecutor

`CommandExecutor` это отдельный класс, который ничего не знает про Qwen, Codex, skills и агентов.

Его задача только такая:

```text
CommandRequestDto -> запустить процесс -> CommandResultDto
```

Он отвечает за:

- сборку `CommandLine`
- рабочую директорию
- timeout
- stdout
- stderr
- exitCode
- признак timedOut

То есть `QwenAgentRunner` знает, какую команду надо собрать для Qwen, а `CommandExecutor` знает только как эту команду выполнить.

## DTO

DTO сделаны обычными классами через Lombok, без `record`.

Основные DTO:

- `CommandRequestDto`
- `CommandResultDto`
- `AgentResultDto`

`CommandResultDto` это низкоуровневый результат запуска команды.

`AgentResultDto` это результат запуска агента. Сейчас он почти повторяет `CommandResultDto`, но это нормально: дальше в `AgentResultDto` можно будет добавить уже агентские поля, например `finalResult`, `events`, `toolCalls`.

## Skills

Важный момент: skills мы не подмешиваем руками в prompt.

Qwen Code сам умеет искать skills, если они лежат в правильном месте:

```text
.qwen/skills/<skill-name>/SKILL.md
```

Так как тесты запускаются из папки:

```text
skills
```

то project skills для этих тестов лежат здесь:

```text
skills/.qwen/skills/<skill-name>/SKILL.md
```

Примеры:

```text
skills/.qwen/skills/arithmetic/SKILL.md
skills/.qwen/skills/chain-check/SKILL.md
```

`SKILL.md` должен иметь YAML frontmatter:

```yaml
---
name: chain-check
description: Verifies that a multi-step skill workflow is followed exactly by emitting required chain markers.
---
```

Без `name` и `description` Qwen может не увидеть skill нормально.

## Как вызывается skill

В интерактивном Qwen можно написать:

```text
/skills chain-check
```

Но в one-shot режиме через Java это не работает:

```text
The command "/skills" is not supported in this mode.
```

При этом Qwen регистрирует project skills как отдельные slash commands.

Поэтому из Java вызываем так:

```text
/chain-check Проверь цепочку skill workflow...
```

В коде это выглядит так:

```java
return executeUserPrompt("/" + skillName + " " + prompt);
```

## Тесты

Сейчас есть три теста.

Обычный prompt:

```java
agentRunner.executeUserPrompt("Верни 1 ответ: сколько будет 2 + 2");
```

Prompt через простой skill:

```java
agentRunner.executeSkillPrompt(
        "arithmetic",
        "как считать 2 + 2 * 2"
);
```

Проверка цепочки skill workflow:

```java
agentRunner.executeSkillPrompt(
        "chain-check",
        "Проверь цепочку skill workflow и верни все обязательные маркеры."
);
```

`chain-check` нужен не для реальной пользы, а чтобы проверить, что агент реально следует инструкции skill-а.

Он должен вернуть маркеры:

```text
CHAIN_STEP_1_READ_TASK
CHAIN_STEP_2_TRANSFORM_TASK
CHAIN_STEP_3_FINAL_ANSWER
CHAIN_SKILL_DONE
```

## Что уже понятно из логов

Рабочий запуск показал:

- Qwen видит локальную модель `carstenuhlig/omnicoder-9b:q4_k_m`
- Qwen видит project skills как slash commands: `arithmetic`, `chain-check`
- `/skills chain-check` в one-shot режиме не работает
- `/chain-check ...` это правильный формат для Java one-shot запуска
- `stream-json` уже отдаёт события, которые можно парсить

## Stream JSON parser

`qwen --output-format stream-json` возвращает не один JSON-документ, а последовательность JSON-объектов:

```json
{"type":"system", "...":"..."}
{"type":"assistant", "...":"..."}
{"type":"result", "result":"..."}
```

В таком виде это удобно читать как поток событий, но это невалидный JSON-файл, потому что сверху нет массива.

Для нормализации добавлен `AgentStreamJsonParser`. Он читает последовательность объектов и превращает её в валидный JSON-массив:

```json
[
  {"type":"system", "...":"..."},
  {"type":"assistant", "...":"..."},
  {"type":"result", "result":"..."}
]
```

`AgentResultDto` теперь хранит:

- `stdout` — сырой вывод CLI, как пришёл от Qwen
- `events` — события как настоящий JSON-массив для отдачи наружу
- `eventsJson` — тот же массив, но строкой pretty-print для логов/отладки
- `finalResult` — значение поля `result` из последнего события `type=result`

Теперь вместо проверки raw `stdout` можно проверять нормализованные поля:

```java
assertTrue(result.getFinalResult().contains("CHAIN_SKILL_DONE"));
```

## Скилы для древовидного сценария

Для проверки древовидного сценария добавлены три project skills:

```text
skills/.qwen/skills/topic-tree-root/SKILL.md
skills/.qwen/skills/topic-tree-branch/SKILL.md
skills/.qwen/skills/topic-tree-leaf/SKILL.md
```

Идея цепочки:

```text
topic-tree-root
  -> topic-tree-branch
      -> topic-tree-leaf
```

Запуск через Java:

```java
AgentResultDto result = agentRunner.executeSkillPrompt(
        "topic-tree-root",
        "Объясни тему HTTP кэширование как дерево."
);
```

Или напрямую через CLI:

```bash
qwen --output-format stream-json --approval-mode yolo \
  "/topic-tree-root Объясни тему HTTP кэширование как дерево."
```

В `stream-json` нужно смотреть:

- `assistant.message.content[].type=tool_use` с `name=skill`
- `input.skill=topic-tree-branch`
- следующий `tool_use` с `input.skill=topic-tree-leaf`
- финальный `type=result`

Финальный ответ должен содержать маркеры:

```text
TREE_ROOT_ENTER
TREE_EDGE_ROOT_TO_BRANCH
TREE_BRANCH_ENTER
TREE_BRANCH_SPLIT_DONE
TREE_EDGE_BRANCH_TO_LEAF
TREE_LEAF_ENTER
TREE_LEAF_EXAMPLES_DONE
TREE_LEAF_DONE
TREE_BRANCH_DONE
TREE_ROOT_DONE
```

По этим маркерам можно проверить не только финальный ответ, но и порядок переходов между скилами.
