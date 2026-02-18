```markdown
# API синтеза речи (TTS)

Spring AI предоставляет унифицированный API для синтеза речи (TTS) через интерфейсы `TextToSpeechModel` и `StreamingTextToSpeechModel`. Это позволяет вам писать переносимый код, который работает с различными поставщиками TTS.

## Поддерживаемые поставщики

- xref:api/audio/speech/openai-speech.adoc[API речи OpenAI]
- xref:api/audio/speech/elevenlabs-speech.adoc[API синтеза речи Eleven Labs]

## Общий интерфейс

Все поставщики TTS реализуют следующие общие интерфейсы:

### TextToSpeechModel

Интерфейс `TextToSpeechModel` предоставляет методы для преобразования текста в речь:

```java
public interface TextToSpeechModel extends Model<TextToSpeechPrompt, TextToSpeechResponse>, StreamingTextToSpeechModel {

    /**
     * Преобразует текст в речь с настройками по умолчанию.
     */
    default byte[] call(String text) {
        // Реализация по умолчанию
    }

    /**
     * Преобразует текст в речь с пользовательскими настройками.
     */
    TextToSpeechResponse call(TextToSpeechPrompt prompt);

    /**
     * Возвращает настройки по умолчанию для этой модели.
     */
    default TextToSpeechOptions getDefaultOptions() {
        // Реализация по умолчанию
    }
}
```

### StreamingTextToSpeechModel

Интерфейс `StreamingTextToSpeechModel` предоставляет методы для потоковой передачи аудио в реальном времени:

```java
@FunctionalInterface
public interface StreamingTextToSpeechModel extends StreamingModel<TextToSpeechPrompt, TextToSpeechResponse> {

    /**
     * Потоковая передача ответов синтеза речи с метаданными.
     */
    Flux<TextToSpeechResponse> stream(TextToSpeechPrompt prompt);

    /**
     * Потоковая передача байтов аудио для данного текста.
     */
    default Flux<byte[]> stream(String text) {
        // Реализация по умолчанию
    }
}
```

### TextToSpeechPrompt

Класс `TextToSpeechPrompt` инкапсулирует входной текст и настройки:

```java
TextToSpeechPrompt prompt = new TextToSpeechPrompt(
    "Здравствуйте, это пример синтеза речи.",
    options
);
```

### TextToSpeechResponse

Класс `TextToSpeechResponse` содержит сгенерированное аудио и метаданные:

```java
TextToSpeechResponse response = model.call(prompt);
byte[] audioBytes = response.getResult().getOutput();
TextToSpeechResponseMetadata metadata = response.getMetadata();
```

## Написание кода, независимого от поставщика

Одним из ключевых преимуществ общих интерфейсов TTS является возможность писать код, который работает с любым поставщиком TTS без модификаций. Фактический поставщик (OpenAI, ElevenLabs и т.д.) определяется вашей конфигурацией Spring Boot, что позволяет вам переключать поставщиков без изменения кода приложения.

### Пример базового сервиса

Общие интерфейсы позволяют вам писать код, который работает с любым поставщиком TTS:

```java
@Service
public class NarrationService {

    private final TextToSpeechModel textToSpeechModel;

    public NarrationService(TextToSpeechModel textToSpeechModel) {
        this.textToSpeechModel = textToSpeechModel;
    }

    public byte[] narrate(String text) {
        // Работает с любым поставщиком TTS
        return textToSpeechModel.call(text);
    }

    public byte[] narrateWithOptions(String text, TextToSpeechOptions options) {
        TextToSpeechPrompt prompt = new TextToSpeechPrompt(text, options);
        TextToSpeechResponse response = textToSpeechModel.call(prompt);
        return response.getResult().getOutput();
    }
}
```

Этот сервис работает без проблем с OpenAI, ElevenLabs или любым другим поставщиком TTS, при этом фактическая реализация определяется вашей конфигурацией Spring Boot.

### Расширенный пример: Поддержка нескольких поставщиков

Вы можете создавать приложения, которые поддерживают несколько поставщиков TTS одновременно:

```java
@Service
public class MultiProviderNarrationService {

    private final Map<String, TextToSpeechModel> providers;

    public MultiProviderNarrationService(List<TextToSpeechModel> models) {
        // Spring внедрит все доступные бины TextToSpeechModel
        this.providers = models.stream()
            .collect(Collectors.toMap(
                model -> model.getClass().getSimpleName(),
                model -> model
            ));
    }

    public byte[] narrateWithProvider(String text, String providerName) {
        TextToSpeechModel model = providers.get(providerName);
        if (model == null) {
            throw new IllegalArgumentException("Неизвестный поставщик: " + providerName);
        }
        return model.call(text);
    }

    public Set<String> getAvailableProviders() {
        return providers.keySet();
    }
}
```

### Пример потоковой передачи аудио

Общие интерфейсы также поддерживают потоковую передачу для генерации аудио в реальном времени:

```java
@Service
public class StreamingNarrationService {

    private final TextToSpeechModel textToSpeechModel;

    public StreamingNarrationService(TextToSpeechModel textToSpeechModel) {
        this.textToSpeechModel = textToSpeechModel;
    }

    public Flux<byte[]> streamNarration(String text) {
        // TextToSpeechModel расширяет StreamingTextToSpeechModel
        return textToSpeechModel.stream(text);
    }

