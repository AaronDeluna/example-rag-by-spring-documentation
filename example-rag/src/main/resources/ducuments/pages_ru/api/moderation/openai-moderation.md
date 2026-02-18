# Модерация

## Введение

Spring AI поддерживает модель модерации OpenAI, которая позволяет обнаруживать потенциально вредоносный или чувствительный контент в тексте. Следуйте этому https://platform.openai.com/docs/guides/moderation[руководству] для получения дополнительной информации о модели модерации OpenAI.

## Предварительные требования

1. Создайте учетную запись OpenAI и получите API-ключ. Вы можете зарегистрироваться на https://platform.openai.com/signup[странице регистрации OpenAI] и сгенерировать API-ключ на https://platform.openai.com/account/api-keys[странице API-ключей].
2. Добавьте зависимость `spring-ai-openai` в файл сборки вашего проекта. Для получения дополнительной информации обратитесь к разделу xref:getting-started.adoc#dependency-management[Управление зависимостями].

## Автоконфигурация

[ПРИМЕЧАНИЕ]
====
В автоконфигурации Spring AI произошли значительные изменения, касающиеся названий артефактов стартовых модулей. Пожалуйста, обратитесь к https://docs.spring.io/spring-ai/reference/upgrade-notes.html[заметкам об обновлении] для получения дополнительной информации.
====

Spring AI предоставляет автоконфигурацию Spring Boot для модели модерации OpenAI. Чтобы включить ее, добавьте следующую зависимость в файл `pom.xml` вашего проекта Maven:

```xml
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-starter-model-openai</artifactId>
</dependency>
```

или в файл сборки Gradle `build.gradle`:

```groovy
dependencies {
    implementation 'org.springframework.ai:spring-ai-starter-model-openai'
}
```

> **Совет:** Обратитесь к разделу xref:getting-started.adoc#dependency-management[Управление зависимостями], чтобы добавить BOM Spring AI в ваш файл сборки.

## Свойства модерации

### Свойства подключения
Префикс spring.ai.openai используется в качестве префикса свойств, который позволяет вам подключаться к OpenAI.
[cols="3,5,1"]
| Свойство | Описание | По умолчанию
| spring.ai.openai.base-url   | URL для подключения |  https://api.openai.com
| spring.ai.openai.api-key    | API-ключ           |  -
| spring.ai.openai.organization-id | При желании вы можете указать, какая организация используется для API-запроса. |  -
| spring.ai.openai.project-id      | При желании вы можете указать, какой проект используется для API-запроса. |  -

> **Совет:** Для пользователей, которые принадлежат нескольким организациям (или получают доступ к своим проектам через свой устаревший API-ключ пользователя), при желании вы можете указать, какая организация и проект используются для API-запроса. Использование этих API-запросов будет учитываться как использование для указанной организации и проекта.

### Свойства конфигурации[NOTE]
====
Включение и отключение автонастроек встраивания теперь настраивается через свойства верхнего уровня с префиксом `spring.ai.model.moderation`.

Чтобы включить, используйте spring.ai.model.moderation=openai (по умолчанию включено)

Чтобы отключить, используйте spring.ai.model.moderation=none (или любое значение, не совпадающее с openai)

Это изменение сделано для возможности настройки нескольких моделей.
====

Префикс spring.ai.openai.moderation используется в качестве префикса свойства для настройки модели модерации OpenAI.
[cols="3,5,2"]
| Свойство | Описание | По умолчанию
| spring.ai.model.moderation   | Включить модель модерации |  openai
| spring.ai.openai.moderation.base-url   | URL для подключения |  https://api.openai.com
| spring.ai.openai.moderation.api-key    | API-ключ           |  -
| spring.ai.openai.moderation.organization-id | Опционально вы можете указать, какая организация используется для API-запроса. |  -
| spring.ai.openai.moderation.project-id      | Опционально вы можете указать, какой проект используется для API-запроса. |  -
| spring.ai.openai.moderation.moderation-path | Путь к конечной точке API для запросов модерации. Полезно для совместимых с OpenAI API с различными структурами конечных точек. | /v1/moderations
| spring.ai.openai.moderation.options.model  | ID модели, используемой для модерации. | omni-moderation-latest

> **Примечание:** Вы можете переопределить общие свойства `spring.ai.openai.base-url`, `spring.ai.openai.api-key`, `spring.ai.openai.organization-id` и `spring.ai.openai.project-id`.
Свойства `spring.ai.openai.moderation.base-url`, `spring.ai.openai.moderation.api-key`, `spring.ai.openai.moderation.organization-id` и `spring.ai.openai.moderation.project-id`, если они установлены, имеют приоритет над общими свойствами.
Это полезно, если вы хотите использовать разные учетные записи OpenAI для различных моделей и различных конечных точек моделей.

> **Совет:** Все свойства с префиксом `spring.ai.openai.moderation.options` могут быть переопределены во время выполнения.

### Пользовательские пути API

