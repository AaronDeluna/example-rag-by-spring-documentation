# OpenAI Text-to-Speech (TTS)

## Введение

Audio API предоставляет конечную точку для синтеза речи на основе модели TTS (текст в речь) от OpenAI, позволяя пользователям:

- Озвучивать написанные блоги.
- Генерировать аудио на нескольких языках.
- Получать аудиовыход в реальном времени с помощью потоковой передачи.

## Предварительные требования

1. Создайте учетную запись OpenAI и получите API-ключ. Вы можете зарегистрироваться на https://platform.openai.com/signup[странице регистрации OpenAI] и сгенерировать API-ключ на https://platform.openai.com/account/api-keys[странице API-ключей].
2. Добавьте зависимость `spring-ai-openai` в файл сборки вашего проекта. Для получения дополнительной информации обратитесь к разделу xref:getting-started.adoc#dependency-management[Управление зависимостями].

## Автоконфигурация

[ПРИМЕЧАНИЕ]
====
В автоконфигурации Spring AI произошли значительные изменения, касающиеся названий артефактов стартовых модулей.
Пожалуйста, обратитесь к https://docs.spring.io/spring-ai/reference/upgrade-notes.html[заметкам об обновлении] для получения дополнительной информации.
====

Spring AI предоставляет автоконфигурацию Spring Boot для клиента OpenAI Text-to-Speech.
Чтобы включить ее, добавьте следующую зависимость в файл `pom.xml` вашего проекта Maven:

```xml
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-starter-model-openai</artifactId>
</dependency>
```

или в файл сборки Gradle `build.gradle`:

```groovy
dependencies {
    implementation 'org.springframework.ai:spring-ai-starter-model-openai'
}
```

> **Совет:** Обратитесь к разделу xref:getting-started.adoc#dependency-management[Управление зависимостями], чтобы добавить BOM Spring AI в файл сборки.

## Свойства речи

### Свойства подключения

Префикс `spring.ai.openai` используется в качестве префикса свойств, который позволяет вам подключаться к OpenAI.

[cols="3,5,1"]
|====
| Свойство | Описание | По умолчанию
| spring.ai.openai.base-url   | URL для подключения |  https://api.openai.com
| spring.ai.openai.api-key    | API-ключ           |  -
| spring.ai.openai.organization-id | Опционально вы можете указать, какая организация используется для API-запроса. |  -
| spring.ai.openai.project-id      | Опционально вы можете указать, какой проект используется для API-запроса. |  -
|====

> **Совет:** Для пользователей, которые принадлежат нескольким организациям (или получают доступ к своим проектам через свой устаревший API-ключ пользователя), вы можете опционально указать, какая организация и проект используются для API-запроса.
Использование этих API-запросов будет учитываться как использование для указанной организации и проекта.

### Свойства конфигурации

[ПРИМЕЧАНИЕ]
====
Включение и отключение автоконфигураций аудиоречи теперь настраивается через свойства верхнего уровня с префиксом `spring.ai.model.audio.speech`.

Чтобы включить, используйте spring.ai.model.audio.speech=openai (по умолчанию включено)

Чтобы отключить, используйте spring.ai.model.audio.speech=none (или любое значение, которое не соответствует openai)

Это изменение сделано для того, чтобы позволить конфигурацию нескольких моделей.
====

Префикс `spring.ai.openai.audio.speech` используется в качестве префикса свойств, который позволяет вам настраивать клиент OpenAI Text-to-Speech.

[cols="3,5,2"]
|====
| Свойство | Описание | По умолчанию

| spring.ai.model.audio.speech   | Включить модель аудиоречи |  openai
| spring.ai.openai.audio.speech.base-url   | URL для подключения |  https://api.openai.com
| spring.ai.openai.audio.speech.api-key    | API-ключ           |  -
| spring.ai.openai.audio.speech.organization-id | Опционально вы можете указать, какая организация используется для API-запроса. |  -
| spring.ai.openai.audio.speech.project-id      | Опционально вы можете указать, какой проект используется для API-запроса. |  -
| spring.ai.openai.audio.speech.speech-path | Путь конечной точки API для генерации аудиоречи. Полезно для совместимых с OpenAI API с различными структурами конечных точек. | /v1/audio/speech
| spring.ai.openai.audio.speech.options.model  | ID модели, используемой для генерации аудио. Доступные модели: `gpt-4o-mini-tts` (по умолчанию, оптимизированная для скорости и стоимости), `gpt-4o-tts` (высокое качество), `tts-1` (устаревшая, оптимизированная для скорости) или `tts-1-hd` (устаревшая, оптимизированная для качества). |  gpt-4o-mini-tts
| spring.ai.openai.audio.speech.options.voice | Голос, используемый для синтеза. Для API TTS от OpenAI один из доступных голосов для выбранной модели: alloy, echo, fable, onyx, nova и shimmer. | alloy
| spring.ai.openai.audio.speech.options.response-format | Формат аудиовыхода. Поддерживаемые форматы: mp3, opus, aac, flac, wav и pcm. | mp3
| spring.ai.openai.audio.speech.options.speed | Скорость синтеза голоса. Допустимый диапазон от 0.25 (самый медленный) до 4.0 (самый быстрый). | 1.0
|====

