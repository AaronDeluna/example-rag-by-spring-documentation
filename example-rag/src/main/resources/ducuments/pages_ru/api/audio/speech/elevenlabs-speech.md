# ElevenLabs Text-to-Speech (TTS)

## Введение

ElevenLabs предоставляет программное обеспечение для синтеза речи с естественным звучанием, использующее глубокое обучение. Его модели ИИ генерируют реалистичную, универсальную и контекстно-осознанную речь, голоса и звуковые эффекты на 32 языках. API ElevenLabs Text-to-Speech позволяет пользователям оживить любую книгу, статью, PDF, новостную рассылку или текст с помощью ультрареалистичного ИИ-озвучивания.

## Предварительные требования

1. Создайте учетную запись ElevenLabs и получите API-ключ. Вы можете зарегистрироваться на [странице регистрации ElevenLabs](https://elevenlabs.io/sign-up). Ваш API-ключ можно найти на странице профиля после входа в систему.
2. Добавьте зависимость `spring-ai-elevenlabs` в файл сборки вашего проекта. Для получения дополнительной информации обратитесь к разделу [Управление зависимостями](xref:getting-started.adoc#dependency-management).

## Автоконфигурация

Spring AI предоставляет автоконфигурацию Spring Boot для клиента ElevenLabs Text-to-Speech. Чтобы включить ее, добавьте следующую зависимость в файл `pom.xml` вашего проекта Maven:

```xml
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-starter-model-elevenlabs</artifactId>
</dependency>
```

или в файл сборки Gradle `build.gradle`:

```groovy
dependencies {
    implementation 'org.springframework.ai:spring-ai-starter-model-elevenlabs'
}
```

> **Совет:** Обратитесь к разделу [Управление зависимостями](xref:getting-started.adoc#dependency-management), чтобы добавить BOM Spring AI в файл сборки.

## Свойства речи

### Свойства подключения

Префикс `spring.ai.elevenlabs` используется в качестве префикса свойств для **всех** конфигураций, связанных с ElevenLabs (как для подключения, так и для конкретных настроек TTS). Это определено в `ElevenLabsConnectionProperties`.

[cols="3,5,1"]
| Свойство | Описание | По умолчанию
| spring.ai.elevenlabs.base-url | Базовый URL для API ElevenLabs. | https://api.elevenlabs.io
| spring.ai.elevenlabs.api-key  | Ваш API-ключ ElevenLabs.           | -

### Свойства конфигурации

[NOTE]
====
Включение и отключение автоконфигураций аудиоречи теперь настраивается через свойства верхнего уровня с префиксом `spring.ai.model.audio.speech`.

Чтобы включить, используйте `spring.ai.model.audio.speech=elevenlabs` (по умолчанию включено).

Чтобы отключить, используйте `spring.ai.model.audio.speech=none` (или любое значение, которое не соответствует elevenlabs).

Это изменение сделано для возможности конфигурации нескольких моделей.
====

Префикс `spring.ai.elevenlabs.tts` используется в качестве префикса свойств для настройки клиента ElevenLabs Text-to-Speech, в частности. Это определено в `ElevenLabsSpeechProperties`.

[cols="3,5,2"]
| Свойство | Описание | По умолчанию

| spring.ai.model.audio.speech   | Включить модель аудиоречи |  elevenlabs
| spring.ai.elevenlabs.tts.options.model-id | ID модели для использования. | eleven_turbo_v2_5
| spring.ai.elevenlabs.tts.options.voice-id | ID голоса для использования. Это **ID голоса**, а не имя голоса. | 9BWtsMINqrJLrRacOk9x
| spring.ai.elevenlabs.tts.options.output-format |  Формат вывода для сгенерированного аудио. См. раздел [Форматы вывода](xref:#output-formats) ниже. | mp3_22050_32

> **Примечание:** Базовый URL и API-ключ также могут быть настроены **конкретно** для TTS с использованием `spring.ai.elevenlabs.tts.base-url` и `spring.ai.elevenlabs.tts.api-key`. Однако, как правило, рекомендуется использовать глобальный префикс `spring.ai.elevenlabs` для простоты, если у вас нет конкретной причины использовать разные учетные данные для различных сервисов ElevenLabs. Более специфические свойства `tts` будут переопределять глобальные.

> **Совет:** Все свойства с префиксом `spring.ai.elevenlabs.tts.options` могут быть переопределены во время выполнения.

[[output-formats]]
.Доступные форматы вывода
[cols="1,1"]
| Значение Enum         | Описание
| MP3_22050_32       | MP3, 22.05 кГц, 32 кбит/с
| MP3_44100_32       | MP3, 44.1 кГц, 32 кбит/с
| MP3_44100_64       | MP3, 44.1 кГц, 64 кбит/с
| MP3_44100_96       | MP3, 44.1 кГц, 96 кбит/с
| MP3_44100_128      | MP3, 44.1 кГц, 128 кбит/с
| MP3_44100_192      | MP3, 44.1 кГц, 192 кбит/с
| PCM_8000           | PCM, 8 кГц
| PCM_16000          | PCM, 16 кГц
| PCM_22050          | PCM, 22.05 кГц
| PCM_24000          | PCM, 24 кГц
| PCM_44100          | PCM, 44.1 кГц
| PCM_48000          | PCM, 48 кГц
| ULAW_8000          | µ-law, 8 кГц
| ALAW_8000          | A-law, 8 кГц
| OPUS_48000_32      | Opus, 48 кГц, 32 кбит/с
| OPUS_48000_64      | Opus, 48 кГц, 64 кбит/с
| OPUS_48000_96      | Opus, 48 кГц, 96 кбит/с
| OPUS_48000_128     | Opus, 48 кГц, 128 кбит/с
| OPUS_48000_192     | Opus, 48 кГц, 192 кбит/с


## Параметры времени выполнения [[speech-options]]

Класс `ElevenLabsTextToSpeechOptions` предоставляет параметры, которые можно использовать при выполнении запроса на синтез речи. При запуске используются параметры, указанные в `spring.ai.elevenlabs.tts`, но вы можете переопределить их во время выполнения. Доступные параметры:

- `modelId`: ID модели для использования.
- `voiceId`: ID голоса для использования.
- `outputFormat`: формат вывода сгенерированного аудио.
- `voiceSettings`: объект, содержащий настройки голоса, такие как `stability`, `similarityBoost`, `style`, `useSpeakerBoost` и `speed`.
- `enableLogging`: логическое значение для включения или отключения ведения журнала.
- `languageCode`: код языка входного текста (например, "en" для английского).
- `pronunciationDictionaryLocators`: список локаторов словарей произношения.
- `seed`: начальное значение для генерации случайных чисел, для воспроизводимости.
- `previousText`: текст перед основным текстом, для контекста в многоповоротных разговорах.
- `nextText`: текст после основного текста, для контекста в многоповоротных разговорах.
- `previousRequestIds`: ID запросов из предыдущих поворотов в разговоре.
- `nextRequestIds`: ID запросов для последующих поворотов в разговоре.
- `applyTextNormalization`: применить нормализацию текста ("auto", "on" или "off").
- `applyLanguageTextNormalization`: применить нормализацию текста на языке.

Например:

```java
ElevenLabsTextToSpeechOptions speechOptions = ElevenLabsTextToSpeechOptions.builder()
    .model("eleven_multilingual_v2")
    .voiceId("your_voice_id")
    .outputFormat(ElevenLabsApi.OutputFormat.MP3_44100_128.getValue())
    .build();

TextToSpeechPrompt speechPrompt = new TextToSpeechPrompt("Hello, this is a text-to-speech example.", speechOptions);
TextToSpeechResponse response = elevenLabsTextToSpeechModel.call(speechPrompt);
```

### Использование настроек голоса

Вы можете настроить выходной голос, предоставив `VoiceSettings` в параметрах. Это позволяет контролировать такие свойства, как стабильность и схожесть.

```java
var voiceSettings = new ElevenLabsApi.SpeechRequest.VoiceSettings(0.75f, 0.75f, 0.0f, true);

ElevenLabsTextToSpeechOptions speechOptions = ElevenLabsTextToSpeechOptions.builder()
    .model("eleven_multilingual_v2")
    .voiceId("your_voice_id")
    .voiceSettings(voiceSettings)
    .build();

TextToSpeechPrompt speechPrompt = new TextToSpeechPrompt("This is a test with custom voice settings!", speechOptions);
TextToSpeechResponse response = elevenLabsTextToSpeechModel.call(speechPrompt);
```

## Ручная конфигурация

Добавьте зависимость `spring-ai-elevenlabs` в файл `pom.xml` вашего проекта Maven:

```xml
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-elevenlabs</artifactId>
</dependency>
```

или в файл сборки Gradle `build.gradle`:

```groovy
dependencies {
    implementation 'org.springframework.ai:spring-ai-elevenlabs'
}
```

> **Совет:** Обратитесь к разделу [Управление зависимостями](xref:getting-started.adoc#dependency-management), чтобы добавить BOM Spring AI в файл сборки.

Затем создайте экземпляр `ElevenLabsTextToSpeechModel`:

```java
ElevenLabsApi elevenLabsApi = ElevenLabsApi.builder()
		.apiKey(System.getenv("ELEVEN_LABS_API_KEY"))
		.build();

ElevenLabsTextToSpeechModel elevenLabsTextToSpeechModel = ElevenLabsTextToSpeechModel.builder()
	.elevenLabsApi(elevenLabsApi)
	.defaultOptions(ElevenLabsTextToSpeechOptions.builder()
		.model("eleven_turbo_v2_5")
		.voiceId("your_voice_id") // например, "9BWtsMINqrJLrRacOk9x"
		.outputFormat("mp3_44100_128")
		.build())
	.build();

// Вызов будет использовать параметры по умолчанию, настроенные выше.
TextToSpeechPrompt speechPrompt = new TextToSpeechPrompt("Hello, this is a text-to-speech example.");
TextToSpeechResponse response = elevenLabsTextToSpeechModel.call(speechPrompt);

byte[] responseAsBytes = response.getResult().getOutput();
```

## Потоковая передача аудио в реальном времени

API ElevenLabs Speech поддерживает потоковую передачу аудио в реальном времени с использованием кодирования передачи чанками. Это позволяет начать воспроизведение аудио до того, как весь аудиофайл будет сгенерирован.

```java
ElevenLabsApi elevenLabsApi = ElevenLabsApi.builder()
		.apiKey(System.getenv("ELEVEN_LABS_API_KEY"))
		.build();

ElevenLabsTextToSpeechModel elevenLabsTextToSpeechModel = ElevenLabsTextToSpeechModel.builder()
	.elevenLabsApi(elevenLabsApi)
	.build();

ElevenLabsTextToSpeechOptions streamingOptions = ElevenLabsTextToSpeechOptions.builder()
    .model("eleven_turbo_v2_5")
    .voiceId("your_voice_id")
    .outputFormat("mp3_44100_128")
    .build();

TextToSpeechPrompt speechPrompt = new TextToSpeechPrompt("Today is a wonderful day to build something people love!", streamingOptions);

Flux<TextToSpeechResponse> responseStream = elevenLabsTextToSpeechModel.stream(speechPrompt);

// Обработайте поток, например, воспроизведите аудиочанки
responseStream.subscribe(speechResponse -> {
    byte[] audioChunk = speechResponse.getResult().getOutput();
    // Воспроизведите audioChunk
});
```

## API голосов

API голосов ElevenLabs позволяет вам получать информацию о доступных голосах, их настройках и настройках по умолчанию. Вы можете использовать этот API, чтобы узнать `voiceId`, которые можно использовать в ваших запросах на синтез речи.

Чтобы использовать API голосов, вам нужно создать экземпляр `ElevenLabsVoicesApi`:

```java
ElevenLabsVoicesApi voicesApi = ElevenLabsVoicesApi.builder()
        .apiKey(System.getenv("ELEVEN_LABS_API_KEY"))
        .build();
```

Затем вы можете использовать следующие методы:

- `getVoices()`: Получает список всех доступных голосов.
- `getDefaultVoiceSettings()`: Получает настройки по умолчанию для голосов.
- `getVoiceSettings(String voiceId)`: Возвращает настройки для конкретного голоса.
- `getVoice(String voiceId)`: Возвращает метаданные о конкретном голосе.

Пример:

```java
// Получить все голоса
ResponseEntity<ElevenLabsVoicesApi.Voices> voicesResponse = voicesApi.getVoices();
List<ElevenLabsVoicesApi.Voice> voices = voicesResponse.getBody().voices();

// Получить настройки голоса по умолчанию
ResponseEntity<ElevenLabsVoicesApi.VoiceSettings> defaultSettingsResponse = voicesApi.getDefaultVoiceSettings();
ElevenLabsVoicesApi.VoiceSettings defaultSettings = defaultSettingsResponse.getBody();

// Получить настройки для конкретного голоса
ResponseEntity<ElevenLabsVoicesApi.VoiceSettings> voiceSettingsResponse = voicesApi.getVoiceSettings(voiceId);
ElevenLabsVoicesApi.VoiceSettings voiceSettings = voiceSettingsResponse.getBody();

// Получить детали для конкретного голоса
ResponseEntity<ElevenLabsVoicesApi.Voice> voiceDetailsResponse = voicesApi.getVoice(voiceId);
ElevenLabsVoicesApi.Voice voiceDetails = voiceDetailsResponse.getBody();
```

## Пример кода

- Тест [ElevenLabsTextToSpeechModelIT.java](https://github.com/spring-projects/spring-ai/blob/main/models/spring-ai-elevenlabs/src/test/java/org/springframework/ai/elevenlabs/ElevenLabsTextToSpeechModelIT.java) предоставляет общие примеры использования библиотеки.
- Тест [ElevenLabsApiIT.java](https://github.com/spring-projects/spring-ai/blob/main/models/spring-ai-elevenlabs/src/test/java/org/springframework/ai/elevenlabs/api/ElevenLabsApiIT.java) предоставляет примеры использования низкоуровневого `ElevenLabsApi`.
