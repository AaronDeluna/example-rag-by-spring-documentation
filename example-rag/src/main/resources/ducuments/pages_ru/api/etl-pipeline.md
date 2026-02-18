# ETL Pipeline

Фреймворк Extract, Transform, and Load (ETL) служит основой обработки данных в случае использования Retrieval Augmented Generation (RAG).

ETL-пайплайн управляет потоком от сырых источников данных к структурированному векторному хранилищу, обеспечивая данные в оптимальном формате для извлечения AI-моделью.

Случай использования RAG заключается в том, чтобы дополнить возможности генеративных моделей, извлекая релевантную информацию из массива данных для повышения качества и актуальности сгенерированного вывода.

## Обзор API

ETL-пайплайны создают, преобразуют и хранят экземпляры `Document`.

![Spring AI Message API, width=400, align="center"](spring-ai-document1-api.jpg)

Класс `Document` содержит текст, метаданные и, при необходимости, дополнительные типы медиа, такие как изображения, аудио и видео.

Существует три основных компонента ETL-пайплайна:

- `DocumentReader`, который реализует `Supplier<List<Document>>`
- `DocumentTransformer`, который реализует `Function<List<Document>, List<Document>>`
- `DocumentWriter`, который реализует `Consumer<List<Document>>`

Содержимое класса `Document` создается из PDF, текстовых файлов и других типов документов с помощью `DocumentReader`.

Чтобы построить простой ETL-пайплайн, вы можете соединить экземпляры каждого типа.

![align="center"](etl-pipeline.jpg)

Предположим, у нас есть следующие экземпляры этих трех типов ETL:

- `PagePdfDocumentReader` — реализация `DocumentReader`
- `TokenTextSplitter` — реализация `DocumentTransformer`
- `VectorStore` — реализация `DocumentWriter`

Для выполнения базовой загрузки данных в векторную базу данных для использования с паттерном Retrieval Augmented Generation используйте следующий код в синтаксисе Java.

```java
vectorStore.accept(tokenTextSplitter.apply(pdfReader.get()));
```

В качестве альтернативы вы можете использовать имена методов, которые более естественно выражают домен.

```java
vectorStore.write(tokenTextSplitter.split(pdfReader.read()));
```

## Интерфейсы ETL

ETL-пайплайн состоит из следующих интерфейсов и реализаций. Подробная диаграмма классов ETL показана в разделе <<etl-class-diagram>>.

### DocumentReader

Предоставляет источник документов из различных источников.
```java
public interface DocumentReader extends Supplier<List<Document>> {

    default List<Document> read() {
		return get();
	}
}
```


### DocumentTransformer

Преобразует пакет документов в рамках рабочего процесса обработки.

```java
public interface DocumentTransformer extends Function<List<Document>, List<Document>> {

    default List<Document> transform(List<Document> transform) {
		return apply(transform);
	}
}
```


### DocumentWriter

Управляет финальной стадией процесса ETL, подготавливая документы для хранения.

```java
public interface DocumentWriter extends Consumer<List<Document>> {

    default void write(List<Document> documents) {
		accept(documents);
	}
}
```


[[etl-class-diagram]]
### Диаграмма классов ETL

Следующая диаграмма классов иллюстрирует интерфейсы и реализации ETL.

// ![align="center", width="800px"](etl-class-diagram.jpg)
![align="center"](etl-class-diagram.jpg)

## DocumentReaders

### JSON

`JsonReader` обрабатывает JSON-документы, преобразуя их в список объектов `Document`.


#### Пример

```java
@Component
class MyJsonReader {

	private final Resource resource;

    MyJsonReader(@Value("classpath:bikes.json") Resource resource) {
        this.resource = resource;
    }

	List<Document> loadJsonAsDocuments() {
        JsonReader jsonReader = new JsonReader(this.resource, "description", "content");
        return jsonReader.get();
	}
}
```

#### Варианты конструктора

`JsonReader` предоставляет несколько вариантов конструктора:

1. `JsonReader(Resource resource)`
2. `JsonReader(Resource resource, String... jsonKeysToUse)`
3. `JsonReader(Resource resource, JsonMetadataGenerator jsonMetadataGenerator, String... jsonKeysToUse)`

#### Параметры```markdown
- `resource`: Объект Spring `Resource`, указывающий на JSON-файл.
- `jsonKeysToUse`: Массив ключей из JSON, которые должны использоваться в качестве текстового содержимого в результирующих объектах `Document`.
- `jsonMetadataGenerator`: Необязательный `JsonMetadataGenerator` для создания метаданных для каждого `Document`.

#### Поведение

`JsonReader` обрабатывает JSON-содержимое следующим образом:

- Он может обрабатывать как JSON-массивы, так и одиночные JSON-объекты.
- Для каждого JSON-объекта (либо в массиве, либо одиночного объекта):
** Он извлекает содержимое на основе указанных `jsonKeysToUse`.
** Если ключи не указаны, он использует весь JSON-объект в качестве содержимого.
** Он генерирует метаданные, используя предоставленный `JsonMetadataGenerator` (или пустой, если не предоставлен).
** Он создает объект `Document` с извлеченным содержимым и метаданными.

#### Использование JSON-указателей

`JsonReader` теперь поддерживает извлечение конкретных частей JSON-документа с помощью JSON-указателей. Эта функция позволяет легко извлекать вложенные данные из сложных JSON-структур.

##### Метод `get(String pointer)`

```java
public List<Document> get(String pointer)
```

Этот метод позволяет использовать JSON-указатель для извлечения конкретной части JSON-документа.

###### Параметры

- `pointer`: Строка JSON-указателя (как определено в RFC 6901) для нахождения желаемого элемента в JSON-структуре.

###### Возвращаемое значение

- Возвращает `List<Document>`, содержащий документы, разобранные из JSON-элемента, расположенного по указателю.

###### Поведение

- Метод использует предоставленный JSON-указатель для навигации к конкретному месту в JSON-структуре.
- Если указатель действителен и указывает на существующий элемент:
** Для JSON-объекта: он возвращает список с одним Document.
** Для JSON-массива: он возвращает список документов, по одному для каждого элемента в массиве.
- Если указатель недействителен или указывает на несуществующий элемент, он выбрасывает `IllegalArgumentException`.

###### Пример

```java
JsonReader jsonReader = new JsonReader(resource, "description");
List<Document> documents = this.jsonReader.get("/store/books/0");
```

#### Пример структуры JSON

```json
[
  {
    "id": 1,
    "brand": "Trek",
    "description": "Высокопроизводительный горный велосипед для катания по тропам."
  },
  {
    "id": 2,
    "brand": "Cannondale",
    "description": "Аэродинамический шоссейный велосипед для любителей гонок."
  }
]
```

В этом примере, если `JsonReader` настроен с `"description"` в качестве `jsonKeysToUse`, он создаст объекты `Document`, где содержимое — это значение поля "description" для каждого велосипеда в массиве.

#### Заметки

- `JsonReader` использует Jackson для разбора JSON.
- Он может эффективно обрабатывать большие JSON-файлы, используя потоковую обработку для массивов.
- Если в `jsonKeysToUse` указано несколько ключей, содержимое будет конкатенацией значений для этих ключей.
- Читатель гибок и может быть адаптирован к различным JSON-структурам путем настройки `jsonKeysToUse` и `JsonMetadataGenerator`.

### Text
`TextReader` обрабатывает текстовые документы, преобразуя их в список объектов `Document`.

#### Пример

```java
@Component
class MyTextReader {

    private final Resource resource;

    MyTextReader(@Value("classpath:text-source.txt") Resource resource) {
        this.resource = resource;
    }

	List<Document> loadText() {
		TextReader textReader = new TextReader(this.resource);
		textReader.getCustomMetadata().put("filename", "text-source.txt");

		return textReader.read();
    }
}
```

#### Варианты конструктора

`TextReader` предоставляет два варианта конструктора:

1. `TextReader(String resourceUrl)`
2. `TextReader(Resource resource)`

#### Параметры

- `resourceUrl`: Строка, представляющая URL ресурса, который нужно прочитать.
- `resource`: Объект Spring `Resource`, указывающий на текстовый файл.

#### Конфигурация
```- `setCharset(Charset charset)`: Устанавливает набор символов, используемый для чтения текстового файла. По умолчанию используется UTF-8.
- `getCustomMetadata()`: Возвращает изменяемую карту, в которую можно добавить пользовательские метаданные для документов.

#### Поведение

`TextReader` обрабатывает текстовое содержимое следующим образом:

- Он считывает все содержимое текстового файла в один объект `Document`.
- Содержимое файла становится содержимым `Document`.
- Метаданные автоматически добавляются в `Document`:
** `charset`: Набор символов, используемый для чтения файла (по умолчанию: "UTF-8").
** `source`: Имя файла исходного текстового файла.
- Любые пользовательские метаданные, добавленные через `getCustomMetadata()`, включаются в `Document`.

#### Примечания

- `TextReader` считывает все содержимое файла в память, поэтому он может быть не подходящим для очень больших файлов.
- Если вам нужно разбить текст на более мелкие части, вы можете использовать разделитель текста, такой как `TokenTextSplitter`, после чтения документа:

```java
List<Document> documents = textReader.get();
List<Document> splitDocuments = new TokenTextSplitter().apply(this.documents);
```

- Читатель использует абстракцию `Resource` из Spring, что позволяет ему считывать из различных источников (classpath, файловая система, URL и т.д.).
- Пользовательские метаданные могут быть добавлены ко всем документам, созданным читателем, с помощью метода `getCustomMetadata()`.


### HTML (JSoup)

`JsoupDocumentReader` обрабатывает HTML-документы, преобразуя их в список объектов `Document` с использованием библиотеки JSoup.

#### Зависимости
Добавьте зависимость в ваш проект, используя Maven или Gradle.

[tabs]
======
Maven::
+
```xml
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-jsoup-document-reader</artifactId>
</dependency>
```

Gradle::
+
```groovy
dependencies {
    implementation 'org.springframework.ai:spring-ai-jsoup-document-reader'
}
```
======

#### Пример

```java
@Component
class MyHtmlReader {

