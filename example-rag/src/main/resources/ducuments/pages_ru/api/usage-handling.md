# Использование Chat/Embedding Response Usage

## Обзор
Spring AI улучшил обработку использования моделей, введя метод `getNativeUsage()` в интерфейсе Usage и предоставив реализацию `DefaultUsage`. Это изменение упрощает отслеживание и отчетность о метриках использования различных AI моделей, сохраняя при этом согласованность в рамках фреймворка.

## Ключевые изменения

### Улучшение интерфейса Usage
Интерфейс `Usage` теперь включает новый метод:
```java
Object getNativeUsage();
```
Этот метод позволяет получить доступ к данным о нативном использовании, специфичным для модели, что позволяет более детально отслеживать использование при необходимости.

### Использование с ChatModel

Вот полный пример, показывающий, как отслеживать использование с ChatModel от OpenAI:

```java
@SpringBootConfiguration
public class Configuration {

        @Bean
        public OpenAiApi chatCompletionApi() {
            return OpenAiApi.builder()
                .apiKey(System.getenv("OPENAI_API_KEY"))
                .build();
        }

        @Bean
        public OpenAiChatModel openAiClient(OpenAiApi openAiApi) {
            return OpenAiChatModel.builder()
                .openAiApi(openAiApi)
                .build();
        }

    }

@Service
public class ChatService {

    private final OpenAiChatModel chatModel;

    public ChatService(OpenAiChatModel chatModel) {
        this.chatModel = chatModel;
    }

    public void demonstrateUsage() {
        // Создание чата
        Prompt prompt = new Prompt("Какова погода сегодня?");

        // Получение ответа на чат
        ChatResponse response = this.chatModel.call(prompt);

        // Доступ к информации об использовании
        Usage usage = response.getMetadata().getUsage();

        // Получение стандартных метрик использования
        System.out.println("Токены запроса: " + usage.getPromptTokens());
        System.out.println("Токены завершения: " + usage.getCompletionTokens());
        System.out.println("Всего токенов: " + usage.getTotalTokens());

        // Доступ к нативным данным использования OpenAI с детальной информацией о токенах
        if (usage.getNativeUsage() instanceof org.springframework.ai.openai.api.OpenAiApi.Usage) {
            org.springframework.ai.openai.api.OpenAiApi.Usage nativeUsage =
                (org.springframework.ai.openai.api.OpenAiApi.Usage) usage.getNativeUsage();

            // Детальная информация о токенах запроса
            System.out.println("Детали токенов запроса:");
            System.out.println("- Токены аудио: " + nativeUsage.promptTokensDetails().audioTokens());
            System.out.println("- Кэшированные токены: " + nativeUsage.promptTokensDetails().cachedTokens());

            // Детальная информация о токенах завершения
            System.out.println("Детали токенов завершения:");
            System.out.println("- Токены рассуждений: " + nativeUsage.completionTokenDetails().reasoningTokens());
            System.out.println("- Принятые токены предсказания: " + nativeUsage.completionTokenDetails().acceptedPredictionTokens());
            System.out.println("- Токены аудио: " + nativeUsage.completionTokenDetails().audioTokens());
            System.out.println("- Отклоненные токены предсказания: " + nativeUsage.completionTokenDetails().rejectedPredictionTokens());
        }
    }
}
```

### Использование с ChatClient

Если вы используете `ChatClient`, вы можете получить доступ к информации об использовании с помощью объекта `ChatResponse`:

```java
// Создание чата
Prompt prompt = new Prompt("Какова погода сегодня?");

// Создание клиента чата
ChatClient chatClient = ChatClient.create(chatModel);

// Получение ответа на чат
ChatResponse response = chatClient.prompt(prompt)
        .call()
        .chatResponse();

// Доступ к информации об использовании
Usage usage = response.getMetadata().getUsage();
```

## Преимущества**Стандартизация**: Обеспечивает единообразный способ обработки использования различных моделей ИИ  
**Гибкость**: Поддерживает специфические для модели данные использования через встроенную функцию использования  
**Упрощение**: Снижает объем шаблонного кода с помощью реализации по умолчанию  
**Расширяемость**: Легко расширяется для специфических требований модели при сохранении совместимости  

### Соображения по типобезопасности

При работе с данными использования, полученными из встроенной функции, внимательно относитесь к приведению типов:  
```java
// Безопасный способ доступа к встроенному использованию
if (usage.getNativeUsage() instanceof org.springframework.ai.openai.api.OpenAiApi.Usage) {
    org.springframework.ai.openai.api.OpenAiApi.Usage nativeUsage =
        (org.springframework.ai.openai.api.OpenAiApi.Usage) usage.getNativeUsage();
    // Работа с данными встроенного использования
}
```
