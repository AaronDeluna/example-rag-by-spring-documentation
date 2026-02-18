# Azure OpenAI Transcriptions

Spring AI поддерживает https://learn.microsoft.com/en-us/azure/ai-services/openai/whisper-quickstart?tabs=command-line%2Cpython-new&pivots=rest-api[модель Azure Whisper].

## Предварительные требования

Получите ваш `endpoint` и `api-key` Azure OpenAI в разделе Azure OpenAI Service на [Azure Portal](https://portal.azure.com).
Spring AI определяет свойство конфигурации с именем `spring.ai.azure.openai.api-key`, которое вы должны установить в значение `API Key`, полученное от Azure.
Также есть свойство конфигурации с именем `spring.ai.azure.openai.endpoint`, которое вы должны установить в URL-адрес конечной точки, полученный при развертывании вашей модели в Azure.
Экспортирование переменной окружения — это один из способов установить это свойство конфигурации:

## Автоконфигурация

[ПРИМЕЧАНИЕ]
====
В автоконфигурации Spring AI произошли значительные изменения, касающиеся имен артефактов модулей-стартеров.
Пожалуйста, обратитесь к https://docs.spring.io/spring-ai/reference/upgrade-notes.html[заметкам об обновлении] для получения дополнительной информации.
====

Spring AI предоставляет автоконфигурацию Spring Boot для клиента генерации транскрипций Azure OpenAI.
Чтобы включить ее, добавьте следующую зависимость в файл `pom.xml` вашего проекта Maven:

```xml
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-starter-model-azure-openai</artifactId>
</dependency>
```

или в файл сборки Gradle `build.gradle`.

```groovy
dependencies {
    implementation 'org.springframework.ai:spring-ai-starter-model-azure-openai'
}
```

> **Совет:** Обратитесь к разделу xref:getting-started.adoc#dependency-management[Управление зависимостями], чтобы добавить BOM Spring AI в ваш файл сборки.

### Свойства транскрипции

[ПРИМЕЧАНИЕ]
====
Включение и отключение автоконфигураций транскрипции аудио теперь настраивается через свойства верхнего уровня с префиксом `spring.ai.model.audio.transcription`.

Чтобы включить, используйте spring.ai.model.audio.transcription=azure-openai (по умолчанию включено)

Чтобы отключить, используйте spring.ai.model.audio.transcription=none (или любое значение, которое не соответствует azure-openai)

Это изменение сделано для того, чтобы позволить конфигурацию нескольких моделей.
====

Префикс `spring.ai.openai.audio.transcription` используется как префикс свойства, который позволяет вам настроить механизм повторных попыток для модели изображения OpenAI.

[cols="3,5,2"]
| Свойство | Описание | По умолчанию

| spring.ai.azure.openai.audio.transcription.enabled (Удалено и больше не действительно)  | Включить модель транскрипции Azure OpenAI. | true
| spring.ai.model.audio.transcription  | Включить модель транскрипции Azure OpenAI. | azure-openai
| spring.ai.azure.openai.audio.transcription.options.model  | ID модели, которую следует использовать. В настоящее время доступна только whisper. | whisper
| spring.ai.azure.openai.audio.transcription.options.deployment-name  | Имя развертывания, под которым модель развернута. |
| spring.ai.azure.openai.audio.transcription.options.response-format | Формат выходной транскрипции, один из следующих вариантов: json, text, srt, verbose_json или vtt. | json
| spring.ai.azure.openai.audio.transcription.options.prompt | Необязательный текст для управления стилем модели или продолжения предыдущего аудиосегмента. Подсказка должна соответствовать языку аудио. |
| spring.ai.azure.openai.audio.transcription.options.language | Язык входного аудио. Указание входного языка в формате ISO-639-1 улучшит точность и задержку. |
| spring.ai.azure.openai.audio.transcription.options.temperature | Температура выборки, от 0 до 1. Более высокие значения, такие как 0.8, сделают вывод более случайным, в то время как более низкие значения, такие как 0.2, сделают его более сосредоточенным и детерминированным. Если установлено в 0, модель будет использовать логарифмическую вероятность для автоматического увеличения температуры до достижения определенных порогов. | 0
| spring.ai.azure.openai.audio.transcription.options.timestamp-granularities | Гранулярности временных меток, которые необходимо заполнить для этой транскрипции. response_format должен быть установлен в verbose_json, чтобы использовать гранулярности временных меток. Поддерживаются одна или обе из этих опций: word или segment. Примечание: дополнительной задержки для временных меток сегментов нет, но генерация временных меток слов требует дополнительной задержки. | segment

## Опции выполнения

Класс `AzureOpenAiAudioTranscriptionOptions` предоставляет опции, которые можно использовать при выполнении транскрипции.
При запуске используются опции, указанные в `spring.ai.azure.openai.audio.transcription`, но вы можете переопределить их во время выполнения.

Например:

```java
AzureOpenAiAudioTranscriptionOptions.TranscriptResponseFormat responseFormat = AzureOpenAiAudioTranscriptionOptions.TranscriptResponseFormat.VTT;

AzureOpenAiAudioTranscriptionOptions transcriptionOptions = AzureOpenAiAudioTranscriptionOptions.builder()
    .language("en")
    .prompt("Ask not this, but ask that")
    .temperature(0f)
    .responseFormat(this.responseFormat)
    .build();
AudioTranscriptionPrompt transcriptionRequest = new AudioTranscriptionPrompt(audioFile, this.transcriptionOptions);
AudioTranscriptionResponse response = azureOpenAiTranscriptionModel.call(this.transcriptionRequest);
```

## Ручная конфигурация

Добавьте зависимость `spring-ai-openai` в файл `pom.xml` вашего проекта Maven:

```xml
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-azure-openai</artifactId>
</dependency>
```

или в файл сборки Gradle `build.gradle`.

```groovy
dependencies {
    implementation 'org.springframework.ai:spring-ai-azure-openai'
}
```

> **Совет:** Обратитесь к разделу xref:getting-started.adoc#dependency-management[Управление зависимостями], чтобы добавить BOM Spring AI в ваш файл сборки.

Затем создайте `AzureOpenAiAudioTranscriptionModel`

```java
var openAIClient = new OpenAIClientBuilder()
    .credential(new AzureKeyCredential(System.getenv("AZURE_OPENAI_API_KEY")))
    .endpoint(System.getenv("AZURE_OPENAI_ENDPOINT"))
    .buildClient();

var azureOpenAiAudioTranscriptionModel = new AzureOpenAiAudioTranscriptionModel(this.openAIClient, null);

var transcriptionOptions = AzureOpenAiAudioTranscriptionOptions.builder()
    .responseFormat(TranscriptResponseFormat.TEXT)
    .temperature(0f)
    .build();

var audioFile = new FileSystemResource("/path/to/your/resource/speech/jfk.flac");

AudioTranscriptionPrompt transcriptionRequest = new AudioTranscriptionPrompt(this.audioFile, this.transcriptionOptions);
AudioTranscriptionResponse response = this.azureOpenAiAudioTranscriptionModel.call(this.transcriptionRequest);
```
