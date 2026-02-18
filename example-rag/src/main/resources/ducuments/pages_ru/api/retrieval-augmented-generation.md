```markdown
# Генерация с использованием извлечения

Генерация с использованием извлечения (RAG) — это техника, полезная для преодоления ограничений больших языковых моделей, которые испытывают трудности с длинными текстами, фактической точностью и осведомленностью о контексте.

Spring AI поддерживает RAG, предоставляя модульную архитектуру, которая позволяет вам самостоятельно создавать пользовательские потоки RAG или использовать готовые потоки RAG с помощью API `Advisor`.

> **Примечание:** Узнайте больше о Генерации с использованием извлечения в разделе xref:concepts.adoc#concept-rag[концепции].

## Советники

Spring AI предоставляет готовую поддержку для общих потоков RAG с помощью API `Advisor`.

Чтобы использовать `QuestionAnswerAdvisor` или `VectorStoreChatMemoryAdvisor`, вам необходимо добавить зависимость `spring-ai-advisors-vector-store` в ваш проект:

```xml
<dependency>
   <groupId>org.springframework.ai</groupId>
   <artifactId>spring-ai-advisors-vector-store</artifactId>
</dependency>
```

### QuestionAnswerAdvisor

Векторная база данных хранит данные, о которых модель ИИ не осведомлена. Когда вопрос пользователя отправляется в модель ИИ, `QuestionAnswerAdvisor` запрашивает векторную базу данных на наличие документов, связанных с вопросом пользователя.

Ответ из векторной базы данных добавляется к тексту пользователя, чтобы предоставить контекст для модели ИИ для генерации ответа.

Предполагая, что вы уже загрузили данные в `VectorStore`, вы можете выполнить Генерацию с использованием извлечения (RAG), предоставив экземпляр `QuestionAnswerAdvisor` в `ChatClient`.

```java
ChatResponse response = ChatClient.builder(chatModel)
        .build().prompt()
        .advisors(QuestionAnswerAdvisor.builder(vectorStore).build())
        .user(userText)
        .call()
        .chatResponse();
```

В этом примере `QuestionAnswerAdvisor` выполнит поиск по всем документам в векторной базе данных. Чтобы ограничить типы документов, которые будут искаться, `SearchRequest` принимает выражение фильтра, похожее на SQL, которое переносимо для всех `VectorStores`.

Это выражение фильтра может быть настроено при создании `QuestionAnswerAdvisor` и, следовательно, будет всегда применяться ко всем запросам `ChatClient`, или его можно предоставить во время выполнения для каждого запроса.

Вот как создать экземпляр `QuestionAnswerAdvisor`, где порог составляет `0.8`, и вернуть топ `6` результатов.

```java
var qaAdvisor = QuestionAnswerAdvisor.builder(vectorStore)
        .searchRequest(SearchRequest.builder().similarityThreshold(0.8d).topK(6).build())
        .build();
```

#### Динамические выражения фильтра

Обновите выражение фильтра `SearchRequest` во время выполнения, используя параметр контекста советника `FILTER_EXPRESSION`:

```java
ChatClient chatClient = ChatClient.builder(chatModel)
    .defaultAdvisors(QuestionAnswerAdvisor.builder(vectorStore)
        .searchRequest(SearchRequest.builder().build())
        .build())
    .build();

// Обновите выражение фильтра во время выполнения
String content = this.chatClient.prompt()
    .user("Пожалуйста, ответьте на мой вопрос XYZ")
    .advisors(a -> a.param(QuestionAnswerAdvisor.FILTER_EXPRESSION, "type == 'Spring'"))
    .call()
    .content();
```

Параметр `FILTER_EXPRESSION` позволяет вам динамически фильтровать результаты поиска на основе предоставленного выражения.

#### Пользовательский шаблон
````QuestionAnswerAdvisor` использует шаблон по умолчанию для дополнения вопроса пользователя полученными документами. Вы можете настроить это поведение, предоставив свой собственный объект `PromptTemplate` через метод сборки `.promptTemplate()`.

> **Примечание:** Предоставленный здесь `PromptTemplate` настраивает, как советник объединяет полученный контекст с запросом пользователя. Это отличается от настройки `TemplateRenderer` на самом `ChatClient` (с использованием `.templateRenderer()`), что влияет на отображение начального содержимого пользовательского/системного запроса **до** выполнения советника. См. xref:api/chatclient.adoc#_prompt_templates[Шаблоны запросов ChatClient] для получения дополнительной информации о рендеринге шаблонов на уровне клиента.

