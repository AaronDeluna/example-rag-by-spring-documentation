## OpenAI Transcriptions

Spring AI поддерживает https://platform.openai.com/docs/api-reference/audio/createTranscription[модель транскрипции OpenAI].

## Prerequisites

Вам необходимо создать API-ключ в OpenAI для доступа к моделям ChatGPT. Создайте учетную запись на https://platform.openai.com/signup[странице регистрации OpenAI] и сгенерируйте токен на https://platform.openai.com/account/api-keys[странице API-ключей]. Проект Spring AI определяет свойство конфигурации с именем `spring.ai.openai.api-key`, которое вы должны установить в значение `API Key`, полученное с openai.com. Экспортирование переменной окружения — это один из способов установить это свойство конфигурации:

## Auto-configuration

[NOTE]
====
В Spring AI произошли значительные изменения в автонастройке, названиях артефактов стартовых модулей. Пожалуйста, обратитесь к https://docs.spring.io/spring-ai/reference/upgrade-notes.html[заметкам об обновлении] для получения дополнительной информации.
====

Spring AI предоставляет автонастройку Spring Boot для клиента транскрипции OpenAI. Чтобы включить ее, добавьте следующую зависимость в файл Maven `pom.xml` вашего проекта:

```xml
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-starter-model-openai</artifactId>
</dependency>
```

или в файл сборки Gradle `build.gradle`.

```groovy
dependencies {
    implementation 'org.springframework.ai:spring-ai-starter-model-openai'
}
```

> **Совет:** Обратитесь к разделу xref:getting-started.adoc#dependency-management[Управление зависимостями], чтобы добавить BOM Spring AI в ваш файл сборки.

### Transcription Properties

#### Connection Properties

Префикс `spring.ai.openai` используется в качестве префикса свойства, который позволяет вам подключаться к OpenAI.

[cols="3,5,1"]
| Property | Description | Default
| spring.ai.openai.base-url   | URL для подключения |  https://api.openai.com
| spring.ai.openai.api-key    | API-ключ           |  -
| spring.ai.openai.organization-id | Опционально вы можете указать, какая организация используется для API-запроса. |  -
| spring.ai.openai.project-id      | Опционально вы можете указать, какой проект используется для API-запроса. |  -

> **Совет:** Для пользователей, которые принадлежат нескольким организациям (или получают доступ к своим проектам через свой устаревший пользовательский API-ключ), вы можете опционально указать, какая организация и проект используются для API-запроса. Использование этих API-запросов будет учитываться как использование для указанной организации и проекта.

#### Configuration Properties

[NOTE]
====
Включение и отключение автонастроек транскрипции аудио теперь настраивается через свойства верхнего уровня с префиксом `spring.ai.model.audio.transcription`.

Чтобы включить, используйте spring.ai.model.audio.transcription=openai (по умолчанию включено)

Чтобы отключить, используйте spring.ai.model.audio.transcription=none (или любое значение, которое не соответствует openai)

Это изменение сделано для того, чтобы позволить конфигурацию нескольких моделей.
====

Префикс `spring.ai.openai.audio.transcription` используется в качестве префикса свойства, который позволяет вам настраивать механизм повторных попыток для модели транскрипции OpenAI.

[cols="3,5,2"]
| Property | Description | Default

| spring.ai.model.audio.transcription   | Включить модель транскрипции аудио OpenAI |  openai
| spring.ai.openai.audio.transcription.base-url   | URL для подключения |  https://api.openai.com
| spring.ai.openai.audio.transcription.api-key    | API-ключ           |  -
| spring.ai.openai.audio.transcription.organization-id | Опционально вы можете указать, какая организация используется для API-запроса. |  -
| spring.ai.openai.audio.transcription.project-id      | Опционально вы можете указать, какой проект используется для API-запроса. |  -
| spring.ai.openai.audio.transcription.transcription-path | Путь к конечной точке API для транскрипции аудио. Полезно для совместимых с OpenAI API с различными структурами конечных точек. | /v1/audio/transcriptions
| spring.ai.openai.audio.transcription.options.model  | ID модели, используемой для транскрипции. Доступные модели: `gpt-4o-transcribe` (преобразование речи в текст на основе GPT-4o), `gpt-4o-mini-transcribe` (преобразование речи в текст на основе GPT-4o mini) или `whisper-1` (модель распознавания речи общего назначения, по умолчанию). |  whisper-1
| spring.ai.openai.audio.transcription.options.response-format | Формат выходного транскрипта, один из следующих вариантов: json, text, srt, verbose_json или vtt. |  json
| spring.ai.openai.audio.transcription.options.prompt | Необязательный текст для управления стилем модели или продолжения предыдущего аудиосегмента. Подсказка должна соответствовать языку аудио. |
| spring.ai.openai.audio.transcription.options.language | Язык входного аудио. Указание входного языка в формате ISO-639-1 улучшит точность и задержку. |
| spring.ai.openai.audio.transcription.options.temperature | Температура выборки, от 0 до 1. Более высокие значения, такие как 0.8, сделают вывод более случайным, в то время как более низкие значения, такие как 0.2, сделают его более сосредоточенным и детерминированным. Если установить 0, модель будет использовать логарифмическую вероятность для автоматического увеличения температуры до достижения определенных порогов. | 0
| spring.ai.openai.audio.transcription.options.timestamp_granularities | Гранулярности временных меток, которые необходимо заполнить для этой транскрипции. response_format должен быть установлен в verbose_json, чтобы использовать гранулярности временных меток. Поддерживаются одна или обе из этих опций: word или segment. Примечание: дополнительной задержки для временных меток сегментов нет, но генерация временных меток слов требует дополнительной задержки. | segment

