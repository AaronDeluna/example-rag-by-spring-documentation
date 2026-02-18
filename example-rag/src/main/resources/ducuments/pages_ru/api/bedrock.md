# Amazon Bedrock

[NOTE]
====
Согласно рекомендациям Bedrock, Spring AI перешел на использование API Converse от Amazon Bedrock для всех реализаций чатов в Spring AI. xref:api/chat/bedrock-converse.adoc[API Bedrock Converse] имеет следующие ключевые преимущества:

- Унифицированный интерфейс: напишите код один раз и используйте его с любой поддерживаемой моделью Amazon Bedrock
- Гибкость модели: без проблем переключайтесь между различными моделями разговоров без изменения кода
- Расширенная функциональность: поддержка параметров, специфичных для модели, через специальные структуры
- Поддержка инструментов: нативная интеграция с возможностями вызова функций и использования инструментов
- Мультимодальные возможности: встроенная поддержка визуальных и других мультимодальных функций
- Защита на будущее: соответствует рекомендуемым лучшим практикам Amazon Bedrock

API Converse не поддерживает операции встраивания, поэтому они останутся в текущем API, а функциональность модели встраивания в существующем `InvokeModel API` будет поддерживаться
====


[Amazon Bedrock](https://docs.aws.amazon.com/bedrock/latest/userguide/what-is-bedrock.html) — это управляемый сервис, который предоставляет базовые модели от различных поставщиков ИИ, доступные через унифицированный API.

Spring AI поддерживает https://docs.aws.amazon.com/bedrock/latest/userguide/model-ids-arns.html[модели ИИ для встраивания], доступные через Amazon Bedrock, реализуя интерфейс Spring `EmbeddingModel`.

Кроме того, Spring AI предоставляет автоматические конфигурации Spring и стартеры Boot для всех клиентов, что упрощает начальную настройку и конфигурацию для моделей Bedrock.

## Начало работы

Есть несколько шагов, чтобы начать

- Добавьте стартер Spring Boot для Bedrock в ваш проект.
- Получите учетные данные AWS: если у вас еще нет учетной записи AWS и настроенного AWS CLI, это видео поможет вам настроить его: [Настройка AWS CLI и SDK менее чем за 4 минуты!](https://youtu.be/gswVHTrRX8I?si=buaY7aeI0l3-bBVb). Вы должны получить свои ключи доступа и безопасности.
- Включите модели для использования: перейдите в [Amazon Bedrock](https://us-east-1.console.aws.amazon.com/bedrock/home) и в меню [Доступ к моделям](https://us-east-1.console.aws.amazon.com/bedrock/home?region=us-east-1#/modelaccess) слева настройте доступ к моделям, которые вы собираетесь использовать.

### Зависимости проекта

Затем добавьте зависимость Spring Boot Starter в файл сборки Maven `pom.xml` вашего проекта:

```xml
<dependency>
 <artifactId>spring-ai-starter-model-bedrock</artifactId>
 <groupId>org.springframework.ai</groupId>
</dependency>
```

или в файл сборки Gradle `build.gradle`.

```groovy
dependencies {
    implementation 'org.springframework.ai:spring-ai-starter-model-bedrock'
}
```

> **Совет:** Обратитесь к разделу xref:getting-started.adoc#dependency-management[Управление зависимостями], чтобы добавить Spring AI BOM в ваш файл сборки.

### Подключение к AWS Bedrock

Используйте `BedrockAwsConnectionProperties` для настройки учетных данных AWS и региона:

```shell
spring.ai.bedrock.aws.region=us-east-1

spring.ai.bedrock.aws.access-key=YOUR_ACCESS_KEY
spring.ai.bedrock.aws.secret-key=YOUR_SECRET_KEY

spring.ai.bedrock.aws.profile.name=YOUR_PROFILE_NAME
spring.ai.bedrock.aws.profile.credentials-path=YOUR_CREDENTIALS_PATH
spring.ai.bedrock.aws.profile.configuration-path=YOUR_CONFIGURATION_PATH

spring.ai.bedrock.aws.timeout=10m
```

Свойство `region` является обязательным.

Учетные данные AWS разрешаются в следующем порядке:

1. Свойства Spring-AI Bedrock `spring.ai.bedrock.aws.access-key` и `spring.ai.bedrock.aws.secret-key`.
2. Свойство Spring-AI Bedrock `spring.ai.bedrock.aws.profile.name`. Если `spring.ai.bedrock.aws.profile.credentials-path` и `spring.ai.bedrock.aws.profile.configuration-path` не указаны, Spring AI использует стандартные общие файлы AWS: `~/.aws/credentials` для учетных данных и `~/.aws/config` для конфигурации.
3. Свойства системы Java - `aws.accessKeyId` и `aws.secretAccessKey`.
4. Переменные окружения - `AWS_ACCESS_KEY_ID` и `AWS_SECRET_ACCESS_KEY`.
5. Учетные данные токена веб-идентификации из свойств системы или переменных окружения.
6. Файл профилей учетных данных в стандартном местоположении (`~/.aws/credentials`), общий для всех AWS SDK и AWS CLI.
7. Учетные данные, предоставленные через сервис контейнеров Amazon EC2, если установлена переменная окружения `AWS_CONTAINER_CREDENTIALS_RELATIVE_URI` и менеджер безопасности имеет разрешение на доступ к переменной.
8. Учетные данные профиля экземпляра, предоставленные через сервис метаданных Amazon EC2 или установленные переменные окружения `AWS_ACCESS_KEY_ID` и `AWS_SECRET_ACCESS_KEY`.

Регион AWS разрешается в следующем порядке:

1. Свойство Spring-AI Bedrock `spring.ai.bedrock.aws.region`.
2. Свойства системы Java - `aws.region`.
3. Переменные окружения - `AWS_REGION`.
4. Файл профилей учетных данных в стандартном местоположении (`~/.aws/credentials`), общий для всех AWS SDK и AWS CLI.
5. Регион профиля экземпляра, предоставленный через сервис метаданных Amazon EC2.

В дополнение к стандартной конфигурации свойств учетных данных и региона Spring-AI Bedrock, Spring-AI предоставляет поддержку для пользовательских бинов `AwsCredentialsProvider` и `AwsRegionProvider`.

> **Примечание:** Например, используя Spring-AI и https://spring.io/projects/spring-cloud-aws[Spring Cloud для Amazon Web Services] одновременно. Spring-AI совместим с конфигурацией учетных данных Spring Cloud для Amazon Web Services.

### Включите выбранную модель Bedrock

> **Примечание:** По умолчанию все модели отключены. Вы должны явно включить выбранные модели Bedrock, используя свойство `spring.ai.bedrock.<model>.embedding.enabled=true`.

Вот поддерживаемые `<model>`:

| Модель |
| --- |
| cohere |
| titan (пока без поддержки пакетной обработки) |

Например, чтобы включить модель встраивания Bedrock Cohere, вам нужно установить `spring.ai.bedrock.cohere.embedding.enabled=true`.

Далее вы можете использовать свойства `spring.ai.bedrock.<model>.embedding.*` для настройки каждой модели, как указано.

Для получения дополнительной информации обратитесь к документации ниже для каждой поддерживаемой модели.

- xref:api/embeddings/bedrock-cohere-embedding.adoc[Spring AI Bedrock Cohere Embeddings]: `spring.ai.bedrock.cohere.embedding.enabled=true`
- xref:api/embeddings/bedrock-titan-embedding.adoc[Spring AI Bedrock Titan Embeddings]: `spring.ai.bedrock.titan.embedding.enabled=true`