> **Примечание:** Вы можете переопределить общие свойства `spring.ai.openai.base-url`, `spring.ai.openai.api-key`, `spring.ai.openai.organization-id` и `spring.ai.openai.project-id`.
Свойства `spring.ai.openai.audio.speech.base-url`, `spring.ai.openai.audio.speech.api-key`, `spring.ai.openai.audio.speech.organization-id` и `spring.ai.openai.audio.speech.project-id`, если они установлены, имеют приоритет над общими свойствами.
Это полезно, если вы хотите использовать разные учетные записи OpenAI для разных моделей и разных конечных точек моделей.

> **Совет:** Все свойства с префиксом `spring.ai.openai.audio.speech.options` могут быть переопределены во время выполнения.

### Пользовательские пути API

Для совместимых с OpenAI API (таких как LocalAI, Ollama с совместимостью OpenAI или пользовательские прокси), которые используют различные пути конечных точек, вы можете настроить путь речи:

```properties
spring.ai.openai.audio.speech.speech-path=/custom/path/to/speech
```

Это особенно полезно, когда:

- Используются API-шлюзы или прокси, которые изменяют стандартные пути OpenAI
- Работаете с совместимыми с OpenAI сервисами, которые реализуют различные структуры URL
- Тестируете против макетных конечных точек с пользовательскими путями
- Разворачиваетесь в средах с требованиями к маршрутизации на основе путей

## Опции времени выполнения [[speech-options]]

Класс `OpenAiAudioSpeechOptions` предоставляет опции, которые можно использовать при выполнении запроса текст-в-речь.
При запуске используются опции, указанные в `spring.ai.openai.audio.speech`, но вы можете переопределить их во время выполнения.

Класс `OpenAiAudioSpeechOptions` реализует интерфейс `TextToSpeechOptions`, предоставляя как переносимые, так и специфические для OpenAI параметры конфигурации.

Например:

```java
OpenAiAudioSpeechOptions speechOptions = OpenAiAudioSpeechOptions.builder()
    .model("gpt-4o-mini-tts")
    .voice(OpenAiAudioApi.SpeechRequest.Voice.ALLOY)
    .responseFormat(OpenAiAudioApi.SpeechRequest.AudioResponseFormat.MP3)
    .speed(1.0)
    .build();

TextToSpeechPrompt speechPrompt = new TextToSpeechPrompt("Hello, this is a text-to-speech example.", speechOptions);
TextToSpeechResponse response = openAiAudioSpeechModel.call(speechPrompt);
```

## Ручная конфигурация

Добавьте зависимость `spring-ai-openai` в файл `pom.xml` вашего проекта Maven:

```xml
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-openai</artifactId>
</dependency>
```

или в файл сборки Gradle `build.gradle`:

```groovy
dependencies {
    implementation 'org.springframework.ai:spring-ai-openai'
}
```

> **Совет:** Обратитесь к разделу xref:getting-started.adoc#dependency-management[Управление зависимостями], чтобы добавить BOM Spring AI в файл сборки.

Затем создайте `OpenAiAudioSpeechModel`:

```java
var openAiAudioApi = new OpenAiAudioApi()
    .apiKey(System.getenv("OPENAI_API_KEY"))
    .build();

var openAiAudioSpeechModel = new OpenAiAudioSpeechModel(openAiAudioApi);

var speechOptions = OpenAiAudioSpeechOptions.builder()
    .responseFormat(OpenAiAudioApi.SpeechRequest.AudioResponseFormat.MP3)
    .speed(1.0)
    .model(OpenAiAudioApi.TtsModel.GPT_4_O_MINI_TTS.value)
    .build();

var speechPrompt = new TextToSpeechPrompt("Hello, this is a text-to-speech example.", speechOptions);
TextToSpeechResponse response = openAiAudioSpeechModel.call(speechPrompt);

// Доступ к метаданным (информация о лимите запросов)
OpenAiAudioSpeechResponseMetadata metadata = (OpenAiAudioSpeechResponseMetadata) response.getMetadata();

byte[] responseAsBytes = response.getResult().getOutput();
```