> **Примечание:** Вы можете переопределить общие свойства `spring.ai.openai.base-url`, `spring.ai.openai.api-key`, `spring.ai.openai.organization-id` и `spring.ai.openai.project-id`. Свойства `spring.ai.openai.audio.transcription.base-url`, `spring.ai.openai.audio.transcription.api-key`, `spring.ai.openai.audio.transcription.organization-id` и `spring.ai.openai.audio.transcription.project-id`, если они установлены, имеют приоритет над общими свойствами. Это полезно, если вы хотите использовать разные учетные записи OpenAI для разных моделей и разных конечных точек моделей.

> **Совет:** Все свойства с префиксом `spring.ai.openai.transcription.options` могут быть переопределены во время выполнения.

### Custom API Paths

Для совместимых с OpenAI API (таких как LocalAI, Ollama с совместимостью OpenAI или пользовательские прокси), которые используют различные пути конечных точек, вы можете настроить путь транскрипции:

```properties
spring.ai.openai.audio.transcription.transcription-path=/custom/path/to/transcriptions
```

Это особенно полезно, когда:

- Используются API-шлюзы или прокси, которые изменяют стандартные пути OpenAI
- Работа с совместимыми с OpenAI сервисами, которые реализуют различные структуры URL
- Тестирование против макетных конечных точек с пользовательскими путями
- Развертывание в средах с требованиями к маршрутизации на основе путей

## Runtime Options [[transcription-options]]

Класс `OpenAiAudioTranscriptionOptions` предоставляет параметры, которые можно использовать при выполнении транскрипции. При запуске используются параметры, указанные в `spring.ai.openai.audio.transcription`, но вы можете переопределить их во время выполнения.

Например:

```java
OpenAiAudioApi.TranscriptResponseFormat responseFormat = OpenAiAudioApi.TranscriptResponseFormat.VTT;

OpenAiAudioTranscriptionOptions transcriptionOptions = OpenAiAudioTranscriptionOptions.builder()
    .language("en")
    .prompt("Ask not this, but ask that")
    .temperature(0f)
    .responseFormat(this.responseFormat)
    .build();
AudioTranscriptionPrompt transcriptionRequest = new AudioTranscriptionPrompt(audioFile, this.transcriptionOptions);
AudioTranscriptionResponse response = openAiTranscriptionModel.call(this.transcriptionRequest);
```

## Manual Configuration

Добавьте зависимость `spring-ai-openai` в файл Maven `pom.xml` вашего проекта:

```xml
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-openai</artifactId>
</dependency>
```

или в файл сборки Gradle `build.gradle`.

```groovy
dependencies {
    implementation 'org.springframework.ai:spring-ai-openai'
}
```

> **Совет:** Обратитесь к разделу xref:getting-started.adoc#dependency-management[Управление зависимостями], чтобы добавить BOM Spring AI в ваш файл сборки.

Затем создайте `OpenAiAudioTranscriptionModel`

```java
var openAiAudioApi = new OpenAiAudioApi(System.getenv("OPENAI_API_KEY"));

var openAiAudioTranscriptionModel = new OpenAiAudioTranscriptionModel(this.openAiAudioApi);

var transcriptionOptions = OpenAiAudioTranscriptionOptions.builder()
    .responseFormat(TranscriptResponseFormat.TEXT)
    .temperature(0f)
    .build();

var audioFile = new FileSystemResource("/path/to/your/resource/speech/jfk.flac");

AudioTranscriptionPrompt transcriptionRequest = new AudioTranscriptionPrompt(this.audioFile, this.transcriptionOptions);
AudioTranscriptionResponse response = openAiTranscriptionModel.call(this.transcriptionRequest);
```

## Example Code
- Тест [OpenAiTranscriptionModelIT.java](https://github.com/spring-projects/spring-ai/blob/main/models/spring-ai-openai/src/test/java/org/springframework/ai/openai/audio/transcription/OpenAiTranscriptionModelIT.java) предоставляет некоторые общие примеры использования библиотеки.
