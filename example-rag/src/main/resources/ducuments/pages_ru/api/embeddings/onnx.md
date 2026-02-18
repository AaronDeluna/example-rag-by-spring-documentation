# Transformers (ONNX) Embeddings

`TransformersEmbeddingModel` — это реализация `EmbeddingModel`, которая локально вычисляет https://www.sbert.net/examples/applications/computing-embeddings/README.html#sentence-embeddings-with-transformers[векторные представления предложений] с использованием выбранного https://www.sbert.net/[sentence transformer].

Вы можете использовать любую [модель векторного представления от HuggingFace](https://huggingface.co/spaces/mteb/leaderboard).

Она использует https://www.sbert.net/docs/pretrained_models.html[предобученные] модели трансформеров, сериализованные в формате https://onnx.ai/[Open Neural Network Exchange (ONNX)].

Библиотеки https://djl.ai/[Deep Java Library] и Microsoft https://onnxruntime.ai/docs/get-started/with-java.html[ONNX Java Runtime] применяются для запуска моделей ONNX и вычисления векторных представлений на Java.

## Предварительные требования

Чтобы запустить все в Java, нам нужно **сериализовать Токенизатор и Модель Трансформера** в формате `ONNX`.

Сериализация с помощью optimum-cli — один из быстрых способов достичь этого, используя инструмент командной строки https://huggingface.co/docs/optimum/exporters/onnx/usage_guides/export_a_model#exporting-a-model-to-onnx-using-the-cli[optimum-cli].
Следующий фрагмент подготавливает виртуальную среду Python, устанавливает необходимые пакеты и сериализует (например, экспортирует) указанную модель с помощью `optimum-cli`:

```bash
python3 -m venv venv
source ./venv/bin/activate
(venv) pip install --upgrade pip
(venv) pip install optimum onnx onnxruntime sentence-transformers
(venv) optimum-cli export onnx --model sentence-transformers/all-MiniLM-L6-v2 onnx-output-folder
```

Этот фрагмент экспортирует https://huggingface.co/sentence-transformers/all-MiniLM-L6-v2[модель sentence-transformers/all-MiniLM-L6-v2] в папку `onnx-output-folder`. Последняя включает файлы `tokenizer.json` и `model.onnx`, используемые моделью векторного представления.

Вместо all-MiniLM-L6-v2 вы можете выбрать любой идентификатор трансформера HuggingFace или указать прямой путь к файлу.

## Автоконфигурация

[ПРИМЕЧАНИЕ]
====
В автоконфигурации Spring AI произошли значительные изменения, касающиеся имен артефактов стартовых модулей.
Пожалуйста, обратитесь к https://docs.spring.io/spring-ai/reference/upgrade-notes.html[заметкам об обновлении] для получения дополнительной информации.
====

Spring AI предоставляет автоконфигурацию Spring Boot для модели векторного представления ONNX Transformer.
Чтобы включить ее, добавьте следующую зависимость в файл `pom.xml` вашего проекта Maven:

```xml
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-starter-model-transformers</artifactId>
</dependency>
```

или в файл сборки Gradle `build.gradle`.

```groovy
dependencies {
    implementation 'org.springframework.ai:spring-ai-starter-model-transformers'
}
```

> **Совет:** Обратитесь к разделу xref:getting-started.adoc#dependency-management[Управление зависимостями], чтобы добавить BOM Spring AI в ваш файл сборки.
Обратитесь к разделу xref:getting-started.adoc#artifact-repositories[Артефактные репозитории], чтобы добавить эти репозитории в вашу систему сборки.

Чтобы настроить это, используйте свойства `spring.ai.embedding.transformer.*`.

Например, добавьте это в ваш файл _application.properties_, чтобы настроить клиент с моделью векторного представления текста https://huggingface.co/intfloat/e5-small-v2[intfloat/e5-small-v2]:

```
spring.ai.embedding.transformer.onnx.modelUri=https://huggingface.co/intfloat/e5-small-v2/resolve/main/model.onnx
spring.ai.embedding.transformer.tokenizer.uri=https://huggingface.co/intfloat/e5-small-v2/raw/main/tokenizer.json
```

Полный список поддерживаемых свойств:

### Свойства векторного представления[NOTE]
====
Включение и отключение автоматических конфигураций встраивания теперь настраивается через свойства верхнего уровня с префиксом `spring.ai.model.embedding`.

Чтобы включить, используйте spring.ai.model.embedding=transformers (по умолчанию включено)

Чтобы отключить, используйте spring.ai.model.embedding=none (или любое значение, не совпадающее с transformers)

Это изменение сделано для возможности конфигурации нескольких моделей.
====

| Свойство | Описание | По умолчанию |
| --- | --- | --- |
| spring.ai.embedding.transformer.enabled (Удалено и больше не актуально) | Включить модель встраивания Transformer. | true |
| spring.ai.model.embedding | Включить модель встраивания Transformer. | transformers |
| spring.ai.embedding.transformer.tokenizer.uri | URI предобученного HuggingFaceTokenizer, созданного движком ONNX (например, tokenizer.json). | onnx/all-MiniLM-L6-v2/tokenizer.json |
| spring.ai.embedding.transformer.tokenizer.options | Опции HuggingFaceTokenizer, такие как '`addSpecialTokens`', '`modelMaxLength`', '`truncation`', '`padding`', '`maxLength`', '`stride`', '`padToMultipleOf`'. Оставьте пустым, чтобы использовать значения по умолчанию. | пусто |
| spring.ai.embedding.transformer.cache.enabled | Включить кэширование удаленных ресурсов. | true |
| spring.ai.embedding.transformer.cache.directory | Путь к директории для кэширования удаленных ресурсов, таких как модели ONNX | ${java.io.tmpdir}/spring-ai-onnx-model |
| spring.ai.embedding.transformer.onnx.modelUri | Существующая предобученная модель ONNX. | onnx/all-MiniLM-L6-v2/model.onnx |
| spring.ai.embedding.transformer.onnx.modelOutputName | Имя выходного узла модели ONNX, которое мы будем использовать для вычисления встраиваний. | last_hidden_state |
| spring.ai.embedding.transformer.onnx.gpuDeviceId | Идентификатор устройства GPU для выполнения. Применимо только если >= 0. В противном случае игнорируется. (Требуется дополнительная зависимость onnxruntime_gpu) | -1 |
| spring.ai.embedding.transformer.metadataMode | Указывает, какие части содержимого и метаданных документов будут использоваться для вычисления встраиваний. | NONE |


### Ошибки и особые случаи

[NOTE]
====
Если вы видите ошибку, такую как `Caused by: ai.onnxruntime.OrtException: Supplied array is ragged,..`, вам также необходимо включить паддинг токенизатора в `application.properties`, как показано ниже:

```
spring.ai.embedding.transformer.tokenizer.options.padding=true
```
====

[NOTE]
====
Если вы получаете ошибку, такую как `The generative output names don't contain expected: last_hidden_state. Consider one of the available model outputs: token_embeddings, ....`, вам нужно установить имя выходного узла модели на правильное значение в соответствии с вашими моделями.
Рассмотрите имена, указанные в сообщении об ошибке.
Например:

```
spring.ai.embedding.transformer.onnx.modelOutputName=token_embeddings
```
====

[NOTE]
====
Если вы получаете ошибку, такую как `ai.onnxruntime.OrtException: Error code - ORT_FAIL - message: Deserialize tensor onnx::MatMul_10319 failed.GetFileLength for ./model.onnx_data failed:Invalid fd was supplied: -1`, 
это означает, что ваша модель больше 2 ГБ и сериализована в два файла: `model.onnx` и `model.onnx_data`. 

`model.onnx_data` называется [Внешние данные](https://onnx.ai/onnx/repo-docs/ExternalData.html#external-data) и ожидается, что он будет находиться в той же директории, что и `model.onnx`.

В настоящее время единственным обходным решением является копирование большого `model.onnx_data` в папку, в которой вы запускаете свое Boot-приложение.
====

[NOTE]
====
Если вы получаете ошибку, такую как `ai.onnxruntime.OrtException: Error code - ORT_EP_FAIL - message: Failed to find CUDA shared provider`,
это означает, что вы используете параметры GPU `spring.ai.embedding.transformer.onnx.gpuDeviceId`, но зависимость onnxruntime_gpu отсутствует.
```
<dependency>
    <groupId>com.microsoft.onnxruntime</groupId>
    <artifactId>onnxruntime_gpu</artifactId>
</dependency>
```
Пожалуйста, выберите соответствующую версию onnxruntime_gpu в зависимости от версии CUDA ([ONNX Java Runtime](https://onnxruntime.ai/docs/get-started/with-java.html)).
====

## Ручная конфигурацияЕсли вы не используете Spring Boot, вы можете вручную настроить модель встраивания Onnx Transformers. Для этого добавьте зависимость `spring-ai-transformers` в файл `pom.xml` вашего проекта:

```xml
<dependency>
  <groupId>org.springframework.ai</groupId>
  <artifactId>spring-ai-transformers</artifactId>
</dependency>
```

> **Совет:** Обратитесь к разделу xref:getting-started.adoc#dependency-management[Управление зависимостями], чтобы добавить BOM Spring AI в ваш файл сборки.

Затем создайте новый экземпляр `TransformersEmbeddingModel` и используйте методы `setTokenizerResource(tokenizerJsonUri)` и `setModelResource(modelOnnxUri)`, чтобы установить URI экспортированных файлов `tokenizer.json` и `model.onnx`. Поддерживаются схемы URI (`classpath:`, `file:` или `https:`).

Если модель не задана явно, `TransformersEmbeddingModel` по умолчанию использует https://huggingface.co/sentence-transformers/all-MiniLM-L6-v2[sentence-transformers/all-MiniLM-L6-v2]:

| Размерности | 384 |
| --- | --- |
| Средняя производительность | 58.80 |
| Скорость | 14200 предложений/сек |
| Размер | 80MB |

Следующий фрагмент иллюстрирует, как вручную использовать `TransformersEmbeddingModel`:

```java
TransformersEmbeddingModel embeddingModel = new TransformersEmbeddingModel();

// (необязательно) по умолчанию classpath:/onnx/all-MiniLM-L6-v2/tokenizer.json
embeddingModel.setTokenizerResource("classpath:/onnx/all-MiniLM-L6-v2/tokenizer.json");

// (необязательно) по умолчанию classpath:/onnx/all-MiniLM-L6-v2/model.onnx
embeddingModel.setModelResource("classpath:/onnx/all-MiniLM-L6-v2/model.onnx");

// (необязательно) по умолчанию ${java.io.tmpdir}/spring-ai-onnx-model
// Только ресурсы http/https кэшируются по умолчанию.
embeddingModel.setResourceCacheDirectory("/tmp/onnx-zoo");

// (необязательно) Установите параметры токенизатора, если вы видите ошибки, такие как:
// "ai.onnxruntime.OrtException: Supplied array is ragged, ..."
embeddingModel.setTokenizerOptions(Map.of("padding", "true"));

embeddingModel.afterPropertiesSet();

List<List<Double>> embeddings = this.embeddingModel.embed(List.of("Hello world", "World is big"));

```

> **Примечание:** Если вы создаете экземпляр `TransformersEmbeddingModel` вручную, вы должны вызвать метод `afterPropertiesSet()` после установки свойств и перед использованием клиента.

Первый вызов `embed()` загружает большую модель ONNX и кэширует ее на локальной файловой системе. Поэтому первый вызов может занять больше времени, чем обычно. Используйте метод `#setResourceCacheDirectory(<path>)`, чтобы установить локальную папку, в которой хранятся модели ONNX. Папка кэша по умолчанию — `${java.io.tmpdir}/spring-ai-onnx-model`.

Создавать `TransformersEmbeddingModel` в виде бина более удобно (и предпочтительно). Тогда вам не нужно будет вручную вызывать `afterPropertiesSet()`.

```java
@Bean
public EmbeddingModel embeddingModel() {
   return new TransformersEmbeddingModel();
}
```
