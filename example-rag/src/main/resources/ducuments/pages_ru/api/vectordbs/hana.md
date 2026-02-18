# SAP HANA Cloud

## Предварительные требования

- Вам нужна учетная запись SAP HANA Cloud vector engine - обратитесь к руководству xref:api/vectordbs/hanadb-provision-a-trial-account.adoc[Создание пробной учетной записи для SAP HANA Cloud vector engine] для создания пробной учетной записи.
- При необходимости, API-ключ для xref:api/embeddings.adoc#available-implementations[EmbeddingModel] для генерации встраиваний, хранящихся в векторном хранилище.

## Автонастройка

Spring AI не предоставляет специального модуля для векторного хранилища SAP Hana. Пользователи должны предоставить свою собственную конфигурацию в приложениях, используя стандартный модуль векторного хранилища для SAP Hana в Spring AI - `spring-ai-hanadb-store`.

> **Совет:** Обратитесь к разделу xref:getting-started.adoc#dependency-management[Управление зависимостями], чтобы добавить BOM Spring AI в ваш файл сборки.

Пожалуйста, ознакомьтесь со списком xref:#hanacloudvectorstore-properties[Свойства HanaCloudVectorStore], чтобы узнать о значениях по умолчанию и параметрах конфигурации.

> **Совет:** Обратитесь к разделу xref:getting-started.adoc#artifact-repositories[Артефактные репозитории], чтобы добавить Maven Central и/или Snapshot репозитории в ваш файл сборки.

Кроме того, вам потребуется настроенный бин `EmbeddingModel`. Обратитесь к разделу xref:api/embeddings.adoc#available-implementations[EmbeddingModel] для получения дополнительной информации.

[[hanacloudvectorstore-properties]]
## Свойства HanaCloudVectorStore

Вы можете использовать следующие свойства в вашей конфигурации Spring Boot для настройки векторного хранилища SAP Hana. Он использует свойства `spring.datasource.**` для настройки источника данных Hana и свойства `spring.ai.vectorstore.hanadb.**` для настройки векторного хранилища Hana.

| Свойство | Описание | Значение по умолчанию |
| --- | --- | --- |
| `spring.datasource.driver-class-name` | Имя класса драйвера | com.sap.db.jdbc.Driver |
| `spring.datasource.url` | URL источника данных Hana | - |
| `spring.datasource.username` | Имя пользователя источника данных Hana | - |
| `spring.datasource.password` | Пароль источника данных Hana | - |
| `spring.ai.vectorstore.hanadb.top-k` | TODO | - |
| `spring.ai.vectorstore.hanadb.table-name` | TODO | - |
| `spring.ai.vectorstore.hanadb.initialize-schema` | инициализировать ли необходимую схему | `false` |


## Создание примера приложения RAGПоказывает, как настроить проект, который использует SAP Hana Cloud в качестве векторной базы данных и использует OpenAI для реализации паттерна RAG

- Создайте таблицу `CRICKET_WORLD_CUP` в SAP Hana DB:
[sql]
```
CREATE TABLE CRICKET_WORLD_CUP (
    _ID VARCHAR2(255) PRIMARY KEY,
    CONTENT CLOB,
    EMBEDDING REAL_VECTOR(1536)
)
```

- Добавьте следующие зависимости в ваш `pom.xml`

Вы можете установить свойство `spring-ai-version` как `<spring-ai-version>1.0.0-SNAPSHOT</spring-ai-version>`:
```xml

<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>org.springframework.ai</groupId>
            <artifactId>spring-ai-bom</artifactId>
            <version>${spring-ai-version}</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>

<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>

<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-pdf-document-reader</artifactId>
</dependency>

<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-starter-model-openai</artifactId>
</dependency>

<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-starter-vector-store-hana</artifactId>
</dependency>

<dependency>
    <groupId>org.projectlombok</groupId>
    <artifactId>lombok</artifactId>
    <version>1.18.30</version>
    <scope>provided</scope>
</dependency>
```

- Добавьте следующие свойства в файл `application.properties`:

[yml]
```
spring.ai.openai.api-key=${OPENAI_API_KEY}
spring.ai.openai.embedding.options.model=text-embedding-ada-002

spring.datasource.driver-class-name=com.sap.db.jdbc.Driver
spring.datasource.url=${HANA_DATASOURCE_URL}
spring.datasource.username=${HANA_DATASOURCE_USERNAME}
spring.datasource.password=${HANA_DATASOURCE_PASSWORD}

spring.ai.vectorstore.hanadb.tableName=CRICKET_WORLD_CUP
spring.ai.vectorstore.hanadb.topK=3
```

### Создайте класс `Entity` с именем `CricketWorldCup`, который наследуется от `HanaVectorEntity`:```markdown
# Cricket World Cup

## Создание репозитория

Создайте `Repository` с именем `CricketWorldCupRepository`, который реализует интерфейс `HanaVectorRepository`:

```java
package com.interviewpedia.spring.ai.hana;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.springframework.ai.vectorstore.hanadb.HanaVectorRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class CricketWorldCupRepository implements HanaVectorRepository<CricketWorldCup> {
    @PersistenceContext
    private EntityManager entityManager;

    @Override
    @Transactional
    public void save(String tableName, String id, String embedding, String content) {
        String sql = String.format("""
                INSERT INTO %s (_ID, EMBEDDING, CONTENT)
                VALUES(:_id, TO_REAL_VECTOR(:embedding), :content)
                """, tableName);

		this.entityManager.createNativeQuery(sql)
                .setParameter("_id", id)
                .setParameter("embedding", embedding)
                .setParameter("content", content)
                .executeUpdate();
    }

    @Override
    @Transactional
    public int deleteEmbeddingsById(String tableName, List<String> idList) {
        String sql = String.format("""
                DELETE FROM %s WHERE _ID IN (:ids)
                """, tableName);

        return this.entityManager.createNativeQuery(sql)
                .setParameter("ids", idList)
                .executeUpdate();
    }

    @Override
    @Transactional
    public int deleteAllEmbeddings(String tableName) {
        String sql = String.format("""
                DELETE FROM %s
                """, tableName);

        return this.entityManager.createNativeQuery(sql).executeUpdate();
    }

    @Override
    public List<CricketWorldCup> cosineSimilaritySearch(String tableName, int topK, String queryEmbedding) {
        String sql = String.format("""
                SELECT TOP :topK * FROM %s
                ORDER BY COSINE_SIMILARITY(EMBEDDING, TO_REAL_VECTOR(:queryEmbedding)) DESC
                """, tableName);

        return this.entityManager.createNativeQuery(sql, CricketWorldCup.class)
                .setParameter("topK", topK)
                .setParameter("queryEmbedding", queryEmbedding)
                .getResultList();
    }
}
```

## Создание REST контроллера

Теперь создайте класс REST контроллера `CricketWorldCupHanaController` и внедрите `ChatModel` и `VectorStore` в качестве зависимостей. В этом контроллере создайте следующие REST-эндпоинты:

- `/ai/hana-vector-store/cricket-world-cup/purge-embeddings` - для удаления всех векторов из Vector Store
- `/ai/hana-vector-store/cricket-world-cup/upload` - для загрузки файла Cricket_World_Cup.pdf, чтобы его данные были сохранены в SAP Hana Cloud Vector DB в виде векторов
- `/ai/hana-vector-store/cricket-world-cup` - для реализации `RAG` с использованием [Cosine_Similarity в SAP Hana DB](https://help.sap.com/docs/hana-cloud-database/sap-hana-cloud-sap-hana-database-vector-engine-guide/vectors-vector-embeddings-and-metrics)

```java
package com.interviewpedia.spring.ai.hana;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.hanadb.HanaCloudVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@RestController
@Slf4j
public class CricketWorldCupHanaController {
    private final VectorStore hanaCloudVectorStore;
    private final ChatModel chatModel;

    @Autowired
    public CricketWorldCupHanaController(ChatModel chatModel, VectorStore hanaCloudVectorStore) {
        this.chatModel = chatModel;
        this.hanaCloudVectorStore = hanaCloudVectorStore;
    }

    @PostMapping("/ai/hana-vector-store/cricket-world-cup/purge-embeddings")
    public ResponseEntity<String> purgeEmbeddings() {
        int deleteCount = ((HanaCloudVectorStore) this.hanaCloudVectorStore).purgeEmbeddings();
        log.info("{} embeddings purged from CRICKET_WORLD_CUP table in Hana DB", deleteCount);
        return ResponseEntity.ok().body(String.format("%d embeddings purged from CRICKET_WORLD_CUP table in Hana DB", deleteCount));
    }

    @PostMapping("/ai/hana-vector-store/cricket-world-cup/upload")
    public ResponseEntity<String> handleFileUpload(@RequestParam("pdf") MultipartFile file) throws IOException {
        Resource pdf = file.getResource();
        Supplier<List<Document>> reader = new PagePdfDocumentReader(pdf);
        Function<List<Document>, List<Document>> splitter = new TokenTextSplitter();
        List<Document> documents = splitter.apply(reader.get());
        log.info("{} documents created from pdf file: {}", documents.size(), pdf.getFilename());
		this.hanaCloudVectorStore.accept(documents);
        return ResponseEntity.ok().body(String.format("%d documents created from pdf file: %s",
                documents.size(), pdf.getFilename()));
    }

    @GetMapping("/ai/hana-vector-store/cricket-world-cup")
    public Map<String, String> hanaVectorStoreSearch(@RequestParam(value = "message") String message) {
        var documents = this.hanaCloudVectorStore.similaritySearch(message);
        var inlined = documents.stream().map(Document::getText).collect(Collectors.joining(System.lineSeparator()));
        var similarDocsMessage = new SystemPromptTemplate("Based on the following: {documents}")
                .createMessage(Map.of("documents", inlined));

        var userMessage = new UserMessage(message);
        Prompt prompt = new Prompt(List.of(similarDocsMessage, userMessage));
        String generation = this.chatModel.call(prompt).getResult().getOutput().getText();
        log.info("Generation: {}", generation);
        return Map.of("generation", generation);
    }
}
```

## Конфигурация бина в приложении

Поскольку поддержка векторного хранилища HanaDB не предоставляет модуля автоконфигурации, вам также необходимо предоставить бин векторного хранилища в вашем приложении, как показано ниже, в качестве примера.

```java
@Bean
public VectorStore hanaCloudVectorStore(CricketWorldCupRepository cricketWorldCupRepository,
        EmbeddingModel embeddingModel) {

    return HanaCloudVectorStore.builder(cricketWorldCupRepository, embeddingModel)
        .tableName("CRICKET_WORLD_CUP")
        .topK(1)
        .build();
}
```

## Загрузка PDF файла

Используйте контекстный PDF файл с Википедии.

Перейдите на [википедию](https://en.wikipedia.org/wiki/Cricket_World_Cup) и [скачайте](https://en.wikipedia.org/w/index.php?title=Special:DownloadAsPdf&page=Cricket_World_Cup&action=show-download-screen) страницу `Cricket World Cup` в формате PDF.

![width=800](hanadb/wikipedia.png)

Загрузите этот PDF файл, используя созданный ранее REST-эндпоинт для загрузки файла.
```
