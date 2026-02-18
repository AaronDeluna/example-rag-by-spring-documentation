# Ollama Embeddings

С помощью https://ollama.ai/[Ollama] вы можете локально запускать различные https://ollama.com/search?c=embedding[AI Models] и генерировать из них векторные представления.
Векторное представление — это вектор (список) чисел с плавающей запятой.
Расстояние между двумя векторами измеряет их взаимосвязь.
Небольшие расстояния указывают на высокую взаимосвязь, а большие расстояния — на низкую взаимосвязь.

Реализация `OllamaEmbeddingModel` использует конечную точку Ollama https://github.com/ollama/ollama/blob/main/docs/api.md#generate-embeddings[Embeddings API].

## Предварительные требования

Сначала вам нужен доступ к экземпляру Ollama. Есть несколько вариантов, включая следующие:

- [Скачайте и установите Ollama](https://ollama.com/download) на своем локальном компьютере.
- Настройте и xref:api/testcontainers.adoc[запустите Ollama через Testcontainers].
- Подключитесь к экземпляру Ollama через xref:api/cloud-bindings.adoc[Служебные привязки Kubernetes].

Вы можете загрузить модели, которые хотите использовать в своем приложении, из https://ollama.com/search?c=embedding[библиотеки моделей Ollama]:

```shellscript
ollama pull <model-name>
```

Вы также можете загрузить любую из тысяч бесплатных [GGUF Hugging Face Models](https://huggingface.co/models?library=gguf&sort=trending):

```shellscript
ollama pull hf.co/<username>/<model-repository>
```

Кроме того, вы можете включить опцию автоматической загрузки любых необходимых моделей: xref:auto-pulling-models[Автоматическая загрузка моделей].

## Автонастройка

[NOTE]
====
В автонастройке Spring AI произошли значительные изменения в названиях артефактов стартовых модулей.
Пожалуйста, обратитесь к https://docs.spring.io/spring-ai/reference/upgrade-notes.html[заметкам об обновлении] для получения дополнительной информации.
====

Spring AI предоставляет автонастройку Spring Boot для модели векторного представления Ollama Azure.
Чтобы включить ее, добавьте следующую зависимость в ваши файлы сборки Maven `pom.xml` или Gradle `build.gradle`:

[tabs]
======
Maven::
+
```xml
<dependency>
   <groupId>org.springframework.ai</groupId>
   <artifactId>spring-ai-starter-model-ollama</artifactId>
</dependency>
```

Gradle::
+
```groovy
dependencies {
    implementation 'org.springframework.ai:spring-ai-starter-model-ollama'
}
```
======

> **Совет:** Обратитесь к разделу xref:getting-started.adoc#dependency-management[Управление зависимостями], чтобы добавить BOM Spring AI в ваш файл сборки.
Артефакты Spring AI публикуются в репозиториях Maven Central и Spring Snapshot.
Обратитесь к разделу Репозитории, чтобы добавить эти репозитории в вашу систему сборки.

### Основные свойства

Префикс `spring.ai.ollama` — это префикс свойств для настройки подключения к Ollama.

[cols="3,6,1"]
| Свойство | Описание | По умолчанию
| spring.ai.ollama.base-url | Базовый URL, по которому работает сервер API Ollama. | `+http://localhost:11434+`

Вот свойства для инициализации интеграции Ollama и xref:auto-pulling-models[автоматической загрузки моделей].

[cols="3,6,1"]
| Свойство | Описание | По умолчанию
| spring.ai.ollama.init.pull-model-strategy | Нужно ли загружать модели при запуске и как. | `never`
| spring.ai.ollama.init.timeout | Как долго ждать загрузки модели. | `5m`
| spring.ai.ollama.init.max-retries | Максимальное количество попыток для операции загрузки модели. | `0`
| spring.ai.ollama.init.embedding.include | Включить этот тип моделей в задачу инициализации. | `true`
| spring.ai.ollama.init.embedding.additional-models | Дополнительные модели для инициализации помимо тех, которые настроены через свойства по умолчанию. | `[]`

### Свойства векторного представления[NOTE]
====
Включение и отключение автоматической конфигурации встраивания теперь настраивается через свойства верхнего уровня с префиксом `spring.ai.model.embedding`.

Чтобы включить, используйте spring.ai.model.embedding=ollama (по умолчанию включено)

Чтобы отключить, используйте spring.ai.model.embedding=none (или любое значение, не соответствующее ollama)

Это изменение сделано для возможности конфигурации нескольких моделей.
====

Префикс `spring.ai.ollama.embedding.options` — это префикс свойства, который настраивает модель встраивания Ollama. Он включает в себя параметры запроса Ollama (расширенные), такие как `model`, `keep-alive` и `truncate`, а также свойства `options` модели Ollama.

Вот расширенные параметры запроса для модели встраивания Ollama:

[cols="4,5,1", stripes=even]
| Свойство | Описание | По умолчанию
| spring.ai.ollama.embedding.enabled (Удалено и больше не актуально)     | Включает автоматическую конфигурацию модели встраивания Ollama. | true
| spring.ai.model.embedding      | Включает автоматическую конфигурацию модели встраивания Ollama. | ollama
| spring.ai.ollama.embedding.options.model  | Имя https://github.com/ollama/ollama?tab=readme-ov-file#model-library[поддерживаемой модели], которую следует использовать. Вы можете использовать специализированные https://ollama.com/search?c=embedding[типы моделей встраивания] | mxbai-embed-large
| spring.ai.ollama.embedding.options.keep_alive  | Управляет тем, как долго модель будет оставаться загруженной в памяти после запроса | 5m
| spring.ai.ollama.embedding.options.truncate  | Укорачивает конец каждого ввода, чтобы вписаться в длину контекста. Возвращает ошибку, если false и превышена длина контекста.  | true

Оставшиеся свойства `options` основаны на [Допустимых параметрах и значениях Ollama](https://github.com/ollama/ollama/blob/main/docs/modelfile.md#valid-parameters-and-values) и [Типах Ollama](https://github.com/ollama/ollama/blob/main/api/types.go). Значения по умолчанию основаны на: [Типах по умолчанию Ollama](https://github.com/ollama/ollama/blob/b538dc3858014f94b099730a592751a5454cab0a/api/types.go#L364).

[cols="4,5,1", stripes=even]
| Свойство | Описание | По умолчанию
| spring.ai.ollama.embedding.options.numa              | Использовать ли NUMA.                                           | false
| spring.ai.ollama.embedding.options.num-ctx           | Устанавливает размер окна контекста, используемого для генерации следующего токена. | 2048
| spring.ai.ollama.embedding.options.num-batch         | Максимальный размер пакета для обработки подсказок. | 512
| spring.ai.ollama.embedding.options.num-gpu           | Количество слоев, которые будут отправлены на GPU(ы). На macOS по умолчанию 1 для включения поддержки metal, 0 для отключения. 1 здесь указывает, что NumGPU должен устанавливаться динамически | -1
| spring.ai.ollama.embedding.options.main-gpu          | При использовании нескольких GPU эта опция управляет тем, какой GPU используется для небольших тензоров, для которых разделение вычислений между всеми GPU нецелесообразно. Указанный GPU будет использовать немного больше VRAM для хранения временного буфера для промежуточных результатов. | 0
| spring.ai.ollama.embedding.options.low-vram          | -                                                             | false
| spring.ai.ollama.embedding.options.f16-kv            | -                                                             | true
| spring.ai.ollama.embedding.options.logits-all        | Возвращает логиты для всех токенов, а не только для последнего. Чтобы включить возвращение logprobs, это должно быть true. | -
| spring.ai.ollama.embedding.options.vocab-only        | Загружает только словарь, а не веса. | -
| spring.ai.ollama.embedding.options.use-mmap          | По умолчанию модели отображаются в память, что позволяет системе загружать только необходимые части модели по мере необходимости. Однако, если модель больше, чем общий объем вашей оперативной памяти, или если в вашей системе недостаточно доступной памяти, использование mmap может увеличить риск pageouts, негативно влияя на производительность. Отключение mmap приводит к более медленному времени загрузки, но может уменьшить количество pageouts, если вы не используете mlock. Обратите внимание, что если модель больше, чем общий объем оперативной памяти, отключение mmap предотвратит загрузку модели вообще. | null
| spring.ai.ollama.embedding.options.use-mlock         | Блокирует модель в памяти, предотвращая ее выгрузку при отображении в память. Это может улучшить производительность, но жертвует некоторыми преимуществами отображения в память, требуя больше оперативной памяти для работы и потенциально замедляя время загрузки по мере загрузки модели в оперативную память. | false
| spring.ai.ollama.embedding.options.num-thread        | Устанавливает количество потоков, используемых во время вычислений. По умолчанию Ollama будет определять это для оптимальной производительности. Рекомендуется установить это значение равным количеству физических ядер ЦП вашего компьютера (в отличие от логического количества ядер). 0 = позволить среде выполнения решить | 0
| spring.ai.ollama.embedding.options.num-keep          | -                                                             | 4
| spring.ai.ollama.embedding.options.seed              | Устанавливает начальное значение случайного числа для генерации. Установка этого значения на конкретное число заставит модель генерировать один и тот же текст для одной и той же подсказки.  | -1
| spring.ai.ollama.embedding.options.num-predict       | Максимальное количество токенов для предсказания при генерации текста. (-1 = бесконечная генерация, -2 = заполнение контекста) | -1
| spring.ai.ollama.embedding.options.top-k             | Уменьшает вероятность генерации бессмысленного текста. Более высокое значение (например, 100) даст более разнообразные ответы, в то время как более низкое значение (например, 10) будет более консервативным.  | 40
| spring.ai.ollama.embedding.options.top-p             | Работает вместе с top-k. Более высокое значение (например, 0.95) приведет к более разнообразному тексту, в то время как более низкое значение (например, 0.5) сгенерирует более сфокусированный и консервативный текст.  | 0.9
| spring.ai.ollama.embedding.options.min-p             | Альтернатива top_p, и направлена на обеспечение баланса между качеством и разнообразием. Параметр p представляет собой минимальную вероятность для токена, чтобы его можно было рассмотреть, относительно вероятности самого вероятного токена. Например, при p=0.05 и самой вероятной токен имеет вероятность 0.9, логиты со значением менее 0.045 отфильтровываются.  | 0.0
| spring.ai.ollama.embedding.options.tfs-z             | Выборка без хвоста используется для уменьшения влияния менее вероятных токенов на вывод. Более высокое значение (например, 2.0) уменьшит влияние больше, в то время как значение 1.0 отключает эту настройку. | 1.0
| spring.ai.ollama.embedding.options.typical-p         | -                                                             | 1.0
| spring.ai.ollama.embedding.options.repeat-last-n     | Устанавливает, насколько далеко назад модель должна смотреть, чтобы предотвратить повторения. (По умолчанию: 64, 0 = отключено, -1 = num_ctx) | 64
| spring.ai.ollama.embedding.options.temperature       | Температура модели. Увеличение температуры заставит модель отвечать более креативно. | 0.8
| spring.ai.ollama.embedding.options.repeat-penalty    | Устанавливает, насколько сильно наказывать за повторения. Более высокое значение (например, 1.5) будет сильнее наказывать за повторения, в то время как более низкое значение (например, 0.9) будет более снисходительным. | 1.1
| spring.ai.ollama.embedding.options.presence-penalty  | -                                                             | 0.0
| spring.ai.ollama.embedding.options.frequency-penalty | -                                                             | 0.0
| spring.ai.ollama.embedding.options.mirostat          | Включает выборку Mirostat для контроля перплексии. (по умолчанию: 0, 0 = отключено, 1 = Mirostat, 2 = Mirostat 2.0) | 0
| spring.ai.ollama.embedding.options.mirostat-tau      | Управляет балансом между связностью и разнообразием вывода. Более низкое значение приведет к более сфокусированному и связному тексту. | 5.0
| spring.ai.ollama.embedding.options.mirostat-eta      | Влияет на то, насколько быстро алгоритм реагирует на обратную связь от сгенерированного текста. Более низкая скорость обучения приведет к более медленным корректировкам, в то время как более высокая скорость обучения сделает алгоритм более отзывчивым. | 0.1
| spring.ai.ollama.embedding.options.penalize-newline  | -                                                             | true
| spring.ai.ollama.embedding.options.stop              | Устанавливает последовательности остановки для использования. Когда этот шаблон встречается, LLM прекратит генерировать текст и вернется. Можно установить несколько последовательностей остановки, указав несколько отдельных параметров остановки в файле модели. | -
| spring.ai.ollama.embedding.options.functions         | Список функций, идентифицированных по их именам, которые следует включить для вызова функций в одном запросе. Функции с этими именами должны существовать в реестре functionCallbacks. | -

> **Совет:** Все свойства с префиксом `spring.ai.ollama.embedding.options` могут быть переопределены во время выполнения, добавив специфичные для запроса <<embedding-options>> в вызов `EmbeddingRequest`.## Параметры выполнения [[embedding-options]]

Файл https://github.com/spring-projects/spring-ai/blob/main/models/spring-ai-ollama/src/main/java/org/springframework/ai/ollama/api/OllamaEmbeddingOptions.java[OllamaEmbeddingOptions.java] предоставляет конфигурации Ollama, такие как используемая модель, тонкая настройка GPU и CPU и т. д.

> **Важно:** Класс `OllamaOptions` устарел. Вместо него используйте `OllamaChatOptions` для чат-моделей и `OllamaEmbeddingOptions` для моделей встраивания. Новые классы предоставляют безопасные по типу, специфические для модели параметры конфигурации.

Стандартные параметры также можно настроить с помощью свойств `spring.ai.ollama.embedding.options`.

При запуске используйте `OllamaEmbeddingModel(OllamaApi ollamaApi, OllamaEmbeddingOptions defaultOptions)`, чтобы настроить стандартные параметры, используемые для всех запросов на встраивание. 
Во время выполнения вы можете переопределить стандартные параметры, используя экземпляр `OllamaEmbeddingOptions` в вашем `EmbeddingRequest`.

Например, чтобы переопределить стандартное имя модели для конкретного запроса:

```java
EmbeddingResponse embeddingResponse = embeddingModel.call(
    new EmbeddingRequest(List.of("Hello World", "World is big and salvation is near"),
        OllamaEmbeddingOptions.builder()
            .model("Different-Embedding-Model-Deployment-Name"))
            .truncates(false)
            .build());
```

[[auto-pulling-models]]
## Автоматическая загрузка моделей

Spring AI Ollama может автоматически загружать модели, когда они недоступны в вашем экземпляре Ollama. 
Эта функция особенно полезна для разработки и тестирования, а также для развертывания ваших приложений в новых средах.

> **Совет:** Вы также можете загружать по имени любую из тысяч бесплатных [GGUF Hugging Face Models](https://huggingface.co/models?library=gguf&sort=trending).

Существует три стратегии для загрузки моделей:

- `always` (определено в `PullModelStrategy.ALWAYS`): Всегда загружать модель, даже если она уже доступна. Полезно для обеспечения использования последней версии модели.
- `when_missing` (определено в `PullModelStrategy.WHEN_MISSING`): Загружать модель только в том случае, если она еще недоступна. Это может привести к использованию более старой версии модели.
- `never` (определено в `PullModelStrategy.NEVER`): Никогда не загружать модель автоматически.

> **Осторожно:** Из-за возможных задержек при загрузке моделей автоматическая загрузка не рекомендуется для производственных сред. Вместо этого рассмотрите возможность оценки и предварительной загрузки необходимых моделей заранее.

Все модели, определенные через свойства конфигурации и стандартные параметры, могут быть автоматически загружены при запуске. 
Вы можете настроить стратегию загрузки, тайм-аут и максимальное количество попыток с помощью свойств конфигурации:

```yaml
spring:
  ai:
    ollama:
      init:
        pull-model-strategy: always
        timeout: 60s
        max-retries: 1
```

> **Осторожно:** Приложение не завершит свою инициализацию, пока все указанные модели не будут доступны в Ollama. В зависимости от размера модели и скорости интернет-соединения это может значительно замедлить время запуска вашего приложения.

Вы можете инициализировать дополнительные модели при запуске, что полезно для моделей, используемых динамически во время выполнения:

```yaml
spring:
  ai:
    ollama:
      init:
        pull-model-strategy: always
        embedding:
          additional-models:
            - mxbai-embed-large
            - nomic-embed-text
```

Если вы хотите применить стратегию загрузки только к определенным типам моделей, вы можете исключить модели встраивания из задачи инициализации:

```yaml
spring:
  ai:
    ollama:
      init:
        pull-model-strategy: always
        embedding:
          include: false
```

Эта конфигурация применит стратегию загрузки ко всем моделям, кроме моделей встраивания.

## Модели HuggingFaceOllama может получить доступ ко всем https://huggingface.co/models?library=gguf&sort=trending[моделям векторного представления GGUF Hugging Face] из коробки. Вы можете загрузить любую из этих моделей по имени: `ollama pull hf.co/<username>/<model-repository>` или настроить стратегию автоматической загрузки: xref:auto-pulling-models[Автоматическая загрузка моделей]:

[source]
```
spring.ai.ollama.embedding.options.model=hf.co/mixedbread-ai/mxbai-embed-large-v1
spring.ai.ollama.init.pull-model-strategy=always
```

- `spring.ai.ollama.embedding.options.model`: Указывает на https://huggingface.co/models?library=gguf&sort=trending[модель Hugging Face GGUF], которую следует использовать. 
- `spring.ai.ollama.init.pull-model-strategy=always`: (необязательно) Включает автоматическую загрузку модели при запуске. 
Для продакшена вам следует заранее загрузить модели, чтобы избежать задержек: `ollama pull hf.co/mixedbread-ai/mxbai-embed-large-v1`.

## Пример контроллера

Это создаст реализацию `EmbeddingModel`, которую вы можете внедрить в свой класс. Вот пример простого класса `@Controller`, который использует реализацию `EmbeddingModel`.

```java
@RestController
public class EmbeddingController {

    private final EmbeddingModel embeddingModel;

    @Autowired
    public EmbeddingController(EmbeddingModel embeddingModel) {
        this.embeddingModel = embeddingModel;
    }

    @GetMapping("/ai/embedding")
    public Map embed(@RequestParam(value = "message", defaultValue = "Tell me a joke") String message) {
        EmbeddingResponse embeddingResponse = this.embeddingModel.embedForResponse(List.of(message));
        return Map.of("embedding", embeddingResponse);
    }
}
```

## Ручная конфигурация

Если вы не используете Spring Boot, вы можете вручную настроить `OllamaEmbeddingModel`. Для этого добавьте зависимость spring-ai-ollama в файл pom.xml вашего проекта Maven или в файл build.gradle Gradle:

[tabs]
======
Maven::
+
```xml
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-ollama</artifactId>
</dependency>
```

Gradle::
+
```groovy
dependencies {
    implementation 'org.springframework.ai:spring-ai-ollama'
}
```
======

> **Совет:** Обратитесь к разделу xref:getting-started.adoc#dependency-management[Управление зависимостями], чтобы добавить BOM Spring AI в ваш файл сборки.

> **Примечание:** Зависимость `spring-ai-ollama` также предоставляет доступ к `OllamaChatModel`. Для получения дополнительной информации о `OllamaChatModel` обратитесь к разделу [Ollama Chat Client](../chat/ollama-chat.html).

Далее создайте экземпляр `OllamaEmbeddingModel` и используйте его для вычисления векторных представлений для двух входных текстов, используя специализированные модели векторного представления `chroma/all-minilm-l6-v2-f32`:

```java
var ollamaApi = OllamaApi.builder().build();

var embeddingModel = new OllamaEmbeddingModel(this.ollamaApi,
        OllamaEmbeddingOptions.builder()
			.model(OllamaModel.MISTRAL.id())
            .build());

EmbeddingResponse embeddingResponse = this.embeddingModel.call(
    new EmbeddingRequest(List.of("Hello World", "World is big and salvation is near"),
        OllamaEmbeddingOptions.builder()
            .model("chroma/all-minilm-l6-v2-f32"))
            .truncate(false)
            .build());
```

`OllamaEmbeddingOptions` предоставляет информацию о конфигурации для всех запросов на векторное представление.
