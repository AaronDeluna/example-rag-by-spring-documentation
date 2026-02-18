# ETL-конвейер

Платформа извлечения, преобразования и загрузки (ETL) служит основой обработки данных в сценарии использования расширенной генерации извлечения (RAG).

Конвейер ETL организует поток от источников необработанных данных к структурированному векторному хранилищу, гарантируя, что данные находятся в оптимальном формате для извлечения моделью ИИ.

Вариант использования RAG — это текст, расширяющий возможности генеративных моделей путем извлечения соответствующей информации из массива данных для повышения качества и актуальности сгенерированных результатов.

## Обзор API

Конвейеры ETL создают, преобразуют и сохраняют экземпляры `Document`.

![API сообщений Spring AI](spring-ai-document1-api.jpg)

Класс `Document` содержит текст, метаданные и, при необходимости, дополнительные типы мультимедиа, такие как изображения, аудио и видео.

Существует три основных компонента конвейера ETL:

- `DocumentReader`, реализующий `Supplier<List<Document>>`
- `DocumentTransformer`, реализующий `Function<List<Document>, List<Document>>`
- `DocumentWriter`, реализующий `Consumer<List<Document>>`

Содержимое класса `Document` создается из PDF-файлов, текстовых файлов и других типов документов с помощью `DocumentReader`.

Чтобы построить простой конвейер ETL, вы можете объединить экземпляры каждого типа.

![выровнять = "центр"](etl-pipeline.jpg)

Допустим, у нас есть следующие экземпляры этих трех типов ETL.

- `PagePdfDocumentReader` реализация `DocumentReader`
- `TokenTextSplitter` реализация `DocumentTransformer`
- `VectorStore` реализация `DocumentWriter`

Чтобы выполнить базовую загрузку данных в базу данных векторов для использования с шаблоном расширенной генерации извлечения, используйте следующий код в синтаксисе функционального стиля Java.

```java
vectorStore.accept(tokenTextSplitter.apply(pdfReader.get()));
```

В качестве альтернативы вы можете использовать имена методов, которые более естественно выразительны для предметной области.

```java
vectorStore.write(tokenTextSplitter.split(pdfReader.read()));
```

## ETL-интерфейсы

Конвейер ETL состоит из следующих интерфейсов и реализаций.
Подробная диаграмма классов ETL показана в разделе <<etl-class-diagram>>.

### DocumentReader

Предоставляет источник документов различного происхождения.
```java
public interface DocumentReader extends Supplier<List<Document>> {

    default List<Document> read() {
		return get();
	}
}
```


### ДокументТрансформер

Преобразует пакет документов в рамках рабочего процесса обработки.

```java
public interface DocumentTransformer extends Function<List<Document>, List<Document>> {

    default List<Document> transform(List<Document> transform) {
		return apply(transform);
	}
}
```


### Автор документов

Управляет заключительным этапом процесса ETL, подготавливая документы к хранению.

```java
public interface DocumentWriter extends Consumer<List<Document>> {

    default void write(List<Document> documents) {
		accept(documents);
	}
}
```


### Диаграмма классов ETL

Следующая диаграмма классов иллюстрирует интерфейсы и реализации ETL.

// изображение::etl-class-diagram.jpg[align="center", width="800px"]
![выровнять = "центр"](etl-class-diagram.jpg)

## Читатели документов

### JSON

`JsonReader` обрабатывает документы JSON, преобразуя их в список объектов `Document`.


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

#### Параметры конструктора

`JsonReader` предоставляет несколько вариантов конструктора:

1. `JsonReader(Resource resource)`
2. `JsonReader(Resource resource, String... jsonKeysToUse)`
3. `JsonReader(Resource resource, JsonMetadataGenerator jsonMetadataGenerator, String... jsonKeysToUse)`

#### Параметры

- `resource`: объект Spring `Resource`, указывающий на файл JSON.
- `jsonKeysToUse`: массив ключей из JSON, который следует использовать в качестве текстового содержимого в результирующих объектах `Document`.
- `jsonMetadataGenerator`: необязательный `JsonMetadataGenerator` для создания метаданных для каждого `Document`.

#### Поведение

`JsonReader` обрабатывает содержимое JSON следующим образом:

- Он может обрабатывать как массивы JSON, так и отдельные объекты JSON.
- Для каждого объекта JSON (в массиве или отдельном объекте):
  - Он извлекает содержимое на основе указанного `jsonKeysToUse`.
  - Если ключи не указаны, в качестве содержимого используется весь объект JSON.
  - Он генерирует метаданные, используя предоставленный `JsonMetadataGenerator` (или пустой, если он не указан).
  - Он создает объект `Document` с извлеченным содержимым и метаданными.


#### Использование указателей JSON

`JsonReader` теперь поддерживает извлечение определенных частей документа JSON с помощью указателей JSON. Эта функция позволяет легко извлекать вложенные данные из сложных структур JSON.

##### Метод `get(String pointer)`

```java
public List<Document> get(String pointer)
```

Этот метод позволяет использовать указатель JSON для получения определенной части документа JSON.

###### Параметры

- `pointer`: строка указателя JSON (как определено в RFC 6901) для поиска нужного элемента в структуре JSON.

###### Возвращаемое значение

- Возвращает `List<Document>`, содержащий документы, проанализированные из элемента JSON, расположенного по указателю.

###### Поведение

- Этот метод использует предоставленный указатель JSON для перехода к определенному местоположению в структуре JSON.
- Если указатель действителен и указывает на существующий элемент:
  - Для объекта JSON: он возвращает список с одним документом.
  - Для массива JSON: он возвращает список документов, по одному для каждого элемента массива.
- Если указатель недействителен или указывает на несуществующий элемент, он выдает `IllegalArgumentException`.

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
    "description": "A high-performance mountain bike for trail riding."
  },
  {
    "id": 2,
    "brand": "Cannondale",
    "description": "An aerodynamic road bike for racing enthusiasts."
  }
]
```

В этом примере, если `JsonReader` настроен с `"description"` в качестве `jsonKeysToUse`, он создаст объекты `Document`, содержимым которых является значение поля «описание» для каждого велосипеда в массиве.

#### Примечания

- `JsonReader` использует Джексона для анализа JSON.
- Он может эффективно обрабатывать большие файлы JSON, используя потоковую передачу для массивов.
- Если в `jsonKeysToUse` указано несколько ключей, содержимое будет представлять собой объединение значений этих ключей.
- Программа чтения является гибкой и может быть адаптирована к различным структурам JSON путем настройки `jsonKeysToUse` и `JsonMetadataGenerator`.


### Текст
`TextReader` обрабатывает обычные текстовые документы, преобразуя их в список объектов `Document`.

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

#### Параметры конструктора

`TextReader` предоставляет два варианта конструктора:

1. `TextReader(String resourceUrl)`
2. `TextReader(Resource resource)`

#### Параметры

- `resourceUrl`: строка, представляющая URL-адрес ресурса, который необходимо прочитать.
- `resource`: объект Spring `Resource`, указывающий на текстовый файл.

#### Конфигурация

- `setCharset(Charset charset)`: устанавливает набор символов, используемый для чтения текстового файла. По умолчанию — UTF-8.
- `getCustomMetadata()`: возвращает изменяемую карту, куда вы можете добавлять собственные метаданные для документов.

#### Поведение

`TextReader` обрабатывает текстовое содержимое следующим образом:

- Он считывает все содержимое текстового файла в один объект `Document`.
- Содержимое файла становится содержимым `Document`.
- Метаданные автоматически добавляются в `Document`:
  - `charset`: набор символов, используемый для чтения файла (по умолчанию: «UTF-8»).
  - `source`: имя исходного текстового файла.
- Любые пользовательские метаданные, добавленные через `getCustomMetadata()`, включаются в файл `Document`.


#### Примечания

- `TextReader` считывает все содержимое файла в память, поэтому он может не подойти для очень больших файлов.
- Если вам нужно разделить текст на более мелкие фрагменты, вы можете использовать разделитель текста, например `TokenTextSplitter`, после прочтения документа:

```java
List<Document> documents = textReader.get();
List<Document> splitDocuments = new TokenTextSplitter().apply(this.documents);
```

- Программа чтения использует абстракцию Spring `Resource`, что позволяет ей читать из различных источников (путь к классам, файловая система, URL-адрес и т. д.).
- Пользовательские метаданные можно добавлять ко всем документам, созданным читателем, с помощью метода `getCustomMetadata()`.


### HTML (JSoup)

`JsoupDocumentReader` обрабатывает HTML-документы, преобразуя их в список объектов `Document` с помощью библиотеки JSoup.

#### Зависимости
Добавьте зависимость в свой проект с помощью Maven или Gradle.

[табы]
======
Мавен::
+
```xml
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-jsoup-document-reader</artifactId>
</dependency>
```

Градл::
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
            .selector("article p") // Extract paragraphs within <article> tags
            .charset("ISO-8859-1")  // Use ISO-8859-1 encoding
            .includeLinkUrls(true) // Include link URLs in metadata
            .metadataTags(List.of("author", "date")) // Extract author and date meta tags
            .additionalMetadata("source", "my-page.html") // Add custom metadata
            .build();

        JsoupDocumentReader reader = new JsoupDocumentReader(this.resource, config);
        return reader.get();
    }
}
```