Пользовательский `PromptTemplate` может использовать любую реализацию `TemplateRenderer` (по умолчанию используется `StPromptTemplate`, основанный на https://www.stringtemplate.org/[StringTemplate] engine). Важным требованием является то, что шаблон должен содержать следующие два заполнителя:

- заполнителя `query` для получения вопроса пользователя.
- заполнителя `question_answer_context` для получения полученного контекста.

```java
PromptTemplate customPromptTemplate = PromptTemplate.builder()
    .renderer(StTemplateRenderer.builder().startDelimiterToken('<').endDelimiterToken('>').build())
    .template("""
            <query>

            Информация о контексте ниже.

			---------------------
			<question_answer_context>
			---------------------

			Учитывая информацию о контексте и отсутствие предварительных знаний, ответьте на запрос.

			Следуйте этим правилам:

			1. Если ответа нет в контексте, просто скажите, что не знаете.
			2. Избегайте утверждений, таких как "Основываясь на контексте..." или "Предоставленная информация...".
            """)
    .build();

    String question = "Где происходит приключение Анаклетуса и Бирбы?";

    QuestionAnswerAdvisor qaAdvisor = QuestionAnswerAdvisor.builder(vectorStore)
        .promptTemplate(customPromptTemplate)
        .build();

    String response = ChatClient.builder(chatModel).build()
        .prompt(question)
        .advisors(qaAdvisor)
        .call()
        .content();
```

> **Примечание:** Метод `QuestionAnswerAdvisor.Builder.userTextAdvise()` устарел в пользу использования `.promptTemplate()` для более гибкой настройки.

### RetrievalAugmentationAdvisor

Spring AI включает в себя xref:api/retrieval-augmented-generation.adoc#modules[библиотеку модулей RAG], которую вы можете использовать для создания собственных потоков RAG. `RetrievalAugmentationAdvisor` — это `Advisor`, предоставляющий готовую реализацию для самых распространенных потоков RAG, основанную на модульной архитектуре.

Чтобы использовать `RetrievalAugmentationAdvisor`, вам нужно добавить зависимость `spring-ai-rag` в ваш проект:

```xml
<dependency>
   <groupId>org.springframework.ai</groupId>
   <artifactId>spring-ai-rag</artifactId>
</dependency>
```

#### Последовательные потоки RAG

##### Наивный RAG```java
Advisor retrievalAugmentationAdvisor = RetrievalAugmentationAdvisor.builder()
        .documentRetriever(VectorStoreDocumentRetriever.builder()
                .similarityThreshold(0.50)
                .vectorStore(vectorStore)
                .build())
        .build();

String answer = chatClient.prompt()
        .advisors(retrievalAugmentationAdvisor)
        .user(question)
        .call()
        .content();
```

По умолчанию `RetrievalAugmentationAdvisor` не позволяет контексту, полученному в результате поиска, быть пустым. Когда это происходит, он инструктирует модель не отвечать на запрос пользователя. Вы можете разрешить пустой контекст следующим образом.

```java
Advisor retrievalAugmentationAdvisor = RetrievalAugmentationAdvisor.builder()
        .documentRetriever(VectorStoreDocumentRetriever.builder()
                .similarityThreshold(0.50)
                .vectorStore(vectorStore)
                .build())
        .queryAugmenter(ContextualQueryAugmenter.builder()
                .allowEmptyContext(true)
                .build())
        .build();

String answer = chatClient.prompt()
        .advisors(retrievalAugmentationAdvisor)
        .user(question)
        .call()
        .content();
```

`VectorStoreDocumentRetriever` принимает `FilterExpression` для фильтрации результатов поиска на основе метаданных. Вы можете предоставить его при создании `VectorStoreDocumentRetriever` или во время выполнения для каждого запроса, используя параметр контекста советника `FILTER_EXPRESSION`.

```java
Advisor retrievalAugmentationAdvisor = RetrievalAugmentationAdvisor.builder()
        .documentRetriever(VectorStoreDocumentRetriever.builder()
                .similarityThreshold(0.50)
                .vectorStore(vectorStore)
                .build())
        .build();

String answer = chatClient.prompt()
        .advisors(retrievalAugmentationAdvisor)
        .advisors(a -> a.param(VectorStoreDocumentRetriever.FILTER_EXPRESSION, "type == 'Spring'"))
        .user(question)
        .call()
        .content();
```

Смотрите xref:api/retrieval-augmented-generation.adoc#_vectorstoredocumentretriever[VectorStoreDocumentRetriever] для получения дополнительной информации.

##### Расширенный RAG

```java
Advisor retrievalAugmentationAdvisor = RetrievalAugmentationAdvisor.builder()
        .queryTransformers(RewriteQueryTransformer.builder()
                .chatClientBuilder(chatClientBuilder.build().mutate())
                .build())
        .documentRetriever(VectorStoreDocumentRetriever.builder()
                .similarityThreshold(0.50)
                .vectorStore(vectorStore)
                .build())
        .build();

String answer = chatClient.prompt()
        .advisors(retrievalAugmentationAdvisor)
        .user(question)
        .call()
        .content();
```

Вы также можете использовать API `DocumentPostProcessor` для постобработки полученных документов перед передачей их модели. Например, вы можете использовать такой интерфейс для повторной оценки полученных документов на основе их релевантности к запросу, удаления нерелевантных или избыточных документов или сжатия содержимого каждого документа для уменьшения шума и избыточности.

## Модули

Spring AI реализует модульную архитектуру RAG, вдохновленную концепцией модульности, подробно описанной в статье
"https://arxiv.org/abs/2407.21059[Modular RAG: Transforming RAG Systems into LEGO-like Reconfigurable Frameworks]".

### Предварительный поиск

Модули предварительного поиска отвечают за обработку пользовательского запроса для достижения наилучших результатов поиска.

#### Преобразование запроса
```A component for transforming the input query to make it more effective for retrieval tasks, addressing challenges such as poorly formed queries, ambiguous terms, complex vocabulary, or unsupported languages.

> **Важно:** При использовании `QueryTransformer` рекомендуется настроить `ChatClient.Builder` с низкой температурой (например, 0.0), чтобы обеспечить более детерминированные и точные результаты, улучшая качество извлечения. Стандартная температура для большинства чат-моделей обычно слишком высока для оптимальной трансформации запросов, что приводит к снижению эффективности извлечения.

##### CompressionQueryTransformer

`CompressionQueryTransformer` использует большую языковую модель для сжатия истории разговора и последующего запроса в самостоятельный запрос, который захватывает суть разговора.

Этот трансформатор полезен, когда история разговора длинная, а последующий запрос связан с контекстом разговора.

```java
Query query = Query.builder()
        .text("А какой у него второй по величине город?")
        .history(new UserMessage("Какова столица Дании?"),
                new AssistantMessage("Копенгаген — столица Дании."))
        .build();

QueryTransformer queryTransformer = CompressionQueryTransformer.builder()
        .chatClientBuilder(chatClientBuilder)
        .build();

Query transformedQuery = queryTransformer.transform(query);
```

Шаблон, используемый этим компонентом, можно настроить с помощью метода `promptTemplate()`, доступного в билдере.

##### RewriteQueryTransformer

`RewriteQueryTransformer` использует большую языковую модель для переписывания пользовательского запроса, чтобы обеспечить лучшие результаты при запросе к целевой системе, такой как векторное хранилище или веб-поисковая система.

Этот трансформатор полезен, когда пользовательский запрос избыточен, неоднозначен или содержит нерелевантную информацию, которая может повлиять на качество результатов поиска.

```java
Query query = new Query("Я изучаю машинное обучение. Что такое LLM?");

QueryTransformer queryTransformer = RewriteQueryTransformer.builder()
        .chatClientBuilder(chatClientBuilder)
        .build();

Query transformedQuery = queryTransformer.transform(query);
```

Шаблон, используемый этим компонентом, можно настроить с помощью метода `promptTemplate()`, доступного в билдере.

##### TranslationQueryTransformer

`TranslationQueryTransformer` использует большую языковую модель для перевода запроса на целевой язык, который поддерживается моделью встраивания, используемой для генерации встраиваний документов. Если запрос уже на целевом языке, он возвращается без изменений. Если язык запроса неизвестен, он также возвращается без изменений.

Этот трансформатор полезен, когда модель встраивания обучена на конкретном языке, а пользовательский запрос на другом языке.

```java
Query query = new Query("Hvad er Danmarks hovedstad?");

QueryTransformer queryTransformer = TranslationQueryTransformer.builder()
        .chatClientBuilder(chatClientBuilder)
        .targetLanguage("english")
        .build();

Query transformedQuery = queryTransformer.transform(query);
```

Шаблон, используемый этим компонентом, можно настроить с помощью метода `promptTemplate()`, доступного в билдере.

#### Query Expansion

Компонент для расширения входного запроса в список запросов, решая проблемы, такие как плохо сформулированные запросы, предоставляя альтернативные формулировки запросов или разбивая сложные задачи на более простые подзапросы.

##### MultiQueryExpander```markdown
`MultiQueryExpander` использует большую языковую модель для расширения запроса в несколько семантически разнообразных вариантов, чтобы захватить разные перспективы, что полезно для получения дополнительной контекстной информации и увеличения шансов на нахождение релевантных результатов.

```java
MultiQueryExpander queryExpander = MultiQueryExpander.builder()
    .chatClientBuilder(chatClientBuilder)
    .numberOfQueries(3)
    .build();
List<Query> queries = queryExpander.expand(new Query("Как запустить приложение Spring Boot?"));
```

По умолчанию `MultiQueryExpander` включает оригинальный запрос в список расширенных запросов. Вы можете отключить это поведение с помощью метода `includeOriginal` в билдере.

```java
MultiQueryExpander queryExpander = MultiQueryExpander.builder()
    .chatClientBuilder(chatClientBuilder)
    .includeOriginal(false)
    .build();
```

Подсказка, используемая этим компонентом, может быть настроена с помощью метода `promptTemplate()`, доступного в билдере.

### Извлечение

Модули извлечения отвечают за запросы к системам данных, таким как векторное хранилище, и извлечение наиболее релевантных документов.

#### Поиск документов

Компонент, отвечающий за извлечение `Documents` из базового источника данных, такого как поисковая система, векторное хранилище, база данных или граф знаний.

##### VectorStoreDocumentRetriever

`VectorStoreDocumentRetriever` извлекает документы из векторного хранилища, которые семантически схожи с входным запросом. Он поддерживает фильтрацию на основе метаданных, порога схожести и топ-k результатов.

```java
DocumentRetriever retriever = VectorStoreDocumentRetriever.builder()
    .vectorStore(vectorStore)
    .similarityThreshold(0.73)
    .topK(5)
    .filterExpression(new FilterExpressionBuilder()
        .eq("genre", "сказка")
        .build())
    .build();
List<Document> documents = retriever.retrieve(new Query("Кто главный герой истории?"));
```

Фильтрационное выражение может быть статическим или динамическим. Для динамических фильтрационных выражений вы можете передать `Supplier`.

```java
DocumentRetriever retriever = VectorStoreDocumentRetriever.builder()
    .vectorStore(vectorStore)
    .filterExpression(() -> new FilterExpressionBuilder()
        .eq("tenant", TenantContextHolder.getTenantIdentifier())
        .build())
    .build();
List<Document> documents = retriever.retrieve(new Query("Какие KPI на следующий семестр?"));
```

Вы также можете предоставить фильтрационное выражение, специфичное для запроса, через API `Query`, используя параметр `FILTER_EXPRESSION`. Если предоставлены как фильтрационное выражение, специфичное для запроса, так и фильтрационное выражение, специфичное для извлекателя, то приоритет будет у фильтрационного выражения, специфичного для запроса.

```java
Query query = Query.builder()
    .text("Кто такой Анаклет?")
    .context(Map.of(VectorStoreDocumentRetriever.FILTER_EXPRESSION, "location == 'Шепчущие леса'"))
    .build();
List<Document> retrievedDocuments = documentRetriever.retrieve(query);
```

#### Объединение документов

Компонент для объединения документов, извлеченных на основе нескольких запросов и из нескольких источников данных, в одну коллекцию документов. В процессе объединения он также может обрабатывать дублирующиеся документы и стратегии взаимного ранжирования.

##### ConcatenationDocumentJoiner

`ConcatenationDocumentJoiner` объединяет документы, извлеченные на основе нескольких запросов и из нескольких источников данных, путем их конкатенации в одну коллекцию документов. В случае дублирующихся документов сохраняется первое вхождение. Оценка каждого документа сохраняется без изменений.

```java
Map<Query, List<List<Document>>> documentsForQuery = ...
DocumentJoiner documentJoiner = new ConcatenationDocumentJoiner();
List<Document> documents = documentJoiner.join(documentsForQuery);
```

### Пост-извлечение

Модули пост-извлечения отвечают за обработку извлеченных документов для достижения наилучших результатов генерации.
```Компонент для постобработки извлеченных документов на основе запроса, решающий такие задачи, как _потеря в середине_, ограничения длины контекста от модели и необходимость уменьшения шума и избыточности в извлеченной информации.

Например, он может ранжировать документы в зависимости от их релевантности к запросу, удалять нерелевантные или избыточные документы или сжимать содержание каждого документа, чтобы уменьшить шум и избыточность.

### Генерация

Модули генерации отвечают за создание окончательного ответа на основе пользовательского запроса и извлеченных документов.

#### Увеличение запроса

Компонент для увеличения входного запроса дополнительными данными, полезный для предоставления большой языковой модели необходимого контекста для ответа на пользовательский запрос.

##### ContextualQueryAugmenter

`ContextualQueryAugmenter` увеличивает пользовательский запрос контекстными данными из содержания предоставленных документов.

```java
QueryAugmenter queryAugmenter = ContextualQueryAugmenter.builder().build();
```

По умолчанию `ContextualQueryAugmenter` не позволяет извлеченному контексту быть пустым. Когда это происходит, он инструктирует модель не отвечать на пользовательский запрос.

Вы можете включить опцию `allowEmptyContext`, чтобы разрешить модели генерировать ответ, даже когда извлеченный контекст пуст.

```java
QueryAugmenter queryAugmenter = ContextualQueryAugmenter.builder()
        .allowEmptyContext(true)
        .build();
```

Подсказки, используемые этим компонентом, могут быть настроены с помощью методов `promptTemplate()` и `emptyContextPromptTemplate()`, доступных в билдере.
