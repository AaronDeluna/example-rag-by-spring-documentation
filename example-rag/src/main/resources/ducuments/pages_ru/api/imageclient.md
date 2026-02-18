```markdown
[[ImageModel]]
# API модели изображений

`Spring Image Model API` разработан как простой и переносимый интерфейс для взаимодействия с различными xref:concepts.adoc#_models[AI Models], специализированными на генерации изображений, позволяя разработчикам переключаться между различными моделями, связанными с изображениями, с минимальными изменениями в коде. Этот дизайн соответствует философии модульности и взаимозаменяемости Spring, обеспечивая быструю адаптацию приложений к различным возможностям ИИ, связанным с обработкой изображений.

Кроме того, с поддержкой вспомогательных классов, таких как `ImagePrompt` для инкапсуляции входных данных и `ImageResponse` для обработки выходных данных, API модели изображений унифицирует взаимодействие с ИИ моделями, посвященными генерации изображений. Он управляет сложностью подготовки запросов и разбора ответов, предлагая прямое и упрощенное взаимодействие с API для функциональности генерации изображений.

API модели изображений Spring построен на основе `Generic Model API` Spring AI, предоставляя абстракции и реализации, специфичные для изображений.

## Обзор API

В этом разделе представлен гид по интерфейсу API модели изображений Spring и связанным классам.

## Модель изображения

Вот определение интерфейса [ImageModel](https://github.com/spring-projects/spring-ai/blob/main/spring-ai-model/src/main/java/org/springframework/ai/image/ImageModel.java):

```java
@FunctionalInterface
public interface ImageModel extends Model<ImagePrompt, ImageResponse> {

	ImageResponse call(ImagePrompt request);

}
```

### ImagePrompt

https://github.com/spring-projects/spring-ai/blob/main/spring-ai-model/src/main/java/org/springframework/ai/image/ImagePrompt.java[ImagePrompt] — это `ModelRequest`, который инкапсулирует список объектов https://github.com/spring-projects/spring-ai/blob/main/spring-ai-model/src/main/java/org/springframework/ai/image/ImageMessage.java[ImageMessage] и необязательные параметры запроса модели. Следующий список показывает сокращенную версию класса `ImagePrompt`, исключая конструкторы и другие утилитарные методы:

```java
public class ImagePrompt implements ModelRequest<List<ImageMessage>> {

    private final List<ImageMessage> messages;

	private ImageOptions imageModelOptions;

    @Override
	public List<ImageMessage> getInstructions() {...}

	@Override
	public ImageOptions getOptions() {...}

    // конструкторы и утилитарные методы опущены
}
```

#### ImageMessage

Класс `ImageMessage` инкапсулирует текст, который будет использоваться, и вес, который этот текст должен иметь для влияния на сгенерированное изображение. Для моделей, которые поддерживают веса, они могут быть положительными или отрицательными.

```java
public class ImageMessage {

	private String text;

	private Float weight;

    public String getText() {...}

	public Float getWeight() {...}

   // конструкторы и утилитарные методы опущены
}
```

#### ImageOptions

Представляет параметры, которые могут быть переданы модели генерации изображений. Интерфейс `ImageOptions` расширяет интерфейс `ModelOptions` и используется для определения нескольких переносимых параметров, которые могут быть переданы модели ИИ.

Интерфейс `ImageOptions` определен следующим образом:

```java
public interface ImageOptions extends ModelOptions {

	Integer getN();

	String getModel();

	Integer getWidth();

	Integer getHeight();

	String getResponseFormat(); // openai - url или base64 : stability ai byte[] или base64

}
```

Кроме того, каждая конкретная реализация ImageModel может иметь свои собственные параметры, которые могут быть переданы модели ИИ. Например, модель генерации изображений OpenAI имеет свои собственные параметры, такие как `quality`, `style` и т.д.

Это мощная функция, которая позволяет разработчикам использовать специфические для модели параметры при запуске приложения, а затем переопределять их во время выполнения с помощью `ImagePrompt`.

### ImageResponse
```Структура класса `ImageResponse` выглядит следующим образом:

```java
public class ImageResponse implements ModelResponse<ImageGeneration> {

	private final ImageResponseMetadata imageResponseMetadata;

	private final List<ImageGeneration> imageGenerations;

	@Override
	public ImageGeneration getResult() {
		// получить первый результат
	}

	@Override
	public List<ImageGeneration> getResults() {...}

	@Override
	public ImageResponseMetadata getMetadata() {...}

    // другие методы опущены

}
```

Класс https://github.com/spring-projects/spring-ai/blob/main/spring-ai-model/src/main/java/org/springframework/ai/image/ImageResponse.java[ImageResponse] содержит вывод AI модели, где каждый экземпляр `ImageGeneration` включает один из потенциально нескольких выводов, полученных в результате одного запроса.

Класс `ImageResponse` также содержит объект `ImageResponseMetadata`, который хранит метаданные о ответе AI модели.

### ImageGeneration

Наконец, класс https://github.com/spring-projects/spring-ai/blob/main/spring-ai-model/src/main/java/org/springframework/ai/image/ImageGeneration.java[ImageGeneration] наследуется от `ModelResult`, чтобы представлять выходной ответ и связанные метаданные об этом результате:

```java
public class ImageGeneration implements ModelResult<Image> {

	private ImageGenerationMetadata imageGenerationMetadata;

	private Image image;

    @Override
	public Image getOutput() {...}

	@Override
	public ImageGenerationMetadata getMetadata() {...}

    // другие методы опущены

}
```

## Доступные реализации

Реализации `ImageModel` предоставляются для следующих поставщиков моделей:

- xref:api/image/openai-image.adoc[OpenAI Image Generation]
- xref:api/image/azure-openai-image.adoc[Azure OpenAI Image Generation]
- xref:api/image/qianfan-image.adoc[QianFan Image Generation]
- xref:api/image/stabilityai-image.adoc[StabilityAI Image Generation]
- xref:api/image/zhipuai-image.adoc[ZhiPuAI Image Generation]

## Документация API

Вы можете найти Javadoc https://docs.spring.io/spring-ai/docs/current-SNAPSHOT/[здесь].

## Обратная связь и вклад

Обсуждения проекта на https://github.com/spring-projects/spring-ai/discussions[GitHub] — отличное место для отправки отзывов.