    private final Resource resource;

    MyHtmlReader(@Value("classpath:/my-page.html") Resource resource) {
        this.resource = resource;
    }

    List<Document> loadHtml() {
        JsoupDocumentReaderConfig config = JsoupDocumentReaderConfig.builder()
            .selector("article p") // Извлекает абзацы внутри тегов <article>
            .charset("ISO-8859-1")  // Использует кодировку ISO-8859-1
            .includeLinkUrls(true) // Включает URL ссылок в метаданные
            .metadataTags(List.of("author", "date")) // Извлекает метатеги автора и даты
            .additionalMetadata("source", "my-page.html") // Добавляет пользовательские метаданные
            .build();

        JsoupDocumentReader reader = new JsoupDocumentReader(this.resource, config);
        return reader.get();
    }
}
```

`JsoupDocumentReaderConfig` позволяет вам настроить поведение `JsoupDocumentReader`:

- `charset`: Указывает кодировку символов HTML-документа (по умолчанию "UTF-8").
- `selector`: CSS-селектор JSoup для указания, из каких элементов извлекать текст (по умолчанию "body").
- `separator`: Строка, используемая для объединения текста из нескольких выбранных элементов (по умолчанию "\n").
- `allElements`: Если `true`, извлекает весь текст из элемента `<body>`, игнорируя `selector` (по умолчанию `false`).
- `groupByElement`: Если `true`, создает отдельный `Document` для каждого элемента, соответствующего `selector` (по умолчанию `false`).
- `includeLinkUrls`: Если `true`, извлекает абсолютные URL ссылок и добавляет их в метаданные (по умолчанию `false`).
- `metadataTags`: Список имен тегов `<meta>`, из которых нужно извлекать содержимое (по умолчанию `["description", "keywords"]`).
- `additionalMetadata`: Позволяет добавлять пользовательские метаданные ко всем созданным объектам `Document`.

#### Пример документа: my-page.html```markdown
Поведение:

`JsoupDocumentReader` обрабатывает HTML-контент и создает объекты `Document` на основе конфигурации:

- `selector` определяет, какие элементы используются для извлечения текста.
- Если `allElements` равно `true`, весь текст внутри `<body>` извлекается в один `Document`.
- Если `groupByElement` равно `true`, каждый элемент, соответствующий `selector`, создает отдельный `Document`.
- Если ни `allElements`, ни `groupByElement` не равны `true`, текст из всех элементов, соответствующих `selector`, объединяется с использованием `separator`.
- Заголовок документа, содержимое указанных тегов `<meta>` и (по желанию) URL-ссылки добавляются в метаданные `Document`.
- Базовый URI, для разрешения относительных ссылок, будет извлечен из URL-ресурсов.

Читатель сохраняет текстовое содержимое выбранных элементов, но удаляет любые HTML-теги внутри них.

### Markdown

`MarkdownDocumentReader` обрабатывает Markdown-документы, преобразуя их в список объектов `Document`.

#### Зависимости
Добавьте зависимость в ваш проект, используя Maven или Gradle.

[tabs]
======
Maven::
+
```xml
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-markdown-document-reader</artifactId>
</dependency>
```

Gradle::
+
```groovy
dependencies {
    implementation 'org.springframework.ai:spring-ai-markdown-document-reader'
}
```
======

#### Пример

```java
@Component
class MyMarkdownReader {

    private final Resource resource;

    MyMarkdownReader(@Value("classpath:code.md") Resource resource) {
        this.resource = resource;
    }

    List<Document> loadMarkdown() {
        MarkdownDocumentReaderConfig config = MarkdownDocumentReaderConfig.builder()
            .withHorizontalRuleCreateDocument(true)
            .withIncludeCodeBlock(false)
            .withIncludeBlockquote(false)
            .withAdditionalMetadata("filename", "code.md")
            .build();

        MarkdownDocumentReader reader = new MarkdownDocumentReader(this.resource, config);
        return reader.get();
    }
}
```

