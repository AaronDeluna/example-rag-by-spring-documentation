# Конвертер структурированного вывода

Способность LLM генерировать структурированные выходные данные важна для последующих приложений, которые полагаются на надежный парсинг выходных значений. Разработчики хотят быстро преобразовывать результаты от модели ИИ в типы данных, такие как JSON, XML или классы Java, которые могут быть переданы другим функциям и методам приложения.

Конвертеры структурированного вывода Spring AI помогают преобразовать вывод LLM в структурированный формат. Как показано на следующей диаграмме, этот подход работает вокруг конечной точки текстового завершения LLM:

![Архитектура конвертера структурированного вывода, ширина=900, выравнивание="центр"](structured-output-architecture.jpg)

Генерация структурированных выходных данных из больших языковых моделей (LLM) с использованием универсальных API завершения требует тщательной обработки входных и выходных данных. Конвертер структурированного вывода играет ключевую роль до и после вызова LLM, обеспечивая достижение желаемой структуры вывода.

Перед вызовом LLM конвертер добавляет инструкции по формату к запросу, предоставляя явные указания моделям по генерации желаемой структуры вывода. Эти инструкции действуют как чертеж, формируя ответ модели в соответствии с указанным форматом.

> **Примечание:** Поскольку все больше моделей ИИ нативно поддерживают структурированные выходные данные, вы можете использовать эту возможность с помощью функции xref:api/chatclient.adoc#_native_structured_output[Нативный структурированный вывод] с `AdvisorParams.ENABLE_NATIVE_STRUCTURED_OUTPUT`. Этот подход использует сгенерированную JSON-схему напрямую с нативным API структурированного вывода модели, устраняя необходимость в предварительных инструкциях по форматированию и обеспечивая более надежные результаты.

После вызова LLM конвертер берет текстовый вывод модели и преобразует его в экземпляры структурированного типа. Этот процесс преобразования включает парсинг сырого текстового вывода и сопоставление его с соответствующим представлением структурированных данных, таким как JSON, XML или специфические для домена структуры данных.

> **Совет:** `StructuredOutputConverter` — это попытка преобразовать вывод модели в структурированный вывод. Модель ИИ не гарантирует возврат структурированного вывода, как запрашивалось. Модель может не понять запрос или не смочь сгенерировать структурированный вывод, как запрашивалось. Рассмотрите возможность реализации механизма валидации, чтобы убедиться, что вывод модели соответствует ожиданиям.

> **Совет:** `StructuredOutputConverter` не используется для вызова инструментов LLM xref:api/tools.adoc[Tool Calling], так как эта функция по умолчанию предоставляет структурированные выходные данные.

## API структурированного выводаИнтерфейс `StructuredOutputConverter` позволяет получать структурированный вывод, например, сопоставляя вывод с классом Java или массивом значений из текстового вывода модели ИИ. Определение интерфейса выглядит следующим образом:

```java
public interface StructuredOutputConverter<T> extends Converter<String, T>, FormatProvider {

}
```

Он объединяет интерфейс Spring https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/core/convert/converter/Converter.html[Converter<String, T>] и интерфейс `FormatProvider`.

```java
public interface FormatProvider {
	String getFormat();
}
```

Следующая диаграмма показывает поток данных при использовании API структурированного вывода.

![Structured Output API, width=900, align="center"](structured-output-api.jpg)

Интерфейс `FormatProvider` предоставляет конкретные рекомендации по форматированию модели ИИ, позволяя ей генерировать текстовые выводы, которые могут быть преобразованы в заданный целевой тип `T` с помощью `Converter`. Вот пример таких инструкций по форматированию:

```
  Ваш ответ должен быть в формате JSON.
  Структура данных для JSON должна соответствовать этому классу Java: java.util.HashMap
  Не включайте никаких объяснений, просто предоставьте ответ в формате JSON, соответствующий RFC8259, следуя этому формату без отклонений.
```

Инструкции по формату чаще всего добавляются в конец пользовательского ввода с использованием xref:api/prompt.adoc#_prompttemplate[PromptTemplate] следующим образом:

```java
    StructuredOutputConverter outputConverter = ...
    String userInputTemplate = """
        ... текстовый ввод пользователя ....
        {format}
        """; // ввод пользователя с заполнителем "format".
    Prompt prompt = new Prompt(
            PromptTemplate.builder()
						.template(this.userInputTemplate)
						.variables(Map.of(..., "format", this.outputConverter.getFormat())) // замените заполнитель "format" на формат конвертера.
						.build().createMessage()
    );
```

Интерфейс Converter<String, T> отвечает за преобразование текстового вывода модели в экземпляры указанного типа `T`.

### Доступные конвертерыВ настоящее время Spring AI предоставляет реализации `AbstractConversionServiceOutputConverter`, `AbstractMessageOutputConverter`, `BeanOutputConverter`, `MapOutputConverter` и `ListOutputConverter`:

![Иерархия классов структурированного вывода, ширина=900, выравнивание="центр"](structured-output-hierarchy4.jpg)

- `AbstractConversionServiceOutputConverter<T>` - Предлагает предварительно настроенный [GenericConversionService](https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/core/convert/support/GenericConversionService.html) для преобразования вывода LLM в желаемый формат. Не предоставляется реализация `FormatProvider` по умолчанию.
- `AbstractMessageOutputConverter<T>` - Обеспечивает предварительно настроенный [MessageConverter](https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/jms/support/converter/MessageConverter.html) для преобразования вывода LLM в желаемый формат. Не предоставляется реализация `FormatProvider` по умолчанию.
- `BeanOutputConverter<T>` - Настроенный с указанным классом Java (например, Bean) или [ParameterizedTypeReference](https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/core/ParameterizedTypeReference.html), этот конвертер использует реализацию `FormatProvider`, которая направляет AI Model на создание JSON-ответа, соответствующего `DRAFT_2020_12`, `JSON Schema`, основанному на указанном классе Java. Затем он использует `ObjectMapper` для десериализации JSON-вывода в экземпляр объекта Java целевого класса.
- `MapOutputConverter` - Расширяет функциональность `AbstractMessageOutputConverter` с реализацией `FormatProvider`, которая направляет AI Model на генерацию JSON-ответа, соответствующего RFC8259. Кроме того, он включает реализацию конвертера, которая использует предоставленный `MessageConverter` для преобразования JSON-данных в экземпляр `java.util.Map<String, Object>`.
- `ListOutputConverter` - Расширяет `AbstractConversionServiceOutputConverter` и включает реализацию `FormatProvider`, адаптированную для вывода в виде списка, разделенного запятыми. Реализация конвертера использует предоставленный `ConversionService` для преобразования текстового вывода модели в `java.util.List`.

## Использование конвертеров

В следующих разделах представлены руководства по использованию доступных конвертеров для генерации структурированных выводов.

### Конвертер вывода Bean

Следующий пример показывает, как использовать `BeanOutputConverter` для генерации фильмографии актера.

Целевая запись, представляющая фильмографию актера:

```java
record ActorsFilms(String actor, List<String> movies) {
}
```

Вот как применить BeanOutputConverter, используя высокоуровневый, удобный API `ChatClient`:

```java
ActorsFilms actorsFilms = ChatClient.create(chatModel).prompt()
        .user(u -> u.text("Сгенерируйте фильмографию из 5 фильмов для {actor}.")
                    .param("actor", "Том Хэнкс"))
        .call()
        .entity(ActorsFilms.class);
```

или используя низкоуровневый API `ChatModel` напрямую:

```java
BeanOutputConverter<ActorsFilms> beanOutputConverter =
    new BeanOutputConverter<>(ActorsFilms.class);

String format = this.beanOutputConverter.getFormat();

String actor = "Том Хэнкс";

String template = """
        Сгенерируйте фильмографию из 5 фильмов для {actor}.
        {format}
        """;

Generation generation = chatModel.call(
    PromptTemplate.builder().template(this.template).variables(Map.of("actor", this.actor, "format", this.format)).build().create()).getResult();

ActorsFilms actorsFilms = this.beanOutputConverter.convert(this.generation.getOutput().getText());
```