`JsoupDocumentReaderConfig` позволяет вам настроить поведение `JsoupDocumentReader`:

- `charset`: указывает кодировку символов HTML-документа (по умолчанию «UTF-8»).
- `selector`: CSS-селектор JSoup, позволяющий указать, из каких элементов следует извлечь текст (по умолчанию «body»).
- `separator`: строка, используемая для объединения текста из нескольких выбранных элементов (по умолчанию «\n»).
- `allElements`: если `true`, извлекается весь текст из элемента `<body>`, игнорируя `selector` (по умолчанию `false`).
- `groupByElement`: если `true`, создается отдельный `Document` для каждого элемента, соответствующего `selector` (по умолчанию `false`).
- `includeLinkUrls`: если `true`, извлекает абсолютные URL-адреса ссылок и добавляет их в метаданные (по умолчанию `false`).
- `metadataTags`: список имен тегов `<meta>` для извлечения содержимого (по умолчанию `["description", "keywords"]`).
- `additionalMetadata`: позволяет добавлять пользовательские метаданные ко всем созданным объектам `Document`.

#### Образец документа: my-page.html

```html
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>My Web Page</title>
    <meta name="description" content="A sample web page for Spring AI">
    <meta name="keywords" content="spring, ai, html, example">
    <meta name="author" content="John Doe">
    <meta name="date" content="2024-01-15">
    <link rel="stylesheet" href="style.css">
</head>
<body>
    <header>
        <h1>Welcome to My Page</h1>
    </header>
    <nav>
        <ul>
            <li><a href="/">Home</a></li>
            <li><a href="/about">About</a></li>
        </ul>
    </nav>
    <article>
        <h2>Main Content</h2>
        <p>This is the main content of my web page.</p>
        <p>It contains multiple paragraphs.</p>
        <a href="https://www.example.com">External Link</a>
    </article>
    <footer>
        <p>&copy; 2024 John Doe</p>
    </footer>
</body>
</html>
```

Поведение:

`JsoupDocumentReader` обрабатывает содержимое HTML и создает объекты `Document` на основе конфигурации:

- `selector` определяет, какие элементы используются для извлечения текста.
- Если `allElements` равен `true`, весь текст внутри `<body>` извлекается в один `Document`.
- Если `groupByElement` равен `true`, каждый элемент, соответствующий `selector`, создает отдельный `Document`.
- Если ни `allElements`, ни `groupByElement` не являются `true`, текст всех элементов, соответствующих `selector`, объединяется с помощью `separator`.
- Заголовок документа, содержимое указанных тегов `<meta>` и (необязательно) URL-адреса ссылок добавляются к метаданным `Document`.
- Базовый URI для разрешения относительных ссылок будет извлечен из ресурсов URL.

Программа чтения сохраняет текстовое содержимое выбранных элементов, но удаляет внутри них все HTML-теги.


### Уценка

`MarkdownDocumentReader` обрабатывает документы Markdown, преобразуя их в список объектов `Document`.

#### Зависимости
Добавьте зависимость в свой проект с помощью Maven или Gradle.

[табы]
======
Мавен::
+
```xml
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-markdown-document-reader</artifactId>
</dependency>
```

Градл::
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

`MarkdownDocumentReaderConfig` позволяет вам настроить поведение MarkdownDocumentReader:

- `horizontalRuleCreateDocument`: если установлено значение `true`, горизонтальные правила в Markdown будут создавать новые объекты `Document`.
- `includeCodeBlock`: если установлено значение `true`, блоки кода будут включены в тот же `Document`, что и окружающий текст. При `false` блоки кода создают отдельные объекты `Document`.
- `includeBlockquote`: если установлено значение `true`, блоковые кавычки будут включены в тот же `Document`, что и окружающий текст. При `false` кавычки создают отдельные объекты `Document`.
- `additionalMetadata`: позволяет добавлять пользовательские метаданные ко всем созданным объектам `Document`.

#### Образец документа: code.md

```markdown
This is a Java sample application:

```java
пакет com.example.demo;

импортировать org.springframework.boot.SpringApplication;
импортировать org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
публичный класс DemoApplication {
    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, аргументы);
}
}
```

Markdown also provides the possibility to `use inline code formatting throughout` the entire sentence.

---

Another possibility is to set block code without specific highlighting:

```
./mvnw весна-javaformat: применить
```
```

Поведение: MarkdownDocumentReader обрабатывает содержимое Markdown и создает объекты Document на основе конфигурации:

- Заголовки становятся метаданными в объектах Document.
- Абзацы становятся содержимым объектов Document.
- Блоки кода можно разделить на отдельные объекты Document или включить в окружающий текст.
- Цитаты можно разделить на отдельные объекты Document или включить в окружающий текст.
- Горизонтальные правила можно использовать для разделения содержимого на отдельные объекты документа.

Средство чтения сохраняет форматирование, такое как встроенный код, списки и стили текста, в содержимом объектов Document.


### PDF-страница
`PagePdfDocumentReader` использует библиотеку Apache PdfBox для анализа PDF-документов.

#### Зависимости
Добавьте зависимость в свой проект с помощью Maven или Gradle.

[табы]
======
Мавен::
+
```xml
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-pdf-document-reader</artifactId>
</dependency>
```

Градл::
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

### PDF-абзац
`ParagraphPdfDocumentReader` использует информацию каталога PDF (e.g. TOC) для разделения входного PDF-файла на текстовые абзацы и вывода одного `Document` для каждого абзаца.
> **Примечание:** Не все документы PDF содержат каталог PDF.

#### Зависимости
Добавьте зависимость в свой проект с помощью Maven или Gradle.

[табы]
======
Мавен::
+
```xml
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-pdf-document-reader</artifactId>
</dependency>
```

Градл::
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


### Тика (DOCX, PPTX, HTML…)
`TikaDocumentReader` использует Apache Tika для извлечения текста из различных форматов документов, таких как PDF, DOC/DOCX, PPT/PPTX и HTML. Полный список поддерживаемых форматов см. в документации https://tika.apache.org/3.1.0/formats.html[Tika].

#### Зависимости
Добавьте зависимость в свой проект с помощью Maven или Gradle.

[табы]
======
Мавен::
+
```xml
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-tika-document-reader</artifactId>
</dependency>
```

Градл::
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

## Трансформеры

### TextSplitter
`TextSplitter` — абстрактный базовый класс, который помогает разделять документы в соответствии с контекстным окном модели ИИ.


### ТокенTextSplitter
`TokenTextSplitter` — это реализация `TextSplitter`, которая разбивает текст на фрагменты на основе количества токенов с использованием кодировки CL100K_BASE.

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

##### Использование шаблона Builder

Рекомендуемый способ создания `TokenTextSplitter` — использование шаблона компоновщика, который обеспечивает более читабельный и гибкий API:

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

Вы можете настроить знаки препинания, используемые для разделения текста на семантически значимые фрагменты. Это особенно полезно для интернационализации:

```java
@Component
class MyInternationalTextSplitter {

    public List<Document> splitChineseText(List<Document> documents) {
        // Use Chinese punctuation marks
        TokenTextSplitter splitter = TokenTextSplitter.builder()
            .withChunkSize(800)
            .withMinChunkSizeChars(350)
            .withPunctuationMarks(List.of('。', '？', '！', '；'))  // Chinese punctuation
            .build();

        return splitter.apply(documents);
    }

    public List<Document> splitWithCustomMarks(List<Document> documents) {
        // Mix of English and other punctuation marks
        TokenTextSplitter splitter = TokenTextSplitter.builder()
            .withChunkSize(800)
            .withPunctuationMarks(List.of('.', '?', '!', '\n', ';', ':', '。'))
            .build();

        return splitter.apply(documents);
    }
}
```

#### Параметры конструктора

`TokenTextSplitter` предоставляет три варианта конструктора:

1. `TokenTextSplitter()`: Создает разделитель с настройками по умолчанию.
2. `TokenTextSplitter(boolean keepSeparator)`: Создает разделитель с настраиваемым поведением разделителя.
3. `TokenTextSplitter(int chunkSize, int minChunkSizeChars, int minChunkLengthToEmbed, int maxNumChunks, boolean keepSeparator, List<Character> punctuationMarks)`: Полный конструктор со всеми параметрами настройки.

> **Примечание:** Шаблон построителя (показанный выше) — рекомендуемый подход для создания экземпляров с настраиваемыми конфигурациями.

#### Параметры

- `chunkSize`: целевой размер каждого фрагмента текста в токенах (по умолчанию: 800).
- `minChunkSizeChars`: минимальный размер каждого фрагмента текста в символах (по умолчанию: 350).
- `minChunkLengthToEmbed`: минимальная длина включаемого фрагмента (по умолчанию: 5).
- `maxNumChunks`: максимальное количество фрагментов, которые можно сгенерировать из текста (по умолчанию: 10000).
- `keepSeparator`: сохранять ли в фрагментах разделители (например, символы новой строки) (по умолчанию: true).
- `punctuationMarks`: список символов, которые будут использоваться в качестве границ предложения для разделения (по умолчанию: `.`, `?`, `!`, `\n`).

#### Поведение

`TokenTextSplitter` обрабатывает текстовое содержимое следующим образом:

1. Он кодирует входной текст в токены, используя кодировку CL100K_BASE.
2. Он разбивает закодированный текст на фрагменты на основе `chunkSize`.
3. Для каждого фрагмента:
   а. Он декодирует фрагмент обратно в текст.
   б. **Только если общее количество токенов превышает размер блока**, он пытается найти подходящую точку останова (с использованием настроенного `punctuationMarks`) после `minChunkSizeChars`.
   в. Если найдена точка останова, фрагмент в этой точке обрезается.
   д. Он обрезает фрагмент и при необходимости удаляет символы новой строки на основе настройки `keepSeparator`.
   е. Если полученный фрагмент длиннее `minChunkLengthToEmbed`, он добавляется в выходные данные.
4. Этот процесс продолжается до тех пор, пока не будут обработаны все токены или не будет достигнуто `maxNumChunks`.
5. Весь оставшийся текст добавляется как последний фрагмент, если его длина превышает `minChunkLengthToEmbed`.

> **Важно:** Разделение на основе знаков препинания применяется только тогда, когда количество токенов превышает размер фрагмента. Текст, который точно соответствует размеру фрагмента или меньше его, возвращается как один фрагмент без усечения на основе знаков препинания. Это предотвращает ненужное разделение небольших текстов.

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


#### Примечания

- `TokenTextSplitter` использует кодировку CL100K_BASE из библиотеки `jtokkit`, которая совместима с более новыми моделями OpenAI.
- Разделитель пытается создать семантически значимые фрагменты, разрывая границы предложений, где это возможно.
- Метаданные из исходных документов сохраняются и копируются во все фрагменты, полученные из этого документа.
- Средство форматирования контента (если установлено) из исходного документа также копируется в производные фрагменты, если для `copyContentFormatter` установлено значение `true` (поведение по умолчанию).
- Этот разделитель особенно полезен для подготовки текста для больших языковых моделей с ограничениями по токенам, гарантируя, что каждый фрагмент находится в пределах вычислительной мощности модели.
- **Пользовательские знаки препинания**. Знаки препинания по умолчанию (`.`, `?`, `!`, `\n`) хорошо подходят для текста на английском языке. Для других языков или специализированного контента настройте знаки препинания, используя метод `withPunctuationMarks()` конструктора.
- **Аспекты производительности**. Хотя разделитель может обрабатывать любое количество знаков препинания, для оптимальной производительности рекомендуется сохранять список достаточно небольшим (менее 20 символов), поскольку каждый знак проверяется для каждого фрагмента.
- **Расширяемость**. Метод `getLastPunctuationIndex(String)` — это `protected`, что позволяет подклассам переопределять логику определения пунктуации для специализированных случаев использования.
- **Обработка мелкого текста**. Начиная с версии 2.0, небольшие тексты (с количеством токенов, равным или меньшим размером фрагмента) больше не разбиваются знаками препинания, что предотвращает ненужную фрагментацию контента, который уже укладывается в пределы размера.