## Потоковая передача аудио в реальном времени

Speech API поддерживает потоковую передачу аудио в реальном времени с использованием кодирования передачи чанками. Это означает, что аудио может воспроизводиться до того, как весь файл будет сгенерирован и станет доступным.

`OpenAiAudioSpeechModel` реализует интерфейс `StreamingTextToSpeechModel`, предоставляя как стандартные, так и потоковые возможности.

```java
var openAiAudioApi = new OpenAiAudioApi()
    .apiKey(System.getenv("OPENAI_API_KEY"))
    .build();

var openAiAudioSpeechModel = new OpenAiAudioSpeechModel(openAiAudioApi);

OpenAiAudioSpeechOptions speechOptions = OpenAiAudioSpeechOptions.builder()
    .voice(OpenAiAudioApi.SpeechRequest.Voice.ALLOY)
    .speed(1.0)
    .responseFormat(OpenAiAudioApi.SpeechRequest.AudioResponseFormat.MP3)
    .model(OpenAiAudioApi.TtsModel.GPT_4_O_MINI_TTS.value)
    .build();

TextToSpeechPrompt speechPrompt = new TextToSpeechPrompt("Today is a wonderful day to build something people love!", speechOptions);

Flux<TextToSpeechResponse> responseStream = openAiAudioSpeechModel.stream(speechPrompt);

// Вы также можете потоково передавать сырые байты аудио напрямую
Flux<byte[]> audioByteStream = openAiAudioSpeechModel.stream("Hello, world!");
```

## Руководство по миграции

Если вы обновляетесь с устаревших классов `SpeechModel` и `SpeechPrompt`, это руководство предоставляет подробные инструкции по миграции на новые общие интерфейсы.

### Резюме изменений

Эта миграция включает следующие изменения:

1. **Удаленные классы**: Шесть устаревших классов были удалены из пакета `org.springframework.ai.openai.audio.speech`
2. **Изменения пакета**: Основные классы TTS перемещены в пакет `org.springframework.ai.audio.tts`
3. **Изменения типов**: Параметр `speed` изменился с `Float` на `Double` во всех компонентах OpenAI TTS
4. **Иерархия интерфейсов**: `TextToSpeechModel` теперь расширяет `StreamingTextToSpeechModel`

### Справочник по сопоставлению классов

[cols="1,1"]
|====
| Устаревший (удаленный) | Новый интерфейс

| `SpeechModel`
| `TextToSpeechModel`

| `StreamingSpeechModel`
| `StreamingTextToSpeechModel`

| `SpeechPrompt`
| `TextToSpeechPrompt`

| `SpeechResponse`
| `TextToSpeechResponse`

| `SpeechMessage`
| `TextToSpeechMessage`

| `Speech` (в `org.springframework.ai.openai.audio.speech`)
| `Speech` (в `org.springframework.ai.audio.tts`)
|====

### Пошаговые инструкции по миграции

#### Шаг 1: Обновите импорты

Замените все импорты из старого пакета `org.springframework.ai.openai.audio.speech` на новые общие интерфейсы:

```text
Найти:    import org.springframework.ai.openai.audio.speech.SpeechModel;
Заменить: import org.springframework.ai.audio.tts.TextToSpeechModel;

Найти:    import org.springframework.ai.openai.audio.speech.StreamingSpeechModel;
Заменить: import org.springframework.ai.audio.tts.StreamingTextToSpeechModel;

Найти:    import org.springframework.ai.openai.audio.speech.SpeechPrompt;
Заменить: import org.springframework.ai.audio.tts.TextToSpeechPrompt;

Найти:    import org.springframework.ai.openai.audio.speech.SpeechResponse;
Заменить: import org.springframework.ai.audio.tts.TextToSpeechResponse;

Найти:    import org.springframework.ai.openai.audio.speech.SpeechMessage;
Заменить: import org.springframework.ai.audio.tts.TextToSpeechMessage;

Найти:    import org.springframework.ai.openai.audio.speech.Speech;
Заменить: import org.springframework.ai.audio.tts.Speech;
```

#### Шаг 2: Обновите ссылки на типы

Замените все ссылки на типы в вашем коде:

```text
Найти:    SpeechModel
Заменить: TextToSpeechModel

Найти:    StreamingSpeechModel
Заменить: StreamingTextToSpeechModel

Найти:    SpeechPrompt
Заменить: TextToSpeechPrompt

Найти:    SpeechResponse
Заменить: TextToSpeechResponse

Найти:    SpeechMessage
Заменить: TextToSpeechMessage
```

#### Шаг 3: Обновите параметр скорости (Float → Double)

Параметр `speed` изменился с `Float` на `Double`. Обновите все вхождения:

```text
Найти:    .speed(1.0f)
Заменить: .speed(1.0)

Найти:    .speed(0.5f)
Заменить: .speed(0.5)

Найти:    Float speed
Заменить: Double speed
```

Если у вас есть сериализованные данные или конфигурационные файлы с значениями Float, вам нужно будет обновить их также:

```json
// До
{
  "speed": 1.0
}

// После (изменений в коде не требуется для JSON, но будьте внимательны к изменению типа в Java)
{
  "speed": 1.0
}
```

#### Шаг 4: Обновите объявления бинов

Если у вас есть автоконфигурация Spring Boot или ручные определения бинов:

```java
// До
@Bean
public SpeechModel speechModel(OpenAiAudioApi audioApi) {
    return new OpenAiAudioSpeechModel(audioApi);
}

// После
@Bean
public TextToSpeechModel textToSpeechModel(OpenAiAudioApi audioApi) {
    return new OpenAiAudioSpeechModel(audioApi);
}
```

### Примеры миграции кода

#### Пример 1: Основное преобразование текст-в-речь

**До (устаревший):**
```java
import org.springframework.ai.openai.audio.speech.*;

@Service
public class OldNarrationService {

    private final SpeechModel speechModel;

    public OldNarrationService(SpeechModel speechModel) {
        this.speechModel = speechModel;
    }

    public byte[] createNarration(String text) {
        SpeechPrompt prompt = new SpeechPrompt(text);
        SpeechResponse response = speechModel.call(prompt);
        return response.getResult().getOutput();
    }
}
```

**После (с использованием общих интерфейсов):**
```java
import org.springframework.ai.audio.tts.*;
import org.springframework.ai.openai.OpenAiAudioSpeechModel;

@Service
public class NarrationService {

    private final TextToSpeechModel textToSpeechModel;

    public NarrationService(TextToSpeechModel textToSpeechModel) {
        this.textToSpeechModel = textToSpeechModel;
    }

    public byte[] createNarration(String text) {
        TextToSpeechPrompt prompt = new TextToSpeechPrompt(text);
        TextToSpeechResponse response = textToSpeechModel.call(prompt);
        return response.getResult().getOutput();
    }
}
```

#### Пример 2: Текст-в-речь с пользовательскими опциями

**До (устаревший):**
```java
import org.springframework.ai.openai.audio.speech.*;
import org.springframework.ai.openai.api.OpenAiAudioApi;

SpeechModel model = new OpenAiAudioSpeechModel(audioApi);

OpenAiAudioSpeechOptions options = OpenAiAudioSpeechOptions.builder()
    .model("tts-1")
    .voice(OpenAiAudioApi.SpeechRequest.Voice.NOVA)
    .speed(1.0f)  // Значение Float
    .responseFormat(OpenAiAudioApi.SpeechRequest.AudioResponseFormat.MP3)
    .build();

SpeechPrompt prompt = new SpeechPrompt("Hello, world!", options);
SpeechResponse response = model.call(prompt);
byte[] audio = response.getResult().getOutput();
```

**После (с использованием общих интерфейсов):**
```java
import org.springframework.ai.audio.tts.*;
import org.springframework.ai.openai.OpenAiAudioSpeechModel;
import org.springframework.ai.openai.OpenAiAudioSpeechOptions;
import org.springframework.ai.openai.api.OpenAiAudioApi;

TextToSpeechModel model = new OpenAiAudioSpeechModel(audioApi);

OpenAiAudioSpeechOptions options = OpenAiAudioSpeechOptions.builder()
    .model("tts-1")
    .voice(OpenAiAudioApi.SpeechRequest.Voice.NOVA)
    .speed(1.0)  // Значение Double
    .responseFormat(OpenAiAudioApi.SpeechRequest.AudioResponseFormat.MP3)
    .build();

TextToSpeechPrompt prompt = new TextToSpeechPrompt("Hello, world!", options);
TextToSpeechResponse response = model.call(prompt);
byte[] audio = response.getResult().getOutput();
```

#### Пример 3: Потоковая передача текст-в-речь