### Упорядочение свойств в сгенерированной схеме```
`BeanOutputConverter` поддерживает пользовательский порядок свойств в сгенерированной JSON-схеме с помощью аннотации `@JsonPropertyOrder`. Эта аннотация позволяет указать точную последовательность, в которой свойства должны появляться в схеме, независимо от их порядка объявления в классе или записи.

Например, чтобы обеспечить определенный порядок свойств в записи `ActorsFilms`:

```java
@JsonPropertyOrder({"actor", "movies"})
record ActorsFilms(String actor, List<String> movies) {}
```

Эта аннотация работает как с записями, так и с обычными классами Java.

#### Обобщенные типы бинов

Используйте конструктор `ParameterizedTypeReference`, чтобы указать более сложную структуру целевого класса. Например, чтобы представить список актеров и их фильмографий:

```java
List<ActorsFilms> actorsFilms = ChatClient.create(chatModel).prompt()
        .user("Сгенерируйте фильмографию из 5 фильмов для Тома Хэнкса и Билла Мюррея.")
        .call()
        .entity(new ParameterizedTypeReference<List<ActorsFilms>>() {});
```

или используя низкоуровневый API `ChatModel` напрямую:

```java
BeanOutputConverter<List<ActorsFilms>> outputConverter = new BeanOutputConverter<>(
        new ParameterizedTypeReference<List<ActorsFilms>>() { });

String format = this.outputConverter.getFormat();
String template = """
        Сгенерируйте фильмографию из 5 фильмов для Тома Хэнкса и Билла Мюррея.
        {format}
        """;

Prompt prompt = PromptTemplate.builder().template(this.template).variables(Map.of("format", this.format)).build().create();

Generation generation = chatModel.call(this.prompt).getResult();

List<ActorsFilms> actorsFilms = this.outputConverter.convert(this.generation.getOutput().getText());
```

### Конвертер вывода Map

Следующий фрагмент показывает, как использовать `MapOutputConverter` для преобразования вывода модели в список чисел в карте.

```java
Map<String, Object> result = ChatClient.create(chatModel).prompt()
        .user(u -> u.text("Предоставьте мне список {subject}")
                    .param("subject", "массив чисел от 1 до 9 под ключом 'numbers'"))
        .call()
        .entity(new ParameterizedTypeReference<Map<String, Object>>() {});
```

или используя низкоуровневый API `ChatModel` напрямую:

```java
MapOutputConverter mapOutputConverter = new MapOutputConverter();

String format = this.mapOutputConverter.getFormat();
String template = """
        Предоставьте мне список {subject}
        {format}
        """;

Prompt prompt = PromptTemplate.builder().template(this.template)
.variables(Map.of("subject", "массив чисел от 1 до 9 под ключом 'numbers'", "format", this.format)).build().create();

Generation generation = chatModel.call(this.prompt).getResult();

Map<String, Object> result = this.mapOutputConverter.convert(this.generation.getOutput().getText());
```

### Конвертер вывода List

Следующий фрагмент показывает, как использовать `ListOutputConverter` для преобразования вывода модели в список вкусов мороженого.

```java
List<String> flavors = ChatClient.create(chatModel).prompt()
                .user(u -> u.text("Перечислите пять {subject}")
                            .param("subject", "вкусов мороженого"))
                .call()
                .entity(new ListOutputConverter(new DefaultConversionService()));
```

или используя низкоуровневый API `ChatModel` напрямую:

```java
ListOutputConverter listOutputConverter = new ListOutputConverter(new DefaultConversionService());

String format = this.listOutputConverter.getFormat();
String template = """
        Перечислите пять {subject}
        {format}
        """;

Prompt prompt = PromptTemplate.builder().template(this.template).variables(Map.of("subject", "вкусов мороженого", "format", this.format)).build().create();

Generation generation = this.chatModel.call(this.prompt).getResult();

List<String> list = this.listOutputConverter.convert(this.generation.getOutput().getText());
```