### Контентформаттрансформер
Обеспечивает единые форматы контента во всех документах.

### Обогащение метаданных ключевого слова
`KeywordMetadataEnricher` — это `DocumentTransformer`, который использует генеративную модель искусственного интеллекта для извлечения ключевых слов из содержимого документа и добавления их в качестве метаданных.

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

        // Or use custom templates
        KeywordMetadataEnricher enricher = KeywordMetadataEnricher.builder(chatModel)
               .keywordsTemplate(YOUR_CUSTOM_TEMPLATE)
               .build();

        return enricher.apply(documents);
    }
}
```

#### Параметры конструктора

`KeywordMetadataEnricher` предоставляет два варианта конструктора:

1. `KeywordMetadataEnricher(ChatModel chatModel, int keywordCount)`: использовать шаблон по умолчанию и извлечь указанное количество ключевых слов.
2. `KeywordMetadataEnricher(ChatModel chatModel, PromptTemplate keywordsTemplate)`: использовать собственный шаблон для извлечения ключевых слов.

#### Поведение

`KeywordMetadataEnricher` обрабатывает документы следующим образом:

1. Для каждого входного документа создается подсказка, использующая содержимое документа.
2. Он отправляет это приглашение на указанный `ChatModel` для генерации ключевых слов.
3. Сгенерированные ключевые слова добавляются в метаданные документа под ключом «excerpt_keywords».
4. Обогащенные документы возвращаются.


#### Кастомизация

Вы можете использовать шаблон по умолчанию или настроить его с помощью параметраkeywordsTemplate.
Шаблон по умолчанию:

```java
\{context_str}. Give %s unique keywords for this document. Format as comma separated. Keywords:
```

Где `+{context_str}+` заменяется содержимым документа, а `%s` заменяется указанным количеством ключевых слов.

#### Пример

```java
ChatModel chatModel = // initialize your chat model
KeywordMetadataEnricher enricher = KeywordMetadataEnricher.builder(chatModel)
                .keywordCount(5)
                .build();

// Or use custom templates
KeywordMetadataEnricher enricher = KeywordMetadataEnricher.builder(chatModel)
                .keywordsTemplate(new PromptTemplate("Extract 5 important keywords from the following text and separate them with commas:\n{context_str}"))
                .build();

Document doc = new Document("This is a document about artificial intelligence and its applications in modern technology.");

List<Document> enrichedDocs = enricher.apply(List.of(this.doc));

Document enrichedDoc = this.enrichedDocs.get(0);
String keywords = (String) this.enrichedDoc.getMetadata().get("excerpt_keywords");
System.out.println("Extracted keywords: " + keywords);
```

#### Примечания

- `KeywordMetadataEnricher` требует работающего `ChatModel` для генерации ключевых слов.
- Количество ключевых слов должно быть 1 или больше.
- Обогатитель добавляет поле метаданных «excerpt_keywords» в каждый обработанный документ.
- Сгенерированные ключевые слова возвращаются в виде строки, разделенной запятыми.
- Это средство обогащения особенно полезно для улучшения возможностей поиска документов и для создания тегов или категорий для документов.
- Если в шаблоне Builder установлен параметр `keywordsTemplate`, параметр `keywordCount` будет игнорироваться.

### СводкаМетаданныхEnricher
`SummaryMetadataEnricher` — это `DocumentTransformer`, который использует генеративную модель искусственного интеллекта для создания сводок документов и добавления их в качестве метаданных. Он может генерировать сводки для текущего документа, а также соседних документов (предыдущего и следующего).

#### Использование

```java
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

- `chatModel`: модель искусственного интеллекта, используемая для создания сводок.
- `summaryTypes`: список значений перечисления `SummaryType`, указывающий, какие сводки создавать (PREVIOUS, CURRENT, NEXT).
- `summaryTemplate`: собственный шаблон для создания сводки (необязательно).
- `metadataMode`: указывает, как обрабатывать метаданные документа при создании сводок (необязательно).


#### Поведение

`SummaryMetadataEnricher` обрабатывает документы следующим образом:

1. Для каждого входного документа создается приглашение, использующее содержимое документа и указанный шаблон сводки.
2. Он отправляет это приглашение на указанный `ChatModel` для создания сводки.
3. В зависимости от указанного `summaryTypes` в каждый документ добавляется следующие метаданные:
- `section_summary`: Краткое описание текущего документа.
- `prev_section_summary`: Краткое изложение предыдущего документа (при наличии и запросе).
- `next_section_summary`: Краткое описание следующего документа (если он доступен и запрошен).
4. Обогащенные документы возвращаются.