`MarkdownDocumentReaderConfig` позволяет вам настраивать поведение `MarkdownDocumentReader`:

- `horizontalRuleCreateDocument`: Если установлено в `true`, горизонтальные правила в Markdown создадут новые объекты `Document`.
- `includeCodeBlock`: Если установлено в `true`, блоки кода будут включены в тот же `Document`, что и окружающий текст. Если `false`, блоки кода создают отдельные объекты `Document`.
- `includeBlockquote`: Если установлено в `true`, блоки цитат будут включены в тот же `Document`, что и окружающий текст. Если `false`, блоки цитат создают отдельные объекты `Document`.
- `additionalMetadata`: Позволяет добавлять пользовательские метаданные ко всем созданным объектам `Document`.

#### Пример документа: code.md
``````markdown
Это пример Java-приложения:

```java
package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class DemoApplication {
    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }
}
```

Markdown также предоставляет возможность `использовать форматирование встроенного кода на протяжении` всего предложения.

---

Еще одна возможность — установить блочный код без конкретного выделения:

```
./mvnw spring-javaformat:apply
```
```

Поведение: MarkdownDocumentReader обрабатывает содержимое Markdown и создает объекты Document на основе конфигурации:

- Заголовки становятся метаданными в объектах Document.
- Параграфы становятся содержимым объектов Document.
- Блоки кода могут быть отделены в отдельные объекты Document или включены с окружающим текстом.
- Цитаты могут быть отделены в отдельные объекты Document или включены с окружающим текстом.
- Горизонтальные линии могут использоваться для разделения содержимого на отдельные объекты Document.

Читатель сохраняет форматирование, такое как встроенный код, списки и стили текста внутри содержимого объектов Document.


### PDF-страница
`PagePdfDocumentReader` использует библиотеку Apache PdfBox для разбора PDF-документов.

#### Зависимости
Добавьте зависимость в ваш проект, используя Maven или Gradle.

[tabs]
======
Maven::
+
```xml
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-pdf-document-reader</artifactId>
</dependency>
```

Gradle::
+
```groovy
dependencies {
    implementation 'org.springframework.ai:spring-ai-pdf-document-reader'
}
```
======

#### Пример

```java
@Component
public class MyPagePdfDocumentReader {

	List<Document> getDocsFromPdf() {

		PagePdfDocumentReader pdfReader = new PagePdfDocumentReader("classpath:/sample1.pdf",
				PdfDocumentReaderConfig.builder()
					.withPageTopMargin(0)
					.withPageExtractedTextFormatter(ExtractedTextFormatter.builder()
						.withNumberOfTopTextLinesToDelete(0)
						.build())
					.withPagesPerDocument(1)
					.build());

		return pdfReader.read();
    }

}

```

### PDF-параграф
`ParagraphPdfDocumentReader` использует информацию из каталога PDF (например, оглавление) для разделения входного PDF на текстовые параграфы и вывода одного `Document` на параграф.
> **Примечание:** Не все PDF-документы содержат каталог PDF.

#### Зависимости
Добавьте зависимость в ваш проект, используя Maven или Gradle.

[tabs]
======
Maven::
+
```xml
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-pdf-document-reader</artifactId>
</dependency>
```

Gradle::
+
```groovy
dependencies {
    implementation 'org.springframework.ai:spring-ai-pdf-document-reader'
}
```
======

#### Пример

```java
@Component
public class MyPagePdfDocumentReader {

	List<Document> getDocsFromPdfWithCatalog() {

        ParagraphPdfDocumentReader pdfReader = new ParagraphPdfDocumentReader("classpath:/sample1.pdf",
                PdfDocumentReaderConfig.builder()
                    .withPageTopMargin(0)
                    .withPageExtractedTextFormatter(ExtractedTextFormatter.builder()
                        .withNumberOfTopTextLinesToDelete(0)
                        .build())
                    .withPagesPerDocument(1)
                    .build());

	    return pdfReader.read();
    }
}
```


### Tika (DOCX, PPTX, HTML...)
`TikaDocumentReader` использует Apache Tika для извлечения текста из различных форматов документов, таких как PDF, DOC/DOCX, PPT/PPTX и HTML. Для получения полного списка поддерживаемых форматов обратитесь к https://tika.apache.org/3.1.0/formats.html[документации Tika].

#### Зависимости
```Add the dependency to your project using Maven or Gradle.

[tabs]
======
Maven::
+
```xml
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-tika-document-reader</artifactId>
</dependency>
```

Gradle::
+
```groovy
dependencies {
    implementation 'org.springframework.ai:spring-ai-tika-document-reader'
}
```
======

#### Пример

