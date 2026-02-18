[[testcontainers]]
# Testcontainers

Spring AI предоставляет автонастройку Spring Boot для установления соединения с сервисом модели или векторным хранилищем, работающим через Testcontainers. Чтобы включить эту функциональность, добавьте следующую зависимость в файл `pom.xml` вашего проекта на Maven:

```xml
<dependency>
   <groupId>org.springframework.ai</groupId>
   <artifactId>spring-ai-spring-boot-testcontainers</artifactId>
</dependency>
```

или в файл сборки Gradle `build.gradle`.

```groovy
dependencies {
    implementation 'org.springframework.ai:spring-ai-spring-boot-testcontainers'
}
```

> **Совет:** Обратитесь к разделу xref:getting-started.adoc#dependency-management[Управление зависимостями], чтобы добавить BOM Spring AI в ваш файл сборки.

## Соединения с сервисами

В модуле `spring-ai-spring-boot-testcontainers` предоставляются следующие фабрики соединений с сервисами:

[cols="|,|"]
|====
| Подробности соединения	 | Соответствует

| `AwsOpenSearchConnectionDetails`
| Контейнеры типа `LocalStackContainer`

| `ChromaConnectionDetails`
| Контейнеры типа `ChromaDBContainer`

| `McpSseClientConnectionDetails`
| Контейнеры типа `DockerMcpGatewayContainer`

| `MilvusServiceClientConnectionDetails`
| Контейнеры типа `MilvusContainer`

| `OllamaConnectionDetails`
| Контейнеры типа `OllamaContainer`

| `OpenSearchConnectionDetails`
| Контейнеры типа `OpensearchContainer`

| `QdrantConnectionDetails`
| Контейнеры типа `QdrantContainer`

| `TypesenseConnectionDetails`
| Контейнеры типа `TypesenseContainer`

| `WeaviateConnectionDetails`
| Контейнеры типа `WeaviateContainer`
|====

Дополнительные соединения с сервисами предоставляются модулем spring boot `spring-boot-testcontainers`. Обратитесь к странице документации https://docs.spring.io/spring-boot/reference/testing/testcontainers.html#testing.testcontainers.service-connections[Соединения сервисов Testcontainers] для получения полного списка.
