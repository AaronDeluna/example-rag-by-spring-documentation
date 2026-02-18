[[cloud-bindings]]
# Облачные привязки

Spring AI предоставляет поддержку облачных привязок на основе основ, изложенных в https://github.com/spring-cloud/spring-cloud-bindings[spring-cloud-bindings]. Это позволяет приложениям указывать тип привязки для провайдера и затем выражать свойства, используя универсальный формат. Облачные привязки spring-ai будут обрабатывать эти свойства и связывать их с соответствующими свойствами spring-ai.

Например, при использовании `OpenAi` тип привязки — `openai`. Используя свойство `spring.ai.cloud.bindings.openai.enabled`, процессор привязки может быть включен или отключен. По умолчанию, при указании типа привязки, это свойство будет включено. Конфигурация для `api-key`, `uri`, `username`, `password` и т.д. может быть указана, и spring-ai сопоставит их с соответствующими свойствами в поддерживаемой системе.

Чтобы включить поддержку облачных привязок, добавьте следующую зависимость в приложение.

```xml
<dependency>
   <groupId>org.springframework.ai</groupId>
   <artifactId>spring-ai-spring-cloud-bindings</artifactId>
</dependency>
```

или в файл сборки Gradle `build.gradle`.

```groovy
dependencies {
    implementation 'org.springframework.ai:spring-ai-spring-cloud-bindings'
}
```

> **Совет:** Обратитесь к разделу xref:getting-started.adoc#dependency-management[Управление зависимостями], чтобы добавить Spring AI BOM в ваш файл сборки.

## Доступные облачные привязки

Следующие компоненты в настоящее время поддерживают облачные привязки в модуле `spring-ai-spring-cloud-bindings`:

[cols="|,|"]
|====
| Тип сервиса	 | Тип привязки | Исходные свойства | Целевые свойства
| `Chroma Vector Store`
| `chroma` | `uri`, `username`, `password` | `spring.ai.vectorstore.chroma.client.host`, `spring.ai.vectorstore.chroma.client.port`, `spring.ai.vectorstore.chroma.client.username`, `spring.ai.vectorstore.chroma.client.host.password`

| `Mistral AI`
| `mistralai` | `api-key`, `uri` | `spring.ai.mistralai.api-key`, `spring.ai.mistralai.base-url`

| `Ollama`
| `ollama` | `uri` | `spring.ai.ollama.base-url`

| `OpenAi`
| `openai` | `api-key`, `uri` | `spring.ai.openai.api-key`, `spring.ai.openai.base-url`

| `Weaviate`
| `weaviate` | `uri`, `api-key` | `spring.ai.vectorstore.weaviate.scheme`, `spring.ai.vectorstore.weaviate.host`, `spring.ai.vectorstore.weaviate.api-key`

| `Tanzu GenAI`
| `genai` | `uri`, `api-key`, `model-capabilities` (`chat` и `embedding`), `model-name` | `spring.ai.openai.chat.base-url`, `spring.ai.openai.chat.api-key`, `spring.ai.openai.chat.options.model`, `spring.ai.openai.embedding.base-url`, `spring.ai.openai.embedding.api-key`, `spring.ai.openai.embedding.options.model`
|====