```java
@Component
class MyTikaDocumentReader {

    private final Resource resource;

    MyTikaDocumentReader(@Value("classpath:/word-sample.docx")
                            Resource resource) {
        this.resource = resource;
    }

    List<Document> loadText() {
        TikaDocumentReader tikaDocumentReader = new TikaDocumentReader(this.resource);
        return tikaDocumentReader.read();
    }
}
```

## Преобразователи

### TextSplitter
`TextSplitter` — это абстрактный базовый класс, который помогает делить документы, чтобы они соответствовали контекстному окну модели ИИ.

### TokenTextSplitter
`TokenTextSplitter` — это реализация `TextSplitter`, которая разбивает текст на части на основе количества токенов, используя кодировку CL100K_BASE.

#### Использование

##### Основное использование

```java
@Component
class MyTokenTextSplitter {

    public List<Document> splitDocuments(List<Document> documents) {
        TokenTextSplitter splitter = new TokenTextSplitter();
        return splitter.apply(documents);
    }

    public List<Document> splitCustomized(List<Document> documents) {
        TokenTextSplitter splitter = new TokenTextSplitter(1000, 400, 10, 5000, true, List.of('.', '?', '!', '\n'));
        return splitter.apply(documents);
    }
}
```

##### Использование паттерна Строитель

Рекомендуемый способ создания `TokenTextSplitter` — использовать паттерн Строитель, который предоставляет более читаемый и гибкий API:

```java
@Component
class MyTokenTextSplitter {

    public List<Document> splitWithBuilder(List<Document> documents) {
        TokenTextSplitter splitter = TokenTextSplitter.builder()
            .withChunkSize(1000)
            .withMinChunkSizeChars(400)
            .withMinChunkLengthToEmbed(10)
            .withMaxNumChunks(5000)
            .withKeepSeparator(true)
            .build();

        return splitter.apply(documents);
    }
}
```

##### Пользовательские знаки препинания

Вы можете настроить знаки препинания, используемые для разделения текста на семантически значимые части. Это особенно полезно для интернационализации:

```java
@Component
class MyInternationalTextSplitter {

    public List<Document> splitChineseText(List<Document> documents) {
        // Используйте китайские знаки препинания
        TokenTextSplitter splitter = TokenTextSplitter.builder()
            .withChunkSize(800)
            .withMinChunkSizeChars(350)
            .withPunctuationMarks(List.of('。', '？', '！', '；'))  // Китайские знаки препинания
            .build();

        return splitter.apply(documents);
    }

    public List<Document> splitWithCustomMarks(List<Document> documents) {
        // Смешанные английские и другие знаки препинания
        TokenTextSplitter splitter = TokenTextSplitter.builder()
            .withChunkSize(800)
            .withPunctuationMarks(List.of('.', '?', '!', '\n', ';', ':', '。'))
            .build();

        return splitter.apply(documents);
    }
}
```

#### Опции конструктора

`TokenTextSplitter` предоставляет три варианта конструктора:

1. `TokenTextSplitter()`: Создает разделитель с настройками по умолчанию.
2. `TokenTextSplitter(boolean keepSeparator)`: Создает разделитель с пользовательским поведением разделителя.
3. `TokenTextSplitter(int chunkSize, int minChunkSizeChars, int minChunkLengthToEmbed, int maxNumChunks, boolean keepSeparator, List<Character> punctuationMarks)`: Полный конструктор со всеми параметрами настройки.

> **Примечание:** Паттерн Строитель (показанный выше) является рекомендуемым подходом для создания экземпляров с пользовательскими конфигурациями.

#### Параметры- `chunkSize`: Целевая размерность каждого текстового фрагмента в токенах (по умолчанию: 800).
- `minChunkSizeChars`: Минимальный размер каждого текстового фрагмента в символах (по умолчанию: 350).
- `minChunkLengthToEmbed`: Минимальная длина фрагмента для включения (по умолчанию: 5).
- `maxNumChunks`: Максимальное количество фрагментов, которые можно сгенерировать из текста (по умолчанию: 10000).
- `keepSeparator`: Нужно ли сохранять разделители (например, переносы строк) в фрагментах (по умолчанию: true).
- `punctuationMarks`: Список символов, используемых в качестве границ предложений для разбиения (по умолчанию: `.`, `?`, `!`, `\n`).

#### Поведение

`TokenTextSplitter` обрабатывает текстовый контент следующим образом:

1. Кодирует входной текст в токены с использованием кодировки CL100K_BASE.
2. Разбивает закодированный текст на фрагменты на основе `chunkSize`.
3. Для каждого фрагмента:
   a. Декодирует фрагмент обратно в текст.
   b. **Только если общее количество токенов превышает размер фрагмента**, пытается найти подходящую точку разбиения (используя настроенные `punctuationMarks`) после `minChunkSizeChars`.
   c. Если точка разбиения найдена, обрезает фрагмент в этой точке.
   d. Обрезает фрагмент и при необходимости удаляет символы переноса строки в зависимости от настройки `keepSeparator`.
   e. Если получившийся фрагмент длиннее `minChunkLengthToEmbed`, он добавляется в выходные данные.