    public Flux<TextToSpeechResponse> streamWithMetadata(String text, TextToSpeechOptions options) {
        TextToSpeechPrompt prompt = new TextToSpeechPrompt(text, options);
        return textToSpeechModel.stream(prompt);
    }
}
```

### Пример REST-контроллера

Создание REST API с независимым от поставщика TTS:

```java
@RestController
@RequestMapping("/api/tts")
public class TextToSpeechController {

    private final TextToSpeechModel textToSpeechModel;

    public TextToSpeechController(TextToSpeechModel textToSpeechModel) {
        this.textToSpeechModel = textToSpeechModel;
    }

    @PostMapping(value = "/synthesize", produces = "audio/mpeg")
    public ResponseEntity<byte[]> synthesize(@RequestBody SynthesisRequest request) {
        byte[] audio = textToSpeechModel.call(request.text());
        return ResponseEntity.ok()
            .contentType(MediaType.parseMediaType("audio/mpeg"))
            .header("Content-Disposition", "attachment; filename=\"speech.mp3\"")
            .body(audio);
    }

    @GetMapping(value = "/stream", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public Flux<byte[]> streamSynthesis(@RequestParam String text) {
        return textToSpeechModel.stream(text);
    }

    record SynthesisRequest(String text) {}
}
```

### Выбор поставщика на основе конфигурации

Переключение между поставщиками с помощью профилей или свойств Spring:

```yaml
# application-openai.yml
spring:
  ai:
    model:
      audio:
        speech: openai
    openai:
      api-key: ${OPENAI_API_KEY}
      audio:
        speech:
          options:
            model: gpt-4o-mini-tts
            voice: alloy

# application-elevenlabs.yml
spring:
  ai:
    model:
      audio:
        speech: elevenlabs
    elevenlabs:
      api-key: ${ELEVENLABS_API_KEY}
      tts:
        options:
          model-id: eleven_turbo_v2_5
          voice-id: your_voice_id
```

Затем активируйте желаемый поставщик:
```bash
# Использовать OpenAI
java -jar app.jar --spring.profiles.active=openai

# Использовать ElevenLabs
java -jar app.jar --spring.profiles.active=elevenlabs
```

### Использование переносимых опций

Для максимальной переносимости используйте только методы общего интерфейса `TextToSpeechOptions`:

```java
@Service
public class PortableNarrationService {

    private final TextToSpeechModel textToSpeechModel;

    public PortableNarrationService(TextToSpeechModel textToSpeechModel) {
        this.textToSpeechModel = textToSpeechModel;
    }

    public byte[] createPortableNarration(String text) {
        // Используйте настройки по умолчанию поставщика для максимальной переносимости
        TextToSpeechOptions defaultOptions = textToSpeechModel.getDefaultOptions();
        TextToSpeechPrompt prompt = new TextToSpeechPrompt(text, defaultOptions);
        TextToSpeechResponse response = textToSpeechModel.call(prompt);
        return response.getResult().getOutput();
    }
}
```

### Работа с особенностями конкретного поставщика

Когда вам нужны особенности конкретного поставщика, вы все равно можете их использовать, сохраняя переносимую кодовую базу:

```java
@Service
public class FlexibleNarrationService {

    private final TextToSpeechModel textToSpeechModel;

    public FlexibleNarrationService(TextToSpeechModel textToSpeechModel) {
        this.textToSpeechModel = textToSpeechModel;
    }

    public byte[] narrate(String text, TextToSpeechOptions baseOptions) {
        TextToSpeechOptions options = baseOptions;

        // Примените оптимизации, специфичные для поставщика, если они доступны
        if (textToSpeechModel instanceof OpenAiAudioSpeechModel) {
            options = OpenAiAudioSpeechOptions.builder()
                .from(baseOptions)
                .model("gpt-4o-tts")  // Специфично для OpenAI: используйте модель высокого качества
                .speed(1.0)
                .build();
        } else if (textToSpeechModel instanceof ElevenLabsTextToSpeechModel) {
            // Специфичные для ElevenLabs опции могут быть здесь
        }

        TextToSpeechPrompt prompt = new TextToSpeechPrompt(text, options);
        TextToSpeechResponse response = textToSpeechModel.call(prompt);
        return response.getResult().getOutput();
    }
}
```

### Лучшие практики для переносимого кода

1. **Зависите от интерфейсов**: Всегда внедряйте `TextToSpeechModel`, а не конкретные реализации
2. **Используйте общие опции**: Придерживайтесь методов интерфейса `TextToSpeechOptions` для максимальной переносимости
3. **Обрабатывайте метаданные аккуратно**: Разные поставщики возвращают разные метаданные; обрабатывайте их обобщенно
4. **Тестируйте с несколькими поставщиками**: Убедитесь, что ваш код работает как минимум с двумя поставщиками TTS
5. **Документируйте предположения о поставщике**: Если вы полагаетесь на конкретное поведение поставщика, документируйте это четко

## Особенности конкретного поставщика

Хотя общие интерфейсы обеспечивают переносимость, каждый поставщик также предлагает специфические функции через классы опций, специфичные для поставщика (например, `OpenAiAudioSpeechOptions`, `ElevenLabsSpeechOptions`). Эти классы реализуют интерфейс `TextToSpeechOptions`, добавляя при этом возможности, специфичные для поставщика.
```
