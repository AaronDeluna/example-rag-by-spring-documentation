# Skills Test Framework

Библиотека для автоматизированного тестирования AI-агентов (Qwen Code CLI) и их скиллов.

---

## 📌 Основные возможности

- 🧪 **Запуск агента** – выполнение пользовательских промптов или прямых вызовов скиллов через CLI.
- 📁 **Подготовка рабочей области (WUT)** – копирование шаблонов проектов с предустановленными скиллами.
- 🔄 **Переключение моделей** – динамическая смена модели (Ollama) для параметризованных тестов.
- ✅ **Встроенные проверки** – валидация успешности выполнения, вызовов скиллов, состояния файловой системы.
- 📊 **Оценка ответов** – использование судьи (Judge) для выставления score и описания проблем.
- 📝 **Логирование** – сохранение каждого запуска в отдельную директорию с событиями в формате JSON.

---

## 🛠️ Требования

- **Java 11+**
- **Maven** или **Gradle** (для сборки)
- **Qwen Code CLI** – установлен глобально или локально (см. [инструкцию](https://github.com/QwenLM/qwen-code))
- **Модель** – доступная через Ollama (или другой провайдер, поддерживаемый Qwen)

---

## ⚙️ Настройка

1. Убедитесь, что Qwen CLI доступен из командной строки:
   ```bash
   qwen --version
   ```
   (для Linux/Mac путь обычно `~/.npm-global/lib/node_modules/@qwen-code/qwen-code/cli.js`)

2. Создайте файл `agent-runner.properties` в корне проекта или в папке `skills/`:
   ```properties
   agent.cli=QWEN
   ```
   (пока поддерживается только `QWEN`)

3. Разместите шаблоны рабочих областей (WUT) в папке `src/test/resources/wut-templates/`. Каждый шаблон – это папка с файлами проекта (включая `.qwen/skills/...`).

---

## 📁 Структура проекта (ключевые компоненты)

```
src/main/java/org/mirent/skills/
├── runner/                – интерфейсы и реализации раннеров (AgentRunner, JudgeRunner)
│   └── qwen/              – реализация для Qwen CLI (QwenAgentRunner, QwenJudgeRunner)
├── service/               – фабрики и сервисы (AgentRunnerFactory, AgentRunnerService, AgentEvaluatorService)
├── matcher/               – статические методы проверок (AgentMatcher)
├── util/                  – утилиты: WutPreparer, QwenSettingsUpdater, QwenCommandFactory
├── parser/                – парсер stream-json логов (AgentStreamJsonParser)
├── spec/                  – модель и рендерер скиллов (SkillSpec, QwenSkillRenderer)
├── dto/                   – DTO для команд, событий, результатов
└── exeptions/             – пользовательские исключения
```

---

## 🚀 Использование в тестах

### 1. Подготовка рабочей области

```java
Path workspace = WutPreparer.builder()
    .wutSourceName("default")                     // имя папки-шаблона
    .wutSourcePath(Path.of("src/test/resources/wut-templates"))
    .overwriteTarget(true)                        // перезаписывать существующую
    .build()
    .prepare();
```

### 2. Создание раннера и переключение модели

```java
AgentRunnerFactory factory = AgentRunnerFactory.defaultFactory(workspace);
QwenAgentRunner agentRunner = factory.create(AgentRunnerProperties.loadDefault());

QwenSettingsUpdater updater = QwenSettingsUpdater.builder()
    .agentRunContext(agentRunner.getAgentRunContext())
    .createSettingsIfMissing(true)
    .build();
updater.updateModelNameAndSave("qwen2.5:1.5b");  // имя модели в Ollama
```

### 3. Выполнение промпта

```java
AgentResultDto result = agentRunner.executeUserPrompt(
    "Настрой Checkstyle в Maven-проекте используй skills maven-checkstyle-setup"
);
```

### 4. Проверки

```java
// Успешное выполнение, без таймаута и с exitCode=0
AgentMatcher.assertSuccessful(result);

// Вызов ровно одного скилла с заданным именем
AgentMatcher.assertSingleSkillCall(result, "maven-checkstyle-setup");

// Проверка файлов
assertTrue(Files.exists(workspace.resolve("checkstyle.xml")));
assertTrue(Files.readString(workspace.resolve("pom.xml"))
    .contains("maven-checkstyle-plugin"));
```

### 5. Оценка ответа судьёй

```java
AgentEvaluatorService evaluator = new AgentEvaluatorService(workspace);
EvaluateResultDto evaluation = evaluator.evaluate(new EvaluateDto(
    query,                                    // исходный запрос
    result.getEventsJson()                    // JSON-лог событий агента
));

// Проверка, что score >= порога (например, 0.7)
AgentMatcher.evaluate(evaluation, 0.7);
```

---

## 📝 Примеры тестов

### Проверка вызова скилла `arithmetic` в шаблоне `default`

```java
@Test
void defaultTemplate_callsArithmetic() throws Exception {
    AgentRunner agent = new AgentRunnerService(prepareWut("default"));
    AgentResultDto result = agent.executeUserPrompt(
        "Верни 1 ответ: сколько будет 2 + 2 используй skills arithmetic"
    );
    assertSuccessful(result);
    assertSingleSkillCall(result, "arithmetic");
}
```

### Параметризованный тест с разными моделями

```java
@ParameterizedTest
@ArgumentsSource(ModelNamesProvider.class)
void executeUserPromptInvokesRequestedSkillsInOrder(String modelName) throws Exception {
    Path wut = WutPreparer.builder()
        .wutSourceName("default")
        .wutSourcePath(WUT_SOURCE)
        .overwriteTarget(true)
        .build()
        .prepare();

    AgentRunnerFactory factory = AgentRunnerFactory.defaultFactory(wut);
    QwenAgentRunner agentRunner = factory.create(AgentRunnerProperties.loadDefault());

    QwenSettingsUpdater updater = QwenSettingsUpdater.builder()
        .agentRunContext(agentRunner.getAgentRunContext())
        .createSettingsIfMissing(true)
        .build();
    updater.updateModelNameAndSave(modelName);

    AgentResultDto result = agentRunner.executeUserPrompt(
        "Верни 1 ответ: сколько будет 2 + 2 используй skills arithmetic"
    );
    assertSuccessful(result);
    assertSingleSkillCall(result, "arithmetic");
}
```

Полные примеры смотрите в тестах:
- `AgentRunnerTest` – сценарии для разных шаблонов (`default`, `text-utils`, `case-*`)
- `MultipleModelsQwenTest` – запуск на разных моделях
- `MultipleModelsSkillTest` – проверка скилла `maven-checkstyle-setup` на разных моделях

---

## 🧩 Расширение и кастомизация

- **Добавление нового CLI** – реализуйте интерфейсы `AgentRunner` и `JudgeRunner`, зарегистрируйте в `AgentRunnerFactory`.
- **Создание новых матчеров** – можно дополнить `AgentMatcher` или написать собственные проверки.
- **Новые шаблоны WUT** – поместите папку с проектом (включая скиллы) в `src/test/resources/wut-templates/`.
- **Кастомизация судьи** – переопределите промпт в `AgentEvaluatorService` или реализуйте свою логику оценки.

---

## ⚠️ Ограничения

- Поддерживается только **Qwen Code CLI** (планируется расширение).
- Требуется ручная установка модели через Ollama (или совместимый провайдер).
- Тесты с аннотацией `@Tag("external")` выполняются в реальном окружении и могут занимать много времени.
- Для корректной работы убедитесь, что путь к Qwen CLI определён в `QwenCommandFactory` (поддерживаются Windows, Linux, Mac).

# Классический тест модели со скиллами: пошагово

Представьте, что вы хотите проверить, правильно ли работает ваш скилл при разных моделях. Типичный тест-кейс выглядит так:

---

### Шаг 1. Подготовка рабочей области (WUT)
Вы берёте готовый шаблон проекта, в котором уже лежат нужные скиллы (папка `.qwen/skills/`).  
Библиотека копирует этот шаблон в отдельную папку (например, `target/wut-target/название-шаблона`).  
Если папка уже существовала, вы можете либо перезаписать её, либо оставить (полезно для серии тестов, где нужно сохранить состояние между запусками).

**Зачем?** Чтобы каждый тест стартовал с чистого, известного состояния – без «мусора» от предыдущих прогонов.

---

### Шаг 2. Настройка агента
Вы указываете, какую модель (например, `qwen2.5:1.5b` или `gemma3:12b`) будет использовать Qwen CLI.  
Библиотека находит файл `settings.json` внутри `.qwen/` и временно меняет там имя модели на нужное.  
Также можно задать таймаут выполнения (по умолчанию 3 минуты) – чтобы тест не зависал навечно.

**Зачем?** Мы можем прогнать один и тот же сценарий на десятке моделей и сравнить, какая лучше справляется.

---

### Шаг 3. Выполнение запроса
Вы отправляете агенту один из двух типов запросов:

- **Пользовательский промпт** – как если бы с агентом разговаривал человек.  
  Пример: *«Настрой Checkstyle в Maven-проекте, используй skills maven-checkstyle-setup»*.  
  Агент сам решает, вызывать ли скилл и когда.

- **Прямой вызов скилла** – вы говорите: *«выполни скилл arithmetic с аргументом "2+2"»*.  
  Это полезно для изолированной проверки конкретного скилла.

Во время выполнения агент генерирует поток событий в формате `stream-json`, который библиотека перехватывает и парсит.

---

### Шаг 4. Проверка успешности выполнения
Вы убеждаетесь, что:

- Процесс не завершился по таймауту.
- Код возврата (exitCode) равен 0 (нет критической ошибки).

Если что-то пошло не так – тест падает сразу с понятным сообщением.

---

### Шаг 5. Проверка вызова скиллов
Из событий агента извлекаются все вызовы `tool_use` с `name="skill"` и считывается поле `input.skill`.  
Вы можете проверить:

- Был ли вызван ровно один нужный скилл.
- Был ли он вызван в определённой последовательности (например, сначала `arithmetic`, потом `word-count`).
- Набор вызовов без учёта порядка (игнорируя лишние).

Это помогает убедиться, что модель правильно интерпретировала запрос и не вызвала случайно другой скилл.

---

### Шаг 6. Проверка результатов на файловой системе (если применимо)
Если скилл что-то создаёт или изменяет (например, добавляет файл `checkstyle.xml` или правит `pom.xml`), вы проверяете:

- Существует ли нужный файл.
- Содержит ли он ожидаемые строки.
- Удалены ли временные файлы (если должны быть).

Это особенно важно для скиллов, которые автоматизируют настройку проектов.

---

### Шаг 7. Оценка ответа судьёй (опционально)
Вы передаёте исходный запрос пользователя и весь JSON-лог событий специальному «судье» – второму экземпляру агента, который работает по строгому промпту.  
Судья выставляет **score** от 0 до 1 и пишет краткое описание проблем на русском языке.  
Вы можете задать порог (например, 0.7) и считать тест пройденным, только если оценка выше.

**Зачем?** Не всегда можно автоматически проверить смысл ответа – судья выступает как независимый эксперт, который может уловить нюансы (например, «ответ правильный, но агент сделал лишний шаг»).

---

### Шаг 8. Восстановление окружения (необязательно)
После теста вы можете вернуть исходную модель в `settings.json` (если она была изменена).  
Библиотека запоминает предыдущее имя модели и восстанавливает его по вызову `restoreOriginalModelName()`.  
Логи каждого запуска сохраняются в отдельную папку с уникальным `runId` – они пригодятся для отладки.

---

## 📌 Итоговая схема

```
Подготовка WUT → Настройка модели → Выполнение запроса
            ↓
Проверка успеха → Проверка скиллов → Проверка ФС → (Оценка судьёй)
            ↓
Восстановление модели (опционально) + сохранение логов
```