#### Кастомизация

Запрос на создание сводки можно настроить, указав собственный `summaryTemplate`. Шаблон по умолчанию:

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
ChatModel chatModel = // initialize your chat model
SummaryMetadataEnricher enricher = new SummaryMetadataEnricher(chatModel,
    List.of(SummaryType.PREVIOUS, SummaryType.CURRENT, SummaryType.NEXT));

Document doc1 = new Document("Content of document 1");
Document doc2 = new Document("Content of document 2");

List<Document> enrichedDocs = enricher.apply(List.of(this.doc1, this.doc2));

// Check the metadata of the enriched documents
for (Document doc : enrichedDocs) {
    System.out.println("Current summary: " + doc.getMetadata().get("section_summary"));
    System.out.println("Previous summary: " + doc.getMetadata().get("prev_section_summary"));
    System.out.println("Next summary: " + doc.getMetadata().get("next_section_summary"));
}
```

Приведенный пример демонстрирует ожидаемое поведение:

- Для списка из двух документов оба документа получают `section_summary`.
- Первый документ получает `next_section_summary`, но не `prev_section_summary`.
- Второй документ получает `prev_section_summary`, но не `next_section_summary`.
- `section_summary` первого документа соответствует `prev_section_summary` второго документа.
- `next_section_summary` первого документа соответствует `section_summary` второго документа.

#### Примечания

- `SummaryMetadataEnricher` требует работающего `ChatModel` для создания сводок.
- Обогатитель может обрабатывать списки документов любого размера, правильно обрабатывая крайние случаи для первого и последнего документов.
- Это средство обогащения особенно полезно для создания контекстно-зависимых сводок, что позволяет лучше понять взаимосвязи документов в последовательности.
- Параметр `MetadataMode` позволяет контролировать, как существующие метаданные включаются в процесс создания сводки.


## Писатели

### Файл

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

- `fileName`: имя файла, в который будут записываться документы.
- `withDocumentMarkers`: включать ли маркеры документа в выходные данные (по умолчанию: false).
- `metadataMode`: указывает, какое содержимое документа будет записано в файл (по умолчанию: MetadataMode.NONE).
- `append`: если true, данные будут записываться в конец файла, а не в начало (по умолчанию: false).

#### Поведение

`FileDocumentWriter` обрабатывает документы следующим образом:

1. Он открывает FileWriter для указанного имени файла.
2. Для каждого документа в списке ввода:
а. Если `withDocumentMarkers` имеет значение true, он записывает маркер документа, включая индекс документа и номера страниц.
б. Он записывает форматированное содержимое документа на основе указанного `metadataMode`.
3. Файл закрывается после написания всех документов.



#### Маркеры документов

Если для `withDocumentMarkers` установлено значение true, средство записи включает маркеры для каждого документа в следующем формате:

[источник]
```
### Doc: [index], pages:[start_page_number,end_page_number]
```

#### Обработка метаданных

Автор использует два конкретных ключа метаданных:

- `page_number`: представляет номер начальной страницы документа.
- `end_page_number`: представляет номер конечной страницы документа.

Они используются при написании маркеров документа.

#### Пример

```java
List<Document> documents = // initialize your documents
FileDocumentWriter writer = new FileDocumentWriter("output.txt", true, MetadataMode.ALL, true);
writer.accept(documents);
```

При этом все документы будут записаны в «output.txt», включая маркеры документов, с использованием всех доступных метаданных и добавлением в файл, если он уже существует.

#### Примечания

- Средство записи использует `FileWriter`, поэтому оно записывает текстовые файлы с кодировкой символов операционной системы по умолчанию.
- Если во время записи возникает ошибка, выдается `RuntimeException`, причиной которого является исходное исключение.
- Параметр `metadataMode` позволяет контролировать, как существующие метаданные включаются в письменный контент.
- Этот модуль записи особенно полезен для отладки или создания удобочитаемых результатов коллекций документов.


### ВекторМагазин

Обеспечивает интеграцию с различными векторными хранилищами.
Полный список см. в [Документация по векторной базе данных](api/vectordbs.md).
