# Ollama Chat

С помощью https://ollama.ai/[Ollama] вы можете запускать различные большие языковые модели (LLM) локально и генерировать текст на их основе. Spring AI поддерживает возможности завершения чата Ollama с помощью API `OllamaChatModel`.

> **Совет:** Ollama также предлагает совместимый с OpenAI API конечный пункт. Раздел xref:_openai_api_compatibility[Совместимость с OpenAI API] объясняет, как использовать xref:api/chat/openai-chat.adoc[Spring AI OpenAI] для подключения к серверу Ollama.

## Предварительные требования

Сначала вам нужен доступ к экземпляру Ollama. Есть несколько вариантов, включая следующие:

- [Скачайте и установите Ollama](https://ollama.com/download) на своем локальном компьютере.
- Настройте и xref:api/testcontainers.adoc[запустите Ollama через Testcontainers].
- Привяжитесь к экземпляру Ollama через xref:api/cloud-bindings.adoc[Привязки сервисов Kubernetes].

Вы можете загрузить модели, которые хотите использовать в своем приложении, из [библиотеки моделей Ollama](https://ollama.com/library):

```shellscript
ollama pull <model-name>
```

Вы также можете загрузить любую из тысяч бесплатных [GGUF моделей Hugging Face](https://huggingface.co/models?library=gguf&sort=trending):

```shellscript
ollama pull hf.co/<username>/<model-repository>
```

В качестве альтернативы вы можете включить опцию автоматической загрузки любых необходимых моделей: xref:auto-pulling-models[Автоматическая загрузка моделей].

## Автонастройка

[NOTE]
====
В автонастройке Spring AI произошли значительные изменения в названиях артефактов стартовых модулей. Пожалуйста, обратитесь к https://docs.spring.io/spring-ai/reference/upgrade-notes.html[заметкам об обновлении] для получения дополнительной информации.
====

Spring AI предоставляет автонастройку Spring Boot для интеграции чата Ollama. Чтобы включить ее, добавьте следующую зависимость в файл сборки Maven `pom.xml` или Gradle `build.gradle` вашего проекта:

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

### Основные свойства

Префикс `spring.ai.ollama` — это префикс свойств для настройки подключения к Ollama.

[cols="3,6,1", stripes=even]
|====
| Свойство | Описание | По умолчанию
| spring.ai.ollama.base-url | Базовый URL, по которому работает сервер API Ollama. | `+http://localhost:11434+`
|====

Вот свойства для инициализации интеграции Ollama и xref:auto-pulling-models[автоматической загрузки моделей].

[cols="3,6,1"]
|====
| Свойство | Описание | По умолчанию
| spring.ai.ollama.init.pull-model-strategy | Нужно ли загружать модели при запуске и как. | `never`
| spring.ai.ollama.init.timeout | Как долго ждать загрузки модели. | `5m`
| spring.ai.ollama.init.max-retries | Максимальное количество попыток для операции загрузки модели. | `0`
| spring.ai.ollama.init.chat.include | Включить этот тип моделей в задачу инициализации. | `true`
| spring.ai.ollama.init.chat.additional-models | Дополнительные модели для инициализации помимо тех, которые настроены через свойства по умолчанию. | `[]`
|====

### Свойства чата[NOTE]
====
Включение и отключение автонастроек чата теперь настраивается через свойства верхнего уровня с префиксом `spring.ai.model.chat`.

Чтобы включить, используйте spring.ai.model.chat=ollama (по умолчанию включено)

Чтобы отключить, используйте spring.ai.model.chat=none (или любое значение, которое не соответствует ollama)

Это изменение сделано для возможности конфигурации нескольких моделей.
====

Префикс `spring.ai.ollama.chat.options` — это префикс свойств, который настраивает модель чата Ollama.
Он включает в себя параметры запроса Ollama (расширенные), такие как `model`, `keep-alive` и `format`, а также свойства `options` модели Ollama.

Вот расширенные параметры запроса для модели чата Ollama:

[cols="3,6,1", stripes=even]
|====
| Свойство | Описание | По умолчанию
| spring.ai.ollama.chat.enabled (Удалено и больше не актуально)     | Включить модель чата Ollama. | true
| spring.ai.model.chat      | Включить модель чата Ollama. | ollama
| spring.ai.ollama.chat.options.model  | Имя https://github.com/ollama/ollama?tab=readme-ov-file#model-library[поддерживаемой модели], которую следует использовать. | mistral
| spring.ai.ollama.chat.options.format  | Формат, в котором будет возвращен ответ. Принимает либо `"json"` (любая структура JSON), либо объект JSON Schema (обязательная структура). См. <<Структурированные выходные данные>> для подробностей. | -
| spring.ai.ollama.chat.options.keep_alive  | Управляет тем, как долго модель будет оставаться загруженной в памяти после запроса | 5m
|====

Оставшиеся свойства `options` основаны на [Допустимых параметрах и значениях Ollama](https://github.com/ollama/ollama/blob/main/docs/modelfile.md#valid-parameters-and-values) и [Типах Ollama](https://github.com/ollama/ollama/blob/main/api/types.go). Значения по умолчанию основаны на [Типах по умолчанию Ollama](https://github.com/ollama/ollama/blob/b538dc3858014f94b099730a592751a5454cab0a/api/types.go#L364).

[cols="3,6,1", stripes=even]
|====
| Свойство | Описание | По умолчанию
| spring.ai.ollama.chat.options.numa              | Использовать ли NUMA.                                           | false
| spring.ai.ollama.chat.options.num-ctx           | Устанавливает размер контекстного окна, используемого для генерации следующего токена. | 2048
| spring.ai.ollama.chat.options.num-batch         | Максимальный размер партии для обработки подсказок. | 512
| spring.ai.ollama.chat.options.num-gpu           | Количество слоев, которые будут отправлены на GPU. На macOS по умолчанию 1 для включения поддержки metal, 0 для отключения. 1 здесь указывает, что NumGPU должен быть установлен динамически | -1
| spring.ai.ollama.chat.options.main-gpu          | При использовании нескольких GPU эта опция управляет тем, какой GPU используется для небольших тензоров, для которых разделение вычислений между всеми GPU нецелесообразно. Указанный GPU будет использовать немного больше VRAM для хранения временного буфера для промежуточных результатов. | 0
| spring.ai.ollama.chat.options.low-vram          | -                                                             | false
| spring.ai.ollama.chat.options.f16-kv            | -                                                             | true
| spring.ai.ollama.chat.options.logits-all        | Возвращает логиты для всех токенов, а не только для последнего. Чтобы включить возвращение logprobs, это должно быть true. | -
| spring.ai.ollama.chat.options.vocab-only        | Загружать только словарь, а не веса. | -
| spring.ai.ollama.chat.options.use-mmap          | По умолчанию модели отображаются в память, что позволяет системе загружать только необходимые части модели по мере необходимости. Однако, если модель больше, чем общий объем вашей оперативной памяти, или если в вашей системе недостаточно доступной памяти, использование mmap может увеличить риск pageouts, негативно влияя на производительность. Отключение mmap приводит к более медленному времени загрузки, но может уменьшить количество pageouts, если вы не используете mlock. Обратите внимание, что если модель больше общего объема оперативной памяти, отключение mmap предотвратит загрузку модели вообще. | null
| spring.ai.ollama.chat.options.use-mlock         | Блокирует модель в памяти, предотвращая ее выгрузку при отображении в память. Это может улучшить производительность, но жертвует некоторыми преимуществами отображения в память, требуя больше оперативной памяти для работы и потенциально замедляя время загрузки, когда модель загружается в оперативную память. | false
| spring.ai.ollama.chat.options.num-thread        | Устанавливает количество потоков, используемых во время вычислений. По умолчанию Ollama будет определять это для оптимальной производительности. Рекомендуется установить это значение равным количеству физических ядер ЦП вашего компьютера (в отличие от логического количества ядер). 0 = позволить среде выполнения решить | 0
| spring.ai.ollama.chat.options.num-keep          | -                                                             | 4
| spring.ai.ollama.chat.options.seed              | Устанавливает начальное значение случайного числа для генерации. Установка этого значения на конкретное число заставит модель генерировать один и тот же текст для одной и той же подсказки.  | -1
| spring.ai.ollama.chat.options.num-predict       | Максимальное количество токенов для предсказания при генерации текста. (-1 = бесконечная генерация, -2 = заполнение контекста) | -1
| spring.ai.ollama.chat.options.top-k             | Уменьшает вероятность генерации бессмысленного текста. Более высокое значение (например, 100) даст более разнообразные ответы, в то время как более низкое значение (например, 10) будет более консервативным.  | 40
| spring.ai.ollama.chat.options.top-p             | Работает вместе с top-k. Более высокое значение (например, 0.95) приведет к более разнообразному тексту, в то время как более низкое значение (например, 0.5) сгенерирует более сфокусированный и консервативный текст.  | 0.9
| spring.ai.ollama.chat.options.min-p             | Альтернатива top_p, и направлена на обеспечение баланса между качеством и разнообразием. Параметр p представляет собой минимальную вероятность для токена, чтобы его можно было рассматривать, относительно вероятности самого вероятного токена. Например, при p=0.05 и самой вероятной вероятности токена 0.9, логиты со значением менее 0.045 отфильтровываются.  | 0.0
| spring.ai.ollama.chat.options.tfs-z             | Выборка без хвоста используется для уменьшения влияния менее вероятных токенов на выходные данные. Более высокое значение (например, 2.0) уменьшит влияние больше, в то время как значение 1.0 отключает эту настройку. | 1.0
| spring.ai.ollama.chat.options.typical-p         | -                                                             | 1.0
| spring.ai.ollama.chat.options.repeat-last-n     | Устанавливает, насколько далеко назад модель должна смотреть, чтобы предотвратить повторения. (По умолчанию: 64, 0 = отключено, -1 = num_ctx) | 64
| spring.ai.ollama.chat.options.temperature       | Температура модели. Увеличение температуры заставит модель отвечать более креативно. | 0.8
| spring.ai.ollama.chat.options.repeat-penalty    | Устанавливает, насколько сильно наказывать за повторения. Более высокое значение (например, 1.5) будет более строго наказывать за повторения, в то время как более низкое значение (например, 0.9) будет более снисходительным. | 1.1
| spring.ai.ollama.chat.options.presence-penalty  | -                                                             | 0.0
| spring.ai.ollama.chat.options.frequency-penalty | -                                                             | 0.0
| spring.ai.ollama.chat.options.mirostat          | Включить выборку Mirostat для контроля перплексии. (по умолчанию: 0, 0 = отключено, 1 = Mirostat, 2 = Mirostat 2.0) | 0
| spring.ai.ollama.chat.options.mirostat-tau      | Управляет балансом между связностью и разнообразием выходных данных. Более низкое значение приведет к более сфокусированному и связному тексту. | 5.0
| spring.ai.ollama.chat.options.mirostat-eta      | Влияет на то, как быстро алгоритм реагирует на обратную связь от сгенерированного текста. Более низкая скорость обучения приведет к более медленным корректировкам, в то время как более высокая скорость обучения сделает алгоритм более отзывчивым. | 0.1
| spring.ai.ollama.chat.options.penalize-newline  | -                                                             | true
| spring.ai.ollama.chat.options.stop              | Устанавливает последовательности остановки для использования. Когда этот шаблон встречается, LLM прекратит генерировать текст и вернет результат. Можно установить несколько последовательностей остановки, указав несколько отдельных параметров остановки в файле модели. | -
| spring.ai.ollama.chat.options.tool-names         | Список инструментов, идентифицированных по их именам, которые следует включить для вызова функций в одном запросе. Инструменты с этими именами должны существовать в реестре ToolCallback. | -
| spring.ai.ollama.chat.options.tool-callbacks     | Обратные вызовы инструментов для регистрации с ChatModel. | -
| spring.ai.ollama.chat.options.internal-tool-execution-enabled | Если false, Spring AI не будет обрабатывать вызовы инструментов внутренне, а будет проксировать их клиенту. Тогда ответственность за обработку вызовов инструментов, их распределение на соответствующие функции и возврат результатов ложится на клиента. Если true (по умолчанию), Spring AI будет обрабатывать вызовы функций внутренне. Применимо только для моделей чата с поддержкой вызова функций | true
|====

> **Совет:** Все свойства с префиксом `spring.ai.ollama.chat.options` могут быть переопределены во время выполнения, добавляя специфические для запроса <<chat-options>> в вызов `Prompt`.## Runtime Options [[chat-options]]

Класс https://github.com/spring-projects/spring-ai/blob/main/models/spring-ai-ollama/src/main/java/org/springframework/ai/ollama/api/OllamaChatOptions.java[OllamaChatOptions.java] предоставляет конфигурации модели, такие как используемая модель, температура, режим мышления и т.д.

> **Важно:** Класс `OllamaOptions` устарел. Вместо него используйте `OllamaChatOptions` для чат-моделей и `OllamaEmbeddingOptions` для моделей встраивания. Новые классы предоставляют безопасные по типу, специфические для модели параметры конфигурации.

При запуске параметры по умолчанию можно настроить с помощью конструктора `OllamaChatModel(api, options)` или свойств `spring.ai.ollama.chat.options.*`.

Во время выполнения вы можете переопределить параметры по умолчанию, добавив новые, специфические для запроса параметры в вызов `Prompt`.
Например, чтобы переопределить модель и температуру по умолчанию для конкретного запроса:

```java
ChatResponse response = chatModel.call(
    new Prompt(
        "Сгенерируйте имена 5 известных пиратов.",
        OllamaChatOptions.builder()
            .model(OllamaModel.LLAMA3_1)
            .temperature(0.4)
            .build()
    ));
```

> **Совет:** В дополнение к специфическим для модели [OllamaChatOptions](https://github.com/spring-projects/spring-ai/blob/main/models/spring-ai-ollama/src/main/java/org/springframework/ai/ollama/api/OllamaChatOptions.java) вы можете использовать переносимый экземпляр [ChatOptions](https://github.com/spring-projects/spring-ai/blob/main/spring-ai-model/src/main/java/org/springframework/ai/chat/prompt/ChatOptions.java), созданный с помощью [ChatOptions#builder()](https://github.com/spring-projects/spring-ai/blob/main/spring-ai-model/src/main/java/org/springframework/ai/chat/prompt/DefaultChatOptionsBuilder.java).

[[auto-pulling-models]]
## Auto-pulling ModelsSpring AI Ollama может автоматически загружать модели, когда они недоступны в вашей инстанции Ollama. Эта функция особенно полезна для разработки и тестирования, а также для развертывания ваших приложений в новых средах.

> **Совет:** Вы также можете загрузить по имени любую из тысяч бесплатных [GGUF Hugging Face Models](https://huggingface.co/models?library=gguf&sort=trending).

Существует три стратегии загрузки моделей:

- `always` (определено в `PullModelStrategy.ALWAYS`): Всегда загружать модель, даже если она уже доступна. Полезно для обеспечения использования последней версии модели.
- `when_missing` (определено в `PullModelStrategy.WHEN_MISSING`): Загружать модель только в том случае, если она еще не доступна. Это может привести к использованию более старой версии модели.
- `never` (определено в `PullModelStrategy.NEVER`): Никогда не загружать модель автоматически.

> **Осторожно:** Из-за возможных задержек при загрузке моделей автоматическая загрузка не рекомендуется для производственных сред. Вместо этого рассмотрите возможность оценки и предварительной загрузки необходимых моделей заранее.

Все модели, определенные через свойства конфигурации и параметры по умолчанию, могут быть автоматически загружены во время запуска. Вы можете настроить стратегию загрузки, таймаут и максимальное количество попыток с помощью свойств конфигурации:

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
        chat:
          additional-models:
            - llama3.2
            - qwen2.5
```

Если вы хотите применить стратегию загрузки только к определенным типам моделей, вы можете исключить модели чата из задачи инициализации:

```yaml
spring:
  ai:
    ollama:
      init:
        pull-model-strategy: always
        chat:
          include: false
```

Эта конфигурация применит стратегию загрузки ко всем моделям, кроме моделей чата.

## Вызов Функций

Вы можете зарегистрировать пользовательские функции Java с `OllamaChatModel` и позволить модели Ollama интеллектуально выбирать вывод JSON-объекта, содержащего аргументы для вызова одной или нескольких зарегистрированных функций. Это мощная техника для соединения возможностей LLM с внешними инструментами и API. Узнайте больше о xref:api/tools.adoc[Вызов Инструментов].

> **Совет:** Вам нужна версия Ollama 0.2.8 или новее, чтобы использовать возможности функционального вызова, и версия Ollama 0.4.6 или новее, чтобы использовать их в режиме потоковой передачи.

## Режим Размышления (Reasoning)

Ollama поддерживает режим размышления для моделей рассуждений, которые могут выводить свой внутренний процесс размышления перед предоставлением окончательного ответа. Эта функция доступна для моделей, таких как Qwen3, DeepSeek-v3.1, DeepSeek R1 и GPT-OSS.

> **Совет:** Режим размышления помогает вам понять процесс размышления модели и может улучшить качество ответов на сложные проблемы.

> **Важно:** **Поведение по умолчанию (Ollama 0.12+)**: Модели, способные к размышлению (такие как `qwen3:**-thinking`, `deepseek-r1`, `deepseek-v3.1`), **автоматически включают размышление по умолчанию**, когда опция размышления не установлена явно. Стандартные модели (такие как `qwen2.5:**`, `llama3.2`) не включают размышление по умолчанию. Чтобы явно контролировать это поведение, используйте `.enableThinking()` или `.disableThinking()`.

### Включение Режима РазмышленияMost models (Qwen3, DeepSeek-v3.1, DeepSeek R1) поддерживают простое булевое включение/выключение:

```java
ChatResponse response = chatModel.call(
    new Prompt(
        "Сколько букв 'r' в слове 'strawberry'?",
        OllamaChatOptions.builder()
            .model("qwen3")
            .enableThinking()
            .build()
    ));

// Доступ к процессу мышления
String thinking = response.getResult().getMetadata().get("thinking");
String answer = response.getResult().getOutput().getText();
```

Вы также можете явно отключить мышление:

```java
ChatResponse response = chatModel.call(
    new Prompt(
        "Сколько будет 2+2?",
        OllamaChatOptions.builder()
            .model("deepseek-r1")
            .disableThinking()
            .build()
    ));
```

### Уровни мышления (только GPT-OSS)

Модель GPT-OSS требует явных уровней мышления вместо булевых значений:

```java
// Низкий уровень мышления
ChatResponse response = chatModel.call(
    new Prompt(
        "Сгенерируйте короткий заголовок",
        OllamaChatOptions.builder()
            .model("gpt-oss")
            .thinkLow()
            .build()
    ));

// Средний уровень мышления
ChatResponse response = chatModel.call(
    new Prompt(
        "Проанализируйте этот набор данных",
        OllamaChatOptions.builder()
            .model("gpt-oss")
            .thinkMedium()
            .build()
    ));

// Высокий уровень мышления
ChatResponse response = chatModel.call(
    new Prompt(
        "Решите эту сложную задачу",
        OllamaChatOptions.builder()
            .model("gpt-oss")
            .thinkHigh()
            .build()
    ));
```

### Доступ к содержимому мышления

Содержимое мышления доступно в метаданных ответа:

```java
ChatResponse response = chatModel.call(
    new Prompt(
        "Вычислите 17 × 23",
        OllamaChatOptions.builder()
            .model("deepseek-r1")
            .enableThinking()
            .build()
    ));

// Получить процесс рассуждения
String thinking = response.getResult().getMetadata().get("thinking");
System.out.println("Рассуждение: " + thinking);
// Вывод: "17 × 20 = 340, 17 × 3 = 51, 340 + 51 = 391"

// Получить окончательный ответ
String answer = response.getResult().getOutput().getText();
System.out.println("Ответ: " + answer);
// Вывод: "Ответ 391"
```

### Потоковая передача с мышлением

Режим мышления также работает с потоковыми ответами:

```java
Flux<ChatResponse> stream = chatModel.stream(
    new Prompt(
        "Объясните квантовую запутанность",
        OllamaChatOptions.builder()
            .model("qwen3")
            .enableThinking()
            .build()
    ));

stream.subscribe(response -> {
    String thinking = response.getResult().getMetadata().get("thinking");
    String content = response.getResult().getOutput().getText();

    if (thinking != null && !thinking.isEmpty()) {
        System.out.println("[Мышление] " + thinking);
    }
    if (content != null && !content.isEmpty()) {
        System.out.println("[Ответ] " + content);
    }
});
```

> **Примечание:** Когда мышление отключено или не установлено, поле метаданных `thinking` будет равно null или пустым.

## МультимодальныйMultimодальность относится к способности модели одновременно понимать и обрабатывать информацию из различных источников, включая текст, изображения, аудио и другие форматы данных.

Некоторые из моделей, доступных в Ollama с поддержкой мультимодальности, это https://ollama.com/library/llava[LLaVA] и https://ollama.com/library/bakllava[BakLLaVA] (см. [полный список](https://ollama.com/search?c=vision)).
Для получения дополнительной информации обратитесь к [LLaVA: Large Language and Vision Assistant](https://llava-vl.github.io/).

Ollama [Message API](https://github.com/ollama/ollama/blob/main/docs/api.md#parameters-1) предоставляет параметр "images" для включения списка изображений в формате base64 с сообщением.

Интерфейс [Message](https://github.com/spring-projects/spring-ai/blob/main/spring-ai-model/src/main/java/org/springframework/ai/chat/messages/Message.java) от Spring AI облегчает работу с мультимодальными AI моделями, вводя тип [Media](https://github.com/spring-projects/spring-ai/blob/main/spring-ai-commons/src/main/java/org/springframework/ai/content/Media.java).
Этот тип охватывает данные и детали, касающиеся медиа-вложений в сообщениях, используя `org.springframework.util.MimeType` и `org.springframework.core.io.Resource` для необработанных медиа-данных.

Ниже приведен простой пример кода, извлеченный из [OllamaChatModelMultimodalIT.java](https://github.com/spring-projects/spring-ai/blob/main/models/spring-ai-ollama/src/test/java/org/springframework/ai/ollama/OllamaChatModelMultimodalIT.java), иллюстрирующий объединение текста пользователя с изображением.

```java
var imageResource = new ClassPathResource("/multimodal.test.png");

var userMessage = new UserMessage("Объясните, что вы видите на этом изображении?",
        new Media(MimeTypeUtils.IMAGE_PNG, this.imageResource));

ChatResponse response = chatModel.call(new Prompt(this.userMessage,
        OllamaChatOptions.builder().model(OllamaModel.LLAVA)).build());
```

В примере модель принимает в качестве входных данных изображение `multimodal.test.png`:

![Multimodal Test Image, 200, 200, align="left"](multimodal.test.png)

вместе с текстовым сообщением "Объясните, что вы видите на этом изображении?", и генерирует ответ, подобный этому:

```
Изображение показывает небольшую металлическую корзину, наполненную спелыми бананами и красными яблоками. Корзина расположена на поверхности,
которая, похоже, является столом или столешницей, так как на заднем плане виден намек на кухонный шкаф или ящик.
Также за корзиной виден золотистый кольцо, что может указывать на то, что это фото было сделано в месте с металлическими украшениями или элементами. Общая обстановка предполагает домашнюю атмосферу,
где фрукты выставлены, возможно, для удобства или эстетических целей.
```

## Структурированные Выходы

Ollama предоставляет пользовательские https://ollama.com/blog/structured-outputs[Структурированные Выходы] API, которые гарантируют, что ваша модель генерирует ответы, строго соответствующие предоставленной вами `JSON Schema`.
В дополнение к существующему xref::api/structured-output-converter.adoc[Конвертеру Структурированных Выходов], эти API предлагают улучшенный контроль и точность.

### Два Режима для Структурированного Выхода

Ollama поддерживает два различных режима для структурированного выхода через параметр `format`:

1. **Простой "json" Формат**: Инструктирует Ollama возвращать любую допустимую JSON-структуру (непредсказуемая схема)
2. **Формат JSON Schema**: Инструктирует Ollama возвращать JSON, соответствующий конкретной схеме (предсказуемая структура)

#### Простой "json" Формат```markdown
Используйте это, когда вам нужен JSON-вывод, но не требуется конкретная структура:

```java
ChatResponse response = chatModel.call(
    new Prompt(
        "Перечислите 3 страны в Европе",
        OllamaChatOptions.builder()
            .model("llama3.2")
            .format("json")  // Любой допустимый JSON
            .build()
    ));
```

Модель может вернуть любую структуру JSON, которую она выберет:

```json
["Франция", "Германия", "Италия"]
// или
{"countries": ["Франция", "Германия", "Италия"]}
// или
{"data": {"european_countries": ["Франция", "Германия", "Италия"]}}
```

#### Формат JSON Schema (Рекомендуется для производства)

Используйте это, когда вам нужна гарантированная, предсказуемая структура:

```java
String jsonSchema = """
{
    "type": "object",
    "properties": {
        "countries": {
            "type": "array",
            "items": { "type": "string" }
        }
    },
    "required": ["countries"]
}
""";

ChatResponse response = chatModel.call(
    new Prompt(
        "Перечислите 3 страны в Европе",
        OllamaChatOptions.builder()
            .model("llama3.2")
            .outputSchema(jsonSchema)  // Обязательная схема
            .build()
    ));
```

Модель **должна** вернуть эту точную структуру:

```json
{"countries": ["Франция", "Германия", "Италия"]}
```

### Конфигурация

Spring AI позволяет вам программно настраивать формат ответа с помощью билдера `OllamaChatOptions`.

#### Использование билдера параметров чата с JSON Schema

Вы можете установить формат ответа программно с помощью билдера `OllamaChatOptions`:

```java
String jsonSchema = """
        {
            "type": "object",
            "properties": {
                "steps": {
                    "type": "array",
                    "items": {
                        "type": "object",
                        "properties": {
                            "explanation": { "type": "string" },
                            "output": { "type": "string" }
                        },
                        "required": ["explanation", "output"],
                        "additionalProperties": false
                    }
                },
                "final_answer": { "type": "string" }
            },
            "required": ["steps", "final_answer"],
            "additionalProperties": false
        }
        """;

Prompt prompt = new Prompt("как я могу решить 8x + 7 = -23",
        OllamaChatOptions.builder()
            .model(OllamaModel.LLAMA3_2.getName())
            .outputSchema(jsonSchema)  // Передать JSON Schema как строку
            .build());

ChatResponse response = this.ollamaChatModel.call(this.prompt);
```

#### Интеграция с утилитами BeanOutputConverter
```Вы можете использовать существующие xref::api/structured-output-converter.adoc#_bean_output_converter[BeanOutputConverter] утилиты для автоматической генерации JSON Schema из ваших доменных объектов, а затем преобразования структурированного ответа в экземпляры, специфичные для домена:

```java
record MathReasoning(
    @JsonProperty(required = true, value = "steps") Steps steps,
    @JsonProperty(required = true, value = "final_answer") String finalAnswer) {

    record Steps(
        @JsonProperty(required = true, value = "items") Items[] items) {

        record Items(
            @JsonProperty(required = true, value = "explanation") String explanation,
            @JsonProperty(required = true, value = "output") String output) {
        }
    }
}

var outputConverter = new BeanOutputConverter<>(MathReasoning.class);

Prompt prompt = new Prompt("как мне решить 8x + 7 = -23",
        OllamaChatOptions.builder()
            .model(OllamaModel.LLAMA3_2.getName())
            .outputSchema(outputConverter.getJsonSchema())  // Получить JSON Schema в виде строки
            .build());

ChatResponse response = this.ollamaChatModel.call(this.prompt);
String content = this.response.getResult().getOutput().getText();

MathReasoning mathReasoning = this.outputConverter.convert(this.content);
```

> **Примечание:** Убедитесь, что вы используете аннотацию `@JsonProperty(required = true,...)` для генерации схемы, которая точно помечает поля как `required`. Хотя это и не обязательно для JSON Schema, рекомендуется, чтобы структурированный ответ работал корректно.

### Методы API: `.format()` против `.outputSchema()`

Spring AI предоставляет два метода для настройки структурированного вывода:

[cols="2,3,3", options="header"]
|====
| Метод | Случай использования | Пример

| `.format("json")`
| Простой режим JSON - любая структура
| `.format("json")`

| `.outputSchema(jsonSchemaString)`
| Режим JSON Schema - принудительная структура
| `.outputSchema("{\"type\":\"object\",...}")`

| `.format(mapObject)`
| Режим JSON Schema - альтернативный API
| `.format(new ObjectMapper().readValue(schema, Map.class))`
|====

> **Совет:** Для большинства случаев используйте `.outputSchema(jsonSchemaString)` для валидации JSON Schema или `.format("json")` для простого JSON-вывода. Подход `.format(Map)` также поддерживается, но требует ручного парсинга JSON.

## Совместимость с API OpenAI

Ollama совместим с API OpenAI, и вы можете использовать xref:api/chat/openai-chat.adoc[Spring AI OpenAI] клиент для общения с Ollama и использования инструментов. Для этого вам нужно настроить базовый URL OpenAI на ваш экземпляр Ollama: `spring.ai.openai.chat.base-url=http://localhost:11434` и выбрать одну из предоставленных моделей Ollama: `spring.ai.openai.chat.options.model=mistral`.

> **Совет:** При использовании клиента OpenAI с Ollama вы можете передавать специфические для Ollama параметры (такие как `top_k`, `repeat_penalty`, `num_predict`) с помощью xref:api/chat/openai-chat.adoc#openai-compatible-servers[`extraBody` опции]. Это позволяет вам использовать все возможности Ollama, работая с клиентом OpenAI.

![Совместимость Ollama с API OpenAI, 800, 600, align="center"](spring-ai-ollama-over-openai.jpg)

### Содержимое рассуждений через совместимость с OpenAIOllama's OpenAI-совместимый конечный пункт поддерживает поле `reasoning_content` для моделей, способных к рассуждению (таких как `qwen3:*-thinking`, `deepseek-r1`, `deepseek-v3.1`).
При использовании клиента Spring AI OpenAI с Ollama процесс рассуждения модели автоматически фиксируется и становится доступным через метаданные ответа.

> **Примечание:** Это альтернатива использованию нативного API режима рассуждения Ollama (документированного в разделе <<Режим Рассуждения (Reasoning)>> выше).
Оба подхода работают с моделями рассуждения Ollama, но OpenAI-совместимый конечный пункт использует имя поля `reasoning_content` вместо `thinking`.

Вот пример доступа к содержимому рассуждения из Ollama через OpenAI клиент:

```java
// Настройка клиента Spring AI OpenAI для работы с Ollama
@Configuration
class OllamaConfig {
    @Bean
    OpenAiChatModel ollamaChatModel() {
        var openAiApi = new OpenAiApi("http://localhost:11434", "ollama");
        return new OpenAiChatModel(openAiApi,
            OpenAiChatOptions.builder()
                .model("deepseek-r1")  // или qwen3, deepseek-v3.1 и т.д.
                .build());
    }
}

// Использование модели с моделями, способными к рассуждению
ChatResponse response = chatModel.call(
    new Prompt("Сколько букв 'r' в слове 'strawberry'?"));

// Доступ к процессу рассуждения из метаданных
String reasoning = response.getResult().getMetadata().get("reasoningContent");
if (reasoning != null && !reasoning.isEmpty()) {
    System.out.println("Процесс рассуждения модели:");
    System.out.println(reasoning);
}

// Получение окончательного ответа
String answer = response.getResult().getOutput().getText();
System.out.println("Ответ: " + answer);
```

> **Совет:** Модели, способные к рассуждению, в Ollama (0.12+) автоматически включают режим рассуждения при доступе через OpenAI-совместимый конечный пункт.
Содержимое рассуждения фиксируется автоматически без необходимости дополнительной настройки.

Проверьте тесты [OllamaWithOpenAiChatModelIT.java](https://github.com/spring-projects/spring-ai/blob/main/models/spring-ai-openai/src/test/java/org/springframework/ai/openai/chat/proxy/OllamaWithOpenAiChatModelIT.java) для примеров использования Ollama через Spring AI OpenAI.

## Модели HuggingFace

Ollama может получить доступ ко всем https://huggingface.co/models?library=gguf&sort=trending[GGUF Hugging Face] чат-моделям прямо "из коробки".
Вы можете загрузить любую из этих моделей по имени: `ollama pull hf.co/<username>/<model-repository>` или настроить стратегию автоматической загрузки: xref:auto-pulling-models[Автоматическая загрузка моделей]:

[source]
```
spring.ai.ollama.chat.options.model=hf.co/bartowski/gemma-2-2b-it-GGUF
spring.ai.ollama.init.pull-model-strategy=always
```

- `spring.ai.ollama.chat.options.model`: Указывает на https://huggingface.co/models?library=gguf&sort=trending[модель Hugging Face GGUF], которую следует использовать. 
- `spring.ai.ollama.init.pull-model-strategy=always`: (необязательно) Включает автоматическую загрузку модели при запуске. 
Для продакшена вы должны заранее загрузить модели, чтобы избежать задержек: `ollama pull hf.co/bartowski/gemma-2-2b-it-GGUF`.

## Пример контроллераhttps://start.spring.io/[Создайте] новый проект Spring Boot и добавьте `spring-ai-starter-model-ollama` в зависимости вашего pom (или gradle).

Добавьте файл `application.yaml` в директорию `src/main/resources`, чтобы включить и настроить модель чата Ollama:

```yaml
spring:
  ai:
    ollama:
      base-url: http://localhost:11434
      chat:
        options:
          model: mistral
          temperature: 0.7
```

> **Совет:** Замените `base-url` на URL вашего сервера Ollama.

Это создаст реализацию `OllamaChatModel`, которую вы сможете внедрить в ваши классы. Вот пример простого класса `@RestController`, который использует модель чата для генерации текста.

```java
@RestController
public class ChatController {

    private final OllamaChatModel chatModel;

    @Autowired
    public ChatController(OllamaChatModel chatModel) {
        this.chatModel = chatModel;
    }

    @GetMapping("/ai/generate")
    public Map<String,String> generate(@RequestParam(value = "message", defaultValue = "Расскажи мне шутку") String message) {
        return Map.of("generation", this.chatModel.call(message));
    }

    @GetMapping("/ai/generateStream")
	public Flux<ChatResponse> generateStream(@RequestParam(value = "message", defaultValue = "Расскажи мне шутку") String message) {
        Prompt prompt = new Prompt(new UserMessage(message));
        return this.chatModel.stream(prompt);
    }

}
```

## Ручная конфигурация

Если вы не хотите использовать автонастройку Spring Boot, вы можете вручную настроить `OllamaChatModel` в вашем приложении. https://github.com/spring-projects/spring-ai/blob/main/models/spring-ai-ollama/src/main/java/org/springframework/ai/ollama/OllamaChatModel.java[OllamaChatModel] реализует `ChatModel` и `StreamingChatModel` и использует <<low-level-api>> для подключения к сервису Ollama.

Чтобы использовать его, добавьте зависимость `spring-ai-ollama` в файлы сборки Maven `pom.xml` или Gradle `build.gradle` вашего проекта:

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

> **Совет:** Обратитесь к разделу xref:getting-started.adoc#dependency-management[Управление зависимостями], чтобы добавить Spring AI BOM в ваш файл сборки.

> **Совет:** Зависимость `spring-ai-ollama` также предоставляет доступ к `OllamaEmbeddingModel`. Для получения дополнительной информации о `OllamaEmbeddingModel` обратитесь к разделу [Ollama Embedding Model](../embeddings/ollama-embeddings.html).

Далее создайте экземпляр `OllamaChatModel` и используйте его для отправки запросов на генерацию текста:

```java
var ollamaApi = OllamaApi.builder().build();

var chatModel = OllamaChatModel.builder()
                    .ollamaApi(ollamaApi)
                    .defaultOptions(
                        OllamaChatOptions.builder()
                            .model(OllamaModel.MISTRAL)
                            .temperature(0.9)
                            .build())
                    .build();

ChatResponse response = this.chatModel.call(
    new Prompt("Сгенерируйте имена 5 известных пиратов."));

// Или с потоковыми ответами
Flux<ChatResponse> response = this.chatModel.stream(
    new Prompt("Сгенерируйте имена 5 известных пиратов."));
```

`OllamaChatOptions` предоставляет информацию о конфигурации для всех запросов чата.

## Клиент низкого уровня OllamaApi [[low-level-api]][OllamaApi](https://github.com/spring-projects/spring-ai/blob/main/models/spring-ai-ollama/src/main/java/org/springframework/ai/ollama/api/OllamaApi.java) предоставляет легковесный Java-клиент для API завершения чата Ollama [Ollama Chat Completion API](https://github.com/ollama/ollama/blob/main/docs/api.md#generate-a-chat-completion).

Следующая диаграмма классов иллюстрирует интерфейсы чата и строительные блоки `OllamaApi`:

![Диаграмма API завершения чата OllamaApi, 800, 600](ollama-chat-completion-api.jpg)

> **Примечание:** `OllamaApi` является низкоуровневым API и не рекомендуется для прямого использования. Вместо этого используйте `OllamaChatModel`.

Вот простой фрагмент кода, показывающий, как использовать API программно:

```java
OllamaApi ollamaApi = new OllamaApi("YOUR_HOST:YOUR_PORT");

// Синхронный запрос
var request = ChatRequest.builder("orca-mini")
    .stream(false) // не потоковый
    .messages(List.of(
            Message.builder(Role.SYSTEM)
                .content("Вы учитель географии. Вы разговариваете со студентом.")
                .build(),
            Message.builder(Role.USER)
                .content("Какова столица Болгарии и каков её размер? "
                        + "Какой национальный гимн?")
                .build()))
    .options(OllamaChatOptions.builder().temperature(0.9).build())
    .build();

ChatResponse response = this.ollamaApi.chat(this.request);

// Потоковый запрос
var request2 = ChatRequest.builder("orca-mini")
    .ttream(true) // потоковый
    .messages(List.of(Message.builder(Role.USER)
        .content("Какова столица Болгарии и каков её размер? " + "Какой национальный гимн?")
        .build()))
    .options(OllamaChatOptions.builder().temperature(0.9).build().toMap())
    .build();

Flux<ChatResponse> streamingResponse = this.ollamaApi.streamingChat(this.request2);
```
