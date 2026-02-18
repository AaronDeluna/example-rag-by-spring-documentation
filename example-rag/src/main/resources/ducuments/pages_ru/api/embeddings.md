```markdown
[[EmbeddingModel]]
# API модели встраивания

Встраивания — это числовые представления текста, изображений или видео, которые захватывают взаимосвязи между входными данными.

Встраивания работают, преобразуя текст, изображения и видео в массивы чисел с плавающей запятой, называемые векторами. Эти векторы предназначены для захвата смысла текста, изображений и видео. Длина массива встраивания называется размерностью вектора.

Путем вычисления числового расстояния между векторными представлениями двух фрагментов текста приложение может определить схожесть между объектами, использованными для генерации векторных встраиваний.

Интерфейс `EmbeddingModel` разработан для простого интегрирования с моделями встраивания в области ИИ и машинного обучения. Его основная функция заключается в преобразовании текста в числовые векторы, обычно называемые встраиваниями. Эти встраивания имеют решающее значение для различных задач, таких как семантический анализ и классификация текста.

Дизайн интерфейса EmbeddingModel сосредоточен вокруг двух основных целей:

- **Портативность**: Этот интерфейс обеспечивает легкую адаптацию к различным моделям встраивания. Он позволяет разработчикам переключаться между различными техниками или моделями встраивания с минимальными изменениями в коде. Этот дизайн соответствует философии модульности и взаимозаменяемости Spring.

- **Простота**: EmbeddingModel упрощает процесс преобразования текста в встраивания. Предоставляя простые методы, такие как `embed(String text)` и `embed(Document document)`, он устраняет сложность работы с сырыми текстовыми данными и алгоритмами встраивания. Этот выбор дизайна облегчает разработчикам, особенно тем, кто нов в области ИИ, использование встраиваний в своих приложениях без углубления в основные механизмы.

## Обзор API

API модели встраивания построен на основе общего https://github.com/spring-projects/spring-ai/tree/main/spring-ai-model/src/main/java/org/springframework/ai/model[Spring AI Model API], который является частью библиотеки Spring AI. Таким образом, интерфейс EmbeddingModel расширяет интерфейс `Model`, который предоставляет стандартный набор методов для взаимодействия с моделями ИИ. Классы `EmbeddingRequest` и `EmbeddingResponse`, расширяющие `ModelRequest` и `ModelResponse`, используются для инкапсуляции входных и выходных данных моделей встраивания соответственно.

API встраивания, в свою очередь, используется более высокоуровневыми компонентами для реализации моделей встраивания для конкретных моделей встраивания, таких как OpenAI, Titan, Azure OpenAI, Ollie и других.

Следующая диаграмма иллюстрирует API встраивания и его связь с API модели Spring AI и моделями встраивания:

image:embeddings-api.jpg[title=Embeddings API,align=center,width=900]

### EmbeddingModel

Этот раздел предоставляет руководство по интерфейсу `EmbeddingModel` и связанным классам.

```java
public interface EmbeddingModel extends Model<EmbeddingRequest, EmbeddingResponse> {

	@Override
	EmbeddingResponse call(EmbeddingRequest request);


	/**
	 * Встраивает содержимое данного документа в вектор.
	 * @param document документ для встраивания.
	 * @return встроенный вектор.
	 */
	float[] embed(Document document);

	/**
	 * Встраивает данный текст в вектор.
	 * @param text текст для встраивания.
	 * @return встроенный вектор.
	 */
	default float[] embed(String text) {
		Assert.notNull(text, "Текст не должен быть null");
		return this.embed(List.of(text)).iterator().next();
	}

	/**
	 * Встраивает пакет текстов в векторы.
	 * @param texts список текстов для встраивания.
	 * @return список встроенных векторов.
	 */
	default List<float[]> embed(List<String> texts) {
		Assert.notNull(texts, "Тексты не должны быть null");
		return this.call(new EmbeddingRequest(texts, EmbeddingOptions.EMPTY))
			.getResults()
			.stream()
			.map(Embedding::getOutput)
			.toList();
	}

