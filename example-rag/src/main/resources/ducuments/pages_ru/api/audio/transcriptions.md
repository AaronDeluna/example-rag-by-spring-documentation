# API транскрипции

Spring AI предоставляет унифицированный API для транскрипции речи в текст через интерфейс `TranscriptionModel`. Это позволяет вам писать переносимый код, который работает с различными провайдерами транскрипции.

## Поддерживаемые провайдеры

- xref:api/audio/transcriptions/openai-transcriptions.adoc[API Whisper от OpenAI]
- xref:api/audio/transcriptions/azure-openai-transcriptions.adoc[API Whisper от Azure OpenAI]

## Общий интерфейс

Все провайдеры транскрипции реализуют следующий общий интерфейс:

### TranscriptionModel

Интерфейс `TranscriptionModel` предоставляет методы для преобразования аудио в текст:

```java
public interface TranscriptionModel extends Model<AudioTranscriptionPrompt, AudioTranscriptionResponse> {

    /**
     * Транскрибирует аудио из данного запроса.
     */
    AudioTranscriptionResponse call(AudioTranscriptionPrompt transcriptionPrompt);

    /**
     * Удобный метод для транскрипции аудиоресурса.
     */
    default String transcribe(Resource resource) {
        AudioTranscriptionPrompt prompt = new AudioTranscriptionPrompt(resource);
        return this.call(prompt).getResult().getOutput();
    }

    /**
     * Удобный метод для транскрипции аудиоресурса с опциями.
     */
    default String transcribe(Resource resource, AudioTranscriptionOptions options) {
        AudioTranscriptionPrompt prompt = new AudioTranscriptionPrompt(resource, options);
        return this.call(prompt).getResult().getOutput();
    }
}
```

### AudioTranscriptionPrompt

Класс `AudioTranscriptionPrompt` инкапсулирует входное аудио и опции:

```java
Resource audioFile = new FileSystemResource("/path/to/audio.mp3");
AudioTranscriptionPrompt prompt = new AudioTranscriptionPrompt(
    audioFile,
    options
);
```

### AudioTranscriptionResponse

Класс `AudioTranscriptionResponse` содержит транскрибированный текст и метаданные:

```java
AudioTranscriptionResponse response = model.call(prompt);
String transcribedText = response.getResult().getOutput();
AudioTranscriptionResponseMetadata metadata = response.getMetadata();
```

## Написание кода, независимого от провайдера

Одним из ключевых преимуществ общего интерфейса транскрипции является возможность написания кода, который работает с любым провайдером транскрипции без модификаций. Фактический провайдер (OpenAI, Azure OpenAI и т.д.) определяется вашей конфигурацией Spring Boot, что позволяет вам переключать провайдеров без изменения кода приложения.

### Пример базового сервиса

Общий интерфейс позволяет вам писать код, который работает с любым провайдером транскрипции:

```java
@Service
public class TranscriptionService {

    private final TranscriptionModel transcriptionModel;

    public TranscriptionService(TranscriptionModel transcriptionModel) {
        this.transcriptionModel = transcriptionModel;
    }

    public String transcribeAudio(Resource audioFile) {
        return transcriptionModel.transcribe(audioFile);
    }

    public String transcribeWithOptions(Resource audioFile, AudioTranscriptionOptions options) {
        AudioTranscriptionPrompt prompt = new AudioTranscriptionPrompt(audioFile, options);
        AudioTranscriptionResponse response = transcriptionModel.call(prompt);
        return response.getResult().getOutput();
    }
}
```

Этот сервис работает без проблем с OpenAI, Azure OpenAI или любым другим провайдером транскрипции, при этом фактическая реализация определяется вашей конфигурацией Spring Boot.

## Особенности, специфичные для провайдера

Хотя общий интерфейс обеспечивает переносимость, каждый провайдер также предлагает специфические функции через классы опций, специфичные для провайдера (например, `OpenAiAudioTranscriptionOptions`, `AzureOpenAiAudioTranscriptionOptions`). Эти классы реализуют интерфейс `AudioTranscriptionOptions`, добавляя специфические возможности для провайдера.

Для получения подробной информации о функциях, специфичных для провайдера, смотрите страницы документации отдельных провайдеров.