**До (устаревший):**
```java
import org.springframework.ai.openai.audio.speech.*;
import reactor.core.publisher.Flux;

StreamingSpeechModel model = new OpenAiAudioSpeechModel(audioApi);
SpeechPrompt prompt = new SpeechPrompt("Stream this text");

Flux<SpeechResponse> stream = model.stream(prompt);
stream.subscribe(response -> {
    byte[] audioChunk = response.getResult().getOutput();
    // Обработка аудиочанка
});
```

**После (с использованием общих интерфейсов):**
```java
import org.springframework.ai.audio.tts.*;
import org.springframework.ai.openai.OpenAiAudioSpeechModel;
import reactor.core.publisher.Flux;

TextToSpeechModel model = new OpenAiAudioSpeechModel(audioApi);
TextToSpeechPrompt prompt = new TextToSpeechPrompt("Stream this text");

Flux<TextToSpeechResponse> stream = model.stream(prompt);
stream.subscribe(response -> {
    byte[] audioChunk = response.getResult().getOutput();
    // Обработка аудиочанка
});
```

#### Пример 4: Внедрение зависимостей с Spring Boot

**До (устаревший):**
```java
@RestController
public class OldSpeechController {

    private final SpeechModel speechModel;

    @Autowired
    public OldSpeechController(SpeechModel speechModel) {
        this.speechModel = speechModel;
    }

    @PostMapping("/narrate")
    public ResponseEntity<byte[]> narrate(@RequestBody String text) {
        SpeechPrompt prompt = new SpeechPrompt(text);
        SpeechResponse response = speechModel.call(prompt);
        return ResponseEntity.ok()
            .contentType(MediaType.parseMediaType("audio/mpeg"))
            .body(response.getResult().getOutput());
    }
}
```

**После (с использованием общих интерфейсов):**
```java
@RestController
public class SpeechController {

    private final TextToSpeechModel textToSpeechModel;

    @Autowired
    public SpeechController(TextToSpeechModel textToSpeechModel) {
        this.textToSpeechModel = textToSpeechModel;
    }

    @PostMapping("/narrate")
    public ResponseEntity<byte[]> narrate(@RequestBody String text) {
        TextToSpeechPrompt prompt = new TextToSpeechPrompt(text);
        TextToSpeechResponse response = textToSpeechModel.call(prompt);
        return ResponseEntity.ok()
            .contentType(MediaType.parseMediaType("audio/mpeg"))
            .body(response.getResult().getOutput());
    }
}
```

### Изменения конфигурации Spring Boot

Свойства автоконфигурации Spring Boot остаются прежними. Изменения в ваших файлах `application.properties` или `application.yml` не требуются.

Однако, если у вас есть явные ссылки на бины или квалификаторы, обновите их:

```java
// До
@Qualifier("speechModel")

// После
@Qualifier("textToSpeechModel")
```

### Преимущества миграции

- **Переносимость**: Пишите код один раз, легко переключайтесь между OpenAI, ElevenLabs или другими поставщиками TTS
- **Согласованность**: Те же шаблоны, что и для ChatModel и других абстракций Spring AI
- **Безопасность типов**: Улучшенная иерархия типов с правильными реализациями интерфейсов
- **Будущее**: Новые поставщики TTS будут автоматически работать с вашим существующим кодом
- **Стандартизация**: Последовательный тип `Double` для параметра скорости для всех поставщиков TTS

### Общие проблемы миграции и решения

#### Проблема 1: Ошибка компиляции - Не удается найти символ SpeechModel

**Ошибка:**
[source]
```
error: cannot find symbol SpeechModel
```

**Решение:** Обновите ваши импорты, как описано в Шаге 1, заменив `SpeechModel` на `TextToSpeechModel`.

#### Проблема 2: Несоответствие типов - Float не может быть преобразован в Double

**Ошибка:**
[source]
```
error: incompatible types: float cannot be converted to Double
```

**Решение:** Удалите суффикс `f` из литералов с плавающей запятой (например, измените `1.0f` на `1.0`).

#### Проблема 3: Ошибка создания бина во время выполнения

**Ошибка:**
[source]
```
NoSuchBeanDefinitionException: No qualifying bean of type 'SpeechModel'
```

**Решение:** Обновите ваше внедрение зависимостей, чтобы использовать `TextToSpeechModel` вместо `SpeechModel`.

## Пример кода

- Тест [OpenAiSpeechModelIT.java](https://github.com/spring-projects/spring-ai/blob/main/models/spring-ai-openai/src/test/java/org/springframework/ai/openai/audio/speech/OpenAiSpeechModelIT.java) предоставляет общие примеры использования библиотеки.