	/**
	 * Встраивает пакет текстов в векторы и возвращает {@link EmbeddingResponse}.
	 * @param texts список текстов для встраивания.
	 * @return ответ на запрос встраивания.
	 */
	default EmbeddingResponse embedForResponse(List<String> texts) {
		Assert.notNull(texts, "Тексты не должны быть null");
		return this.call(new EmbeddingRequest(texts, EmbeddingOptions.EMPTY));
	}

	/**
	 * @return количество размерностей встроенных векторов. Это специфично для генеративных моделей.
	 */
	default int dimensions() {
		return embed("Тестовая строка").size();
	}

}
```

Методы embed предлагают различные варианты преобразования текста в встраивания, учитывая одиночные строки, структурированные объекты `Document` или пакеты текста.

Предоставлены несколько методов-ярлыков для встраивания текста, включая метод `embed(String text)`, который принимает одну строку и возвращает соответствующий вектор встраивания. Все ярлыки реализованы вокруг метода `call`, который является основным методом для вызова модели встраивания.

Как правило, встраивание возвращает списки чисел с плавающей запятой, представляющих встраивания в числовом векторном формате.

Метод `embedForResponse` предоставляет более полные выходные данные, потенциально включая дополнительную информацию о встраиваниях.

Метод dimensions является удобным инструментом для разработчиков, позволяющим быстро определить размер векторов встраивания, что важно для понимания пространства встраивания и для последующих этапов обработки.

#### EmbeddingRequest

`EmbeddingRequest` — это `ModelRequest`, который принимает список текстовых объектов и необязательные параметры запроса на встраивание. Следующий список показывает сокращенную версию класса EmbeddingRequest, исключая конструкторы и другие утилитарные методы:

```java
public class EmbeddingRequest implements ModelRequest<List<String>> {
	private final List<String> inputs;
	private final EmbeddingOptions options;
	// другие методы опущены
}
```

#### EmbeddingResponse

Структура класса `EmbeddingResponse` выглядит следующим образом:

```java
public class EmbeddingResponse implements ModelResponse<Embedding> {

	private List<Embedding> embeddings;
	private EmbeddingResponseMetadata metadata = new EmbeddingResponseMetadata();
	// другие методы опущены
}
```

Класс `EmbeddingResponse` содержит выходные данные модели ИИ, при этом каждый экземпляр `Embedding` содержит данные результирующего вектора от одного текстового ввода.

Класс `EmbeddingResponse` также содержит `EmbeddingResponseMetadata`, метаданные о ответе модели ИИ.

#### Embedding

`Embedding` представляет собой один вектор встраивания.

```java
public class Embedding implements ModelResult<float[]> {
	private float[] embedding;
	private Integer index;
	private EmbeddingResultMetadata metadata;
	// другие методы опущены
}
```

## Доступные реализации [[available-implementations]]

Внутренние различные реализации `EmbeddingModel` используют разные низкоуровневые библиотеки и API для выполнения задач встраивания. Ниже приведены некоторые из доступных реализаций `EmbeddingModel`:

- xref:api/embeddings/openai-embeddings.adoc[Spring AI OpenAI Embeddings]
- xref:api/embeddings/azure-openai-embeddings.adoc[Spring AI Azure OpenAI Embeddings]
- xref:api/embeddings/ollama-embeddings.adoc[Spring AI Ollama Embeddings]
- xref:api/embeddings/onnx.adoc[Spring AI Transformers (ONNX) Embeddings]
- xref:api/embeddings/postgresml-embeddings.adoc[Spring AI PostgresML Embeddings]
- xref:api/embeddings/bedrock-cohere-embedding.adoc[Spring AI Bedrock Cohere Embeddings]
- xref:api/embeddings/bedrock-titan-embedding.adoc[Spring AI Bedrock Titan Embeddings]
- xref:api/embeddings/vertexai-embeddings-text.adoc[Spring AI VertexAI Embeddings]
- xref:api/embeddings/mistralai-embeddings.adoc[Spring AI Mistral AI Embeddings]
- xref:api/embeddings/oci-genai-embeddings.adoc[Spring AI Oracle Cloud Infrastructure GenAI Embeddings]
```
