[[docker-compose]]
# Docker Compose

Spring AI предоставляет автонастройку Spring Boot для установления соединения с сервисом модели или векторным хранилищем, работающим через Docker Compose. Чтобы включить эту функциональность, добавьте следующую зависимость в файл `pom.xml` вашего проекта на Maven:

```xml
<dependency>
   <groupId>org.springframework.ai</groupId>
   <artifactId>spring-ai-spring-boot-docker-compose</artifactId>
</dependency>
```

или в файл сборки Gradle `build.gradle`.

```groovy
dependencies {
    implementation 'org.springframework.ai:spring-ai-spring-boot-docker-compose'
}
```

> **Совет:** Обратитесь к разделу xref:getting-started.adoc#dependency-management[Управление зависимостями], чтобы добавить BOM Spring AI в ваш файл сборки.

## Соединения с сервисами

В модуле `spring-ai-spring-boot-docker-compose` предоставлены следующие фабрики соединений с сервисами:

[cols="|,|"]
| Подробности соединения	 | Совпадает с
| `AwsOpenSearchConnectionDetails` | Контейнеры с именем `localstack/localstack`
| `ChromaConnectionDetails` | Контейнеры с именем `chromadb/chroma`, `ghcr.io/chroma-core/chroma`
| `OllamaConnectionDetails` | Контейнеры с именем `ollama/ollama`
| `OpenSearchConnectionDetails` | Контейнеры с именем `opensearchproject/opensearch`
| `QdrantConnectionDetails` | Контейнеры с именем `qdrant/qdrant`
| `TypesenseConnectionDetails` | Контейнеры с именем `typesense/typesense`
| `WeaviateConnectionDetails` | Контейнеры с именем `semitechnologies/weaviate`, `cr.weaviate.io/semitechnologies/weaviate`
| `McpSseClientConnectionDetails` | Контейнеры с именем `docker/mcp-gateway`

Дополнительные соединения с сервисами предоставляются модулем spring boot `spring-boot-docker-compose`. Обратитесь к странице документации https://docs.spring.io/spring-boot/reference/features/dev-services.html#features.dev-services.docker-compose[Поддержка Docker Compose] для получения полного списка.