4. Этот процесс продолжается, пока все токены не будут обработаны или не будет достигнуто `maxNumChunks`.
5. Любой оставшийся текст добавляется в качестве финального фрагмента, если он длиннее `minChunkLengthToEmbed`.

> **Важно:** Разбиение на основе пунктуации применяется только в том случае, если количество токенов превышает размер фрагмента. Текст, который точно соответствует или меньше размера фрагмента, возвращается в виде одного фрагмента без обрезки на основе пунктуации. Это предотвращает ненужное разбиение небольших текстов.

#### Пример

```java
Document doc1 = new Document("This is a long piece of text that needs to be split into smaller chunks for processing.",
        Map.of("source", "example.txt"));
Document doc2 = new Document("Another document with content that will be split based on token count.",
        Map.of("source", "example2.txt"));

TokenTextSplitter splitter = new TokenTextSplitter();
List<Document> splitDocuments = this.splitter.apply(List.of(this.doc1, this.doc2));

for (Document doc : splitDocuments) {
    System.out.println("Chunk: " + doc.getContent());
    System.out.println("Metadata: " + doc.getMetadata());
}
```

#### Заметки

- `TokenTextSplitter` использует кодировку CL100K_BASE из библиотеки `jtokkit`, которая совместима с новыми моделями OpenAI.
- Разделитель пытается создать семантически значимые фрагменты, разбивая их на границах предложений, где это возможно.
- Метаданные из оригинальных документов сохраняются и копируются во все фрагменты, полученные из этого документа.
- Форматировщик контента (если установлен) из оригинального документа также копируется в производные фрагменты, если `copyContentFormatter` установлен в `true` (поведение по умолчанию).
- Этот разделитель особенно полезен для подготовки текста для больших языковых моделей, которые имеют ограничения по токенам, обеспечивая, чтобы каждый фрагмент находился в пределах возможностей обработки модели.
- **Пользовательские знаки препинания**: Стандартные знаки препинания (`.`, `?`, `!`, `\n`) хорошо работают для английского текста. Для других языков или специализированного контента настройте знаки препинания с помощью метода `withPunctuationMarks()` строителя.
- **Учет производительности**: Хотя разделитель может обрабатывать любое количество знаков препинания, рекомендуется держать список разумно небольшим (менее 20 символов) для оптимальной производительности, так как каждый знак проверяется для каждого фрагмента.
- **Расширяемость**: Метод `getLastPunctuationIndex(String)` является `protected`, что позволяет подклассам переопределять логику обнаружения пунктуации для специализированных случаев использования.
- **Обработка небольших текстов**: Начиная с версии 2.0, небольшие тексты (с количеством токенов на уровне или ниже размера фрагмента) больше не разбиваются по знакам препинания, предотвращая ненужную фрагментацию контента, который уже соответствует размерным ограничениям.

### ContentFormatTransformerОбеспечивает единообразные форматы контента во всех документах.

### KeywordMetadataEnricher
`KeywordMetadataEnricher` — это `DocumentTransformer`, который использует модель генеративного ИИ для извлечения ключевых слов из содержимого документа и добавления их в качестве метаданных.

#### Использование

```java
@Component
class MyKeywordEnricher {

    private final ChatModel chatModel;

    MyKeywordEnricher(ChatModel chatModel) {
        this.chatModel = chatModel;
    }

    List<Document> enrichDocuments(List<Document> documents) {
        KeywordMetadataEnricher enricher = KeywordMetadataEnricher.builder(chatModel)
                .keywordCount(5)
                .build();

        // Или используйте пользовательские шаблоны
        KeywordMetadataEnricher enricher = KeywordMetadataEnricher.builder(chatModel)
               .keywordsTemplate(YOUR_CUSTOM_TEMPLATE)
               .build();

        return enricher.apply(documents);
    }
}
```

#### Опции конструктора

`KeywordMetadataEnricher` предоставляет две опции конструктора:

1. `KeywordMetadataEnricher(ChatModel chatModel, int keywordCount)`: Для использования шаблона по умолчанию и извлечения указанного количества ключевых слов.
2. `KeywordMetadataEnricher(ChatModel chatModel, PromptTemplate keywordsTemplate)`: Для использования пользовательского шаблона для извлечения ключевых слов.

#### Поведение

`KeywordMetadataEnricher` обрабатывает документы следующим образом:

1. Для каждого входного документа он создает запрос, используя содержимое документа.
2. Он отправляет этот запрос в предоставленный `ChatModel` для генерации ключевых слов.
3. Сгенерированные ключевые слова добавляются в метаданные документа под ключом "excerpt_keywords".
4. Возвращаются обогащенные документы.

