## Доступ к нативному клиенту

Реализация Coherence Vector Store предоставляет доступ к базовому нативному клиенту Coherence (`Session`) через метод `getNativeClient()`:

```java
CoherenceVectorStore vectorStore = context.getBean(CoherenceVectorStore.class);
Optional<Session> nativeClient = vectorStore.getNativeClient();

if (nativeClient.isPresent()) {
    Session session = nativeClient.get();
    // Используйте нативный клиент для операций, специфичных для Coherence
}
```

Нативный клиент предоставляет доступ к функциям и операциям, специфичным для Coherence, которые могут не быть доступны через интерфейс `VectorStore`.