## Нативный структурированный вывод
```Многие современные модели ИИ теперь предоставляют нативную поддержку структурированного вывода, что обеспечивает более надежные результаты по сравнению с форматированием на основе подсказок. Spring AI поддерживает это через функцию xref:api/chatclient.adoc#_native_structured_output[Native Structured Output].

При использовании нативного структурированного вывода JSON-схема, сгенерированная `BeanOutputConverter`, отправляется напрямую в API структурированного вывода модели, что устраняет необходимость в инструкциях по формату в подсказке. Этот подход обеспечивает:

- **Более высокую надежность**: Модель гарантирует вывод, соответствующий схеме
- **Чистые подсказки**: Нет необходимости добавлять инструкции по формату
- **Лучшее качество работы**: Модели могут оптимизировать внутреннюю работу для структурированного вывода

### Использование нативного структурированного вывода

Чтобы включить нативный структурированный вывод, используйте параметр `AdvisorParams.ENABLE_NATIVE_STRUCTURED_OUTPUT`:

```java
ActorsFilms actorsFilms = ChatClient.create(chatModel).prompt()
    .advisors(AdvisorParams.ENABLE_NATIVE_STRUCTURED_OUTPUT)
    .user("Сгенерируйте фильмографию для случайного актера.")
    .call()
    .entity(ActorsFilms.class);
```

Вы также можете установить это глобально, используя `defaultAdvisors()` в `ChatClient.Builder`:

```java
@Bean
ChatClient chatClient(ChatClient.Builder builder) {
    return builder
        .defaultAdvisors(AdvisorParams.ENABLE_NATIVE_STRUCTURED_OUTPUT)
        .build();
}
```

### Поддерживаемые модели для нативного структурированного вывода

Следующие модели в настоящее время поддерживают нативный структурированный вывод:

- **OpenAI**: модели GPT-4o и более поздние с поддержкой JSON Schema
- **Anthropic**: модели Claude 3.5 Sonnet и более поздние
- **Vertex AI Gemini**: модели Gemini 1.5 Pro и более поздние
- **Mistral AI**: модели Mistral Small и более поздние с поддержкой JSON Schema

> **Примечание:** Некоторые модели ИИ, такие как OpenAI, не поддерживают массивы объектов на верхнем уровне. В таких случаях вы можете использовать стандартное преобразование структурированного вывода Spring AI (без советника нативного структурированного вывода).

### Встроенный режим JSON

Некоторые модели ИИ предоставляют специальные параметры конфигурации для генерации структурированного (обычно JSON) вывода.

- xref:api/chat/openai-chat.adoc#_structured_outputs[OpenAI Structured Outputs] может гарантировать, что ваша модель генерирует ответы, строго соответствующие предоставленной вами JSON-схеме. Вы можете выбрать между `JSON_OBJECT`, который гарантирует, что сообщение, сгенерированное моделью, является допустимым JSON, или `JSON_SCHEMA` с предоставленной схемой, которая гарантирует, что модель сгенерирует ответ, соответствующий вашей схеме (`spring.ai.openai.chat.options.responseFormat`).
- xref:api/chat/azure-openai-chat.adoc[Azure OpenAI] - предоставляет параметры `spring.ai.azure.openai.chat.options.responseFormat`, указывающие формат, который модель должна выводить. Установка на `{ "type": "json_object" }` включает режим JSON, который гарантирует, что сообщение, сгенерированное моделью, является допустимым JSON.
- xref:api/chat/ollama-chat.adoc[Ollama] - предоставляет параметр `spring.ai.ollama.chat.options.format` для указания формата, в котором следует вернуть ответ. В настоящее время единственным допустимым значением является `json`.
- xref:api/chat/mistralai-chat.adoc[Mistral AI] - предоставляет параметр `spring.ai.mistralai.chat.options.responseFormat` для указания формата, в котором следует вернуть ответ. Установка на `{ "type": "json_object" }` включает режим JSON, который гарантирует, что сообщение, сгенерированное моделью, является допустимым JSON. Кроме того, установка на `{ "type": "json_schema" }` с предоставленной схемой включает поддержку нативного структурированного вывода, что гарантирует, что модель сгенерирует ответ, соответствующий вашей схеме.