#### Настройка

Вы можете использовать шаблон по умолчанию или настроить шаблон через параметр keywordsTemplate. Шаблон по умолчанию:

```java
\{context_str}. Give %s unique keywords for this document. Format as comma separated. Keywords:
```

Где `+{context_str}+` заменяется на содержимое документа, а `%s` заменяется на указанное количество ключевых слов.

#### Пример

```java
ChatModel chatModel = // инициализируйте вашу модель чата
KeywordMetadataEnricher enricher = KeywordMetadataEnricher.builder(chatModel)
                .keywordCount(5)
                .build();

// Или используйте пользовательские шаблоны
KeywordMetadataEnricher enricher = KeywordMetadataEnricher.builder(chatModel)
                .keywordsTemplate(new PromptTemplate("Извлеките 5 важных ключевых слов из следующего текста и разделите их запятыми:\n{context_str}"))
                .build();

Document doc = new Document("Это документ о искусственном интеллекте и его применении в современных технологиях.");

List<Document> enrichedDocs = enricher.apply(List.of(this.doc));

Document enrichedDoc = this.enrichedDocs.get(0);
String keywords = (String) this.enrichedDoc.getMetadata().get("excerpt_keywords");
System.out.println("Извлеченные ключевые слова: " + keywords);
```

#### Примечания

- `KeywordMetadataEnricher` требует функционирующую `ChatModel` для генерации ключевых слов.
- Количество ключевых слов должно быть 1 или больше.
- Обогатитель добавляет поле метаданных "excerpt_keywords" в каждый обработанный документ.
- Сгенерированные ключевые слова возвращаются в виде строки, разделенной запятыми.
- Этот обогатитель особенно полезен для улучшения поиска по документам и для генерации тегов или категорий для документов.
- В паттерне Builder, если параметр `keywordsTemplate` установлен, параметр `keywordCount` будет проигнорирован.

### SummaryMetadataEnricher
`SummaryMetadataEnricher` — это `DocumentTransformer`, который использует модель генеративного ИИ для создания резюме для документов и добавления их в качестве метаданных. Он может генерировать резюме для текущего документа, а также для соседних документов (предыдущего и следующего).

#### Использование```java
@Configuration
class EnricherConfig {

    @Bean
    public SummaryMetadataEnricher summaryMetadata(OpenAiChatModel aiClient) {
        return new SummaryMetadataEnricher(aiClient,
            List.of(SummaryType.PREVIOUS, SummaryType.CURRENT, SummaryType.NEXT));
    }
}

@Component
class MySummaryEnricher {

    private final SummaryMetadataEnricher enricher;

    MySummaryEnricher(SummaryMetadataEnricher enricher) {
        this.enricher = enricher;
    }

    List<Document> enrichDocuments(List<Document> documents) {
        return this.enricher.apply(documents);
    }
}
```


#### Конструктор

`SummaryMetadataEnricher` предоставляет два конструктора:

1. `SummaryMetadataEnricher(ChatModel chatModel, List<SummaryType> summaryTypes)`
2. `SummaryMetadataEnricher(ChatModel chatModel, List<SummaryType> summaryTypes, String summaryTemplate, MetadataMode metadataMode)`

#### Параметры

- `chatModel`: ИИ модель, используемая для генерации резюме.
- `summaryTypes`: Список значений перечисления `SummaryType`, указывающий, какие резюме генерировать (PREVIOUS, CURRENT, NEXT).
- `summaryTemplate`: Пользовательский шаблон для генерации резюме (необязательно).
- `metadataMode`: Указывает, как обрабатывать метаданные документа при генерации резюме (необязательно).

#### Поведение

`SummaryMetadataEnricher` обрабатывает документы следующим образом:

1. Для каждого входного документа он создает запрос, используя содержимое документа и указанный шаблон резюме.
2. Он отправляет этот запрос в предоставленную `ChatModel` для генерации резюме.
3. В зависимости от указанных `summaryTypes`, он добавляет следующие метаданные к каждому документу:
- `section_summary`: Резюме текущего документа.
- `prev_section_summary`: Резюме предыдущего документа (если доступно и запрошено).
- `next_section_summary`: Резюме следующего документа (если доступно и запрошено).
4. Возвращаются обогащенные документы.

#### Настройка

Запрос на генерацию резюме можно настроить, предоставив пользовательский `summaryTemplate`. Шаблон по умолчанию:

```java
"""
Here is the content of the section:
{context_str}

Summarize the key topics and entities of the section.

Summary:
"""
```

#### Пример

```java
ChatModel chatModel = // инициализируйте вашу модель чата
SummaryMetadataEnricher enricher = new SummaryMetadataEnricher(chatModel,
    List.of(SummaryType.PREVIOUS, SummaryType.CURRENT, SummaryType.NEXT));

