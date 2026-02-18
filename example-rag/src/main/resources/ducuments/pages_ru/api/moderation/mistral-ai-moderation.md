# Модерация

## Введение

Spring AI поддерживает новую службу модерации, представленную Mistral AI и работающую на основе модели модерации Mistral. Она позволяет обнаруживать вредоносный текстовый контент по нескольким направлениям политики. Для получения дополнительной информации о модели модерации Mistral, следуйте по этой https://docs.mistral.ai/capabilities/guardrailing/[ссылке].

## Предварительные требования

1. Создайте учетную запись Mistral AI и получите API-ключ. Вы можете зарегистрироваться на https://auth.mistral.ai/ui/registration[странице регистрации Mistral AI] и сгенерировать API-ключ на https://console.mistral.ai/api-keys/[странице API-ключей].
2. Добавьте зависимость `spring-ai-mistral-ai` в файл сборки вашего проекта. Для получения дополнительной информации обратитесь к разделу xref:getting-started.adoc#dependency-management[Управление зависимостями].

## Автоконфигурация

[ПРИМЕЧАНИЕ]
====
В автоконфигурации Spring AI произошли значительные изменения, касающиеся имен артефактов стартовых модулей. Пожалуйста, обратитесь к https://docs.spring.io/spring-ai/reference/upgrade-notes.html[заметкам об обновлении] для получения дополнительной информации.
====

Spring AI предоставляет автоконфигурацию Spring Boot для модели модерации Mistral AI. Чтобы включить ее, добавьте следующую зависимость в файл `pom.xml` вашего проекта Maven:

```xml
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-starter-model-mistral-ai</artifactId>
</dependency>
```

или в файл сборки Gradle `build.gradle`:

```groovy
dependencies {
    implementation 'org.springframework.ai:spring-ai-starter-model-mistral-ai'
}
```

> **Совет:** Обратитесь к разделу xref:getting-started.adoc#dependency-management[Управление зависимостями], чтобы добавить BOM Spring AI в ваш файл сборки.

## Свойства модерации

### Свойства подключения
Префикс spring.ai.mistralai используется в качестве префикса свойств, который позволяет вам подключаться к Mistral AI.
[cols="3,3,1"]
|====
| Свойство | Описание | По умолчанию
| spring.ai.mistralai.base-url   | URL для подключения |  https://api.mistral.ai
| spring.ai.mistralai.api-key    | API-ключ           |  -
|====

### Свойства конфигурации

[ПРИМЕЧАНИЕ]
====
Включение и отключение автоконфигураций модерации теперь настраивается через свойства верхнего уровня с префиксом `spring.ai.model.moderation`.

Чтобы включить, используйте spring.ai.model.moderation=mistral (по умолчанию включено)

Чтобы отключить, используйте spring.ai.model.moderation=none (или любое значение, не совпадающее с mistral)

Это изменение сделано для возможности конфигурации нескольких моделей.
====

Префикс spring.ai.mistralai.moderation используется в качестве префикса свойств для настройки модели модерации Mistral AI.
[cols="3,5,1"]
|====
| Свойство | Описание | По умолчанию
| spring.ai.model.moderation   | Включить модель модерации |  mistral
| spring.ai.mistralai.moderation.base-url   | URL для подключения |  https://api.mistral.ai
| spring.ai.mistralai.moderation.api-key    | API-ключ           |  -
| spring.ai.mistralai.moderation.options.model  | ID модели, используемой для модерации. | mistral-moderation-latest
|====

> **Примечание:** Вы можете переопределить общие свойства `spring.ai.mistralai.base-url`, `spring.ai.mistralai.api-key`. Свойства `spring.ai.mistralai.moderation.base-url`, `spring.ai.mistralai.moderation.api-key`, если они установлены, имеют приоритет над общими свойствами. Это полезно, если вы хотите использовать разные учетные записи Mistral AI для разных моделей и разные конечные точки моделей.

> **Совет:** Все свойства с префиксом `spring.ai.mistralai.moderation.options` могут быть переопределены во время выполнения.

## Параметры времени выполнения```markdown
Класс MistralAiModerationOptions предоставляет параметры, которые используются при выполнении запроса на модерацию. При запуске используются параметры, указанные в spring.ai.mistralai.moderation, но вы можете переопределить их во время выполнения.

Например:

```java
MistralAiModerationOptions moderationOptions = MistralAiModerationOptions.builder()
    .model("mistral-moderation-latest")
    .build();

ModerationPrompt moderationPrompt = new ModerationPrompt("Text to be moderated", this.moderationOptions);
ModerationResponse response = mistralAiModerationModel.call(this.moderationPrompt);

// Доступ к результатам модерации
Moderation moderation = moderationResponse.getResult().getOutput();

// Вывод общей информации
System.out.println("Moderation ID: " + moderation.getId());
System.out.println("Model used: " + moderation.getModel());

// Доступ к результатам модерации (обычно их только один, но это список)
for (ModerationResult result : moderation.getResults()) {
    System.out.println("\nModeration Result:");
    System.out.println("Flagged: " + result.isFlagged());

    // Доступ к категориям
    Categories categories = this.result.getCategories();
    System.out.println("\nCategories:");
    System.out.println("Law: " + categories.isLaw());
    System.out.println("Financial: " + categories.isFinancial());
    System.out.println("PII: " + categories.isPii());
    System.out.println("Sexual: " + categories.isSexual());
    System.out.println("Hate: " + categories.isHate());
    System.out.println("Harassment: " + categories.isHarassment());
    System.out.println("Self-Harm: " + categories.isSelfHarm());
    System.out.println("Sexual/Minors: " + categories.isSexualMinors());
    System.out.println("Hate/Threatening: " + categories.isHateThreatening());
    System.out.println("Violence/Graphic: " + categories.isViolenceGraphic());
    System.out.println("Self-Harm/Intent: " + categories.isSelfHarmIntent());
    System.out.println("Self-Harm/Instructions: " + categories.isSelfHarmInstructions());
    System.out.println("Harassment/Threatening: " + categories.isHarassmentThreatening());
    System.out.println("Violence: " + categories.isViolence());

    // Доступ к оценкам категорий
    CategoryScores scores = this.result.getCategoryScores();
    System.out.println("\nCategory Scores:");
    System.out.println("Law: " + scores.getLaw());
    System.out.println("Financial: " + scores.getFinancial());
    System.out.println("PII: " + scores.getPii());
    System.out.println("Sexual: " + scores.getSexual());
    System.out.println("Hate: " + scores.getHate());
    System.out.println("Harassment: " + scores.getHarassment());
    System.out.println("Self-Harm: " + scores.getSelfHarm());
    System.out.println("Sexual/Minors: " + scores.getSexualMinors());
    System.out.println("Hate/Threatening: " + scores.getHateThreatening());
    System.out.println("Violence/Graphic: " + scores.getViolenceGraphic());
    System.out.println("Self-Harm/Intent: " + scores.getSelfHarmIntent());
    System.out.println("Self-Harm/Instructions: " + scores.getSelfHarmInstructions());
    System.out.println("Harassment/Threatening: " + scores.getHarassmentThreatening());
    System.out.println("Violence: " + scores.getViolence());
}
```

## Ручная конфигурация
```Добавьте зависимость `spring-ai-mistral-ai` в файл `pom.xml` вашего проекта:

```xml
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-mistral-ai</artifactId>
</dependency>
```

или в файл сборки `build.gradle` вашего проекта:

```groovy
dependencies {
    implementation 'org.springframework.ai:spring-ai-mistral-ai'
}
```

> **Совет:** Обратитесь к разделу xref:getting-started.adoc#dependency-management[Управление зависимостями], чтобы добавить Spring AI BOM в ваш файл сборки.

Далее создайте MistralAiModerationModel:

```java
MistralAiModerationApi mistralAiModerationApi = new MistralAiModerationApi(System.getenv("MISTRAL_AI_API_KEY"));

MistralAiModerationModel mistralAiModerationModel = new MistralAiModerationModel(this.mistralAiModerationApi);

MistralAiModerationOptions moderationOptions = MistralAiModerationOptions.builder()
    .model("mistral-moderation-latest")
    .build();

ModerationPrompt moderationPrompt = new ModerationPrompt("Text to be moderated", this.moderationOptions);
ModerationResponse response = this.mistralAiModerationModel.call(this.moderationPrompt);
```

## Пример кода
Тест `MistralAiModerationModelIT` предоставляет общие примеры использования библиотеки. Вы можете обратиться к этому тесту для более подробных примеров использования.
