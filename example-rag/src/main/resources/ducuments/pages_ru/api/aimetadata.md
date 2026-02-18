[[AiMetadata]]
# AI метаданные

Использование ИИ, такого как ChatGPT от OpenAI, потребляет ресурсы и генерирует метрики, возвращаемые поставщиком ИИ на основе использования и запросов, сделанных к ИИ через API. Потребление обычно измеряется в виде сделанных запросов или использованных токенов за определенный период времени, например, ежемесячно, который поставщики ИИ используют для измерения этого потребления и сброса лимитов. Ваши лимиты запросов напрямую определяются вашим тарифным планом, когда вы подписались на услуги вашего поставщика ИИ. Например, вы можете ознакомиться с деталями о https://platform.openai.com/docs/guides/rate-limits?context=tier-free[лимитах запросов] и https://openai.com/pricing#language-models[тарифах] OpenAI, перейдя по ссылкам.

Чтобы получить представление о вашем потреблении ИИ (модели) и общем использовании, Spring AI предоставляет API для анализа метаданных, которые возвращаются поставщиками ИИ в их API.

Spring AI определяет 3 основных интерфейса для изучения этих метрик: `GenerationMetadata`, `RateLimit` и `Usage`. Все эти интерфейсы можно получить программно из `ChatResponse`, возвращаемого и инициируемого запросом к ИИ.

[[AiMetadata-GenerationMetadata]]
## Интерфейс `GenerationMetadata`

Интерфейс `GenerationMetadata` определен как:

.GenerationMetadata интерфейс
```java
interface GenerationMetadata {

	default RateLimit getRateLimit() {
		return RateLimit.NULL;
	}

	default Usage getUsage() {
		return Usage.NULL;
	}

}
```

Экземпляр `GenerationMetadata` автоматически создается Spring AI, когда запрос к ИИ отправляется через API поставщика ИИ и возвращается ответ ИИ. Вы можете получить доступ к метаданным поставщика ИИ из `ChatResponse`, используя:

.Получение доступа к `GenerationMetadata` из `ChatResponse`
```java
@Service
class MyService {

	ApplicationObjectType askTheAi(ServiceRequest request) {

        Prompt prompt = createPrompt(request);

        ChatResponse response = chatModel.call(prompt);

        // Обработка ответа чата

        GenerationMetadata metadata = response.getMetadata();

        // Анализ метаданных ИИ, возвращенных в ответе чата API поставщика ИИ

        Long totalTokensUsedInAiPromptAndResponse = metadata.getUsage().getTotalTokens();

        // Используйте эту информацию каким-либо образом
	}
}
```

Вы можете представить, что вы можете ограничить количество запросов в своих собственных приложениях Spring с использованием ИИ или ограничить размеры `Prompt`, что влияет на использование токенов, в автоматическом, интеллектуальном и реальном времени.

Минимально, вы можете просто собирать эти метрики для мониторинга и отчетности о вашем потреблении.

[[AiMetadata-RateLimit]]
## RateLimit

Интерфейс `RateLimit` предоставляет доступ к фактической информации, возвращаемой поставщиком ИИ о вашем использовании API при выполнении запросов к ИИ.

.`RateLimit` интерфейс
```java
interface RateLimit {

	Long getRequestsLimit();

	Long getRequestsRemaining();

	Duration getRequestsReset();

	Long getTokensLimit();

	Long getTokensRemaining();

	Duration getTokensReset();

}
```

`requestsLimit` и `requestsRemaining` сообщают вам, сколько запросов к ИИ, исходя из плана поставщика ИИ, который вы выбрали при регистрации, вы можете сделать всего, а также ваш оставшийся баланс в течение данного периода времени. `requestsReset` возвращает `Duration` времени до истечения периода времени и сброса ваших лимитов в зависимости от выбранного вами плана.

Методы для `tokensLimit`, `tokensRemaining` и `tokensReset` аналогичны методам для запросов, но сосредоточены на лимитах токенов, балансе и сбросах.

Экземпляр `RateLimit` можно получить из `GenerationMetadata`, следующим образом:

.Получение доступа к `RateLimit` из `GenerationMetadata`
```java
RateLimit rateLimit = generationMetadata.getRateLimit();

Long tokensRemaining = this.rateLimit.getTokensRemaining();

// сделайте что-то интересное с метаданными RateLimit
```

Для поставщиков ИИ, таких как OpenAI, метаданные лимита запросов возвращаются в https://platform.openai.com/docs/guides/rate-limits/rate-limits-in-headers[HTTP заголовках] из их (REST) API, доступного через HTTP-клиенты, такие как OkHttp.

Поскольку это может быть потенциально затратной операцией, сбор метаданных лимита запросов ИИ должен быть явно включен. Вы можете включить этот сбор с помощью свойства Spring AI в файле application.properties Spring Boot; например:

.Включение сбора лимитов API из метаданных ИИ
```properties
# Spring Boot application.properties
spring.ai.openai.metadata.rate-limit-metrics-enabled=true
```

[[AiMetadata-Usage]]
## Usage

Как показано [выше](#AiMetadata-GenerationMetadata), данные `Usage` можно получить из объекта `GenerationMetadata`. Интерфейс `Usage` определен как:

.`Usage` интерфейс
```java
interface Usage {

	Long getPromptTokens();

	Long getGenerationTokens();

	default Long getTotalTokens() {
		return getPromptTokens() + getGenerationTokens();
	}

}
```

Имена методов говорят сами за себя, но сообщают вам токены, которые ИИ потребовалось для обработки `Prompt` и генерации ответа.

`totalTokens` — это сумма `promptTokens` и `generationTokens`. Spring AI вычисляет это по умолчанию, но информация возвращается в ответе ИИ от OpenAI.