Document doc1 = new Document("Содержимое документа 1");
Document doc2 = new Document("Содержимое документа 2");

List<Document> enrichedDocs = enricher.apply(List.of(this.doc1, this.doc2));

// Проверьте метаданные обогащенных документов
for (Document doc : enrichedDocs) {
    System.out.println("Текущее резюме: " + doc.getMetadata().get("section_summary"));
    System.out.println("Предыдущее резюме: " + doc.getMetadata().get("prev_section_summary"));
    System.out.println("Следующее резюме: " + doc.getMetadata().get("next_section_summary"));
}
```

Предоставленный пример демонстрирует ожидаемое поведение:

- Для списка из двух документов оба документа получают `section_summary`.
- Первый документ получает `next_section_summary`, но не получает `prev_section_summary`.
- Второй документ получает `prev_section_summary`, но не получает `next_section_summary`.
- `section_summary` первого документа совпадает с `prev_section_summary` второго документа.
- `next_section_summary` первого документа совпадает с `section_summary` второго документа.

#### Примечания

- `SummaryMetadataEnricher` требует функционирующую `ChatModel` для генерации резюме.
- Обогатитель может обрабатывать списки документов любого размера, правильно обрабатывая крайние случаи для первого и последнего документов.
- Этот обогатитель особенно полезен для создания контекстно-зависимых резюме, что позволяет лучше понять взаимосвязи документов в последовательности.
- Параметр `MetadataMode` позволяет контролировать, как существующие метаданные включаются в процесс генерации резюме.


## Авторы

### Файл
``````markdown
`FileDocumentWriter` — это реализация `DocumentWriter`, которая записывает содержимое списка объектов `Document` в файл.

#### Использование

```java
@Component
class MyDocumentWriter {

    public void writeDocuments(List<Document> documents) {
        FileDocumentWriter writer = new FileDocumentWriter("output.txt", true, MetadataMode.ALL, false);
        writer.accept(documents);
    }
}
```

#### Конструкторы

`FileDocumentWriter` предоставляет три конструктора:

1. `FileDocumentWriter(String fileName)`
2. `FileDocumentWriter(String fileName, boolean withDocumentMarkers)`
3. `FileDocumentWriter(String fileName, boolean withDocumentMarkers, MetadataMode metadataMode, boolean append)`

#### Параметры

- `fileName`: Имя файла, в который будут записаны документы.
- `withDocumentMarkers`: Указывает, следует ли включать маркеры документов в вывод (по умолчанию: false).
- `metadataMode`: Указывает, какое содержимое документа будет записано в файл (по умолчанию: MetadataMode.NONE).
- `append`: Если true, данные будут записаны в конец файла, а не в начало (по умолчанию: false).

#### Поведение

`FileDocumentWriter` обрабатывает документы следующим образом:

1. Открывает FileWriter для указанного имени файла.
2. Для каждого документа в входном списке:
   a. Если `withDocumentMarkers` равно true, записывает маркер документа, включая индекс документа и номера страниц.
   b. Записывает отформатированное содержимое документа в зависимости от указанного `metadataMode`.
3. Файл закрывается после записи всех документов.

#### Маркеры документов

Когда `withDocumentMarkers` установлено в true, писатель включает маркеры для каждого документа в следующем формате:

[source]
```
### Doc: [index], pages:[start_page_number,end_page_number]
```

#### Обработка метаданных

Писатель использует два специфических ключа метаданных:

- `page_number`: Представляет начальный номер страницы документа.
- `end_page_number`: Представляет конечный номер страницы документа.

Эти ключи используются при записи маркеров документов.

#### Пример

```java
List<Document> documents = // инициализируйте ваши документы
FileDocumentWriter writer = new FileDocumentWriter("output.txt", true, MetadataMode.ALL, true);
writer.accept(documents);
```

Это запишет все документы в "output.txt", включая маркеры документов, используя все доступные метаданные и добавляя данные в файл, если он уже существует.

#### Примечания

- Писатель использует `FileWriter`, поэтому он записывает текстовые файлы с кодировкой символов по умолчанию для операционной системы.
- Если во время записи происходит ошибка, выбрасывается `RuntimeException` с оригинальным исключением в качестве причины.
- Параметр `metadataMode` позволяет контролировать, как существующие метаданные включаются в записанное содержимое.
- Этот писатель особенно полезен для отладки или создания читаемых человеком выводов коллекций документов.

### VectorStore

Обеспечивает интеграцию с различными векторными хранилищами.
Смотрите xref:api/vectordbs.adoc[Документация по Vector DB] для полного списка.
```