Для совместимых с OpenAI API (таких как LocalAI, пользовательские прокси или другие совместимые с OpenAI сервисы), которые используют различные пути конечных точек, вы можете настроить путь модерации:

```properties
spring.ai.openai.moderation.moderation-path=/custom/path/to/moderations
```

Это особенно полезно, когда:

- Используются API-шлюзы или прокси, которые изменяют стандартные пути OpenAI
- Работа с совместимыми с OpenAI сервисами, которые реализуют различные структуры URL
- Тестирование с использованием макетных конечных точек с пользовательскими путями
- Развертывание в средах с требованиями к маршрутизации на основе путей

## Опции времени выполнения```markdown
Класс OpenAiModerationOptions предоставляет параметры, которые используются при выполнении запроса на модерацию. При запуске используются параметры, указанные в spring.ai.openai.moderation, но вы можете переопределить их во время выполнения.

Например:

```java
OpenAiModerationOptions moderationOptions = OpenAiModerationOptions.builder()
    .model("omni-moderation-latest")
    .build();

ModerationPrompt moderationPrompt = new ModerationPrompt("Текст для модерации", this.moderationOptions);
ModerationResponse response = openAiModerationModel.call(this.moderationPrompt);

// Доступ к результатам модерации
Moderation moderation = moderationResponse.getResult().getOutput();

// Вывод общей информации
System.out.println("ID модерации: " + moderation.getId());
System.out.println("Используемая модель: " + moderation.getModel());

// Доступ к результатам модерации (обычно их только один, но это список)
for (ModerationResult result : moderation.getResults()) {
    System.out.println("\nРезультат модерации:");
    System.out.println("Помечено: " + result.isFlagged());

    // Доступ к категориям
    Categories categories = this.result.getCategories();
    System.out.println("\nКатегории:");
    System.out.println("Сексуальный: " + categories.isSexual());
    System.out.println("Ненависть: " + categories.isHate());
    System.out.println("Домогательство: " + categories.isHarassment());
    System.out.println("Самоповреждение: " + categories.isSelfHarm());
    System.out.println("Сексуальный/Несовершеннолетние: " + categories.isSexualMinors());
    System.out.println("Ненависть/Угроза: " + categories.isHateThreatening());
    System.out.println("Насилие/Графика: " + categories.isViolenceGraphic());
    System.out.println("Самоповреждение/Намерение: " + categories.isSelfHarmIntent());
    System.out.println("Самоповреждение/Инструкции: " + categories.isSelfHarmInstructions());
    System.out.println("Домогательство/Угроза: " + categories.isHarassmentThreatening());
    System.out.println("Насилие: " + categories.isViolence());

    // Доступ к баллам по категориям
    CategoryScores scores = this.result.getCategoryScores();
    System.out.println("\nБаллы по категориям:");
    System.out.println("Сексуальный: " + scores.getSexual());
    System.out.println("Ненависть: " + scores.getHate());
    System.out.println("Домогательство: " + scores.getHarassment());
    System.out.println("Самоповреждение: " + scores.getSelfHarm());
    System.out.println("Сексуальный/Несовершеннолетние: " + scores.getSexualMinors());
    System.out.println("Ненависть/Угроза: " + scores.getHateThreatening());
    System.out.println("Насилие/Графика: " + scores.getViolenceGraphic());
    System.out.println("Самоповреждение/Намерение: " + scores.getSelfHarmIntent());
    System.out.println("Самоповреждение/Инструкции: " + scores.getSelfHarmInstructions());
    System.out.println("Домогательство/Угроза: " + scores.getHarassmentThreatening());
    System.out.println("Насилие: " + scores.getViolence());
}
```

## Ручная конфигурация

Добавьте зависимость `spring-ai-openai` в файл `pom.xml` вашего проекта Maven:

```xml
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-openai</artifactId>
</dependency>
```

или в файл сборки Gradle `build.gradle`:

```groovy
dependencies {
    implementation 'org.springframework.ai:spring-ai-openai'
}
```

> **Совет:** Обратитесь к разделу xref:getting-started.adoc#dependency-management[Управление зависимостями], чтобы добавить Spring AI BOM в ваш файл сборки.

Далее создайте OpenAiModerationModel:

```java
OpenAiModerationApi openAiModerationApi = new OpenAiModerationApi(System.getenv("OPENAI_API_KEY"));

OpenAiModerationModel openAiModerationModel = new OpenAiModerationModel(this.openAiModerationApi);

OpenAiModerationOptions moderationOptions = OpenAiModerationOptions.builder()
    .model("omni-moderation-latest")
    .build();

ModerationPrompt moderationPrompt = new ModerationPrompt("Текст для модерации", this.moderationOptions);
ModerationResponse response = this.openAiModerationModel.call(this.moderationPrompt);
```## Пример кода
Тест `OpenAiModerationModelIT` предоставляет общие примеры использования библиотеки. Вы можете обратиться к этому тесту для получения более подробных примеров использования.
